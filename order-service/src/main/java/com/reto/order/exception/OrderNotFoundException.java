package com.reto.order.exception;

//Indica que el pedido solicitado no existe.
public class OrderNotFoundException
        extends RuntimeException {

    public OrderNotFoundException(Long orderId) {

        super("No se encontró el pedido con el ID: " + orderId);
    }
}