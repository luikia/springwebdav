package org.luikia.sinsimito.plugin;

import lombok.extern.slf4j.Slf4j;
import org.luikia.sinsimito.utils.AesUtils;
import org.luikia.sinsimito.utils.RsaUtils;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.nio.ByteBuffer;
import java.security.Key;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

@Slf4j
public class JarRsaSecureClassLoader extends JarByteArrayClassLoader {

    private final Key key;

    public JarRsaSecureClassLoader(List<InputStream> inputStreams, String key, ClassLoader parent) {
        super(inputStreams, parent);
        Key k = null;
        try {
            k = RsaUtils.convertToRasKey(key);
        } catch (Exception e) {
            log.error("rsa key parse error", e);
        }
        this.key = k;
    }

    public static JarRsaSecureClassLoader of(ClassLoader parent, String key, InputStream... inputStreams) {
        return new JarRsaSecureClassLoader(Arrays.asList(inputStreams), key, parent);
    }

    public static JarRsaSecureClassLoader of(ClassLoader parent, String key, List<byte[]> bytes) {
        List<InputStream> list = bytes.stream().map(ByteArrayInputStream::new).collect(Collectors.toList());
        return new JarRsaSecureClassLoader(list, key, parent);
    }

    @Override
    protected byte[] decode(byte[] data) {
        if (Objects.isNull(this.key)) {
            return super.decode(data);
        }
        try {
            int dataSize = data.length - 256;
            ByteBuffer bf = ByteBuffer.wrap(data);
            byte[] encryptPassword = new byte[256];
            bf.get(encryptPassword);
            byte[] password = RsaUtils.decrypt(encryptPassword, key);
            byte[] encryptData = new byte[dataSize];
            bf.get(encryptData);
            return AesUtils.decrypt(encryptData, password);
        } catch (Exception e) {
            throw new RuntimeException("class parse error");
        }
    }
}
