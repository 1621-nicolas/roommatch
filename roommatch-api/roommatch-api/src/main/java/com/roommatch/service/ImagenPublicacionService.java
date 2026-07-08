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
        PublicacionRoomie publicacion = publicacionRepository
                .findByIdPublicacionAndUsuarioIdUsuario(idPublicacion, idUsuario)
                .orElseThrow(() -> new IllegalArgumentException("Publicación no encontrada o no te pertenece"));

        long cantidadImagenes = imagenRepository.countByPublicacionIdPublicacion(idPublicacion);

        if (cantidadImagenes >= 5) {
            throw new IllegalArgumentException("Solo puedes registrar hasta 5 imágenes por publicación");
        }

        boolean esPrincipal = Boolean.TRUE.equals(request.getPrincipal());

        if (esPrincipal) {
            imagenRepository.desmarcarImagenesPrincipales(idPublicacion);
        }

        ImagenPublicacion imagen = new ImagenPublicacion();
        imagen.setPublicacion(publicacion);
        imagen.setUrlImagen(request.getUrlImagen());
        imagen.setOrden(request.getOrden() != null ? request.getOrden() : (int) cantidadImagenes + 1);
        imagen.setPrincipal(esPrincipal || cantidadImagenes == 0);

        ImagenPublicacion guardada = imagenRepository.save(imagen);

        return ImagenPublicacionResponse.fromEntity(guardada);
    }

    public List<ImagenPublicacionResponse> listarImagenesPorPublicacion(Integer idPublicacion) {
        return imagenRepository
                .findByPublicacionIdPublicacionOrderByOrdenAsc(idPublicacion)
                .stream()
                .map(ImagenPublicacionResponse::fromEntity)
                .toList();
    }

    @Transactional
    public ImagenPublicacionResponse marcarComoPrincipal(
            Integer idUsuario,
            Integer idImagen
    ) {
        ImagenPublicacion imagen = imagenRepository
                .findByIdImagenAndPublicacionUsuarioIdUsuario(idImagen, idUsuario)
                .orElseThrow(() -> new IllegalArgumentException("Imagen no encontrada o no te pertenece"));

        Integer idPublicacion = imagen.getPublicacion().getIdPublicacion();

        imagenRepository.desmarcarImagenesPrincipales(idPublicacion);

        imagen.setPrincipal(true);

        ImagenPublicacion actualizada = imagenRepository.save(imagen);

        return ImagenPublicacionResponse.fromEntity(actualizada);
    }

    @Transactional
    public void eliminarImagen(
            Integer idUsuario,
            Integer idImagen
    ) {
        ImagenPublicacion imagen = imagenRepository
                .findByIdImagenAndPublicacionUsuarioIdUsuario(idImagen, idUsuario)
                .orElseThrow(() -> new IllegalArgumentException("Imagen no encontrada o no te pertenece"));

        imagenRepository.delete(imagen);
    }
}