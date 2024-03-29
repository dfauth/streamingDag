package com.github.dfauth.stream.dag.function;

import com.github.dfauth.stream.dag.CachingTransformer;
import com.github.dfauth.stream.dag.PublishingQueue;
import org.junit.Test;
import org.reactivestreams.Publisher;
import reactor.core.publisher.Flux;

import java.util.ArrayList;
import java.util.List;
import java.util.function.BiFunction;
import java.util.function.Function;

import static com.github.dfauth.stream.dag.NonCompletingPublisher.supply;
import static com.github.dfauth.stream.dag.function.Function2.function2;
import static com.github.dfauth.stream.dag.function.Function3.unwind;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

public class FunctionNTest {

    @Test
    public void testFunction2() {
        BiFunction<A, B, I> f = FunctionNTest::doit2;
        Function2<A, B, I> f1 = function2(f);
        Function<A, Function<B, I>> f2 = f1.curry();
        Function<A, Function<B, I>> f3 = f1.unwind();
        assertNotNull(f2.apply(new A()).apply(new B()));
        assertNotNull(f3.apply(new A()).apply(new B()));
    }

    @Test
    public void testFunction3() {
        {
            Function3<A,B,C,I> f = FunctionNTest::doit3;
            Function<A, Function2<B, C, I>> f2 = f.curry();
            Function<A, Function<B, Function<C, I>>> f3 = f.unwind();
            assertNotNull(f3.apply(new A()).apply(new B()).apply(new C()));
        }
        {
            Function<A, Function<B, Function<C, I>>> f = unwind(FunctionNTest::doit3);
            assertEquals(new I(), f.apply(new A()).apply(new B()).apply(new C()));
        }
        {
            List<I> out = new ArrayList<>();
            Function3<Publisher<A>,Publisher<B>,Publisher<C>,Publisher<I>> f = CachingTransformer.compose3(FunctionNTest::doit3);
            Publisher<I> p = f.apply(supply(new A()), supply(new B()), supply(new C()));
            Flux.from(p).subscribe(out::add);
            assertEquals(List.of(new I()), out);
        }
    }


    @Test
    public void testFunction4() {
        {
            Function4<A,B,C,D,I> f = FunctionNTest::doit4;
            Function<A, Function3<B, C, D, I>> f2 = f.curry();
            Function<A, Function<B, Function<C, Function<D, I>>>> f3 = f.unwind();
            assertNotNull(f3.apply(new A()).apply(new B()).apply(new C()).apply(new D()));
        }
        {
            Function<A, Function<B, Function<C, Function<D, I>>>> f = Function4.unwind(FunctionNTest::doit4);
            assertEquals(new I(), f.apply(new A()).apply(new B()).apply(new C()).apply(new D()));
        }
        {
            List<I> out = new ArrayList<>();
            Function4<Publisher<A>,Publisher<B>,Publisher<C>,Publisher<D>,Publisher<I>> f = CachingTransformer.compose4(FunctionNTest::doit4);
            Publisher<I> p = f.apply(supply(new A()), supply(new B()), supply(new C()), supply(new D()));
            Flux.from(p).subscribe(out::add);
            assertEquals(List.of(new I()), out);
        }
        {
            List<Integer> out = new ArrayList<>();
            Function4<Publisher<Integer>, Publisher<Integer>, Publisher<Integer>, Publisher<Integer>, Publisher<Integer>> f = CachingTransformer.compose4(FunctionNTest::testInt4);
            PublishingQueue<Integer> q1 = new PublishingQueue<>();
            Publisher<Integer> p = f.apply(supply(1), supply(2), supply(3), q1);
            Flux.from(p).subscribe(out::add);
            assertEquals(List.of(), out);
            q1.offer(4);
            assertEquals(List.of(10), out);
        }
    }

