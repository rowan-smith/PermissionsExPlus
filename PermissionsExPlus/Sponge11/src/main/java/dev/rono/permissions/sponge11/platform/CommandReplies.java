package dev.rono.permissions.sponge11.platform;

import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import net.kyori.adventure.audience.Audience;
import net.kyori.adventure.audience.MessageType;
import net.kyori.adventure.identity.Identity;
import net.kyori.adventure.text.Component;
import org.spongepowered.api.Server;
import org.spongepowered.api.command.CommandCause;
import org.spongepowered.api.network.RconConnection;

/**
 * Where a command's replies go.
 *
 * <p>
 * Normally that is {@link CommandCause#audience()}. For a command sent over
 * RCON, SpongeVanilla puts the RCON client and the server in the cause, but not
 * the RCON client's own command source, so {@code audience()} falls back to the
 * console and the RCON caller gets an empty reply. That source is an
 * {@link RconConnection} held by the RCON client, and replies go to it instead.
 * </p>
 *
 * <p>
 * The RCON source only implements the three-argument Adventure method
 * {@code sendMessage(Identity, Component, MessageType)}. Current Adventure no
 * longer calls it from {@code sendMessage(Component)}, so replies call it
 * directly.
 * </p>
 */
public final class CommandReplies {
    /** Per class in the cause, the field holding its RCON source, if it has one. */
    private static final Map<Class<?>, Optional<Field>> RCON_FIELDS = new ConcurrentHashMap<>();

    private CommandReplies() {}

    public static Audience audience(CommandCause cause) {
        var audience = cause.audience();

        if (audience instanceof RconConnection) {
            return audience;
        }

        // The fallback differs by version (the server on API 19+, the system
        // subject on API 11), so the RCON client in the cause decides.
        return rconSource(cause).map(CommandReplies::toRcon).orElse(audience);
    }

    /** The RCON source of the first object in the cause that holds one. */
    private static Optional<RconConnection> rconSource(CommandCause cause) {
        for (Object source : cause.cause().all()) {
            if (source instanceof Server) {
                continue;
            }

            var field = RCON_FIELDS.computeIfAbsent(source.getClass(), CommandReplies::findRconField);

            if (field.isPresent()) {
                try {
                    var connection = (RconConnection) field.get().get(source);

                    if (connection != null) {
                        return Optional.of(connection);
                    }
                } catch (IllegalAccessException | RuntimeException unavailable) {
                    return Optional.empty();
                }
            }
        }

        return Optional.empty();
    }

    private static Optional<Field> findRconField(Class<?> type) {
        for (Class<?> current = type; current != null; current = current.getSuperclass()) {
            for (Field field : current.getDeclaredFields()) {
                if (!Modifier.isStatic(field.getModifiers()) && RconConnection.class.isAssignableFrom(field.getType())) {
                    try {
                        field.setAccessible(true);
                        return Optional.of(field);
                    } catch (RuntimeException denied) {
                        return Optional.empty();
                    }
                }
            }
        }

        return Optional.empty();
    }

    @SuppressWarnings("deprecation")
    private static Audience toRcon(RconConnection connection) {
        return new Audience() {
            @Override
            public void sendMessage(Component message) {
                connection.sendMessage(Identity.nil(), message, MessageType.SYSTEM);
            }
        };
    }
}
