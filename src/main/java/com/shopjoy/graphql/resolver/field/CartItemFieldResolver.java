package com.shopjoy.graphql.resolver.field;

import com.shopjoy.dto.response.CartItemResponse;
import com.shopjoy.dto.response.ProductResponse;
import com.shopjoy.service.ProductService;
import org.springframework.graphql.data.method.annotation.BatchMapping;
import org.springframework.stereotype.Controller;

import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

@Controller
public class CartItemFieldResolver {

    private final ProductService productService;

    public CartItemFieldResolver(ProductService productService) {
        this.productService = productService;
    }

    @BatchMapping(typeName = "CartItem", field = "product")
    public Map<CartItemResponse, ProductResponse> product(List<CartItemResponse> cartItems) {
        List<Integer> productIds = cartItems.stream()
                .map(CartItemResponse::getProductId)
                .distinct()
                .collect(Collectors.toList());

        List<ProductResponse> products = productService.getProductsByIds(productIds);
        Map<Integer, ProductResponse> productMap = products.stream()
                .collect(Collectors.toMap(ProductResponse::getProductId, Function.identity()));

        return cartItems.stream()
                .collect(Collectors.toMap(
                        cartItem -> cartItem,
                        cartItem -> productMap.get(cartItem.getProductId())
                ));
    }
}
