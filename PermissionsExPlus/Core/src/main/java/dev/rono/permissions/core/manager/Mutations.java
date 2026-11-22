package dev.rono.permissions.core.manager;

import lombok.var;

import dev.rono.permissions.core.store.DataStore;
import dev.rono.permissions.core.store.StorageConflictException;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Supplier;

/**
 * Runs a manager operation as one storage transaction.
 *
 * <p>
 * Events are published only once the operation commits, and cache changes are
 * undone if it rolls back, so a failed operation leaves no trace. An operation
 * that loses a revision race with another server is run again from a fresh
 * read, up to {@link #MAX_ATTEMPTS} times. Operations started inside another
 * join it, and only the outermost one is retried.
 * </p>
 */
final class Mutations {
    static final int MAX_ATTEMPTS = 5;

    private static final ThreadLocal<Scope> CURRENT = new ThreadLocal<>();

    private Mutations() {}

    static <T> T run(DataStore store, Supplier<T> work) {
        if (CURRENT.get() != null) {
            return work.get();
        }

        for (int attempt = 1;; attempt++) {
            var scope = new Scope();
            T result;

            CURRENT.set(scope);

            try {
                result = store.transaction(work);
            } catch (RuntimeException | Error error) {
                CURRENT.remove();
                scope.rollBack();

                if (error instanceof StorageConflictException && attempt < MAX_ATTEMPTS) {
                    continue;
                }

                throw error;
            }

            CURRENT.remove();
            scope.commit();

            return result;
        }
    }

    /** Runs {@code action} once the current operation commits, or now. */
    static void afterCommit(Runnable action) {
        var scope = CURRENT.get();

        if (scope == null) {
            action.run();
        } else {
            scope.afterCommit.add(action);
        }
    }

    /** Registers how to undo a cache change if the current operation fails. */
    static void onRollback(Runnable undo) {
        var scope = CURRENT.get();

        if (scope != null) {
            scope.undo.add(undo);
        }
    }

    private static final class Scope {
        private final List<Runnable> afterCommit = new ArrayList<>();
        private final List<Runnable> undo = new ArrayList<>();

        void commit() {
            afterCommit.forEach(Runnable::run);
        }

        void rollBack() {
            for (int index = undo.size() - 1; index >= 0; index--) {
                undo.get(index).run();
            }
        }
    }
}
