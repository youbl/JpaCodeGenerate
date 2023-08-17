package cn.beinet.codegenerate.menu;

import cn.beinet.codegenerate.menu.dto.MenuGroupDto;
import cn.beinet.codegenerate.menu.service.MenuService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
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

    @GetMapping("/menuGroup")
    public List<MenuGroupDto> findAllForShow() {
        return menuService.selectGroupWithMenus();
    }
}
