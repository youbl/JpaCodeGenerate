package cn.beinet.codegenerate.util;

import org.springframework.util.ResourceUtils;
import org.springframework.util.StringUtils;

import java.io.*;

public final class FileHelper {

    /**
     * 把内容写入文件，存在时覆盖
     *
     * @param filePath 文件路径
     * @param content  内容
     * @throws IOException 写入异常
     */
    public static void saveFile(String filePath, String content) throws IOException {
        ensureDirectory(filePath);

        try (BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(filePath)))) {
            writer.write(content);
            writer.flush();
        }
    }

    /**
     * 获取当前目录
     *
     * @return 目录
     */
    public static String getResourceBasePath() {
        // 获取当前根目录
        File path = null;
        try {
            path = new File(ResourceUtils.getURL("classpath:").getPath());
        } catch (FileNotFoundException e) {
            // nothing to do
        }
        if (path == null || !path.exists()) {
            path = new File("");
        }

        String pathStr = path.getAbsolutePath();
        // 如果是在eclipse中运行，则和target同级目录,如果是jar部署到服务器，则默认和jar包同级
        pathStr = pathStr.replace("\\target\\classes", "\\target");

        return pathStr;
    }

    private static void ensureDirectory(String filePath) {
        if (!StringUtils.hasText(filePath)) {
            return;
        }
        filePath = replaceSeparator(filePath);
        if (filePath.contains("/")) {
            String dirPath = filePath.substring(0, filePath.lastIndexOf('/'));
            File dir = new File(dirPath);
            if (!dir.exists() && !dir.mkdirs()) {
                throw new RuntimeException("目录创建失败:" + dirPath);
            }
        }
    }

    private static String replaceSeparator(String str) {
        return str.replace("\\", "/").replace("\\\\", "/");
    }

    /**
     * 替换字符串里，不允许用于文件名的字符
     *
     * @param name 文件名
     * @return 无特殊字符的文件名
     */
    public static String replaceInvalidCh(String name) {
        if (!StringUtils.hasText(name))
            return "";
        //private static Pattern pattern = Pattern.compile("[?*:\"<>\\/|\\s]");
        return name.replaceAll("[?*:\"<>\\/|\\s]", "_");
    }
}
