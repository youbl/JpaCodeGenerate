package cn.beinet.codegenerate.configs.thirdLogin.dingtalk;

import cn.beinet.codegenerate.configs.thirdLogin.dingtalk.dto.*;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

/**
 * 新类
 *
 * @author youbl
 * @since 2024/1/3 16:11
 */
@FeignClient(name = "dingtalkOApi", url = "https://oapi.dingtalk.com")
public interface DingtalkOApiFeign {

    // 接口文档： https://open.dingtalk.com/document/orgapp/obtain-orgapp-token
    // 获取钉钉旧版接口的 accessToken
    @GetMapping("/gettoken")
    GetTokenResult getToken(@RequestParam String appkey, @RequestParam String appsecret);

    // 接口文档： https://open.dingtalk.com/document/orgapp/obtain-the-userid-of-your-mobile-phone-number
    // 根据钉钉旧版接口的 accessToken 和 手机号，找钉钉获取userId
    @PostMapping("/topapi/v2/user/getbymobile")
    DingtalkUserIdResult getUserId(@RequestParam String access_token, DingtalkUserIdDto dto);

    // 接口文档： https://open.dingtalk.com/document/isvapp/queries-the-details-of-a-dedicated-account
    // 根据钉钉旧版接口的 accessToken 和 userId，找钉钉获取企业用户信息
    @PostMapping("/topapi/v2/user/get")
    DingtalkUserInfoResult getUserInfo(@RequestParam String access_token, DingtalkUserInfoDto dto);

}
