package com.roommatch.controller;

import com.roommatch.dto.ApiResponse;
import com.roommatch.dto.MatchResponse;
import com.roommatch.model.Usuario;
import com.roommatch.service.MatchService;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.List;

@RestController
@RequestMapping("/api/matches")
public class MatchController {

    private final MatchService matchService;

    public MatchController(MatchService matchService) {
        this.matchService = matchService;
    }

    @PostMapping("/calcular")
    public ResponseEntity<ApiResponse<List<MatchResponse>>> calcularMatches(
            Authentication authentication
    ) {

            Usuario usuario = (Usuario) authentication.getPrincipal();

            List<MatchResponse> matches = matchService.calcularMatches(
                    usuario.getIdUsuario()
            );

            return ResponseEntity.ok(
                    ApiResponse.success(matches, "Matches calculados correctamente")
            );


    }

    @GetMapping
    public ResponseEntity<ApiResponse<Page<MatchResponse>>> listarMisMatches(
            Authentication authentication,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "0") BigDecimal porcentajeMinimo
    ) {
        Usuario usuario = (Usuario) authentication.getPrincipal();

        Pageable pageable = PageRequest.of(page, size);

        Page<MatchResponse> matches = matchService.listarMisMatches(
                usuario.getIdUsuario(),
                porcentajeMinimo,
                pageable
        );

        return ResponseEntity.ok(
                ApiResponse.success(matches, "Matches obtenidos correctamente")
        );
    }
}