    @Test
    public void testFunction5() {
        {
            Function5<A,B,C,D,E,I> f = FunctionNTest::doit5;
            Function<A, Function4<B, C, D, E, I>> f2 = f.curry();
            Function<A, Function<B, Function<C, Function<D, Function<E, I>>>>> f3 = f.unwind();
            assertNotNull(f3.apply(new A()).apply(new B()).apply(new C()).apply(new D()).apply(new E()));
        }
        {
            Function<A, Function<B, Function<C, Function<D, Function<E, I>>>>> f = Function5.unwind(FunctionNTest::doit5);
            assertEquals(new I(), f.apply(new A()).apply(new B()).apply(new C()).apply(new D()).apply(new E()));
        }
        {
            List<I> out = new ArrayList<>();
            Function5<Publisher<A>, Publisher<B>, Publisher<C>, Publisher<D>, Publisher<E>, Publisher<I>> f = CachingTransformer.compose5(FunctionNTest::doit5);
            Publisher<I> p = f.apply(supply(new A()), supply(new B()), supply(new C()), supply(new D()), supply(new E()));
            Flux.from(p).subscribe(out::add);
            assertEquals(List.of(new I()), out);
        }
        {
            List<Integer> out = new ArrayList<>();
            Function5<Publisher<Integer>, Publisher<Integer>, Publisher<Integer>, Publisher<Integer>, Publisher<Integer>, Publisher<Integer>> f = CachingTransformer.compose5(FunctionNTest::testInt5);
            Publisher<Integer> p = f.apply(supply(1), supply(2), supply(3), supply(4), supply(5));
            Flux.from(p).subscribe(out::add);
            assertEquals(List.of(15), out);
        }
    }

    @Test
    public void testFunction6() {
        {
            Function6<A, B, C, D, E, F, I> f = FunctionNTest::doit6;
            Function<A, Function5<B, C, D, E, F, I>> f2 = f.curry();
            Function<A, Function<B, Function<C, Function<D, Function<E, Function<F, I>>>>>> f3 = f.unwind();
            assertNotNull(f3.apply(new A()).apply(new B()).apply(new C()).apply(new D()).apply(new E()));
        }
        {
            Function<A, Function<B, Function<C, Function<D, Function<E, Function<F, I>>>>>> f = Function6.unwind(FunctionNTest::doit6);
            assertEquals(new I(), f.apply(new A()).apply(new B()).apply(new C()).apply(new D()).apply(new E()).apply(new F()));
        }
        {
            List<I> out = new ArrayList<>();
            Function6<Publisher<A>, Publisher<B>, Publisher<C>, Publisher<D>, Publisher<E>, Publisher<F>, Publisher<I>> f = CachingTransformer.compose6(FunctionNTest::doit6);
            Publisher<I> p = f.apply(supply(new A()), supply(new B()), supply(new C()), supply(new D()), supply(new E()), supply(new F()));
            Flux.from(p).subscribe(out::add);
            assertEquals(List.of(new I()), out);
        }
        {
            List<Integer> out = new ArrayList<>();
            Function6<Publisher<Integer>, Publisher<Integer>, Publisher<Integer>, Publisher<Integer>, Publisher<Integer>, Publisher<Integer>, Publisher<Integer>> f = CachingTransformer.compose6(FunctionNTest::testInt6);
            Publisher<Integer> p = f.apply(supply(1), supply(2), supply(3), supply(4), supply(5), supply(6));
            Flux.from(p).subscribe(out::add);
            assertEquals(List.of(21), out);
        }
    }

    @Test
    public void testFunction7() {
        {
            Function7<A,B,C,D,E,F,G,I> f = FunctionNTest::doit7;
            Function<A, Function6<B, C, D, E, F, G, I>> f2 = f.curry();
            Function<A, Function<B, Function<C, Function<D, Function<E, Function<F, Function<G, I>>>>>>> f3 = f.unwind();
            assertNotNull(f3.apply(new A()).apply(new B()).apply(new C()).apply(new D()).apply(new E()).apply(new F()));
        }
        {
            Function<A, Function<B, Function<C, Function<D, Function<E, Function<F, Function<G, I>>>>>>> f = Function7.unwind(FunctionNTest::doit7);
            assertEquals(new I(), f.apply(new A()).apply(new B()).apply(new C()).apply(new D()).apply(new E()).apply(new F()).apply(new G()));
        }
        {
            List<I> out = new ArrayList<>();
            Function7<Publisher<A>, Publisher<B>, Publisher<C>, Publisher<D>, Publisher<E>, Publisher<F>, Publisher<G>, Publisher<I>> f = CachingTransformer.compose7(FunctionNTest::doit7);
            Publisher<I> p = f.apply(supply(new A()), supply(new B()), supply(new C()), supply(new D()), supply(new E()), supply(new F()), supply(new G()));
            Flux.from(p).subscribe(out::add);
            assertEquals(List.of(new I()), out);
        }
        {
            List<Integer> out = new ArrayList<>();
            Function7<Publisher<Integer>, Publisher<Integer>, Publisher<Integer>, Publisher<Integer>, Publisher<Integer>, Publisher<Integer>, Publisher<Integer>, Publisher<Integer>> f = CachingTransformer.compose7(FunctionNTest::testInt7);
            Publisher<Integer> p = f.apply(supply(1), supply(2), supply(3), supply(4), supply(5), supply(6), supply(7));
            Flux.from(p).subscribe(out::add);
            assertEquals(List.of(28), out);
        }
    }

