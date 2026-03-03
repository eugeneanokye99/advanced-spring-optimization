package com.shopjoy.service.impl;

import com.shopjoy.dto.response.OrderResponse;
import com.shopjoy.service.EmailService;
import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import java.util.concurrent.CompletableFuture;

@Slf4j
@Service
@RequiredArgsConstructor
public class EmailServiceImpl implements EmailService {

    private final JavaMailSender mailSender;

    @Value("${app.mail.from}")
    private String mailFrom;

    @Value("${app.mail.from-name}")
    private String mailFromName;

    @Override
    @Async("appTaskExecutor")
    public CompletableFuture<Void> sendOrderConfirmationEmail(OrderResponse order, String userEmail) {
        long startTime = System.nanoTime();

        try {
            log.info("Sending order confirmation email to {} for order {}", userEmail, order.getId());

            String subject = "Order Confirmation - Order #" + order.getId();
            String htmlContent = buildOrderConfirmationEmailBody(order);

            sendHtmlEmail(userEmail, subject, htmlContent);

            long executionTimeMs = (System.nanoTime() - startTime) / 1_000_000;
            log.info("Order confirmation email sent successfully in {}ms to {}", executionTimeMs, userEmail);

            return CompletableFuture.completedFuture(null);

        } catch (Exception e) {
            long executionTimeMs = (System.nanoTime() - startTime) / 1_000_000;
            log.error("Failed to send order confirmation email after {}ms: {}", executionTimeMs, e.getMessage(), e);
            return CompletableFuture.failedFuture(e);
        }
    }

    @Override
    @Async("appTaskExecutor")
    public CompletableFuture<Void> sendOrderCancellationEmail(Integer orderId, String userEmail) {
        long startTime = System.nanoTime();

        try {
            log.info("Sending order cancellation email to {} for order {}", userEmail, orderId);

            String subject = "Order Cancelled - Order #" + orderId;
            String htmlContent = buildOrderCancellationEmailBody(orderId);

            sendHtmlEmail(userEmail, subject, htmlContent);

            long executionTimeMs = (System.nanoTime() - startTime) / 1_000_000;
            log.info("Order cancellation email sent successfully in {}ms to {}", executionTimeMs, userEmail);

            return CompletableFuture.completedFuture(null);

        } catch (Exception e) {
            long executionTimeMs = (System.nanoTime() - startTime) / 1_000_000;
            log.error("Failed to send order cancellation email after {}ms: {}", executionTimeMs, e.getMessage(), e);
            return CompletableFuture.failedFuture(e);
        }
    }

    @Override
    @Async("appTaskExecutor")
    public CompletableFuture<Void> sendPaymentConfirmationEmail(OrderResponse order, String userEmail) {
        long startTime = System.nanoTime();

        try {
            log.info("Sending payment confirmation email to {} for order {}", userEmail, order.getId());

            String subject = "Payment Confirmed - Order #" + order.getId();
            String htmlContent = buildPaymentConfirmationEmailBody(order);

            sendHtmlEmail(userEmail, subject, htmlContent);

            long executionTimeMs = (System.nanoTime() - startTime) / 1_000_000;
            log.info("Payment confirmation email sent successfully in {}ms to {}", executionTimeMs, userEmail);

            return CompletableFuture.completedFuture(null);

        } catch (Exception e) {
            long executionTimeMs = (System.nanoTime() - startTime) / 1_000_000;
            log.error("Failed to send payment confirmation email after {}ms: {}", executionTimeMs, e.getMessage(), e);
            return CompletableFuture.failedFuture(e);
        }
    }

    private void sendHtmlEmail(String to, String subject, String htmlContent) throws MessagingException {
        MimeMessage message = mailSender.createMimeMessage();
        MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");

        try {
            helper.setFrom(mailFrom, mailFromName);
        } catch (Exception e) {
            helper.setFrom(mailFrom);
        }
        helper.setTo(to);
        helper.setSubject(subject);
        helper.setText(htmlContent, true);

        mailSender.send(message);
    }

