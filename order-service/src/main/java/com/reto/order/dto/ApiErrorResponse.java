package com.reto.order.dto;

import java.time.LocalDateTime;

/*
 Contrato estándar para todos los errores
 que devuelve Order.

 De esta manera no dependemos del formato
 de error que Spring devuelve por defecto.
 */
public record ApiErrorResponse(

        //Momento en que ocurrió el error.
        LocalDateTime timestamp,

        //Código HTTP: 400, 404, 409, 500...
        int status,

        //Código interno que identifica el tipo de error.
        String code,

        //Explicación entendible del problema.
        String message,

        //Identificador para rastrear la petición.
        String traceId

) {
}