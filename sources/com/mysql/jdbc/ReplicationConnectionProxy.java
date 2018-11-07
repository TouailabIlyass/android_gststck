package com.mysql.jdbc;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;
import java.util.concurrent.Executor;

public class ReplicationConnectionProxy extends MultiHostConnectionProxy implements PingTarget {
    private static Class<?>[] INTERFACES_TO_PROXY;
    private static Constructor<?> JDBC_4_REPL_CONNECTION_CTOR;
    protected boolean allowMasterDownConnections = false;
    protected boolean allowSlaveDownConnections = false;
    ReplicationConnectionGroup connectionGroup;
    private long connectionGroupID = -1;
    private NonRegisteringDriver driver;
    protected boolean enableJMX = false;
    protected LoadBalancedConnection masterConnection;
    private List<String> masterHosts;
    private Properties masterProperties;
    protected boolean readFromMasterWhenNoSlaves = false;
    protected boolean readFromMasterWhenNoSlavesOriginal = false;
    protected boolean readOnly = false;
    private List<String> slaveHosts;
    private Properties slaveProperties;
    protected LoadBalancedConnection slavesConnection;
    private ReplicationConnection thisAsReplicationConnection = ((ReplicationConnection) this.thisAsConnection);

    static {
        if (Util.isJdbc4()) {
            try {
                JDBC_4_REPL_CONNECTION_CTOR = Class.forName("com.mysql.jdbc.JDBC4ReplicationMySQLConnection").getConstructor(new Class[]{ReplicationConnectionProxy.class});
                INTERFACES_TO_PROXY = new Class[]{ReplicationConnection.class, Class.forName("com.mysql.jdbc.JDBC4MySQLConnection")};
                return;
            } catch (SecurityException e) {
                throw new RuntimeException(e);
            } catch (NoSuchMethodException e2) {
                throw new RuntimeException(e2);
            } catch (ClassNotFoundException e3) {
                throw new RuntimeException(e3);
            }
        }
        INTERFACES_TO_PROXY = new Class[]{ReplicationConnection.class};
    }

    public static ReplicationConnection createProxyInstance(List<String> masterHostList, Properties masterProperties, List<String> slaveHostList, Properties slaveProperties) throws SQLException {
        return (ReplicationConnection) Proxy.newProxyInstance(ReplicationConnection.class.getClassLoader(), INTERFACES_TO_PROXY, new ReplicationConnectionProxy(masterHostList, masterProperties, slaveHostList, slaveProperties));
    }

