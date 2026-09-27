package com.roommatch.service;

import com.roommatch.dto.ContactoUsuarioRequest;
import com.roommatch.exception.ConflictException;
import com.roommatch.model.*;
import com.roommatch.repository.*;
import java.util.Optional;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.*;

class ContactPreferencesTest {
    private final ContactoUsuarioRepository contacts = mock(ContactoUsuarioRepository.class);
    private final UsuarioRepository users = mock(UsuarioRepository.class);
    private final ContactoRoomieRepository connections = mock(ContactoRoomieRepository.class);
    private final ContactoUsuarioService service = new ContactoUsuarioService(contacts, users, connections);

    @Test void newContactDoesNotOptIntoSharingAndUsesOwnerLock() {
        var user = new Usuario(); user.setIdUsuario(1);
        when(users.lockById(1)).thenReturn(Optional.of(user));
        when(contacts.saveAndFlush(any())).thenAnswer(invocation -> invocation.getArgument(0));
        var request = new ContactoUsuarioRequest(); request.setEmailContacto("chosen@example.test");
        var saved = service.guardarOModificarMiContacto(1, request);
        assertThat(saved.getMostrarEmail()).isFalse();
        assertThat(saved.getEmailContacto()).isEqualTo("chosen@example.test");
        verify(users).lockById(1);
        verify(users, never()).findById(any());
    }

    @Test void stalePrivacyUpdateDoesNotChangeStoredPreferences() {
        var user = new Usuario(); user.setIdUsuario(1);
        var stored = new ContactoUsuario(); stored.setIdContacto(2); stored.setVersion(3L); stored.setUsuario(user);
        when(users.lockById(1)).thenReturn(Optional.of(user));
        when(contacts.findByUsuarioIdUsuario(1)).thenReturn(Optional.of(stored));
        var request = new ContactoUsuarioRequest(); request.setVersion(2L); request.setMostrarEmail(true);
        assertThatThrownBy(() -> service.guardarOModificarMiContacto(1, request)).isInstanceOf(ConflictException.class);
        assertThat(stored.getMostrarEmail()).isFalse();
        verify(contacts, never()).saveAndFlush(any());
    }

    @Test void emptyConnectionPageDoesNotLoadContactsAndBoundsAreEnforced() {
        when(connections.paginaConexiones(eq(1), any())).thenReturn(org.springframework.data.domain.Page.empty());
        assertThat(service.listarDesbloqueados(1, 0, 20)).isEmpty();
        verifyNoInteractions(contacts, users);
        assertThatThrownBy(() -> service.listarDesbloqueados(1, 0, 101)).isInstanceOf(IllegalArgumentException.class);
        assertThatThrownBy(() -> service.listarDesbloqueados(1, -1, 20)).isInstanceOf(IllegalArgumentException.class);
        assertThatThrownBy(() -> service.listarDesbloqueados(0, 0, 20)).isInstanceOf(IllegalArgumentException.class);
    }

    @ParameterizedTest @ValueSource(strings = {"javascript:alert(1)", "data:text/html,test", "https://evil.test/person", "https://instagram.com.evil.test/name", "https://evil.test@instagram.com/name", "https://instagram.com/name?redirect=evil", "http://instagram.com/name", "https://instagram.com/name#fragment"})
    void rejectsUntrustedSocialLinks(String url) {
        assertThatThrownBy(() -> ContactoValuePolicy.social(url, "instagram")).isInstanceOf(IllegalArgumentException.class);
    }

    @Test void permitsRealProfileShapesAndRejectsRedirectorsOrMalformedNumbers() {
        assertThat(ContactoValuePolicy.social(" @roommatch.peru ", "instagram")).isEqualTo("roommatch.peru");
        assertThat(ContactoValuePolicy.social("www.instagram.com/person", "instagram")).isEqualTo("https://www.instagram.com/person");
        assertThat(ContactoValuePolicy.social("https://www.facebook.com/profile.php?id=123", "facebook")).endsWith("id=123");
        assertThatThrownBy(() -> ContactoValuePolicy.social("https://facebook.com/l.php?u=https://evil.test", "facebook")).isInstanceOf(IllegalArgumentException.class);
        assertThat(ContactoValuePolicy.phone("+51 999 888 777", "Teléfono")).isEqualTo("+51 999 888 777");
        assertThat(ContactoValuePolicy.phone(" ", "Teléfono")).isNull();
        assertThatThrownBy(() -> ContactoValuePolicy.phone("123", "Teléfono")).isInstanceOf(IllegalArgumentException.class);
        assertThatThrownBy(() -> ContactoValuePolicy.phone("9999999\r\nX:evil", "Teléfono")).isInstanceOf(IllegalArgumentException.class);
    }

    @Test void emailLengthMatchesDatabaseAndVersionIsNotShared() {
        try (var factory = jakarta.validation.Validation.buildDefaultValidatorFactory()) {
            var request = new ContactoUsuarioRequest(); request.setEmailContacto("a".repeat(145) + "@test.com");
            assertThat(factory.getValidator().validate(request)).anySatisfy(v -> assertThat(v.getPropertyPath().toString()).isEqualTo("emailContacto"));
        }
        var contact = new ContactoUsuario(); contact.setVersion(10L);
        assertThat(com.roommatch.dto.ContactoUsuarioResponse.fromEntity(contact, true).getVersion()).isNull();
        assertThat(com.roommatch.dto.ContactoUsuarioResponse.fromEntity(contact, false).getVersion()).isEqualTo(10L);
    }
}
