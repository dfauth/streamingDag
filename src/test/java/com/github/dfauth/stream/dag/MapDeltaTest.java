package com.github.dfauth.stream.dag;

import com.github.dfauth.stream.dag.function.Maps;
import com.github.dfauth.stream.dag.function.Tuple3;
import org.junit.Test;
import org.reactivestreams.Publisher;
import reactor.core.publisher.Flux;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.function.Function;

import static com.github.dfauth.stream.dag.function.Lists.extendedList;
import static java.lang.Thread.sleep;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

public class MapDeltaTest {

    @Test
    public void testInitialHappyCase() throws InterruptedException {
        CountingFunction<Integer, String> f = toCountingFunction(String::valueOf);
        Function<Map.Entry<String,Integer>, Map.Entry<String,String>> f0 = e -> Map.entry(e.getKey(), f.apply(e.getValue()));
        Function<Map<String,Integer>, Map<String,String>> f1 = m -> Maps.map(m, (k, v) -> f.apply(v));
        MapDeltaSplitter<String, Integer> splitter = new MapDeltaSplitter<>();
        MapDeltaCombiner<String, String> combiner = new MapDeltaCombiner<>();
        PublishingQueue<Map<String,Integer>> q = new PublishingQueue<>();
        Tuple3<Publisher<Map<String, Integer>>, Publisher<Map<String, Integer>>, Publisher<Map<String, Integer>>> t3 = splitter.apply(q);
        List<Map<String, String>> out = new ArrayList<>();
        Flux.from(combiner.apply(Flux.from(t3._1()).flatMap(m -> Flux.fromIterable(m.entrySet())).map(f0), Flux.from(t3._2()).map(f1))).subscribe(out::add);

        q.offer(Map.of("a",1, "b", 2, "c", 3));
        sleep(100);
        assertMapEquals(Map.of("a","1", "b", "2", "c", "3"), extendedList(out).reverse().headOption().orElse(Collections.emptyMap()));
        assertEquals(3, f.count());
        q.offer(Map.of("a",2));
        sleep(100);
        assertMapEquals(Map.of("a","2", "b", "2", "c", "3"), extendedList(out).reverse().headOption().orElse(Collections.emptyMap()));
        assertEquals(4, f.count());
        q.offer(Map.of("d",4));
        sleep(100);
        assertMapEquals(Map.of("a","2", "b", "2", "c", "3", "d", "4"), extendedList(out).reverse().headOption().orElse(Collections.emptyMap()));
        assertEquals(5, f.count());
    }

    private <K,V> void assertMapEquals(Map<K,V> expected, Map<K,V> actual) {
        assertTrue(String.format("actual map %s does not equal expected map %s",actual,expected),expected.equals(actual));
    }

    public static <T,R> CountingFunction<T,R> toCountingFunction(Function<T,R> f) {
        return new CountingFunction<T, R>() {
            private int cnt;

            @Override
            public int count() {
                return cnt;
            }

            @Override
            public void reset() {
                cnt = 0;
            }

            @Override
            public R apply(T t) {
                cnt++;
                return f.apply(t);
            }
        };
    }
    interface CountingFunction<T,R> extends Function<T,R> {
        int count();
        void reset();
    }

}
