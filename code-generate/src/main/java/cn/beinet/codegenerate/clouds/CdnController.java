package cn.beinet.codegenerate.clouds;

import cn.beinet.codegenerate.clouds.dto.CdnDto;
import lombok.SneakyThrows;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;

/**
 * CDN操作控制类
 *
 * @author youbl
 * @since 2023/11/9 20:31
 */
@RestController
public class CdnController {
    @Value("${cloud.ali-cdn.ak}")
    private String aliCdnAk;

    @Value("${cloud.ali-cdn.sk}")
    private String aliCdnSk;

    @Value("${cloud.ali-cdn.exe-path}")
    private String aliCdnExePath;

    @GetMapping(value = "/cdn/ali", produces = "text/plain")
    public String refreshAliCdn(CdnDto dto) {
        // cdn缓存刷新参考：https://help.aliyun.com/zh/cdn/developer-reference/api-cdn-2018-05-10-refreshobjectcaches
        //aliyun --access-key-id 111 --access-key-secret 222 --region ap-southeast-1 cdn RefreshObjectCaches --ObjectPath https://get.beinet.cn/prod/ --ObjectType Directory

        if (dto.getObjType().equals("Directory") && !dto.getObjUrl().endsWith("/")) {
            // 目录清理，必须以 / 结尾
            dto.setObjUrl(dto.getObjUrl() + "/");
        }
        String command = aliCdnExePath +
                " --access-key-id \"" + aliCdnAk + "\"" +
                " --access-key-secret \"" + aliCdnSk + "\"" +
                " --region \"" + dto.getRegion() + "\"" +
                " cdn RefreshObjectCaches" +
                " --ObjectPath \"" + dto.getObjUrl() + "\"" +
                " --ObjectType \"" + dto.getObjType() + "\"";
        return executeCommand(command);

    }

    @SneakyThrows
    private static String executeCommand(String exePath) {
        // 执行exe程序
        Process process = Runtime.getRuntime().exec(exePath);

        // 获取exe程序的输出流
        try (InputStream inputStream = process.getInputStream();
             BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream));
             InputStream errStream = process.getErrorStream();
             BufferedReader errReader = new BufferedReader(new InputStreamReader(errStream))) {
            // 读取输出结果
            String line;
            StringBuilder result = new StringBuilder();
            while ((line = reader.readLine()) != null) {
                result.append(line).append("\n");
            }

            StringBuilder errResult = new StringBuilder();
            while ((line = errReader.readLine()) != null) {
                errResult.append(line).append("\n");
            }

            // 等待exe程序执行完毕
            int exitCode = process.waitFor();

            String err = errResult.toString();
            if (StringUtils.hasLength(err)) {
                result.append("\n执行出错了:").append(err);
            }
            return "Exit code: " + exitCode + "\nOutput: " + result.toString();
        }
    }
}
