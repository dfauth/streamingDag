package com.github.dfauth.stream.dag;

import org.reactivestreams.Processor;
import org.reactivestreams.Subscriber;
import org.reactivestreams.Subscription;

import java.util.Optional;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.function.Function;

public abstract class AbstractBaseProcessor<T,R> implements Processor<T,R> {

    private Subscriber<? super R> subscriber;
    protected Subscription subscription;
    protected Function<T, R> f;
    private final AtomicBoolean initialized = new AtomicBoolean(false);

    protected AbstractBaseProcessor(Function<T, R> f) {
        this.f = f;
    }

    @Override
    public void subscribe(Subscriber<? super R> subscriber) {
        this.subscriber = subscriber;
        init();
    }

    protected synchronized void init() {
        Optional.ofNullable(subscriber)
                .flatMap(s -> Optional.ofNullable(subscription))
                .ifPresent(_ignored -> {
                    synchronized (this.initialized) {
                        if(initialized.compareAndSet(false, true)) {
                            subscriber.onSubscribe(subscription());
                        }
                    }
                });
    }

    protected Subscription subscription() {
        return subscription;
    }

    @Override
    public void onSubscribe(Subscription subscription) {
        this.subscription = subscription;
        init();
    }

    @Override
    public void onNext(T t) {
        Optional.ofNullable(subscriber)
                .ifPresent(_ignored -> Optional.ofNullable(t)
                .map(f).ifPresent(subscriber::onNext));
    }

    @Override
    public void onError(Throwable throwable) {
        Optional.ofNullable(subscriber).flatMap(_ignored -> Optional.ofNullable(throwable)).ifPresent(subscriber::onError);
    }

    @Override
    public void onComplete() {
        Optional.ofNullable(subscriber).ifPresent(_ignored -> subscriber.onComplete());
    }
}
