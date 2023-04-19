package cn.beinet.codegenerate.codeGenerate;

import cn.beinet.codegenerate.codeGenerate.service.CodeDbService;
import cn.beinet.codegenerate.model.ColumnDto;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * 代码生成所需的数据库支持
 *
 * @author youbl
 * @date 2023/4/19 10:46
 */
@RestController
@RequestMapping("codeDb")
@RequiredArgsConstructor
public class CodeDbController {
    private final CodeDbService codeDbService;


    /**
     * 获取数据库列表
     *
     * @return 数据库列表
     */
    @GetMapping("databases")
    public List<String> getDatabases() {
        return codeDbService.getDatabases();
    }

    /**
     * 获取数据库的表清单
     *
     * @return 表清单
     */
    @GetMapping("tables")
    public List<String> getTables(@RequestParam String database) {
        return codeDbService.getTables(database);
    }


    /**
     * 获取数据库的字段清单
     *
     * @return 字段清单
     */
    @GetMapping("columns")
    public List<ColumnDto> getColumns(@RequestParam String database, @RequestParam String[] tables) {
        return codeDbService.getFields(database, tables);
    }
}
