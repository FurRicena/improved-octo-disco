package com.example.demo.Controller;

import com.example.demo.Annotation.Log;
import com.example.demo.Common.Result;
import com.example.demo.DTO.MenuExportDTO;
import com.example.demo.Entity.Menu;
import com.example.demo.Service.MenuService;
import com.example.demo.Utils.ExcelExporter;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.data.domain.Page;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;
import java.io.PrintWriter;
import java.util.List;

@Tag(name = "菜品接口")
@RestController
@RequestMapping("/menu")
@PreAuthorize("isAuthenticated()")
public class MenuController {
    private final MenuService menuService;

    public MenuController(MenuService menuService) {
        this.menuService = menuService;
    }

    /**
     * 新增菜品（仅限管理员）
     * <p>菜品默认状态为上架（status=1）。</p>
     *
     * @param menu 菜品信息（名称、价格、分类、描述、图片等）
     * @return 包含保存后菜品对象的统一响应结果
     */
    @Operation(summary = "新增菜品")
    @PostMapping("/addMenu")
    @PreAuthorize("hasAuthority('ADMIN')")
    @Log("新增菜品")
    public Result<Menu> addMenu(@RequestBody Menu menu){
        return Result.success(menuService.addMenu(menu));
    }

    /**
     * 修改菜品信息（仅限管理员）
     * <p>仅更新请求体中非空字段。</p>
     *
     * @param id   菜品ID
     * @param menu 包含待修改字段的菜品对象
     * @return 包含更新后菜品对象的统一响应结果
     */
    @Operation(summary = "修改菜品")
    @PutMapping("/{id}")
    @PreAuthorize("hasAuthority('ADMIN')")
    @Log("修改菜品")
    public Result<Menu> updateMenu(@PathVariable Long id, @RequestBody Menu menu){
        return Result.success(menuService.updateMenu(id, menu));
    }

    /**
     * 删除菜品（仅限管理员）
     *
     * @param id 菜品ID
     * @return 操作成功的统一响应结果（data 为 null）
     */
    @Operation(summary = "删除菜品")
    @DeleteMapping("/{id}")
    @PreAuthorize("hasAuthority('ADMIN')")
    @Log("删除菜品")
    public Result<?> deleteMenu(@PathVariable Long id){
        menuService.deleteMenu(id);
        return Result.success(null);
    }

    /**
     * 查询单个菜品详情（公开接口）
     *
     * @param id 菜品ID
     * @return 包含菜品对象的统一响应结果
     */
    @Operation(summary = "查询单个菜品")
    @GetMapping("/{id}")
    public Result<Menu> getMenu(@PathVariable Long id){
        return Result.success(menuService.getMenu(id));
    }

    /**
     * 查询所有已上架菜品（公开接口）
     *
     * @return 包含上架菜品列表的统一响应结果
     */
    @Operation(summary = "查询所有上架菜品")
    @GetMapping("/available")
    public Result<List<Menu>> getAllAvailable(){
        return Result.success(menuService.getAllAvailable());
    }

    /**
     * 按分类查询已上架菜品（公开接口）
     *
     * @param category 菜品类别（凉菜、热菜、汤羹、主食、饮品）
     * @return 包含该分类下上架菜品列表的统一响应结果
     */
    @Operation(summary = "按分类查询")
    @GetMapping("/category/{category}")
    public Result<List<Menu>> getByCategory(@PathVariable String category){
        return Result.success(menuService.getByCategory(category));
    }

    /**
     * 分页查询菜品（后台管理用，支持多条件过滤）
     *
     * @param name     菜品名称（模糊匹配，可选）
     * @param category 菜品类别（精确匹配，可选）
     * @param status   菜品状态（1-上架，0-下架，可选）
     * @param pageNum  页码（默认1）
     * @param pageSize 每页条数（默认10）
     * @return 包含分页菜品数据的统一响应结果
     */
    @Operation(summary = "分页查询")
    @GetMapping("/page")
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

    /**
     * 导出菜品数据至Excel（仅限管理员）
     * <p>支持按名称、分类、状态过滤，导出符合条件的全部菜品（不分页）。</p>
     * <p>导出成功时直接输出Excel文件流；失败时返回500错误及JSON格式错误信息。</p>
     *
     * @param name     菜品名称（模糊匹配，可选）
     * @param category 菜品类别（可选）
     * @param status   菜品状态（1-上架，0-下架，可选）
     * @param response HTTP响应对象，用于写入导出文件或错误信息
     */
    @GetMapping("/export")
    @PreAuthorize("hasAuthority('ADMIN')")
    public void exportMenus(@RequestParam(required = false) String name,
                            @RequestParam(required = false) String category,
                            @RequestParam(required = false) Integer status,
                            HttpServletResponse response) {
        try {
            System.out.println("导出菜品参数：name=" + name + ", category=" + category + ", status=" + status);
            List<MenuExportDTO> exportData = menuService.getMenusForExport(name, category, status);

            String[] fieldNames = {"id", "name", "category", "price", "statusText", "createTime"};
            String[] columnHeaders = {"菜品ID", "菜品名称", "类别", "价格(元)", "状态", "创建时间"};

            ExcelExporter.exportToExcel(exportData, "Menus", columnHeaders, fieldNames, response);
        } catch (Exception e) {
            e.printStackTrace();
            response.reset();
            response.setContentType("application/json;charset=UTF-8");
            response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            String errorJson = String.format("{\"code\":500,\"msg\":\"导出失败: %s\"}", e.getMessage());
            try (PrintWriter writer = response.getWriter()) {
                writer.write(errorJson);
                writer.flush();
            } catch (IOException ioException) {
                ioException.printStackTrace();
            }
        }
    }
}
