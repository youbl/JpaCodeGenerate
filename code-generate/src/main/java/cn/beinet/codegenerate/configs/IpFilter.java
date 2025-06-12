package cn.beinet.codegenerate.configs;

import cn.beinet.codegenerate.configs.config.ConfigIpFilter;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import java.io.IOException;
import java.nio.charset.StandardCharsets;

/**
 * 过滤一些IP的访问
 *
 * @author : youbl
 * @create: 2023/12/08 16:34
 */
@Component
@Slf4j
@RequiredArgsConstructor
public class IpFilter extends BaseFilter {

    private final ConfigIpFilter configs;

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain) throws ServletException, IOException {
        if (isExcluedUrl(request) || isWhiteIp(request)) {
            filterChain.doFilter(request, response);
            return;
        }
        String ret = "ip denied: " + getClientIp(request) + " " + request.getRequestURL();
        log.warn(ret);
        response.getOutputStream().write(ret.getBytes(StandardCharsets.UTF_8));
    }

    private boolean isExcluedUrl(HttpServletRequest request) {
        if (configs.getExcludeUrl() != null) {
            //request.getRequestURL() 带有域名，所以不用
            //request.getRequestURI() 带有ContextPath，所以不用
            String url = request.getServletPath();
            for (String item : configs.getExcludeUrl()) {
                if (item != null && url.startsWith(item))
                    return true;
            }
        }
        return false;
    }

    private boolean isWhiteIp(HttpServletRequest request) {
        // nginx获取到的，都是127，所以所有IP都要判断
        String ip1 = request.getRemoteAddr();
        String ip2 = request.getHeader("x-forwarded-for");
        String ip3 = request.getHeader("x-real-for");
        return (isWhiteIp(ip1) &&
                (!StringUtils.hasLength(ip2) || isWhiteIp(ip2)) &&
                (!StringUtils.hasLength(ip3) || isWhiteIp(ip3)));
    }

    private boolean isWhiteIp(String ip) {
//        String[] interIps = new String[]{
//                "127.",
//                "10.",
//                "192.168.",
//                "0:0:0:0:0:0:0:1",
//        };
        if (configs.getWhiteIp() != null) {
            for (String item : configs.getWhiteIp()) {
                if (item != null && ip.startsWith(item))
                    return true;
            }
        }
        return false;
    }

    private String getClientIp(HttpServletRequest request) {
        String ip1 = request.getRemoteAddr();
        String ip2 = request.getHeader("x-forwarded-for");
        String ip3 = request.getHeader("x-real-for");
        return ip1 + ";f:" + ip2 + ";r:" + ip3;
    }
}
