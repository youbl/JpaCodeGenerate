package cn.beinet.codegenerate.configs.logins;

import cn.beinet.codegenerate.consts.Consts;
import cn.beinet.codegenerate.service.SaltService;
import cn.beinet.codegenerate.util.RequestHelper;
import cn.beinet.codegenerate.util.TokenHelper;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

/**
 * 判断Cookie是否存在有效登录信息的类
 *
 * @author youbl
 * @date 2023/1/4 18:18
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class CookieValidator implements Validator {
    // 登录token有效时长, 超过要重新登录
    @Value("${login.keepSecond:" + Consts.LOGIN_KEEP_SECOND + "}")
    private long loginSecond;

    // 用于获取token的md5加密随机盐值，确保安全性
    private final SaltService saltService;

    /**
     * 正常优先级
     *
     * @return 排序
     */
    @Override
    public int getOrder() {
        return 0;
    }

    @Override
    public Result validated(HttpServletRequest request, HttpServletResponse response) {
        String cookName = TokenHelper.getTokenCookieName();
        // 判断有没有cookie，有cookie时是否有效
        String token = RequestHelper.getCookie(cookName, request);
        log.debug("url:{} token: {}", request.getRequestURI(), token);
        TokenHelper.Token loginUser = TokenHelper.getLoginUserFromToken(token, saltService.getSalt());
        if (!isSuccess(loginUser)) {
            log.debug("token无效: {}", token);
            return Result.fail();
        }
        return Result.ok(loginUser.getUsername());
    }

    private boolean isSuccess(TokenHelper.Token loginUser) {
        if (loginUser == null) {
            return false;
        }
        return loginUser.getSeconds() < loginSecond;
    }
}
