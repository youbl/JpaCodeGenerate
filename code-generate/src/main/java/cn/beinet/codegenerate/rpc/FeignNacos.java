package cn.beinet.codegenerate.rpc;

import cn.beinet.codegenerate.rpc.dto.NacosFiles;
import cn.beinet.codegenerate.rpc.dto.NacosNameSpaces;
import cn.beinet.codegenerate.rpc.dto.NacosToken;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.net.URI;

/**
 * 随便写一个url，实际上是外部传入传入uri + PostMapping里的后缀。
 * nacos api文档参考 https://nacos.io/zh-cn/docs/open-api.html
 */
@FeignClient(value = "nacos", url = "http://beinet.cn")
public interface FeignNacos {

    /**
     * 登录接口
     * POST http://beinet.cn/nacos/v1/auth/users/login?password=123456&username=bbb
     *
     * @param uri      uri
     * @param username 登录用户名
     * @param password 登录密码
     * @return 结果
     */
    @PostMapping("v1/auth/users/login")
    NacosToken login(URI uri, @RequestParam String username, @RequestParam String password);

    /**
     * 获取命名空间列表，无需登录
     *
     * @param uri uri
     * @return 命名空间列表
     */
    @GetMapping("v1/console/namespaces")
    NacosNameSpaces getNamespaces(URI uri);

    /**
     * 读取指定命名空间下的所有配置。用于读取文件列表
     * GET http://beinet.cn/nacos/v1/cs/configs?dataId=&group=&appName=&config_tags=&pageNo=1&pageSize=10&tenant=ns-rpa-scheduler&search=accurate
     *
     * @param uri         uri
     * @param tenant      命名空间
     * @param accessToken 登录信息
     * @return 配置
     */
    @GetMapping("v1/cs/configs?dataId=&group=&appName=&config_tags=&pageNo=1&pageSize=100&search=accurate")
    NacosFiles getConfigFiles(URI uri,
                              @RequestParam String tenant,
                              @RequestParam String accessToken);

    /**
     * 读取配置
     * GET http://beinet.cn/nacos/v1/cs/configs?dataId=配置文件名&tenant=命名空间&group=DEFAULT_GROUP&accessToken=上面的accessToken
     *
     * @param uri         uri
     * @param dataId      配置文件名
     * @param tenant      命名空间
     * @param group       分组
     * @param accessToken 登录信息
     * @return 配置
     */
    @GetMapping("v1/cs/configs")
    String getConfig(URI uri,
                     @RequestParam String dataId,
                     @RequestParam String tenant,
                     @RequestParam String group,
                     @RequestParam String accessToken);
}
