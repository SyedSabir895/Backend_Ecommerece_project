package com.ecommerce.backend.controller;

import com.ecommerce.backend.dto.AdminStatsResponse;
import com.ecommerce.backend.dto.CategoryResponse;
import com.ecommerce.backend.dto.ProductRequest;
import com.ecommerce.backend.dto.UserResponse;
import com.ecommerce.backend.model.Category;
import com.ecommerce.backend.model.Product;
import com.ecommerce.backend.model.ProductImage;
import com.ecommerce.backend.model.User;
import com.ecommerce.backend.repository.CartItemRepository;
import com.ecommerce.backend.repository.CategoryRepository;
import com.ecommerce.backend.repository.ProductRepository;
import com.ecommerce.backend.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/admin")
@RequiredArgsConstructor
@CrossOrigin(originPatterns = "http://localhost:*")
public class AdminController {

    private final ProductRepository productRepository;
    private final CategoryRepository categoryRepository;
    private final UserRepository userRepository;
    private final CartItemRepository cartItemRepository;

    // ─── Dashboard Stats ───────────────────────────────────────

    @GetMapping("/stats")
    public ResponseEntity<AdminStatsResponse> getStats() {
        AdminStatsResponse stats = AdminStatsResponse.builder()
                .totalProducts(productRepository.count())
                .totalCategories(categoryRepository.count())
                .totalUsers(userRepository.count())
                .totalCartItems(cartItemRepository.count())
                .build();
        return ResponseEntity.ok(stats);
    }

    // ─── Users ─────────────────────────────────────────────────

    @GetMapping("/users")
    public ResponseEntity<List<UserResponse>> getAllUsers() {
        List<UserResponse> users = userRepository.findAll().stream()
                .map(u -> UserResponse.builder()
                        .id(u.getId())
                        .fullName(u.getFullName())
                        .email(u.getEmail())
                        .role(u.getRole())
                        .build())
                .toList();
        return ResponseEntity.ok(users);
    }

    @PatchMapping("/users/{id}/role")
    public ResponseEntity<?> updateUserRole(@PathVariable Integer id, @RequestBody Map<String, String> body) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("User not found"));
        String newRole = body.get("role");
        if (newRole == null || (!newRole.equals("ROLE_USER") && !newRole.equals("ROLE_ADMIN"))) {
            return ResponseEntity.badRequest().body(Map.of("error", "Invalid role. Use ROLE_USER or ROLE_ADMIN"));
        }
        user.setRole(newRole);
        userRepository.save(user);
        return ResponseEntity.ok(Map.of("message", "Role updated"));
    }

    // ─── Products CRUD ─────────────────────────────────────────

    @PostMapping("/products")
    public ResponseEntity<Product> createProduct(@RequestBody ProductRequest request) {
        Product product = new Product();
        product.setName(request.getName());
        product.setDescription(request.getDescription());
        product.setPrice(request.getPrice());
        product.setStock(request.getStock());
        product.setCategoryId(request.getCategoryId());

        if (request.getImageUrls() != null && !request.getImageUrls().isEmpty()) {
            List<ProductImage> images = request.getImageUrls().stream()
                    .map(url -> {
                        ProductImage img = new ProductImage();
                        img.setProduct(product);
                        img.setImageUrl(url);
                        return img;
                    }).toList();
            product.setImages(images);
        }

        Product saved = productRepository.save(product);
        return ResponseEntity.ok(saved);
    }

    @PutMapping("/products/{id}")
    public ResponseEntity<Product> updateProduct(@PathVariable Long id, @RequestBody ProductRequest request) {
        Product product = productRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Product not found"));

        if (request.getName() != null) product.setName(request.getName());
        if (request.getDescription() != null) product.setDescription(request.getDescription());
        if (request.getPrice() != null) product.setPrice(request.getPrice());
        if (request.getStock() != null) product.setStock(request.getStock());
        if (request.getCategoryId() != null) product.setCategoryId(request.getCategoryId());

        Product saved = productRepository.save(product);
        return ResponseEntity.ok(saved);
    }

    @DeleteMapping("/products/{id}")
    public ResponseEntity<?> deleteProduct(@PathVariable Long id) {
        if (!productRepository.existsById(id)) {
            return ResponseEntity.notFound().build();
        }
        productRepository.deleteById(id);
        return ResponseEntity.ok(Map.of("message", "Product deleted"));
    }

    // ─── Categories CRUD ───────────────────────────────────────

    @PostMapping("/categories")
    public ResponseEntity<CategoryResponse> createCategory(@RequestBody Map<String, String> body) {
        String name = body.get("name");
        if (name == null || name.isBlank()) {
            return ResponseEntity.badRequest().build();
        }

        Category category = Category.builder().name(name).build();
        Category saved = categoryRepository.save(category);

        return ResponseEntity.ok(CategoryResponse.builder()
                .id(saved.getId())
                .name(saved.getName())
                .build());
    }

    @DeleteMapping("/categories/{id}")
    public ResponseEntity<?> deleteCategory(@PathVariable Long id) {
        if (!categoryRepository.existsById(id)) {
            return ResponseEntity.notFound().build();
        }
        categoryRepository.deleteById(id);
        return ResponseEntity.ok(Map.of("message", "Category deleted"));
    }
}