package com.github.dfauth.stream.dag;

import com.github.dfauth.function.Function2;
import com.github.dfauth.stream.dag.function.Function3;
import com.github.dfauth.stream.dag.function.Function8;
import org.junit.Test;
import org.reactivestreams.Publisher;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.ArrayList;
import java.util.List;
import java.util.function.BiFunction;
import java.util.function.Function;

import static com.github.dfauth.function.Function2.asFunction2;
import static com.github.dfauth.stream.dag.KillSwitch.killSwitch;
import static com.github.dfauth.stream.dag.NonCompletingPublisher.supply;
import static org.junit.Assert.assertEquals;
import static reactor.core.publisher.Mono.just;

public class OneSidedCachingTransformerTest {

    @Test
    public void testIt() {
        List<Integer> out = new ArrayList<>();
        OneSidedCachingTransformer<Integer, Integer, Integer> tx = new OneSidedCachingTransformer<>(Integer::sum);
        Flux.from(tx).subscribe(i -> {});
        Flux.from(tx.apply(supply(1), just(2))).subscribe(out::add);
        assertEquals(List.of(3),out);
    }

    @Test
    public void testThat() {
        Function<Integer, Function<Integer, Integer>> f = asFunction2(Integer::sum).curried();
        List<Integer> out = new ArrayList<>();
        SubscriberFunction<Integer,Integer,Integer> subscriberFn = new SubscriberFunction<>(f);
        Flux.from(subscriberFn).subscribe(i -> {});
        Flux.from(supply(2)).subscribe(subscriberFn);
        KillSwitch<Integer> killSwitch = killSwitch(just(1).flatMap(subscriberFn.andThen(Mono::justOrEmpty)));
        Flux.from(killSwitch).subscribe(out::add);
        assertEquals(List.of(3),out);
    }

    @Test
    public void testMySanity() {
        List<Integer> out = new ArrayList<>();
        Flux.from(supply(1)).log().subscribe(out::add);
        assertEquals(List.of(1),out);
    }

    @Test
    public void testCompose2() {
        Function2<Integer,Integer,Integer> _f = OneSidedCachingTransformerTest::doit2;
        BiFunction<Publisher<Integer>, Publisher<Integer>, Publisher<Integer>> _g = OneSidedCachingTransformer.compose2(_f);

        PublishingQueue<Integer> q1 = new PublishingQueue<>();
        PublishingQueue<Integer> q2 = new PublishingQueue<>();

        Publisher<Integer> result = _g.apply(q1, q2);

        List<Integer> out = new ArrayList<>();
        Flux.from(result).log().subscribe(out::add);
        assertEquals(List.of(),out);
        q1.offer(1);
        assertEquals(List.of(),out);
        q2.offer(1);
        assertEquals(List.of(2),out);
        q1.offer(2);
        assertEquals(List.of(2),out);
        q2.offer(2);
        assertEquals(List.of(2,4),out);
    }

    @Test
    public void testCompose3() {
        Function3<Integer,Integer,Integer,Integer> _f = OneSidedCachingTransformerTest::doit3;
        Function3<Publisher<Integer>, Publisher<Integer>, Publisher<Integer>, Publisher<Integer>> _g = OneSidedCachingTransformer.compose3(_f);

        PublishingQueue<Integer> q1 = new PublishingQueue<>();
        PublishingQueue<Integer> q2 = new PublishingQueue<>();
        PublishingQueue<Integer> q3 = new PublishingQueue<>();

        Publisher<Integer> result = _g.apply(q1,q2,q3);

        List<Integer> out = new ArrayList<>();
        Flux.from(result).log().subscribe(out::add);
        assertEquals(List.of(),out);
        q1.offer(1);
        assertEquals(List.of(),out);
        q2.offer(2);
        assertEquals(List.of(),out);
        q3.offer(3);
        assertEquals(List.of(6),out);
        q1.offer(4);
        assertEquals(List.of(6),out);
        q2.offer(5);
        assertEquals(List.of(6),out);
        q3.offer(6);
        assertEquals(List.of(6,15),out);
    }

    @Test
    public void testCompose8() {
        Function8<Integer,Integer,Integer,Integer,Integer,Integer,Integer,Integer,Integer> _f = OneSidedCachingTransformerTest::doit8;
        Function8<Publisher<Integer>, Publisher<Integer>, Publisher<Integer>, Publisher<Integer>, Publisher<Integer>, Publisher<Integer>, Publisher<Integer>, Publisher<Integer>, Publisher<Integer>> _g = OneSidedCachingTransformer.compose8(_f);

        PublishingQueue<Integer> q1 = new PublishingQueue<>();
        PublishingQueue<Integer> q2 = new PublishingQueue<>();
        PublishingQueue<Integer> q3 = new PublishingQueue<>();
        PublishingQueue<Integer> q4 = new PublishingQueue<>();
        PublishingQueue<Integer> q5 = new PublishingQueue<>();
        PublishingQueue<Integer> q6 = new PublishingQueue<>();
        PublishingQueue<Integer> q7 = new PublishingQueue<>();
        PublishingQueue<Integer> q8 = new PublishingQueue<>();

        Publisher<Integer> result = _g.apply(q1, q2, q3, q4, q5, q6, q7, q8);

        List<Integer> out = new ArrayList<>();
        Flux.from(result).subscribe(out::add);
        assertEquals(List.of(),out);
        q1.offer(1);
        assertEquals(List.of(),out);
        q2.offer(1);
        assertEquals(List.of(),out);
        q3.offer(1);
        assertEquals(List.of(),out);
        q4.offer(1);
        assertEquals(List.of(),out);
        q5.offer(1);
        assertEquals(List.of(),out);
        q6.offer(1);
        assertEquals(List.of(),out);
        q7.offer(1);
        assertEquals(List.of(),out);
        q8.offer(1);
        assertEquals(List.of(8),out);
    }

    public static int doit2(int a, int b) {
        return a+b;
    }

    public static int doit3(int a, int b, int c) {
        return a+b+c;
    }

    public static int doit8(int a, int b, int c, int d, int e, int f, int g, int h) {
        return a+b+c+d+e+f+g+h;
    }
}
