package com.roommatch.service;

import com.roommatch.dto.NotificacionResponse;
import com.roommatch.model.Notificacion;
import com.roommatch.repository.NotificacionRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class NotificacionService {

    private final NotificacionRepository notificacionRepository;

    public NotificacionService(NotificacionRepository notificacionRepository) {
        this.notificacionRepository = notificacionRepository;
    }

    public Page<NotificacionResponse> listarMisNotificaciones(
            Integer idUsuario,
            Boolean leido,
            Pageable pageable
    ) {
        return notificacionRepository
                .listarPorUsuario(idUsuario, leido, pageable)
                .map(NotificacionResponse::fromEntity);
    }

    public long contarNoLeidas(Integer idUsuario) {
        return notificacionRepository.countByUsuarioIdUsuarioAndLeidoFalse(idUsuario);
    }

    @Transactional
    public NotificacionResponse marcarComoLeida(
            Integer idUsuario,
            Integer idNotificacion
    ) {
        Notificacion notificacion = notificacionRepository
                .findByIdNotificacionAndUsuarioIdUsuario(idNotificacion, idUsuario)
                .orElseThrow(() -> new IllegalArgumentException("Notificación no encontrada"));

        notificacion.setLeido(true);

        Notificacion notificacionActualizada = notificacionRepository.save(notificacion);

        return NotificacionResponse.fromEntity(notificacionActualizada);
    }

    @Transactional
    public int marcarTodasComoLeidas(Integer idUsuario) {
        return notificacionRepository.marcarTodasComoLeidas(idUsuario);
    }
}