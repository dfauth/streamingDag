package com.github.dfauth.stream.dag;

import org.reactivestreams.Subscriber;
import org.reactivestreams.Subscription;

import java.util.List;
import java.util.Queue;
import java.util.concurrent.CompletableFuture;

public abstract class CompletableFutureSubscriber<T, R> implements Subscriber<T> {
    private final CompletableFuture<R> f;
    protected final R payload;

    public CompletableFutureSubscriber(CompletableFuture<R> f, R payload) {
        this.f = f;
        this.payload = payload;
    }

    public static <T> Subscriber<T> subscribingFuture(CompletableFuture<T> f) {
        return new CompletableFutureSubscriber<>(f, null) {
            @Override
            public void onNext(T t) {
                f.complete(t);
            }

        };
    }

    public static <T> Subscriber<T> subscribingList(CompletableFuture<List<T>> f, List<T> l) {
        return new CompletableFutureSubscriber<>(f, l) {
            @Override
            public void onNext(T t) {
                l.add(t);
            }
        };
    }

    public static <T> Subscriber<T> subscribingQueue(CompletableFuture<Queue<T>> f, Queue<T> l) {
        return new CompletableFutureSubscriber<>(f, l) {
            @Override
            public void onNext(T t) {
                l.offer(t);
            }
        };
    }

    @Override
    public void onSubscribe(Subscription subscription) {
        subscription.request(Long.MAX_VALUE);
    }

    @Override
    public abstract void onNext(T t);

    @Override
    public void onError(Throwable throwable) {
        f.completeExceptionally(throwable);
    }

    @Override
    public void onComplete() {
        f.complete(payload);
    }
}
