package cn.beinet.codegenerate.configs.thirdLogin.dingtalk;

import cn.beinet.codegenerate.configs.thirdLogin.dingtalk.dto.AccessTokenResult;
import cn.beinet.codegenerate.configs.thirdLogin.dingtalk.dto.DingtalkDto;
import cn.beinet.codegenerate.configs.thirdLogin.dingtalk.dto.DingtalkUserResult;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestHeader;

/**
 * 新类
 *
 * @author youbl
 * @since 2024/1/3 16:11
 */
@FeignClient(name = "dingtalkApi", url = "https://api.dingtalk.com")
public interface DingtalkFeign {
    // 接口文档： https://open.dingtalk.com/document/orgapp/obtain-user-token
    // 根据回调的authCode，找钉钉获取accessToken
    @PostMapping("/v1.0/oauth2/userAccessToken")
    AccessTokenResult userAccessToken(DingtalkDto dto);

    // 接口文档 https://open.dingtalk.com/document/isvapp/dingtalk-retrieve-user-information
    // 根据accessToken，找钉钉查询个人信息
    @GetMapping("/v1.0/contact/users/me")
    DingtalkUserResult users(@RequestHeader("x-acs-dingtalk-access-token") String accessToken);
}
