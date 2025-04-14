package com.shrinidhi.EcommerceProductCatalog.controller;

import com.shrinidhi.EcommerceProductCatalog.model.Product;
import com.shrinidhi.EcommerceProductCatalog.model.Category;
import com.shrinidhi.EcommerceProductCatalog.model.Review;
import com.shrinidhi.EcommerceProductCatalog.repository.ProductRepository;
import com.shrinidhi.EcommerceProductCatalog.repository.CategoryRepository;
import com.shrinidhi.EcommerceProductCatalog.repository.ReviewRepository;
import com.shrinidhi.EcommerceProductCatalog.exception.ProductNotFoundException;
import com.shrinidhi.EcommerceProductCatalog.exception.ResourceNotFoundException;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import org.springframework.http.ResponseEntity;
import org.springframework.http.HttpStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;

import jakarta.validation.Valid;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.*;
import java.util.*;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/products")
public class ProductController {

    @Autowired
    private ProductRepository productRepository;

    @Autowired
    private CategoryRepository categoryRepository;

    @Autowired
    private ReviewRepository reviewRepository;

    @PreAuthorize("hasRole('ADMIN')")
    @PostMapping
    public ResponseEntity<?> createProduct(@RequestBody Map<String, Object> request) {
        String name = (String) request.get("name");
        String description = (String) request.get("description");
        String imageUrl = (String) request.get("imageUrl");
        double price = Double.parseDouble(request.get("price").toString());
        int quantity = Integer.parseInt(request.get("quantity").toString());
        Long categoryId = Long.parseLong(request.get("categoryId").toString());

        Category category = categoryRepository.findById(categoryId)
                .orElseThrow(() -> new ResourceNotFoundException("Category not found with ID: " + categoryId));

        Product product = Product.builder()
                .name(name)
                .description(description)
                .imageUrl(imageUrl)
                .price(price)
                .quantity(quantity)
                .category(category)
                .build();

        return ResponseEntity.ok(productRepository.save(product));
    }

    @PreAuthorize("hasRole('ADMIN')")
    @PostMapping("/upload-image")
    public ResponseEntity<Map<String, String>> uploadImage(@RequestParam("file") MultipartFile file) {
        try {
            String fileName = UUID.randomUUID() + "_" + file.getOriginalFilename();
            Path uploadPath = Paths.get("src/main/resources/static/uploads");

            if (!Files.exists(uploadPath)) {
                Files.createDirectories(uploadPath);
            }

            Path filePath = uploadPath.resolve(fileName);
            Files.copy(file.getInputStream(), filePath, StandardCopyOption.REPLACE_EXISTING);

            String fileUrl = "/uploads/" + fileName;
            Map<String, String> response = new HashMap<>();
            response.put("imageUrl", fileUrl);
            return ResponseEntity.ok(response);
        } catch (IOException e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(Map.of("error", "Upload failed"));
        }
    }

    @PreAuthorize("hasAnyRole('USER', 'ADMIN')")
    @GetMapping
    public List<Product> getAllProducts() {
        List<Product> products = productRepository.findAll();
        products.forEach(product -> {
            List<Review> reviews = reviewRepository.findByProduct(product);
            double avgRating = reviews.stream().mapToInt(Review::getRating).average().orElse(0.0);
            product.setAverageRating(avgRating);
        });
        return products;
    }

    @PreAuthorize("hasAnyRole('USER', 'ADMIN')")
    @GetMapping("/{id}")
    public ResponseEntity<Map<String, Object>> getProductById(@PathVariable Long id) {
        Product product = productRepository.findById(id)
                .orElseThrow(() -> new ProductNotFoundException("Product not found with ID: " + id));

        List<Review> reviews = reviewRepository.findByProduct(product);
        double averageRating = reviews.stream().mapToInt(Review::getRating).average().orElse(0.0);
        product.setAverageRating(averageRating);

        Map<String, Object> response = new HashMap<>();
        response.put("product", product);
        response.put("reviews", reviews);
        response.put("averageRating", averageRating);

        return ResponseEntity.ok(response);
    }

