package com.github.dfauth.stream.dag;

import org.reactivestreams.Processor;
import org.reactivestreams.Subscriber;
import org.reactivestreams.Subscription;

import java.util.Objects;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.function.BiFunction;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class PartitioningPublisher<T> implements Processor<T,T> {

    private final BiFunction<T, Integer, Integer> partitioner;
    private PartitioningSubscriber<? super T>[] subscribers;
    private Subscription subscription;
    private final AtomicBoolean initialized = new AtomicBoolean(false);

    protected PartitioningPublisher(BiFunction<T,Integer,Integer> partitioner, int size) {
        this.partitioner = partitioner;
        this.subscribers = new PartitioningSubscriber[size];
    }

    @Override
    public void onSubscribe(Subscription subscription) {
        this.subscription = subscription;
        init();
    }

    @Override
    public void onNext(T t) {
        int index = partitioner.apply(t,subscribers.length);
        Optional.ofNullable(subscribers[index]).ifPresent(s -> s.onNext(t));
    }

    @Override
    public void onComplete() {
        Stream.of(subscribers).forEach(Subscriber::onComplete);
    }

    @Override
    public void onError(Throwable throwable) {
        Stream.of(subscribers).forEach(s -> s.onError(throwable));
    }

    @Override
    public void subscribe(Subscriber<? super T> subscriber) {
        if(subscriber instanceof PartitioningSubscriber) {
            PartitioningSubscriber<? super T> partitioningSubscriber = (PartitioningSubscriber<? super T>) subscriber;
            subscribers[partitioningSubscriber.partition()] = partitioningSubscriber;
            init();
        }
    }

    protected synchronized void init() {
        Optional.of(Stream.of(subscribers)
                        .filter(Objects::nonNull)
                        .collect(Collectors.toList())
                ).filter(l -> l.size() >= subscribers.length)
                .filter(_ignore -> subscription != null)
                .ifPresent(l -> Optional.ofNullable(subscription)
                        .ifPresent(_s -> {
                            synchronized (this.initialized) {
                                if(initialized.compareAndSet(false, true)) {
                                    l.forEach(s -> s.onSubscribe(_s));
                                }
                            }
                    }));
    }
}
