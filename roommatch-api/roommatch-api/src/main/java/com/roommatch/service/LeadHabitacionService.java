package com.roommatch.service;

import com.roommatch.dto.LeadHabitacionRequest;
import com.roommatch.dto.LeadHabitacionResponse;
import com.roommatch.model.Habitacion;
import com.roommatch.model.LeadHabitacion;
import com.roommatch.model.Notificacion;
import com.roommatch.model.Propietario;
import com.roommatch.model.Usuario;
import com.roommatch.repository.HabitacionRepository;
import com.roommatch.repository.LeadHabitacionRepository;
import com.roommatch.repository.NotificacionRepository;
import com.roommatch.repository.PropietarioRepository;
import com.roommatch.repository.UsuarioRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Set;

@Service
public class LeadHabitacionService {

    private final LeadHabitacionRepository leadRepository;
    private final HabitacionRepository habitacionRepository;
    private final UsuarioRepository usuarioRepository;
    private final PropietarioRepository propietarioRepository;
    private final NotificacionRepository notificacionRepository;

    public LeadHabitacionService(
            LeadHabitacionRepository leadRepository,
            HabitacionRepository habitacionRepository,
            UsuarioRepository usuarioRepository,
            PropietarioRepository propietarioRepository,
            NotificacionRepository notificacionRepository
    ) {
        this.leadRepository = leadRepository;
        this.habitacionRepository = habitacionRepository;
        this.usuarioRepository = usuarioRepository;
        this.propietarioRepository = propietarioRepository;
        this.notificacionRepository = notificacionRepository;
    }

    @Transactional
    public LeadHabitacionResponse crearLead(
            Integer idUsuarioInteresado,
            Integer idHabitacion,
            LeadHabitacionRequest request
    ) {
        Usuario usuarioInteresado = usuarioRepository.findById(idUsuarioInteresado)
                .orElseThrow(() -> new IllegalArgumentException("Usuario interesado no encontrado"));

        Habitacion habitacion = habitacionRepository.findById(idHabitacion)
                .orElseThrow(() -> new IllegalArgumentException("Habitación no encontrada"));

        if (!habitacion.getEstado().equalsIgnoreCase("activa")) {
            throw new IllegalArgumentException("La habitación no está disponible");
        }

        Integer idUsuarioPropietario = habitacion.getPropietario().getUsuario().getIdUsuario();

        if (idUsuarioPropietario.equals(idUsuarioInteresado)) {
            throw new IllegalArgumentException("No puedes enviar interés a tu propia habitación");
        }

        if (leadRepository.existsByHabitacionIdHabitacionAndUsuarioInteresadoIdUsuario(
                idHabitacion,
                idUsuarioInteresado
        )) {
            throw new IllegalArgumentException("Ya enviaste interés por esta habitación");
        }

        LeadHabitacion lead = new LeadHabitacion();
        lead.setHabitacion(habitacion);
        lead.setUsuarioInteresado(usuarioInteresado);
        lead.setMensaje(request.getMensaje());
        lead.setEstado("pendiente");

        LeadHabitacion leadGuardado = leadRepository.save(lead);

        Notificacion notificacion = new Notificacion();
        notificacion.setUsuario(habitacion.getPropietario().getUsuario());
        notificacion.setTitulo("Nuevo interesado en tu habitación");
        notificacion.setMensaje(
                usuarioInteresado.getNombres() +
                " está interesado en tu habitación: " +
                habitacion.getTitulo()
        );
        notificacion.setTipo("lead");
        notificacion.setUrlDestino("/propietario/leads");

        notificacionRepository.save(notificacion);

        return LeadHabitacionResponse.fromEntity(leadGuardado);
    }

    public List<LeadHabitacionResponse> listarMisIntereses(Integer idUsuarioInteresado) {
        return leadRepository
                .findByUsuarioInteresadoIdUsuarioOrderByFechaLeadDesc(idUsuarioInteresado)
                .stream()
                .map(LeadHabitacionResponse::fromEntity)
                .toList();
    }

    public Page<LeadHabitacionResponse> listarLeadsPropietario(
            Integer idUsuarioPropietario,
            String estado,
            Pageable pageable
    ) {
        Propietario propietario = propietarioRepository.findByUsuarioIdUsuario(idUsuarioPropietario)
                .orElseThrow(() -> new IllegalArgumentException("No tienes perfil de propietario"));

        return leadRepository
                .listarLeadsDePropietario(
                        propietario.getIdPropietario(),
                        estado,
                        pageable
                )
                .map(LeadHabitacionResponse::fromEntity);
    }

    @Transactional
    public LeadHabitacionResponse cambiarEstadoLead(
            Integer idUsuarioPropietario,
            Integer idLead,
            String nuevoEstado
    ) {
        validarEstadoLead(nuevoEstado);

        Propietario propietario = propietarioRepository.findByUsuarioIdUsuario(idUsuarioPropietario)
                .orElseThrow(() -> new IllegalArgumentException("No tienes perfil de propietario"));

        LeadHabitacion lead = leadRepository
                .findByIdLeadAndHabitacionPropietarioIdPropietario(
                        idLead,
                        propietario.getIdPropietario()
                )
                .orElseThrow(() -> new IllegalArgumentException("Lead no encontrado o no pertenece a tus habitaciones"));

        lead.setEstado(nuevoEstado.toLowerCase());

        LeadHabitacion leadActualizado = leadRepository.save(lead);

        Notificacion notificacion = new Notificacion();
        notificacion.setUsuario(lead.getUsuarioInteresado());
        notificacion.setTitulo("Actualización de tu interés");
        notificacion.setMensaje(
                "Tu interés por la habitación \"" +
                lead.getHabitacion().getTitulo() +
                "\" cambió a estado: " +
                nuevoEstado.toLowerCase()
        );
        notificacion.setTipo("habitacion");
        notificacion.setUrlDestino("/mis-intereses");

        notificacionRepository.save(notificacion);

        return LeadHabitacionResponse.fromEntity(leadActualizado);
    }

    private void validarEstadoLead(String estado) {
        Set<String> estadosPermitidos = Set.of(
                "pendiente",
                "contactado",
                "cerrado",
                "rechazado"
        );

        if (estado == null || !estadosPermitidos.contains(estado.toLowerCase())) {
            throw new IllegalArgumentException("Estado de lead no válido");
        }
    }
}