package com.shrinidhi.EcommerceProductCatalog.controller;

import com.shrinidhi.EcommerceProductCatalog.exception.ResourceNotFoundException;
import com.shrinidhi.EcommerceProductCatalog.model.Order;
import com.shrinidhi.EcommerceProductCatalog.model.User;
import com.shrinidhi.EcommerceProductCatalog.repository.UserRepository;
import com.shrinidhi.EcommerceProductCatalog.security.JwtUtil;
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
    private JwtUtil jwtUtil;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private OrderService orderService;

    private String getUsernameFromRequest(HttpServletRequest request) {
        String authHeader = request.getHeader("Authorization");
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            throw new RuntimeException("Missing or invalid Authorization header");
        }
        return jwtUtil.extractUsername(authHeader.substring(7));
    }

    @PreAuthorize("hasRole('USER')")
    @PostMapping("/place")
    public ResponseEntity<?> placeOrder(HttpServletRequest request) {
        // Now calling placeOrder using request directly
        Order order = orderService.placeOrder(request);
        return ResponseEntity.ok(order);
    }

    @PreAuthorize("hasRole('USER')")
    @GetMapping("/user")
    public ResponseEntity<List<Order>> getUserOrders(HttpServletRequest request) {
        String username = getUsernameFromRequest(request);
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));

        List<Order> orders = orderService.getOrdersForUser(user);
        return ResponseEntity.ok(orders);
    }
}
