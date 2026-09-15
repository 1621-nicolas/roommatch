package com.roommatch.service;

import com.roommatch.dto.*;
import com.roommatch.model.*;
import com.roommatch.repository.*;
import com.roommatch.exception.*;
import java.math.BigDecimal;
import java.util.Optional;
import org.junit.jupiter.api.Test;
import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

class ProfileEditingTest {
    final PerfilConvivenciaRepository profiles = mock(PerfilConvivenciaRepository.class);
    final UsuarioRepository users = mock(UsuarioRepository.class);
    final PerfilConvivenciaService service = new PerfilConvivenciaService(profiles, users, mock(PerfilConvivenciaNamedRepository.class));

    @Test void descriptionPatchOnlyUpdatesOwnedDescriptionAndCanClearIt() {
        PerfilConvivencia profile = new PerfilConvivencia(); profile.setPresupuestoMin(new BigDecimal("600")); profile.setLimpieza(4);
        when(profiles.findByUsuarioIdUsuario(1)).thenReturn(Optional.of(profile));
        when(profiles.saveAndFlush(any())).thenAnswer(i -> i.getArgument(0));
        assertThat(service.actualizarDescripcion(1, new PerfilDescripcionRequest("  Mi presentación  ", 0L)).getDescripcionPersonal()).isEqualTo("Mi presentación");
        assertThat(profile.getPresupuestoMin()).isEqualByComparingTo("600"); assertThat(profile.getLimpieza()).isEqualTo(4);
        service.actualizarDescripcion(1, new PerfilDescripcionRequest("", 0L));
        assertThat(profile.getDescripcionPersonal()).isNull();
        assertThatThrownBy(() -> service.actualizarDescripcion(2, new PerfilDescripcionRequest("Ajena", 0L))).isInstanceOf(ResourceNotFoundException.class);
        verify(profiles, times(2)).saveAndFlush(profile);
    }

    @Test void versionIsRequiredAndStaleVersionsCannotOverwrite() {
        PerfilConvivencia profile = new PerfilConvivencia();
        org.springframework.test.util.ReflectionTestUtils.setField(profile, "version", 2L);
        when(profiles.findByUsuarioIdUsuario(1)).thenReturn(Optional.of(profile));
        assertThatThrownBy(() -> service.actualizarDescripcion(1, new PerfilDescripcionRequest("Obsoleto", 1L))).isInstanceOf(ConflictException.class);
        assertThatThrownBy(() -> service.actualizarDescripcion(1, new PerfilDescripcionRequest("Sin versión", null))).isInstanceOf(IllegalArgumentException.class);
        verify(profiles, never()).saveAndFlush(any());
    }

    @Test void categoryValuesAndSqlMoneyPrecisionAreValidatedBeforeSave() {
        when(users.findById(1)).thenReturn(Optional.of(new Usuario()));
        var request = valid();
        when(profiles.saveAndFlush(any())).thenAnswer(i -> i.getArgument(0));
        assertThat(service.crearPerfil(1, request).getHorario()).isEqualTo("mañana");
        request.setAlcohol("desconocido");
        assertThatThrownBy(() -> service.crearPerfil(1, request)).isInstanceOf(IllegalArgumentException.class).hasMessageContaining("alcohol");
        final var invalidPrecision = valid(); invalidPrecision.setPresupuestoMin(new BigDecimal("12.123"));
        assertThatThrownBy(() -> service.crearPerfil(1, invalidPrecision)).isInstanceOf(IllegalArgumentException.class);
        verify(profiles, times(1)).saveAndFlush(any());
    }

    private PerfilConvivenciaRequest valid() {
        var request = new PerfilConvivenciaRequest();
        request.setPresupuestoMin(new BigDecimal("500.00")); request.setPresupuestoMax(new BigDecimal("800.00"));
        request.setDistritoPreferido("Lima"); request.setLimpieza(4); request.setRuido(3); request.setSociabilidad(3);
        request.setHorario(" MAÑANA "); request.setVisitas("bajas"); request.setMascotas("si"); request.setFumar("no");
        request.setAlcohol("no"); request.setGastos("divididos"); request.setConvivencia("tranquila");
        return request;
    }
}
