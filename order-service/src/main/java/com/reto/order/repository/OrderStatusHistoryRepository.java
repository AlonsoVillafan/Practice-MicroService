package com.reto.order.repository;

import com.reto.order.entity.OrderStatusHistoryEntity;
import org.springframework.data.repository.reactive.ReactiveCrudRepository;
import reactor.core.publisher.Flux;

public interface OrderStatusHistoryRepository extends ReactiveCrudRepository<OrderStatusHistoryEntity, Long> {

    //Busca todo el historial del pedido
    //y lo devuelve ordenado desde el más antiguo al más reciente.
    Flux<OrderStatusHistoryEntity>
    findAllByOrderIdOrderByCreatedAtAsc(Long orderId);
}