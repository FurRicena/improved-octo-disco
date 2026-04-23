package com.example.demo.DTO.Request;

import lombok.Data;

@Data
public class CommentAddRequest {

    private Long menuId;
    private Integer rating;
    private String content;
}
