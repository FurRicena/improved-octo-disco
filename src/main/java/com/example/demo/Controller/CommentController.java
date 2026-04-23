package com.example.demo.Controller;

import com.example.demo.Annotation.Log;
import com.example.demo.Common.Result;
import com.example.demo.DTO.CommentDTO;
import com.example.demo.DTO.Request.CommentAddRequest;
import com.example.demo.Entity.Comment;
import com.example.demo.Security.UserDetailsImpl;
import com.example.demo.Service.CommentService;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.data.domain.Page;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

@Tag(name = "评论接口")
@RestController
@RequestMapping("/comment")
@PreAuthorize("isAuthenticated()")
public class CommentController {

    private final CommentService commentService;

    public CommentController(CommentService commentService) {
        this.commentService = commentService;
    }

    // 用户发表评论（需要登录）
    @PostMapping("/add")
    @Log("发表评论")
    public Result<CommentDTO> addComment(@RequestBody CommentAddRequest request,
                                         Authentication authentication) {
        Long userId = getUserIdFromAuth(authentication);
        CommentDTO comment = commentService.addComment(userId, request.getMenuId(), request.getRating(), request.getContent());
        return Result.success(comment);
    }

    // 获取某个菜品的评论（公开）
    @GetMapping("/menu/{menuId}")
    public Result<Page<CommentDTO>> getCommentsByMenu(@PathVariable Long menuId,
                                                      @RequestParam(defaultValue = "1") int page,
                                                      @RequestParam(defaultValue = "10") int size) {
        Page<CommentDTO> comments = commentService.getCommentsByMenu(menuId, page, size);
        return Result.success(comments);
    }

    // 获取菜品平均评分
    @GetMapping("/menu/{menuId}/rating")
    public Result<Double> getAverageRating(@PathVariable Long menuId) {
        Double rating = commentService.getAverageRating(menuId);
        return Result.success(rating);
    }

    private Long getUserIdFromAuth(Authentication authentication) {
        // 1. 防御性检查
        if (authentication == null) {
            return null;
        }

        // 2. 获取 principal（认证主体）
        Object principal = authentication.getPrincipal();

        // 3. 类型判断：如果是匿名用户或其它类型，返回 null
        if (principal instanceof UserDetailsImpl) {
            // 4. 强转并调用 getId()（Lombok @Data 自动生成）
            return ((UserDetailsImpl) principal).getId();
        }

        // 5. 其他情况（如 anonymousUser 字符串、JWT 的 principal 等）返回 null
        return null;
    }

    @GetMapping("/list")
    @PreAuthorize("hasAuthority('ADMIN')")
    public Result<Page<CommentDTO>> listComments(@RequestParam(defaultValue = "1") int page,
                                                 @RequestParam(defaultValue = "10") int size) {
        return Result.success(commentService.getAllCommentsForAdmin(page, size));
    }

    @PutMapping("/{commentId}/status")
    @PreAuthorize("hasAuthority('ADMIN')")
    @Log("修改评论状态")
    public Result<Comment> updateStatus(@PathVariable Long commentId, @RequestParam Integer status) {
        return Result.success(commentService.updateCommentStatus(commentId, status));
    }

    @DeleteMapping("/{commentId}")
    @PreAuthorize("hasAuthority('ADMIN')")
    @Log("删除评论")
    public Result<?> deleteComment(@PathVariable Long commentId) {
        commentService.deleteComment(commentId);
        return Result.success(null);
    }
}
