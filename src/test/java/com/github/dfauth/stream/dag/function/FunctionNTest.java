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
            Function3<Publisher<A>,Publisher<B>,Publisher<C>,Publisher<I>> f = CachingTransformer.compose(FunctionNTest::doit3);
            Publisher<I> p = f.apply(supply(new A()), supply(new B()), supply(new C()));
            Flux.from(p).subscribe(out::add);
            assertEquals(List.of(new I()), out);
        }
    }


    @Test
    public void testFunction4() throws InterruptedException {
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
            Function4<Publisher<A>,Publisher<B>,Publisher<C>,Publisher<D>,Publisher<I>> f = CachingTransformer.compose(FunctionNTest::doit4);
            Publisher<I> p = f.apply(supply(new A()), supply(new B()), supply(new C()), supply(new D()));
            Flux.from(p).subscribe(out::add);
            assertEquals(List.of(new I()), out);
        }
        {
            List<Integer> out = new ArrayList<>();
            Function4<Publisher<Integer>, Publisher<Integer>, Publisher<Integer>, Publisher<Integer>, Publisher<Integer>> f = CachingTransformer.compose(FunctionNTest::testInt4);
            PublishingQueue<Integer> q1 = new PublishingQueue<>();
            Publisher<Integer> p = f.apply(supply(1), supply(2), supply(3), q1);
            Flux.from(p).subscribe(out::add);
            assertEquals(List.of(), out);
            q1.offer(4);
            assertEquals(List.of(10), out);
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
    public static I doit7(A a, B b, C c, D d, E e,F f,G g,H h) {
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
            return obj != null && obj instanceof I;
        }
    }

    public static int testInt4(int a, int b, int c, int d) {
        return a+b+c+d;
    }
}
