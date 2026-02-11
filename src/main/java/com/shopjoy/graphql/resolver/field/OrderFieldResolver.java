package com.shopjoy.graphql.resolver.field;

import com.shopjoy.dto.response.OrderResponse;
import com.shopjoy.dto.response.UserResponse;
import com.shopjoy.service.UserService;
import org.springframework.graphql.data.method.annotation.BatchMapping;
import org.springframework.stereotype.Controller;

import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

@Controller
public class OrderFieldResolver {

    private final UserService userService;

    public OrderFieldResolver(UserService userService) {
        this.userService = userService;
    }

    @BatchMapping(typeName = "Order", field = "user")
    public Map<OrderResponse, UserResponse> user(List<OrderResponse> orders) {
        List<Integer> userIds = orders.stream()
                .map(OrderResponse::getUserId)
                .distinct()
                .collect(Collectors.toList());

        List<UserResponse> users = userService.getUsersByIds(userIds);
        Map<Integer, UserResponse> userMap = users.stream()
                .collect(Collectors.toMap(UserResponse::getUserId, Function.identity()));

        return orders.stream()
                .collect(Collectors.toMap(
                        order -> order,
                        order -> userMap.get(order.getUserId())
                ));
    }
}
