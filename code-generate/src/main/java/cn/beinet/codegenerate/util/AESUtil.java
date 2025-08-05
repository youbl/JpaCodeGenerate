package cn.beinet.codegenerate.util;

import lombok.SneakyThrows;
import org.springframework.util.Base64Utils;

import javax.crypto.Cipher;
import javax.crypto.KeyGenerator;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;

/**
 * AES加密/解密工具类
 */
public class AESUtil {

    private static final String KEY_ALGORITHM = "AES";
    private static final String DEFAULT_CIPHER_ALGORITHM = "AES/CBC/PKCS5Padding";//默认的加密算法
    private static final Charset CHARSET = StandardCharsets.UTF_8;
    private static final int IV_LENGTH = 16;

    private static final String DEFALUT_KEY = "beinet.cn-beinet";

    /**
     * AES 加密操作(使用默认密钥）
     *
     * @param content 待加密内容
     * @return 返回Base64转码后的加密数据
     */
    @SneakyThrows
    public static String encrypt(String content) {
        return encrypt(DEFALUT_KEY, content);
    }

    /**
     * AES 加密操作
     *
     * @param key     加密密码
     * @param content 待加密内容
     * @return 返回Base64转码后的加密数据
     */
    @SneakyThrows
    public static String encrypt(String key, String content) {
        String password = key.substring(0, IV_LENGTH);
        String iv = key.substring(key.length() - IV_LENGTH);

        // 不足16位，补全
        // content = content + "\0".repeat((16 - content.length() % 16));

        byte[] byteKey = password.getBytes(CHARSET);
        byte[] byteContent = content.getBytes(CHARSET);
        byte[] byteIv = iv.getBytes(CHARSET);

        Cipher cipher = Cipher.getInstance(DEFAULT_CIPHER_ALGORITHM);// 创建密码器
        cipher.init(Cipher.ENCRYPT_MODE, new SecretKeySpec(byteKey, KEY_ALGORITHM), new IvParameterSpec(byteIv));
        byte[] result = cipher.doFinal(byteContent);// 加密

        return Base64Utils.encodeToString(result);//通过Base64转码返回
    }

    /**
     * AES 解密操作(使用默认密钥）
     *
     * @param content 待解密内容
     * @return 解密结果
     */
    public static String decrypt(String content) {
        return decrypt(DEFALUT_KEY, content);
    }

    /**
     * AES 解密操作
     *
     * @param key     加密用的密钥
     * @param content 待解密内容
     * @return 解密结果
     */
    @SneakyThrows
    public static String decrypt(String key, String content) {
        String password = key.substring(0, IV_LENGTH);
        String iv = key.substring(key.length() - IV_LENGTH);

        byte[] byteKey = password.getBytes(CHARSET);
        byte[] byteIv = iv.getBytes(CHARSET);

        Cipher cipher = Cipher.getInstance(DEFAULT_CIPHER_ALGORITHM);// 创建密码器
        cipher.init(Cipher.DECRYPT_MODE, new SecretKeySpec(byteKey, KEY_ALGORITHM), new IvParameterSpec(byteIv));

        String result = new String(cipher.doFinal(Base64Utils.decodeFromString(content)), CHARSET);

        return result.trim();
    }

    /**
     * 生成一个新的密钥返回
     *
     * @return 密钥
     */
    @SneakyThrows
    public static String generateKey() {
        KeyGenerator keyGenerator = KeyGenerator.getInstance(KEY_ALGORITHM);
        keyGenerator.init(128);
        return Base64Utils.encodeToString(keyGenerator.generateKey().getEncoded());
    }
}