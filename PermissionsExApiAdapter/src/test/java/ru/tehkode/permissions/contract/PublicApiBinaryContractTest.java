package ru.tehkode.permissions.contract;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.fail;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.List;
import org.junit.jupiter.api.Test;
import ru.tehkode.permissions.HierarchyTraverser;
import ru.tehkode.permissions.NativeInterface;
import ru.tehkode.permissions.PermissionCheckResult;
import ru.tehkode.permissions.PermissionEntity;
import ru.tehkode.permissions.PermissionGroup;
import ru.tehkode.permissions.PermissionManager;
import ru.tehkode.permissions.PermissionMatcher;
import ru.tehkode.permissions.PermissionUser;
import ru.tehkode.permissions.PermissionsData;
import ru.tehkode.permissions.PermissionsGroupData;
import ru.tehkode.permissions.PermissionsUserData;
import ru.tehkode.permissions.RegExpMatcher;
import ru.tehkode.permissions.backends.PermissionBackend;
import ru.tehkode.permissions.bukkit.PermissionsEx;
import ru.tehkode.permissions.bukkit.PermissionsExConfig;
import ru.tehkode.permissions.events.PermissionEntityEvent;
import ru.tehkode.permissions.events.PermissionEvent;
import ru.tehkode.permissions.events.PermissionSystemEvent;
import ru.tehkode.permissions.exceptions.PermissionBackendException;
import ru.tehkode.permissions.exceptions.PermissionsNotAvailable;
import ru.tehkode.permissions.exceptions.RankingException;

/**
 * Locks the ApiAdapter public binary surface against classic PermissionsEx <b>1.23.5</b>
 * ({@code STABLE-1.23.5}), the final stable API plugins compile against.
 *
 * <p>Source of truth: {@code src/test/resources/baselines/PermissionsEx-1.23.5-api.jar}
 * (classes-only jar built from that tag). Required members from 1.23.5 must remain present;
 * additive public members on the shim are allowed.
 *
 * <p>Regenerate the checked-in signature dump from the baseline JAR (not from the adapter):
 * {@code mvn -pl PermissionsExApiAdapter test -Dtest=PublicApiBinaryContractTest -Dpex.contracts.update=true}
 */
class PublicApiBinaryContractTest {

    /** Fully-qualified names frozen to the 1.23.5 public contract surface. */
    static final String[] PUBLIC_CONTRACT_TYPE_NAMES = {
            "ru.tehkode.permissions.PermissionManager",
            "ru.tehkode.permissions.PermissionEntity",
            "ru.tehkode.permissions.PermissionUser",
            "ru.tehkode.permissions.PermissionGroup",
            "ru.tehkode.permissions.PermissionCheckResult",
            "ru.tehkode.permissions.PermissionMatcher",
            "ru.tehkode.permissions.RegExpMatcher",
            "ru.tehkode.permissions.NativeInterface",
            "ru.tehkode.permissions.PermissionsData",
            "ru.tehkode.permissions.PermissionsUserData",
            "ru.tehkode.permissions.PermissionsGroupData",
            "ru.tehkode.permissions.HierarchyTraverser",
            "ru.tehkode.permissions.bukkit.PermissionsEx",
            "ru.tehkode.permissions.bukkit.PermissionsExConfig",
            "ru.tehkode.permissions.backends.PermissionBackend",
            "ru.tehkode.permissions.events.PermissionEvent",
            "ru.tehkode.permissions.events.PermissionEntityEvent",
            "ru.tehkode.permissions.events.PermissionSystemEvent",
            "ru.tehkode.permissions.exceptions.PermissionBackendException",
            "ru.tehkode.permissions.exceptions.PermissionsNotAvailable",
            "ru.tehkode.permissions.exceptions.RankingException",
    };

    /** Adapter-side mirrors used when comparing the live shim classpath. */
    static final Class<?>[] ADAPTER_CONTRACT_TYPES = {
            PermissionManager.class,
            PermissionEntity.class,
            PermissionUser.class,
            PermissionGroup.class,
            PermissionCheckResult.class,
            PermissionMatcher.class,
            RegExpMatcher.class,
            NativeInterface.class,
            PermissionsData.class,
            PermissionsUserData.class,
            PermissionsGroupData.class,
            HierarchyTraverser.class,
            PermissionsEx.class,
            PermissionsExConfig.class,
            PermissionBackend.class,
            PermissionEvent.class,
            PermissionEntityEvent.class,
            PermissionSystemEvent.class,
            PermissionBackendException.class,
            PermissionsNotAvailable.class,
            RankingException.class,
    };

