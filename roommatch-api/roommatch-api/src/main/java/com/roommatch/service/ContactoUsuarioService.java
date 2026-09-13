package com.roommatch.service;

import com.roommatch.exception.ResourceNotFoundException;

import com.roommatch.dto.ContactoUsuarioRequest;
import com.roommatch.dto.ContactoUsuarioResponse;
import com.roommatch.model.ContactoUsuario;
import com.roommatch.model.Usuario;
import com.roommatch.repository.ContactoRoomieRepository;
import com.roommatch.repository.ContactoUsuarioRepository;
import com.roommatch.repository.UsuarioRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Objects;

@Service
public class ContactoUsuarioService {

    private final ContactoUsuarioRepository contactoRepository;
    private final UsuarioRepository usuarioRepository;
    private final ContactoRoomieRepository contactoRoomieRepository;

    public ContactoUsuarioService(
            ContactoUsuarioRepository contactoRepository,
            UsuarioRepository usuarioRepository,
            ContactoRoomieRepository contactoRoomieRepository
    ) {
        this.contactoRepository = contactoRepository;
        this.usuarioRepository = usuarioRepository;
        this.contactoRoomieRepository = contactoRoomieRepository;
    }

    @Transactional
    public ContactoUsuarioResponse guardarOModificarMiContacto(
            Integer idUsuario,
            ContactoUsuarioRequest request
    ) {
        Integer usuarioId = requerirId(idUsuario, "idUsuario");
        Objects.requireNonNull(request, "request");

        Usuario usuario = usuarioRepository
                .findById(usuarioId)
                .orElseThrow(() -> new ResourceNotFoundException("Usuario no encontrado"));

        ContactoUsuario contacto = contactoRepository
                .findByUsuarioIdUsuario(usuarioId)
                .orElseGet(ContactoUsuario::new);

        contacto.setUsuario(usuario);
        copiarDatos(request, contacto);

        ContactoUsuario guardado = contactoRepository.save(
                Objects.requireNonNull(contacto)
        );

        return ContactoUsuarioResponse.fromEntity(guardado, false);
    }

    @Transactional(readOnly = true)
    public ContactoUsuarioResponse obtenerMiContacto(Integer idUsuario) {
        Integer usuarioId = requerirId(idUsuario, "idUsuario");

        ContactoUsuario contacto = contactoRepository
                .findByUsuarioIdUsuario(usuarioId)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Aún no has registrado tus datos de contacto"
                ));

        return ContactoUsuarioResponse.fromEntity(contacto, false);
    }

    @Transactional(readOnly = true)
    public ContactoUsuarioResponse obtenerMiContactoOpcional(Integer idUsuario) {
        Integer usuarioId = requerirId(idUsuario, "idUsuario");

        return contactoRepository
                .findByUsuarioIdUsuario(usuarioId)
                .map(contacto -> ContactoUsuarioResponse.fromEntity(contacto, false))
                .orElse(null);
    }

    @Transactional(readOnly = true)
    public ContactoUsuarioResponse verContactoDesbloqueado(
            Integer idUsuarioActual,
            Integer idUsuarioObjetivo
    ) {
        Integer usuarioActualId = requerirId(idUsuarioActual, "idUsuarioActual");
        Integer usuarioObjetivoId = requerirId(idUsuarioObjetivo, "idUsuarioObjetivo");

        if (usuarioActualId.equals(usuarioObjetivoId)) {
            return obtenerMiContacto(usuarioActualId);
        }

        boolean contactoDesbloqueado = contactoRoomieRepository
                .existeContactoDesbloqueado(usuarioActualId, usuarioObjetivoId);

        if (!contactoDesbloqueado) {
            throw new org.springframework.security.access.AccessDeniedException(
                    "El contacto aún no está desbloqueado. Primero debe existir una solicitud aceptada"
            );
        }

        ContactoUsuario contacto = contactoRepository
                .findByUsuarioIdUsuario(usuarioObjetivoId)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "El usuario aún no registró datos de contacto"
                ));

        return ContactoUsuarioResponse.fromEntity(contacto, true);
    }

    private void copiarDatos(
            ContactoUsuarioRequest request,
            ContactoUsuario contacto
    ) {
        contacto.setTelefono(normalizarTexto(request.getTelefono()));
        contacto.setWhatsapp(normalizarTexto(request.getWhatsapp()));
        contacto.setInstagram(normalizarTexto(request.getInstagram()));
        contacto.setFacebook(normalizarTexto(request.getFacebook()));
        contacto.setEmailContacto(normalizarTexto(request.getEmailContacto()));
        contacto.setMostrarTelefono(Boolean.TRUE.equals(request.getMostrarTelefono()));
        contacto.setMostrarWhatsapp(Boolean.TRUE.equals(request.getMostrarWhatsapp()));
        contacto.setMostrarInstagram(Boolean.TRUE.equals(request.getMostrarInstagram()));
        contacto.setMostrarFacebook(Boolean.TRUE.equals(request.getMostrarFacebook()));
        contacto.setMostrarEmail(
                request.getMostrarEmail() == null || Boolean.TRUE.equals(request.getMostrarEmail())
        );
    }

    private String normalizarTexto(String valor) {
        return valor == null || valor.isBlank() ? null : valor.trim();
    }

    private Integer requerirId(Integer id, String nombre) {
        if (id == null || id <= 0) {
            throw new IllegalArgumentException(nombre + " debe ser un identificador válido");
        }
        return id;
    }
}
