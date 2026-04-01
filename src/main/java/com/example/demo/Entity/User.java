//package com.example.demo.Entity;
//
//import io.swagger.v3.oas.annotations.media.Schema;
//import jakarta.persistence.*;
//import lombok.Data;
//
//@Entity
//@Data
//@Schema(description = "用户实体")
//public class User {
//
//    @Id
//    @GeneratedValue(strategy = GenerationType.IDENTITY)
//    @Schema(description = "用户id")
//    private Long id;
//
//    @Schema(description = "用户名")
//    private String username;
//
//    @Schema(description = "用户密码")
//    private String password;
//}
package com.example.demo.Entity;

import com.example.demo.Enums.UserRole;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.persistence.*;
import lombok.Data;

import java.time.LocalDateTime;

@Entity
@Data
@Schema(description = "用户实体")
public class User {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Schema(description = "用户id")
    private Long id;

    @Schema(description = "用户姓名")
    private String username;

    @Schema(description = "用户密码")
    private String password;

    @Schema(description = "用户角色")
    @Enumerated(EnumType.STRING)
    private UserRole role = UserRole.USER; // 'USER' 或 'ADMIN'

    @Schema(description = "创建时间")
    private LocalDateTime createTime = LocalDateTime.now();
}