package com.example.demo.Controller;

import com.example.demo.Annotation.Log;
import com.example.demo.Common.Result;
import com.example.demo.DTO.Responce.AiRecommendResponse;
import com.example.demo.Repository.MenuRepository;
import com.example.demo.Service.AIService;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import java.util.Map;

@RestController
@RequestMapping("/ai")
@PreAuthorize("isAuthenticated()")
public class AIController {

    private final AIService aiService;

    public AIController(AIService aiService, MenuRepository menuRepository) { this.aiService = aiService;}

    /**
     * 智能推荐菜品（基于 AI 自然语言解析）
     * <p>用户输入自然语言查询（如“推荐几个辣的凉菜，价格不超过50”），
     * 系统调用 AI 模型解析意图并提取查询参数，然后根据参数动态查询数据库，
     * 最后返回符合条件且数量不超过用户期望的菜品列表。</p>
     *
     * @param request 包含用户消息的 JSON 对象，格式为 {"message": "用户输入的文字"}
     * @return 推荐菜品列表，数量由 AI 解析的 limit 字段决定（默认最多3个）
     */
    @PostMapping("/recommend")
    @Log("向AI提问")
    public Result<AiRecommendResponse> aiRecommend(@RequestBody Map<String, String> request) {
        String userMessage = request.get("message");
        AiRecommendResponse response = aiService.getRecommendWithText(userMessage);
        return Result.success(response);
    }
}
