package cn.beinet.codegenerate.configs;

import cn.beinet.codegenerate.configs.logins.ImgCodeService;
import cn.beinet.codegenerate.configs.logins.Validator;
import cn.beinet.codegenerate.configs.thirdLogin.ThirdLoginService;
import cn.beinet.codegenerate.service.SaltService;
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

import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
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
public class LdapLoginFilter extends BaseFilter {
    // 要不要登录
    @Value("${login.needLogin:1}")
    private int needLogin;

    @Value("${spring.ldap.email-domain}")
    private String emailDomain;

    private static final String LOGIN_INFO = "loginUser";

    // 登录用户名使用的字段名
    private static final String USER_PARA = "beinetUser";
    // 登录密码使用的字段名
    private static final String PWD_PARA = "beinetPwd";

    // 登录认证地址
    private static final String loginActionPage = "/login";

    private final List<Validator> validatorList;
    private final ImgCodeService codeService;

    private final ThirdLoginService thirdLoginInfo;

    // 用于获取token的md5加密随机盐值，确保安全性
    private final SaltService saltService;

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain) throws ServletException, IOException {
        if (needLogin == 0) {
            request.setAttribute(LOGIN_INFO, "匿名");
            filterChain.doFilter(request, response);
            return;
        }
        //request.getRequestURL() 带有域名，所以不用
        //request.getRequestURI() 带有ContextPath，所以不用
        String url = request.getServletPath();
        //log.debug("收到请求: {}", url);

        if (url.endsWith(loginActionPage)) {
            // 验证LDAP的用户名密码
            try {
                processLoginRequest(request, response);
            } catch (Exception exp) {
                log.error("登录出错:" + exp.getMessage());
                endResponse(request, response, "认证出错，请与管理员联系", thirdLoginInfo.combineLoginUrl(request));
            }
            return;
        }

        for (Validator item : validatorList) {
            Validator.Result result = item.validated(request, response);
            if (result.isPassed()) {
                // 添加登录后的信息
                request.setAttribute(LOGIN_INFO, result.getAccount());
                filterChain.doFilter(request, response);
                return;
            }
        }

        // 所有校验均失败，清空登录cookie
        addToken("", request, response);
        endResponse(request, response, "请重新登录", thirdLoginInfo.combineLoginUrl(request));
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
            endResponse(request, response, "验证码错误", thirdLoginInfo.combineLoginUrl(request));
            return;
        }

        String username = request.getParameter(USER_PARA);
        String pwd = request.getParameter(PWD_PARA);
        log.debug("用户名: {}, 准备登录", username);
        if (!validateUser(username, pwd)) {
            log.debug("用户名: {}, 账号或密码错误", username);
            //throw new RuntimeException("账号或密码错误");
            addToken("", request, response);
            endResponse(request, response, "账号或密码错误", thirdLoginInfo.combineLoginUrl(request));
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
     * 为响应添加登录cookie
     *
     * @param username 用户名，为空表示删除token
     * @param response 响应上下文
     */
    public void addToken(String username, HttpServletRequest request, HttpServletResponse response) {
        Cookie loginCookie = new Cookie(TokenHelper.getTokenCookieName(), "");
        loginCookie.setPath("/");
        if (username.isEmpty()) {
            loginCookie.setMaxAge(0);
        } else {
            loginCookie.setMaxAge(Consts.LOGIN_KEEP_SECOND);
            String token = TokenHelper.buildNewToken(username, saltService.getSalt());
            log.debug("登录成功:{}", token);
            loginCookie.setValue(token);
        }

        // 需要注意的是，在nginx里转发，要配置 proxy_set_header Host $host;
        // 否则这里拿到的只会是 localhost
        String baseDomain = RequestHelper.getBaseDomain(request);
        // 设置为二级域名用，便于跨域统一登录
        loginCookie.setDomain(baseDomain);
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
        username = username.trim();
        pwd = pwd.trim();
        if (username.equalsIgnoreCase(Consts.getSdkAppKey()) &&
                pwd.equals(Consts.getSdkSecurityKey())) {
            log.debug("使用SDK直接登录");
            return true;
        }

        log.debug("用户名: {}, 去ldap认证", username);
        return validateFromLDAP(username, pwd);
    }

    private boolean validateFromLDAP(String username, String pwd) {
        if (username.indexOf("@") < 0 && StringUtils.hasLength(emailDomain)) {
            username += "@" + emailDomain;
        }
        Object context = SpringUtil.getBean(LdapTemplate.class).getContextSource().getContext(username, pwd);
        log.info(context.toString());
        return true;
    }
}
