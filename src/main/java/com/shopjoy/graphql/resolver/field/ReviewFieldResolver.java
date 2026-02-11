package com.shopjoy.graphql.resolver.field;

import com.shopjoy.dto.response.ProductResponse;
import com.shopjoy.dto.response.ReviewResponse;
import com.shopjoy.dto.response.UserResponse;
import com.shopjoy.service.ProductService;
import com.shopjoy.service.UserService;
import org.springframework.graphql.data.method.annotation.BatchMapping;
import org.springframework.stereotype.Controller;

import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

@Controller
public class ReviewFieldResolver {

    private final ProductService productService;
    private final UserService userService;

    public ReviewFieldResolver(ProductService productService, UserService userService) {
        this.productService = productService;
        this.userService = userService;
    }

    @BatchMapping(typeName = "Review", field = "product")
    public Map<ReviewResponse, ProductResponse> product(List<ReviewResponse> reviews) {
        List<Integer> productIds = reviews.stream()
                .map(ReviewResponse::getProductId)
                .distinct()
                .collect(Collectors.toList());

        List<ProductResponse> products = productService.getProductsByIds(productIds);
        Map<Integer, ProductResponse> productMap = products.stream()
                .collect(Collectors.toMap(ProductResponse::getProductId, Function.identity()));

        return reviews.stream()
                .collect(Collectors.toMap(
                        review -> review,
                        review -> productMap.get(review.getProductId())
                ));
    }

    @BatchMapping(typeName = "Review", field = "user")
    public Map<ReviewResponse, UserResponse> user(List<ReviewResponse> reviews) {
        List<Integer> userIds = reviews.stream()
                .map(ReviewResponse::getUserId)
                .distinct()
                .collect(Collectors.toList());

        List<UserResponse> users = userService.getUsersByIds(userIds);
        Map<Integer, UserResponse> userMap = users.stream()
                .collect(Collectors.toMap(UserResponse::getUserId, Function.identity()));

        return reviews.stream()
                .collect(Collectors.toMap(
                        review -> review,
                        review -> userMap.get(review.getUserId())
                ));
    }
}
