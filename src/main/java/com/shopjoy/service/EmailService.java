package com.shopjoy.service;

import com.shopjoy.dto.response.OrderResponse;

import java.util.concurrent.CompletableFuture;

public interface EmailService {

    CompletableFuture<Void> sendOrderConfirmationEmail(OrderResponse order, String userEmail);

    CompletableFuture<Void> sendOrderCancellationEmail(Integer orderId, String userEmail);

    CompletableFuture<Void> sendPaymentConfirmationEmail(OrderResponse order, String userEmail);
}

