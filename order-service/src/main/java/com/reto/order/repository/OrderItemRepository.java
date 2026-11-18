package com.reto.order.repository;

import com.reto.order.entity.OrderItemEntity;
import org.springframework.data.repository.reactive.ReactiveCrudRepository;
import reactor.core.publisher.Flux;

public interface OrderItemRepository
        extends ReactiveCrudRepository<OrderItemEntity, Long> {


    //Spring interpreta el nombre del método: findAllByOrderId

    //y entiende:

    //SELECT * FROM order_items
    //WHERE order_id = ?

    //Devuelve Flux porque un pedido puede tener muchos items.
    Flux<OrderItemEntity> findAllByOrderId(Long orderId);
}