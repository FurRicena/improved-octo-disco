package com.example.demo.DTO;

import com.example.demo.Enums.UserRole;
import lombok.Data;

@Data
public class UserExportDTO {

    private Long id;
    private String username;
    private UserRole role;
    private String createTime;
}
