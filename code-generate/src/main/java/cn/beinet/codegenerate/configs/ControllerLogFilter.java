package cn.beinet.codegenerate.configs;

import lombok.extern.slf4j.Slf4j;
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
import java.util.Collection;
import java.util.Enumeration;
import java.util.Objects;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Slf4j
@Component
public class ControllerLogFilter extends OncePerRequestFilter {
    static Pattern patternRequest = Pattern.compile("(?i)^/actuator/?|\\.(ico|jpg|png|bmp|txt|xml|html?|js|css|ttf|woff|map)$");

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
            doLog(request, response, latency, exception);
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

    private void doLog(HttpServletRequest request, HttpServletResponse response, long latency, Exception exception) {
        StringBuilder sb = new StringBuilder();
        try {
            getRequestMsg(request, sb);

            sb.append("\n--响应 ")
                    .append(response.getStatus())
                    .append("  耗时 ")
                    .append(latency)
                    .append("ms");

            getResponseMsg(response, sb);

            if (exception != null) {
                sb.append("\n--异常 ")
                        .append(exception.getMessage());
            }
            /* // 直接输出到响应流里
            try (ServletOutputStream stream = response.getOutputStream()) {
                stream.write(sb.toString().getBytes(StandardCharsets.UTF_8));
                stream.flush();
            }
            */

            logger.debug(sb.toString());
        } catch (Exception exp) {
            sb.append("\n").append(exp.getMessage());
            logger.error(sb.toString());
        }
    }

    private static void getRequestMsg(HttpServletRequest request, StringBuilder sb) throws IOException {
        String query = request.getQueryString();
        if (!StringUtils.isEmpty(query)) {
            query = "?" + query;
        } else {
            query = "";
        }
        sb.append("\n")
                .append(request.getMethod())
                .append(" ")
                .append(request.getRequestURL())
                .append(query)
                .append("\n--用户IP: ")
                .append(request.getRemoteAddr())
                .append("\n--请求Header:");
        // 读取请求头信息
        Enumeration<String> headerNames = request.getHeaderNames();
        while (headerNames.hasMoreElements()) {
            String header = headerNames.nextElement();
            Enumeration<String> values = request.getHeaders(header);
            while (values.hasMoreElements()) {
                sb.append("\n")
                        .append(header)
                        .append(" : ")
                        .append(values.nextElement()).append("; ");
            }
        }
        // 读取请求体
        String requestBody = readFromStream(request.getInputStream());
        if (!StringUtils.isEmpty(requestBody)) {
            sb.append("\n--请求体:\n")
                    .append(requestBody);
        }
    }

    private static void getResponseMsg(HttpServletResponse response, StringBuilder sb) throws UnsupportedEncodingException {
        sb.append("\n--响应Header: ");
        for (String header : response.getHeaderNames()) {
            Collection<String> values = response.getHeaders(header);//.stream().collect(Collectors.joining("; "));
            for (String value : values) {
                sb.append("\n")
                        .append(header)
                        .append(" : ")
                        .append(value);
            }
        }
        // 读取响应体
        ContentCachingResponseWrapper wrapper = WebUtils.getNativeResponse(response, ContentCachingResponseWrapper.class);
        if (wrapper != null) {
            // 不能用 wrapper.getCharacterEncoding()
            String responseBody = transferFromByte(wrapper.getContentAsByteArray(), StandardCharsets.UTF_8.name());
            if (!StringUtils.isEmpty(responseBody)) {
                sb.append("\n--响应Body:\n")
                        .append(responseBody);
            } else {
                sb.append("\n--无响应Body.");
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