    private ReplicationConnectionProxy(List<String> masterHostList, Properties masterProperties, List<String> slaveHostList, Properties slaveProperties) throws SQLException {
        try {
            this.enableJMX = Boolean.parseBoolean(masterProperties.getProperty("replicationEnableJMX", "false"));
            try {
                this.allowMasterDownConnections = Boolean.parseBoolean(masterProperties.getProperty("allowMasterDownConnections", "false"));
                try {
                    this.allowSlaveDownConnections = Boolean.parseBoolean(masterProperties.getProperty("allowSlaveDownConnections", "false"));
                    try {
                        this.readFromMasterWhenNoSlavesOriginal = Boolean.parseBoolean(masterProperties.getProperty("readFromMasterWhenNoSlaves"));
                        String group = masterProperties.getProperty("replicationConnectionGroup", null);
                        if (group != null) {
                            this.connectionGroup = ReplicationConnectionGroupManager.getConnectionGroupInstance(group);
                            if (this.enableJMX) {
                                ReplicationConnectionGroupManager.registerJmx();
                            }
                            this.connectionGroupID = this.connectionGroup.registerReplicationConnection(this.thisAsReplicationConnection, masterHostList, slaveHostList);
                            this.slaveHosts = new ArrayList(this.connectionGroup.getSlaveHosts());
                            this.masterHosts = new ArrayList(this.connectionGroup.getMasterHosts());
                        } else {
                            this.slaveHosts = new ArrayList(slaveHostList);
                            this.masterHosts = new ArrayList(masterHostList);
                        }
                        this.driver = new NonRegisteringDriver();
                        this.slaveProperties = slaveProperties;
                        this.masterProperties = masterProperties;
                        resetReadFromMasterWhenNoSlaves();
                        try {
                            initializeSlavesConnection();
                        } catch (SQLException e) {
                            if (!this.allowSlaveDownConnections) {
                                if (this.connectionGroup != null) {
                                    this.connectionGroup.handleCloseConnection(this.thisAsReplicationConnection);
                                }
                                throw e;
                            }
                        }
                        SQLException e2 = null;
                        try {
                            this.currentConnection = initializeMasterConnection();
                        } catch (SQLException e3) {
                            e2 = e3;
                        }
                        if (this.currentConnection != null) {
                            return;
                        }
                        if (!this.allowMasterDownConnections || this.slavesConnection == null) {
                            if (this.connectionGroup != null) {
                                this.connectionGroup.handleCloseConnection(this.thisAsReplicationConnection);
                            }
                            if (e2 != null) {
                                throw e2;
                            }
                            throw SQLError.createSQLException(Messages.getString("ReplicationConnectionProxy.initializationWithEmptyHostsLists"), SQLError.SQL_STATE_ILLEGAL_ARGUMENT, null);
                        }
                        this.readOnly = true;
                        this.currentConnection = this.slavesConnection;
                    } catch (Exception e4) {
                        throw SQLError.createSQLException(Messages.getString("ReplicationConnectionProxy.badValueForReadFromMasterWhenNoSlaves", new Object[]{readFromMasterWhenNoSlavesAsString}), SQLError.SQL_STATE_ILLEGAL_ARGUMENT, null);
                    }
                } catch (Exception e5) {
                    throw SQLError.createSQLException(Messages.getString("ReplicationConnectionProxy.badValueForAllowSlaveDownConnections", new Object[]{allowSlaveDownConnectionsAsString}), SQLError.SQL_STATE_ILLEGAL_ARGUMENT, null);
                }
            } catch (Exception e6) {
                throw SQLError.createSQLException(Messages.getString("ReplicationConnectionProxy.badValueForAllowMasterDownConnections", new Object[]{allowMasterDownConnectionsAsString}), SQLError.SQL_STATE_ILLEGAL_ARGUMENT, null);
            }
        } catch (Exception e7) {
            throw SQLError.createSQLException(Messages.getString("ReplicationConnectionProxy.badValueForReplicationEnableJMX", new Object[]{enableJMXAsString}), SQLError.SQL_STATE_ILLEGAL_ARGUMENT, null);
        }
    }

    MySQLConnection getNewWrapperForThisAsConnection() throws SQLException {
        if (!Util.isJdbc4()) {
            if (JDBC_4_REPL_CONNECTION_CTOR == null) {
                return new ReplicationMySQLConnection(this);
            }
        }
        return (MySQLConnection) Util.handleNewInstance(JDBC_4_REPL_CONNECTION_CTOR, new Object[]{this}, null);
    }

    protected void propagateProxyDown(MySQLConnection proxyConn) {
        if (this.masterConnection != null) {
            this.masterConnection.setProxy(proxyConn);
        }
        if (this.slavesConnection != null) {
            this.slavesConnection.setProxy(proxyConn);
        }
    }

    boolean shouldExceptionTriggerConnectionSwitch(Throwable t) {
        return false;
    }

    public boolean isMasterConnection() {
        return this.currentConnection != null && this.currentConnection == this.masterConnection;
    }

    public boolean isSlavesConnection() {
        return this.currentConnection != null && this.currentConnection == this.slavesConnection;
    }

    void pickNewConnection() throws SQLException {
    }

