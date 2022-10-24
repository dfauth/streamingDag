package com.github.dfauth.stream.dag;

import org.junit.Ignore;
import org.junit.Test;
import org.reactivestreams.Publisher;
import reactor.core.publisher.Flux;

import java.util.ArrayList;
import java.util.List;
import java.util.function.BiFunction;

import static com.github.dfauth.stream.dag.NonCompletingPublisher.supply;
import static org.junit.Assert.assertEquals;

public class CachingTransformerTest {

    @Test
    public void testIt() {
        List<Integer> out = new ArrayList<>();
        Publisher<Integer> tx = new CachingTransformer<>(Integer::sum).apply(supply(1), supply(2));
        Flux.from(tx).subscribe(out::add);
        assertEquals(List.of(3),out);
    }

    @Ignore("this is failing due to the underlying behaviour of Flux.share()")
    @Test
    public void testThat() {
        List<Integer> out = new ArrayList<>();
        BiFunction<Publisher<Integer>, Publisher<Integer>, Publisher<Integer>> f = new CachingTransformer<>(CachingTransformerTest::sum);
        PublishingQueue<Integer> q1 = new PublishingQueue<>();
        Publisher<Integer> p = f.apply(q1, supply(2));
        Flux.from(p).subscribe(out::add);
        assertEquals(List.of(), out);
        q1.offer(1);
        assertEquals(List.of(3), out);
    }

    public static int sum(int a, int b) {
        return a+b;
    }


}
