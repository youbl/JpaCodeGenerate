package cn.beinet.codegenerate.configs;

import cn.beinet.codegenerate.configs.arguments.AuthDetailArgumentResolver;
import cn.beinet.codegenerate.configs.thirdLogin.ThirdLoginInfo;
import com.fzzixun.etools.oauth.client.AuthProperties;
import com.fzzixun.etools.oauth.client.AuthService;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.context.request.RequestContextListener;
import org.springframework.web.method.support.HandlerMethodArgumentResolver;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import java.util.List;

/**
 * Description:
 *
 * @author : youbl
 * @since 2022/6/9 20:51
 */
@Configuration
public class WebMvcConfig implements WebMvcConfigurer {
    /**
     * 为Controller增加一个参数解析器
     *
     * @param resolvers 参数解析器
     */
    @Override
    public void addArgumentResolvers(List<HandlerMethodArgumentResolver> resolvers) {
        resolvers.add(new AuthDetailArgumentResolver());
    }

    /**
     * 配置 RequestContextListener，解决 RequestContextHolder.getRequestAttributes()为null问题。原因暂时不明
     */
    @Bean
    public RequestContextListener requestContextListener() {
        return new RequestContextListener();
    }

    /**
     * ETools提供的飞书登录类
     *
     * @param thirdLoginInfo yml配置
     * @return 登录类实例
     */
    @Bean
    public AuthService authService(ThirdLoginInfo thirdLoginInfo) {
        AuthProperties authProperties = new AuthProperties();
        authProperties.setAuthHost(thirdLoginInfo.getLoginUrl());
        authProperties.setAuthApiHost(thirdLoginInfo.getLoginUrl());
        authProperties.setAppKey(thirdLoginInfo.getAppKey());
        authProperties.setAppSecret(thirdLoginInfo.getAppSecret());
        return new AuthService(authProperties);
    }
}
