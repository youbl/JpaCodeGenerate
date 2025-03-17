package cn.beinet.codegenerate.service;

import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

/**
 * 生成随机盐值的服务类。
 * 注：如果多实例部署，则生成的盐值必须存入Redis或数据库，否则多实例会出现登录立即失效的问题
 *
 * @author youbl
 * @since 2024/6/26 17:51
 */
@Service
public class SaltService {
    /**
     * 每个月, 生成一个新的盐值
     *
     * @return 盐值
     */
    public String getSalt() {
        LocalDateTime now = LocalDateTime.now();
        long ret = now.getYear() * 10000L + now.getMonthValue() * 100 + 1;
        return String.valueOf(ret);
        // 下面代码重启会导致要重新登录
//        if (salt == null || Duration.between(saltTime, now).toDays() > keepDays) {
//            // 多实例部署，必须把生成的salt存入Redis或数据库，还要考虑乐观锁，避免冲突
//            salt = UUID.randomUUID().toString();
//            saltTime = now;
//        }
//        return salt;
    }
}
