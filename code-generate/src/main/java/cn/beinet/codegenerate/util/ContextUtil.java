package cn.beinet.codegenerate.util;

import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import org.springframework.util.StringUtils;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import java.util.Enumeration;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * 上下文辅助类
 * @author youbl
 * @since 2025/4/24 15:28
 */
@Slf4j
public abstract class ContextUtil {
    /**
     * 收到请求的时间，可以便于后端计算响应时长
     */
    public static final String HEADER_REQUEST_TIME = "x-request-time";
    /**
     * 保存在请求上下文里的用户名属性
     */
    public static final String LOGIN_INFO = "loginUser";

    /**
     * 获取当前登录用户名
     * @return 用户名
     */
    public static String getLoginUser() {
        return getAttribute(LOGIN_INFO);
    }

    /**
     * 设置当前登录用户名
     * @param loginUser 登录用户
     */
    public static void setLoginUser(String loginUser) {
        setAttribute(LOGIN_INFO, loginUser);
    }

    /**
     * 当前登录用户，是否管理员
     * @return bool
     */
    public static boolean isAdmin() {
        return (getLoginUser().startsWith("beiliang_you"));
    }

    /**
     * 获取Web上下文里的请求对象
     * @return request
     */
    public static HttpServletRequest getRequest() {
        ServletRequestAttributes servletRequestAttributes = (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
        return servletRequestAttributes == null ? null : servletRequestAttributes.getRequest();
    }

    /**
     * 按优先级顺序获取用户IP
     *
     * @return 单个IP
     */
    public static String getIp() {
        try {
            HttpServletRequest request = getRequest();
            return request != null ? IpHelper.getIpAddr(request) : null;
        } catch (Exception exp) {
            log.error("取IP出错", exp);
            return "";
        }
    }

    /**
     * 返回完整用户IP，包括所有header
     *
     * @return 所有IP
     */
    public static String getFullIp() {
        try {
            HttpServletRequest request = getRequest();
            return request != null ? IpHelper.getClientIp(request) : null;
        } catch (Exception exp) {
            log.error("getFullIpErr", exp);
            return "";
        }
    }

    /**
     * 获取当前请求url
     *
     * @param getMethod  是否拼接METHOD
     * @param getReferer 是否拼接Referer
     * @return 请求url
     */
    public static String getRequestUrl(boolean getMethod, boolean getReferer) {
        try {
            HttpServletRequest request = getRequest();
            if (request != null) {
                String para = request.getQueryString();
                if (StringUtils.hasLength(para))
                    para = "?" + para;
                else
                    para = "";

                String method = getMethod ? request.getMethod() + " " : "";
                String referer = getReferer ? " refer:" + request.getHeader("referer") : "";
                return method + request.getRequestURL() + para + referer;
            }
        } catch (Exception exp) {
            log.error("取url出错", exp);
        }
        return "";
    }

    /**
     * 获取Web上下文里的请求Header信息
     * @return header键值
     */
    public static Map<String, String> getHeaders() {
        Map<String, String> headers = new LinkedHashMap<>();
        try {
            HttpServletRequest request = getRequest();
            if (request == null) {
                return headers;
            }
            Enumeration<String> headerNames = request.getHeaderNames();
            while (headerNames != null && headerNames.hasMoreElements()) {
                try {
                    String key = headerNames.nextElement();
                    headers.put(key, request.getHeader(key));
                } catch (Exception e) {
                    // headerNames.nextElement() 可能出现空指针
                    log.warn("获取header某个key异常", e);
                }
            }
        } catch (Exception exp) {
            log.error("取header异常:", exp);
        }
        return headers;
    }

    /**
     * 获取Web上下文里的请求Header值
     * @param key header key
     * @return header值
     */
    public static String getHeader(String key) {
        if (!StringUtils.hasLength(key))
            return "";

        try {
            HttpServletRequest request = getRequest();
            if (request == null) {
                return "";
            }
            Enumeration<String> headerNames = request.getHeaderNames();
            while (headerNames != null && headerNames.hasMoreElements()) {
                try {
                    if (key.equals(headerNames.nextElement())) {
                        return request.getHeader(key);
                    }
                } catch (Exception e) {
                    // headerNames.nextElement() 可能出现空指针
                    log.warn("获取header某个key异常", e);
                }
            }
        } catch (Exception exp) {
            log.error("取header异常:", exp);
        }
        return "";
    }

    /**
     * 获取Web上下文里的请求cookie值
     * @param name cookie key
     * @return cookie值
     */
    public static String getCookie(String name) {
        if (!StringUtils.hasLength(name))
            return "";

        HttpServletRequest request = getRequest();
        if (request == null) {
            return "";
        }

        Cookie[] cookies = request.getCookies();
        if (cookies == null)
            return "";
        for (Cookie cookie : cookies) {
            if (name.equalsIgnoreCase(cookie.getName())) {
                return cookie.getValue();
            }
        }
        return "";
    }


    /**
     * 设置当前时间戳到Web请求上下文中
     */
    public static void setRequestTime() {
        long timestamp = System.currentTimeMillis();
        setAttribute(HEADER_REQUEST_TIME, String.valueOf(timestamp));
    }

    /**
     * 获取请求上下文里的请求时间戳
     *
     * @return 请求时间戳
     */
    public static long getRequestTime() {
        String result = getAttribute(HEADER_REQUEST_TIME);
        if (StringUtils.hasLength(result))
            return Long.parseLong(result);
        return 0;
    }

    /**
     * 在当前请求上下文里设置属性值
     * @param name 属性名
     * @param value 属性值
     */
    public static void setAttribute(String name, String value) {
        ServletRequestAttributes requestAttributes = (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
        if (requestAttributes == null) {
            return;
        }
        requestAttributes.setAttribute(name, value, ServletRequestAttributes.SCOPE_REQUEST);
    }

    /**
     * 在当前请求上下文里读取属性值
     * @param name 属性名
     * @return 属性值
     */
    public static String getAttribute(String name) {
        ServletRequestAttributes requestAttributes = (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
        if (requestAttributes == null) {
            return null;
        }
        Object object = requestAttributes.getAttribute(name, ServletRequestAttributes.SCOPE_REQUEST);
        if (object == null)
            return "";
        return object.toString();
    }
}
