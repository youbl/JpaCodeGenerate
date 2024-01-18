package cn.beinet.codegenerate.dataClean.configDal.entity;

import lombok.Data;
import lombok.experimental.Accessors;
import org.springframework.util.StringUtils;

import java.time.LocalDateTime;

/**
 * 清理配置表
 *
 * @author youbl
 * @since 2024/1/16 13:36
 */
@Data
@Accessors(chain = true)
public class CleanTable {
    /**
     * 说明
     */
    private String title;
    /**
     * 主键
     */
    private int id;

    /**
     * 对应配置主表id
     */
    private int configId;

    /**
     * 是否允许当前表进行数据清理
     */
    private Boolean enabled;

    public boolean getEnabled() {
        return enabled != null && enabled;
    }

    /**
     * 要清理的表名
     */
    private String tableName;

    public String getTableName() {
        if (StringUtils.hasLength(tableName)) {
            return tableName;
        }
        throw new IllegalArgumentException("表名配置 不能为空");
    }

    /**
     * 用于时间筛选的字段名,通常为记录入库时间或更新时间
     */
    private String timeField;

    public String getTimeField() {
        if (StringUtils.hasLength(timeField)) {
            return timeField;
        }
        throw new IllegalArgumentException(getTableName() + "的时间字段配置 不能为空");
    }

    /**
     * 可空，有时mysql会不使用索引，有此配置表示强制使用该索引进行查询。
     * 注：索引不存在会报错
     */
    private String forceIndexName;

    /**
     * 主键字段名，只支持自增字段 或 雪花算法这种递增值，只支持单个key
     */
    private String keyField;

    public String getKeyField() {
        if (keyField == null) {
            keyField = "id";
        } else {
            keyField = keyField.trim();
            if (keyField.length() == 0) {
                keyField = "id";
            }
        }
        return keyField;
    }

    /**
     * 进行备份或删除的数据，需要的其它sql筛选条件, 可以以and或or开头，也可以不加开头
     */
    private String otherCondition;

    /**
     * 要保留的记录天数，超过该时长的数据，进行删除
     * 注：根据timeField计算
     */
    private Integer keepDays;

    public int getKeepDays() {
        if (keepDays == null || keepDays <= 0)
            keepDays = 90;
        return keepDays;
    }

    /**
     * 执行清理的小时范围，[0,4-10,15-23] 表示0点，4点到10点，15到23点; 为空表示0-23
     */
    private String runHours;

    /**
     * true表示先备份再删除，false表示不备份直接删除
     */
    private Boolean needBackup;

    public boolean getNeedBackup() {
        return needBackup != null && needBackup;
    }

    /**
     * 如果要备份，备份到哪个数据库名（同一IP实例下的另一个数据库）
     * 注：为空时，使用全局配置
     */
    private String backDb;

    /**
     * 表的分区数量，设置该值，可以更高效的对分区表进行清理，会对每个分区单独清理，非分区表请填写0或1，或置空
     */
    private Integer partitionNum;

    public int getPartitionNum() {
        if (partitionNum == null || partitionNum < 1)
            return 1;
        return partitionNum;
    }

    // 非字段，内部用，备份到哪个表
    private String backToTableName;

    private LocalDateTime createTime;
    private LocalDateTime updateTime;
}
