/******************************************************************
 *
 *    Powered By tianxia-online.
 *
 *    Copyright (c) 2018-2020 Digital Telemedia 天下科技
 *    http://www.d-telemedia.com/
 *
 *    Package:     com.tx.platform.utils
 *
 *    Filename:    SHA256WithRSAUtils.java
 *
 *    Description: TODO(用一句话描述该文件做什么)
 *
 *    Copyright:   Copyright (c) 2018-2020
 *
 *    Company:     天下科技
 *
 *    @author:     Finlay
 *
 *    @version:    1.0.0
 *
 *    Create at:   2019年05月17日 21:50
 *
 *    Revision:
 *
 *    2019/5/17 21:50
 *        - first revision
 *
 *****************************************************************/
package com.letsvpn.pay.third.util;

import lombok.extern.slf4j.Slf4j;
import org.apache.commons.io.IOUtils;

import javax.crypto.Cipher;
import java.io.*;
import java.security.*;
import java.security.spec.PKCS8EncodedKeySpec;
import java.security.spec.X509EncodedKeySpec;
import java.util.Base64;
import java.util.HashMap;
import java.util.Map;

//import org.apache.commons.io.IOUtils;

/**
 *  * @ClassName SHA256WithRSAUtils
 *  * @Description TODO(SHA256WithRSA 签名工具类)
 *  * @Author one
 *  * @Date 2019年11月10日 21:50
 *  * @Version 1.0.0
 *  
 **/
@Slf4j
public class SHA256WithRSAUtils {

    public static final String CHARSET = "UTF-8";
    //密钥算法
    public static final String ALGORITHM_RSA = "RSA";
    //RSA 签名算法
    public static final String ALGORITHM_RSA_SIGN = "SHA256WithRSA";
    public static final int ALGORITHM_RSA_PRIVATE_KEY_LENGTH = 2048;

    private static final int    DEFAULT_BUFFER_SIZE       = 8192;

    private SHA256WithRSAUtils() {
    }

    /**
     * 初始化RSA算法密钥对
     *
     * @param keysize RSA1024已经不安全了,建议2048
     * @return 经过Base64编码后的公私钥Map, 键名分别为publicKey和privateKey
     */
    public static Map<String, String> initRSAKey(int keysize) {
        if (keysize != ALGORITHM_RSA_PRIVATE_KEY_LENGTH) {
            throw new IllegalArgumentException("RSA1024已经不安全了,请使用" + ALGORITHM_RSA_PRIVATE_KEY_LENGTH + "初始化RSA密钥对");
        }
        //为RSA算法创建一个KeyPairGenerator对象
        KeyPairGenerator kpg;
        try {
            kpg = KeyPairGenerator.getInstance(ALGORITHM_RSA);
        } catch (NoSuchAlgorithmException e) {
            throw new IllegalArgumentException("No such algorithm-->[" + ALGORITHM_RSA + "]");
        }
        //初始化KeyPairGenerator对象,不要被initialize()源码表面上欺骗,其实这里声明的size是生效的
        kpg.initialize(ALGORITHM_RSA_PRIVATE_KEY_LENGTH);
        //生成密匙对
        KeyPair keyPair = kpg.generateKeyPair();
        //得到公钥
        Key publicKey = keyPair.getPublic();
        String publicKeyStr = Base64.getEncoder().encodeToString(publicKey.getEncoded());
        //得到私钥
        Key privateKey = keyPair.getPrivate();
        String privateKeyStr = Base64.getEncoder().encodeToString(privateKey.getEncoded());
        Map<String, String> keyPairMap = new HashMap<String, String>();
        keyPairMap.put("publicKey", publicKeyStr);
        keyPairMap.put("privateKey", privateKeyStr);
        return keyPairMap;
    }

