package com.reto.inventory.controller;

import com.reto.inventory.dto.ProductResponse;
import com.reto.inventory.service.InventoryService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Mono;
import com.reto.inventory.dto.AvailabilityRequest;
import com.reto.inventory.dto.AvailabilityResponse;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

/*
Su única responsabilidad es recibir el ID de la URL y decirle al servicio:
"Busca esto y entrega el flujo con el resultado al cliente cuando esté listo".
*/

@RestController //recibe peticiones http get o post y la respuesta en JSON //
@RequestMapping("/api/inventory") //ruta base para todos los endpoints
@RequiredArgsConstructor //lombok, genera constructor para mi servicio inmutable
public class InventoryController {

    //capa de logica de negocios
    //el controlador no busca directamente en la base de datos
    //Inyecto la dependencia InventoryService
    private final InventoryService inventoryService;


    //ENDPOINT DE PRUEBA
    @GetMapping("/health")
    public Mono<String> health() {
        return Mono.just("Inventory Service OK");
    }

    //ENDPOINT PARA CONSULTAR PRODUCTO

    @GetMapping("/products/{productId}")
    //creo el metodo findProduct que va recibir como parametro
    //el productId desde la url, y la guardo en una variable
    public Mono<ProductResponse> findProduct(@PathVariable String productId) {
        //envio el productId capturado al metodo de inventoryService
        //el cual me retorna un mono de ProductResponse
        return inventoryService.findProductById(productId);
    }

    //ENDPOINT PARA CONSULTAR DISPONIBILIDAD
    @PostMapping("/availability")
    public Mono<AvailabilityResponse> checkAvailability(
            @Valid @RequestBody AvailabilityRequest request
    ) {
        return inventoryService.checkAvailability(request);
    }
}