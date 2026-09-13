package com.roommatch.exception;

import com.roommatch.dto.ApiResponse;
import jakarta.validation.ConstraintViolationException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.dao.DataAccessException;
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

    private static final Logger log = LoggerFactory.getLogger(GlobalExceptionHandler.class);

    @ExceptionHandler(ResourceNotFoundException.class)
    public ResponseEntity<ApiResponse<Void>> notFound(ResourceNotFoundException ex) {
        return ResponseEntity.status(404).body(ApiResponse.fail(ex.getMessage()));
    }

    @ExceptionHandler(ConflictException.class)
    public ResponseEntity<ApiResponse<Void>> conflict(ConflictException ex) {
        return ResponseEntity.status(409).body(ApiResponse.fail(ex.getMessage()));
    }

    @ExceptionHandler(org.springframework.orm.ObjectOptimisticLockingFailureException.class)
    public ResponseEntity<ApiResponse<Void>> concurrentUpdate(Exception ex) {
        return ResponseEntity.status(409).body(ApiResponse.fail("Los datos cambiaron. Actualiza la página y vuelve a intentarlo"));
    }

    @ExceptionHandler({org.springframework.http.converter.HttpMessageNotReadableException.class,
            org.springframework.web.bind.MissingServletRequestParameterException.class})
    public ResponseEntity<ApiResponse<Void>> invalidPayload(Exception ex) {
        return ResponseEntity.badRequest().body(ApiResponse.fail("La solicitud contiene datos incompletos o con formato inválido"));
    }

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
    public ResponseEntity<ApiResponse<Void>> manejarErrorIntegridadBaseDatos(
            DataIntegrityViolationException ex
    ) {
        log.warn("Restricción de integridad en RoomMatch: {}", ex.getClass().getSimpleName());

        return ResponseEntity
                .status(HttpStatus.CONFLICT)
                .body(ApiResponse.fail(
                        "No se pudo completar la operación por una restricción de la base de datos"
                ));
    }

    @ExceptionHandler(DataAccessException.class)
    public ResponseEntity<ApiResponse<Void>> manejarAccesoBaseDatos(
            DataAccessException ex
    ) {
        log.error("Error de acceso a datos: {}", ex.getClass().getSimpleName());

        return ResponseEntity
                .status(HttpStatus.SERVICE_UNAVAILABLE)
                .body(ApiResponse.error(
                        "El servicio de datos no está disponible temporalmente. Inténtalo nuevamente."
                ));
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
    public ResponseEntity<ApiResponse<Void>> manejarExcepcionGeneral(
            Exception ex
    ) {
        log.error("Error interno no controlado: {}", ex.getClass().getSimpleName());

        return ResponseEntity
                .status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(ApiResponse.error(
                        "Ocurrió un error interno. Inténtalo nuevamente."
                ));
    }
}
