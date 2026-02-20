package com.shopjoy.service.impl;

import com.shopjoy.dto.mapper.CartItemMapperStruct;
import com.shopjoy.dto.request.AddToCartRequest;
import com.shopjoy.dto.response.CartItemResponse;
import com.shopjoy.dto.response.ProductResponse;
import com.shopjoy.entity.CartItem;
import com.shopjoy.exception.InsufficientStockException;
import com.shopjoy.exception.ResourceNotFoundException;
import com.shopjoy.exception.ValidationException;
import com.shopjoy.repository.CartItemRepository;
import com.shopjoy.repository.UserRepository;
import com.shopjoy.repository.ProductRepository;
import com.shopjoy.service.CartService;
import com.shopjoy.service.InventoryService;
import com.shopjoy.service.ProductService;
import com.shopjoy.util.SecurityUtil;
import lombok.AllArgsConstructor;

import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.cache.annotation.Caching;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;


import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * The type Cart service.
 */
@Service
@Transactional(readOnly = true)
@AllArgsConstructor
public class CartServiceImpl implements CartService {

    private final CartItemRepository cartItemRepository;
    private final UserRepository userRepository;
    private final ProductRepository productRepository;
    private final ProductService productService;
    private final InventoryService inventoryService;
    private final CartItemMapperStruct cartItemMapper;

    @Override
    @Transactional()
    @Caching(evict = {
        @CacheEvict(value = "cartItems", key = "#request.userId"),
        @CacheEvict(value = "cartTotal", key = "#request.userId"),
        @CacheEvict(value = "cartCount", key = "#request.userId")
    })
    public CartItemResponse addToCart(AddToCartRequest request) {
        if (request.getQuantity() <= 0) {
            throw new ValidationException("quantity", "must be positive");
        }

        productService.getProductById(request.getProductId());

        if (!inventoryService.hasAvailableStock(request.getProductId(), request.getQuantity())) {
            throw new InsufficientStockException(request.getProductId(), request.getQuantity(), 0);
        }

        Optional<CartItem> existingItem = cartItemRepository.findByUserIdAndProductId(request.getUserId(),
                request.getProductId());

        if (existingItem.isPresent()) {
            CartItem cartItem = existingItem.get();
            int newQuantity = cartItem.getQuantity() + request.getQuantity();

            if (!inventoryService.hasAvailableStock(request.getProductId(), newQuantity)) {
                throw new InsufficientStockException(request.getProductId(), newQuantity, 0);
            }

            cartItem.setQuantity(newQuantity);
            CartItem updatedItem = cartItemRepository.save(cartItem);
            return cartItemMapper.toCartItemResponse(updatedItem);
        } else {
            CartItem cartItem = CartItem.builder()
                    .user(userRepository.getReferenceById(request.getUserId()))
                    .product(productRepository.getReferenceById(request.getProductId()))
                    .quantity(request.getQuantity())
                    .build();

            CartItem savedItem = cartItemRepository.save(cartItem);
            return cartItemMapper.toCartItemResponse(savedItem);
        }
    }

    @Override
    @Transactional()
    @Caching(evict = {
        @CacheEvict(value = "cartItems", allEntries = true),
        @CacheEvict(value = "cartTotal", allEntries = true),
        @CacheEvict(value = "cartCount", allEntries = true)
    })
    public CartItemResponse updateCartItemQuantity(Integer cartItemId, int newQuantity) {
        if (newQuantity <= 0) {
            throw new ValidationException("quantity", "must be positive");
        }
        
        Integer cartItemOwnerId = cartItemRepository.findUserIdByCartItemId(cartItemId);
        if (cartItemOwnerId == null) {
            throw new ResourceNotFoundException("CartItem", "id", cartItemId);
        }
        
        if (!SecurityUtil.isAdmin() && !SecurityUtil.isCurrentUser(cartItemOwnerId)) {
            throw new AccessDeniedException("You do not have permission to update this cart item");
        }

        CartItem cartItem = cartItemRepository.findById(cartItemId)
                .orElseThrow(() -> new ResourceNotFoundException("CartItem", "id", cartItemId));

        if (!inventoryService.hasAvailableStock(cartItem.getProduct().getId(), newQuantity)) {
            throw new InsufficientStockException(cartItem.getProduct().getId(), newQuantity, 0);
        }

        cartItem.setQuantity(newQuantity);
        CartItem updatedItem = cartItemRepository.save(cartItem);
        return cartItemMapper.toCartItemResponse(updatedItem);
    }

    @Override
    @Transactional()
    @Caching(evict = {
        @CacheEvict(value = "cartItems", allEntries = true),
        @CacheEvict(value = "cartTotal", allEntries = true),
        @CacheEvict(value = "cartCount", allEntries = true)
    })
    public void removeFromCart(Integer cartItemId) {
        Integer cartItemOwnerId = cartItemRepository.findUserIdByCartItemId(cartItemId);
        if (cartItemOwnerId == null) {
            throw new ResourceNotFoundException("CartItem", "id", cartItemId);
        }
        
        if (!SecurityUtil.isAdmin() && !SecurityUtil.isCurrentUser(cartItemOwnerId)) {
            throw new AccessDeniedException("You do not have permission to remove this cart item");
        }

        cartItemRepository.deleteById(cartItemId);
    }

    @Override
    @Cacheable(value = "cartItems", key = "#userId", cacheManager = "shortCacheManager")
    public List<CartItemResponse> getCartItems(Integer userId) {
        List<CartItem> items = cartItemRepository.findByUserId(userId);
        return items.stream()
                .map(cartItemMapper::toCartItemResponse)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional()
    @Caching(evict = {
        @CacheEvict(value = "cartItems", key = "#userId"),
        @CacheEvict(value = "cartTotal", key = "#userId"),
        @CacheEvict(value = "cartCount", key = "#userId")
    })
    public void clearCart(Integer userId) {
        cartItemRepository.deleteByUserId(userId);
    }

    @Override
    @Cacheable(value = "cartTotal", key = "#userId", cacheManager = "shortCacheManager")
    public double getCartTotal(Integer userId) {
        List<CartItem> items = cartItemRepository.findByUserId(userId);

        return items.stream()
                .mapToDouble(item -> {
                    ProductResponse product = productService.getProductById(item.getProduct().getId());
                    return product.getPrice() * item.getQuantity();
                })
                .sum();
    }

    @Override
    @Cacheable(value = "cartCount", key = "#userId", cacheManager = "shortCacheManager")
    public int getCartItemCount(Integer userId) {
        List<CartItem> items = cartItemRepository.findByUserId(userId);
        return items.stream()
                .mapToInt(CartItem::getQuantity)
                .sum();
    }

}
