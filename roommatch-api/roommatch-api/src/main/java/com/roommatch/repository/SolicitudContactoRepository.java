package com.roommatch.repository;

import com.roommatch.model.SolicitudContacto;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface SolicitudContactoRepository extends JpaRepository<SolicitudContacto, Integer> {
    @Query("""
        select count(s) > 0 from SolicitudContacto s where s.estado = 'pendiente' and
        ((s.usuarioEmisor.idUsuario = :a and s.usuarioReceptor.idUsuario = :b) or
         (s.usuarioEmisor.idUsuario = :b and s.usuarioReceptor.idUsuario = :a))
        """)
    boolean existePendiente(@Param("a") Integer a, @Param("b") Integer b);

    Optional<SolicitudContacto> findFirstByUsuarioEmisorIdUsuarioAndUsuarioReceptorIdUsuarioOrderByFechaSolicitudDescIdSolicitudDesc(
            Integer emisor, Integer receptor);

    interface Partes {
        Integer getEmisor();
        Integer getReceptor();
    }
    @Query("select s.usuarioEmisor.idUsuario as emisor, s.usuarioReceptor.idUsuario as receptor from SolicitudContacto s where s.idSolicitud = :id")
    Optional<Partes> partes(@Param("id") Integer id);

    @EntityGraph(attributePaths = {"usuarioEmisor", "usuarioReceptor"})
    List<SolicitudContacto> findByUsuarioReceptorIdUsuarioOrderByFechaSolicitudDesc(Integer receptor);

    @EntityGraph(attributePaths = {"usuarioEmisor", "usuarioReceptor"})
    List<SolicitudContacto> findByUsuarioEmisorIdUsuarioOrderByFechaSolicitudDesc(Integer emisor);
}
