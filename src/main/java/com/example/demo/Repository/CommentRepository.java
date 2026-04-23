package com.example.demo.Repository;

import com.example.demo.Entity.Comment;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface CommentRepository extends JpaRepository<Comment, Long> {

    Page<Comment> findByMenuId(Long menuId, Pageable pageable);

    @Query("SELECT c FROM Comment c WHERE c.status = 1 ORDER BY c.createTime DESC")
    Page<Comment> findAllVisible(Pageable pageable);

    @Query("SELECT AVG(c.rating) FROM Comment c WHERE c.menuId = :menuId AND c.status = 1")
    Double getAverageRatingByMenuId(@Param("menuId") Long menuId);
}
