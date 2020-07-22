package cn.beinet.codegenerate.controller;

import cn.beinet.codegenerate.service.CodeGenerateService;
import org.apache.commons.io.IOUtils;
import cn.beinet.codegenerate.model.ColumnDto;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import javax.activation.MimetypesFileTypeMap;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.List;

@RestController
public class CodeController {

    @Autowired
    CodeGenerateService codeGenerateService;


    /**
     * 获取数据库列表
     *
     * @return 数据库列表
     */
    @GetMapping("/v1/code/databases")
    public List<String> getDatabases() {
        return codeGenerateService.getDatabases();
    }

    /**
     * 获取数据库的表清单
     *
     * @return 表清单
     */
    @GetMapping("/v1/code/tables")
    public List<String> getTables(@RequestParam String database) {
        return codeGenerateService.getTables(database);
    }


    /**
     * 获取数据库的字段清单
     *
     * @return 字段清单
     */
    @GetMapping("/v1/code/columns")
    public List<ColumnDto> getColumns(@RequestParam String database, @RequestParam String[] tables) {
        return codeGenerateService.getFields(database, tables);
    }

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
    public ResponseEntity<byte[]> testDownload(@RequestParam String zipfile) throws IOException {
        if (StringUtils.isEmpty(zipfile)) {
            throw new IllegalArgumentException("文件不能为空");
        }
        zipfile = codeGenerateService.getRealPath(zipfile);

        // 设置Header
        String type = new MimetypesFileTypeMap().getContentType(zipfile);
        File file = new File(zipfile);
        String downName = new String(file.getName().getBytes("utf-8"), "iso-8859-1");

        HttpHeaders headers = new HttpHeaders();
        headers.add("Content-Disposition", "attachment;filename=" + downName);
        headers.add("Content-Type", type);

        // 输出二进制流
        byte[] arr;
        try (FileInputStream fis = new FileInputStream(zipfile)) {
            arr = IOUtils.toByteArray(fis);
        }
        return new ResponseEntity(arr, headers, HttpStatus.OK);
    }
}
