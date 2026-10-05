package ru.tehkode.permissions.contract;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.net.URL;
import java.net.URLClassLoader;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * Reflection helpers that fingerprint the public binary surface plugins compile against.
 * Signatures use JVM-ish descriptors so return type / parameter changes fail loudly.
 */
final class ApiSignatures {
    private ApiSignatures() {
    }

    static List<String> fingerprint(Class<?> type) {
        Set<String> lines = new LinkedHashSet<>();
        lines.add(typeLine(type));

        if (type.isEnum()) {
            for (Object constant : Objects.requireNonNull(type.getEnumConstants())) {
                lines.add("ENUM " + type.getName() + "#" + ((Enum<?>) constant).name());
            }
        }

        for (Class<?> nested : sorted(type.getDeclaredClasses())) {
            if (!Modifier.isPublic(nested.getModifiers())) {
                continue;
            }

            lines.addAll(fingerprint(nested));
        }

        for (Constructor<?> ctor : sorted(type.getDeclaredConstructors(), Comparator.comparing(ApiSignatures::ctorKey))) {
            if (!Modifier.isPublic(ctor.getModifiers())) {
                continue;
            }

            lines.add("CTOR " + modifiers(ctor.getModifiers()) + " " + type.getName()
                    + "(" + params(ctor.getParameterTypes()) + ")");
        }

        for (Field field : sorted(type.getDeclaredFields(), Comparator.comparing(Field::getName))) {
            if (!Modifier.isPublic(field.getModifiers())) {
                continue;
            }

            lines.add("FIELD " + modifiers(field.getModifiers()) + " " + name(field.getType())
                    + " " + type.getName() + "#" + field.getName());
        }

        for (Method method : sorted(type.getDeclaredMethods(), Comparator.comparing(ApiSignatures::methodKey))) {
            if (!Modifier.isPublic(method.getModifiers())) {
                continue;
            }

            // Skip synthetic/bridge methods generated for generics covariance
            if (method.isSynthetic() || method.isBridge()) {
                continue;
            }

            lines.add("METHOD " + modifiers(method.getModifiers()) + " " + name(method.getReturnType())
                    + " " + type.getName() + "#" + method.getName()
                    + "(" + params(method.getParameterTypes()) + ")");
        }

        return new ArrayList<>(lines);
    }

    static List<String> fingerprintAll(Class<?>... types) {
        Set<String> lines = new LinkedHashSet<>();
        for (Class<?> type : types) {
            lines.addAll(fingerprint(type));
        }

        return lines.stream().sorted().collect(Collectors.toList());
    }

    static List<String> fingerprintAll(Iterable<Class<?>> types) {
        Set<String> lines = new LinkedHashSet<>();
        for (Class<?> type : types) {
            lines.addAll(fingerprint(type));
        }

        return lines.stream().sorted().collect(Collectors.toList());
    }

    /**
     * Load contract types from a baseline JAR (e.g. classic PermissionsEx 1.23.5).
     * Uses child-first loading for {@code ru.tehkode.*} so the adapter classpath cannot
     * shadow the frozen 1.23.5 bytecode. Bukkit and other provided types still resolve
     * via {@code parent}.
     */
    static List<Class<?>> loadTypes(Path jar, ClassLoader parent, String... typeNames) throws Exception {
        URLClassLoader loader = new URLClassLoader(new URL[]{jar.toUri().toURL()}, parent) {
            @Override
            protected Class<?> loadClass(String name, boolean resolve) throws ClassNotFoundException {
                synchronized (getClassLoadingLock(name)) {
                    Class<?> loaded = findLoadedClass(name);
                    if (loaded != null) {
                        return loaded;
                    }

                    if (name.startsWith("ru.tehkode.")) {
                        try {
                            loaded = findClass(name);
                            if (resolve) {
                                resolveClass(loaded);
                            }

                            return loaded;
                        } catch (ClassNotFoundException ignored) {
                            // fall through to parent for rare tehkode types not in the baseline jar
                        }
                    }

                    return super.loadClass(name, resolve);
                }
            }
        };

        List<Class<?>> types = new ArrayList<>();
        for (String name : typeNames) {
            types.add(Class.forName(name, false, loader));
        }

        return types;
    }

    static Path resolveBaselineJar() throws IOException {
        String override = System.getProperty("pex.contracts.baselineJar");
        if (override != null && !override.isBlank()) {

            Path path = Path.of(override);
            if (!Files.isRegularFile(path)) {
                throw new IOException("Baseline JAR not found: " + path.toAbsolutePath());
            }

            return path.toAbsolutePath().normalize();
        }

        URL resource = ApiSignatures.class.getResource("/baselines/PermissionsEx-1.23.5-api.jar");
        if (resource == null) {
            throw new IOException("Missing classpath resource /baselines/PermissionsEx-1.23.5-api.jar");
        }

        // When running from Maven, the resource is inside target/test-classes or a jar URL.
        if ("file".equals(resource.getProtocol())) {
            try {
                return Path.of(resource.toURI());
            } catch (Exception e) {
                throw new IOException("Cannot convert baseline JAR URL to path: " + resource, e);
            }
        }

        // Fall back to the source-tree copy for update mode / IDE runs from module root.
        Path moduleRelative = Path.of("src/test/resources/baselines/PermissionsEx-1.23.5-api.jar");
        if (Files.isRegularFile(moduleRelative)) {
            return moduleRelative.toAbsolutePath().normalize();
        }

        throw new IOException("Cannot resolve baseline JAR from resource URL: " + resource);
    }

