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

    @Schema(description = "添加菜品")
    public Menu addMenu(Menu menu){
        menu.setStatus(1);
        return menuRepository.save(menu);
    }

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

    @Schema(description = "删除菜品")
    public void deleteMenu(Long id) {
        if(!menuRepository.existsById(id)) {
            throw new RuntimeException("菜品不存在");
        }
        menuRepository.deleteById(id);
    }

    @Schema(description = "查询单个菜品")
    public Menu getMenu(Long id) {
        return menuRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("菜品不存在"));
    }

    @Schema(description = "查询全部上架菜品")
    public List<Menu> getAllAvailable() {
        return menuRepository.findByStatus(1);
    }

    @Schema(description = "按分类查询上架菜品")
    public List<Menu> getByCategory(String category) {
        return menuRepository.findByCategoryAndStatus(category, 1);
    }

    // 分页查询
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


    // MenuService.java
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
