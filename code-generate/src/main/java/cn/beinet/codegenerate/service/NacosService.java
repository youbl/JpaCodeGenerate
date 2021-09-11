package cn.beinet.codegenerate.service;

import cn.beinet.codegenerate.rpc.FeignNacos;
import cn.beinet.codegenerate.rpc.dto.NacosFiles;
import cn.beinet.codegenerate.rpc.dto.NacosNameSpaces;
import cn.beinet.codegenerate.rpc.dto.NacosToken;
import org.springframework.beans.factory.config.YamlMapFactoryBean;
import org.springframework.beans.factory.config.YamlPropertiesFactoryBean;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.InputStreamResource;
import org.springframework.stereotype.Service;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.net.URI;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.stream.Collectors;

@Service
public class NacosService {
    private final FeignNacos feignNacos;

    public NacosService(FeignNacos feignNacos) {
        this.feignNacos = feignNacos;
    }

    public List<String> getNamespaces(String url) {
        try {
            NacosNameSpaces nameSpaces = feignNacos.getNamespaces(new URI(url));
            return nameSpaces.getData().stream().map(NacosNameSpaces.NameSpace::getNamespace).collect(Collectors.toList());
        } catch (Exception exp) {
            throw new RuntimeException(exp);
        }
    }

    // 获取指定命名空间的文件列表
    public List<String> getFiles(String url, String user, String pwd, String nameSpace) {
        try {
            URI uri = new URI(url);
            NacosToken token = feignNacos.login(uri, user, pwd);
            NacosFiles files = feignNacos.getConfigFiles(uri, nameSpace, token.getAccessToken());
            return files.getPageItems().stream().map(NacosFiles.File::getDataId).collect(Collectors.toList());
        } catch (Exception exp) {
            throw new RuntimeException(exp);
        }
    }

    // 获取文件内容
    public String getFile(String url, String user, String pwd, String nameSpace, String dataId, String group) {
        try {
            URI uri = new URI(url);
            NacosToken token = feignNacos.login(uri, user, pwd);
            return feignNacos.getConfig(uri, dataId, nameSpace, group, token.getAccessToken());
        } catch (Exception exp) {
            throw new RuntimeException(exp);
        }
    }

    /**
     * 转换为Map<String, Map>形式，如 {"aa":1, "spring":{"name":"xx", "bb":"zz"}}
     * 不是我需要的KeyValue形式
     *
     * @param yml yml格式的字符串
     * @return map
     */
    public Map<String, Object> parseYmlToMap(String yml) {
        try (InputStream inputStream = new ByteArrayInputStream(yml.getBytes())) {
            InputStreamResource resource = new InputStreamResource(inputStream);
            YamlMapFactoryBean yamlMapFactoryBean = new YamlMapFactoryBean();
            yamlMapFactoryBean.setResources(resource);//(new ClassPathResource("application.yml"));
            yamlMapFactoryBean.afterPropertiesSet();
            Map<String, Object> object = yamlMapFactoryBean.getObject();
            return object;
        } catch (Exception exp) {
            throw new RuntimeException(exp);
        }
    }

    /**
     * 转换为KeyValue形式
     *
     * @param yml yml格式的字符串
     * @return prop
     */
    public Properties parseYmlToKV(String yml) {
        try (InputStream inputStream = new ByteArrayInputStream(yml.getBytes())) {
            InputStreamResource resource = new InputStreamResource(inputStream);
            YamlPropertiesFactoryBean yamlBean = new YamlPropertiesFactoryBean();
            yamlBean.setResources(resource);//(new ClassPathResource("application.yml"));
            yamlBean.afterPropertiesSet();
            return yamlBean.getObject();
        } catch (Exception exp) {
            throw new RuntimeException(exp);
        }
    }
}
