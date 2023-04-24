package cn.beinet.codegenerate.configs.logins;

import org.springframework.core.Ordered;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * 新类
 *
 * @author youbl
 * @since 2023/1/4 18:17
 */
public interface Validator extends Ordered {
    boolean validated(HttpServletRequest request, HttpServletResponse response);
}
