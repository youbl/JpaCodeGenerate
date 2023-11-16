package cn.beinet.codegenerate.controller;

import cn.beinet.codegenerate.util.RequestHelper;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.HttpServletRequest;
import java.util.Enumeration;

/**
 * 新类
 *
 * @author youbl
 * @since 2023/10/16 15:20
 */
@RestController
public class TestController {
    @GetMapping(value = "test", produces = {"text/plain"})
    public String test(HttpServletRequest request) {
        StringBuilder sb = new StringBuilder();
        sb.append("base domain:")
                .append(RequestHelper.getBaseDomain(request))
                .append("\r\n\n");

        sb.append(request.getMethod())
                .append(" ")
                .append(request.getRequestURL())
                .append("\r\n\n");
        sb.append("IP-addr: ")
                .append(request.getRemoteAddr())
                .append(" ")
                .append(request.getRemoteHost())
                .append(" ")
                .append(request.getRemotePort())
                .append(" ")
                .append(request.getRemoteUser())
                .append("\r\n\n");

        Enumeration<String> allHeader = request.getHeaderNames();
        while (allHeader.hasMoreElements()) {
            String header = allHeader.nextElement();
            Enumeration<String> vals = request.getHeaders(header);
            while (vals.hasMoreElements()) {
                String val = vals.nextElement();
                sb.append(header)
                        .append(" : ")
                        .append(val)
                        .append("\r\n");
            }
        }

        return sb.toString();
    }
}
