package cn.beinet.codegenerate.controller.diff;

import cn.beinet.codegenerate.controller.dto.SqlDto;
import cn.beinet.codegenerate.linkinfo.service.LinkInfoService;
import cn.beinet.codegenerate.model.ColumnDto;
import cn.beinet.codegenerate.model.IndexDto;
import cn.beinet.codegenerate.repository.ColumnRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequiredArgsConstructor
public class MySqlController {
    private final LinkInfoService linkInfoService;

    @GetMapping("mysql/databases")
    public List<String> GetMySqlDbs(SqlDto dto) {
        ColumnRepository columnRepository = linkInfoService.getRepository(dto);
        return columnRepository.findDatabases();
    }

    @GetMapping("mysql/tableNames")
    public List<String> GetMySqlTableNames(SqlDto dto) {
        ColumnRepository columnRepository = linkInfoService.getRepository(dto);
        return columnRepository.findTables(dto.getDb());
    }

    /**
     * 返回所有字段信息
     *
     * @return 字段信息
     */
    @GetMapping("mysql/tables/ddl")
    public String GetMySqlTableDDL(SqlDto dto) {
        ColumnRepository columnRepository = linkInfoService.getRepository(dto);
        return columnRepository.getTableDDL(dto.getDb(), dto.getTableName());
    }

    /**
     * 返回所有字段信息
     *
     * @return 字段信息
     */
    @GetMapping("mysql/tables")
    public Map<String, List<ColumnDto>> GetMySqlTables(SqlDto dto) {
        ColumnRepository columnRepository = linkInfoService.getRepository(dto);

        List<ColumnDto> allCols = columnRepository.findColumnByTable(dto.getDb(), null);
        Map<String, List<ColumnDto>> ret = new HashMap<>();
        for (ColumnDto col : allCols) {
            List<ColumnDto> tableCols = ret.computeIfAbsent(col.getTable(), tableName -> new ArrayList<>());
            tableCols.add(col);
        }
        return ret;
    }


    /**
     * 返回所有字段信息
     *
     * @return 字段信息
     */
    @GetMapping("mysql/columns")
    public List<ColumnDto> GetMySqlTableColumns(SqlDto dto) {
        List<ColumnDto> ret = new ArrayList<>();
        String[] arrTables = dto.getTableName().split(",");
        ColumnRepository columnRepository = linkInfoService.getRepository(dto);
        for (String item : arrTables) {
            ret.addAll(columnRepository.findColumnByTable(dto.getDb(), item));
        }
        return ret;
    }

    /**
     * 返回值第一个Map的Key是表名，子Map的key是索引名,Value是建索引语句
     *
     * @return 索引信息
     */
    @GetMapping("mysql/indexes")
    public Map<String, Map<String, String>> GetMySqlIndexes(SqlDto dto) {
        ColumnRepository columnRepository = linkInfoService.getRepository(dto);

        List<IndexDto> allIndexes = columnRepository.findIndexesByTable(dto.getDb(), null);
        Map<String, Map<String, String>> ret = new HashMap<>();
        Map<String, IndexDto> firstColPerIndex = new HashMap<>(); // 收集每个索引的一列, 随意，用于拼接其它信息

        // 收集每个索引的字段列表，拼接sql
        for (IndexDto index : allIndexes) {
            String tbName = index.getOriginTable();
            Map<String, String> tableIndexes = ret.computeIfAbsent(tbName, tableName -> new HashMap<>());
            String sql = tableIndexes.get(index.getIndexName());
            if (StringUtils.hasLength(sql))
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

                IndexDto idxDto = firstColPerIndex.get(key);// 肯定有值拉
                // 拼接唯一、type、comment
                StringBuilder sb = new StringBuilder();
                if (idxDto.isUnique())
                    sb.append(" UNIQUE");
                sb.append(" INDEX ")
                        .append(idxDto.getIndexName())
                        .append("(")
                        .append(index.getValue())
                        .append(") COMMENT \"")
                        .append(idxDto.getComment())
                        .append("\"");
                table.getValue().put(indexName, sb.toString());
            }
        }
        return ret;
    }
}
