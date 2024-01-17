package cn.beinet.codegenerate.dataClean.impl.services;

import cn.beinet.codegenerate.dataClean.configDal.entity.CleanTable;
import cn.beinet.codegenerate.repository.MySqlExecuteRepository;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * 进行数据库清理的服务类
 *
 * @author youbl
 * @since 2024/1/11 11:26
 */
@Service
@Slf4j
public class MysqlCleanService {
    // 备份表使用的主键
    private final String BACKUP_TABLE_KEY = "backup_id";

    ConcurrentHashMap<String, Integer> map = new ConcurrentHashMap<>();
    private final AtomicInteger count = new AtomicInteger(0);

    @Async
    public void cleanTable(CleanTable table,
                           MySqlExecuteRepository repository) {
        String key = repository.getIp() + ":" + repository.getDb() + ":" + table.getTableName();
        try {
            if (!isRunningHour(table.getRunHours())) {
                log.warn("当前时段不允许执行: {} {},当前线程数: {}", table.getRunHours(), key, count.get());
                return;
            }

            Integer existVal = map.putIfAbsent(key, 1);
            if (existVal != null) {
                log.warn("前次执行未完成: {},当前线程数: {}", key, count.get());
                return;
            }
            log.info("{} 启动,当前线程数: {}", key, count.incrementAndGet());

            doCleanTable(table, repository);
            log.info("{} 完成，剩余线程数: {}", key, count.decrementAndGet());
        } catch (Exception exp) {
            log.error("{} 异常，剩余线程数: {}", key, count.decrementAndGet(), exp);
        } finally {
            map.remove(key);// 移除该表
        }
    }

    protected void doCleanTable(CleanTable table,
                                MySqlExecuteRepository repository) {
        String tbnameForLog = getTableWithPartition(repository.getDb(), table.getTableName(), -1);
        int keepDays = table.getKeepDays();
        if (keepDays < 1) {
            log.error("{} 数据保留天数不能小于1: {}", tbnameForLog, keepDays);
            return;
        }
        LocalDateTime now = LocalDateTime.now();
        LocalDateTime moveMaxTime = now.minusDays(keepDays);
        log.info("{} 开始操作{}前的数据", tbnameForLog, moveMaxTime);

        // 获取要删除记录的最大主键，后续用主键操作，效率高
        // 注：不兼容没有自增主键的表
        Long maxId = getCleanupMaxId(table, moveMaxTime, repository);
        if (maxId == null || maxId <= 0) {
            log.warn("{} 未获取到要删除记录的最大id: {}", tbnameForLog, maxId);
            return;
        }

        if (table.getNeedBackup()) {
            // 先创建备份用的表
            String backupTbName = createBackupTable(table, moveMaxTime, repository);
            table.setBackToTableName(backupTbName);
        }

        int partition = table.getPartitionNum();
        if (partition <= 1) {
            process(table, maxId, -1, repository);
        } else {
            for (int currentPart = 0; currentPart < partition; currentPart++) {
                process(table, maxId, currentPart, repository);
            }
        }
    }

    protected void process(CleanTable table,
                           long maxId,
                           int partition,
                           MySqlExecuteRepository repository) {
        String tbnameForLog = getTableWithPartition(repository.getDb(), table.getTableName(), partition);

        long startTime = System.currentTimeMillis();

        // 备份数据
        long backupedRows = backupOldRecords(table, maxId, partition, repository);
        long backEndTime = System.currentTimeMillis();
        long costTime = backEndTime - startTime;
        log.info("{} 已备份:{}行 耗时:{}ms", tbnameForLog, backupedRows, costTime);

        // 清理数据
        long deletedRows = deleteOldRecords(table, maxId, partition, repository);
        costTime = System.currentTimeMillis() - backEndTime;
        log.info("{} 操作结束 备份{}行/删除{}行 耗时:{}ms", tbnameForLog, backupedRows, deletedRows, costTime);
    }

    protected long backupOldRecords(CleanTable table,
                                    long maxId,
                                    int partition,
                                    MySqlExecuteRepository repository) {
        if (!table.getNeedBackup()) {
            return 0;// 无需备份
        }
        String sql = getBackupSql(table, maxId, partition, repository);
        if (!StringUtils.hasLength(sql)) {
            log.warn("{} 迁移SQL为空: {}", table.getTableName(), sql);
            return 0L;
        }

        log.info("准备执行备份语句:{}", sql);
        return repository.executeDml(sql);
    }

