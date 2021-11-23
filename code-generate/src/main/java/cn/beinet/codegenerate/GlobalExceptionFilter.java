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
        return ResponseData.fail(e.getMessage());
    }


    @Data
    @AllArgsConstructor
    public static class ResponseData {

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
        private Object result;

        public static ResponseData fail(String msg) {
            return new ResponseData(500, msg, null);
        }

        public static ResponseData fail(int code, String msg) {
            return new ResponseData(code, msg, null);
        }

        public static ResponseData ok(Object obj) {
            return new ResponseData(200, "", obj);
        }

        public static ResponseData ok(String msg, Object obj) {
            return new ResponseData(200, msg, obj);
        }
    }

}
