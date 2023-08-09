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
public class BackupConfigs {
    private Boolean enable;

    private Gitlab gitlab;

    private Nacos nacos;

    private Mysql mysql;

    private Jenkins jenkins;

    private BackFiles files;

    // Gitlab配置，结果是备份到Gitlab上的
    @Data
    public static class Gitlab {
        private String url;
        private String username;
        private String password;
        private String rootDir;
    }

    // 要备份的文件或目录配置
    @Data
    public static class BackFiles {
        private Boolean enable;
        private String backDir;
        private String[] paths;
    }

    // Jenkins的备份配置
    @Data
    public static class Jenkins {
        private Boolean enable;
        private String backDir;
        private JenkinsSite[] sites;
    }

    // Jenkins的备份细项，每个Jenkins服务
    @Data
    public static class JenkinsSite {
        private Boolean enable;
        private String url;
        private String username;
        private String password;
    }

    // Nacos的备份配置
    @Data
    public static class Nacos {
        private Boolean enable;
        private String backDir;
        private NacosSite[] sites;
    }

    // Nacos的备份细项，每个Nacos服务
    @Data
    public static class NacosSite {
        // 是否启用，默认true
        private Boolean enable;
        private String url;
        private String username;
        private String password;
    }

    // Mysql的备份配置
    @Data
    public static class Mysql {
        private Boolean enable;
        private String backDir;
        private MysqlInstance[] instances;
    }

    // Mysql的备份细项，每个Mysql服务
    @Data
    public static class MysqlInstance {
        private Boolean enable;
        private String ip;
        private Integer port;
        private String username;
        private String password;
    }
}
