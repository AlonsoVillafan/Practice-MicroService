package com.reto.inventory.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Positive;

//evito que el id este vacio
//Indico que la cantidad solicitada sea estrictamente mayor a cero

//Representa cada producto individual dentro de la petición.
//Por ejemplo: "Quiero saber si hay X cantidad del producto Y".
public record AvailabilityItemRequest(@NotBlank String productId, @Positive Integer quantity){
}