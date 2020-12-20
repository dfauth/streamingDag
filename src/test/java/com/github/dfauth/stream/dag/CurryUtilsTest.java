package com.github.dfauth.stream.dag;

import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.reflect.Modifier;
import java.math.BigDecimal;
import java.util.function.BiFunction;
import java.util.function.Function;
import java.util.stream.Stream;

import static com.github.dfauth.stream.dag.CurryUtils.*;
import static org.junit.Assert.*;

public class CurryUtilsTest {

    private static final Logger logger = LoggerFactory.getLogger(CurryUtilsTest.class);

    private static BiFunction<Integer, Double, BigDecimal> sum = (a,b) -> {
        BigDecimal result = BigDecimal.valueOf(a).add(BigDecimal.valueOf(b));
        try {
            throw new RuntimeException("Oops");
        } catch (RuntimeException e) {
            logger.error(e.getMessage(), e);
        }
        Stream.of(CurryUtilsTest.class.getDeclaredMethods())
                .filter(m -> Modifier.isPrivate(m.getModifiers()))
                .filter(m -> Modifier.isStatic(m.getModifiers()))
                .filter(m -> m.getReturnType() == BigDecimal.class)
                .forEach(m -> logger.info("method: "+m));
        return result;
    };

    @Test
    public void testCurry() {

        Function<Integer, Function<Double, BigDecimal>> curriedSum = curry(sum);
        BiFunction<Integer, Double, BigDecimal> uncurriedCurriedSum = uncurry(curriedSum);

        assertEquals(BigDecimal.valueOf(3.0), sum.apply(1, 2.0));
        assertEquals(curriedSum.apply(1).apply(2.0), sum.apply(1, 2.0));
        assertEquals(uncurriedCurriedSum.apply(1, 2.0), sum.apply(1, 2.0));
    }

    @Test
    public void testIsLambda() {

        assertTrue(isLambda(sum.getClass()));
        assertFalse(isLambda(SumFunction.class));
        assertFalse(isLambda(new BiFunction<Integer, Double, BigDecimal>(){
            @Override
            public BigDecimal apply(Integer a, Double b) {
                return BigDecimal.valueOf(a).add(BigDecimal.valueOf(b));
            }
        }.getClass()));

    }

    private static class SumFunction implements BiFunction<Integer, Double, BigDecimal> {

        @Override
        public BigDecimal apply(Integer a, Double b) {
            return BigDecimal.valueOf(a).add(BigDecimal.valueOf(b));
        }
    }
}
