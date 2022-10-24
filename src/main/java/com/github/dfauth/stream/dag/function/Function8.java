package com.github.dfauth.stream.dag.function;

import java.util.function.Function;

@FunctionalInterface
public interface Function8<A, B, C, D, E, F, G, H, I> {

    I apply(A a, B b, C c, D d, E e, F f, G g, H h);

    default Function<A, Function7<B, C, D, E, F, G, H, I>> curry() {
        return a -> (b, c, d, e, f, g, h) -> apply(a, b, c, d, e, f, g, h);
    }

    default Function<A, Function<B, Function<C, Function<D, Function<E, Function<F, Function<G, Function<H, I>>>>>>>> unwind() {
        return a -> curry().apply(a).unwind();
    }
}
