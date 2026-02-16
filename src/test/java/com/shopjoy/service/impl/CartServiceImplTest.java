package com.shopjoy.service.impl;

import com.shopjoy.dto.request.AddToCartRequest;
import com.shopjoy.dto.response.CartItemResponse;
import com.shopjoy.dto.response.ProductResponse;
import com.shopjoy.entity.CartItem;
import com.shopjoy.entity.Product;
import com.shopjoy.entity.User;
import com.shopjoy.repository.CartItemRepository;
import com.shopjoy.repository.ProductRepository;
import com.shopjoy.repository.UserRepository;
import com.shopjoy.service.InventoryService;
import com.shopjoy.service.ProductService;
import com.shopjoy.dto.mapper.CartItemMapperStruct;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class CartServiceImplTest {

    @Mock
    private CartItemRepository cartItemRepository;

    @Mock
    private UserRepository userRepository;

    @Mock
    private ProductRepository productRepository;

    @Mock
    private InventoryService inventoryService;

    @Mock
    private ProductService productService;

    @Mock
    private CartItemMapperStruct cartItemMapper;

    @InjectMocks
    private CartServiceImpl cartService;

    private User user;
    private Product product;
    private CartItem cartItem;
    private AddToCartRequest addToCartRequest;

    @BeforeEach
    void setUp() {
        user = new User();
        user.setId(1);

        product = new Product();
        product.setId(1);
        product.setProductName("Test Product");

        cartItem = new CartItem();
        cartItem.setId(1);
        cartItem.setUser(user);
        cartItem.setProduct(product);
        cartItem.setQuantity(2);

        addToCartRequest = new AddToCartRequest();
        addToCartRequest.setUserId(1);
        addToCartRequest.setProductId(1);
        addToCartRequest.setQuantity(2);
    }

    @Test
    @DisplayName("Add to Cart - Success (New Item)")
    void addToCart_Success_NewItem() {
        ProductResponse pr = new ProductResponse();
        pr.setProductId(1);
        when(productService.getProductById(1)).thenReturn(pr);
        when(cartItemRepository.findByUser_IdAndProduct_Id(1, 1)).thenReturn(Optional.empty());
        when(inventoryService.hasAvailableStock(1, 2)).thenReturn(true);
        when(userRepository.getReferenceById(1)).thenReturn(user);
        when(productRepository.getReferenceById(1)).thenReturn(product);
        when(cartItemRepository.save(any(CartItem.class))).thenReturn(cartItem);
        when(cartItemMapper.toCartItemResponse(any(CartItem.class))).thenReturn(new CartItemResponse());

        CartItemResponse response = cartService.addToCart(addToCartRequest);

        assertThat(response).isNotNull();
        verify(cartItemRepository).save(any(CartItem.class));
    }

    @Test
    @DisplayName("Add to Cart - Success (Existing Item Update)")
    void addToCart_Success_ExistingItem() {
        ProductResponse pr = new ProductResponse();
        pr.setProductId(1);
        when(productService.getProductById(1)).thenReturn(pr);
        when(cartItemRepository.findByUser_IdAndProduct_Id(1, 1)).thenReturn(Optional.of(cartItem));
        when(inventoryService.hasAvailableStock(1, 2)).thenReturn(true);
        when(inventoryService.hasAvailableStock(1, 4)).thenReturn(true);
        when(cartItemRepository.save(any(CartItem.class))).thenReturn(cartItem);
        when(cartItemMapper.toCartItemResponse(any(CartItem.class))).thenReturn(new CartItemResponse());

        assertThat(cartItem.getQuantity()).isEqualTo(4);
        verify(cartItemRepository).save(cartItem);
    }
}
