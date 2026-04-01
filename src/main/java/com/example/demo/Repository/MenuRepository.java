package com.example.demo.Repository;

import com.example.demo.Entity.Menu;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface MenuRepository extends JpaRepository<Menu, Long> {

    List<Menu> findByStatus(Integer status);

    List<Menu> findByCategoryAndStatus(String category, Integer status);
}
