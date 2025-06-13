package com.letsvpn.pay.third.util;

import org.apache.commons.codec.binary.Base64;
import org.apache.commons.lang3.StringUtils;

import javax.crypto.Cipher;
import java.io.ByteArrayOutputStream;
import java.security.KeyFactory;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.Signature;
import java.security.spec.PKCS8EncodedKeySpec;
import java.security.spec.X509EncodedKeySpec;

/**
 * 获取RSA公私钥 地址：http://web.chacuo.net/netrsakeypair
 * Java 版本 填写 （ 生成密钥位数：1024  密钥格式： PKCS#8   证书密码：不用填 ）
 * 非Java 版本 填写 （ 生成密钥位数：1024  密钥格式： PKCS#1   证书密码：不用填 ）
 */
public class RSAUtil {

    //加密长度
    private static final int MAX_ENCRYPT_BLOCK = 117;

    //解密长度
    private static final int MAX_DECRYPT_BLOCK = 128;

    /**
     * 公钥 - 加密
     */
    public static String Encrypt(String content, String publicKey){
        try {
            if(StringUtils.isBlank(content)){
                return null;
            }
            publicKey = publicKey.replaceAll("\\s*|\t|\r|\n", StringUtils.EMPTY);
            byte[] keyBytes = Base64.decodeBase64(publicKey.getBytes());
            X509EncodedKeySpec bobPubKeySpec = new X509EncodedKeySpec(keyBytes);
            KeyFactory keyFactory = KeyFactory.getInstance("RSA");
            PublicKey key = keyFactory.generatePublic(bobPubKeySpec);

            Cipher cipher = Cipher.getInstance("RSA");
            cipher.init(Cipher.ENCRYPT_MODE, key);
            int inputLen = content.getBytes().length;
            ByteArrayOutputStream out = new ByteArrayOutputStream();
            int offset = 0;
            byte[] cache;
            int i = 0;
            // 对数据分段加密
            while (inputLen - offset > 0) {
                if (inputLen - offset > MAX_ENCRYPT_BLOCK) {
                    cache = cipher.doFinal(content.getBytes(), offset, MAX_ENCRYPT_BLOCK);
                } else {
                    cache = cipher.doFinal(content.getBytes(), offset, inputLen - offset);
                }
                out.write(cache, 0, cache.length);
                i++;
                offset = i * MAX_ENCRYPT_BLOCK;
            }
            byte[] encryptedData = out.toByteArray();
            out.close();
            // 获取加密内容使用base64进行编码,并以UTF-8为标准转化成字符串
            // 加密后的字符串
            return new String(Base64.encodeBase64String(encryptedData));
        }catch (Exception e){
            return null;
        }
    }

    /**
     * 私钥 - 解密
     */
    public static String Decrypt(String content, String privateKey){
        try {
            if(StringUtils.isBlank(content)){
                return null;
            }
            privateKey = privateKey.replaceAll("\\s*|\t|\r|\n", StringUtils.EMPTY);
            byte[] keyBytes = Base64.decodeBase64(privateKey.getBytes("UTF-8"));
            PKCS8EncodedKeySpec spec = new PKCS8EncodedKeySpec(keyBytes);
            KeyFactory keyFactory = KeyFactory.getInstance("RSA");
            PrivateKey key = keyFactory.generatePrivate(spec);

            Cipher cipher = Cipher.getInstance("RSA");
            cipher.init(Cipher.DECRYPT_MODE, key);
            byte[] dataBytes = Base64.decodeBase64(content);
            int inputLen = dataBytes.length;
            ByteArrayOutputStream out = new ByteArrayOutputStream();
            int offset = 0;
            byte[] cache;
            int i = 0;
            // 对数据分段解密
            while (inputLen - offset > 0) {
                if (inputLen - offset > MAX_DECRYPT_BLOCK) {
                    cache = cipher.doFinal(dataBytes, offset, MAX_DECRYPT_BLOCK);
                } else {
                    cache = cipher.doFinal(dataBytes, offset, inputLen - offset);
                }
                out.write(cache, 0, cache.length);
                i++;
                offset = i * MAX_DECRYPT_BLOCK;
            }
            byte[] decryptedData = out.toByteArray();
            out.close();
            // 解密后的内容
            return new String(decryptedData, "UTF-8");
        }catch (Exception e){
            return null;
        }
    }



