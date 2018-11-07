package com.mysql.jdbc.jdbc2.optional;

import com.mysql.jdbc.Connection;
import com.mysql.jdbc.Util;
import java.lang.reflect.Constructor;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.Map;
import javax.sql.XAConnection;
import javax.transaction.xa.XAException;
import javax.transaction.xa.XAResource;
import javax.transaction.xa.Xid;

public class SuspendableXAConnection extends MysqlPooledConnection implements XAConnection, XAResource {
    private static final Constructor<?> JDBC_4_XA_CONNECTION_WRAPPER_CTOR;
    private static final Map<Xid, XAConnection> XIDS_TO_PHYSICAL_CONNECTIONS = new HashMap();
    private XAConnection currentXAConnection;
    private XAResource currentXAResource;
    private Xid currentXid;
    private Connection underlyingConnection;

    static {
        if (Util.isJdbc4()) {
            try {
                JDBC_4_XA_CONNECTION_WRAPPER_CTOR = Class.forName("com.mysql.jdbc.jdbc2.optional.JDBC4SuspendableXAConnection").getConstructor(new Class[]{Connection.class});
            } catch (SecurityException e) {
                throw new RuntimeException(e);
            } catch (NoSuchMethodException e2) {
                throw new RuntimeException(e2);
            } catch (ClassNotFoundException e3) {
                throw new RuntimeException(e3);
            }
        }
        JDBC_4_XA_CONNECTION_WRAPPER_CTOR = null;
    }

    protected static SuspendableXAConnection getInstance(Connection mysqlConnection) throws SQLException {
        if (!Util.isJdbc4()) {
            return new SuspendableXAConnection(mysqlConnection);
        }
        return (SuspendableXAConnection) Util.handleNewInstance(JDBC_4_XA_CONNECTION_WRAPPER_CTOR, new Object[]{mysqlConnection}, mysqlConnection.getExceptionInterceptor());
    }

    public SuspendableXAConnection(Connection connection) {
        super(connection);
        this.underlyingConnection = connection;
    }

    private static synchronized XAConnection findConnectionForXid(Connection connectionToWrap, Xid xid) throws SQLException {
        XAConnection conn;
        synchronized (SuspendableXAConnection.class) {
            conn = (XAConnection) XIDS_TO_PHYSICAL_CONNECTIONS.get(xid);
            if (conn == null) {
                conn = new MysqlXAConnection(connectionToWrap, connectionToWrap.getLogXaCommands());
                XIDS_TO_PHYSICAL_CONNECTIONS.put(xid, conn);
            }
        }
        return conn;
    }

    private static synchronized void removeXAConnectionMapping(Xid xid) {
        synchronized (SuspendableXAConnection.class) {
            XIDS_TO_PHYSICAL_CONNECTIONS.remove(xid);
        }
    }

    private synchronized void switchToXid(Xid xid) throws XAException {
        if (xid == null) {
            throw new XAException();
        }
        try {
            if (!xid.equals(this.currentXid)) {
                XAConnection toSwitchTo = findConnectionForXid(this.underlyingConnection, xid);
                this.currentXAConnection = toSwitchTo;
                this.currentXid = xid;
                this.currentXAResource = toSwitchTo.getXAResource();
            }
        } catch (SQLException e) {
            throw new XAException();
        }
    }

    public XAResource getXAResource() throws SQLException {
        return this;
    }

    public void commit(Xid xid, boolean arg1) throws XAException {
        switchToXid(xid);
        this.currentXAResource.commit(xid, arg1);
        removeXAConnectionMapping(xid);
    }

    public void end(Xid xid, int arg1) throws XAException {
        switchToXid(xid);
        this.currentXAResource.end(xid, arg1);
    }

    public void forget(Xid xid) throws XAException {
        switchToXid(xid);
        this.currentXAResource.forget(xid);
        removeXAConnectionMapping(xid);
    }

    public int getTransactionTimeout() throws XAException {
        return 0;
    }

    public boolean isSameRM(XAResource xaRes) throws XAException {
        return xaRes == this;
    }

    public int prepare(Xid xid) throws XAException {
        switchToXid(xid);
        return this.currentXAResource.prepare(xid);
    }

    public Xid[] recover(int flag) throws XAException {
        return MysqlXAConnection.recover(this.underlyingConnection, flag);
    }

    public void rollback(Xid xid) throws XAException {
        switchToXid(xid);
        this.currentXAResource.rollback(xid);
        removeXAConnectionMapping(xid);
    }

    public boolean setTransactionTimeout(int arg0) throws XAException {
        return false;
    }

    public void start(Xid xid, int arg1) throws XAException {
        switchToXid(xid);
        if (arg1 != 2097152) {
            this.currentXAResource.start(xid, arg1);
        } else {
            this.currentXAResource.start(xid, 134217728);
        }
    }

    public synchronized java.sql.Connection getConnection() throws SQLException {
        if (this.currentXAConnection == null) {
            return getConnection(false, true);
        }
        return this.currentXAConnection.getConnection();
    }

    public void close() throws SQLException {
        if (this.currentXAConnection == null) {
            super.close();
            return;
        }
        removeXAConnectionMapping(this.currentXid);
        this.currentXAConnection.close();
    }
}
