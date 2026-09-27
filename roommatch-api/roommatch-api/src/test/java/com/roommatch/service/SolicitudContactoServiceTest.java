package com.roommatch.service;

import com.roommatch.dto.SolicitudContactoRequest;
import com.roommatch.exception.ConflictException;
import com.roommatch.model.SolicitudContacto;
import com.roommatch.model.Usuario;
import com.roommatch.repository.*;
import java.time.*;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.springframework.security.access.AccessDeniedException;
import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.*;

class SolicitudContactoServiceTest {
    final SolicitudContactoRepository solicitudes = mock(SolicitudContactoRepository.class);
    final ContactoRoomieRepository contactos = mock(ContactoRoomieRepository.class);
    final UsuarioRepository usuarios = mock(UsuarioRepository.class);
    final NotificacionRepository notificaciones = mock(NotificacionRepository.class);
    final Clock clock = Clock.fixed(Instant.parse("2026-09-01T12:00:00Z"), ZoneOffset.UTC);
    final SolicitudContactoService service = new SolicitudContactoService(solicitudes, contactos, usuarios, notificaciones,
            clock, Duration.ofDays(30), Duration.ofDays(1));
    final SolicitudContacto solicitud = new SolicitudContacto();

    @BeforeEach
    void setup() {
        Usuario a = usuario(1), b = usuario(2);
        when(usuarios.lockById(1)).thenReturn(Optional.of(a));
        when(usuarios.lockById(2)).thenReturn(Optional.of(b));
        solicitud.setIdSolicitud(10);
        solicitud.setUsuarioEmisor(a);
        solicitud.setUsuarioReceptor(b);
        solicitud.setEstado("pendiente");
        solicitud.setFechaSolicitud(LocalDateTime.now(clock).minusDays(40));
        var partes = mock(SolicitudContactoRepository.Partes.class);
        when(partes.getEmisor()).thenReturn(1);
        when(partes.getReceptor()).thenReturn(2);
        when(solicitudes.partes(10)).thenReturn(Optional.of(partes));
        when(solicitudes.findById(10)).thenReturn(Optional.of(solicitud));
    }

    @ParameterizedTest
    @CsvSource({"aceptar,1", "aceptar,3", "rechazar,1", "rechazar,3", "cancelar,2", "cancelar,3"})
    void enforcesActorForEveryTransition(String accion, int actor) {
        assertThatThrownBy(() -> accion(accion, actor)).isInstanceOf(AccessDeniedException.class);
        verify(solicitudes, never()).saveAndFlush(any());
        verifyNoInteractions(contactos);
    }

    @ParameterizedTest
    @CsvSource({"aceptar,2,aceptada", "rechazar,2,rechazada", "cancelar,1,cancelada"})
    void onlyPendingRequestsCanTransition(String accion, int actor, String estado) {
        accion(accion, actor);
        assertThat(solicitud.getEstado()).isEqualTo(estado);
        assertThat(solicitud.getFechaRespuesta()).isEqualTo(LocalDateTime.now(clock));
        assertThatThrownBy(() -> accion(accion, actor)).isInstanceOf(ConflictException.class);
        verify(solicitudes, times(1)).saveAndFlush(solicitud);
        if ("aceptada".equals(estado)) verify(contactos).saveAndFlush(any());
        else verifyNoInteractions(contactos);
    }

    @Test
    void canRetryAfterRejectionWithoutOverwritingHistory() {
        solicitud.setEstado("rechazada");
        solicitud.setFechaRespuesta(LocalDateTime.now(clock).minusDays(30));
        when(solicitudes.findFirstByUsuarioEmisorIdUsuarioAndUsuarioReceptorIdUsuarioOrderByFechaSolicitudDescIdSolicitudDesc(1, 2))
                .thenReturn(Optional.of(solicitud));
        service.enviarSolicitud(1, 2, new SolicitudContactoRequest());
        verify(solicitudes).saveAndFlush(argThat(nueva -> nueva != solicitud && "pendiente".equals(nueva.getEstado())));
        assertThat(solicitud.getEstado()).isEqualTo("rechazada");
    }

    @ParameterizedTest
    @CsvSource({"rechazada,29", "cancelada,0"})
    void sameSenderMustRespectRetryWindow(String estado, int days) {
        solicitud.setEstado(estado);
        solicitud.setFechaRespuesta(LocalDateTime.now(clock).minusDays(days));
        when(solicitudes.findFirstByUsuarioEmisorIdUsuarioAndUsuarioReceptorIdUsuarioOrderByFechaSolicitudDescIdSolicitudDesc(1, 2))
                .thenReturn(Optional.of(solicitud));
        assertThatThrownBy(() -> service.enviarSolicitud(1, 2, new SolicitudContactoRequest())).isInstanceOf(ConflictException.class);
        verify(solicitudes, never()).saveAndFlush(any());
    }

    @Test
    void otherPersonCanInitiateAfterCancellationAndLocksHaveCanonicalOrder() {
        service.cancelarSolicitud(1, 10);
        clearInvocations(usuarios);
        service.enviarSolicitud(2, 1, new SolicitudContactoRequest());
        var order = inOrder(usuarios);
        order.verify(usuarios).lockById(1);
        order.verify(usuarios).lockById(2);
        verify(solicitudes).saveAndFlush(argThat(s -> s != solicitud && s.getUsuarioEmisor().getIdUsuario() == 2));
    }

    @Test
    void blocksPendingReciprocalRequestAndExistingContact() {
        when(solicitudes.existePendiente(2, 1)).thenReturn(true);
        assertThatThrownBy(() -> service.enviarSolicitud(2, 1, new SolicitudContactoRequest())).isInstanceOf(ConflictException.class);
        when(contactos.existeContactoDesbloqueado(1, 2)).thenReturn(true);
        assertThatThrownBy(() -> service.enviarSolicitud(1, 2, new SolicitudContactoRequest())).isInstanceOf(ConflictException.class);
        verify(solicitudes, never()).saveAndFlush(any());
    }

    @Test
    void cannotSendToSelfOrUnlockSuspendedUser() {
        assertThatThrownBy(() -> service.enviarSolicitud(1, 1, new SolicitudContactoRequest())).isInstanceOf(IllegalArgumentException.class);
        solicitud.getUsuarioEmisor().setEstado("suspendido");
        assertThatThrownBy(() -> service.aceptarSolicitud(2, 10)).isInstanceOf(AccessDeniedException.class);
        verify(contactos, never()).saveAndFlush(any());
    }

    void accion(String accion, int actor) {
        switch (accion) {
            case "aceptar" -> service.aceptarSolicitud(actor, 10);
            case "rechazar" -> service.rechazarSolicitud(actor, 10);
            case "cancelar" -> service.cancelarSolicitud(actor, 10);
            default -> throw new AssertionError(accion);
        }
    }

    static Usuario usuario(int id) {
        Usuario usuario = new Usuario();
        usuario.setIdUsuario(id); usuario.setNombres("Test"); usuario.setApellidos("Contacto"); usuario.setEstado("activo");
        return usuario;
    }
}
