package com.example.demo.Service;

import com.example.demo.Entity.Menu;
import com.example.demo.Repository.MenuRepository;
import io.swagger.v3.oas.annotations.media.Schema;
import org.springframework.stereotype.Service;

import java.util.List;

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
}
