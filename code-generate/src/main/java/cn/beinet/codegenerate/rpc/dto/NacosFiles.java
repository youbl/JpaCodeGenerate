package cn.beinet.codegenerate.rpc.dto;

import lombok.Data;
import org.springframework.util.StringUtils;

import java.util.List;

/**
 * 调nacos命名空间下的文件列表
 */
@Data
public class NacosFiles {
    private int pageNumber;
    private int pagesAvailable;
    private int totalCount;
    private List<File> pageItems;

    @Data
    public static class File {
        private String dataId;
        private String group;
        private String type;

        // 这是配置的实际内容,本程序暂时不用
        // private String content;
    }
}
