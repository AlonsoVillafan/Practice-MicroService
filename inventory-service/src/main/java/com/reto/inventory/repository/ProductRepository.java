package com.reto.inventory.repository;

import com.reto.inventory.entity.Product;
import org.springframework.data.repository.reactive.ReactiveCrudRepository;
import org.springframework.stereotype.Repository;

//@Repository

//Primer parametro indica que este repositorio administra la entidad Product
//Segundo parametro indica que la llave primaria es de tipo String
//Con ReactiveCrudRepository, utilizo operaciones CRUD
//de forma no bloqueante y reactiva sobre una BD
//se encarga exclusivamente de ir a la base de datos.
public interface ProductRepository extends ReactiveCrudRepository<Product,String> {

}