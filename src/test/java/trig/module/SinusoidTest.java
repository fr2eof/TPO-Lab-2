package trig.module;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.MethodSource;
import se.ifmo.ru.trig.Sinusoid;

import java.math.BigDecimal;
import java.util.stream.Stream;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.assertj.core.api.Assertions.within;

public class SinusoidTest {

    private static final BigDecimal EPSILON = new BigDecimal("1e-10");
    private static final BigDecimal LARGE_X = new BigDecimal("1000000");

    private final Sinusoid sin = new Sinusoid();

    @Nested
    @DisplayName("Валидация входных параметров")
    class ValidationTests {

        @Test
        @DisplayName("Должен выбрасывать исключение при epsilon <= 0")
        void shouldThrowException_whenEpsilonIsNonPositive() {
            BigDecimal x = BigDecimal.ONE;

            assertThatThrownBy(() -> sin.calculate(x, BigDecimal.ZERO))
                    .isInstanceOf(IllegalArgumentException.class);

            assertThatThrownBy(() -> sin.calculate(x, new BigDecimal("-1e-3")))
                    .isInstanceOf(IllegalArgumentException.class);
        }

        @Test
        @DisplayName("Должен выбрасывать исключение при null")
        void shouldThrowException_whenNull() {
            assertThatThrownBy(() -> sin.calculate(null, EPSILON))
                    .isInstanceOf(IllegalArgumentException.class);

            assertThatThrownBy(() -> sin.calculate(BigDecimal.ONE, null))
                    .isInstanceOf(IllegalArgumentException.class);
        }
    }

    @Nested
    @DisplayName("Корректность вычисления sin(x)")
    class SinCalculationTests {

        static Stream<Arguments> provideAngles() {
            double sqrt2over2 = Math.sqrt(2) / 2;

            return Stream.of(
                    Arguments.of(0.0, 0.0),
                    Arguments.of(Math.PI / 2, 1.0),
                    Arguments.of(-Math.PI / 2, -1.0),
                    Arguments.of(Math.PI, 0.0),
                    Arguments.of(-Math.PI, 0.0),
                    Arguments.of(Math.PI / 4, sqrt2over2),
                    Arguments.of(-Math.PI / 4, -sqrt2over2)
            );
        }

        @Test
        @DisplayName("sin(0) должен быть равен 0")
        void shouldReturnZero_whenXIsZero() {
            BigDecimal actual = sin.calculate(BigDecimal.ZERO, EPSILON);

            assertThat(actual.doubleValue()).isCloseTo(0.0, within(1e-10));
        }

        @ParameterizedTest(name = "sin({0}) ~~ {1}")
        @MethodSource("provideAngles")
        @DisplayName("Должен корректно считать значения в характерных точках")
        void shouldReturnCorrectValues(double x, double expected) {
            BigDecimal actual = sin.calculate(BigDecimal.valueOf(x), EPSILON);

            assertThat(actual.doubleValue())
                    .isCloseTo(expected, within(1e-8));
        }

        @ParameterizedTest
        @CsvSource({"0.1", "0.5", "1.0", "2.0"})
        @DisplayName("sin(x) должен быть нечётной функцией")
        void shouldBeOddFunction(double x) {
            BigDecimal positive = sin.calculate(BigDecimal.valueOf(x), EPSILON);
            BigDecimal negative = sin.calculate(BigDecimal.valueOf(-x), EPSILON);

            assertThat(positive.doubleValue())
                    .isCloseTo(-negative.doubleValue(), within(1e-9));
        }
    }

    @Nested
    @DisplayName("Сходимость и точность")
    class ConvergenceTests {

        @Test
        @DisplayName("Должен выбрасывать исключение если ряд не сходится")
        void shouldThrowException_whenSeriesDoesNotConverge() {
            assertThatThrownBy(() -> sin.calculate(LARGE_X, new BigDecimal("1e-20")))
                    .isInstanceOf(IllegalStateException.class);
        }

        @ParameterizedTest
        @CsvSource({"0.1", "1.0", "3.0", "10.0"})
        @DisplayName("Ряд должен сходиться к значению Math.sin(x)")
        void shouldConvergeToMathSin(double x) {
            BigDecimal actual = sin.calculate(BigDecimal.valueOf(x), EPSILON);
            double expected = Math.sin(x);

            assertThat(actual.doubleValue()).isCloseTo(expected, within(1e-8));
        }

        static Stream<Arguments> provideAccuracyCases() {
            return Stream.of(
                    Arguments.of(1.0, 1e-3),
                    Arguments.of(1.0, 1e-6),
                    Arguments.of(1.0, 1e-10)
            );
        }

        @ParameterizedTest
        @MethodSource("provideAccuracyCases")
        @DisplayName("Точность должна улучшаться при уменьшении epsilon")
        void shouldImproveAccuracy_whenEpsilonDecreases(double x, double epsilon) {
            BigDecimal actual = sin.calculate(BigDecimal.valueOf(x), new BigDecimal(epsilon));
            double expected = Math.sin(x);

            assertThat(actual.doubleValue()).isCloseTo(expected, within(epsilon * 10));
        }
    }
}
