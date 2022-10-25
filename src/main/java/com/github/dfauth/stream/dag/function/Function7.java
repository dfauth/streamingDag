package com.github.dfauth.stream.dag.function;

import java.util.function.Function;

import static com.github.dfauth.stream.dag.function.Function6.function6;

@FunctionalInterface
public interface Function7<A, B, C, D, E, F, G, H> {

    static <A,B,C,D,E,F,G,H> Function7<A,B,C,D,E,F,G,H> function7(Function<A, Function<B, Function<C, Function<D, Function<E, Function<F,Function<G,H>>>>>>> _f) {
        return (a, b, c, d, e, f, g) -> function6(_f.apply(a)).apply(b, c, d, e, f, g);
    }

    static <A,B,C,D,E,F,G,H> Function<A, Function<B, Function<C, Function<D, Function<E, Function<F,Function<G,H>>>>>>> unwind(Function7<A,B,C,D,E,F,G,H> f) {
        return f.unwind();
    }

    H apply(A a, B b, C c, D d, E e, F f, G g);

    default Function<A, Function6<B, C, D, E, F, G, H>> curry() {
        return a -> (b, c, d, e, f, g) -> apply(a, b, c, d, e, f, g);
    }

    default Function<A, Function<B, Function<C, Function<D, Function<E, Function<F, Function<G, H>>>>>>> unwind() {
        return a -> curry().apply(a).unwind();
    }
}
