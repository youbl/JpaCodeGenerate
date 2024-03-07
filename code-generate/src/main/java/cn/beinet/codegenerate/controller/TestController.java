package cn.beinet.codegenerate.controller;

import cn.beinet.codegenerate.util.IpHelper;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
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
@Slf4j
public class TestController {
    @Value("${getip-url}")
    private String getIpUrl;

    @GetMapping(value = "test", produces = {"text/plain"})
    public String test(HttpServletRequest request) {
        StringBuilder sb = new StringBuilder();
        sb.append(request.getMethod())
                .append(" ")
                .append(request.getRequestURL())
                .append("\r\n\n");
        sb.append("IP-addr:")
                .append(request.getRemoteAddr())
                .append(" Host:")
                .append(request.getRemoteHost())
                .append(" Port:")
                .append(request.getRemotePort())
                .append(" User:")
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

    @GetMapping("test/loop")
    @SneakyThrows
    public String testLoop() {
        int loopTime = 1000;
        while (loopTime > 0) {
            log.info(loopTime + " looping...");
            loopTime--;
            Thread.sleep(10000);
        }
        return "ok";
    }

    @GetMapping("test/ip")
    public String testShowClientIp(HttpServletRequest request) {
        return IpHelper.getClientIp(request);
    }

    @GetMapping("test/sip")
    public String testShowServerIp(HttpServletRequest request) {
        return "local:[" + IpHelper.getServerIp() + "] outer:[" + IpHelper.getOuterIp(getIpUrl) + "]";
    }

    /**
     * 在本机测试与远端IP的网络连接性，模拟Telnet
     *
     * @param ip      远端IP
     * @param port    远端端口
     * @param timeout 超时时间，默认5000毫秒
     * @return 测试结果
     */
    @GetMapping("test/telnet")
    @SneakyThrows
    public String testTelnet(@RequestParam String ip, @RequestParam int port, @RequestParam(required = false) Integer timeout) {
        String ret = ip + ":" + port + " 连接";
        long start = System.currentTimeMillis();
        try {
            IpHelper.telnet(ip, port, timeout);
            ret += "成功";
        } catch (Exception exp) {
            ret += "失败:" + exp.getMessage();
        }
        return ret + " 耗时:" + (System.currentTimeMillis() - start) + "ms";
    }
}
