package cn.beinet.codegenerate.model;

import lombok.Data;

@Data
public class ColumnDto {
    /**
     * 数据库名
     */
    private String catalog;
    /**
     * 表名
     */
    private String table;
    /**
     * 字段名
     */
    private String column;
    /**
     * 这一列在表定义里的序号
     */
    private long position;
    /**
     * 是否主键
     */
    private boolean primaryKey;
    /**
     * 字段默认值
     */
    private String defaultVal;
    /**
     * 字段扩展信息，如
     * auto_increment
     * on update CURRENT_TIMESTAMP
     * VIRTUAL GENERATED
     * STORED GENERATED
     */
    private String extra;
    /**
     * 字段类型
     */
    private String type;
    /**
     * 字段注释
     */
    private String comment;
}
