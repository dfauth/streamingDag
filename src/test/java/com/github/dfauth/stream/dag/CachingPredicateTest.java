package com.github.dfauth.stream.dag;

import org.junit.Test;
import reactor.core.publisher.Flux;

import java.util.ArrayList;
import java.util.List;

import static com.github.dfauth.stream.dag.CachingPredicate.duplicates;
import static com.github.dfauth.stream.dag.NonCompletingPublisher.supply;
import static org.junit.Assert.assertEquals;

public class CachingPredicateTest {

    @Test
    public void testThis() {
        List<Integer> out = new ArrayList<>();
        Flux.from(supply(1,2,3,1,1,4,4,4,2,1,2,2,5,6)).filter(duplicates()).subscribe(out::add);
        assertEquals(List.of(1,2,3,1,4,2,1,2,5,6), out);
    }

    @Test
    public void testThat() {
//        List<Integer> out = new ArrayList<>();
//        BiPredicate<Integer,Integer> f = Integer::equals;
//        Function<Integer, Predicate<Integer>> f1 = function2(p).unwind();
//        Predicate<Integer> f2 = f1.apply(2);
//        Flux.from(supply(1,2,3,1,1,4)).map(new DynamicPredicate<Integer>(f1)).subscribe(out::add);
//        assertEquals(List.of(1,2,3,1,4), out);
    }


}
