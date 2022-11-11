package cn.beinet.codegenerate.job;

import cn.beinet.codegenerate.job.backup.Backup;
import cn.beinet.codegenerate.util.GitHelper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * Description:
 *
 * @author : youbl
 * @create: 2022/4/8 10:53
 */
@Component
@Slf4j
@RequiredArgsConstructor
@ConditionalOnProperty(value = "spring.profiles.active", havingValue = "prod", matchIfMissing = false)
public class Jobs {
    private final List<Backup> backupList;
    private final GitHelper gitHelper;
    private static boolean isOk;

    /**
     * 每小时跑一次，启动Backup接口实现类里的备份功能
     */
    @Scheduled(cron = "0 30 * * * *")
    //@Scheduled(cron = "* * * * * *")
    public void backupOperations() {
//        if (isOk)
//            return;
//        isOk = true;
        log.info("backupOperations 启动...");
        for (Backup item : backupList) {
            try {
                if (item.enabled()) {
                    item.operate();
                }
            } catch (Exception exp) {
                log.error("backupOperations error:{0} {1}", item.getClass().getName(), exp.getMessage());
            }
        }
        log.info("文件写入完成，开始操作git...");
        gitHelper.pull();
        gitHelper.commit("备份");
        log.info("backupOperations 结束.");
    }
}
