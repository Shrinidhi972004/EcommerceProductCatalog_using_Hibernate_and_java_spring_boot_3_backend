package com.shrinidhi.EcommerceProductCatalog.controller;

import com.shrinidhi.EcommerceProductCatalog.dto.ReviewRequestDTO;
import com.shrinidhi.EcommerceProductCatalog.model.Review;
import com.shrinidhi.EcommerceProductCatalog.service.ReviewService;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/reviews")
public class ReviewController {

    @Autowired
    private ReviewService reviewService;

    @PreAuthorize("hasRole('USER')")
    @PostMapping("/product/{productId}")
    public ResponseEntity<Review> addReview(@PathVariable Long productId,
                                            @RequestBody ReviewRequestDTO reviewRequest,
                                            HttpServletRequest request) {
        Review review = reviewService.addReview(productId, reviewRequest, request);
        return ResponseEntity.ok(review);
    }

    @GetMapping("/product/{productId}")
    public ResponseEntity<List<Review>> getProductReviews(@PathVariable Long productId) {
        List<Review> reviews = reviewService.getReviewsForProduct(productId);
        return ResponseEntity.ok(reviews);
    }

    @GetMapping("/product/{productId}/average-rating")
    public ResponseEntity<Double> getAverageRating(@PathVariable Long productId) {
        double average = reviewService.getAverageRating(productId);
        return ResponseEntity.ok(average);
    }

    @PreAuthorize("hasRole('USER')")
    @PutMapping("/{reviewId}")
    public ResponseEntity<Review> updateReview(@PathVariable Long reviewId,
                                               @RequestBody ReviewRequestDTO reviewRequest,
                                               HttpServletRequest request) {
        Review updatedReview = reviewService.updateReview(reviewId, reviewRequest, request);
        return ResponseEntity.ok(updatedReview);
    }

    @PreAuthorize("hasRole('USER')")
    @DeleteMapping("/{reviewId}")
    public ResponseEntity<?> deleteReview(@PathVariable Long reviewId,
                                          HttpServletRequest request) {
        reviewService.deleteReview(reviewId, request);
        return ResponseEntity.ok("Review deleted successfully");
    }
}
