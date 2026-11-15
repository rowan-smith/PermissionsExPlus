package dev.rono.permissions.core.logger;

import lombok.var;

import dev.rono.permissions.core.platform.PlatformConfiguration;
import dev.rono.permissions.core.platform.PlatformLogger;
import dev.rono.permissions.core.platform.PlatformScheduler;

import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.StandardOpenOption;
import java.time.Instant;
import java.util.function.BooleanSupplier;
import java.util.function.Consumer;

/**
 * Append-only, asynchronous administrative audit trail.
 */
public final class AuditLogger {
    private final PlatformConfiguration configuration;
    private final PlatformScheduler scheduler;
    private final PlatformLogger logger;
    private final BooleanSupplier fileEnabled;
    private final BooleanSupplier broadcastEnabled;
    private final BooleanSupplier networkEnabled;
    private final Consumer<String> operatorBroadcast;
    private volatile Consumer<String> networkPublisher = ignored -> {};

    public AuditLogger(
            PlatformConfiguration configuration,
            PlatformScheduler scheduler,
            PlatformLogger logger,
            BooleanSupplier enabled) {
        this(configuration, scheduler, logger, enabled, () -> false, () -> false, ignored -> {});
    }

    public AuditLogger(
            PlatformConfiguration configuration,
            PlatformScheduler scheduler,
            PlatformLogger logger,
            BooleanSupplier fileEnabled,
            BooleanSupplier broadcastEnabled,
            BooleanSupplier networkEnabled,
            Consumer<String> operatorBroadcast) {
        this.configuration = configuration;
        this.scheduler = scheduler;
        this.logger = logger;
        this.fileEnabled = fileEnabled;
        this.broadcastEnabled = broadcastEnabled;
        this.networkEnabled = networkEnabled;
        this.operatorBroadcast = operatorBroadcast;
    }

    /**
     * Creates an always-enabled audit logger.
     */
    public AuditLogger(PlatformConfiguration configuration, PlatformScheduler scheduler, PlatformLogger logger) {
        this(configuration, scheduler, logger, () -> true);
    }

    public void log(String actor, String action) {
        var entry = Instant.now() + " [Audit] Staff '" + actor + "' " + action;

        if (broadcastEnabled.getAsBoolean()) {
            scheduler.execute(() -> operatorBroadcast.accept(entry));
        }

        if (networkEnabled.getAsBoolean()) {
            networkPublisher.accept(entry);
        }

        if (!fileEnabled.getAsBoolean()) {
            return;
        }

        scheduler.executeAsync(() -> {
            try {
                var file = configuration.resolve("audit.log");

                Files.createDirectories(file.getParent());
                Files.write(file, (entry + System.lineSeparator()).getBytes(StandardCharsets.UTF_8), StandardOpenOption.CREATE, StandardOpenOption.APPEND);
            } catch (Exception error) {
                logger.error("Unable to write PermissionsExPlus audit entry", error);
            }
        });
    }

    /**
     * Installs the active network transport after messaging has started.
     */
    public void attachNetworkPublisher(Consumer<String> publisher) {
        networkPublisher = publisher == null ? ignored -> {} : publisher;
    }

    /**
     * Handles a remote audit record without publishing it back to the network.
     */
    public void receiveRemote(String entry) {
        if (!networkEnabled.getAsBoolean()) {
            return;
        }

        logger.info(entry);

        if (broadcastEnabled.getAsBoolean()) {
            scheduler.execute(() -> operatorBroadcast.accept(entry));
        }

        if (!fileEnabled.getAsBoolean()) {
            return;
        }

        scheduler.executeAsync(() -> {
            try {
                var file = configuration.resolve("audit.log");

                Files.createDirectories(file.getParent());
                Files.write(file, (entry + System.lineSeparator()).getBytes(StandardCharsets.UTF_8), StandardOpenOption.CREATE, StandardOpenOption.APPEND);
            } catch (Exception error) {
                logger.error("Unable to write remote PermissionsExPlus audit entry", error);
            }
        });
    }
}
