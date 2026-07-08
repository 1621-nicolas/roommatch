package com.roommatch.controller;

import com.roommatch.dto.ApiResponse;
import com.roommatch.dto.PropietarioRequest;
import com.roommatch.dto.PropietarioResponse;

import com.roommatch.model.Usuario;

import com.roommatch.service.PropietarioService;

import jakarta.validation.Valid;

import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;


@RestController
@RequestMapping("/api/propietarios")
public class PropietarioController {

    private final PropietarioService propietarioService;


    public PropietarioController(

            PropietarioService propietarioService

    ) {

        this.propietarioService =
                propietarioService;
    }


    /*
     * =========================================================
     * CONVERTIRME EN PROPIETARIO
     * =========================================================
     */

    @PostMapping("/me")
    public ResponseEntity<
            ApiResponse<PropietarioResponse>
            > convertirmeEnPropietario(

            Authentication authentication,

            @Valid
            @RequestBody
            PropietarioRequest request

    ) {

        try {

            Usuario usuario =
                    (Usuario)
                            authentication.getPrincipal();


            PropietarioResponse response =
                    propietarioService
                            .convertirmeEnPropietario(

                                    usuario.getIdUsuario(),

                                    request

                            );


            return ResponseEntity.ok(

                    ApiResponse.success(

                            response,

                            "Perfil de propietario creado correctamente"

                    )

            );

        } catch (
                IllegalArgumentException e
        ) {

            return ResponseEntity
                    .badRequest()
                    .body(

                            ApiResponse.fail(
                                    e.getMessage()
                            )

                    );
        }
    }


    /*
     * =========================================================
     * OBTENER MI PERFIL DE PROPIETARIO
     * =========================================================
     */

    @GetMapping("/me")
    public ResponseEntity<
            ApiResponse<PropietarioResponse>
            > obtenerMiPerfilPropietario(

            Authentication authentication

    ) {

        Usuario usuario =
                (Usuario)
                        authentication.getPrincipal();


        PropietarioResponse response =
                propietarioService
                        .obtenerMiPerfilPropietarioOpcional(

                                usuario.getIdUsuario()

                        );


        /*
         * No ser propietario
         * es una situación válida.
         */

        if (response == null) {

            return ResponseEntity.ok(

                    ApiResponse.success(

                            null,

                            "El usuario no tiene perfil de propietario"

                    )

            );
        }


        return ResponseEntity.ok(

                ApiResponse.success(

                        response,

                        "Perfil de propietario obtenido correctamente"

                )

        );
    }
}