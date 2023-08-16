package cn.beinet.codegenerate.controller.execute;

import cn.beinet.codegenerate.GlobalExceptionFilter;
import cn.beinet.codegenerate.controller.dto.RedisDto;
import cn.beinet.codegenerate.model.RedisResultDto;
import cn.beinet.codegenerate.repository.RedisRepository;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class RedisExeController {

    @PostMapping("v1/redis/executeCmd")
    public GlobalExceptionFilter.ResponseData GetMySqlDbs(@RequestParam String ip,
                                                          @RequestParam int port,
                                                          @RequestParam String pwd,
                                                          @RequestParam int db,
                                                          @RequestBody RedisDto cmd) {
        RedisRepository repository = new RedisRepository(ip, port, db, pwd);
        RedisResultDto result = repository.get(cmd.getCmd());
        return GlobalExceptionFilter.ResponseData.ok(cmd.getCmd(), result);
    }
}
