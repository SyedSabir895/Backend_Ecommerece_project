package com.ecommerce.backend.service;

import com.ecommerce.backend.dto.CartItemResponse;
import com.ecommerce.backend.dto.CartRequest;
import com.ecommerce.backend.model.CartItem;
import com.ecommerce.backend.model.Product;
import com.ecommerce.backend.model.ProductImage;
import com.ecommerce.backend.model.User;
import com.ecommerce.backend.repository.CartItemRepository;
import com.ecommerce.backend.repository.ProductRepository;
import com.ecommerce.backend.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class CartItemService {

    private final CartItemRepository cartItemRepository;
    private final UserRepository userRepository;
    private final ProductRepository productRepository;

    private Integer getCurrentUserId() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !authentication.isAuthenticated()) {
            throw new RuntimeException("User not authenticated");
        }

        Object principal = authentication.getPrincipal();
        String email = principal instanceof User user
                ? user.getEmail()
                : principal != null ? principal.toString() : authentication.getName();

        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("User not found"));
        return user.getId();
    }

    public List<CartItemResponse> getCart() {
        Integer userId = getCurrentUserId();
        List<CartItem> items = cartItemRepository.findByUserId(userId);
        return toResponses(items);
    }

    public CartItemResponse addToCart(CartRequest request) {
        Integer userId = getCurrentUserId();

        productRepository.findById(request.getProductId())
                .orElseThrow(() -> new RuntimeException("Product not found with id: " + request.getProductId()));

        Optional<CartItem> existing = cartItemRepository.findByUserIdAndProductId(userId, request.getProductId());

        if (existing.isPresent()) {
            CartItem item = existing.get();
            item.setQuantity(item.getQuantity() + (request.getQuantity() != null ? request.getQuantity() : 1));
            return toResponse(cartItemRepository.save(item));
        } else {
            CartItem item = CartItem.builder()
                    .userId(userId)
                    .productId(request.getProductId())
                    .quantity(request.getQuantity() != null ? request.getQuantity() : 1)
                    .build();
            return toResponse(cartItemRepository.save(item));
        }
    }

    public CartItemResponse updateQuantity(CartRequest request) {
        Integer userId = getCurrentUserId();
        CartItem item = cartItemRepository.findByUserIdAndProductId(userId, request.getProductId())
                .orElseThrow(() -> new RuntimeException("Item not found in cart"));

        if (request.getQuantity() != null && request.getQuantity() <= 0) {
            cartItemRepository.delete(item);
            return toResponse(item);
        }

        item.setQuantity(request.getQuantity());
        return toResponse(cartItemRepository.save(item));
    }

    public void removeFromCart(Long productId) {
        Integer userId = getCurrentUserId();
        cartItemRepository.deleteByUserIdAndProductId(userId, productId);
    }

    public void clearCart() {
        Integer userId = getCurrentUserId();
        cartItemRepository.deleteByUserId(userId);
    }

    private List<CartItemResponse> toResponses(List<CartItem> items) {
        if (items.isEmpty()) return List.of();

        List<Long> productIds = items.stream().map(CartItem::getProductId).toList();
        Map<Long, Product> productMap = productRepository.findAllById(productIds)
                .stream().collect(Collectors.toMap(Product::getId, p -> p));

        return items.stream().map(item -> {
            Product product = productMap.get(item.getProductId());
            return toResponse(item, product);
        }).toList();
    }

    private CartItemResponse toResponse(CartItem item) {
        Product product = productRepository.findById(item.getProductId()).orElse(null);
        return toResponse(item, product);
    }

    private CartItemResponse toResponse(CartItem item, Product product) {
        List<String> imageUrls = (product != null && product.getImages() != null)
                ? product.getImages().stream().map(ProductImage::getImageUrl).toList()
                : List.of();

        return CartItemResponse.builder()
                .cartItemId(item.getId())
                .productId(item.getProductId())
                .productName(product != null ? product.getName() : "Unknown Product")
                .productDescription(product != null ? product.getDescription() : "")
                .productPrice(product != null ? product.getPrice() : java.math.BigDecimal.ZERO)
                .productStock(product != null ? product.getStock() : 0)
                .productImageUrls(imageUrls)
                .quantity(item.getQuantity())
                .build();
    }
}