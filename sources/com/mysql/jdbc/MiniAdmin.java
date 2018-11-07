package com.mysql.jdbc;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.Properties;

public class MiniAdmin {
    private Connection conn;

    public MiniAdmin(Connection conn) throws SQLException {
        if (conn == null) {
            throw SQLError.createSQLException(Messages.getString("MiniAdmin.0"), SQLError.SQL_STATE_GENERAL_ERROR, null);
        } else if (conn instanceof Connection) {
            this.conn = (Connection) conn;
        } else {
            throw SQLError.createSQLException(Messages.getString("MiniAdmin.1"), SQLError.SQL_STATE_GENERAL_ERROR, ((ConnectionImpl) conn).getExceptionInterceptor());
        }
    }

    public MiniAdmin(String jdbcUrl) throws SQLException {
        this(jdbcUrl, new Properties());
    }

    public MiniAdmin(String jdbcUrl, Properties props) throws SQLException {
        this.conn = (Connection) new Driver().connect(jdbcUrl, props);
    }

    public void shutdown() throws SQLException {
        this.conn.shutdownServer();
    }
}
