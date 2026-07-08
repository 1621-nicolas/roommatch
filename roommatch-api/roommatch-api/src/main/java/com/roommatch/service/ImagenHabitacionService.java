package com.roommatch.service;

import com.roommatch.dto.ImagenHabitacionRequest;
import com.roommatch.dto.ImagenHabitacionResponse;

import com.roommatch.model.Habitacion;
import com.roommatch.model.ImagenHabitacion;
import com.roommatch.model.Propietario;

import com.roommatch.repository.HabitacionRepository;
import com.roommatch.repository.ImagenHabitacionRepository;
import com.roommatch.repository.PropietarioRepository;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;


@Service
public class ImagenHabitacionService {

    private final ImagenHabitacionRepository imagenRepository;

    private final HabitacionRepository habitacionRepository;

    private final PropietarioRepository propietarioRepository;


    public ImagenHabitacionService(
            ImagenHabitacionRepository imagenRepository,
            HabitacionRepository habitacionRepository,
            PropietarioRepository propietarioRepository
    ) {

        this.imagenRepository =
                imagenRepository;

        this.habitacionRepository =
                habitacionRepository;

        this.propietarioRepository =
                propietarioRepository;
    }


    /*
     * =========================================================
     * AGREGAR IMAGEN
     * =========================================================
     */

    @Transactional
    public ImagenHabitacionResponse agregarImagen(
            Integer idUsuario,
            Integer idHabitacion,
            ImagenHabitacionRequest request
    ) {

        Propietario propietario =
                obtenerPropietario(
                        idUsuario
                );


        Habitacion habitacion =
                obtenerHabitacionPropietario(
                        idHabitacion,
                        propietario.getIdPropietario()
                );


        long cantidadImagenes =
                imagenRepository
                        .countByHabitacionIdHabitacion(
                                idHabitacion
                        );


        /*
         * Máximo 5 imágenes.
         */

        if (cantidadImagenes >= 5) {

            throw new IllegalArgumentException(
                    "Solo puedes registrar hasta 5 imágenes por habitación"
            );
        }


        /*
         * Validar URL.
         */

        if (
                request.getUrlImagen() == null ||
                request.getUrlImagen()
                        .trim()
                        .isEmpty()
        ) {

            throw new IllegalArgumentException(
                    "La URL de la imagen es obligatoria"
            );
        }


        boolean primeraImagen =
                cantidadImagenes == 0;


        boolean solicitarPrincipal =
                Boolean.TRUE.equals(
                        request.getPrincipal()
                );


        /*
         * La primera imagen siempre será principal.
         */

        boolean esPrincipal =

                primeraImagen ||

                solicitarPrincipal;


        /*
         * Si será principal,
         * quitamos principal a las demás.
         */

        if (esPrincipal) {

            imagenRepository
                    .desmarcarImagenesPrincipales(
                            idHabitacion
                    );
        }


        ImagenHabitacion imagen =
                new ImagenHabitacion();


        imagen.setHabitacion(
                habitacion
        );


        imagen.setUrlImagen(

                request
                        .getUrlImagen()
                        .trim()

        );


        imagen.setOrden(

                request.getOrden() != null

                        ? request.getOrden()

                        : (int) cantidadImagenes + 1

        );


        imagen.setPrincipal(
                esPrincipal
        );


        ImagenHabitacion imagenGuardada =

                imagenRepository.save(
                        imagen
                );


        return ImagenHabitacionResponse
                .fromEntity(
                        imagenGuardada
                );
    }


    /*
     * =========================================================
     * LISTAR IMÁGENES
     * =========================================================
     */

    @Transactional(readOnly = true)
    public List<ImagenHabitacionResponse>
    listarImagenesPorHabitacion(
            Integer idHabitacion
    ) {

        /*
         * Validar que la habitación exista.
         */

        if (
                !habitacionRepository
                        .existsById(
                                idHabitacion
                        )
        ) {

            throw new IllegalArgumentException(
                    "Habitación no encontrada"
            );
        }


        return imagenRepository

                .findByHabitacionIdHabitacionOrderByOrdenAsc(
                        idHabitacion
                )

                .stream()

                .map(
                        ImagenHabitacionResponse::fromEntity
                )

                .toList();
    }


