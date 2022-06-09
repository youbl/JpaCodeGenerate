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
        int affectedRows = repository.executeDml(sql.getSql());
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
            long startTime = System.currentTimeMillis();
            int result = executeDml(sql);
            long costTime = System.currentTimeMillis() - startTime;
            log.info("影响行数:{} 耗时:{}ms {}\r\n {}", result, costTime, sql.getDb(), sql.getSql());
            if (result <= 0) {
                log.info("结束运行");
                break;
            }
        }
    }
}
