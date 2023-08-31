package cn.beinet.codegenerate.codeGenerate.service.mybatisPlusGenerater;

import cn.beinet.codegenerate.codeGenerate.dto.GenerateDto;
import cn.beinet.codegenerate.codeGenerate.dto.GenerateResult;
import cn.beinet.codegenerate.codeGenerate.enums.GenerateType;
import cn.beinet.codegenerate.codeGenerate.enums.Vars;
import cn.beinet.codegenerate.codeGenerate.service.commonGenerater.Generater;
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
    public String getTemplateName() {
        return "static/template/mybatis_service.template";
    }

    @Override
    public String getTargetDirName() {
        return "service";
    }

    @Override
    public String getFullFileName(String entityName) {
        return getTargetDirName() + "/" + entityName + "Service.java";
    }

    @Override
    public GenerateResult generate(List<ColumnDto> columns, GenerateDto generateDto) {
        StringBuilder sb = new StringBuilder(getTemplate());
        replaceSymbol(sb, Vars.PACKAGE_NAME, generateDto.getPackageName());

        String now = TimeHelper.getNow();
        replaceSymbol(sb, Vars.DATE_TIME, now);

        String entityName = getEntityName(columns.get(0).getTable(), generateDto.getRemovePrefix());
        replaceSymbol(sb, Vars.ENTITY_NAME, entityName);

        replaceSymbol(sb, Vars.SERVICE_COND_FIELDS, getBody(columns, entityName));

        replaceSymbol(sb, Vars.UP_KEY_FIELD, getKeyName(columns, false));

        return new GenerateResult(getFullFileName(entityName), sb.toString());
    }

    private String getBody(List<ColumnDto> columns, String entityName) {

        StringBuilder sb = new StringBuilder("\n");
        for (ColumnDto item : columns) {
            String colName = getFieldName(item.getColumn(), false);
            // wrapper.eq(dto.getCreateMember() != null, {{entity_name}}::getCreateMember, dto.getCreateMember());
            String cond = "wrapper.eq(dto.get" + colName +
                    "() != null, " + entityName + "::get" + colName +
                    ", dto.get" + colName +
                    "());";
            sb.append("            ").append(cond).append("\n");
        }
        return sb.toString();
    }
}
