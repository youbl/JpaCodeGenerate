package cn.beinet.codegenerate.util;

import org.springframework.util.StringUtils;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

/**
 * 新类
 *
 * @author youbl
 * @date 2023/3/27 15:45
 */
public final class TokenHelper {
    // token 名
    public static final String TOKEN_NAME = "token";

    // token校验md5的盐值
    private static String TOKEN_SALT = "beinet.cn.";

    // token不同信息的分隔符
    private static final String TOKEN_SPLIT = ":";
    private static final DateTimeFormatter FORMATTER = DateTimeFormatter.ofPattern("yyyyMMddHHmmss");

    static {
        setTokenSalt(SpringUtil.getProperty("token.salt"));
    }

    /**
     * 修改加盐值
     *
     * @param salt
     */
    public static void setTokenSalt(String salt) {
        TOKEN_SALT = salt;
    }

    /**
     * 校验token格式和md5是否正确
     *
     * @param token token
     * @return 是否正确token
     */
    public static String getLoginUserFromToken(String token) {
        if (!StringUtils.hasLength(token)) {
            return null;
        }
        // 0为用户名，1为时间，2为md5
        String[] arr = token.split(TOKEN_SPLIT);
        if (arr.length != 3 || arr[0].isEmpty() || arr[1].isEmpty() || arr[2].isEmpty()) {
            return null;
        }
        long loginTime = Long.parseLong(arr[1]);
        long now = Long.parseLong(LocalDateTime.now().format(FORMATTER));
        long diff = now - loginTime;
        // 1000000为1天，要求每天登录
        if (diff < 0 || diff > 1000000) {
            return null;
        }

        /* todo:
            算法和盐值泄露，客户端可以伪造一个有效的token进入。
            简单作法是把token写入redis。
            这个是内部系统，暂时不浪费redis空间吧。
            另一个作法，是使用独立服务的rsa算法，在独立服务里，用私钥生成token，
            在校验token的地方，使用公钥验证客户端提交的token有效性
            */
        String countToken = buildToken(arr[0], arr[1]);
        if (countToken.equalsIgnoreCase(token)) {
            return arr[0];
        }
        return null;
    }

    public static String buildToken(String username) {
        String date = LocalDateTime.now().format(FORMATTER);
        return buildToken(username, date);
    }

    /**
     * 根据用户名和登录时间，计算md5，并拼接token返回
     *
     * @param username 用户名
     * @param date     登录时间
     * @return token
     */
    public static String buildToken(String username, String date) {
        if (date == null) {
            date = LocalDateTime.now().format(FORMATTER);
        }
        String ret = username + TOKEN_SPLIT + date + TOKEN_SPLIT;
        String md5 = StringHelper.md5(ret, TOKEN_SALT, username);
        return ret + md5;
    }
}
