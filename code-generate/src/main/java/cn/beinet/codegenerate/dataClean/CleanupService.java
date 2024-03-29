package cn.beinet.codegenerate.dataClean;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * 完成所有数据清理操作的服务类
 *
 * @author youbl
 * @since 2024/1/10 14:09
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class CleanupService {
    @Value("${data-clean.enabled:false}")
    private boolean enabled;

    // 清理实现类
    private final List<Cleanup> cleanupList;

    public void run() {
        if (!enabled)
            return;

        log.info("CleanupService 启动...");
        for (Cleanup item : cleanupList) {
            try {
                if (item.enabled()) {
                    item.clean();
                }
            } catch (Exception exp) {
                log.error("CleanupService error:{0} {1}", item.getClass().getName(), exp.getMessage());
            }
        }
        log.info("CleanupService 结束.");
    }
}
