package com.roommatch.matching;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.text.Normalizer;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.*;

/** Deterministic preference index, not an empirically calibrated probability. */
public final class CompatibilityCalculator {
    public static final String VERSION = "preferences-v2";
    public static final int MINIMUM_COVERAGE = 70;
    private static final Set<String> YES_NO = Set.of("si", "no");
    private static final Set<String> SCHEDULES = Set.of("manana", "tarde", "noche", "variable");
    private static final List<String> VISITS = List.of("bajas", "moderadas", "frecuentes");
    private static final Set<String> EXPENSES = Set.of("divididos", "proporcional");
    private static final Set<String> LIVING = Set.of("tranquila", "social", "independiente", "mixta");

    public record Criterion(String id, String nombre, int peso, BigDecimal puntuacion) { }
    public record Result(BigDecimal porcentaje, int cobertura, Boolean hayImporteComun, List<Criterion> criterios) {
        public String coincidencias() {
            return String.join(", ", criterios.stream().filter(c -> c.puntuacion != null && c.puntuacion.doubleValue() >= .75)
                    .map(Criterion::nombre).toList());
        }
        public String diferencias() {
            return String.join(", ", criterios.stream().filter(c -> c.puntuacion == null || c.puntuacion.doubleValue() < .75)
                    .map(c -> c.nombre + (c.puntuacion == null ? " (sin datos)" : c.puntuacion.signum() > 0 ? " (parcial)" : " (diferente)")).toList());
        }
    }

    public Result calculate(CompatibilityProfile a, CompatibilityProfile b) {
        Objects.requireNonNull(a); Objects.requireNonNull(b);
        Double budget = budget(a.presupuestoMin(), a.presupuestoMax(), b.presupuestoMin(), b.presupuestoMax());
        List<Criterion> criteria = List.of(
            criterion("distrito", "Distrito", 14, text(a.distrito(), b.distrito())),
            criterion("presupuesto", "Presupuesto", 18, budget),
            criterion("fechaMudanza", "Fecha de mudanza", 8, movingDate(a.fechaMudanza(), b.fechaMudanza())),
            criterion("limpieza", "Limpieza", 12, ordinal(a.limpieza(), b.limpieza())),
            criterion("ruido", "Ruido", 10, ordinal(a.ruido(), b.ruido())),
            criterion("sociabilidad", "Sociabilidad", 6, ordinal(a.sociabilidad(), b.sociabilidad())),
            criterion("horario", "Horario", 7, schedule(a.horario(), b.horario())),
            criterion("visitas", "Visitas", 6, visits(a.visitas(), b.visitas())),
            criterion("mascotas", "Mascotas", 5, category(a.mascotas(), b.mascotas(), YES_NO)),
            criterion("fumar", "Fumar", 6, category(a.fumar(), b.fumar(), YES_NO)),
            criterion("alcohol", "Alcohol", 2, category(a.alcohol(), b.alcohol(), YES_NO)),
            criterion("gastos", "Reparto de gastos", 4, category(a.gastos(), b.gastos(), EXPENSES)),
            criterion("convivencia", "Convivencia", 2, living(a.convivencia(), b.convivencia()))
        );
        int coverage = criteria.stream().filter(c -> c.puntuacion != null).mapToInt(Criterion::peso).sum();
        BigDecimal points = criteria.stream().filter(c -> c.puntuacion != null)
                .map(c -> c.puntuacion.multiply(BigDecimal.valueOf(c.peso))).reduce(BigDecimal.ZERO, BigDecimal::add);
        BigDecimal score = coverage < MINIMUM_COVERAGE ? null
                : points.multiply(BigDecimal.valueOf(100)).divide(BigDecimal.valueOf(coverage), 2, RoundingMode.HALF_UP);
        return new Result(score, coverage, budget == null ? null : budget > 0, criteria);
    }

    private static Criterion criterion(String id, String name, int weight, Double score) {
        return new Criterion(id, name, weight, score == null ? null : BigDecimal.valueOf(score).setScale(12, RoundingMode.HALF_UP));
    }

    static String normalized(String value) {
        if (value == null || value.isBlank()) return null;
        return Normalizer.normalize(value.trim().toLowerCase(Locale.ROOT), Normalizer.Form.NFD)
                .replaceAll("\\p{M}", "").replaceAll("\\s+", " ");
    }

    private static Double text(String a, String b) {
        a = normalized(a); b = normalized(b);
        return a == null || b == null ? null : a.equals(b) ? 1.0 : 0.0;
    }

    private static Double category(String a, String b, Set<String> values) {
        a = normalized(a); b = normalized(b);
        if (a == null || b == null || !values.contains(a) || !values.contains(b)) return null;
        return a.equals(b) ? 1.0 : 0.0;
    }

    static Double ordinal(Integer a, Integer b) {
        return a == null || b == null || a < 1 || a > 5 || b < 1 || b > 5 ? null : 1 - Math.abs(a - b) / 4.0;
    }

    static Double visits(String a, String b) {
        a = normalized(a); b = normalized(b);
        if (a == null || b == null) return null;
        int x = VISITS.indexOf(a), y = VISITS.indexOf(b);
        return x < 0 || y < 0 ? null : 1 - Math.abs(x - y) / 2.0;
    }

    static Double schedule(String a, String b) {
        Double same = category(a, b, SCHEDULES);
        if (same == null || same == 1) return same;
        return "variable".equals(normalized(a)) || "variable".equals(normalized(b)) ? .5 : 0;
    }

    static Double living(String a, String b) {
        Double same = category(a, b, LIVING);
        if (same == null || same == 1) return same;
        a = normalized(a); b = normalized(b);
        return a.equals("mixta") || b.equals("mixta") || Set.of(a, b).equals(Set.of("tranquila", "independiente")) ? .5 : 0;
    }

    static Double movingDate(LocalDate a, LocalDate b) {
        return a == null || b == null ? null : Math.max(0, 1 - Math.abs(ChronoUnit.DAYS.between(a, b)) / 60.0);
    }

    /** Inclusive integer-cent Jaccard; a single common cent is feasible but not full similarity. */
    static Double budget(BigDecimal minA, BigDecimal maxA, BigDecimal minB, BigDecimal maxB) {
        try {
            if (minA == null || maxA == null || minB == null || maxB == null) return null;
            long a = cents(minA), b = cents(maxA), c = cents(minB), d = cents(maxB);
            if (a < 0 || c < 0 || b < a || d < c || b > 9_999_999_999L || d > 9_999_999_999L) return null;
            long intersection = Math.max(0, Math.min(b, d) - Math.max(a, c) + 1);
            long union = (b - a + 1) + (d - c + 1) - intersection;
            return (double) intersection / union;
        } catch (ArithmeticException invalidPrecisionOrRange) { return null; }
    }

    private static long cents(BigDecimal value) { return value.movePointRight(2).longValueExact(); }
}
