package cn.beinet.codegenerate.dataClean.impl;

import cn.beinet.codegenerate.dataClean.Cleanup;
import cn.beinet.codegenerate.dataClean.configDal.CleanConfigDal;
import cn.beinet.codegenerate.dataClean.configDal.entity.CleanConfig;
import cn.beinet.codegenerate.dataClean.configDal.entity.CleanTable;
import cn.beinet.codegenerate.dataClean.impl.services.MysqlCleanService;
import cn.beinet.codegenerate.repository.MySqlExecuteRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import java.util.List;

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
    private final MysqlCleanService cleanService;
    private final CleanConfigDal cleanConfigDal;


    @Override
    public boolean enabled() {
        return true;
    }

    @Override
    public void clean() {
        List<CleanConfig> configList = cleanConfigDal.getAllConfig(true);
        if (configList == null || configList.isEmpty())
            return;

        for (CleanConfig mysql : configList) {
            if (!mysql.getEnabled() || mysql.getTableList() == null || mysql.getTableList().isEmpty())
                continue;

            // 判断备份到哪个数据库
            String backToDb = StringUtils.hasLength(mysql.getBackDb()) ?
                    mysql.getBackDb() : mysql.getDb();

            MySqlExecuteRepository repositoryWrite = new MySqlExecuteRepository(mysql.getMysqlWrite(), mysql.getDb(), 10);
            MySqlExecuteRepository repositoryRead = new MySqlExecuteRepository(mysql.getMysqlRead(), mysql.getDb(), 300);
            for (CleanTable table : mysql.getTableList()) {
                if (!table.getEnabled() || !StringUtils.hasLength(table.getTableName()))
                    continue;
                if (!StringUtils.hasLength(table.getBackDb())) {
                    table.setBackDb(backToDb);
                }
                cleanService.cleanTable(table, repositoryRead, repositoryWrite);
            }
        }
    }
}
