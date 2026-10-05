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
package ru.tehkode.permissions.backends.sql;

import java.io.Closeable;
import java.sql.Connection;
import java.sql.SQLException;

/**
 * Compatibility stub for the legacy SQL connection helper.
 */
public class SQLConnection implements Closeable {
    private final Connection conn;
    private final SQLBackend backend;

    public SQLConnection(Connection conn, SQLBackend backend) {
        this.conn = conn;
        this.backend = backend;
    }

    public Connection getConnection() {
        return conn;
    }

    public SQLBackend getBackend() {
        return backend;
    }

    @Override
    public void close() {
        if (conn != null) {
            try {
                conn.close();
            } catch (SQLException ignored) {
            }
        }
    }
}