    void doClose() throws SQLException {
        if (this.masterConnection != null) {
            this.masterConnection.close();
        }
        if (this.slavesConnection != null) {
            this.slavesConnection.close();
        }
        if (this.connectionGroup != null) {
            this.connectionGroup.handleCloseConnection(this.thisAsReplicationConnection);
        }
    }

    void doAbortInternal() throws SQLException {
        this.masterConnection.abortInternal();
        this.slavesConnection.abortInternal();
        if (this.connectionGroup != null) {
            this.connectionGroup.handleCloseConnection(this.thisAsReplicationConnection);
        }
    }

    void doAbort(Executor executor) throws SQLException {
        this.masterConnection.abort(executor);
        this.slavesConnection.abort(executor);
        if (this.connectionGroup != null) {
            this.connectionGroup.handleCloseConnection(this.thisAsReplicationConnection);
        }
    }

    Object invokeMore(Object proxy, Method method, Object[] args) throws Throwable {
        checkConnectionCapabilityForMethod(method);
        boolean invokeAgain = false;
        while (true) {
            try {
                break;
            } catch (InvocationTargetException e) {
                if (invokeAgain) {
                    invokeAgain = false;
                } else if (e.getCause() != null && (e.getCause() instanceof SQLException) && ((SQLException) e.getCause()).getSQLState() == SQLError.SQL_STATE_INVALID_TRANSACTION_STATE && ((SQLException) e.getCause()).getErrorCode() == MysqlErrorNumbers.ERROR_CODE_NULL_LOAD_BALANCED_CONNECTION) {
                    try {
                        setReadOnly(this.readOnly);
                        invokeAgain = true;
                    } catch (SQLException e2) {
                    }
                }
                if (!invokeAgain) {
                    throw e;
                }
            }
        }
        Object result = method.invoke(this.thisAsConnection, args);
        if (result != null && (result instanceof Statement)) {
            ((Statement) result).setPingTarget(this);
        }
        return result;
    }

    private void checkConnectionCapabilityForMethod(Method method) throws Throwable {
        if (this.masterHosts.isEmpty() && this.slaveHosts.isEmpty() && !ReplicationConnection.class.isAssignableFrom(method.getDeclaringClass())) {
            throw SQLError.createSQLException(Messages.getString("ReplicationConnectionProxy.noHostsInconsistentState"), SQLError.SQL_STATE_INVALID_TRANSACTION_STATE, (int) MysqlErrorNumbers.ERROR_CODE_REPLICATION_CONNECTION_WITH_NO_HOSTS, true, null);
        }
    }

    public void doPing() throws SQLException {
        boolean isMasterConn = isMasterConnection();
        SQLException mastersPingException = null;
        SQLException slavesPingException = null;
        if (this.masterConnection != null) {
            try {
                this.masterConnection.ping();
            } catch (SQLException e) {
                mastersPingException = e;
            }
        } else {
            initializeMasterConnection();
        }
        if (this.slavesConnection != null) {
            try {
                this.slavesConnection.ping();
            } catch (SQLException e2) {
                slavesPingException = e2;
            }
        } else {
            try {
                initializeSlavesConnection();
                if (switchToSlavesConnectionIfNecessary()) {
                    isMasterConn = false;
                }
            } catch (SQLException e22) {
                if (this.masterConnection != null) {
                    if (!this.readFromMasterWhenNoSlaves) {
                    }
                }
                throw e22;
            }
        }
        if (isMasterConn && mastersPingException != null) {
            if (this.slavesConnection != null && slavesPingException == null) {
                this.masterConnection = null;
                this.currentConnection = this.slavesConnection;
                this.readOnly = true;
            }
            throw mastersPingException;
        } else if (!isMasterConn) {
            if (slavesPingException != null || this.slavesConnection == null) {
                if (this.masterConnection != null && this.readFromMasterWhenNoSlaves && mastersPingException == null) {
                    this.slavesConnection = null;
                    this.currentConnection = this.masterConnection;
                    this.readOnly = true;
                    this.currentConnection.setReadOnly(true);
                }
                if (slavesPingException != null) {
                    throw slavesPingException;
                }
            }
        }
    }

