package com.reto.gateway.filter;

import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import org.springframework.web.server.WebFilter;
import org.springframework.web.server.WebFilterChain;
import reactor.core.publisher.Mono;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.UUID;

//Este filtro se ejecuta para todas las peticiones
//que entran al API Gateway.
//Su responsabilidad es garantizar que toda petición tenga un X-Trace-Id.
@Component
@Order(Ordered.HIGHEST_PRECEDENCE)
public class TraceIdFilter implements WebFilter {
    //Nombre del header utilizado para trazabilidad.
    private static final String TRACE_ID_HEADER = "X-Trace-Id";
    //Logger utilizado para registrar las peticiones junto con su TraceId.
    private static final Logger log = LoggerFactory.getLogger(TraceIdFilter.class);

    @Override
    public Mono<Void> filter(
            ServerWebExchange exchange,
            WebFilterChain chain
    ) {
        //Revisamos si el cliente ya envió un X-Trace-Id.
        String traceId =
                exchange.getRequest()
                        .getHeaders()
                        .getFirst(TRACE_ID_HEADER);
        //Si no existe, el Gateway genera uno.
        if (traceId == null || traceId.isBlank()) {
            traceId = UUID.randomUUID().toString();
        }

        //Necesitamos una variable final porque
        //será utilizada dentro del flujo reactivo.
        String finalTraceId = traceId;

        /*
         * Registramos la petición que entró al Gateway.
         */
        log.info(
                "[traceId={}] {} {}",
                finalTraceId,
                exchange.getRequest().getMethod(),
                exchange.getRequest().getURI().getPath()
        );

        //Agregamos el TraceId al request que continuará hacia Order o Inventory.
        ServerHttpRequest request =
                exchange.getRequest()
                        .mutate()
                        .headers(headers ->
                                headers.set(
                                        TRACE_ID_HEADER,
                                        finalTraceId
                                )
                        )
                        .build();
        //También lo devolvemos como header en la respuesta HTTP.
        exchange.getResponse()
                .getHeaders()
                .set(
                        TRACE_ID_HEADER,
                        finalTraceId
                );
        //Continuamos el flujo usando el request modificado.
        return chain.filter(
                exchange.mutate()
                        .request(request)
                        .build()
        );
    }
}