package cn.beinet.codegenerate.configs;

import cn.beinet.codegenerate.requestLogs.RequestLog;
import cn.beinet.codegenerate.requestLogs.RequestLogService;
import cn.beinet.codegenerate.util.MultiReadHttpServletRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.filter.OncePerRequestFilter;
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
    static Pattern patternRequest = Pattern.compile("(?i)^/(actuator|loginuser|logs)/?|\\.(ico|jpg|png|bmp|txt|xml|html?|js|css|ttf|woff|map|svg)$");

    private final RequestLogService logService;

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain) throws ServletException, IOException {
        if (!log.isDebugEnabled() || isNotApiRequest(request)) {
            filterChain.doFilter(request, response);
            return;
        }

        long startTime = System.currentTimeMillis();
        if (!(request instanceof MultiReadHttpServletRequest)) {
            // 解决 inputStream 只能读取一次的问题
            request = new MultiReadHttpServletRequest(request);
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
        if (url.equals("/"))
            return true;
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
            logService.saveToDB(logRec);
        } catch (Exception exp) {
            log.error("写日志出错：", exp);
        }
    }

    private static void getRequestMsg(HttpServletRequest request, RequestLog logRec) throws IOException {
        logRec.setMethod(request.getMethod())
                .setUrl(request.getRequestURI())  // getRequestURI没有域名，getRequestURL会带域名
                .setQuery(request.getQueryString())
                .setIp(request.getRemoteAddr())
                .setRequestHeaderMap(new HashMap<>());

        Map<String, String> headers = logRec.getRequestHeaderMap();

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
        //String requestBody = IOUtils.toString(request.getReader());
        String requestBody = readFromStream(request.getInputStream());
        logRec.setRequestBody(requestBody);
    }

    private static void getResponseMsg(HttpServletResponse response, RequestLog logRec) throws UnsupportedEncodingException {
        logRec.setResponseStatus(response.getStatus())
                .setResponseHeaderMap(new HashMap<>());

        // 读取响应的头信息
        Map<String, String> headers = logRec.getResponseHeaderMap();
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

}