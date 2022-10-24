package com.github.dfauth.stream.dag.function;

import java.util.function.Function;

import static com.github.dfauth.stream.dag.function.Function3.function3;

@FunctionalInterface
public interface Function4<A, B, C, D, E> {

    static <A, B, C, D, E> Function4<A, B, C, D, E> function4(Function<A, Function<B, Function<C, Function<D, E>>>> f) {
        return (a, b, c, d) -> function3(f.apply(a)).apply(b, c, d);
    }

    E apply(A a, B b, C c, D d);

    default Function<A, Function3<B, C, D, E>> curry() {
        return a -> (b, c, d) -> apply(a, b, c, d);
    }

    default Function<A, Function<B, Function<C, Function<D, E>>>> unwind() {
        return a -> curry().apply(a).unwind();
    }
}
