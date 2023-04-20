package cn.beinet.codegenerate.codeGenerate.service.commonGenerater;

import cn.beinet.codegenerate.codeGenerate.dto.GenerateDto;
import cn.beinet.codegenerate.codeGenerate.dto.GenerateResult;
import cn.beinet.codegenerate.codeGenerate.enums.GenerateType;
import cn.beinet.codegenerate.codeGenerate.enums.Vars;
import cn.beinet.codegenerate.model.ColumnDto;
import cn.beinet.codegenerate.util.StringHelper;
import cn.beinet.codegenerate.util.TimeHelper;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class ControllerGenerater implements Generater {
    @Override
    public GenerateType getType() {
        return GenerateType.COMMON;
    }

    @Override
    public String getTemplateName() {
        return "static/template/controller.template";
    }

    @Override
    public String getTargetDirName() {
        return "controller";
    }

    @Override
    public String getFullFileName(String entityName) {
        return getTargetDirName() + "/" + entityName + "Controller.java";
    }

    @Override
    public GenerateResult generate(List<ColumnDto> columns, GenerateDto generateDto) {
        StringBuilder sb = new StringBuilder(getTemplate());
        replaceSymbol(sb, Vars.PACKAGE_NAME, generateDto.getPackageName());

        String now = TimeHelper.getNow();
        replaceSymbol(sb, Vars.DATE_TIME, now);

        String table = columns.get(0).getTable();
        String entityName = getEntityName(table, generateDto.getRemovePrefix());
        replaceSymbol(sb, Vars.ENTITY_NAME, entityName);
        replaceSymbol(sb, Vars.TABLE_NAME, table);
        replaceSymbol(sb, Vars.LOW_ENTITY_NAME, StringHelper.lowFirstChar(entityName));

        return new GenerateResult(getFullFileName(entityName), sb.toString());
    }
}
