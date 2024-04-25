package cn.beinet.codegenerate.controller.dto;

import lombok.Data;
import lombok.experimental.Accessors;

/**
 * Description:
 *
 * @author : youbl
 * @create: 2021/11/23 14:20
 */
@Data
@Accessors(chain = true)
public class SqlDto {
    /**
     * 配置名
     */
    private String name;
    /**
     * 数据库IP或域名
     */
    private String ip;
    private int port;
    /**
     * 数据库账号
     */
    private String user;
    /**
     * 数据库密码
     */
    private String pwd;
    /**
     * 要连接的数据库db名
     */
    private String db;
    /**
     * 要执行的sql
     */
    private String sql;
    /**
     * 执行超时
     */
    private Integer dbTimeout;
    /**
     * 要操作的数据库表名
     */
    private String tableName;

    private int time;
}
