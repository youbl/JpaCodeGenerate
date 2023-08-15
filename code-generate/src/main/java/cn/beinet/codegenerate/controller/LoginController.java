package cn.beinet.codegenerate.controller;

import cn.beinet.codegenerate.configs.AuthDetails;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * 新类
 *
 * @author youbl
 * @since 2023/8/15 14:11
 */
@RestController
public class LoginController {
    // 返回当前登录用户
    @GetMapping("loginuser")
    public String getLoginUser(AuthDetails authDetails) {
        return authDetails.getAccount();
    }
}
