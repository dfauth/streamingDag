package com.github.dfauth.stream.dag;

import lombok.extern.slf4j.Slf4j;
import org.reactivestreams.Publisher;
import org.reactivestreams.Subscription;

import java.util.concurrent.atomic.AtomicLong;
import java.util.stream.Stream;

@Slf4j
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
                public void request(long l) {
                    log.info("request({})",l);
                    AtomicLong i = new AtomicLong(l);
                    Stream.of(ts).filter(t -> i.decrementAndGet()>0).forEach(subscriber::onNext);
                    if(doComplete) {
                        subscriber.onComplete();
                    }
                }

                @Override
                public void cancel() {}
            });
        };
    }
}
