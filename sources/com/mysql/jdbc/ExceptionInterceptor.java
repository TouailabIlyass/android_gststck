package com.mysql.jdbc;

import java.sql.SQLException;

public interface ExceptionInterceptor extends Extension {
    SQLException interceptException(SQLException sQLException, Connection connection);
}
