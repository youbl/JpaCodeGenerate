package cn.beinet.codegenerate.diff;

import cn.beinet.codegenerate.model.ColumnDto;
import cn.beinet.codegenerate.model.IndexDto;
import cn.beinet.codegenerate.repository.ColumnRepository;
import org.springframework.core.env.Environment;
import org.springframework.util.StringUtils;
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

    /**
     * 返回所有字段信息
     *
     * @param isProd true使用生产环境连接串，false使用测试环境连接串
     * @param db     读取哪个数据库
     * @return 字段信息
     */
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

    /**
     * 返回值第一个Map的Key是表名，子Map的key是索引名,Value是建索引语句
     *
     * @param isProd true使用生产环境连接串，false使用测试环境连接串
     * @param db     读取哪个数据库
     * @return 索引信息
     */
    @GetMapping("mysql/indexes/{isProd}")
    public Map<String, Map<String, String>> GetMySqlIndexes(@PathVariable int isProd, @RequestParam String db) {
        ColumnRepository columnRepository = getRepository(isProd);

        List<IndexDto> allIndexes = columnRepository.findIndexesByTable(db, null);
        Map<String, Map<String, String>> ret = new HashMap<>();
        Map<String, IndexDto> firstColPerIndex = new HashMap<>(); // 收集每个索引的一列, 随意，用于拼接其它信息

        // 收集每个索引的字段列表，拼接sql
        for (IndexDto index : allIndexes) {
            String tbName = index.getTable();
            Map<String, String> tableIndexes = ret.computeIfAbsent(tbName, tableName -> new HashMap<>());
            String sql = tableIndexes.get(index.getIndexName());
            if (!StringUtils.isEmpty(sql))
                sql += ",";
            else
                sql = ""; // 必须赋初值，否则 sql += "xxx"; 得到 nullxxx
            sql += "`" + index.getColumn() + "`";
            if (index.getSubPart() > 0)
                sql += "(" + index.getSubPart() + ")";
            tableIndexes.put(index.getIndexName(), sql);

            String key = tbName + "-" + index.getIndexName();
            firstColPerIndex.put(key, index);
        }

        // 上面只拼了字段，这里拼其它信息
        for (Map.Entry<String, Map<String, String>> table : ret.entrySet()) {
            String tableName = table.getKey();
            for (Map.Entry<String, String> index : table.getValue().entrySet()) {
                String indexName = index.getKey();
                String key = tableName + "-" + indexName;

                IndexDto dto = firstColPerIndex.get(key);// 肯定有值拉
                // 拼接唯一、type、comment
                StringBuilder sb = new StringBuilder();
                if (dto.isUnique())
                    sb.append(" UNIQUE");
                sb.append(" INDEX ")
                        .append(dto.getIndexName())
                        .append("(")
                        .append(index.getValue())
                        .append(") COMMENT \"")
                        .append(dto.getComment())
                        .append("\"");
                table.getValue().put(indexName, sb.toString());
            }
        }
        return ret;
    }

    ColumnRepository getRepository(int isProd) {
        ColumnRepository.DbEnv dbEnv = isProd == 1 ? ColumnRepository.DbEnv.PROD : ColumnRepository.DbEnv.TEST;
        return new ColumnRepository(env, dbEnv);
    }
}
