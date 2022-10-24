package com.github.dfauth.stream.dag.function;

import java.util.function.Function;

import static com.github.dfauth.stream.dag.function.Function4.function4;

@FunctionalInterface
public interface Function5<A, B, C, D, E, F> {

    static <A, B, C, D, E, F> Function5<A, B, C, D, E, F> function5(Function<A, Function<B, Function<C, Function<D, Function<E,F>>>>> f) {
        return (a, b, c, d, e) -> function4(f.apply(a)).apply(b, c, d, e);
    }

    static <A,B,C,D,E,F> Function<A, Function<B, Function<C, Function<D, Function<E, F>>>>> unwind(Function5<A,B,C,D,E,F> f) {
        return f.unwind();
    }

    F apply(A a, B b, C c, D d, E e);

    default Function<A, Function4<B, C, D, E, F>> curry() {
        return a -> (b, c, d, e) -> apply(a, b, c, d, e);
    }

    default Function<A, Function<B, Function<C, Function<D, Function<E, F>>>>> unwind() {
        return a -> curry().apply(a).unwind();
    }
}
