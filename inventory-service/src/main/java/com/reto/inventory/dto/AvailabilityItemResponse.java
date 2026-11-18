package com.reto.inventory.dto;

//almacena el resultado del inventario para un solo producto consultado.

/*Guarda cuatro datos informativos básicos:
    * el ID
    * cuántos pidieron
    * cuántos hay en la base de datos reales (stock)
    * y el veredicto individual (available) de si alcanza o no.
 */
public record AvailabilityItemResponse(String productId, Integer requestedQuantity,
                                       Integer stock, boolean available) {
}