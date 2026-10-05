package ru.tehkode.utils;

import java.lang.reflect.Field;

/**
 * @author zml2008
 * ABI shell. Reflection helpers are unused; Plus owns Bukkit injection.
 */
public class FieldReplacer<T, V> {
    private final Class<T> clazz;
    private final String fieldName;
    private final Class<V> fieldValueClass;

    public FieldReplacer(Class<T> clazz, String fieldName, Class<V> fieldValueClass) {
        this.clazz = clazz;
        this.fieldName = fieldName;
        this.fieldValueClass = fieldValueClass;
    }

    public V get(T instance) {
        return null;
    }

    public void set(T instance, V newValue) {}

    private Field getField() {
        return null;
    }
}
