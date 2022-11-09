package com.github.dfauth.stream.dag;

import org.reactivestreams.Subscriber;
import org.reactivestreams.Subscription;

import java.util.Optional;

public abstract class AbstractSubscriber<T> implements Subscriber<T> {

    private Subscription subscription;

    @Override
    public void onSubscribe(Subscription subscription) {
        this.subscription = subscription;
        init();
    }

    protected void init() {
    }

    @Override
    public void onError(Throwable throwable) {
        subscription().ifPresent(Subscription::cancel);
    }

    @Override
    public void onComplete() {
        subscription().ifPresent(Subscription::cancel);
    }

    protected Optional<Subscription> subscription() {
        return Optional.ofNullable(subscription);
    }

}
