package cn.beinet.codegenerate.controller.diff;

import cn.beinet.codegenerate.controller.dto.NacosDto;
import cn.beinet.codegenerate.service.NacosService;
import lombok.RequiredArgsConstructor;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Properties;

@RestController
@RequiredArgsConstructor
public class NacosController {
    private final NacosService nacosService;
    private final JdbcTemplate jdbcTemplate;

    // 获取命名空间列表
    @GetMapping("v1/nacos/namespaces")
    public List<String> getNamespaces(NacosDto dto) {
        return nacosService.getNamespaces(dto);
    }


    // 获取指定命名空间的文件列表
    @GetMapping("v1/nacos/files")
    public List<String> getFiles(NacosDto dto) {
        return nacosService.getFiles(dto);
    }


    // 获取指定文件配置
    @GetMapping("v1/nacos/concfig")
    public Properties getFile(NacosDto dto) {
        dto.setGroup("DEFAULT_GROUP");
        String ymlStr = nacosService.getFile(dto);
        return nacosService.parseYmlToKV(ymlStr, true);
    }

    // 忽略指定的key差异，或取消忽略
    @GetMapping("v1/nacos/ignore")
    public List<String> ignoreCompareGet(@RequestParam String app) {
        String sql = "select `key` from configIgnoreKeys where app_key='" +
                formatSqlVal(app) + "'";
        return jdbcTemplate.queryForList(sql, String.class);
    }

    // 添加 【忽略指定的key差异】
    @PostMapping("v1/nacos/ignore")
    public void ignoreCompareAdd(@RequestParam String app, @RequestParam String key) {
        String sql = "INSERT INTO configIgnoreKeys (app_key,`key`)VALUES('" +
                formatSqlVal(app) + "','" + formatSqlVal(key) + "')";
        jdbcTemplate.execute(sql);
    }

    // 删除 【忽略指定的key差异】
    @DeleteMapping("v1/nacos/ignore")
    public void ignoreCompareDel(@RequestParam String app, @RequestParam String key) {
        String sql = "delete from configIgnoreKeys where app_key='" +
                formatSqlVal(app) + "'";

        key = formatSqlVal(key);
        if (!key.equals("all")) {
            sql += " and `key`='" + key + "'";
        }
        jdbcTemplate.execute(sql);
    }

    // 替换单引号
    private String formatSqlVal(String val) {
        if (!StringUtils.hasLength(val)) {
            return "";
        }
        return val.trim().replaceAll("'", "");
    }
}
/*

CREATE TABLE `configIgnoreKeys` (
  `id` INT(11) NOT NULL AUTO_INCREMENT COMMENT '自增ID',
  `app_key` VARCHAR(1000) NOT NULL DEFAULT '' COMMENT 'app标识',
  `key` VARCHAR(200) NOT NULL DEFAULT '' COMMENT 'app密钥',
  `create_date` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  PRIMARY KEY (`id`),
  KEY `idx_appkey` (`app_key`(768))
) ENGINE=INNODB DEFAULT CHARSET=utf8mb4 COMMENT='配置忽略信息表'

* */
