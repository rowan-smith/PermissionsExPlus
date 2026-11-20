package ru.tehkode.permissions.contract;

import static org.junit.jupiter.api.Assertions.fail;
import static org.junit.jupiter.api.Assumptions.assumeFalse;
import java.io.DataInputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Deque;
import java.util.Enumeration;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;
import org.junit.jupiter.api.Test;

/**
 * Real plugins built against the original PermissionsEx must link against the
 * adapter: every method, constructor and field they reference in
 * {@code ru.tehkode} must resolve, with the same descriptor, the way the JVM
 * resolves it when the plugin runs.
 *
 * <p>
 * Plugins come from {@code src/test/resources/legacy-plugins} (real plugins of
 * the PermissionsEx 1.x era), {@code target/legacy-plugins} (Vault 1.7.3 and
 * the
 * LegacyPexProbe, built by {@code scripts/legacy-plugins/build.sh}) and the
 * directory in {@code -Dpex.legacyPlugins=...}, if set.
 */
class LegacyPluginLinkageTest {
    private static final String PACKAGE = "ru/tehkode/";

    @Test
    void everyPermissionsExReferenceInLegacyPluginsResolves() throws Exception {
        List<File> jars = legacyPluginJars();
        assumeFalse(jars.isEmpty(), "No legacy plugin jars; run scripts/legacy-plugins/build.sh or set -Dpex.legacyPlugins");

        List<String> problems = new ArrayList<>();
        int checked = 0;

        for (File jar : jars) {
            Set<String> references = references(jar);

            for (String reference : references) {
                checked++;
                String problem = resolve(reference);

                if (problem != null) {
                    problems.add(jar.getName() + ": " + problem);
                }
            }

            System.out.println("Linkage: " + jar.getName() + " - " + references.size() + " PermissionsEx reference(s)");
        }

        if (!problems.isEmpty()) {
            fail(problems.size() + " of " + checked + " PermissionsEx reference(s) in legacy plugins do not resolve against the adapter:\n"
                    + String.join("\n", problems));
        }
    }

    private static List<File> legacyPluginJars() {
        List<File> jars = new ArrayList<>();
        addJars(jars, new File("src/test/resources/legacy-plugins"));
        addJars(jars, new File("../target/legacy-plugins"));

        String extra = System.getProperty("pex.legacyPlugins");
        if (extra != null && !extra.isEmpty()) {
            addJars(jars, new File(extra));
        }

        return jars;
    }

    private static void addJars(List<File> jars, File directory) {
        File[] files = directory.listFiles((dir, name) -> name.endsWith(".jar"));

        if (files != null) {
            for (File file : files) {
                jars.add(file);
            }
        }
    }

    /**
     * "kind owner name descriptor" for each reference into ru/tehkode in the jar.
     */
    private static Set<String> references(File jar) throws IOException {
        Set<String> references = new TreeSet<>();

        try (JarFile file = new JarFile(jar)) {
            Enumeration<JarEntry> entries = file.entries();

            while (entries.hasMoreElements()) {
                JarEntry entry = entries.nextElement();

                if (entry.getName().endsWith(".class")) {
                    try (InputStream in = file.getInputStream(entry)) {
                        readReferences(new DataInputStream(in), references);
                    }
                }
            }
        }

        return references;
    }

    /**
     * Reads Fieldref, Methodref and InterfaceMethodref entries from a class file's
     * constant pool.
     */
    private static void readReferences(DataInputStream in, Set<String> references) throws IOException {
        if (in.readInt() != 0xCAFEBABE) {
            return;
        }

        in.readUnsignedShort();
        in.readUnsignedShort();

        int count = in.readUnsignedShort();
        int[] tags = new int[count];
        String[] utf8 = new String[count];
        int[] first = new int[count];
        int[] second = new int[count];

        for (int i = 1; i < count; i++) {
            int tag = in.readUnsignedByte();
            tags[i] = tag;

            switch (tag) {
                case 1 :
                    utf8[i] = in.readUTF();
                    break;
                case 7 :
                case 8 :
                case 16 :
                case 19 :
                case 20 :
                    first[i] = in.readUnsignedShort();
                    break;
                case 3 :
                case 4 :
                    in.readInt();
                    break;
                case 5 :
                case 6 :
                    in.readLong();
                    i++;
                    break;
                case 9 :
                case 10 :
                case 11 :
                case 12 :
                case 17 :
                case 18 :
                    first[i] = in.readUnsignedShort();
                    second[i] = in.readUnsignedShort();
                    break;
                case 15 :
                    in.readUnsignedByte();
                    in.readUnsignedShort();
                    break;
                default :
                    throw new IOException("Unknown constant pool tag " + tag);
            }
        }

        for (int i = 1; i < count; i++) {
            if (tags[i] == 9 || tags[i] == 10 || tags[i] == 11) {
                String owner = utf8[first[first[i]]];
                int nameAndType = second[i];
                String name = utf8[first[nameAndType]];
                String descriptor = utf8[second[nameAndType]];

                if (owner.startsWith(PACKAGE)) {
                    references.add((tags[i] == 9 ? "field " : "method ") + owner + " " + name + " " + descriptor);
                }
            }
        }
    }

