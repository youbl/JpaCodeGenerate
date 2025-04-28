package cn.beinet.codegenerate.controller;

import cn.beinet.codegenerate.util.IpHelper;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.util.StringUtils;
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
    private final String callbackPrefix = "jsonp_callback";

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

    @GetMapping(value = "test/ip", produces = {"application/javascript"})
    public String testShowClientIp(HttpServletRequest request) {
        String ret = IpHelper.getClientIp(request);
        return combinJsonp(ret, request);
    }

    @GetMapping(value = "test/sip", produces = {"application/javascript"})
    public String testShowServerIp(HttpServletRequest request) {
        String ret = "client:[" + IpHelper.getClientIp(request) + "]" +
                " local:[" + IpHelper.getServerIp() + "]" +
                " outer:[" + IpHelper.getOuterIp(getIpUrl) + "]";
        return combinJsonp(ret, request);
    }

    /**
     * 在本机测试与远端IP的网络连接性，模拟Telnet
     *
     * @param ip      远端IP
     * @param port    远端端口
     * @param timeout 超时时间，默认5000毫秒
     * @return 测试结果
     */
    @GetMapping(value = "test/telnet", produces = {"application/javascript"})
    @SneakyThrows
    public String testTelnet(@RequestParam String ip,
                             @RequestParam int port,
                             @RequestParam(required = false) Integer timeout,
                             HttpServletRequest request) {
        // 不允许操作内网IP，以防安全问题
        validateIp(ip);

        String ret = "telnet " + ip + " " + port + "    连接";
        long start = System.currentTimeMillis();
        try {
            IpHelper.telnet(ip, port, timeout);
            ret += "成功";
        } catch (Exception exp) {
            ret += "失败:" + exp.getMessage();
        }

        ret += " 耗时:" + (System.currentTimeMillis() - start) + "ms";
        return combinJsonp(ret, request);
    }


    /**
     * 在本机测试与远端IP的网络连接性，模拟ping
     *
     * @param ip      远端IP
     * @param timeout 超时时间，默认5000毫秒
     * @return 测试结果
     */
    @GetMapping(value = "test/ping", produces = {"application/javascript"})
    @SneakyThrows
    public String testPing(@RequestParam String ip,
                           @RequestParam(required = false) Integer timeout,
                           HttpServletRequest request) {
        // 不允许操作内网IP，以防安全问题
        validateIp(ip);

        String ret = IpHelper.ping(ip, timeout, 3);
        return combinJsonp(ret, request);
    }

    private String combinJsonp(String str, HttpServletRequest request) {
        // 前端jsonp的支持
        String jsonp = getCallbackValue(request);
        if (StringUtils.hasLength(jsonp)) {
            str = str.replaceAll("\r", "\\r")
                    .replaceAll("\n", "\\n");
            str = jsonp + "('" + str + "')";
        }
        return str;
    }

    /**
     * 从请求上下文中，读取jsonp的回调函数名
     *
     * @param request 请求上下文
     * @return callback函数名
     */
    private String getCallbackValue(HttpServletRequest request) {
        Enumeration<String> paraNameArr = request.getParameterNames();
        while (paraNameArr.hasMoreElements()) {
            String paraName = paraNameArr.nextElement();
            if (StringUtils.hasLength(paraName) && paraName.startsWith(callbackPrefix))
                return request.getParameter(paraName);
        }
        return "";
    }

    private void validateIp(String ip) {
        if (ip == null) {
            throw new RuntimeException("ip is null");
        }
        ip = ip.trim();
        if (ip.isEmpty()) {
            throw new RuntimeException("ip is null");
        }
        if (!IpHelper.isIPv4(ip)) {
            // 不是IP，不判断
            return;
        }
        if (IpHelper.isPrivateIpAddr(ip)) {
            throw new RuntimeException("can't operate private ip");
        }
    }
}
