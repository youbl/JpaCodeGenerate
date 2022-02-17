package cn.beinet.codegenerate.configs;

import cn.beinet.codegenerate.util.RequestHelper;
import cn.beinet.codegenerate.util.SpringUtil;
import cn.beinet.codegenerate.util.StringHelper;
import org.springframework.ldap.core.LdapTemplate;
import org.springframework.ldap.filter.EqualsFilter;
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
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Description:
 *
 * @author : youbl
 * @create: 2022/2/16 16:34
 */
@Component
public class LdapLoginFilter extends OncePerRequestFilter {

    // token校验md5的盐值
    private static final String TOKEN_SALT = "beinet.cn";

    private static final String TOKEN_COOKIE_NAME = "beinetUser";
    private static final String TOKEN_SPLIT = ":";

    private static final String USER_PARA = "beinetUser";
    private static final String PWD_PARA = "beinetPwd";

    private static final String loginPage = "login.html";
    private static final String loginActionPage = "login";
    private static final Pattern patternRequest = Pattern.compile("(?i)^/actuator/?|\\.(ico|jpg|png|bmp|txt|xml|js|css|ttf|woff|map)$");// |html?

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain) throws ServletException, IOException {
        String url = request.getRequestURI(); //request.getRequestURL() 带有域名，所以不用

        // 验证用户名密码
        if (url.endsWith(loginActionPage)) {
            processLoginRequest(request, response);
            return;
        }

        // 不需要登录的页面
        if (!needValidation(url)) {//!logger.isDebugEnabled() ||
            filterChain.doFilter(request, response);
            return;
        }

        // 判断cookie是否有效
        String token = RequestHelper.getCookie(TOKEN_COOKIE_NAME, request);
        if (!validateToken(token)) {
            addToken("", response);
            if (StringUtils.isEmpty(token)) {
                endResponse(response, "未登录");
            } else {
                endResponse(response, "token无效");
            }
            return;
        }

        filterChain.doFilter(request, response);
    }


    private boolean needValidation(String url) {
        // 登录页跳过
        if (url.endsWith(loginPage))
            return false;

        Matcher matcher = patternRequest.matcher(url);
        return !matcher.find();
    }

    private void processLoginRequest(HttpServletRequest request, HttpServletResponse response) {
        String username = request.getParameter(USER_PARA);
        String pwd = request.getParameter(PWD_PARA);
        if (!validateUser(username, pwd)) {
            //throw new RuntimeException("账号或密码错误");
            addToken("", response);
            endResponse(response, "账号或密码错误");
            return;
        }

        addToken(username, response);
        redirect(request, response);
    }

    private void redirect(HttpServletRequest request, HttpServletResponse response) {
        String url = request.getRequestURI(); //request.getRequestURL() 带有域名，所以不用
        if (url.endsWith(loginActionPage)) {
            response.setStatus(302);
            response.setHeader("Location", "index.html");//设置新请求的URL
        }
    }

    private void addToken(String username, HttpServletResponse response) {
        String now = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMddHHmmss"));

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

    private boolean validateToken(String token) {
        if (StringUtils.isEmpty(token)) {
            return false;
        }
        // 0为用户名，1为时间，2为md5
        String[] arr = token.split(TOKEN_SPLIT);
        if (arr.length != 3 || arr[0].isEmpty() || arr[1].isEmpty()) {
            return false;
        }
        String countToken = buildToken(arr[0], arr[1]);
        return (countToken.equalsIgnoreCase(token));
    }

    private String buildToken(String username, String date) {
        String ret = username + TOKEN_SPLIT + date + TOKEN_SPLIT;
        String md5 = StringHelper.md5(ret, TOKEN_SALT);
        return ret + md5;
    }

    private boolean validateUser(String username, String pwd) {
        if (!StringUtils.hasLength(username) || !StringUtils.hasLength(pwd)) {
            return false;
        }

        String filter = new EqualsFilter("samAccountName", username).toString();
        LdapTemplate ldapTemplate = SpringUtil.getBean(LdapTemplate.class);
//        Object obj = ldapTemplate.search("", filter, new AttributesMapper() {
//            @Override
//            public Object mapFromAttributes(Attributes attributes) throws NamingException {
//                return null;
//            }
//        });
        return ldapTemplate.authenticate("", filter, pwd);
    }

    private void endResponse(HttpServletResponse response, String msg) {
        response.setContentType("application/json; charset=UTF-8");
        try {
            String ret = "{\"ret\":500, \"msg\":\"" + msg.replaceAll("[\"']", "") + "\"}";
            response.getOutputStream().write(ret.getBytes(StandardCharsets.UTF_8));
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
