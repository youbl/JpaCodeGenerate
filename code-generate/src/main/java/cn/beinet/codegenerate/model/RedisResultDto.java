package cn.beinet.codegenerate.model;

import lombok.Data;

/**
 * Description:
 *
 * @author : youbl
 * @create: 2021/12/13 16:11
 */
@Data
public class RedisResultDto {
    private String result = "";
    private String type;
    private Long ttl;
}
