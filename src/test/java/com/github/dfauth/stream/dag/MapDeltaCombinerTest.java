package com.github.dfauth.stream.dag;

import org.junit.Test;
import org.reactivestreams.Publisher;
import reactor.core.publisher.Flux;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import static com.github.dfauth.stream.dag.function.Lists.extendedList;
import static org.junit.Assert.assertTrue;

public class MapDeltaCombinerTest {

    @Test
    public void testInitialHappyCase() {
        MapDeltaCombiner<String, Integer> combiner = new MapDeltaCombiner<>();
        PublishingQueue<Map<String,Integer>> mapQ = new PublishingQueue<>();
        PublishingQueue<Map.Entry<String,Integer>> entryQ = new PublishingQueue<>();
        Publisher<Map<String, Integer>> outputPublisher = combiner.apply(entryQ, mapQ);
        List<Map<String, Integer>> out = new ArrayList<>();
        Flux.from(outputPublisher).subscribe(out::add);

        mapQ.offer(Map.of("a",1, "b", 2, "c", 3));
        assertMapEquals(Map.of("a",1, "b", 2, "c", 3), extendedList(out).reverse().headOption().orElse(Collections.emptyMap()));
        entryQ.offer(Map.entry("a",2));
        assertMapEquals(Map.of("a",2, "b", 2, "c", 3), extendedList(out).reverse().headOption().orElse(Collections.emptyMap()));
        entryQ.offer(Map.entry("d",4));
        assertMapEquals(Map.of("a",2, "b", 2, "c", 3, "d", 4), extendedList(out).reverse().headOption().orElse(Collections.emptyMap()));
    }

    @Test
    public void testInitialMapEmpty() {
        MapDeltaCombiner<String, Integer> combiner = new MapDeltaCombiner<>();
        PublishingQueue<Map<String,Integer>> mapQ = new PublishingQueue<>();
        PublishingQueue<Map.Entry<String,Integer>> entryQ = new PublishingQueue<>();
        Publisher<Map<String, Integer>> outputPublisher = combiner.apply(entryQ, mapQ);
        List<Map<String, Integer>> out = new ArrayList<>();
        Flux.from(outputPublisher).subscribe(out::add);

        entryQ.offer(Map.entry("a",1));
        assertMapEquals(Map.of("a", 1), extendedList(out).reverse().headOption().orElse(Collections.emptyMap()));
    }

    private <K,V> void assertMapEquals(Map<K,V> m1, Map<K,V> m2) {
        assertTrue(m1.equals(m2));
    }

}
