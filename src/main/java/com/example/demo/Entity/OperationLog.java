package com.example.demo.Entity;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.persistence.*;
import lombok.Data;
import java.time.LocalDateTime;

@Entity
@Schema(description = "日志实体")
@Data
public class OperationLog {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private String operator;      // 操作人（用户名）
    private String operation;     // 操作类型（如：新增菜品、删除用户）
    private String target;        // 操作对象（如：菜品ID、用户名）
    private String detail;        // 详情（JSON或描述）
    private String ip;            // 操作IP
    private LocalDateTime createTime = LocalDateTime.now();
}
