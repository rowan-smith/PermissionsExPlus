package dev.rono.permissions.core.api.pex;

import dev.rono.permissions.api.permission.PermissionContext;
import dev.rono.permissions.api.permission.PermissionHolder;
import dev.rono.permissions.api.subject.SubjectType;
import dev.rono.permissions.api.subject.TimedGroupMembership;
import dev.rono.permissions.api.user.User;
import dev.rono.permissions.api.world.Worlds;
import dev.rono.permissions.core.DefaultPermissionManager;
import dev.rono.permissions.core.api.ContextPermissionEvaluator;
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
    public List<String> groups(PermissionContext context, boolean inherit) {
        var legacyWorld = storageRealm(context);
        return inherit ? user.getParentIdentifiers(legacyWorld) : user.getOwnParentIdentifiers(legacyWorld);
    }

    @Override
    public boolean inGroup(String groupName, PermissionContext context, boolean inherit) {
        return user.inGroup(groupName, storageRealm(context), inherit);
    }

    @Override
    public void addGroup(String groupName, PermissionContext context) {
        user.addGroup(groupName, storageRealm(context));
    }

    @Override
    public void addGroup(String groupName, PermissionContext context, int lifetimeSeconds) {
        user.addGroup(groupName, storageRealm(context), lifetimeSeconds);
    }

    @Override
    public void removeGroup(String groupName, PermissionContext context) {
        user.removeGroup(groupName, storageRealm(context));
    }

    @Override
    public List<TimedGroupMembership> timedGroupMemberships(PermissionContext context) {
        var legacyWorld = storageRealm(context);
        var memberships = new ArrayList<TimedGroupMembership>();
        for (Map.Entry<String, String> entry : user.getOptions(legacyWorld).entrySet()) {
            var groupName = parseTimedGroupOption(entry.getKey());
            if (groupName == null) {
                continue;
            }
            memberships.add(new TimedGroupMembership(
                    groupName, context, groupMembershipRemainingSeconds(groupName, context)));
        }
        return List.copyOf(memberships);
    }

    @Override
    public List<TimedGroupMembership> allTimedGroupMemberships() {
        var memberships = new ArrayList<TimedGroupMembership>();
        memberships.addAll(timedGroupMemberships(PermissionContext.global()));
        for (String realm : configuredRealms()) {
            if (!Worlds.isGlobal(realm)) {
                memberships.addAll(timedGroupMemberships(ContextPermissionEvaluator.fromLegacyWorld(realm)));
            }
        }
        return List.copyOf(memberships);
    }

    @Override
    public int groupMembershipRemainingSeconds(String groupName, PermissionContext context) {
        var until = user.getOption("group-" + groupName + "-until", storageRealm(context), null);
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
