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
    public String getTemplateName() {
        return "static/template/dto.template";
    }

    @Override
    public String getTargetDirName() {
        return "dto";
    }

    @Override
    public String getFullFileName(String entityName) {
        return getTargetDirName() + "/" + entityName + "Dto.java";
    }

    @Override
    public GenerateResult generate(List<ColumnDto> columns, GenerateDto generateDto) {
        StringBuilder sb = new StringBuilder(getTemplate());
        replaceSymbol(sb, Vars.PACKAGE_NAME, generateDto.getPackageName());

        String now = TimeHelper.getNow();
        replaceSymbol(sb, Vars.DATE_TIME, now);

        String entityName = getEntityName(columns.get(0).getTable(), generateDto);
        replaceSymbol(sb, Vars.ENTITY_NAME, entityName);

        // class的成员字段
        String fieldsBody = getClassBody(columns);
        replaceSymbol(sb, Vars.DTO_FIELDS, "\n" + fieldsBody);

        return new GenerateResult(getFullFileName(entityName), sb.toString());
    }

    private String getClassBody(List<ColumnDto> columns) {
        StringBuilder sb = new StringBuilder();
        for (ColumnDto column : columns) {
            sb.append(getColumnComment(column));
            sb.append(getSizeAnnotate(column));
            sb.append(getDateFormatAnnotate(column));
            sb.append(getColumnDefine(column));
        }
        return sb.toString();
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
        if (column.getManagerType().contains("LocalDateTime")) {
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
