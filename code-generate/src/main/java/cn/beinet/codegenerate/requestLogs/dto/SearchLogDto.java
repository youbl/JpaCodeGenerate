package cn.beinet.codegenerate.requestLogs.dto;

import lombok.Data;
import lombok.experimental.Accessors;

/**
 * 新类
 *
 * @author youbl
 * @since 2023/12/13 14:56
 */
@Data
@Accessors(chain = true)
public class SearchLogDto {
    private Boolean except;
    private String loginUser;
    private String url;
    private String reportTimeBegin;
    private String reportTimeEnd;
    private Integer limit;
}
