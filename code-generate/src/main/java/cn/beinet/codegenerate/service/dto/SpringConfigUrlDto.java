package cn.beinet.codegenerate.service.dto;

import lombok.Data;

/**
 * 新类
 * @author youbl
 * @since 2024/11/21 17:34
 */
@Data
public class SpringConfigUrlDto {
    private String url;
    private String application;
    private String profile;
    private String label;
    private boolean ignoreGlobal;

    public String getUrl() {
        if (url == null) {
            url = "/";
        }
        if (!url.endsWith("/")) {
            url += "/";
        }
        return url;
    }
}
