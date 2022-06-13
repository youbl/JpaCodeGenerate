package cn.beinet.codegenerate.service;

import cn.beinet.codegenerate.controller.execute.dto.SqlDto;
import cn.beinet.codegenerate.repository.MySqlExecuteRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

/**
 * Description:
 *
 * @author : youbl
 * @create: 2022/6/9 17:46
 */
@Service
@Slf4j
public class MySqlService {
    public int executeDml(SqlDto sql) {
        String ip = sql.getIp();
        String user = sql.getUser();
        String pwd = sql.getPwd();
        String db = sql.getDb();
        MySqlExecuteRepository repository = new MySqlExecuteRepository(ip, 3306, user, pwd, db);

        int affectedRows = 0;
        String[] arrSql = splitSql(sql.getSql());
        for (String itemSql : arrSql) {
            String item = itemSql.trim();
            if (item.length() <= 0)
                continue;
            long startTime = System.currentTimeMillis();
            int rows = repository.executeDml(item);
            long costTime = System.currentTimeMillis() - startTime;
            affectedRows += rows;
            log.info("{}\r\n影响行数:{} 耗时:{}ms \r\n {}", sql.getDb(), rows, costTime, item);
        }

        return affectedRows;
    }

    @Async
    public void executeDmlAsync(SqlDto sql) {
        if (sql.getTime() < 0) {
            log.warn("执行次数不能小于0");
            return;
        }
        if (sql.getTime() <= 0) {
            sql.setTime(100000);
        }
        for (int i = 0; i < sql.getTime(); i++) {
            int result = executeDml(sql);
            if (result <= 0) {
                log.info("结束运行");
                break;
            }
            sleep(10);
        }
    }

    private String[] splitSql(String sql) {
        return sql.split(";");
    }

    private void sleep(long millis) {
        try {
            Thread.sleep(millis);
        } catch (Exception exp) {
        }
    }
}
