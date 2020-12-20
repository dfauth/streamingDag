package com.github.dfauth.stream.dag;

import org.reactivestreams.Subscriber;
import org.reactivestreams.Subscription;

import java.util.Optional;

import static java.util.Objects.nonNull;

public abstract class BaseSubscriber<T> implements Subscriber<T> {

    protected Optional<Subscription> optSubscription = Optional.empty();

    @Override
    public void onSubscribe(Subscription subscription) {
        nonNull(subscription);
        optSubscription = Optional.of(subscription);
        _onSubscribe(subscription);
    }

    protected abstract void _onSubscribe(Subscription subscription);

    @Override
    public void onError(Throwable throwable) {

    }

    @Override
    public void onComplete() {

    }
}
