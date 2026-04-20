package com.example.demo.Repository;

import com.example.demo.Entity.Menu;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

import java.util.List;

public interface MenuRepository extends JpaRepository<Menu, Long>, JpaSpecificationExecutor<Menu> {

    List<Menu> findByStatus(Integer status);

    List<Menu> findByCategoryAndStatus(String category, Integer status);

    // 根据多个条件查询（使用 JPA Specification）给ai用的
    List<Menu> findAll(Specification<Menu> spec);
}
