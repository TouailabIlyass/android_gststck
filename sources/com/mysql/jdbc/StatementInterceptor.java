package com.mysql.jdbc;

import java.sql.SQLException;
import java.util.Properties;

public interface StatementInterceptor extends Extension {
    void destroy();

    boolean executeTopLevelOnly();

    void init(Connection connection, Properties properties) throws SQLException;

    ResultSetInternalMethods postProcess(String str, Statement statement, ResultSetInternalMethods resultSetInternalMethods, Connection connection) throws SQLException;

    ResultSetInternalMethods preProcess(String str, Statement statement, Connection connection) throws SQLException;
}
