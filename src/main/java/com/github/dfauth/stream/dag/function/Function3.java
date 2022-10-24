package com.github.dfauth.stream.dag.function;

import java.util.function.Function;

import static com.github.dfauth.stream.dag.function.Function2.function2;

@FunctionalInterface
public interface Function3<A, B, C, D> {

    static <A, B, C, D> Function3<A, B, C, D> function3(Function<A, Function<B, Function<C, D>>> f) {
        return (a, b, c) -> function2(f.apply(a)).apply(b, c);
    }

    static <A, B, C, D> Function<A, Function<B, Function<C, D>>> unwind(Function3<A,B,C,D> f) {
        return f.unwind();
    }

    D apply(A a, B b, C c);

    default Function<A, Function2<B, C, D>> curry() {
        return a -> (b, c) -> apply(a, b, c);
    }

    default Function<A, Function<B, Function<C, D>>> unwind() {
        return a -> curry().apply(a).unwind();
    }
}
