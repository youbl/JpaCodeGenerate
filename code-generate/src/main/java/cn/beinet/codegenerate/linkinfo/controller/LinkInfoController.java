package cn.beinet.codegenerate.linkinfo.controller;

import cn.beinet.codegenerate.linkinfo.controller.dto.LinkInfoDto;
import cn.beinet.codegenerate.linkinfo.service.LinkInfoService;
import lombok.RequiredArgsConstructor;
import org.springframework.util.Assert;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 通用连接信息的接口，把所有工具所需的mysql、redis、nacos连接信息统一保存，避免前端泄露
 *
 * @author youbl
 * @since 2023/8/11 13:32
 */
@RestController
@RequiredArgsConstructor
@RequestMapping("linkinfo")
public class LinkInfoController {
    private final LinkInfoService linkInfoService;

    /**
     * 返回指定类型的连接信息
     *
     * @return 列表
     */
    @GetMapping("list")
    public List<LinkInfoDto> getCode(@RequestParam String type) {
        return linkInfoService.getLinkInfoDto(type);
    }

    /**
     * 保存一条连接信息
     *
     * @param dto 记录
     * @return 影响行数
     */
    @PostMapping("")
    public int saveCode(@RequestBody LinkInfoDto dto) {
        Assert.notNull(dto, "信息不能为空");
        return linkInfoService.saveInfo(dto);
    }
}
