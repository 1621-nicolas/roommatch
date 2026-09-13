package com.roommatch.controller;

import com.roommatch.dto.ApiResponse;
import com.roommatch.dto.ImagenHabitacionRequest;
import com.roommatch.dto.ImagenHabitacionResponse;

import com.roommatch.model.Usuario;

import com.roommatch.service.ImagenHabitacionService;

import jakarta.validation.Valid;

import org.springframework.http.ResponseEntity;

import org.springframework.security.core.Authentication;

import org.springframework.web.bind.annotation.*;

import java.util.List;


@RestController
@RequestMapping("/api/imagenes-habitacion")

public class ImagenHabitacionController {

    private final ImagenHabitacionService imagenService;


    public ImagenHabitacionController(
            ImagenHabitacionService imagenService
    ) {

        this.imagenService =
                imagenService;
    }


    /*
     * =========================================================
     * AGREGAR IMAGEN
     * =========================================================
     */

    @PostMapping(
            "/habitacion/{idHabitacion}"
    )
    public ResponseEntity<
            ApiResponse<ImagenHabitacionResponse>
            > agregarImagen(

            Authentication authentication,

            @PathVariable
            Integer idHabitacion,

            @Valid
            @RequestBody
            ImagenHabitacionRequest request

    ) {



            Usuario usuario =
                    (Usuario)
                            authentication.getPrincipal();


            ImagenHabitacionResponse response =

                    imagenService.agregarImagen(

                            usuario.getIdUsuario(),

                            idHabitacion,

                            request

                    );


            return ResponseEntity.ok(

                    ApiResponse.success(

                            response,

                            "Imagen agregada correctamente"

                    )

            );


    }


    /*
     * =========================================================
     * LISTAR IMÁGENES
     * =========================================================
     */

    @GetMapping(
            "/habitacion/{idHabitacion}"
    )
    public ResponseEntity<
            ApiResponse<List<ImagenHabitacionResponse>>
            > listarImagenes(

            @PathVariable
            Integer idHabitacion

    ) {



            List<ImagenHabitacionResponse> response =

                    imagenService
                            .listarImagenesPorHabitacion(
                                    idHabitacion
                            );


            return ResponseEntity.ok(

                    ApiResponse.success(

                            response,

                            "Imágenes obtenidas correctamente"

                    )

            );


    }


    /*
     * =========================================================
     * MARCAR PRINCIPAL
     * =========================================================
     */

    @PutMapping(
            "/{idImagen}/principal"
    )
    public ResponseEntity<
            ApiResponse<ImagenHabitacionResponse>
            > marcarPrincipal(

            Authentication authentication,

            @PathVariable
            Integer idImagen

    ) {



            Usuario usuario =
                    (Usuario)
                            authentication.getPrincipal();


            ImagenHabitacionResponse response =

                    imagenService
                            .marcarComoPrincipal(

                                    usuario.getIdUsuario(),

                                    idImagen

                            );


            return ResponseEntity.ok(

                    ApiResponse.success(

                            response,

                            "Imagen principal actualizada correctamente"

                    )

            );


    }


    /*
     * =========================================================
     * ELIMINAR IMAGEN
     * =========================================================
     */

    @DeleteMapping(
            "/{idImagen}"
    )
    public ResponseEntity<
            ApiResponse<Void>
            > eliminarImagen(

            Authentication authentication,

            @PathVariable
            Integer idImagen

    ) {



            Usuario usuario =
                    (Usuario)
                            authentication.getPrincipal();


            imagenService.eliminarImagen(

                    usuario.getIdUsuario(),

                    idImagen

            );


            return ResponseEntity.ok(

                    ApiResponse.success(

                            null,

                            "Imagen eliminada correctamente"

                    )

            );


    }
}
