package cn.beinet.codegenerate.configs;

import lombok.Data;
import lombok.RequiredArgsConstructor;
import lombok.experimental.Accessors;
import lombok.extern.slf4j.Slf4j;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.filter.OncePerRequestFilter;
import org.springframework.web.util.ContentCachingRequestWrapper;
import org.springframework.web.util.ContentCachingResponseWrapper;
import org.springframework.web.util.WebUtils;

import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Slf4j
@Component
@RequiredArgsConstructor
public class ControllerLogFilter extends OncePerRequestFilter {
    static Pattern patternRequest = Pattern.compile("(?i)^/actuator/?|\\.(ico|jpg|png|bmp|txt|xml|html?|js|css|ttf|woff|map)$");

    private final JdbcTemplate jdbcTemplate;

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain) throws ServletException, IOException {
        if (!log.isDebugEnabled() || isNotApiRequest(request)) {
            filterChain.doFilter(request, response);
            return;
        }

        long startTime = System.currentTimeMillis();
        if (!(request instanceof ContentCachingRequestWrapper)) {
            // 解决 inputStream 只能读取一次的问题
            request = new ContentCachingRequestWrapper(request);
        }
        if (!(response instanceof ContentCachingResponseWrapper)) {
            // 同样用于解决 响应只能读取一次的问题，注意要在最后调用 responseWrapper.copyBodyToResponse();
            response = new ContentCachingResponseWrapper(response);
        }

        Exception exception = null;
        try {
            filterChain.doFilter(request, response);
        } catch (Exception exp) {
            exception = exp;
            throw exp;
        } finally {
            long latency = System.currentTimeMillis() - startTime;
            readAndLog(request, response, latency, exception);
            repairResponse(response);
        }
    }

    private boolean isNotApiRequest(HttpServletRequest request) {
        //request.getRequestURL() 带有域名，所以不用
        //request.getRequestURI() 带有ContextPath，所以不用
        String url = request.getServletPath();
        Matcher matcher = patternRequest.matcher(url);
        return matcher.find();
    }

    private void readAndLog(HttpServletRequest request, HttpServletResponse response, long latency, Exception exception) {
        RequestLog logRec = new RequestLog();
        try {
            logRec.setLoginUser(LdapLoginFilter.getLoginInfo(request));
            logRec.setCostMillis(latency);
            getRequestMsg(request, logRec);
            getResponseMsg(response, logRec);

            if (exception != null) {
                logRec.setExp(exception.getMessage());
            }
            /* // 直接输出到响应流里
            try (ServletOutputStream stream = response.getOutputStream()) {
                stream.write(sb.toString().getBytes(StandardCharsets.UTF_8));
                stream.flush();
            }
            */
        } catch (Exception exp) {
            String msg = exp.getMessage();
            if (StringUtils.hasLength(logRec.getExp()))
                msg += "; " + logRec.getExp();
            logRec.setExp(msg);
        }
        doLog(logRec);
    }

    private void doLog(RequestLog logRec) {
        try {
            saveToDB(logRec);
        } catch (Exception exp) {
            log.error("写日志出错：", exp);
        }
    }

    private static void getRequestMsg(HttpServletRequest request, RequestLog logRec) throws IOException {
        logRec.setMethod(request.getMethod())
                .setUrl(request.getRequestURI())  // getRequestURI没有域名，getRequestURL会带域名
                .setQuery(request.getQueryString())
                .setIp(request.getRemoteAddr())
                .setRequestHeaders(new HashMap<>());

        Map<String, String> headers = logRec.getRequestHeaders();

        // 读取请求头信息
        Enumeration<String> headerNames = request.getHeaderNames();
        while (headerNames.hasMoreElements()) {
            String header = headerNames.nextElement();
            String value = "";
            Enumeration<String> values = request.getHeaders(header);
            while (values.hasMoreElements()) {
                if (value.length() > 0)
                    value += "; ";
                value += values.nextElement();
            }
            headers.put(header, value);
        }

        // 读取请求体
        String requestBody = readFromStream(request.getInputStream());
        logRec.setRequestBody(requestBody);
    }

    private static void getResponseMsg(HttpServletResponse response, RequestLog logRec) throws UnsupportedEncodingException {
        logRec.setResponseStatus(response.getStatus())
                .setResponseHeaders(new HashMap<>());

        // 读取响应的头信息
        Map<String, String> headers = logRec.getResponseHeaders();
        for (String header : response.getHeaderNames()) {
            String value = "";
            Collection<String> values = response.getHeaders(header);//.stream().collect(Collectors.joining("; "));
            for (String item : values) {
                if (value.length() > 0)
                    value += "; ";
                value += item;
            }
            headers.put(header, value);
        }

        // 读取响应体
        ContentCachingResponseWrapper wrapper = WebUtils.getNativeResponse(response, ContentCachingResponseWrapper.class);
        if (wrapper != null) {
            // 不能用 wrapper.getCharacterEncoding()
            String responseBody = transferFromByte(wrapper.getContentAsByteArray(), StandardCharsets.UTF_8.name());
            if (StringUtils.hasLength(responseBody)) {
                logRec.setResponseContent(responseBody);
            }
        }
    }

    private static void repairResponse(HttpServletResponse response) throws IOException {
        ContentCachingResponseWrapper responseWrapper = WebUtils.getNativeResponse(response, ContentCachingResponseWrapper.class);
        Objects.requireNonNull(responseWrapper).copyBodyToResponse();
    }

    private static String readFromStream(InputStream stream) throws IOException {
        ByteArrayOutputStream result = new ByteArrayOutputStream();
        byte[] buffer = new byte[1024];
        int length;
        while ((length = stream.read(buffer)) != -1) {
            result.write(buffer, 0, length);
        }
        return result.toString(StandardCharsets.UTF_8.name());
        // return new BufferedReader(new InputStreamReader(stream)).lines().collect(Collectors.joining(System.lineSeparator()));
    }

    private static String transferFromByte(byte[] arr, String encoding) throws UnsupportedEncodingException {
        return new String(arr, encoding);
    }

    @Data
    @Accessors(chain = true)
    private class RequestLog {
        private String loginUser;

        private String method;
        private String url;
        private String query;
        private String ip;
        private Map<String, String> requestHeaders;
        private String requestBody;

        private int responseStatus;
        private String responseContent;
        private Map<String, String> responseHeaders;

        private long costMillis;
        private String exp;
    }

    private void saveToDB(RequestLog logRec) {
        String sql = "INSERT INTO admin_log" +
                "(loginUser, method, url, query, ip, requestHeaders, requestBody, responseStatus, responseHeaders, costMillis, exp)" +
                "VALUES(?,?,?,?,?,?,?,?,?,?,?)";
        jdbcTemplate.update(sql, new Object[]{
                transNull(logRec.getLoginUser()),
                transNull(logRec.getMethod()),
                transNull(logRec.getUrl()),
                transNull(logRec.getQuery()),
                transNull(logRec.getIp()),
                combineMap(logRec.getRequestHeaders()),
                transNull(logRec.getRequestBody()),
                logRec.getResponseStatus(),
                combineMap(logRec.getResponseHeaders()),
                logRec.getCostMillis(),
                transNull(logRec.getExp())
        });
    }

    private String transNull(String val) {
        if (val == null)
            return "";
        return val;
    }

    private String combineMap(Map<String, String> map) {
        StringBuilder sb = new StringBuilder();
        for (Map.Entry<String, String> item : map.entrySet()) {
            if (sb.length() > 0)
                sb.append("\n");
            sb.append(item.getKey())
                    .append(" : ")
                    .append(item.getValue());
        }
        return sb.toString();
    }
}

/**
 CREATE TABLE `admin_log` (
 `id` int(11) NOT NULL AUTO_INCREMENT COMMENT '自增ID',
 `loginUser` varchar(50) NOT NULL DEFAULT '' COMMENT '登录人',
 `method` varchar(10) NOT NULL DEFAULT '' COMMENT '请求方法',
 `url` varchar(200) NOT NULL DEFAULT '' COMMENT '请求地址',
 `query` varchar(2000) NOT NULL DEFAULT '' COMMENT '查询串',
 `ip` varchar(20) NOT NULL DEFAULT '' COMMENT '请求IP',
 `requestHeaders` text NOT NULL COMMENT '请求头',
 `requestBody` text NOT NULL COMMENT '请求体',
 `responseStatus` int(11) NOT NULL DEFAULT '0' COMMENT '响应状态',
 `responseHeaders` text NOT NULL COMMENT '响应头',
 `costMillis` int(11) NOT NULL DEFAULT '0' COMMENT '耗时ms',
 `exp` text NOT NULL COMMENT '异常',
 `createDate` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
 PRIMARY KEY (`id`),
 KEY `idx_user` (`loginUser`),
 KEY `idx_url` (`url`)
 ) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='请求日志表';
 */