    @Test
    public void testFunction8() {
        {
            Function8<A, B, C, D, E, F, G, H, I> f = FunctionNTest::doit8;
            Function<A, Function7<B, C, D, E, F, G, H, I>> f2 = f.curry();
            Function<A, Function<B, Function<C, Function<D, Function<E, Function<F, Function<G, Function<H, I>>>>>>>> f3 = f.unwind();
            assertNotNull(f3.apply(new A()).apply(new B()).apply(new C()).apply(new D()).apply(new E()).apply(new F()).apply(new G()));
        }
        {
            Function<A, Function<B, Function<C, Function<D, Function<E, Function<F, Function<G, Function<H, I>>>>>>>> f = Function8.unwind(FunctionNTest::doit8);
            assertEquals(new I(), f.apply(new A()).apply(new B()).apply(new C()).apply(new D()).apply(new E()).apply(new F()).apply(new G()).apply(new H()));
        }
        {
            List<I> out = new ArrayList<>();
            Function8<Publisher<A>, Publisher<B>, Publisher<C>, Publisher<D>, Publisher<E>, Publisher<F>, Publisher<G>, Publisher<H>, Publisher<I>> f = CachingTransformer.compose8(FunctionNTest::doit8);
            Publisher<I> p = f.apply(supply(new A()), supply(new B()), supply(new C()), supply(new D()), supply(new E()), supply(new F()), supply(new G()), supply(new H()));
            Flux.from(p).subscribe(out::add);
            assertEquals(List.of(new I()), out);
        }
        {
            List<Integer> out = new ArrayList<>();
            Function8<Publisher<Integer>, Publisher<Integer>, Publisher<Integer>, Publisher<Integer>, Publisher<Integer>, Publisher<Integer>, Publisher<Integer>, Publisher<Integer>, Publisher<Integer>> f = CachingTransformer.compose8(FunctionNTest::testInt8);
            Publisher<Integer> p = f.apply(supply(1), supply(2), supply(3), supply(4), supply(5), supply(6), supply(7), supply(8));
            Flux.from(p).subscribe(out::add);
            assertEquals(List.of(36), out);
        }
    }

    public static I doit0() {
        return new I();
    }
    public static I doit1(A a) {
        return new I();
    }
    public static I doit2(A a, B b) {
        return new I();
    }
    public static I doit3(A a, B b, C c) {
        return new I();
    }
    public static I doit4(A a, B b, C c, D d) {
        return new I();
    }
    public static I doit5(A a, B b, C c, D d, E e) {
        return new I();
    }
    public static I doit6(A a, B b, C c, D d, E e,F f) {
        return new I();
    }
    public static I doit7(A a, B b, C c, D d, E e,F f,G g) {
        return new I();
    }
    public static I doit8(A a, B b, C c, D d, E e,F f,G g,H h) {
        return new I();
    }

    static class A {
    }
    static class B {
    }
    static class C {
    }
    static class D {
    }
    static class E {
    }
    static class F {
    }
    static class G {
    }
    static class H {
    }
    static class I {
        @Override
        public boolean equals(Object obj) {
            return obj instanceof I;
        }
    }

    public static int testInt4(int a, int b, int c, int d) {
        return a+b+c+d;
    }
    public static int testInt5(int a, int b, int c, int d, int e) {
        return a+b+c+d+e;
    }
    public static int testInt6(int a, int b, int c, int d, int e, int f) {
        return a+b+c+d+e+f;
    }
    public static int testInt7(int a, int b, int c, int d, int e, int f, int g) {
        return a+b+c+d+e+f+g;
    }
    public static int testInt8(int a, int b, int c, int d, int e, int f, int g, int h) {
        return a+b+c+d+e+f+g+h;
    }
}
