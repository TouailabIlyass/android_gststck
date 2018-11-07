package com.mysql.jdbc;

import java.sql.SQLException;
import java.util.Properties;

public interface Extension {
    void destroy();

    void init(Connection connection, Properties properties) throws SQLException;
}
