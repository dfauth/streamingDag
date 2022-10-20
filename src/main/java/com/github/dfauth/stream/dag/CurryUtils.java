package com.github.dfauth.stream.dag;

import org.reactivestreams.Publisher;

import java.util.Arrays;
import java.util.List;
import java.util.function.BiFunction;
import java.util.function.Function;

import static com.github.dfauth.Lists.*;
import static com.github.dfauth.function.Function2.asFunction2;
import static com.github.dfauth.function.Function2.uncurry;

public class CurryUtils {

    public static <T,R,S> BiFunction<Publisher<T>,Publisher<R>,Publisher<S>> biFunctionTransformer(BiFunction<T,R,S> f) {
        return biFunctionTransformer(asFunction2(f).curried());
    }

    public static <T,R,S> BiFunction<Publisher<T>,Publisher<R>,Publisher<S>> biFunctionTransformer(Function<T,Function<R,S>> f) {

        return new CachingTransformer<>(f);

//        return (left, right) -> {
//
//            // left and right streams are both split
//            Flux<T> leftShare = Flux.from(left).share();
//            Flux<R> rightShare = Flux.from(right).share();
//
//            // another copy is used to transform the stream to the output
//            Publisher<S> leftOutput = combineLatest(f).apply(leftShare).apply(rightShare);
//            Publisher<S> rightOutput = combineLatest(uncurry(f).flip()).apply(rightShare).apply(leftShare);
//
//            // the results are merged into one stream
//            return Flux.from(leftOutput).mergeWith(rightOutput);
//        };
    }

    public static <T,S,R> Function<Publisher<T>, Function<Publisher<S>,Publisher<R>>> combineLatest(BiFunction<T,S,R> f) {
        return combineLatest(asFunction2(f).curried());
    }

    public static <T,S,R> Function<Publisher<T>, Function<Publisher<S>,Publisher<R>>> combineLatest(Function<T,Function<S,R>> f) {
        return new OneSidedCachingTransformer<>(f);
    }

    public static <T,S,R> Publisher<?> curryingMerge(Function<T, Function<S,R>> f, Publisher<?>... publishers) {
        return curryingMerge(f, Arrays.asList(publishers));
    }

    public static <T,S,R> Publisher<?> curryingMerge(Function<T, Function<S,R>> f, List<Publisher<?>> publishers) {
        switch (publishers.size()) {
            case 0 : // should never happen
            case 1 : // should never happen
                throw new IllegalArgumentException("list must be have at least 2 members");
            case 2 :
                return biFunctionTransformer(
                        uncurry(f)).apply(
                            (Publisher<T>) head(publishers),
                            (Publisher<S>) tail(publishers).get(0)
                );
            default:
                List<Publisher<?>> reversed = reverse(publishers);
                Publisher<?> head = head(reversed);
                List<Publisher<?>> tail = tail(reversed);
                return biFunctionTransformer(
                        (T _t, S _f) -> ((Function<T,R>)_f).apply(_t)).apply(
                        (Publisher<T>) head,
                        (Publisher<S>) curryingMerge(f, reverse(tail))
                );
        }
    }
}
