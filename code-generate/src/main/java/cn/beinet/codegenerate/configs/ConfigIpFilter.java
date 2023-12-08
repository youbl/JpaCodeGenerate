package cn.beinet.codegenerate.configs;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

/**
 * 新类
 *
 * @author youbl
 * @since 2023/12/8 16:15
 */
@Data
@Component
@ConfigurationProperties(prefix = "ip-filter")
public class ConfigIpFilter {
    private String[] whiteIp;

    private String[] excludeUrl;
}
