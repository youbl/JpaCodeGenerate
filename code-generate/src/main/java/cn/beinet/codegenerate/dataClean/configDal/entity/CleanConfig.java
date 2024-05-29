package cn.beinet.codegenerate.dataClean.configDal.entity;

import cn.beinet.codegenerate.linkinfo.service.entity.LinkInfo;
import lombok.Data;
import lombok.experimental.Accessors;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * 清理配置主类
 *
 * @author youbl
 * @since 2024/1/16 11:58
 */
@Data
@Accessors(chain = true)
public class CleanConfig {
    /**
     * 主键
     */
    private int id;

    /**
     * 是否允许当前实例进行数据清理
     */
    private Boolean enabled;

    public boolean getEnabled() {
        return enabled != null && enabled;
    }

    /**
     * 数据库连接信息表id，读库
     */
    private int linkReadId;

    /**
     * 数据库连接信息表id，写库
     */
    private int linkWriteId;

    /**
     * 要清理的数据库名
     */
    private String db;
    /**
     * 全局备份库名，如果要备份，备份到哪个数据库名（同一IP实例下的另一个数据库）
     * 注：为空时，备份到当前数据库
     */
    private String backDb;

    private LocalDateTime createTime;
    private LocalDateTime updateTime;

    /**
     * 要清理的表所在的数据库连接信息, 读库
     */
    private LinkInfo mysqlRead;

    /**
     * 要清理的表所在的数据库连接信息, 写库
     */
    private LinkInfo mysqlWrite;

    /**
     * 要清理的表清单
     */
    private List<CleanTable> tableList = new ArrayList<>();
}
