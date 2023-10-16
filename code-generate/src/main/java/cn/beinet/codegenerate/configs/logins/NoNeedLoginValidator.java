package cn.beinet.codegenerate.configs.logins;

import cn.beinet.codegenerate.configs.LdapLoginFilter;
import org.springframework.stereotype.Component;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * 那些不需要登录的url验证
 *
 * @author youbl
 * @since 2023/1/4 18:18
 */
@Component
public class NoNeedLoginValidator implements Validator {
    // 无须登录认证的url正则
    private static final Pattern patternRequest =
            Pattern.compile("(?i)^/(actuator|githook|login|test)/?|" +
                    "(menu.html|menuGroup|\\.(ico|jpg|png|bmp|txt|xml|js|css|ttf|woff|map|svg))$");// |html?

    @Override
    public int getOrder() {
        return -999;
    }

    @Override
    public boolean validated(HttpServletRequest request, HttpServletResponse response) {
        //request.getRequestURL() 带有域名，所以不用
        //request.getRequestURI() 带有ContextPath，所以不用
        String url = request.getServletPath();
        // 登录页跳过
        if (url.endsWith(LdapLoginFilter.loginPage))
            return true;

        Matcher matcher = patternRequest.matcher(url);
        return matcher.find();
    }

}
