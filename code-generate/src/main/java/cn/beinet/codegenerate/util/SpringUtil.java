package cn.beinet.codegenerate.util;

import org.springframework.beans.BeansException;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Component;

import java.util.Map;

/**
 * Description:
 *
 * @author : youbl
 * @create: 2022/2/16 16:41
 */

@Component
public class SpringUtil implements ApplicationContextAware {

    private static ApplicationContext springApplicationContext;
    private static Environment springEnvironment;

    @Override
    public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
        springApplicationContext = applicationContext;
        springEnvironment = springApplicationContext.getEnvironment();
    }


    public static ApplicationContext getApplicationContext() {
        return springApplicationContext;
    }

    public static Environment getEnv() {
        return springEnvironment;
    }


    /**
     * get bean
     *
     * @param name bean名
     * @return bean
     */
    public static Object getBean(String name) {
        return springApplicationContext.getBean(name);
    }

    /**
     * get bean
     *
     * @param clazz bean类型
     * @param <T>   bean类型
     * @return bean
     */
    public static <T> T getBean(Class<T> clazz) {
        return springApplicationContext.getBean(clazz);
    }


    /**
     * get bean
     *
     * @param name  bean名
     * @param clazz bean类型
     * @param <T>   bean类型
     * @return bean
     */
    public static <T> T getBean(String name, Class<T> clazz) {
        return springApplicationContext.getBean(name, clazz);
    }

    /**
     * get getBeansOfType
     *
     * @param clazz bean类型
     * @param <T>   bean类型
     * @return bean
     */
    public <T> Map<String, T> getBeansOfType(Class<T> clazz) {
        return springApplicationContext.getBeansOfType(clazz);
    }

    /**
     * 读取配置值
     *
     * @param key 配置的key
     * @return 值
     */
    public static String getProperty(String key) {
        String ret = springApplicationContext.getEnvironment().getProperty(key);
        return (ret == null) ? "" : ret;
    }
}
