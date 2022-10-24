package com.github.dfauth.stream.dag.function;

import java.util.function.Function;

@FunctionalInterface
public interface Function5<A, B, C, D, E, F> {

    F apply(A a, B b, C c, D d, E e);

    default Function<A, Function4<B, C, D, E, F>> curry() {
        return a -> (b, c, d, e) -> apply(a, b, c, d, e);
    }

    default Function<A, Function<B, Function<C, Function<D, Function<E, F>>>>> unwind() {
        return a -> curry().apply(a).unwind();
    }
}
