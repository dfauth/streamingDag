package com.github.dfauth.stream.dag;

import com.github.dfauth.stream.dag.BaseSubscriber;
import org.reactivestreams.Subscription;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.function.Consumer;

public class TestingSubscriber<T> extends BaseSubscriber<T> {

    private CompletableFuture<List<T>> f = new CompletableFuture<>();
    private List<T> results = new ArrayList<>();
    private final Consumer<T> consumer;

    public TestingSubscriber() {
        this(t -> {});
    }

    public TestingSubscriber(Consumer<T> consumer) {
        this.consumer = consumer;
    }

    @Override
    protected void _onSubscribe(Subscription subscription) {
        subscription.request(Long.MAX_VALUE);
    }

    @Override
    public void onNext(T t) {
        results.add(t);
        consumer.accept(t);
    }

    @Override
    public void onComplete() {
        f.complete(results);
    }

    @Override
    public void onError(Throwable throwable) {
        f.completeExceptionally(throwable);
    }

    public CompletableFuture<List<T>> toCompletableFuture() {
        return f;
    }
}
