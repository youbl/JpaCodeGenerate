package cn.beinet.codegenerate.model;

import lombok.Data;
import org.springframework.util.StringUtils;

/**
 * MySQL表结构获取生成的列对象
 */
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
     * 原始的表名
     */
    private String originTable;

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
     * 是否允许空
     */
    private boolean nullable;
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
     * MySQL8.0不返回整型长度，所以这里要替换，兼容8.0跟5.7对比
     *
     * @return 类型
     */
    public String getType() {
        if (!StringUtils.hasLength(type))
            return "";

        int idx = type.indexOf('(');
        if (idx < 0)
            return type;
        String[] numType = new String[]{
                "int",
                "bigint",
                "tinyint",
                "smallint",
        };
        for (String numTp : numType) {
            if (type.startsWith(numTp)) {
                type = type.replaceAll("\\([^)]*\\)", "");
                break;
            }
        }
        return type;
    }

    /**
     * 字段注释
     */
    private String comment;

    /**
     * 获取当前字段定义的长度，
     * 注：只有 char和varchar才返回
     *
     * @return 长度
     */
    public int getSize() {
        String strType = getType();
        if (!StringUtils.hasLength(strType))
            return 0;

        // 只有 char和varchar，才返回长度
        String chType = "char(";
        int idx = strType.toLowerCase().indexOf(chType);
        if (idx < 0) {
            return 0;
        }
        strType = strType.substring(idx + chType.length());
        idx = strType.indexOf(')');
        if (idx <= 0) {
            return 0;
        }

        return Integer.parseInt(strType.substring(0, idx));
    }

    /**
     * 获取当前字段映射到的Java类型，如果是基本类型，要转换为引用类型，
     * 如 int 要转换为 Integer
     *
     * @return Java类型
     */
    public String getManagerType() {
        String strType = getFieldType();
        switch (strType) {
            case "long":
                return "Long";
            case "boolean":
                return "Boolean";
            case "double":
                return "Double";
            case "float":
                return "Float";
            case "int":
                return "Integer";
            default:
                return strType;
        }
    }

    /**
     * 获取当前字段映射到的Java类型
     *
     * @return Java类型
     */
    private String getFieldType() {
        String type = getType();
        if (!StringUtils.hasLength(type))
            return "String";

        int idx = type.indexOf('(');
        if (idx > 0) {
            type = type.substring(0, idx);
        }
        idx = type.indexOf(' '); // 比如类型为：float unsigned
        if (idx > 0) {
            type = type.substring(0, idx);
        }
        switch (type.toLowerCase()) {
            case "bigint":
                return "long";
            case "bit":
                return "boolean";
            case "date":
            case "datetime":
            case "timestamp":
                return "java.time.LocalDateTime";
            case "decimal":
                return "java.math.BigDecimal";
            case "double":
                return "double";
            case "float":
                return "float";
            case "int":
            case "mediumint":
            case "smallint":
            case "tinyint":
                return "int";
            default: // varchar char blob text enum longblob longtext mediumblob mediumtext tinytext varbinary json
                return "String";
        }
    }

    /**
     * 当前字段是否虚拟字段
     *
     * @return true false
     */
    public boolean isVirtual() {
        String strExtra = getExtra();
        return (strExtra != null && strExtra.toUpperCase().contains("GENERATED"));
    }

    /**
     * 当前字段的值是否由DB管理，
     * 不允许程序修改
     *
     * @return true false
     */
    public boolean isDbManager() {
        String colName = getColumn().toLowerCase();
        return (colName.equals("creationtime") || colName.equals("lastmodificationtime"));
    }

    /**
     * 当前字段的值是否自增
     *
     * @return true false
     */
    public boolean isAuto() {
        String strExtra = getExtra();
        return (strExtra != null && strExtra.toLowerCase().contains("auto_increment"));
    }

}
