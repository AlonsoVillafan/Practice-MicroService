package com.reto.order.entity;

import com.reto.order.enums.OrderStatus;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.data.annotation.Id;
import org.springframework.data.relational.core.mapping.Column;
import org.springframework.data.relational.core.mapping.Table;

import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Table("order_status_history") //Tabla donde almacenamos cada cambio de estado
public class OrderStatusHistoryEntity {

    @Id
    @Column("id")
    private Long id;

    //Pedido al que pertenece este evento histórico.
    @Column("order_id")
    private Long orderId;

    //Será null para el primer estado,
    //porque antes de PENDING no existía otro estado.
    @Column("previous_status")
    private OrderStatus previousStatus;

    //Estado nuevo después del cambio.
    @Column("new_status")
    private OrderStatus newStatus;

    //Momento en el que ocurrió el cambio.
    @Column("created_at")
    private LocalDateTime createdAt;
}