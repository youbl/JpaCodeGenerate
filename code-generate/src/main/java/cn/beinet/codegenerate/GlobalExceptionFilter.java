package cn.beinet.codegenerate;

import lombok.AllArgsConstructor;
import lombok.Data;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

/**
 * Description:
 *
 * @author : youbl
 * @create: 2021/11/23 14:41
 */
@RestControllerAdvice
public class GlobalExceptionFilter {
    @ExceptionHandler(value = Exception.class)
    public ResponseData exceptionHandler(Exception e) {
        //System.out.println("未知异常！原因是:" + e);
        return new ResponseData(false, e.getMessage());
    }


    @Data
    @AllArgsConstructor
    public static class ResponseData {

        /**
         * 统一成功编码定义
         */
        public static final String RESPONSE_SUCCESS_CODE = "0";

        /**
         * 成功标识：true，业务请求成功，false：业务请求失败
         */
        private boolean success;

        /**
         * 异常堆栈
         */
        private String stackTrace;

    }

}
