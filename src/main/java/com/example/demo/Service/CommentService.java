package com.example.demo.Service;

import com.example.demo.DTO.CommentDTO;
import com.example.demo.Entity.Comment;
import com.example.demo.Entity.Menu;
import com.example.demo.Entity.User;
import com.example.demo.Repository.CommentRepository;
import com.example.demo.Repository.MenuRepository;
import com.example.demo.Repository.UserRepository;
import io.swagger.v3.oas.annotations.media.Schema;
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

    /**
     * 用户发表评论（包含评分和内容）
     * <p>评论初始状态为可见（status=1），创建时间为当前时间</p>
     *
     * @param userId  评论用户ID
     * @param menuId  被评论的菜品ID
     * @param rating  评分（通常1-5分）
     * @param content 评论内容
     * @return 评论DTO（包含用户名和菜品名）
     * @throws RuntimeException 如果用户或菜品不存在
     */
    @Schema(description = "用户发表评论")
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

    /**
     * 获取指定菜品的评论（分页，仅返回状态为可见的评论）
     *
     * @param menuId 菜品ID
     * @param page   页码（从1开始）
     * @param size   每页条数
     * @return 分页的评论DTO数据，按创建时间降序排列
     */
    @Schema(description = "获取菜品的评论（分页，只显示可见的）")
    public Page<CommentDTO> getCommentsByMenu(Long menuId, int page, int size) {
        Pageable pageable = PageRequest.of(page - 1, size, Sort.by(Sort.Direction.DESC, "createTime"));
        Page<Comment> commentPage = commentRepository.findByMenuId(menuId, pageable);
        return commentPage.map(this::convertToDTO);
    }

    /**
     * 获取菜品的平均评分
     *
     * @param menuId 菜品ID
     * @return 平均评分（保留小数），若无评论则返回0.0
     */
    @Schema(description = "获取菜品平均评分")
    public Double getAverageRating(Long menuId) {
        Double avg = commentRepository.getAverageRatingByMenuId(menuId);
        return avg != null ? avg : 0.0;
    }

    /**
     * 管理员查询所有评论（包含已隐藏的评论），分页返回
     *
     * @param page 页码（从1开始）
     * @param size 每页条数
     * @return 分页的评论DTO数据，按创建时间降序排列
     */
    @Schema(description = "获取所有评论")
    public Page<CommentDTO> getAllCommentsForAdmin(int page, int size) {
        Pageable pageable = PageRequest.of(page - 1, size, Sort.by(Sort.Direction.DESC, "createTime"));
        Page<Comment> commentPage = commentRepository.findAll(pageable);
        return commentPage.map(this::convertToDTO);
    }

    /**
     * 管理员修改评论状态（显示/隐藏）
     *
     * @param commentId 评论ID
     * @param status    状态值（1-可见，0-隐藏）
     * @return 更新后的评论实体
     * @throws RuntimeException 如果评论不存在
     */
    @Transactional
    @Schema(description = "修改评论状态（显示/隐藏）")
    public Comment updateCommentStatus(Long commentId, Integer status) {
        Comment comment = commentRepository.findById(commentId).orElseThrow(() -> new RuntimeException("评论不存在"));
        comment.setStatus(status);
        commentRepository.save(comment);
        return comment;
    }

    /**
     * 管理员删除评论（物理删除）
     *
     * @param commentId 评论ID
     */
    @Transactional
    @Schema(description = "删除评论")
    public void deleteComment(Long commentId) {
        commentRepository.deleteById(commentId);
    }

    /**
     * 将评论实体转换为评论DTO，并填充用户名和菜品名称
     * <p><b>性能提示：</b>该方法内逐个查询用户和菜品，在批量转换时可能存在N+1问题，建议优化为批量查询或使用@EntityGraph</p>
     *
     * @param comment 评论实体
     * @return 包含用户名和菜品名的评论DTO
     */
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
