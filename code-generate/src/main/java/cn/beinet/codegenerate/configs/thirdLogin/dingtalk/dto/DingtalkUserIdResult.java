package cn.beinet.codegenerate.configs.thirdLogin.dingtalk.dto;

import lombok.Data;

/**
 * 新类
 *
 * @author youbl
 * @since 2024/1/3 16:13
 */
@Data
public class DingtalkUserIdResult {
    private String request_id;
    private int errcode;
    private String errmsg;
    private UserId result;

    @Data
    public static class UserId {
        private String userId;
        private String[] exclusive_account_userid_list;
    }
}
