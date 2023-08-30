package cn.beinet.codegenerate.codeGenerate.service.jpaGenerater;

import cn.beinet.codegenerate.codeGenerate.dto.GenerateDto;
import cn.beinet.codegenerate.codeGenerate.dto.GenerateResult;
import cn.beinet.codegenerate.codeGenerate.enums.GenerateType;
import cn.beinet.codegenerate.codeGenerate.service.commonGenerater.Generater;
import cn.beinet.codegenerate.model.ColumnDto;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * Model类生成工具
 */
@Component
public class ModelGenerater implements Generater {
    @Override
    public GenerateType getType() {
        return GenerateType.JPA;
    }

    @Override
    public GenerateResult generate(List<ColumnDto> columns, GenerateDto generateDto) {
        StringBuilder sb = new StringBuilder();
        // 头部的package和import
        sb.append(getHead(generateDto.getPackageName()));

        String database = columns.get(0).getCatalog();
        String table = columns.get(0).getTable();

        // class的定义
        sb.append("@Data\n")
                .append("@AllArgsConstructor\n")
                .append("@NoArgsConstructor\n")
                .append("@Builder\n")
                .append("@DynamicInsert\n")
                .append("@DynamicUpdate\n")
                .append("@Entity\n")
                .append("@Table(name = \"").append(table).append("\", catalog = \"").append(database).append("\")\n")
                .append("public class ").append(table).append(" {\n");

        // class的成员
        sb.append(getClassBody(columns));

        sb.append(mapToDto(columns));

        sb.append("\n/*")
                .append(getInsertSQL(columns))
                .append(getUpdateSQL(columns))
                .append("*/");
        sb.append("\n}");

        //todo JPA整体需改造
        return null;
    }

    @Override
    public String getTemplateName() {
        return "static/template/jpa_entity.template";
    }

    @Override
    public String getTargetDirName() {
        return "model";
    }

    @Override
    public String getFullFileName(String entityName) {
        return getTargetDirName() + "/" + entityName + ".java";
    }

    @Override
    public String getHead(String packageName) {
        return Generater.super.getHead(packageName) +
                "import javax.persistence.*;\n" +
                "// <groupId>org.hibernate.validator</groupId><artifactId>hibernate-validator</artifactId>\n" +
                "import javax.validation.constraints.*;\n\n";
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

        if (column.isVirtual() || column.isDbManager()) {
            // 虚拟字段，不能插入和更新
            sb.append(", insertable = false, updatable = false");
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
        if (!column.isAuto()) {
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
        int size = column.getSize();
        if (size <= 0) {
            return "";
        }
        return "    @Size(max = " + size + ")\n";
    }

    private String mapToDto(List<ColumnDto> columns) {
        String table = columns.get(0).getTable();

        StringBuilder sb = new StringBuilder();
        sb.append("    public ")
                .append(table)
                .append("Dto mapTo() {\n")
                .append("        ")
                .append(table)
                .append("Dto result = new ")
                .append(table)
                .append("Dto();\n");
        for (ColumnDto column : columns) {
            String colName = getFieldName(column.getColumn(), false);
            sb.append("        ")
                    .append("result.set")
                    .append(colName)
                    .append("(")
                    .append("this.get")
                    .append(colName)
                    .append("());\n");
        }
        sb.append("        return result;\n    }");
        return sb.toString();
    }

    /**
     * 生成INSERT语句，方便复制用
     *
     * @param columns 列清单
     * @return INSERT语句
     */
    private String getInsertSQL(List<ColumnDto> columns) {
        StringBuilder sbColNames = new StringBuilder();
        StringBuilder sbColValue = new StringBuilder();
        for (ColumnDto column : columns) {
            if (column.isDbManager() || column.isAuto() || column.isVirtual()) {
                continue;
            }
            if (sbColNames.length() > 0) {
                sbColNames.append(", ");
                sbColValue.append(", ");
            }
            sbColNames.append(column.getColumn());
            sbColValue.append(":").append(column.getColumn());
        }

        return "\nINSERT INTO " +
                columns.get(0).getCatalog() +
                '.' +
                columns.get(0).getTable() +
                " (\n  " +
                sbColNames +
                "\n)VALUES(\n  " +
                sbColValue +
                "\n);\n";
    }

    /**
     * 生成 UPDATE 语句，方便复制用
     *
     * @param columns 列清单
     * @return UPDATE语句
     */
    private String getUpdateSQL(List<ColumnDto> columns) {
        StringBuilder sbColNames = new StringBuilder();
        StringBuilder sbColWhere = new StringBuilder();
        for (ColumnDto column : columns) {
            if (column.isDbManager() || column.isAuto() || column.isVirtual()) {
                continue;
            }

            StringBuilder sbTmp = column.isPrimaryKey() ? sbColWhere : sbColNames;
            if (sbTmp.length() > 0) {
                sbTmp.append(column.isPrimaryKey() ? " AND " : ", ");
            }
            sbTmp.append(column.getColumn())
                    .append(" = :")
                    .append(column.getColumn());
        }

        return "\nUPDATE " +
                columns.get(0).getCatalog() +
                '.' +
                columns.get(0).getTable() +
                " SET\n  " +
                sbColNames +
                "\nWHERE " +
                sbColWhere +
                ";\n";
    }
}
