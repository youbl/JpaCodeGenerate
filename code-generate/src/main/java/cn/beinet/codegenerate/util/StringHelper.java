package cn.beinet.codegenerate.util;

import lombok.extern.slf4j.Slf4j;
import org.springframework.util.DigestUtils;
import org.springframework.util.StringUtils;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

/**
 * StringHelper
 *
 * @author youbl
 * @version 1.0
 * @since 2020/11/6 17:11
 */
@Slf4j
public final class StringHelper {
    private static final Random RANDOM = new Random();

    /**
     * 首字母大写
     *
     * @param name 名称
     * @return 首字母大写
     */
    public static String upFirstChar(String name) {
        if (!StringUtils.hasLength(name))
            return "";
        return name.substring(0, 1).toUpperCase() + name.substring(1);
    }

    /**
     * 把传入的字符串转换为Pascal格式，即：
     * 首字母大写，之后的每个下划线后第一个字母大写，并移除下划线
     *
     * @param name 名称
     * @return Pascal格式
     */
    public static String castToPascal(String name) {
        return cast(name, false);
    }

    /**
     * 把传入的字符串转换为Camel格式，即：
     * 首字母小写，之后的每个下划线后第一个字母大写，并移除下划线
     *
     * @param name 名称
     * @return Camel格式
     */
    public static String castToCamel(String name) {
        return cast(name, true);
    }

    private static String cast(String name, boolean isCamel) {
        if (!StringUtils.hasLength(name))
            return "";
        StringBuilder sb = new StringBuilder();
        String chFirst = String.valueOf(name.charAt(0));
        String strCh = isCamel ? chFirst.toLowerCase() : chFirst.toUpperCase();
        sb.append(strCh);

        boolean isUnderline = false;
        int chIdx = 0;
        for (char item : name.toCharArray()) {
            chIdx++;
            if (chIdx == 1)
                continue; // 第一个字符，上面已经append了

            if (isUnderline) {
                // 不考虑连续2个下划线的情况，也不考虑首字母为下划线的情况
                sb.append(String.valueOf(item).toUpperCase());
                isUnderline = false;
                continue;
            }
            isUnderline = (item == '_');
            if (!isUnderline) {
                sb.append(item);
            }
        }
        return sb.toString();
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
        String md5Str = DigestUtils.md5DigestAsHex(useStr.getBytes());
        log.debug("{} md5:{}", useStr, md5Str);
        return md5Str;
    }

    /**
     * 拆分字符串，并剔除空项
     *
     * @param str   要拆分的字符串
     * @param regex 拆分正则
     * @return 拆分后数组
     */
    public static List<String> splitAndRemoveEmpty(String str, String regex) {
        if (str == null)
            return new ArrayList<>();
        String[] arr = str.split(regex);
        List<String> list = new ArrayList<String>();
        for (String item : arr) {
            String tmp = item.trim();
            if (!tmp.isEmpty())
                list.add(tmp);
        }
        return list;
    }

    /**
     * 对orgin字符串进行批量替换
     *
     * @param origin     要替换的源字符串
     * @param replaceOld 替换前的内容
     * @param replaceNew 替换后的内容
     * @return 替换结果
     */
    public static String replaceBatch(String origin, List<String> replaceOld, List<String> replaceNew) {
        if (!StringUtils.hasLength(origin)) {
            return "";
        }
        if (replaceOld == null || replaceNew == null || replaceOld.size() != replaceNew.size()) {
            return origin;
        }
        for (int i = 0, j = replaceOld.size(); i < j; i++) {
            origin = origin.replace(replaceOld.get(i), replaceNew.get(i));
        }
        return origin;
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

    /**
     * 全部替换StringBuilder里的字符串
     *
     * @param builder StringBuilder
     * @param from    要被替换的旧串
     * @param to      要替换为哪个新串
     */
    public static int replaceAll(StringBuilder builder, String from, String to) {
        return replaceAll(builder, from, to, Integer.MAX_VALUE);
    }

    /**
     * 替换StringBuilder里的字符串
     *
     * @param builder     StringBuilder
     * @param from        要被替换的旧串
     * @param to          要替换为哪个新串
     * @param replaceTime 最多替换次数
     */
    public static int replaceAll(StringBuilder builder, String from, String to, int replaceTime) {
        int ret = 0;
        int index = builder.indexOf(from);
        while (ret < replaceTime && index != -1) {
            builder.replace(index, index + from.length(), to);
            index += to.length(); // Move to the end of the replacement
            index = builder.indexOf(from, index);
            ret++;
        }
        return ret;
    }

    /**
     * 从StringBuilder里查找起始串和结束串，把包含起始和结束的所有内容替换掉
     *
     * @param builder StringBuilder
     * @param start   要被替换的起始串
     * @param end     要被替换的结束串
     * @param to      要替换为哪个新串
     * @return 替换成功还是失败
     */
    public static boolean replaceByStartAndEnd(StringBuilder builder, String start, String end, String to) {
        int indexStart = builder.indexOf(start);
        int indexEnd = builder.indexOf(end);
        if (indexStart >= 0 && indexEnd <= 0)
            throw new RuntimeException("找到了开始标志，没找到结束");
        if (indexStart < 0 && indexEnd >= 0)
            throw new RuntimeException("找到了结束标志，没找到开始");

        if (indexStart >= 0) {
            builder.replace(indexStart, indexEnd + end.length(), to);
            return true;
        }
        return false;
    }
}
