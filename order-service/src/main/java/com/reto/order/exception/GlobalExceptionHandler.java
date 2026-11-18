package com.reto.order.exception;

import com.reto.order.dto.ApiErrorResponse;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.support.WebExchangeBindException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.server.ServerWebExchange;

import java.time.LocalDateTime;
import java.util.UUID;

//Captura las excepciones lanzadas por los Controllers/Services
//y transforma todas en un mismo formato JSON.
@RestControllerAdvice //anotacion para centralizar el manejo de excepciones en APIs REST

public class GlobalExceptionHandler {

    //Pedido inexistente.
    //Ejemplo: GET /api/orders/999
    @ExceptionHandler(OrderNotFoundException.class)
    public ResponseEntity<ApiErrorResponse> handleOrderNotFound(
            OrderNotFoundException exception,
            ServerWebExchange exchange
    ) {
        return buildError(
                HttpStatus.NOT_FOUND,
                "ORDER_NOT_FOUND",
                exception.getMessage(),
                exchange
        );
    }

    //Inventory indicó que no existe stock suficiente.
    @ExceptionHandler(StockInsufficientException.class)
    public ResponseEntity<ApiErrorResponse> handleStockInsufficient(
            StockInsufficientException exception,
            ServerWebExchange exchange
    ) {
        return buildError(
                HttpStatus.CONFLICT,
                "STOCK_INSUFFICIENT",
                exception.getMessage(),
                exchange
        );
    }


    //Se intentó realizar un cambio de estado
    //que nuestra lógica no permite.
    @ExceptionHandler(InvalidOrderTransitionException.class)
    public ResponseEntity<ApiErrorResponse> handleInvalidTransition(
            InvalidOrderTransitionException exception,
            ServerWebExchange exchange
    ) {
        return buildError(
                HttpStatus.CONFLICT,
                "INVALID_ORDER_TRANSITION",
                exception.getMessage(),
                exchange
        );
    }

    /*Capturo errores producidos por @Valid, por ejemplo:
     -quantity = 0
     -productId = ""
     -items = []
     */
    @ExceptionHandler(WebExchangeBindException.class)
    public ResponseEntity<ApiErrorResponse> handleValidation(
            WebExchangeBindException exception,
            ServerWebExchange exchange
    ) {
        //Por ahora usamos un mensaje general.
        //Más adelante se podría indicar exactamente qué campo produjo el error.
        return buildError(
                HttpStatus.BAD_REQUEST,
                "INVALID_REQUEST",
                "Los datos enviados no son válidos",
                exchange
        );
    }


    //Y por ultimo cualquier excepción
    // que no tenga un handler específico termina aquí.
    @ExceptionHandler(Exception.class)
    public ResponseEntity<ApiErrorResponse> handleGenericError(
            Exception exception,
            ServerWebExchange exchange
    ) {
        return buildError(
                HttpStatus.INTERNAL_SERVER_ERROR,
                "INTERNAL_ERROR",
                "Ocurrió un error interno",
                exchange
        );
    }

    //Método privado reutilizado por todos los handlers.
    private ResponseEntity<ApiErrorResponse> buildError(
            HttpStatus status,
            String code,
            String message,
            ServerWebExchange exchange
    ) {
        //Buscamos X-Trace-Id en la petición.
        //Cuando tengamos API Gateway, este header vendrá generado desde allí.
        String traceId =
                exchange.getRequest()
                        .getHeaders()
                        .getFirst("X-Trace-Id");

        /*
         Mientras todavía llamamos directamente
         a Order Service desde Postman,
         generamos uno si no vino en el request.
         */
        if (traceId == null || traceId.isBlank()) {
            traceId = UUID.randomUUID().toString();
        }
        ApiErrorResponse error =
                new ApiErrorResponse(
                        LocalDateTime.now(),
                        status.value(),
                        code,
                        message,
                        traceId
                );
        return ResponseEntity
                .status(status)
                .body(error);
    }

    //REORDEN NO VALIDA
    @ExceptionHandler(InvalidReorderRequestException.class)
    public ResponseEntity<ApiErrorResponse> handleInvalidReorderRequest(
            InvalidReorderRequestException exception,
            ServerWebExchange exchange
    ) {

        return buildError(
                HttpStatus.BAD_REQUEST,
                "INVALID_REORDER_REQUEST",
                exception.getMessage(),
                exchange
        );
    }
}