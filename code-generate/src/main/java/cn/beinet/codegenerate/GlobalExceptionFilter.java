package cn.beinet.codegenerate;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

/**
 * Description:
 *
 * @author : youbl
 * @create: 2021/11/23 14:41
 */
@RestControllerAdvice
@Slf4j
public class GlobalExceptionFilter {
    @ExceptionHandler(value = Exception.class)
    public ResponseData exceptionHandler(Exception e) {
        //System.out.println("未知异常！原因是:" + e);
        log.error("全局异常:", e);
        String ret = e.getMessage();
        if (e.getCause() != null)
            ret = e.getCause().getMessage();
        return ResponseData.fail(ret);
    }
}
