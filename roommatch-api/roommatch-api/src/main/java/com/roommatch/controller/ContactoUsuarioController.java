package com.roommatch.controller;

import com.roommatch.dto.ApiResponse;
import com.roommatch.dto.ContactoUsuarioRequest;
import com.roommatch.dto.ContactoUsuarioResponse;
import com.roommatch.model.Usuario;
import com.roommatch.service.ContactoUsuarioService;

import jakarta.validation.Valid;

import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;


@RestController
@RequestMapping("/api/contactos")
public class ContactoUsuarioController {

    private final ContactoUsuarioService contactoService;


    public ContactoUsuarioController(
            ContactoUsuarioService contactoService
    ) {

        this.contactoService = contactoService;
    }


    /*
     * =========================================================
     * GUARDAR MIS DATOS DE CONTACTO
     * =========================================================
     */

    @PostMapping("/me")
    public ResponseEntity<
            ApiResponse<ContactoUsuarioResponse>
            > guardarMiContacto(

            Authentication authentication,

            @Valid
            @RequestBody
            ContactoUsuarioRequest request

    ) {



            Usuario usuario =
                    (Usuario)
                            authentication.getPrincipal();


            ContactoUsuarioResponse response =
                    contactoService
                            .guardarOModificarMiContacto(

                                    usuario.getIdUsuario(),

                                    request

                            );


            return ResponseEntity.ok(

                    ApiResponse.success(

                            response,

                            "Datos de contacto guardados correctamente"

                    )

            );


    }


    /*
     * =========================================================
     * ACTUALIZAR MIS DATOS DE CONTACTO
     * =========================================================
     */

    @PutMapping("/me")
    public ResponseEntity<
            ApiResponse<ContactoUsuarioResponse>
            > actualizarMiContacto(

            Authentication authentication,

            @Valid
            @RequestBody
            ContactoUsuarioRequest request

    ) {



            Usuario usuario =
                    (Usuario)
                            authentication.getPrincipal();


            ContactoUsuarioResponse response =
                    contactoService
                            .guardarOModificarMiContacto(

                                    usuario.getIdUsuario(),

                                    request

                            );


            return ResponseEntity.ok(

                    ApiResponse.success(

                            response,

                            "Datos de contacto actualizados correctamente"

                    )

            );


    }


    /*
     * =========================================================
     * OBTENER MIS DATOS DE CONTACTO
     * =========================================================
     */

    @GetMapping("/me")
    public ResponseEntity<
            ApiResponse<ContactoUsuarioResponse>
            > obtenerMiContacto(

            Authentication authentication

    ) {

        Usuario usuario =
                (Usuario)
                        authentication.getPrincipal();


        ContactoUsuarioResponse response =
                contactoService
                        .obtenerMiContactoOpcional(

                                usuario.getIdUsuario()

                        );


        /*
         * El usuario puede todavía
         * no tener datos de contacto.
         *
         * Esto no es un error.
         */

        if (response == null) {

            return ResponseEntity.ok(

                    ApiResponse.success(

                            null,

                            "El usuario aún no registró datos de contacto"

                    )

            );
        }


        return ResponseEntity.ok(

                ApiResponse.success(

                        response,

                        "Datos de contacto obtenidos correctamente"

                )

        );
    }


    /*
     * =========================================================
     * VER CONTACTO DESBLOQUEADO
     * =========================================================
     */

    @GetMapping("/desbloqueado/{idUsuario}")
    public ResponseEntity<
            ApiResponse<ContactoUsuarioResponse>
            > verContactoDesbloqueado(

            Authentication authentication,

            @PathVariable
            Integer idUsuario

    ) {



            Usuario usuario =
                    (Usuario)
                            authentication.getPrincipal();


            ContactoUsuarioResponse response =
                    contactoService
                            .verContactoDesbloqueado(

                                    usuario.getIdUsuario(),

                                    idUsuario

                            );


            return ResponseEntity.ok(

                    ApiResponse.success(

                            response,

                            "Contacto desbloqueado obtenido correctamente"

                    )

            );


    }
}
