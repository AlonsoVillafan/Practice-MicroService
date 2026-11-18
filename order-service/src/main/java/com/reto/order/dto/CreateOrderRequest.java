package com.reto.order.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;

import java.util.List;

//Representa el JSON que llegará desde Postman cuando el cliente quiera crear un pedido.

//Un pedido debe contener al menos un producto.
//También validamos cada elemento interno.
public record CreateOrderRequest(@NotEmpty List<@Valid CreateOrderItemRequest> items) {

}