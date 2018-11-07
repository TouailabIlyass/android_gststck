package com.mysql.jdbc.jdbc2.optional;

import com.mysql.jdbc.SQLError;
import java.lang.reflect.Proxy;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.HashMap;

public class JDBC4StatementWrapper extends StatementWrapper {
    public JDBC4StatementWrapper(ConnectionWrapper c, MysqlPooledConnection conn, Statement toWrap) {
        super(c, conn, toWrap);
    }

    public void close() throws SQLException {
        try {
            super.close();
        } finally {
            this.unwrappedInterfaces = null;
        }
    }

    public boolean isClosed() throws SQLException {
        try {
            if (this.wrappedStmt != null) {
                return this.wrappedStmt.isClosed();
            }
            throw SQLError.createSQLException("Statement already closed", SQLError.SQL_STATE_ILLEGAL_ARGUMENT, this.exceptionInterceptor);
        } catch (SQLException sqlEx) {
            checkAndFireConnectionError(sqlEx);
            return false;
        }
    }

    public void setPoolable(boolean poolable) throws SQLException {
        try {
            if (this.wrappedStmt != null) {
                this.wrappedStmt.setPoolable(poolable);
                return;
            }
            throw SQLError.createSQLException("Statement already closed", SQLError.SQL_STATE_ILLEGAL_ARGUMENT, this.exceptionInterceptor);
        } catch (SQLException sqlEx) {
            checkAndFireConnectionError(sqlEx);
        }
    }

    public boolean isPoolable() throws SQLException {
        try {
            if (this.wrappedStmt != null) {
                return this.wrappedStmt.isPoolable();
            }
            throw SQLError.createSQLException("Statement already closed", SQLError.SQL_STATE_ILLEGAL_ARGUMENT, this.exceptionInterceptor);
        } catch (SQLException sqlEx) {
            checkAndFireConnectionError(sqlEx);
            return false;
        }
    }

    public boolean isWrapperFor(Class<?> iface) throws SQLException {
        boolean z = true;
        if (iface.isInstance(this)) {
            return true;
        }
        String interfaceClassName = iface.getName();
        if (!(interfaceClassName.equals("com.mysql.jdbc.Statement") || interfaceClassName.equals("java.sql.Statement"))) {
            if (!interfaceClassName.equals("java.sql.Wrapper")) {
                z = false;
            }
        }
        return z;
    }

    public synchronized <T> T unwrap(Class<T> iface) throws SQLException {
        try {
            if (!"java.sql.Statement".equals(iface.getName())) {
                if (!"java.sql.Wrapper.class".equals(iface.getName())) {
                    if (this.unwrappedInterfaces == null) {
                        this.unwrappedInterfaces = new HashMap();
                    }
                    Object cachedUnwrapped = this.unwrappedInterfaces.get(iface);
                    if (cachedUnwrapped == null) {
                        cachedUnwrapped = Proxy.newProxyInstance(this.wrappedStmt.getClass().getClassLoader(), new Class[]{iface}, new ConnectionErrorFiringInvocationHandler(this.wrappedStmt));
                        this.unwrappedInterfaces.put(iface, cachedUnwrapped);
                    }
                    return iface.cast(cachedUnwrapped);
                }
            }
            return iface.cast(this);
        } catch (ClassCastException e) {
            StringBuilder stringBuilder = new StringBuilder();
            stringBuilder.append("Unable to unwrap to ");
            stringBuilder.append(iface.toString());
            throw SQLError.createSQLException(stringBuilder.toString(), SQLError.SQL_STATE_ILLEGAL_ARGUMENT, this.exceptionInterceptor);
        }
    }
}
