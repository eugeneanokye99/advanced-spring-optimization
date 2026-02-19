package com.shopjoy.graphql.resolver.field;

import com.shopjoy.dto.response.CategoryResponse;
import com.shopjoy.dto.response.ProductResponse;
import com.shopjoy.service.CategoryService;
import com.shopjoy.service.ProductService;
import org.springframework.graphql.data.method.annotation.BatchMapping;
import org.springframework.stereotype.Controller;

import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

@Controller
public class CategoryFieldResolver {

    private final ProductService productService;

    public CategoryFieldResolver(ProductService productService) {
        this.productService = productService;
    }

    @BatchMapping(typeName = "Category", field = "products")
    public Map<CategoryResponse, List<ProductResponse>> products(List<CategoryResponse> categories) {
        List<Integer> categoryIds = categories.stream()
                .map(CategoryResponse::getId)
                .distinct()
                .collect(Collectors.toList());

        List<ProductResponse> allProducts = productService.getProductsByCategories(categoryIds);
        
        Map<Integer, List<ProductResponse>> productsByCategory = allProducts.stream()
                .collect(Collectors.groupingBy(ProductResponse::getCategoryId));

        return categories.stream()
                .collect(Collectors.toMap(
                        category -> category,
                        category -> productsByCategory.getOrDefault(category.getId(), java.util.Collections.emptyList())
                ));
    }
}
