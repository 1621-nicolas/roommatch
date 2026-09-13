package com.roommatch.controller;

import com.roommatch.dto.ApiResponse;
import com.roommatch.dto.NotificacionResponse;
import com.roommatch.model.Usuario;
import com.roommatch.service.NotificacionService;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/notificaciones")
public class NotificacionController {

    private final NotificacionService notificacionService;

    public NotificacionController(NotificacionService notificacionService) {
        this.notificacionService = notificacionService;
    }

    @GetMapping
    public ResponseEntity<ApiResponse<Page<NotificacionResponse>>> listarMisNotificaciones(
            Authentication authentication,
            @RequestParam(required = false) Boolean leido,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size
    ) {
        Usuario usuario = (Usuario) authentication.getPrincipal();

        Pageable pageable = PageRequest.of(page, size);

        Page<NotificacionResponse> notificaciones =
                notificacionService.listarMisNotificaciones(
                        usuario.getIdUsuario(),
                        leido,
                        pageable
                );

        return ResponseEntity.ok(
                ApiResponse.success(notificaciones, "Notificaciones obtenidas correctamente")
        );
    }

    @GetMapping("/no-leidas/count")
    public ResponseEntity<ApiResponse<Long>> contarNoLeidas(
            Authentication authentication
    ) {
        Usuario usuario = (Usuario) authentication.getPrincipal();

        long cantidad = notificacionService.contarNoLeidas(usuario.getIdUsuario());

        return ResponseEntity.ok(
                ApiResponse.success(cantidad, "Cantidad de notificaciones no leídas obtenida correctamente")
        );
    }

    @PutMapping("/{idNotificacion}/leer")
    public ResponseEntity<ApiResponse<NotificacionResponse>> marcarComoLeida(
            Authentication authentication,
            @PathVariable Integer idNotificacion
    ) {

            Usuario usuario = (Usuario) authentication.getPrincipal();

            NotificacionResponse response = notificacionService.marcarComoLeida(
                    usuario.getIdUsuario(),
                    idNotificacion
            );

            return ResponseEntity.ok(
                    ApiResponse.success(response, "Notificación marcada como leída")
            );


    }

    @PutMapping("/leer-todas")
    public ResponseEntity<ApiResponse<Integer>> marcarTodasComoLeidas(
            Authentication authentication
    ) {
        Usuario usuario = (Usuario) authentication.getPrincipal();

        int cantidadActualizada = notificacionService.marcarTodasComoLeidas(
                usuario.getIdUsuario()
        );

        return ResponseEntity.ok(
                ApiResponse.success(cantidadActualizada, "Todas las notificaciones fueron marcadas como leídas")
        );
    }
}