    /**
     * RSA算法公钥加密数据
     *
     * @param data 待加密的明文字符串
     * @param key  RSA公钥字符串
     * @return RSA公钥加密后的经过Base64编码的密文字符串
     */
    public static String buildRSAEncryptByPublicKey(String data, String key) {
        try {
            //通过X509编码的Key指令获得公钥对象
            X509EncodedKeySpec x509KeySpec = new X509EncodedKeySpec(Base64.getDecoder().decode(key));
            KeyFactory keyFactory = KeyFactory.getInstance(ALGORITHM_RSA);
            Key publicKey = keyFactory.generatePublic(x509KeySpec);
            Cipher cipher = Cipher.getInstance(keyFactory.getAlgorithm());
            cipher.init(Cipher.ENCRYPT_MODE, publicKey);
            return Base64.getEncoder().encodeToString(rsaSplitCodec(cipher, Cipher.ENCRYPT_MODE, data.getBytes(CHARSET)));
        } catch (Exception e) {
            throw new RuntimeException("加密字符串[" + data + "]时遇到异常", e);
        }
    }
    /**
     * RSA算法公钥解密数据
     *
     * @param data 待解密的经过Base64编码的密文字符串
     * @param key  RSA公钥字符串
     * @return RSA公钥解密后的明文字符串
     */
    public static String buildRSADecryptByPublicKey(String data, String key) {
        try {
            //通过X509编码的Key指令获得公钥对象
            X509EncodedKeySpec x509KeySpec = new X509EncodedKeySpec(Base64.getDecoder().decode(key));
            KeyFactory keyFactory = KeyFactory.getInstance(ALGORITHM_RSA);
            Key publicKey = keyFactory.generatePublic(x509KeySpec);
            Cipher cipher = Cipher.getInstance(keyFactory.getAlgorithm());
            cipher.init(Cipher.DECRYPT_MODE, publicKey);
            return new String(rsaSplitCodec(cipher, Cipher.DECRYPT_MODE, Base64.getDecoder().decode(data)), CHARSET);
        } catch (Exception e) {
            throw new RuntimeException("解密字符串[" + data + "]时遇到异常", e);
        }
    }
    /**
     * RSA算法私钥加密数据
     *
     * @param data 待加密的明文字符串
     * @param key  RSA私钥字符串
     * @return RSA私钥加密后的经过Base64编码的密文字符串
     */
    public static String buildRSAEncryptByPrivateKey(String data, String key) {
        try {
            //通过PKCS#8编码的Key指令获得私钥对象
            PKCS8EncodedKeySpec pkcs8KeySpec = new PKCS8EncodedKeySpec(Base64.getDecoder().decode(key));
            KeyFactory keyFactory = KeyFactory.getInstance(ALGORITHM_RSA);
            Key privateKey = keyFactory.generatePrivate(pkcs8KeySpec);
            Cipher cipher = Cipher.getInstance(keyFactory.getAlgorithm());
            cipher.init(Cipher.ENCRYPT_MODE, privateKey);
            return Base64.getEncoder().encodeToString(rsaSplitCodec(cipher, Cipher.ENCRYPT_MODE, data.getBytes(CHARSET)));
        } catch (Exception e) {
            throw new RuntimeException("加密字符串[" + data + "]时遇到异常", e);
        }
    }
    /**
     * RSA算法私钥解密数据
     * @param data 待解密的经过Base64编码的密文字符串
     * @param key  RSA私钥字符串
     * @return RSA私钥解密后的明文字符串
     */
    public static String buildRSADecryptByPrivateKey(String data, String key) {
        try {
            //通过PKCS#8编码的Key指令获得私钥对象
            PKCS8EncodedKeySpec pkcs8KeySpec = new PKCS8EncodedKeySpec(Base64.getDecoder().decode(key));
            KeyFactory keyFactory = KeyFactory.getInstance(ALGORITHM_RSA);
            Key privateKey = keyFactory.generatePrivate(pkcs8KeySpec);
            Cipher cipher = Cipher.getInstance(keyFactory.getAlgorithm());
            cipher.init(Cipher.DECRYPT_MODE, privateKey);
            return new String(rsaSplitCodec(cipher, Cipher.DECRYPT_MODE, Base64.getDecoder().decode(data)), CHARSET);
        } catch (Exception e) {
            throw new RuntimeException("解密字符串[" + data + "]时遇到异常", e);
        }
    }
    /**
     * RSA算法使用私钥对数据生成数字签名
     *
     * @param data 待签名的明文字符串
     * @param key  RSA私钥字符串
     * @return RSA私钥签名后的经过Base64编码的字符串
     */
    public static String buildRSASignByPrivateKey(String data, String key) {
        try {
            //通过PKCS#8编码的Key指令获得私钥对象
            PKCS8EncodedKeySpec pkcs8KeySpec = new PKCS8EncodedKeySpec(Base64.getDecoder().decode(key));
            log.info(pkcs8KeySpec+"------------");
            KeyFactory keyFactory = KeyFactory.getInstance(ALGORITHM_RSA);
            PrivateKey privateKey = keyFactory.generatePrivate(pkcs8KeySpec);
            Signature signature = Signature.getInstance(ALGORITHM_RSA_SIGN);
            signature.initSign(privateKey);
            signature.update(data.getBytes(CHARSET));
            return Base64.getEncoder().encodeToString(signature.sign());
        } catch (Exception e) {
            throw new RuntimeException("签名字符串[" + data + "]时遇到异常", e);
        }
    }