    private MySQLConnection initializeMasterConnection() throws SQLException {
        this.masterConnection = null;
        if (this.masterHosts.size() == 0) {
            return null;
        }
        LoadBalancedConnection newMasterConn = (LoadBalancedConnection) this.driver.connect(buildURL(this.masterHosts, this.masterProperties), this.masterProperties);
        newMasterConn.setProxy(getProxy());
        this.masterConnection = newMasterConn;
        return this.masterConnection;
    }

    private MySQLConnection initializeSlavesConnection() throws SQLException {
        this.slavesConnection = null;
        if (this.slaveHosts.size() == 0) {
            return null;
        }
        LoadBalancedConnection newSlavesConn = (LoadBalancedConnection) this.driver.connect(buildURL(this.slaveHosts, this.slaveProperties), this.slaveProperties);
        newSlavesConn.setProxy(getProxy());
        newSlavesConn.setReadOnly(true);
        this.slavesConnection = newSlavesConn;
        return this.slavesConnection;
    }

    private String buildURL(List<String> hosts, Properties props) {
        StringBuilder url = new StringBuilder(NonRegisteringDriver.LOADBALANCE_URL_PREFIX);
        boolean firstHost = true;
        for (String host : hosts) {
            if (!firstHost) {
                url.append(',');
            }
            url.append(host);
            firstHost = false;
        }
        url.append("/");
        String masterDb = props.getProperty(NonRegisteringDriver.DBNAME_PROPERTY_KEY);
        if (masterDb != null) {
            url.append(masterDb);
        }
        return url.toString();
    }

    private synchronized boolean switchToMasterConnection() throws SQLException {
        if (this.masterConnection == null || this.masterConnection.isClosed()) {
            try {
                if (initializeMasterConnection() == null) {
                    return false;
                }
            } catch (SQLException e) {
                this.currentConnection = null;
                throw e;
            }
        }
        if (!(isMasterConnection() || this.masterConnection == null)) {
            MultiHostConnectionProxy.syncSessionState(this.currentConnection, this.masterConnection, false);
            this.currentConnection = this.masterConnection;
        }
        return true;
    }

    /* JADX WARNING: inconsistent code. */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    private synchronized boolean switchToSlavesConnection() throws java.sql.SQLException {
        /*
        r3 = this;
        monitor-enter(r3);
        r0 = r3.slavesConnection;	 Catch:{ all -> 0x0034 }
        if (r0 == 0) goto L_0x000d;
    L_0x0005:
        r0 = r3.slavesConnection;	 Catch:{ all -> 0x0034 }
        r0 = r0.isClosed();	 Catch:{ all -> 0x0034 }
        if (r0 == 0) goto L_0x0017;
    L_0x000d:
        r0 = r3.initializeSlavesConnection();	 Catch:{ SQLException -> 0x002f }
        if (r0 != 0) goto L_0x0016;
    L_0x0013:
        r0 = 0;
        monitor-exit(r3);
        return r0;
    L_0x0017:
        r0 = r3.isSlavesConnection();	 Catch:{ all -> 0x0034 }
        r1 = 1;
        if (r0 != 0) goto L_0x002d;
    L_0x001e:
        r0 = r3.slavesConnection;	 Catch:{ all -> 0x0034 }
        if (r0 == 0) goto L_0x002d;
    L_0x0022:
        r0 = r3.currentConnection;	 Catch:{ all -> 0x0034 }
        r2 = r3.slavesConnection;	 Catch:{ all -> 0x0034 }
        com.mysql.jdbc.MultiHostConnectionProxy.syncSessionState(r0, r2, r1);	 Catch:{ all -> 0x0034 }
        r0 = r3.slavesConnection;	 Catch:{ all -> 0x0034 }
        r3.currentConnection = r0;	 Catch:{ all -> 0x0034 }
    L_0x002d:
        monitor-exit(r3);
        return r1;
    L_0x002f:
        r0 = move-exception;
        r1 = 0;
        r3.currentConnection = r1;	 Catch:{ all -> 0x0034 }
        throw r0;	 Catch:{ all -> 0x0034 }
    L_0x0034:
        r0 = move-exception;
        monitor-exit(r3);
        throw r0;
        */
        throw new UnsupportedOperationException("Method not decompiled: com.mysql.jdbc.ReplicationConnectionProxy.switchToSlavesConnection():boolean");
    }

