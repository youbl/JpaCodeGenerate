package cn.beinet.codegenerate.codeGenerate;

import cn.beinet.codegenerate.codeGenerate.service.CodeDbService;
import cn.beinet.codegenerate.model.ColumnDto;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * 代码生成所需的数据库支持
 *
 * @author youbl
 * @date 2023/4/19 10:46
 */
@RestController
@RequestMapping("codeDb")
@RequiredArgsConstructor
public class CodeDbController {
    private final CodeDbService codeDbService;


    /**
     * 获取数据库列表
     *
     * @param ip   数据库IP，可空，使用系统连接串
     * @param port 数据库端口
     * @param user 数据库账号
     * @param pwd  数据库密码
     * @return 数据库列表
     */
    @GetMapping("databases")
    public List<String> getDatabases(@RequestParam(required = false) String ip,
                                     @RequestParam(required = false) Integer port,
                                     @RequestParam(required = false) String user,
                                     @RequestParam(required = false) String pwd) {
        if (port == null)
            port = 3306;
        return codeDbService.getDatabases(ip, port, user, pwd);
    }

    /**
     * 获取指定数据库的表清单
     *
     * @param ip       数据库IP，可空，使用系统连接串
     * @param port     数据库端口
     * @param user     数据库账号
     * @param pwd      数据库密码
     * @param database 库名
     * @return 表清单
     */
    @GetMapping("tables")
    public List<String> getTables(@RequestParam(required = false) String ip,
                                  @RequestParam(required = false) Integer port,
                                  @RequestParam(required = false) String user,
                                  @RequestParam(required = false) String pwd,
                                  @RequestParam String database) {
        if (port == null)
            port = 3306;
        return codeDbService.getTables(ip, port, user, pwd, database);
    }

    /**
     * 获取指定表的字段清单
     *
     * @param ip       数据库IP，可空，使用系统连接串
     * @param port     数据库端口
     * @param user     数据库账号
     * @param pwd      数据库密码
     * @param database 库名
     * @param tables   多个表半角逗号分隔
     * @return 列清单
     */
    @GetMapping("columns")
    public List<ColumnDto> getColumns(@RequestParam(required = false) String ip,
                                      @RequestParam(required = false) Integer port,
                                      @RequestParam(required = false) String user,
                                      @RequestParam(required = false) String pwd,
                                      @RequestParam String database,
                                      @RequestParam String tables) {
        if (port == null)
            port = 3306;
        String[] arrTables = tables.split(",");
        return codeDbService.getFields(ip, port, user, pwd, database, arrTables);
    }
}
