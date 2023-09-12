package cn.beinet.codegenerate.menu;

import cn.beinet.codegenerate.menu.dto.MenuGroupDto;
import cn.beinet.codegenerate.menu.service.MenuService;
import lombok.RequiredArgsConstructor;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * 菜单读取控制器
 *
 * @author youbl
 * @since 2023/8/17 13:45
 */
@RestController
@RequiredArgsConstructor
public class MenuController {
    private final MenuService menuService;

    /**
     * 获取指定SaaS ID下，所有的菜单树返回
     *
     * @param saasId SaaS ID，默认值hub
     * @return 菜单树
     */
    @GetMapping("/menuGroup")
    public List<MenuGroupDto> findAllForShow(@RequestParam(required = false) String saasId) {
        if (!StringUtils.hasLength(saasId)) {
            saasId = "hub";
        }
        return menuService.selectGroupWithMenus(saasId);
    }
}
