package com.github.dfauth.stream.dag;

import org.junit.Test;
import org.reactivestreams.Publisher;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.ArrayList;
import java.util.List;
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
        Publisher<Integer> tx = new OneSidedCachingTransformer<>(Integer::sum).apply(supply(1), just(2));
        Flux.from(tx).subscribe(out::add);
        assertEquals(List.of(3),out);
    }

    @Test
    public void testThat() {
        Function<Integer, Function<Integer, Integer>> f = asFunction2(Integer::sum).curried();
        List<Integer> out = new ArrayList<>();
        SubscriberFunction<Integer,Integer> subscriberFn = new SubscriberFunction<>();
        Flux.from(just(2)).map(f).subscribe(subscriberFn);
        KillSwitch<Integer> killSwitch = killSwitch(just(1).flatMap(subscriberFn.andThen(Mono::justOrEmpty)));
//        Flux.from(just(add2)).subscribe(subscriberFn);
        Flux.from(killSwitch).subscribe(out::add);
        assertEquals(List.of(3),out);
    }

    @Test
    public void testMySanity() {
        List<Integer> out = new ArrayList<>();
        Flux.from(supply(1)).log().subscribe(out::add);
        assertEquals(List.of(1),out);
    }
}
