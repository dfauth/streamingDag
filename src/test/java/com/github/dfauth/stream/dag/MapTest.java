package com.github.dfauth.stream.dag;

import com.github.dfauth.stream.dag.function.Maps;
import org.junit.Test;

import java.util.Map;
import java.util.function.BiFunction;
import java.util.function.Function;

import static org.junit.Assert.assertEquals;

public class MapTest {

    private static final Map<String, Integer> REF = Map.of("a",1, "b", 2, "c", 3,"d",4);
    Function<Integer,String> f1 = String::valueOf;
    Function<Map<String,Integer>,Map<String,String>> f = i -> Maps.map(i, (k,v) -> f1.apply(v));

    public static <K,V> BiFunction<Map<K,V>, V, Map<K,V>> merge(Function<V,K> keyExtractor) {
        return (m,e) -> Maps.mergeEntry(m, keyExtractor.apply(e), e);
    }

    public static <K,V,T> BiFunction<Map<K,V>, V, Map<K,T>> merge(Function<V,K> keyExtractor, Function<V,T> transformer) {
        return (m,e) -> {
            return Maps.map(
                    Maps.mergeEntry(m, keyExtractor.apply(e), e),
                    (k,v) -> transformer.apply(v)
            );
        };
    }

    public static <K,V,T> BiFunction<Map<K,V>, V, Map<K,T>> merge2(Function<V,K> keyExtractor, Function<V,T> transformer) {
        return (m,e) -> {
            Map<K, T> tmp = Maps.map(
                    m,
                    (k, v) -> transformer.apply(v)
            );
            return Maps.mergeEntry(tmp, keyExtractor.apply(e), transformer.apply(e));
        };
    }

    @Test
    public void testIt() {
        assertEquals(Map.of("a","1", "b", "2", "c", "3","d","4"), f.apply(REF));
        CountingFunction<Map<String, Integer>, Map<String, String>> g = toCountingFunction(f);
        assertEquals(Map.of("a","1", "b", "2", "c", "3","d","4"), g.apply(REF));
        assertEquals(1, g.count());

        assertEquals(Map.of("a","1", "b", "2", "c", "3","d","4"), Maps.map(REF, (k,v) -> f1.apply(v)));
        CountingFunction<Integer, String> h = toCountingFunction(f1);
        assertEquals(Map.of("a","1", "b", "2", "c", "3","d","4"), Maps.map(REF, (k,v) -> h.apply(v)));
        assertEquals(REF.size(), h.count());

        BiFunction<Map<String, Integer>, Integer, Map<String, Integer>> f2 = merge(f1);

        assertEquals(Map.of("5", 5,"a",1, "b", 2, "c", 3,"d",4),f2.apply(REF,5));

        CountingFunction<Integer, Integer> f3 = toCountingFunction(i -> i * 2);
        BiFunction<Map<String, Integer>, Integer, Map<String, Integer>> f4 = merge(f1, f3);
        assertEquals(Map.of("5", 10,"a",2, "b", 4, "c", 6,"d",8),f4.apply(REF,5));
        assertEquals(5, f3.count());

        CountingFunction<Integer, Integer> f5 = toCountingFunction(i -> i * 2);
        BiFunction<Map<String, Integer>, Integer, Map<String, Integer>> f6 = merge2(f1, f5);
        assertEquals(Map.of("5", 10,"a",2, "b", 4, "c", 6,"d",8),f6.apply(REF,5));
        assertEquals(1, f5.count());
    }

    public static <T,R,S> CountingBiFunction<T,R,S> toCountingBiFunction(BiFunction<T,R,S> f) {
        return new CountingBiFunction<>() {
            private int cnt;

            @Override
            public S apply(T t, R r) {
                cnt++;
                return f.apply(t,r);
            }

            @Override
            public int count() {
                return cnt;
            }
        };
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

    interface CountingBiFunction<T,R,S> extends BiFunction<T,R,S> {
        int count();
    }
}
