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
public class NacosDto {
    // 配置名
    private String name;

    private String url;
    private String user;
    private String pwd;
    private String nameSpace;
    private String dataId;
    private String group;
}
