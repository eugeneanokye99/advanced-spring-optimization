package com.shopjoy.graphql.resolver.field;

import com.shopjoy.dto.response.CategoryResponse;
import com.shopjoy.dto.response.ProductResponse;
import com.shopjoy.service.CategoryService;
import org.springframework.graphql.data.method.annotation.BatchMapping;
import org.springframework.stereotype.Controller;

import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

@Controller
public class ProductFieldResolver {

    private final CategoryService categoryService;

    public ProductFieldResolver(CategoryService categoryService) {
        this.categoryService = categoryService;
    }

    @BatchMapping(typeName = "Product", field = "category")
    public Map<ProductResponse, CategoryResponse> category(List<ProductResponse> products) {
        List<Integer> categoryIds = products.stream()
                .map(ProductResponse::getCategoryId)
                .distinct()
                .collect(Collectors.toList());

        List<CategoryResponse> categories = categoryService.getCategoriesByIds(categoryIds);
        Map<Integer, CategoryResponse> categoryMap = categories.stream()
                .collect(Collectors.toMap(CategoryResponse::getCategoryId, Function.identity()));

        return products.stream()
                .collect(Collectors.toMap(
                        product -> product,
                        product -> categoryMap.get(product.getCategoryId())
                ));
    }
}
