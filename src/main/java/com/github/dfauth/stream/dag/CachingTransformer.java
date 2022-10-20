package com.github.dfauth.stream.dag;

import lombok.extern.slf4j.Slf4j;
import org.reactivestreams.Publisher;
import reactor.core.publisher.Flux;

import java.util.function.BiFunction;
import java.util.function.Function;

import static com.github.dfauth.function.Function2.asFunction2;
import static com.github.dfauth.function.Function2.uncurry;

@Slf4j
public class CachingTransformer<T,S,R> implements BiFunction<Publisher<T>, Publisher<R>, Publisher<S>> {

    private final OneSidedCachingTransformer<T, R, S> leftTransformer;
    private final OneSidedCachingTransformer<R, T, S> rightTransformer;

    public CachingTransformer(BiFunction<T, R, S> f) {
        this(asFunction2(f).curried());
    }

    public CachingTransformer(Function<T, Function<R, S>> f) {
        this.leftTransformer = new OneSidedCachingTransformer<>(f);
        this.rightTransformer = new OneSidedCachingTransformer<>(uncurry(f).curriedRight());
    }

    @Override
    public Publisher<S> apply(Publisher<T> left, Publisher<R> right) {

        Flux<T> leftShare = Flux.from(left).share();
        Flux<R> rightShare = Flux.from(right).share();

        // another copy is used to transform the stream to the output
        Function<Publisher<R>, Publisher<S>> leftOutput = leftTransformer.apply(leftShare);
        Function<Publisher<T>, Publisher<S>> rightOutput = rightTransformer.apply(rightShare);

        // the results are merged into one stream
        return Flux.from(leftOutput.apply(rightShare)).mergeWith(rightOutput.apply(leftShare));
    }
}
