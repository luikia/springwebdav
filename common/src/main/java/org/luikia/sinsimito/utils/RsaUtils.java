package org.luikia.sinsimito.utils;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;

import javax.crypto.Cipher;
import java.security.Key;
import java.security.KeyFactory;
import java.security.spec.KeySpec;
import java.security.spec.PKCS8EncodedKeySpec;
import java.security.spec.X509EncodedKeySpec;
import java.util.Base64;
import java.util.function.Function;

@NoArgsConstructor(access = AccessLevel.PACKAGE)
public class RsaUtils {

    private static String ALGORITHM = "RSA";

    private static Function<byte[], KeySpec> PUBLIC_SPEC_FUNCTION = X509EncodedKeySpec::new;

    private static Function<byte[], KeySpec> PRIVATE_SPEC_FUNCTION = PKCS8EncodedKeySpec::new;

    public static Key convertToRasKey(String keyStr) throws Exception {
        byte[] key = Base64.getDecoder().decode(keyStr);
        return convertToRasKey(key);
    }

    private static Key convertToRasKey(byte[] key) throws Exception {
        KeyFactory kf = KeyFactory.getInstance(ALGORITHM);
        return kf.generatePublic(PUBLIC_SPEC_FUNCTION.apply(key));
    }

    public static byte[] decrypt(byte[] data, Key key) throws Exception {
        Cipher cipher = Cipher.getInstance(ALGORITHM);
        cipher.init(Cipher.DECRYPT_MODE, key);
        return cipher.doFinal(data);
    }

}
