package cn.beinet.codegenerate.otpcode.service;

import lombok.SneakyThrows;
import org.apache.commons.codec.binary.Base32;
import org.springframework.util.StringUtils;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.security.SecureRandom;
import java.util.Base64;
import java.util.HashMap;
import java.util.Map;

/**
 * 2FA使用的OTP（one time password）生成辅助工具类
 */
public class OtpCodeGeneratorTool {
    private static final long second_per_size = 30L;// 每次时间长度，默认30秒
    private static final String SALT = ".beinet.cn";

    /**
     * 生成一个16位的Base32字符组成的SecretKey，用于外部绑定到用户.
     * 注1：Android的google authenticator只支持Base32字符，不限制长度。
     * 注2： iPhone的google authenticator只支持16位长度的Base32字符。
     *
     * @return SecretKey
     */
    @SneakyThrows
    public static String generateSecretKey() {
        SecureRandom sr = SecureRandom.getInstance("SHA1PRNG");
        sr.setSeed(getSeed());
        byte[] buffer = sr.generateSeed(64);
        String ret = Base64.getEncoder().encodeToString(buffer);
        // ret = ret.replaceAll("[\\\\=+\\s/]", "");// 移除末尾的等号

        // google authenticator导入报错：密钥无法识别，因为google authenticator期望secret是有效的Base32编码
        // Base32 编码使用的字符集限于 A-Z 和 2-7
        ret = ret.replaceAll("[^A-Z2-7]", "")
                // .replaceAll("(.{8})", "$1 ") 每8个字符后加1个空格
                .trim();

        // iPhone手机要求长度必须是16位
        if (ret.length() < 16) {
            // 将ret长度补足16位
            StringBuilder sb = new StringBuilder(ret);
            while (sb.length() < 16) {
                sb.append(ret);
            }
            ret = sb.toString();
        }
        if (ret.length() > 16) {
            ret = ret.substring(0, 16);
        }
        return ret;
    }

    private static byte[] getSeed() {
        String str = SALT + System.currentTimeMillis() + SALT;
        return str.getBytes(StandardCharsets.UTF_8);
    }

    /**
     * 生成二维码所需的字符串，注：这个format不可修改，否则会导致身份验证器无法识别二维码
     *
     * @param title  标题
     * @param user   绑定到的用户名
     * @param secret 对应的secretKey
     * @return 二维码字符串
     */
    public static String getQRBarcode(String title, String user, String secret) {
        if (StringUtils.hasLength(title)) {
            title = title.replaceAll(":", ""); // 不允许包含冒号
            user = title + ":" + user;
        }
        String format = "otpauth://totp/%s?secret=%s";
        String ret = String.format(format, user, secret);
//        if (StringUtils.hasLength(title)) {
//            ret += "&issuer=" + title;
//        }
        return ret;
    }

    /**
     * 获取指定密钥，当前时间的otpCode
     *
     * @param secret 用户绑定的secretKey
     * @return 生成的与时间关联的code
     */
    public static String countCodeStr(String secret) {
        long timeSecond = System.currentTimeMillis() / 1000L / second_per_size;
        return countCode(secret, timeSecond);
    }

    /**
     * 获取指定密钥的otpCode, 键为到期时间，值为otp
     *
     * @param secret  用户绑定的secretKey
     * @param codeNum 生成几个code返回
     * @return 生成的与时间关联的code
     */
    public static Map<String, String> countCodeStr(String secret, int codeNum) {
        Map<String, String> ret = new HashMap<>();
        long timeSecond = System.currentTimeMillis() / 1000L;
//        int winSize = codeNum / 2;
//        for (int i = -winSize; i <= winSize; ++i) { // 负号用于计算过期的otp
        for (int i = 0; i < codeNum; ++i) {
            long countTime = timeSecond + (i * second_per_size);

            // 把秒级时间戳，转换为30秒的时间窗口，即每30秒生成一个otp
            // this is per the TOTP spec (see the RFC for details)
            long timeWindow = countTime / second_per_size;
            String strCode = countCode(secret, timeWindow);

            // 起始时间为每个30秒的起始
            long secondBegin = timeWindow * second_per_size;// otp起始时间
            long secondEnd = secondBegin + second_per_size; // otp失效时间
            ret.put(String.valueOf(secondEnd), strCode);
        }
        return ret;
    }

    /**
     * 获取指定密钥的otpcode
     *
     * @param secret     用户绑定的secretKey
     * @param timeWindow 时间窗口，截止当前是每30秒一个窗口，一般为 秒级时间戳 除以 30
     * @return 生成的当前时间的code
     */
    public static String countCode(String secret, long timeWindow) {
        if (secret == null || secret.isEmpty())
            return "";
        byte[] decodedKey = countKey(secret);
        return addZero(verifyCode(decodedKey, timeWindow), 6);
    }

    /**
     * 把指定的otp密钥，转换为字节数组返回
     *
     * @param secret 密钥
     * @return 字节数组
     */
    private static byte[] countKey(String secret) {
        Base32 codec = new Base32();
        return codec.decode(secret);
    }

    /**
     * 还原密钥
     *
     * @param secretKey 字节数组
     * @return 密钥
     */
    public static String restoreSecret(byte[] secretKey) {
        Base32 codec = new Base32();
        return codec.encodeAsString(secretKey);
    }

    @SneakyThrows
    private static int verifyCode(byte[] key, long t) {
        byte[] data = new byte[8];
        long value = t;
        for (int i = 8; i-- > 0; value >>>= 8) {
            data[i] = (byte) value;
        }
        SecretKeySpec signKey = new SecretKeySpec(key, "HmacSHA1");
        Mac mac = Mac.getInstance("HmacSHA1");
        mac.init(signKey);
        byte[] hash = mac.doFinal(data);
        int offset = hash[20 - 1] & 0xF;
        // We're using a long because Java hasn't got unsigned int.
        long truncatedHash = 0;
        for (int i = 0; i < 4; ++i) {
            truncatedHash <<= 8;
            // We are dealing with signed bytes:
            // we just keep the first byte.
            truncatedHash |= (hash[offset + i] & 0xFF);
        }
        truncatedHash &= 0x7FFFFFFF;
        truncatedHash %= 1000000;
        return (int) truncatedHash;
    }

    private static String addZero(int code, int returnLen) {
        StringBuilder ret = new StringBuilder(String.valueOf(code));
        for (int i = ret.length(); i < returnLen; i++) {
            ret.insert(0, "0");
        }
        return ret.toString();
    }
}