package dev.rono.permissions.core.config;

import lombok.Value;
import lombok.experimental.Accessors;
@Value
@Accessors(fluent = true)
public class DatabasePool {
    int maximumPoolSize;
    int minimumIdle;
    long connectionTimeout;
    long maxLifetime;

    public DatabasePool(int maximumPoolSize, int minimumIdle, long connectionTimeout, long maxLifetime) {
        this.maximumPoolSize = maximumPoolSize;
        this.minimumIdle = minimumIdle;
        this.connectionTimeout = connectionTimeout;
        this.maxLifetime = maxLifetime;
    }

    public DatabasePool(int maximumPoolSize, int minimumIdle, long maxLifetime) {
        this(maximumPoolSize, minimumIdle, 5_000L, maxLifetime);
    }
}
