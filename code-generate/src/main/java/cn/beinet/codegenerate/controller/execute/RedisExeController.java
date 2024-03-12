package cn.beinet.codegenerate.controller.execute;

import cn.beinet.codegenerate.ResponseData;
import cn.beinet.codegenerate.configs.AuthDetails;
import cn.beinet.codegenerate.controller.dto.RedisDto;
import cn.beinet.codegenerate.linkinfo.service.LinkInfoService;
import cn.beinet.codegenerate.model.RedisResultDto;
import cn.beinet.codegenerate.repository.RedisRepository;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import org.springframework.http.HttpHeaders;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.HttpServletResponse;

@RestController
@RequiredArgsConstructor
public class RedisExeController {
    private final LinkInfoService linkInfoService;

    /**
     * 查询指定key的值（仅查询key，不支持其它命令
     *
     * @param cmd 命令信息
     * @return 值
     */
    @PostMapping("v1/redis/executeCmd")
    public ResponseData GetMySqlDbs(@RequestBody RedisDto cmd) {
        RedisRepository repository = linkInfoService.getRedisRepository(cmd);
        RedisResultDto result = repository.get(cmd.getCmd());
        return ResponseData.ok(cmd.getCmd(), result);
    }

    /**
     * scan所有redis的key，并返回
     *
     * @param response 响应上下文
     */
    @GetMapping(value = "v1/redis/allkeys", produces = "application/unknown")
    @SneakyThrows
    public void GetAllKeys(@RequestParam String config, @RequestParam int db,
                           HttpServletResponse response,
                           AuthDetails loginInfo) {
        if (loginInfo == null || !loginInfo.isAdmin()) {
            throw new RuntimeException("不允许操作");
        }

        RedisDto dto = new RedisDto().setName(config).setDb(db);
        RedisRepository repository = linkInfoService.getRedisRepository(dto);
        // response.addHeader(HttpHeaders.CONTENT_TYPE, "application/unknown");
        response.addHeader(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=keys-" + db + ".txt");
        repository.saveAllKeys(response.getOutputStream());
    }
}
