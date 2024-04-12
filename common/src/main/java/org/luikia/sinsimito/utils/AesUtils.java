package org.luikia.sinsimito.utils;

import javax.crypto.Cipher;
import javax.crypto.spec.SecretKeySpec;

public class AesUtils {

    public static byte[] decrypt(byte[] sSrc, byte[] sKey) throws Exception {
        if (sKey == null) {
            throw new Exception("Key is null");
        }
        if (sKey.length != 16) {
            throw new Exception("Key length is not 16");
        }
        SecretKeySpec skeySpec = new SecretKeySpec(sKey, "AES");
        Cipher cipher = Cipher.getInstance("AES/ECB/PKCS5Padding");
        cipher.init(Cipher.DECRYPT_MODE, skeySpec);
        byte[] original = cipher.doFinal(sSrc);
        return original;
    }

}
