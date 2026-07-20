package com.example.demo.Service;
import com.example.demo.Entity.Menu;
import com.example.demo.Repository.MenuRepository;
import com.example.demo.Utils.MenuSpecifications;
import org.springframework.ai.chat.client.ChatClient;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import com.example.demo.DTO.Responce.AiRecommendResponse;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
public class AIService {
    private final ChatClient chatClient;
    private final ObjectMapper objectMapper = new ObjectMapper();
    private final MenuRepository menuRepository;

    public AIService(ChatClient.Builder builder, MenuRepository menuRepository) {
        this.chatClient = builder.build();
        this.menuRepository = menuRepository;
    }

    /**
     * 基于用户输入的自然语言，调用 AI 模型解析用户的点餐意图，提取查询参数
     * <p>
     * 支持的提取字段：菜品关键词、类别、价格区间、口味、推荐数量等。
     * 解析失败时返回默认参数（关键词为空、limit=3）。
     * </p>
     *
     * @param userMessage 用户输入的原始文本（例如：“推荐几个辣的凉菜，价格不超过50”）
     * @return QueryParams 对象，包含解析出的结构化查询参数
     */
    public QueryParams parseUserIntent(String userMessage) {
        String prompt = """
                你是一个点餐意图解析器。请把用户的话解析成以下JSON格式，只输出JSON，不要有其他文字：
                {
                    "keywords": "提取出的菜品名称关键词，没有就空字符串",
                    "category": "菜品类别，只能是：凉菜、热菜、汤羹、主食、饮品，无法判断就空字符串",
                    "maxPrice": 价格上限，没有就null,
                    "minPrice": 价格下限，没有就null,
                    "flavor": "口味偏好，如：辣、甜、酸等，没有就空字符串",
                    "limit": 推荐数量，用户说了“几个”就取数字，默认3
                }
                
                用户说：%s
                """.formatted(userMessage);

        String response = chatClient.prompt().user(prompt).call().content();
        // 解析 JSON
        try {
            JsonNode node = objectMapper.readTree(response);
            QueryParams params = new QueryParams();
            params.keywords = node.get("keywords").asText();
            params.category = node.get("category").asText();
            params.maxPrice = node.has("maxPrice") && !node.get("maxPrice").isNull() ? node.get("maxPrice").asDouble() : null;
            params.minPrice = node.has("minPrice") && !node.get("minPrice").isNull() ? node.get("minPrice").asDouble() : null;
            params.flavor = node.get("flavor").asText();
            params.limit = node.get("limit").asInt(3);
            return params;
        } catch (Exception e) {
            // 解析失败，返回默认值
            return new QueryParams();
        }
    }

    /**
     * 新增：根据用户输入，同时获取推荐的菜品列表和自然语言推荐语
     * @param userMessage 用户原始输入
     * @return AiRecommendResponse
     */
    public AiRecommendResponse getRecommendWithText(String userMessage) {
        // 1. 获取所有菜品的简要信息
        String menuContext = getAllMenusJson();
        // 2. 构建 Prompt，要求 AI 返回推荐菜品 ID 列表 + 推荐语
        String prompt = String.format("""
            你是一个餐厅的智能点餐助手。用户说：“%s”。
            以下是餐厅的全部菜品（id, 名称, 类别, 价格）：
            %s
            请根据用户的需求，推荐2-3个最合适的菜品，并生成一句亲切的推荐语（20字以内）。
            返回格式必须是严格的JSON，不要有其他任何文字：
            {
                "recommendIds": [菜品id数组],
                "speakText": "推荐语"
            }
            """, userMessage, menuContext);
        String response = chatClient.prompt().user(prompt).call().content();
        // 3. 解析 AI 返回的 JSON
        List<Long> recommendIds = new ArrayList<>();
        String speakText = "感谢您的点餐，祝您用餐愉快！";  // 默认推荐语
        try {
            JsonNode node = objectMapper.readTree(response);
            if (node.has("recommendIds")) {
                node.get("recommendIds").forEach(id -> recommendIds.add(id.asLong()));
            }
            if (node.has("speakText")) {
                speakText = node.get("speakText").asText();
            }
        } catch (Exception e) {
            // 解析失败，降级使用原有意图解析逻辑
            return fallbackRecommendation(userMessage);
        }
        // 4. 根据 ID 查询完整菜品
        List<Menu> menus = menuRepository.findAllById(recommendIds);
        if (menus.isEmpty()) {
            // 如果查询结果为空（例如 ID 无效），降级
            return fallbackRecommendation(userMessage);
        }
        // 5. 组装返回对象
        AiRecommendResponse result = new AiRecommendResponse();
        result.setMenus(menus);
        result.setSpeakText(speakText);
        return result;
    }

    /**
     * 降级方案：使用原有的 parseUserIntent 查询数据库，并生成默认推荐语
     */
    private AiRecommendResponse fallbackRecommendation(String userMessage) {
        QueryParams params = parseUserIntent(userMessage);
        Specification<Menu> spec = MenuSpecifications.withParams(params);
        List<Menu> menus = menuRepository.findAll(spec);
        if (menus.size() > params.limit) {
            menus = menus.subList(0, params.limit);
        }

        String defaultSpeakText;
        if (menus.isEmpty()) {
            defaultSpeakText = "抱歉，没有找到符合您要求的菜品，请尝试其他描述。";
        } else {
            String names = menus.stream().map(Menu::getName).collect(Collectors.joining("、"));
            defaultSpeakText = "为您推荐：" + names + "，希望您喜欢。";
        }

        AiRecommendResponse result = new AiRecommendResponse();
        result.setMenus(menus);
        result.setSpeakText(defaultSpeakText);
        return result;
    }

    /**
     * 获取所有菜品的 JSON 字符串（仅 id、name、category、price）
     */
    private String getAllMenusJson() {
        List<Menu> allMenus = menuRepository.findAll();
        List<Map<String, Object>> simpleList = allMenus.stream()
                .map(m -> {
                    Map<String, Object> map = new HashMap<>();
                    map.put("id", m.getId());
                    map.put("name", m.getName());
                    map.put("category", m.getCategory());
                    map.put("price", m.getPrice());
                    return map;
                })
                .collect(Collectors.toList());
        try {
            return objectMapper.writeValueAsString(simpleList);
        } catch (Exception e) {
            return "[]";
        }
    }


    /**
     * 点餐查询参数封装类，用于传递用户意图解析后的结构化条件
     * <p>
     * 字段说明：
     * <ul>
     *     <li>keywords - 菜品名称关键词（模糊匹配）</li>
     *     <li>category - 菜品类别（凉菜、热菜、汤羹、主食、饮品）</li>
     *     <li>maxPrice - 价格上限（null 表示不限制）</li>
     *     <li>minPrice - 价格下限（null 表示不限制）</li>
     *     <li>flavor - 口味偏好（辣、甜、酸等）</li>
     *     <li>limit - 推荐/返回的菜品数量（默认3）</li>
     * </ul>
     * </p>
     */
    public static class QueryParams {
        public String keywords = "";
        public String category = "";
        public Double maxPrice = null;
        public Double minPrice = null;
        public String flavor = "";
        public int limit = 3;
    }
}