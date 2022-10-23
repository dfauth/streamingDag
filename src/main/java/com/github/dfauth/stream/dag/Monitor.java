package com.github.dfauth.stream.dag;

import java.util.concurrent.CompletableFuture;
import java.util.stream.Stream;

public class Monitor<T> {

    private CompletableFuture<T> f = new CompletableFuture<>();

    public void complete(T t) {
        if (!f.isDone()) {
            f.complete(t);
        }
    }

    public void completeExceptionally(Throwable t) {
        if (!f.isDone()) {
            f.completeExceptionally(t);
        }
    }

    public <R> CompletableFuture<R> handle(MonitorAware<T, R> monitorAware) {
        return f.handle(monitorAware::_handle);
    }

    static class VoidMonitor extends Monitor<Void> {

        public static VoidMonitor from(VoidMonitor... monitors) {
            VoidMonitor m = new VoidMonitor();
            Stream.of(monitors).forEach(_m -> _m.handle((MonitorAware.VoidConsumer) m::complete));
            return m;
        }

        public void complete() {
            complete(null);
        }

    }
}
