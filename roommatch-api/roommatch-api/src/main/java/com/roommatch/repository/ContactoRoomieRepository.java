package com.roommatch.repository;

import com.roommatch.model.ContactoRoomie;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface ContactoRoomieRepository extends JpaRepository<ContactoRoomie, Integer> {

    @Query(value = """
            SELECT CASE WHEN a.idUsuario = :usuario THEN b.idUsuario ELSE a.idUsuario END AS idUsuario,
                   CASE WHEN a.idUsuario = :usuario THEN b.nombres ELSE a.nombres END AS nombres,
                   CASE WHEN a.idUsuario = :usuario THEN b.apellidos ELSE a.apellidos END AS apellidos,
                   c.fechaDesbloqueo AS fechaConexion
            FROM ContactoRoomie c JOIN c.usuarioA a JOIN c.usuarioB b
            WHERE a.idUsuario = :usuario OR b.idUsuario = :usuario
            ORDER BY c.fechaDesbloqueo DESC, c.idContactoRoomie DESC
            """, countQuery = """
            SELECT COUNT(c) FROM ContactoRoomie c
            WHERE c.usuarioA.idUsuario = :usuario OR c.usuarioB.idUsuario = :usuario
            """)
    org.springframework.data.domain.Page<ContactoConexionProjection> paginaConexiones(
            @Param("usuario") Integer usuario, org.springframework.data.domain.Pageable pageable);

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
