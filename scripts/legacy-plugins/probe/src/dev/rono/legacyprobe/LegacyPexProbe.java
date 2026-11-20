package dev.rono.legacyprobe;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.atomic.AtomicInteger;
import net.milkbowl.vault.chat.Chat;
import net.milkbowl.vault.permission.Permission;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.plugin.RegisteredServiceProvider;
import org.bukkit.plugin.java.JavaPlugin;
import ru.tehkode.permissions.PermissionGroup;
import ru.tehkode.permissions.PermissionManager;
import ru.tehkode.permissions.PermissionUser;
import ru.tehkode.permissions.bukkit.PermissionsEx;
import ru.tehkode.permissions.events.PermissionEntityEvent;

/**
 * Stands in for a plugin written for PermissionsEx 1.23.4: compiled against the
 * 1.23.4 API and Vault 1.7.3, it uses PermissionsEx directly the way plugins of
 * that era did, and through Vault's own PermissionsEx hooks the way every Vault
 * consumer does. "/legacyprobe" runs every check and reports each one, then
 * "PROBE PASSED" or "PROBE FAILED".
 */
public final class LegacyPexProbe extends JavaPlugin implements Listener {
    private final AtomicInteger entityEvents = new AtomicInteger();

    @Override
    public void onEnable() {
        getServer().getPluginManager().registerEvents(this, this);
    }

    @EventHandler
    public void onEntityEvent(PermissionEntityEvent event) {
        entityEvents.incrementAndGet();
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        List<String> failures = new ArrayList<String>();
        List<String> lines = new ArrayList<String>();

        runDirectChecks(lines, failures);
        runVaultChecks(lines, failures);

        for (String line : lines) {
            sender.sendMessage(line);
        }

        sender.sendMessage(failures.isEmpty()
                ? "PROBE PASSED " + lines.size() + " checks"
                : "PROBE FAILED " + failures.size() + "/" + lines.size() + ": " + failures);
        return true;
    }

    private void runDirectChecks(List<String> lines, List<String> failures) {
        check(lines, failures, "PermissionsEx.isAvailable()", new Callable<Boolean>() {
            public Boolean call() {
                return PermissionsEx.isAvailable();
            }
        });

        final PermissionManager manager = PermissionsEx.getPermissionManager();

        // PermissionsEx.getUser(String) was removed in 1.23.5; plugins built for 1.23.4 call it.
        check(lines, failures, "static PermissionsEx.getUser(String)", new Callable<Boolean>() {
            @SuppressWarnings("deprecation")
            public Boolean call() {
                return PermissionsEx.getUser("ProbeUser") != null;
            }
        });

        final PermissionGroup group = manager.getGroup("probegroup");
        final PermissionUser user = manager.getUser("ProbeUser");
        final int eventsBefore = entityEvents.get();

        check(lines, failures, "group permission is inherited", new Callable<Boolean>() {
            public Boolean call() {
                group.addPermission("probe.group.node");
                user.addGroup("probegroup");
                return user.inGroup("probegroup") && user.has("probe.group.node");
            }
        });

        check(lines, failures, "PermissionEntityEvent is fired", new Callable<Boolean>() {
            public Boolean call() {
                return entityEvents.get() > eventsBefore;
            }
        });

        check(lines, failures, "world-specific permission", new Callable<Boolean>() {
            public Boolean call() {
                user.addPermission("probe.world.node", "probe_world");
                return user.has("probe.world.node", "probe_world") && !user.has("probe.world.node", "probe_other");
            }
        });

        check(lines, failures, "negated node overrides an inherited grant", new Callable<Boolean>() {
            public Boolean call() {
                group.addPermission("probe.denied");
                user.addPermission("-probe.denied");
                return !user.has("probe.denied");
            }
        });

        check(lines, failures, "group prefix is inherited", new Callable<Boolean>() {
            public Boolean call() {
                group.setPrefix("&a[Probe]", null);
                return "&a[Probe]".equals(user.getPrefix());
            }
        });

        check(lines, failures, "options round-trip", new Callable<Boolean>() {
            public Boolean call() {
                user.setOption("probe-option", "42");
                return user.getOptionInteger("probe-option", null, 0) == 42 && "42".equals(user.getOption("probe-option"));
            }
        });

        check(lines, failures, "timed permission", new Callable<Boolean>() {
            public Boolean call() {
                user.addTimedPermission("probe.timed", null, 600);
                int lifetime = user.getTimedPermissionLifetime("probe.timed", null);
                return user.has("probe.timed") && lifetime > 0 && lifetime <= 600;
            }
        });

        check(lines, failures, "rank ladder promotion", new Callable<Boolean>() {
            public Boolean call() throws Exception {
                PermissionGroup member = manager.getGroup("probe_member");
                PermissionGroup vip = manager.getGroup("probe_vip");
                member.setRank(100);
                member.setRankLadder("probe");
                vip.setRank(50);
                vip.setRankLadder("probe");

                PermissionUser ranked = manager.getUser("ProbeRanked");
                ranked.addGroup(member);

                return ranked.promote(null, "probe").getName().equalsIgnoreCase("probe_vip") && ranked.inGroup("probe_vip");
            }
        });

        check(lines, failures, "groups are listed", new Callable<Boolean>() {
            public Boolean call() {
                for (PermissionGroup listed : manager.getGroupList()) {
                    if (listed.getName().equalsIgnoreCase("probegroup")) {
                        return true;
                    }
                }

                return false;
            }
        });

        check(lines, failures, "group membership removal", new Callable<Boolean>() {
            public Boolean call() {
                PermissionUser temporary = manager.getUser("ProbeRemoved");
                temporary.addGroup("probegroup");
                temporary.removeGroup("probegroup");
                return !temporary.inGroup("probegroup");
            }
        });
    }

