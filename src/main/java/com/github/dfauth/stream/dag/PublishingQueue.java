package com.github.dfauth.stream.dag;

import org.reactivestreams.Publisher;
import org.reactivestreams.Subscriber;
import org.reactivestreams.Subscription;

import java.util.*;

public class PublishingQueue<T> extends AbstractQueue<T> implements Publisher<T>, Queue<T> {

    private Optional<Subscriber<? super T>> optSub = Optional.empty();

    @Override
    public boolean offer(T t) {
        return optSub.map(s -> {
            s.onNext(t);
            return true;
        }).orElse(false);
    }

    @Override
    public T poll() {
        throw new UnsupportedOperationException("Oops messages are dequeued into a subscriber");
    }

    @Override
    public T peek() {
        throw new UnsupportedOperationException("Oops messages are dequeued into a subscriber");
    }

    @Override
    public void subscribe(Subscriber<? super T> subscriber) {
        optSub = Optional.ofNullable(subscriber);
        optSub.ifPresent(s -> s.onSubscribe(new Subscription() {
            @Override
            public void request(long l) {
            }

            @Override
            public void cancel() {
            }
        }));
    }

    @Override
    public Iterator<T> iterator() {
        return Collections.emptyIterator();
    }

    @Override
    public int size() {
        return 0;
    }

    public void stop() {
        optSub.ifPresent(Subscriber::onComplete);
    }
}
