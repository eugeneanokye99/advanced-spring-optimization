package com.shopjoy.graphql.resolver.field;

import com.shopjoy.dto.response.InventoryResponse;
import com.shopjoy.dto.response.ProductResponse;
import com.shopjoy.service.ProductService;
import org.springframework.graphql.data.method.annotation.BatchMapping;
import org.springframework.stereotype.Controller;

import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

@Controller
public class InventoryFieldResolver {

    private final ProductService productService;

    public InventoryFieldResolver(ProductService productService) {
        this.productService = productService;
    }

    @BatchMapping(typeName = "Inventory", field = "product")
    public Map<InventoryResponse, ProductResponse> product(List<InventoryResponse> inventories) {
        List<Integer> productIds = inventories.stream()
                .map(InventoryResponse::getProductId)
                .distinct()
                .collect(Collectors.toList());

        List<ProductResponse> products = productService.getProductsByIds(productIds);
        Map<Integer, ProductResponse> productMap = products.stream()
                .collect(Collectors.toMap(ProductResponse::getProductId, Function.identity()));

        return inventories.stream()
                .collect(Collectors.toMap(
                        inventory -> inventory,
                        inventory -> productMap.get(inventory.getProductId())
                ));
    }
}
