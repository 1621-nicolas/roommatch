package com.roommatch.exception;

import com.roommatch.dto.ApiResponse;
import jakarta.validation.ConstraintViolationException;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.AuthenticationException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;

import java.util.LinkedHashMap;
import java.util.Map;

@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<ApiResponse<Void>> manejarIllegalArgument(
            IllegalArgumentException ex
    ) {
        return ResponseEntity
                .badRequest()
                .body(ApiResponse.fail(ex.getMessage()));
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ApiResponse<Map<String, String>>> manejarValidaciones(
            MethodArgumentNotValidException ex
    ) {
        Map<String, String> errores = new LinkedHashMap<>();

        ex.getBindingResult().getFieldErrors().forEach(error ->
                errores.put(error.getField(), error.getDefaultMessage())
        );

        return ResponseEntity
                .badRequest()
                .body(ApiResponse.fail("Error de validación", errores));
    }

    @ExceptionHandler(ConstraintViolationException.class)
    public ResponseEntity<ApiResponse<Map<String, String>>> manejarConstraintViolation(
            ConstraintViolationException ex
    ) {
        Map<String, String> errores = new LinkedHashMap<>();

        ex.getConstraintViolations().forEach(error ->
                errores.put(
                        error.getPropertyPath().toString(),
                        error.getMessage()
                )
        );

        return ResponseEntity
                .badRequest()
                .body(ApiResponse.fail("Error de validación", errores));
    }

    @ExceptionHandler(MethodArgumentTypeMismatchException.class)
    public ResponseEntity<ApiResponse<Void>> manejarTipoParametroIncorrecto(
            MethodArgumentTypeMismatchException ex
    ) {
        String mensaje = "El parámetro '" + ex.getName() + "' tiene un formato inválido";

        return ResponseEntity
                .badRequest()
                .body(ApiResponse.fail(mensaje));
    }

    @ExceptionHandler(DataIntegrityViolationException.class)
    public ResponseEntity<ApiResponse<Void>> manejarErrorBaseDatos(
            DataIntegrityViolationException ex
    ) {
        return ResponseEntity
                .status(HttpStatus.CONFLICT)
                .body(ApiResponse.fail("No se pudo completar la operación por una restricción de la base de datos"));
    }

    @ExceptionHandler(AuthenticationException.class)
    public ResponseEntity<ApiResponse<Void>> manejarNoAutenticado(
            AuthenticationException ex
    ) {
        return ResponseEntity
                .status(HttpStatus.UNAUTHORIZED)
                .body(ApiResponse.error("No estás autenticado o el token no es válido"));
    }

    @ExceptionHandler(AccessDeniedException.class)
    public ResponseEntity<ApiResponse<Void>> manejarAccesoDenegado(
            AccessDeniedException ex
    ) {
        return ResponseEntity
                .status(HttpStatus.FORBIDDEN)
                .body(ApiResponse.error("No tienes permisos para realizar esta acción"));
    }

    @ExceptionHandler(Exception.class)
public ResponseEntity<ApiResponse<Object>> manejarExcepcionGeneral(
        Exception exception
) {

    System.err.println();
    System.err.println("================================================");
    System.err.println("ERROR INTERNO ROOMMATCH");
    System.err.println("================================================");

    System.err.println(
            "TIPO DE ERROR: "
                    + exception
                            .getClass()
                            .getName()
    );

    System.err.println(
            "MENSAJE: "
                    + exception.getMessage()
    );

    System.err.println("================================================");

    exception.printStackTrace();

    System.err.println("================================================");
    System.err.println();

    return ResponseEntity
            .status(
                    HttpStatus.INTERNAL_SERVER_ERROR
            )
            .body(
                    ApiResponse.error(
                            exception
                                    .getClass()
                                    .getSimpleName()
                                    + ": "
                                    + exception.getMessage()
                    )
            );
}
}