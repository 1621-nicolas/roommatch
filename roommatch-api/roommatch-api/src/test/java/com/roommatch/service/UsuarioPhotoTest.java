package com.roommatch.service;

import com.roommatch.dto.ActualizarUsuarioRequest;
import com.roommatch.exception.ResourceNotFoundException;
import com.roommatch.model.Usuario;
import com.roommatch.repository.UsuarioRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import java.util.Optional;
import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.*;

class UsuarioPhotoTest {
    private final UsuarioRepository users = mock(UsuarioRepository.class);
    private final UsuarioService service = new UsuarioService(users, new ImageUrlPolicy(false));
    private Usuario user;
    @BeforeEach void setup() {
        user = new Usuario(); user.setIdUsuario(7); user.setNombres("Original"); user.setApellidos("Apellido");
        user.setFoto("https://example.test/old.jpg"); user.setEmail("private@example.test"); user.setPasswordHash("stored-hash");
        when(users.findById(7)).thenReturn(Optional.of(user));
        when(users.save(any(Usuario.class))).thenAnswer(invocation -> invocation.getArgument(0));
    }
    private ActualizarUsuarioRequest request(String photo) {
        ActualizarUsuarioRequest request = new ActualizarUsuarioRequest();
        request.setNombres(" Nuevo "); request.setApellidos(" Apellido "); request.setEdad(28); request.setFoto(photo);
        return request;
    }
    @ParameterizedTest
    @ValueSource(strings = {"javascript:alert(1)", "data:image/png;base64,abc", "http://example.test/a.jpg",
        "https://user:secret@example.test/a.jpg", "//example.test/a.jpg", "https://example.test/a#fragment", "https://example.test/a b"})
    void rejectsInvalidPhotoBeforeMutatingPersonalData(String photo) {
        assertThatThrownBy(() -> service.actualizarMiUsuario(7, request(photo))).isInstanceOf(IllegalArgumentException.class);
        assertThat(user.getNombres()).isEqualTo("Original");
        assertThat(user.getFoto()).isEqualTo("https://example.test/old.jpg");
        verify(users, never()).save(any());
    }
    @Test void storesValidEncodedPhotoWithoutChangingCredentials() {
        var response = service.actualizarMiUsuario(7, request(" https://example.test/á.jpg "));
        assertThat(response.getFoto()).isEqualTo("https://example.test/%C3%A1.jpg");
        assertThat(user.getNombres()).isEqualTo("Nuevo");
        assertThat(user.getEmail()).isEqualTo("private@example.test");
        assertThat(user.getPasswordHash()).isEqualTo("stored-hash");
    }
    @Test void blankPhotoRemovesOnlyTheOptionalImage() {
        assertThat(service.actualizarMiUsuario(7, request(" ")).getFoto()).isNull();
        assertThat(user.getEmail()).isEqualTo("private@example.test");
    }
    @Test void developmentCanKeepItsExplicitHttpConfiguration() {
        var local = new UsuarioService(users, new ImageUrlPolicy(true));
        assertThat(local.actualizarMiUsuario(7, request("http://localhost:8080/a.jpg")).getFoto()).isEqualTo("http://localhost:8080/a.jpg");
    }
    @Test void absentUserDoesNotBecomeAnUpsert() {
        when(users.findById(9)).thenReturn(Optional.empty());
        assertThatThrownBy(() -> service.actualizarMiUsuario(9, request("https://example.test/a.jpg"))).isInstanceOf(ResourceNotFoundException.class);
        verify(users, never()).save(any());
    }
}
