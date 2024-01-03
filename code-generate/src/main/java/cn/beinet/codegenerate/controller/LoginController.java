package cn.beinet.codegenerate.controller;

import cn.beinet.codegenerate.configs.AuthDetails;
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

    @Value("${spring.ldap.email-domain:}")
    private String emailDomain;


    /**
     * 获取图形验证码
     *
     * @return
     */
    @GetMapping("login/imgcode")
    public ImgCodeService.ImgCodeDto getImgCode() {
        return codeService.getImgCode();
    }


    // 返回当前登录用户
    @GetMapping("currentuser")
    public String getLoginUser(AuthDetails authDetails) {
        return authDetails.getAccount();
    }

    /**
     * 钉钉回调地址
     *
     * @param code
     * @param authCode
     * @param state
     * @return
     */
    @GetMapping("authCallback")
    @SneakyThrows
    public String authCallback(@RequestParam(required = false) String code,
                               @RequestParam(required = false) String authCode,
                               @RequestParam(required = false) String state,
                               HttpServletRequest request,
                               HttpServletResponse response) {
        String accessToken = dingtalkService.getUserToken(authCode);
        DingtalkUserResult userInfo = dingtalkService.getUserInfo(accessToken);
        DingtalkUserInfoResult userInfo2 = dingtalkService.getUserInfoByMobile(accessToken, userInfo.getMobile());
        //return new Object[]{userInfo, userInfo2};

        if (userInfo2 == null || userInfo2.getResult() == null || userInfo2.getResult().getOrg_email() == null)
            return "未成功获取用户邮箱";

        String email = userInfo2.getResult().getOrg_email();
        if (email.indexOf(emailDomain) > 0) {
            LdapLoginFilter.addToken(email, request, response);
            LdapLoginFilter.redirect(response, "index.html");
            return "ok";
        }

        return email + "域名不匹配: " + emailDomain;
    }
}
