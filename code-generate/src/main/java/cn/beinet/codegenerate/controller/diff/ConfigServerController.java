package cn.beinet.codegenerate.controller.diff;

import cn.beinet.codegenerate.service.ConfigServerDiffService;
import cn.beinet.codegenerate.service.dto.SpringConfigUrlDto;
import lombok.RequiredArgsConstructor;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import java.util.Properties;

/**
 * 用于Spring Cloud Config Server配置对比的接口类
 */
@RestController
@RequiredArgsConstructor
public class ConfigServerController {
    private final ConfigServerDiffService configServerDiffService;
    private final JdbcTemplate jdbcTemplate;

    // 获取指定url配置
    @PostMapping("v1/configServer/config")
    public Properties getConfigs(@RequestBody SpringConfigUrlDto dto) {
        return configServerDiffService.getConfigs(dto);
    }
}