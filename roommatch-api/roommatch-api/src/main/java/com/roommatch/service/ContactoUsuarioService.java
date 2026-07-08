package com.roommatch.service;

import com.roommatch.dto.ContactoUsuarioRequest;
import com.roommatch.dto.ContactoUsuarioResponse;

import com.roommatch.model.ContactoUsuario;
import com.roommatch.model.Usuario;

import com.roommatch.repository.ContactoRoomieRepository;
import com.roommatch.repository.ContactoUsuarioRepository;
import com.roommatch.repository.UsuarioRepository;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;


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

        this.contactoRepository =
                contactoRepository;

        this.usuarioRepository =
                usuarioRepository;

        this.contactoRoomieRepository =
                contactoRoomieRepository;
    }


    /*
     * =========================================================
     * GUARDAR O ACTUALIZAR MI CONTACTO
     * =========================================================
     */

    @Transactional
    public ContactoUsuarioResponse guardarOModificarMiContacto(

            Integer idUsuario,

            ContactoUsuarioRequest request

    ) {

        Usuario usuario =
                usuarioRepository
                        .findById(idUsuario)
                        .orElseThrow(

                                () ->
                                        new IllegalArgumentException(
                                                "Usuario no encontrado"
                                        )

                        );


        ContactoUsuario contacto =
                contactoRepository
                        .findByUsuarioIdUsuario(idUsuario)
                        .orElse(
                                new ContactoUsuario()
                        );


        contacto.setUsuario(usuario);


        copiarDatos(
                request,
                contacto
        );


        ContactoUsuario contactoGuardado =
                contactoRepository.save(
                        contacto
                );


        return ContactoUsuarioResponse
                .fromEntity(

                        contactoGuardado,

                        false

                );
    }


    /*
     * =========================================================
     * OBTENER MI CONTACTO OBLIGATORIO
     * =========================================================
     */

    @Transactional(readOnly = true)
    public ContactoUsuarioResponse obtenerMiContacto(

            Integer idUsuario

    ) {

        ContactoUsuario contacto =
                contactoRepository
                        .findByUsuarioIdUsuario(idUsuario)
                        .orElseThrow(

                                () ->
                                        new IllegalArgumentException(

                                                "Aún no has registrado tus datos de contacto"

                                        )

                        );


        return ContactoUsuarioResponse
                .fromEntity(

                        contacto,

                        false

                );
    }


    /*
     * =========================================================
     * OBTENER MI CONTACTO OPCIONAL
     * =========================================================
     */

    @Transactional(readOnly = true)
    public ContactoUsuarioResponse obtenerMiContactoOpcional(

            Integer idUsuario

    ) {

        return contactoRepository
                .findByUsuarioIdUsuario(
                        idUsuario
                )
                .map(

                        contacto ->

                                ContactoUsuarioResponse
                                        .fromEntity(

                                                contacto,

                                                false

                                        )

                )
                .orElse(null);
    }


    /*
     * =========================================================
     * VER CONTACTO DESBLOQUEADO
     * =========================================================
     */

    @Transactional(readOnly = true)
    public ContactoUsuarioResponse verContactoDesbloqueado(

            Integer idUsuarioActual,

            Integer idUsuarioObjetivo

    ) {

        /*
         * No se consulta el propio contacto
         * mediante contacto desbloqueado.
         */

        if (
                idUsuarioActual.equals(
                        idUsuarioObjetivo
                )
        ) {

            throw new IllegalArgumentException(

                    "No necesitas desbloquear tu propio contacto"

            );
        }


        /*
         * Verificar conexión aceptada.
         */

        boolean contactoDesbloqueado =
                contactoRoomieRepository
                        .existeContactoDesbloqueado(

                                idUsuarioActual,

                                idUsuarioObjetivo

                        );


        if (!contactoDesbloqueado) {

            throw new IllegalArgumentException(

                    "El contacto aún no está desbloqueado. Primero debe existir una solicitud aceptada"

            );
        }


        /*
         * Buscar información de contacto
         * del usuario objetivo.
         */

        ContactoUsuario contacto =
                contactoRepository
                        .findByUsuarioIdUsuario(
                                idUsuarioObjetivo
                        )
                        .orElseThrow(

                                () ->
                                        new IllegalArgumentException(

                                                "El usuario aún no registró datos de contacto"

                                        )

                        );


        /*
         * true:
         *
         * Aplicar privacidad.
         */

        return ContactoUsuarioResponse
                .fromEntity(

                        contacto,

                        true

                );
    }


    /*
     * =========================================================
     * COPIAR DATOS
     * =========================================================
     */

    private void copiarDatos(

            ContactoUsuarioRequest request,

            ContactoUsuario contacto

    ) {

        contacto.setTelefono(
                request.getTelefono()
        );


        contacto.setWhatsapp(
                request.getWhatsapp()
        );


        contacto.setInstagram(
                request.getInstagram()
        );


        contacto.setFacebook(
                request.getFacebook()
        );


        contacto.setEmailContacto(
                request.getEmailContacto()
        );


        contacto.setMostrarTelefono(

                valorBoolean(

                        request.getMostrarTelefono(),

                        false

                )

        );


        contacto.setMostrarWhatsapp(

                valorBoolean(

                        request.getMostrarWhatsapp(),

                        false

                )

        );


        contacto.setMostrarInstagram(

                valorBoolean(

                        request.getMostrarInstagram(),

                        false

                )

        );


        contacto.setMostrarFacebook(

                valorBoolean(

                        request.getMostrarFacebook(),

                        false

                )

        );


        contacto.setMostrarEmail(

                valorBoolean(

                        request.getMostrarEmail(),

                        true

                )

        );
    }


    /*
     * =========================================================
     * VALOR BOOLEAN SEGURO
     * =========================================================
     */

    private Boolean valorBoolean(

            Boolean valor,

            Boolean defecto

    ) {

        return valor != null
                ? valor
                : defecto;
    }
}