package cn.beinet.codegenerate.controller;

import cn.beinet.codegenerate.configs.AuthDetails;
import cn.beinet.codegenerate.configs.logins.ImgCodeService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * 新类
 *
 * @author youbl
 * @since 2023/8/15 14:11
 */
@RestController
@RequiredArgsConstructor
public class LoginController {
    private final ImgCodeService codeService;

    /**
     * 获取图形验证码
     *
     * @return
     */
    @GetMapping("login/imgcode")
    public ImgCodeService.ImgCodeDto getImgCode() {
        return codeService.getImgCode();
    }


    // 返回当前登录用户
    @GetMapping("loginuser")
    public String getLoginUser(AuthDetails authDetails) {
        return authDetails.getAccount();
    }
}
