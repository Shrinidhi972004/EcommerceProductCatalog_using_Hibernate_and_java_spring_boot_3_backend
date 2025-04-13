package com.shrinidhi.EcommerceProductCatalog.repository;

import com.shrinidhi.EcommerceProductCatalog.model.Category;
import org.springframework.data.jpa.repository.JpaRepository;

public interface CategoryRepository extends JpaRepository<Category, Long> {
    boolean existsByName(String name);
}
