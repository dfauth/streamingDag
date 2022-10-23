package com.github.dfauth.stream.dag;

import org.reactivestreams.Publisher;
import org.reactivestreams.Subscription;

import java.util.stream.Stream;

public class NonCompletingPublisher<T> {

    public static <T> Publisher<T> supplyAndComplete(T... ts) {
        return supply(true, ts);
    }

    public static <T> Publisher<T> supply(T... ts) {
        return supply(false, ts);
    }

    public static <T> Publisher<T> supply(boolean doComplete, T... ts) {
        return subscriber -> {
            subscriber.onSubscribe(new Subscription() {
                @Override
                public void request(long l) {}

                @Override
                public void cancel() {}
            });
            Stream.of(ts).forEach(subscriber::onNext);
            if(doComplete) {
                subscriber.onComplete();
            }
        };
    }
}
