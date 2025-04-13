package com.shrinidhi.EcommerceProductCatalog.service;

import com.shrinidhi.EcommerceProductCatalog.exception.ResourceNotFoundException;
import com.shrinidhi.EcommerceProductCatalog.model.*;
import com.shrinidhi.EcommerceProductCatalog.repository.*;
import com.shrinidhi.EcommerceProductCatalog.security.JwtUtil;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class OrderService {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private CartItemRepository cartItemRepository;

    @Autowired
    private OrderRepository orderRepository;

    @Autowired
    private OrderItemRepository orderItemRepository;

    @Autowired
    private JwtUtil jwtUtil;

    private String getUsernameFromRequest(HttpServletRequest request) {
        String authHeader = request.getHeader("Authorization");
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            throw new RuntimeException("Missing or invalid Authorization header");
        }
        return jwtUtil.extractUsername(authHeader.substring(7));
    }

    @Transactional
    public Order placeOrder(HttpServletRequest request) {
        String username = getUsernameFromRequest(request);
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));

        List<CartItem> cartItems = cartItemRepository.findByUser(user);
        if (cartItems.isEmpty()) {
            throw new ResourceNotFoundException("Cart is empty");
        }

        Order order = Order.builder()
                .user(user)
                .orderDate(LocalDateTime.now())
                .totalAmount(cartItems.stream()
                        .mapToDouble(item -> item.getProduct().getPrice() * item.getQuantity())
                        .sum())
                .build();
        orderRepository.save(order);

        List<OrderItem> orderItems = cartItems.stream().map(cartItem -> OrderItem.builder()
                .order(order)
                .product(cartItem.getProduct())
                .quantity(cartItem.getQuantity())
                .price(cartItem.getProduct().getPrice())
                .build()).collect(Collectors.toList());

        orderItemRepository.saveAll(orderItems);

        // Clear user's cart after placing order
        cartItemRepository.deleteByUser(user);

        return order;
    }

    public List<Order> getOrdersForUser(User user) {
        return orderRepository.findByUser(user);
    }
}
