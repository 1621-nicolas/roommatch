package com.roommatch.repository;

import com.roommatch.model.ImagenPublicacion;
import org.springframework.data.jpa.repository.*;
import org.springframework.data.repository.query.Param;
import java.util.List;
import java.util.Optional;

public interface ImagenPublicacionRepository extends JpaRepository<ImagenPublicacion, Integer> {
    List<ImagenPublicacion> findByPublicacionIdPublicacionOrderByOrdenAsc(Integer idPublicacion);
    @Query("select i.publicacion.idPublicacion from ImagenPublicacion i where i.idImagen=:image and i.publicacion.usuario.idUsuario=:user")
    Optional<Integer> findOwnedParentId(@Param("image") Integer image, @Param("user") Integer user);
}
