package cn.beinet.codegenerate.requestLogs;

import lombok.RequiredArgsConstructor;
import org.springframework.jdbc.core.BeanPropertyRowMapper;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import java.util.List;
import java.util.Map;

/**
 * 新类
 *
 * @author youbl
 * @since 2023/8/25 23:49
 */
@Component
@RequiredArgsConstructor
public class RequestLogService {
    private final JdbcTemplate jdbcTemplate;

    public List<RequestLog> getNewLogs(String user) {
        String sql = "select * from admin_log ";
        Object[] args;
        if (StringUtils.hasLength(user)) {
            args = new Object[]{user};
            sql += "where loginUser=? ";
        } else {
            args = new Object[0];
        }
        sql += "order by id desc limit 1000";
        return jdbcTemplate.query(sql, args, new BeanPropertyRowMapper<>(RequestLog.class));
    }

    public void saveToDB(RequestLog logRec) {
        String sql = "INSERT INTO admin_log" +
                "(loginUser, method, url, query, ip, requestHeaders, requestBody, responseStatus, responseHeaders, costMillis, exp)" +
                "VALUES(?,?,?,?,?,?,?,?,?,?,?)";
        jdbcTemplate.update(sql, new Object[]{
                transNull(logRec.getLoginUser()),
                transNull(logRec.getMethod()),
                transNull(logRec.getUrl()),
                transNull(logRec.getQuery()),
                transNull(logRec.getIp()),
                combineMap(logRec.getRequestHeaderMap()),
                transNull(logRec.getRequestBody()),
                logRec.getResponseStatus(),
                combineMap(logRec.getResponseHeaderMap()),
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
 * CREATE TABLE `admin_log` (
 * `id` int(11) NOT NULL AUTO_INCREMENT COMMENT '自增ID',
 * `loginUser` varchar(50) NOT NULL DEFAULT '' COMMENT '登录人',
 * `method` varchar(10) NOT NULL DEFAULT '' COMMENT '请求方法',
 * `url` varchar(200) NOT NULL DEFAULT '' COMMENT '请求地址',
 * `query` varchar(2000) NOT NULL DEFAULT '' COMMENT '查询串',
 * `ip` varchar(20) NOT NULL DEFAULT '' COMMENT '请求IP',
 * `requestHeaders` text NOT NULL COMMENT '请求头',
 * `requestBody` text NOT NULL COMMENT '请求体',
 * `responseStatus` int(11) NOT NULL DEFAULT '0' COMMENT '响应状态',
 * `responseHeaders` text NOT NULL COMMENT '响应头',
 * `costMillis` int(11) NOT NULL DEFAULT '0' COMMENT '耗时ms',
 * `exp` text NOT NULL COMMENT '异常',
 * `createDate` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
 * PRIMARY KEY (`id`),
 * KEY `idx_user` (`loginUser`),
 * KEY `idx_url` (`url`)
 * ) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='请求日志表';
 */