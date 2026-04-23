package com.example.demo.Service;

import com.example.demo.DTO.CommentDTO;
import com.example.demo.Entity.Comment;
import com.example.demo.Entity.Menu;
import com.example.demo.Entity.User;
import com.example.demo.Repository.CommentRepository;
import com.example.demo.Repository.MenuRepository;
import com.example.demo.Repository.UserRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

@Service
public class CommentService {

    private final CommentRepository commentRepository;
    private final UserRepository userRepository;
    private final MenuRepository menuRepository;

    public CommentService(CommentRepository commentRepository, UserRepository userRepository, MenuRepository menuRepository) {
        this.commentRepository = commentRepository;
        this.userRepository = userRepository;
        this.menuRepository = menuRepository;
    }

    // 用户发表评论
    @Transactional
    public CommentDTO addComment(Long userId, Long menuId, Integer rating, String content) {
        userRepository.findById(userId).orElseThrow(() -> new RuntimeException("用户不存在"));
        menuRepository.findById(menuId).orElseThrow(() -> new RuntimeException("菜品不存在"));

        Comment comment = new Comment();
        comment.setUserId(userId);
        comment.setMenuId(menuId);
        comment.setRating(rating);
        comment.setContent(content);
        comment.setStatus(1);
        comment.setCreateTime(LocalDateTime.now());
        comment = commentRepository.save(comment);

        return convertToDTO(comment);
    }

    // 获取菜品的评论（分页，只显示可见的）
    public Page<CommentDTO> getCommentsByMenu(Long menuId, int page, int size) {
        Pageable pageable = PageRequest.of(page - 1, size, Sort.by(Sort.Direction.DESC, "createTime"));
        Page<Comment> commentPage = commentRepository.findByMenuId(menuId, pageable);
        return commentPage.map(this::convertToDTO);
    }

    // 获取菜品平均评分
    public Double getAverageRating(Long menuId) {
        Double avg = commentRepository.getAverageRatingByMenuId(menuId);
        return avg != null ? avg : 0.0;
    }

    // 管理员获取所有评论（分页，包括隐藏的）
    public Page<CommentDTO> getAllCommentsForAdmin(int page, int size) {
        Pageable pageable = PageRequest.of(page - 1, size, Sort.by(Sort.Direction.DESC, "createTime"));
        Page<Comment> commentPage = commentRepository.findAll(pageable);
        return commentPage.map(this::convertToDTO);
    }

    // 管理员修改评论状态（显示/隐藏）
    @Transactional
    public Comment updateCommentStatus(Long commentId, Integer status) {
        Comment comment = commentRepository.findById(commentId).orElseThrow(() -> new RuntimeException("评论不存在"));
        comment.setStatus(status);
        commentRepository.save(comment);
        return comment;
    }

    // 管理员删除评论
    @Transactional
    public void deleteComment(Long commentId) {
        commentRepository.deleteById(commentId);
    }

    // 转换实体到DTO，填充用户名和菜品名
    private CommentDTO convertToDTO(Comment comment) {
        CommentDTO dto = new CommentDTO();
        dto.setId(comment.getId());
        dto.setMenuId(comment.getMenuId());
        dto.setUserId(comment.getUserId());
        dto.setRating(comment.getRating());
        dto.setContent(comment.getContent());
        dto.setStatus(comment.getStatus());
        dto.setCreateTime(comment.getCreateTime());

        // 查询用户名和菜品名（可能影响性能，可优化为批量查询或使用 @EntityGraph）
        userRepository.findById(comment.getUserId()).ifPresent(user -> dto.setUsername(user.getUsername()));
        menuRepository.findById(comment.getMenuId()).ifPresent(menu -> dto.setMenuName(menu.getName()));

        return dto;
    }
}
