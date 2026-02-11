package com.shopjoy.graphql.resolver.query;

import com.shopjoy.dto.response.OrderResponse;
import com.shopjoy.graphql.type.OrderConnection;
import com.shopjoy.graphql.type.PageInfo;
import com.shopjoy.service.OrderService;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.graphql.data.method.annotation.Argument;
import org.springframework.graphql.data.method.annotation.QueryMapping;
import org.springframework.stereotype.Controller;

import java.util.List;

@Controller
public class OrderQueryResolver {

    private final OrderService orderService;

    public OrderQueryResolver(OrderService orderService) {
        this.orderService = orderService;
    }

    @QueryMapping
    public OrderResponse order(@Argument Long id) {
        return orderService.getOrderById(id.intValue());
    }

    @QueryMapping
    public OrderConnection orders(
            @Argument Long userId,
            @Argument Integer page,
            @Argument Integer size
    ) {
        int pageNum = page != null ? page : 0;
        int pageSize = size != null ? size : 20;
        Pageable pageable = PageRequest.of(pageNum, pageSize);

        Page<OrderResponse> orderPage;
        if (userId != null) {
            orderPage = orderService.getOrdersByUserPaginated(userId.intValue(), pageable);
        } else {
            orderPage = orderService.getAllOrdersPaginated(pageable);
        }
        
        PageInfo pageInfo = new PageInfo(
                pageNum,
                pageSize,
                orderPage.getTotalElements(),
                orderPage.getTotalPages()
        );

        return new OrderConnection(orderPage.getContent(), pageInfo);
    }
}
