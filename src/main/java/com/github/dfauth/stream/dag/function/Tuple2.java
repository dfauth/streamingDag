package com.github.dfauth.stream.dag.function;

import java.util.Map;
import java.util.function.BiConsumer;
import java.util.function.BiFunction;
import java.util.function.Function;

import static com.github.dfauth.stream.dag.function.Function2.uncurry;
import static java.util.function.Function.identity;

public interface Tuple2<T1, T2> {

    static <T1,T2> Function<T1, Tuple2<T1, T2>> tuplize(Function<T1,T2> f) {
        return t1 -> tuple2(t1, f.apply(t1));
    }

    static <T1,T2> Function<T1, Map.Entry<T1, T2>> asMapEntry(Function<T1,T2> f) {
        return t1 -> tuple2(t1, f.apply(t1)).toMapEntry();
    }

    static <T1,T2, T3> BiFunction<T1, T2, Map.Entry<T1, T3>> asMapEntry(BiFunction<T1,T2,T3> f) {
        return (t1, t2) -> {
            T3 t3 = f.apply(t1, t2);
            return tuple2(t1, t3).toMapEntry();
        };
    }

    private static <V, K, T> BiFunction<K,V,Map.Entry<K,T>> adapt(Function<Map.Entry<K, V>, Map.Entry<K, T>> f) {
        return (k,v) -> f.apply(Tuple2.tuple2(k, v).toMapEntry());
    }

    static <K, V, T> Function<Map.Entry<K,V>, Map.Entry<K,T>> adapt(BiFunction<K, V, Map.Entry<K, T>> f) {
        return e -> f.apply(e.getKey(), e.getValue());
    }

    static <K, V, T> BiFunction<K,V,Tuple2<K, T>> of(BiFunction<K,V,T> f) {
        return (k,v) -> tuple2(k, f.apply(k,v));
    }

    static <K, V> Tuple2<K, V> tuple2(Map.Entry<K,V> e) {
        return tuple2(e.getKey(), e.getValue());
    }

    static <T1,T2> Function<T2,Tuple2<T1, T2>> partialTuple2(T1 t1) {
        return t2 -> Tuple2.tuple2(t1,t2);
    }

    static <T1,T2> Tuple2<T1, T2> tuple2(T1 t1, T2 t2) {
        return new Tuple2<>() {
            @Override
            public T1 _1() {
                return t1;
            }

            @Override
            public T2 _2() {
                return t2;
            }

            @Override
            public int hashCode() {
                return _1().hashCode() ^ _2().hashCode();
            }

            @Override
            public boolean equals(Object obj) {
                if(obj == this) {
                    return true;
                } else if(obj == null) {
                    return false;
                } else if(obj instanceof Tuple2) {
                    Tuple2<T1, T2> other = (Tuple2<T1, T2>) obj;
                    return other.map((t1,t2) -> t1.equals(_1()) && t2.equals(_2()));
                } else {
                    return false;
                }
            }

            @Override
            public String toString() {
                return String.format("Tuple2(%s, %s)",_1(),_2());
            }
        };
    }

    static <T1,T2> Map.Entry<T1, T2> asMapEntry(T1 t1, T2 t2) {
        return tuple2(t1, t2).toMapEntry();
    }

    T1 _1();

    T2 _2();

    default <T> T map(BiFunction<T1,T2,T> f) {
        return f.apply(_1(), _2());
    }

    default <R1,R2> Tuple2<R1,R2> map(Function<T1,R1> f1, Function<T2,R2> f2) {
        return Tuple2.tuple2(f1.apply(_1()),f2.apply(_2()));
    }

    default <R1> Tuple2<R1,T2> mapLeft(Function<T1,R1> f) {
        return map(f,identity());
    }

    default <R2> Tuple2<T1,R2> mapRight(Function<T2,R2> f) {
        return map(identity(),f);
    }

    default <T> T map(Function<T1,Function<T2,T>> f) {
        return map(uncurry(f));
    }

    default Map.Entry<T1,T2> toMapEntry() {
        return new Map.Entry<>(){
            @Override
            public T1 getKey() {
                return _1();
            }

            @Override
            public T2 getValue() {
                return _2();
            }

            @Override
            public T2 setValue(T2 value) {
                throw new UnsupportedOperationException();
            }
        };
    }

    default void forEach(BiConsumer<T1,T2> c) {
        c.accept(_1(), _2());
    }
}
