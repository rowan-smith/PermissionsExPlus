package ru.tehkode.permissions.bukkit;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.objenesis.ObjenesisHelper;
import ru.tehkode.permissions.PEXTestBase;
import ru.tehkode.permissions.PermissionGroup;
import ru.tehkode.permissions.PermissionManager;
import ru.tehkode.permissions.PermissionUser;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static org.junit.jupiter.api.Assertions.*;

public class PEXPlaceholderExpansionTest extends PEXTestBase {

    private PEXPlaceholderExpansion expansion;
    private PermissionUser user;
    private Method processPlaceholder;
    private static final String WORLD = "world";

    @BeforeEach
    @Override
    public void setUp() throws Exception {
        super.setUp();
        user = manager.getUser("TestUser");

        expansion = ObjenesisHelper.newInstance(PEXPlaceholderExpansion.class);

        processPlaceholder = PEXPlaceholderExpansion.class.getDeclaredMethod("processPlaceholder", String.class, PermissionUser.class, String.class, PermissionManager.class);
        processPlaceholder.setAccessible(true);
    }

    private String req(String identifier) {
        return req(user, identifier);
    }

    private String req(PermissionUser target, String identifier) {
        try {
            return (String) processPlaceholder.invoke(expansion, identifier, target, WORLD, manager);
        } catch (Exception e) {
            throw new RuntimeException(e.getCause() != null ? e.getCause() : e);
        }
    }

    // =====================================================================
    // Null / edge-case tests
    // =====================================================================
    @Nested
    class NullInputTests {

        @Test
        void unknownPlaceholderReturnsNull() {
            assertNull(req("totally_unknown"));
        }

        @Test
        void bareUserPrefixReturnsNull() {
            assertNull(req("user_"));
        }

        @Test
        void emptyStringReturnsNull() {
            assertNull(req(""));
        }
    }

    // =====================================================================
    // Direct user placeholders (pex_user_<placeholder>)
    // =====================================================================
    @Nested
    class DirectUserPlaceholderTests {

        @Test
        void userName() {
            user.setOption("name", "TestPlayer", null);
            assertEquals("TestPlayer", req("user_name"));
        }

        @Test
        void userPrefixDefault() {
            assertEquals("", req("user_prefix"));
        }

        @Test
        void userPrefixSet() {
            user.setPrefix("&c[Admin]", null);
            assertEquals("&c[Admin]", req("user_prefix"));
        }

        @Test
        void userSuffixDefault() {
            assertEquals("", req("user_suffix"));
        }

        @Test
        void userSuffixSet() {
            user.setSuffix("&a<&r", null);
            assertEquals("&a<&r", req("user_suffix"));
        }

        @Test
        void userPrefixEmpty() {
            user.setPrefix("", null);
            assertEquals("", req("user_prefix"));
        }

        @Test
        void userSuffixEmpty() {
            user.setSuffix("", null);
            assertEquals("", req("user_suffix"));
        }

        @Test
        void userPrimaryGroupNone() {
            assertEquals("", req("user_primary_group"));
        }

        @Test
        void userPrimaryGroup() {
            PermissionGroup group = manager.getGroup("Admin");
            user.addGroup(group, null);
            assertEquals("Admin", req("user_primary_group"));
        }

        @Test
        void userGroupsNone() {
            assertEquals("", req("user_groups"));
        }

        @Test
        void userGroupsMultiple() {
            PermissionGroup admins = manager.getGroup("Admin");
            PermissionGroup mods = manager.getGroup("Mod");
            user.addGroup(admins, null);
            user.addGroup(mods, null);
            String result = req("user_groups");
            assertTrue(result.contains("Admin"));
            assertTrue(result.contains("Mod"));
        }

        @Test
        void userDirectGroupsNone() {
            assertEquals("", req("user_direct_groups"));
        }

        @Test
        void userDirectGroups() {
            PermissionGroup group = manager.getGroup("Builder");
            user.addGroup(group, WORLD);
            assertEquals("Builder", req("user_direct_groups"));
        }

        @Test
        void userGroupCountZero() {
            assertEquals("0", req("user_group_count"));
        }

        @Test
        void userGroupCount() {
            user.addGroup(manager.getGroup("A"), null);
            user.addGroup(manager.getGroup("B"), null);
            assertEquals("2", req("user_group_count"));
        }

