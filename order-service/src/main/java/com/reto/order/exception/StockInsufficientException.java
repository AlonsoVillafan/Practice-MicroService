package com.reto.order.exception;

public class StockInsufficientException
        extends RuntimeException {

    public StockInsufficientException() {

        super("Stock insuficiente para los productos solicitados");
    }
}