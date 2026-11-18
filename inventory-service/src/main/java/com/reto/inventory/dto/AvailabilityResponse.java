package com.reto.inventory.dto;

import java.util.List;


//Es la respuesta global que devuelve tu API.
//Dice: "En total, ¿se puede procesar el pedido? Sí/No, y aquí tienes el detalle de cada uno".

//Será true solo si todos los productos de la lista tuvieron stock suficiente.
//Si tan solo uno falló, se vuelve false.
public record AvailabilityResponse(boolean available, List<AvailabilityItemResponse> items){
}