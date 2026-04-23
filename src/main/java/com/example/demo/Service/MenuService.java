package com.example.demo.Service;

import com.example.demo.DTO.MenuExportDTO;
import com.example.demo.Entity.Menu;
import com.example.demo.Repository.MenuRepository;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.persistence.criteria.Predicate;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class MenuService {
    private final MenuRepository menuRepository;

    public MenuService(MenuRepository menuRepository) {
        this.menuRepository = menuRepository;
    }

    /**
     * 添加新菜品（默认状态为上架，status=1）
     *
     * @param menu 菜品实体（需包含名称、价格、分类等信息，id由数据库自动生成）
     * @return 保存后的菜品对象（包含生成的id和创建时间）
     */
    @Schema(description = "添加菜品")
    public Menu addMenu(Menu menu){
        menu.setStatus(1);
        return menuRepository.save(menu);
    }

    /**
     * 修改菜品信息（仅更新传入的非空字段）
     *
     * @param id   菜品ID
     * @param menu 包含待修改字段的菜品对象（支持名称、价格、分类、描述、图片、状态）
     * @return 修改后的菜品对象
     * @throws RuntimeException 如果指定ID的菜品不存在
     */
    @Schema(description = "修改菜品")
    public Menu updateMenu(Long id, Menu menu) {
        Menu exist = menuRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("菜品不存在"));
        if(menu.getName() != null) exist.setName(menu.getName());
        if(menu.getPrice() != null) exist.setPrice(menu.getPrice());
        if(menu.getCategory() != null) exist.setCategory(menu.getCategory());
        if(menu.getDescription() != null) exist.setDescription(menu.getDescription());
        if(menu.getImageUrl() != null) exist.setImageUrl(menu.getImageUrl());
        if(menu.getStatus() != null) exist.setStatus(menu.getStatus());
        return menuRepository.save(exist);
    }

    /**
     * 根据ID删除菜品（物理删除）
     *
     * @param id 菜品ID
     * @throws RuntimeException 如果菜品不存在
     */
    @Schema(description = "删除菜品")
    public void deleteMenu(Long id) {
        if(!menuRepository.existsById(id)) {
            throw new RuntimeException("菜品不存在");
        }
        menuRepository.deleteById(id);
    }

    /**
     * 查询单个菜品详情
     *
     * @param id 菜品ID
     * @return 菜品实体对象
     * @throws RuntimeException 如果菜品不存在
     */
    @Schema(description = "查询单个菜品")
    public Menu getMenu(Long id) {
        return menuRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("菜品不存在"));
    }

    /**
     * 查询所有上架菜品（status=1）
     *
     * @return 上架菜品列表，按创建时间或其他默认排序（由Repository定义）
     */
    @Schema(description = "查询全部上架菜品")
    public List<Menu> getAllAvailable() {
        return menuRepository.findByStatus(1);
    }

    /**
     * 根据分类查询该分类下的所有上架菜品
     *
     * @param category 菜品分类（如 "主食"、"饮品"）
     * @return 该分类下状态为上架的菜品列表
     */
    @Schema(description = "按分类查询上架菜品")
    public List<Menu> getByCategory(String category) {
        return menuRepository.findByCategoryAndStatus(category, 1);
    }

    /**
     * 后台管理：分页查询菜品，支持按名称模糊搜索、分类精确匹配、状态筛选
     *
     * @param name     菜品名称（模糊匹配，可选）
     * @param category 菜品分类（精确匹配，可选）
     * @param status   状态（1上架/0下架，可选）
     * @param pageNum  页码（从1开始，默认1）
     * @param pageSize 每页条数（默认10，最大100）
     * @return 分页后的菜品数据，按ID升序排列
     */
    @Schema(description = "分页查询")
    public Page<Menu> getAdminMenuPage(String name, String category, Integer status, Integer pageNum, Integer pageSize) {
        int currentPage = (pageNum == null || pageNum < 1) ? 1 : pageNum;
        int size = (pageSize == null || pageSize < 1) ? 10 : Math.min(pageSize, 100);
        Pageable pageable = PageRequest.of(currentPage - 1, size, Sort.by(Sort.Direction.ASC, "id"));

        Specification<Menu> spec = (root, query, cb) -> {
            List<Predicate> predicates = new ArrayList<>();
            if (StringUtils.hasText(name)) {
                predicates.add(cb.like(root.get("name"), "%" + name + "%"));
            }
            if (StringUtils.hasText(category)) {
                predicates.add(cb.equal(root.get("category"), category));
            }
            if (status != null) {
                predicates.add(cb.equal(root.get("status"), status));
            }
            return cb.and(predicates.toArray(new Predicate[0]));
        };
        return menuRepository.findAll(spec, pageable);
    }

    /**
     * 导出符合条件的菜品数据（用于Excel等导出功能）
     *
     * @param name     菜品名称（模糊匹配，可选）
     * @param category 菜品分类（精确匹配，可选）
     * @param status   状态（1上架/0下架，可选）
     * @return 菜品导出DTO列表，包含状态文本（上架/下架）和格式化创建时间
     */
    @Schema(description = "导出菜单")
    public List<MenuExportDTO> getMenusForExport(String name, String category, Integer status) {
        // 复用已有的查询逻辑（不分页）
        Specification<Menu> spec = (root, query, cb) -> {
            List<Predicate> predicates = new ArrayList<>();
            if (StringUtils.hasText(name)) {
                predicates.add(cb.like(root.get("name"), "%" + name + "%"));
            }
            if (StringUtils.hasText(category)) {
                predicates.add(cb.equal(root.get("category"), category));
            }
            if (status != null) {
                predicates.add(cb.equal(root.get("status"), status));
            }
            return cb.and(predicates.toArray(new Predicate[0]));
        };
        List<Menu> menus = menuRepository.findAll(spec, Sort.by(Sort.Direction.DESC, "createTime"));
        return menus.stream().map(this::convertToExportDTO).collect(Collectors.toList());
    }

    /**
     * 将菜品实体转换为导出用的DTO
     *
     * @param menu 菜品实体
     * @return 菜品导出DTO，包含状态中文描述（上架/下架）和格式化创建时间
     */
    private MenuExportDTO convertToExportDTO(Menu menu) {
        MenuExportDTO dto = new MenuExportDTO();
        dto.setId(menu.getId());
        dto.setName(menu.getName());
        dto.setCategory(menu.getCategory());
        dto.setPrice(menu.getPrice());
        dto.setStatusText(menu.getStatus() == 1 ? "上架" : "下架");
        dto.setCreateTime(menu.getCreateTime().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")));
        return dto;
    }
}
