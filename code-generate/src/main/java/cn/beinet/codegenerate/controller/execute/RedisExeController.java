package cn.beinet.codegenerate.controller.execute;

import cn.beinet.codegenerate.ResponseData;
import cn.beinet.codegenerate.configs.arguments.AuthDetails;
import cn.beinet.codegenerate.controller.dto.RedisDto;
import cn.beinet.codegenerate.linkinfo.service.LinkInfoService;
import cn.beinet.codegenerate.model.RedisResultDto;
import cn.beinet.codegenerate.repository.RedisRepository;
import cn.beinet.codegenerate.util.ContextUtil;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import org.springframework.http.HttpHeaders;
import org.springframework.util.Assert;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.io.BufferedWriter;
import java.io.OutputStreamWriter;
import java.util.ArrayList;
import java.util.List;

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

    @DeleteMapping("v1/redis/removeKey")
    public int removeKey(@RequestBody RedisDto cmd,
                         AuthDetails loginInfo) {
        if (!ContextUtil.isAdmin()) {
            throw new RuntimeException("仅管理员允许操作");
        }

        Assert.isTrue(StringUtils.hasLength(cmd.getCmd()), "key不能为空");

        RedisRepository repository = linkInfoService.getRedisRepository(cmd);
        Boolean ret = repository.removeKey(cmd.getCmd());
        if (ret == null || !ret)
            return 0;
        return 1;
    }

    /**
     * scan所有redis的key，并返回
     *
     * @param response 响应上下文
     */
    /**
     * scan所有redis的key，并返回
     *
     * @param config    使用哪个redis配置
     * @param db        查询哪个db
     * @param ttl       是否查询ttl，默认不查询，为1时要查询
     * @param response  响应上下文
     */
    @GetMapping(value = "v1/redis/allkeys", produces = "application/unknown")
    @SneakyThrows
    public void GetAllKeys(@RequestParam String config,
                           @RequestParam int db,
                           @RequestParam(required = false) Integer ttl,
                           HttpServletResponse response) {
        if (!ContextUtil.isAdmin()) {
            throw new RuntimeException("仅管理员允许操作");
        }

        RedisDto dto = new RedisDto().setName(config).setDb(db);
        RedisRepository repository = linkInfoService.getRedisRepository(dto);
        // response.addHeader(HttpHeaders.CONTENT_TYPE, "application/unknown");
        response.addHeader(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=keys-" + db + ".txt");

        boolean queryTTL = ttl != null && ttl == 1;
        if (!queryTTL) {
            repository.saveAllKeys(response.getOutputStream());
            return;
        }

        List<String> keys = getAllKeys(repository);
        try (BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(response.getOutputStream()))) {
            int idx = 0;
            for (String key : keys) {
                idx++;
                if (idx % 1000 == 0) {
                    writer.flush();
                    Thread.sleep(5);
                }
                writer.write(key);
                writer.write(",");
                writer.write(repository.getTTL(key).toString());
                writer.write("\r\n");
            }
            writer.flush();
        }
    }

    private List<String> getAllKeys(RedisRepository repository) {
        List<String> ret = new ArrayList<>();
        repository.getAllKeys("*", key -> ret.add(key.toString()));
        return ret;
    }
}
