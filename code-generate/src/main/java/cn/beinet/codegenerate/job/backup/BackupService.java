package cn.beinet.codegenerate.job.backup;

import cn.beinet.codegenerate.util.GitHelper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * 完成所有备份操作的服务类
 *
 * @author youbl
 * @since 2024/1/10 14:09
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class BackupService {
    private final List<Backup> backupList;
    private final GitHelper gitHelper;

    public void run() {
        if (!gitHelper.enabled())
            return;
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
