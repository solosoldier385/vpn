package com.letsvpn.pay.third.util;

import java.security.KeyFactory;
import java.security.NoSuchAlgorithmException;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.interfaces.RSAPrivateKey;
import java.security.interfaces.RSAPublicKey;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.PKCS8EncodedKeySpec;
import java.security.spec.X509EncodedKeySpec;
import java.util.Base64;

/**
 * RSA 工具类
 * @author jianLing
 *
 */
public class JLRSAUtil {


    private static final String ALGORITHM = "RSA";
    private static final String ALGORITHMS_SHA1WithRSA = "SHA1WithRSA";
    private static final String ALGORITHMS_SHA256WithRSA = "SHA256WithRSA";
    private static final String DEFAULT_CHARSET = "UTF-8";


    /**
     * 私钥签名
     * @throws InvalidKeySpecException
     * @throws Exception
     */
    public static String sign(String content, String privateKey, boolean isRsa2) {
        PrivateKey priKey = getPrivateKey(privateKey);
        try {
            java.security.Signature signature = java.security.Signature.getInstance(getAlgorithms(isRsa2));
            signature.initSign(priKey);
            signature.update(content.getBytes(DEFAULT_CHARSET));
            byte[] signed = signature.sign();
            return Base64.getEncoder().encodeToString(signed);
        }catch (Exception e) {
            throw new RuntimeException("私钥签名出现异常", e);
        }

    }

    /**
     * 获取私钥
     * @param privateKey
     * @return
     * @throws NoSuchAlgorithmException
     * @throws InvalidKeySpecException
     * @throws Exception
     */
    public static RSAPrivateKey getPrivateKey(String privateKey) {
        byte[] keyBytes = Base64.getDecoder().decode(privateKey);
        PKCS8EncodedKeySpec spec = new PKCS8EncodedKeySpec(keyBytes);
        KeyFactory keyFactory = null;
        try {
            keyFactory = KeyFactory.getInstance(ALGORITHM);
            return (RSAPrivateKey) keyFactory.generatePrivate(spec);
        } catch (Exception e) {
            throw new RuntimeException("获取私钥出现异常", e);
        }
    }

    private static String getAlgorithms(boolean isRsa2) {
        return isRsa2 ? ALGORITHMS_SHA256WithRSA : ALGORITHMS_SHA1WithRSA;
    }

    /**
     * 获取公钥
     * @param publicKey
     * @return
     * @throws Exception
     */
    public static RSAPublicKey getPublicKey(String publicKey) {
        byte[] keyBytes = Base64.getDecoder().decode(publicKey);
        X509EncodedKeySpec spec = new X509EncodedKeySpec(keyBytes);
        try {
            KeyFactory keyFactory = KeyFactory.getInstance(ALGORITHM);
            return (RSAPublicKey) keyFactory.generatePublic(spec);
        }catch (Exception e) {
            throw new RuntimeException("获取公钥出现异常", e);
        }
    }


    /**
     * 公钥验签
     */
    public static boolean verify(String content,String sign,String publicKey,boolean isRsa2) {
        PublicKey pubKey = getPublicKey(publicKey);
        try {
            java.security.Signature signature = java.security.Signature.getInstance(getAlgorithms(isRsa2));
            signature.initVerify(pubKey);
            signature.update(content.getBytes(DEFAULT_CHARSET));
            return signature.verify(Base64.getDecoder().decode(sign));
        }catch (Exception e) {
            throw new RuntimeException("公钥验签出现异常", e);
        }

    }
}
