package dev.rono.permissions.core;

import java.util.List;
import java.util.Set;

public interface PEXConfigurationSection {
    String getString(String key);

    String getString(String key, String def);

    int getInt(String key, int def);

    boolean getBoolean(String key, boolean def);

    List<String> getStringList(String key);

    PEXConfigurationSection getSection(String key);

    Set<String> getKeys(boolean deep);

    boolean isSet(String key);

    String getName();

    java.util.Map<String, Object> getValues(boolean deep);
}
