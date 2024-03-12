package cn.beinet.codegenerate.configs;

import cn.beinet.codegenerate.configs.logins.RoleType;
import lombok.Data;
import org.springframework.util.StringUtils;

/**
 * Description:
 *
 * @author : youbl
 * @create: 2022/6/9 20:49
 */
@Data
public class AuthDetails {
    private String account;

    private RoleType role;

    private String userAgent;

    public String getAccount() {
        if (!StringUtils.hasLength(account)) {
            return "匿名";
        }
        return account;
    }

    public boolean isAdmin() {
        return role != null && role.equals(RoleType.ADMIN);
    }
}
