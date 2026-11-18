package com.reto.order.dto;

import com.reto.order.enums.OrderStatus;

import java.time.LocalDateTime;
import java.util.List;

//Es el contrato que Order Service devolverá al consumidor de la API.

public record OrderResponse(Long id, OrderStatus status, List<OrderItemResponse> items, LocalDateTime createdAt) {

}