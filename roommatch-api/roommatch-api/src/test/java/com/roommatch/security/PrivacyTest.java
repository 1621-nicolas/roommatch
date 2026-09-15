package com.roommatch.security;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.roommatch.dto.*;
import com.roommatch.model.*;
import com.roommatch.repository.*;
import com.roommatch.service.ContactoUsuarioService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;
import org.springframework.security.access.AccessDeniedException;
import java.util.Optional;
import java.util.stream.IntStream;
import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.*;

class PrivacyTest {
    private final ContactoUsuarioRepository contacts = mock(ContactoUsuarioRepository.class);
    private final UsuarioRepository users = mock(UsuarioRepository.class);
    private final ContactoRoomieRepository connections = mock(ContactoRoomieRepository.class);
    private final ContactoUsuarioService service = new ContactoUsuarioService(contacts, users, connections);
    static IntStream flags() { return IntStream.range(0, 32); }

    private ContactoUsuario contact(int mask) {
        var c = new ContactoUsuario();
        var user = new Usuario(); user.setIdUsuario(2); user.setNombres("B"); user.setApellidos("Fixture");
        c.setUsuario(user);
        c.setTelefono("tel"); c.setWhatsapp("wa"); c.setInstagram("ig"); c.setFacebook("fb"); c.setEmailContacto("private@example.test");
        c.setMostrarTelefono((mask & 1) != 0); c.setMostrarWhatsapp((mask & 2) != 0);
        c.setMostrarInstagram((mask & 4) != 0); c.setMostrarFacebook((mask & 8) != 0); c.setMostrarEmail((mask & 16) != 0);
        return c;
    }

    @ParameterizedTest @MethodSource("flags")
    void unlockedContactRespectsEveryVisibilityCombination(int mask) {
        when(connections.existeContactoDesbloqueado(1, 2)).thenReturn(true);
        when(contacts.findByUsuarioIdUsuario(2)).thenReturn(Optional.of(contact(mask)));
        var dto = service.verContactoDesbloqueado(1, 2);
        assertThat(dto.getTelefono()).isEqualTo((mask & 1) != 0 ? "tel" : null);
        assertThat(dto.getWhatsapp()).isEqualTo((mask & 2) != 0 ? "wa" : null);
        assertThat(dto.getInstagram()).isEqualTo((mask & 4) != 0 ? "ig" : null);
        assertThat(dto.getFacebook()).isEqualTo((mask & 8) != 0 ? "fb" : null);
        assertThat(dto.getEmailContacto()).isEqualTo((mask & 16) != 0 ? "private@example.test" : null);
    }

    @Test void ownerAlwaysSeesOwnFields() {
        when(contacts.findByUsuarioIdUsuario(2)).thenReturn(Optional.of(contact(0)));
        var dto = service.verContactoDesbloqueado(2, 2);
        assertThat(dto.getTelefono()).isEqualTo("tel");
        assertThat(dto.getWhatsapp()).isEqualTo("wa");
        assertThat(dto.getEmailContacto()).isEqualTo("private@example.test");
        verifyNoInteractions(connections);
    }

    @Test void unrelatedAccountCannotReadContact() {
        assertThatThrownBy(() -> service.verContactoDesbloqueado(3, 2)).isInstanceOf(AccessDeniedException.class);
        verifyNoInteractions(contacts);
    }

    @Test void discoveryDtosDoNotSerializeLoginEmail() {
        var user = new Usuario(); user.setIdUsuario(2); user.setEmail("private@example.test");
        var match = new MatchResultado(); match.setUsuarioDestino(user);
        var favorite = new FavoritoUsuario(); favorite.setUsuarioFavorito(user);
        var mapper = new ObjectMapper();
        for (Object dto : new Object[]{MatchResponse.fromEntity(match), FavoritoResponse.fromEntity(favorite)}) {
            var json = mapper.valueToTree(dto);
            assertThat(json.has("email")).isFalse();
            assertThat(json.toString()).doesNotContain("private@example.test");
        }
    }
}
