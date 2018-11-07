package com.mysql.jdbc.jdbc2.optional;

import com.mysql.jdbc.ExceptionInterceptor;
import com.mysql.jdbc.SQLError;
import com.mysql.jdbc.Util;
import java.lang.reflect.Constructor;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;
import javax.sql.ConnectionEvent;
import javax.sql.ConnectionEventListener;
import javax.sql.PooledConnection;

public class MysqlPooledConnection implements PooledConnection {
    public static final int CONNECTION_CLOSED_EVENT = 2;
    public static final int CONNECTION_ERROR_EVENT = 1;
    private static final Constructor<?> JDBC_4_POOLED_CONNECTION_WRAPPER_CTOR;
    private Map<ConnectionEventListener, ConnectionEventListener> connectionEventListeners;
    private ExceptionInterceptor exceptionInterceptor;
    private Connection logicalHandle = null;
    private com.mysql.jdbc.Connection physicalConn;

    static {
        if (Util.isJdbc4()) {
            try {
                JDBC_4_POOLED_CONNECTION_WRAPPER_CTOR = Class.forName("com.mysql.jdbc.jdbc2.optional.JDBC4MysqlPooledConnection").getConstructor(new Class[]{com.mysql.jdbc.Connection.class});
                return;
            } catch (SecurityException e) {
                throw new RuntimeException(e);
            } catch (NoSuchMethodException e2) {
                throw new RuntimeException(e2);
            } catch (ClassNotFoundException e3) {
                throw new RuntimeException(e3);
            }
        }
        JDBC_4_POOLED_CONNECTION_WRAPPER_CTOR = null;
    }

    protected static MysqlPooledConnection getInstance(com.mysql.jdbc.Connection connection) throws SQLException {
        if (!Util.isJdbc4()) {
            return new MysqlPooledConnection(connection);
        }
        return (MysqlPooledConnection) Util.handleNewInstance(JDBC_4_POOLED_CONNECTION_WRAPPER_CTOR, new Object[]{connection}, connection.getExceptionInterceptor());
    }

    public MysqlPooledConnection(com.mysql.jdbc.Connection connection) {
        this.physicalConn = connection;
        this.connectionEventListeners = new HashMap();
        this.exceptionInterceptor = this.physicalConn.getExceptionInterceptor();
    }

    public synchronized void addConnectionEventListener(ConnectionEventListener connectioneventlistener) {
        if (this.connectionEventListeners != null) {
            this.connectionEventListeners.put(connectioneventlistener, connectioneventlistener);
        }
    }

    public synchronized void removeConnectionEventListener(ConnectionEventListener connectioneventlistener) {
        if (this.connectionEventListeners != null) {
            this.connectionEventListeners.remove(connectioneventlistener);
        }
    }

    public synchronized Connection getConnection() throws SQLException {
        return getConnection(true, false);
    }

    protected synchronized Connection getConnection(boolean resetServerState, boolean forXa) throws SQLException {
        if (this.physicalConn == null) {
            SQLException sqlException = SQLError.createSQLException("Physical Connection doesn't exist", this.exceptionInterceptor);
            callConnectionEventListeners(1, sqlException);
            throw sqlException;
        }
        try {
            if (this.logicalHandle != null) {
                ((ConnectionWrapper) this.logicalHandle).close(false);
            }
            if (resetServerState) {
                this.physicalConn.resetServerState();
            }
            this.logicalHandle = ConnectionWrapper.getInstance(this, this.physicalConn, forXa);
        } catch (SQLException sqlException2) {
            callConnectionEventListeners(1, sqlException2);
            throw sqlException2;
        }
        return this.logicalHandle;
    }

    public synchronized void close() throws SQLException {
        if (this.physicalConn != null) {
            this.physicalConn.close();
            this.physicalConn = null;
        }
        if (this.connectionEventListeners != null) {
            this.connectionEventListeners.clear();
            this.connectionEventListeners = null;
        }
    }

    protected synchronized void callConnectionEventListeners(int eventType, SQLException sqlException) {
        if (this.connectionEventListeners != null) {
            ConnectionEvent connectionevent = new ConnectionEvent(this, sqlException);
            for (Entry value : this.connectionEventListeners.entrySet()) {
                ConnectionEventListener connectioneventlistener = (ConnectionEventListener) value.getValue();
                if (eventType == 2) {
                    connectioneventlistener.connectionClosed(connectionevent);
                } else if (eventType == 1) {
                    connectioneventlistener.connectionErrorOccurred(connectionevent);
                }
            }
        }
    }

    protected ExceptionInterceptor getExceptionInterceptor() {
        return this.exceptionInterceptor;
    }
}
