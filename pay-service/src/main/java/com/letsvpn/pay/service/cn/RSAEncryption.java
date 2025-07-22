package com.letsvpn.pay.service.cn;

import java.io.InputStream;
import java.security.KeyFactory;
import java.security.PublicKey;
import java.security.spec.X509EncodedKeySpec;
import javax.crypto.Cipher;
import java.util.Base64;

public class RSAEncryption {

    // 从 resources 中加载 .der 公钥文件
    public static PublicKey getPublicKeyFromDer(String filenameInResources) throws Exception {
        InputStream inputStream = RSAEncryption.class.getClassLoader().getResourceAsStream(filenameInResources);
        if (inputStream == null) {
            throw new RuntimeException("未找到 DER 文件: " + filenameInResources);
        }

        byte[] keyBytes = inputStream.readAllBytes(); // Java 9+，如用 Java 8，请用 ByteArrayOutputStream
        X509EncodedKeySpec keySpec = new X509EncodedKeySpec(keyBytes);
        KeyFactory keyFactory = KeyFactory.getInstance("RSA");
        return keyFactory.generatePublic(keySpec);
    }

    // 执行 RSA 加密
    public static String encrypt(String plainText, PublicKey publicKey) throws Exception {
        Cipher cipher = Cipher.getInstance("RSA/ECB/PKCS1Padding");
        cipher.init(Cipher.ENCRYPT_MODE, publicKey);
        byte[] encryptedBytes = cipher.doFinal(plainText.getBytes("UTF-8"));
        return Base64.getEncoder().encodeToString(encryptedBytes);
    }

    // 主函数示例
    public static void main(String[] args) {
        try {
            // 构造明文串，例如: 923451234567:12345 或 1234@923451234567:12345
            String msisdn = "923319154345";
            String pin = "23980";
            String payload = msisdn + ":" + pin; // 或 "1234@" + msisdn + ":" + pin
            String a = "923319154345~2025-07-03 23:29:41.0";
            // 加载公钥并加密
            PublicKey publicKey = getPublicKeyFromDer("subgateway.der");
            String encryptedPayload = encrypt(a, publicKey);

            // 打印最终 LoginPayload
            System.out.println("Encrypted LoginPayload:");
            System.out.println(encryptedPayload);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
