package com.github.dfauth.stream.dag;

import org.reactivestreams.Publisher;
import org.reactivestreams.Subscriber;
import org.reactivestreams.Subscription;

import java.util.Optional;

public abstract class AbstractPublisher<T> implements Publisher<T>, Subscription {

    private Subscriber<? super T> subscriber;

    @Override
    public void subscribe(Subscriber<? super T> subscriber) {
        this.subscriber = subscriber;
        subscriber.onSubscribe(this);
    }

    @Override
    public void request(long l) {
    }

    @Override
    public void cancel() {
    }

    protected Optional<Subscriber<? super T>> subscriber() {
        return Optional.ofNullable(subscriber);
    }
}
