package com.reto.inventory.filter;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import org.springframework.web.server.WebFilter;
import org.springframework.web.server.WebFilterChain;
import reactor.core.publisher.Mono;

//Este filtro registra las peticiones que llegan
//a Inventory Service junto con su X-Trace-Id.
@Component
public class TraceLoggingFilter implements WebFilter {


    //Logger utilizado para escribir información
    //en la consola de Inventory Service.
    private static final Logger log =
            LoggerFactory.getLogger(TraceLoggingFilter.class);


    //Nombre del header utilizado para trazabilidad.
    private static final String TRACE_ID_HEADER =
            "X-Trace-Id";

    @Override
    public Mono<Void> filter(
            ServerWebExchange exchange,
            WebFilterChain chain
    ) {
        //Leemos el TraceId que recibió Inventory.
        //Normalmente debe venir propagado desde Order o directamente desde Gateway.
        String traceId =
                exchange.getRequest()
                        .getHeaders()
                        .getFirst(TRACE_ID_HEADER);
        /*
         Registramos:
         -TraceId
         -método HTTP
         -ruta solicitada
         */
        log.info(
                "[traceId={}] {} {}",
                traceId,
                exchange.getRequest().getMethod(),
                exchange.getRequest().getURI().getPath()
        );
        //Continuamos normalmente con la petición.
        return chain.filter(exchange);
    }
}