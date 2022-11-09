package com.github.dfauth.stream.dag;

import org.reactivestreams.Publisher;
import org.reactivestreams.Subscriber;
import org.reactivestreams.Subscription;

public class RequestListener {

    public static <T> Publisher<T> requestListener(Publisher<T> p, Callback callback) {
        return subscriber -> p.subscribe(requestListener(subscriber, callback));
    }

    public static <T> Subscriber<? super T> requestListener(Subscriber<? super T> subscriber, Callback callback) {
        return new Subscriber<T>() {
            @Override
            public void onSubscribe(Subscription subscription) {
                subscriber.onSubscribe(requestListener(subscription, callback));
            }

            @Override
            public void onNext(T t) {
                subscriber.onNext(t);
            }

            @Override
            public void onError(Throwable throwable) {
                subscriber.onError(throwable);
            }

            @Override
            public void onComplete() {
                subscriber.onComplete();
            }
        };
    }

    private static Subscription requestListener(Subscription subscription, RequestListener.Callback callback) {
        return new Subscription() {
            @Override
            public void request(long l) {
                subscription.request(l);
                callback.request(l);
            }

            @Override
            public void cancel() {
                subscription.cancel();
                callback.cancel();
            }
        };
    }

    interface Callback extends Subscription {
        default void cancel() {
        }
    }
}
