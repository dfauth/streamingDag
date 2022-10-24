package com.github.dfauth.stream.dag;

import com.github.dfauth.stream.dag.function.Function2;
import org.junit.Test;
import reactor.core.publisher.Flux;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.function.BiFunction;
import java.util.function.Function;

import static org.junit.Assert.assertEquals;
import static reactor.core.publisher.Mono.just;

public class SubscriberFunctionTest {

    @Test
    public void testIt() {
        List<Integer> out = new ArrayList<>();
        BiFunction<Integer,Integer,Integer> f = Integer::sum;
        Function<Integer, Function<Integer, Integer>> f2 = Function2.function2(f).curry();
        Function<Integer, Integer> add2 = f2.apply(2);
        SubscriberFunction<Integer, Integer> tx = new SubscriberFunction<>();
        Flux.from(just(add2)).subscribe(tx);
        assertEquals(List.of(),out);
        Flux.from(just(1)).map(tx).filter(Optional::isPresent).map(Optional::get).subscribe(out::add);
        assertEquals(List.of(3),out);
    }
}
