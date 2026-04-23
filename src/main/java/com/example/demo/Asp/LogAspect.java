package com.example.demo.Asp;

import com.example.demo.Annotation.Log;
import com.example.demo.Entity.OperationLog;
import com.example.demo.Repository.OperationLogRepository;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.http.HttpServletRequest;
import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.annotation.AfterReturning;
import org.aspectj.lang.annotation.Aspect;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;

import java.util.Arrays;

@Aspect
@Component
public class LogAspect {

    private static final Logger logger = LoggerFactory.getLogger(LogAspect.class);
    private final OperationLogRepository logRepository;
    private final HttpServletRequest request;
    private final ObjectMapper objectMapper = new ObjectMapper();

    public LogAspect(OperationLogRepository logRepository, HttpServletRequest request) {
        this.logRepository = logRepository;
        this.request = request;
    }

    /**
     * 方法执行成功后的日志记录（后置通知）
     * <p>当被 @Log 注解标记的方法正常返回时，自动记录操作日志，包含操作人、操作描述、目标对象、请求参数、IP地址等信息。</p>
     *
     * @param joinPoint 连接点对象，包含目标方法签名、参数等信息
     * @param log       方法上的 @Log 注解实例，用于获取操作描述
     * @param result    目标方法的返回值（本实现中未直接使用，但保留用于扩展）
     */
    @AfterReturning(pointcut = "@annotation(log)", returning = "result")
    public void logAfter(JoinPoint joinPoint, Log log, Object result) {
        try {
            String username = getCurrentUsername();
            String operation = log.value();
            String target = getTarget(joinPoint);
            String detail = getDetail(joinPoint);
            String ip = request.getRemoteAddr();

            OperationLog entity = new OperationLog();
            entity.setOperator(username);
            entity.setOperation(operation);
            entity.setTarget(target);
            entity.setDetail(detail);
            entity.setIp(ip);
            logRepository.save(entity);
        } catch (Exception e) {
            logger.error("记录操作日志失败，方法: {}", joinPoint.getSignature().getName(), e);
        }
    }

    /**
     * 获取当前登录用户的用户名
     * <p>从 Spring Security 的 SecurityContextHolder 中获取当前认证用户的名称；如果未认证（如系统内部调用），则返回 "system"。</p>
     *
     * @return 用户名（认证用户）或 "system"
     */
    private String getCurrentUsername() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        return auth != null ? auth.getName() : "system";
    }

    /**
     * 获取操作的目标对象（通常为方法的第一个参数）
     * <p>用于日志中记录被操作的目标对象描述。如果方法无参数，则返回方法名；否则返回第一个参数的字符串表示。</p>
     *
     * @param joinPoint 连接点对象，包含目标方法的参数列表
     * @return 目标对象的字符串描述（参数0的 toString() 或方法名）
     */
    private String getTarget(JoinPoint joinPoint) {
        Object[] args = joinPoint.getArgs();
        if (args == null || args.length == 0) {
            return joinPoint.getSignature().getName();  // 返回方法名
        }
        Object arg = args[0];
        return arg != null ? arg.toString() : "";
    }

    /**
     * 获取操作详情（完整请求参数的 JSON 或字符串表示）
     * <p>将目标方法的所有参数序列化为 JSON 字符串；若序列化失败，则使用 Arrays.toString() 作为备选。</p>
     *
     * @param joinPoint 连接点对象，包含目标方法的参数列表
     * @return 参数的字符串表示（JSON 或数组字符串）
     */
    private String getDetail(JoinPoint joinPoint) {
        Object[] args = joinPoint.getArgs();
        if (args == null || args.length == 0) {
            return "无参数";
        }
        try {
            return objectMapper.writeValueAsString(args);
        } catch (JsonProcessingException e) {
            return Arrays.toString(args);
        }
    }
}