    @SneakyThrows
    protected long deleteOldRecords(CleanTable table,
                                    long maxId,
                                    int partition,
                                    MySqlExecuteRepository repository) {
        String tbname = getTableWithPartition(repository.getDb(), table.getTableName(), partition);

        String sql = getDeleteSql(table, repository.getDb(), maxId, partition);
        if (!StringUtils.hasLength(sql)) {
            log.warn("{} 删除SQL为空: {}", tbname, sql);
            return 0L;
        }

        // 移除末尾的分号
        sql = sql.trim();
        while (sql.endsWith(";")) {
            sql = sql.substring(0, sql.length() - 1).trim();
        }
        // 末尾添加limit 10000
        Pattern regLimit = Pattern.compile("(?i)\\slimit\\s+\\d+(,\\s*\\d+)?$");
        if (!regLimit.matcher(sql).find()) {
            sql += " limit 10000";
        }

        log.info("准备执行删除语句:{}", sql);
        int totalRows = 0;
        long totalTime = 0;
        long startTime = System.currentTimeMillis();
        int affectedRows = 1;
        while (affectedRows > 0) {
            affectedRows = repository.executeDml(sql);

            long endTime = System.currentTimeMillis();
            long costTime = endTime - startTime;
            totalTime += costTime;
            startTime = endTime;

            totalRows += affectedRows;
            log.info("{} 已删:{}行 耗时:{}ms/{}ms", tbname, totalRows, costTime, totalTime);
            Thread.sleep(10);
        }
        return totalRows;
    }

    /**
     * 检查并创建备份数据目标表，并返回表名
     *
     * @return 表名
     */
    protected String createBackupTable(CleanTable table,
                                       LocalDateTime moveMaxTime,
                                       MySqlExecuteRepository repository) {
        String originTb = table.getTableName();
        // 默认按月备份
        String backTb = originTb + "_bak" + moveMaxTime.format(DateTimeFormatter.ofPattern("yyyyMM"));

        if (repository.existTable(table.getBackDb(), backTb)) {
            return backTb;
        }
        // 备份表不存在时，创建它
        String createSql = getCreateSql(table.getBackDb(), backTb, originTb, repository);
        repository.executeDml(createSql);
        log.info("{}库备份表创建成功:{}", table.getBackDb(), backTb);
        return backTb;
    }

    // 生成建表语句
    private String getCreateSql(String db,
                                String backTb,
                                String originTb,
                                MySqlExecuteRepository repository) {
        // 使用 CREATE TABLE backTb LIKE originTb 会得到一模一样的表结构，包括主键、索引、计算列等等
        // 但是备份完成，删除源表时，可能出现中断，导致下次再备份，就出现备份主键冲突的情况。
        // 因此，暂时不用这种建表语句
//        if (StringUtils.hasLength(db)) {
//            return "CREATE TABLE `" + db + "`.`" + backTb + "` LIKE `" + originTb + "`";
//        }
//        return "CREATE TABLE `" + backTb + "` LIKE `" + originTb + "`";

        List<String> columns = repository.findColumnByTable(repository.getDb(), originTb);
        StringBuilder colSb = new StringBuilder();
        for (String col : columns) {
            if (colSb.length() > 0) {
                colSb.append(',');
            }
            colSb.append('`').append(col).append('`');
        }

        if (StringUtils.hasLength(db)) {
            db = "`" + db + "`.";
        }
        // 这种复制表结构语句，不会复制索引
        return "CREATE TABLE " + db + "`" + backTb + "`(" + BACKUP_TABLE_KEY + " BIGINT NOT NULL AUTO_INCREMENT PRIMARY KEY) AS " +
                " SELECT " + colSb + " FROM `" + originTb + "`" +
                " WHERE 1=2";
    }

    /**
     * 根据时间，查询出要删除数据的最大主键id
     *
     * @param moveMaxTime 时间
     * @return id
     */
    protected Long getCleanupMaxId(CleanTable table,
                                   LocalDateTime moveMaxTime, MySqlExecuteRepository repository) {
        String forceIndexSql = StringUtils.hasLength(table.getForceIndexName()) ?
                "FORCE INDEX(`" + table.getForceIndexName() + "`)" : "";
        String strTime = moveMaxTime.format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:00:00"));
        String sql = "select `" + table.getKeyField() + "` from " + table.getTableName() +
                " " + forceIndexSql +
                " where `" + table.getTimeField() + "`<'" + strTime +
                "' ORDER BY `" + table.getKeyField() + "` DESC LIMIT 1";
        log.info("取最大id:{}", sql);
        Object obj = repository.queryOneField(sql);
        if (obj == null)
            return 0L;
        return Long.parseLong(obj.toString());
    }

