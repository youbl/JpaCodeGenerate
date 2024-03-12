package cn.beinet.codegenerate.configs;

import cn.beinet.codegenerate.configs.logins.RoleType;
import org.springframework.core.MethodParameter;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.support.WebDataBinderFactory;
import org.springframework.web.context.request.NativeWebRequest;
import org.springframework.web.method.support.HandlerMethodArgumentResolver;
import org.springframework.web.method.support.ModelAndViewContainer;

/**
 * Description:
 *
 * @author : youbl
 * @create: 2022/6/9 20:48
 */
public class AuthDetailArgumentResolver implements HandlerMethodArgumentResolver {
    @Override
    public boolean supportsParameter(MethodParameter parameter) {
        return parameter.getParameterType().isAssignableFrom(AuthDetails.class);
    }

    @Override
    public Object resolveArgument(MethodParameter parameter, ModelAndViewContainer mavContainer, NativeWebRequest webRequest, WebDataBinderFactory binderFactory) throws Exception {
        AuthDetails ret = new AuthDetails();

        // 调用者的信息
        ret.setUserAgent(webRequest.getHeader("user-agent"));

//        Principal principal = webRequest.getUserPrincipal();
//        if (principal == null) {
//            return ret;
//        }
//        ret.setAccount(principal.getName());
        String loginAccount = LdapLoginFilter.getLoginInfo(webRequest);
        ret.setAccount(loginAccount);
        ret.setRole(getRole(loginAccount));

        return ret;
    }

    /**
     * 根据登录账号，获取对应的角色
     *
     * @param account 账号
     * @return 角色
     */
    private RoleType getRole(String account) {
        if (!StringUtils.hasLength(account) || account.equals("匿名"))
            return RoleType.ANONYMOUS;

        if (account.startsWith("beiliang_you"))
            return RoleType.ADMIN;

        return RoleType.MEMBER;
    }
}
