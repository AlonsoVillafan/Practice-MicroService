package com.reto.order.dto;

import com.reto.order.enums.OrderStatus;
import java.time.LocalDateTime;

//Representa un cambio de estado dentro del historial de un pedido.
public record OrderHistoryResponse(

        OrderStatus previousStatus,
        OrderStatus newStatus,
        LocalDateTime createdAt
) {
}