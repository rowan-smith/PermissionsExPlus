package dev.rono.permissions.core.store;

import dev.rono.permissions.core.config.DatabasePool;
import dev.rono.permissions.core.config.DdlGeneration;
import dev.rono.permissions.core.model.GroupSnapshot;
import dev.rono.permissions.core.model.LadderSnapshot;
import dev.rono.permissions.core.model.UserSnapshot;
import dev.rono.permissions.core.store.dto.ContextDto;
import dev.rono.permissions.core.store.dto.GroupDto;
import dev.rono.permissions.core.store.dto.LadderDto;
import dev.rono.permissions.core.store.dto.LadderGroupDto;
import dev.rono.permissions.core.store.dto.OptionDto;
import dev.rono.permissions.core.store.dto.ParentDto;
import dev.rono.permissions.core.store.dto.PermissionDto;
import dev.rono.permissions.core.store.dto.UserDto;
import dev.rono.permissions.core.store.repository.GroupRepository;
import dev.rono.permissions.core.store.repository.LadderRepository;
import dev.rono.permissions.core.store.repository.NodeRepository;
import dev.rono.permissions.core.store.repository.UserRepository;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.hibernate.cfg.AvailableSettings;
import org.hibernate.cfg.Configuration;

/**
 * Relational {@link DataStore} that persists typed snapshots through Hibernate
 * repositories without a JSON encode/decode round-trip.
 */
public final class HibernateDataStore implements DataStore {
    private final String name, url, driver, username, password;
    private final DatabasePool pool;
    private final DdlGeneration ddlGeneration;
    private final boolean persistent;
    private final NodeRepository nodes = new NodeRepository();
    private final UserRepository users = new UserRepository(nodes);
    private final GroupRepository groups = new GroupRepository(nodes);
    private final LadderRepository ladders = new LadderRepository();
    private SessionFactory factory;

    public HibernateDataStore(String name, String url, String driver, String username, String password, DatabasePool pool, boolean persistent) {
        this(name, url, driver, username, password, pool, DdlGeneration.UPDATE, persistent);
    }

    public HibernateDataStore(String name, String url, String driver, String username, String password, DatabasePool pool, DdlGeneration ddlGeneration, boolean persistent) {
        this.name = name;
        this.url = url;
        this.driver = driver;
        this.username = username;
        this.password = password;
        this.pool = pool;
        this.ddlGeneration = ddlGeneration;
        this.persistent = persistent;
    }

    @Override
    public synchronized void open() {
        if (factory != null) {
            return;
        }

        PersistenceLogging.suppressRoutineMessages();

        var config = new Configuration().addAnnotatedClass(UserDto.class).addAnnotatedClass(GroupDto.class)
                .addAnnotatedClass(LadderDto.class)
                .addAnnotatedClass(PermissionDto.class).addAnnotatedClass(OptionDto.class)
                .addAnnotatedClass(ParentDto.class).addAnnotatedClass(ContextDto.class)
                .addAnnotatedClass(LadderGroupDto.class);
        config.setProperty(AvailableSettings.JAKARTA_JDBC_URL, url);
        config.setProperty(AvailableSettings.JAKARTA_JDBC_DRIVER, driver);

        if (username != null && !username.isBlank()) {
            config.setProperty(AvailableSettings.JAKARTA_JDBC_USER, username);
        }

        if (password != null && !password.isBlank()) {
            config.setProperty(AvailableSettings.JAKARTA_JDBC_PASSWORD, password);
        }

        config.setProperty(AvailableSettings.HBM2DDL_AUTO, ddlGeneration.hibernateValue());
        config.setProperty(AvailableSettings.SHOW_SQL, "false");
        config.setProperty("hibernate.connection.provider_class", "org.hibernate.hikaricp.internal.HikariCPConnectionProvider");
        config.setProperty("hibernate.hikari.maximumPoolSize", Integer.toString(pool.maximumPoolSize()));
        config.setProperty("hibernate.hikari.minimumIdle", Integer.toString(pool.minimumIdle()));
        config.setProperty("hibernate.hikari.connectionTimeout", Long.toString(pool.connectionTimeout()));
        config.setProperty("hibernate.hikari.maxLifetime", Long.toString(pool.maxLifetime()));

        factory = config.buildSessionFactory();
    }

    @Override
    public Optional<String> get(String category, String key) {
        return switch (category) {
            case "users" -> getUser(UUID.fromString(key)).map(SnapshotCodec::user);
            case "groups" -> getGroup(key).map(SnapshotCodec::group);
            case "ladders" -> getLadder(key).map(SnapshotCodec::ladder);
            default -> throw unknownCategory(category);
        };
    }

