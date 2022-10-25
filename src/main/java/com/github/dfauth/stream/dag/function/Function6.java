package com.github.dfauth.stream.dag.function;

import java.util.function.Function;

import static com.github.dfauth.stream.dag.function.Function5.function5;

@FunctionalInterface
public interface Function6<A, B, C, D, E, F, G> {

    static <A, B, C, D, E, F,G> Function6<A, B, C, D, E, F, G> function6(Function<A, Function<B, Function<C, Function<D, Function<E, Function<F,G>>>>>> _f) {
        return (a, b, c, d, e, f) -> function5(_f.apply(a)).apply(b, c, d, e, f);
    }

    static <A, B, C, D, E, F, G> Function<A, Function<B, Function<C, Function<D, Function<E, Function<F,G>>>>>> unwind(Function6<A,B,C,D,E,F,G> f) {
        return f.unwind();
    }

    G apply(A a, B b, C c, D d, E e, F f);

    default Function<A, Function5<B, C, D, E, F, G>> curry() {
        return a -> (b, c, d, e, f) -> apply(a, b, c, d, e, f);
    }

    default Function<A, Function<B, Function<C, Function<D, Function<E, Function<F, G>>>>>> unwind() {
        return a -> curry().apply(a).unwind();
    }
}
