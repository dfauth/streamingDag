package com.github.dfauth.stream.dag;

import lombok.extern.slf4j.Slf4j;
import org.reactivestreams.Subscriber;
import org.reactivestreams.Subscription;

import java.util.function.Consumer;

@Slf4j
public class Subscribers {

    public static <T> Subscriber<? super T> fromConsumer(Consumer<T> consumer) {
        return new SubscriberImpl<T>() {
            @Override
            public void onNext(T t) {
                consumer.accept(t);
            }
        };
    }

    private static abstract class SubscriberImpl<T> implements Subscriber<T> {
        @Override
        public void onSubscribe(Subscription subscription) {
            subscription.request(Long.MAX_VALUE);
        }

        @Override
        public void onError(Throwable throwable) {
            log.error(throwable.getMessage(), throwable);
        }

        @Override
        public void onComplete() {

        }
    }
}
