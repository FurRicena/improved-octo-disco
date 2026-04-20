package com.example.demo.Service;
import org.springframework.ai.chat.client.ChatClient;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.stereotype.Service;

@Service
public class AIService {
    private final ChatClient chatClient;
    private final ObjectMapper objectMapper = new ObjectMapper();

    public AIService(ChatClient.Builder builder) {
        this.chatClient = builder.build();
    }

    // 解析用户输入，返回查询参数
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

    // 简单的参数封装类
    public static class QueryParams {
        public String keywords = "";
        public String category = "";
        public Double maxPrice = null;
        public Double minPrice = null;
        public String flavor = "";
        public int limit = 3;
    }
}