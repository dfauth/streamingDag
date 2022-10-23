package com.github.dfauth.stream.dag;

import lombok.extern.slf4j.Slf4j;
import org.reactivestreams.Processor;
import org.reactivestreams.Publisher;
import org.reactivestreams.Subscriber;
import org.reactivestreams.Subscription;

import java.util.Optional;
import java.util.concurrent.atomic.AtomicBoolean;

@Slf4j
public class KillSwitch<T> implements Processor<T, T>, AutoCloseable, MonitorAware.VoidConsumer, Monitorable.VoidMonitorable, Subscription {

    public static <T> KillSwitch<T> killSwitch(Subscriber<T> subscriber) {
        KillSwitch<T> k = new KillSwitch<>();
        k.subscribe(subscriber);
        return k;
    }

    public static <T> KillSwitch<T> killSwitch(Subscriber<T> subscriber, VoidMonitorable monitorable) {
        KillSwitch<T> k = killSwitch(subscriber);
        k.handle(monitorable);
        return k;
    }

    public static <T> KillSwitch<T> killSwitch(Publisher<T> publisher) {
        KillSwitch<T> k = new KillSwitch<>();
        publisher.subscribe(k);
        return k;
    }

    public static <T> KillSwitch<T> killSwitch(Publisher<T> publisher, VoidMonitorable monitorable) {
        KillSwitch<T> k = killSwitch(publisher);
        k.handle(monitorable);
        return k;
    }

    private Subscriber<? super T> subscriber = null;
    private Subscription subscription;
    private final AtomicBoolean initialised = new AtomicBoolean(false);
    private final Monitor.VoidMonitor monitor = new Monitor.VoidMonitor();

    @Override
    public void subscribe(Subscriber<? super T> subscriber) {
        this.subscriber = subscriber;
        initialise();
    }

    @Override
    public void onSubscribe(Subscription subscription) {
        this.subscription = subscription;
        initialise();
    }

    private void initialise() {
        if(!initialised.get()) {
            Optional.ofNullable(subscription).flatMap(s -> Optional.ofNullable(subscriber)).ifPresent(_s -> {
                synchronized (initialised) {
                    if (!initialised.get()) {
                        _s.onSubscribe(this);
                        initialised.set(true);
                    }
                }
            });
        }
    }

    @Override
    public void onNext(T t) {
        Optional.ofNullable(subscriber).ifPresent(s -> s.onNext(t));
    }

    @Override
    public void onError(Throwable t) {
        close(t);
    }

    @Override
    public void _onFailure(Throwable t) {

    }

    @Override
    public void onComplete() {
        close();
    }

    @Override
    public void _onComplete() {
        close();
    }

    @Override
    public void close() {
        Optional.ofNullable(subscriber).ifPresent(Subscriber::onComplete);
        Optional.ofNullable(subscription).ifPresent(Subscription::cancel);
        monitor.complete();
    }

    public void close(Throwable t) {
        Optional.ofNullable(subscriber).ifPresent(s -> s.onError(t));
        Optional.ofNullable(subscription).ifPresent(Subscription::cancel);
        monitor.completeExceptionally(t);
    }

    public void kill() {
        close();
    }

    @Override
    public Monitor.VoidMonitor monitor() {
        return monitor;
    }

    @Override
    public void request(long l) {
        Optional.ofNullable(subscription).ifPresent(s -> s.request(l));
    }

    @Override
    public void cancel() {
        Optional.ofNullable(subscription).ifPresent(Subscription::cancel);
        close();
    }
}
