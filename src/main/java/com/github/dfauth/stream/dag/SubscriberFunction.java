package com.github.dfauth.stream.dag;

import org.reactivestreams.Subscriber;
import org.reactivestreams.Subscription;

import java.util.Optional;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.Function;

public class SubscriberFunction<T,R> extends BaseSubscriber<Function<T,R>> implements Function<T,Optional<R>>, Subscriber<Function<T,R>> {

    private final int n;
    private final AtomicReference<Function<T,R>> ref = new AtomicReference<>();
    private AtomicInteger latch = new AtomicInteger(0);

    public SubscriberFunction() {
        this(16);
    }

    public SubscriberFunction(int n) {
        this.n = n;
    }

    @Override
    public Optional<R> apply(T t) {
        return Optional.ofNullable(ref.get()).map(f -> f.apply(t));
    }

    @Override
    protected void _onSubscribe(Subscription subscription) {
        checkLatch(latch.get());
    }

    @Override
    public void onNext(Function<T,R> f) {
        ref.set(f);
        checkLatch(this.latch.decrementAndGet());
    }

    @Override
    public void onError(Throwable throwable) {
        unset();
    }

    @Override
    public void onComplete() {
        unset();
    }

    private void checkLatch(int i) {
        if(i <= 0) {
            latch.set(n);
            optSubscription.ifPresent(s -> s.request(n));
        }
    }

    private void unset() {
        ref.set(null);
        optSubscription.ifPresent(s -> s.cancel());
    }
}
