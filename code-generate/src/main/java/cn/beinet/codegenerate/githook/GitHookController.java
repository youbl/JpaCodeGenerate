package cn.beinet.codegenerate.githook;

import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.HttpServletRequest;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;

/**
 * 新类
 *
 * @author youbl
 * @since 2023/5/29 17:23
 */
@RestController
@Slf4j
@RequiredArgsConstructor
public class GitHookController {
    private final GitlogService gitlogService;

    @PostMapping("githook")
    @SneakyThrows
    public int hook(HttpServletRequest request) {
        String body;
        try (InputStream stream = request.getInputStream()) {
            body = readFromStream(stream);
        }
        return gitlogService.add(body);
    }

    private static String readFromStream(InputStream stream) throws IOException {
        ByteArrayOutputStream result = new ByteArrayOutputStream();
        byte[] buffer = new byte[1024];
        int length;
        while ((length = stream.read(buffer)) != -1) {
            result.write(buffer, 0, length);
        }
        return result.toString(StandardCharsets.UTF_8.name());
        // return new BufferedReader(new InputStreamReader(stream)).lines().collect(Collectors.joining(System.lineSeparator()));
    }
}

/**
 * 1、进入gitlab的项目配置Settings=>Webhooks，添加配置：
 * URL: http://部署服务器IP/githook
 * Trigger选择：Push Events, Tag Push Events, Merge Requests Events
 * 去掉勾选 Enable SSL Verification
 * 如果项目需要token认证，则要在Secret token里输入本项目所需的认证token
 * 注：gitlab是通过在HTTP Header里添加 X-Gitlab-Token来传递这个输入的token，需要在代码里处理
 * <p>
 * 对应的数据，通过这个接口写入表中：
 * CREATE TABLE `gitlog` (
 * `id` INT(10) UNSIGNED NOT NULL AUTO_INCREMENT,
 * `postdata` JSON NOT NULL COMMENT 'git回调时的body数据',
 * `status` TINYINT(4) NOT NULL COMMENT '0未处理，1已处理',
 * `creation` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
 * `username` VARCHAR(20) GENERATED ALWAYS AS (TRIM(BOTH '"' FROM JSON_EXTRACT(`postdata`,'$.user_name'))) VIRTUAL,
 * `branch` VARCHAR(50) GENERATED ALWAYS AS (REPLACE(TRIM(BOTH '"' FROM JSON_EXTRACT(`postdata`,'$.ref')),'refs/heads/','')) VIRTUAL,
 * `project` VARCHAR(20) GENERATED ALWAYS AS (TRIM(BOTH '"' FROM JSON_EXTRACT(`postdata`,'$.repository.name'))) VIRTUAL,
 * PRIMARY KEY (`id`),
 * KEY `idx_status` (`status`)
 * ) ENGINE=INNODB DEFAULT CHARSET=utf8mb4 COMMENT='gitlab hook回调记录';
 */