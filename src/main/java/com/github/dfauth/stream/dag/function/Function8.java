package com.github.dfauth.stream.dag.function;

import java.util.function.Function;

import static com.github.dfauth.stream.dag.function.Function7.function7;

@FunctionalInterface
public interface Function8<A, B, C, D, E, F, G, H, I> {

    static <A,B,C,D,E,F,G,H,I> Function8<A,B,C,D,E,F,G,H,I> function8(Function<A, Function<B, Function<C, Function<D, Function<E, Function<F,Function<G,Function<H,I>>>>>>>> _f) {
        return (a, b, c, d, e, f, g, h) -> function7(_f.apply(a)).apply(b, c, d, e, f, g, h);
    }

    static <A,B,C,D,E,F,G,H,I> Function<A, Function<B, Function<C, Function<D, Function<E, Function<F,Function<G,Function<H,I>>>>>>>> unwind(Function8<A,B,C,D,E,F,G,H,I> f) {
        return f.unwind();
    }

    I apply(A a, B b, C c, D d, E e, F f, G g, H h);

    default Function<A, Function7<B, C, D, E, F, G, H, I>> curry() {
        return a -> (b, c, d, e, f, g, h) -> apply(a, b, c, d, e, f, g, h);
    }

    default Function<A, Function<B, Function<C, Function<D, Function<E, Function<F, Function<G, Function<H, I>>>>>>>> unwind() {
        return a -> curry().apply(a).unwind();
    }
}
