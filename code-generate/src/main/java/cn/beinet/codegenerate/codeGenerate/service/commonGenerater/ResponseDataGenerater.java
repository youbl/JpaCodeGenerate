package cn.beinet.codegenerate.codeGenerate.service.commonGenerater;

import cn.beinet.codegenerate.codeGenerate.dto.GenerateDto;
import cn.beinet.codegenerate.codeGenerate.dto.GenerateResult;
import cn.beinet.codegenerate.codeGenerate.enums.GenerateType;
import cn.beinet.codegenerate.codeGenerate.enums.Vars;
import cn.beinet.codegenerate.model.ColumnDto;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * Controller使用的标准返回数据Dto类生成工具
 */
@Component
public class ResponseDataGenerater implements Generater {
    @Override
    public GenerateType getType() {
        return GenerateType.COMMON;
    }

    @Override
    public String getTemplateName(GenerateDto generateDto) {
        return "static/template/responsedata.template";
    }

    @Override
    public String getTargetDirName(GenerateDto generateDto) {
        return "";
    }

    @Override
    public String getFullFileName(String entityName, GenerateDto generateDto) {
        return "ResponseData.java";
    }

    @Override
    public GenerateResult generate(List<ColumnDto> columns, GenerateDto generateDto) {
        StringBuilder sb = new StringBuilder(getTemplate(generateDto));
        replaceSymbol(sb, Vars.PACKAGE_NAME, generateDto.getPackageResponseData());
        return new GenerateResult(getFullFileName("", generateDto), sb.toString());
    }
}
