package dev.rono.permissions.core.api.pex;

import dev.rono.permissions.api.group.Group;
import dev.rono.permissions.api.group.GroupAlreadyExistsException;
import dev.rono.permissions.api.group.GroupManager;
import dev.rono.permissions.api.group.GroupNotFoundException;
import dev.rono.permissions.api.user.User;
import dev.rono.permissions.api.user.UserAlreadyExistsException;
import dev.rono.permissions.api.user.UserManager;
import dev.rono.permissions.api.user.UserNotFoundException;
import dev.rono.permissions.core.DefaultPermissionManager;
import dev.rono.permissions.core.commands.CoreCommandService;
import ru.tehkode.permissions.PermissionUser;

import java.util.HashSet;
import java.util.Objects;
import java.util.Optional;
import java.util.UUID;
import java.util.function.Predicate;

final class DefaultUserManager implements UserManager {

    private final DefaultPermissionManager manager;

    DefaultUserManager(DefaultPermissionManager manager) {
        this.manager = manager;
    }

    @Override
    public Optional<User> findUser(UUID uuid) {
        if (uuid == null || !manager.getBackend().hasUser(uuid.toString())) {
            return Optional.empty();
        }
        return Optional.of(wrapUser(uuid, manager.getUser(uuid)));
    }

    @Override
    public Optional<User> findUser(String name) {
        if (name == null || name.isEmpty()) {
            return Optional.empty();
        }
        UUID resolved = tryUuid(name);
        if (resolved != null && manager.getBackend().hasUser(resolved.toString())) {
            return Optional.of(wrapUser(resolved, manager.getUser(resolved)));
        }
        if (manager.getBackend().hasUser(name)) {
            var user = manager.getUser(name);
            return Optional.of(wrapUser(parseId(user.getIdentifier()), user));
        }
        return Optional.empty();
    }

    @Override
    public User getUser(UUID uuid) throws UserNotFoundException {
        if (!exists(uuid)) {
            throw new UserNotFoundException(uuid);
        }
        return wrapUser(uuid, manager.getUser(uuid));
    }

    @Override
    public User getUser(String name) throws UserNotFoundException {
        var found = findUser(name);
        if (found.isEmpty()) {
            throw new UserNotFoundException(name);
        }
        return found.get();
    }

    @Override
    public User createUser(UUID uuid) throws UserAlreadyExistsException {
        if (exists(uuid)) {
            throw new UserAlreadyExistsException(uuid);
        }
        var backend = manager.getBackend();
        backend.getUserData(uuid.toString());
        var created = manager.getUser(uuid);
        created.save();
        return wrapUser(uuid, created);
    }

    @Override
    public User createUser(String name) throws UserAlreadyExistsException {
        if (exists(name)) {
            throw new UserAlreadyExistsException(name);
        }
        var backend = manager.getBackend();
        backend.getUserData(name);
        var created = manager.getUser(name);
        created.save();
        return wrapUser(parseId(created.getIdentifier()), created);
    }

    @Override
    public boolean exists(UUID uuid) {
        return uuid != null && manager.getBackend().hasUser(uuid.toString());
    }

    @Override
    public boolean exists(String name) {
        if (name == null || name.isEmpty()) {
            return false;
        }
        UUID resolved = tryUuid(name);
        if (resolved != null && manager.getBackend().hasUser(resolved.toString())) {
            return true;
        }
        return manager.getBackend().hasUser(name);
    }

    @Override
    public int count() {
        return manager.getBackend().getUserIdentifiers().size();
    }

    @Override
    public int count(Predicate<User> filter) {
        Objects.requireNonNull(filter, "filter");
        int matched = 0;
        for (String identifier : manager.getBackend().getUserIdentifiers()) {
            var user = findUser(identifier);
            if (user.isPresent() && filter.test(user.get())) {
                matched++;
            }
        }
        return matched;
    }

    private User wrapUser(UUID id, PermissionUser delegate) {
        return new UserImpl(id, delegate, manager);
    }

