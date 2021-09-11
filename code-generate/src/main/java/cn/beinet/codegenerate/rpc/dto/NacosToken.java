package cn.beinet.codegenerate.rpc.dto;

import lombok.Data;

/**
 * 调nacos登录接口的返回结果
 */
@Data
public class NacosToken {
    private String accessToken;
    private int tokenTtl;
    private boolean globalAdmin;
}
