package com.github.dfauth.stream.dag;

import org.junit.Test;
import org.reactivestreams.Publisher;
import reactor.core.publisher.Flux;

import java.util.ArrayList;
import java.util.List;

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

}
