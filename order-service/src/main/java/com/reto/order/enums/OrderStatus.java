package com.reto.order.enums;

//Limita los estados posibles.
//En lugar de permitir cualquier String:"CONFIRMD", "CANCEL", "PEPE"
//Java solamente permitirá estos estados.
public enum OrderStatus {

    PENDING,
    CONFIRMED,
    CANCELLED
}