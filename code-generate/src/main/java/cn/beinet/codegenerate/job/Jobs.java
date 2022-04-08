package cn.beinet.codegenerate.job;

import cn.beinet.codegenerate.job.service.Backup;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
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
public class Jobs {
    private final List<Backup> backupList;
//    private static boolean isOk;

    /**
     * 每小时跑一次，备份nacos配置
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
                item.operate();
            } catch (Exception exp) {
                log.error("backupOperations error:{0} {1}", item.getClass().getName(), exp.getMessage());
            }
        }
        log.info("backupOperations 结束.");
    }
}
