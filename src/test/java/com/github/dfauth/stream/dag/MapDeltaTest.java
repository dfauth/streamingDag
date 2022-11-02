package com.github.dfauth.stream.dag;

import java.util.Map;
import java.util.function.Function;

import static org.junit.Assert.assertTrue;

public class MapDeltaTest {

//    @Test
//    public void testInitialHappyCase() {
//        CountingFunction<Integer, String> f = toCountingFunction(String::valueOf);
//        MapDeltaSplitter<String, Integer> splitter = new MapDeltaSplitter<>();
//        MapDeltaCombiner<String, String> combiner = new MapDeltaCombiner<>();
//        combiner.apply(splitter.)
//        PublishingQueue<Map<String,Integer>> mapQ = new PublishingQueue<>();
//        PublishingQueue<Map.Entry<String,Integer>> entryQ = new PublishingQueue<>();
//        Publisher<Map<String, String>> outputPublisher = combiner.apply(entryQ, mapQ);
//        List<Map<String, String>> out = new ArrayList<>();
//        Flux.from(outputPublisher).subscribe(out::add);
//
//        mapQ.offer(Map.of("a",1, "b", 2, "c", 3));
//        assertMapEquals(Map.of("a","1", "b", "2", "c", "3"), extendedList(out).reverse().headOption().orElse(Collections.emptyMap()));
//        assertEquals(3, f.count());
//        entryQ.offer(Map.entry("a",2));
//        assertMapEquals(Map.of("a","2", "b", "2", "c", "3"), extendedList(out).reverse().headOption().orElse(Collections.emptyMap()));
//        assertEquals(4, f.count());
//        entryQ.offer(Map.entry("d",4));
//        assertMapEquals(Map.of("a","2", "b", "2", "c", "3", "d", "4"), extendedList(out).reverse().headOption().orElse(Collections.emptyMap()));
//        assertEquals(5, f.count());
//    }

    private <K,V> void assertMapEquals(Map<K,V> m1, Map<K,V> m2) {
        assertTrue(m1.equals(m2));
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
