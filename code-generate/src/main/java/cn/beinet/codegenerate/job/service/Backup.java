package cn.beinet.codegenerate.job.service;

/**
 * Description:
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
