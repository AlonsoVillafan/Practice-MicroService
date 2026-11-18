package com.reto.order.entity;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.data.annotation.Id;
import org.springframework.data.relational.core.mapping.Column;
import org.springframework.data.relational.core.mapping.Table;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Table("order_items") //Mapea esta clase con la tabla order_items
public class OrderItemEntity {

    @Id
    @Column("id")
    private Long id;

    //Este campo conecta el item con su pedido.
    //Ejemplo: orderId = "ABC123"
    @Column("order_id")
    private Long orderId;

    //Inventory Service conoce productos como:
    //P001, P002, etc.
    @Column("product_id")
    private String productId;

    //Cantidad que el cliente quiere comprar.
    @Column("quantity")
    private Integer quantity;
}