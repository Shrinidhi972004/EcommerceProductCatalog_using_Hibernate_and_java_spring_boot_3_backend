package com.shrinidhi.EcommerceProductCatalog.repository;

import com.shrinidhi.EcommerceProductCatalog.model.Order;
import com.shrinidhi.EcommerceProductCatalog.model.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface OrderRepository extends JpaRepository<Order, Long> {
    List<Order> findByUser(User user);
}
