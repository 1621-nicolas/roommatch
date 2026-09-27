package com.roommatch.repository;

import com.roommatch.model.ImagenHabitacion;
import org.springframework.data.jpa.repository.*;
import org.springframework.data.repository.query.Param;
import java.util.List;
import java.util.Optional;

public interface ImagenHabitacionRepository extends JpaRepository<ImagenHabitacion, Integer> {
    @Query("""
        select new com.roommatch.dto.ImagePreviewRow(i.habitacion.idHabitacion, i.urlImagen)
        from ImagenHabitacion i where i.habitacion.idHabitacion in :ids
        order by i.principal desc, i.orden asc, i.idImagen asc
        """)
    List<com.roommatch.dto.ImagePreviewRow> findPreviews(@Param("ids") java.util.Collection<Integer> ids);

    List<ImagenHabitacion> findByHabitacionIdHabitacionOrderByOrdenAsc(Integer idHabitacion);
    @Query("select i.habitacion.idHabitacion from ImagenHabitacion i where i.idImagen=:image and i.habitacion.propietario.usuario.idUsuario=:user")
    Optional<Integer> findOwnedParentId(@Param("image") Integer image, @Param("user") Integer user);
}
