package dev.rono.permissions.core;

import ru.tehkode.permissions.PermissionGroup;
import ru.tehkode.permissions.PermissionUser;

import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

/**
 * Unified expiry engine for timed permissions and timed group memberships.
 *
 * <p>Timed grants use two storage shapes (permission maps vs {@code group-*-until} options) but
 * <strong>all expiry scheduling and sweeps must go through this coordinator</strong>. Callers register
 * the earliest known expiry via {@link #notifyEarliestExpiry(long)}; {@link #runSweep()} removes
 * expired timed permissions and group memberships in one pass.</p>
 *
 * <p>Do not add parallel timer tasks or ad-hoc expiry loops elsewhere in the engine.</p>
 */
final class TimedExpiryCoordinator {

    private final DefaultPermissionManager manager;
    private ScheduledFuture<?> nextTask;
    private long scheduledAtEpochSecond = Long.MAX_VALUE;

    TimedExpiryCoordinator(DefaultPermissionManager manager) {
        this.manager = manager;
    }

    void reset() {
        cancelScheduled();
        scheduledAtEpochSecond = Long.MAX_VALUE;
    }

    /**
     * Schedules a sweep at {@code epochSecond} when it is earlier than any pending sweep.
     */
    void notifyEarliestExpiry(long epochSecond) {
        if (epochSecond <= 0 || epochSecond == Long.MAX_VALUE) {
            return;
        }
        long now = System.currentTimeMillis() / 1000L;
        if (epochSecond <= now) {
            runSweep();
            return;
        }
        if (epochSecond < scheduledAtEpochSecond) {
            scheduleAt(epochSecond);
        }
    }

    void runSweep() {
        cancelScheduled();
        scheduledAtEpochSecond = Long.MAX_VALUE;
        long now = System.currentTimeMillis() / 1000L;
        long next = Long.MAX_VALUE;

        for (PermissionUser user : manager.getActiveUsers()) {
            if (user instanceof DefaultPermissionUser defaultUser) {
                next = Math.min(next, defaultUser.sweepTimedGroups(now));
            }
            if (user instanceof AbstractPermissionEntity entity) {
                next = Math.min(next, entity.sweepTimedPermissions(now));
            }
        }

        for (PermissionGroup group : manager.getGroupList()) {
            if (group instanceof AbstractPermissionEntity entity) {
                next = Math.min(next, entity.sweepTimedPermissions(now));
            }
        }

        if (next < Long.MAX_VALUE) {
            notifyEarliestExpiry(next);
        }
    }

    private void scheduleAt(long epochSecond) {
        cancelScheduled();
        long delay = epochSecond - (System.currentTimeMillis() / 1000L);
        if (delay < 0) {
            delay = 0;
        }
        scheduledAtEpochSecond = epochSecond;
        nextTask = manager.getExecutor().schedule(this::runSweep, delay, TimeUnit.SECONDS);
    }

    private void cancelScheduled() {
        if (nextTask != null) {
            nextTask.cancel(false);
            nextTask = null;
        }
    }
}
