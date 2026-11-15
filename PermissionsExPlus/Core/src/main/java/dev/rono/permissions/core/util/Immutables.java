package dev.rono.permissions.core.util;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collector;
import java.util.stream.Collectors;

/**
 * Java 8 stand-ins for the Java 9+ immutable collection factories.
 *
 * <p>
 * Like {@code List.of}/{@code Set.of}/{@code Map.of} and the {@code copyOf}
 * methods, results are unmodifiable and reject {@code null} elements, keys and
 * values; the {@code of} factories reject duplicates. Unlike the JDK versions,
 * sets and maps iterate in insertion order, which is deterministic across runs.
 * </p>
 */
public final class Immutables {
    private Immutables() {
        throw new AssertionError();
    }

    @SafeVarargs
    public static <E> List<E> listOf(E... elements) {
        List<E> list = new ArrayList<>(elements.length);

        for (E element : elements) {
            list.add(Objects.requireNonNull(element, "element"));
        }

        return Collections.unmodifiableList(list);
    }

    @SafeVarargs
    public static <E> Set<E> setOf(E... elements) {
        Set<E> set = new LinkedHashSet<>(elements.length * 2);

        for (E element : elements) {
            if (!set.add(Objects.requireNonNull(element, "element"))) {
                throw new IllegalArgumentException("duplicate element: " + element);
            }
        }

        return Collections.unmodifiableSet(set);
    }

    public static <K, V> Map<K, V> mapOf() {
        return Collections.emptyMap();
    }

    public static <K, V> Map<K, V> mapOf(K k1, V v1) {
        return entries(k1, v1);
    }

    public static <K, V> Map<K, V> mapOf(K k1, V v1, K k2, V v2) {
        return entries(k1, v1, k2, v2);
    }

    public static <K, V> Map<K, V> mapOf(K k1, V v1, K k2, V v2, K k3, V v3) {
        return entries(k1, v1, k2, v2, k3, v3);
    }

    public static <K, V> Map<K, V> mapOf(K k1, V v1, K k2, V v2, K k3, V v3, K k4, V v4) {
        return entries(k1, v1, k2, v2, k3, v3, k4, v4);
    }

    public static <K, V> Map<K, V> mapOf(K k1, V v1, K k2, V v2, K k3, V v3, K k4, V v4, K k5, V v5) {
        return entries(k1, v1, k2, v2, k3, v3, k4, v4, k5, v5);
    }

    @SuppressWarnings("unchecked")
    private static <K, V> Map<K, V> entries(Object... pairs) {
        Map<K, V> map = new LinkedHashMap<>(pairs.length);

        for (int index = 0; index < pairs.length; index += 2) {
            K key = (K) Objects.requireNonNull(pairs[index], "key");
            V value = (V) Objects.requireNonNull(pairs[index + 1], "value");

            if (map.put(key, value) != null) {
                throw new IllegalArgumentException("duplicate key: " + key);
            }
        }

        return Collections.unmodifiableMap(map);
    }

    public static <E> List<E> copyOfList(Collection<? extends E> elements) {
        List<E> list = new ArrayList<>(elements.size());

        for (E element : elements) {
            list.add(Objects.requireNonNull(element, "element"));
        }

        return Collections.unmodifiableList(list);
    }

    public static <E> Set<E> copyOfSet(Collection<? extends E> elements) {
        Set<E> set = new LinkedHashSet<>(Math.max(16, elements.size() * 2));

        for (E element : elements) {
            set.add(Objects.requireNonNull(element, "element"));
        }

        return Collections.unmodifiableSet(set);
    }

    public static <K, V> Map<K, V> copyOfMap(Map<? extends K, ? extends V> source) {
        Map<K, V> map = new LinkedHashMap<>(Math.max(16, source.size() * 2));

        for (Map.Entry<? extends K, ? extends V> entry : source.entrySet()) {
            map.put(Objects.requireNonNull(entry.getKey(), "key"), Objects.requireNonNull(entry.getValue(), "value"));
        }

        return Collections.unmodifiableMap(map);
    }

    /**
     * {@code Stream.toList()}: an unmodifiable list that, like the JDK method,
     * permits nulls.
     */
    public static <T> Collector<T, ?, List<T>> toList() {
        return Collectors.collectingAndThen(Collectors.toCollection(ArrayList::new), Collections::unmodifiableList);
    }

    /** {@code Collectors.toUnmodifiableSet()}. */
    public static <T> Collector<T, ?, Set<T>> toSet() {
        return Collectors.collectingAndThen(Collectors.toCollection(LinkedHashSet::new), set -> {
            for (T element : set) {
                Objects.requireNonNull(element, "element");
            }

            return Collections.unmodifiableSet(set);
        });
    }
}
