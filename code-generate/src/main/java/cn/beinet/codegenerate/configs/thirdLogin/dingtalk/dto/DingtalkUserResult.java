package cn.beinet.codegenerate.configs.thirdLogin.dingtalk.dto;

import lombok.Data;
import lombok.experimental.Accessors;

/**
 * 钉钉 users 接口返回的用户信息对象
 *
 * @author youbl
 * @since 2024/1/3 16:24
 */
@Data
@Accessors(chain = true)
public class DingtalkUserResult {
    private String nick;
    private String avatarUrl;
    private String mobile;
    private String openId;
    private String unionId;
    private String email;
    private String stateCode;
}
