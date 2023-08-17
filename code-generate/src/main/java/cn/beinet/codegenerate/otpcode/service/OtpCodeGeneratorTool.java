package cn.beinet.codegenerate.otpcode.service;

import lombok.SneakyThrows;
import org.apache.commons.codec.binary.Base32;
import org.springframework.util.StringUtils;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;

public class OtpCodeGeneratorTool {
    static long second_per_size = 30L;// 每次时间长度，默认30秒

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
     * 获取指定密钥的otpcode
     *
     * @param secret 用户绑定的secretKey
     * @return 生成的当前时间的code
     */
    public static String countCodeStr(String secret) {
        int code = countCode(secret);
        return addZero(code, 6);
    }

    /**
     * 获取指定密钥的otpcode
     *
     * @param secret 用户绑定的secretKey
     * @return 生成的当前时间的code
     */
    public static int countCode(String secret) {
        Base32 codec = new Base32();
        byte[] decodedKey = codec.decode(secret);
        // convert unix msec time into a 30 second "window"
        // this is per the TOTP spec (see the RFC for details)
        long timeMsec = System.currentTimeMillis();
        long t = (timeMsec / 1000L) / second_per_size;
        return verifyCode(decodedKey, t);
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
        String ret = String.valueOf(code);
        for (int i = ret.length(), j = returnLen; i < j; i++) {
            ret = "0" + ret;
        }
        return ret;
    }
}