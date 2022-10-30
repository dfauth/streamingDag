package com.github.dfauth.stream.dag;

import org.reactivestreams.Processor;
import org.reactivestreams.Subscriber;

import java.util.Optional;
import java.util.function.BiFunction;
import java.util.function.Function;
import java.util.stream.Stream;

public class PartitioningPublisher<T> extends AbstractBaseProcessor<T,T> implements Processor<T,T> {

    private final BiFunction<T, Integer, Integer> partitioner;
    private PartitioningSubscriber<? super T>[] subscribers;

    protected PartitioningPublisher(BiFunction<T,Integer,Integer> partitioner, int size) {
        super(Function.identity());
        this.partitioner = partitioner;
        this.subscribers = new PartitioningSubscriber[size];
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
        }
    }
}
