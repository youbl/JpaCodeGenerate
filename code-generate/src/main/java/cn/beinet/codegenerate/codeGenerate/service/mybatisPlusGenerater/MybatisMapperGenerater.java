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
    public String getTemplateName(GenerateDto generateDto) {
        return "static/template/mybatis_mapper.template";
    }

    @Override
    public String getTargetDirName(GenerateDto generateDto) {
        return getPackageDir(generateDto) + "/dao";
    }

    @Override
    public String getFullFileName(String entityName, GenerateDto generateDto) {
        return getTargetDirName(generateDto) + "/" + entityName + "Mapper.java";
    }

    @Override
    public GenerateResult generate(List<ColumnDto> columns, GenerateDto generateDto) {
        StringBuilder sb = new StringBuilder(getTemplate(generateDto));
        replaceSymbol(sb, Vars.PACKAGE_NAME, generateDto.getPackageName());

        String now = TimeHelper.getNow();
        replaceSymbol(sb, Vars.DATE_TIME, now);

        String entityName = getEntityName(columns.get(0).getTable(), generateDto);
        replaceSymbol(sb, Vars.ENTITY_NAME, entityName);

        String table = columns.get(0).getTable();
        replaceSymbol(sb, Vars.TABLE_NAME, table);

        replaceSymbol(sb, Vars.LOW_KEY_FIELD, getKeyName(columns, true));
        replaceSymbol(sb, Vars.KEY_TYPE, getKeyType(columns));

        return new GenerateResult(getFullFileName(entityName, generateDto), sb.toString());
    }
}
