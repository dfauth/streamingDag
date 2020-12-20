package com.github.dfauth.stream.dag;

import org.reactivestreams.Publisher;
import org.reactivestreams.Subscriber;
import org.reactivestreams.Subscription;
import reactor.core.publisher.Flux;

import java.util.Objects;
import java.util.Optional;
import java.util.function.Function;
import java.util.function.UnaryOperator;

import static com.github.dfauth.trycatch.AsyncUtil.executeAsync;

class Node<T,R> implements Publisher<R>, Subscription {

    private final Function<T,R> f;
    private Optional<R> payload = Optional.empty();
    private Optional<Subscriber<? super R>> optSubscriber = Optional.empty();

    public static <T> Node<T,T> identity() {
        return of(UnaryOperator.<T>identity());
    }

    public static <T,R> Node<T,R> of(Function<T,R> f) {
        return new Node<>(f);
    }

    private Node(Function<T,R> f) {
        this.f = f;
    }

    public Node<T, R> update(T t) {
        this.payload = Optional.of(f.apply(t));
        executeAsync(() -> {
            optSubscriber.ifPresent(s -> this.payload.ifPresent(p -> s.onNext(p)));
        });
        return this;
    }

    @Override
    public void subscribe(Subscriber<? super R> subscriber) {
        Objects.nonNull(subscriber);
        optSubscriber = Optional.of(subscriber);
        subscriber.onSubscribe(this);
    }

    @Override
    public void request(long l) {
        // TODO
    }

    @Override
    public void cancel() {
        // TODO
    }

    public <S> Node<R,S> curry(Function<R,S> f) {
        Node<R, S> node = Node.of(f);
        Flux.from(this).subscribe(r -> node.update(r));
        return node;
    }

    public void stop() {
        optSubscriber.ifPresent(s -> s.onComplete());
    }
}
