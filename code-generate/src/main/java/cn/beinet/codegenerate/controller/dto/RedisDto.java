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
public class RedisDto {
    private String name;
    private String ip;
    private int port;
    private String pwd;
    private int db;
    private String cmd;
}
