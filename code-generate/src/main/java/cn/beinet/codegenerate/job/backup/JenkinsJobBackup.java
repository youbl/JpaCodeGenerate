package cn.beinet.codegenerate.job.backup;

import cn.beinet.codegenerate.job.backup.services.JenkinsService;
import cn.beinet.codegenerate.job.backup.services.dto.JenkinsJob;
import cn.beinet.codegenerate.job.config.BackupConfigs;
import cn.beinet.codegenerate.util.FileHelper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.util.List;

/**
 * Description:
 * 遍历指定的Jenkins的Job，把所有Job配置进行备份
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
        if (configs == null)
            return false;
        if (configs.getEnable() != null && !configs.getEnable())
            return false;
        return true;
    }

    @Override
    public void operate() {
        if (configs.getSites() == null || configs.getSites().length <= 0)
            return;
        for (BackupConfigs.JenkinsSite item : configs.getSites()) {
            if (item.getEnable() != null && !item.getEnable())
                continue;

            if (!StringUtils.hasLength(item.getUsername()) || !StringUtils.hasLength(item.getPassword())) {
                log.error("{} 用户名密码为空，无法备份", item.getUrl());
                continue;
            }
            log.info("准备备份url {}", item.getUrl());

            try {
                JenkinsService jenkinsService = new JenkinsService(item.getUrl(), item.getUsername(), item.getPassword());
                List<JenkinsJob> jobNames = jenkinsService.getAllJobName();
                for (JenkinsJob job : jobNames) {
                    backupJob(job, jenkinsService);
                }
            } catch (Exception namespaceEx) {
                log.error("备份出错:{} {}", item.getUrl(), namespaceEx.getMessage());
            }
        }
    }

    private void backupJob(JenkinsJob job, JenkinsService jenkinsService) {
        try {
            String xml = jenkinsService.getJobConfig(job);
            String filePath = getFileName(job.getUrl());
            FileHelper.saveFile(filePath, xml);
            if (job.getSubJobs() != null) {
                for (JenkinsJob subJob : job.getSubJobs()) {
                    backupJob(subJob, jenkinsService);
                }
            }
        } catch (Exception exp) {
            log.error("备份jenkins出错:{} {}", job.getName(), exp.getMessage());
        }

    }

    private String getFileName(String jobName) {
        String dir = configs.getBackDir();
        if (!dir.endsWith("/"))
            dir += "/";

        int idx = jobName.indexOf("/", "https://".length() + 1); // 查找http协议之后的第一个斜杠
        if (idx > 0) {
            jobName = jobName.substring(idx + 1);
        }
        if (jobName.startsWith("job/")) {
            jobName = jobName.substring(4);
        }
        if (jobName.endsWith("/")) {
            jobName = jobName.substring(0, jobName.length() - 1);
        }

        jobName = FileHelper.replaceInvalidCh(jobName);

        return dir + jobName + ".xml";
    }
}
