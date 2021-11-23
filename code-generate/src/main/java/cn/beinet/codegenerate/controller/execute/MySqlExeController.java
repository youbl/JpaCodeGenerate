package cn.beinet.codegenerate.controller.execute;

import cn.beinet.codegenerate.GlobalExceptionFilter;
import cn.beinet.codegenerate.controller.execute.dto.SqlDto;
import cn.beinet.codegenerate.repository.MySqlExecuteRepository;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Map;
import java.util.regex.Pattern;

@RestController
public class MySqlExeController {

    @PostMapping("mysql/executeSql")
    public GlobalExceptionFilter.ResponseData GetMySqlDbs(@RequestParam String ip,
                                                          @RequestParam String user,
                                                          @RequestParam String pwd,
                                                          @RequestParam String db,
                                                          @RequestBody SqlDto sql) {
        MySqlExecuteRepository repository = new MySqlExecuteRepository(ip, 3306, user, pwd, db);
        List<Map<String, Object>> result = repository.executeSql(formatSql(sql.getSql()));
        return GlobalExceptionFilter.ResponseData.ok(result);
    }

    private String formatSql(String originSql) {
        if (originSql == null) {
            throw new RuntimeException("SQL不能为空");
        }
        originSql = originSql.trim();
        if (originSql.length() == 0) {
            throw new RuntimeException("SQL不能为空");
        }

        // 判断是否修改语句
        Pattern regModify = Pattern.compile("(?i)(insert|update|delete|alter|create|set)\\s");
        if (regModify.matcher(originSql).find()) {
            throw new RuntimeException("只支持查询语句");
        }

        while (originSql.endsWith(";")) {
            originSql = originSql.substring(0, originSql.length() - 1).trim();
        }

        if (originSql.startsWith("show ")) {
            return originSql;
        }

        // 末尾添加limit 100
        Pattern regLimit = Pattern.compile("(?i)\\slimit\\s+\\d+$");
        if (!regLimit.matcher(originSql).find()) {
            originSql += " limit 100";
        }
        return originSql;
    }
}