    /**
     * RSA算法使用私钥对数据生成数字签名
     *
     * @param data 待签名的明文字符串
     * @param key  RSA私钥字符串
     * @return RSA私钥签名后的经过Base64编码的字符串
     */
    public static String buildRSASignByPrivateKey1(String data, String key) {
        try {
            KeyFactory keyFactory = KeyFactory.getInstance(ALGORITHM_RSA);

            byte[] encodedKey = readText(new ByteArrayInputStream(key.getBytes())).getBytes();

            encodedKey = org.apache.commons.codec.binary.Base64.decodeBase64(encodedKey);

            PrivateKey priKey = keyFactory.generatePrivate(new PKCS8EncodedKeySpec(encodedKey));

            Signature signature = Signature.getInstance(ALGORITHM_RSA_SIGN);
            signature.initSign(priKey);

            signature.update(data.getBytes("utf-8"));
            byte[] signed = signature.sign();

            return new String(org.apache.commons.codec.binary.Base64.encodeBase64(signed));
        } catch (Exception e) {
            throw new RuntimeException("签名字符串[" + data + "]时遇到异常", e);
        }
    }

    private static String readText(InputStream ins) throws IOException {
        Reader reader = new InputStreamReader(ins);
        StringWriter writer = new StringWriter();

        io(reader, writer, -1);
        return writer.toString();
    }

    private static void io(Reader in, Writer out, int bufferSize) throws IOException {
        if (bufferSize == -1) {
            bufferSize = DEFAULT_BUFFER_SIZE >> 1;
        }

        char[] buffer = new char[bufferSize];
        int amount;

        while ((amount = in.read(buffer)) >= 0) {
            out.write(buffer, 0, amount);
        }
    }

    /**
     * RSA算法使用公钥校验数字签名
     *
     * @param data 参与签名的明文字符串
     * @param key  RSA公钥字符串
     * @param sign RSA签名得到的经过Base64编码的字符串
     * @return true--验签通过,false--验签未通过
     */
    public static boolean   buildRSAverifyByPublicKey(String data, String key, String sign) {
        try {
            //通过X509编码的Key指令获得公钥对象
            X509EncodedKeySpec x509KeySpec = new X509EncodedKeySpec(Base64.getDecoder().decode(key));
            KeyFactory keyFactory = KeyFactory.getInstance(ALGORITHM_RSA);
            PublicKey publicKey = keyFactory.generatePublic(x509KeySpec);
            Signature signature = Signature.getInstance(ALGORITHM_RSA_SIGN);
            signature.initVerify(publicKey);
            signature.update(data.getBytes(CHARSET));
            return signature.verify(Base64.getDecoder().decode(sign));
        } catch (Exception e) {
            throw new RuntimeException("验签字符串[" + data + "]时遇到异常", e);
        }
    }


