package com.letsvpn.pay.service.cn;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.security.KeyFactory;
import java.security.PublicKey;
import java.security.spec.X509EncodedKeySpec;
import javax.crypto.Cipher;
import java.util.Base64;

public class LoginPayloadGenerator {

    // 读取资源文件中的 PEM 公钥
    public static PublicKey loadPublicKeyFromResources(String fileName) throws Exception {
        InputStream inputStream = LoginPayloadGenerator.class.getClassLoader().getResourceAsStream(fileName);
        if (inputStream == null) {
            throw new IllegalArgumentException("找不到资源文件: " + fileName);
        }

        BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream));
        StringBuilder pemBuilder = new StringBuilder();
        String line;
        while ((line = reader.readLine()) != null) {
            if (line.contains("BEGIN PUBLIC KEY") || line.contains("END PUBLIC KEY")) {
                continue;
            }
            pemBuilder.append(line.trim());
        }

        byte[] keyBytes = Base64.getDecoder().decode(pemBuilder.toString());
        X509EncodedKeySpec keySpec = new X509EncodedKeySpec(keyBytes);
        KeyFactory kf = KeyFactory.getInstance("RSA");
        return kf.generatePublic(keySpec);
    }

    // 加密函数
    public static String encrypt(String plainText, PublicKey publicKey) throws Exception {
        Cipher cipher = Cipher.getInstance("RSA/ECB/PKCS1Padding"); // 常见填充方式
        cipher.init(Cipher.ENCRYPT_MODE, publicKey);
        byte[] encryptedBytes = cipher.doFinal(plainText.getBytes("UTF-8"));
        return Base64.getEncoder().encodeToString(encryptedBytes);
    }

    // 主函数
    public static void main(String[] args) throws Exception {
        String msisdn = "923319154345";
        String pin = "23980";
        String accountType = "subscriber"; // 可为 "subscriber" 或 "retailer"

        String dataToEncrypt;
        if ("subscriber".equalsIgnoreCase(accountType)) {
            dataToEncrypt = msisdn + ":" + pin;
        } else {
            dataToEncrypt = "1234@" + msisdn + ":" + pin;
        }

        PublicKey publicKey = loadPublicKeyFromResources("subgateway.pem");
        String encryptedPayload = encrypt(dataToEncrypt, publicKey);

        System.out.println("LoginPayload 加密结果为:");
        System.out.println(encryptedPayload);
    }
}
