package cn.beinet.codegenerate.dataClean.configDal;

import cn.beinet.codegenerate.dataClean.configDal.entity.CleanConfig;
import cn.beinet.codegenerate.dataClean.configDal.entity.CleanTable;
import cn.beinet.codegenerate.linkinfo.service.LinkInfoService;
import cn.beinet.codegenerate.linkinfo.service.entity.LinkInfo;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.jdbc.core.BeanPropertyRowMapper;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

/**
 * 清理配置的数据库操作服务类,
 * 暂时只支持mysql
 *
 * @author youbl
 * @since 2024/1/16 11:30
 */
@Component
@Slf4j
@RequiredArgsConstructor
public class CleanConfigDal {
    private final LinkInfoService linkInfoService;
    private final JdbcTemplate jdbcTemplate;

    /**
     * 获取所有配置
     *
     * @param isRun true表示获取可运行的配置，false表示获取全部配置
     * @return 配置列表
     */
    public List<CleanConfig> getAllConfig(boolean isRun) {
        String enableSql = "";
        if (isRun) {
            enableSql = " AND a.enabled=1 ";
        }
        String sql = "SELECT * FROM clean_config a WHERE a.link_write_id>0 " + enableSql + " ORDER BY id";
        List<CleanConfig> configList = jdbcTemplate.query(sql, new Object[0], new BeanPropertyRowMapper<>(CleanConfig.class));
        if (configList == null)
            return new ArrayList<>();
        if (configList.size() > 0) {
            if (isRun)
                fillLinkInfo(configList);
            fillTableConfig(configList, isRun);
        }
        return configList;
    }

    public int saveConfig(CleanConfig config) {
        String sql;
        List<Object> params = new ArrayList<>();
        params.add(config.getLinkReadId());
        params.add(config.getLinkWriteId());
        params.add(config.getEnabled());
        params.add(config.getDb());
        params.add(config.getBackDb());

        if (config.getId() > 0) {
            sql = "UPDATE `clean_config` SET `link_read_id`=?,`link_write_id`=?,`enabled`=?,`db`=?,`back_db`=? WHERE `id`=?";
            // sql里的问号，必须跟参数个数一样多，否则报错：Parameter index out of range (5 > number of parameters, which is 4).
            params.add(config.getId());
        } else {
            sql = "INSERT INTO `clean_config` (`link_read_id`,`link_write_id`,`enabled`,`db`,`back_db`)VALUES(?,?,?,?,?)";
        }
        return jdbcTemplate.update(sql, params.toArray());
    }

    public int changeStatus(int id, boolean status) {
        int enabled = status ? 1 : 0;
        String sql = "UPDATE clean_config SET enabled=? WHERE id=?";
        return jdbcTemplate.update(sql, new Object[]{enabled, id});
    }


    public int saveTableConfig(CleanTable table) {
        String sql;
        List<Object> params = new ArrayList<>();
        params.add(table.getTitle());
        params.add(table.getConfigId());
        params.add(table.getEnabled());
        params.add(table.getTableName());
        params.add(table.getTimeField());
        params.add(table.getForceIndexName());
        params.add(table.getKeyField());
        params.add(table.getOtherCondition());
        params.add(table.getKeepDays());
        params.add(table.getRunHours());
        params.add(table.getNeedBackup());
        params.add(table.getPartitionNum());

        if (table.getId() > 0) {
            sql = "UPDATE `clean_tables` SET " +
                    "`title`=?,`config_id`=?,`enabled`=?,`table_name`=?,`time_field`=?,`force_index_name`=?,`key_field`=?," +
                    "`other_condition`=?,`keep_days`=?,`run_hours`=?,`need_backup`=?,`partition_num`=? " +
                    " WHERE `id`=?";
            // sql里的问号，必须跟参数个数一样多，否则报错：Parameter index out of range (5 > number of parameters, which is 4).
            params.add(table.getId());
        } else {
            sql = "INSERT INTO `clean_tables` (`title`,`config_id`,`enabled`,`table_name`,`time_field`,`force_index_name`,`key_field`,`other_condition`,`keep_days`,`run_hours`,`need_backup`,`partition_num`)" +
                    " VALUES(?,?,?,?,?,?,?,?,?,?,?,?)";
        }
        return jdbcTemplate.update(sql, params.toArray());
    }

    public int changeTableStatus(int id, boolean status) {
        int enabled = status ? 1 : 0;
        String sql = "UPDATE clean_tables SET enabled=? WHERE id=?";
        return jdbcTemplate.update(sql, new Object[]{enabled, id});
    }

    /**
     * 根据配置的连接id，读取连接信息并填充
     *
     * @param configList 配置清单
     */
    private void fillLinkInfo(List<CleanConfig> configList) {
        List<LinkInfo> infoList = linkInfoService.getLinkInfo("mysql");
        for (CleanConfig config : configList) {
            for (LinkInfo info : infoList) {
                if (info.getId() == config.getLinkReadId()) {
                    config.setMysqlRead(info);
                }
                if (info.getId() == config.getLinkWriteId()) {
                    config.setMysqlWrite(info);
                }
            }
        }
    }

    /**
     * 填充每条配置的表清单
     *
     * @param configList 配置清单
     */
    private void fillTableConfig(List<CleanConfig> configList, boolean isRun) {
        List<CleanTable> tableList = getAllTableConfig(isRun);
        for (CleanTable table : tableList) {
            for (CleanConfig config : configList) {
                if (config.getId() == table.getConfigId()) {
                    config.getTableList().add(table);
                    break;
                }
            }
        }
    }

    private List<CleanTable> getAllTableConfig(boolean isRun) {
        String enableSql = "";
        if (isRun) {
            enableSql = " AND a.enabled=1 ";
        }
        String sql = "SELECT * FROM clean_tables a WHERE a.config_id>0 " + enableSql + " ORDER BY config_id,id";
        List<CleanTable> tableList = jdbcTemplate.query(sql, new Object[0], new BeanPropertyRowMapper<>(CleanTable.class));
        if (tableList == null)
            return new ArrayList<>();
        return tableList;
    }
}
