package com.mysql.jdbc.jdbc2.optional;

import com.mysql.jdbc.Connection;
import java.sql.SQLException;
import javax.sql.ConnectionPoolDataSource;
import javax.sql.PooledConnection;

public class MysqlConnectionPoolDataSource extends MysqlDataSource implements ConnectionPoolDataSource {
    static final long serialVersionUID = -7767325445592304961L;

    public synchronized PooledConnection getPooledConnection() throws SQLException {
        return MysqlPooledConnection.getInstance((Connection) getConnection());
    }

    public synchronized PooledConnection getPooledConnection(String s, String s1) throws SQLException {
        return MysqlPooledConnection.getInstance((Connection) getConnection(s, s1));
    }
}