    static List<String> readResource(String resourcePath) throws IOException {
        InputStream in = ApiSignatures.class.getResourceAsStream(resourcePath);
        if (in == null) {
            throw new IOException("Missing contract resource: " + resourcePath);
        }

        try (BufferedReader reader = new BufferedReader(new InputStreamReader(in, StandardCharsets.UTF_8))) {
            return reader.lines()
                    .map(String::trim)
                    .filter(line -> !line.isEmpty() && !line.startsWith("#"))
                    .collect(Collectors.toList());
        }
    }

    static void writeLines(Path path, List<String> lines) throws IOException {
        Files.createDirectories(path.getParent());

        try (PrintWriter out = new PrintWriter(Files.newBufferedWriter(path, StandardCharsets.UTF_8))) {
            out.println("# Public API binary contract baseline: PermissionsEx 1.23.5 (tag STABLE-1.23.5).");
            out.println("# Source of truth JAR: src/test/resources/baselines/PermissionsEx-1.23.5-api.jar");
            out.println("# Do not edit by hand. Regenerate from that JAR with:");
            out.println("#   mvn -pl PermissionsExApiAdapter test -Dtest=PublicApiBinaryContractTest -Dpex.contracts.update=true");
            out.println("# Required members must remain on the ApiAdapter; additive public members are allowed.");

            for (String line : lines) {
                out.println(line);
            }
        }
    }

    static List<String> missingRequired(List<String> required, List<String> actual) {
        Set<String> have = new LinkedHashSet<>(actual);
        List<String> missing = new ArrayList<>();

        for (String line : required) {
            if (!have.contains(line)) {
                missing.add(line);
            }
        }

        return missing;
    }

    private static String typeLine(Class<?> type) {
        String kind;
        if (type.isAnnotation()) {
            kind = "ANNOTATION";

        } else if (type.isInterface()) {
            kind = "INTERFACE";

        } else if (type.isEnum()) {
            kind = "ENUM_TYPE";

        } else {
            kind = "CLASS";
        }

        return kind + " " + modifiers(type.getModifiers()) + " " + type.getName()
                + (type.getSuperclass() != null && type.getSuperclass() != Object.class
                ? " extends " + type.getSuperclass().getName() : "")
                + interfacesSuffix(type);
    }

    private static String interfacesSuffix(Class<?> type) {
        Class<?>[] interfaces = type.getInterfaces();
        if (interfaces.length == 0) {
            return "";
        }

        return " implements " + Arrays.stream(interfaces)
                .map(Class::getName)
                .sorted()
                .collect(Collectors.joining(","));
    }

    private static String modifiers(int mods) {
        List<String> parts = new ArrayList<>();
        if (Modifier.isPublic(mods)) {
            parts.add("public");
        }

        if (Modifier.isProtected(mods)) {
            parts.add("protected");
        }

        if (Modifier.isStatic(mods)) {
            parts.add("static");
        }

        if (Modifier.isFinal(mods)) {
            parts.add("final");
        }

        if (Modifier.isAbstract(mods) && !Modifier.isInterface(mods)) {
            parts.add("abstract");
        }

        return String.join(" ", parts);
    }

    private static String params(Class<?>[] types) {
        return Arrays.stream(types).map(ApiSignatures::name).collect(Collectors.joining(","));
    }

    private static String name(Class<?> type) {
        if (type.isArray()) {
            return name(type.getComponentType()) + "[]";
        }
        return type.getName();
    }

    private static String ctorKey(Constructor<?> ctor) {
        return params(ctor.getParameterTypes());
    }

    private static String methodKey(Method method) {
        return method.getName() + "(" + params(method.getParameterTypes()) + ")" + name(method.getReturnType());
    }

    private static <T> List<T> sorted(T[] values) {
        return sorted(values, null);
    }

    private static <T> List<T> sorted(T[] values, Comparator<T> comparator) {
        List<T> list = new ArrayList<>(Arrays.asList(values));

        if (comparator != null) {
            list.sort(comparator);

        } else if (!list.isEmpty() && list.getFirst() instanceof Class) {
            @SuppressWarnings("unchecked")
            Comparator<T> className = (Comparator<T>) Comparator.comparing((Class<?> c) -> c.getName());
            list.sort(className);
        }

        return list;
    }
}
