package dev.rono.permissions.core.sync;

import lombok.var;

import static org.junit.jupiter.api.Assertions.assertEquals;
import dev.rono.permissions.core.config.AdvancedConfiguration;
import dev.rono.permissions.core.platform.PlatformLogger;
import dev.rono.permissions.core.platform.PlatformScheduler;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.ArrayList;
import java.util.Base64;
import java.util.List;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import dev.rono.permissions.core.util.Immutables;

class RedisSyncServiceTest {

    @Test
    void validRemoteInvalidationRequestsARefresh() {
        var scheduler = new RecordingScheduler();
        var invalidated = new ArrayList<UUID>();
        var local = UUID.randomUUID();
        var remote = UUID.randomUUID();
        var service = new RedisSyncService(config(), scheduler, new RecordingLogger(), invalidated::add, local);
        var user = UUID.randomUUID();

        service.handleMessage("pex-sync", "permissionsexplus:invalidate-user:" + remote + ':' + user);

        assertEquals(Immutables.listOf(user), invalidated);
    }

    @Test
    void remoteGroupInvalidationRequestsAGroupRefresh() {
        var local = UUID.randomUUID();
        var remote = UUID.randomUUID();
        var users = new ArrayList<UUID>();
        var groups = new ArrayList<String>();
        var service = new RedisSyncService(config(), new RecordingScheduler(), new RecordingLogger(), users::add, groups::add, ignored -> {}, ignored -> {}, local);

        service.handleMessage("pex-sync", "permissionsexplus:invalidate-group:" + remote + ":vip");
        service.handleMessage("pex-sync", "permissionsexplus:invalidate-group:" + local + ":staff");

        assertEquals(Immutables.listOf("vip"), groups);
        assertEquals(Immutables.listOf(), users);
    }

    @Test
    void remoteLadderInvalidationRequestsALadderRefresh() {
        var ladders = new ArrayList<String>();
        var service = new RedisSyncService(config(), new RecordingScheduler(), new RecordingLogger(), ignored -> {}, ignored -> {}, ladders::add, ignored -> {}, UUID.randomUUID());

        service.handleMessage("pex-sync", "permissionsexplus:invalidate-ladder:" + UUID.randomUUID() + ":staff");

        assertEquals(Immutables.listOf("staff"), ladders);
    }

    @Test
    void dropsGroupInvalidationsWithoutAName() {
        var logger = new RecordingLogger();
        var groups = new ArrayList<String>();
        var service = new RedisSyncService(config(), new RecordingScheduler(), logger, ignored -> {}, groups::add, ignored -> {}, ignored -> {}, UUID.randomUUID());

        service.handleMessage("pex-sync", "permissionsexplus:invalidate-group:" + UUID.randomUUID() + ':');

        assertEquals(Immutables.listOf(), groups);
        assertEquals(1, logger.warnings.size());
    }

    @Test
    void ignoresWrongChannelsAndMalformedPackets() {
        var scheduler = new RecordingScheduler();
        var logger = new RecordingLogger();
        var service = new RedisSyncService(config(), scheduler, logger, user -> {});

        service.handleMessage("other", "permissionsexplus:invalidate-user:" + UUID.randomUUID() + ':' + UUID.randomUUID());
        service.handleMessage("pex-sync", "unexpected");
        service.handleMessage("pex-sync", "permissionsexplus:invalidate-user:not-a-uuid");

        assertEquals(0, scheduler.mainTasks.size());
        assertEquals(1, logger.warnings.size());
    }

    @Test
    void ignoresMessagesPublishedByTheSameRuntime() {
        var local = UUID.randomUUID();
        var refreshed = new ArrayList<UUID>();
        var service = new RedisSyncService(config(), new RecordingScheduler(), new RecordingLogger(), refreshed::add, local);

        service.handleMessage("pex-sync", "permissionsexplus:invalidate-user:" + local + ':' + UUID.randomUUID());

        assertEquals(Immutables.listOf(), refreshed);
    }

    @Test
    void decodesRemoteAuditRecordsAndIgnoresLocalOnes() {
        var local = UUID.randomUUID();
        var remote = UUID.randomUUID();
        var received = new ArrayList<String>();
        var service = new RedisSyncService(config(), new RecordingScheduler(), new RecordingLogger(), ignored -> {}, received::add, local);
        var encoded = Base64.getUrlEncoder().withoutPadding().encodeToString("staff changed a group".getBytes(StandardCharsets.UTF_8));

        service.handleMessage("pex-sync", "permissionsexplus:audit:" + remote + ':' + encoded);
        service.handleMessage("pex-sync", "permissionsexplus:audit:" + local + ':' + encoded);

        assertEquals(Immutables.listOf("staff changed a group"), received);
    }

    private static AdvancedConfiguration config() {
        return new AdvancedConfiguration("redis", 10, 30, "mix", "global", true, true, true, true, Immutables.mapOf(), 10, "localhost", 6379, "", "pex-sync", 1000, true);
    }

    private static final class RecordingScheduler implements PlatformScheduler {
        private final List<Runnable> mainTasks = new ArrayList<>();

        @Override
        public void execute(Runnable task) {
            mainTasks.add(task);
        }

        @Override
        public void executeAsync(Runnable task) {}

        @Override
        public void executeLater(Runnable task, Duration delay) {}

        @Override
        public void executeLaterAsync(Runnable task, Duration delay) {}

        @Override
        public void executeRepeating(Runnable task, Duration interval) {}

        @Override
        public void executeRepeatingAsync(Runnable task, Duration interval) {}

        @Override
        public boolean isMainThread() {
            return true;
        }

        @Override
        public void cancelTask(int taskId) {}

        @Override
        public void cancelTasks() {}
    }

    private static final class RecordingLogger implements PlatformLogger {
        private final List<String> warnings = new ArrayList<>();

        @Override
        public void info(String message) {}

        @Override
        public void warn(String message) {
            warnings.add(message);
        }

        @Override
        public void error(String message, Throwable error) {}
    }
}
