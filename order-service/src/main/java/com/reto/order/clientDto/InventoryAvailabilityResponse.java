package com.reto.order.clientDto;

import java.util.List;

//Respuesta completa que recibiremos desde Inventory Service.

public record InventoryAvailabilityResponse(boolean available, List<InventoryItemResponse> items) {

}