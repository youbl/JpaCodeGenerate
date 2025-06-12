package cn.beinet.codegenerate.util;

import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.util.StringUtils;

import java.util.regex.Pattern;

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

    /**
     * 从请求上下文中提取二级域名，如
     * www.baidu.com 返回 baidu.com
     * abc.def.ghi.xxx返回ghi.xxx
     *
     * @param request 请求上下文
     * @return 二级域名
     */
    public static String getBaseDomain(HttpServletRequest request) {
        String domain = request.getServerName();
        // IP直接返回
        if (Pattern.matches("^\\d{1,3}\\.\\d{1,3}\\.\\d{1,3}\\.\\d{1,3}$", domain))
            return domain;
        return domain.replaceAll(".*\\.(?=.*\\.)", "");
    }
}
