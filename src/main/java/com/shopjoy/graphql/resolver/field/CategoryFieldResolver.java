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

    private final CategoryService categoryService;
    private final ProductService productService;

    public CategoryFieldResolver(CategoryService categoryService, ProductService productService) {
        this.categoryService = categoryService;
        this.productService = productService;
    }

    @BatchMapping(typeName = "Category", field = "parentCategory")
    public Map<CategoryResponse, CategoryResponse> parentCategory(List<CategoryResponse> categories) {
        List<Integer> parentIds = categories.stream()
                .map(CategoryResponse::getParentCategoryId)
                .filter(java.util.Objects::nonNull)
                .distinct()
                .collect(Collectors.toList());

        List<CategoryResponse> parents = categoryService.getCategoriesByIds(parentIds);
        Map<Integer, CategoryResponse> parentMap = parents.stream()
                .collect(Collectors.toMap(CategoryResponse::getCategoryId, Function.identity()));

        return categories.stream()
                .collect(Collectors.toMap(
                        category -> category,
                        category -> category.getParentCategoryId() != null ? parentMap.get(category.getParentCategoryId()) : null
                ));
    }

    @BatchMapping(typeName = "Category", field = "products")
    public Map<CategoryResponse, List<ProductResponse>> products(List<CategoryResponse> categories) {
        List<Integer> categoryIds = categories.stream()
                .map(CategoryResponse::getCategoryId)
                .distinct()
                .collect(Collectors.toList());

        List<ProductResponse> allProducts = productService.getProductsByCategories(categoryIds);
        
        Map<Integer, List<ProductResponse>> productsByCategory = allProducts.stream()
                .collect(Collectors.groupingBy(ProductResponse::getCategoryId));

        return categories.stream()
                .collect(Collectors.toMap(
                        category -> category,
                        category -> productsByCategory.getOrDefault(category.getCategoryId(), java.util.Collections.emptyList())
                ));
    }
}
