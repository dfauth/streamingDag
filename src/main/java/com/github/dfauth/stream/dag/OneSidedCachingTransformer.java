package com.github.dfauth.stream.dag;

import com.github.dfauth.function.Function2;
import com.github.dfauth.stream.dag.function.*;
import lombok.extern.slf4j.Slf4j;
import org.reactivestreams.Publisher;
import org.reactivestreams.Subscriber;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.Optional;
import java.util.function.BiFunction;
import java.util.function.Function;
import java.util.function.Supplier;

import static com.github.dfauth.function.Function2.asFunction2;
import static com.github.dfauth.stream.dag.KillSwitch.killSwitch;
import static com.github.dfauth.stream.dag.RequestListener.requestListener;
import static com.github.dfauth.stream.dag.function.Function2.function2;
import static com.github.dfauth.stream.dag.function.Function3.function3;
import static com.github.dfauth.stream.dag.function.Function4.function4;
import static com.github.dfauth.stream.dag.function.Function5.function5;
import static com.github.dfauth.stream.dag.function.Function6.function6;
import static com.github.dfauth.stream.dag.function.Function7.function7;
import static com.github.dfauth.stream.dag.function.Function8.function8;

@Slf4j
public class OneSidedCachingTransformer<T,R,S> implements BiFunction<Publisher<T>, Publisher<R>,Publisher<S>>, Publisher<T>, Monitorable.VoidMonitorable {

    public static <A, D> com.github.dfauth.stream.dag.function.Function2<A, Function<A, D>, D> combine() {
        return (a,f) -> f.apply(a);
    }

    public static <A> Supplier<Publisher<A>> compose0() {
        return () -> new AbstractPublisher<>(){};
    }

    public static <A,B> Function<Publisher<A>,Publisher<B>> compose1(Function<A,B> _f) {
        return a -> Flux.from(a).map(_f);
    }

    public static <A,B,C> BiFunction<Publisher<A>,Publisher<B>,Publisher<C>> compose2(BiFunction<A,B,C> _f) {
        return new OneSidedCachingTransformer<>(_f, ConsumingSubscriber.subscribe(t -> {}));
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

    private Monitor.VoidMonitor monitor;
    private SubscriberFunction<T, R, S> subscriberFn;
    private Subscriber<? super T> subscriber;

    public OneSidedCachingTransformer(BiFunction<T,R,S> f) {
        this(asFunction2(f).curried(), null);
    }

    public OneSidedCachingTransformer(BiFunction<T,R,S> f, Subscriber<T> subscriber) {
        this(asFunction2(f).curried(), subscriber);
    }

    public OneSidedCachingTransformer(Function<T,Function<R,S>> f) {
        this(f, null);
    }

    public OneSidedCachingTransformer(Function<T,Function<R,S>> f, Subscriber<T> subscriber) {
        subscriberFn = new SubscriberFunction<>(f);
        this.subscriber = subscriber;
    }

    @Override
    public Publisher<S> apply(Publisher<T> cachedPublisher, Publisher<R> publisher) {

        // Both a Function<T,Optional<R>> and a Subscriber<Function<T,R>> - used to map the input

        // cachedInput is fed to the curriedFn to create a partially applied fn cached in subscriberFn
        Flux.from(cachedPublisher).subscribe(subscriberFn);

        // return a publisher which will stream the input transformed by the cached partially applied function
        KillSwitch<S> killSwitch = killSwitch(Flux.from(publisher).flatMap(subscriberFn.andThen(Mono::justOrEmpty)));
        monitor = Monitor.VoidMonitor.from(killSwitch.monitor(), subscriberFn.monitor());
        subscriberFn.handle(() -> monitor);
        killSwitch.handle(() -> monitor);
        return requestListener((Publisher<S>)killSwitch, l -> Optional.ofNullable(subscriber)
                .ifPresent(e -> subscribe(subscriber)));
    }

    public Function<Publisher<T>, Function<Publisher<R>, Publisher<S>>> curried() {
        return Function2.asFunction2(this).curried();
    }

    @Override
    public Monitor.VoidMonitor monitor() {
        return monitor;
    }

    @Override
    public void subscribe(Subscriber<? super T> subscriber) {
        subscriberFn.subscribe(subscriber);
    }
}
