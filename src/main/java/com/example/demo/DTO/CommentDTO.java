package com.example.demo.DTO;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class CommentDTO {
    private Long id;
    private Long menuId;
    private String menuName;
    private Long userId;
    private String username;
    private Integer rating;
    private String content;
    private Integer status;
    private LocalDateTime createTime;
}