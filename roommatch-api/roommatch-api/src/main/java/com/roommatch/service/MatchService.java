package com.roommatch.service;

import com.roommatch.dto.CompatibilidadCalculada;
import com.roommatch.dto.MatchResponse;
import com.roommatch.exception.ConflictException;
import com.roommatch.matching.*;
import com.roommatch.repository.PerfilConvivenciaRepository;
import java.math.BigDecimal;
import java.time.Clock;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional(readOnly = true)
public class MatchService {
    private final PerfilConvivenciaRepository perfiles;
    private final Clock clock;
    private final CompatibilityCalculator calculator = new CompatibilityCalculator();
    private static final Comparator<MatchResponse> BEST_FIRST = Comparator.comparing(MatchResponse::getPorcentaje).reversed()
            .thenComparing(MatchResponse::getIdUsuarioDestino);

    public MatchService(PerfilConvivenciaRepository perfiles, Clock clock) { this.perfiles = perfiles; this.clock = clock; }

    /** Legacy command returns a bounded first page; GET provides the complete pageable ranking. */
    public List<MatchResponse> calcularMatches(Integer usuario) {
        return listarMisMatches(usuario, BigDecimal.ZERO, PageRequest.of(0, 100)).getContent();
    }

    public Page<MatchResponse> listarMisMatches(Integer usuario, BigDecimal minimo, Pageable pageable) {
        if (pageable.getPageSize() < 1 || pageable.getPageSize() > 100 || pageable.getPageNumber() > 1000)
            throw new IllegalArgumentException("Usa páginas de 1 a 100 resultados y page entre 0 y 1000");
        if (minimo == null || minimo.signum() < 0 || minimo.compareTo(BigDecimal.valueOf(100)) > 0)
            throw new IllegalArgumentException("El índice mínimo debe estar entre 0 y 100");
        MatchCandidate origin = perfiles.findCandidates(Set.of(usuario)).stream().findFirst()
                .orElseThrow(() -> new ConflictException("Completa tu perfil de convivencia para descubrir personas compatibles"));
        CompatibilityProfile originProfile = origin.profile();
        if (calculator.calculate(originProfile, originProfile).porcentaje() == null)
            throw new ConflictException("Actualiza tu perfil: faltan preferencias válidas para calcular la compatibilidad");
        int limit = Math.toIntExact(pageable.getOffset() + pageable.getPageSize());
        PriorityQueue<MatchResponse> best = new PriorityQueue<>(Math.min(limit, 1000), BEST_FIRST.reversed());
        long total = 0;
        LocalDateTime time = LocalDateTime.now(clock);
        // One flat cursor, no find/save inside the loop and no persistent cache to go stale.
        try (var stream = perfiles.streamCandidates(usuario)) {
            var iterator = stream.iterator();
            while (iterator.hasNext()) {
                MatchCandidate candidate = iterator.next();
                var result = calculator.calculate(originProfile, candidate.profile());
                if (result.porcentaje() == null || result.porcentaje().compareTo(minimo) < 0) continue;
                total++;
                if (best.size() == limit) {
                    int scoreOrder = result.porcentaje().compareTo(best.peek().getPorcentaje());
                    if (scoreOrder < 0 || (scoreOrder == 0 && candidate.idUsuario() > best.peek().getIdUsuarioDestino())) continue;
                }
                MatchResponse response = response(candidate, result, time);
                if (best.size() < limit) best.add(response);
                else if (BEST_FIRST.compare(response, best.peek()) < 0) { best.poll(); best.add(response); }
            }
        }
        List<MatchResponse> page = best.stream().sorted(BEST_FIRST).skip(pageable.getOffset()).limit(pageable.getPageSize()).toList();
        return new PageImpl<>(page, pageable, total);
    }

    public Optional<CompatibilidadCalculada> obtenerCompatibilidadEntreUsuarios(Integer actual, Integer objetivo) {
        if (actual == null || objetivo == null || actual.equals(objetivo)) return Optional.empty();
        return Optional.ofNullable(obtenerCompatibilidades(actual, Set.of(objetivo)).get(objetivo));
    }

    public Map<Integer, CompatibilidadCalculada> obtenerCompatibilidades(Integer actual, Collection<Integer> objetivos) {
        if (actual == null || objetivos.isEmpty()) return Map.of();
        Set<Integer> ids = objetivos.stream().filter(Objects::nonNull).collect(Collectors.toSet());
        ids.remove(actual);
        if (ids.isEmpty()) return Map.of();
        if (ids.size() > 100) throw new IllegalArgumentException("La carga de compatibilidad admite hasta 100 personas");
        ids.add(actual);
        Map<Integer, MatchCandidate> batch = perfiles.findCandidates(ids).stream().collect(Collectors.toMap(MatchCandidate::idUsuario, p -> p));
        MatchCandidate origin = batch.get(actual);
        if (origin == null) return Map.of();
        Map<Integer, CompatibilidadCalculada> results = new HashMap<>();
        batch.forEach((id, candidate) -> {
            if (!id.equals(actual)) {
                var result = calculator.calculate(origin.profile(), candidate.profile());
                if (result.porcentaje() != null) results.put(id, new CompatibilidadCalculada(result));
            }
        });
        return results;
    }

    private MatchResponse response(MatchCandidate candidate, CompatibilityCalculator.Result result, LocalDateTime time) {
        MatchResponse response = new MatchResponse();
        // idMatch is retained as nullable legacy metadata; user ID identifies a live candidate.
        response.setIdUsuarioDestino(candidate.idUsuario());
        response.setNombres(candidate.nombres()); response.setApellidos(candidate.apellidos()); response.setEdad(candidate.edad());
        response.setOcupacion(candidate.ocupacion()); response.setUniversidad(candidate.universidad()); response.setFoto(candidate.foto());
        response.setPorcentaje(result.porcentaje()); response.setCoincidencias(result.coincidencias()); response.setDiferencias(result.diferencias());
        response.setFechaCalculo(time); response.setCobertura(result.cobertura()); response.setVersionAlgoritmo(CompatibilityCalculator.VERSION);
        response.setHayImporteComun(result.hayImporteComun()); response.setCriterios(result.criterios());
        return response;
    }
}
