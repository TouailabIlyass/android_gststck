package com.mysql.jdbc.integration.c3p0;

import com.mchange.v2.c3p0.C3P0ProxyConnection;
import com.mchange.v2.c3p0.QueryConnectionTester;
import com.mysql.jdbc.CommunicationsException;
import com.mysql.jdbc.Connection;
import java.lang.reflect.Method;
import java.sql.SQLException;
import java.sql.Statement;

public final class MysqlConnectionTester implements QueryConnectionTester {
    private static final Object[] NO_ARGS_ARRAY = new Object[0];
    private static final long serialVersionUID = 3256444690067896368L;
    private transient Method pingMethod;

    public MysqlConnectionTester() {
        try {
            this.pingMethod = Connection.class.getMethod("ping", (Class[]) null);
        } catch (Exception e) {
        }
    }

    public int activeCheckConnection(java.sql.Connection con) {
        Statement pingStatement;
        try {
            if (this.pingMethod == null) {
                pingStatement = null;
                pingStatement = con.createStatement();
                pingStatement.executeQuery("SELECT 1").close();
                if (pingStatement != null) {
                    pingStatement.close();
                }
            } else if (con instanceof Connection) {
                ((Connection) con).ping();
            } else {
                ((C3P0ProxyConnection) con).rawConnectionOperation(this.pingMethod, C3P0ProxyConnection.RAW_CONNECTION, NO_ARGS_ARRAY);
            }
            return 0;
        } catch (Exception e) {
            return -1;
        } catch (Throwable th) {
            if (pingStatement != null) {
                pingStatement.close();
            }
        }
    }

    public int statusOnException(java.sql.Connection arg0, Throwable throwable) {
        if (!(throwable instanceof CommunicationsException)) {
            if (!"com.mysql.jdbc.exceptions.jdbc4.CommunicationsException".equals(throwable.getClass().getName())) {
                if (!(throwable instanceof SQLException)) {
                    return -1;
                }
                String sqlState = ((SQLException) throwable).getSQLState();
                if (sqlState == null || !sqlState.startsWith("08")) {
                    return 0;
                }
                return -1;
            }
        }
        return -1;
    }

    public int activeCheckConnection(java.sql.Connection arg0, String arg1) {
        return 0;
    }
}
