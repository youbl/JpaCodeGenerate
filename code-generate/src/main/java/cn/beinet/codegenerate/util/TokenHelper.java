package cn.beinet.codegenerate.util;

import lombok.Data;
import org.springframework.util.StringUtils;

import java.time.Duration;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

/**
 * Token生成和校验辅助类
 *
 * @author youbl
 * @date 2023/3/27 15:45
 */
public final class TokenHelper {
    // token 名
    public static final String TOKEN_NAME = "token";

    // token校验md5的盐值
    private static String TOKEN_SALT = "beinet.cn.";

    // token cookie名
    private static String TOKEN_COOKIE_NAME = null;

    // token不同信息的分隔符
    private static final String TOKEN_SPLIT = ":";
    private static final DateTimeFormatter FORMATTER = DateTimeFormatter.ofPattern("yyyyMMddHHmmss");

    private static void init() {
        if (TOKEN_COOKIE_NAME == null) {
            setTokenSalt(SpringUtil.getProperty("token.salt"));
            TOKEN_COOKIE_NAME = SpringUtil.getProperty("token.cookie-name");
            if (!StringUtils.hasLength(TOKEN_COOKIE_NAME))
                TOKEN_COOKIE_NAME = "beinetUser";
        }
    }

    public static String getTokenCookieName() {
        init();
        return TOKEN_COOKIE_NAME;
    }

    /**
     * 修改加盐值
     *
     * @param salt 盐值
     */
    public static void setTokenSalt(String salt) {
        if (StringUtils.hasLength(salt))
            TOKEN_SALT = salt;
    }

    /**
     * 校验token格式和md5是否正确
     *
     * @param token token
     * @param salt  计算md5使用的盐值（为保证安全性, 建议定期更换）
     * @return 是否正确token
     */
    public static Token getLoginUserFromToken(String token, String salt) {
        init();
        if (!StringUtils.hasLength(token)) {
            return null;
        }
        // 0为用户名，1为时间，2为md5
        String[] arr = token.split(TOKEN_SPLIT);
        if (arr.length != 3 || arr[0].isEmpty() || arr[1].isEmpty() || arr[2].isEmpty()) {
            return null;
        }

        /* todo:
            算法和盐值泄露，客户端可以伪造一个有效的token进入。
            简单作法是把token写入redis。
            这个是内部系统，暂时不浪费redis空间吧。
            另一个作法，是使用独立服务的rsa算法，在独立服务里，用私钥生成token，
            在校验token的地方，使用公钥验证客户端提交的token有效性
            */
        String countToken = buildToken(arr[0], arr[1], salt);
        if (countToken.equalsIgnoreCase(token)) {
            Token ret = new Token();
            long loginTime = Long.parseLong(arr[1]);
            long diffSecond = getDiffTime(loginTime);
            ret.setLoginTime(loginTime);
            ret.setUsername(arr[0]);
            ret.setSeconds(diffSecond);
            return ret;
        }
        return null;
    }

    /**
     * 根据用户名和当前时间，计算md5，并拼接token返回
     *
     * @param username 用户名
     * @param salt     计算md5使用的盐值（为保证安全性, 建议定期更换）
     * @return token
     */
    public static String buildNewToken(String username, String salt) {
        String date = LocalDateTime.now().format(FORMATTER);
        return buildToken(username, date, salt);
    }

    /**
     * 根据用户名和登录时间，计算md5，并拼接token返回
     *
     * @param username 用户名
     * @param date     登录时间
     * @param salt     计算md5使用的盐值（为保证安全性, 建议定期更换）
     * @return token
     */
    public static String buildToken(String username, String date, String salt) {
        init();
        if (date == null) {
            date = LocalDateTime.now().format(FORMATTER);
        }
        String ret = username + TOKEN_SPLIT + date + TOKEN_SPLIT;
        String md5 = StringHelper.md5(ret, TOKEN_SALT, username, salt);
        return ret + md5;
    }

    /**
     * 计算当前时间与登录时间的差值，返回相差多少秒
     *
     * @param loginTime 登录时间
     * @return 秒
     */
    private static long getDiffTime(long loginTime) {
        LocalDateTime now = LocalDateTime.now();
        LocalDateTime loginDt = LocalDateTime.parse(String.valueOf(loginTime), FORMATTER);
        Duration duration = Duration.between(loginDt, now);
        return duration.getSeconds();
    }

    @Data
    public static class Token {
        /**
         * 用户名
         */
        private String username;
        /**
         * 登录时间，格式为14位数字：yyyyMMddHHmmss
         */
        private long loginTime;

        /**
         * 登录到现在，经过了多少秒
         */
        private long seconds;
    }

}
