package cn.beinet.codegenerate.service;

import org.springframework.stereotype.Service;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.UUID;

/**
 * 生成随机盐值的服务类。
 * 注：如果多实例部署，则生成的盐值必须存入Redis或数据库，否则多实例会出现登录立即失效的问题
 *
 * @author youbl
 * @since 2024/6/26 17:51
 */
@Service
public class SaltService {
    private LocalDateTime saltTime = LocalDateTime.MIN;
    private String salt;

    /**
     * 每10天, 生成一个新的盐值
     *
     * @return 盐值
     */
    public String getSalt() {
        return getSalt(10);
    }

    /**
     * 每keepDays天, 生成一个新的盐值
     *
     * @param keepDays 盐值保留天数
     * @return 盐值
     */
    public String getSalt(int keepDays) {
        LocalDateTime now = LocalDateTime.now();
        if (salt == null || Duration.between(saltTime, now).toDays() > keepDays) {
            // 多实例部署，必须把生成的salt存入Redis或数据库，还要考虑乐观锁，避免冲突
            salt = UUID.randomUUID().toString();
            saltTime = now;
        }
        return salt;
    }
}
