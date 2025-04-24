package cn.beinet.codegenerate.controller;

import cn.beinet.codegenerate.configs.arguments.AuthDetails;
import cn.beinet.codegenerate.configs.LdapLoginFilter;
import cn.beinet.codegenerate.configs.logins.ImgCodeService;
import cn.beinet.codegenerate.configs.thirdLogin.dingtalk.DingtalkService;
import cn.beinet.codegenerate.configs.thirdLogin.dingtalk.dto.DingtalkUserInfoResult;
import cn.beinet.codegenerate.configs.thirdLogin.dingtalk.dto.DingtalkUserResult;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * 新类
 *
 * @author youbl
 * @since 2023/8/15 14:11
 */
@RestController
@RequiredArgsConstructor
public class LoginController {
    private final ImgCodeService codeService;

    private final DingtalkService dingtalkService;

    private final LdapLoginFilter ldapLoginFilter;

    @Value("${login.emailDomain:}")
    private String emailDomain;


    /**
     * 获取图形验证码
     *
     * @return 图形验证码的图片和序号
     */
    @GetMapping("login/imgcode")
    public ImgCodeService.ImgCodeDto getImgCode() {
        return codeService.getImgCode();
    }


    /**
     * 返回当前登录用户
     * @param authDetails 当前登录上下文
     * @return 登录用户名
     */
    @GetMapping("currentuser")
    public String getLoginUser(AuthDetails authDetails) {
        return authDetails.getAccount();
    }

    /**
     * 钉钉回调地址
     *
     * @param code     暂不清楚用途
     * @param authCode 钉钉认证通过时，回调的authCode，用于获取后续的钉钉token用
     * @param state    暂不清楚用途
     * @return html
     */
    @GetMapping("authCallback")
    @SneakyThrows
    public String authCallback(@RequestParam(required = false) String code,
                               @RequestParam(required = false) String authCode,
                               @RequestParam(required = false) String state) {
        // 因为chrome的同源策略限制，iframe里无法写入cookie，
        // 如果在frame框架内设置cookie，因为浏览器默认使用SameSite=lax，导致cookie不生效，所以要手工设置
        // 但是，SameSite=lax必须搭配Secure属性，而Secure属性仅https服务允许设置，所以http无法设置SameSite=none
        // 参考: https://developer.mozilla.org/zh-CN/docs/Web/HTTP/Headers/Set-Cookie#samesitesamesite-value

        // 因此增加这段代码，钉钉回调时，使用js强制顶部跳转，从而在top写cookie，而不是在iframe写cookie
        return "<script>top.location.href='authCallbackOperation?authCode=" +
                authCode + "';</script>";
    }

    @GetMapping("authCallbackOperation")
    @SneakyThrows
    public String authCallbackOperation(@RequestParam(required = false) String authCode,
                                        HttpServletRequest request,
                                        HttpServletResponse response) {
        String accessToken = dingtalkService.getUserToken(authCode);
        DingtalkUserResult userInfo = dingtalkService.getUserInfo(accessToken);
        DingtalkUserInfoResult userInfo2 = dingtalkService.getUserInfoByMobile(accessToken, userInfo.getMobile());
        //return new Object[]{userInfo, userInfo2};

        if (userInfo2 == null || userInfo2.getResult() == null || userInfo2.getResult().getOrg_email() == null)
            return "未成功获取用户邮箱";

        String email = userInfo2.getResult().getOrg_email();
        if (!emailDomain.startsWith("@")) {
            emailDomain = "@" + emailDomain;
        }
        int idx = email.indexOf(emailDomain);
        if (idx < 0) {
            return email + "域名不匹配: " + emailDomain;
        }

        // cookie里不要放域名
        String username = email.substring(0, idx);
        ldapLoginFilter.addToken(username, request, response);
        ldapLoginFilter.redirect(response, "index.html");
        return "ok";
    }
}
