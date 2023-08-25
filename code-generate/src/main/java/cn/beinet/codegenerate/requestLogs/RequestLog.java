package cn.beinet.codegenerate.requestLogs;

import lombok.Data;
import lombok.experimental.Accessors;

import java.time.LocalDateTime;
import java.util.Map;

/**
 * 新类
 *
 * @author youbl
 * @since 2023/8/25 23:48
 */
@Data
@Accessors(chain = true)
public class RequestLog {
    private int id;
    private LocalDateTime createDate;

    private String loginUser;

    private String method;
    private String url;
    private String query;
    private String ip;
    private Map<String, String> requestHeaderMap;
    private String requestHeaders;
    private String requestBody;

    private int responseStatus;
    private String responseContent;
    private Map<String, String> responseHeaderMap;
    private String responseHeaders;

    private long costMillis;
    private String exp;
}