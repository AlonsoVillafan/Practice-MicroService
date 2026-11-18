package com.reto.order.repository;

import com.reto.order.entity.OrderEntity;
import org.springframework.data.repository.reactive.ReactiveCrudRepository;


//ProductRepository hace lo mismo en Inventory-service.

//Aquí:
//OrderEntity = entidad administrada.
//Long = tipo de la llave primaria.

public interface OrderRepository extends ReactiveCrudRepository<OrderEntity, Long> {

}