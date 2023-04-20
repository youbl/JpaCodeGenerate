package cn.beinet.codegenerate.codeGenerate;

import cn.beinet.codegenerate.codeGenerate.dto.GenerateDto;
import cn.beinet.codegenerate.codeGenerate.service.JpaCodeGenerateService;
import cn.beinet.codegenerate.codeGenerate.service.MybatisPlusCodeGenerateService;
import lombok.AllArgsConstructor;
import org.apache.commons.io.IOUtils;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.*;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;

@RestController
@AllArgsConstructor
public class CodeMybatisPlusController {

    private final MybatisPlusCodeGenerateService codeGenerateService;


    /**
     * 生成代码，并返回生成的文件地址
     *
     * @return 表清单
     */
    @PostMapping("/codeMybatis/generateAndZip")
    public String buildTables(@RequestBody GenerateDto dto) throws IOException {
        return codeGenerateService.generateCode(dto);
    }

}
