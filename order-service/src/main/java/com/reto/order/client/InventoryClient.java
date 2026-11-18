package com.reto.order.client;

import com.reto.order.clientDto.InventoryAvailabilityRequest;
import com.reto.order.clientDto.InventoryAvailabilityResponse;
import com.reto.order.clientDto.InventoryItemRequest;
import com.reto.order.dto.CreateOrderRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

@Component
@RequiredArgsConstructor
public class InventoryClient {

    //esta clase sabe como comunicarse con Inventory Service

    //WebClient ya configurado previamente con la URL base de Inventory Service.
    private final WebClient inventoryWebClient;

    public Mono<InventoryAvailabilityResponse> checkAvailability(CreateOrderRequest orderRequest
    ) {

        //Convertimos los items del pedido al formato que espera Inventory Service.
        var inventoryItems = orderRequest.items()
                .stream()
                .map(item ->
                        new InventoryItemRequest(
                                item.productId(),
                                item.quantity())
                )
                .toList();

        //Construimos el request que enviaremos al otro microservicio.
        var inventoryRequest = new InventoryAvailabilityRequest(inventoryItems);

        //Aquí ocurre  la comunicación HTTP.
        return inventoryWebClient

                //Método HTTP utilizado por Inventory.
                .post()

                //Endpoint que ya construimos anteriormente.
                .uri("/api/inventory/availability")

                //Convierto nuestro DTO a JSON y lo enviamos.
                .bodyValue(inventoryRequest)

                //Ejecuto la petición y comienzo a procesar la respuesta.
                .retrieve()

                //Convierto el JSON recibido desde Inventory en un objeto Java
                .bodyToMono(
                        InventoryAvailabilityResponse.class
                );
    }
}