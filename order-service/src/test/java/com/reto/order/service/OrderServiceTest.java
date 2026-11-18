package com.reto.order.service;

import com.reto.order.client.InventoryClient;
import com.reto.order.clientDto.InventoryAvailabilityResponse;
import com.reto.order.dto.CreateOrderItemRequest;
import com.reto.order.dto.CreateOrderRequest;
import com.reto.order.exception.StockInsufficientException;
import com.reto.order.repository.OrderItemRepository;
import com.reto.order.repository.OrderRepository;
import com.reto.order.repository.OrderStatusHistoryRepository;
import com.reto.order.entity.OrderEntity;
import com.reto.order.entity.OrderItemEntity;
import com.reto.order.entity.OrderStatusHistoryEntity;

import reactor.core.publisher.Flux;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import java.util.List;

import static org.mockito.Mockito.when;


/*
 * Habilitamos Mockito con JUnit 5.
 */
@ExtendWith(MockitoExtension.class)
class OrderServiceTest {

    /*
     Simulamos las dependencias de OrderService para no necesitar
     bases de datos ni microservicios reales.
     */
    @Mock
    private OrderRepository orderRepository;

    @Mock
    private OrderItemRepository orderItemRepository;

    @Mock
    private OrderStatusHistoryRepository historyRepository;

    @Mock
    private InventoryClient inventoryClient;


    //Esta es la clase REAL que vamos a probar.
    @InjectMocks
    private OrderService orderService;


    /*
     TEST 3:
     Inventory responde: available = false.
     Order debe detener el flujo y lanzar StockInsufficientException.
     */
    @Test
    void shouldReturnErrorWhenStockIsInsufficient() {

        //Pedido de 999 unidades de P001.
        CreateOrderRequest request =
                new CreateOrderRequest(
                        List.of(
                                new CreateOrderItemRequest(
                                        "P001",
                                        999
                                )
                        )
                );

        /*
         Simulamos la respuesta de Inventory.
         No hay stock disponible.
         */
        InventoryAvailabilityResponse inventoryResponse =
                new InventoryAvailabilityResponse(
                        false,
                        List.of()
                );

        /*
         Cuando Order consulte Inventory,
         nuestro mock devolverá available=false.
         */
        when(
                inventoryClient.checkAvailability(request)
        ).thenReturn(
                Mono.just(inventoryResponse)
        );

        /*
         Ejecutamos createOrder().

         Esperamos que el flujo termine
         exactamente con StockInsufficientException.
         */
        StepVerifier.create(
                        orderService.createOrder(request)
                )
                .expectError(
                        StockInsufficientException.class
                )
                .verify();
    }

    //PROBAR RE PEDIDO
    @Test
    void shouldReorderExistingOrder() {

        Long orderId = 1L;

        //Pedido original.
        OrderEntity originalOrder = new OrderEntity();
        originalOrder.setId(orderId);

        //Producto que tenía el pedido original.
        OrderItemEntity originalItem =
                new OrderItemEntity(
                        1L,
                        orderId,
                        "P001",
                        2
                );

    /*
     Simulamos que el pedido original existe.
     */
        when(orderRepository.findById(orderId))
                .thenReturn(
                        Mono.just(originalOrder)
                );

    /*
     Simulamos los productos del pedido original.
     */
        when(orderItemRepository.findAllByOrderId(orderId))
                .thenReturn(
                        Flux.just(originalItem)
                );

    /*
     Inventory indica que sí existe stock.
     */
        InventoryAvailabilityResponse inventoryResponse =
                new InventoryAvailabilityResponse(
                        true,
                        List.of()
                );

        when(
                inventoryClient.checkAvailability(
                        any(CreateOrderRequest.class)
                )
        ).thenReturn(
                Mono.just(inventoryResponse)
        );

    /*
     Simulamos el guardado del NUEVO pedido.

     OrderService llama a save() dos veces:
     1. Cuando crea el pedido como PENDING.
     2. Cuando lo cambia a CONFIRMED.
     */
        when(
                orderRepository.save(
                        any(OrderEntity.class)
                )
        ).thenAnswer(invocation -> {

            OrderEntity savedOrder =
                    invocation.getArgument(0);

            //Simulamos el ID generado por MySQL.
            savedOrder.setId(10L);

            return Mono.just(savedOrder);
        });

    /*
     Simulamos el guardado de los productos.
     */
        when(
                orderItemRepository.save(
                        any(OrderItemEntity.class)
                )
        ).thenAnswer(invocation ->
                Mono.just(
                        invocation.getArgument(0)
                )
        );

    /*
     Simulamos el guardado del historial.
     */
        when(
                historyRepository.save(
                        any(OrderStatusHistoryEntity.class)
                )
        ).thenAnswer(invocation ->
                Mono.just(
                        invocation.getArgument(0)
                )
        );


    /*
     Ejecutamos la recompra sin enviar nuevas cantidades.
     Por eso enviamos null.
     */
        StepVerifier.create(
                        orderService.reorder(
                                orderId,
                                null
                        )
                )

                //Esperamos que se genere un nuevo pedido.
                .expectNextMatches(response ->
                        response.id().equals(10L)
                                &&
                                response.items().size() == 1
                                &&
                                response.items()
                                        .get(0)
                                        .productId()
                                        .equals("P001")
                                &&
                                response.items()
                                        .get(0)
                                        .quantity()
                                        .equals(2)
                )

                .verifyComplete();


    /*
     Verificamos que el flujo de recompra
     realmente ejecutó las operaciones importantes.
     */
        verify(orderRepository)
                .findById(orderId);

        verify(orderItemRepository)
                .findAllByOrderId(orderId);

        verify(inventoryClient)
                .checkAvailability(
                        any(CreateOrderRequest.class)
                );
    }
}