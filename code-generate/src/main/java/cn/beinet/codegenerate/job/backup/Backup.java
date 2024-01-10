package cn.beinet.codegenerate.job.backup;

/**
 * 数据备份能力接口，
 * 具体备份哪些数据，由实现类完成
 *
 * @author : youbl
 * @create: 2022/4/8 10:57
 */
public interface Backup {
    /**
     * 是否允许启动
     *
     * @return true false
     */
    boolean enabled();

    /**
     * 备份操作
     */
    void operate();
}
