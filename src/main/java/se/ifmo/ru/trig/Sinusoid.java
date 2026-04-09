package se.ifmo.ru.trig;

import se.ifmo.ru.common.MathFunction;

import java.math.BigDecimal;
import java.math.MathContext;

public class Sinusoid implements MathFunction {

    private static final MathContext MC = MathContext.DECIMAL128;

    @Override
    public BigDecimal calculate(BigDecimal x, BigDecimal epsilon) {

        if (x == null || epsilon == null) {
            throw new IllegalArgumentException("Arguments must not be null");
        }

        if (epsilon.compareTo(BigDecimal.ZERO) <= 0) {
            throw new IllegalArgumentException("Epsilon must be positive");
        }

        BigDecimal term = x;
        BigDecimal sum = term;
        int k = 1;

        while (term.abs().compareTo(epsilon) >= 0) {

            BigDecimal numerator = term.multiply(x).multiply(x).negate();

            BigDecimal denominator = BigDecimal.valueOf(2L * k)
                    .multiply(BigDecimal.valueOf(2L * k + 1L));

            term = numerator.divide(denominator, MC);

            sum = sum.add(term, MC);

            k++;

            if (k > 1_000_000) {
                throw new IllegalStateException("Series did not converge");
            }
        }

        return sum;
    }
}
