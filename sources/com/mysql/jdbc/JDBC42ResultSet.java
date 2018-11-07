package com.mysql.jdbc;

import java.sql.Date;
import java.sql.SQLException;
import java.sql.SQLType;
import java.sql.Time;
import java.sql.Timestamp;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.OffsetDateTime;
import java.time.OffsetTime;
import java.time.format.DateTimeParseException;

public class JDBC42ResultSet extends JDBC4ResultSet {
    public JDBC42ResultSet(long updateCount, long updateID, MySQLConnection conn, StatementImpl creatorStmt) {
        super(updateCount, updateID, conn, creatorStmt);
    }

    public JDBC42ResultSet(String catalog, Field[] fields, RowData tuples, MySQLConnection conn, StatementImpl creatorStmt) throws SQLException {
        super(catalog, fields, tuples, conn, creatorStmt);
    }

    public <T> T getObject(int columnIndex, Class<T> type) throws SQLException {
        if (type == null) {
            throw SQLError.createSQLException("Type parameter can not be null", SQLError.SQL_STATE_ILLEGAL_ARGUMENT, getExceptionInterceptor());
        }
        T t = null;
        if (type.equals(LocalDate.class)) {
            Date date = getDate(columnIndex);
            if (date != null) {
                t = type.cast(date.toLocalDate());
            }
            return t;
        } else if (type.equals(LocalDateTime.class)) {
            Timestamp timestamp = getTimestamp(columnIndex);
            if (timestamp != null) {
                t = type.cast(timestamp.toLocalDateTime());
            }
            return t;
        } else if (type.equals(LocalTime.class)) {
            Time time = getTime(columnIndex);
            if (time != null) {
                t = type.cast(time.toLocalTime());
            }
            return t;
        } else if (type.equals(OffsetDateTime.class)) {
            try {
                string = getString(columnIndex);
                if (string != null) {
                    t = type.cast(OffsetDateTime.parse(string));
                }
                return t;
            } catch (DateTimeParseException e) {
            }
        } else {
            if (type.equals(OffsetTime.class)) {
                try {
                    string = getString(columnIndex);
                    if (string != null) {
                        t = type.cast(OffsetTime.parse(string));
                    }
                    return t;
                } catch (DateTimeParseException e2) {
                }
            }
            return super.getObject(columnIndex, type);
        }
    }

    public void updateObject(int columnIndex, Object x, SQLType targetSqlType) throws SQLException {
        throw new NotUpdatable();
    }

    public void updateObject(int columnIndex, Object x, SQLType targetSqlType, int scaleOrLength) throws SQLException {
        throw new NotUpdatable();
    }

    public void updateObject(String columnLabel, Object x, SQLType targetSqlType) throws SQLException {
        throw new NotUpdatable();
    }

    public void updateObject(String columnLabel, Object x, SQLType targetSqlType, int scaleOrLength) throws SQLException {
        throw new NotUpdatable();
    }
}
