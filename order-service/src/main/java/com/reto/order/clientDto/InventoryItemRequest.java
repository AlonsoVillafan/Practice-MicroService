package com.reto.order.clientDto;


//Este DTO representa el formato que Inventory Service espera recibir.

public record InventoryItemRequest(String productId, Integer quantity) {
}