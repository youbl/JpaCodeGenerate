package cn.beinet.codegenerate.job.dataClean.impl;

import cn.beinet.codegenerate.job.dataClean.CleanConfigs;
import cn.beinet.codegenerate.job.dataClean.Cleanup;
import cn.beinet.codegenerate.job.dataClean.impl.services.MysqlCleanService;
import cn.beinet.codegenerate.repository.MySqlExecuteRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

/**
 * 新类
 *
 * @author youbl
 * @since 2024/1/10 20:50
 */
@Component
@Slf4j
@RequiredArgsConstructor
public class MysqlCleanup implements Cleanup {
    // 清理的配置
    private final CleanConfigs cleanConfigs;

    private final MysqlCleanService cleanService;

    @Override
    public boolean enabled() {
        if (cleanConfigs == null || !cleanConfigs.getEnable())
            return false;
        return cleanConfigs.getMysql() != null && cleanConfigs.getMysql().length > 0;
    }

    @Override
    public void clean() {
        for (CleanConfigs.MysqlInstance mysql : cleanConfigs.getMysql()) {
            if (!mysql.getEnable() ||
                    mysql.getTables() == null)
                continue;

            MySqlExecuteRepository repository = new MySqlExecuteRepository(
                    mysql.getIp(),
                    mysql.getPort(),
                    mysql.getUsername(),
                    mysql.getPassword(),
                    mysql.getDatabase());

            String backToDb = StringUtils.hasLength(mysql.getBackToDb()) ?
                    mysql.getBackToDb() : mysql.getDatabase();
            for (CleanConfigs.MysqlTable table : mysql.getTables()) {
                if (!table.getEnable() || !StringUtils.hasLength(table.getTableName()))
                    continue;
                if (!StringUtils.hasLength(table.getBackToDb())) {
                    table.setBackToDb(backToDb);
                }
                cleanService.cleanTable(table, repository);
            }
        }
    }
}
