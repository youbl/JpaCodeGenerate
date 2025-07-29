package cn.beinet.codegenerate.codeGenerate.service.commonGenerater;

import cn.beinet.codegenerate.codeGenerate.dto.GenerateDto;
import cn.beinet.codegenerate.codeGenerate.dto.GenerateResult;
import cn.beinet.codegenerate.codeGenerate.enums.GenerateType;
import cn.beinet.codegenerate.codeGenerate.enums.Vars;
import cn.beinet.codegenerate.model.ColumnDto;
import cn.beinet.codegenerate.util.TimeHelper;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import java.util.List;

/**
 * Model的Dto类生成工具
 */
@Component
public class DtoGenerater implements Generater {
    @Override
    public GenerateType getType() {
        return GenerateType.COMMON;
    }

    @Override
    public String getTemplateName(GenerateDto generateDto) {
        return "static/template/dto.template";
    }

    @Override
    public String getTargetDirName(GenerateDto generateDto) {
        return getPackageDir(generateDto) + "/dto";
    }

    @Override
    public String getFullFileName(String entityName, GenerateDto generateDto) {
        return getTargetDirName(generateDto) + "/" + entityName + "Dto.java";
    }

    @Override
    public GenerateResult generate(List<ColumnDto> columns, GenerateDto generateDto) {
        StringBuilder sb = new StringBuilder(getTemplate(generateDto));
        replaceSymbol(sb, Vars.PACKAGE_NAME, generateDto.getPackageName());

        String now = TimeHelper.getNow();
        replaceSymbol(sb, Vars.DATE_TIME, now);

        String entityName = getEntityName(columns.get(0).getTable(), generateDto);
        replaceSymbol(sb, Vars.ENTITY_NAME, entityName);

        // class的成员字段
        String fieldsBody = getClassBody(columns, generateDto.getDtoUseTs());
        replaceSymbol(sb, Vars.DTO_FIELDS, "\n" + fieldsBody);

        // 替换对应的jdk版本代码
        replaceJDK(sb, generateDto.getJdkVer());

        return new GenerateResult(getFullFileName(entityName, generateDto), sb.toString());
    }

    private String getClassBody(List<ColumnDto> columns, Boolean dtoUseTs) {
        StringBuilder sb = new StringBuilder();
        for (ColumnDto column : columns) {
            sb.append(getColumnComment(column));
            sb.append(getSizeAnnotate(column));

            boolean isTime = (column.isLocalDateTime());
            String colDefine;
            if (dtoUseTs != null && dtoUseTs && isTime) {
                // 把LocalDateTime转换为时间戳返回
                colDefine = getColumnDefineInner(column);
            } else {
                sb.append(getDateFormatAnnotate(column));
                colDefine = getColumnDefine(column);
            }
            sb.append(colDefine);

            String colName = getFieldName(column.getColumn(), true);
            if (isTime && HtmlGenerater.isSearchKey(column.getColumn())) {
                // 搜索字段增加Begin和End区间条件字段
                sb.append("    private Long ").append(colName).append("Begin;\n");
                sb.append("    private Long ").append(colName).append("End;\n\n");
            }
        }
        return sb.toString();
    }

    /**
     * 重写，把LocalDateTime转换为Long类型的时间戳
     *
     * @param column 对应的列
     * @return 字段在DTO里的定义
     */
    //@Override
    private String getColumnDefineInner(ColumnDto column) {
        String colName = getFieldName(column.getColumn(), true);
        return "    private Long " + colName + ";\n\n";
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
     * 如果是日期类型，要生成 注解 @DateTimeFormat
     *
     * @param column 对应的列
     * @return 注解字符串
     */
    private String getDateFormatAnnotate(ColumnDto column) {
        if (column.isLocalDateTime()) {
            return "    @DateTimeFormat(pattern = \"yyyy-MM-dd HH:mm:ss\")\n";
        }
        return "";
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
}
