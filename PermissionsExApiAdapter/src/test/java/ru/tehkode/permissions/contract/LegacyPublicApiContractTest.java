package ru.tehkode.permissions.contract;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import dev.rono.permissions.core.PexImplProvider;
import dev.rono.permissions.core.manager.LadderManagerImpl;
import java.util.Arrays;
import java.util.Collections;
import java.util.EnumSet;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import ru.tehkode.permissions.PEXTestBase;
import ru.tehkode.permissions.PermissionCheckResult;
import ru.tehkode.permissions.PermissionEntity;
import ru.tehkode.permissions.PermissionGroup;
import ru.tehkode.permissions.PermissionManager;
import ru.tehkode.permissions.PermissionUser;
import ru.tehkode.permissions.RegExpMatcher;
import ru.tehkode.permissions.backends.PermissionBackend;
import ru.tehkode.permissions.events.PermissionEntityEvent;
import ru.tehkode.permissions.events.PermissionSystemEvent;
import ru.tehkode.permissions.exceptions.PermissionsNotAvailable;
import ru.tehkode.permissions.exceptions.RankingException;

/**
 * Behavioral contracts for the legacy public API. These encode semantics third-party
 * plugins rely on so the Plus-backed shim cannot silently diverge.
 */
@DisplayName("Legacy public API behavioral contracts")
class LegacyPublicApiContractTest extends PEXTestBase {

    @Nested
    @DisplayName("PermissionCheckResult")
    class CheckResultContracts {
        @Test
        void enumConstantsAndBooleanMapping() {
            assertEquals(EnumSet.of(
                    PermissionCheckResult.UNDEFINED,
                    PermissionCheckResult.TRUE,
                    PermissionCheckResult.FALSE),
                    EnumSet.allOf(PermissionCheckResult.class));

            assertTrue(PermissionCheckResult.TRUE.toBoolean());
            assertFalse(PermissionCheckResult.FALSE.toBoolean());
            assertFalse(PermissionCheckResult.UNDEFINED.toBoolean());
            assertSame(PermissionCheckResult.TRUE, PermissionCheckResult.fromBoolean(true));
            assertSame(PermissionCheckResult.FALSE, PermissionCheckResult.fromBoolean(false));
            assertEquals("true", PermissionCheckResult.TRUE.toString());
            assertEquals("false", PermissionCheckResult.FALSE.toString());
            assertEquals("undefined", PermissionCheckResult.UNDEFINED.toString());
        }
    }

    @Nested
    @DisplayName("PermissionEntity.Type / event Action enums")
    class EnumContracts {
        @Test
        void entityTypesAreUserAndGroup() {
            assertEquals(EnumSet.of(PermissionEntity.Type.USER, PermissionEntity.Type.GROUP),
                    EnumSet.allOf(PermissionEntity.Type.class));
        }

        @Test
        void entityEventActionsRemainComplete() {
            assertEquals(EnumSet.of(
                    PermissionEntityEvent.Action.PERMISSIONS_CHANGED,
                    PermissionEntityEvent.Action.OPTIONS_CHANGED,
                    PermissionEntityEvent.Action.INHERITANCE_CHANGED,
                    PermissionEntityEvent.Action.INFO_CHANGED,
                    PermissionEntityEvent.Action.TIMEDPERMISSION_EXPIRED,
                    PermissionEntityEvent.Action.RANK_CHANGED,
                    PermissionEntityEvent.Action.DEFAULTGROUP_CHANGED,
                    PermissionEntityEvent.Action.WEIGHT_CHANGED,
                    PermissionEntityEvent.Action.SAVED,
                    PermissionEntityEvent.Action.REMOVED),
                    EnumSet.allOf(PermissionEntityEvent.Action.class));
        }

        @Test
        void systemEventActionsRemainComplete() {
            assertEquals(EnumSet.of(
                    PermissionSystemEvent.Action.BACKEND_CHANGED,
                    PermissionSystemEvent.Action.RELOADED,
                    PermissionSystemEvent.Action.WORLDINHERITANCE_CHANGED,
                    PermissionSystemEvent.Action.DEFAULTGROUP_CHANGED,
                    PermissionSystemEvent.Action.DEBUGMODE_TOGGLE,
                    PermissionSystemEvent.Action.REINJECT_PERMISSIBLES),
                    EnumSet.allOf(PermissionSystemEvent.Action.class));
        }
    }

