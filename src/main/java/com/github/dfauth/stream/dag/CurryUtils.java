package com.github.dfauth.stream.dag;

import org.reactivestreams.Publisher;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.*;
import java.util.function.BiFunction;
import java.util.function.Function;
import java.util.stream.Stream;

import static com.github.dfauth.trycatch.TryCatch.tryCatch;
import static java.util.function.Function.identity;

public class CurryUtils {

    public static <T,S,R> Publisher<R> curryingMerge(BiFunction<T, S, R> f, Publisher<T> left, Publisher<S> right) {
        return curryingMerge(16, f, left, right);
    }

    public static <T,S,R> Publisher<R> curryingMerge(int n, BiFunction<T, S, R> f, Publisher<T> left, Publisher<S> right) {
        SubscriberFunction<S, R> leftFn = new SubscriberFunction<>(n);
        SubscriberFunction<T, R> rightFn = new SubscriberFunction<>(n);

        // left and right streams are both split
        Flux<T> leftShare = Flux.from(left).share();
        Flux<S> rightShare = Flux.from(right).share();

        // one copy is used to curry the function transforming the other stream
        leftShare.map(curryLeft(f)).subscribe(leftFn);
        rightShare.map(curryRight(f)).subscribe(rightFn);

        // another copy is used to transform the stream to the output
        Flux<R> leftOutput = leftShare.flatMap(rightFn.andThen(Mono::justOrEmpty));
        Flux<R> rightOutput = rightShare.flatMap(leftFn.andThen(Mono::justOrEmpty));

        // the results are merged into one stream
        return leftOutput.mergeWith(rightOutput);
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
                        uncurry(f),
                        (Publisher<T>) headOpt(publishers).get(),
                        (Publisher<S>) tail(publishers).get(0)
                );
            default:
                List<Publisher<?>> reversed = reverse(publishers);
                Publisher<?> head = headOpt(reversed).get();
                List<Publisher<?>> tail = tail(reversed);
                return curryingMerge(n,
                        (T _t, S _f) -> ((Function<T,R>)_f).apply(_t),
                        (Publisher<T>) head,
                        (Publisher<S>) curryingMerge(n, f, reverse(tail))
                );
        }
    }

    private static <T> List<T> reverse(List<T> list) {
        ArrayList<T> tmp = new ArrayList<>(list);
        Collections.reverse(tmp);
        return tmp;
    }

    private static <T> Optional<T> headOpt(List<T> list) {
        return list.size() > 0 ? Optional.of(list.get(0)) : Optional.empty();
    }

    private static <T> List<T> tail(List<T> list) {
        return list.size() > 1 ? list.subList(1,list.size()) : Collections.emptyList();
    }

    public static <T,S,R> Function<T, Function<S, R>> curry(BiFunction<T, S, R> f) {
        return curryLeft(f);
    }

    public static <T,S,R> Function<T, Function<S, R>> curryLeft(BiFunction<T, S, R> f) {
        return t -> s -> f.apply(t, s);
    }

    public static <T,S,R> Function<S, Function<T, R>> curryRight(BiFunction<T, S, R> f) {
        return s -> t -> f.apply(t, s);
    }

    public static <T,S,R> BiFunction<T,S,R> uncurry(Function<T, Function<S, R>> f) {
        return uncurryLeft(f);
    }

    public static <T,S,R> BiFunction<T,S,R> uncurryLeft(Function<T, Function<S, R>> f) {
        return (t, s) -> tryCatch(() -> {
            return f.apply(t).apply(s);
        });
    }

    public static <T,S,R> BiFunction<S,T,R> uncurryRight(Function<T, Function<S, R>> f) {
        return (t, s) -> tryCatch(() -> {
            return f.apply(s).apply(t);
        });
    }

    public static boolean isFunctionalInterface(Class cls) {
        return cls.isInterface() &&
                Stream.of(cls.getDeclaredMethods())
                        .filter(m -> Modifier.isPublic(m.getModifiers()) && !Modifier.isStatic(m.getModifiers()) && !m.isDefault())
                        .peek(m ->
                                System.out.println("m: "+m))
                        .count() == 1;
    }

    public static Method getFunctionalInterfaceMethod(Class cls) {
        return Stream.of(cls.getDeclaredMethods()).filter(m -> !m.isDefault()).findFirst().orElseThrow(() -> new IllegalArgumentException(cls.getCanonicalName()+" is not a FunctionalInterface"));
    }

    public static <T,R extends T> boolean isLambda(Class<T> classOfT) {
        String className = classOfT.getName();
        ClassLoader loader = classOfT.getClassLoader();
        try {
            loader.loadClass(className);
            return false;
        } catch (ClassNotFoundException e) {
            return true;
        }
    }

    public static <T,R extends T> boolean isLambdaOf(Class<R> cls, Class<T> interfaceClass) {
        Method m = getFunctionalInterfaceMethod(interfaceClass);
        return Stream.of(cls.getDeclaredMethods()).filter(_m ->
                _m.getName().equals(m.getName()) &&
                        _m.getParameterCount() == m.getParameterCount()
        )
                .findAny().isEmpty();
    }

    public static <T> Publisher<T> loggingProcessor(String loggerName, Publisher<T> p) {
        BaseProcessor<T, T> p1 = new BaseProcessor<>(identity()) {
            private final Logger logger = LoggerFactory.getLogger(loggerName);
            @Override
            public void onNext(T t) {
                super.onNext(t);
                logger.info("onNext({})",t);
            }
        };
        p.subscribe(p1);
        return p1;
    }
}
