package cn.beinet.codegenerate.configs.thirdLogin.dingtalk;

import cn.beinet.codegenerate.configs.thirdLogin.ThirdLoginInfo;
import cn.beinet.codegenerate.configs.thirdLogin.dingtalk.dto.*;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.util.Assert;

/**
 * 新类
 *
 * @author youbl
 * @since 2024/1/3 15:35
 */
@Service
@RequiredArgsConstructor
public class DingtalkService {
    private final ThirdLoginInfo thirdLoginInfo;
    private final DingtalkFeign dingtalkFeign;
    private final DingtalkOApiFeign dingtalkOApiFeign;

    /**
     * 根据回调url里的authCode，换取accessToken
     */
    public String getUserToken(String authCode) {
        DingtalkDto dto = new DingtalkDto()
                .setClientId(thirdLoginInfo.getAppKey())
                .setClientSecret(thirdLoginInfo.getAppSecret())
                .setCode(authCode)
                .setGrantType("authorization_code");
        AccessTokenResult ret = dingtalkFeign.userAccessToken(dto);
        // 返回值参考： {"expireIn":7200,"accessToken":"aaaaa","refreshToken":"bbbbb"}
        System.out.println(ret);
        return ret.getAccessToken();
    }

    /**
     * 根据 getUserToken 方法的返回值，获取用户信息
     */
    public DingtalkUserResult getUserInfo(String accessToken) {
        DingtalkUserResult ret = dingtalkFeign.users(accessToken);
        // 返回值参考：
        // {"nick":"北亮","unionId":"aaa","avatarUrl":"https://static-legacy.dingtalk.com/media/bbb","openId":"ccc","mobile":"15999999999","stateCode":"86","email":"youbl@126.com"}
        System.out.println(ret);
        return ret;
    }

    public DingtalkUserInfoResult getUserInfoByMobile(String accessToken, String mobile) {
        GetTokenResult oapiToken = dingtalkOApiFeign.getToken(thirdLoginInfo.getAppKey(), thirdLoginInfo.getAppSecret());
        // 返回值参考： {"errcode":0,"access_token":"abc","errmsg":"ok","expires_in":7200}
        System.out.println(oapiToken);
        DingtalkUserIdDto dto = new DingtalkUserIdDto()
                .setMobile(mobile)
                .setSupport_exclusive_account_search(false);
        // 返回值参考：
        // {"errcode":0,"errmsg":"ok","result":{"exclusive_account_userid_list":[],"userid":"11"},"request_id":"abc"}
        DingtalkUserIdResult ret = dingtalkOApiFeign.getUserId(oapiToken.getAccess_token(), dto);
        System.out.println(ret);
        Assert.notNull(ret.getResult(), "根据手机号，未找到钉钉userId");
        String userId = ret.getResult().getUserId();

        DingtalkUserInfoDto infoDto = new DingtalkUserInfoDto()
                .setLanguage("zh_CN")
                .setUserid(userId);
        DingtalkUserInfoResult result = dingtalkOApiFeign.getUserInfo(oapiToken.getAccess_token(), infoDto);
        System.out.println(result);
        return result;
    }
}
