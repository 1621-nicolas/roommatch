package com.roommatch.service;

import com.roommatch.dto.ImagePreviewRow;
import com.roommatch.model.Habitacion;
import com.roommatch.repository.*;
import java.util.*;
import org.junit.jupiter.api.Test;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.*;
import static org.mockito.ArgumentMatchers.*;

class ImagePreviewServiceTest {
    final ImagenHabitacionRepository rooms = mock(ImagenHabitacionRepository.class);
    final ImagenPublicacionRepository publications = mock(ImagenPublicacionRepository.class);
    final ImagePreviewService previews = new ImagePreviewService(rooms, publications, new ImageUrlPolicy(false));

    @Test
    void invalidHistoricalPrimaryFallsBackToFirstValidPhotoWithoutPublishingUnsafeUrls() {
        when(rooms.findPreviews(List.of(1, 2, 3))).thenReturn(List.of(
                new ImagePreviewRow(1, "javascript:alert(1)"), new ImagePreviewRow(2, "https://images.example/primary.jpg"),
                new ImagePreviewRow(3, "http://images.example/old.jpg"), new ImagePreviewRow(1, "https://images.example/á.jpg"),
                new ImagePreviewRow(2, "https://images.example/secondary.jpg")));
        assertThat(previews.rooms(List.of(1, 2, 3))).containsExactlyInAnyOrderEntriesOf(Map.of(
                1, "https://images.example/%C3%A1.jpg", 2, "https://images.example/primary.jpg"));
        verifyNoInteractions(publications);
    }

    @Test
    void emptyParentsDoNotIssueQueriesAndIdsAreDeduplicated() {
        assertThat(previews.rooms(List.of())).isEmpty();
        assertThat(previews.publications(List.of())).isEmpty();
        verifyNoInteractions(rooms, publications);
        when(publications.findPreviews(List.of(7))).thenReturn(List.of(new ImagePreviewRow(7, "https://images.example/7.jpg")));
        assertThat(previews.publications(Arrays.asList(7, null, 7))).containsEntry(7, "https://images.example/7.jpg");
        verify(publications).findPreviews(List.of(7));
    }

    @Test
    void roomPageLoadsAllThumbnailsWithOneQueryAndPreservesTotal() {
        var search = mock(HabitacionBusquedaRepository.class);
        var rows = java.util.stream.IntStream.rangeClosed(1, 50).mapToObj(id -> {
            var row = new Habitacion(); row.setIdHabitacion(id); return row;
        }).toList();
        var ids = rows.stream().map(Habitacion::getIdHabitacion).toList();
        when(search.buscarHabitacionesAvanzado(isNull(), isNull(), isNull(), isNull(), isNull(), isNull(), any()))
                .thenReturn(new PageImpl<>(rows, PageRequest.of(0, 50), 51));
        when(rooms.findPreviews(ids)).thenReturn(ids.stream().map(id -> new ImagePreviewRow(id, "https://images.example/" + id + ".jpg")).toList());
        var service = new HabitacionService(mock(HabitacionRepository.class), mock(PropietarioRepository.class), mock(PlanPolicy.class), search, previews);
        var page = service.listarHabitacionesPublicas(null, null, null, null, null, null, PageRequest.of(0, 50));
        assertThat(page.getTotalElements()).isEqualTo(51);
        assertThat(page.getContent()).hasSize(50).allSatisfy(dto ->
                assertThat(dto.getImagenPrincipal()).isEqualTo("https://images.example/" + dto.getIdHabitacion() + ".jpg"));
        verify(rooms, times(1)).findPreviews(ids);
        verifyNoMoreInteractions(rooms);
    }

    @Test
    void invisibleRoomIsRejectedBeforeReadingItsPhotos() {
        var parents = mock(HabitacionRepository.class);
        var policy = mock(PlanPolicy.class);
        var row = new Habitacion(); row.setIdHabitacion(5);
        when(parents.findById(5)).thenReturn(Optional.of(row));
        var service = new HabitacionService(parents, mock(PropietarioRepository.class), policy, mock(HabitacionBusquedaRepository.class), previews);
        assertThatThrownBy(() -> service.obtenerHabitacionPorId(5)).isInstanceOf(com.roommatch.exception.ResourceNotFoundException.class);
        verifyNoInteractions(rooms, publications);
    }
}
