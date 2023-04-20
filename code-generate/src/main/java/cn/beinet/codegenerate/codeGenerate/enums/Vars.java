package cn.beinet.codegenerate.codeGenerate.enums;

/**
 * 新类
 *
 * @author youbl
 * @date 2023/4/19 16:22
 */
public enum Vars {
    /**
     * 生成文件所在的包名
     */
    PACKAGE_NAME("package_name"),
    /**
     * 实际的表名，用于实体类映射到数据库
     */
    TABLE_NAME("table_name"),
    /**
     * 实体类名，首字母大写
     */
    ENTITY_NAME("entity_name"),
    /**
     * 首字母小写的实体类名，用于Controller的路由
     */
    LOW_ENTITY_NAME("low_entity_name"),
    /**
     * DTO文件的body内容
     */
    DTO_FIELDS("dto_fields"),
    /**
     * 生成文件的注释里的时间
     */
    DATE_TIME("date_time"),
    /**
     * 实体类文件的body内容
     */
    ENTITY_FIELDS("entity_fields"),
    ;

    private String val;

    Vars(String val) {
        this.val = val;
    }

    public String getVal() {
        return val;
    }
}
