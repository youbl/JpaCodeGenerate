package cn.beinet.codegenerate.controller.execute.dto;

import lombok.Data;

/**
 * Description:
 *
 * @author : youbl
 * @create: 2021/11/23 14:20
 */
@Data
public class SqlDto {
    private String ip;
    private String user;
    private String pwd;
    private String db;
    private String sql;

    private int time;
}
