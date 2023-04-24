package cn.beinet.codegenerate.util;

import org.springframework.util.StringUtils;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;

/**
 * 新类
 *
 * @author youbl
 * @since 2023/1/4 11:35
 */
public final class ProcessHelper {

    /**
     * 执行shell命令，并读取返回内容
     *
     * @param command
     * @param arguments
     * @return
     */
    public static String run(String command, String... arguments) {
        List<String> commands = new ArrayList<>();
        commands.add(command);
        if (arguments != null) {
            for (String item : arguments)
                commands.add(item);
        }
        //Runtime.getRuntime().exec(commands);
        ProcessBuilder processBuilder = new ProcessBuilder(commands);
        try {
            Process process = processBuilder.start();
            String outputInfo = readInputStream(process.getInputStream());
            String outputErr = readInputStream(process.getErrorStream());
            if (StringUtils.hasLength(outputErr)) {
                outputInfo += "\nError: " + outputErr;
            }
            return outputInfo;
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * 读取输入流里的字符串信息
     *
     * @param stream 流
     * @return string
     */
    private static String readInputStream(InputStream stream) {
        try {
            BufferedReader stdInput = new BufferedReader(new InputStreamReader(stream));
            StringBuilder stringBuilder = new StringBuilder();
            String line;
            while ((line = stdInput.readLine()) != null) {
                stringBuilder.append(line).append("\n");
            }
            return stringBuilder.toString();
        } catch (Exception exp) {
            throw new RuntimeException(exp);
        }
    }
}