    private boolean switchToSlavesConnectionIfNecessary() throws SQLException {
        if (!(this.currentConnection == null || (isMasterConnection() && (this.readOnly || (this.masterHosts.isEmpty() && this.currentConnection.isClosed()))))) {
            if (isMasterConnection() || !this.currentConnection.isClosed()) {
                return false;
            }
        }
        return switchToSlavesConnection();
    }

    public synchronized Connection getCurrentConnection() {
        return this.currentConnection == null ? LoadBalancedConnectionProxy.getNullLoadBalancedConnectionInstance() : this.currentConnection;
    }

    public long getConnectionGroupId() {
        return this.connectionGroupID;
    }

    public synchronized Connection getMasterConnection() {
        return this.masterConnection;
    }

    public synchronized void promoteSlaveToMaster(String hostPortPair) throws SQLException {
        this.masterHosts.add(hostPortPair);
        removeSlave(hostPortPair);
        if (this.masterConnection != null) {
            this.masterConnection.addHost(hostPortPair);
        }
        if (!(this.readOnly || isMasterConnection())) {
            switchToMasterConnection();
        }
    }

    public synchronized void removeMasterHost(String hostPortPair) throws SQLException {
        removeMasterHost(hostPortPair, true);
    }

    public synchronized void removeMasterHost(String hostPortPair, boolean waitUntilNotInUse) throws SQLException {
        removeMasterHost(hostPortPair, waitUntilNotInUse, false);
    }

    /* JADX WARNING: inconsistent code. */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    public synchronized void removeMasterHost(java.lang.String r3, boolean r4, boolean r5) throws java.sql.SQLException {
        /*
        r2 = this;
        monitor-enter(r2);
        if (r5 == 0) goto L_0x000e;
    L_0x0003:
        r0 = r2.slaveHosts;	 Catch:{ all -> 0x000c }
        r0.add(r3);	 Catch:{ all -> 0x000c }
        r2.resetReadFromMasterWhenNoSlaves();	 Catch:{ all -> 0x000c }
        goto L_0x000e;
    L_0x000c:
        r3 = move-exception;
        goto L_0x0046;
    L_0x000e:
        r0 = r2.masterHosts;	 Catch:{ all -> 0x000c }
        r0.remove(r3);	 Catch:{ all -> 0x000c }
        r0 = r2.masterConnection;	 Catch:{ all -> 0x000c }
        r1 = 0;
        if (r0 == 0) goto L_0x0042;
    L_0x0018:
        r0 = r2.masterConnection;	 Catch:{ all -> 0x000c }
        r0 = r0.isClosed();	 Catch:{ all -> 0x000c }
        if (r0 == 0) goto L_0x0021;
    L_0x0020:
        goto L_0x0042;
    L_0x0021:
        if (r4 == 0) goto L_0x0029;
    L_0x0023:
        r0 = r2.masterConnection;	 Catch:{ all -> 0x000c }
        r0.removeHostWhenNotInUse(r3);	 Catch:{ all -> 0x000c }
        goto L_0x002e;
    L_0x0029:
        r0 = r2.masterConnection;	 Catch:{ all -> 0x000c }
        r0.removeHost(r3);	 Catch:{ all -> 0x000c }
    L_0x002e:
        r0 = r2.masterHosts;	 Catch:{ all -> 0x000c }
        r0 = r0.isEmpty();	 Catch:{ all -> 0x000c }
        if (r0 == 0) goto L_0x0040;
    L_0x0036:
        r0 = r2.masterConnection;	 Catch:{ all -> 0x000c }
        r0.close();	 Catch:{ all -> 0x000c }
        r2.masterConnection = r1;	 Catch:{ all -> 0x000c }
        r2.switchToSlavesConnectionIfNecessary();	 Catch:{ all -> 0x000c }
    L_0x0040:
        monitor-exit(r2);
        return;
    L_0x0042:
        r2.masterConnection = r1;	 Catch:{ all -> 0x000c }
        monitor-exit(r2);
        return;
    L_0x0046:
        monitor-exit(r2);
        throw r3;
        */
        throw new UnsupportedOperationException("Method not decompiled: com.mysql.jdbc.ReplicationConnectionProxy.removeMasterHost(java.lang.String, boolean, boolean):void");
    }