    public static boolean buildRSAverifyByPublicKey1(String content,String sign,String publicKey) {
        String charset = "utf-8";
        try {
            KeyFactory keyFactory = KeyFactory.getInstance(ALGORITHM_RSA);

            StringWriter writer = new StringWriter();
            io(new InputStreamReader(new ByteArrayInputStream(publicKey.getBytes())), writer, -1);

            byte[] encodedKey = writer.toString().getBytes();

            encodedKey = org.apache.commons.codec.binary.Base64.decodeBase64(encodedKey);

            PublicKey pubKey = keyFactory.generatePublic(new X509EncodedKeySpec(encodedKey));

            Signature signature = Signature.getInstance(ALGORITHM_RSA_SIGN);
            signature.initVerify(pubKey);

            signature.update(content.getBytes(charset));
            return signature.verify(org.apache.commons.codec.binary.Base64.decodeBase64(sign.getBytes()));
        } catch (Exception e) {
            throw new RuntimeException("验签字符串[" + content + "]时遇到异常", e);
        }
    }

    /**
     * RSA算法分段加解密数据
     *
     * @param cipher 初始化了加解密工作模式后的javax.crypto.Cipher对象
     * @param opmode 加解密模式,值为javax.crypto.Cipher.ENCRYPT_MODE/DECRYPT_MODE
     * @return 加密或解密后得到的数据的字节数组
     */
    private static byte[] rsaSplitCodec(Cipher cipher, int opmode, byte[] datas) {
        int maxBlock = 0;
        if (opmode == Cipher.DECRYPT_MODE) {
            maxBlock = ALGORITHM_RSA_PRIVATE_KEY_LENGTH / 8;
        } else {
            maxBlock = ALGORITHM_RSA_PRIVATE_KEY_LENGTH / 8 - 11;
        }
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        int offSet = 0;
        byte[] buff;
        int i = 0;
        try {
            while (datas.length > offSet) {
                if (datas.length - offSet > maxBlock) {
                    buff = cipher.doFinal(datas, offSet, maxBlock);
                } else {
                    buff = cipher.doFinal(datas, offSet, datas.length - offSet);
                }
                out.write(buff, 0, buff.length);
                i++;
                offSet = i * maxBlock;
            }
        } catch (Exception e) {
            throw new RuntimeException("加解密阀值为[" + maxBlock + "]的数据时发生异常", e);
        }
        byte[] resultDatas = out.toByteArray();
        IOUtils.closeQuietly(out);
        return resultDatas;
    }

