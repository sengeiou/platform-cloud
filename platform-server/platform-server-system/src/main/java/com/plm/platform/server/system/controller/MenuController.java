package com.plm.platform.server.system.controller;

import com.plm.platform.common.core.entity.PlatformResponse;
import com.plm.platform.common.core.entity.constant.StringConstant;
import com.plm.platform.common.core.entity.router.VueRouter;
import com.plm.platform.common.core.entity.system.Menu;
import com.plm.platform.server.system.annotation.ControllerEndpoint;
import com.plm.platform.server.system.service.IMenuService;
import com.wuwenze.poi.ExcelKit;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletResponse;
import javax.validation.Valid;
import javax.validation.constraints.NotBlank;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @author crystal
 */
@Slf4j
@Validated
@RestController
@RequiredArgsConstructor
@RequestMapping("/menu")
public class MenuController {

    private final IMenuService menuService;

    @GetMapping("/{username}")
    public PlatformResponse getUserRouters(@NotBlank(message = "{required}") @PathVariable String username) {
        Map<String, Object> result = new HashMap<>(2);
        List<VueRouter<Menu>> userRouters = this.menuService.getUserRouters(username);
        String userPermissions = this.menuService.findUserPermissions(username);
        String[] permissionArray = new String[0];
        if (StringUtils.isNoneBlank(userPermissions)) {
            permissionArray = StringUtils.splitByWholeSeparatorPreserveAllTokens(userPermissions, StringConstant.COMMA);
        }
        result.put("routes", userRouters);
        result.put("permissions", permissionArray);
        return new PlatformResponse().data(result);
    }

    @GetMapping
    public PlatformResponse menuList(Menu menu) {
        Map<String, Object> menus = this.menuService.findMenus(menu);
        return new PlatformResponse().data(menus);
    }

    @GetMapping("/permissions")
    public String findUserPermissions(String username) {
        return this.menuService.findUserPermissions(username);
    }

    @PostMapping
    @PreAuthorize("hasAuthority('menu:add')")
    @ControllerEndpoint(operation = "新增菜单/按钮", exceptionMessage = "新增菜单/按钮失败")
    public void addMenu(@Valid Menu menu) {
        this.menuService.createMenu(menu);
    }

    @DeleteMapping("/{menuIds}")
    @PreAuthorize("hasAuthority('menu:delete')")
    @ControllerEndpoint(operation = "删除菜单/按钮", exceptionMessage = "删除菜单/按钮失败")
    public void deleteMenus(@NotBlank(message = "{required}") @PathVariable String menuIds) {
        String[] ids = menuIds.split(StringConstant.COMMA);
        this.menuService.deleteMeuns(ids);
    }

    @PutMapping
    @PreAuthorize("hasAuthority('menu:update')")
    @ControllerEndpoint(operation = "修改菜单/按钮", exceptionMessage = "修改菜单/按钮失败")
    public void updateMenu(@Valid Menu menu) {
        this.menuService.updateMenu(menu);
    }

    @PostMapping("excel")
    @PreAuthorize("hasAuthority('menu:export')")
    @ControllerEndpoint(operation = "导出菜单数据", exceptionMessage = "导出Excel失败")
    public void export(Menu menu, HttpServletResponse response) {
        List<Menu> menus = this.menuService.findMenuList(menu);
        ExcelKit.$Export(Menu.class, response).downXlsx(menus, false);
    }
}