    @Override
    public Map<String, String> all(String category) {
        var result = new LinkedHashMap<String, String>();

        switch (category) {
            case "users" -> allUsers().forEach((key, value) -> result.put(key, SnapshotCodec.user(value)));
            case "groups" -> allGroups().forEach((key, value) -> result.put(key, SnapshotCodec.group(value)));
            case "ladders" -> allLadders().forEach((key, value) -> result.put(key, SnapshotCodec.ladder(value)));
            default -> throw unknownCategory(category);
        }

        return Map.copyOf(result);
    }

    @Override
    public void put(String category, String key, String payload) {
        switch (category) {
            case "users" -> putUser(SnapshotCodec.user(payload));
            case "groups" -> putGroup(SnapshotCodec.group(payload));
            case "ladders" -> putLadder(SnapshotCodec.ladder(payload));
            default -> throw unknownCategory(category);
        }
    }

    @Override
    public boolean remove(String category, String key) {
        return switch (category) {
            case "users" -> removeUser(UUID.fromString(key));
            case "groups" -> removeGroup(key);
            case "ladders" -> removeLadder(key);
            default -> throw unknownCategory(category);
        };
    }

    @Override
    public Optional<UserSnapshot> getUser(UUID id) {
        try (var session = session()) {
            return users.find(session, id.toString());
        }
    }

    @Override
    public Map<String, UserSnapshot> allUsers() {
        try (var session = session()) {
            return Map.copyOf(users.all(session));
        }
    }

    @Override
    public void putUser(UserSnapshot user) {
        try (var session = session()) {
            var tx = session.beginTransaction();

            try {
                users.save(session, user);
                tx.commit();
            } catch (RuntimeException error) {
                if (tx.isActive()) {
                    tx.rollback();
                }

                throw error;
            }
        }
    }

    @Override
    public boolean removeUser(UUID id) {
        try (var session = session()) {
            var tx = session.beginTransaction();

            try {
                var changed = users.delete(session, id.toString());
                tx.commit();
                return changed;
            } catch (RuntimeException error) {
                if (tx.isActive()) {
                    tx.rollback();
                }

                throw error;
            }
        }
    }

    @Override
    public Optional<GroupSnapshot> getGroup(String name) {
        try (var session = session()) {
            return groups.find(session, name);
        }
    }

    @Override
    public Map<String, GroupSnapshot> allGroups() {
        try (var session = session()) {
            return Map.copyOf(groups.all(session));
        }
    }

    @Override
    public void putGroup(GroupSnapshot group) {
        try (var session = session()) {
            var tx = session.beginTransaction();

            try {
                groups.save(session, group);
                tx.commit();
            } catch (RuntimeException error) {
                if (tx.isActive()) {
                    tx.rollback();
                }

                throw error;
            }
        }
    }

    @Override
    public boolean removeGroup(String name) {
        try (var session = session()) {
            var tx = session.beginTransaction();

            try {
                var changed = groups.delete(session, name);
                tx.commit();
                return changed;
            } catch (RuntimeException error) {
                if (tx.isActive()) {
                    tx.rollback();
                }

                throw error;
            }
        }
    }

    @Override
    public Optional<LadderSnapshot> getLadder(String name) {
        try (var session = session()) {
            return ladders.find(session, name);
        }
    }

    @Override
    public Map<String, LadderSnapshot> allLadders() {
        try (var session = session()) {
            return Map.copyOf(ladders.all(session));
        }
    }

    @Override
    public void putLadder(LadderSnapshot ladder) {
        try (var session = session()) {
            var tx = session.beginTransaction();

            try {
                ladders.save(session, ladder);
                tx.commit();
            } catch (RuntimeException error) {
                if (tx.isActive()) {
                    tx.rollback();
                }

                throw error;
            }
        }
    }

    @Override
    public boolean removeLadder(String name) {
        try (var session = session()) {
            var tx = session.beginTransaction();

            try {
                var changed = ladders.delete(session, name);
                tx.commit();
                return changed;
            } catch (RuntimeException error) {
                if (tx.isActive()) {
                    tx.rollback();
                }

                throw error;
            }
        }
    }

    @Override
    public String name() {
        return name;
    }

    @Override
    public boolean persistent() {
        return persistent;
    }

    @Override
    public synchronized void close() {
        if (factory != null) {
            factory.close();
            factory = null;
        }
    }

    private Session session() {
        if (factory == null) {
            throw new IllegalStateException("Storage is not open");
        }

        return factory.openSession();
    }

    private IllegalArgumentException unknownCategory(String category) {
        return new IllegalArgumentException("Unsupported relational storage category: " + category);
    }
}
