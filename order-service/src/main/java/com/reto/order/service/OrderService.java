package com.reto.order.service;

import com.reto.order.client.InventoryClient;
import com.reto.order.dto.CreateOrderRequest;
import com.reto.order.dto.OrderItemResponse;
import com.reto.order.dto.OrderResponse;
import com.reto.order.entity.OrderEntity;
import com.reto.order.entity.OrderItemEntity;
import com.reto.order.entity.OrderStatusHistoryEntity;
import com.reto.order.enums.OrderStatus;
import com.reto.order.exception.InvalidOrderTransitionException;
import com.reto.order.exception.OrderNotFoundException;
import com.reto.order.exception.StockInsufficientException;
import com.reto.order.repository.OrderItemRepository;
import com.reto.order.repository.OrderRepository;
import com.reto.order.repository.OrderStatusHistoryRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import com.reto.order.dto.OrderHistoryResponse;
import java.util.List;
import com.reto.order.dto.ReorderRequest;
import com.reto.order.dto.CreateOrderItemRequest;
import com.reto.order.exception.InvalidReorderRequestException;

import java.util.Set;
import java.util.stream.Collectors;


import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
public class OrderService {

    private final OrderRepository orderRepository;

    private final OrderItemRepository orderItemRepository;

    private final OrderStatusHistoryRepository historyRepository;

    //Cliente encargado de comunicarse con Inventory Service.
    private final InventoryClient inventoryClient;

    public Mono<OrderResponse> createOrder(CreateOrderRequest request) {

        //PRIMER PASO: Antes de guardar cualquier pedido,
        //preguntamos a Inventory si existe stock.
        return inventoryClient
                .checkAvailability(request)

                //checkAvailability devuelve un Mono, por eso utilizamos flatMap.
                .flatMap(inventoryResponse -> {

                    //Si al menos un producto no tiene stock,
                    //detenemos el flujo lanzando un error.
                    //No persistimos ningún pedido.
                    if (!inventoryResponse.available()) {

                        return Mono.error(
                                new StockInsufficientException()
                        );
                    }
                    //Si sí existe stock para todos los productos,
                    //comenzamos la creación del pedido.
                    return saveOrder(request);
                });
    }

    //GUARDAR PEDIDO
    private Mono<OrderResponse> saveOrder(
            CreateOrderRequest request
    ) {
        LocalDateTime now =
                LocalDateTime.now();
        //El ID es null porque el pedido todavía no existe en mysql
        //El pedido comienza en PENDING.
        OrderEntity order =
                new OrderEntity(
                        null,
                        OrderStatus.PENDING,
                        now,
                        now
                );
        //Primero debemos guardar orders porque
        //order_items y history tienen una FK hacia orders.
        return orderRepository.save(order)

                .flatMap(savedOrder ->
                        //Guardamos los productos del pedido.
                        saveItems(
                                savedOrder.getId(),
                                request
                        )
                                //Cuando termina saveItems(),
                                //continuamos guardando el primer estado en el historial.
                                .then(
                                        saveHistory(
                                                savedOrder.getId(),
                                                null,
                                                OrderStatus.PENDING
                                        )
                                )

                                //Finalmente cambiamos el pedido de PENDING a CONFIRMED.
                                .then(
                                        confirmOrder(
                                                savedOrder,
                                                request
                                        )
                                )
                );
    }


    //METODO PARA GUARDAR LOS ITEMS
    private Mono<Void> saveItems(
            Long orderId,
            CreateOrderRequest request
    ) {
        //Convertimos cada item del request en una entidad.
        return Flux.fromIterable(request.items())

                //Convertimos cada item del request en una entidad.
                .map(item -> new OrderItemEntity(
                                null, //null porque MySQL genera el ID con AUTO_INCREMENT.
                                orderId, //ID del pedido al que pertenece el item.
                                item.productId(),
                                item.quantity()
                        )
                )

                //Guardamos cada item en MySQL.
                //save() devuelve Mono<OrderItemEntity>, por eso usamos flatMap.
                .flatMap(orderItemRepository::save)

                //No necesito devolver los items aquí.
                //Solo quiero saber cuándo terminó todo el guardado.
                .then(); //Espera a que todo lo anterior termine correctamente y después continuo
    }

    //GUARDAR HISTORIAL
    private Mono<OrderStatusHistoryEntity> saveHistory(Long orderId, OrderStatus previousStatus,
                                                       OrderStatus newStatus) {
        //Creamos un registro histórico del cambio.
        OrderStatusHistoryEntity history =
                new OrderStatusHistoryEntity(
                        null, //MySQL genera automáticamente este ID.
                        orderId, //Pedido al que pertenece el cambio.
                        previousStatus, //Estado anterior.
                        newStatus, //Nuevo estado.
                        LocalDateTime.now() //Momento del cambio.
                );
        //Persistimos el registro.
        return historyRepository.save(history);
    }

