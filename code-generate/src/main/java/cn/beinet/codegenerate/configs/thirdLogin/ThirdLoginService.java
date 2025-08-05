package cn.beinet.codegenerate.configs.thirdLogin;

import com.fzzixun.etools.oauth.client.AuthService;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.util.Assert;
import org.springframework.util.StringUtils;

import javax.servlet.http.HttpServletRequest;

/**
 * 第三方登录信息配置
 *
 * @author youbl
 * @since 2023/12/26 20:47
 */
@Component
@RequiredArgsConstructor
public class ThirdLoginService {
    private final ThirdLoginInfo thirdLoginInfo;
    private final AuthService authService;

    @Value("${server.servlet.context-path:}")
    private String prefix;

    /**
     * 拼接返回第三方登录的url
     * 这里的格式是钉钉的，
     * 注：钉钉登录对接官方文档： <a href="https://open.dingtalk.com/document/orgapp/tutorial-obtaining-user-personal-information">...</a>
     *
     * @param request 当前请求上下文，用于提取域名，拼接回调地址
     * @return 钉钉登录url
     */
    @SneakyThrows
    public String combineLoginUrl(HttpServletRequest request) {
        Assert.isTrue((StringUtils.hasLength(thirdLoginInfo.getLoginUrl())), "未配置登录url信息");
        Assert.isTrue(StringUtils.hasLength(thirdLoginInfo.getCallbackUrl()), "未配置回调地址");

        String callbackUrl = combinCallbackUrl(thirdLoginInfo.getCallbackUrl(), request);
        String loginUrl = authService.generateOauthLink(callbackUrl, thirdLoginInfo.getCallbackPara());
        System.out.println(loginUrl);
        return loginUrl;
    }

    private String combinCallbackUrl(String url, HttpServletRequest request) {
        if (!StringUtils.hasLength(url)) {
            return "";
        }
        if (url.startsWith("http")) {
            return url;
        }
        if (!url.startsWith("/")) {
            url = "/" + url;
        }
        url = prefix + url;
        String currentUrl = request.getRequestURL().toString();
        String domain = getDomain(currentUrl);
        return domain + url;
    }

    private static String getDomain(String url) {
        int idx = url.indexOf('/', 8); // 跳过https://
        if (idx > 0) {
            return url.substring(0, idx);
        }
        return url;
    }
}
