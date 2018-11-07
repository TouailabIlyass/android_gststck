package com.mysql.jdbc;

import java.sql.Driver;
import java.sql.DriverManager;
import java.sql.SQLException;

public class ReplicationDriver extends NonRegisteringReplicationDriver implements Driver {
    static {
        try {
            DriverManager.registerDriver(new NonRegisteringReplicationDriver());
        } catch (SQLException e) {
            throw new RuntimeException("Can't register driver!");
        }
    }
}