    private void runVaultChecks(List<String> lines, List<String> failures) {
        // Vault registers its own PermissionsEx hooks at Highest priority, so they are what
        // Vault hands to every consumer. Test those hooks specifically.
        final Permission perms = provider(Permission.class);
        final Chat chat = provider(Chat.class);

        check(lines, failures, "Vault's PermissionsEx permission hook is active", new Callable<Boolean>() {
            public Boolean call() {
                return perms != null && perms.isEnabled()
                        && Bukkit.getServicesManager().getRegistration(Permission.class).getProvider() == perms;
            }
        });

        check(lines, failures, "Vault's PermissionsEx chat hook is active", new Callable<Boolean>() {
            public Boolean call() {
                return chat != null && chat.isEnabled();
            }
        });

        if (perms == null || chat == null) {
            return;
        }

        @SuppressWarnings("deprecation")
        final OfflinePlayer player = Bukkit.getOfflinePlayer("VaultUser");

        check(lines, failures, "Vault playerAdd / playerHas / playerRemove", new Callable<Boolean>() {
            public Boolean call() {
                boolean added = perms.playerAdd((String) null, player, "vault.node") && perms.playerHas((String) null, player, "vault.node");
                boolean removed = perms.playerRemove((String) null, player, "vault.node") && !perms.playerHas((String) null, player, "vault.node");
                return added && removed;
            }
        });

        check(lines, failures, "Vault world permission", new Callable<Boolean>() {
            public Boolean call() {
                return perms.playerAdd("probe_world", player, "vault.world.node") && perms.playerHas("probe_world", player, "vault.world.node")
                        && !perms.playerHas("probe_other", player, "vault.world.node");
            }
        });

        check(lines, failures, "Vault group membership and primary group", new Callable<Boolean>() {
            public Boolean call() {
                perms.playerAddGroup((String) null, player, "probegroup");
                return perms.playerInGroup((String) null, player, "probegroup")
                        && Arrays.asList(perms.getPlayerGroups((String) null, player)).contains("probegroup")
                        && "probegroup".equalsIgnoreCase(perms.getPrimaryGroup((String) null, player));
            }
        });

        check(lines, failures, "Vault inherited group permission", new Callable<Boolean>() {
            public Boolean call() {
                return perms.groupAdd((String) null, "probegroup", "vault.group.node") && perms.groupHas((String) null, "probegroup", "vault.group.node")
                        && perms.playerHas((String) null, player, "vault.group.node");
            }
        });

        check(lines, failures, "Vault lists groups", new Callable<Boolean>() {
            public Boolean call() {
                return Arrays.asList(perms.getGroups()).contains("probegroup");
            }
        });

        check(lines, failures, "Vault chat player prefix", new Callable<Boolean>() {
            public Boolean call() {
                chat.setPlayerPrefix((String) null, player, "&b[V]");
                return "&b[V]".equals(chat.getPlayerPrefix((String) null, player));
            }
        });

        check(lines, failures, "Vault chat group prefix", new Callable<Boolean>() {
            public Boolean call() {
                return "&a[Probe]".equals(chat.getGroupPrefix((String) null, "probegroup"));
            }
        });

        check(lines, failures, "Vault chat player info string", new Callable<Boolean>() {
            public Boolean call() {
                chat.setPlayerInfoString((String) null, player, "vault-option", "hello");
                String value = chat.getPlayerInfoString((String) null, player, "vault-option", null);
                if (!"hello".equals(value)) {
                    throw new IllegalStateException("got " + value);
                }
                return true;
            }
        });

        // Read only: Vault 1.7.3's group info setters pass (option, world, value) to
        // PermissionGroup.setOption(option, value, world), so they never worked with any
        // PermissionsEx. Set the option through PermissionsEx instead.
        check(lines, failures, "Vault chat group info integer", new Callable<Boolean>() {
            public Boolean call() {
                PermissionsEx.getPermissionManager().getGroup("probegroup").setOption("vault-number", "7");
                int value = chat.getGroupInfoInteger((String) null, "probegroup", "vault-number", 0);
                if (value != 7) {
                    throw new IllegalStateException("got " + value);
                }
                return true;
            }
        });
    }

    /** Vault's own PermissionsEx hook for this service. */
    private static <T> T provider(Class<T> service) {
        for (RegisteredServiceProvider<T> registration : Bukkit.getServicesManager().getRegistrations(service)) {
            T provider = registration.getProvider();
            String name = provider instanceof Permission ? ((Permission) provider).getName()
                    : provider instanceof Chat ? ((Chat) provider).getName() : null;

            // "PermissionsEx" and "PermissionsEx_Chat"; PermissionsExPlus's own hooks are named differently.
            if (name != null && (name.equals("PermissionsEx") || name.equals("PermissionsEx_Chat"))) {
                return provider;
            }
        }

        return null;
    }

    private static void check(List<String> lines, List<String> failures, String name, Callable<Boolean> check) {
        String result;

        try {
            result = Boolean.TRUE.equals(check.call()) ? "ok" : "FAIL";
        } catch (Throwable error) {
            // LinkageError included: a missing or changed method is exactly what this catches.
            result = "FAIL (" + error.getClass().getSimpleName() + ": " + error.getMessage() + ")";
        }

        if (!result.equals("ok")) {
            failures.add(name);
        }

        lines.add("PROBE " + result + " " + name);
    }
}
