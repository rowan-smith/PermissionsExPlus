package dev.rono.permissions.core;

import dev.rono.permissions.api.subject.PermissionMutator;
import dev.rono.permissions.api.subject.PermissionSubject;
import dev.rono.permissions.api.subject.PermissionView;
import dev.rono.permissions.api.subject.SubjectIdentity;
import dev.rono.permissions.api.subject.SubjectWorldContext;
import dev.rono.permissions.api.subject.SubjectWorldContexts;
import dev.rono.permissions.api.group.Group;
import dev.rono.permissions.api.user.User;
import dev.rono.permissions.api.world.Worlds;
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
        assertTrue(SubjectIdentity.class.isAssignableFrom(PermissionSubject.class));
        assertTrue(PermissionView.class.isAssignableFrom(PermissionSubject.class));
        assertTrue(PermissionMutator.class.isAssignableFrom(PermissionSubject.class));
    }

    @Test
    void roleInterfacesDeclareAllNonDefaultSubjectMethods() throws Exception {
        var roleMethods = declaredAbstractMethods(SubjectIdentity.class);
        roleMethods.addAll(declaredAbstractMethods(PermissionView.class));
        roleMethods.addAll(declaredAbstractMethods(PermissionMutator.class));

        var subjectMethods = declaredAbstractMethods(PermissionSubject.class);

        assertEquals(roleMethods, subjectMethods,
                "PermissionSubject abstract methods must match the union of role interfaces");
    }

    @Test
    void subjectWorldContextFactoryIsPureDelegation() throws Exception {
        var user = ((DefaultPermissionManager) manager).permissionsExApi()
                .getUserManager()
                .createUser("invariant-facade-user");

        SubjectWorldContext context = user.inWorld("world");
        user.addPermission("facade.test", "world");

        assertTrue(context.hasPermission("facade.test"));
        assertTrue(user.inWorld("world").hasPermission("facade.test"));
        assertEquals("world", context.world());
        assertSame(user, context.subject());

        user.delete();
    }

    @Test
    void userAdapterImplementsRoleInterfaces() {
        var user = ((DefaultPermissionManager) manager).permissionsExApi()
                .getUserManager()
                .createUser("invariant-roles-user");

        assertInstanceOf(SubjectIdentity.class, user);
        assertInstanceOf(PermissionView.class, user);
        assertInstanceOf(PermissionMutator.class, user);
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
        child.addParent(parent.getName(), Worlds.GLOBAL);
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
