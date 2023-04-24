package cn.beinet.codegenerate.util;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

/**
 * 新类
 *
 * @author youbl
 * @since 2023/4/19 16:27
 */
public final class TimeHelper {
    public static final String PATTERN_STR = "yyyy-MM-dd HH:mm:ss";

    public static final DateTimeFormatter PATTERN = DateTimeFormatter.ofPattern(PATTERN_STR);

    /**
     * 获取字符串形式的当前时间
     *
     * @return 时间
     */
    public static String getNow() {
        return LocalDateTime.now().format(PATTERN);
    }
}
