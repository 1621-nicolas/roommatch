package com.roommatch.dto;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.math.BigDecimal;
import java.time.LocalDate;

public class PerfilConvivenciaRequest {

    @NotNull(message = "El presupuesto mínimo es obligatorio")
    @DecimalMin(value = "0.0", message = "El presupuesto mínimo no puede ser negativo")
    private BigDecimal presupuestoMin;

    @NotNull(message = "El presupuesto máximo es obligatorio")
    @DecimalMin(value = "0.0", message = "El presupuesto máximo no puede ser negativo")
    private BigDecimal presupuestoMax;

    @NotBlank(message = "El distrito preferido es obligatorio")
    @Size(max = 100, message = "El distrito no puede superar 100 caracteres")
    private String distritoPreferido;

    private LocalDate fechaMudanza;

    @NotNull(message = "El nivel de limpieza es obligatorio")
    @Min(value = 1, message = "La limpieza debe ser mínimo 1")
    @Max(value = 5, message = "La limpieza debe ser máximo 5")
    private Integer limpieza;

    @NotNull(message = "El nivel de ruido es obligatorio")
    @Min(value = 1, message = "El ruido debe ser mínimo 1")
    @Max(value = 5, message = "El ruido debe ser máximo 5")
    private Integer ruido;

    @NotNull(message = "El nivel de sociabilidad es obligatorio")
    @Min(value = 1, message = "La sociabilidad debe ser mínimo 1")
    @Max(value = 5, message = "La sociabilidad debe ser máximo 5")
    private Integer sociabilidad;

    @NotBlank(message = "El horario es obligatorio")
    @Size(max = 50, message = "El horario no puede superar 50 caracteres")
    private String horario;

    @NotBlank(message = "Las visitas son obligatorias")
    @Size(max = 50, message = "La preferencia de visitas no puede superar 50 caracteres")
    private String visitas;

    @NotBlank(message = "La preferencia sobre mascotas es obligatoria")
    @Size(max = 50, message = "La preferencia de mascotas no puede superar 50 caracteres")
    private String mascotas;

    @NotBlank(message = "La preferencia sobre fumar es obligatoria")
    @Size(max = 50, message = "La preferencia sobre fumar no puede superar 50 caracteres")
    private String fumar;

    @NotBlank(message = "La preferencia sobre alcohol es obligatoria")
    @Size(max = 50, message = "La preferencia sobre alcohol no puede superar 50 caracteres")
    private String alcohol;

    @NotBlank(message = "La preferencia sobre gastos es obligatoria")
    @Size(max = 50, message = "La preferencia sobre gastos no puede superar 50 caracteres")
    private String gastos;

    @NotBlank(message = "El tipo de convivencia es obligatorio")
    @Size(max = 50, message = "La convivencia no puede superar 50 caracteres")
    private String convivencia;

    @Size(max = 500, message = "La descripción no puede superar 500 caracteres")
    private String descripcionPersonal;

    public BigDecimal getPresupuestoMin() { return presupuestoMin; }
    public void setPresupuestoMin(BigDecimal presupuestoMin) { this.presupuestoMin = presupuestoMin; }
    public BigDecimal getPresupuestoMax() { return presupuestoMax; }
    public void setPresupuestoMax(BigDecimal presupuestoMax) { this.presupuestoMax = presupuestoMax; }
    public String getDistritoPreferido() { return distritoPreferido; }
    public void setDistritoPreferido(String distritoPreferido) { this.distritoPreferido = distritoPreferido; }
    public LocalDate getFechaMudanza() { return fechaMudanza; }
    public void setFechaMudanza(LocalDate fechaMudanza) { this.fechaMudanza = fechaMudanza; }
    public Integer getLimpieza() { return limpieza; }
    public void setLimpieza(Integer limpieza) { this.limpieza = limpieza; }
    public Integer getRuido() { return ruido; }
    public void setRuido(Integer ruido) { this.ruido = ruido; }
    public Integer getSociabilidad() { return sociabilidad; }
    public void setSociabilidad(Integer sociabilidad) { this.sociabilidad = sociabilidad; }
    public String getHorario() { return horario; }
    public void setHorario(String horario) { this.horario = horario; }
    public String getVisitas() { return visitas; }
    public void setVisitas(String visitas) { this.visitas = visitas; }
    public String getMascotas() { return mascotas; }
    public void setMascotas(String mascotas) { this.mascotas = mascotas; }
    public String getFumar() { return fumar; }
    public void setFumar(String fumar) { this.fumar = fumar; }
    public String getAlcohol() { return alcohol; }
    public void setAlcohol(String alcohol) { this.alcohol = alcohol; }
    public String getGastos() { return gastos; }
    public void setGastos(String gastos) { this.gastos = gastos; }
    public String getConvivencia() { return convivencia; }
    public void setConvivencia(String convivencia) { this.convivencia = convivencia; }
    public String getDescripcionPersonal() { return descripcionPersonal; }
    public void setDescripcionPersonal(String descripcionPersonal) { this.descripcionPersonal = descripcionPersonal; }
}
