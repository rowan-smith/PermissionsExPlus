package dev.rono.permissions.core.config;

import lombok.Value;
import lombok.experimental.Accessors;
@Value
@Accessors(fluent = true)
public class DatabaseCredentials {
    String host;
    int port;
    String database;
    String username;
    String password;

    public DatabaseCredentials(String host, int port, String database, String username, String password) {
        this.host = host;
        this.port = port;
        this.database = database;
        this.username = username;
        this.password = password;
    }
}
