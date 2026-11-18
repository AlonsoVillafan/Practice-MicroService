package com.reto.order.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.reactive.function.client.ClientRequest;
import org.springframework.web.reactive.function.client.ExchangeFilterFunction;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

@Configuration
public class WebClientConfig {

    @Bean
    public WebClient inventoryWebClient(
            @Value("${services.inventory.base-url}")
            String inventoryBaseUrl
    ) {

        return WebClient.builder()

                //URL de Inventory Service.
                .baseUrl(inventoryBaseUrl)

                //Este filtro se ejecuta antes de que WebClient mande la petición.
                .filter(traceIdPropagationFilter())

                .build();
    }

    private ExchangeFilterFunction traceIdPropagationFilter() {

        return (request, next) ->


                //Recuperamos información del Reactor Context actual.
                Mono.deferContextual(contextView -> {
                    String traceId =
                            contextView.getOrDefault(
                                    TraceContext.TRACE_ID_CONTEXT_KEY,
                                    null
                            );

                    //Creamos una copia del request que WebClient iba a enviar.
                    ClientRequest.Builder requestBuilder =
                            ClientRequest.from(request);

                    //Si tenemos traceId, lo propagamos a Inventory.
                    if (traceId != null) {

                        requestBuilder.header(
                                TraceContext.TRACE_ID_HEADER,
                                traceId
                        );
                    }

                    //Continuamos la llamada HTTP.
                    return next.exchange(
                            requestBuilder.build()
                    );
                });
    }
}