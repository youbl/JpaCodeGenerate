package cn.beinet.codegenerate.model;

import cn.beinet.codegenerate.util.StringHelper;
import lombok.Data;

@Data
public class IndexDto {
    /**
     * 数据库名
     */
    private String catalog;
    /**
     * 设置首字段大写的表名
     */
    private String table;
    /**
     * 原始的表名
     */
    private String originTable;

    /**
     * 首字母大写后设置表名（类名要首字母大写）
     */
    public void setTable(String table) {
        this.originTable = table;
        this.table = StringHelper.upFirstChar(table);
    }

    /**
     * 索引名
     */
    private String indexName;

    /**
     * 索引中的顺序号
     */
    private int index;

    /**
     * 设置首字母小写的字段名
     */
    private String column;
    /**
     * 原始的字段名
     */
    private String originColumn;

    /**
     * 首字母小写后设置字段名（类的field名要首字母小写）
     */
    public void setColumn(String column) {
        this.originColumn = column;
        this.column = StringHelper.lowFirstChar(column);
    }

    /**
     * 大于0表示前缀索引的长度
     */
    private int subPart;
    /**
     * 是否唯一索引
     */
    private boolean unique;
    /**
     * 索引类型, BTREE，FULLTEXT，HASH，RTREE
     */
    private String indexType;
    /**
     * 注释
     */
    private String comment;

}
