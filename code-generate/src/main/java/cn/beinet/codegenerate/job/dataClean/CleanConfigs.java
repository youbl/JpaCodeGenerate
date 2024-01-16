package cn.beinet.codegenerate.job.dataClean;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

/**
 * 数据清理的配置读取类
 *
 * @author : youbl
 * @create: 2024/1/10 11:06
 */
@Data
@Component
@ConfigurationProperties(prefix = "data-clean")
public class CleanConfigs {
    /**
     * 是否允许数据清理
     */
    private Boolean enable;

    public boolean getEnable() {
        return enable != null && enable;
    }

    /**
     * 要清理的MySQL数据库配置列表
     */
    private MysqlInstance[] mysql;

    @Data
    public static class MysqlInstance {
        /**
         * 是否允许当前实例进行数据清理
         */
        private Boolean enable;

        public boolean getEnable() {
            return enable != null && enable;
        }

        /**
         * 数据库IP
         */
        private String ip;
        /**
         * 数据库端口
         */
        private Integer port;
        /**
         * 数据库登录账号
         */
        private String username;
        /**
         * 数据库登录密码
         */
        private String password;
        /**
         * 要清理的数据库名
         */
        private String database;
        /**
         * 全局备份库名，如果要备份，备份到哪个数据库名（同一IP实例下的另一个数据库）
         * 注：为空时，备份到当前数据库
         */
        private String backToDb;

        /**
         * 要清理的表
         */
        private MysqlTable[] tables;
    }

    @Data
    public static class MysqlTable {
        /**
         * 是否允许当前表进行数据清理
         */
        private Boolean enable;

        public boolean getEnable() {
            return enable != null && enable;
        }

        /**
         * 要清理的表名
         */
        private String tableName;
        /**
         * true表示先备份再删除，false表示不备份直接删除
         */
        private Boolean needBackup;

        public boolean getNeedBackup() {
            return needBackup != null && needBackup;
        }

        /**
         * 主键字段名，只支持自增字段 或 雪花算法这种递增值，只支持单个key
         */
        private String keyField;
        /**
         * 表的分区数量，设置该值，可以更高效的对分区表进行清理，会对每个分区单独清理，非分区表请填写0或1，或置空
         */
        private Integer partitionNum;

        public int getPartitionNum() {
            if (partitionNum == null || partitionNum < 1)
                return 1;
            return partitionNum;
        }

        /**
         * 可空，有时mysql会不使用索引，有此配置表示强制使用该索引进行查询。
         * 注：索引不存在会报错
         */
        private String forceIndexName;
        /**
         * 进行备份或删除的数据，需要的其它sql筛选条件, 可以以and或or开头，也可以不加开头
         */
        private String otherCondition;
        /**
         * 用于时间筛选的字段名,通常为记录入库时间或更新时间
         */
        private String timeField;
        /**
         * 要保留的记录天数，超过该时长的数据，进行删除
         * 注：根据timeField计算
         */
        private int keepDays;
        /**
         * 如果要备份，备份到哪个数据库名（同一IP实例下的另一个数据库）
         * 注：为空时，使用全局配置
         */
        private String backToDb;
        /**
         * 执行清理的小时范围，[0,4-10,15-23] 表示0点，4点到10点，15到23点; 为空表示0-23
         */
        private String cleanHours;

        // 内部用，备份到哪个表
        private String backToTableName;
    }
}
