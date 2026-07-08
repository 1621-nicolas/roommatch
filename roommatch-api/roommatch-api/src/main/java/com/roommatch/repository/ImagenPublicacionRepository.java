package com.roommatch.repository;

import com.roommatch.model.ImagenPublicacion;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ImagenPublicacionRepository extends JpaRepository<ImagenPublicacion, Integer> {

    List<ImagenPublicacion> findByPublicacionIdPublicacionOrderByOrdenAsc(
            Integer idPublicacion
    );

    Optional<ImagenPublicacion> findByIdImagenAndPublicacionUsuarioIdUsuario(
            Integer idImagen,
            Integer idUsuario
    );

    long countByPublicacionIdPublicacion(Integer idPublicacion);

    @Modifying
    @Query("""
            UPDATE ImagenPublicacion i
            SET i.principal = false
            WHERE i.publicacion.idPublicacion = :idPublicacion
            """)
    void desmarcarImagenesPrincipales(
            @Param("idPublicacion") Integer idPublicacion
    );

    void deleteByPublicacionIdPublicacion(
            Integer idPublicacion
    );

}