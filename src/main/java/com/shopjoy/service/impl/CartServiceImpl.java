package com.shopjoy.service.impl;

import com.shopjoy.dto.mapper.CartItemMapper;
import com.shopjoy.dto.request.AddToCartRequest;
import com.shopjoy.dto.response.CartItemResponse;
import com.shopjoy.dto.response.ProductResponse;
import com.shopjoy.entity.CartItem;
import com.shopjoy.exception.InsufficientStockException;
import com.shopjoy.exception.ResourceNotFoundException;
import com.shopjoy.exception.ValidationException;
import com.shopjoy.repository.CartItemRepository;
import com.shopjoy.service.CartService;
import com.shopjoy.service.InventoryService;
import com.shopjoy.service.ProductService;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * The type Cart service.
 */
@Service
@Transactional(readOnly = true)
public class CartServiceImpl implements CartService {



    private final CartItemRepository cartItemRepository;
    private final ProductService productService;
    private final InventoryService inventoryService;

    /**
     * Instantiates a new Cart service.
     *
     * @param cartItemRepository the cart item repository
     * @param productService     the product service
     * @param inventoryService   the inventory service
     */
    public CartServiceImpl(CartItemRepository cartItemRepository,
            ProductService productService,
            InventoryService inventoryService) {
        this.cartItemRepository = cartItemRepository;
        this.productService = productService;
        this.inventoryService = inventoryService;
    }

    @Override
    @Transactional()
    public CartItemResponse addToCart(AddToCartRequest request) {
        if (request.getQuantity() <= 0) {
            throw new ValidationException("quantity", "must be positive");
        }

        productService.getProductById(request.getProductId());

        if (!inventoryService.hasAvailableStock(request.getProductId(), request.getQuantity())) {
            throw new InsufficientStockException(request.getProductId(), request.getQuantity(), 0);
        }

        Optional<CartItem> existingItem = cartItemRepository.findByUserAndProduct(request.getUserId(),
                request.getProductId());

        if (existingItem.isPresent()) {
            CartItem cartItem = existingItem.get();
            int newQuantity = cartItem.getQuantity() + request.getQuantity();

            if (!inventoryService.hasAvailableStock(request.getProductId(), newQuantity)) {
                throw new InsufficientStockException(request.getProductId(), newQuantity, 0);
            }

            cartItem.setQuantity(newQuantity);
            CartItem updatedItem = cartItemRepository.update(cartItem);
            return convertToResponse(updatedItem);
        } else {
            CartItem cartItem = CartItem.builder()
                    .userId(request.getUserId())
                    .productId(request.getProductId())
                    .quantity(request.getQuantity())
                    .createdAt(LocalDateTime.now())
                    .build();

            CartItem savedItem = cartItemRepository.save(cartItem);
            return convertToResponse(savedItem);
        }
    }

    @Override
    @Transactional()
    public CartItemResponse updateCartItemQuantity(Integer cartItemId, int newQuantity) {
        if (newQuantity <= 0) {
            throw new ValidationException("quantity", "must be positive");
        }

        CartItem cartItem = cartItemRepository.findById(cartItemId)
                .orElseThrow(() -> new ResourceNotFoundException("CartItem", "id", cartItemId));

        if (!inventoryService.hasAvailableStock(cartItem.getProductId(), newQuantity)) {
            throw new InsufficientStockException(cartItem.getProductId(), newQuantity, 0);
        }

        cartItem.setQuantity(newQuantity);
        CartItem updatedItem = cartItemRepository.update(cartItem);
        return convertToResponse(updatedItem);
    }

    @Override
    @Transactional()
    public void removeFromCart(Integer cartItemId) {
        if (!cartItemRepository.existsById(cartItemId)) {
            throw new ResourceNotFoundException("CartItem", "id", cartItemId);
        }

        cartItemRepository.delete(cartItemId);
    }

    @Override
    public List<CartItemResponse> getCartItems(Integer userId) {
        List<CartItem> items = cartItemRepository.findByUserId(userId);
        return items.stream()
                .map(this::convertToResponse)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional()
    public void clearCart(Integer userId) {
        cartItemRepository.clearCart(userId);
    }

    @Override
    public double getCartTotal(Integer userId) {
        List<CartItem> items = cartItemRepository.findByUserId(userId);

        return items.stream()
                .mapToDouble(item -> {
                    ProductResponse product = productService.getProductById(item.getProductId());
                    return product.getPrice() * item.getQuantity();
                })
                .sum();
    }

    @Override
    public int getCartItemCount(Integer userId) {
        List<CartItem> items = cartItemRepository.findByUserId(userId);
        return items.stream()
                .mapToInt(CartItem::getQuantity)
                .sum();
    }

    private CartItemResponse convertToResponse(CartItem cartItem) {
        String productName = "Unknown Product";
        double price = 0.0;
        try {
            ProductResponse product = productService.getProductById(cartItem.getProductId());
            productName = product.getProductName();
            price = product.getPrice();
        } catch (Exception e) {
            // Ignore product fetch errors
        }
        return CartItemMapper.toCartItemResponse(cartItem, productName, price);
    }
}