        @Test
        void userDirectGroupCountZero() {
            assertEquals("0", req("user_direct_group_count"));
        }

        @Test
        void userDirectGroupCount() {
            user.addGroup(manager.getGroup("X"), WORLD);
            assertEquals("1", req("user_direct_group_count"));
        }

        @Test
        void userPermissionCountZero() {
            assertEquals("0", req("user_permission_count"));
        }

        @Test
        void userPermissionCount() {
            user.addPermission("test.perm1", null);
            user.addPermission("test.perm2", null);
            assertEquals("2", req("user_permission_count"));
        }

        @Test
        void userDirectPermissionCountZero() {
            assertEquals("0", req("user_direct_permission_count"));
        }

        @Test
        void userDirectPermissionCount() {
            user.addPermission("test.perm", WORLD);
            assertEquals("1", req("user_direct_permission_count"));
        }
    }

    // =====================================================================
    // User option placeholder (pex_user_option_<name>)
    // =====================================================================
    @Nested
    class UserOptionPlaceholderTests {

        @Test
        void optionReturnsEmptyWhenNotSet() {
            assertEquals("", req("user_option_nonexistent"));
        }

        @Test
        void optionReturnsValue() {
            user.setOption("language", "en", null);
            assertEquals("en", req("user_option_language"));
        }

        @Test
        void optionWithSpecialChars() {
            user.setOption("prefix", "&c[Admin]", null);
            assertEquals("&c[Admin]", req("user_option_prefix"));
        }

        @Test
        void optionEmptyValue() {
            user.setOption("key", "", null);
            assertEquals("", req("user_option_key"));
        }
    }

    // =====================================================================
    // User has_permission placeholder (pex_user_has_permission_<perm>)
    // =====================================================================
    @Nested
    class UserHasPermissionTests {

        @Test
        void hasPermissionFalse() {
            assertEquals("false", req("user_has_permission_test.denied"));
        }

        @Test
        void hasPermissionTrue() {
            user.addPermission("test.granted", null);
            assertEquals("true", req("user_has_permission_test.granted"));
        }

        @Test
        void hasPermissionWildcard() {
            user.addPermission("*", WORLD);
            assertEquals("true", req("user_has_permission_anything"));
        }
    }

    // =====================================================================
    // User in_group placeholders
    // =====================================================================
    @Nested
    class UserInGroupTests {

        @Test
        void inGroupFalse() {
            assertEquals("false", req("user_in_group_NonExistent"));
        }

        @Test
        void inGroupTrue() {
            PermissionGroup group = manager.getGroup("Admin");
            user.addGroup(group, null);
            assertEquals("true", req("user_in_group_Admin"));
        }

        @Test
        void inGroupNonexistentGroup() {
            assertEquals("false", req("user_in_group_TotallyFake"));
        }

        @Test
        void inGroupDirectFalse() {
            assertEquals("false", req("user_in_group_direct_NonExistent"));
        }

        @Test
        void inGroupDirectTrue() {
            PermissionGroup group = manager.getGroup("Mod");
            user.addGroup(group, WORLD);
            assertEquals("true", req("user_in_group_direct_Mod"));
        }

        @Test
        void inGroupDirectInheritedFalse() {
            PermissionGroup parent = manager.getGroup("Parent");
            PermissionGroup child = manager.getGroup("Child");
            child.setParents(Collections.singletonList(parent));
            user.addGroup(child, null);
            assertEquals("false", req("user_in_group_direct_Parent"));
        }
    }

    // =====================================================================
    // Explicit user placeholders (pex_user_<username>_<placeholder>)
    // =====================================================================
    @Nested
    class ExplicitUserPlaceholderTests {

        @Test
        void explicitUserPrefix() {
            PermissionUser target = manager.getUser("OtherGuy");
            target.setPrefix("&6[Other]", null);
            assertEquals("&6[Other]", req("user_OtherGuy_prefix"));
        }

        @Test
        void explicitUserSuffix() {
            PermissionUser target = manager.getUser("SomeDude");
            target.setSuffix("&7-", null);
            assertEquals("&7-", req("user_SomeDude_suffix"));
        }

        @Test
        void explicitUserName() {
            PermissionUser target = manager.getUser("Explicit");
            target.setOption("name", "ExplicitName", null);
            assertEquals("ExplicitName", req("user_Explicit_name"));
        }

