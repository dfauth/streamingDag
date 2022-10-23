package com.github.dfauth.stream.dag;

import java.util.function.BiFunction;
import java.util.function.Function;

import static com.github.dfauth.stream.dag.Function2.function2;
import static com.github.dfauth.stream.dag.Function3.function3;

public interface Functions {
//    static <A,B,C> Function<A,Function<B,C>> unwind(Function2<A,B,C> f2) {
//        return f2.curry();
//    }
//    static <A,B,C,D> Function<A,Function<B,Function<C,D>>> unwind(Function3<A,B,C,D> f3) {
//        return a -> unwind(f3.curry().apply(a));
//    }
//    static <A,B,C,D,E> Function<A,Function<B,Function<C,Function<D,E>>>> unwind(Function4<A,B,C,D,E> f4) {
//        return a -> unwind(f4.curry().apply(a));
//    }
}

@FunctionalInterface
interface Function2<A,B,C> extends BiFunction<A,B,C> {

    static <A,B,C> Function2<A,B,C> function2(BiFunction<A,B,C> f) {
        return f::apply;
    }

    static <A,B,C> Function2<A,B,C> function2(Function<A,Function<B,C>> f) {
        return (a, b) -> f.apply(a).apply(b);
    }

    default Function<A, Function<B,C>> curry() {
        return a -> b -> apply(a,b);
    }
    default Function<A, Function<B,C>> unwind() {
        return curry();
    }
}

@FunctionalInterface
interface Function3<A,B,C,D> {

    static <A,B,C,D> Function3<A,B,C,D> function3(Function<A, Function<B,Function<C,D>>> f) {
        return (a, b, c) -> function2(f.apply(a)).apply(b,c);
    }

    D apply(A a,B b,C c);

    default Function<A, Function2<B,C,D>> curry() {
        return a -> (b, c) -> apply(a,b,c);
    }

    default Function<A, Function<B,Function<C,D>>> unwind() {
        return a -> curry().apply(a).unwind();
    }
}

@FunctionalInterface
interface Function4<A,B,C,D,E> {

    static <A,B,C,D,E> Function4<A,B,C,D,E> function4(Function<A, Function<B,Function<C,Function<D,E>>>> f) {
        return (a, b, c,d) -> function3(f.apply(a)).apply(b,c,d);
    }

    E apply(A a,B b,C c,D d);

    default Function<A, Function3<B,C,D,E>> curry() {
        return a -> (b, c, d) -> apply(a,b,c,d);
    }

    default Function<A, Function<B,Function<C,Function<D,E>>>> unwind() {
        return a -> curry().apply(a).unwind();
    }
}

@FunctionalInterface
interface Function5<A,B,C,D,E,F> {

    F apply(A a,B b,C c,D d,E e);

    default Function<A, Function4<B,C,D,E,F>> curry() {
        return a -> (b, c, d, e) -> apply(a,b,c,d,e);
    }

    default Function<A, Function<B,Function<C,Function<D,Function<E,F>>>>> unwind() {
        return a -> curry().apply(a).unwind();
    }
}

@FunctionalInterface
interface Function6<A,B,C,D,E,F,G> {

    G apply(A a,B b,C c,D d,E e,F f);

    default Function<A, Function5<B,C,D,E,F,G>> curry() {
        return a -> (b, c, d, e, f) -> apply(a,b,c,d,e,f);
    }

    default Function<A, Function<B,Function<C,Function<D,Function<E,Function<F,G>>>>>> unwind() {
        return a -> curry().apply(a).unwind();
    }
}

@FunctionalInterface
interface Function7<A,B,C,D,E,F,G,H> {

    H apply(A a,B b,C c,D d,E e,F f,G g);

    default Function<A, Function6<B,C,D,E,F,G,H>> curry() {
        return a -> (b, c, d, e, f, g) -> apply(a,b,c,d,e,f,g);
    }

    default Function<A,Function<B,Function<C,Function<D,Function<E,Function<F,Function<G,H>>>>>>> unwind() {
        return a -> curry().apply(a).unwind();
    }
}

@FunctionalInterface
interface Function8<A,B,C,D,E,F,G,H,I> {

    I apply(A a,B b,C c,D d,E e,F f,G g,H h);

    default Function<A, Function7<B,C,D,E,F,G,H,I>> curry() {
        return a -> (b, c, d, e, f, g, h) -> apply(a,b,c,d,e,f,g,h);
    }

    default Function<A,Function<B,Function<C,Function<D,Function<E,Function<F,Function<G,Function<H,I>>>>>>>> unwind() {
        return a -> curry().apply(a).unwind();
    }
}
