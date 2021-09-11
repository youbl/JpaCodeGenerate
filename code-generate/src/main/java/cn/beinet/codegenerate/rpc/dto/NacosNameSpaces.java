package cn.beinet.codegenerate.rpc.dto;

import lombok.Data;
import org.springframework.util.StringUtils;

import java.util.List;

/**
 * 调nacos命名空间列表
 */
@Data
public class NacosNameSpaces {
    private String message;
    private int code;
    private List<NameSpace> data;

    @Data
    public static class NameSpace {
        private String namespace;

        public String getNamespace() {
            if (StringUtils.hasLength(namespace))
                return namespace;
            return namespaceShowName;
        }

        private String namespaceShowName;
        private int quota;
        private int configCount;
        private int type;
    }
}
