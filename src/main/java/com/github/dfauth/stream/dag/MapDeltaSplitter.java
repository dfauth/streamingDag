package com.github.dfauth.stream.dag;

import com.google.common.collect.MapDifference;
import com.google.common.collect.Maps;
import org.reactivestreams.Subscriber;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.Executor;
import java.util.concurrent.ForkJoinPool;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.Function;

import static java.util.function.Predicate.not;

public class MapDeltaSplitter<K,V> extends AbstractBaseProcessor<Map<K,V>, Map<K,V>> {

    private final AtomicReference<Map<K,V>> previous = new AtomicReference<>(new HashMap<>());
    private Subscriber<? super Map<K, V>> modifiedSubscriber;
    private Subscriber<? super Map<K, V>> removedSubscriber;
    private final Executor executor;

    protected MapDeltaSplitter() {
        this(ForkJoinPool.commonPool());
    }

    protected MapDeltaSplitter(Executor executor) {
        super(Function.identity());
        this.executor = executor;
    }

    @Override
    public void onNext(Map<K, V> kvMap) {

        MapDifference<K, V> difference = Maps.difference(kvMap, previous.getAndSet(kvMap));

        // modified entries
        Map<K, V> modified = com.github.dfauth.stream.dag.function.Maps.map(difference.entriesDiffering(), (k, v) -> v.leftValue());

        // merge with new entries
        Optional.of(com.github.dfauth.stream.dag.function.Maps.merge(modified, difference.entriesOnlyOnLeft()))
                .filter(not(Map::isEmpty))
                .ifPresent(left -> Optional.ofNullable(modifiedSubscriber)
                        .ifPresent(s -> executor.execute(() -> s.onNext(left))));

        // unchanged entries
        Map<K, V> common = difference.entriesInCommon();
        if(common.size() > 0) {
            super.onNext(common);
        }

        // removed entries
        Optional.of(difference.entriesOnlyOnRight())
                .filter(not(Map::isEmpty))
                .ifPresent(removed -> Optional.ofNullable(removedSubscriber)
                        .ifPresent(s -> executor.execute(() -> s.onNext(removed))));

    }

    public void subscribeModified(Subscriber<? super Map<K, V>> subscriber) {
        modifiedSubscriber = subscriber;
    }

    public void subscribeRemoved(Subscriber<? super Map<K, V>> subscriber) {
        removedSubscriber = subscriber;
    }
}
