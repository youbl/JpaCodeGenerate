package cn.beinet.codegenerate.job.backup.impl;

import cn.beinet.codegenerate.job.backup.Backup;
import cn.beinet.codegenerate.job.backup.BackupConfigs;
import cn.beinet.codegenerate.util.FileHelper;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;

/**
 * Description:
 * 查询Redis实例的info 进行备份
 *
 * @author : youbl
 * @create: 2024/03/11 10:57
 */
@Service
@Slf4j
public class RedisInfoBackup implements Backup {
    private final BackupConfigs.Redis configs;

    public RedisInfoBackup(BackupConfigs configs) {
        this.configs = configs.getRedis();
    }

    @Override
    public boolean enabled() {
        if (configs == null)
            return false;
        return configs.getEnable() == null || configs.getEnable();
    }

    @Override
    public void operate() {
        if (configs.getInstances() == null || configs.getInstances().length <= 0)
            return;

        for (BackupConfigs.RedisInstance item : configs.getInstances()) {
            if (item.getEnable() != null && !item.getEnable())
                continue;

            if (item.getPort() == null) {
                item.setPort(6379);
            }
            log.info("准备备份实例 {}:{}", item.getIp(), item.getPort());

            try {
                String redisInfo = readInfo(item);
                saveToFile(item, redisInfo);
            } catch (Exception exp) {
                log.error("备份redis出错:{}.{} {}", item.getIp(), item.getPort(), exp.getMessage());
            }
        }
    }

    @SneakyThrows
    private String readInfo(BackupConfigs.RedisInstance item) {
        try (Socket socket = new Socket(item.getIp(), item.getPort());
             BufferedReader reader = new BufferedReader(new InputStreamReader(socket.getInputStream()));
             PrintWriter writer = new PrintWriter(socket.getOutputStream())) {

            // 读取服务器返回的欢迎信息，要注释，没有欢迎信息
//                String welcomeMessage = reader.readLine();
//                System.out.println("=================" + welcomeMessage);

            if (StringUtils.hasLength(item.getPassword())) {
                // 发送AUTH命令，进行身份验证
                writer.println("AUTH " + item.getPassword());
                writer.flush();

                // 读取身份验证结果
                String authResult = reader.readLine();
                if (authResult.startsWith("-")) {
                    // -ERR invalid password
                    // 正常是 +OK
                    throw new RuntimeException("redisPasswordError");
                }
            }

            // 发送INFO命令
            writer.println("INFO");
            writer.flush();

            // 读取服务器返回的信息
            StringBuilder sb = new StringBuilder();
            char[] buffer = new char[10240];
            int readCharLen;
            while ((readCharLen = reader.read(buffer)) != -1) {
                sb.append(buffer, 0, readCharLen);
                // 极端情况可能出现误判
                if (readCharLen < buffer.length && sb.toString().endsWith("\r\n"))
                    break;
            }
            return sb.toString();
        }
    }

    @SneakyThrows
    private void saveToFile(BackupConfigs.RedisInstance instance, String redisInfo) {
        String filePath = getFileName(instance);
        FileHelper.saveFile(filePath, redisInfo);
    }

    private String getFileName(BackupConfigs.RedisInstance instance) {
        String dir = configs.getBackDir();
        if (!dir.endsWith("/"))
            dir += "/";

        String url = instance.getIp() + "_" + instance.getPort();
        url = FileHelper.replaceInvalidCh(url);

        return dir + url + ".txt";
    }
}
