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
public class MenusDto {

    private Integer id;

    private Integer groupId;

    private String img;

    private String url;

    private String title;

    private String memo;

    private String openMode;

    private String popTitle;

    private Integer popWidth;

    private Integer show;

    private Integer sort;

    @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private java.time.LocalDateTime createDate;

    @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private java.time.LocalDateTime updateDate;


    /**
     * 当前菜单下的所有子菜单
     */
    private List<SubmenusDto> subMenus;
}