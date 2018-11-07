package com.mysql.jdbc;

import java.sql.Connection;
import java.sql.SQLClientInfoException;
import java.sql.SQLException;
import java.util.Properties;

public interface JDBC4ClientInfoProvider {
    void destroy() throws SQLException;

    String getClientInfo(Connection connection, String str) throws SQLException;

    Properties getClientInfo(Connection connection) throws SQLException;

    void initialize(Connection connection, Properties properties) throws SQLException;

    void setClientInfo(Connection connection, String str, String str2) throws SQLClientInfoException;

    void setClientInfo(Connection connection, Properties properties) throws SQLClientInfoException;
}
