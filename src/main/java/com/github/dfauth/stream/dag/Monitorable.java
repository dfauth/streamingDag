package com.github.dfauth.stream.dag;

import java.util.function.Supplier;

public interface Monitorable<T> extends Supplier<Monitor<T>> {

    default Monitor<T> get() {
        return monitor();
    }

    Monitor<T> monitor();

    interface VoidMonitorable extends Monitorable<Void> {
        @Override
        Monitor.VoidMonitor monitor();
    }
}
