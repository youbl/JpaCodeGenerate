package cn.beinet.codegenerate.util;

import org.springframework.util.StringUtils;

import java.util.Random;

/**
 * StringHelper
 *
 * @author youbl
 * @version 1.0
 * @date 2020/11/6 17:11
 */
public final class StringHelper {
    private static final Random RANDOM = new Random();

    /**
     * 首字母大写
     *
     * @param name 名称
     * @return 首字母大写
     */
    public static String upFirstChar(String name) {
        if (StringUtils.isEmpty(name))
            return "";
        return name.substring(0, 1).toUpperCase() + name.substring(1);
    }

    /**
     * 首字母小写
     *
     * @param name 名称
     * @return 首字母小写
     */
    public static String lowFirstChar(String name) {
        if (StringUtils.isEmpty(name))
            return "";
        return name.substring(0, 1).toLowerCase() + name.substring(1);
    }

    /**
     * 获取0到endExclusive区间的随机值
     *
     * @param endExclusive 结束值，返回值不含endExclusive
     * @return 随机值
     */
    public static int nextInt(int endExclusive) {
        return nextInt(0, endExclusive);
    }

    /**
     * 获取区间随机值
     *
     * @param startInclusive 起始值
     * @param endExclusive   结束值，返回值不含endExclusive
     * @return 随机值
     */
    public static int nextInt(int startInclusive, int endExclusive) {
        if (startInclusive < 0) {
            throw new IllegalArgumentException("startInclusive必须大于等于0");
        }
        if (endExclusive <= startInclusive) {
            throw new IllegalArgumentException("endExclusive必须大于startInclusive");
        }
        return startInclusive + RANDOM.nextInt(endExclusive - startInclusive);
    }
}
