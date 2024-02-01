package cn.beinet.codegenerate;

import lombok.AllArgsConstructor;
import lombok.Data;

/**
 * 新类
 *
 * @author youbl
 * @since 2024/2/1 13:42
 */
@Data
@AllArgsConstructor
public class ResponseData<T> {

    /**
     * 编码：200成功，其它失败
     */
    private int code;

    /**
     * 异常堆栈
     */
    private String errMsg;

    /**
     * 源数据
     */
    private T result;

    /**
     * 当前时间戳
     */
    private long ts;

    public static <T> ResponseData<T> fail(String msg) {
        return ResponseData.fail(500, msg);
    }

    public static <T> ResponseData<T> fail(int code, String msg) {
        return new ResponseData<>(code, msg, null, System.currentTimeMillis());
    }

    public static <T> ResponseData<T> ok(T obj) {
        return ResponseData.ok("", obj);
    }

    public static <T> ResponseData<T> ok(String msg, T obj) {
        return new ResponseData<>(200, msg, obj, System.currentTimeMillis());
    }
}

