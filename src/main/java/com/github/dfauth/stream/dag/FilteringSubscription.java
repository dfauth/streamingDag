package com.github.dfauth.stream.dag;

import org.reactivestreams.Subscriber;
import org.reactivestreams.Subscription;

import java.util.Optional;
import java.util.concurrent.atomic.AtomicLong;
import java.util.function.Predicate;

public class FilteringSubscription<T> implements Subscription, Predicate<T> {

    private AtomicLong count = new AtomicLong(0l);
    private Subscriber<? super T> subscriber;

    @Override
    public void request(long l) {
        count.set(l);
    }

    @Override
    public void cancel() {
        count.set(0);
        Optional.ofNullable(subscriber).ifPresent(Subscriber::onComplete);
    }

    @Override
    public boolean test(T t) {
        return count.get() > 0;
    }

    public void commit(T ignored) {
        count.decrementAndGet();
    }

    public FilteringSubscription<T> withSubscriber(Subscriber<? super T> subscriber) {
        this.subscriber = subscriber;
        return this;
    }
}
