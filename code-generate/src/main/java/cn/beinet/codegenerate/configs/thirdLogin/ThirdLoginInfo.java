package cn.beinet.codegenerate.configs.thirdLogin;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

/**
 * 第三方登录信息配置
 *
 * @author youbl
 * @since 2023/12/26 20:47
 */
@Data
@Component
@ConfigurationProperties(prefix = "third-login-info")
public class ThirdLoginInfo {
    private String loginUrl;
    private String callbackUrl;
    private String callbackPara;
    private String appKey;
    private String appSecret;
}
