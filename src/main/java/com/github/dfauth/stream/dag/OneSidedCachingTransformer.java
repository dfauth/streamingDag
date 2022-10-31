package com.github.dfauth.stream.dag;

import com.github.dfauth.function.Function2;
import lombok.extern.slf4j.Slf4j;
import org.reactivestreams.Publisher;
import org.reactivestreams.Subscriber;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.function.BiFunction;
import java.util.function.Function;

import static com.github.dfauth.function.Function2.asFunction2;
import static com.github.dfauth.stream.dag.KillSwitch.killSwitch;

@Slf4j
public class OneSidedCachingTransformer<T,R,S> implements BiFunction<Publisher<T>, Publisher<R>,Publisher<S>>, Publisher<T>, Monitorable.VoidMonitorable {

    private Monitor.VoidMonitor monitor;
    private SubscriberFunction<T, R, S> subscriberFn;

    public OneSidedCachingTransformer(BiFunction<T,R,S> f) {
        this(asFunction2(f).curried());
    }

    public OneSidedCachingTransformer(Function<T,Function<R,S>> f) {
        subscriberFn = new SubscriberFunction<>(f);
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
        return killSwitch;
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
