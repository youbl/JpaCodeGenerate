package cn.beinet.codegenerate.configs.thirdLogin.dingtalk.dto;

import lombok.Data;
import lombok.experimental.Accessors;

/**
 * 钉钉 userAccessToken 接口返回的对象
 *
 * @author youbl
 * @since 2024/1/3 16:24
 */
@Data
@Accessors(chain = true)
public class AccessTokenResult {
    private int expireIn;
    private String refreshToken;
    private String accessToken;
}
