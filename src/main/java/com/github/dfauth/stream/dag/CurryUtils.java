package com.github.dfauth.stream.dag;

import com.github.dfauth.function.Function2;
import org.reactivestreams.Publisher;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.Arrays;
import java.util.List;
import java.util.function.BiFunction;
import java.util.function.Function;

import static com.github.dfauth.Lists.*;
import static com.github.dfauth.function.Function2.asFunction2;

public class CurryUtils {

    public static <T,S,R> Publisher<R> curryingMerge(BiFunction<T, S, R> f, Publisher<T> left, Publisher<S> right) {
        return curryingMerge(16, f, left, right);
    }

    public static <T,S,R> Publisher<R> curryingMerge(int n, BiFunction<T, S, R> f, Publisher<T> left, Publisher<S> right) {

        // left and right streams are both split
        Flux<T> leftShare = Flux.from(left).share();
        Flux<S> rightShare = Flux.from(right).share();

        // another copy is used to transform the stream to the output
        Publisher<R> leftOutput = curryingTransformation(leftShare, f, rightShare);
        Publisher<R> rightOutput = curryingTransformation(rightShare, asFunction2(f).flip(), leftShare);

        // the results are merged into one stream
        return Flux.from(leftOutput).mergeWith(rightOutput);
    }

    public static <T,S,R> Publisher<R> curryingTransformation(Publisher<T> cachedInput, BiFunction<T,S,R> f, Publisher<S> input) {
        return curryingTransformation(16, cachedInput, f, input);
    }

    public static <T,S,R> Publisher<R> curryingTransformation(int n, Publisher<T> cachedInput, BiFunction<T,S,R> f, Publisher<S> input) {

        // Both a Function<T,Optional<R>> and a Subscriber<Function<T,R>> - used to map the input
        SubscriberFunction<S, R> subscriberFn = new SubscriberFunction<>(n);

        // cachedInput is fed to the curriedFn to create a partially applied fn cached in subscriberFn
        Flux.from(cachedInput).map(asFunction2(f).curried()).subscribe(subscriberFn);

        // return a publisher which will stream the input transformed by the cached partially applied function
        return Flux.from(input).flatMap(subscriberFn.andThen(Mono::justOrEmpty));
    }

    public static <T,S,R> Publisher<?> curryingMerge(Function<T, Function<S,R>> f, Publisher<?>... publishers) {
        return curryingMerge(16, f, publishers);
    }

    public static <T,S,R> Publisher<?> curryingMerge(int n, Function<T, Function<S,R>> f, Publisher<?>... publishers) {
        return curryingMerge(n, f, Arrays.asList(publishers));
    }

    public static <T,S,R> Publisher<?> curryingMerge(int n, Function<T, Function<S,R>> f, List<Publisher<?>> publishers) {
        switch (publishers.size()) {
            case 0 : // should never happen
                throw new IllegalArgumentException("list must be have at least 2 members");
            case 1 : // should never happen
                throw new IllegalArgumentException("list must be have at least 2 members");
            case 2 :
                return curryingMerge(n,
                        Function2.uncurry(f),
                        (Publisher<T>) head(publishers),
                        (Publisher<S>) tail(publishers).get(0)
                );
            default:
                List<Publisher<?>> reversed = reverse(publishers);
                Publisher<?> head = head(reversed);
                List<Publisher<?>> tail = tail(reversed);
                return curryingMerge(n,
                        (T _t, S _f) -> ((Function<T,R>)_f).apply(_t),
                        (Publisher<T>) head,
                        (Publisher<S>) curryingMerge(n, f, reverse(tail))
                );
        }
    }
}
