package com.reto.order.exception;

/*
Cuando alguien intenta realizar un cambio de estado que nuestra lógica no permite.
Ejemplo: CANCELLED -> CANCELLED
 */
public class InvalidReorderRequestException
        extends RuntimeException {

    public InvalidReorderRequestException(String message) {

        super(message);
    }
}