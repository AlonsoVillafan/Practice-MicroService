package com.reto.inventory.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;

import java.util.List;

//con @NoEmpty valido que la lista contenga al menos un articulo
//tambien se valida que cumpla con las reglas establecidas en AvailabilityItemRequest

//Representa la petición completa. Por ejemplo: "Quiero verificar este grupo de productos".
//(por ejemplo, pedidos completos)
public record AvailabilityRequest(@NotEmpty List<@Valid AvailabilityItemRequest> items){
}