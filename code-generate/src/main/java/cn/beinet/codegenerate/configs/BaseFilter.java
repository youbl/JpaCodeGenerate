package cn.beinet.codegenerate.configs;

import cn.beinet.codegenerate.util.SpringUtil;
import org.springframework.util.StringUtils;
import org.springframework.web.filter.OncePerRequestFilter;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.nio.charset.StandardCharsets;

/**
 * filter基类
 *
 * @author youbl
 * @since 2023/12/8 16:27
 */
public abstract class BaseFilter extends OncePerRequestFilter {

    // 登录输入页地址
    public static final String loginPage = "/login.html";

    private static String prefix;

    /**
     * 终止响应，并返回错误信息
     *
     * @param request  请求上下文
     * @param response 响应上下文
     * @param msg      错误信息
     */
    protected void endResponse(HttpServletRequest request, HttpServletResponse response, String msg, String loginUrl) {
        if (!isAjax(request)) {
            redirect(response, loginUrl);
            return;
        }
        response.setContentType("application/json; charset=UTF-8");
        try {
            msg = msg.replaceAll("[\"']", "");
            loginUrl = loginUrl.replaceAll("[\"']", "");
            String ret = "{\"ret\":500, \"msg\":\"" + msg + "\", \"loginUrl\":\"" + loginUrl + "\"}";
            response.getOutputStream().write(ret.getBytes(StandardCharsets.UTF_8));
        } catch (IOException e) {
            e.printStackTrace();
        }
    }


    /**
     * 重定向到index首页
     *
     * @param response 响应上下文
     * @param url      跳转地址
     */
    public void redirect(HttpServletResponse response, String url) {
        if (!StringUtils.hasLength(url)) {
            url = "/index.html";
        }
        if (!url.startsWith("http")) {
            if (!url.startsWith("/"))
                url = "/" + url;

            if (prefix == null) {
                prefix = SpringUtil.getProperty("server.servlet.context-path");
            }
            url = prefix + url;
        }
        response.setStatus(302);
        response.setHeader("Location", url);//设置新请求的URL
    }

    /**
     * 判断是否ajax请求
     *
     * @param request 当前请求上下文
     * @return 是否
     */
    private static boolean isAjax(HttpServletRequest request) {
        String header = request.getHeader("accept");
        if (header != null && header.contains("application/json"))
            return true;
        header = request.getHeader("x-requested-with");
        return header != null && header.equalsIgnoreCase("XMLHttpRequest");
    }

}
