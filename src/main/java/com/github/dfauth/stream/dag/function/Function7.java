package com.github.dfauth.stream.dag.function;

import java.util.function.Function;

@FunctionalInterface
public interface Function7<A, B, C, D, E, F, G, H> {

    H apply(A a, B b, C c, D d, E e, F f, G g);

    default Function<A, Function6<B, C, D, E, F, G, H>> curry() {
        return a -> (b, c, d, e, f, g) -> apply(a, b, c, d, e, f, g);
    }

    default Function<A, Function<B, Function<C, Function<D, Function<E, Function<F, Function<G, H>>>>>>> unwind() {
        return a -> curry().apply(a).unwind();
    }
}
