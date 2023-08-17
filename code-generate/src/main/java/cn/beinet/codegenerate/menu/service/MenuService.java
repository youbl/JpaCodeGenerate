package cn.beinet.codegenerate.menu.service;

import cn.beinet.codegenerate.menu.dto.MenuGroupDto;
import cn.beinet.codegenerate.menu.dto.MenusDto;
import cn.beinet.codegenerate.menu.dto.SubmenusDto;
import lombok.RequiredArgsConstructor;
import org.springframework.jdbc.core.BeanPropertyRowMapper;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 菜单读取服务类
 *
 * @author youbl
 * @since 2023/8/17 13:54
 */
@Service
@RequiredArgsConstructor
public class MenuService {
    private final JdbcTemplate jdbcTemplate;

    /**
     * 获取所有菜单组，并填充主菜单和子菜单返回
     *
     * @return 菜单列表
     */
    public List<MenuGroupDto> selectGroupWithMenus() {
        String sql = "SELECT * FROM admin_menu_group a WHERE a.show=1 ORDER BY a.sort, a.id";
        List<MenuGroupDto> ret = jdbcTemplate.query(sql, new Object[0], new BeanPropertyRowMapper<>(MenuGroupDto.class));
        if (ret == null)
            return new ArrayList<>();

        fillMainMenus(ret);
        return ret;
    }

    // 填充主菜单
    private void fillMainMenus(List<MenuGroupDto> data) {
        // 先按id进行分组，方便后面查找
        Map<Integer, MenuGroupDto> map = new HashMap<>();
        for (MenuGroupDto item : data) {
            map.put(item.getId(), item);
        }

        List<MenusDto> submenus = selectMainMenuWithSubMenus();
        for (MenusDto item : submenus) {
            MenuGroupDto parent = map.get(item.getGroupId());
            if (parent == null)
                continue; // 这个子菜单对应的父菜单没了，或隐藏了

            if (parent.getMenus() == null) {
                parent.setMenus(new ArrayList<>());
            }
            parent.getMenus().add(item);
        }
    }

    private List<MenusDto> selectMainMenuWithSubMenus() {
        String sql = "SELECT * FROM admin_menus a WHERE a.show=1 ORDER BY a.groupId,a.sort,a.id";
        List<MenusDto> ret = jdbcTemplate.query(sql, new Object[0], new BeanPropertyRowMapper<>(MenusDto.class));
        if (ret == null)
            return new ArrayList<>();

        fillSubMenus(ret);
        return ret;
    }

    // 填充子菜单
    private void fillSubMenus(List<MenusDto> data) {
        // 先按id进行分组，方便后面查找
        Map<Integer, MenusDto> map = new HashMap<>();
        for (MenusDto item : data) {
            map.put(item.getId(), item);
        }

        List<SubmenusDto> submenus = selectSubMenus();
        for (SubmenusDto item : submenus) {
            MenusDto parent = map.get(item.getMenuId());
            if (parent == null)
                continue; // 这个子菜单对应的父菜单没了，或隐藏了

            if (parent.getSubMenus() == null) {
                parent.setSubMenus(new ArrayList<>());
            }
            parent.getSubMenus().add(item);
        }
    }

    private List<SubmenusDto> selectSubMenus() {
        String sql = "SELECT * FROM admin_submenus a WHERE a.show=1 ORDER BY a.menuId,a.sort,a.id";
        List<SubmenusDto> ret = jdbcTemplate.query(sql, new Object[0], new BeanPropertyRowMapper<>(SubmenusDto.class));
        if (ret == null)
            return new ArrayList<>();
        return ret;
    }
}
