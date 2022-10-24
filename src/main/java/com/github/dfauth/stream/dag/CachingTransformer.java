package com.github.dfauth.stream.dag;

import com.github.dfauth.stream.dag.function.Function3;
import com.github.dfauth.stream.dag.function.Function4;
import com.github.dfauth.stream.dag.function.Function5;
import lombok.extern.slf4j.Slf4j;
import org.reactivestreams.Publisher;
import reactor.core.publisher.Flux;

import java.util.function.BiFunction;
import java.util.function.Function;

import static com.github.dfauth.stream.dag.KillSwitch.killSwitch;
import static com.github.dfauth.stream.dag.function.Function2.function2;
import static com.github.dfauth.stream.dag.function.Function3.function3;
import static com.github.dfauth.stream.dag.function.Function4.function4;
import static com.github.dfauth.stream.dag.function.Function5.function5;

@Slf4j
public class CachingTransformer<T,R,S> implements BiFunction<Publisher<T>, Publisher<R>, Publisher<S>>, Monitorable.VoidMonitorable {

    public static <C,D> BiFunction<Publisher<Function<C,D>>,Publisher<C>,Publisher<D>> compose() {
        return new CachingTransformer<>(Function::apply);
    }

    public static <A,B,C> BiFunction<Publisher<A>,Publisher<B>,Publisher<C>> compose(BiFunction<A,B,C> f) {
        return new CachingTransformer<>(f);
    }

    public static <A,B,C,D> Function3<Publisher<A>,Publisher<B>,Publisher<C>,Publisher<D>> compose(Function3<A,B,C,D> f) {
        return function3(a -> b -> c ->
            CachingTransformer.<C,D>compose().apply(compose(function2(f.unwind())).apply(a, b), c)
        );
    }

    public static <A,B,C,D,E> Function4<Publisher<A>,Publisher<B>,Publisher<C>,Publisher<D>,Publisher<E>> compose(Function4<A,B,C,D,E> f) {
        return function4(a -> b -> c -> d ->
            CachingTransformer.<D,E>compose().apply(compose(function3(f.unwind())).apply(a,b,c),d)
        );
    }

    public static <A,B,C,D,E,F> Function5<Publisher<A>,Publisher<B>,Publisher<C>,Publisher<D>,Publisher<E>,Publisher<F>> compose(Function5<A,B,C,D,E,F> f) {
        return function5(a -> b -> c -> d -> e ->
            CachingTransformer.<E,F>compose().apply(compose(function4(f.unwind())).apply(a,b,c,d),e)
        );
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

        Flux<T> leftShare = Flux.from(left).share();
        Flux<R> rightShare = Flux.from(right).share();

        // another copy is used to transform the stream to the output
        Function<Publisher<R>, Publisher<S>> leftOutput = leftTransformer.curried().apply(leftShare);
        Function<Publisher<T>, Publisher<S>> rightOutput = rightTransformer.curried().apply(rightShare);

        // the results are merged into one stream
        KillSwitch<S> k = killSwitch(Flux.from(leftOutput.apply(rightShare)).mergeWith(rightOutput.apply(leftShare)));
        this.monitor = Monitor.VoidMonitor.from(leftTransformer.monitor(), rightTransformer.monitor());
        k.handle(this);
        return k;
    }

    @Override
    public Monitor.VoidMonitor monitor() {
        return monitor;
    }
}
