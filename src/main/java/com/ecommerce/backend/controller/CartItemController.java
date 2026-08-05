package com.ecommerce.backend.controller;

import com.ecommerce.backend.dto.CartItemResponse;
import com.ecommerce.backend.dto.CartRequest;
import com.ecommerce.backend.service.CartItemService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/cart")
@RequiredArgsConstructor
@CrossOrigin(originPatterns = "http://localhost:*")
public class CartItemController {

    private final CartItemService cartItemService;

    @GetMapping
    public ResponseEntity<List<CartItemResponse>> getCart() {
        return ResponseEntity.ok(cartItemService.getCart());
    }

    @PostMapping("/add")
    public ResponseEntity<CartItemResponse> addToCart(@RequestBody CartRequest request) {
        return ResponseEntity.ok(cartItemService.addToCart(request));
    }

    @PutMapping("/update")
    public ResponseEntity<CartItemResponse> updateQuantity(@RequestBody CartRequest request) {
        return ResponseEntity.ok(cartItemService.updateQuantity(request));
    }

    @DeleteMapping("/remove/{productId}")
    public ResponseEntity<?> removeFromCart(@PathVariable Long productId) {
        cartItemService.removeFromCart(productId);
        return ResponseEntity.ok(Map.of("message", "Item removed from cart"));
    }

    @DeleteMapping("/clear")
    public ResponseEntity<?> clearCart() {
        cartItemService.clearCart();
        return ResponseEntity.ok(Map.of("message", "Cart cleared"));
    }
}