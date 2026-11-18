package com.reto.order.dto;

import jakarta.validation.Valid;

import java.util.List;

//USO RECORD PORQUE LOS DATOS QUE LLEGUEN DEBEN SER INMUTABLES
public record ReorderRequest(
        List<@Valid CreateOrderItemRequest> items
) {
}