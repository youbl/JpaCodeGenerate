package cn.beinet.codegenerate.job.dataClean.configDal;

import cn.beinet.codegenerate.job.dataClean.configDal.entity.CleanConfig;
import cn.beinet.codegenerate.job.dataClean.configDal.entity.CleanTable;
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

    public List<CleanConfig> getAllConfig() {
        String sql = "SELECT * FROM clean_config a WHERE a.enabled=1 AND a.linkinfo_id>0 ORDER BY id";
        List<CleanConfig> configList = jdbcTemplate.query(sql, new Object[0], new BeanPropertyRowMapper<>(CleanConfig.class));
        if (configList == null)
            return new ArrayList<>();
        if (configList.size() > 0) {
            fillLinkInfo(configList);
            fillTableConfig(configList);
        }
        return configList;
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
                if (info.getId() == config.getLinkinfoId()) {
                    config.setMysql(info);
                    break;
                }
            }
        }
    }

    /**
     * 填充每条配置的表清单
     *
     * @param configList 配置清单
     */
    private void fillTableConfig(List<CleanConfig> configList) {
        List<CleanTable> tableList = getAllTableConfig();
        for (CleanTable table : tableList) {
            for (CleanConfig config : configList) {
                if (config.getId() == table.getConfigId()) {
                    config.getTableList().add(table);
                    break;
                }
            }
        }
    }

    private List<CleanTable> getAllTableConfig() {
        String sql = "SELECT * FROM clean_tables a WHERE a.enabled=1 AND a.config_id>0 ORDER BY config_id,id";
        List<CleanTable> tableList = jdbcTemplate.query(sql, new Object[0], new BeanPropertyRowMapper<>(CleanTable.class));
        if (tableList == null)
            return new ArrayList<>();
        return tableList;
    }
}
