package cn.beinet.codegenerate.configs.logins;

import lombok.Data;
import lombok.experimental.Accessors;
import org.springframework.core.Ordered;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * 新类
 *
 * @author youbl
 * @since 2023/1/4 18:17
 */
public interface Validator  extends Ordered {

    /**
     * 身份验证方法，返回验证结果
     *
     * @param request  请求上下文
     * @param response 响应上下文
     * @return 验证结果
     */
    Result validated(HttpServletRequest request, HttpServletResponse response);

    @Data
    @Accessors(chain = true)
    public static class Result {
        /**
         * 验证通过与否
         */
        private boolean passed;
        /**
         * 验证通过时的账号
         */
        private String account;

        public static Result fail() {
            return new Result().setPassed(false).setAccount("匿名");
        }

        public static Result ok(String account) {
            return new Result().setPassed(true).setAccount(account);
        }
    }
}
