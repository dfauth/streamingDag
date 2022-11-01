package com.github.dfauth.stream.dag;

import lombok.extern.slf4j.Slf4j;
import org.reactivestreams.Processor;
import org.reactivestreams.Subscriber;
import org.reactivestreams.Subscription;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicLong;
import java.util.function.Function;
import java.util.function.Predicate;

import static com.github.dfauth.function.Function2.peek;

@Slf4j
public class DynamicPartitioningPublisher<T,K> implements Processor<T,T> {

    private final Function<T, K> keyExtractor;
    private final Map<K, Subscriber<? super T>> subscribers = new HashMap<>();
    private final Map<K, SubscriptionImpl> subscriptions = new HashMap<>();
    private Subscription subscription;
    private final AtomicBoolean initialized = new AtomicBoolean(false);
    private final Function<K, Subscriber<T>> factory;
    private AtomicLong requested = new AtomicLong(0);

    protected DynamicPartitioningPublisher(Function<T, K> keyExtractor, Function<K, Subscriber<T>> factory) {
        this.keyExtractor = keyExtractor;
        this.factory = factory;
    }

    @Override
    public void onSubscribe(Subscription subscription) {
        this.subscription = subscription;
        init();
    }

    @Override
    public void onNext(T t) {
        K key = keyExtractor.apply(t);
        Subscriber<? super T> subscriber = subscribers.computeIfAbsent(key, factory.andThen(subscribeTo(key)));
        Optional.ofNullable(subscriber).filter(demandExists(key)).map(peek(s -> s.onNext(t))).orElseGet(peek(() -> log.warn("no capacity")));
        if(requested.decrementAndGet() <= 0) {
            request();
        }
    }

    private Predicate<? super Subscriber<? super T>> demandExists(K key) {
        return Optional.ofNullable(subscriptions.get(key)).map(SubscriptionImpl::decrement).orElse(_ignore -> false);
    }

    private Function<? super Subscriber<T>, ? extends Subscriber<? super T>> subscribeTo(K key) {
        return peek(s -> s.onSubscribe(subscriptions.computeIfAbsent(key, k -> new SubscriptionImpl(key))));
    }

    @Override
    public void onComplete() {
        subscribers.values().stream().forEach(Subscriber::onComplete);
    }

    @Override
    public void onError(Throwable throwable) {
        subscribers.values().stream().forEach(s -> s.onError(throwable));
    }

    @Override
    public void subscribe(Subscriber<? super T> subscriber) {
        if(subscribers.size() == 1) {
            init();
        }
    }

    protected synchronized void init() {
        request(1);
//        Optional.of(subscribers)
//                .filter(m -> m.size() > 0).flatMap(s -> Optional.ofNullable(subscription)).ifPresent(_s -> {
//                    if(!initialized.get()) {
//                        synchronized (this.initialized) {
//                            if (initialized.compareAndSet(false, true)) {
//                                request();
//                            }
//                        }
//                    }
//                });
    }

    private void request() {
        requested.set(Optional.ofNullable(subscription).map(this::minPossible).orElse(0l));
    }

    private void request(long l) {
        requested.set(Optional.ofNullable(subscription).map(s -> {
            s.request(l);
            return l;
        }).orElse(0l));
    }

    private long minPossible(Subscription s) {
        return subscriptions.values().stream().min((s1, s2) -> (int) (s1.remaining() - s2.remaining())).map(_s -> {
            long l = _s.remaining();
            s.request(l);
            return l;
        }).orElse(0l);
    }

    private class SubscriptionImpl implements Subscription {
        private final K key;
        private AtomicLong latch = new AtomicLong(0);

        public SubscriptionImpl(K key) {
            this.key = key;
        }

        public <T> Predicate<T> decrement() {
            return t -> latch.getAndDecrement() > 0;
        }

        @Override
        public void request(long l) {
            latch.set(l);
        }

        @Override
        public void cancel() {
            latch.set(0);

        }

        public long remaining() {
            return latch.get();
        }
    }
}
