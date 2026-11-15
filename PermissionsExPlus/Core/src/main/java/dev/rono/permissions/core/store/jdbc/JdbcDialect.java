package dev.rono.permissions.core.store.jdbc;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.sql.Types;
import java.time.Instant;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.Calendar;
import java.util.TimeZone;

/**
 * The small set of SQL differences between the supported databases.
 *
 * <p>
 * Column types and temporal bindings match what Hibernate used for the same
 * tables on H2, MySQL, and Postgres, so databases created by earlier 2.0
 * snapshots are read correctly. (Hibernate had no SQLite dialect, so SQLite
 * has no earlier data to stay compatible with.)
 * </p>
 */
public enum JdbcDialect {
    H2("TIMESTAMP(6) WITH TIME ZONE", true, TemporalBinding.OFFSET_DATE_TIME),
    SQLITE("TEXT", true, TemporalBinding.ISO_TEXT),
    MYSQL("DATETIME(6)", false, TemporalBinding.UTC_TIMESTAMP),
    POSTGRES("TIMESTAMP(6) WITH TIME ZONE", true, TemporalBinding.OFFSET_DATE_TIME);

    private final String timestampType;
    private final boolean createIndexIfNotExists;
    private final TemporalBinding temporal;

    JdbcDialect(String timestampType, boolean createIndexIfNotExists, TemporalBinding temporal) {
        this.timestampType = timestampType;
        this.createIndexIfNotExists = createIndexIfNotExists;
        this.temporal = temporal;
    }

    /** Column type used for node expiry instants. */
    public String timestampType() {
        return timestampType;
    }

    /** Whether {@code CREATE INDEX IF NOT EXISTS} is available (MySQL lacks it). */
    public boolean createIndexIfNotExists() {
        return createIndexIfNotExists;
    }

    /**
     * SQLite serialises writers on one file; a single pooled connection avoids
     * SQLITE_BUSY between concurrent saves.
     */
    public boolean singleConnection() {
        return this == SQLITE;
    }

    void setInstant(PreparedStatement statement, int column, Instant value) throws SQLException {
        if (value == null) {
            statement.setNull(column, temporal == TemporalBinding.ISO_TEXT ? Types.VARCHAR : Types.TIMESTAMP);
            return;
        }

        switch (temporal) {
            case OFFSET_DATE_TIME :
                statement.setObject(column, OffsetDateTime.ofInstant(value, ZoneOffset.UTC));
                break;
            case UTC_TIMESTAMP :
                statement.setTimestamp(column, Timestamp.from(value), utc());
                break;
            default :
                statement.setString(column, value.toString());
        }
    }

    Instant getInstant(ResultSet rows, int column) throws SQLException {
        switch (temporal) {
            case OFFSET_DATE_TIME : {
                OffsetDateTime value = rows.getObject(column, OffsetDateTime.class);
                return value == null ? null : value.toInstant();
            }

            case UTC_TIMESTAMP : {
                Timestamp value = rows.getTimestamp(column, utc());
                return value == null ? null : value.toInstant();
            }

            default : {
                String value = rows.getString(column);
                return value == null ? null : Instant.parse(value);
            }
        }
    }

    private static Calendar utc() {
        return Calendar.getInstance(TimeZone.getTimeZone("UTC"));
    }

    /**
     * How an {@link Instant} is bound, chosen so no driver applies the JVM time
     * zone.
     */
    private enum TemporalBinding {
        /**
         * JDBC 4.2 {@code OffsetDateTime} for {@code TIMESTAMP WITH TIME ZONE} columns.
         */
        OFFSET_DATE_TIME,
        /**
         * {@code Timestamp} through a UTC calendar for zone-less {@code DATETIME}
         * columns.
         */
        UTC_TIMESTAMP,
        /**
         * ISO-8601 text: SQLite has no timestamp type and its driver keeps only
         * milliseconds.
         */
        ISO_TEXT
    }
}
