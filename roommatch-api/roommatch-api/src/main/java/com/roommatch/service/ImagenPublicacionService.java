package com.roommatch.service;

import com.roommatch.dto.ImagenPublicacionRequest;
import com.roommatch.dto.ImagenPublicacionResponse;
import com.roommatch.model.ImagenPublicacion;
import com.roommatch.model.PublicacionRoomie;
import com.roommatch.repository.ImagenPublicacionRepository;
import com.roommatch.repository.PublicacionRoomieRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Objects;

@Service
public class ImagenPublicacionService {

    private final ImagenPublicacionRepository imagenRepository;
    private final PublicacionRoomieRepository publicacionRepository;

    public ImagenPublicacionService(
            ImagenPublicacionRepository imagenRepository,
            PublicacionRoomieRepository publicacionRepository
    ) {
        this.imagenRepository = imagenRepository;
        this.publicacionRepository = publicacionRepository;
    }

    @Transactional
    public ImagenPublicacionResponse agregarImagen(
            Integer idUsuario,
            Integer idPublicacion,
            ImagenPublicacionRequest request
    ) {
        Integer usuarioId = requerirId(idUsuario, "idUsuario");
        Integer publicacionId = requerirId(idPublicacion, "idPublicacion");
        Objects.requireNonNull(request, "request");

        PublicacionRoomie publicacion = publicacionRepository
                .findByIdPublicacionAndUsuarioIdUsuario(publicacionId, usuarioId)
                .orElseThrow(() -> new IllegalArgumentException(
                        "Publicación no encontrada o no te pertenece"
                ));

        long cantidadImagenes = imagenRepository.countByPublicacionIdPublicacion(publicacionId);

        if (cantidadImagenes >= 5) {
            throw new IllegalArgumentException(
                    "Solo puedes registrar hasta 5 imágenes por publicación"
            );
        }

        boolean esPrincipal = Boolean.TRUE.equals(request.getPrincipal());

        if (esPrincipal) {
            imagenRepository.desmarcarImagenesPrincipales(publicacionId);
        }

        ImagenPublicacion imagen = new ImagenPublicacion();
        imagen.setPublicacion(publicacion);
        imagen.setUrlImagen(request.getUrlImagen().trim());
        imagen.setOrden(
                request.getOrden() != null
                        ? request.getOrden()
                        : (int) cantidadImagenes + 1
        );
        imagen.setPrincipal(esPrincipal || cantidadImagenes == 0);

        ImagenPublicacion guardada = imagenRepository.save(
                Objects.requireNonNull(imagen)
        );

        return ImagenPublicacionResponse.fromEntity(guardada);
    }

    @Transactional(readOnly = true)
    public List<ImagenPublicacionResponse> listarImagenesPorPublicacion(Integer idPublicacion) {
        Integer publicacionId = requerirId(idPublicacion, "idPublicacion");

        return imagenRepository
                .findByPublicacionIdPublicacionOrderByOrdenAsc(publicacionId)
                .stream()
                .map(ImagenPublicacionResponse::fromEntity)
                .toList();
    }

    @Transactional
    public ImagenPublicacionResponse marcarComoPrincipal(
            Integer idUsuario,
            Integer idImagen
    ) {
        Integer usuarioId = requerirId(idUsuario, "idUsuario");
        Integer imagenId = requerirId(idImagen, "idImagen");

        ImagenPublicacion imagen = imagenRepository
                .findByIdImagenAndPublicacionUsuarioIdUsuario(imagenId, usuarioId)
                .orElseThrow(() -> new IllegalArgumentException(
                        "Imagen no encontrada o no te pertenece"
                ));

        Integer publicacionId = requerirId(
                imagen.getPublicacion().getIdPublicacion(),
                "idPublicacion"
        );

        imagenRepository.desmarcarImagenesPrincipales(publicacionId);
        imagen.setPrincipal(true);

        ImagenPublicacion actualizada = imagenRepository.save(
                Objects.requireNonNull(imagen)
        );

        return ImagenPublicacionResponse.fromEntity(actualizada);
    }

    @Transactional
    public void eliminarImagen(
            Integer idUsuario,
            Integer idImagen
    ) {
        Integer usuarioId = requerirId(idUsuario, "idUsuario");
        Integer imagenId = requerirId(idImagen, "idImagen");

        ImagenPublicacion imagen = imagenRepository
                .findByIdImagenAndPublicacionUsuarioIdUsuario(imagenId, usuarioId)
                .orElseThrow(() -> new IllegalArgumentException(
                        "Imagen no encontrada o no te pertenece"
                ));

        imagenRepository.delete(Objects.requireNonNull(imagen));
    }

    private Integer requerirId(Integer id, String nombre) {
        if (id == null || id <= 0) {
            throw new IllegalArgumentException(nombre + " debe ser un identificador válido");
        }
        return id;
    }
}
