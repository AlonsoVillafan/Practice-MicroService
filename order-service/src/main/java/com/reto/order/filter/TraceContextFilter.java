package com.reto.order.filter;

import com.reto.order.config.TraceContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import org.springframework.web.server.WebFilter;
import org.springframework.web.server.WebFilterChain;
import reactor.core.publisher.Mono;

import java.util.UUID;

/*
 Captura el X-Trace-Id recibido desde Gateway
 y lo coloca dentro del Reactor Context.
 */
@Component
@Order(Ordered.HIGHEST_PRECEDENCE)
public class TraceContextFilter implements WebFilter {

    //Logger de esta clase. Spring Boot utiliza SLF4J + Logback.
    private static final Logger log =
            LoggerFactory.getLogger(TraceContextFilter.class);

    @Override
    public Mono<Void> filter(
            ServerWebExchange exchange,
            WebFilterChain chain
    ) {

        //Intentamos obtener el X-Trace-Id
        //que normalmente viene desde Gateway.
        String traceId =
                exchange.getRequest()
                        .getHeaders()
                        .getFirst(
                                TraceContext.TRACE_ID_HEADER
                        );

        //Si llamamos directamente a Order durante
        //desarrollo y no existe un TraceId, generamos uno como respaldo.
        if (traceId == null || traceId.isBlank()) {
            traceId = UUID.randomUUID().toString();
        }


        //Variable final para utilizarla dentro de lambdas/reactor.
        String finalTraceId = traceId;

        //Mostramos en consola qué petición recibió Order.
        log.info(
                "[traceId={}] {} {}",
                finalTraceId,
                exchange.getRequest().getMethod(),
                exchange.getRequest().getURI().getPath()
        );

        //Garantizamos que el request dentro de Order
        //también tenga X-Trace-Id.
        ServerHttpRequest request =
                exchange.getRequest()
                        .mutate()
                        .headers(headers ->
                                headers.set(
                                        TraceContext.TRACE_ID_HEADER,
                                        finalTraceId
                                )
                        )
                        .build();

        //También podemos devolver el TraceId como header de respuesta.
        //Esto es especialmente útil si llamamos
        //directamente a Order durante desarrollo.
        exchange.getResponse()
                .getHeaders()
                .set(
                        TraceContext.TRACE_ID_HEADER,
                        finalTraceId
                );


        //Continuamos la petición utilizando el request modificado.
        //Además guardamos el TraceId en Reactor Context
        //para que posteriormente WebClient pueda recuperarlo.
        return chain.filter(
                        exchange.mutate()
                                .request(request)
                                .build()
                )
                .contextWrite(
                        context ->
                                context.put(
                                        TraceContext.TRACE_ID_CONTEXT_KEY,
                                        finalTraceId
                                )
                );
    }
}