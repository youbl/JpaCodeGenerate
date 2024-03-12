package cn.beinet.codegenerate.util;

import java.io.IOException;

/**
 * 新接口
 *
 * @author youbl
 * @since 2024/3/12 15:02
 */
@FunctionalInterface
public interface ParamRunnable {
    void run(Object parameter) throws IOException;
}
