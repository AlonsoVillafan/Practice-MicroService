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

@Getter //Lombok genera los métodos get...
@Setter //Lombok genera los métodos set...
@NoArgsConstructor //Constructor vacío necesario para trabajar cómodamente con la entidad
@AllArgsConstructor //Constructor con todos los atributos

@Table("orders") //Esta clase representa la tabla "orders"

public class OrderEntity {
    //el id empieza en null, Mysql genera automaticamente con AUTO_INCREMENT.
    @Id //Indico que este atributo es la llave primaria
    @Column("id")
    private Long id;
    //Usamos un enum en Java.
    //Eso evita manejar estados como Strings libres
    //por toda nuestra aplicación.

    @Column("status")
    private OrderStatus status;

    @Column("created_at")
    private LocalDateTime createdAt;

    @Column("updated_at")
    private LocalDateTime updatedAt;
}