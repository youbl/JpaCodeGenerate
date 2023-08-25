package cn.beinet.codegenerate.requestLogs;

import cn.beinet.codegenerate.configs.AuthDetails;
import lombok.RequiredArgsConstructor;
import org.springframework.util.Assert;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * 新类
 *
 * @author youbl
 * @since 2023/8/25 23:54
 */
@RestController
@RequiredArgsConstructor
public class LogController {
    private final RequestLogService logService;

    @GetMapping("logs")
    public List<RequestLog> getLogs(AuthDetails loginInfo) {
        Assert.isTrue(loginInfo != null, "不允许访问");
        String user;
        if ("beiliang_you".equals(loginInfo.getAccount())) {
            user = "";
        } else {
            user = loginInfo.getAccount();
        }
        return logService.getNewLogs(user);
    }
}
