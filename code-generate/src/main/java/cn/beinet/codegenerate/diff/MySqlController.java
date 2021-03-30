package cn.beinet.codegenerate.diff;

import cn.beinet.codegenerate.model.ColumnDto;
import cn.beinet.codegenerate.repository.ColumnRepository;
import org.springframework.core.env.Environment;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
public class MySqlController {
    private final Environment env;

    public MySqlController(Environment env) {
        this.env = env;
    }

    @GetMapping("mysql/databases/{isProd}")
    public List<String> GetMySqlDbs(@PathVariable int isProd) {
        ColumnRepository columnRepository = getRepository(isProd);
        return columnRepository.findDatabases();
    }

    @GetMapping("mysql/tables/{isProd}")
    public Map<String, List<ColumnDto>> GetMySqlTables(@PathVariable int isProd, @RequestParam String db) {
        ColumnRepository columnRepository = getRepository(isProd);

        List<ColumnDto> allCols = columnRepository.findColumnByTable(db, null);
        Map<String, List<ColumnDto>> ret = new HashMap<>();
        for (ColumnDto col : allCols) {
            List<ColumnDto> tableCols = ret.computeIfAbsent(col.getOriginTable(), tableName -> new ArrayList<>());
            tableCols.add(col);
        }
        return ret;
    }

    ColumnRepository getRepository(int isProd) {
        ColumnRepository.DbEnv dbEnv = isProd == 1 ? ColumnRepository.DbEnv.PROD : ColumnRepository.DbEnv.TEST;
        return new ColumnRepository(env, dbEnv);
    }
}
