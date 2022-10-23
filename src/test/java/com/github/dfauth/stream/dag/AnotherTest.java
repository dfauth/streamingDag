package com.github.dfauth.stream.dag;

import com.github.dfauth.trycatch.ExceptionalRunnable;
import lombok.extern.slf4j.Slf4j;
import org.junit.Test;
import org.reactivestreams.Publisher;
import reactor.core.publisher.Flux;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.function.Function;

import static org.junit.Assert.assertEquals;

@Slf4j
public class AnotherTest {

    @Test
    public void testIt() throws NoSuchMethodException, InterruptedException {
        ExceptionalRunnable f0 = AnotherTest::doit0;
        Function<A, D> f1 = AnotherTest::doit1;
        Function2<A, B, D> f2 = AnotherTest::doit2;
        Function3<A, B, C, D> f3 = AnotherTest::doit3;
        Function4<A, B, C, BigDecimal, D> f4 = AnotherTest::doit4;
        Function5<A, B, C, BigDecimal, String, D> f5 = AnotherTest::doit5;
        PublishingQueue<A> q1 = new PublishingQueue<>();
        PublishingQueue<B> q2 = new PublishingQueue<>();
        PublishingQueue<C> q3 = new PublishingQueue<>();
        PublishingQueue<BigDecimal> q4 = new PublishingQueue<>();
        PublishingQueue<String> q5 = new PublishingQueue<>();
        Function<A, Function<B, Function<C, Function<BigDecimal, Function<String, D>>>>> f = f5.unwind();
        Publisher<D> g = (Publisher<D>) CurryUtils.curryingMerge(f, q1,q2,q3,q4,q5);
        ArrayList<D> out = new ArrayList<>();
        Flux.from(g).log().subscribe(out::add);
        assertEquals(0, out.size());
        q1.offer(new A());
        assertEquals(0, out.size());
        q2.offer(new B());
        assertEquals(0, out.size());
        q3.offer(new C());
        assertEquals(0, out.size());
        q4.offer(new BigDecimal(0));
        assertEquals(0, out.size());
        q5.offer("new B()");
        assertEquals(1, out.size());
//        sleep(1000);
    }

    private Function<Publisher<A>, Function<Publisher<B>, Function<Publisher<C>, Function<Publisher<BigDecimal>, Function<Publisher<String>, Publisher<D>>>>>> resolve(Object doit1) {
        return null;
    }

    public static D doit0() {
        return new D();
    }
    public static D doit1(A a) {
        return new D();
    }
    public static D doit2(A a, B b) {
        return new D();
    }
    public static D doit3(A a, B b,C c) {
        return new D();
    }
    public static D doit4(A a, B b,C c, BigDecimal d) {
        return new D();
    }
    public static D doit5(A a, B b, C c, BigDecimal d, String e) {
        return new D();
    }

    static class A {
    }
    static class B {
    }
    static class C {
    }
    static class D {
    }
}
