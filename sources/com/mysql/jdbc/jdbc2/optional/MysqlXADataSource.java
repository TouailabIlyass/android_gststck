package com.mysql.jdbc.jdbc2.optional;

import java.sql.Connection;
import java.sql.SQLException;
import javax.sql.XAConnection;
import javax.sql.XADataSource;

public class MysqlXADataSource extends MysqlDataSource implements XADataSource {
    static final long serialVersionUID = 7911390333152247455L;

    public XAConnection getXAConnection() throws SQLException {
        return wrapConnection(getConnection());
    }

    public XAConnection getXAConnection(String u, String p) throws SQLException {
        return wrapConnection(getConnection(u, p));
    }

    private XAConnection wrapConnection(Connection conn) throws SQLException {
        if (!getPinGlobalTxToPhysicalConnection()) {
            if (!((com.mysql.jdbc.Connection) conn).getPinGlobalTxToPhysicalConnection()) {
                return MysqlXAConnection.getInstance((com.mysql.jdbc.Connection) conn, getLogXaCommands());
            }
        }
        return SuspendableXAConnection.getInstance((com.mysql.jdbc.Connection) conn);
    }
}
