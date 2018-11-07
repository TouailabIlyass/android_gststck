package com.mysql.jdbc;

import java.sql.SQLException;
import java.util.Properties;

public interface ConnectionPropertiesTransform {
    Properties transformProperties(Properties properties) throws SQLException;
}
