package com.roommatch.matching;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import org.junit.jupiter.api.Test;
import static org.assertj.core.api.Assertions.*;

class CompatibilityCalculatorTest {
    final CompatibilityCalculator calculator = new CompatibilityCalculator();
    static final CompatibilityProfile COMPLETE = profile("Lima", new BigDecimal("500"), new BigDecimal("900"), LocalDate.of(2028, 2, 29), 4, "no", "divididos");

    @Test
    void identityCoversAllThirteenCriteriaAndWeightsSumToHundred() {
        var result = calculator.calculate(COMPLETE, COMPLETE);
        assertThat(result.porcentaje()).isEqualByComparingTo("100.00");
        assertThat(result.cobertura()).isEqualTo(100);
        assertThat(result.criterios()).hasSize(13);
        assertThat(result.criterios().stream().mapToInt(CompatibilityCalculator.Criterion::peso).sum()).isEqualTo(100);
    }

    @Test
    void allTwentyFiveOrdinalPairsAreSymmetricBoundedAndPreserveDistance() {
        for (int a = 1; a <= 5; a++) for (int b = 1; b <= 5; b++) {
            assertThat(CompatibilityCalculator.ordinal(a, b)).isEqualTo(1 - Math.abs(a - b) / 4.0)
                    .isEqualTo(CompatibilityCalculator.ordinal(b, a)).isBetween(0.0, 1.0);
        }
        assertThat(CompatibilityCalculator.ordinal(4, 5)).isGreaterThan(CompatibilityCalculator.ordinal(1, 5));
        assertThat(CompatibilityCalculator.ordinal(null, 5)).isNull();
        assertThat(CompatibilityCalculator.ordinal(0, 5)).isNull();
    }

    @Test
    void everySmallMoneyIntervalPairMatchesAnIndependentSetOracle() {
        for (int a = 0; a <= 10; a++) for (int b = a; b <= 10; b++)
            for (int c = 0; c <= 10; c++) for (int d = c; d <= 10; d++) {
                Set<Integer> left = IntStream.rangeClosed(a, b).boxed().collect(Collectors.toSet());
                Set<Integer> right = IntStream.rangeClosed(c, d).boxed().collect(Collectors.toSet());
                long intersection = left.stream().filter(right::contains).count();
                var union = new java.util.HashSet<>(left); union.addAll(right);
                Double actual = CompatibilityCalculator.budget(money(a), money(b), money(c), money(d));
                assertThat(actual).isCloseTo((double) intersection / union.size(), within(1e-12))
                        .isEqualTo(CompatibilityCalculator.budget(money(c), money(d), money(a), money(b)));
            }
    }

    @Test
    void budgetLimitsDisjointTouchingFixedMissingAndInvalidValues() {
        assertThat(CompatibilityCalculator.budget(money(5), money(5), money(5), money(5))).isEqualTo(1);
        assertThat(CompatibilityCalculator.budget(money(0), money(5), money(6), money(8))).isZero();
        assertThat(CompatibilityCalculator.budget(money(0), money(5), money(5), money(10))).isCloseTo(1.0 / 11, within(1e-12));
        assertThat(CompatibilityCalculator.budget(null, money(5), money(5), money(5))).isNull();
        assertThat(CompatibilityCalculator.budget(money(-1), money(5), money(5), money(5))).isNull();
        assertThat(CompatibilityCalculator.budget(money(6), money(5), money(5), money(5))).isNull();
        assertThat(CompatibilityCalculator.budget(new BigDecimal("0.001"), money(5), money(5), money(5))).isNull();
        assertThat(CompatibilityCalculator.budget(BigDecimal.ZERO, new BigDecimal("1E30"), money(5), money(5))).isNull();
    }

    @Test
    void moveDateUsesRealDaysIncludingLeapDayAndClampsAtSixty() {
        LocalDate leap = LocalDate.of(2028, 2, 29);
        assertThat(CompatibilityCalculator.movingDate(leap, leap.plusDays(30))).isEqualTo(.5);
        assertThat(CompatibilityCalculator.movingDate(leap, leap.minusDays(30))).isEqualTo(.5);
        assertThat(CompatibilityCalculator.movingDate(leap, leap.plusDays(60))).isZero();
        assertThat(CompatibilityCalculator.movingDate(leap, leap.plusDays(100))).isZero();
        assertThat(CompatibilityCalculator.movingDate(null, leap)).isNull();
    }

    @Test
    void categoriesHaveExplicitPartialRulesAndUnknownValuesNeverMatch() {
        assertThat(CompatibilityCalculator.visits("bajas", "moderadas")).isEqualTo(.5);
        assertThat(CompatibilityCalculator.visits("bajas", "frecuentes")).isZero();
        assertThat(CompatibilityCalculator.schedule("MAÑANA", "manana")).isEqualTo(1);
        assertThat(CompatibilityCalculator.schedule("variable", "noche")).isEqualTo(.5);
        assertThat(CompatibilityCalculator.schedule("mañana", "noche")).isZero();
        for (String a : Set.of("tranquila", "social", "independiente", "mixta"))
            for (String b : Set.of("tranquila", "social", "independiente", "mixta"))
                assertThat(CompatibilityCalculator.living(a, b)).isEqualTo(CompatibilityCalculator.living(b, a));
        assertThat(CompatibilityCalculator.living("tranquila", "independiente")).isEqualTo(.5);
        assertThat(CompatibilityCalculator.living("social", "tranquila")).isZero();
        assertThat(CompatibilityCalculator.living("unknown", "unknown")).isNull();
        assertThat(CompatibilityCalculator.schedule("", "")).isNull();
    }

    @Test
    void alcoholExpensesAndDateChangeTheScoreByTheirDocumentedWeights() {
        var other = profile("Lima", new BigDecimal("500"), new BigDecimal("900"), COMPLETE.fechaMudanza().plusDays(60), 4, "si", "proporcional");
        var result = calculator.calculate(COMPLETE, other);
        assertThat(result.porcentaje()).isEqualByComparingTo("86.00");
        assertThat(result).isEqualTo(calculator.calculate(other, COMPLETE));
    }

    @Test
    void missingDataIsExcludedAndInsufficientCoverageDoesNotInventPercentage() {
        var noDate = profile(" Lima ", new BigDecimal("500"), new BigDecimal("900"), null, 4, "no", "divididos");
        var result = calculator.calculate(COMPLETE, noDate);
        assertThat(result.cobertura()).isEqualTo(92);
        assertThat(result.porcentaje()).isEqualByComparingTo("100.00");
        var almostEmpty = new CompatibilityProfile("Lima", null, null, null, null, null, null, null, null, null, null, null, null, null);
        var sparse = calculator.calculate(COMPLETE, almostEmpty);
        assertThat(sparse.cobertura()).isEqualTo(14);
        assertThat(sparse.porcentaje()).isNull();
        assertThat(sparse.hayImporteComun()).isNull();
    }

    static BigDecimal money(int cents) { return BigDecimal.valueOf(cents, 2); }
    static CompatibilityProfile profile(String district, BigDecimal min, BigDecimal max, LocalDate date, int clean, String alcohol, String expenses) {
        return new CompatibilityProfile(district, min, max, date, clean, 3, 3, "mañana", "moderadas", "no", "no", alcohol, expenses, "tranquila");
    }
}
