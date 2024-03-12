package cn.beinet.codegenerate.requestLogs;

import cn.beinet.codegenerate.configs.AuthDetails;
import cn.beinet.codegenerate.requestLogs.dto.SearchLogDto;
import lombok.RequiredArgsConstructor;
import org.springframework.util.Assert;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.ArrayList;
import java.util.List;

/**
 * 日志查询
 *
 * @author youbl
 * @since 2023/8/25 23:54
 */
@RestController
@RequiredArgsConstructor
public class LogController {
    private final RequestLogService logService;

    @GetMapping("logs")
    public List<RequestLog> getLogs(SearchLogDto dto, AuthDetails loginInfo) {
        Assert.isTrue(loginInfo != null, "不允许访问");
        if (!loginInfo.isAdmin()) {
            // 非管理员，只能查自己的日志
            dto.setLoginUser(loginInfo.getAccount());
        }
        return logService.getNewLogs(dto);
    }

    /**
     * 给前端用于过滤的人员列表和url列表
     *
     * @param loginInfo 登录信息
     * @return 条件
     */
    @GetMapping("logs/conditions")
    public List<List<String>> getConditions(AuthDetails loginInfo) {
        List<List<String>> ret = new ArrayList<>(2);
        if (!loginInfo.isAdmin()) {
            // 不是管理员，不让条件过滤
            return ret;
        }
        ret.add(logService.getDistinctField("loginUser"));
        ret.add(logService.getDistinctField("url"));
        return ret;
    }
}
