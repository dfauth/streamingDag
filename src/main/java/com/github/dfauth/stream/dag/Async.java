package com.github.dfauth.stream.dag;

import com.github.dfauth.trycatch.ExceptionalRunnable;
import lombok.extern.slf4j.Slf4j;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;
import java.util.concurrent.ForkJoinPool;

import static com.github.dfauth.trycatch.TryCatchBuilder.tryCatch;

@Slf4j
public class Async {

    public static CompletableFuture<Void> async(ExceptionalRunnable r) {
        return async(r, ForkJoinPool.commonPool());
    }

    public static CompletableFuture<Void> async(Runnable r, Executor executor) {
        CompletableFuture<Void> f = new CompletableFuture<>();
        executor.execute(() -> tryCatch(() -> {
            r.run();
            f.complete(null);
        }).handleThrowable(t -> {
            f.completeExceptionally(t);
            return null;
        }).build());
        return f;
    }

}
