/*
 * PermissionsEx - Permissions plugin for Bukkit
 * Copyright (C) 2011 t3hk0d3 http://www.tehkode.ru
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License
 * as published by the Free Software Foundation; either version 2
 * of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02110-1301, USA.
 */
package ru.tehkode.permissions.backends.file;

import java.io.File;
import org.bukkit.configuration.ConfigurationSection;
import ru.tehkode.permissions.PermissionManager;
import ru.tehkode.permissions.exceptions.PermissionBackendException;

/**
 * Legacy {@code file} backend alias.
 *
 * <p>
 * Storage is provided by PermissionsExPlus via the data bridge. The public type
 * and fields remain for binary compatibility with plugins that reference
 * {@code FileBackend}.
 * </p>
 */
public class FileBackend extends ru.tehkode.permissions.backends.data.PermissionBackend {
    public final static char PATH_SEPARATOR = '/';

    public FileConfig permissions;
    public File permissionsFile;

    public FileBackend(PermissionManager manager, ConfigurationSection config) throws PermissionBackendException {
        super(manager, config);

        String permissionFilename = config != null ? config.getString("file") : null;
        if (permissionFilename == null) {
            permissionFilename = "permissions.yml";
        }

        this.permissionsFile = new File(permissionFilename);
        this.permissions = new FileConfig(this.permissionsFile);
        manager.getLogger().info("file backend alias redirects to PermissionsExPlus data storage");
    }
}
