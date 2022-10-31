package com.github.dfauth.stream.dag;

import org.reactivestreams.Publisher;
import reactor.core.publisher.Flux;

import java.util.Arrays;
import java.util.List;
import java.util.function.BiFunction;
import java.util.function.Consumer;
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
    }

    public static <T,S,R> Function<Publisher<T>, Function<Publisher<S>,Publisher<R>>> combineLatest(BiFunction<T,S,R> f, Consumer<T> consumer) {
        return combineLatest(asFunction2(f).curried(), consumer);
    }

    public static <T,S,R> Function<Publisher<T>, Function<Publisher<S>,Publisher<R>>> combineLatest(Function<T,Function<S,R>> f, Consumer<T> consumer) {
        OneSidedCachingTransformer<T, S, R> t = new OneSidedCachingTransformer<>(f);
        Flux.from(t).subscribe(consumer);
        return t.curried();
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
