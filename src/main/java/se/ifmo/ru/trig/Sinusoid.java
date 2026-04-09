package se.ifmo.ru.trig;

public class Sinusoid {
    public double calculate(double x, double epsilon) {
        if (epsilon <= 0.0) {
            throw new IllegalArgumentException("Epsilon must be positive");
        }

        double term = x;
        double sum = term;
        int k = 1;

        while (Math.abs(term) >= epsilon) {
            term *= -x * x / ((2.0 * k) * (2.0 * k + 1.0));
            sum += term;
            k++;

            if (k > 1_000_000) {
                throw new IllegalStateException("Series did not converge");
            }
        }

        return sum;
    }
}
