package cn.beinet.codegenerate.controller;

import cn.beinet.codegenerate.configs.AuthDetails;
import cn.beinet.codegenerate.util.ProcessHelper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

/**
 * 接收文件上传，和服务重启。
 * 用于部署在某台单机上的服务发布
 *
 * @author youbl
 * @since 2023/1/4 10:48
 */
@Slf4j
//@RestController
public class PublishController {
    private final Map<String, String> serviceDir;

    public PublishController() {
        // key为服务名，value为对应文件路径
        serviceDir = new HashMap<>();
        serviceDir.put("rpaopsTest", "/data/app/jars/test-scheduler-ops-0.0.1-SNAPSHOT.jar");
        serviceDir.put("rpaopsPrev", "/data/app/jars/prev-scheduler-ops-0.0.1-SNAPSHOT.jar");
        serviceDir.put("rpaops", "/data/app/jars/prod-scheduler-ops-0.0.1-SNAPSHOT.jar");

        serviceDir.put("testdebugTool", "/data/app/jars_debugTool/testdebugTool.jar");
        serviceDir.put("debugTool", "/data/app/jars_debugTool/debugTool.jar");

        serviceDir.put("sqlCompare", "/data/app/jars_sqlCompare/code-generate-0.0.1-SNAPSHOT.jar");
    }

    // 上传文件
    @PostMapping("publish/upload/{serviceName}")
    public String upload(@RequestParam("file") MultipartFile file,
                         @PathVariable String serviceName,
                         AuthDetails loginInfo) throws IOException {
        if (file == null || file.isEmpty()) {
            return "文件内容为空";
        }
        String savePath = serviceDir.get(serviceName);
        if (savePath == null) {
            String errmsg = "不支持的服务:" + serviceName;
            log.error(errmsg);
            return errmsg;
        }
        file.transferTo(new File(savePath));
        log.info("{} 上传文件 {} 存入 {}", loginInfo.getAccount(), serviceName, savePath);
        return "OK:" + savePath;
    }


    // 重启指定服务
    @PostMapping("publish/{serviceName}")
    public String publish(@PathVariable String serviceName, AuthDetails loginInfo) {
        String command = "/bin/supervisorctl restart '" + serviceName + "'";
        log.info("{} 执行命令 {} ", loginInfo.getAccount(), command);
        // String result = ProcessHelper.run("sh", "-c", command);
        String result = ProcessHelper.run(command);//("/bin/supervisorctl", "restart", serviceName);
        log.info("{} 执行命令 {} 结果 {}", loginInfo.getAccount(), command, result);
        return result;
    }
}
