package dev.rono.permissions.core;

import dev.rono.permissions.api.permission.PermissionContext;
import dev.rono.permissions.api.subject.SubjectContext;
import dev.rono.permissions.api.group.Group;
import dev.rono.permissions.api.user.User;
import org.junit.jupiter.api.Test;
import ru.tehkode.permissions.PEXTestBase;

import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.Arrays;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import static org.junit.jupiter.api.Assertions.*;

/** Guards architectural invariants documented in docs/api/API_INVARIANTS.md. */
class ApiLayerInvariantTest extends PEXTestBase {

    @Test
    void permissionSubjectComposesRoleInterfaces() {
        assertTrue(dev.rono.permissions.api.subject.SubjectIdentity.class.isAssignableFrom(
                dev.rono.permissions.api.subject.PermissionSubject.class));
        assertTrue(dev.rono.permissions.api.subject.PermissionView.class.isAssignableFrom(
                dev.rono.permissions.api.subject.PermissionSubject.class));
        assertTrue(dev.rono.permissions.api.subject.PermissionMutator.class.isAssignableFrom(
                dev.rono.permissions.api.subject.PermissionSubject.class));
    }

    @Test
    void roleInterfacesDeclareAllNonDefaultSubjectMethods() throws Exception {
        var roleMethods = declaredAbstractMethods(dev.rono.permissions.api.subject.SubjectIdentity.class);
        roleMethods.addAll(declaredAbstractMethods(dev.rono.permissions.api.subject.PermissionView.class));
        roleMethods.addAll(declaredAbstractMethods(dev.rono.permissions.api.subject.PermissionMutator.class));

        var subjectMethods = declaredAbstractMethods(dev.rono.permissions.api.subject.PermissionSubject.class);

        assertTrue(subjectMethods.containsAll(roleMethods),
                "PermissionSubject must declare every abstract method from role interfaces");
    }

    @Test
    void subjectContextFactoryIsPureDelegation() throws Exception {
        var user = ((DefaultPermissionManager) manager).permissionsExApi()
                .getUserManager()
                .createUser("invariant-facade-user");

        SubjectContext context = user.inContext(PermissionContext.world("world"));
        user.addPermission("facade.test", PermissionContext.world("world"));

        assertTrue(context.has("facade.test"));
        assertTrue(user.inContext(PermissionContext.world("world")).has("facade.test"));
        assertEquals("world", context.context().get(PermissionContext.WORLD).orElseThrow());
        assertSame(user, context.subject());

        user.delete();
    }

    @Test
    void userAdapterImplementsRoleInterfaces() {
        var user = ((DefaultPermissionManager) manager).permissionsExApi()
                .getUserManager()
                .createUser("invariant-roles-user");

        assertInstanceOf(dev.rono.permissions.api.subject.SubjectIdentity.class, user);
        assertInstanceOf(dev.rono.permissions.api.subject.PermissionView.class, user);
        assertInstanceOf(dev.rono.permissions.api.subject.PermissionMutator.class, user);
        assertInstanceOf(User.class, user);

        user.delete();
    }

    @Test
    void groupHierarchyUsesCanonicalEngine() throws Exception {
        var parent = ((DefaultPermissionManager) manager).permissionsExApi()
                .getGroupManager()
                .createGroup("engine-parent");
        var child = ((DefaultPermissionManager) manager).permissionsExApi()
                .getGroupManager()
                .createGroup("engine-child");
        child.addParent(parent.getName());
        child.save();

        assertEquals(List.of("engine-child"), parent.childIdentifiers());
        assertEquals(parent.childIdentifiers(), parent.children().stream().map(Group::getName).toList());
    }

    private static Set<String> declaredAbstractMethods(Class<?> type) {
        return Arrays.stream(type.getMethods())
                .filter(m -> Modifier.isAbstract(m.getModifiers()))
                .map(ApiLayerInvariantTest::signature)
                .collect(Collectors.toSet());
    }

    private static String signature(Method method) {
        return method.getName() + Arrays.toString(method.getParameterTypes());
    }
}
