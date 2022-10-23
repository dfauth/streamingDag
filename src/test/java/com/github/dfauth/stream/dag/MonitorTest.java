package com.github.dfauth.stream.dag;

import lombok.extern.slf4j.Slf4j;
import org.junit.Test;

import java.util.concurrent.CompletableFuture;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

@Slf4j
public class MonitorTest {

    @Test
    public void testHappyCase() {
        CompletableFuture<Boolean> b = new CompletableFuture<>();
        MonitorAware.VoidConsumer aware = () -> b.complete(true);
        Monitor<Void> m = new Monitor<>();
        aware.handle(() -> m);
        m.complete(null);
        assertTrue(b.isDone());
        assertFalse(b.isCancelled());
        assertFalse(b.isCompletedExceptionally());
    }

    @Test
    public void testFailure() {
        CompletableFuture<Boolean> b = new CompletableFuture<>();
        MonitorAware.VoidConsumer aware = new MonitorAware.VoidConsumer() {
            @Override
            public void _onFailure(Throwable t) {
                b.completeExceptionally(t);
            }

            @Override
            public void _onComplete() {
                b.complete(true);
            }
        };
        Monitor<Void> m = new Monitor<>();
        aware.handle(() -> m);
        m.completeExceptionally(new RuntimeException("Oops"));
        assertTrue(b.isDone());
        assertFalse(b.isCancelled());
        assertTrue(b.isCompletedExceptionally());
    }

    @Test
    public void testIt() {
        CompletableFuture<Boolean> b = new CompletableFuture<>();
        MonitorAware.VoidConsumer aware = new MonitorAware.VoidConsumer() {
            @Override
            public void _onFailure(Throwable t) {
                b.completeExceptionally(t);
            }

            @Override
            public void _onComplete() {
                b.complete(true);
            }
        };
        Monitor<Void> m = new Monitor<>();
        m.handle(aware);
        m.completeExceptionally(new RuntimeException("Oops"));
        assertTrue(b.isDone());
        assertFalse(b.isCancelled());
        assertTrue(b.isCompletedExceptionally());
    }
}
