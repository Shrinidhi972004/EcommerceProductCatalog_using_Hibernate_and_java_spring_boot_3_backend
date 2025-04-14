package com.shrinidhi.EcommerceProductCatalog.controller;

import com.shrinidhi.EcommerceProductCatalog.model.Order;
import com.shrinidhi.EcommerceProductCatalog.model.OrderStatus;
import com.shrinidhi.EcommerceProductCatalog.service.OrderService;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/orders")
public class OrderController {

    @Autowired
    private OrderService orderService;

    // User can place an order
    @PreAuthorize("hasRole('USER')")
    @PostMapping("/place")
    public ResponseEntity<Order> placeOrder(HttpServletRequest request) {
        Order order = orderService.placeOrder(request);
        return ResponseEntity.ok(order);
    }

    // User can view their orders
    @PreAuthorize("hasRole('USER')")
    @GetMapping("/user")
    public ResponseEntity<List<Order>> getUserOrders(HttpServletRequest request) {
        List<Order> orders = orderService.getOrdersForUser(request);
        return ResponseEntity.ok(orders);
    }

    // Admin can update order status
    @PreAuthorize("hasRole('ADMIN')")
    @PutMapping("/{orderId}/status")
    public ResponseEntity<Order> updateOrderStatus(@PathVariable Long orderId,
                                                   @RequestBody OrderStatus status) {
        Order updatedOrder = orderService.updateOrderStatus(orderId, status);
        return ResponseEntity.ok(updatedOrder); // ✅ Make sure this line is present!
    }
}
