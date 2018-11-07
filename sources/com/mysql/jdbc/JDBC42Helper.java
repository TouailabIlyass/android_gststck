package com.mysql.jdbc;

import java.sql.Date;
import java.sql.JDBCType;
import java.sql.SQLException;
import java.sql.SQLType;
import java.sql.Time;
import java.sql.Timestamp;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;

public class JDBC42Helper {
    static Object convertJavaTimeToJavaSql(Object x) {
        if (x instanceof LocalDate) {
            return Date.valueOf((LocalDate) x);
        }
        if (x instanceof LocalDateTime) {
            return Timestamp.valueOf((LocalDateTime) x);
        }
        if (x instanceof LocalTime) {
            return Time.valueOf((LocalTime) x);
        }
        return x;
    }

    static boolean isSqlTypeSupported(int sqlType) {
        return (sqlType == 2012 || sqlType == 2013 || sqlType == 2014) ? false : true;
    }

    static int checkSqlType(int sqlType, ExceptionInterceptor exceptionInterceptor) throws SQLException {
        if (isSqlTypeSupported(sqlType)) {
            return sqlType;
        }
        StringBuilder stringBuilder = new StringBuilder();
        stringBuilder.append(Messages.getString("UnsupportedSQLType.0"));
        stringBuilder.append(JDBCType.valueOf(sqlType));
        throw SQLError.createSQLFeatureNotSupportedException(stringBuilder.toString(), SQLError.SQL_STATE_DRIVER_NOT_CAPABLE, exceptionInterceptor);
    }

    static int translateAndCheckSqlType(SQLType sqlType, ExceptionInterceptor exceptionInterceptor) throws SQLException {
        return checkSqlType(sqlType.getVendorTypeNumber().intValue(), exceptionInterceptor);
    }
}
