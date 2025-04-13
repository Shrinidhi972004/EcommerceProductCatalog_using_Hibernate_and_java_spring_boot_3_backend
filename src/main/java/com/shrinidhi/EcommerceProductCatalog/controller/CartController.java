package com.shrinidhi.EcommerceProductCatalog.controller;

import com.shrinidhi.EcommerceProductCatalog.dto.CartItemResponseDTO;
import com.shrinidhi.EcommerceProductCatalog.exception.ResourceNotFoundException;
import com.shrinidhi.EcommerceProductCatalog.model.CartItem;
import com.shrinidhi.EcommerceProductCatalog.model.Product;
import com.shrinidhi.EcommerceProductCatalog.model.User;
import com.shrinidhi.EcommerceProductCatalog.repository.CartItemRepository;
import com.shrinidhi.EcommerceProductCatalog.repository.ProductRepository;
import com.shrinidhi.EcommerceProductCatalog.repository.UserRepository;
import com.shrinidhi.EcommerceProductCatalog.security.JwtUtil;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.transaction.Transactional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/api/cart")
public class CartController {

    @Autowired
    private CartItemRepository cartItemRepository;

    @Autowired
    private ProductRepository productRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private JwtUtil jwtUtil;

    private String getUsernameFromRequest(HttpServletRequest request) {
        String authHeader = request.getHeader("Authorization");
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            throw new RuntimeException("Missing or invalid Authorization header");
        }
        return jwtUtil.extractUsername(authHeader.substring(7));
    }

    @PreAuthorize("hasRole('USER')")
    @PostMapping("/add")
    public ResponseEntity<?> addToCart(HttpServletRequest request, @RequestBody CartItem cartRequest) {
        String username = getUsernameFromRequest(request);
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));

        Product product = productRepository.findById(cartRequest.getProduct().getId())
                .orElseThrow(() -> new ResourceNotFoundException("Product not found"));

        Optional<CartItem> existing = cartItemRepository.findByUserAndProduct(user, product);
        CartItem item;

        if (existing.isPresent()) {
            item = existing.get();
            item.setQuantity(item.getQuantity() + cartRequest.getQuantity());
        } else {
            item = CartItem.builder()
                    .user(user)
                    .product(product)
                    .quantity(cartRequest.getQuantity())
                    .build();
        }

        CartItem savedItem = cartItemRepository.save(item);
        return ResponseEntity.ok(savedItem);
    }

    @PreAuthorize("hasRole('USER')")
    @GetMapping
    public ResponseEntity<List<CartItemResponseDTO>> getUserCart(HttpServletRequest request) {
        String username = getUsernameFromRequest(request);
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));

        List<CartItem> cartItems = cartItemRepository.findByUser(user);

        List<CartItemResponseDTO> response = cartItems.stream().map(item -> {
            Product p = item.getProduct();
            return CartItemResponseDTO.builder()
                    .cartItemId(item.getId())
                    .productName(p.getName())
                    .productImageUrl(p.getImageUrl())
                    .productPrice(p.getPrice())
                    .quantity(item.getQuantity())
                    .totalPrice(p.getPrice() * item.getQuantity())
                    .build();
        }).toList();

        return ResponseEntity.ok(response);
    }

    @PreAuthorize("hasRole('USER')")
    @PutMapping("/update/{productId}")
    public ResponseEntity<?> updateQuantity(
            HttpServletRequest request,
            @PathVariable Long productId,
            @RequestParam int quantity) {

        String username = getUsernameFromRequest(request);
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));

        Product product = productRepository.findById(productId)
                .orElseThrow(() -> new ResourceNotFoundException("Product not found"));

        CartItem cartItem = cartItemRepository.findByUserAndProduct(user, product)
                .orElseThrow(() -> new ResourceNotFoundException("Item not found in cart"));

        cartItem.setQuantity(quantity);
        cartItemRepository.save(cartItem);

        return ResponseEntity.ok("Quantity updated successfully");
    }

    @PreAuthorize("hasRole('USER')")
    @DeleteMapping("/remove/{productId}")
    @Transactional
    public ResponseEntity<?> removeItem(HttpServletRequest request, @PathVariable Long productId) {
        String username = getUsernameFromRequest(request);
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));

        Product product = productRepository.findById(productId)
                .orElseThrow(() -> new ResourceNotFoundException("Product not found"));

        cartItemRepository.deleteByUserAndProduct(user, product);
        return ResponseEntity.ok("Item removed from cart");
    }

    @PreAuthorize("hasRole('USER')")
    @DeleteMapping("/clear")
    @Transactional
    public ResponseEntity<?> clearCart(HttpServletRequest request) {
        String username = getUsernameFromRequest(request);
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));

        cartItemRepository.deleteByUser(user);
        return ResponseEntity.ok("Cart cleared successfully");
    }
}
