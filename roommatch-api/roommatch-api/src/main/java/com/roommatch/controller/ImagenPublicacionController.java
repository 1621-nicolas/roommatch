package com.roommatch.controller;

import com.roommatch.dto.ApiResponse;
import com.roommatch.dto.ImagenPublicacionRequest;
import com.roommatch.dto.ImagenPublicacionResponse;
import com.roommatch.model.Usuario;
import com.roommatch.service.ImagenPublicacionService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/publicaciones-roomie")
public class ImagenPublicacionController {

    private final ImagenPublicacionService imagenService;

    public ImagenPublicacionController(ImagenPublicacionService imagenService) {
        this.imagenService = imagenService;
    }

    @PostMapping("/{idPublicacion}/imagenes")
    public ResponseEntity<ApiResponse<ImagenPublicacionResponse>> agregarImagen(
            Authentication authentication,
            @PathVariable Integer idPublicacion,
            @Valid @RequestBody ImagenPublicacionRequest request
    ) {
        Usuario usuario = (Usuario) authentication.getPrincipal();

        ImagenPublicacionResponse response = imagenService.agregarImagen(
                usuario.getIdUsuario(),
                idPublicacion,
                request
        );

        return ResponseEntity.ok(
                ApiResponse.success(response, "Imagen agregada correctamente a la publicación")
        );
    }

    @GetMapping("/{idPublicacion}/imagenes")
    public ResponseEntity<ApiResponse<List<ImagenPublicacionResponse>>> listarImagenes(
            @PathVariable Integer idPublicacion
    ) {
        List<ImagenPublicacionResponse> imagenes =
                imagenService.listarImagenesPorPublicacion(idPublicacion);

        return ResponseEntity.ok(
                ApiResponse.success(imagenes, "Imágenes de publicación obtenidas correctamente")
        );
    }

    @PutMapping("/imagenes/{idImagen}/principal")
    public ResponseEntity<ApiResponse<ImagenPublicacionResponse>> marcarPrincipal(
            Authentication authentication,
            @PathVariable Integer idImagen
    ) {
        Usuario usuario = (Usuario) authentication.getPrincipal();

        ImagenPublicacionResponse response = imagenService.marcarComoPrincipal(
                usuario.getIdUsuario(),
                idImagen
        );

        return ResponseEntity.ok(
                ApiResponse.success(response, "Imagen marcada como principal correctamente")
        );
    }

    @DeleteMapping("/imagenes/{idImagen}")
    public ResponseEntity<ApiResponse<Void>> eliminarImagen(
            Authentication authentication,
            @PathVariable Integer idImagen
    ) {
        Usuario usuario = (Usuario) authentication.getPrincipal();

        imagenService.eliminarImagen(usuario.getIdUsuario(), idImagen);

        return ResponseEntity.ok(
                ApiResponse.success(null, "Imagen eliminada correctamente")
        );
    }
}
