package com.github.dfauth.stream.dag;

import com.github.dfauth.function.Function2;
import org.reactivestreams.Publisher;
import org.reactivestreams.Subscriber;
import org.reactivestreams.Subscription;

import java.util.*;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicLong;

public class PublishingQueue<T> extends AbstractQueue<T> implements Publisher<T>, Queue<T> {

    private Subscriber<? super T> subscriber = null;
    private AtomicBoolean completed = new AtomicBoolean(false);
    private AtomicLong countdown = new AtomicLong(0);

    @Override
    public boolean offer(T t) {
        return Optional.ofNullable(subscriber).filter(this::pending).map(s -> {
            s.onNext(t);
            countdown.decrementAndGet();
            return true;
        }).orElse(false);
    }

    private boolean pending(Subscriber<? super T> subscriber) {
        return countdown.get()>0;
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
        this.subscriber = subscriber;
        Optional.ofNullable(this.subscriber).ifPresent(s -> s.onSubscribe(new Subscription() {
            @Override
            public void request(long l) {
                countdown.set(l);
            }

            @Override
            public void cancel() {
                countdown.set(0);
                stop();
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
        completed.set(Optional.ofNullable(this.subscriber).map(Function2.peek(Subscriber::onComplete)).isPresent());
    }

    public boolean isStopped() {
        return completed.get();
    }
}
