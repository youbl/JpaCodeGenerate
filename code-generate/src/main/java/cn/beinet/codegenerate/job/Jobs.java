package cn.beinet.codegenerate.job;

import cn.beinet.codegenerate.job.backup.BackupService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

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
    private final BackupService backupService;
    private static boolean isOk;

    /**
     * 每小时跑一次，启动Backup接口实现类里的数据备份功能
     */
    @Scheduled(cron = "0 30 * * * *")
    //@Scheduled(cron = "* * * * * *")
    public void backupOperations() {
//        if (isOk)
//            return;
//        isOk = true;
        backupService.run();
    }
}
