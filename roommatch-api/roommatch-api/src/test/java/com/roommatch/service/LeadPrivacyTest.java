package com.roommatch.service;

import com.roommatch.dto.*;
import com.roommatch.exception.ResourceNotFoundException;
import com.roommatch.model.*;
import com.roommatch.repository.*;
import java.util.Optional;
import org.junit.jupiter.api.Test;
import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

class LeadPrivacyTest {
    final LeadHabitacionRepository leads = mock(LeadHabitacionRepository.class);
    final HabitacionRepository rooms = mock(HabitacionRepository.class);
    final UsuarioRepository users = mock(UsuarioRepository.class);
    final PropietarioRepository owners = mock(PropietarioRepository.class);
    final NotificacionRepository notifications = mock(NotificacionRepository.class);
    final PlanPolicy policy = mock(PlanPolicy.class);
    final LeadHabitacionService service = new LeadHabitacionService(leads, rooms, users, owners, notifications, policy);

    @Test
    void onlyExplicitContactEmailIsReturnedIncludingForHistoricalRows() {
        Usuario user = new Usuario(); user.setIdUsuario(1); user.setEmail("private@example.test");
        LeadHabitacion lead = new LeadHabitacion(); lead.setUsuarioInteresado(user);
        assertThat(LeadHabitacionResponse.fromEntity(lead).getEmailInteresado()).isNull();
        lead.setEmailContacto("chosen@example.test");
        assertThat(LeadHabitacionResponse.fromEntity(lead).getEmailInteresado()).isEqualTo("chosen@example.test");
    }

    @Test
    void creationPersistsSelectedEmailAndRequiresPubliclyAvailableRoom() {
        Usuario interested = new Usuario(); interested.setIdUsuario(1); interested.setEmail("private@example.test");
        Usuario ownerUser = new Usuario(); ownerUser.setIdUsuario(2);
        Propietario owner = new Propietario(); owner.setUsuario(ownerUser);
        Habitacion room = new Habitacion(); room.setIdHabitacion(3); room.setPropietario(owner); room.setEstado("activa");
        when(users.findById(1)).thenReturn(Optional.of(interested));
        when(rooms.findById(3)).thenReturn(Optional.of(room));
        when(leads.save(any())).thenAnswer(i -> i.getArgument(0));
        LeadHabitacionRequest request = new LeadHabitacionRequest(); request.setEmailContacto("  chosen@example.test  ");
        assertThatThrownBy(() -> service.crearLead(1, 3, request)).isInstanceOf(ResourceNotFoundException.class);
        verifyNoInteractions(leads, notifications);
        when(policy.visible(room)).thenReturn(true);
        assertThat(service.crearLead(1, 3, request).getEmailInteresado()).isEqualTo("chosen@example.test");
        verify(leads).save(argThat(row -> "chosen@example.test".equals(row.getEmailContacto())));
    }

    @Test
    void ownerCannotChangeAnotherOwnersInquiry() {
        Propietario owner = new Propietario(); owner.setIdPropietario(9);
        when(owners.findByUsuarioIdUsuario(2)).thenReturn(Optional.of(owner));
        assertThatThrownBy(() -> service.cambiarEstadoLead(2, 50, "contactado")).isInstanceOf(ResourceNotFoundException.class);
        verify(leads).findByIdLeadAndHabitacionPropietarioIdPropietario(50, 9);
        verify(leads, never()).save(any());
        verifyNoInteractions(notifications);
    }

    @Test
    void emailValidationRejectsMalformedAndOversizedValuesButAllowsOmission() {
        try (var factory = jakarta.validation.Validation.buildDefaultValidatorFactory()) {
            var validator = factory.getValidator();
            LeadHabitacionRequest request = new LeadHabitacionRequest();
            assertThat(validator.validate(request)).isEmpty();
            request.setEmailContacto("invalid"); assertThat(validator.validate(request)).isNotEmpty();
            request.setEmailContacto("x".repeat(151) + "@example.test"); assertThat(validator.validate(request)).isNotEmpty();
            request.setEmailContacto("chosen@example.test"); assertThat(validator.validate(request)).isEmpty();
        }
    }
}
