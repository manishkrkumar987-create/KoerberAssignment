package com.koerber.assignment.service;

import com.koerber.assignment.dto.OrderRequest;
import com.koerber.assignment.dto.OrderResponse;
import com.koerber.assignment.entity.Order;
import com.koerber.assignment.repository.OrderRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.client.RestTemplate;

import java.time.LocalDate;
import java.util.Collections;
import java.util.List;

@Service
public class OrderService {
    @Autowired
    private OrderRepository orderRepository;

    @Autowired
    private RestTemplate restTemplate;

    private final String INVENTORY_SERVICE_URL = "http://localhost:8081/inventory";

    @Transactional
    public OrderResponse placeOrder(OrderRequest request) {
        try {
            // 1. Call Inventory (Updated to capture batch IDs)
            String updateUrl = INVENTORY_SERVICE_URL + "/update?productId=" + request.getProductId() + "&quantity=" + request.getQuantity();

            // We use ParameterizedTypeReference to get the List<Long> back
            ResponseEntity<List<Long>> response = restTemplate.exchange(
                    updateUrl, HttpMethod.POST, null, new ParameterizedTypeReference<List<Long>>() {});

            List<Long> batchIds = response.getBody();

            // 2. Save Order only if inventory update succeeded
            Order order = Order.builder()
                    .productId(request.getProductId())
                    .productName("Product " + request.getProductId())
                    .quantity(request.getQuantity())
                    .status("PLACED")
                    .orderDate(LocalDate.now())
                    .build();

            Order savedOrder = orderRepository.save(order);

            return OrderResponse.builder()
                    .orderId(savedOrder.getOrderId())
                    .productId(savedOrder.getProductId())
                    .productName(savedOrder.getProductName())
                    .quantity(savedOrder.getQuantity())
                    .status(savedOrder.getStatus())
                    .reservedFromBatchIds(batchIds) // Now correctly populated
                    .message("Order placed. Inventory reserved.")
                    .build();
        } catch (Exception e) {
            throw new RuntimeException("Inventory Update Failed: " + e.getMessage());
        }
    }
}