    private static final String RESOURCE = "/contracts/public-api-signatures.txt";
    private static final String BASELINE_VERSION = "1.23.5";

    @Test
    void requiredPublicMembersFrom1235RemainPresent() throws Exception {
        Path baselineJar = ApiSignatures.resolveBaselineJar();
        List<Class<?>> baselineTypes = ApiSignatures.loadTypes(
                baselineJar,
                PublicApiBinaryContractTest.class.getClassLoader(),
                PUBLIC_CONTRACT_TYPE_NAMES);
        List<String> requiredFromJar = ApiSignatures.fingerprintAll(baselineTypes);

        if (Boolean.getBoolean("pex.contracts.update")) {
            Path out = Path.of("src/test/resources/contracts/public-api-signatures.txt");
            ApiSignatures.writeLines(out, requiredFromJar);
            System.out.println("Updated " + BASELINE_VERSION + " public API contract dump at "
                    + out.toAbsolutePath() + " from " + baselineJar);
            return;
        }

        // Integrity: committed dump must match the 1.23.5 baseline JAR.
        List<String> requiredFromResource = ApiSignatures.readResource(RESOURCE);
        List<String> dumpDrift = ApiSignatures.missingRequired(requiredFromJar, requiredFromResource);
        List<String> dumpExtra = ApiSignatures.missingRequired(requiredFromResource, requiredFromJar);
        if (!dumpDrift.isEmpty() || !dumpExtra.isEmpty()) {
            fail("Committed signature dump does not match PermissionsEx " + BASELINE_VERSION
                    + " baseline JAR (" + baselineJar + ").\n"
                    + "Missing from dump:\n" + String.join("\n", dumpDrift) + "\n"
                    + "Extra in dump:\n" + String.join("\n", dumpExtra) + "\n"
                    + "Regenerate with -Dpex.contracts.update=true");
        }

        List<String> actualAdapter = ApiSignatures.fingerprintAll(ADAPTER_CONTRACT_TYPES);
        List<String> missing = ApiSignatures.missingRequired(requiredFromJar, actualAdapter);
        if (!missing.isEmpty()) {
            fail("ApiAdapter broke binary compatibility with PermissionsEx " + BASELINE_VERSION
                    + " — required members missing or signature-changed:\n"
                    + String.join("\n", missing));
        }

        assertTrue(actualAdapter.size() >= requiredFromJar.size(),
                "Adapter public surface should be at least as large as the " + BASELINE_VERSION + " baseline");
    }

    @Test
    void baselineJarIsPermissionsEx1235() throws Exception {
        Path jar = ApiSignatures.resolveBaselineJar();
        assertTrue(jar.getFileName().toString().contains("1.23.5"),
                "Baseline JAR name must identify 1.23.5: " + jar);
        assertTrue(jar.toString().replace('\\', '/').contains("baselines/")
                        || System.getProperty("pex.contracts.baselineJar") != null,
                "Baseline should come from test resources/baselines unless overridden");
    }

    @Test
    void contractTypeCatalogMatchesAdapterAndBaseline() {
        assertEquals(PUBLIC_CONTRACT_TYPE_NAMES.length, ADAPTER_CONTRACT_TYPES.length);
        for (int i = 0; i < PUBLIC_CONTRACT_TYPE_NAMES.length; i++) {
            assertEquals(PUBLIC_CONTRACT_TYPE_NAMES[i], ADAPTER_CONTRACT_TYPES[i].getName());
        }

        List<String> names = Arrays.stream(PUBLIC_CONTRACT_TYPE_NAMES).sorted().toList();
        assertTrue(names.contains("ru.tehkode.permissions.PermissionManager"));
        assertTrue(names.contains("ru.tehkode.permissions.PermissionUser"));
        assertTrue(names.contains("ru.tehkode.permissions.PermissionGroup"));
        assertTrue(names.contains("ru.tehkode.permissions.bukkit.PermissionsEx"));
        assertTrue(names.contains("ru.tehkode.permissions.backends.PermissionBackend"));
    }
}
