package cn.beinet.codegenerate.controller.execute;

import cn.beinet.codegenerate.GlobalExceptionFilter;
import cn.beinet.codegenerate.controller.dto.RedisDto;
import cn.beinet.codegenerate.linkinfo.service.LinkInfoService;
import cn.beinet.codegenerate.model.RedisResultDto;
import cn.beinet.codegenerate.repository.RedisRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
public class RedisExeController {
    private final LinkInfoService linkInfoService;

    @PostMapping("v1/redis/executeCmd")
    public GlobalExceptionFilter.ResponseData GetMySqlDbs(@RequestBody RedisDto cmd) {
        RedisRepository repository = linkInfoService.getRedisRepository(cmd);
        RedisResultDto result = repository.get(cmd.getCmd());
        return GlobalExceptionFilter.ResponseData.ok(cmd.getCmd(), result);
    }
}
