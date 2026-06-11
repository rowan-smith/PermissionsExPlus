package dev.rono.permissions.core.api;

import dev.rono.permissions.api.RankingException;
import dev.rono.permissions.api.subject.Group;
import dev.rono.permissions.api.subject.SubjectType;
import dev.rono.permissions.api.subject.TimedGroupMembership;
import dev.rono.permissions.api.subject.User;
import dev.rono.permissions.api.world.Worlds;
import dev.rono.permissions.core.DefaultPermissionManager;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import ru.tehkode.permissions.PermissionUser;

public final class ModernUserAdapter extends AbstractModernSubjectAdapter implements User {
    private final PermissionUser user;

    public ModernUserAdapter(PermissionUser user, DefaultPermissionManager manager) {
        super(user, manager);
        this.user = user;
    }

    PermissionUser delegate() {
        return user;
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
        String legacyWorld = ModernWorlds.toLegacy(world);
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
        String legacyWorld = ModernWorlds.toLegacy(world);
        String apiWorld = Worlds.normalize(world);
        List<TimedGroupMembership> memberships = new ArrayList<>();
        for (Map.Entry<String, String> entry : user.getOptions(legacyWorld).entrySet()) {
            String groupName = parseTimedGroupOption(entry.getKey());
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
        String until = user.getOption("group-" + groupName + "-until", ModernWorlds.toLegacy(world), null);
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
        String id = user.getIdentifier();
        user.remove();
        manager.resetUser(id);
    }

    @Override
    public Group promote(String ladderName) throws RankingException {
        return promote(null, ladderName);
    }

    @Override
    public Group promote(User promoter, String ladderName) throws RankingException {
        try {
            return ModernSubjects.wrapGroup(user.promote(ModernSubjects.optionalUser(promoter), ladderName), manager);
        } catch (ru.tehkode.permissions.exceptions.RankingException ex) {
            throw ModernSubjects.toRankingException(ex);
        }
    }

    @Override
    public Group demote(String ladderName) throws RankingException {
        return demote(null, ladderName);
    }

    @Override
    public Group demote(User demoter, String ladderName) throws RankingException {
        try {
            return ModernSubjects.wrapGroup(user.demote(ModernSubjects.optionalUser(demoter), ladderName), manager);
        } catch (ru.tehkode.permissions.exceptions.RankingException ex) {
            throw ModernSubjects.toRankingException(ex);
        }
    }

    @Override
    public boolean isRanked(String ladderName) {
        return user.isRanked(ladderName);
    }

    @Override
    public int rank(String ladderName) {
        return user.getRank(ladderName);
    }

    private static String parseTimedGroupOption(String option) {
        if (!option.startsWith("group-") || !option.endsWith("-until")) {
            return null;
        }
        return option.substring("group-".length(), option.length() - "-until".length());
    }
}
