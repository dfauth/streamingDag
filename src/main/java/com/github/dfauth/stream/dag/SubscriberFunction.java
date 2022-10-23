package com.github.dfauth.stream.dag;

import org.reactivestreams.Subscriber;
import org.reactivestreams.Subscription;

import java.util.Optional;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.Function;

/**
 * A Consumer<T> who accepts a Function<T,R> and which also implements Function<T,Optional<R>>
 * as long as the consumer is not called, the function operation will always return Optional.empty()
 * once a function is defined, it will be applied to the input when apply is called abd returned wrapped in an Optional
 */
public class SubscriberFunction<T,R> implements Subscriber<Function<T,R>>, Function<T,Optional<R>>, Monitorable.VoidMonitorable, MonitorAware.VoidConsumer {

    private final AtomicReference<Function<T, R>> fn = new AtomicReference<>();
    private Subscription subscription;
    private final Monitor.VoidMonitor monitor = new Monitor.VoidMonitor();

    @Override
    public Optional<R> apply(T t) {
        return Optional.ofNullable(fn.get()).map(_f -> _f.apply(t));
    }

    @Override
    public void onSubscribe(Subscription subscription) {
        this.subscription = subscription;
        subscription.request(Integer.MAX_VALUE);
    }

    @Override
    public void onNext(Function<T, R> f) {
        fn.set(f);
    }

    @Override
    public void onError(Throwable t) {
        Optional.ofNullable(subscription).ifPresent(Subscription::cancel);
        monitor.completeExceptionally(t);
    }

    @Override
    public void onComplete() {
        Optional.ofNullable(subscription).ifPresent(Subscription::cancel);
        monitor.complete();
    }

    public Monitor.VoidMonitor monitor() {
        return monitor;
    }

    @Override
    public void _onFailure(Throwable t) {
        onError(t);
    }

    @Override
    public void _onComplete() {
        onComplete();
    }
}
