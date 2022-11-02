package com.github.dfauth.stream.dag;

import org.junit.Test;
import reactor.core.publisher.Flux;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import static com.github.dfauth.stream.dag.Subscribers.fromConsumer;
import static com.github.dfauth.stream.dag.function.Lists.extendedList;
import static org.junit.Assert.assertEquals;

public class MapDeltaSplitterTest {

    @Test
    public void testIt() throws InterruptedException {
        MapDeltaSplitter<String, Integer> splitter = new MapDeltaSplitter<>();
        PublishingQueue<Map<String,Integer>> q = new PublishingQueue<>();
        q.subscribe(splitter);
        List<Map<String, Integer>> unchanged = new ArrayList<>();
        List<Map<String, Integer>> modified = new ArrayList<>();
        List<Map<String, Integer>> removed = new ArrayList<>();
        Flux.from(splitter).subscribe(unchanged::add);
        splitter.subscribeModified(fromConsumer(modified::add));
        splitter.subscribeRemoved(fromConsumer(removed::add));

        q.offer(Map.of("a",1));
        Thread.sleep(100);
        assertEquals(Map.of("a", 1), extendedList(modified).reverse().headOption().orElse(null));
        assertEquals(null, extendedList(unchanged).reverse().headOption().orElse(null));
        assertEquals(null, extendedList(removed).reverse().headOption().orElse(null));

        q.offer(Map.of("a",2));
        Thread.sleep(100);
        assertEquals(Map.of("a", 2), extendedList(modified).reverse().headOption().orElse(null));
        assertEquals(null, extendedList(unchanged).reverse().headOption().orElse(null));
        assertEquals(null, extendedList(removed).reverse().headOption().orElse(null));

        q.offer(Map.of("a",2, "c", 3));
        Thread.sleep(100);
        assertEquals(Map.of("c", 3), extendedList(modified).reverse().headOption().orElse(null));
        assertEquals(Map.of("a", 2), extendedList(unchanged).reverse().headOption().orElse(null));
        assertEquals(null, extendedList(removed).reverse().headOption().orElse(null));

        q.offer(Map.of("b",2, "c", 3));
        Thread.sleep(100);
        assertEquals(Map.of("b", 2), extendedList(modified).reverse().headOption().orElse(null));
        assertEquals(Map.of("c", 3), extendedList(unchanged).reverse().headOption().orElse(null));
        assertEquals(Map.of("a", 2), extendedList(removed).reverse().headOption().orElse(null));
    }
}
