package com.mysql.jdbc;

import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.sql.SQLException;
import java.util.List;
import java.util.Properties;
import java.util.concurrent.Executor;

public class FailoverConnectionProxy extends MultiHostConnectionProxy {
    private static final int DEFAULT_PRIMARY_HOST_INDEX = 0;
    private static Class<?>[] INTERFACES_TO_PROXY = null;
    private static final String METHOD_COMMIT = "commit";
    private static final String METHOD_ROLLBACK = "rollback";
    private static final String METHOD_SET_AUTO_COMMIT = "setAutoCommit";
    private static final String METHOD_SET_READ_ONLY = "setReadOnly";
    private static final int NO_CONNECTION_INDEX = -1;
    private int currentHostIndex = -1;
    private boolean enableFallBackToPrimaryHost;
    private boolean explicitlyAutoCommit;
    private Boolean explicitlyReadOnly;
    private boolean failoverReadOnly;
    private long primaryHostFailTimeMillis;
    private int primaryHostIndex;
    private long queriesBeforeRetryPrimaryHost;
    private long queriesIssuedSinceFailover;
    private int retriesAllDown;
    private int secondsBeforeRetryPrimaryHost;

    class FailoverJdbcInterfaceProxy extends JdbcInterfaceProxy {
        FailoverJdbcInterfaceProxy(Object toInvokeOn) {
            super(toInvokeOn);
        }

