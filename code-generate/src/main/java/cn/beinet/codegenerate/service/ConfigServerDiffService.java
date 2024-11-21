package cn.beinet.codegenerate.service;

import cn.beinet.codegenerate.service.dto.SpringConfigUrlDto;
import cn.beinet.codegenerate.util.HttpHelper;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Properties;

/**
 * 用于Spring Cloud Config Server配置对比的接口类
 * @author youbl
 * @since 2024/11/21 14:50
 */
@Service
@RequiredArgsConstructor
public class ConfigServerDiffService {
    private final ObjectMapper objectMapper;

    @SneakyThrows
    public Properties getConfigs(SpringConfigUrlDto dto) {
        String configServerUrl = dto.getUrl() + dto.getApplication() + '/' + dto.getProfile() + '/' + dto.getLabel();
        /*
        从Spring Cloud Config Server获取到的数据格式参考：
{
	"name": "beinet-app",
	"profiles": ["dev"],
	"label": "test",
	"version": "abc",
	"state": null,
	"propertySources": [{
		"name": "https://git.beinet.cn/backend-configs.git/configs-repo/application-dev.yml",
		"source": {
			"spring.data.redis.host": "10.100.72.155",
			"spring.data.redis.port": 8379
		}
	}, {
		"name": "https://git.beinet.cn/backend-configs.git/configs-repo/beinet-app.yml",
		"source": {
			"server.port": 8080,
			"spring.redis.database": 0,
			"user.invite.expire": 2
		}
	}, {
		"name": "https://git.beinet.cn/backend-configs.git/configs-repo/application.yml",
		"source": {
			"spring.cache.type": "redis"
		}
	}]
}
        * */
        String jsonStr = HttpHelper.GetPage(configServerUrl);
        if (!StringUtils.hasLength(jsonStr)) {
            return new Properties();
        }

        Map<String, Object> map = objectMapper.readValue(jsonStr, Map.class);
        if (map == null || map.isEmpty()) {
            return new Properties();
        }
        Object tmp = map.get("propertySources");
        if (!(tmp instanceof List)) {
            return new Properties();
        }
        List<Map<String, Object>> allMap = resort((List<Map<String, Object>>) tmp, dto);

        Properties props = new Properties();
        for (Map<String, Object> mapSource : allMap) {
            mapSource.forEach((key, value) -> {
                props.put(key, value);
            });
        }
        return props;
    }

    // 按配置生效顺序，从低优先级到高优先级排序后返回，
    // 方便外部加载数据
    private List<Map<String, Object>> resort(List<Map<String, Object>> arrSources, SpringConfigUrlDto dto) {
        Map<String, Object> applicationBase = null;     // 指向application.yml所有环境的全局配置
        Map<String, Object> applicationProfile = null;  // 指向application-dev.yml 这种指定环境的全局配置
        Map<String, Object> appBase = null;             // 指向beinet-app.yml 这种指定应用的所有环境的配置
        Map<String, Object> appProfile = null;          // 指向beinet-app-dev.yml 这种指定应用且指定环境的配置

        for (Map<String, Object> mapSource : arrSources) {
            String name = (String) mapSource.get("name");
            if (name == null || name.isEmpty()) {
                throw new RuntimeException("name is null: " + mapSource);
            }
            if (!name.endsWith(".yml")) {
                throw new RuntimeException("不是yml文件: " + mapSource);
            }

            Map<String, Object> source = (Map<String, Object>) mapSource.get("source");
            if (name.endsWith("application.yml")) {
                applicationBase = source;
                continue;
            }

//            Pattern pattern = Pattern.compile("application[^/]+\\.yml");
//            Matcher matcher = pattern.matcher(name);
//            if (matcher.find()) {
            if (name.endsWith("application-" + dto.getProfile() + ".yml")) {
                applicationProfile = source;
                continue;
            }

            if (name.endsWith(dto.getApplication() + ".yml")) {
                appBase = source;
                continue;
            }

            if (name.endsWith(dto.getApplication() + "-" + dto.getProfile() + ".yml")) {
                appProfile = source;
                continue;
            }

            throw new RuntimeException("未识别的配置文件: " + mapSource);
        }

        // 按从低到高，添加配置
        List<Map<String, Object>> ret = new ArrayList<>();
        if (!dto.isIgnoreGlobal() && applicationBase != null) {
            ret.add(applicationBase);
        }
        if (!dto.isIgnoreGlobal() && applicationProfile != null) {
            ret.add(applicationProfile);
        }
        if (appBase != null) {
            ret.add(appBase);
        }
        if (appProfile != null) {
            ret.add(appProfile);
        }
        return ret;
    }

}
