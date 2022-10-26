package com.github.dfauth.stream.dag;

import org.reactivestreams.Subscriber;
import org.reactivestreams.Subscription;

import java.util.Optional;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.BiFunction;
import java.util.function.Function;

import static com.github.dfauth.stream.dag.function.Function2.function2;

/**
 * A Consumer<T> who accepts a Function<T,R> and which also implements Function<T,Optional<R>>
 * as long as the consumer is not called, the function operation will always return Optional.empty()
 * once a function is defined, it will be applied to the input when apply is called abd returned wrapped in an Optional
 */
public class SubscriberFunction<T,R,S> implements Subscriber<T>, Function<R,Optional<S>>, Monitorable.VoidMonitorable, MonitorAware.VoidConsumer {

    private final AtomicReference<T> a = new AtomicReference<>();
    private final Function<T,Function<R,S>> f;
    private Subscription subscription;
    private final Monitor.VoidMonitor monitor = new Monitor.VoidMonitor();

    public SubscriberFunction(BiFunction<T,R,S> f) {
        this.f = function2(f).unwind();
    }

    public SubscriberFunction(Function<T,Function<R,S>> f) {
        this.f = f;
    }

    @Override
    public Optional<S> apply(R r) {
        return Optional.ofNullable(a.get()).map(t -> f.apply(t).apply(r));
    }

    @Override
    public void onSubscribe(Subscription subscription) {
        this.subscription = subscription;
        subscription.request(Integer.MAX_VALUE);
    }

    @Override
    public void onNext(T t) {
        a.set(t);
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
