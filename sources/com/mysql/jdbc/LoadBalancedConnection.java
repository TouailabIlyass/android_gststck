package com.mysql.jdbc;

import java.sql.SQLException;

public interface LoadBalancedConnection extends MySQLConnection {
    boolean addHost(String str) throws SQLException;

    void ping(boolean z) throws SQLException;

    void removeHost(String str) throws SQLException;

    void removeHostWhenNotInUse(String str) throws SQLException;
}