    public static void main(String[] args) {
        String privateKey="MIIEwgIBADANBgkqhkiG9w0BAQEFAASCBKwwggSoAgEAAoIBAQCKwSVPH5mvuMoDhnShnggQ6j2gEJe8vMpjToyqYI9HpFjYlbGgVu6JmD+Q7tUA7QU+2MdWk3XQJ9d7+BrUWyAdw1FHa98o1L1y1UNXhvb8zJ/2rnK215BVJPP7vLA1nMgwq4pBh8H65bWFabmtKcQqqwkyDXxU/6CDrzLVQbpWZ1AprHXuJDH/hb6j6Bs/6IU2HrKQER50H/IuSBtYJ4rcpa7vQJS2PxY9J+OJB6t/U+clhdp74frvaU9Qa6zPdbUdG/J0IzoD1g4G7ko5dC3wGawdvBsgJt8FiQy5D4jD+11vEFABWfF339k9p5DuPdXFBPxi719TB15K+nMlfclZAgMBAAECggEAGYQzbD2PcbU10TgksqPxL8sj0g+7F8ZQIbsMOzjOoIoknI/KvoW+ECejaO/C1AzCbjBHWDB99e2dvvp9VJf9a/vDMrMvsr6MqFV3sNztBshKkn66Mc2qiT3mcGDetr1CQ1EId8yTa6RaoVI0BxgrLv2ND1tThf4MTl0gYcWj8glNVA/vHXMg84Uq4mjDmRX1RnQhK7bHuqNxNNf1wdZzVGiURRkV4dgXrPtfSHequTcjVnqvMkLcTpsW4JFlXKWOZVliuxzl5mUIFrHGOMjgr0ehppCEea5D/ADRdkOExLiSgh2AqrfIKg2QJL9MJMDVgP37BHIELXEDARkFOLUmEQKBiQDFar5IxDKnsE/LQCXHWYCd6hBokT+UKkCHCv485hCG6/Yf/qho9n4aBY/2EvDjaxo3Do213NA+6iOzwfzbBi7Quf/q8QctwBkgJjOnORm9xDvS8eh/1BPesAYm2SQzk/yHwql6EXYhw8QNnSPOHsQ88dRIiMTiPj8Rqlle8mCKpGmjhLX8KS5fAnkAs+328ya9o0jA48rCTbtlMQOclCm9vMrvvnBrV7gnKBFTxv+WscxdLUGrEtkFKKLhcBCHi1gZnkOlylOQV5/oCS/XKhLStZEZ0Uk4hMIy85/qV+bgZ8o8/sYZp3O8cLp8/vc6ojgfGEP9n+IX2SBwwyBzi7IUSzNHAoGIC07j+3MRm71jtLbF8zdTQLhpnDPFuoz5CUM98OsUw0W84XRK3UhRTNhIuMW4+q3l4IV65JHzc8chqGhSPZGLGPWujRylVp+MUOUfTFLfAJm++4RT1w6k3Lqkf5Esm6+8ZWJnJtDdPW3jSwxXQrVMdFk92bHL5VG/dxmU6PVlRkvIqU8uxXTubQJ4WbdgGWHJ0S8nru2c8PFtxYDNhDtD9m0iGc0oWPbj32C0a1dqJoGGPFiwGpfHsLl2u1AipeJp87Y7ZqVAJdD//AS0vq/gDNSrEOX3norOqRel2+qtHlODEm5owEw/O95hESjpPZXKO2k7f4tW5Nb+Iu19b7x9zfllAoGIVeIj5goXh5dH/qEj75ANDvakPff4dt1pZHRbOttrlp/SC73ujtmlDYgus31KMfgB33IVqWTaIEl8DGD0cBnx0yI9tTIVKmAMIpV2h2RjydvkBpLp3qJkoLapSano5COn8bd0D0D5LHm5JHOyO5GLu13CksHGsP4GvHIXF9Zr/Q8Qvyhp45CvFA==";
        String publicKey="MIIBIjANBgkqhkiG9w0BAQEFAAOCAQ8AMIIBCgKCAQEA2rznmBAPpR/w6DwrybdyFQnhqD48r8Hy6gwHj+OYOY9Rkg0bZYcg+ArTQIM4yyqzi41LIpzCB5zQT34PQBs29oGC7ntNnBSxzPoka+gzOKbnrAwpuLeWRvEmzVjGTplrjY7tK+ZZVPsYf+rCKxkECDCM4wkCpOgwtulH63Vw+8mBcWvFu+LpIUNMJl3fzHVlDO6qGJ25CsmbLVGS3gpapYTsIQ00cluocFgYqkZ6COpzg6LukkVrT/bvzDEKlxoAbOB26uLluuc0Regahwq9KOXnU7SPOHCgctBX28p7q9CdsjQzUGk3RX9J/u7YjnFB5ysxTy1QWU5+KbqR4cKo8wIDAQAB";
        System.out.println("签名:"+buildRSASignByPrivateKey("callback=https%3A%2F%2Fwww.aaabbb.com%2Fapi%2Forder%2Fcreateorder&merchantNo=aaabbb&orderId=ee1234567890123456789&payment=300.00&userId=1234567890", privateKey));
        System.out.println("校验:"+buildRSAverifyByPublicKey("callback=https%3A%2F%2Fwww.aaabbb.com%2Fapi%2Forder%2Fcreateorder&merchantNo=aaabbb&orderId=ee1234567890123456789&payment=300.00&userId=1234567890",publicKey,buildRSASignByPrivateKey("callback=https%3A%2F%2Fwww.aaabbb.com%2Fapi%2Forder%2Fcreateorder&merchantNo=aaabbb&orderId=ee1234567890123456789&payment=300.00&userId=1234567890", privateKey)));

    }

}
