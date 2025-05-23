package cn.beinet.codegenerate.linkinfo.service.entity;

import lombok.Data;
import lombok.experimental.Accessors;

import java.time.LocalDateTime;

/**
 * 新类
 *
 * @author youbl
 * @since 2023/8/15 15:46
 */
@Data
@Accessors(chain = true)
public class LinkInfo {
    private int id;
    private String link_type;// 类型：mysql/redis/nacos
    private String name;// 说明
    private String address;// IP或域名、URL信息
    private String account;// 登录账号
    private String pwd;// 登录密码
    private int port;// 连接端口信息
    private String info;// json结构，其它连接信息
    /**
     * 0正常；1危险要验证
     */
    private Integer type;

    private LocalDateTime create_time;
    private LocalDateTime update_time;
}
