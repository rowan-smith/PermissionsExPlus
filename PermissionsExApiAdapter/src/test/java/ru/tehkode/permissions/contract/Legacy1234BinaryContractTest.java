package ru.tehkode.permissions.contract;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.fail;
import java.net.URL;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import org.junit.jupiter.api.Test;

/**
 * Plugins written for PermissionsEx 1.23.4 and earlier link against its API,
 * not
 * 1.23.5's, and 1.23.5 removed members they commonly use (such as the static
 * {@code PermissionsEx.getUser(Player)}). Every public member of the 1.23.4 API
 * types must therefore still exist in the adapter with the same signature.
 *
 * <p>
 * The baseline jar is built from tag STABLE-1.23.4 by
 * {@code scripts/pex-baseline/build-api.sh}, for Java 8, so this also runs on
 * the
 * Java 8 JVM job.
 */
class Legacy1234BinaryContractTest {
    private static final String BASELINE = "/baselines/PermissionsEx-1.23.4-api.jar";

    @Test
    void everyPublicMemberOfThe1234ApiRemainsPresent() throws Exception {
        List<Class<?>> baselineTypes = ApiSignatures.loadTypes(baselineJar(), Legacy1234BinaryContractTest.class.getClassLoader(),
                PublicApiBinaryContractTest.PUBLIC_CONTRACT_TYPE_NAMES);
        List<String> required = ApiSignatures.fingerprintAll(baselineTypes);
        List<String> actual = ApiSignatures.fingerprintAll(PublicApiBinaryContractTest.ADAPTER_CONTRACT_TYPES);

        assertFalse(required.isEmpty(), "The 1.23.4 baseline produced no signatures");

        List<String> missing = ApiSignatures.missingRequired(required, actual);
        if (!missing.isEmpty()) {
            fail("Plugins built against PermissionsEx 1.23.4 would fail to link: " + missing.size()
                    + " member(s) missing or changed in the adapter:\n" + String.join("\n", missing));
        }
    }

    private static Path baselineJar() throws Exception {
        URL resource = Legacy1234BinaryContractTest.class.getResource(BASELINE);

        if (resource == null) {
            throw new IllegalStateException("Missing " + BASELINE + "; build it with scripts/pex-baseline/build-api.sh");
        }

        return Paths.get(resource.toURI());
    }
}