    //CONFIRMAR PEDIDO
    private Mono<OrderResponse> confirmOrder(OrderEntity order, CreateOrderRequest request) {

        //Guardamos el estado anterior para poder registrarlo en el historial.
        OrderStatus previousStatus = order.getStatus();

        //Cambiamos el estado actual.
        order.setStatus(OrderStatus.CONFIRMED);

        order.setUpdatedAt(LocalDateTime.now());

        //Actualizamos el pedido en MySQL.
        return orderRepository.save(order)

                .flatMap(confirmedOrder ->
                        //Registramos: PENDING -> CONFIRMED
                        saveHistory(
                                confirmedOrder.getId(),
                                previousStatus,
                                OrderStatus.CONFIRMED
                        )
                                //Cuando se guarde el historial, construimos la respuesta final.
                                //En otras palabras: Cuando termine correctamente lo anterior, devuelve este objeto
                                .thenReturn(
                                        buildOrderResponse(
                                                confirmedOrder,
                                                request
                                        )
                                )
                );
    }

    //CONSTRUYO LA RESPUESTA
    private OrderResponse buildOrderResponse(
            OrderEntity order,
            CreateOrderRequest request
    ) {
        //Convertimos los items recibidos al formato que queremos exponer.
        var items = request.items()
                .stream()
                .map(item ->
                        new OrderItemResponse(
                                item.productId(),
                                item.quantity()
                        )
                )
                .toList();


        return new OrderResponse(
                order.getId(),
                order.getStatus(),
                items,
                order.getCreatedAt()
        );
    }

    //CONSULTAR UN PEDIDO
    //Si el repository no encontró ningún pedido, cambia el flujo vacío por este error.”
    public Mono<OrderResponse> findOrderById(Long orderId) {

        //Buscamos primero el pedido principal.
        //findById devuelve Mono<OrderEntity>.
        return orderRepository.findById(orderId)

                //Si no existe el pedido, convertimos Mono.empty() en un error 404.
                .switchIfEmpty(
                        Mono.error(
                                new OrderNotFoundException(orderId)
                        )
                )
                //Si el pedido existe, necesitamos también recuperar sus items.
                .flatMap(order ->
                        orderItemRepository
                                .findAllByOrderId(orderId)

                                //Convertimos cada Entity en un DTO de respuesta.
                                .map(item ->
                                        new OrderItemResponse(
                                                item.getProductId(),
                                                item.getQuantity()
                                        )
                                )

                                //Tenemos Flux<OrderItemResponse>.
                                //collectList() lo convierte en: Mono<List<OrderItemResponse>>

                                .collectList()

                                 //Cuando tenemos la lista completa, construimos el OrderResponse.
                                .map(items ->
                                        new OrderResponse(
                                                order.getId(),
                                                order.getStatus(),
                                                items,
                                                order.getCreatedAt()
                                        )
                                )
                );
    }


    //CONSULTAR HISTORIAL
    public Mono<List<OrderHistoryResponse>> findOrderHistory(
            Long orderId
    ) {
        //Primero comprobamos que el pedido exista.
        //Esto evita que un pedido inexistente simplemente devuelva una lista vacía.
        return orderRepository.findById(orderId)

                //Si no existe: Mono.empty() -> OrderNotFoundException.
                .switchIfEmpty(
                        Mono.error(
                                new OrderNotFoundException(orderId)
                        )
                )

                //Si existe el pedido, buscamos todos sus cambios de estado.
                .flatMap(order -> historyRepository
                                .findAllByOrderIdOrderByCreatedAtAsc(orderId)

                                //Cada Entity del historial se transforma en un DTO.
                                .map(history ->
                                        new OrderHistoryResponse(
                                                history.getPreviousStatus(),
                                                history.getNewStatus(),
                                                history.getCreatedAt()
                                        )
                                )

                                //Juntamos todos los registros en una única lista
                                .collectList()
                );
    }


