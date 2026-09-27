package com.roommatch.repository;

import com.roommatch.model.PerfilConvivencia;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

import com.roommatch.matching.MatchCandidate;
import java.util.Collection;
import java.util.List;
import java.util.stream.Stream;
import jakarta.persistence.QueryHint;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.jpa.repository.QueryHints;
import org.springframework.data.repository.query.Param;

public interface PerfilConvivenciaRepository extends JpaRepository<PerfilConvivencia, Integer> {

    Optional<PerfilConvivencia> findByUsuarioIdUsuario(Integer idUsuario);

    boolean existsByUsuarioIdUsuario(Integer idUsuario);
    String CANDIDATE = """
        select new com.roommatch.matching.MatchCandidate(u.idUsuario,u.nombres,u.apellidos,u.edad,u.ocupacion,u.universidad,u.foto,
            p.distritoPreferido,p.presupuestoMin,p.presupuestoMax,p.fechaMudanza,p.limpieza,p.ruido,p.sociabilidad,
            p.horario,p.visitas,p.mascotas,p.fumar,p.alcohol,p.gastos,p.convivencia)
        from PerfilConvivencia p join p.usuario u
        """;

    @Query(CANDIDATE + "where u.idUsuario <> :origen and u.estado = 'activo' and p.perfilCompleto = true order by u.idUsuario")
    @QueryHints({@QueryHint(name = "org.hibernate.fetchSize", value = "500"), @QueryHint(name = "org.hibernate.readOnly", value = "true")})
    Stream<MatchCandidate> streamCandidates(@Param("origen") Integer origen);

    @Query(CANDIDATE + "where u.idUsuario in :ids and u.estado = 'activo' and p.perfilCompleto = true")
    List<MatchCandidate> findCandidates(@Param("ids") Collection<Integer> ids);
}