    private static UUID parseId(String identifier) {
        try {
            return UUID.fromString(identifier);
        } catch (IllegalArgumentException ex) {
            return UUID.nameUUIDFromBytes(identifier.getBytes(java.nio.charset.StandardCharsets.UTF_8));
        }
    }

    private static UUID tryUuid(String value) {
        try {
            return UUID.fromString(value);
        } catch (IllegalArgumentException ex) {
            return null;
        }
    }
}

final class DefaultGroupManager implements GroupManager {

    private final DefaultPermissionManager manager;

    DefaultGroupManager(DefaultPermissionManager manager) {
        this.manager = manager;
    }

    @Override
    public Optional<Group> findGroup(String name) {
        if (name == null || name.isEmpty() || !manager.getBackend().hasGroup(name)) {
            return Optional.empty();
        }
        return Optional.of(new GroupImpl(name, manager.getGroup(name), manager));
    }

    @Override
    public Group getGroup(String name) throws GroupNotFoundException {
        if (!exists(name)) {
            throw new GroupNotFoundException(name);
        }
        return new GroupImpl(name, manager.getGroup(name), manager);
    }

    @Override
    public Group createGroup(String name) throws GroupAlreadyExistsException {
        if (exists(name)) {
            throw new GroupAlreadyExistsException(name);
        }
        manager.getBackend().getGroupData(name);
        var created = manager.getGroup(name);
        created.save();
        return new GroupImpl(name, created, manager);
    }

    @Override
    public boolean exists(String name) {
        return name != null && !name.isEmpty() && manager.getBackend().hasGroup(name);
    }

    @Override
    public int count() {
        return manager.getBackend().getGroupNames().size();
    }

    @Override
    public int count(Predicate<Group> filter) {
        Objects.requireNonNull(filter, "filter");
        int matched = 0;
        for (String name : manager.getBackend().getGroupNames()) {
            var group = findGroup(name);
            if (group.isPresent() && filter.test(group.get())) {
                matched++;
            }
        }
        return matched;
    }
}

final class DefaultWorldManager implements dev.rono.permissions.api.world.WorldManager {

    private final DefaultPermissionManager manager;

    DefaultWorldManager(DefaultPermissionManager manager) {
        this.manager = manager;
    }

    @Override
    public Optional<dev.rono.permissions.api.world.World> findWorld(String name) {
        if (!exists(name)) {
            return Optional.empty();
        }
        return Optional.of(new WorldImpl(name));
    }

    @Override
    public dev.rono.permissions.api.world.World getWorld(String name)
            throws dev.rono.permissions.api.world.WorldNotFoundException {
        if (!exists(name)) {
            throw new dev.rono.permissions.api.world.WorldNotFoundException(name);
        }
        return new WorldImpl(name);
    }

    @Override
    public dev.rono.permissions.api.world.World createWorld(String name)
            throws dev.rono.permissions.api.world.WorldAlreadyExistsException {
        if (exists(name)) {
            throw new dev.rono.permissions.api.world.WorldAlreadyExistsException(name);
        }
        manager.getBackend().setWorldInheritance(name, java.util.Collections.emptyList());
        return new WorldImpl(name);
    }

    @Override
    public boolean exists(String name) {
        if (name == null || name.isEmpty()) {
            return false;
        }
        return manager.getWorldNames().contains(name)
                || manager.getBackend().getAllWorldInheritance().containsKey(name);
    }

    @Override
    public int count() {
        return allWorldNames().size();
    }

    @Override
    public int count(Predicate<dev.rono.permissions.api.world.World> filter) {
        Objects.requireNonNull(filter, "filter");
        int matched = 0;
        for (String name : allWorldNames()) {
            var world = findWorld(name);
            if (world.isPresent() && filter.test(world.get())) {
                matched++;
            }
        }
        return matched;
    }

