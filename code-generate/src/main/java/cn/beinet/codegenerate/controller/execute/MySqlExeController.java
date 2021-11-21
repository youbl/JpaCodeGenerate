package cn.beinet.codegenerate.controller.execute;

import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Map;

@RestController
public class MySqlExeController {

    @PostMapping("mysql/executeSql")
    public List<Map<String, Object>> GetMySqlDbs(@RequestParam String ip,
                                                 @RequestParam String user,
                                                 @RequestParam String pwd,
                                                 @RequestParam String db,
                                                 @RequestBody String sql) {

        return null;
    }
}
