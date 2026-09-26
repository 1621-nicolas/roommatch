package com.roommatch.matching;

import com.roommatch.repository.PerfilConvivenciaRepository;
import com.roommatch.service.MatchService;
import java.math.BigDecimal;
import java.time.*;
import java.util.*;
import org.junit.jupiter.api.Test;
import org.springframework.data.domain.PageRequest;
import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

class LiveMatchServiceTest {
    final PerfilConvivenciaRepository perfiles = mock(PerfilConvivenciaRepository.class);
    final MatchService service = new MatchService(perfiles, Clock.fixed(Instant.parse("2026-09-01T12:00:00Z"), ZoneOffset.UTC));

    @Test
    void ranksAllCandidatesBeforePagingAndNeverSavesOneMatchPerCandidate() {
        when(perfiles.findCandidates(Set.of(1))).thenReturn(List.of(candidate(1, 5)));
        when(perfiles.streamCandidates(1)).thenAnswer(invocation -> List.of(candidate(2, 1), candidate(3, 3), candidate(4, 5)).stream());
        var first = service.listarMisMatches(1, BigDecimal.ZERO, PageRequest.of(0, 1));
        var second = service.listarMisMatches(1, BigDecimal.ZERO, PageRequest.of(1, 1));
        assertThat(first.getTotalElements()).isEqualTo(3);
        assertThat(first.getContent().get(0).getIdUsuarioDestino()).isEqualTo(4);
        assertThat(second.getContent().get(0).getIdUsuarioDestino()).isEqualTo(3);
        assertThat(first.getContent().get(0).getIdMatch()).isNull();
        verify(perfiles, never()).save(any());
    }

    @Test
    void editingEitherProfileChangesNextCompatibilityAndBatchReadsOnlyOnce() {
        when(perfiles.findCandidates(anyCollection())).thenReturn(List.of(candidate(1, 5), candidate(2, 5)));
        assertThat(service.obtenerCompatibilidadEntreUsuarios(1, 2).orElseThrow().getPorcentaje()).isEqualByComparingTo("100");
        clearInvocations(perfiles);
        when(perfiles.findCandidates(anyCollection())).thenReturn(List.of(candidate(1, 5), candidate(2, 1), candidate(3, 4)));
        var batch = service.obtenerCompatibilidades(1, List.of(2, 3, 2));
        assertThat(batch.get(2).getPorcentaje()).isEqualByComparingTo("88");
        verify(perfiles, times(1)).findCandidates(anyCollection());
        when(perfiles.findCandidates(anyCollection())).thenReturn(List.of(candidate(1, 1), candidate(2, 1)));
        assertThat(service.obtenerCompatibilidadEntreUsuarios(1, 2).orElseThrow().getPorcentaje()).isEqualByComparingTo("100");
    }

    @Test
    void unauthenticatedOwnOrMissingProfileHasNoCompatibility() {
        assertThat(service.obtenerCompatibilidadEntreUsuarios(null, 2)).isEmpty();
        assertThat(service.obtenerCompatibilidadEntreUsuarios(1, 1)).isEmpty();
        verifyNoInteractions(perfiles);
        when(perfiles.findCandidates(anyCollection())).thenReturn(List.of(candidate(1, 5)));
        assertThat(service.obtenerCompatibilidadEntreUsuarios(1, 2)).isEmpty();
    }

    static MatchCandidate candidate(int id, int clean) {
        return new MatchCandidate(id, "Test", "Match", 25, null, null, null, "Lima", new BigDecimal("500"), new BigDecimal("900"),
                LocalDate.of(2026, 10, 1), clean, 3, 3, "mañana", "moderadas", "no", "no", "no", "divididos", "tranquila");
    }
}
