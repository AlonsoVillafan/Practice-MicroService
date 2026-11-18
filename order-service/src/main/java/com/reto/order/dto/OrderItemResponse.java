package com.reto.order.dto;


//Representa un producto dentro de la respuesta de un pedido.
public record OrderItemResponse(String productId, Integer quantity) {

}