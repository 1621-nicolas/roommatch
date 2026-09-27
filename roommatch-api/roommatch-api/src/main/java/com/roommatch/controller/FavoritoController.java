package com.roommatch.controller;

import com.roommatch.dto.ApiResponse;
import com.roommatch.dto.FavoritoResponse;
import com.roommatch.model.Usuario;
import com.roommatch.service.FavoritoService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/favoritos")
public class FavoritoController {

    private final FavoritoService favoritoService;

    public FavoritoController(FavoritoService favoritoService) {
        this.favoritoService = favoritoService;
    }

    @PostMapping("/{idUsuarioFavorito}")
    public ResponseEntity<ApiResponse<FavoritoResponse>> agregarFavorito(
            Authentication authentication,
            @PathVariable Integer idUsuarioFavorito
    ) {

            Usuario usuario = (Usuario) authentication.getPrincipal();

            FavoritoResponse response = favoritoService.agregarFavorito(
                    usuario.getIdUsuario(),
                    idUsuarioFavorito
            );

            return ResponseEntity.ok(
                    ApiResponse.success(response, "Usuario agregado a favoritos correctamente")
            );


    }

    @GetMapping
    public ResponseEntity<ApiResponse<List<FavoritoResponse>>> listarFavoritos(
            Authentication authentication
    ) {
        Usuario usuario = (Usuario) authentication.getPrincipal();

        List<FavoritoResponse> favoritos = favoritoService.listarMisFavoritos(
                usuario.getIdUsuario()
        );

        return ResponseEntity.ok(
                ApiResponse.success(favoritos, "Favoritos obtenidos correctamente")
        );
    }

    @DeleteMapping("/{idUsuarioFavorito}")
    public ResponseEntity<ApiResponse<Void>> eliminarFavorito(
            Authentication authentication,
            @PathVariable Integer idUsuarioFavorito
    ) {

            Usuario usuario = (Usuario) authentication.getPrincipal();

            favoritoService.eliminarFavorito(
                    usuario.getIdUsuario(),
                    idUsuarioFavorito
            );

            return ResponseEntity.ok(
                    ApiResponse.success(null, "Favorito eliminado correctamente")
            );


    }
}
