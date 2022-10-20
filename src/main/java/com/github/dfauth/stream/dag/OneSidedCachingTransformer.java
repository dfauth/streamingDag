package com.github.dfauth.stream.dag;

import lombok.extern.slf4j.Slf4j;
import org.reactivestreams.Publisher;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.Optional;
import java.util.function.BiFunction;
import java.util.function.Function;

import static com.github.dfauth.function.Function2.asFunction2;

@Slf4j
public class OneSidedCachingTransformer<T,R,S> implements Function<Publisher<T>, Function<Publisher<R>,Publisher<S>>> {

    private final Function<T, Function<R,S>> f;

    public OneSidedCachingTransformer(BiFunction<T,R,S> f) {
        this(asFunction2(f).curried());
    }

    public OneSidedCachingTransformer(Function<T,Function<R,S>> f) {
        this.f = f;
    }

    @Override
    public Function<Publisher<R>, Publisher<S>> apply(Publisher<T> cachedInput) {

        // Both a Function<T,Optional<R>> and a Subscriber<Function<T,R>> - used to map the input
        SubscriberFunction<R,S> subscriberFn = new SubscriberFunction<>();

        // cachedInput is fed to the curriedFn to create a partially applied fn cached in subscriberFn
        Flux.from(cachedInput).map(f).subscribe(subscriberFn);

        // return a publisher which will stream the input transformed by the cached partially applied function
        return input -> Flux.from(input).flatMap(((Function<R, Optional<S>>)subscriberFn).andThen(Mono::justOrEmpty));
    }
}
