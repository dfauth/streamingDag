package com.github.dfauth.stream.dag;

import org.reactivestreams.Processor;
import org.reactivestreams.Subscriber;
import org.reactivestreams.Subscription;

import java.util.Optional;
import java.util.function.Function;

import static java.util.Objects.nonNull;

public abstract class BaseProcessor<I,O> implements Processor<I,O> {

    private final Function<I,O> f;
    private Optional<Subscriber<? super O>> optSubscriber = Optional.empty();
    private Optional<Subscription> optSubscription = Optional.empty();

    public static <T> Processor<T,T> identity() {
        return new BaseProcessor<>(Function.<T>identity()){};
    }

    protected BaseProcessor(Function<I, O> f) {
        this.f = f;
    }

    @Override
    public void onSubscribe(Subscription subscription) {
        nonNull(subscription);
        Optional<Subscriber<? super O>> tmp;
        synchronized (this) {
            optSubscription = Optional.of(subscription);
            tmp = optSubscriber;
        }
        tmp.ifPresent(s -> s.onSubscribe(subscription));
    }

    @Override
    public void subscribe(Subscriber<? super O> subscriber) {
        nonNull(subscriber);
        Optional<Subscription> tmp;
        synchronized (this) {
            optSubscriber = Optional.of(subscriber);
            tmp = optSubscription;
        }
        tmp.ifPresent(s -> subscriber.onSubscribe(s));
    }

    @Override
    public void onNext(I i) {
        optSubscriber.ifPresent(s -> s.onNext(f.apply(i)));
    }

    @Override
    public void onError(Throwable t) {
        optSubscriber.ifPresent(s -> s.onError(t));
    }

    @Override
    public void onComplete() {
        optSubscriber.ifPresent(s -> s.onComplete());
    }
}