    private java.util.Set<String> allWorldNames() {
        var names = new HashSet<String>();
        names.addAll(manager.getWorldNames());
        names.addAll(manager.getBackend().getAllWorldInheritance().keySet());
        return names;
    }
}

final class DefaultLadderManager implements dev.rono.permissions.api.ladder.LadderManager {

    private final DefaultPermissionManager manager;

    DefaultLadderManager(DefaultPermissionManager manager) {
        this.manager = manager;
    }

    @Override
    public Optional<dev.rono.permissions.api.ladder.Ladder> findLadder(String name) {
        if (!exists(name)) {
            return Optional.empty();
        }
        return Optional.of(new LadderImpl(name));
    }

    @Override
    public dev.rono.permissions.api.ladder.Ladder getLadder(String name)
            throws dev.rono.permissions.api.ladder.LadderNotFoundException {
        if (!exists(name)) {
            throw new dev.rono.permissions.api.ladder.LadderNotFoundException(name);
        }
        return new LadderImpl(name);
    }

    @Override
    public dev.rono.permissions.api.ladder.Ladder createLadder(String name)
            throws dev.rono.permissions.api.ladder.LadderAlreadyExistsException {
        if (exists(name)) {
            throw new dev.rono.permissions.api.ladder.LadderAlreadyExistsException(name);
        }
        return new LadderImpl(name);
    }

    @Override
    public boolean exists(String name) {
        if (name == null || name.isEmpty()) {
            return false;
        }
        return commandService().knownLadders().contains(name);
    }

    @Override
    public int count() {
        return commandService().knownLadders().size();
    }

    @Override
    public int count(Predicate<dev.rono.permissions.api.ladder.Ladder> filter) {
        Objects.requireNonNull(filter, "filter");
        int matched = 0;
        for (String name : commandService().knownLadders()) {
            var ladder = findLadder(name);
            if (ladder.isPresent() && filter.test(ladder.get())) {
                matched++;
            }
        }
        return matched;
    }

    private CoreCommandService commandService() {
        return new CoreCommandService(manager);
    }

    @Override
    public dev.rono.permissions.api.group.Group promote(
            dev.rono.permissions.api.user.User user, String ladderName)
            throws dev.rono.permissions.api.RankingException {
        return promote(null, user, ladderName);
    }

    @Override
    public dev.rono.permissions.api.group.Group promote(
            dev.rono.permissions.api.user.User actor,
            dev.rono.permissions.api.user.User user,
            String ladderName)
            throws dev.rono.permissions.api.RankingException {
        try {
            return SubjectSupport.wrapGroup(
                    SubjectSupport.requireUser(user).promote(SubjectSupport.optionalUser(actor), ladderName),
                    manager);
        } catch (ru.tehkode.permissions.exceptions.RankingException ex) {
            throw SubjectSupport.toRankingException(ex);
        }
    }

    @Override
    public dev.rono.permissions.api.group.Group demote(
            dev.rono.permissions.api.user.User user, String ladderName)
            throws dev.rono.permissions.api.RankingException {
        return demote(null, user, ladderName);
    }

    @Override
    public dev.rono.permissions.api.group.Group demote(
            dev.rono.permissions.api.user.User actor,
            dev.rono.permissions.api.user.User user,
            String ladderName)
            throws dev.rono.permissions.api.RankingException {
        try {
            return SubjectSupport.wrapGroup(
                    SubjectSupport.requireUser(user).demote(SubjectSupport.optionalUser(actor), ladderName),
                    manager);
        } catch (ru.tehkode.permissions.exceptions.RankingException ex) {
            throw SubjectSupport.toRankingException(ex);
        }
    }

    @Override
    public boolean isRanked(dev.rono.permissions.api.user.User user, String ladderName) {
        return SubjectSupport.requireUser(user).isRanked(ladderName);
    }

    @Override
    public int rank(dev.rono.permissions.api.user.User user, String ladderName) {
        return SubjectSupport.requireUser(user).getRank(ladderName);
    }
}
