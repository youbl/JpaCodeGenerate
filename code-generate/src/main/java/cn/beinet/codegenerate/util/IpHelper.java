package cn.beinet.codegenerate.util;

import jakarta.servlet.http.HttpServletRequest;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.springframework.util.StringUtils;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.Inet4Address;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.net.SocketAddress;
import java.net.URL;
import java.net.UnknownHostException;
import java.util.Arrays;
import java.util.Enumeration;

/**
 * Description:
 * IP相关工具方法
 *
 * @author youbl
 * @since 2021/9/15 20:57
 */
@Slf4j
public class IpHelper {
    private static final String[][] PRIVATE_IPV4 = {
            {"0.0.0.0", "0.255.255.255"},  // 0.0.0.0/8
            {"10.0.0.0", "10.255.255.255"},  // 10.0.0.0/8
            {"100.64.0.0", "100.127.255.255"},  // 100.64.0.0/10
            {"127.0.0.0", "127.255.255.255"},  // 127.0.0.0/8
            {"169.254.0.0", "169.254.255.255"},  // 169.254.0.0/16
            {"172.16.0.0", "172.31.255.255"},  // 172.16.0.0/12
            {"192.0.0.0", "192.0.0.255"},  // 192.0.0.0/24
            {"192.0.2.0", "192.0.2.255"},  // 192.0.2.0/24
            {"192.88.99.0", "192.88.99.255"},  // 192.88.99.0/24
            {"192.168.0.0", "192.168.255.255"},  // 192.168.0.0/16
            {"198.18.0.0", "198.19.255.255"},  // 198.18.0.0/15
            {"198.51.100.0", "198.51.100.255"},  // 198.51.100.0/24
            {"203.0.113.0", "203.0.113.255"},  // 203.0.113.0/24
            {"224.0.0.0", "239.255.255.255"},  // 224.0.0.0/4
            {"233.252.0.0", "233.252.0.255"},  // 233.252.0.0/24
            {"240.0.0.0", "255.255.255.254"},  // 240.0.0.0/4
            {"255.255.255.255", "255.255.255.254"}  // 255.255.255.255/32
    };
    private static final Long[][] PRIVATE_IPV4Number = Arrays.stream(PRIVATE_IPV4)
            .map(range -> Arrays.stream(range).map(IpHelper::ipV4ToNumber).toArray(Long[]::new))
            .toArray(Long[][]::new);

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
     * 获取本机的公网出口IP
     *
     * @param ipUrl 检测出口IP的地址，可空
     * @return 公网出口IP
     */
    public static String getOuterIp(String ipUrl) {
        if (!StringUtils.hasLength(ipUrl)) {
            ipUrl = "https://tools01.ziniao.com/get_ip";
        }
        return getResponse(ipUrl);
    }

    private static String getResponse(String url) {
        HttpURLConnection conn = null;
        try {
            conn = (HttpURLConnection) (new URL(url).openConnection());
            conn.setRequestMethod("GET");
            conn.setConnectTimeout(3000); // 设置连接超时为3秒
            conn.setReadTimeout(3000); // 设置读取超时为3秒
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
     * 获取发起放的请求ip地址
     * 注意：因为客户端Header可以伪造，不推荐此方法用于对安全有要求的场景。
     *
     * @param request 请求上下文
     * @return IP地址
     */
    public static String getIpAddr(HttpServletRequest request) {
        String ip = request.getHeader("x-forwarded-for");
        try {
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
        } catch (Exception exp) {
            log.error("getIPErr", exp);
        }
        if (ip == null) {
            logRequestHeader(request);
            return "";
        }
        return ip.equals("0:0:0:0:0:0:0:1") ? "127.0.0.1" : ip.split(",")[0].trim();
    }

    public static String getRequestHeader(HttpServletRequest request) {
        StringBuilder sb = new StringBuilder();
        sb.append(request.getMethod())
                .append(" ")
                .append(request.getRequestURL())
                .append("\r\n\n");
        sb.append("IP-addr:")
                .append(request.getRemoteAddr())
                .append(" Port:")
                .append(request.getRemotePort())
                .append("\r\n\nAll Headers:\r\n");

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

    private static void logRequestHeader(HttpServletRequest request) {
        try {
            String headers = getRequestHeader(request);
            log.error("getIPErr empty: " + headers + " callStack:" + Arrays.toString(Thread.currentThread().getStackTrace()));
//            if (Objects.isNull(request.getMethod())) {
//                throw new RuntimeException("未知请求，抛个异常打印堆栈来看看。");
//            }
        } catch (Exception exp) {
            log.error("logHeaderErr", exp);
        }
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

//    /**
//     * 判断给出的IP是否IPv6
//     *
//     * @param ip ip
//     * @return true false
//     */
//    public static boolean isIPv6(String ip) {
//        try {
//            Inet6Address inetAddress = (Inet6Address) Inet6Address.getByAddress(ip.getBytes());
//            return inetAddress != null;
//        } catch (UnknownHostException | ClassCastException e) {
//            return false;
//        }
//    }

    /**
     * 判断给出的IP是否IPv4
     *
     * @param ip ip
     * @return true false
     */
    public static boolean isIPv4(String ip) {
        try {
            String[] arr = ip.split("\\.");
            if (arr.length != 4)
                return false;

            // 不能用Byte.parseByte，因为Byte范围只支持 -128~127
            byte[] arrBt = new byte[]{(byte) Integer.parseInt(arr[0]),
                    (byte) Integer.parseInt(arr[1]),
                    (byte) Integer.parseInt(arr[2]),
                    (byte) Integer.parseInt(arr[3])};
            Inet4Address inetAddress = (Inet4Address) Inet4Address.getByAddress(arrBt);
            return inetAddress != null;
        } catch (UnknownHostException | ClassCastException e) {
            return false;
        }
    }

    /**
     * 把ip地址转换为整数返回
     *
     * @param ip ip地址
     * @return 整数
     */
    public static long ipV4ToNumber(String ip) {
        String[] ipArr = ip.split("\\.");
        if (ipArr.length != 4) {
            throw new IllegalArgumentException("IP地址格式不对，应该有3个小数点");
        }
        long ret = 0;
        // 验证每个项是否小于等于255
        for (int i = 0; i < 4; i++) {
            int number = Integer.parseInt(ipArr[i]);
            if (number > 255 || number < 0) {
                throw new IllegalArgumentException("IP地址中的每个项都应在0~255之间");
            }
            ret = (ret << 8) + number;
        }
        // 转无符号数，避免负数返回
        return ret;
    }

    /**
     * 给定的ip，是否在给定的ip起止范围内
     *
     * @param ip         要判断的ip
     * @param startIPNum ip范围起始值
     * @param endIPNum   ip范围结束值
     * @return 是否在范围内
     */
    public static boolean inIpV4Range(String ip, long startIPNum, long endIPNum) {
        long ipNum = ipV4ToNumber(ip);
        return ipNum >= startIPNum && ipNum <= endIPNum;
    }

    /**
     * 判断给定的ip，是否属于 IANA定义的保留地址（即私有地址）
     *
     * @param ip 给定的ip
     * @return 是否私有地址
     */
    public static boolean isPrivateIpAddr(String ip) {
        try {
            if (isIPv4(ip)) {
                for (Long[] range : PRIVATE_IPV4Number) {
                    if (inIpV4Range(ip, range[0], range[1])) {
                        return true;
                    }
                }
            }
        } catch (Exception e) {
            log.error("isPrivateIpAddr", e);
        }
        return false;
    }
}
