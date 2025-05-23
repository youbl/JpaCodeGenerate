package cn.beinet.codegenerate.linkinfo.controller.dto;

import cn.beinet.codegenerate.linkinfo.service.entity.LinkInfo;
import lombok.Data;
import lombok.experimental.Accessors;

import java.time.LocalDateTime;

/**
 * 新类
 *
 * @author youbl
 * @since 2023/8/11 13:34
 */
@Data
@Accessors(chain = true)
public class LinkInfoDto {
    private int id;
    private String link_type;// 类型：mysql/redis/nacos
    private String name;// 说明
    private String address;// IP或域名、URL信息
    private String account;// 登录账号
    private String pwd;// 登录密码
    private int port;// 连接端口信息
    private String info;// json结构，其它连接信息
    private LocalDateTime create_time;
    private LocalDateTime update_time;
    /**
     * 0正常；1危险要验证
     */
    private Integer type;

    public static LinkInfoDto convertToDto(LinkInfo item, boolean fillPassword) {
        LinkInfoDto ret = new LinkInfoDto()
                .setAccount(item.getAccount())
                .setAddress(item.getAddress())
                .setCreate_time(item.getCreate_time())
                .setUpdate_time(item.getUpdate_time())
                .setId(item.getId())
                .setInfo(item.getInfo())
                .setLink_type(item.getLink_type())
                .setName(item.getName())
                .setPort(item.getPort())
                .setType(item.getType())
                .setPwd("");
        if (fillPassword) {
            ret.setPwd(item.getPwd());
        }
        return ret;
    }
}
