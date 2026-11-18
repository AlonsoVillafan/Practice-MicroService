package com.reto.order.clientDto;

//Debe coincidir con el JSON que devuelve actualmente Inventory Service.
public record InventoryItemResponse(String productId, Integer requestedQuantity, Integer stock, boolean available) {

}