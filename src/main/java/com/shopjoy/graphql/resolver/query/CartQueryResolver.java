package com.shopjoy.graphql.resolver.query;

import com.shopjoy.dto.response.CartItemResponse;
import com.shopjoy.service.CartService;
import org.springframework.graphql.data.method.annotation.Argument;
import org.springframework.graphql.data.method.annotation.QueryMapping;
import org.springframework.stereotype.Controller;

import java.util.List;

@Controller
public class CartQueryResolver {

    private final CartService cartService;

    public CartQueryResolver(CartService cartService) {
        this.cartService = cartService;
    }

    @QueryMapping
    public List<CartItemResponse> cartItems(@Argument Long userId) {
        if (userId == null) {
            return java.util.Collections.emptyList();
        }
        return cartService.getCartItems(userId.intValue());
    }
}
