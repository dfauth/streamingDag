package com.github.dfauth.stream.dag;

import java.util.function.Consumer;

public class ConsumingSubscriber<T> extends AbstractSubscriber<T> {

    public static <T> ConsumingSubscriber<T> subscribe(Consumer consumer) {
        return new ConsumingSubscriber<>(consumer);
    }

    private final Consumer<T> consumer;

    public ConsumingSubscriber(Consumer<T> consumer) {
        this.consumer = consumer;
    }

    @Override
    public void onNext(T t) {
        consumer.accept(t);
    }

    @Override
    protected void init() {
        subscription().ifPresent(s -> s.request(Integer.MAX_VALUE));
    }
}
