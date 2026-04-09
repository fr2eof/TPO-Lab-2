package trig.module;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.MethodSource;
import se.ifmo.ru.trig.Sinusoid;

import java.util.stream.Stream;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.assertj.core.api.Assertions.within;

public class SinusoidTest {

    private static final double EPSILON = Math.pow(10, -10);
    private static final double LARGE_X = 1_000_000.0;

    private final Sinusoid sin = new Sinusoid();

    @Nested
    @DisplayName("Валидация входных параметров")
    class ValidationTests {

        @Test
        @DisplayName("Должен выбрасывать исключение при epsilon <= 0")
        void shouldThrowException_whenEpsilonIsNonPositive() {
            double x = 1.0;

            assertThatThrownBy(() -> sin.calculate(x, 0.0))
                    .isInstanceOf(IllegalArgumentException.class);

            assertThatThrownBy(() -> sin.calculate(x, -1e-3))
                    .isInstanceOf(IllegalArgumentException.class);
        }

        @Test
        @DisplayName("Должен корректно обрабатывать NaN")
        void shouldHandleNaN() {
            double actual = sin.calculate(Double.NaN, EPSILON);

            assertThat(actual).isNaN();
        }

        @Test
        @DisplayName("Должен корректно обрабатывать бесконечность")
        void shouldHandleInfinity() {
            assertThatThrownBy(() -> sin.calculate(Double.POSITIVE_INFINITY, EPSILON))
                    .isInstanceOf(Exception.class);
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
            double actual = sin.calculate(0.0, EPSILON);

            assertThat(actual).isEqualTo(0.0);
        }

        @ParameterizedTest(name = "sin({0}) ~~ {1}")
        @MethodSource("provideAngles")
        @DisplayName("Должен корректно считать значения в характерных точках")
        void shouldReturnCorrectValues(double x, double expected) {
            double actual = sin.calculate(x, EPSILON);

            assertThat(actual)
                    .isCloseTo(expected, within(1e-8));
        }

        @ParameterizedTest
        @CsvSource({
                "0.1",
                "0.5",
                "1.0",
                "2.0"
        })
        @DisplayName("sin(x) должен быть нечётной функцией")
        void shouldBeOddFunction(double x) {
            double positive = sin.calculate(x, EPSILON);
            double negative = sin.calculate(-x, EPSILON);

            assertThat(positive)
                    .isCloseTo(-negative, within(1e-9));
        }
    }

    @Nested
    @DisplayName("Сходимость и точность")
    class ConvergenceTests {

        @Test
        @DisplayName("Должен выбрасывать исключение если ряд не сходится")
        void shouldThrowException_whenSeriesDoesNotConverge() {
            assertThatThrownBy(() ->
                    sin.calculate(LARGE_X, 1e-20)
            ).isInstanceOf(IllegalStateException.class);
        }

        @ParameterizedTest
        @CsvSource({
                "0.1",
                "1.0",
                "3.0",
                "10.0"
        })
        @DisplayName("Ряд должен сходиться к значению Math.sin(x)")
        void shouldConvergeToMathSin(double x) {
            double expected = Math.sin(x);
            double actual = sin.calculate(x, EPSILON);

            assertThat(actual)
                    .isCloseTo(expected, within(1e-8));
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
            double expected = Math.sin(x);
            double actual = sin.calculate(x, epsilon);

            assertThat(actual)
                    .isCloseTo(expected, within(epsilon * 10));
        }
    }
}
