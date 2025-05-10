// 文件路径: user-service/src/main/java/com/letsvpn/user/util/WireGuardKeyGenerator.java
package com.letsvpn.user.util;

import org.bouncycastle.crypto.generators.X25519KeyPairGenerator;
import org.bouncycastle.crypto.params.X25519KeyGenerationParameters;
import org.bouncycastle.crypto.params.X25519PrivateKeyParameters;
import org.bouncycastle.crypto.params.X25519PublicKeyParameters;
import org.bouncycastle.jce.provider.BouncyCastleProvider;

import java.security.SecureRandom;
import java.security.Security;
import java.util.Base64;

public class WireGuardKeyGenerator {

    static {
        // 确保BouncyCastle提供者已加载
        if (Security.getProvider(BouncyCastleProvider.PROVIDER_NAME) == null) {
            Security.addProvider(new BouncyCastleProvider());
        }
    }

    public static class KeyPair {
        private final String privateKey; // Base64 encoded
        private final String publicKey;  // Base64 encoded

        public KeyPair(String privateKey, String publicKey) {
            this.privateKey = privateKey;
            this.publicKey = publicKey;
        }

        public String getPrivateKey() {
            return privateKey;
        }

        public String getPublicKey() {
            return publicKey;
        }
    }

    /**
     * 生成一个新的WireGuard密钥对 (私钥和公钥)。
     * 私钥是32字节的随机数据。
     * 公钥是基于私钥通过Curve25519计算得出。
     * 两者都返回为Base64编码的字符串。
     *
     * @return KeyPair 包含Base64编码的私钥和公钥。
     */
    public static KeyPair generateKeyPair() {
        SecureRandom random = new SecureRandom();
        X25519KeyPairGenerator kpg = new X25519KeyPairGenerator();
        kpg.init(new X25519KeyGenerationParameters(random));

        // 生成密钥对参数对象
        org.bouncycastle.crypto.AsymmetricCipherKeyPair bouncyCastleKeyPair = kpg.generateKeyPair();

        // 获取私钥参数和公钥参数
        X25519PrivateKeyParameters privateKeyParams = (X25519PrivateKeyParameters) bouncyCastleKeyPair.getPrivate();
        X25519PublicKeyParameters publicKeyParams = (X25519PublicKeyParameters) bouncyCastleKeyPair.getPublic();

        // 获取原始的32字节密钥数据
        byte[] privateKeyBytes = privateKeyParams.getEncoded();
        byte[] publicKeyBytes = publicKeyParams.getEncoded();

        // WireGuard 要求对私钥进行一些特定的位操作（clamping），
        // 但Bouncy Castle的X25519PrivateKeyParameters.getEncoded()返回的已经是符合要求的私钥。
        // clampPrivateKey(privateKeyBytes); // 通常库会处理好，如果需要手动clamp，逻辑如下

        // 将字节数组编码为Base64字符串
        String privateKeyBase64 = Base64.getEncoder().encodeToString(privateKeyBytes);
        String publicKeyBase64 = Base64.getEncoder().encodeToString(publicKeyBytes);

        return new KeyPair(privateKeyBase64, publicKeyBase64);
    }

    /**
     * WireGuard私钥的"clamping"操作。
     * 根据RFC 7748, Curve25519的私钥需要进行clamping。
     * BouncyCastle的X25519PrivateKeyParameters.getEncoded()应该已经返回了clamped的私钥。
     * 这个方法主要是为了演示clamping的过程，通常不需要自己调用，除非你从完全随机的32字节生成私钥。
     *
     * @param privateKey 32字节的私钥
     */
    @SuppressWarnings("unused") // 标记为未使用，因为库通常会处理
    private static void clampPrivateKey(byte[] privateKey) {
        if (privateKey == null || privateKey.length != 32) {
            throw new IllegalArgumentException("Private key must be 32 bytes.");
        }
        privateKey[0] &= 248;  // 清除最低三位 (0, 1, 2)
        privateKey[31] &= 127; // 清除最高位 (bit 7)
        privateKey[31] |= 64;  // 设置次高位 (bit 6)
    }

    /**
     * （可选）从给定的私钥（Base64编码）计算出公钥。
     * 如果你只有私钥，想得到对应的公钥，可以用这个方法。
     *
     * @param privateKeyBase64 Base64编码的私钥
     * @return Base64编码的公钥
     */
    public static String getPublicKey(String privateKeyBase64) {
        byte[] privateKeyBytes = Base64.getDecoder().decode(privateKeyBase64);
        if (privateKeyBytes.length != 32) {
            throw new IllegalArgumentException("Decoded private key is not 32 bytes long.");
        }

        // 使用解码后的私钥字节创建X25519PrivateKeyParameters
        // 注意：这里假设传入的私钥已经是经过clamping的（如果它是从标准WG工具生成的）
        // 或者，如果你想确保clamping，可以在这里调用 clampPrivateKey(privateKeyBytes);
        // 但如果私钥是从我们自己的generateKeyPair()生成的，它已经是clamped。
        X25519PrivateKeyParameters privateKeyParams = new X25519PrivateKeyParameters(privateKeyBytes, 0);
        X25519PublicKeyParameters publicKeyParams = privateKeyParams.generatePublicKey();

        byte[] publicKeyBytes = publicKeyParams.getEncoded();
        return Base64.getEncoder().encodeToString(publicKeyBytes);
    }


    // 主方法用于测试
    public static void main(String[] args) {
        KeyPair keyPair = generateKeyPair();
        System.out.println("Private Key: " + keyPair.getPrivateKey());
        System.out.println("Public Key: " + keyPair.getPublicKey());

        // 测试从私钥计算公钥
        String reCalculatedPublicKey = getPublicKey(keyPair.getPrivateKey());
        System.out.println("Re-calculated Public Key: " + reCalculatedPublicKey);
        System.out.println("Public keys match: " + keyPair.getPublicKey().equals(reCalculatedPublicKey));
    }
}