    public boolean isHostMaster(String hostPortPair) {
        if (hostPortPair == null) {
            return false;
        }
        for (String masterHost : this.masterHosts) {
            if (masterHost.equalsIgnoreCase(hostPortPair)) {
                return true;
            }
        }
        return false;
    }

    public synchronized Connection getSlavesConnection() {
        return this.slavesConnection;
    }

    /* JADX WARNING: inconsistent code. */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    public synchronized void addSlaveHost(java.lang.String r2) throws java.sql.SQLException {
        /*
        r1 = this;
        monitor-enter(r1);
        r0 = r1.isHostSlave(r2);	 Catch:{ all -> 0x0023 }
        if (r0 == 0) goto L_0x0009;
    L_0x0007:
        monitor-exit(r1);
        return;
    L_0x0009:
        r0 = r1.slaveHosts;	 Catch:{ all -> 0x0023 }
        r0.add(r2);	 Catch:{ all -> 0x0023 }
        r1.resetReadFromMasterWhenNoSlaves();	 Catch:{ all -> 0x0023 }
        r0 = r1.slavesConnection;	 Catch:{ all -> 0x0023 }
        if (r0 != 0) goto L_0x001c;
    L_0x0015:
        r1.initializeSlavesConnection();	 Catch:{ all -> 0x0023 }
        r1.switchToSlavesConnectionIfNecessary();	 Catch:{ all -> 0x0023 }
        goto L_0x0021;
    L_0x001c:
        r0 = r1.slavesConnection;	 Catch:{ all -> 0x0023 }
        r0.addHost(r2);	 Catch:{ all -> 0x0023 }
    L_0x0021:
        monitor-exit(r1);
        return;
    L_0x0023:
        r2 = move-exception;
        monitor-exit(r1);
        throw r2;
        */
        throw new UnsupportedOperationException("Method not decompiled: com.mysql.jdbc.ReplicationConnectionProxy.addSlaveHost(java.lang.String):void");
    }

    public synchronized void removeSlave(String hostPortPair) throws SQLException {
        removeSlave(hostPortPair, true);
    }

