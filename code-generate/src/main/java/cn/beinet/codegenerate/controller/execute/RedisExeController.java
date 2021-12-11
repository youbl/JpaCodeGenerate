package cn.beinet.codegenerate.controller.execute;

import cn.beinet.codegenerate.GlobalExceptionFilter;
import cn.beinet.codegenerate.controller.execute.dto.RedisDto;
import cn.beinet.codegenerate.controller.execute.dto.SqlDto;
import cn.beinet.codegenerate.repository.MySqlExecuteRepository;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Map;
import java.util.regex.Pattern;

@RestController
public class RedisExeController {

    @PostMapping("redis/executeCmd")
    public GlobalExceptionFilter.ResponseData GetMySqlDbs(@RequestParam String ip,
                                                          @RequestParam String pwd,
                                                          @RequestParam int db,
                                                          @RequestBody RedisDto cmd) {
        return GlobalExceptionFilter.ResponseData.ok(cmd.getCmd(), cmd.getCmd() + "的结果");
    }
}
