package cn.beinet.codegenerate.configs.thirdLogin.dingtalk.dto;

import lombok.Data;
import lombok.experimental.Accessors;

/**
 * 钉钉 https://oapi.dingtalk.com/gettoken 接口返回的对象
 *
 * @author youbl
 * @since 2024/1/3 16:24
 */
@Data
@Accessors(chain = true)
public class GetTokenResult {
    private int expires_in;
    private String access_token;
    private String errmsg;
    private int errcode;
}
