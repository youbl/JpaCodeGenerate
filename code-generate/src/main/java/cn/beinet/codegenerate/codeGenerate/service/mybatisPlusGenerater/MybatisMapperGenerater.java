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
public class MybatisMapperGenerater implements Generater {

    @Override
    public GenerateType getType() {
        return GenerateType.MYBATIS;
    }

    @Override
    public String getTemplateName() {
        return "static/template/mybatis_mapper.template";
    }

    @Override
    public String getTargetDirName() {
        return "dal";
    }

    @Override
    public String getFullFileName(String entityName) {
        return getTargetDirName() + "/" + entityName + "Mapper.java";
    }

    @Override
    public GenerateResult generate(List<ColumnDto> columns, GenerateDto generateDto) {
        StringBuilder sb = new StringBuilder(getTemplate());
        replaceSymbol(sb, Vars.PACKAGE_NAME, generateDto.getPackageName());

        String now = TimeHelper.getNow();
        replaceSymbol(sb, Vars.DATE_TIME, now);

        String entityName = getEntityName(columns.get(0).getTable(), generateDto.getRemovePrefix());
        replaceSymbol(sb, Vars.ENTITY_NAME, entityName);

        return new GenerateResult(getFullFileName(entityName), sb.toString());
    }
}
