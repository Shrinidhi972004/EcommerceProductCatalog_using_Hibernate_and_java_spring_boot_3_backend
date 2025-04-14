package com.shrinidhi.EcommerceProductCatalog.service;

import com.shrinidhi.EcommerceProductCatalog.dto.ReviewRequestDTO;
import com.shrinidhi.EcommerceProductCatalog.exception.ResourceNotFoundException;
import com.shrinidhi.EcommerceProductCatalog.model.*;
import com.shrinidhi.EcommerceProductCatalog.repository.*;
import com.shrinidhi.EcommerceProductCatalog.security.JwtUtil;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

@Service
public class ReviewService {

    @Autowired
    private ReviewRepository reviewRepository;

    @Autowired
    private ProductRepository productRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private JwtUtil jwtUtil;

    public Review addReview(Long productId, ReviewRequestDTO reviewRequest, HttpServletRequest request) {
        String authHeader = request.getHeader("Authorization");
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            throw new RuntimeException("Missing or invalid Authorization header");
        }
        String username = jwtUtil.extractUsername(authHeader.substring(7));

        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));
        Product product = productRepository.findById(productId)
                .orElseThrow(() -> new ResourceNotFoundException("Product not found"));

        Review review = Review.builder()
                .content(reviewRequest.getContent())
                .rating(reviewRequest.getRating())
                .createdAt(LocalDateTime.now())
                .user(user)
                .product(product)
                .build();

        return reviewRepository.save(review);
    }

    public List<Review> getReviewsForProduct(Long productId) {
        Product product = productRepository.findById(productId)
                .orElseThrow(() -> new ResourceNotFoundException("Product not found"));

        return reviewRepository.findByProduct(product);
    }

    public double getAverageRating(Long productId) {
        Product product = productRepository.findById(productId)
                .orElseThrow(() -> new ResourceNotFoundException("Product not found"));

        List<Review> reviews = reviewRepository.findByProduct(product);
        return reviews.stream()
                .mapToInt(Review::getRating)
                .average()
                .orElse(0.0);
    }

    public Review updateReview(Long reviewId, ReviewRequestDTO reviewRequest, HttpServletRequest request) {
        String username = extractUsernameFromRequest(request);

        Review review = reviewRepository.findById(reviewId)
                .orElseThrow(() -> new ResourceNotFoundException("Review not found"));

        if (!review.getUser().getUsername().equals(username)) {
            throw new RuntimeException("Unauthorized to update this review");
        }

        review.setContent(reviewRequest.getContent());
        review.setRating(reviewRequest.getRating());
        review.setCreatedAt(LocalDateTime.now());

        return reviewRepository.save(review);
    }

    public void deleteReview(Long reviewId, HttpServletRequest request) {
        String username = extractUsernameFromRequest(request);

        Review review = reviewRepository.findById(reviewId)
                .orElseThrow(() -> new ResourceNotFoundException("Review not found"));

        if (!review.getUser().getUsername().equals(username)) {
            throw new RuntimeException("Unauthorized to delete this review");
        }

        reviewRepository.delete(review);
    }

    private String extractUsernameFromRequest(HttpServletRequest request) {
        String authHeader = request.getHeader("Authorization");
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            throw new RuntimeException("Missing or invalid Authorization header");
        }
        return jwtUtil.extractUsername(authHeader.substring(7));
    }
}
