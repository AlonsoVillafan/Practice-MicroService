package com.reto.order.clientDto;

import java.util.List;

//Solicitud que Order enviará a Inventory.

public record InventoryAvailabilityRequest(List<InventoryItemRequest> items) {

}