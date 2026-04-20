package com.example.demo.Controller;

import com.example.demo.Common.Result;
import com.example.demo.Entity.Menu;
import com.example.demo.Service.MenuService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.data.domain.Page;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Tag(name = "菜品接口")
@RestController
@RequestMapping("/menu")
public class MenuController {
    private final MenuService menuService;

    public MenuController(MenuService menuService) {
        this.menuService = menuService;
    }

    @Operation(summary = "新增菜品")
    @PostMapping("/addMenu")
    public Result<Menu> addMenu(@RequestBody Menu menu){
        return Result.success(menuService.addMenu(menu));
    }

    @Operation(summary = "修改菜品")
    @PutMapping("/{id}")
    public Result<Menu> updateMenu(@PathVariable Long id, @RequestBody Menu menu){
        return Result.success(menuService.updateMenu(id, menu));
    }

    @Operation(summary = "删除菜品")
    @DeleteMapping("/{id}")
    public Result<?> deleteMenu(@PathVariable Long id){
        menuService.deleteMenu(id);
        return Result.success(null);
    }

    @Operation(summary = "查询单个菜品")
    @GetMapping("/{id}")
    public Result<Menu> getMenu(@PathVariable Long id){
        return Result.success(menuService.getMenu(id));
    }

    @Operation(summary = "查询所有上架菜品")
    @GetMapping("/available")
    public Result<List<Menu>> getAllAvailable(){
        return Result.success(menuService.getAllAvailable());
    }

    @Operation(summary = "按分类查询")
    @GetMapping("/category/{category}")
    public Result<List<Menu>> getByCategory(@PathVariable String category){
        return Result.success(menuService.getByCategory(category));
    }

    @Operation(summary = "分页查询")
    @GetMapping("/page")
//    @PreAuthorize("hasRole('ADMIN')")
    public Result<Page<Menu>> getAdminMenuPage(
            @RequestParam(required = false) String name,
            @RequestParam(required = false) String category,
            @RequestParam(required = false) Integer status,
            @RequestParam(defaultValue = "1") Integer pageNum,
            @RequestParam(defaultValue = "10") Integer pageSize
    ) {
        Page<Menu> page = menuService.getAdminMenuPage(name, category, status, pageNum, pageSize);
        return Result.success(page);
    }
}