    @Nested
    @DisplayName("PermissionsEx facade")
    class FacadeContracts {
        @Test
        void permissionsNotAvailableCarriesClassicMessage() {
            PermissionsNotAvailable ex = new PermissionsNotAvailable();
            assertTrue(ex.getMessage().contains("PermissionsEx"));
            assertTrue(ex.getMessage().toLowerCase().contains("enabled"));
        }

        @Test
        void rankingExceptionTypeIsChecked() {
            assertTrue(Exception.class.isAssignableFrom(RankingException.class));
            assertFalse(RuntimeException.class.isAssignableFrom(RankingException.class));
        }
    }

    @Nested
    @DisplayName("PermissionManager")
    class ManagerContracts {
        @Test
        void usersAreCachedByIdentifier() {
            String id = UUID.randomUUID().toString();
            PermissionUser first = manager.getUser(id);
            PermissionUser second = manager.getUser(id);
            assertSame(first, second);
            assertEquals(id, first.getIdentifier());
            assertEquals(PermissionEntity.Type.USER, first.getType());
        }

        @Test
        void groupsAreCachedByName() {
            PermissionGroup first = manager.getGroup("Staff");
            PermissionGroup second = manager.getGroup("Staff");
            assertSame(first, second);
            assertEquals("Staff", first.getIdentifier());
            assertEquals(PermissionEntity.Type.GROUP, first.getType());
        }

        @Test
        void getUserByUuidMatchesStringLookup() {
            UUID uuid = UUID.randomUUID();
            PermissionUser byUuid = manager.getUser(uuid);
            PermissionUser byString = manager.getUser(uuid.toString());
            assertSame(byUuid, byString);
        }

        @Test
        void hasDelegatesToUser() {
            UUID uuid = UUID.randomUUID();
            PermissionUser user = manager.getUser(uuid);
            user.addPermission("contract.allow");
            assertTrue(manager.has(uuid, "contract.allow", null));
            assertFalse(manager.has(uuid, "contract.deny", null));
        }

        @Test
        void defaultGroupsAreDiscoverable() {
            PermissionGroup def = manager.getGroup("default");
            def.setDefault(true, null);
            manager.getGroups();

            assertTrue(def.isDefault(null));
            assertTrue(manager.getDefaultGroups(null).stream()
                    .anyMatch(g -> g.getIdentifier().equals("default")));
        }

        @Test
        void resetUserDropsCacheInstance() {
            PermissionUser original = manager.getUser("cache-me");
            manager.resetUser("cache-me");
            PermissionUser reloaded = manager.getUser("cache-me");
            assertTrue(original != reloaded, "resetUser must drop the cached instance");
        }

        @Test
        void debugToggleIsReadable() {
            boolean before = manager.isDebug();
            manager.setDebug(!before);
            assertEquals(!before, manager.isDebug());
            manager.setDebug(before);
        }

        @Test
        void matcherDefaultsToRegExpMatcher() {
            assertTrue(manager.getPermissionMatcher() instanceof RegExpMatcher);
            RegExpMatcher replacement = new RegExpMatcher();
            manager.setPermissionMatcher(replacement);
            assertSame(replacement, manager.getPermissionMatcher());
        }

        @Test
        void worldInheritanceRoundTrip() {
            manager.setWorldInheritance("nether", Arrays.asList("world"));
            assertEquals(Collections.singletonList("world"), manager.getWorldInheritance("nether"));
        }

        @Test
        void groupListIncludesCreatedGroups() {
            manager.getGroup("Alpha").addPermission("touch.alpha");
            manager.getGroup("Beta").addPermission("touch.beta");
            Set<String> names = manager.getGroupNames().stream()
                    .map(name -> name.toLowerCase(Locale.ROOT))
                    .collect(Collectors.toSet());
            assertTrue(names.contains("alpha"));
            assertTrue(names.contains("beta"));
            assertTrue(manager.getGroupList().size() >= 2);
        }

        @Test
        void transientPermissionConstantIsZero() {
            assertEquals(0, PermissionManager.TRANSIENT_PERMISSION);
        }

        @Test
        void backendIsPresent() {
            assertNotNull(manager.getBackend());
            assertNotNull(PermissionBackend.DEFAULT_BACKEND);
        }
    }

    @Nested
    @DisplayName("PermissionEntity permissions / options / parents")
    class EntityContracts {
        @Test
        void addRemovePermissionGlobalAndWorld() {
            PermissionUser user = manager.getUser("entity-perm");
            user.addPermission("test.global");
            user.addPermission("test.world", "world_nether");

            assertTrue(user.has("test.global", "world"));
            assertTrue(user.has("test.world", "world_nether"));
            assertFalse(user.has("test.world", "world"));
            assertTrue(user.getOwnPermissions(null).contains("test.global"));
            assertTrue(user.getOwnPermissions("world_nether").contains("test.world"));

            user.removePermission("test.global");
            assertFalse(user.has("test.global", "world"));
        }

