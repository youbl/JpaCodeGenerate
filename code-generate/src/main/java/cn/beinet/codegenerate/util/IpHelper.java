package cn.beinet.codegenerate.util;

import lombok.SneakyThrows;
import org.springframework.util.StringUtils;

import javax.servlet.http.HttpServletRequest;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.net.SocketAddress;
import java.net.URL;
import java.net.UnknownHostException;

/**
 * Description:
 * IP相关工具方法
 *
 * @author : youbl
 * @create: 2021/9/15 20:57
 */
public class IpHelper {

    /**
     * 获取本机网卡IP
     *
     * @return 本机网卡IP
     */
    public static String getServerIp() {
        try {
            InetAddress address = InetAddress.getLocalHost();
            return address.getHostAddress();
        } catch (UnknownHostException e) {
            return e.getMessage();
        }
    }

    /**
     * 获取本机的公网出口IP
     *
     * @param ipUrl 检测出口IP的地址，可空
     * @return 公网出口IP
     */
    public static String getOuterIp(String ipUrl) {
        if (!StringUtils.hasLength(ipUrl)) {
            ipUrl = "https://tools01.ziniao.com/get_ip";
        }
        HttpURLConnection conn = null;
        try {
            conn = (HttpURLConnection) (new URL(ipUrl).openConnection());
            conn.setRequestMethod("GET");
            int responseCode = conn.getResponseCode();

            if (responseCode == HttpURLConnection.HTTP_OK) {
                try (BufferedReader in = new BufferedReader(new InputStreamReader(conn.getInputStream()))) {
                    String inputLine;
                    StringBuilder response = new StringBuilder();

                    while ((inputLine = in.readLine()) != null) {
                        response.append(inputLine);
                    }
                    return response.toString();
                }
            }
            return ("响应：" + responseCode);
        } catch (Exception e) {
            return e.getMessage();
        } finally {
            if (conn != null)
                conn.disconnect();
        }
    }


    /**
     * 获取当前请求上下文里的所有客户端IP
     *
     * @param request 请求上下文
     * @return 客户端IP列表
     */
    public static String getClientIp(HttpServletRequest request) {
        String remoteAddr = request.getRemoteAddr();
        remoteAddr += getHeader(request, "X-Forwarded-For", "f");
        remoteAddr += getHeader(request, "X-Real-IP", "r");
        remoteAddr += getHeader(request, "ali-cdn-real-ip", "d");
        remoteAddr += getHeader(request, "Proxy-Client-IP", "p");
        remoteAddr += getHeader(request, "WL-Proxy-Client-IP", "w");
        return remoteAddr;
    }

    private static String getHeader(HttpServletRequest request, String header, String prefix) {
        String realIp = request.getHeader(header);
        if (StringUtils.hasLength(realIp)) {
            return ";" + prefix + ":" + realIp;
        }
        return "";
    }

    /**
     * 按定死的顺序，获取唯一一个客户端请求ip
     * 注意：因为客户端Header可以伪造，不推荐此方法用于对安全有要求的场景。
     *
     * @param request 请求上下文
     * @return 唯一IP
     */
    public static String getIpAddr(HttpServletRequest request) {
        String ip = request.getHeader("x-forwarded-for");
        if (ip == null || ip.isEmpty() || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getHeader("ali-cdn-real-ip");
        }
        if (ip == null || ip.isEmpty() || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getHeader("x-real-ip");
        }
        if (ip == null || ip.isEmpty() || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getHeader("Proxy-Client-IP");
        }
        if (ip == null || ip.isEmpty() || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getHeader("WL-Proxy-Client-IP");
        }
        if (ip == null || ip.isEmpty() || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getRemoteAddr();
        }
        return ip.equals("0:0:0:0:0:0:0:1") ? "127.0.0.1" : ip.split(",")[0].trim();
    }

    /**
     * 尝试连接指定IP和端口，用于检测网络是否连通
     *
     * @param ip      远端IP或域名
     * @param port    远端端口
     * @param timeout 超时时间，毫秒，默认5000
     */
    @SneakyThrows
    public static void telnet(String ip, int port, Integer timeout) {
        if (timeout == null)
            timeout = 5000;

        SocketAddress address = new InetSocketAddress(ip, port);
        try (Socket socket = new Socket()) {
            socket.connect(address, timeout);
            // socket.setSoTimeout(timeout);

            // 增加休眠代码，可以在cmd里查询端口连接情况： netstat -ano |findstr 对应IP
            // 休眠期间，会输出：  TCP    10.100.68.142:14290    IP:端口      ESTABLISHED     11556
            // Thread.sleep(3000);
        }
    }

    @SneakyThrows
    public static String ping(String ip, Integer timeout, int tryTimes) {
        if (timeout == null || timeout <= 0)
            timeout = 3000;
        if (tryTimes <= 0)
            tryTimes = 3;

        StringBuilder stringBuilder = new StringBuilder();
        while (tryTimes > 0) {
            tryTimes--;

            String result;
            long start = System.currentTimeMillis();
            try {
                InetAddress address = InetAddress.getByName(ip);
                // java1.4支持的ping命令
                if (address.isReachable(timeout)) {
                    result = "成功";
                } else {
                    result = "失败";
                }
            } catch (Exception exp) {
                result = "错误:" + exp.getMessage();
            }

            long cost = System.currentTimeMillis() - start;
            stringBuilder.append(ip)
                    .append(" ping")
                    .append(result)
                    .append(",耗时:")
                    .append(cost)
                    .append("ms")
                    .append("; ");
        }
        return stringBuilder.toString();
    }
}
