package com.reto.inventory.entity;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.data.annotation.Id;
import org.springframework.data.relational.core.mapping.Column;
import org.springframework.data.relational.core.mapping.Table;

@Getter
@Setter
@NoArgsConstructor //constructor vacio
@AllArgsConstructor //constructor con todos los atributos

@Table("products")
public class Product {
    @Id //indico que es la llave primaria
    @Column("product_id")
    private String productId;

    @Column("name")
    private String name;

    @Column("stock")
    private Integer stock;
}
