package cn.beinet.codegenerate.configs;

import cn.beinet.codegenerate.configs.logins.Validator;
import cn.beinet.codegenerate.util.RequestHelper;
import cn.beinet.codegenerate.util.SpringUtil;
import cn.beinet.codegenerate.util.StringHelper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.ldap.core.LdapTemplate;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.filter.OncePerRequestFilter;

import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

/**
 * Description:
 *
 * @author : youbl
 * @create: 2022/2/16 16:34
 */
@Component
@Slf4j
@RequiredArgsConstructor
public class LdapLoginFilter extends OncePerRequestFilter {
    @Value("${server.servlet.context-path}")
    private String prefix;

    @Value("${spring.ldap.email-domain}")
    private String emailDomain;


    // token校验md5的盐值
    private static final String TOKEN_SALT = "beinet.cn";

    // token cookie名
    private static final String TOKEN_COOKIE_NAME = "beinetUser";
    // token不同信息的分隔符
    private static final String TOKEN_SPLIT = ":";

    // 登录用户名使用的字段名
    private static final String USER_PARA = "beinetUser";
    // 登录密码使用的字段名
    private static final String PWD_PARA = "beinetPwd";

    // 登录输入页地址
    public static final String loginPage = "/login.html";
    // 登录认证地址
    private static final String loginActionPage = "/login";

    private static final DateTimeFormatter FORMATTER = DateTimeFormatter.ofPattern("yyyyMMddHHmmss");

    private final List<Validator> validatorList;

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain) throws ServletException, IOException {
        //request.getRequestURL() 带有域名，所以不用
        //request.getRequestURI() 带有ContextPath，所以不用
        String url = request.getServletPath();
        log.debug("收到请求: {}", url);

        // 验证用户名密码
        if (url.endsWith(loginActionPage)) {
            processLoginRequest(request, response);
            return;
        }

        for (Validator item : validatorList) {
            if (item.validated(request, response)) {
                filterChain.doFilter(request, response);
                return;
            }
        }

        // 判断cookie是否有效
        String token = RequestHelper.getCookie(TOKEN_COOKIE_NAME, request);
        log.info("url:{} token: {}", url, token);
        String loginUser = getLoginUserFromToken(token);
        if (!StringUtils.hasLength(loginUser)) {
            log.debug("token无效: {}", token);
            addToken("", response);
            if (StringUtils.isEmpty(token)) {
                endResponse(request, response, "未登录");
            } else {
                endResponse(request, response, "token无效");
            }
            return;
        }

        // 添加登录后的信息
        request.setAttribute("loginUser", loginUser);

        log.debug("token校验成功: {}", token);
        filterChain.doFilter(request, response);
    }


    /**
     * 提取用户名密码，并进行登录
     *
     * @param request  请求上下文
     * @param response 响应上下文
     */
    private void processLoginRequest(HttpServletRequest request, HttpServletResponse response) {
        String username = request.getParameter(USER_PARA);
        String pwd = request.getParameter(PWD_PARA);
        log.debug("用户名: {}, 准备登录", username);
        if (!validateUser(username, pwd)) {
            log.debug("用户名: {}, 账号或密码错误", username);
            //throw new RuntimeException("账号或密码错误");
            addToken("", response);
            endResponse(request, response, "账号或密码错误");
            return;
        }

        log.debug("用户名: {}, ldap认证成功", username);
        addToken(username, response);
        redirect(response, "index.html");
    }

    /**
     * 重定向到index首页
     *
     * @param response 响应上下文
     * @param url      跳转地址
     */
    private void redirect(HttpServletResponse response, String url) {
        if (StringUtils.isEmpty(url)) {
            url = "/index.html";
        } else if (!url.startsWith("/")) {
            url = "/" + url;
        }
        response.setStatus(302);
        response.setHeader("Location", prefix + url);//设置新请求的URL
    }

    /**
     * 为响应添加登录cookie
     *
     * @param username 用户名，为空表示删除token
     * @param response 响应上下文
     */
    private void addToken(String username, HttpServletResponse response) {
        String now = LocalDateTime.now().format(FORMATTER);

        Cookie loginCookie = new Cookie(TOKEN_COOKIE_NAME, "");
        loginCookie.setPath("/");
        if (username.isEmpty()) {
            loginCookie.setMaxAge(0);
        } else {
            loginCookie.setMaxAge(7 * 24 * 3600);
            String token = buildToken(username, now);
            loginCookie.setValue(token);
        }
        response.addCookie(loginCookie);
    }

    /**
     * 校验token格式和md5是否正确
     *
     * @param token token
     * @return 是否正确token
     */
    private String getLoginUserFromToken(String token) {
        if (StringUtils.isEmpty(token)) {
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

        String countToken = buildToken(arr[0], arr[1]);
        if (countToken.equalsIgnoreCase(token)) {
            return arr[0];
        }
        return null;
    }

    /**
     * 根据用户名和登录时间，计算md5，并拼接token返回
     *
     * @param username 用户名
     * @param date     登录时间
     * @return token
     */
    private String buildToken(String username, String date) {
        String ret = username + TOKEN_SPLIT + date + TOKEN_SPLIT;
        String md5 = StringHelper.md5(ret, TOKEN_SALT, username);
        return ret + md5;
    }

    /**
     * 去ldap验证用户名密码是否正确
     *
     * @param username 用户名
     * @param pwd      密码
     * @return 是否正确
     */
    private boolean validateUser(String username, String pwd) {
        if (!StringUtils.hasLength(username) || !StringUtils.hasLength(pwd)) {
            return false;
        }

        log.debug("用户名: {}, 去ldap认证", username);
        return validateFromLDAP(username, pwd);
    }

    private boolean validateFromLDAP(String username, String pwd) {
        if (username.indexOf("@") < 0 && StringUtils.hasLength(emailDomain)) {
            username += "@" + emailDomain;
        }
        try {
            Object context = SpringUtil.getBean(LdapTemplate.class).getContextSource().getContext(username, pwd);
            log.info(context.toString());
            return true;
        } catch (Exception exp) {
            log.error(exp.getMessage());
            return false;
        }
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

    /**
     * 终止响应，并返回错误信息
     *
     * @param request  请求上下文
     * @param response 响应上下文
     * @param msg      错误信息
     */
    private void endResponse(HttpServletRequest request, HttpServletResponse response, String msg) {
        if (!isAjax(request)) {
            redirect(response, loginPage);
            return;
        }
        response.setContentType("application/json; charset=UTF-8");
        try {
            String ret = "{\"ret\":500, \"msg\":\"" + msg.replaceAll("[\"']", "") + "\"}";
            response.getOutputStream().write(ret.getBytes(StandardCharsets.UTF_8));
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
