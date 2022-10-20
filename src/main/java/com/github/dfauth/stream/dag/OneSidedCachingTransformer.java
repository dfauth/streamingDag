package com.github.dfauth.stream.dag;

import lombok.extern.slf4j.Slf4j;
import org.reactivestreams.Publisher;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.Optional;
import java.util.function.Function;

@Slf4j
public class OneSidedCachingTransformer<T,S,R> implements Function<Publisher<T>, Function<Publisher<S>,Publisher<R>>> {

    private final Function<T, Function<S, R>> f;

    public OneSidedCachingTransformer(Function<T, Function<S, R>> f) {
        this.f = f;
    }

    @Override
    public Function<Publisher<S>, Publisher<R>> apply(Publisher<T> cachedInput) {

        // Both a Function<T,Optional<R>> and a Subscriber<Function<T,R>> - used to map the input
        SubscriberFunction<S, R> subscriberFn = new SubscriberFunction<>();

        // cachedInput is fed to the curriedFn to create a partially applied fn cached in subscriberFn
        Flux.from(cachedInput).map(f).subscribe(subscriberFn);

        // return a publisher which will stream the input transformed by the cached partially applied function
        return input -> Flux.from(input).flatMap(((Function<S, Optional<R>>)subscriberFn).andThen(Mono::justOrEmpty));
    }
}