    //CANCELAR PEDIDO
    public Mono<OrderResponse> cancelOrder(Long orderId) {

        //Primero buscamos el pedido.
        return orderRepository.findById(orderId)

                //Si no existe, devolvemos 404.
                .switchIfEmpty(
                        Mono.error(
                                new OrderNotFoundException(orderId)
                        )
                )

                //Si existe, analizamos su estado actual.

                .flatMap(order -> {

                    //Para nuestra solución solamente permitimos: CONFIRMED -> CANCELLED

                    if (order.getStatus() != OrderStatus.CONFIRMED) {

                        return Mono.error(
                                new InvalidOrderTransitionException(
                                        "Order cannot be cancelled from status: "
                                                + order.getStatus()
                                )
                        );
                    }
                    /*
                     Guardamos el estado anterior porque necesitaremos registrarlo en historial.
                     */
                    OrderStatus previousStatus =
                            order.getStatus();

                    //cambiamos el estado.
                    order.setStatus(
                            OrderStatus.CANCELLED
                    );

                    order.setUpdatedAt(
                            LocalDateTime.now()
                    );

                    //Guardamos el pedido actualizado.
                    return orderRepository.save(order)

                            .flatMap(cancelledOrder ->

                                    /*
                                     Registramos el cambio:
                                     CONFIRMED -> CANCELLED
                                     */
                                    saveHistory(
                                            cancelledOrder.getId(),
                                            previousStatus,
                                            OrderStatus.CANCELLED
                                    )
                                            //Después de guardar el historial, obtenemos los items
                                            //y construimos la respuesta final.

                                            .then(
                                                    buildOrderResponseFromDatabase(
                                                            cancelledOrder
                                                    )
                                            )
                            );
                });
    }



    private Mono<OrderResponse> buildOrderResponseFromDatabase(
            OrderEntity order
    ) {

        /*
         Recuperamos de MySQL todos los items que pertenecen al pedido.
         */
        return orderItemRepository
                .findAllByOrderId(order.getId())

                /*
                 Convertimos cada Entity a DTO.
                 */
                .map(item ->
                        new OrderItemResponse(
                                item.getProductId(),
                                item.getQuantity()
                        )
                )

                /*
                 Juntamos los items.
                 */
                .collectList()

                /*
                 Construimos finalmente la respuesta.
                 */
                .map(items ->
                        new OrderResponse(
                                order.getId(),
                                order.getStatus(),
                                items,
                                order.getCreatedAt()
                        )
                );
    }

    //METODO PARA HACER UN PEDIDO NUEVAMENTE
    public Mono<OrderResponse> reorder(
            Long originalOrderId,
            ReorderRequest reorderRequest
    ) {
        //Primero buscamos el pedido original.
        return orderRepository.findById(originalOrderId)

                //Si no existe el pedido, devolvemos error 404.
                .switchIfEmpty(
                        Mono.error(
                                new OrderNotFoundException(originalOrderId)
                        )
                )
                //Si existe el pedido, buscamos sus productos.
                .flatMap(originalOrder ->

                        orderItemRepository
                                .findAllByOrderId(originalOrderId)

                                //Tenemos Flux<OrderItemEntity>.
                                //Lo convertimos en Mono<List<OrderItemEntity>>.
                                .collectList()

                                .flatMap(originalItems -> {

                                    //Construimos un nuevo CreateOrderRequest.
                                    //CON EL METODO IMPLEMENTADO SEGUN SEA EL CASO
                                    CreateOrderRequest newOrderRequest =
                                            buildReorderRequest(
                                                    originalItems,
                                                    reorderRequest
                                            );

                                    /*
                                     * Reutilizamos el flujo normal
                                     * de creación del pedido.
                                     */
                                    return createOrder(newOrderRequest);
                                })
                );
    }




    private CreateOrderRequest buildReorderRequest(
            List<OrderItemEntity> originalItems,
            ReorderRequest reorderRequest
    ) {

        /*
         * CASO 1:
         * No se envió body.
         *
         * Copiamos productos y cantidades
         * exactamente como estaban.
         */
        if (reorderRequest == null
                || reorderRequest.items() == null
                || reorderRequest.items().isEmpty()) {

            var copiedItems = originalItems.stream()

                    .map(item ->
                            new CreateOrderItemRequest(
                                    item.getProductId(),
                                    item.getQuantity()
                            )
                    )

                    .toList();

            return new CreateOrderRequest(copiedItems);
        }


        /*
         * CASO 2:
         * El cliente envió nuevas cantidades.
         *
         * Permitimos modificar cantidades,
         * pero no agregar o eliminar productos.
         */

        Set<String> originalProductIds =
                originalItems.stream()

                        .map(OrderItemEntity::getProductId)

                        .collect(Collectors.toSet());


        Set<String> requestedProductIds =
                reorderRequest.items().stream()

                        .map(CreateOrderItemRequest::productId)

                        .collect(Collectors.toSet());


        /*
         * También evitamos que se envíe
         * dos veces el mismo producto.
         */
        boolean hasDuplicateProducts =
                requestedProductIds.size()
                        != reorderRequest.items().size();


        if (hasDuplicateProducts
                || !originalProductIds.equals(requestedProductIds)) {

            throw new InvalidReorderRequestException(
                    "La recompra debe contener exactamente " +
                            "los mismos productos del pedido original; " +
                            "solo pueden cambiar las cantidades"
            );
        }

        /*
         * Si todo está correcto reutilizamos
         * los items enviados por el cliente.
         */
        return new CreateOrderRequest(
                reorderRequest.items()
        );
    }




}