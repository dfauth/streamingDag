package com.github.dfauth.stream.dag;

import java.util.Optional;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.Consumer;
import java.util.function.Function;

/**
 * A Subscriber<T> whose payload is a Function<T,R> and which also implements Function<T,Optional<R>>
 * This is used to implement a Function applied to a stream whose implementation can change - by partially applying streamed values (from another stream)
 * to a function
 */
public class SubscriberFunction<T,R> implements Consumer<Function<T,R>>, Function<T,Optional<R>> {

    private AtomicReference<Function<T, R>> fn = new AtomicReference<>();

    @Override
    public void accept(Function<T, R> f) {
        fn.set(f);
    }

    @Override
    public Optional<R> apply(T t) {
        return Optional.ofNullable(fn.get()).map(_f -> _f.apply(t));
    }
}
