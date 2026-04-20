package com.example.demo.Controller;

import com.example.demo.Common.Result;
import com.example.demo.Entity.Menu;
import com.example.demo.Repository.MenuRepository;
import com.example.demo.Service.AIService;
import com.example.demo.Utils.MenuSpecifications;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/ai")
@PreAuthorize("isAuthenticated()")
public class AIController {

    private final AIService aiService;
    private final MenuRepository menuRepository;

    public AIController(AIService aiService, MenuRepository menuRepository) {
        this.aiService = aiService;
        this.menuRepository = menuRepository;
    }

    @PostMapping("/recommend")
    public Result<List<Menu>> aiRecommend(@RequestBody Map<String, String> request) {
        String userMessage = request.get("message");
        // 1. AI 解析用户意图
        AIService.QueryParams params = aiService.parseUserIntent(userMessage);
        // 2. 用解析出的参数查询数据库
        Specification<Menu> spec = MenuSpecifications.withParams(params);
        List<Menu> menus = menuRepository.findAll(spec);
        // 3. 限制返回数量
        if (menus.size() > params.limit) {
            menus = menus.subList(0, params.limit);
        }
        return Result.success(menus);
    }
}
