package com.roommatch.repository;

import com.roommatch.model.ImagenHabitacion;
import org.springframework.data.jpa.repository.*;
import org.springframework.data.repository.query.Param;
import java.util.List;
import java.util.Optional;

public interface ImagenHabitacionRepository extends JpaRepository<ImagenHabitacion, Integer> {
    List<ImagenHabitacion> findByHabitacionIdHabitacionOrderByOrdenAsc(Integer idHabitacion);
    @Query("select i.habitacion.idHabitacion from ImagenHabitacion i where i.idImagen=:image and i.habitacion.propietario.usuario.idUsuario=:user")
    Optional<Integer> findOwnedParentId(@Param("image") Integer image, @Param("user") Integer user);
}
