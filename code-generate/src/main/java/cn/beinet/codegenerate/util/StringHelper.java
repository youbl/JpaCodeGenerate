package cn.beinet.codegenerate.util;

import org.springframework.util.DigestUtils;
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

    /**
     * 拼接成字符串计算MD5
     *
     * @param objArr 对象列表
     * @return MD5
     */
    public static String md5(Object... objArr) {
        if (objArr == null || objArr.length <= 0) {
            throw new RuntimeException("md5参数不能为空");
        }
        String useStr;
        if (objArr.length > 1) {
            StringBuilder sb = new StringBuilder();
            int idx = 0;
            for (Object item : objArr) {
                if (idx > 0)
                    sb.append('-');
                sb.append(item);
                idx++;
            }
            useStr = sb.toString();
        } else {
            useStr = String.valueOf(objArr[0]);
        }
        if (useStr.length() <= 0) {
            throw new RuntimeException("md5参数全部为空");
        }
        return DigestUtils.md5DigestAsHex(useStr.getBytes());
    }

    /**
     * 忽略大小写，从源串中查找位置
     *
     * @param str       源串
     * @param searchStr 查找串
     * @return 找到的位置，未找到返回-1
     */
    public static int indexOfIgnoreCase(String str, String searchStr) {
        return indexOfIgnoreCase(str, searchStr, 0);
    }

    /**
     * 忽略大小写，从源串中查找位置
     *
     * @param str       源串
     * @param searchStr 查找串
     * @param startPos  查找起始位置
     * @return 找到的位置，未找到返回-1
     */
    public static int indexOfIgnoreCase(String str, String searchStr, int startPos) {
        if (str == null || searchStr == null) {
            return -1;
        }
        if (str.length() - startPos < searchStr.length()) {
            return -1;
        }
        return str.toLowerCase().indexOf(searchStr.toLowerCase(), startPos);
    }

}
