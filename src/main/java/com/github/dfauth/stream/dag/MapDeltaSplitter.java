package com.github.dfauth.stream.dag;

import com.github.dfauth.stream.dag.function.Tuple3;
import com.google.common.collect.MapDifference;
import com.google.common.collect.Maps;
import org.reactivestreams.Publisher;
import org.reactivestreams.Subscriber;
import reactor.core.publisher.Flux;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.Executor;
import java.util.concurrent.ForkJoinPool;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.Function;

import static com.github.dfauth.function.Function2.peek;
import static com.github.dfauth.stream.dag.function.Tuple3.tuple3;
import static java.util.function.Predicate.not;

public class MapDeltaSplitter<K,V> implements Function<Publisher<Map<K,V>>, Tuple3<Publisher<Map<K,V>>, Publisher<Map<K,V>>, Publisher<Map<K,V>>>> {

    private final AtomicReference<Map<K,V>> previous = new AtomicReference<>(new HashMap<>());
    private final Executor executor;
    private Subscriber<? super Map<K, V>> modifiedSubscriber;
    private FilteringSubscription<Map<K, V>> modifiedSubscription = new FilteringSubscription<>();
    private Subscriber<? super Map<K, V>> unchangedSubscriber;
    private FilteringSubscription<Map<K, V>> unchangedSubscription = new FilteringSubscription<>();
    private Subscriber<? super Map<K, V>> removedSubscriber;
    private FilteringSubscription<Map<K, V>> removedSubscription = new FilteringSubscription<>();

    protected MapDeltaSplitter() {
        this(ForkJoinPool.commonPool());
    }

    protected MapDeltaSplitter(Executor executor) {
        this.executor = executor;
    }

    @Override
    public Tuple3<Publisher<Map<K, V>>, Publisher<Map<K, V>>, Publisher<Map<K, V>>> apply(Publisher<Map<K, V>> publisher) {

        Flux.from(publisher).subscribe(m -> {
            MapDifference<K, V> difference = Maps.difference(m, previous.getAndSet(m));

            // modified entries
            Map<K, V> modified = com.github.dfauth.stream.dag.function.Maps.map(difference.entriesDiffering(), (k, v) -> v.leftValue());

            // merge with new entries
            Optional.of(com.github.dfauth.stream.dag.function.Maps.merge(modified, difference.entriesOnlyOnLeft()))
                    .filter(not(Map::isEmpty))
                    .filter(modifiedSubscription)
                    .map(peek(next -> Optional.ofNullable(modifiedSubscriber)
                            .ifPresent(s -> executor.execute(() -> s.onNext(next)))))
                    .ifPresent(modifiedSubscription::commit);

            // unchanged entries
            Optional.of(difference.entriesInCommon())
                    .filter(not(Map::isEmpty))
                    .filter(unchangedSubscription)
                    .map(peek(next -> Optional.ofNullable(unchangedSubscriber)
                            .ifPresent(s -> executor.execute(() -> s.onNext(next)))))
                    .ifPresent(unchangedSubscription::commit);

            // removed entries
            Optional.of(difference.entriesOnlyOnRight())
                    .filter(not(Map::isEmpty))
                    .filter(removedSubscription)
                    .map(peek(next -> Optional.ofNullable(removedSubscriber)
                            .ifPresent(s -> executor.execute(() -> s.onNext(next)))))
                    .ifPresent(removedSubscription::commit);
        });


        return tuple3(subscriber -> {
                    modifiedSubscriber = subscriber;
                    subscriber.onSubscribe(modifiedSubscription.withSubscriber(modifiedSubscriber));
            }, subscriber -> {
                    unchangedSubscriber = subscriber;
                    subscriber.onSubscribe(unchangedSubscription.withSubscriber(unchangedSubscriber));
            }, subscriber -> {
                    removedSubscriber = subscriber;
                    subscriber.onSubscribe(removedSubscription.withSubscriber(removedSubscriber));
            }
        );
    }
}
