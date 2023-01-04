package cn.beinet.codegenerate.util;

import org.springframework.util.StringUtils;

import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;

/**
 * Description:
 *
 * @author : youbl
 * @create: 2022/2/16 19:56
 */
public final class RequestHelper {
    public static String getCookie(String name, HttpServletRequest request) {
        if (request == null || !StringUtils.hasLength(name))
            return "";
        Cookie[] cookies = request.getCookies();
        if (cookies == null || cookies.length == 0)
            return "";
        for (Cookie cookie : cookies) {
            if (name.equalsIgnoreCase(cookie.getName())) {
                return cookie.getValue();
            }
        }
        return "";
    }
}
