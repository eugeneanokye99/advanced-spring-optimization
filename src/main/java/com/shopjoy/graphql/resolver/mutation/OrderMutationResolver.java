package com.shopjoy.graphql.resolver.mutation;

import com.shopjoy.dto.mapper.GraphQLMapperStruct;
import com.shopjoy.dto.response.OrderResponse;
import com.shopjoy.entity.OrderStatus;
import com.shopjoy.graphql.input.UpdateOrderInput;
import com.shopjoy.service.OrderService;
import jakarta.validation.Valid;
import lombok.AllArgsConstructor;
import org.springframework.graphql.data.method.annotation.Argument;
import org.springframework.graphql.data.method.annotation.MutationMapping;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;

import java.util.concurrent.CompletableFuture;

@Controller
@AllArgsConstructor
public class OrderMutationResolver {

    private final OrderService orderService;
    private final GraphQLMapperStruct graphQLMapper;

    @MutationMapping
    @PreAuthorize("isAuthenticated()")
    public CompletableFuture<OrderResponse> updateOrder(@Argument Long id, @Argument @Valid UpdateOrderInput input) {
        return CompletableFuture.supplyAsync(() -> {
            var request = graphQLMapper.toUpdateOrderRequest(input);
            return orderService.updateOrder(id.intValue(), request);
        });
    }

    @MutationMapping
    @PreAuthorize("isAuthenticated()")
    public CompletableFuture<Boolean> deleteOrder(@Argument Long id) {
        return CompletableFuture.supplyAsync(() -> {
            orderService.deleteOrder(id.intValue());
            return true;
        });
    }

    @MutationMapping
    @PreAuthorize("isAuthenticated()")
    public CompletableFuture<OrderResponse> cancelOrder(@Argument Long id) {
        return CompletableFuture.supplyAsync(() ->
            orderService.cancelOrder(id.intValue())
        );
    }

    @MutationMapping
    @PreAuthorize("hasRole('ADMIN')")
    public CompletableFuture<OrderResponse> updateOrderStatus(@Argument Long id, @Argument String status) {
        return CompletableFuture.supplyAsync(() -> {
            OrderStatus orderStatus = OrderStatus.valueOf(status);
            return orderService.updateOrderStatus(id.intValue(), orderStatus);
        });
    }
}
