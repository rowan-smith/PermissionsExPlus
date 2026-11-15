package dev.rono.permissions.core.threading;

import lombok.var;

import java.time.Duration;
import java.util.Objects;
import java.util.concurrent.Callable;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionStage;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Consumer;
import org.jspecify.annotations.NonNull;

/** Fixed-size, lifecycle-owned executor for all core background work. */
public final class CoreTaskExecutor implements AutoCloseable {
    private static final Duration SHUTDOWN_TIMEOUT = Duration.ofSeconds(5);

    private final ThreadPoolExecutor executor;
    private final ThreadLocal<Boolean> workerThread = ThreadLocal.withInitial(() -> false);

    public CoreTaskExecutor(int workers) {
        this(workers, Throwable::printStackTrace);
    }

    public CoreTaskExecutor(int workers, Consumer<Throwable> uncaughtErrors) {
        if (workers < 1) {
            throw new IllegalArgumentException("workers must be at least 1");
        }

        Objects.requireNonNull(uncaughtErrors, "uncaughtErrors");

        var factory = getThreadFactory(uncaughtErrors);
        executor = new ThreadPoolExecutor(workers, workers, 0, TimeUnit.MILLISECONDS, new LinkedBlockingQueue<>(), factory, new ThreadPoolExecutor.AbortPolicy());
    }

    private @NonNull ThreadFactory getThreadFactory(Consumer<Throwable> uncaughtErrors) {
        var sequence = new AtomicInteger();

        return task -> {
            var thread = new Thread(() -> {
                workerThread.set(true);

                try {
                    task.run();
                } finally {
                    workerThread.remove();
                }
            }, "permissionsexplus-worker-" + sequence.incrementAndGet());

            thread.setDaemon(true);
            thread.setUncaughtExceptionHandler((ignored, error) -> uncaughtErrors.accept(error));

            return thread;
        };
    }

    public void execute(Runnable task) {
        Objects.requireNonNull(task, "task");

        if (workerThread.get()) {
            task.run();
            return;
        }

        executor.execute(task);
    }

    public <T> CompletionStage<T> submit(Callable<T> task) {
        Objects.requireNonNull(task, "task");

        var future = new CompletableFuture<T>();

        execute(() -> {
            try {
                future.complete(task.call());
            } catch (Throwable error) {
                future.completeExceptionally(error);
            }
        });

        return future;
    }

    public int workerCount() {
        return executor.getCorePoolSize();
    }

    public boolean isShutdown() {
        return executor.isShutdown();
    }

    @Override
    public void close() {
        executor.shutdown();

        try {
            if (!executor.awaitTermination(SHUTDOWN_TIMEOUT.toMillis(), TimeUnit.MILLISECONDS)) {
                executor.shutdownNow();
            }
        } catch (InterruptedException error) {
            executor.shutdownNow();

            Thread.currentThread().interrupt();
        }
    }
}