    /** @return null if the reference resolves, or why it does not */
    private static String resolve(String reference) {
        String[] parts = reference.split(" ");
        String kind = parts[0];
        String owner = parts[1];
        String name = parts[2];
        String descriptor = parts[3];
        Class<?> type;

        try {
            type = Class.forName(owner.replace('/', '.'), false, LegacyPluginLinkageTest.class.getClassLoader());
        } catch (ClassNotFoundException | LinkageError missing) {
            return "missing class " + owner.replace('/', '.');
        }

        boolean found = kind.equals("field") ? hasField(type, name, descriptor) : hasMethod(type, name, descriptor);

        return found ? null : "missing " + kind + " " + owner.replace('/', '.') + "#" + name + descriptor;
    }

    private static boolean hasMethod(Class<?> type, String name, String descriptor) {
        if (name.equals("<init>")) {
            for (Constructor<?> constructor : type.getDeclaredConstructors()) {
                if (descriptor(constructor.getParameterTypes(), void.class).equals(descriptor)) {
                    return true;
                }
            }

            return false;
        }

        for (Class<?> candidate : hierarchy(type)) {
            for (Method method : candidate.getDeclaredMethods()) {
                if (method.getName().equals(name) && descriptor(method.getParameterTypes(), method.getReturnType()).equals(descriptor)) {
                    return true;
                }
            }
        }

        return false;
    }

    private static boolean hasField(Class<?> type, String name, String descriptor) {
        for (Class<?> candidate : hierarchy(type)) {
            for (Field field : candidate.getDeclaredFields()) {
                if (field.getName().equals(name) && descriptor(field.getType()).equals(descriptor)) {
                    return true;
                }
            }
        }

        return false;
    }

    /**
     * The type, its superclasses and all their interfaces: where the JVM looks up
     * members.
     */
    private static List<Class<?>> hierarchy(Class<?> type) {
        List<Class<?>> result = new ArrayList<>();
        Set<Class<?>> seen = new HashSet<>();
        Deque<Class<?>> queue = new ArrayDeque<>();
        queue.add(type);

        while (!queue.isEmpty()) {
            Class<?> next = queue.poll();

            if (!seen.add(next)) {
                continue;
            }

            result.add(next);

            if (next.getSuperclass() != null) {
                queue.add(next.getSuperclass());
            }

            for (Class<?> parent : next.getInterfaces()) {
                queue.add(parent);
            }
        }

        return result;
    }

    private static String descriptor(Class<?>[] parameters, Class<?> returnType) {
        StringBuilder builder = new StringBuilder("(");

        for (Class<?> parameter : parameters) {
            builder.append(descriptor(parameter));
        }

        return builder.append(')').append(descriptor(returnType)).toString();
    }

    private static String descriptor(Class<?> type) {
        if (type.isArray()) {
            return type.getName().replace('.', '/');
        }

        if (type.isPrimitive()) {
            if (type == void.class) {
                return "V";
            }

            if (type == boolean.class) {
                return "Z";
            }

            if (type == byte.class) {
                return "B";
            }

            if (type == char.class) {
                return "C";
            }

            if (type == short.class) {
                return "S";
            }

            if (type == int.class) {
                return "I";
            }

            if (type == long.class) {
                return "J";
            }

            if (type == float.class) {
                return "F";
            }

            return "D";
        }

        return "L" + type.getName().replace('.', '/') + ";";
    }
}