    /**
     * 私钥 - 签名
     */
    public static String Sign(String signStr, String privateKey){
        try {
            if(StringUtils.isBlank(signStr)){
                return null;
            }
            privateKey = privateKey.replaceAll("\\s*|\t|\r|\n", StringUtils.EMPTY);
            byte[] keyBytes = Base64.decodeBase64(privateKey.getBytes("UTF-8"));
            PKCS8EncodedKeySpec spec = new PKCS8EncodedKeySpec(keyBytes);
            KeyFactory keyFactory = KeyFactory.getInstance("RSA");
            PrivateKey signKey = keyFactory.generatePrivate(spec);
            Signature signature = Signature.getInstance("SHA1withRSA");
            signature.initSign(signKey);
            signature.update(signStr.getBytes());
            return byteArray2HexString(signature.sign());
        }catch (Exception e){
            return null;
        }
    }


    /**
     * 公钥 - 验证签名
     */
    public static boolean Verify(String signData, String signStr, String publicKey){
        try {
            if(StringUtils.isBlank(signStr) || StringUtils.isBlank(signData)){
                return false;
            }
            publicKey = publicKey.replaceAll("\\s*|\t|\r|\n", StringUtils.EMPTY);
            byte[] keyBytes = Base64.decodeBase64(publicKey.getBytes("UTF-8"));
            X509EncodedKeySpec bobPubKeySpec = new X509EncodedKeySpec(keyBytes);
            KeyFactory keyFactory = KeyFactory.getInstance("RSA");
            PublicKey verifyKey = keyFactory.generatePublic(bobPubKeySpec);
            Signature verifier = Signature.getInstance("SHA1withRSA");
            verifier.initVerify(verifyKey);
            verifier.update(signData.getBytes());
            return verifier.verify(hexString2ByteArray(signStr));
        }catch (Exception e){
            return false;
        }
    }

    private static byte[] hexString2ByteArray(String hexStr){
        if (hexStr == null)
            return null;
        if (hexStr.length() % 2 != 0)
            return null;
        byte data[] = new byte[hexStr.length() / 2];
        for (int i = 0; i < hexStr.length() / 2; i++){
            char hc = hexStr.charAt(2 * i);
            char lc = hexStr.charAt(2 * i + 1);
            byte hb = hexChar2Byte(hc);
            byte lb = hexChar2Byte(lc);
            if (hb < 0 || lb < 0)
                return null;
            int n = hb << 4;
            data[i] = (byte)(n + lb);
        }
        return data;
    }

    private static byte hexChar2Byte(char c){
        if (c >= '0' && c <= '9')
            return (byte)(c - 48);
        if (c >= 'a' && c <= 'f')
            return (byte)((c - 97) + 10);
        if (c >= 'A' && c <= 'F')
            return (byte)((c - 65) + 10);
        else
            return -1;
    }

    private static String byteArray2HexString(byte arr[]){
        StringBuilder sbd = new StringBuilder();
        for (byte b:arr) {
            String tmp = Integer.toHexString(0xff & b);
            if (tmp.length() < 2)
                tmp = (new StringBuilder()).append("0").append(tmp).toString();
            sbd.append(tmp);
        }
        return sbd.toString();
    }


