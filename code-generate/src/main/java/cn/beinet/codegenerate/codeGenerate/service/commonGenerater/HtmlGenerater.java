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

/**
 * 基于VUE的前端编辑页面生成工具
 */
@Component
public class HtmlGenerater implements Generater {
    @Override
    public GenerateType getType() {
        return GenerateType.COMMON;
    }

    @Override
    public String getTemplateName() {
        return "static/template/list_html.template";
    }

    @Override
    public String getTargetDirName() {
        return "html";
    }

    @Override
    public String getFullFileName(String entityName) {
        return getTargetDirName() + "/" + StringHelper.lowFirstChar(entityName) + ".html";
    }

    @Override
    public GenerateResult generate(List<ColumnDto> columns, GenerateDto generateDto) {
        StringBuilder sb = new StringBuilder(getTemplate());

        String now = TimeHelper.getNow();
        replaceSymbol(sb, Vars.DATE_TIME, now);

        String entityName = getEntityName(columns.get(0).getTable(), generateDto.getRemovePrefix());
        replaceSymbol(sb, Vars.ENTITY_NAME, entityName);

        String lowEntityName = StringHelper.lowFirstChar(entityName);
        replaceSymbol(sb, Vars.LOW_ENTITY_NAME, lowEntityName);

        return new GenerateResult(getFullFileName(entityName), sb.toString());
    }
}
