package com.github.dfauth.stream.dag.function;

import java.util.function.Function;

@FunctionalInterface
public interface Function6<A, B, C, D, E, F, G> {

    G apply(A a, B b, C c, D d, E e, F f);

    default Function<A, Function5<B, C, D, E, F, G>> curry() {
        return a -> (b, c, d, e, f) -> apply(a, b, c, d, e, f);
    }

    default Function<A, Function<B, Function<C, Function<D, Function<E, Function<F, G>>>>>> unwind() {
        return a -> curry().apply(a).unwind();
    }
}
