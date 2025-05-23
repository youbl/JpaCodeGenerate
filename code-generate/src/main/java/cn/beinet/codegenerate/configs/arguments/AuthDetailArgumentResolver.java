package cn.beinet.codegenerate.configs.arguments;

import cn.beinet.codegenerate.util.ContextUtil;
import org.springframework.core.MethodParameter;
import org.springframework.web.bind.support.WebDataBinderFactory;
import org.springframework.web.context.request.NativeWebRequest;
import org.springframework.web.method.support.HandlerMethodArgumentResolver;
import org.springframework.web.method.support.ModelAndViewContainer;

/**
 * Description:
 *
 * @author : youbl
 * @since 2022/6/9 20:48
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
        String loginAccount = ContextUtil.getLoginUser();
        ret.setAccount(loginAccount);
        return ret;
    }
}
