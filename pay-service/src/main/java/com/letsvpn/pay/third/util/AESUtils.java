package com.letsvpn.pay.third.util;

import lombok.SneakyThrows;

import javax.crypto.Cipher;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;
public class AESUtils {

    /**
     * Convert Bytes to HEX
     */
    public static String bytesToHex(byte[] bytes) {
        char[] hexArray = "0123456789ABCDEF".toCharArray();
        char[] hexChars = new char[bytes.length * 2];
        for (int j = 0; j < bytes.length; j++) {
            int v = bytes[j] & 0xFF;
            hexChars[j * 2] = hexArray[v >>> 4];
            hexChars[j * 2 + 1] = hexArray[v & 0x0F];
        }
        return new String(hexChars);
    }

    /**
     * AES加密 - CBC/PKCS5Padding
     */
    @SneakyThrows
    public static String encrypt(String data, String strKey) {
        strKey = strKey.substring(0,16); //必须要求是16位密钥
        Cipher ciper = Cipher.getInstance("AES/CBC/PKCS5Padding");
        SecretKeySpec key = new SecretKeySpec(strKey.getBytes(), "AES");
        IvParameterSpec iv = new IvParameterSpec(strKey.getBytes());
        ciper.init(Cipher.ENCRYPT_MODE, key, iv);
        String encrypted = bytesToHex(ciper.doFinal(data.getBytes()));
        return encrypted;
    }

    /**
     * Convert HEX to Bytes
     */
    public static byte[] hexToBytes(String s) {
        int len = s.length();
        byte[] data = new byte[len / 2];
        for (int i = 0; i < len; i += 2) {
            data[i / 2] = (byte) ((Character.digit(s.charAt(i), 16) << 4) + Character.digit(s.charAt(i + 1), 16));
        }
        return data;
    }

    /**
     * AES解密 - CBC/PKCS5Padding
     */
    @SneakyThrows
    public static String decrypt(String data, String strKey) {
        strKey = strKey.substring(0, 16); //必须要求是16位密钥
        Cipher ciper = Cipher.getInstance("AES/CBC/PKCS5Padding");
        SecretKeySpec key = new SecretKeySpec(strKey.getBytes(), "AES");
        IvParameterSpec iv = new IvParameterSpec(strKey.getBytes());
        ciper.init(Cipher.DECRYPT_MODE, key, iv);
        String decrypted = new String(ciper.doFinal(hexToBytes(data)));
        return decrypted;
    }
}
