package cn.beinet.codegenerate.job.backup;

import cn.beinet.codegenerate.job.backup.services.JenkinsService;
import cn.beinet.codegenerate.job.config.BackupConfigs;
import cn.beinet.codegenerate.util.FileHelper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.util.List;

/**
 * Description:
 * 遍历Jenkins所有Job配置并备份到本地文件
 *
 * @author : youbl
 * @create: 2022/11/11 10:57
 */
@Service
@Slf4j
public class JenkinsJobBackup implements Backup {
    private final BackupConfigs.Jenkins configs;

    public JenkinsJobBackup(BackupConfigs configs) {
        this.configs = configs.getJenkins();
    }

    @Override
    public boolean enabled() {
        return true;
    }

    @Override
    public void operate() {
        for (BackupConfigs.JenkinsSite item : configs.getSites()) {
            if (!StringUtils.hasLength(item.getUsername()) || !StringUtils.hasLength(item.getPassword())) {
                log.error("{} 用户名密码为空，无法备份", item.getUrl());
                continue;
            }
            log.info("准备备份url {}", item.getUrl());

            try {
                JenkinsService jenkinsService = new JenkinsService(item.getUrl(), item.getUsername(), item.getPassword());
                List<String> jobNames = jenkinsService.getAllJobName();
                for (String job : jobNames) {
                    String xml = jenkinsService.getJobConfig(job);
                    backupJob(job, xml);
                }
            } catch (Exception namespaceEx) {
                log.error("备份出错:{} {}", item.getUrl(), namespaceEx.getMessage());
            }
        }
    }

    private void backupJob(String jobName, String xml) {
        try {
            String filePath = getFileName(jobName);
            FileHelper.saveFile(filePath, xml);
        } catch (Exception exp) {
            log.error("备份jenkins出错:{} {}", jobName, exp.getMessage());
        }

    }

    private String getFileName(String jobName) {
        String dir = configs.getBackDir();
        if (!dir.endsWith("/"))
            dir += "/";

        jobName = FileHelper.replaceInvalidCh(jobName);

        return dir + jobName + ".xml";
    }
}
