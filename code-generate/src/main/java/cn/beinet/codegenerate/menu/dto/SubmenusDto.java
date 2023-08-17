package cn.beinet.codegenerate.menu.dto;

import lombok.Data;
import lombok.experimental.Accessors;
import org.springframework.format.annotation.DateTimeFormat;

/**
 * @author youbl.blog.csdn.net
 * @date 2023-04-24 16:26:23
 */
@Data
@Accessors(chain = true)
public class SubmenusDto {

    private Integer id;

    private Integer menuId;

    private String url;

    private String title;

    private String openMode;

    private Integer show;

    private Integer sort;

    @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private java.time.LocalDateTime createDate;

    @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private java.time.LocalDateTime updateDate;
}