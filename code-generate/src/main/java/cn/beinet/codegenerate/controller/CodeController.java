package cn.beinet.codegenerate.controller;

import cn.beinet.codegenerate.service.CodeGenerateService;
import cn.beinet.codegenerate.model.ColumnDto;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

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
     * 获取数据库的表清单
     *
     * @return 表清单
     */
    @PostMapping("/v1/code/tables")
    public String buildTables(@RequestParam("package") String packageName, @RequestParam String database, @RequestParam String[] tables) throws IOException {
        return codeGenerateService.generateCode(database, tables, packageName);
    }
}
