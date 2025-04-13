package com.shrinidhi.EcommerceProductCatalog.repository;

import com.shrinidhi.EcommerceProductCatalog.model.OrderItem;
import org.springframework.data.jpa.repository.JpaRepository;

public interface OrderItemRepository extends JpaRepository<OrderItem, Long> {
}
