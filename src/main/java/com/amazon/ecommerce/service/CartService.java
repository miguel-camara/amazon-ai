package com.amazon.ecommerce.service;

import com.amazon.ecommerce.dto.request.CartItemRequest;
import com.amazon.ecommerce.entity.CartItem;
import com.amazon.ecommerce.entity.Product;
import com.amazon.ecommerce.entity.User;
import com.amazon.ecommerce.exception.BadRequestException;
import com.amazon.ecommerce.exception.ResourceNotFoundException;
import com.amazon.ecommerce.repository.CartItemRepository;
import com.amazon.ecommerce.repository.ProductRepository;
import com.amazon.ecommerce.repository.UserRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class CartService {

    private final CartItemRepository cartItemRepository;
    private final ProductRepository productRepository;
    private final UserRepository userRepository;

    public CartService(CartItemRepository cartItemRepository, ProductRepository productRepository,
                       UserRepository userRepository) {
        this.cartItemRepository = cartItemRepository;
        this.productRepository = productRepository;
        this.userRepository = userRepository;
    }

    public List<CartItem> getUserCart(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User", userId));
        return cartItemRepository.findByUser(user);
    }

    @Transactional
    public CartItem addToCart(Long userId, CartItemRequest request) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User", userId));

        Product product = productRepository.findById(request.getProductId())
                .orElseThrow(() -> new ResourceNotFoundException("Product", request.getProductId()));

        if (product.getQuantity() < request.getQuantity()) {
            throw new BadRequestException("Insufficient stock. Available: " + product.getQuantity());
        }

        var existing = cartItemRepository.findByUserAndProduct(user, product);
        if (existing.isPresent()) {
            CartItem item = existing.get();
            item.setQuantity(item.getQuantity() + request.getQuantity());
            if (item.getQuantity() > product.getQuantity()) {
                throw new BadRequestException("Insufficient stock. Available: " + product.getQuantity());
            }
            return cartItemRepository.save(item);
        }

        CartItem cartItem = CartItem.builder()
                .user(user)
                .product(product)
                .quantity(request.getQuantity())
                .build();
        return cartItemRepository.save(cartItem);
    }

    @Transactional
    public CartItem updateCartItem(Long userId, Long cartItemId, CartItemRequest request) {
        CartItem cartItem = cartItemRepository.findById(cartItemId)
                .orElseThrow(() -> new ResourceNotFoundException("CartItem", cartItemId));

        if (!cartItem.getUser().getId().equals(userId)) {
            throw new BadRequestException("Cart item does not belong to user");
        }

        Product product = cartItem.getProduct();
        if (product.getQuantity() < request.getQuantity()) {
            throw new BadRequestException("Insufficient stock. Available: " + product.getQuantity());
        }

        cartItem.setQuantity(request.getQuantity());
        return cartItemRepository.save(cartItem);
    }

    @Transactional
    public void removeFromCart(Long userId, Long cartItemId) {
        CartItem cartItem = cartItemRepository.findById(cartItemId)
                .orElseThrow(() -> new ResourceNotFoundException("CartItem", cartItemId));

        if (!cartItem.getUser().getId().equals(userId)) {
            throw new BadRequestException("Cart item does not belong to user");
        }

        cartItemRepository.delete(cartItem);
    }
}
