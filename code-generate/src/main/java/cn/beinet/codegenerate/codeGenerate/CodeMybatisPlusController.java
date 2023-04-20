package cn.beinet.codegenerate.codeGenerate;

import cn.beinet.codegenerate.codeGenerate.dto.GenerateDto;
import cn.beinet.codegenerate.codeGenerate.dto.GenerateResult;
import cn.beinet.codegenerate.codeGenerate.service.MybatisPlusCodeGenerateService;
import lombok.AllArgsConstructor;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import java.io.IOException;
import java.util.List;

@RestController
@AllArgsConstructor
public class CodeMybatisPlusController {

    private final MybatisPlusCodeGenerateService codeGenerateService;

    /**
     * 生成代码，并直接返回，用于前端预览
     *
     * @return 响应内容
     */
    @PostMapping("/codeMybatis/generatePreview")
    public List<GenerateResult> generatePreview(@RequestBody GenerateDto dto) throws IOException {
        return codeGenerateService.generateCode(dto);
    }

    /**
     * 生成代码，并返回生成的文件地址
     *
     * @return zip文件名
     */
    @PostMapping("/codeMybatis/generateAndZip")
    public String generateAndZip(@RequestBody GenerateDto dto) throws IOException {
        return codeGenerateService.generateAndZip(dto);
    }

}
