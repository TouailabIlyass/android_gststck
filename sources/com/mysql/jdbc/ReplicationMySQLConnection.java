package com.mysql.jdbc;

import java.sql.SQLException;
import java.util.Properties;
import java.util.concurrent.Executor;

public class ReplicationMySQLConnection extends MultiHostMySQLConnection implements ReplicationConnection {
    public ReplicationMySQLConnection(MultiHostConnectionProxy proxy) {
        super(proxy);
    }

    protected ReplicationConnectionProxy getThisAsProxy() {
        return (ReplicationConnectionProxy) super.getThisAsProxy();
    }

    public MySQLConnection getActiveMySQLConnection() {
        return (MySQLConnection) getCurrentConnection();
    }

    public synchronized Connection getCurrentConnection() {
        return getThisAsProxy().getCurrentConnection();
    }

    public long getConnectionGroupId() {
        return getThisAsProxy().getConnectionGroupId();
    }

    public synchronized Connection getMasterConnection() {
        return getThisAsProxy().getMasterConnection();
    }

    private Connection getValidatedMasterConnection() {
        Connection conn = getThisAsProxy().masterConnection;
        Connection connection = null;
        if (conn != null) {
            try {
                if (!conn.isClosed()) {
                    connection = conn;
                }
            } catch (SQLException e) {
                return null;
            }
        }
        return connection;
    }

    public void promoteSlaveToMaster(String host) throws SQLException {
        getThisAsProxy().promoteSlaveToMaster(host);
    }

    public void removeMasterHost(String host) throws SQLException {
        getThisAsProxy().removeMasterHost(host);
    }

    public void removeMasterHost(String host, boolean waitUntilNotInUse) throws SQLException {
        getThisAsProxy().removeMasterHost(host, waitUntilNotInUse);
    }

    public boolean isHostMaster(String host) {
        return getThisAsProxy().isHostMaster(host);
    }

    public synchronized Connection getSlavesConnection() {
        return getThisAsProxy().getSlavesConnection();
    }

    private Connection getValidatedSlavesConnection() {
        Connection conn = getThisAsProxy().slavesConnection;
        Connection connection = null;
        if (conn != null) {
            try {
                if (!conn.isClosed()) {
                    connection = conn;
                }
            } catch (SQLException e) {
                return null;
            }
        }
        return connection;
    }

    public void addSlaveHost(String host) throws SQLException {
        getThisAsProxy().addSlaveHost(host);
    }

    public void removeSlave(String host) throws SQLException {
        getThisAsProxy().removeSlave(host);
    }

    public void removeSlave(String host, boolean closeGently) throws SQLException {
        getThisAsProxy().removeSlave(host, closeGently);
    }

    public boolean isHostSlave(String host) {
        return getThisAsProxy().isHostSlave(host);
    }

    public void setReadOnly(boolean readOnlyFlag) throws SQLException {
        getThisAsProxy().setReadOnly(readOnlyFlag);
    }

    public boolean isReadOnly() throws SQLException {
        return getThisAsProxy().isReadOnly();
    }

    public synchronized void ping() throws SQLException {
        Connection validatedMasterConnection;
        SQLException e;
        Connection conn;
        try {
            validatedMasterConnection = getValidatedMasterConnection();
            conn = validatedMasterConnection;
            if (validatedMasterConnection != null) {
                try {
                    conn.ping();
                } catch (SQLException e2) {
                    e = e2;
                    if (isMasterConnection()) {
                        throw e;
                    }
                    validatedMasterConnection = getValidatedSlavesConnection();
                    conn = validatedMasterConnection;
                    if (validatedMasterConnection != null) {
                        conn.ping();
                    }
                }
            }
        } catch (SQLException e3) {
            e = e3;
            if (isMasterConnection()) {
                throw e;
            }
            validatedMasterConnection = getValidatedSlavesConnection();
            conn = validatedMasterConnection;
            if (validatedMasterConnection != null) {
                conn.ping();
            }
        }
        try {
            validatedMasterConnection = getValidatedSlavesConnection();
            conn = validatedMasterConnection;
            if (validatedMasterConnection != null) {
                conn.ping();
            }
        } catch (SQLException e4) {
            if (!isMasterConnection()) {
                throw e4;
            }
        }
    }

    public synchronized void changeUser(String userName, String newPassword) throws SQLException {
        Connection validatedMasterConnection = getValidatedMasterConnection();
        Connection conn = validatedMasterConnection;
        if (validatedMasterConnection != null) {
            conn.changeUser(userName, newPassword);
        }
        validatedMasterConnection = getValidatedSlavesConnection();
        conn = validatedMasterConnection;
        if (validatedMasterConnection != null) {
            conn.changeUser(userName, newPassword);
        }
    }

    public synchronized void setStatementComment(String comment) {
        Connection validatedMasterConnection = getValidatedMasterConnection();
        Connection conn = validatedMasterConnection;
        if (validatedMasterConnection != null) {
            conn.setStatementComment(comment);
        }
        validatedMasterConnection = getValidatedSlavesConnection();
        conn = validatedMasterConnection;
        if (validatedMasterConnection != null) {
            conn.setStatementComment(comment);
        }
    }

    public boolean hasSameProperties(Connection c) {
        Connection connM = getValidatedMasterConnection();
        Connection connS = getValidatedSlavesConnection();
        boolean z = false;
        if (connM == null && connS == null) {
            return false;
        }
        if (connM == null || connM.hasSameProperties(c)) {
            if (connS != null) {
                if (connS.hasSameProperties(c)) {
                }
            }
            z = true;
            return z;
        }
        return z;
    }

    public Properties getProperties() {
        Properties props = new Properties();
        Connection validatedMasterConnection = getValidatedMasterConnection();
        Connection conn = validatedMasterConnection;
        if (validatedMasterConnection != null) {
            props.putAll(conn.getProperties());
        }
        validatedMasterConnection = getValidatedSlavesConnection();
        conn = validatedMasterConnection;
        if (validatedMasterConnection != null) {
            props.putAll(conn.getProperties());
        }
        return props;
    }

    public void abort(Executor executor) throws SQLException {
        getThisAsProxy().doAbort(executor);
    }

    public void abortInternal() throws SQLException {
        getThisAsProxy().doAbortInternal();
    }

    public boolean getAllowMasterDownConnections() {
        return getThisAsProxy().allowMasterDownConnections;
    }

    public void setAllowMasterDownConnections(boolean connectIfMasterDown) {
        getThisAsProxy().allowMasterDownConnections = connectIfMasterDown;
    }

    public boolean getReplicationEnableJMX() {
        return getThisAsProxy().enableJMX;
    }

    public void setReplicationEnableJMX(boolean replicationEnableJMX) {
        getThisAsProxy().enableJMX = replicationEnableJMX;
    }

    public void setProxy(MySQLConnection proxy) {
        getThisAsProxy().setProxy(proxy);
    }
}
