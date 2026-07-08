package com.roommatch.repository;

import com.roommatch.model.ContactoRoomie;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface ContactoRoomieRepository extends JpaRepository<ContactoRoomie, Integer> {

    boolean existsBySolicitudIdSolicitud(Integer idSolicitud);

    @Query("""
            SELECT COUNT(c) > 0
            FROM ContactoRoomie c
            WHERE 
            (c.usuarioA.idUsuario = :idUsuarioActual AND c.usuarioB.idUsuario = :idUsuarioObjetivo)
            OR
            (c.usuarioA.idUsuario = :idUsuarioObjetivo AND c.usuarioB.idUsuario = :idUsuarioActual)
            """)
    boolean existeContactoDesbloqueado(
            @Param("idUsuarioActual") Integer idUsuarioActual,
            @Param("idUsuarioObjetivo") Integer idUsuarioObjetivo
    );

    @Query("""
            SELECT c
            FROM ContactoRoomie c
            WHERE c.usuarioA.idUsuario = :idUsuario
            OR c.usuarioB.idUsuario = :idUsuario
            ORDER BY c.fechaDesbloqueo DESC
            """)
    List<ContactoRoomie> listarContactosDesbloqueados(
            @Param("idUsuario") Integer idUsuario
    );
}