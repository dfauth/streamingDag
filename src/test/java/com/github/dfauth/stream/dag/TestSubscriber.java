package com.github.dfauth.stream.dag;

import com.github.dfauth.trycatch.Try;
import lombok.extern.slf4j.Slf4j;
import org.reactivestreams.Subscriber;
import org.reactivestreams.Subscription;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.atomic.AtomicReference;

import static com.github.dfauth.trycatch.Try.failure;
import static com.github.dfauth.trycatch.Try.success;

@Slf4j
public class TestSubscriber<T> implements Subscriber<T> {

    private final AtomicReference<T> ref = new AtomicReference<>();
    private final CompletableFuture<Try<T>> monitor = new CompletableFuture<>();
    private Subscription subscription;

    @Override
    public void onSubscribe(Subscription subscription) {
        this.subscription = subscription;
        subscription.request(Integer.MAX_VALUE);
    }

    @Override
    public void onNext(T t) {
        ref.set(t);
    }

    @Override
    public void onError(Throwable t) {
        monitor.complete(failure(t));
    }

    @Override
    public void onComplete() {
        monitor.complete(success(ref.get()));
    }

    public boolean isEmpty() {
        return ref.get() == null;
    }

    public T get() {
        return ref.get();
    }

    public CompletableFuture<Try<T>> monitor() {
        return monitor;
    }

    public void stop() {
        subscription.cancel();
    }
}
