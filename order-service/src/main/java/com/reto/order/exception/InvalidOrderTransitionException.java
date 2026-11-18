package com.reto.order.exception;

/*
Cuando alguien intenta realizar un cambio de estado que nuestra lógica no permite.
Ejemplo: CANCELLED -> CANCELLED
 */
public class InvalidOrderTransitionException
        extends RuntimeException {

    public InvalidOrderTransitionException(String message) {

        super(message);
    }
}