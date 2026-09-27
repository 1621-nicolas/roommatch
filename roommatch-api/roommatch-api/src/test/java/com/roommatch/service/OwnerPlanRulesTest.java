package com.roommatch.service;

import com.roommatch.dto.HabitacionRequest;
import com.roommatch.dto.PropietarioRequest;
import com.roommatch.exception.ConflictException;
import com.roommatch.exception.ResourceNotFoundException;
import com.roommatch.model.*;
import com.roommatch.repository.*;
import java.time.*;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.security.access.AccessDeniedException;
import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.*;

class OwnerPlanRulesTest {
    final HabitacionRepository rooms = mock(HabitacionRepository.class);
    final PropietarioRepository owners = mock(PropietarioRepository.class);
    final SuscripcionPropietarioRepository subs = mock(SuscripcionPropietarioRepository.class);
    final PlanPropietarioRepository plans = mock(PlanPropietarioRepository.class);
    final Clock clock = Clock.fixed(Instant.parse("2026-09-01T12:00:00Z"), ZoneOffset.UTC);
    final PlanPolicy policy = new PlanPolicy(subs, rooms, clock);
    final HabitacionService service = new HabitacionService(rooms, owners, policy, mock(HabitacionBusquedaRepository.class), mock(ImagePreviewService.class));
    final PlanPropietarioService planService = new PlanPropietarioService(plans, owners, subs, mock(NotificacionRepository.class), rooms, policy, clock);
    final Propietario owner = new Propietario();
    final PlanPropietario plan = new PlanPropietario();
    final SuscripcionPropietario subscription = new SuscripcionPropietario();
    final Habitacion room = new Habitacion();

    @BeforeEach
    void setup() {
        Usuario user = new Usuario(); user.setIdUsuario(1); user.setEstado("activo");
        owner.setUsuario(user); owner.setIdPropietario(2); owner.setEstado("activo");
        plan.setIdPlan(3); plan.setLimiteHabitaciones(1); plan.setEstado("activo"); plan.setPermiteDestacar(false);
        subscription.setPlan(plan); subscription.setPropietario(owner); subscription.setFechaInicio(LocalDateTime.now(clock).minusDays(1));
        room.setIdHabitacion(4); room.setPropietario(owner); room.setEstado("pausada");
        when(owners.lockByUsuarioId(1)).thenReturn(Optional.of(owner));
        when(subs.findFirstByPropietarioIdPropietarioAndEstadoOrderByFechaInicioDesc(2, "activo")).thenReturn(Optional.of(subscription));
        when(rooms.findByIdHabitacionAndPropietarioIdPropietario(4, 2)).thenReturn(Optional.of(room));
        when(rooms.saveAndFlush(any())).thenAnswer(i -> i.getArgument(0));
    }

    @Test
    void downgradeFromTenToOneWithEightOccupiedIsRejectedWithoutMutation() {
        PlanPropietario one = new PlanPropietario(); one.setIdPlan(5); one.setEstado("activo"); one.setLimiteHabitaciones(1);
        plan.setLimiteHabitaciones(10);
        when(plans.findById(5)).thenReturn(Optional.of(one));
        when(rooms.countByPropietarioIdPropietarioAndEstadoIn(2, PlanPolicy.ESTADOS_CON_CUPO)).thenReturn(8L);
        assertThatThrownBy(() -> planService.cambiarPlan(1, 5)).isInstanceOf(ConflictException.class).hasMessageContaining("Tienes 8");
        verify(subs, never()).saveAndFlush(any());
        assertThat(subscription.getEstado()).isEqualTo("activo");
    }

    @Test
    void pausedRoomsCannotBypassLegacyOverQuotaButWithinQuotaReactivationDoesNotCountTwice() {
        when(rooms.countByPropietarioIdPropietarioAndEstadoIn(2, PlanPolicy.ESTADOS_CON_CUPO)).thenReturn(8L);
        assertThatThrownBy(() -> service.activarHabitacion(1, 4)).isInstanceOf(ConflictException.class);
        assertThat(room.getEstado()).isEqualTo("pausada");
        when(rooms.countByPropietarioIdPropietarioAndEstadoIn(2, PlanPolicy.ESTADOS_CON_CUPO)).thenReturn(1L);
        assertThat(service.activarHabitacion(1, 4).getEstado()).isEqualTo("activa");
    }

    @Test
    void rentedRoomConsumesNewSlotAndCannotBecomePausedToEvadeQuota() {
        room.setEstado("alquilada");
        when(rooms.countByPropietarioIdPropietarioAndEstadoIn(2, PlanPolicy.ESTADOS_CON_CUPO)).thenReturn(1L);
        assertThatThrownBy(() -> service.activarHabitacion(1, 4)).isInstanceOf(ConflictException.class);
        assertThatThrownBy(() -> service.pausarHabitacion(1, 4)).isInstanceOf(ConflictException.class);
    }

    @Test
    void expirationBoundaryHidesRoomAndBlocksReactivation() {
        subscription.setFechaFin(LocalDateTime.now(clock));
        assertThatThrownBy(() -> service.activarHabitacion(1, 4)).isInstanceOf(ConflictException.class);
        room.setEstado("activa");
        assertThat(policy.visible(room)).isFalse();
        subscription.setFechaFin(LocalDateTime.now(clock).plusSeconds(1));
        assertThat(policy.visible(room)).isTrue();
    }

    @Test
    void moderationCannotBeUndoneByOwnerAndArchiveKeepsHistoricalRecord() {
        room.setBloqueada(true);
        assertThatThrownBy(() -> service.activarHabitacion(1, 4)).isInstanceOf(AccessDeniedException.class);
        assertThat(service.archivarHabitacion(1, 4).getEstado()).isEqualTo("eliminada");
        assertThat(room.getBloqueada()).isTrue();
        assertThatThrownBy(() -> service.activarHabitacion(1, 4)).isInstanceOf(ConflictException.class);
        verify(rooms, never()).delete(any());
    }

    @Test
    void foreignRoomIsNotWritable() {
        assertThatThrownBy(() -> service.activarHabitacion(1, 999)).isInstanceOf(ResourceNotFoundException.class);
        assertThatThrownBy(() -> service.actualizarHabitacion(1, 999, new HabitacionRequest())).isInstanceOf(ResourceNotFoundException.class);
        verify(rooms, never()).saveAndFlush(any());
    }

    @Test
    void conversionPreservesAdministratorRole() {
        UsuarioRepository users = mock(UsuarioRepository.class);
        RolRepository roles = mock(RolRepository.class);
        Usuario admin = owner.getUsuario();
        Rol role = new Rol(); role.setNombreRol("ADMIN"); admin.setRol(role);
        Rol ownerRole = new Rol(); ownerRole.setNombreRol("PROPIETARIO");
        when(users.lockById(1)).thenReturn(Optional.of(admin));
        when(roles.findByNombreRol("PROPIETARIO")).thenReturn(Optional.of(ownerRole));
        when(plans.findByNombrePlan("Gratis")).thenReturn(Optional.of(plan));
        when(owners.saveAndFlush(any())).thenAnswer(i -> i.getArgument(0));
        when(subs.saveAndFlush(any())).thenAnswer(i -> i.getArgument(0));
        var conversion = new PropietarioService(owners, users, roles, plans, subs, mock(NotificacionRepository.class));
        PropietarioRequest request = new PropietarioRequest(); request.setTipoPropietario("persona");
        conversion.convertirmeEnPropietario(1, request);
        assertThat(admin.getRol().getNombreRol()).isEqualTo("ADMIN");
    }
}
