package com.shopjoy.graphql.resolver.query;

import com.shopjoy.dto.filter.ProductFilter;
import com.shopjoy.dto.response.ProductResponse;
import com.shopjoy.graphql.input.ProductFilterInput;
import com.shopjoy.graphql.type.PageInfo;
import com.shopjoy.graphql.type.ProductConnection;
import com.shopjoy.service.ProductService;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.graphql.data.method.annotation.Argument;
import org.springframework.graphql.data.method.annotation.QueryMapping;
import org.springframework.stereotype.Controller;

@Controller
public class ProductQueryResolver {

    private final ProductService productService;

    public ProductQueryResolver(ProductService productService) {
        this.productService = productService;
    }

    @QueryMapping
    public ProductResponse product(@Argument Long id) {
        return productService.getProductById(id.intValue());
    }

    @QueryMapping
    public ProductConnection products(
            @Argument ProductFilterInput filter,
            @Argument Integer page,
            @Argument Integer size,
            @Argument String sortBy,
            @Argument String sortDirection) {
        int pageNum = page != null ? page : 0;
        int pageSize = size != null ? size : 20;
        String sort = sortBy != null ? sortBy : "id";
        String direction = sortDirection != null ? sortDirection : "ASC";

        ProductFilter productFilter = null;
        if (filter != null) {
            productFilter = ProductFilter.builder()
                    .minPrice(filter.minPrice() != null ? filter.minPrice().doubleValue() : null)
                    .maxPrice(filter.maxPrice() != null ? filter.maxPrice().doubleValue() : null)
                    .categoryId(filter.categoryId() != null ? filter.categoryId().intValue() : null)
                    .searchTerm(filter.searchTerm())
                    .inStock(filter.inStock())
                    .build();
        }

        // Create Pageable object and call correct service method
        Sort sortObj = Sort.by(Sort.Direction.fromString(direction), sort);
        Pageable pageable = PageRequest.of(pageNum, pageSize, sortObj);
        
        Page<ProductResponse> productsPage = productService.getProductsWithFilters(
                productFilter,
                pageable,
                sort,
                direction,
                null // Default to DATABASE
        );

        PageInfo pageInfo = new PageInfo(
                pageNum,
                pageSize,
                productsPage.getTotalElements(),
                productsPage.getTotalPages());

        return new ProductConnection(productsPage.getContent(), pageInfo);
    }
}
