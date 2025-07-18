package cn.beinet.codegenerate.configs;

import cn.beinet.codegenerate.configs.arguments.AuthDetailArgumentResolver;
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
 * @create: 2022/6/9 20:51
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
}
