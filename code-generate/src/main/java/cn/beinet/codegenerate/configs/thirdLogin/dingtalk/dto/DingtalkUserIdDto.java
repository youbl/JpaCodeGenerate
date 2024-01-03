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
public class DingtalkUserIdDto {
    private String mobile;
    private boolean support_exclusive_account_search;
}
