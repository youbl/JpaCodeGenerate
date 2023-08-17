package cn.beinet.codegenerate.menu.dto;

import lombok.Data;
import lombok.experimental.Accessors;
import org.springframework.format.annotation.DateTimeFormat;

import java.util.List;

/**
 * @author youbl.blog.csdn.net
 * @date 2023-04-24 16:26:23
 */
@Data
@Accessors(chain = true)
public class MenuGroupDto {

    private Integer id;

    private String title;

    private String url;

    private String openMode;

    private Integer show;

    private Integer sort;

    @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private java.time.LocalDateTime createDate;

    @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private java.time.LocalDateTime updateDate;

    /**
     * 当前分组下的所有菜单
     */
    private List<MenusDto> menus;
}