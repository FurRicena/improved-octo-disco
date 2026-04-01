package com.example.demo.Hander;

import com.example.demo.Common.Result;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(RuntimeException.class)
    public ResponseEntity<Result<?>> handleRuntimeException(RuntimeException e) {
        if ("用户不存在".equals(e.getMessage())) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(Result.error(404, e.getMessage()));
        }
        if ("用户名或密码错误".equals(e.getMessage())) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(Result.error(401, e.getMessage()));
        }
        if ("菜品不存在".equals(e.getMessage())) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(Result.error(404, e.getMessage()));
        }
        if ("非法状态变更".equals(e.getMessage())) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(Result.error(401, e.getMessage()));
        }
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(Result.error(500, e.getMessage()));
    }
}

