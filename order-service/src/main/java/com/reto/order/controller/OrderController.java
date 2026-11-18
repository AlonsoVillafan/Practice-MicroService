package com.reto.order.controller;

import com.reto.order.dto.CreateOrderRequest;
import com.reto.order.dto.OrderResponse;
import com.reto.order.dto.ReorderRequest;
import com.reto.order.service.OrderService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Mono;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import com.reto.order.dto.OrderHistoryResponse;
import java.util.List;

@RestController
@RequestMapping("/api/orders")
@RequiredArgsConstructor
public class OrderController {

    private final OrderService orderService;

    //POST /api/orders
    //Crea un nuevo pedido.
    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)


    //CREAR PEDIDO
    // @Valid ejecuta las validaciones definidas en nuestros DTOs.
    public Mono<OrderResponse> createOrder(@Valid @RequestBody CreateOrderRequest request) {
        //El Controller no contiene lógica.
        //Simplemente delega al Service.
        return orderService.createOrder(request);
    }

    //REHACER PEDIDO
    @PostMapping("/{id}/reorder")
    @ResponseStatus(HttpStatus.CREATED)
    public Mono<OrderResponse> reorder(
            @PathVariable Long id,
            @RequestBody(required = false) ReorderRequest request
    ) {
        return orderService.reorder(id, request);
    }

    //----------------------------------------------------------------------------------------
    //CONSULTA PEDIDO
    @GetMapping("/{id}")
    public Mono<OrderResponse> findOrderById(@PathVariable Long id) {
        //recibo el ID y delega la lógica al Service.
        return orderService.findOrderById(id);
    }

    //CONSULTA HISTORIAL
    @GetMapping("/{id}/history")
    public Mono<List<OrderHistoryResponse>> findOrderHistory(@PathVariable Long id
    ) {
        //Recibo el ID delega al Service
        return orderService.findOrderHistory(id);
    }

    //CANCELAR PEDIDO
    @PostMapping("/{id}/cancel")
    public Mono<OrderResponse> cancelOrder(
            @PathVariable Long id
    ) {
        return orderService.cancelOrder(id);
    }

}