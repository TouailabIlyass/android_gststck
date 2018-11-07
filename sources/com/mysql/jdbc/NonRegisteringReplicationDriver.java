package com.mysql.jdbc;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.Properties;

public class NonRegisteringReplicationDriver extends NonRegisteringDriver {
    public Connection connect(String url, Properties info) throws SQLException {
        return connectReplicationConnection(url, info);
    }
}