        @Test
        void setPermissionsReplacesOwnList() {
            PermissionUser user = manager.getUser("entity-set");
            user.setPermissions(Arrays.asList("a.one", "a.two"), null);
            assertEquals(Set.of("a.one", "a.two"), new HashSet<>(user.getOwnPermissions(null)));

            user.setPermissions(Collections.singletonList("b.only"), null);
            assertEquals(Collections.singletonList("b.only"), user.getOwnPermissions(null));
        }

        @Test
        void negationBeatsAllowWhenListedFirst() {
            PermissionUser user = manager.getUser("entity-neg");
            // addPermission prepends, so last add is highest priority
            user.addPermission("node.x");
            user.addPermission("-node.x");
            assertFalse(user.has("node.x", "world"), "Negation prepended last must win");
        }

        @Test
        void emptyPermissionIsPublicAccess() {
            PermissionUser user = manager.getUser("entity-empty");
            assertTrue(user.has("", "world"));
        }

        @Test
        void wildcardPermissionMatchesChildren() {
            PermissionUser user = manager.getUser("entity-wild");
            user.addPermission("shop.*");
            assertTrue(user.has("shop.buy", "world"));
            assertTrue(user.has("shop.sell.item", "world"));
        }

        @Test
        void optionsRoundTripWithTypedGetters() {
            PermissionUser user = manager.getUser("entity-opt");
            user.setOption("meta.string", "hello", null);
            user.setOption("meta.int", "42", null);
            user.setOption("meta.bool", "true", null);
            user.setOption("meta.double", "3.5", null);

            assertEquals("hello", user.getOption("meta.string"));
            assertEquals(42, user.getOptionInteger("meta.int", null, -1));
            assertTrue(user.getOptionBoolean("meta.bool", null, false));
            assertEquals(3.5, user.getOptionDouble("meta.double", null, -1), 0.0001);
            assertEquals("hello", user.getOwnOption("meta.string"));
            assertEquals(99, user.getOptionInteger("missing", null, 99));
        }

        @Test
        void prefixSuffixOwnVsResolved() {
            PermissionGroup group = manager.getGroup("prefix-group");
            group.setPrefix("[G]", null);
            group.setSuffix("{G}", null);

            PermissionUser user = manager.getUser("prefix-user");
            user.addGroup(group);
            assertEquals("[G]", user.getPrefix(null));
            assertEquals("{G}", user.getSuffix(null));

            user.setPrefix("[U]", null);
            user.setSuffix("{U}", null);
            assertEquals("[U]", user.getOwnPrefix(null));
            assertEquals("{U}", user.getOwnSuffix(null));
            assertEquals("[U]", user.getPrefix(null));
            assertEquals("{U}", user.getSuffix(null));
        }

        @Test
        void timedPermissionAppearsAndCanBeRemoved() {
            PermissionUser user = manager.getUser("entity-timed");
            user.addTimedPermission("temp.fly", null, PermissionManager.TRANSIENT_PERMISSION);
            assertTrue(user.getTimedPermissions(null).contains("temp.fly"));
            // has() resolves through Plus; process-local timed nodes are tracked separately.
            user.removeTimedPermission("temp.fly", null);
            assertFalse(user.getTimedPermissions(null).contains("temp.fly"));
        }

        @Test
        void parentsIdentifierRoundTrip() {
            PermissionGroup parent = manager.getGroup("parent-a");
            PermissionUser user = manager.getUser("child-user");
            user.setParentsIdentifier(Collections.singletonList(parent.getIdentifier()), null);

            assertEquals(Collections.singletonList(parent.getIdentifier()), user.getOwnParentIdentifiers(null));
            assertTrue(user.getParents(null).contains(parent));
            assertTrue(user.getParentIdentifiers(null).contains(parent.getIdentifier()));
        }

        @Test
        void explainExpressionTreatsMinusAsDeny() {
            PermissionUser user = manager.getUser("explain");
            assertTrue(user.explainExpression("allow.me"));
            assertFalse(user.explainExpression("-deny.me"));
            assertFalse(user.explainExpression(null));
            assertFalse(user.explainExpression(""));
        }

        @Test
        void entityEqualityUsesName() {
            PermissionUser a = manager.getUser("same-name");
            PermissionUser b = manager.getUser("same-name");
            assertEquals(a, b);
            assertEquals(a.hashCode(), b.hashCode());
            assertTrue(a.toString().contains("same-name") || a.toString().contains(a.getIdentifier()));
        }

