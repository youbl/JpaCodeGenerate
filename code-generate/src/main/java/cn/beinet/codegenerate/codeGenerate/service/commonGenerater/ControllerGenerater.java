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
    public String getTemplateName(GenerateDto generateDto) {
        if (generateDto.getFeignSdk())
            return "static/template/controller_sdk.template";
        return "static/template/controller_nosdk.template";
    }

    @Override
    public String getTargetDirName(GenerateDto generateDto) {
        return getPackageDir(generateDto) + "/controller";
    }

    @Override
    public String getFullFileName(String entityName, GenerateDto generateDto) {
        return getTargetDirName(generateDto) + "/" + entityName + "Controller.java";
    }

    @Override
    public GenerateResult generate(List<ColumnDto> columns, GenerateDto generateDto) {
        StringBuilder sb = new StringBuilder(getTemplate(generateDto));
        // 开启or关闭增删改代码
        if (generateDto.getModify() != null && !generateDto.getModify()) {
            replaceSymbolAndInner(sb, Vars.MODIFY_CONTENT, "");
        } else {
            removeSymbol(sb, Vars.MODIFY_CONTENT);
        }
        replaceSymbol(sb, Vars.PACKAGE_NAME, generateDto.getPackageName());
        replaceSymbol(sb, Vars.RESPONSE_PACKAGE_NAME, generateDto.getPackageResponseData());

        String now = TimeHelper.getNow();
        replaceSymbol(sb, Vars.DATE_TIME, now);

        String table = columns.get(0).getTable();
        String entityName = getEntityName(table, generateDto);
        replaceSymbol(sb, Vars.ENTITY_NAME, entityName);
        replaceSymbol(sb, Vars.TABLE_NAME, table);
        replaceSymbol(sb, Vars.LOW_ENTITY_NAME, StringHelper.lowFirstChar(entityName));
        replaceSymbol(sb, Vars.UPALL_ENTITY_NAME, entityName.toUpperCase());
        replaceSymbol(sb, Vars.LOW_KEY_FIELD, getKeyName(columns, true));
        replaceSymbol(sb, Vars.UP_KEY_FIELD, getKeyName(columns, false));
        replaceSymbol(sb, Vars.KEY_TYPE, getKeyType(columns));

        return new GenerateResult(getFullFileName(entityName, generateDto), sb.toString());
    }
}
