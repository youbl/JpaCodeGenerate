package cn.beinet.codegenerate.diff;

import cn.beinet.codegenerate.service.NacosService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Map;

@RestController
public class NacosController {
    private final NacosService nacosService;

    public NacosController(NacosService nacosService) {
        this.nacosService = nacosService;
    }


    // 获取命名空间列表
    @GetMapping("nacos/namespaces")
    public List<String> getNamespaces(@RequestParam String url) {
        return nacosService.getNamespaces(url);
    }


    // 获取指定命名空间的文件列表
    @GetMapping("nacos/files")
    public List<String> getFiles(@RequestParam String url,
                                 @RequestParam String user,
                                 @RequestParam String pwd,
                                 @RequestParam String nameSpace) {
        return nacosService.getFiles(url, user, pwd, nameSpace);
    }


    // 获取指定文件配置
    @GetMapping("nacos/concfig")
    public Map<String, Object> getFiles(@RequestParam String url,
                                        @RequestParam String user,
                                        @RequestParam String pwd,
                                        @RequestParam String nameSpace,
                                        @RequestParam String dataId) {
        String ymlStr = nacosService.getFile(url, user, pwd, nameSpace, dataId, "DEFAULT_GROUP");
        return nacosService.parseYml(ymlStr);
    }
}