    @PreAuthorize("hasRole('ADMIN')")
    @PutMapping("/{id}")
    public ResponseEntity<?> updateProduct(@PathVariable Long id, @RequestBody Map<String, Object> request) {
        Product product = productRepository.findById(id)
                .orElseThrow(() -> new ProductNotFoundException("Product not found with ID: " + id));

        product.setName((String) request.get("name"));
        product.setDescription((String) request.get("description"));
        product.setImageUrl((String) request.get("imageUrl"));
        product.setPrice(Double.parseDouble(request.get("price").toString()));
        product.setQuantity(Integer.parseInt(request.get("quantity").toString()));

        Long categoryId = Long.parseLong(request.get("categoryId").toString());
        Category category = categoryRepository.findById(categoryId)
                .orElseThrow(() -> new ResourceNotFoundException("Category not found with ID: " + categoryId));
        product.setCategory(category);

        return ResponseEntity.ok(productRepository.save(product));
    }

    @PreAuthorize("hasRole('ADMIN')")
    @DeleteMapping("/{id}")
    public ResponseEntity<String> deleteProduct(@PathVariable Long id) {
        if (!productRepository.existsById(id)) {
            throw new ProductNotFoundException("Product not found with ID: " + id);
        }
        productRepository.deleteById(id);
        return ResponseEntity.ok("Product deleted successfully");
    }

    @PreAuthorize("hasAnyRole('USER', 'ADMIN')")
    @GetMapping("/search")
    public List<Product> searchProducts(
            @RequestParam(required = false) String name,
            @RequestParam(required = false) Double minPrice,
            @RequestParam(required = false) Double maxPrice,
            @RequestParam(required = false) Long categoryId
    ) {
        List<Product> products = productRepository.searchProducts(name, minPrice, maxPrice, categoryId);
        products.forEach(product -> {
            List<Review> reviews = reviewRepository.findByProduct(product);
            double avgRating = reviews.stream().mapToInt(Review::getRating).average().orElse(0.0);
            product.setAverageRating(avgRating);
        });
        return products;
    }

    @PreAuthorize("hasAnyRole('USER', 'ADMIN')")
    @GetMapping("/paginated")
    public Page<Product> getPaginatedProducts(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "5") int size,
            @RequestParam(defaultValue = "id") String sortBy,
            @RequestParam(defaultValue = "asc") String sortDir
    ) {
        Sort sort = sortDir.equalsIgnoreCase("asc") ? Sort.by(sortBy).ascending() : Sort.by(sortBy).descending();
        Pageable pageable = PageRequest.of(page, size, sort);
        Page<Product> paged = productRepository.findAll(pageable);
        paged.forEach(product -> {
            List<Review> reviews = reviewRepository.findByProduct(product);
            double avgRating = reviews.stream().mapToInt(Review::getRating).average().orElse(0.0);
            product.setAverageRating(avgRating);
        });
        return paged;
    }

    @PreAuthorize("hasAnyRole('USER', 'ADMIN')")
    @GetMapping("/filter-paginated")
    public Page<Product> getFilteredPaginatedProducts(
            @RequestParam(required = false) String name,
            @RequestParam(required = false) Double minPrice,
            @RequestParam(required = false) Double maxPrice,
            @RequestParam(required = false) Long categoryId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "5") int size,
            @RequestParam(defaultValue = "id") String sortBy,
            @RequestParam(defaultValue = "asc") String sortDir
    ) {
        Sort sort = sortDir.equalsIgnoreCase("asc") ? Sort.by(sortBy).ascending() : Sort.by(sortBy).descending();
        Pageable pageable = PageRequest.of(page, size, sort);
        Page<Product> paged = productRepository.filterPaginated(name, minPrice, maxPrice, categoryId, pageable);
        paged.forEach(product -> {
            List<Review> reviews = reviewRepository.findByProduct(product);
            double avgRating = reviews.stream().mapToInt(Review::getRating).average().orElse(0.0);
            product.setAverageRating(avgRating);
        });
        return paged;
    }
}
