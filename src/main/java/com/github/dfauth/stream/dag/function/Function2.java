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

    static <A,B,C> Function2<A,B,C> uncurry(Function<A,Function<B,C>> f) {
        return function2(f);
    }

    static <A,B,C> Function2<B,A,C> flip(Function2<A,B,C> f) {
        return (b,a) -> f.apply(a,b);
    }

    default Function<A, Function<B, C>> curry() {
        return a -> b -> apply(a, b);
    }

    default Function<A, Function<B, C>> unwind() {
        return curry();
    }

    default Function2<B,A,C> flip() {
        return flip(this);
    }
}
