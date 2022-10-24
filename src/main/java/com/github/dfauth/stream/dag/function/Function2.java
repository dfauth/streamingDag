package com.github.dfauth.stream.dag.function;

import java.util.function.BiFunction;
import java.util.function.Function;

@FunctionalInterface
public interface Function2<A, B, C> extends BiFunction<A, B, C> {

    static <A, B, C> Function2<A, B, C> function2(BiFunction<A, B, C> f) {
        return f::apply;
    }

    static <A, B, C> Function2<A, B, C> function2(Function<A, Function<B, C>> f) {
        return (a, b) -> f.apply(a).apply(b);
    }

    default Function<A, Function<B, C>> curry() {
        return a -> b -> apply(a, b);
    }

    default Function<A, Function<B, C>> unwind() {
        return curry();
    }
}