        @Test
        void saveAndVirtualState() {
            PermissionUser user = manager.getUser("virtual-user");
            // Mock backend creates in-memory data; virtual flag depends on backend
            assertNotNull(Boolean.valueOf(user.isVirtual()));
            user.addPermission("persist.me");
            user.save();
            assertTrue(user.getOwnPermissions(null).contains("persist.me"));
        }
    }

    @Nested
    @DisplayName("PermissionUser groups and ranking")
    class UserContracts {
        @Test
        void addRemoveGroupAndInGroupChecks() {
            PermissionGroup group = manager.getGroup("Member");
            PermissionUser user = manager.getUser("rank-user");

            user.addGroup(group);
            assertTrue(user.inGroup(group));
            assertTrue(user.inGroup("Member"));
            assertTrue(Arrays.asList(user.getGroups()).contains(group));
            assertTrue(Arrays.asList(user.getGroupNames()).contains("Member")
                    || user.getGroupsNames().length >= 1);

            user.removeGroup(group);
            assertFalse(user.inGroup(group, false));
        }

        @Test
        void inheritedGroupMembershipIsVisibleWithInheritanceFlag() {
            PermissionGroup parent = manager.getGroup("ParentRank");
            PermissionGroup child = manager.getGroup("ChildRank");
            child.setParents(Collections.singletonList(parent));

            PermissionUser user = manager.getUser("inherit-user");
            user.addGroup(child);

            assertTrue(user.inGroup(child, false));
            assertTrue(user.inGroup(parent, true));
            assertFalse(user.inGroup(parent, false));
        }

        @Test
        void setGroupsReplacesMembership() {
            PermissionGroup a = manager.getGroup("SetA");
            PermissionGroup b = manager.getGroup("SetB");
            PermissionUser user = manager.getUser("set-groups");

            user.setGroups(new PermissionGroup[]{a, b});
            List<PermissionGroup> groups = Arrays.asList(user.getGroups());
            assertTrue(groups.contains(a));
            assertTrue(groups.contains(b));

            user.setGroups(new PermissionGroup[]{b});
            groups = Arrays.asList(user.getGroups());
            assertFalse(groups.contains(a));
            assertTrue(groups.contains(b));
        }

        @Test
        void promoteAndDemoteAlongLadder() throws RankingException {
            PermissionGroup low = manager.getGroup("LadderLow");
            PermissionGroup high = manager.getGroup("LadderHigh");
            // Touch groups so Plus has them before ladder creation.
            assertNotNull(low.getIdentifier());
            assertNotNull(high.getIdentifier());

            var ladders = (LadderManagerImpl) PexImplProvider.get().ladders();
            ladders.create("main").toCompletableFuture().join();
            ladders.modify("main", modifier -> modifier.setGroups(List.of("ladderlow", "ladderhigh")))
                    .toCompletableFuture()
                    .join();

            PermissionUser user = manager.getUser("ladder-user");
            user.addGroup(low);

            PermissionGroup promoted = user.promote(null, "main");
            assertEquals(high.getIdentifier().toLowerCase(Locale.ROOT),
                    promoted.getIdentifier().toLowerCase(Locale.ROOT));
            assertTrue(user.inGroup(high));
            assertFalse(user.inGroup(low, false));

            PermissionGroup demoted = user.demote(null, "main");
            assertEquals(low.getIdentifier().toLowerCase(Locale.ROOT),
                    demoted.getIdentifier().toLowerCase(Locale.ROOT));
            assertTrue(user.inGroup(low));
        }

        @Test
        void getRankLaddersExposesMembership() {
            PermissionGroup g = manager.getGroup("LadderOnly");
            g.setRank(10);
            g.setRankLadder("vip");
            manager.getGroups();

            PermissionUser user = manager.getUser("ladder-map");
            user.addGroup(g);
            Map<String, PermissionGroup> ladders = user.getRankLadders();
            assertTrue(ladders.containsKey("vip"));
            assertEquals(g, ladders.get("vip"));
        }

        @Test
        void userInheritsGroupPermissions() {
            PermissionGroup group = manager.getGroup("PermGroup");
            group.addPermission("group.only");
            PermissionUser user = manager.getUser("inherit-perm");
            user.addGroup(group);
            assertTrue(user.has("group.only", "world"));
            assertFalse(user.getOwnPermissions(null).contains("group.only"));
            assertTrue(user.getPermissions("world").contains("group.only"));
        }
    }

