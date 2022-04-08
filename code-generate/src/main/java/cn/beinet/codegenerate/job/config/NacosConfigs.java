package cn.beinet.codegenerate.job.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

/**
 * Description:
 *
 * @author : youbl
 * @create: 2022/4/8 11:06
 */
@Data
@Component
@ConfigurationProperties(prefix = "backup")
public class NacosConfigs {
    private Gitlab gitlab;

    private Nacos nacos;

    @Data
    public static class Nacos {
        private String backDir;
        private NacosSite[] sites;
    }

    @Data
    public static class NacosSite {
        private String url;
        private String username;
        private String password;
    }

    @Data
    public static class Gitlab {
        private String url;
        private String username;
        private String password;
        private String rootDir;
    }
}