    /*
     * =========================================================
     * MARCAR COMO PRINCIPAL
     * =========================================================
     */

    @Transactional
    public ImagenHabitacionResponse marcarComoPrincipal(
            Integer idUsuario,
            Integer idImagen
    ) {

        Propietario propietario =
                obtenerPropietario(
                        idUsuario
                );


        ImagenHabitacion imagen =

                imagenRepository
                        .findByIdImagenAndHabitacionPropietarioIdPropietario(

                                idImagen,

                                propietario.getIdPropietario()

                        )
                        .orElseThrow(
                                () ->

                                        new IllegalArgumentException(
                                                "Imagen no encontrada o no te pertenece"
                                        )
                        );


        Integer idHabitacion =

                imagen
                        .getHabitacion()
                        .getIdHabitacion();


        /*
         * Quitamos principal
         * a todas las imágenes.
         */

        imagenRepository
                .desmarcarImagenesPrincipales(
                        idHabitacion
                );


        /*
         * Marcamos la seleccionada.
         */

        imagen.setPrincipal(
                true
        );


        ImagenHabitacion imagenActualizada =

                imagenRepository.save(
                        imagen
                );


        return ImagenHabitacionResponse
                .fromEntity(
                        imagenActualizada
                );
    }


    /*
     * =========================================================
     * ELIMINAR IMAGEN
     * =========================================================
     */

    @Transactional
    public void eliminarImagen(
            Integer idUsuario,
            Integer idImagen
    ) {

        Propietario propietario =
                obtenerPropietario(
                        idUsuario
                );


        ImagenHabitacion imagen =

                imagenRepository
                        .findByIdImagenAndHabitacionPropietarioIdPropietario(

                                idImagen,

                                propietario.getIdPropietario()

                        )
                        .orElseThrow(
                                () ->

                                        new IllegalArgumentException(
                                                "Imagen no encontrada o no te pertenece"
                                        )
                        );


        Integer idHabitacion =

                imagen
                        .getHabitacion()
                        .getIdHabitacion();


        boolean eraPrincipal =

                Boolean.TRUE.equals(
                        imagen.getPrincipal()
                );


        /*
         * Eliminamos la imagen.
         */

        imagenRepository.delete(
                imagen
        );


        /*
         * Forzamos el DELETE antes
         * de consultar las imágenes restantes.
         */

        imagenRepository.flush();


        /*
         * Si eliminamos la principal,
         * elegimos automáticamente otra.
         */

        if (eraPrincipal) {

            List<ImagenHabitacion> imagenesRestantes =

                    imagenRepository
                            .findByHabitacionIdHabitacionOrderByOrdenAsc(
                                    idHabitacion
                            );


            if (
                    !imagenesRestantes.isEmpty()
            ) {

                ImagenHabitacion nuevaPrincipal =

                        imagenesRestantes.get(0);


                nuevaPrincipal.setPrincipal(
                        true
                );


                imagenRepository.save(
                        nuevaPrincipal
                );
            }
        }
    }


    /*
     * =========================================================
     * OBTENER PROPIETARIO
     * =========================================================
     */

    private Propietario obtenerPropietario(
            Integer idUsuario
    ) {

        return propietarioRepository
                .findByUsuarioIdUsuario(
                        idUsuario
                )
                .orElseThrow(
                        () ->

                                new IllegalArgumentException(
                                        "No tienes perfil de propietario"
                                )
                );
    }


    /*
     * =========================================================
     * OBTENER HABITACIÓN DEL PROPIETARIO
     * =========================================================
     */

    private Habitacion obtenerHabitacionPropietario(
            Integer idHabitacion,
            Integer idPropietario
    ) {

        return habitacionRepository

                .findByIdHabitacionAndPropietarioIdPropietario(

                        idHabitacion,

                        idPropietario

                )

                .orElseThrow(
                        () ->

                                new IllegalArgumentException(
                                        "Habitación no encontrada o no te pertenece"
                                )
                );
    }
}