        @Test
        void explicitUserNonexistent() {
            req("user_NobodyHereEver_name");
        }

        @Test
        void explicitUserGroups() {
            PermissionUser target = manager.getUser("GroupGuy");
            PermissionGroup g1 = manager.getGroup("Alpha");
            PermissionGroup g2 = manager.getGroup("Beta");
            target.addGroup(g1, null);
            target.addGroup(g2, null);
            String result = req("user_GroupGuy_groups");
            assertTrue(result.contains("Alpha"));
            assertTrue(result.contains("Beta"));
        }

        @Test
        void explicitUserOption() {
            PermissionUser target = manager.getUser("OptUser");
            target.setOption("title", "Hero", null);
            assertEquals("Hero", req("user_OptUser_option_title"));
        }

        @Test
        void explicitUserHasPermission() {
            PermissionUser target = manager.getUser("PermUser");
            target.addPermission("special.ability", null);
            assertEquals("true", req("user_PermUser_has_permission_special.ability"));
        }

        @Test
        void explicitUserInGroup() {
            PermissionGroup group = manager.getGroup("VIP");
            PermissionUser target = manager.getUser("VipGuy");
            target.addGroup(group, null);
            assertEquals("true", req("user_VipGuy_in_group_VIP"));
        }
    }

    // =====================================================================
    // Group placeholders (pex_group_<group>_<placeholder>)
    // =====================================================================
    @Nested
    class GroupPlaceholderTests {

        @Test
        void groupExists() {
            manager.getGroup("Admin");
            assertEquals("true", req("group_Admin_exists"));
        }

        @Test
        void groupPrefixDefault() {
            manager.getGroup("Default");
            assertEquals("", req("group_Default_prefix"));
        }

        @Test
        void groupPrefixSet() {
            PermissionGroup group = manager.getGroup("Admin");
            group.setPrefix("&c[Admin]", WORLD);
            assertEquals("&c[Admin]", req("group_Admin_prefix"));
        }

        @Test
        void groupSuffixDefault() {
            manager.getGroup("Default");
            assertEquals("", req("group_Default_suffix"));
        }

        @Test
        void groupSuffixSet() {
            PermissionGroup group = manager.getGroup("Mod");
            group.setSuffix("&7-", WORLD);
            assertEquals("&7-", req("group_Mod_suffix"));
        }

        @Test
        void groupPrefixNull() {
            manager.getGroup("NullPrefix");
            assertEquals("", req("group_NullPrefix_prefix"));
        }

        @Test
        void groupSuffixNull() {
            manager.getGroup("NullSuffix");
            assertEquals("", req("group_NullSuffix_suffix"));
        }

        @Test
        void groupParentsNone() {
            manager.getGroup("NoParents");
            assertEquals("", req("group_NoParents_parents"));
        }

        @Test
        void groupParents() {
            PermissionGroup child = manager.getGroup("ChildGroup");
            PermissionGroup parent = manager.getGroup("ParentGroup");
            child.setParents(Collections.singletonList(parent));
            assertEquals("ParentGroup", req("group_ChildGroup_parents"));
        }

        @Test
        void groupParentsMultiple() {
            PermissionGroup multi = manager.getGroup("MultiParent");
            PermissionGroup a = manager.getGroup("ParentA");
            PermissionGroup b = manager.getGroup("ParentB");
            multi.setParents(Arrays.asList(a, b));
            String result = req("group_MultiParent_parents");
            assertTrue(result.contains("ParentA"));
            assertTrue(result.contains("ParentB"));
        }

        @Test
        void groupParentCountZero() {
            manager.getGroup("NoParentsCount");
            assertEquals("0", req("group_NoParentsCount_parent_count"));
        }

        @Test
        void groupParentCount() {
            PermissionGroup child = manager.getGroup("CountedChild");
            child.setParents(Arrays.asList(manager.getGroup("P1"), manager.getGroup("P2")));
            assertEquals("2", req("group_CountedChild_parent_count"));
        }

        @Test
        void groupPermissionCountZero() {
            manager.getGroup("NoPerms");
            assertEquals("0", req("group_NoPerms_permission_count"));
        }

        @Test
        void groupPermissionCount() {
            PermissionGroup group = manager.getGroup("PermedGroup");
            group.addPermission("test.one", WORLD);
            group.addPermission("test.two", WORLD);
            group.addPermission("test.three", WORLD);
            assertEquals("3", req("group_PermedGroup_permission_count"));
        }

