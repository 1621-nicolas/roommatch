package com.roommatch.dto;

public class DashboardAdminResponse {

    private Long totalUsuarios;
    private Long usuariosActivos;
    private Long usuariosSuspendidos;

    private Long totalPropietarios;
    private Long totalPerfilesConvivencia;

    private Long totalHabitaciones;
    private Long habitacionesActivas;
    private Long habitacionesPausadas;

    private Long totalPublicacionesRoomie;
    private Long publicacionesActivas;

    private Long totalMatches;
    private Long solicitudesPendientes;
    private Long leadsPendientes;

    private Long reportesUsuariosPendientes;
    private Long reportesHabitacionesPendientes;

    private Long notificacionesNoLeidas;

    public Long getTotalUsuarios() {
        return totalUsuarios;
    }

    public void setTotalUsuarios(Long totalUsuarios) {
        this.totalUsuarios = totalUsuarios;
    }

    public Long getUsuariosActivos() {
        return usuariosActivos;
    }

    public void setUsuariosActivos(Long usuariosActivos) {
        this.usuariosActivos = usuariosActivos;
    }

    public Long getUsuariosSuspendidos() {
        return usuariosSuspendidos;
    }

    public void setUsuariosSuspendidos(Long usuariosSuspendidos) {
        this.usuariosSuspendidos = usuariosSuspendidos;
    }

    public Long getTotalPropietarios() {
        return totalPropietarios;
    }

    public void setTotalPropietarios(Long totalPropietarios) {
        this.totalPropietarios = totalPropietarios;
    }

    public Long getTotalPerfilesConvivencia() {
        return totalPerfilesConvivencia;
    }

    public void setTotalPerfilesConvivencia(Long totalPerfilesConvivencia) {
        this.totalPerfilesConvivencia = totalPerfilesConvivencia;
    }

    public Long getTotalHabitaciones() {
        return totalHabitaciones;
    }

    public void setTotalHabitaciones(Long totalHabitaciones) {
        this.totalHabitaciones = totalHabitaciones;
    }

    public Long getHabitacionesActivas() {
        return habitacionesActivas;
    }

    public void setHabitacionesActivas(Long habitacionesActivas) {
        this.habitacionesActivas = habitacionesActivas;
    }

    public Long getHabitacionesPausadas() {
        return habitacionesPausadas;
    }

    public void setHabitacionesPausadas(Long habitacionesPausadas) {
        this.habitacionesPausadas = habitacionesPausadas;
    }

    public Long getTotalPublicacionesRoomie() {
        return totalPublicacionesRoomie;
    }

    public void setTotalPublicacionesRoomie(Long totalPublicacionesRoomie) {
        this.totalPublicacionesRoomie = totalPublicacionesRoomie;
    }

    public Long getPublicacionesActivas() {
        return publicacionesActivas;
    }

    public void setPublicacionesActivas(Long publicacionesActivas) {
        this.publicacionesActivas = publicacionesActivas;
    }

    public Long getTotalMatches() {
        return totalMatches;
    }

    public void setTotalMatches(Long totalMatches) {
        this.totalMatches = totalMatches;
    }

    public Long getSolicitudesPendientes() {
        return solicitudesPendientes;
    }

    public void setSolicitudesPendientes(Long solicitudesPendientes) {
        this.solicitudesPendientes = solicitudesPendientes;
    }

    public Long getLeadsPendientes() {
        return leadsPendientes;
    }

    public void setLeadsPendientes(Long leadsPendientes) {
        this.leadsPendientes = leadsPendientes;
    }

    public Long getReportesUsuariosPendientes() {
        return reportesUsuariosPendientes;
    }

    public void setReportesUsuariosPendientes(Long reportesUsuariosPendientes) {
        this.reportesUsuariosPendientes = reportesUsuariosPendientes;
    }

    public Long getReportesHabitacionesPendientes() {
        return reportesHabitacionesPendientes;
    }

    public void setReportesHabitacionesPendientes(Long reportesHabitacionesPendientes) {
        this.reportesHabitacionesPendientes = reportesHabitacionesPendientes;
    }

    public Long getNotificacionesNoLeidas() {
        return notificacionesNoLeidas;
    }

    public void setNotificacionesNoLeidas(Long notificacionesNoLeidas) {
        this.notificacionesNoLeidas = notificacionesNoLeidas;
    }
}