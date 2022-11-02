package com.github.dfauth.stream.dag;

import com.github.dfauth.stream.dag.function.Maps;
import org.reactivestreams.Publisher;
import reactor.core.publisher.Flux;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.BiFunction;

import static java.util.Map.entry;

public class MapDeltaCombiner<K,V> implements BiFunction<Publisher<Map.Entry<K,V>>, Publisher<Map<K,V>>, Publisher<Map<K,V>>> {

    private final AtomicReference<Map.Entry<K,V>> leftPrevious = new AtomicReference<>(null);
    private final AbstractBaseProcessor<Map.Entry<K, V>, Map<K,V>> leftProcessor;
    private final AtomicReference<Map<K,V>> rightPrevious = new AtomicReference<>(new HashMap<>());
    private final AbstractBaseProcessor<Map<K, V>, Map<K,V>> rightProcessor;

    public MapDeltaCombiner() {
        leftProcessor = new AbstractBaseProcessor<>(e -> {
            Map.Entry<K,V> e1 = entry(e.getKey(), e.getValue());
            Map<K,V> m1 = Maps.mergeEntry(rightPrevious.get(), e1.getKey(), e1.getValue());
            rightPrevious.set(m1);
            return m1;
        }){};
        rightProcessor = new AbstractBaseProcessor<>(m -> {
            Map<K,V> m1 = Maps.map(m, (k, v) -> v);
            rightPrevious.set(m1);
            return m1;
        }){};
    }

    @Override
    public Publisher<Map<K,V>> apply(Publisher<Map.Entry<K,V>> leftPublisher, Publisher<Map<K,V>> rightPublisher) {
        AbstractBaseProcessor<Map.Entry<K, V>, Map<K,V>> left = Flux.from(leftPublisher).filter(e -> {
            return !e.equals(leftPrevious.getAndSet(e));
        }).subscribeWith(leftProcessor);

        AbstractBaseProcessor<Map<K, V>, Map<K,V>> right = Flux.from(rightPublisher).subscribeWith(rightProcessor);
        return Flux.from(left).mergeWith(right);
    }
}
