package cn.beinet.codegenerate.codeGenerate;

import cn.beinet.codegenerate.codeGenerate.service.CodeGenerateService;
import lombok.AllArgsConstructor;
import org.apache.commons.io.IOUtils;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;

@RestController
@AllArgsConstructor
public class CodeController {

    private final CodeGenerateService codeGenerateService;



    /**
     * 生成代码，并返回生成的文件地址
     *
     * @return 表清单
     */
    @PostMapping("/v1/code/tables")
    public String buildTables(@RequestParam("package") String packageName, @RequestParam String database, @RequestParam String[] tables) throws IOException {
        return codeGenerateService.generateCode(database, tables, packageName);
    }


    @GetMapping(value = "/v1/code/down")
    public ResponseEntity testDownload(@RequestParam String zipfile) throws IOException {
        if (StringUtils.isEmpty(zipfile)) {
            throw new IllegalArgumentException("文件不能为空");
        }
        zipfile = codeGenerateService.getRealPath(zipfile);

        File file = new File(zipfile);
        String downName = new String(file.getName().getBytes(StandardCharsets.UTF_8), StandardCharsets.ISO_8859_1);

        HttpHeaders headers = new HttpHeaders();
        headers.add("Content-Disposition", "attachment;filename=" + downName);
        headers.add("Content-Type", "application/x-zip-compressed");

        // 输出二进制流
        byte[] arr;
        try (FileInputStream fis = new FileInputStream(zipfile)) {
            arr = IOUtils.toByteArray(fis);
        }
        return new ResponseEntity(arr, headers, HttpStatus.OK);
    }
}
