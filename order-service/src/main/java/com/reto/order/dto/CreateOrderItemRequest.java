package com.reto.order.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Positive;

//Representa cada producto que el cliente quiere incluir dentro del pedido.

//evito que el id este vacio
//No permitimos cantidades 0 o negativas.
public record CreateOrderItemRequest(@NotBlank String productId, @Positive Integer quantity) {

}