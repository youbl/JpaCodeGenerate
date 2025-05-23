package cn.beinet.codegenerate.codeGenerate.service.mybatisPlusGenerater;

import cn.beinet.codegenerate.codeGenerate.dto.GenerateDto;
import cn.beinet.codegenerate.codeGenerate.dto.GenerateResult;
import cn.beinet.codegenerate.codeGenerate.enums.GenerateType;
import cn.beinet.codegenerate.codeGenerate.enums.Vars;
import cn.beinet.codegenerate.codeGenerate.service.commonGenerater.Generater;
import cn.beinet.codegenerate.codeGenerate.service.commonGenerater.HtmlGenerater;
import cn.beinet.codegenerate.model.ColumnDto;
import cn.beinet.codegenerate.util.TimeHelper;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class MybatisServiceGenerater implements Generater {

    @Override
    public GenerateType getType() {
        return GenerateType.MYBATIS;
    }

    @Override
    public String getTemplateName(GenerateDto generateDto) {
        return "static/template/mybatis_service.template";
    }

    @Override
    public String getTargetDirName(GenerateDto generateDto) {
        return getPackageDir(generateDto) + "/service";
    }

    @Override
    public String getFullFileName(String entityName, GenerateDto generateDto) {
        return getTargetDirName(generateDto) + "/" + entityName + "Service.java";
    }

    @Override
    public GenerateResult generate(List<ColumnDto> columns, GenerateDto generateDto) {
        StringBuilder sb = new StringBuilder(getTemplate(generateDto));
        replaceSymbol(sb, Vars.PACKAGE_NAME, generateDto.getPackageName());

        String now = TimeHelper.getNow();
        replaceSymbol(sb, Vars.DATE_TIME, now);

        String entityName = getEntityName(columns.get(0).getTable(), generateDto);
        replaceSymbol(sb, Vars.ENTITY_NAME, entityName);

        replaceSymbol(sb, Vars.SERVICE_COND_FIELDS, getBody(columns, entityName));

        replaceSymbol(sb, Vars.UP_KEY_FIELD, getKeyName(columns, false));

        replaceSymbol(sb, Vars.KEY_TYPE, getKeyType(columns));

        return new GenerateResult(getFullFileName(entityName, generateDto), sb.toString());
    }

    private String getBody(List<ColumnDto> columns, String entityName) {

        StringBuilder sb = new StringBuilder("\n");
        for (ColumnDto item : columns) {
            String colName = getFieldName(item.getColumn(), false);
            String colType = item.getManagerType();
            String condCheck;
            if (colType.equals("String")) {
                condCheck = "StringUtils.hasLength(dto.get" + colName + "())";
            } else {
                condCheck = "dto.get" + colName + "() != null";
            }
            // wrapper.eq(dto.getCreateMember() != null, {{entity_name}}::getCreateMember, dto.getCreateMember());
            String cond = "wrapper.eq(" + condCheck + ", " + entityName + "::get" + colName +
                    ", dto.get" + colName +
                    "());";
            sb.append("            ").append(cond).append("\n");

            if (HtmlGenerater.isSearchKey(colName) && item.isLocalDateTime()) {
                // 时间字段要增加 开始和结束的区间搜索
                String condBegin = "wrapper.ge(dto.get" + colName + "Begin() != null, " + entityName + "::get" + colName +
                        ", dto.get" + colName + "Begin());";
                sb.append("            ").append(condBegin).append("\n");

                String condEnd = "wrapper.le(dto.get" + colName + "End() != null, " + entityName + "::get" + colName +
                        ", dto.get" + colName + "End());";
                sb.append("            ").append(condEnd).append("\n");
            }
        }
        return sb.toString();
    }
}
