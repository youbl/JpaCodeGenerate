package cn.beinet.codegenerate.configs.thirdLogin.dingtalk.dto;

import lombok.Data;
import lombok.experimental.Accessors;

/**
 * 新类
 *
 * @author youbl
 * @since 2024/1/3 16:13
 */
@Data
@Accessors(chain = true)
public class DingtalkDto {
    private String clientId;
    private String clientSecret;
    private String code;
    private String refreshToken;
    private String grantType;
}
