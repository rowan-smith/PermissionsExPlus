package ru.tehkode.utils;

import java.lang.reflect.Field;

/**
 * Type-safe read/write accessor for a declared field on an object instance.
 *
 * <p>Resolves the field by name on the given class or its superclasses, makes it accessible, and
 * validates that its declared type is assignable to the requested type parameter. Intended for
 * internal compatibility shims where direct field access is required.</p>
 *
 * @param <Instance> type of object that owns the field
 * @param <Type>     expected field value type
 * @author zml2008
 */
public class FieldReplacer<Instance, Type> {
	private final Class<Type> requiredType;
	private final Field field;

	/**
	 * Locates and prepares a field for reflective access.
	 *
	 * @param clazz        class that declares (or inherits) the field; must not be {@code null}
	 * @param fieldName    name of the field to access; must not be {@code null}
	 * @param requiredType expected field value type; must not be {@code null}
	 * @throws ExceptionInInitializerError if the field does not exist or is not assignable to {@code requiredType}
	 */
	public FieldReplacer(Class<? extends Instance> clazz, String fieldName, Class<Type> requiredType) {
		this.requiredType = requiredType;
		field = getField(clazz, fieldName);
		if (field == null) {
			throw new ExceptionInInitializerError("No such field " + fieldName + " in class " + clazz);
		}

		field.setAccessible(true);
		if (!requiredType.isAssignableFrom(field.getType())) {
			throw new ExceptionInInitializerError("Field of wrong type");
		}
	}

	/**
	 * Reads the current value of the bound field from an instance.
	 *
	 * @param instance object whose field should be read; must not be {@code null}
	 * @return current field value cast to {@code Type}
	 * @throws Error if reflective access is unexpectedly denied
	 */
	public Type get(Instance instance) {
		try {
			return this.requiredType.cast(field.get(instance));
		} catch (IllegalAccessException e) {
			throw new Error(e);
		}
	}

	/**
	 * Writes a new value to the bound field on an instance.
	 *
	 * @param instance  object whose field should be updated; must not be {@code null}
	 * @param newValue  new field value; may be {@code null} if the field type allows it
	 * @throws Error if reflective access is unexpectedly denied
	 */
	public void set(Instance instance, Type newValue) {
		try {
			field.set(instance, newValue);
		} catch (IllegalAccessException e) {
			throw new Error(e); // This shouldn't happen because we call setAccessible in the constructor
		}
	}

	private static Field getField(Class<?> clazz, String fieldName) {
		while (clazz != null && clazz != Object.class) {
			try {
				return clazz.getDeclaredField(fieldName);
			} catch (NoSuchFieldException e) {
				clazz = clazz.getSuperclass();
			}
		}
		return null;
	}
}
