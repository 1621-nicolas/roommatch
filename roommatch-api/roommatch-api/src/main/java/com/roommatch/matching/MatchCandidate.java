package com.roommatch.matching;

import java.math.BigDecimal;
import java.time.LocalDate;

/** Flat projection: avoids managed entities and per-user relation queries during ranking. */
public record MatchCandidate(Integer idUsuario, String nombres, String apellidos, Integer edad, String ocupacion,
        String universidad, String foto, String distrito, BigDecimal presupuestoMin, BigDecimal presupuestoMax,
        LocalDate fechaMudanza, Integer limpieza, Integer ruido, Integer sociabilidad, String horario,
        String visitas, String mascotas, String fumar, String alcohol, String gastos, String convivencia) {
    public CompatibilityProfile profile() {
        return new CompatibilityProfile(distrito, presupuestoMin, presupuestoMax, fechaMudanza, limpieza, ruido,
                sociabilidad, horario, visitas, mascotas, fumar, alcohol, gastos, convivencia);
    }
}