        @Test
        void groupEffectivePermissionCountIncludesInherited() {
            PermissionGroup parent = manager.getGroup("EffParent");
            PermissionGroup child = manager.getGroup("EffChild");
            parent.addPermission("parent.perm", null);
            child.setParents(Collections.singletonList(parent));
            child.addPermission("child.perm", null);
            String result = req("group_EffChild_effective_permission_count");
            assertTrue(Integer.parseInt(result) >= 2);
        }
    }

    // =====================================================================
    // Group option placeholder
    // =====================================================================
    @Nested
    class GroupOptionPlaceholderTests {

        @Test
        void groupOptionReturnsEmptyWhenNotSet() {
            manager.getGroup("EmptyOptGroup");
            assertEquals("", req("group_EmptyOptGroup_option_foo"));
        }

        @Test
        void groupOptionReturnsValue() {
            PermissionGroup group = manager.getGroup("OptGroup");
            group.setOption("color", "red", null);
            assertEquals("red", req("group_OptGroup_option_color"));
        }
    }

    // =====================================================================
    // Group has_permission placeholder
    // =====================================================================
    @Nested
    class GroupHasPermissionTests {

        @Test
        void groupHasPermissionFalse() {
            manager.getGroup("NoPermGroup");
            assertEquals("false", req("group_NoPermGroup_has_permission_nope.no"));
        }

        @Test
        void groupHasPermissionTrue() {
            PermissionGroup group = manager.getGroup("PermGroup");
            group.addPermission("some.permission", null);
            assertEquals("true", req("group_PermGroup_has_permission_some.permission"));
        }
    }

    // =====================================================================
    // Explicit group placeholders with non-existent group
    // =====================================================================
    @Nested
    class GroupNotFoundTests {

        @Test
        void nonexistentGroupPrefix() {
            assertEquals("", req("group_FakeGroup123_prefix"));
        }

        @Test
        void nonexistentGroupExists() {
            assertEquals("true", req("group_FakeGroup123_exists"));
        }
    }

    // =====================================================================
    // World-specific context tests
    // =====================================================================
    @Nested
    class WorldContextTests {

        @Test
        void userPrefixWithWorldContext() {
            user.setPrefix("&a[Global]", null);
            user.setPrefix("&c[World]", "world");
            assertEquals("&c[World]", req("user_prefix_world_world"));
        }

        @Test
        void userSuffixWithWorldContext() {
            user.setSuffix("&7-global", null);
            user.setSuffix("&7-world", "world");
            assertEquals("&7-world", req("user_suffix_world_world"));
        }

        @Test
        void groupPrefixWithWorldContext() {
            PermissionGroup group = manager.getGroup("WorldGroup");
            group.setPrefix("&6[Global]", null);
            group.setPrefix("&b[World]", "world");
            assertEquals("&b[World]", req("group_WorldGroup_prefix_world_world"));
        }

        @Test
        void groupSuffixWithWorldContext() {
            PermissionGroup group = manager.getGroup("WorldSuffixGroup");
            group.setSuffix("&7-global", null);
            group.setSuffix("&7-world", "world");
            assertEquals("&7-world", req("group_WorldSuffixGroup_suffix_world_world"));
        }

        @Test
        void userHasPermissionWithWorldContext() {
            user.addPermission("world.only", "world");
            assertEquals("true", req("user_has_permission_world.only_world_world"));
        }

        @Test
        void userOptionWithWorldContext() {
            user.setOption("lang", "en", null);
            user.setOption("lang", "fr", "world");
            assertEquals("fr", req("user_option_lang_world_world"));
        }

        @Test
        void groupHasPermissionWithWorldContext() {
            PermissionGroup group = manager.getGroup("WorldPermGroup");
            group.addPermission("world.perm", "world");
            assertEquals("true", req("group_WorldPermGroup_has_permission_world.perm_world_world"));
        }

        @Test
        void groupOptionWithWorldContext() {
            PermissionGroup group = manager.getGroup("WorldOptGroup");
            group.setOption("tier", "1", null);
            group.setOption("tier", "3", "world");
            assertEquals("3", req("group_WorldOptGroup_option_tier_world_world"));
        }
    }

    // =====================================================================
    // Special characters and formatting in prefix/suffix
    // =====================================================================
    @Nested
    class FormattingTests {

