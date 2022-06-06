package cn.beinet.codegenerate.model;

import lombok.Data;

import java.time.LocalDateTime;

/**
 * MySQL表信息
 */
@Data
public class TableDto {
    /**
     * 数据库名
     */
    private String table_schema;
    /**
     * 表名
     */
    private String table_name;
    /**
     * 估计的行数
     */
    private Long table_rows;

    /**
     * 平均行大小
     */
    private Long avg_row_length;

    /**
     * 数据占用空间大小
     */
    private Long data_length;

    /**
     * 索引占用空间大小
     */
    private Long index_length;

    /**
     * 下一个自增值
     */
    private Long auto_increment;

    /**
     * 创建时间
     */
    private String create_time;

    /**
     * 表注释
     */
    private String table_comment;
}
