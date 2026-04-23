package com.example.demo.Entity;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.persistence.*;
import lombok.Data;

import java.time.LocalDateTime;

@Entity
@Schema(description = "comment")
@Data
public class Comment {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Schema(description = "评论Id")
    private Long id;

    @Schema(description = "菜品Id")
    private Long menuId;

    @Schema(description = "用户Id")
    private Long userId;

    @Schema(description = "评分")
    private Integer rating;

    @Schema(description = "评分内容")
    private String content;

    @Schema(description = "显示状态")
    private Integer status; // 1-显示 0-隐藏

    @Schema(description = "评论时间")
    private LocalDateTime createTime;

    // 关联查询需要的字段（非持久化）
    @Transient
    private String username;
    @Transient
    private String menuName;
}