    private String buildOrderConfirmationEmailBody(OrderResponse order) {
        return String.format("""
            <!DOCTYPE html>
            <html>
            <head>
                <style>
                    body { font-family: Arial, sans-serif; line-height: 1.6; color: #333; }
                    .container { max-width: 600px; margin: 0 auto; padding: 20px; }
                    .header { background-color: #4CAF50; color: white; padding: 20px; text-align: center; }
                    .content { padding: 20px; background-color: #f9f9f9; }
                    .order-details { background-color: white; padding: 15px; margin: 15px 0; border-radius: 5px; }
                    .footer { text-align: center; padding: 20px; color: #666; font-size: 12px; }
                    .btn { display: inline-block; padding: 10px 20px; background-color: #4CAF50; color: white; text-decoration: none; border-radius: 5px; margin: 10px 0; }
                </style>
            </head>
            <body>
                <div class="container">
                    <div class="header">
                        <h1>Order Confirmation</h1>
                    </div>
                    <div class="content">
                        <p>Dear Customer,</p>
                        <p>Thank you for your order! Your order has been successfully placed.</p>

                        <div class="order-details">
                            <h3>Order Details:</h3>
                            <p><strong>Order ID:</strong> #%d</p>
                            <p><strong>Order Date:</strong> %s</p>
                            <p><strong>Total Amount:</strong> $%.2f</p>
                            <p><strong>Status:</strong> %s</p>
                            <p><strong>Shipping Address:</strong> %s</p>
                        </div>
                        
                        <p>We will notify you once your order is shipped.</p>
                        <p>Thank you for shopping with us!</p>
                    </div>
                    <div class="footer">
                        <p>&copy; 2026 ShopJoy E-Commerce. All rights reserved.</p>
                        <p>If you have any questions, please contact our customer support.</p>
                    </div>
                </div>
            </body>
            </html>
            """,
            order.getId(),
            order.getOrderDate(),
            order.getTotalAmount(),
            order.getStatus(),
            order.getShippingAddress()
        );
    }

    private String buildOrderCancellationEmailBody(Integer orderId) {
        return String.format("""
            <!DOCTYPE html>
            <html>
            <head>
                <style>
                    body { font-family: Arial, sans-serif; line-height: 1.6; color: #333; }
                    .container { max-width: 600px; margin: 0 auto; padding: 20px; }
                    .header { background-color: #f44336; color: white; padding: 20px; text-align: center; }
                    .content { padding: 20px; background-color: #f9f9f9; }
                    .order-details { background-color: white; padding: 15px; margin: 15px 0; border-radius: 5px; }
                    .footer { text-align: center; padding: 20px; color: #666; font-size: 12px; }
                </style>
            </head>
            <body>
                <div class="container">
                    <div class="header">
                        <h1>Order Cancelled</h1>
                    </div>
                    <div class="content">
                        <p>Dear Customer,</p>
                        <p>Your order #%d has been cancelled as requested.</p>

                        <div class="order-details">
                            <p>If you did not request this cancellation or have any questions,
                            please contact our customer support immediately.</p>
                        </div>
                        
                        <p>We hope to serve you again soon!</p>
                    </div>
                    <div class="footer">
                        <p>&copy; 2026 ShopJoy E-Commerce. All rights reserved.</p>
                        <p>Customer Support: support@shopjoy.com</p>
                    </div>
                </div>
            </body>
            </html>
            """,
            orderId
        );
    }

    private String buildPaymentConfirmationEmailBody(OrderResponse order) {
        return String.format("""
            <!DOCTYPE html>
            <html>
            <head>
                <style>
                    body { font-family: Arial, sans-serif; line-height: 1.6; color: #333; }
                    .container { max-width: 600px; margin: 0 auto; padding: 20px; }
                    .header { background-color: #2196F3; color: white; padding: 20px; text-align: center; }
                    .content { padding: 20px; background-color: #f9f9f9; }
                    .order-details { background-color: white; padding: 15px; margin: 15px 0; border-radius: 5px; }
                    .footer { text-align: center; padding: 20px; color: #666; font-size: 12px; }
                    .success { color: #4CAF50; font-weight: bold; }
                </style>
            </head>
            <body>
                <div class="container">
                    <div class="header">
                        <h1>Payment Confirmed</h1>
                    </div>
                    <div class="content">
                        <p>Dear Customer,</p>
                        <p class="success">Your payment has been successfully processed!</p>

                        <div class="order-details">
                            <h3>Payment Details:</h3>
                            <p><strong>Order ID:</strong> #%d</p>
                            <p><strong>Amount Paid:</strong> $%.2f</p>
                            <p><strong>Payment Status:</strong> %s</p>
                        </div>
                        
                        <p>Your order is now being processed and will be shipped soon.</p>
                        <p>Thank you for your payment!</p>
                    </div>
                    <div class="footer">
                        <p>&copy; 2026 ShopJoy E-Commerce. All rights reserved.</p>
                        <p>Track your order at shopjoy.com/orders</p>
                    </div>
                </div>
            </body>
            </html>
            """,
            order.getId(),
            order.getTotalAmount(),
            order.getPaymentStatus()
        );
    }
}


