package com.github.dfauth.stream.dag;

import java.util.Optional;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.Consumer;
import java.util.function.Function;

/**
 * A Consumer<T> who accepts a Function<T,R> and which also implements Function<T,Optional<R>>
 * as long as the consumer is not called, the function operation will always return Optional.empty()
 * once a function is defined, it will be applied to the input when apply is called abd returned wrapped in an Optional
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
