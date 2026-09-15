package com.roommatch.matching;

import java.math.BigDecimal;
import java.time.LocalDate;

/** Only preferences enter scoring; identity, age and education do not. */
public record CompatibilityProfile(String distrito, BigDecimal presupuestoMin, BigDecimal presupuestoMax,
        LocalDate fechaMudanza, Integer limpieza, Integer ruido, Integer sociabilidad, String horario,
        String visitas, String mascotas, String fumar, String alcohol, String gastos, String convivencia) { }
