package com.roommatch.dto;
import com.roommatch.model.ModeracionEvento;
import java.time.Instant;
import java.time.ZoneOffset;
public record ModeracionEventoResponse(Integer idEvento, Integer idAdmin, String administrador, String accion, String motivo, Instant fecha) {
    public static ModeracionEventoResponse fromEntity(ModeracionEvento row) {
        var admin = row.getAdmin();
        return new ModeracionEventoResponse(row.getIdEvento(), admin.getIdUsuario(), admin.getNombres() + " " + admin.getApellidos(), row.getAccion(), row.getMotivo(), row.getFecha().toInstant(ZoneOffset.UTC));
    }
}
