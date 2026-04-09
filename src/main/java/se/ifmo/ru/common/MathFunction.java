package se.ifmo.ru.common;

import java.math.BigDecimal;

public interface MathFunction {
    BigDecimal calculate(BigDecimal x, BigDecimal eps);
}
