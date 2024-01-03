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
public class DingtalkUserInfoResult {
    private String request_id;
    private int errcode;
    private String errmsg;
    private UserInfo result;

    @Data
    public static class UserInfo {
        private String userId;
        private String unionid;
        private String name;
        private String avatar;
        private String state_code;
        private String manager_userid;
        private String mobile;
        private boolean hide_mobile;
        private String telephone;
        private String job_number;
        private String title;
        private String email;
        private String work_place;
        private String remark;
        private String login_id;
        private String exclusive_account_type;
        private String exclusive_account;
        private String org_email;
        private int[] dept_id_list;
        private Object[] dept_order_list;
        private String extension;
        private long hired_date;
        private boolean active;
        private boolean real_authed;
        private String org_email_type;
        private String nickname;
        private boolean senior;
        private boolean admin;
        private boolean boss;
        private Object[] leader_in_dept;
        private Object[] role_list;
        private Object union_emp_ext;
        private String exclusive_account_corp_name;
        private String exclusive_account_corp_id;
    }
}