        public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
            boolean isExecute = method.getName().startsWith("execute");
            if (FailoverConnectionProxy.this.connectedToSecondaryHost() && isExecute) {
                FailoverConnectionProxy.this.incrementQueriesIssuedSinceFailover();
            }
            Object result = super.invoke(proxy, method, args);
            if (FailoverConnectionProxy.this.explicitlyAutoCommit && isExecute && FailoverConnectionProxy.this.readyToFallBackToPrimaryHost()) {
                FailoverConnectionProxy.this.fallBackToPrimaryIfAvailable();
            }
            return result;
        }
    }

    static {
        if (Util.isJdbc4()) {
            try {
                INTERFACES_TO_PROXY = new Class[]{Class.forName("com.mysql.jdbc.JDBC4MySQLConnection")};
                return;
            } catch (ClassNotFoundException e) {
                throw new RuntimeException(e);
            }
        }
        INTERFACES_TO_PROXY = new Class[]{MySQLConnection.class};
    }

    public static Connection createProxyInstance(List<String> hosts, Properties props) throws SQLException {
        return (Connection) Proxy.newProxyInstance(Connection.class.getClassLoader(), INTERFACES_TO_PROXY, new FailoverConnectionProxy(hosts, props));
    }

    private FailoverConnectionProxy(List<String> hosts, Properties props) throws SQLException {
        super(hosts, props);
        boolean z = false;
        this.primaryHostIndex = 0;
        this.explicitlyReadOnly = null;
        this.explicitlyAutoCommit = true;
        this.enableFallBackToPrimaryHost = true;
        this.primaryHostFailTimeMillis = 0;
        this.queriesIssuedSinceFailover = 0;
        ConnectionPropertiesImpl connProps = new ConnectionPropertiesImpl();
        connProps.initializeProperties(props);
        this.secondsBeforeRetryPrimaryHost = connProps.getSecondsBeforeRetryMaster();
        this.queriesBeforeRetryPrimaryHost = (long) connProps.getQueriesBeforeRetryMaster();
        this.failoverReadOnly = connProps.getFailOverReadOnly();
        this.retriesAllDown = connProps.getRetriesAllDown();
        if (this.secondsBeforeRetryPrimaryHost <= 0) {
            if (this.queriesBeforeRetryPrimaryHost <= 0) {
                this.enableFallBackToPrimaryHost = z;
                pickNewConnection();
                this.explicitlyAutoCommit = this.currentConnection.getAutoCommit();
            }
        }
        z = true;
        this.enableFallBackToPrimaryHost = z;
        pickNewConnection();
        this.explicitlyAutoCommit = this.currentConnection.getAutoCommit();
    }

    JdbcInterfaceProxy getNewJdbcInterfaceProxy(Object toProxy) {
        return new FailoverJdbcInterfaceProxy(toProxy);
    }

    boolean shouldExceptionTriggerConnectionSwitch(Throwable t) {
        if (!(t instanceof SQLException)) {
            return false;
        }
        String sqlState = ((SQLException) t).getSQLState();
        if ((sqlState == null || !sqlState.startsWith("08")) && !(t instanceof CommunicationsException)) {
            return false;
        }
        return true;
    }

    boolean isMasterConnection() {
        return connectedToPrimaryHost();
    }

    /* JADX WARNING: inconsistent code. */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    synchronized void pickNewConnection() throws java.sql.SQLException {
        /*
        r2 = this;
        monitor-enter(r2);
        r0 = r2.isClosed;	 Catch:{ all -> 0x002e }
        if (r0 == 0) goto L_0x000b;
    L_0x0005:
        r0 = r2.closedExplicitly;	 Catch:{ all -> 0x002e }
        if (r0 == 0) goto L_0x000b;
    L_0x0009:
        monitor-exit(r2);
        return;
    L_0x000b:
        r0 = r2.isConnected();	 Catch:{ all -> 0x002e }
        if (r0 == 0) goto L_0x001c;
    L_0x0011:
        r0 = r2.readyToFallBackToPrimaryHost();	 Catch:{ all -> 0x002e }
        if (r0 == 0) goto L_0x0018;
    L_0x0017:
        goto L_0x001c;
    L_0x0018:
        r2.failOver();	 Catch:{ all -> 0x002e }
        goto L_0x002c;
    L_0x001c:
        r0 = r2.primaryHostIndex;	 Catch:{ SQLException -> 0x0022 }
        r2.connectTo(r0);	 Catch:{ SQLException -> 0x0022 }
    L_0x0021:
        goto L_0x002c;
    L_0x0022:
        r0 = move-exception;
        r2.resetAutoFallBackCounters();	 Catch:{ all -> 0x002e }
        r1 = r2.primaryHostIndex;	 Catch:{ all -> 0x002e }
        r2.failOver(r1);	 Catch:{ all -> 0x002e }
        goto L_0x0021;
    L_0x002c:
        monitor-exit(r2);
        return;
    L_0x002e:
        r0 = move-exception;
        monitor-exit(r2);
        throw r0;
        */
        throw new UnsupportedOperationException("Method not decompiled: com.mysql.jdbc.FailoverConnectionProxy.pickNewConnection():void");
    }

    synchronized ConnectionImpl createConnectionForHostIndex(int hostIndex) throws SQLException {
        return createConnectionForHost((String) this.hostList.get(hostIndex));
    }

    private synchronized void connectTo(int hostIndex) throws SQLException {
        try {
            switchCurrentConnectionTo(hostIndex, createConnectionForHostIndex(hostIndex));
        } catch (SQLException e) {
            if (this.currentConnection != null) {
                StringBuilder msg = new StringBuilder("Connection to ");
                msg.append(isPrimaryHostIndex(hostIndex) ? "primary" : "secondary");
                msg.append(" host '");
                msg.append((String) this.hostList.get(hostIndex));
                msg.append("' failed");
                this.currentConnection.getLog().logWarn(msg.toString(), e);
            }
            throw e;
        }
    }

    private synchronized void switchCurrentConnectionTo(int hostIndex, MySQLConnection connection) throws SQLException {
        invalidateCurrentConnection();
        boolean readOnly = false;
        if (isPrimaryHostIndex(hostIndex)) {
            if (this.explicitlyReadOnly != null) {
                readOnly = this.explicitlyReadOnly.booleanValue();
            }
        } else if (this.failoverReadOnly) {
            readOnly = true;
        } else if (this.explicitlyReadOnly != null) {
            readOnly = this.explicitlyReadOnly.booleanValue();
        } else if (this.currentConnection != null) {
            readOnly = this.currentConnection.isReadOnly();
        } else {
            MultiHostConnectionProxy.syncSessionState(this.currentConnection, connection, readOnly);
            this.currentConnection = connection;
            this.currentHostIndex = hostIndex;
        }
        MultiHostConnectionProxy.syncSessionState(this.currentConnection, connection, readOnly);
        this.currentConnection = connection;
        this.currentHostIndex = hostIndex;
    }

    private synchronized void failOver() throws SQLException {
        failOver(this.currentHostIndex);
    }

    private synchronized void failOver(int failedHostIdx) throws SQLException {
        boolean firstConnOrPassedByPrimaryHost;
        SQLException e;
        int prevHostIndex = this.currentHostIndex;
        int nextHostIndex = nextHost(failedHostIdx, false);
        int firstHostIndexTried = nextHostIndex;
        SQLException lastExceptionCaught = null;
        int attempts = 0;
        boolean gotConnection = false;
        if (prevHostIndex != -1) {
            if (!isPrimaryHostIndex(prevHostIndex)) {
                firstConnOrPassedByPrimaryHost = false;
                do {
                    if (!firstConnOrPassedByPrimaryHost) {
                        try {
                            if (isPrimaryHostIndex(nextHostIndex)) {
                                e = null;
                                firstConnOrPassedByPrimaryHost = e;
                                connectTo(nextHostIndex);
                                if (firstConnOrPassedByPrimaryHost && connectedToSecondaryHost() != null) {
                                    resetAutoFallBackCounters();
                                }
                                gotConnection = true;
                                if (attempts >= this.retriesAllDown) {
                                    break;
                                }
                            }
                        } catch (SQLException e2) {
                            lastExceptionCaught = e2;
                            if (shouldExceptionTriggerConnectionSwitch(e2)) {
                                int newNextHostIndex;
                                int newNextHostIndex2 = nextHost(nextHostIndex, attempts > 0 ? 1 : 0);
                                if (newNextHostIndex2 == firstHostIndexTried) {
                                    int nextHost = nextHost(nextHostIndex, true);
                                    newNextHostIndex = nextHost;
                                    if (newNextHostIndex2 == nextHost) {
                                        attempts++;
                                        try {
                                            Thread.sleep(250);
                                        } catch (InterruptedException e3) {
                                        }
                                    }
                                } else {
                                    newNextHostIndex = newNextHostIndex2;
                                }
                                nextHostIndex = newNextHostIndex;
                            } else {
                                throw e2;
                            }
                        }
                    }
                    e2 = 1;
                    firstConnOrPassedByPrimaryHost = e2;
                    connectTo(nextHostIndex);
                    resetAutoFallBackCounters();
                    gotConnection = true;
                    if (attempts >= this.retriesAllDown) {
                        break;
                    }
                    break;
                } while (!gotConnection);
                if (!gotConnection) {
                    throw lastExceptionCaught;
                }
            }
        }
        firstConnOrPassedByPrimaryHost = true;
        do {
            if (firstConnOrPassedByPrimaryHost) {
                if (isPrimaryHostIndex(nextHostIndex)) {
                    e2 = null;
                    firstConnOrPassedByPrimaryHost = e2;
                    connectTo(nextHostIndex);
                    resetAutoFallBackCounters();
                    gotConnection = true;
                    if (attempts >= this.retriesAllDown) {
                        break;
                    }
                }
            }
            e2 = 1;
            firstConnOrPassedByPrimaryHost = e2;
            connectTo(nextHostIndex);
            resetAutoFallBackCounters();
            gotConnection = true;
            if (attempts >= this.retriesAllDown) {
                break;
            }
            break;
        } while (!gotConnection);
        if (!gotConnection) {
            throw lastExceptionCaught;
        }
    }

    synchronized void fallBackToPrimaryIfAvailable() {
        MySQLConnection connection = null;
        try {
            connection = createConnectionForHostIndex(this.primaryHostIndex);
            switchCurrentConnectionTo(this.primaryHostIndex, connection);
        } catch (SQLException e) {
            if (connection != null) {
                try {
                    connection.close();
                } catch (SQLException e2) {
                }
            }
            resetAutoFallBackCounters();
        }
        return;
    }

    private int nextHost(int currHostIdx, boolean vouchForPrimaryHost) {
        int nextHostIdx = (currHostIdx + 1) % this.hostList.size();
        if (isPrimaryHostIndex(nextHostIdx) && isConnected() && !vouchForPrimaryHost && this.enableFallBackToPrimaryHost && !readyToFallBackToPrimaryHost()) {
            return nextHost(nextHostIdx, vouchForPrimaryHost);
        }
        return nextHostIdx;
    }

    synchronized void incrementQueriesIssuedSinceFailover() {
        this.queriesIssuedSinceFailover++;
    }

    synchronized boolean readyToFallBackToPrimaryHost() {
        boolean z;
        z = this.enableFallBackToPrimaryHost && connectedToSecondaryHost() && (secondsBeforeRetryPrimaryHostIsMet() || queriesBeforeRetryPrimaryHostIsMet());
        return z;
    }

    synchronized boolean isConnected() {
        return this.currentHostIndex != -1;
    }

    synchronized boolean isPrimaryHostIndex(int hostIndex) {
        return hostIndex == this.primaryHostIndex;
    }

    synchronized boolean connectedToPrimaryHost() {
        return isPrimaryHostIndex(this.currentHostIndex);
    }

    synchronized boolean connectedToSecondaryHost() {
        boolean z;
        z = this.currentHostIndex >= 0 && !isPrimaryHostIndex(this.currentHostIndex);
        return z;
    }

    private synchronized boolean secondsBeforeRetryPrimaryHostIsMet() {
        boolean z;
        z = this.secondsBeforeRetryPrimaryHost > 0 && Util.secondsSinceMillis(this.primaryHostFailTimeMillis) >= ((long) this.secondsBeforeRetryPrimaryHost);
        return z;
    }

    private synchronized boolean queriesBeforeRetryPrimaryHostIsMet() {
        boolean z;
        z = this.queriesBeforeRetryPrimaryHost > 0 && this.queriesIssuedSinceFailover >= this.queriesBeforeRetryPrimaryHost;
        return z;
    }

    private synchronized void resetAutoFallBackCounters() {
        this.primaryHostFailTimeMillis = System.currentTimeMillis();
        this.queriesIssuedSinceFailover = 0;
    }

    synchronized void doClose() throws SQLException {
        this.currentConnection.close();
    }

    synchronized void doAbortInternal() throws SQLException {
        this.currentConnection.abortInternal();
    }

    synchronized void doAbort(Executor executor) throws SQLException {
        this.currentConnection.abort(executor);
    }

    /* JADX WARNING: inconsistent code. */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    public synchronized java.lang.Object invokeMore(java.lang.Object r6, java.lang.reflect.Method r7, java.lang.Object[] r8) throws java.lang.Throwable {
        /*
        r5 = this;
        monitor-enter(r5);
        r0 = r7.getName();	 Catch:{ all -> 0x00a9 }
        r1 = "setReadOnly";
        r1 = r1.equals(r0);	 Catch:{ all -> 0x00a9 }
        r2 = 0;
        r3 = 0;
        if (r1 == 0) goto L_0x0021;
    L_0x000f:
        r1 = r8[r2];	 Catch:{ all -> 0x00a9 }
        r1 = (java.lang.Boolean) r1;	 Catch:{ all -> 0x00a9 }
        r5.explicitlyReadOnly = r1;	 Catch:{ all -> 0x00a9 }
        r1 = r5.failoverReadOnly;	 Catch:{ all -> 0x00a9 }
        if (r1 == 0) goto L_0x0021;
    L_0x0019:
        r1 = r5.connectedToSecondaryHost();	 Catch:{ all -> 0x00a9 }
        if (r1 == 0) goto L_0x0021;
    L_0x001f:
        monitor-exit(r5);
        return r3;
    L_0x0021:
        r1 = r5.isClosed;	 Catch:{ all -> 0x00a9 }
        if (r1 == 0) goto L_0x0062;
    L_0x0025:
        r1 = r5.allowedOnClosedConnection(r7);	 Catch:{ all -> 0x00a9 }
        if (r1 != 0) goto L_0x0062;
    L_0x002b:
        r1 = r5.autoReconnect;	 Catch:{ all -> 0x00a9 }
        if (r1 == 0) goto L_0x003e;
    L_0x002f:
        r1 = r5.closedExplicitly;	 Catch:{ all -> 0x00a9 }
        if (r1 != 0) goto L_0x003e;
    L_0x0033:
        r1 = -1;
        r5.currentHostIndex = r1;	 Catch:{ all -> 0x00a9 }
        r5.pickNewConnection();	 Catch:{ all -> 0x00a9 }
        r5.isClosed = r2;	 Catch:{ all -> 0x00a9 }
        r5.closedReason = r3;	 Catch:{ all -> 0x00a9 }
        goto L_0x0062;
    L_0x003e:
        r1 = "No operations allowed after connection closed.";
        r2 = r5.closedReason;	 Catch:{ all -> 0x00a9 }
        if (r2 == 0) goto L_0x005b;
    L_0x0044:
        r2 = new java.lang.StringBuilder;	 Catch:{ all -> 0x00a9 }
        r2.<init>();	 Catch:{ all -> 0x00a9 }
        r2.append(r1);	 Catch:{ all -> 0x00a9 }
        r4 = "  ";
        r2.append(r4);	 Catch:{ all -> 0x00a9 }
        r4 = r5.closedReason;	 Catch:{ all -> 0x00a9 }
        r2.append(r4);	 Catch:{ all -> 0x00a9 }
        r2 = r2.toString();	 Catch:{ all -> 0x00a9 }
        r1 = r2;
    L_0x005b:
        r2 = "08003";
        r2 = com.mysql.jdbc.SQLError.createSQLException(r1, r2, r3);	 Catch:{ all -> 0x00a9 }
        throw r2;	 Catch:{ all -> 0x00a9 }
    L_0x0062:
        r1 = r3;
        r3 = r5.thisAsConnection;	 Catch:{ InvocationTargetException -> 0x0074 }
        r3 = r7.invoke(r3, r8);	 Catch:{ InvocationTargetException -> 0x0074 }
        r1 = r3;
        r3 = r7.getReturnType();	 Catch:{ InvocationTargetException -> 0x0074 }
        r3 = r5.proxyIfReturnTypeIsJdbcInterface(r3, r1);	 Catch:{ InvocationTargetException -> 0x0074 }
        r1 = r3;
        goto L_0x0078;
    L_0x0074:
        r3 = move-exception;
        r5.dealWithInvocationException(r3);	 Catch:{ all -> 0x00a9 }
    L_0x0078:
        r3 = "setAutoCommit";
        r3 = r3.equals(r0);	 Catch:{ all -> 0x00a9 }
        if (r3 == 0) goto L_0x008a;
    L_0x0080:
        r2 = r8[r2];	 Catch:{ all -> 0x00a9 }
        r2 = (java.lang.Boolean) r2;	 Catch:{ all -> 0x00a9 }
        r2 = r2.booleanValue();	 Catch:{ all -> 0x00a9 }
        r5.explicitlyAutoCommit = r2;	 Catch:{ all -> 0x00a9 }
    L_0x008a:
        r2 = r5.explicitlyAutoCommit;	 Catch:{ all -> 0x00a9 }
        if (r2 != 0) goto L_0x009e;
    L_0x008e:
        r2 = "commit";
        r2 = r2.equals(r0);	 Catch:{ all -> 0x00a9 }
        if (r2 != 0) goto L_0x009e;
    L_0x0096:
        r2 = "rollback";
        r2 = r2.equals(r0);	 Catch:{ all -> 0x00a9 }
        if (r2 == 0) goto L_0x00a7;
    L_0x009e:
        r2 = r5.readyToFallBackToPrimaryHost();	 Catch:{ all -> 0x00a9 }
        if (r2 == 0) goto L_0x00a7;
    L_0x00a4:
        r5.fallBackToPrimaryIfAvailable();	 Catch:{ all -> 0x00a9 }
    L_0x00a7:
        monitor-exit(r5);
        return r1;
    L_0x00a9:
        r6 = move-exception;
        monitor-exit(r5);
        throw r6;
        */
        throw new UnsupportedOperationException("Method not decompiled: com.mysql.jdbc.FailoverConnectionProxy.invokeMore(java.lang.Object, java.lang.reflect.Method, java.lang.Object[]):java.lang.Object");
    }
}
