package com.roommatch.repository;

import com.roommatch.model.ImagenHabitacion;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;

import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;


public interface ImagenHabitacionRepository
        extends JpaRepository<ImagenHabitacion, Integer> {


    /*
     * =========================================================
     * LISTAR IMÁGENES
     * =========================================================
     */

    List<ImagenHabitacion>
    findByHabitacionIdHabitacionOrderByOrdenAsc(
            Integer idHabitacion
    );


    /*
     * =========================================================
     * BUSCAR IMAGEN DEL PROPIETARIO
     * =========================================================
     */

    Optional<ImagenHabitacion>
    findByIdImagenAndHabitacionPropietarioIdPropietario(
            Integer idImagen,
            Integer idPropietario
    );


    /*
     * =========================================================
     * CONTAR IMÁGENES
     * =========================================================
     */

    long countByHabitacionIdHabitacion(
            Integer idHabitacion
    );


    /*
     * =========================================================
     * OBTENER IMAGEN PRINCIPAL
     * =========================================================
     */

    Optional<ImagenHabitacion>
    findFirstByHabitacionIdHabitacionAndPrincipalTrue(
            Integer idHabitacion
    );


    /*
     * =========================================================
     * DESMARCAR IMÁGENES PRINCIPALES
     * =========================================================
     */

    @Modifying
    @Query("""
            UPDATE ImagenHabitacion i
            SET i.principal = false
            WHERE i.habitacion.idHabitacion = :idHabitacion
            """)
    void desmarcarImagenesPrincipales(
            @Param("idHabitacion")
            Integer idHabitacion
    );
}