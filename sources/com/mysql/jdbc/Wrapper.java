package com.mysql.jdbc;

import java.sql.SQLException;

public interface Wrapper {
    boolean isWrapperFor(Class<?> cls) throws SQLException;

    <T> T unwrap(Class<T> cls) throws SQLException;
}