    /* JADX WARNING: inconsistent code. */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    public synchronized void removeSlave(java.lang.String r3, boolean r4) throws java.sql.SQLException {
        /*
        r2 = this;
        monitor-enter(r2);
        r0 = r2.slaveHosts;	 Catch:{ all -> 0x0049 }
        r0.remove(r3);	 Catch:{ all -> 0x0049 }
        r2.resetReadFromMasterWhenNoSlaves();	 Catch:{ all -> 0x0049 }
        r0 = r2.slavesConnection;	 Catch:{ all -> 0x0049 }
        r1 = 0;
        if (r0 == 0) goto L_0x0045;
    L_0x000e:
        r0 = r2.slavesConnection;	 Catch:{ all -> 0x0049 }
        r0 = r0.isClosed();	 Catch:{ all -> 0x0049 }
        if (r0 == 0) goto L_0x0017;
    L_0x0016:
        goto L_0x0045;
    L_0x0017:
        if (r4 == 0) goto L_0x001f;
    L_0x0019:
        r0 = r2.slavesConnection;	 Catch:{ all -> 0x0049 }
        r0.removeHostWhenNotInUse(r3);	 Catch:{ all -> 0x0049 }
        goto L_0x0024;
    L_0x001f:
        r0 = r2.slavesConnection;	 Catch:{ all -> 0x0049 }
        r0.removeHost(r3);	 Catch:{ all -> 0x0049 }
    L_0x0024:
        r0 = r2.slaveHosts;	 Catch:{ all -> 0x0049 }
        r0 = r0.isEmpty();	 Catch:{ all -> 0x0049 }
        if (r0 == 0) goto L_0x0043;
    L_0x002c:
        r0 = r2.slavesConnection;	 Catch:{ all -> 0x0049 }
        r0.close();	 Catch:{ all -> 0x0049 }
        r2.slavesConnection = r1;	 Catch:{ all -> 0x0049 }
        r2.switchToMasterConnection();	 Catch:{ all -> 0x0049 }
        r0 = r2.isMasterConnection();	 Catch:{ all -> 0x0049 }
        if (r0 == 0) goto L_0x0043;
    L_0x003c:
        r0 = r2.currentConnection;	 Catch:{ all -> 0x0049 }
        r1 = r2.readOnly;	 Catch:{ all -> 0x0049 }
        r0.setReadOnly(r1);	 Catch:{ all -> 0x0049 }
    L_0x0043:
        monitor-exit(r2);
        return;
    L_0x0045:
        r2.slavesConnection = r1;	 Catch:{ all -> 0x0049 }
        monitor-exit(r2);
        return;
    L_0x0049:
        r3 = move-exception;
        monitor-exit(r2);
        throw r3;
        */
        throw new UnsupportedOperationException("Method not decompiled: com.mysql.jdbc.ReplicationConnectionProxy.removeSlave(java.lang.String, boolean):void");
    }

    public boolean isHostSlave(String hostPortPair) {
        if (hostPortPair == null) {
            return false;
        }
        for (String test : this.slaveHosts) {
            if (test.equalsIgnoreCase(hostPortPair)) {
                return true;
            }
        }
        return false;
    }

    public synchronized void setReadOnly(boolean readOnly) throws SQLException {
        SQLException sQLException = null;
        boolean switched;
        if (readOnly) {
            if (!isSlavesConnection() || this.currentConnection.isClosed()) {
                try {
                    switched = switchToSlavesConnection();
                } catch (SQLException e) {
                    switched = false;
                    sQLException = e;
                }
                if (!switched) {
                    if (this.readFromMasterWhenNoSlaves && switchToMasterConnection()) {
                        sQLException = null;
                    }
                }
                if (sQLException != null) {
                    throw sQLException;
                }
            }
        } else if (!isMasterConnection() || this.currentConnection.isClosed()) {
            try {
                switched = switchToMasterConnection();
            } catch (SQLException e2) {
                switched = false;
                sQLException = e2;
            }
            if (!switched) {
                if (switchToSlavesConnectionIfNecessary()) {
                    sQLException = null;
                }
            }
            if (sQLException != null) {
                throw sQLException;
            }
        }
        this.readOnly = readOnly;
        if (this.readFromMasterWhenNoSlaves && isMasterConnection()) {
            this.currentConnection.setReadOnly(this.readOnly);
        }
    }

    public boolean isReadOnly() throws SQLException {
        if (isMasterConnection()) {
            if (!this.readOnly) {
                return false;
            }
        }
        return true;
    }

    private void resetReadFromMasterWhenNoSlaves() {
        boolean z;
        if (!this.slaveHosts.isEmpty()) {
            if (!this.readFromMasterWhenNoSlavesOriginal) {
                z = false;
                this.readFromMasterWhenNoSlaves = z;
            }
        }
        z = true;
        this.readFromMasterWhenNoSlaves = z;
    }
}
