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

    private static final String TOKEN_COOKIE_NAME = "zxuser";
    private static final String TOKEN_SPLIT = ":";

    private static final String USER_PARA = "zxuser";
    private static final String PWD_PARA = "zxpwd";

    static Pattern patternRequest = Pattern.compile("(?i)^/actuator/?|\\.(ico|jpg|png|bmp|txt|xml|js|css|ttf|woff|map)$");// |html?

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain) throws ServletException, IOException {
        if (isNotApiRequest(request)) {//!logger.isDebugEnabled() ||
            filterChain.doFilter(request, response);
            return;
        }

        String token = RequestHelper.getCookie(TOKEN_COOKIE_NAME, request);
        if (!validateToken(token)) {
            String username = request.getParameter(USER_PARA);
            String pwd = request.getParameter(PWD_PARA);
            if (!validateUser(username, pwd)) {
                //throw new RuntimeException("账号或密码错误");
                endResponse(response, "账号或密码错误");
                return;
            }

            addToken(username, response);
        }

        filterChain.doFilter(request, response);
    }


    private boolean isNotApiRequest(HttpServletRequest request) {
        String url = request.getRequestURI(); //request.getRequestURL() 带有域名，所以不用
        Matcher matcher = patternRequest.matcher(url);
        return matcher.find();
    }

    private void addToken(String username, HttpServletResponse response) {
        String now = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMddHHmmss"));
        String token = buildToken(username, now);

        Cookie loginCookie = new Cookie(TOKEN_COOKIE_NAME, token);
        loginCookie.setPath("/");
        loginCookie.setMaxAge(7 * 24 * 3600);
        response.addCookie(loginCookie);
    }

    private boolean validateToken(String token) {
        if (StringUtils.isEmpty(token)) {
            return false;
        }
        String[] arr = token.split(TOKEN_SPLIT);
        if (arr.length != 3) {
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
            String ret = "{'ret':500, 'msg':'" + msg.replaceAll("'", "") + "'}";
            response.getOutputStream().write(ret.getBytes(StandardCharsets.UTF_8));
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
