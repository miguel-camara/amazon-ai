package com.amazon.ecommerce.controller;

import com.amazon.ecommerce.dto.request.CartItemRequest;
import com.amazon.ecommerce.dto.response.ApiResponse;
import com.amazon.ecommerce.entity.CartItem;
import com.amazon.ecommerce.service.CartService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/cart")
public class CartController {

    private final CartService cartService;

    public CartController(CartService cartService) {
        this.cartService = cartService;
    }

    @GetMapping
    public ResponseEntity<ApiResponse<List<CartItem>>> getCart(@AuthenticationPrincipal UserDetails userDetails) {
        Long userId = extractUserId(userDetails);
        List<CartItem> cartItems = cartService.getUserCart(userId);
        return ResponseEntity.ok(ApiResponse.success(cartItems));
    }

    @PostMapping
    public ResponseEntity<ApiResponse<CartItem>> addToCart(
            @AuthenticationPrincipal UserDetails userDetails,
            @Valid @RequestBody CartItemRequest request) {
        Long userId = extractUserId(userDetails);
        CartItem cartItem = cartService.addToCart(userId, request);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success("Item added to cart", cartItem));
    }

    @PutMapping("/{id}")
    public ResponseEntity<ApiResponse<CartItem>> updateCartItem(
            @AuthenticationPrincipal UserDetails userDetails,
            @PathVariable Long id,
            @Valid @RequestBody CartItemRequest request) {
        Long userId = extractUserId(userDetails);
        CartItem cartItem = cartService.updateCartItem(userId, id, request);
        return ResponseEntity.ok(ApiResponse.success("Cart item updated", cartItem));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<Void>> removeFromCart(
            @AuthenticationPrincipal UserDetails userDetails,
            @PathVariable Long id) {
        Long userId = extractUserId(userDetails);
        cartService.removeFromCart(userId, id);
        return ResponseEntity.ok(ApiResponse.success("Item removed from cart", null));
    }

    private Long extractUserId(UserDetails userDetails) {
        return Long.parseLong(userDetails.getUsername());
    }
}
