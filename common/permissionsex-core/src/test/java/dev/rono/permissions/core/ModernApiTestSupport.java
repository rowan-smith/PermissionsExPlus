package dev.rono.permissions.core;

import dev.rono.permissions.api.PermissionsExApi;
import ru.tehkode.permissions.PEXTestBase;

/** Shared harness for modern {@link PermissionsExApi} integration tests. */
abstract class ModernApiTestSupport extends PEXTestBase {

    protected final PermissionsExApi api() {
        return ((DefaultPermissionManager) manager).permissionsExApi();
    }
}
