package dev.rono.permissions.core.api.pex;

import dev.rono.permissions.api.permission.PermissionHolder;
import dev.rono.permissions.api.subject.SubjectType;
import dev.rono.permissions.api.subject.TimedGroupMembership;
import dev.rono.permissions.api.user.User;
import dev.rono.permissions.api.world.Worlds;
import dev.rono.permissions.core.DefaultPermissionManager;
import dev.rono.permissions.core.api.ModernWorlds;
import ru.tehkode.permissions.PermissionUser;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

public final class UserImpl extends AbstractPermissionSubjectAdapter implements User {

    private final UUID id;
    private final PermissionUser user;
    private final PermissionHolder holder;

    public UserImpl(UUID id, PermissionUser user, DefaultPermissionManager manager) {
        super(user, manager);
        this.id = id;
        this.user = user;
        this.holder = new UserPermissionHolder(id);
    }

    PermissionUser delegate() {
        return user;
    }

    @Override
    public UUID getId() {
        return id;
    }

    @Override
    public String getName() {
        String name = user.getName();
        return name != null ? name : id.toString();
    }

    @Override
    public PermissionHolder asHolder() {
        return holder;
    }

    @Override
    public SubjectType type() {
        return SubjectType.USER;
    }

    @Override
    public Optional<UUID> uniqueId() {
        try {
            return Optional.of(UUID.fromString(user.getIdentifier()));
        } catch (IllegalArgumentException ignored) {
            return Optional.empty();
        }
    }

    @Override
    public List<String> groups(String world, boolean inherit) {
        var legacyWorld = ModernWorlds.toLegacy(world);
        return inherit ? user.getParentIdentifiers(legacyWorld) : user.getOwnParentIdentifiers(legacyWorld);
    }

    @Override
    public boolean inGroup(String groupName, String world, boolean inherit) {
        return user.inGroup(groupName, ModernWorlds.toLegacy(world), inherit);
    }

    @Override
    public void addGroup(String groupName, String world) {
        user.addGroup(groupName, ModernWorlds.toLegacy(world));
    }

    @Override
    public void addGroup(String groupName, String world, int lifetimeSeconds) {
        user.addGroup(groupName, ModernWorlds.toLegacy(world), lifetimeSeconds);
    }

    @Override
    public void removeGroup(String groupName, String world) {
        user.removeGroup(groupName, ModernWorlds.toLegacy(world));
    }

    @Override
    public List<TimedGroupMembership> timedGroupMemberships(String world) {
        var legacyWorld = ModernWorlds.toLegacy(world);
        var apiWorld = Worlds.normalize(world);
        var memberships = new ArrayList<TimedGroupMembership>();
        for (Map.Entry<String, String> entry : user.getOptions(legacyWorld).entrySet()) {
            var groupName = parseTimedGroupOption(entry.getKey());
            if (groupName == null) {
                continue;
            }
            memberships.add(new TimedGroupMembership(
                    groupName, apiWorld, groupMembershipRemainingSeconds(groupName, world)));
        }
        return List.copyOf(memberships);
    }

    @Override
    public int groupMembershipRemainingSeconds(String groupName, String world) {
        var until = user.getOption("group-" + groupName + "-until", ModernWorlds.toLegacy(world), null);
        if (until == null) {
            return 0;
        }
        try {
            long expiry = Long.parseLong(until);
            return (int) Math.max(0, expiry - (System.currentTimeMillis() / 1000L));
        } catch (NumberFormatException ignored) {
            return 0;
        }
    }

    @Override
    public void delete() {
        var identifier = user.getIdentifier();
        user.remove();
        manager.resetUser(identifier);
    }

    private static String parseTimedGroupOption(String option) {
        if (!option.startsWith("group-") || !option.endsWith("-until")) {
            return null;
        }
        return option.substring("group-".length(), option.length() - "-until".length());
    }
}
