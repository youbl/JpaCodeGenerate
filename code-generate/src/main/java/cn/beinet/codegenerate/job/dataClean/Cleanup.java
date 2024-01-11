package cn.beinet.codegenerate.job.dataClean;

/**
 * 数据清理能力接口，
 * 具体清理哪些数据，由实现类完成
 *
 * @author youbl
 * @since 2024/1/10 20:39
 */
public interface Cleanup {
    /**
     * 是否允许启动
     *
     * @return true false
     */
    boolean enabled();

    /**
     * 清理操作
     */
    void clean();
}
