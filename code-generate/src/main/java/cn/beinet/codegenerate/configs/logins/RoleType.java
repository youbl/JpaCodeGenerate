package cn.beinet.codegenerate.configs.logins;

import lombok.Getter;

/**
 * 账号角色枚举
 *
 * @author youbl
 * @since 2024/3/12 10:18
 */
@Getter
public enum RoleType {
    ANONYMOUS(0, "匿名"),

    MEMBER(1, "成员"),
    ADMIN(9, "管理员"),
    ;

    private final int code;
    private final String name;

    RoleType(int code, String name) {
        this.code = code;
        this.name = name;
    }
}
