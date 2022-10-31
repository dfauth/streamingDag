package com.github.dfauth.stream.dag;

import org.junit.Test;
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
}
