package com.shrinidhi.EcommerceProductCatalog.repository;

import com.shrinidhi.EcommerceProductCatalog.model.Product;
import com.shrinidhi.EcommerceProductCatalog.model.Review;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ReviewRepository extends JpaRepository<Review, Long> {
    List<Review> findByProduct(Product product);
}