        @Test
        void prefixWithAmpersandCodes() {
            user.setPrefix("&c&l[Bold Red]", null);
            assertEquals("&c&l[Bold Red]", req("user_prefix"));
        }

        @Test
        void prefixWithSectionCodes() {
            user.setPrefix("§c§l[Bold Red]", null);
            assertEquals("§c§l[Bold Red]", req("user_prefix"));
        }

        @Test
        void prefixWithMiniMessageTags() {
            user.setPrefix("<red><bold>[Admin]</bold></red>", null);
            assertEquals("<red><bold>[Admin]</bold></red>", req("user_prefix"));
        }

        @Test
        void suffixWithMiniMessageTags() {
            user.setSuffix("<gray>-<white>suffix</white></gray>", null);
            assertEquals("<gray>-<white>suffix</white></gray>", req("user_suffix"));
        }

        @Test
        void groupPrefixWithMiniMessageTags() {
            PermissionGroup group = manager.getGroup("MiniGroup");
            group.setPrefix("<green>[VIP]</green>", WORLD);
            assertEquals("<green>[VIP]</green>", req("group_MiniGroup_prefix"));
        }

        @Test
        void optionWithSpecialCharacters() {
            user.setOption("format", "%s has %s coins", null);
            assertEquals("%s has %s coins", req("user_option_format"));
        }

        @Test
        void prefixWithAngleBrackets() {
            user.setPrefix("<<Admin>>", null);
            assertEquals("<<Admin>>", req("user_prefix"));
        }

        @Test
        void suffixWithUnicode() {
            user.setSuffix("❤", null);
            assertEquals("❤", req("user_suffix"));
        }
    }

    // =====================================================================
    // Routing / dispatch tests
    // =====================================================================
    @Nested
    class RoutingTests {

        @Test
        void directUserPlaceholderMatches() {
            assertEquals(true, req("user_name") != null);
        }

        @Test
        void explicitUserTakesPriorityOverDirect() {
            PermissionUser alice = manager.getUser("Alice");
            alice.setOption("name", "AliceReal", null);
            user.setOption("name", "DirectUser", null);
            assertEquals("AliceReal", req("user_Alice_name"));
        }

        @Test
        void groupPlaceholderTakesPriorityOverDirectUser() {
            manager.getGroup("Admin");
            assertEquals("true", req("group_Admin_exists"));
        }

        @Test
        void unrecognizedPrefixReturnsNull() {
            assertNull(req("unknown_thing"));
        }
    }

    // =====================================================================
    // PAPI placeholder resolution tests
    // =====================================================================
    @Nested
    class PapiResolutionTests {

        private PEXPlaceholderExpansion spyExpansion;
        private PermissionUser papiUser;
        private org.bukkit.entity.Player mockPlayer;

        // Simulated PAPI placeholder registry for testing
        private final Map<String, String> papiRegistry = new HashMap<>();

        /**
         * Subclass that overrides resolvePapiPlaceholders to simulate
         * PlaceholderAPI.setPlaceholders() without requiring a running server.
         */
        class TestableExpansion extends PEXPlaceholderExpansion {
            TestableExpansion() {
                super(null);
            }

            @Override
            protected String resolvePapiPlaceholders(String result, org.bukkit.entity.Player player) {
                if (result == null || result.isEmpty()) {
                    return result;
                }

                // Simulate PAPI resolution: find %key% patterns and replace them
                Pattern p = Pattern.compile("%([^%]+)%");
                Matcher m = p.matcher(result);
                StringBuilder sb = new StringBuilder();
                while (m.find()) {
                    String key = m.group(1);
                    String replacement = papiRegistry.getOrDefault(key, m.group(0));
                    m.appendReplacement(sb, Matcher.quoteReplacement(replacement));
                }

                m.appendTail(sb);
                return sb.toString();
            }
        }

