package com.github.dfauth.stream.dag;

import org.reactivestreams.Publisher;
import org.reactivestreams.Subscriber;
import org.reactivestreams.Subscription;

import java.util.Objects;
import java.util.Optional;

public abstract class BasePublisher<T> implements Publisher<T>, Subscription {

    protected Optional<Subscriber<? super T>> optSubscriber = Optional.empty();

    @Override
    public void subscribe(Subscriber<? super T> subscriber) {
        Objects.nonNull(subscriber);
        optSubscriber = Optional.of(subscriber);
        subscriber.onSubscribe(this);
        _onSubscribe(subscriber);
    }

    protected void _onSubscribe(Subscriber<? super T> subscriber) {
    }

    @Override
    public void request(long l) {
        // TODO
    }

    @Override
    public void cancel() {
        // TODO
    }
}
