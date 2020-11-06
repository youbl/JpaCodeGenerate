package cn.beinet.codegenerate.util;

import org.springframework.util.StringUtils;

/**
 * StringHelper
 *
 * @author youbl
 * @version 1.0
 * @date 2020/11/6 17:11
 */
public final class StringHelper {

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
}
