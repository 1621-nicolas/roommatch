package com.roommatch.controller;

import org.springframework.security.authentication.AuthenticationCredentialsNotFoundException;

import com.roommatch.dto.ApiResponse;
import com.roommatch.dto.PublicacionRoomieRequest;
import com.roommatch.dto.PublicacionRoomieResponse;

import com.roommatch.model.Usuario;

import com.roommatch.service.PublicacionRoomieService;

import jakarta.validation.Valid;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

import org.springframework.http.ResponseEntity;

import org.springframework.security.core.Authentication;

import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;


@RestController
@RequestMapping("/api/publicaciones-roomie")
public class PublicacionRoomieController {


    private final PublicacionRoomieService publicacionService;


    public PublicacionRoomieController(

            PublicacionRoomieService publicacionService

    ) {

        this.publicacionService = publicacionService;

    }


    /*
     * =========================================================
     * CREAR PUBLICACIÓN
     * =========================================================
     */

    @PostMapping
    public ResponseEntity<
            ApiResponse<PublicacionRoomieResponse>
            >
    crearPublicacion(

            Authentication authentication,

            @Valid
            @RequestBody
            PublicacionRoomieRequest request

    ) {

        Integer idUsuario = obtenerIdUsuarioAutenticado(
                authentication
        );


        PublicacionRoomieResponse response =

                publicacionService
                        .crearPublicacion(

                                idUsuario,

                                request

                        );


        return ResponseEntity.ok(

                ApiResponse.success(

                        response,

                        "Publicación roomie creada correctamente"

                )

        );

    }


    /*
     * =========================================================
     * LISTAR PUBLICACIONES
     * =========================================================
     *
     * IMPORTANTE:
     *
     * AQUÍ NECESITAMOS EL USUARIO AUTENTICADO
     * PARA CALCULAR LA COMPATIBILIDAD DE CADA PUBLICACIÓN.
     *
     * =========================================================
     */

    @GetMapping
public ResponseEntity<
        ApiResponse<Page<PublicacionRoomieResponse>>
        > listarPublicaciones(

        Authentication authentication,

        @RequestParam(required = false)
        String tipo,

        @RequestParam(required = false)
        String distrito,

        @RequestParam(required = false)
        BigDecimal presupuestoMin,

        @RequestParam(required = false)
        BigDecimal presupuestoMax,

        @RequestParam(defaultValue = "0")
        int page,

        @RequestParam(defaultValue = "6")
        int size

) {

    /*
     * =========================================================
     * USUARIO ACTUAL OPCIONAL
     * =========================================================
     *
     * Las publicaciones Roomie pueden visualizarse
     * sin iniciar sesión.
     *
     * Si existe una sesión, obtenemos el id del usuario
     * para calcular la compatibilidad.
     */

    Integer idUsuario = null;


    if (
            authentication != null
            &&
            authentication.isAuthenticated()
            &&
            authentication.getPrincipal() instanceof Usuario
    ) {

        Usuario usuario =
                (Usuario)
                        authentication.getPrincipal();


        idUsuario =
                usuario.getIdUsuario();
    }


    /*
     * =========================================================
     * PAGINACIÓN
     * =========================================================
     */

    Pageable pageable =
            PageRequest.of(
                    page,
                    size
            );


    /*
     * =========================================================
     * LISTAR PUBLICACIONES
     * =========================================================
     */

    Page<PublicacionRoomieResponse> publicaciones =
            publicacionService
                    .listarPublicaciones(

                            idUsuario,

                            tipo,

                            distrito,

                            presupuestoMin,

                            presupuestoMax,

                            pageable

                    );


    return ResponseEntity.ok(

            ApiResponse.success(

                    publicaciones,

                    "Publicaciones obtenidas correctamente"

            )

    );
}

    /*
     * =========================================================
     * OBTENER DETALLE
     * =========================================================
     */

    @GetMapping("/{idPublicacion}")
    public ResponseEntity<
            ApiResponse<PublicacionRoomieResponse>
            >
    obtenerPublicacion(

            Authentication authentication,

            @PathVariable
            Integer idPublicacion

    ) {

        Integer idUsuario = obtenerIdUsuarioAutenticado(

                authentication

        );


        PublicacionRoomieResponse response =

                publicacionService
                        .obtenerPublicacion(

                                idUsuario,

                                idPublicacion

                        );


        return ResponseEntity.ok(

                ApiResponse.success(

                        response,

                        "Publicación roomie obtenida correctamente"

                )

        );

    }


    /*
     * =========================================================
     * MIS PUBLICACIONES
     * =========================================================
     */

    @GetMapping("/mis")
    public ResponseEntity<
            ApiResponse<Page<PublicacionRoomieResponse>>
            >
    listarMisPublicaciones(

            Authentication authentication,

            @RequestParam(defaultValue = "0")
            int page,

            @RequestParam(defaultValue = "6")
            int size

    ) {

        Integer idUsuario = obtenerIdUsuarioAutenticado(

                authentication

        );


        Pageable pageable =

                PageRequest.of(

                        page,

                        size

                );


        Page<PublicacionRoomieResponse> publicaciones =

                publicacionService
                        .listarMisPublicaciones(

                                idUsuario,

                                pageable

                        );


        return ResponseEntity.ok(

                ApiResponse.success(

                        publicaciones,

                        "Mis publicaciones obtenidas correctamente"

                )

        );

    }


