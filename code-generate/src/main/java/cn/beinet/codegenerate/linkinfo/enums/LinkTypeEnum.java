package cn.beinet.codegenerate.linkinfo.enums;

import lombok.Getter;

/**
 * 连接类型枚举
 * @author youbl
 * @since 2025/4/24 21:00
 */
public enum LinkTypeEnum {
    // 正常
    NORMAL(0),
    // 仅管理员
    ADMIN(1),
    ;

    @Getter
    private Integer value;

    LinkTypeEnum(int value) {
        this.value = value;
    }

    public boolean match(Integer value) {
        return this.value.equals(value);
    }
}
