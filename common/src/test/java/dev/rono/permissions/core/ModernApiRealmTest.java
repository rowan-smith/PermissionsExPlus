package dev.rono.permissions.core;

import dev.rono.permissions.api.realm.RealmNotFoundException;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/** Realm registry and inheritance behavior on the modern API. */
class ModernApiRealmTest extends ModernApiTestSupport {

    @Test
    void realmFindGetCreateLifecycle() {
        assertTrue(api().getRealmManager().findRealm("missing-realm-xyz").isEmpty());
        assertThrows(RealmNotFoundException.class, () -> api().getRealmManager().getRealm("missing-realm-xyz"));

        api().getRealmManager().createRealm("lifecycle-realm");
        assertTrue(api().getRealmManager().exists("lifecycle-realm"));
        assertEquals("lifecycle-realm", api().getRealmManager().getRealm("lifecycle-realm").getName());
        assertTrue(api().getRealmManager().listRealmNames().contains("lifecycle-realm"));
    }

    @Test
    void realmInheritanceParentsAndTree() {
        api().getRealmManager().createRealm("parent-realm");
        var child = api().getRealmManager().createRealm("child-realm");
        child.addParent("parent-realm");

        assertEquals(List.of("parent-realm"), child.parents());
        assertTrue(child.parentTree().contains("parent-realm"));

        child.removeParent("parent-realm");
        assertTrue(child.parents().isEmpty());
    }
}
