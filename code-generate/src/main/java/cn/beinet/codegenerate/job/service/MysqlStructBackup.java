package cn.beinet.codegenerate.job.service;

import cn.beinet.codegenerate.job.config.BackupConfigs;
import cn.beinet.codegenerate.repository.ColumnRepository;
import cn.beinet.codegenerate.util.FileHelper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * Description:
 * 遍历Mysql实例的所有DB，并备份结构到本地文件
 *
 * @author : youbl
 * @create: 2022/4/11 10:57
 */
@Service
@Slf4j
public class MysqlStructBackup implements Backup {
    private final BackupConfigs.Mysql configs;

    public MysqlStructBackup(BackupConfigs configs) {
        this.configs = configs.getMysql();
    }

    @Override
    public void operate() {
        for (BackupConfigs.MysqlInstance item : configs.getInstances()) {
            if (item.getPort() == null) {
                item.setPort(3306);
            }
            log.info("准备备份实例 {}:{}", item.getIp(), item.getPort());

            try {
                ColumnRepository repository = ColumnRepository.getRepository(
                        item.getIp(), 3306, item.getUsername(), item.getPassword());
                List<String> databases = repository.findDatabases();
                for (String db : databases) {
                    backupDatabase(db, repository);
                }
            } catch (Exception namespaceEx) {
                log.error("备份出错:{}:{} {}", item.getIp(), item.getPort(), namespaceEx.getMessage());
            }
        }
    }

    private void backupDatabase(String db, ColumnRepository repository) {
        log.info("准备备份db {}", db);
        int idx = 0;
        try {
            List<String> tables = repository.findTables(db);
            for (String tb : tables) {
                backupTable(db, tb, repository);
                idx++;
            }
            log.info("备份db结束:{} {}个表", db, idx);
        } catch (Exception exp) {
            log.error("备份db出错:{}-{} {}", db, idx, exp.getMessage());
        }
    }

    private void backupTable(String db, String table, ColumnRepository repository) {
        try {
            String ddl = repository.getTableDDL(db, table);
            ddl = replaceNouseSql(ddl);
            String filePath = getFileName(db, table, repository);
            FileHelper.saveFile(filePath, ddl);
        } catch (Exception exp) {
            log.error("备份table出错:{}.{} {}", db, table, exp.getMessage());
        }
    }

    // 替换掉无用的sql内容，如自增数据
    private String replaceNouseSql(String ddl) {
        return ddl.replaceAll("(?i)\\s+AUTO_INCREMENT\\s*=\\s*\\d+\\s+", "");
    }

    private String getFileName(String db, String table, ColumnRepository repository) {
        String dir = configs.getBackDir();
        if (!dir.endsWith("/"))
            dir += "/";

        String url = repository.getIpPort();
        url = FileHelper.replaceInvalidCh(url);
        db = FileHelper.replaceInvalidCh(db);
        table = FileHelper.replaceInvalidCh(table);

        return dir + url + "/" + db + "/" + table;
    }

}