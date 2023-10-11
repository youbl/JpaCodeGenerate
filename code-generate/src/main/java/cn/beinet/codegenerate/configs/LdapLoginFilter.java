package cn.beinet.codegenerate.configs;

import cn.beinet.codegenerate.configs.logins.ImgCodeService;
import cn.beinet.codegenerate.configs.logins.Validator;
import cn.beinet.codegenerate.util.RequestHelper;
import cn.beinet.codegenerate.util.SpringUtil;
import cn.beinet.codegenerate.util.TokenHelper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.annotation.Order;
import org.springframework.ldap.core.LdapTemplate;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.context.request.WebRequest;
import org.springframework.web.filter.OncePerRequestFilter;

import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
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
@Order(-999) // 需要设置登录信息，所以filter的顺序要高一些
public class LdapLoginFilter extends OncePerRequestFilter {
    @Value("${server.servlet.context-path}")
    private String prefix;

    @Value("${spring.ldap.email-domain}")
    private String emailDomain;

    private static final String LOGIN_INFO = "loginUser";

    // token cookie名
    @Value("${token.cookie-name:beinetUser}")
    private String TOKEN_COOKIE_NAME;

    // 登录用户名使用的字段名
    private static final String USER_PARA = "beinetUser";
    // 登录密码使用的字段名
    private static final String PWD_PARA = "beinetPwd";

    // 登录输入页地址
    public static final String loginPage = "/login.html";
    // 登录认证地址
    private static final String loginActionPage = "/login";

    private final List<Validator> validatorList;
    private final ImgCodeService codeService;

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
        log.debug("url:{} token: {}", url, token);
        String loginUser = TokenHelper.getLoginUserFromToken(token);
        if (!StringUtils.hasLength(loginUser)) {
            log.debug("token无效: {}", token);
            addToken("", request, response);
            if (StringUtils.isEmpty(token)) {
                endResponse(request, response, "未登录");
            } else {
                endResponse(request, response, "token无效");
            }
            return;
        }

        // 添加登录后的信息
        request.setAttribute(LOGIN_INFO, loginUser);

        log.debug("token校验成功: {}", token);
        filterChain.doFilter(request, response);
    }

    public static String getLoginInfo(WebRequest request) {
        Object ret = request.getAttribute(LOGIN_INFO, 0);
        if (ret == null)
            return "";
        return ret.toString();
    }

    public static String getLoginInfo(ServletRequest request) {
        Object ret = request.getAttribute(LOGIN_INFO);
        if (ret == null)
            return "";
        return ret.toString();
    }

    /**
     * 提取用户名密码，并进行登录
     *
     * @param request  请求上下文
     * @param response 响应上下文
     */
    private void processLoginRequest(HttpServletRequest request, HttpServletResponse response) {
        if (!validateCode(request)) {
            addToken("", request, response);
            endResponse(request, response, "验证码错误");
            return;
        }

        String username = request.getParameter(USER_PARA);
        String pwd = request.getParameter(PWD_PARA);
        log.debug("用户名: {}, 准备登录", username);
        if (!validateUser(username, pwd)) {
            log.debug("用户名: {}, 账号或密码错误", username);
            //throw new RuntimeException("账号或密码错误");
            addToken("", request, response);
            endResponse(request, response, "账号或密码错误");
            return;
        }

        log.debug("用户名: {}, ldap认证成功", username);
        addToken(username, request, response);
        redirect(response, "index.html");
    }

    private boolean validateCode(HttpServletRequest request) {
        String sn = request.getParameter("beinetCodeSn");
        String code = request.getParameter("beinetCode");
        return (codeService.validImgCode(sn, code));
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
    private void addToken(String username, HttpServletRequest request, HttpServletResponse response) {
        Cookie loginCookie = new Cookie(TOKEN_COOKIE_NAME, "");
        loginCookie.setPath("/");
        if (username.isEmpty()) {
            loginCookie.setMaxAge(0);
        } else {
            loginCookie.setMaxAge(7 * 24 * 3600);
            String token = TokenHelper.buildToken(username);
            log.debug("登录成功:{}", token);
            loginCookie.setValue(token);
        }
        // 设置为二级域名用，便于跨域统一登录
        loginCookie.setDomain(RequestHelper.getBaseDomain(request));
        response.addCookie(loginCookie);
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
