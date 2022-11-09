package com.github.dfauth.stream.dag;

import com.github.dfauth.stream.dag.function.*;
import lombok.extern.slf4j.Slf4j;
import org.reactivestreams.Publisher;
import reactor.core.publisher.Flux;

import java.util.function.BiFunction;
import java.util.function.Function;
import java.util.function.Supplier;

import static com.github.dfauth.stream.dag.KillSwitch.killSwitch;
import static com.github.dfauth.stream.dag.function.Function2.function2;
import static com.github.dfauth.stream.dag.function.Function3.function3;
import static com.github.dfauth.stream.dag.function.Function4.function4;
import static com.github.dfauth.stream.dag.function.Function5.function5;
import static com.github.dfauth.stream.dag.function.Function6.function6;
import static com.github.dfauth.stream.dag.function.Function7.function7;
import static com.github.dfauth.stream.dag.function.Function8.function8;

@Slf4j
public class CachingTransformer<T,R,S> implements BiFunction<Publisher<T>, Publisher<R>, Publisher<S>>, Monitorable.VoidMonitorable {

    public static <A> Supplier<Publisher<A>> compose0() {
        return () -> new AbstractPublisher<>(){};
    }

    public static <A,B> Function<Publisher<A>,Publisher<B>> compose1(Function<A,B> _f) {
        return a -> Flux.from(a).map(_f);
    }

    public static <A,B,C> BiFunction<Publisher<A>,Publisher<B>,Publisher<C>> compose2(BiFunction<A,B,C> _f) {
        return new CachingTransformer<>(_f);
    }

    public static <A,B,C,D> Function3<Publisher<A>,Publisher<B>,Publisher<C>,Publisher<D>> compose3(Function3<A,B,C,D> _f) {
        return function3(a -> b -> c ->
                compose2(OneSidedCachingTransformer.<A,D>combine())
                        .apply(a,compose2(function2(_f.flip().unwind()).flip())
                                .apply(b,c)));
    }

    public static <A,B,C,D,E> Function4<Publisher<A>,Publisher<B>,Publisher<C>,Publisher<D>,Publisher<E>> compose4(Function4<A,B,C,D,E> _f) {
        return function4(a -> b -> c -> d ->
                compose2(OneSidedCachingTransformer.<A,E>combine())
                        .apply(a,compose3(function3(_f.flip().unwind()).flip())
                                .apply(b,c,d)));
    }

    public static <A,B,C,D,E,F> Function5<Publisher<A>,Publisher<B>,Publisher<C>,Publisher<D>,Publisher<E>,Publisher<F>> compose5(Function5<A,B,C,D,E,F> _f) {
        return function5(a -> b -> c -> d -> e ->
                compose2(OneSidedCachingTransformer.<A,F>combine())
                        .apply(a,compose4(function4(_f.flip().unwind()).flip())
                                .apply(b,c,d,e)));
    }

    public static <A,B,C,D,E,F,G> Function6<Publisher<A>,Publisher<B>,Publisher<C>,Publisher<D>,Publisher<E>,Publisher<F>,Publisher<G>> compose6(Function6<A,B,C,D,E,F,G> _f) {
        return function6(a -> b -> c -> d -> e -> f ->
                compose2(OneSidedCachingTransformer.<A,G>combine())
                        .apply(a,compose5(function5(_f.flip().unwind()).flip())
                                .apply(b,c,d,e,f)));
    }

    public static <A,B,C,D,E,F,G,H> Function7<Publisher<A>,Publisher<B>,Publisher<C>,Publisher<D>,Publisher<E>,Publisher<F>,Publisher<G>,Publisher<H>> compose7(Function7<A,B,C,D,E,F,G,H> _f) {
        return function7(a -> b -> c -> d -> e -> f -> g ->
                compose2(OneSidedCachingTransformer.<A,H>combine())
                        .apply(a,compose6(function6(_f.flip().unwind()).flip())
                                .apply(b,c,d,e,f,g)));
    }

    public static <A,B,C,D,E,F,G,H,I> Function8<Publisher<A>,Publisher<B>,Publisher<C>,Publisher<D>,Publisher<E>,Publisher<F>,Publisher<G>,Publisher<H>,Publisher<I>> compose8(Function8<A,B,C,D,E,F,G,H,I> _f) {
        return function8(a -> b -> c -> d -> e -> f -> g -> h ->
                compose2(OneSidedCachingTransformer.<A,I>combine())
                        .apply(a,compose7(function7(_f.flip().unwind()).flip())
                                .apply(b,c,d,e,f,g,h)));
    }

    private final OneSidedCachingTransformer<T, R, S> leftTransformer;
    private final OneSidedCachingTransformer<R, T, S> rightTransformer;
    private Monitor.VoidMonitor monitor;

    public CachingTransformer(BiFunction<T, R, S> f) {
        this(function2(f).curry());
    }

    public CachingTransformer(Function<T, Function<R, S>> f) {
        this.leftTransformer = new OneSidedCachingTransformer<>(f);
        this.rightTransformer = new OneSidedCachingTransformer<>(com.github.dfauth.function.Function2.uncurry(f).curriedRight());
    }

    public static <T,R,S> CachingTransformer<T,R,S> stream(BiFunction<T,R,S> f) {
        return new CachingTransformer<>(f);
    }

    @Override
    public Publisher<S> apply(Publisher<T> left, Publisher<R> right) {

        // another copy is used to transform the stream to the output
        Function<Publisher<R>, Publisher<S>> leftOutput = leftTransformer.curried().apply(left);
        Function<Publisher<T>, Publisher<S>> rightOutput = rightTransformer.curried().apply(right);

        // the results are merged into one stream
        KillSwitch<S> k = killSwitch(Flux.from(leftOutput.apply(rightTransformer)).mergeWith(rightOutput.apply(leftTransformer)));
        this.monitor = Monitor.VoidMonitor.from(leftTransformer.monitor(), rightTransformer.monitor());
        k.handle(this);
        return k;
//        return Flux.from(leftOutput.apply(rightTransformer.getThingy())).mergeWith(rightOutput.apply(leftTransformer.getThingy()));
    }

    @Override
    public Monitor.VoidMonitor monitor() {
        return monitor;
    }
}
