package dev.rono.permissions.core.storage;

import dev.rono.permissions.core.backends.file.YamlMaps;
import org.yaml.snakeyaml.DumperOptions;
import org.yaml.snakeyaml.Yaml;

import java.io.Writer;
import java.sql.SQLException;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/** Exports local SQL storage to YAML-compatible text for diagnostics and backup. */
public final class LocalSqlExporter {

    private LocalSqlExporter() {}

    public static void exportYaml(LocalSqlRepository repository, Writer writer) throws Exception {
        Map<String, Object> root = new LinkedHashMap<>();
        root.put(YamlMaps.SCHEMA_VERSION, LocalSqlSchemaUpgrader.LATEST_VERSION);
        root.put(YamlMaps.GROUPS, exportGroups(repository));
        root.put(YamlMaps.USERS, exportUsers(repository));
        root.put(YamlMaps.WORLD_INHERITANCE_LEGACY, exportWorldInheritance(repository));

        DumperOptions options = new DumperOptions();
        options.setDefaultFlowStyle(DumperOptions.FlowStyle.BLOCK);
        options.setIndent(2);
        new Yaml(options).dump(root, writer);
    }

    private static Map<String, Object> exportGroups(LocalSqlRepository repository) throws Exception {
        Map<String, Object> groups = new LinkedHashMap<>();
        for (String name : repository.listGroupNames()) {
            int id = repository.findGroupId(name).orElseThrow();
            Map<String, Object> node = new LinkedHashMap<>();
            node.put(YamlMaps.PERMISSIONS, repository.getGroupPermissions(id, null));
            List<String> parents = repository.getGroupParents(id);
            if (!parents.isEmpty()) {
                node.put(YamlMaps.GROUP_PARENT_LIST, parents);
            }
            Map<String, String> options = repository.getGroupEntityOptions(id, null);
            if (!options.isEmpty()) {
                node.put(YamlMaps.OPTIONS, new LinkedHashMap<>(options));
            }
            groups.put(name, node);
        }
        return groups;
    }

    private static Map<String, Object> exportUsers(LocalSqlRepository repository) throws Exception {
        Map<String, Object> users = new LinkedHashMap<>();
        for (String name : repository.listUserNames()) {
            var user = repository.findUserByName(name).orElseThrow();
            Map<String, Object> node = new LinkedHashMap<>();
            List<String> parents = repository.getUserParents(user.getId(), null);
            if (!parents.isEmpty()) {
                node.put(YamlMaps.USER_PARENT_LIST, parents);
            }
            node.put(YamlMaps.PERMISSIONS, repository.getUserPermissions(user.getId(), null));
            Map<String, String> options = repository.getUserEntityOptions(user.getId(), null);
            if (!options.isEmpty()) {
                node.put(YamlMaps.OPTIONS, new LinkedHashMap<>(options));
            }
            users.put(name, node);
        }
        return users;
    }

    private static Map<String, List<String>> exportWorldInheritance(LocalSqlRepository repository) throws SQLException {
        return repository.getAllWorldInheritance();
    }
}