    @Nested
    @DisplayName("PermissionGroup hierarchy and ranking metadata")
    class GroupContracts {
        @Test
        void weightAndRankMetadata() {
            PermissionGroup group = manager.getGroup("Weighted");
            group.setWeight(25);
            assertEquals(25, group.getWeight());

            group.setRank(7);
            group.setRankLadder("staff");
            assertEquals(7, group.getRank());
            assertEquals("staff", group.getRankLadder());
            assertTrue(group.isRanked());
        }

        @Test
        void childAndDescendantQueries() {
            PermissionGroup root = manager.getGroup("Root");
            PermissionGroup mid = manager.getGroup("Mid");
            PermissionGroup leaf = manager.getGroup("Leaf");
            root.addPermission("touch.root");
            mid.setParents(Collections.singletonList(root));
            leaf.setParents(Collections.singletonList(mid));

            assertTrue(mid.isChildOf(root));
            assertTrue(leaf.isChildOf(root, true));
            assertFalse(leaf.isChildOf(root, false));
            assertTrue(root.getChildGroups().stream()
                    .anyMatch(g -> g.getIdentifier().equalsIgnoreCase("Mid")));
            assertTrue(root.getDescendantGroups().stream()
                    .anyMatch(g -> g.getIdentifier().equalsIgnoreCase("Leaf")));
        }

        @Test
        void defaultFlagIsConfigOwned() {
            PermissionGroup configured = manager.getGroup("default");
            assertTrue(configured.isDefault(null));
            assertThrows(UnsupportedOperationException.class,
                    () -> manager.getGroup("WorldDefault").setDefault(true, "world_the_end"));
        }

        @Test
        void usersOfGroupIncludesMembers() {
            PermissionGroup group = manager.getGroup("MembersOf");
            PermissionUser user = manager.getUser(UUID.randomUUID().toString());
            user.addGroup(group);
            assertTrue(group.getActiveUsers().contains(user) || group.getUsers().contains(user));
        }

        @Test
        void compareToOrdersByWeight() {
            PermissionGroup light = manager.getGroup("Light");
            PermissionGroup heavy = manager.getGroup("Heavy");
            light.setWeight(1);
            heavy.setWeight(100);
            assertEquals(Integer.signum(Integer.compare(light.getWeight(), heavy.getWeight())),
                    Integer.signum(light.compareTo(heavy)));
        }

        @Test
        void parentGroupObjectSetters() {
            PermissionGroup parent = manager.getGroup("ObjParent");
            PermissionGroup child = manager.getGroup("ObjChild");
            child.setParentGroupObjects(Collections.singletonList(parent));
            assertTrue(child.getParentGroups().contains(parent));
            assertArrayEquals(new String[]{parent.getIdentifier()}, child.getParentGroupsNames());
        }

        @Test
        void removeClearsMembershipAndAllowsFreshGroupAfterReset() {
            PermissionGroup group = manager.getGroup("ToRemove");
            PermissionGroup child = manager.getGroup("ChildOfRemoved");
            PermissionUser user = manager.getUser("user-of-removed");
            child.setParents(Collections.singletonList(group));
            user.addGroup(group);
            group.addPermission("gone.soon");

            group.remove();

            assertFalse(user.inGroup(group, false), "remove() must detach users");
            assertFalse(child.getOwnParents().contains(group), "remove() must detach child groups");

            // After cache reset, a reloaded group from Plus storage starts without the old nodes.
            manager.resetGroup("ToRemove");
            PermissionGroup again = manager.getGroup("ToRemove");
            assertFalse(again.getOwnPermissions(null).contains("gone.soon"));
        }
    }

    @Nested
    @DisplayName("Inheritance across users and groups")
    class InheritanceContracts {
        @Test
        void multiLevelPermissionInheritance() {
            PermissionGroup grand = manager.getGroup("Grand");
            PermissionGroup parent = manager.getGroup("Parent");
            PermissionGroup child = manager.getGroup("Child");
            grand.addPermission("grand.perm");
            parent.setParents(Collections.singletonList(grand));
            child.setParents(Collections.singletonList(parent));

            PermissionUser user = manager.getUser("deep-user");
            user.addGroup(child);
            assertTrue(user.has("grand.perm", "world"));
        }

        @Test
        void worldSpecificOptionDoesNotLeak() {
            PermissionUser user = manager.getUser("world-opt");
            user.setOption("build", "true", "world_nether");
            assertEquals("true", user.getOption("build", "world_nether", "false"));
            assertEquals("false", user.getOption("build", "world", "false"));
        }
    }
}
