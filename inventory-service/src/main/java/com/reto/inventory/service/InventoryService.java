package com.reto.inventory.service;
import com.reto.inventory.dto.ProductResponse;
import com.reto.inventory.repository.ProductRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

import com.reto.inventory.dto.AvailabilityItemResponse;
import com.reto.inventory.dto.AvailabilityRequest;
import com.reto.inventory.dto.AvailabilityResponse;
import reactor.core.publisher.Flux;

@Service
//aqui va la logica de negocio: validar stock, descuentos, total
//o transformar entidades a DTOs

@RequiredArgsConstructor
//Esto genera automáticamente un constructor para todos los atributos declarados como final

public class InventoryService {

    //agrego la dependencia para usar los metodos de la interface
    private final ProductRepository productRepository;

    //Metodo que usará el id del producto recibido en el controller
    public Mono<ProductResponse> findProductById(String productId){
        //gracias a la dependencia inyectada puedo usar funciones de busqueda
        //de la interface ProductRepository
        //este metodo me va a devolver un Mono<ProductResponse>
        //el cual es un flujo reactivo asíncrono que emitirá un único objeto de respuesta
        //con los datos mapeados del producto (o terminará vacío si el producto no existe)."
        return productRepository.findById(productId)
                //tomo el objeto que viaja dentro del flujo reactivo,
                //transformo en un objeto diferente y vuelvo a meterlo en el flujo sin romper la tubería.
                .map(product -> new ProductResponse(
                        product.getProductId(),
                        product.getName(),
                        product.getStock()
                ));
    }

    //Metodo que funciona como pipeline (tuberia)
    public Mono<AvailabilityResponse> checkAvailability(AvailabilityRequest request) {

        //Convertimos la lista de productos solicitados en un Flux para procesarlos de forma reactiva.
        return Flux.fromIterable(request.items())
                 //Por cada item buscamos su producto en MySQL
                 //Usamos flatMap porque findById() devuelve un Mono<Product>
                .flatMap(item ->
                        productRepository.findById(item.productId())
                                //Si encontramos el producto,
                                //abro la caja con los productos
                                //calculamos si existe stock suficiente
                                .map(product -> {
                                    boolean available =
                                            product.getStock() >= item.quantity();

                                     //Creamos la respuesta individual para este producto.
                                    return new AvailabilityItemResponse(
                                            product.getProductId(),
                                            item.quantity(),
                                            product.getStock(),
                                            available
                                    );
                                })
                                 //Si findById() no encuentra el producto, devuelve Mono.empty().
                                 //En ese caso lo consideramos como no disponible.
                                .defaultIfEmpty(
                                        new AvailabilityItemResponse(
                                                item.productId(),
                                                item.quantity(),
                                                0,
                                                false
                                        )
                                )
                )

                 //Hasta este punto tenemos: Flux<AvailabilityItemResponse>
                 //agrupo todos los elementos en una sola lista.
                .collectList()

                //Ahora tenemos una única lista con todos los resultados.
                .map(items -> {
                    //devuelve true solamente si TODOS los productos tienen : available = true.
                    boolean allAvailable =
                            items.stream() //reviso cada elemento de la lista items.
                                    //solo devuelve true si absolutamente todos cumplen la condición
                                    .allMatch(
                                            //Para cada ítem, revisa el valor de su función available()
                                            AvailabilityItemResponse::available
                                    );
                    //Construimos la respuesta final.
                    return new AvailabilityResponse(allAvailable, items);
                });
    }
}
