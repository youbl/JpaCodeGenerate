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

    /**
     * Mybatis-plus的service文件条件字段内容
     */
    SERVICE_COND_FIELDS("service_cond_fields"),

    /**
     * HTML文件的字段内容，用于页面遍历生成检索框和编辑框
     */
    HTML_FIELDS("html_fields");

    private String val;

    Vars(String val) {
        this.val = val;
    }

    public String getVal() {
        return val;
    }
}
