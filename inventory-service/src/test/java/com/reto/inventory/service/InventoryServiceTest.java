package com.reto.inventory.service;

import com.reto.inventory.dto.AvailabilityItemRequest;
import com.reto.inventory.dto.AvailabilityRequest;
import com.reto.inventory.entity.Product;
import com.reto.inventory.repository.ProductRepository;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.when;


/*
 MockitoExtension permite utilizar
 @Mock e @InjectMocks con JUnit 5.
 */
@ExtendWith(MockitoExtension.class)
class InventoryServiceTest {

    //Repository simulado. No necesitamos conectarnos a MySQL.
    @Mock
    private ProductRepository productRepository;

    //Esta es la clase REAL que queremos probar.
    //Mockito le inyectará el repository simulado.
    @InjectMocks
    private InventoryService inventoryService;

    /*
     TEST 1:
     -Producto existe y tiene stock suficiente.

     P001 tiene stock 10.
     Solicitamos 2.

     Resultado esperado: available = true.
     */
    @Test
    void shouldReturnAvailableWhenStockIsEnough() {
        //Producto que simularemos como si hubiese salido de MySQL.
        Product product =
                new Product(
                        "P001",
                        "Laptop",
                        10
                );
        /*
         Simulamos el comportamiento del Repository.
         Cuando se busque P001, devolvemos nuestro producto.
         */
        when(
                productRepository.findById("P001")
        ).thenReturn(
                Mono.just(product)
        );

        //Simulamos que el cliente solicita 2 unidades de P001.
        AvailabilityRequest request =
                new AvailabilityRequest(
                        List.of(
                                new AvailabilityItemRequest(
                                        "P001",
                                        2
                                )
                        )
                );

        //Ejecutamos el método REAL de InventoryService.
        StepVerifier.create(
                        inventoryService.checkAvailability(request)
                )

                //Verificamos la respuesta obtenida.
                .assertNext(response -> {
                    //La disponibilidad general debe ser true.
                    assertTrue(
                            response.available()
                    );
                    //Debemos tener un producto en la respuesta.
                    assertEquals(
                            1,
                            response.items().size()
                    );
                    //El producto también debe estar disponible.
                    assertTrue(
                            response.items()
                                    .get(0)
                                    .available()
                    );
                })

                //El flujo debe terminar correctamente, sin errores.
                .verifyComplete();
    }



    /*
     TEST 2:
     Producto inexistente.
     Resultado esperado: available = false.
     */
    @Test
    void shouldReturnUnavailableWhenProductDoesNotExist() {

        /*
         Simulamos que P999 no existe.
         findById() devuelve Mono.empty().
         */
        when(
                productRepository.findById("P999")
        ).thenReturn(
                Mono.empty()
        );
        AvailabilityRequest request =
                new AvailabilityRequest(
                        List.of(
                                new AvailabilityItemRequest(
                                        "P999",
                                        1
                                )
                        )
                );
        //Ejecutamos el método real.
        StepVerifier.create(
                        inventoryService.checkAvailability(request)
                )

                .assertNext(response -> {

                    //La respuesta general debe ser false.
                    assertFalse(
                            response.available()
                    );

                    /*
                     Tu lógica actual considera que
                     un producto inexistente tiene stock 0.
                     */
                    assertEquals(
                            0,
                            response.items()
                                    .get(0)
                                    .stock()
                    );
                    //El producto debe aparecer como no disponible.
                    assertFalse(
                            response.items()
                                    .get(0)
                                    .available()
                    );
                })

                .verifyComplete();
    }
}