    /**
     * 获取备份用的sql，在当前基类里，用jdbcTemplate执行
     *
     * @param maxId 最大主键
     * @return sql
     */
    protected String getBackupSql(CleanTable table,
                                  long maxId,
                                  int partition,
                                  MySqlExecuteRepository repository) {
        String targetTable = table.getBackToTableName();
        List<String> columns = repository.findColumnByTable(table.getBackDb(), targetTable);
        StringBuilder colSb = new StringBuilder();
        for (String col : columns) {
            if (col.equals(BACKUP_TABLE_KEY)) {
                continue;
            }
            if (colSb.length() > 0) {
                colSb.append(',');
            }
            colSb.append('`').append(col).append('`');
        }
        String tbName = getTableWithPartition(repository.getDb(), table.getTableName(), partition);
        String ret = "INSERT INTO `" + table.getBackDb() + "`.`" + targetTable + "`(" + colSb + ")" +
                " SELECT " + colSb + " FROM " + tbName +
                " WHERE `" + table.getKeyField() + "`<=" + maxId;

        return ret + getOtherCondition(table.getOtherCondition());
    }

    /**
     * 拼接表名，如果有分区号，增加指定分区号
     *
     * @param dbName    库名
     * @param tableName 表名
     * @param partition 要查询的分区号，小于0表示无分区
     * @return 带指定分区的表名
     */
    private String getTableWithPartition(String dbName, String tableName, int partition) {
        String ret = "`" + tableName + "`";
        if (StringUtils.hasLength(dbName)) {
            ret = "`" + dbName + "`." + ret;
        }
        if (partition >= 0)
            ret += " PARTITION(p" + partition + ")";
        return ret;
    }

    /**
     * 获取删除用的sql，在当前基类里，用jdbcTemplate执行
     * 基类里会加上 limit 10000 每次只删除一万
     *
     * @param maxId 最大主键
     * @return sql
     */
    protected String getDeleteSql(CleanTable table, String dbName, long maxId, int partition) {
        String tbName = getTableWithPartition(dbName, table.getTableName(), partition);
        String ret = "DELETE FROM " + tbName + " WHERE `" + table.getKeyField() + "`<=" + maxId;
        return ret + getOtherCondition(table.getOtherCondition());
    }

    protected String getOtherCondition(String otherCondSql) {
        if (otherCondSql == null) {
            return "";
        }
        otherCondSql = otherCondSql.trim();
        if (otherCondSql.length() <= 0) {
            return "";
        }
        Pattern pattern = Pattern.compile("^(?i)(and|or)\\s");
        Matcher matcher = pattern.matcher(otherCondSql);
        if (matcher.find()) {
            return " " + otherCondSql;
        }
        return " AND " + otherCondSql;
    }

    protected boolean isRunningHour(String hours) {
        // 默认都要备份
        if (!StringUtils.hasLength(hours)) {
            return true;
        }
        int currentHour = LocalDateTime.now().getHour();
        String[] hoursArr = hours.split("[,\\s]");
        for (String hour : hoursArr) {
            if (hour.length() <= 0) {
                continue;
            }
            int[] arrHour = parseHourItem(hour);
            if (arrHour.length != 2) {
                continue;
            }
            if (arrHour[0] <= currentHour && currentHour <= arrHour[1]) {
                return true;
            }
        }
        return false;
    }

    // 解析时间项，返回2维数组：起始时间和结束时间
    protected int[] parseHourItem(String strHour) {
        if (isNumeric(strHour)) {
            int hour = Integer.parseInt(strHour);
            return new int[]{hour, hour};
        }

        String[] arr = strHour.split("-");
        if (arr.length != 2) {
            return new int[0];
        }
        if (!isNumeric(arr[0]) || !isNumeric(arr[1])) {
            return new int[0];
        }
        int hour1 = Integer.parseInt(arr[0]);
        int hour2 = Integer.parseInt(arr[1]);
        return new int[]{hour1, hour2};
    }

    private static boolean isNumeric(String str) {
        for (int i = str.length() - 1; i >= 0; i--) {
            if (!Character.isDigit(str.charAt(i))) {
                return false;
            }
        }
        return true;
    }
}
