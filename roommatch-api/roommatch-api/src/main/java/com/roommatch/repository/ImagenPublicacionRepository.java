package com.roommatch.repository;

import com.roommatch.model.ImagenPublicacion;
import org.springframework.data.jpa.repository.*;
import org.springframework.data.repository.query.Param;
import java.util.List;
import java.util.Optional;

public interface ImagenPublicacionRepository extends JpaRepository<ImagenPublicacion, Integer> {
    @Query("""
        select new com.roommatch.dto.ImagePreviewRow(i.publicacion.idPublicacion, i.urlImagen)
        from ImagenPublicacion i where i.publicacion.idPublicacion in :ids
        order by i.principal desc, i.orden asc, i.idImagen asc
        """)
    List<com.roommatch.dto.ImagePreviewRow> findPreviews(@Param("ids") java.util.Collection<Integer> ids);

    List<ImagenPublicacion> findByPublicacionIdPublicacionOrderByOrdenAsc(Integer idPublicacion);
    @Query("select i.publicacion.idPublicacion from ImagenPublicacion i where i.idImagen=:image and i.publicacion.usuario.idUsuario=:user")
    Optional<Integer> findOwnedParentId(@Param("image") Integer image, @Param("user") Integer user);
}
