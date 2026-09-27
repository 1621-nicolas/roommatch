package com.roommatch.service;

import com.roommatch.dto.*;
import com.roommatch.model.*;
import com.roommatch.repository.*;
import com.roommatch.exception.*;
import java.time.Clock;
import java.util.Optional;
import org.junit.jupiter.api.Test;
import org.springframework.security.access.AccessDeniedException;
import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

class ReportModerationTest {
    final UsuarioRepository users = mock(UsuarioRepository.class);
    final HabitacionRepository rooms = mock(HabitacionRepository.class);
    final ReporteUsuarioRepository userReports = mock(ReporteUsuarioRepository.class);
    final ReporteHabitacionRepository roomReports = mock(ReporteHabitacionRepository.class);
    final ModeracionEventoRepository events = mock(ModeracionEventoRepository.class);
    final NotificacionRepository notifications = mock(NotificacionRepository.class);
    final PlanPolicy visibility = mock(PlanPolicy.class);
    final ReporteService service = new ReporteService(userReports, roomReports, users, rooms, notifications, events, new AdminAuthorization(users), visibility, Clock.systemUTC());

    @Test void everyAdministrativeActionRequiresAnActiveDatabaseAdmin() {
        when(users.findById(99)).thenReturn(Optional.of(user(99,"USUARIO")));
        assertThatThrownBy(() -> service.sancionarUsuario(99,7,"Motivo")).isInstanceOf(AccessDeniedException.class);
        Usuario admin = user(99,"ADMIN"); admin.setEstado("suspendido");
        when(users.findById(99)).thenReturn(Optional.of(admin));
        assertThatThrownBy(() -> service.listarReportesUsuarios(99,null,org.springframework.data.domain.PageRequest.of(0,10))).isInstanceOf(AccessDeniedException.class);
        verifyNoInteractions(userReports, roomReports, events);
    }

    @Test void closedReportCannotBeRewrittenOrSanctionedAndEachDecisionHasAnEvent() {
        admin(); var report = userReport();
        service.revisarReporteUsuario(99,7,"revisado","Analizando evidencia");
        service.revisarReporteUsuario(99,7,"rechazado","La evidencia no confirma la incidencia");
        assertThat(report.getEstado()).isEqualTo("rechazado");
        assertThatThrownBy(() -> service.sancionarUsuario(99,7,"Cambiar resultado")).isInstanceOf(ConflictException.class);
        assertThatThrownBy(() -> service.revisarReporteUsuario(99,7,"revisado","Volver atrás")).isInstanceOf(ConflictException.class);
        verify(events,times(2)).save(argThat(e -> e.getAdmin().getIdUsuario()==99 && e.getMotivo()!=null && e.getFecha()!=null));
        assertThat(report.getUsuarioReportado().getEstado()).isEqualTo("activo");
    }

    @Test void userSuspensionAndExplicitRestorationPreserveReportHistory() {
        admin(); var report = userReport();
        service.sancionarUsuario(99,7,"Evidencia comprobada");
        assertThat(report.getUsuarioReportado().getEstado()).isEqualTo("suspendido");
        service.restaurarUsuario(99,7,"Apelación revisada");
        assertThat(report.getUsuarioReportado().getEstado()).isEqualTo("activo");
        assertThat(report.getEstado()).isEqualTo("sancionado");
        verify(events,times(2)).save(any());
        assertThatThrownBy(() -> service.restaurarUsuario(99,7,"Duplicada")).isInstanceOf(ConflictException.class);
    }

    @Test void administrativeAccountsCannotBeSuspendedByReportActions() {
        admin(); var report = userReport(); report.setUsuarioReportado(user(99,"ADMIN"));
        assertThatThrownBy(() -> service.sancionarUsuario(99,7,"Accidental")).isInstanceOf(AccessDeniedException.class);
        verifyNoInteractions(events,notifications);
    }

    @Test void roomBlockPreservesArchiveAndRestorationDoesNotPublishIt() {
        admin();
        Propietario owner = new Propietario(); owner.setUsuario(user(2,"PROPIETARIO"));
        Habitacion room = new Habitacion(); room.setIdHabitacion(4); room.setPropietario(owner); room.setEstado("eliminada"); room.setDestacada(true);
        ReporteHabitacion report = new ReporteHabitacion(); report.setIdReporteHabitacion(8); report.setHabitacion(room); report.setUsuarioReportante(user(1,"USUARIO"));
        when(roomReports.lockById(8)).thenReturn(Optional.of(report));
        service.sancionarHabitacion(99,8,"Publicación fraudulenta");
        assertThat(room.getEstado()).isEqualTo("eliminada"); assertThat(room.getBloqueada()).isTrue(); assertThat(room.getDestacada()).isFalse();
        service.restaurarHabitacion(99,8,"Corrección verificada");
        assertThat(room.getEstado()).isEqualTo("eliminada"); assertThat(room.getBloqueada()).isFalse();
    }

    @Test void duplicateOpenReportIsRejectedBeforeCreatingAnotherRow() {
        Usuario reporter=user(1,"USUARIO"); when(users.lockById(1)).thenReturn(Optional.of(reporter));
        when(users.findById(2)).thenReturn(Optional.of(user(2,"USUARIO")));
        when(userReports.hasOpenReport(1,2)).thenReturn(true);
        assertThatThrownBy(() -> service.reportarUsuario(1,2,new ReporteRequest())).isInstanceOf(ConflictException.class);
        verify(userReports,never()).saveAndFlush(any());
    }

    @Test void reasonAndPageLimitsAreExplicitValidationErrors() {
        admin();
        assertThatThrownBy(() -> service.sancionarUsuario(99,7," ")).isInstanceOf(IllegalArgumentException.class);
        assertThatThrownBy(() -> service.sancionarUsuario(99,7,"x".repeat(501))).isInstanceOf(IllegalArgumentException.class);
        assertThatThrownBy(() -> service.listarReportesUsuarios(99,null,org.springframework.data.domain.PageRequest.of(0,101))).isInstanceOf(IllegalArgumentException.class);
        verifyNoInteractions(events,userReports);
    }

    private void admin() { when(users.findById(99)).thenReturn(Optional.of(user(99,"ADMIN"))); }
    private ReporteUsuario userReport() {
        ReporteUsuario row = new ReporteUsuario(); row.setIdReporte(7); row.setUsuarioReportante(user(1,"USUARIO")); row.setUsuarioReportado(user(2,"USUARIO"));
        when(userReports.lockById(7)).thenReturn(Optional.of(row)); return row;
    }
    private Usuario user(int id, String role) {
        Rol rol=new Rol(); rol.setNombreRol(role);
        Usuario user=new Usuario(); user.setIdUsuario(id); user.setRol(rol); user.setEstado("activo"); user.setNombres("Test"); user.setApellidos("Moderación"); return user;
    }
}
