package cn.beinet.codegenerate.codeGenerate.service.mybatisPlusGenerater;

import cn.beinet.codegenerate.codeGenerate.dto.GenerateDto;
import cn.beinet.codegenerate.codeGenerate.dto.GenerateResult;
import cn.beinet.codegenerate.codeGenerate.enums.GenerateType;
import cn.beinet.codegenerate.codeGenerate.enums.Vars;
import cn.beinet.codegenerate.codeGenerate.service.commonGenerater.Generater;
import cn.beinet.codegenerate.model.ColumnDto;
import cn.beinet.codegenerate.util.TimeHelper;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import java.util.List;

/**
 * MybatisPlus的entity类生成工具
 */
@Component
public class MybatisEntityGenerater implements Generater {

    @Override
    public GenerateType getType() {
        return GenerateType.MYBATIS;
    }

    @Override
    public String getTemplateName(GenerateDto generateDto) {
        return "static/template/mybatis_entity.template";
    }

    @Override
    public String getTargetDirName() {
        return "dal/entity";
    }

    @Override
    public String getFullFileName(String entityName) {
        return getTargetDirName() + "/" + entityName + ".java";
    }

    @Override
    public GenerateResult generate(List<ColumnDto> columns, GenerateDto generateDto) {
        StringBuilder sb = new StringBuilder(getTemplate(generateDto));
        replaceSymbol(sb, Vars.PACKAGE_NAME, generateDto.getPackageName());

        String now = TimeHelper.getNow();
        replaceSymbol(sb, Vars.DATE_TIME, now);

        String table = columns.get(0).getTable();
        String entityName = getEntityName(table, generateDto);
        replaceSymbol(sb, Vars.ENTITY_NAME, entityName);
        replaceSymbol(sb, Vars.TABLE_NAME, table);

        // class的成员字段
        String fieldsBody = getClassBody(columns);
        replaceSymbol(sb, Vars.ENTITY_FIELDS, "\n" + fieldsBody);

        // 替换对应的jdk版本代码
        replaceJDK(sb, generateDto.getJdkVer());

        // 添加辅助内容，插入和更新语句
        sb.append("\n/*")
                .append(getInsertSQL(columns))
                .append(getUpdateSQL(columns))
                .append("*/");
        return new GenerateResult(getFullFileName(entityName), sb.toString());
    }

    private String getClassBody(List<ColumnDto> columns) {
        StringBuilder sb = new StringBuilder();
        for (ColumnDto column : columns) {
            sb.append(getColumnComment(column));
            sb.append(getSizeAnnotate(column));

            if (column.isPrimaryKey()) {
                if (column.isAuto())
                    sb.append("    @TableId(type = IdType.AUTO)\n");
                else
                    sb.append("    @TableId(type = IdType.INPUT)\n");
            } else {
                // mybatisplus里， 有TableId注解，就会忽略TableField注解
                sb.append(getColumnAnnotate(column));
            }
            sb.append(getColumnDefine(column));
        }
        return sb.toString();
    }

    private void replaceJDK(StringBuilder sb, String jdkVer) {
        String code = jdkVer.equals("8") ?
                "import javax.validation.constraints.*;" :
                "import jakarta.validation.constraints.*;";
        replaceSymbol(sb, Vars.JDK_CONTENT, code);
    }

    /**
     * 生成字段注释
     *
     * @param column 对应的列
     * @return 注释
     */
    private String getColumnComment(ColumnDto column) {
        String comment = column.getComment();
        if (StringUtils.hasLength(comment)) {
            comment = comment.replace("*/", "* /")
                    .replace("\n", " ");
        }
        return "    /**\n     * " + comment + "\n     */\n";
    }


    /**
     * 生成 注解 @TableField
     *
     * @param column 对应的列
     * @return 注解字符串
     */
    private String getColumnAnnotate(ColumnDto column) {
        StringBuilder sb = new StringBuilder();
        sb.append("    @TableField(value = \"`")
                .append(column.getColumn())
                .append("`\"");

        if (column.isVirtual() || column.isDbManager()) {
            // 虚拟字段，不能插入和更新
            sb.append(", insertStrategy = FieldStrategy.NEVER, updateStrategy = FieldStrategy.NEVER");
        }
        sb.append(")\n");
        return sb.toString();
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
