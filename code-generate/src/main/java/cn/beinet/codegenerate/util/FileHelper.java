package cn.beinet.codegenerate.util;

import lombok.SneakyThrows;
import org.apache.commons.io.IOUtils;
import org.springframework.core.io.ClassPathResource;
import org.springframework.util.ResourceUtils;
import org.springframework.util.StringUtils;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStreamWriter;
import java.nio.charset.StandardCharsets;

public final class FileHelper {

    /**
     * 把内容写入文件，存在时覆盖
     *
     * @param filePath 文件路径
     * @param content  内容
     * @throws IOException 写入异常
     */
    @SneakyThrows
    public static void saveFile(String filePath, String content) {
        ensureDirectory(filePath);

        try (FileOutputStream fos = new FileOutputStream(filePath);
             OutputStreamWriter osw = new OutputStreamWriter(fos, StandardCharsets.UTF_8);
             BufferedWriter writer = new BufferedWriter(osw)) {
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

    /**
     * 读取resources目录下的文件内容
     *
     * @param fileName 相对路径, 如 "static/template/dto.template"
     * @return 内容
     */
    @SneakyThrows
    public static String readFileFromResources(String fileName) {
        ClassPathResource resource = new ClassPathResource(fileName);
        try (InputStream stream = resource.getInputStream()) {
            return IOUtils.toString(stream, StandardCharsets.UTF_8);
        }
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


    /**
     * 拼接可以下载的文件路径返回
     *
     * @param file 相对路径
     * @return 绝对路径
     */
    public static String getRealPath(String file) {
        String basePath = getResourceBasePath();
        return new File(basePath, file).getAbsolutePath();
    }

    @SneakyThrows
    public static int readEachLine(String file, ParamRunnable runnable) {
        if (runnable == null)
            throw new RuntimeException("方法不能为空");

        int ret = 0;
        try (BufferedReader reader = new BufferedReader(new FileReader(file))) {
            String line;
            while ((line = reader.readLine()) != null) {
                ret++;
                runnable.run(line);
            }
        }
        return ret;
    }
}
