package com.roommatch.service;

import com.roommatch.dto.*;
import com.roommatch.exception.ConflictException;
import com.roommatch.exception.ResourceNotFoundException;
import com.roommatch.model.*;
import com.roommatch.repository.*;
import java.math.BigDecimal;
import java.time.Clock;
import java.util.*;
import java.util.stream.IntStream;
import org.junit.jupiter.api.Test;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

class PublicacionRoomieServiceTest {
    final PublicacionRoomieRepository publicaciones = mock(PublicacionRoomieRepository.class);
    final UsuarioRepository usuarios = mock(UsuarioRepository.class);
    final HabitacionRepository habitaciones = mock(HabitacionRepository.class);
    final MatchService matches = mock(MatchService.class);
    final PlanPolicy policy = mock(PlanPolicy.class);
    final PublicacionRoomieService service = new PublicacionRoomieService(publicaciones, usuarios, matches, habitaciones, policy, Clock.systemUTC(), mock(ImagePreviewService.class));

    @Test
    void logicalDeletePreservesRowAndCannotBeReactivated() {
        PublicacionRoomie row = publication(1);
        when(publicaciones.findByIdPublicacionAndUsuarioIdUsuario(1, 1)).thenReturn(Optional.of(row));
        service.eliminarPublicacion(1, 1);
        assertThat(row.getEstado()).isEqualTo("eliminada");
        verify(publicaciones).saveAndFlush(row);
        verify(publicaciones, never()).delete(any());
        assertThatThrownBy(() -> service.activarPublicacion(1, 1)).isInstanceOf(ResourceNotFoundException.class);
        assertThatThrownBy(() -> service.eliminarPublicacion(2, 1)).isInstanceOf(ResourceNotFoundException.class);
    }

    @Test
    void pageLoadsCompatibilityOnceInsteadOfPerCard() {
        var rows = IntStream.rangeClosed(2, 51).mapToObj(PublicacionRoomieServiceTest::publication).toList();
        when(publicaciones.buscarPublicaciones(isNull(), isNull(), isNull(), isNull(), any())).thenReturn(new PageImpl<>(rows));
        var result = service.listarPublicaciones(1, null, null, null, null, PageRequest.of(0, 50));
        assertThat(result.getContent()).hasSize(50);
        verify(matches, times(1)).obtenerCompatibilidades(eq(1), argThat(ids -> ids.size() == 50));
        verify(matches, never()).obtenerCompatibilidadEntreUsuarios(any(), any());
        verifyNoInteractions(habitaciones);
    }

    @Test
    void withdrawnLinkedRoomDoesNotLeakItsFormerDetails() {
        var row = publication(1);
        Habitacion room = new Habitacion(); room.setIdHabitacion(50); room.setTitulo("Información retirada");
        row.setHabitacion(room); row.setTipoVinculacionVivienda("roommatch");
        when(publicaciones.findById(1)).thenReturn(Optional.of(row));
        var response = service.obtenerPublicacion(null, 1);
        assertThat(response.getViviendaReferenciaDisponible()).isFalse();
        assertThat(response.getIdHabitacion()).isNull();
        assertThat(response.getHabitacionTitulo()).isNull();
    }

    @Test
    void publicThirdPartyReferenceIsAllowedButPrivateRoomIsRejected() {
        var author = publication(1).getUsuario();
        when(usuarios.findById(1)).thenReturn(Optional.of(author));
        Habitacion room = new Habitacion(); room.setIdHabitacion(50); room.setTitulo("Anuncio público de otra persona");
        when(habitaciones.findById(50)).thenReturn(Optional.of(room));
        when(habitaciones.findPublicIds(anyCollection(), any())).thenReturn(List.of(50));
        when(publicaciones.saveAndFlush(any())).thenAnswer(i -> i.getArgument(0));
        PublicacionRoomieRequest request = new PublicacionRoomieRequest();
        request.setTipoPublicacion("busco_compartir"); request.setTitulo("Busco con quién compartir");
        request.setDescripcion("Referencia de interés"); request.setDistrito("Lima");
        request.setTipoVinculacionVivienda("roommatch"); request.setIdHabitacion(50);
        when(policy.visible(room)).thenReturn(true);
        assertThat(service.crearPublicacion(1, request).getIdHabitacion()).isEqualTo(50);
        when(policy.visible(room)).thenReturn(false);
        assertThatThrownBy(() -> service.crearPublicacion(1, request)).isInstanceOf(ConflictException.class);
        verify(publicaciones, times(1)).saveAndFlush(any());
    }

    static PublicacionRoomie publication(int author) {
        Usuario user = new Usuario(); user.setIdUsuario(author); user.setEstado("activo"); user.setNombres("Test"); user.setApellidos("Autor");
        PublicacionRoomie row = new PublicacionRoomie(); row.setIdPublicacion(author); row.setUsuario(user); row.setEstado("activa");
        return row;
    }
}
