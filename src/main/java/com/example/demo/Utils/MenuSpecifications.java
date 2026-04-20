package com.example.demo.Utils;
import com.example.demo.Entity.Menu;
import com.example.demo.Service.AIService;
import jakarta.persistence.criteria.Predicate;
import org.springframework.data.jpa.domain.Specification;
import java.util.ArrayList;
import java.util.List;

public class MenuSpecifications {
    public static Specification<Menu> withParams(AIService.QueryParams params) {
        return (root, query, cb) -> {
            List<Predicate> predicates = new ArrayList<>();
            // 必须上架
            predicates.add(cb.equal(root.get("status"), 1));

            // 关键词模糊匹配（菜品名或描述）
            if (params.keywords != null && !params.keywords.isEmpty()) {
                String likePattern = "%" + params.keywords + "%";
                Predicate nameLike = cb.like(root.get("name"), likePattern);
                Predicate descLike = cb.like(root.get("description"), likePattern);
                predicates.add(cb.or(nameLike, descLike));
            }
            // 分类匹配
            if (params.category != null && !params.category.isEmpty()) {
                predicates.add(cb.equal(root.get("category"), params.category));
            }
            // 价格范围
            if (params.minPrice != null) {
                predicates.add(cb.greaterThanOrEqualTo(root.get("price"), params.minPrice));
            }
            if (params.maxPrice != null) {
                predicates.add(cb.lessThanOrEqualTo(root.get("price"), params.maxPrice));
            }
            // 口味：在描述里模糊匹配（简单实现，更精确可用额外字段）
            if (params.flavor != null && !params.flavor.isEmpty()) {
                predicates.add(cb.like(root.get("description"), "%" + params.flavor + "%"));
            }
            return cb.and(predicates.toArray(new Predicate[0]));
        };
    }
}