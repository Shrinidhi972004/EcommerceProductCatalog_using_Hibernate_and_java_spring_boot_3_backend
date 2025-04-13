package com.shrinidhi.EcommerceProductCatalog.controller;

import com.shrinidhi.EcommerceProductCatalog.model.Product;
import com.shrinidhi.EcommerceProductCatalog.model.Category;
import com.shrinidhi.EcommerceProductCatalog.repository.ProductRepository;
import com.shrinidhi.EcommerceProductCatalog.repository.CategoryRepository;
import com.shrinidhi.EcommerceProductCatalog.exception.ProductNotFoundException;
import com.shrinidhi.EcommerceProductCatalog.exception.ResourceNotFoundException;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import org.springframework.http.ResponseEntity;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;

import jakarta.validation.Valid;
import org.springframework.security.access.prepost.PreAuthorize;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/products")
public class ProductController {

    @Autowired
    private ProductRepository productRepository;

    @Autowired
    private CategoryRepository categoryRepository;

    // Create product (Admin only)
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

    // Get all products (User or Admin)
    @PreAuthorize("hasAnyRole('USER', 'ADMIN')")
    @GetMapping
    public List<Product> getAllProducts() {
        return productRepository.findAll();
    }

    // Get product by ID (User or Admin)
    @PreAuthorize("hasAnyRole('USER', 'ADMIN')")
    @GetMapping("/{id}")
    public Product getProductById(@PathVariable Long id) {
        return productRepository.findById(id)
                .orElseThrow(() -> new ProductNotFoundException("Product not found with ID: " + id));
    }

    // Update product (Admin only)
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

    // Delete product (Admin only)
    @PreAuthorize("hasRole('ADMIN')")
    @DeleteMapping("/{id}")
    public ResponseEntity<String> deleteProduct(@PathVariable Long id) {
        if (!productRepository.existsById(id)) {
            throw new ProductNotFoundException("Product not found with ID: " + id);
        }
        productRepository.deleteById(id);
        return ResponseEntity.ok("Product deleted successfully");
    }

    // Search products (User or Admin)
    @PreAuthorize("hasAnyRole('USER', 'ADMIN')")
    @GetMapping("/search")
    public List<Product> searchProducts(
            @RequestParam(required = false) String name,
            @RequestParam(required = false) Double minPrice,
            @RequestParam(required = false) Double maxPrice,
            @RequestParam(required = false) Long categoryId
    ) {
        return productRepository.searchProducts(name, minPrice, maxPrice, categoryId);
    }

    // Get paginated products (User or Admin)
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
        return productRepository.findAll(pageable);
    }

    // ✅ Filter + Pagination combined
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
        return productRepository.filterPaginated(name, minPrice, maxPrice, categoryId, pageable);
    }
}