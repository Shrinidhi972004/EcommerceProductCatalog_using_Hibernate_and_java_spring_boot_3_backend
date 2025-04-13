package com.shrinidhi.EcommerceProductCatalog.repository;

import com.shrinidhi.EcommerceProductCatalog.model.CartItem;
import com.shrinidhi.EcommerceProductCatalog.model.Product;
import com.shrinidhi.EcommerceProductCatalog.model.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface CartItemRepository extends JpaRepository<CartItem, Long> {

    List<CartItem> findByUser(User user);

    Optional<CartItem> findByUserAndProduct(User user, Product product);

    void deleteByUser(User user);

    void deleteByUserAndProduct(User user, Product product); // 👈 Add this line
}
