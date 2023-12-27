package cn.beinet.codegenerate.controller.execute;

import cn.beinet.codegenerate.GlobalExceptionFilter;
import cn.beinet.codegenerate.configs.AuthDetails;
import cn.beinet.codegenerate.controller.dto.SqlDto;
import cn.beinet.codegenerate.linkinfo.service.LinkInfoService;
import cn.beinet.codegenerate.repository.MySqlExecuteRepository;
import cn.beinet.codegenerate.service.MySqlService;
import lombok.RequiredArgsConstructor;
import org.springframework.util.Assert;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Map;
import java.util.regex.Pattern;

@RestController
@RequiredArgsConstructor
public class MySqlExeController {

    private final MySqlService mySqlService;
    private final LinkInfoService linkInfoService;

    /**
     * 执行查询sql
     *
     * @param sql 要执行的sql和数据库信息，只能是查询
     * @return 结果集
     */
    @PostMapping("mysql/executeSql")
    public GlobalExceptionFilter.ResponseData executeSelectSql(@RequestBody SqlDto sql) {
        String sqlStr = formatSql(sql.getSql());
        MySqlExecuteRepository repository = linkInfoService.getExeRepository(sql);
        List<Map<String, Object>> result = repository.queryData(sqlStr);
        return GlobalExceptionFilter.ResponseData.ok(sqlStr, result);
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
        Pattern regModify = Pattern.compile("(?i)(insert|update|delete|alter|create)\\s");
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
        Pattern regLimit = Pattern.compile("(?i)\\slimit\\s+\\d+(,\\s*\\d+)?$");
        if (!regLimit.matcher(originSql).find()) {
            originSql += " limit 100";
        }
        return originSql;
    }

    /**
     * 执行DML，即增删改的SQL，注意安全
     *
     * @param sql 要执行的sql和数据库信息，只能是无返回结果的sql（只能返回行数）
     *            里面的time参数为 执行次数，为0表示无限次，执行到影响行数为0为止
     * @return 影响行数（如果次数大于1，则是异步执行，返回-1）
     */
    @PostMapping("mysql/executeDml")
    public GlobalExceptionFilter.ResponseData executeDML(@RequestBody SqlDto sql, AuthDetails loginInfo) {
        Assert.isTrue(loginInfo != null && "beiliang_you".equals(loginInfo.getAccount()),
                "不允许访问");

        if (sql.getTime() == 1) {
            int affectedRows = mySqlService.executeDml(sql);
            return GlobalExceptionFilter.ResponseData.ok("OK", affectedRows);
        }
        mySqlService.executeDmlAsync(sql);
        return GlobalExceptionFilter.ResponseData.ok("已开始执行,请关注后台日志", -1);
    }
}
