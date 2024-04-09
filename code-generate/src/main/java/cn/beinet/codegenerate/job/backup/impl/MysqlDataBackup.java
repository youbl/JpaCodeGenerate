package cn.beinet.codegenerate.job.backup.impl;

import cn.beinet.codegenerate.job.backup.Backup;
import cn.beinet.codegenerate.job.backup.BackupConfigs;
import cn.beinet.codegenerate.repository.MySqlExecuteRepository;
import cn.beinet.codegenerate.util.FileHelper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.util.List;
import java.util.Map;

/**
 * Description:
 * 遍历指定Mysql实例的所有DB，对有需要备份数据的表，进行数据备份
 *
 * @author : youbl
 * @create: 2024/4/8 20:57
 */
@Service
@Slf4j
public class MysqlDataBackup implements Backup {
    private final BackupConfigs.Mysql configs;

    public MysqlDataBackup(BackupConfigs configs) {
        this.configs = configs.getMysql();
    }

    @Override
    public boolean enabled() {
        if (configs == null)
            return false;
        if (configs.getEnable() != null && !configs.getEnable())
            return false;
        // 只有上午10点和下午10点允许备份
        int hour = LocalDateTime.now().getHour();
        return hour == 10 || hour == 22;
    }

    @Override
    public void operate() {
        if (configs.getInstances() == null || configs.getInstances().length <= 0)
            return;
        for (BackupConfigs.MysqlInstance item : configs.getInstances()) {
            if (item.getEnable() != null && !item.getEnable())
                continue;
            if (item.getBackDataTables() == null || item.getBackDataTables().length == 0)
                continue;

            if (item.getPort() == null) {
                item.setPort(3306);
            }
            log.info("准备备份实例数据 {}:{}", item.getIp(), item.getPort());

            try {
                MySqlExecuteRepository repository = new MySqlExecuteRepository(
                        item.getIp(), item.getPort(), item.getUsername(), item.getPassword(), "information_schema");

                for (String table : item.getBackDataTables()) {
                    // 必须有小数点，数据库名.表名
                    if (StringUtils.hasLength(table) && table.indexOf(".") > 0)
                        backupData(table, repository);
                }
            } catch (Exception namespaceEx) {
                log.error("备份出错:{}:{} {}", item.getIp(), item.getPort(), namespaceEx.getMessage());
            }
        }
    }

    private void backupData(String table, MySqlExecuteRepository repository) {
        log.info("准备备份表数据 {}", table);
        try {
            StringBuilder sbAllData = new StringBuilder();

            String sqlStr = "SELECT * FROM " + table;
            List<Map<String, Object>> result = repository.queryData(sqlStr);
            for (Map<String, Object> row : result) {
                String sql = rowToInsertSQL(table, row);
                sbAllData.append(sql).append("\n");
            }

            String filePath = getFileName(table, repository);
            FileHelper.saveFile(filePath, sbAllData.toString());
            log.info("备份表数据结束:{} {}行,文件:{}", table, result.size(), filePath);
        } catch (Exception exp) {
            log.error("备份表数据出错:{} {}", table, exp.getMessage());
        }
    }

    private String rowToInsertSQL(String table, Map<String, Object> rowData) {
        StringBuilder sbColNames = new StringBuilder();
        StringBuilder sbColValue = new StringBuilder();
        for (Map.Entry<String, Object> field : rowData.entrySet()) {
            if (sbColNames.length() > 0) {
                sbColNames.append(", ");
                sbColValue.append(", ");
            }

            sbColNames.append("`").append(field.getKey()).append("`");
            String val = field.getValue() == null ? "" : field.getValue().toString();
            val = val.replaceAll("'", "''");
            sbColValue.append("'").append(val).append("'");
        }
        return "INSERT INTO " + table + " (" + sbColNames + ")\n  VALUES(" + sbColValue + ");";
    }

    // 替换掉无用的sql内容，如自增数据
    private String replaceNouseSql(String ddl) {
        return ddl.replaceAll("(?i)\\s+AUTO_INCREMENT\\s*=\\s*\\d+\\s+", " ");
    }

    private String getFileName(String table, MySqlExecuteRepository repository) {
        String dir = configs.getBackDir();
        if (!dir.endsWith("/"))
            dir += "/";

        int idx = table.indexOf(".");
        String db = table.substring(0, idx);
        table = table.substring(idx + 1);

        String url = repository.getIpPort();
        url = FileHelper.replaceInvalidCh(url);
        db = FileHelper.replaceInvalidCh(db);
        table = FileHelper.replaceInvalidCh(table);

        return dir + url + "/" + db + "/" + table + "_INSERT.sql";
    }

}