    /**
     * @param args
     * @throws Exception
     */
    public static void main(String[] args) throws Exception {

        // 非 Java 版 RSA 私钥  密钥格式： PKCS#1
        // Java 版 RSA 私钥  密钥格式： PKCS#8
        String prk8 = "MIICdgIBADANBgkqhkiG9w0BAQEFAASCAmAwggJcAgEAAoGBAMDbkPbygYyJuXEeawT7uhJc2BS+AuYjt5H7HH/ERJtuIxGkFO1i3URwBg1jTyAqIFDXa+yFImmCcSbJDibkgxujaE4ohRoHPT43qJ5BuYTJpQ8AA7GVggTzCylIDI/4ZZrQ//yUPd8d0v6fs2qnUeWEyr55AvAPrbY5gR0QQfmXAgMBAAECgYAXeuP0XOQO4zmznvtymN83mxwnslaSBGk2GbeHirK7pbYZULvHhxDfYzApcxzQErDqhr+6BR+CbwItsa7KZ2navjgu0qKkKINjOPqeXK+fI/1svIpU9NzQTs251LOxRlEV/mVe0vFy63ShlzWVyNobo8tbg6kPr2IKda1NeimXIQJBAPSpN3GrHupyHQwgw7kfqxLUg8i8JTczNh/DogRBttJi9ENKSXCkJchmThsNykNDnqEcs3lq5FefJ5Wm1BuRh6cCQQDJy7qCkkeAAzBuLLk4bEvh2n+qryoWkyyGBP9Xor5j0zhcYndbKV6SC8svbPdLAF5gzvOTjEKBK2fsZxkBzzyRAkEA7hCCDK7vxD0JmrV+/XGdKxNysC6zH8VLExlvWyj+VjVHbPqp/1saEKfLtHN0roLrpeWhlbG3QrNw9yOAHNs83QJAXyUDh1TZCW53gzrrCcNl89NFpoB4Y7R4paftjLBa/E42oLh8disHP+z3nCDQaDiARzzMc6EKKkUA9uGVVNkVQQJAXiTSUk54iWAyq2JZ7VtfPiznp4FupzpL59A2RCNT7VjLLvt9QNN1y363gdMO/a/1vJoHeNqAEcCgvUsa7OTKXw==";

        String puk = "MIGfMA0GCSqGSIb3DQEBAQUAA4GNADCBiQKBgQDA25D28oGMiblxHmsE+7oSXNgUvgLmI7eR+xx/xESbbiMRpBTtYt1EcAYNY08gKiBQ12vshSJpgnEmyQ4m5IMbo2hOKIUaBz0+N6ieQbmEyaUPAAOxlYIE8wspSAyP+GWa0P/8lD3fHdL+n7Nqp1HlhMq+eQLwD622OYEdEEH5lwIDAQAB";

        //签名内容
        String content = "a1234plmoooooooooooooooookpokpbiuho8798kj";

        String enStr = Encrypt(content, puk);
        System.out.println("加密： "+enStr);

        String deStr = Decrypt(enStr, prk8);
        System.out.println("解密： "+deStr);

        String sign = Sign(content, prk8).toLowerCase();
        System.out.println("签名： "+sign);

        boolean ver = Verify(content,sign,puk);
        System.out.println("验证： "+ver);


        String a = "kTopumPxja12i3kNqEZuk3Q6pUsLqL2L9wg3OruPJgGF7hFMppc2Qy5voVPtE5me0kqtUqFmc3NjL7n+6Ok8wWG22eGu32Co12DcBKIW7tkKOL288LmWIYbO2J6OPBDG4JyvRRpOBUt4Ro4piMA54oPpC9UIVOlnOlhWn73GMaWp7UE3O++wR1JPqX3RusfBqX0rebYjBcHOaA4aDiYIYfoV+2+943zgNNOISGdH4ukhWD3lAN3ZiI9IRTbrQfAo7wHOY/P0iyH2Natflp2pmE8iG75piyUH8vRYuRedds5vlvifWz26WHpx8oLCiCYkio3ixLaG0G9W8ljUKkZH2lzgD/JuHgCoZXShjDia1+JwciE6vlLvCqbWJuxDoYbPU6LNnVL1EYv4Ex366AVnEAc/Yfw09dCWvY+5hG8tZfZFbyy5Sr8Zjfw0aB61ltYo9m4cXb/xF1t/sqVCNUdJWwXIDw08H0HOuc/V4h4MUzf6WYrh08xoaYrvSvTqpMUGsD2zjiumaThIsvwDzFy/2Z9TyH87MArsGRX6sEpJ2jqxraTzPGoXo5ejsQHRBr6gfotpdAoHyoX53wO/XyDVyc+Gat7H7z9mWl/59vQQo9RJpYPt3mYKHQz/Ai2EsU+VCk7w8SS5E4RzePQLwQ04Rn56o8Y1gJwXs7vThPau8bA=";

        String decypred = Decrypt(a, prk8);

        System.out.println("解密出来后"+decypred);


    }
}

