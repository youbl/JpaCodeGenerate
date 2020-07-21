package cn.beinet.codegenerate.service;

import cn.beinet.codegenerate.model.ColumnDto;
import cn.beinet.codegenerate.repository.ColumnRepository;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class ModelGenerater {
    private final ColumnRepository columnRepository;

    public ModelGenerater(ColumnRepository columnRepository) {
        this.columnRepository = columnRepository;
    }

    public String generate(String database, String table, String packageName) {
        List<ColumnDto> columns = columnRepository.findColumnByTable(database, table);
        if (columns == null || columns.isEmpty())
            return "";

        StringBuilder sb = new StringBuilder();
        // 头部的package和import
        sb.append(getHead(packageName));

        // class的定义
        sb.append("@Data\n")
                .append("@Entity\n")
                .append("@Table(name = \"").append(table).append("\", catalog = \"").append(database).append("\")\n")
                .append("public class ").append(table).append(" {\n");

        // class的成员
        sb.append(getClassBody(columns));

        sb.append("\n}");
        return sb.toString();
    }

    private String getHead(String packageName) {
        return "package " + packageName + ".model;\n\n" +
                "import lombok.Data;\n\n" +
                "import javax.persistence.*;\n" +
                "import javax.validation.constraints.NotNull;\n" +
                "import javax.validation.constraints.Size;\n" +
                "import java.time.LocalDateTime;\n" +
                "import java.math.BigDecimal;\n\n";
    }

    private String getClassBody(List<ColumnDto> columns) {
        StringBuilder sb = new StringBuilder();
        for (ColumnDto column : columns) {
            sb.append(getSizeAnnotate(column));

            if (column.isPrimaryKey()) {
                sb.append("    @Id\n");
            }

            sb.append(getColumnAnnotate(column));
            sb.append(getGeneratedValueAnnotate(column));
            sb.append(getColumnDefine(column));
        }
        return sb.toString();
    }

    /**
     * 获取字段在Model类里的field定义
     *
     * @param column 对应的列
     * @return 定义串
     */
    private String getColumnDefine(ColumnDto column) {
        StringBuilder sb = new StringBuilder();
        sb.append("    private ")
                .append(getFieldType(column.getType()))
                .append(' ')
                .append(lowFirstChar(column.getColumn()))
                .append(";\n\n");
        return sb.toString();
    }

    private String getFieldType(String type) {
        int idx = type.indexOf('(');
        if (idx > 0) {
            type = type.substring(0, idx);
        }
        idx = type.indexOf(' '); // float unsigned
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
                return "LocalDateTime";
            case "decimal":
                return "BigDecimal";
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
     * 生成 注解 @Column
     *
     * @param column 对应的列
     * @return 注解字符串
     */
    private String getColumnAnnotate(ColumnDto column) {
        StringBuilder sb = new StringBuilder();
        sb.append("    @Column(columnDefinition = \"")
                .append(column.getType())
                .append(" COMMENT '")
                .append(column.getComment().replaceAll("[\\r\\n]", ";"))
                .append("'\"");

        String extra = column.getExtra();
        if (extra != null && extra.toUpperCase().contains("GENERATED")) {
            // 虚拟字段，不能插入和更新
            sb.append(", insertable = false, updatable = false");
        } else {
            String colName = column.getColumn().toLowerCase();
            if (colName.equals("creationtime") || colName.equals("lastmodificationtime")) {
                // 插入时间和更新时间，由数据库自行处理，不让代码处理
                sb.append(", insertable = false, updatable = false");
            }
        }

        sb.append(")\n");
        return sb.toString();
    }


    /**
     * 生成 注解 @GeneratedValue
     *
     * @param column 对应的列
     * @return 注解字符串
     */
    private String getGeneratedValueAnnotate(ColumnDto column) {
        String extra = column.getExtra();
        if (extra == null || !extra.toLowerCase().contains("auto_increment")) {
            return "";
        }
        return "    @GeneratedValue(strategy = GenerationType.IDENTITY)\n";
    }


    /**
     * 生成 注解 @Size
     *
     * @param column 对应的列
     * @return 注解字符串
     */
    private String getSizeAnnotate(ColumnDto column) {
        String type = column.getType();
        if (type == null) {
            return "";
        }
        String chType = "char(";
        int idx = type.toLowerCase().indexOf(chType);
        if (idx < 0) {
            return "";
        }
        type = type.substring(idx + chType.length());
        idx = type.indexOf(')');
        if (idx <= 0) {
            return "";
        }

        return "    @Size(max = " + type.substring(0, idx) + ")\n";
    }

    private static String lowFirstChar(String name) {
        return name.substring(0, 1).toLowerCase() + name.substring(1);
    }
}
