package cn.beinet.codegenerate.job.dataClean.impl;

import cn.beinet.codegenerate.job.dataClean.Cleanup;
import cn.beinet.codegenerate.job.dataClean.configDal.CleanConfigDal;
import cn.beinet.codegenerate.job.dataClean.configDal.entity.CleanConfig;
import cn.beinet.codegenerate.job.dataClean.configDal.entity.CleanTable;
import cn.beinet.codegenerate.job.dataClean.impl.services.MysqlCleanService;
import cn.beinet.codegenerate.repository.MySqlExecuteRepository;
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
public class MysqlCleanup implements Cleanup {
    // 清理的配置
    private final List<CleanConfig> configList;
    private final MysqlCleanService cleanService;

    public MysqlCleanup(MysqlCleanService cleanService, CleanConfigDal cleanConfigDal) {
        this.cleanService = cleanService;
        this.configList = cleanConfigDal.getAllConfig();
    }

    @Override
    public boolean enabled() {
        return configList != null && configList.size() > 0;
    }

    @Override
    public void clean() {
        for (CleanConfig mysql : configList) {
            if (!mysql.getEnabled() || mysql.getTableList() == null || mysql.getTableList().isEmpty())
                continue;

            // 判断备份到哪个数据库
            String backToDb = StringUtils.hasLength(mysql.getBackDb()) ?
                    mysql.getBackDb() : mysql.getDb();

            MySqlExecuteRepository repository = new MySqlExecuteRepository(mysql.getMysql(), mysql.getDb());
            for (CleanTable table : mysql.getTableList()) {
                if (!table.getEnabled() || !StringUtils.hasLength(table.getTableName()))
                    continue;
                if (!StringUtils.hasLength(table.getBackDb())) {
                    table.setBackDb(backToDb);
                }
                cleanService.cleanTable(table, repository);
            }
        }
    }
}
