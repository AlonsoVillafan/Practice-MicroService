package com.reto.order.config;

//Constantes relacionadas con trazabilidad.
public final class TraceContext {

    //Header HTTP utilizado entre servicios.
    public static final String TRACE_ID_HEADER =
            "X-Trace-Id";

    //Clave utilizada dentro del Reactor Context.
    public static final String TRACE_ID_CONTEXT_KEY =
            "traceId";

    //Creo un constructor privado
    //Para evitar crear objetos de esta clase.
    private TraceContext() {
    }
}