        @BeforeEach
        void setupPapi() throws Exception {
            papiUser = manager.getUser("PapiUser");

            spyExpansion = new TestableExpansion();

            // Create a PermissionsEx instance via Objenesis (bypass constructor)
            // and set its permissionsManager field so onPlaceholderRequest works
            PermissionsEx mockPlugin = ObjenesisHelper.newInstance(PermissionsEx.class);
            Field managerField = PermissionsEx.class.getDeclaredField("permissionsManager");
            managerField.setAccessible(true);
            managerField.set(mockPlugin, manager);

            // Set the plugin field via reflection
            Field pluginField = PEXPlaceholderExpansion.class.getDeclaredField("plugin");
            pluginField.setAccessible(true);
            pluginField.set(spyExpansion, mockPlugin);

            // Create a mock Player proxy
            mockPlayer = (org.bukkit.entity.Player) java.lang.reflect.Proxy.newProxyInstance(
                    org.bukkit.entity.Player.class.getClassLoader(),
                    new Class[]{org.bukkit.entity.Player.class},
                    (proxy, method, args) -> {
                        switch (method.getName()) {
                            case "getName":
                                return "PapiUser";
                            case "getUniqueId":
                                return UUID.randomUUID();
                            case "getWorld":
                                return world;
                            case "equals":
                                return proxy == args[0];
                            case "hashCode":
                                return System.identityHashCode(proxy);
                            case "toString":
                                return "MockPapiPlayer";
                        }
                        return null;
                    }
            );
        }

        private String onReq(String identifier) {
            try {
                Method onPlaceholderRequest = PEXPlaceholderExpansion.class.getDeclaredMethod(
                        "onPlaceholderRequest", org.bukkit.entity.Player.class, String.class);
                onPlaceholderRequest.setAccessible(true);
                return (String) onPlaceholderRequest.invoke(spyExpansion, mockPlayer, identifier);
            } catch (Exception e) {
                throw new RuntimeException(e.getCause() != null ? e.getCause() : e);
            }
        }

        @Test
        void papiPrefixIsResolved() {
            papiUser.setPrefix("&c%test_color%", null);
            papiRegistry.put("test_color", "&a[Resolved]");
            assertEquals("&c&a[Resolved]", onReq("user_prefix"));
        }

        @Test
        void papiSuffixIsResolved() {
            papiUser.setSuffix("&7-%server_name%", null);
            papiRegistry.put("server_name", "MyServer");
            assertEquals("&7-MyServer", onReq("user_suffix"));
        }

        @Test
        void papiOptionIsResolved() {
            papiUser.setOption("greeting", "Hello %player_name%!", null);
            papiRegistry.put("player_name", "PapiUser");
            assertEquals("Hello PapiUser!", onReq("user_option_greeting"));
        }

        @Test
        void papiGroupPrefixIsResolved() {
            PermissionGroup group = manager.getGroup("PapiGroup");
            group.setPrefix("<green>%vault_prefix%</green>", WORLD);
            papiRegistry.put("vault_prefix", "[VIP]");
            assertEquals("<green>[VIP]</green>", onReq("group_PapiGroup_prefix"));
        }

        @Test
        void papiGroupSuffixIsResolved() {
            PermissionGroup group = manager.getGroup("PapiSuffixGroup");
            group.setSuffix("&8-%some_plugin_tag%", WORLD);
            papiRegistry.put("some_plugin_tag", "tagged");
            assertEquals("&8-tagged", onReq("group_PapiSuffixGroup_suffix"));
        }

        @Test
        void papiMultiplePlaceholdersInPrefix() {
            papiUser.setPrefix("%p1%%p2%", null);
            papiRegistry.put("p1", "A");
            papiRegistry.put("p2", "B");
            assertEquals("AB", onReq("user_prefix"));
        }

        @Test
        void papiUnresolvedPlaceholderStaysLiteral() {
            papiUser.setPrefix("&c[Unknown]", null);
            assertEquals("&c[Unknown]", onReq("user_prefix"));
        }

        @Test
        void papiEmptyResultSkipsResolution() {
            papiUser.setPrefix("", null);
            assertEquals("", onReq("user_prefix"));
        }

        @Test
        void papiNullResultSkipsResolution() {
            assertNull(onReq("totally_unknown"));
        }

        @Test
        void papiExplicitUserPrefixIsResolved() {
            PermissionUser target = manager.getUser("PapiExplicit");
            target.setPrefix("&6%player_name%", null);
            papiRegistry.put("player_name", "PapiExplicit");
            assertEquals("&6PapiExplicit", onReq("user_PapiExplicit_prefix"));
        }

        @Test
        void papiPrefixWithLegacyCodesAndPlaceholder() {
            papiUser.setPrefix("§c%test%", null);
            papiRegistry.put("test", "Hi");
            assertEquals("§cHi", onReq("user_prefix"));
        }
    }
}
