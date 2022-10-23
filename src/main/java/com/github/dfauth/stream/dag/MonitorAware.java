package com.github.dfauth.stream.dag;

import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.function.Supplier;

import static com.github.dfauth.trycatch.TryCatch.loggingOperator;

public interface MonitorAware<T,R> {

    default CompletableFuture<R> handle(Monitorable<T> monitorable) {
        return monitorable.monitor().handle(this);
    }

    default R _handle(T t, Throwable throwable) {
        return Optional.ofNullable(throwable)
                .map(_t -> Optional.ofNullable(t)
                        .<R>map(MonitorAware::shouldNeverHappen)
                        .orElseGet(() -> onFailure(_t)))
                .orElseGet(() -> onComplete(t));
    }

    R onFailure(Throwable t);

    R onComplete(T t);

    static <R> R shouldNeverHappen() {
        return shouldNeverHappen(null);
    }
    static <T,R> R shouldNeverHappen(T t) {
        return _throw(() -> new IllegalStateException("should never happen"));
    }

    static <R> R _throw(Supplier<RuntimeException> supplier) {
        throw supplier.get();
    }

    interface Consumer<T> extends MonitorAware<T,Void> {
        @Override
        default Void onFailure(Throwable t) {
            _onFailure(t);
            return null;
        }

        default void _onFailure(Throwable t) {
            loggingOperator.apply(t);
        }

        @Override
        default Void onComplete(T t) {
            _onComplete(t);
            return null;
        }

        void _onComplete(T t);
    }

    interface VoidConsumer extends Consumer<Void> {

        default void _onComplete(Void v) {
            _onComplete();
        }

        void _onComplete();
    }
}
