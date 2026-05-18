package com.example.demo.DTO.Responce;

import com.example.demo.Entity.Menu;
import lombok.Data;

import java.util.List;

@Data
public class AiRecommendResponse {
    private List<Menu> menus;      // 推荐的菜品列表
    private String speakText;      // 自然语言推荐语，如：“亲，根据您的要求，我推荐清炒西兰花和鸡胸肉沙拉哦～”
}