    /*
     * =========================================================
     * ACTUALIZAR
     * =========================================================
     */

    @PutMapping("/{idPublicacion}")
    public ResponseEntity<
            ApiResponse<PublicacionRoomieResponse>
            >
    actualizarPublicacion(

            Authentication authentication,

            @PathVariable
            Integer idPublicacion,

            @Valid
            @RequestBody
            PublicacionRoomieRequest request

    ) {

        Integer idUsuario = obtenerIdUsuarioAutenticado(

                authentication

        );


        PublicacionRoomieResponse response =

                publicacionService
                        .actualizarPublicacion(

                                idUsuario,

                                idPublicacion,

                                request

                        );


        return ResponseEntity.ok(

                ApiResponse.success(

                        response,

                        "Publicación roomie actualizada correctamente"

                )

        );

    }


    /*
     * =========================================================
     * PAUSAR
     * =========================================================
     */

    @PutMapping("/{idPublicacion}/pausar")
    public ResponseEntity<
            ApiResponse<PublicacionRoomieResponse>
            >
    pausarPublicacion(

            Authentication authentication,

            @PathVariable
            Integer idPublicacion

    ) {

        Integer idUsuario = obtenerIdUsuarioAutenticado(

                authentication

        );


        PublicacionRoomieResponse response =

                publicacionService
                        .pausarPublicacion(

                                idUsuario,

                                idPublicacion

                        );


        return ResponseEntity.ok(

                ApiResponse.success(

                        response,

                        "Publicación pausada correctamente"

                )

        );

    }


    /*
     * =========================================================
     * ACTIVAR
     * =========================================================
     */

    @PutMapping("/{idPublicacion}/activar")
    public ResponseEntity<
            ApiResponse<PublicacionRoomieResponse>
            >
    activarPublicacion(

            Authentication authentication,

            @PathVariable
            Integer idPublicacion

    ) {

        Integer idUsuario = obtenerIdUsuarioAutenticado(

                authentication

        );


        PublicacionRoomieResponse response =

                publicacionService
                        .activarPublicacion(

                                idUsuario,

                                idPublicacion

                        );


        return ResponseEntity.ok(

                ApiResponse.success(

                        response,

                        "Publicación activada correctamente"

                )

        );

    }


    /*
     * =========================================================
     * CERRAR
     * =========================================================
     */

    @PutMapping("/{idPublicacion}/cerrar")
    public ResponseEntity<
            ApiResponse<PublicacionRoomieResponse>
            >
    cerrarPublicacion(

            Authentication authentication,

            @PathVariable
            Integer idPublicacion

    ) {

        Integer idUsuario = obtenerIdUsuarioAutenticado(

                authentication

        );


        PublicacionRoomieResponse response =

                publicacionService
                        .cerrarPublicacion(

                                idUsuario,

                                idPublicacion

                        );


        return ResponseEntity.ok(

                ApiResponse.success(

                        response,

                        "Publicación cerrada correctamente"

                )

        );

    }


    /*
     * =========================================================
     * ELIMINAR
     * =========================================================
     */

    @DeleteMapping("/{idPublicacion}")
    public ResponseEntity<
            ApiResponse<Void>
            >
    eliminarPublicacion(

            Authentication authentication,

            @PathVariable
            Integer idPublicacion

    ) {

        Integer idUsuario = obtenerIdUsuarioAutenticado(

                authentication

        );


        publicacionService
                .eliminarPublicacion(

                        idUsuario,

                        idPublicacion

                );


        return ResponseEntity.ok(

                ApiResponse.success(

                        null,

                        "Publicación eliminada correctamente"

                )

        );

    }


    /*
     * =========================================================
     * OBTENER ID DEL USUARIO AUTENTICADO
     * =========================================================
     */

    private Integer obtenerIdUsuarioAutenticado(

            Authentication authentication

    ) {

        if (

                authentication == null

                ||

                !authentication.isAuthenticated()

        ) {

            throw new AuthenticationCredentialsNotFoundException(

                    "Usuario no autenticado"

            );

        }


        Object principal =

                authentication.getPrincipal();


        /*
         * =====================================================
         * JWT FILTER GUARDA INTEGER
         * =====================================================
         */

        if (

                principal instanceof Integer idUsuario

        ) {

            return idUsuario;

        }


        /*
         * =====================================================
         * COMPATIBILIDAD CON USUARIO
         * =====================================================
         */

        if (

                principal instanceof Usuario usuario

        ) {

            return usuario.getIdUsuario();

        }


        throw new IllegalStateException(

                "Tipo de usuario autenticado no compatible: "

                +

                (

                        principal != null

                                ? principal
                                        .getClass()
                                        .getName()

                                : "null"

                )

        );

    }

}
