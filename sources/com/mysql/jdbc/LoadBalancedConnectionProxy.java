package com.mysql.jdbc;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.Set;
import java.util.concurrent.Executor;

public class LoadBalancedConnectionProxy extends MultiHostConnectionProxy implements PingTarget {
    public static final String BLACKLIST_TIMEOUT_PROPERTY_KEY = "loadBalanceBlacklistTimeout";
    public static final String HOST_REMOVAL_GRACE_PERIOD_PROPERTY_KEY = "loadBalanceHostRemovalGracePeriod";
    private static Class<?>[] INTERFACES_TO_PROXY;
    private static Constructor<?> JDBC_4_LB_CONNECTION_CTOR;
    private static Map<String, Long> globalBlacklist = new HashMap();
    private static LoadBalancedConnection nullLBConnectionInstance = null;
    private int autoCommitSwapThreshold = 0;
    private BalanceStrategy balancer;
    private ConnectionGroup connectionGroup = null;
    private long connectionGroupProxyID = 0;
    private Map<ConnectionImpl, String> connectionsToHostsMap;
    private LoadBalanceExceptionChecker exceptionChecker;
    private int globalBlacklistTimeout = 0;
    private int hostRemovalGracePeriod = 0;
    private Map<String, Integer> hostsToListIndexMap;
    private Set<String> hostsToRemove = new HashSet();
    private boolean inTransaction = false;
    protected Map<String, ConnectionImpl> liveConnections;
    private long[] responseTimes;
    private int retriesAllDown;
    private long totalPhysicalConnections = 0;
    private long transactionCount = 0;
    private long transactionStartTime = 0;

    private static class NullLoadBalancedConnectionProxy implements InvocationHandler {
        public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
            SQLException exceptionToThrow = SQLError.createSQLException(Messages.getString("LoadBalancedConnectionProxy.unusableConnection"), SQLError.SQL_STATE_INVALID_TRANSACTION_STATE, (int) MysqlErrorNumbers.ERROR_CODE_NULL_LOAD_BALANCED_CONNECTION, true, null);
            for (Class<?> declEx : method.getExceptionTypes()) {
                if (declEx.isAssignableFrom(exceptionToThrow.getClass())) {
                    throw exceptionToThrow;
                }
            }
            throw new IllegalStateException(exceptionToThrow.getMessage(), exceptionToThrow);
        }
    }

    static {
        if (Util.isJdbc4()) {
            try {
                JDBC_4_LB_CONNECTION_CTOR = Class.forName("com.mysql.jdbc.JDBC4LoadBalancedMySQLConnection").getConstructor(new Class[]{LoadBalancedConnectionProxy.class});
                INTERFACES_TO_PROXY = new Class[]{LoadBalancedConnection.class, Class.forName("com.mysql.jdbc.JDBC4MySQLConnection")};
            } catch (SecurityException e) {
                throw new RuntimeException(e);
            } catch (NoSuchMethodException e2) {
                throw new RuntimeException(e2);
            } catch (ClassNotFoundException e3) {
                throw new RuntimeException(e3);
            }
        }
        INTERFACES_TO_PROXY = new Class[]{LoadBalancedConnection.class};
    }

    public static LoadBalancedConnection createProxyInstance(List<String> hosts, Properties props) throws SQLException {
        return (LoadBalancedConnection) Proxy.newProxyInstance(LoadBalancedConnection.class.getClassLoader(), INTERFACES_TO_PROXY, new LoadBalancedConnectionProxy(hosts, props));
    }

    private LoadBalancedConnectionProxy(List<String> hosts, Properties props) throws SQLException {
        boolean z;
        String str;
        NumberFormatException nfe;
        Properties properties = props;
        String group = properties.getProperty("loadBalanceConnectionGroup", null);
        try {
            List<String> hosts2;
            boolean enableJMX = Boolean.parseBoolean(properties.getProperty("loadBalanceEnableJMX", "false"));
            if (group != null) {
                r1.connectionGroup = ConnectionGroupManager.getConnectionGroupInstance(group);
                if (enableJMX) {
                    ConnectionGroupManager.registerJmx();
                }
                r1.connectionGroupProxyID = r1.connectionGroup.registerConnectionProxy(r1, hosts);
                hosts2 = new ArrayList(r1.connectionGroup.getInitialHosts());
            } else {
                hosts2 = hosts;
            }
            int numHosts = initializeHostsSpecs(hosts2, properties);
            r1.liveConnections = new HashMap(numHosts);
            r1.hostsToListIndexMap = new HashMap(numHosts);
            for (int i = 0; i < numHosts; i++) {
                r1.hostsToListIndexMap.put(r1.hostList.get(i), Integer.valueOf(i));
            }
            r1.connectionsToHostsMap = new HashMap(numHosts);
            r1.responseTimes = new long[numHosts];
            try {
                r1.retriesAllDown = Integer.parseInt(r1.localProps.getProperty("retriesAllDown", "120"));
                try {
                    r1.globalBlacklistTimeout = Integer.parseInt(r1.localProps.getProperty(BLACKLIST_TIMEOUT_PROPERTY_KEY, "0"));
                    try {
                        r1.hostRemovalGracePeriod = Integer.parseInt(r1.localProps.getProperty(HOST_REMOVAL_GRACE_PERIOD_PROPERTY_KEY, "15000"));
                        String strategy = r1.localProps.getProperty("loadBalanceStrategy", "random");
                        if ("random".equals(strategy)) {
                            r1.balancer = (BalanceStrategy) Util.loadExtensions(null, properties, RandomBalanceStrategy.class.getName(), "InvalidLoadBalanceStrategy", null).get(0);
                        } else if ("bestResponseTime".equals(strategy)) {
                            r1.balancer = (BalanceStrategy) Util.loadExtensions(null, properties, BestResponseTimeBalanceStrategy.class.getName(), "InvalidLoadBalanceStrategy", null).get(0);
                        } else if ("serverAffinity".equals(strategy)) {
                            r1.balancer = (BalanceStrategy) Util.loadExtensions(null, properties, ServerAffinityStrategy.class.getName(), "InvalidLoadBalanceStrategy", null).get(0);
                        } else {
                            r1.balancer = (BalanceStrategy) Util.loadExtensions(null, properties, strategy, "InvalidLoadBalanceStrategy", null).get(0);
                        }
                        try {
                            r1.autoCommitSwapThreshold = Integer.parseInt(properties.getProperty("loadBalanceAutoCommitStatementThreshold", "0"));
                            String autoCommitSwapRegex = properties.getProperty("loadBalanceAutoCommitStatementRegex", "");
                            if ("".equals(autoCommitSwapRegex)) {
                                z = enableJMX;
                            } else {
                                try {
                                    "".matches(autoCommitSwapRegex);
                                    str = group;
                                    z = enableJMX;
                                } catch (Exception e) {
                                    throw SQLError.createSQLException(Messages.getString("LoadBalancedConnectionProxy.badValueForLoadBalanceAutoCommitStatementRegex", new Object[]{autoCommitSwapRegex}), SQLError.SQL_STATE_ILLEGAL_ARGUMENT, null);
                                }
                            }
                            if (r1.autoCommitSwapThreshold > 0) {
                                group = r1.localProps.getProperty("statementInterceptors");
                                if (group == null) {
                                    r1.localProps.setProperty("statementInterceptors", "com.mysql.jdbc.LoadBalancedAutoCommitInterceptor");
                                    String str2 = autoCommitSwapRegex;
                                } else if (group.length() > 0) {
                                    StringBuilder stringBuilder = new StringBuilder();
                                    stringBuilder.append(group);
                                    stringBuilder.append(",com.mysql.jdbc.LoadBalancedAutoCommitInterceptor");
                                    r1.localProps.setProperty("statementInterceptors", stringBuilder.toString());
                                }
                                properties.setProperty("statementInterceptors", r1.localProps.getProperty("statementInterceptors"));
                            }
                            r1.balancer.init(null, properties);
                            r1.exceptionChecker = (LoadBalanceExceptionChecker) Util.loadExtensions(null, properties, r1.localProps.getProperty("loadBalanceExceptionChecker", "com.mysql.jdbc.StandardLoadBalanceExceptionChecker"), "InvalidLoadBalanceExceptionChecker", null).get(0);
                            pickNewConnection();
                        } catch (NumberFormatException e2) {
                            str = group;
                            z = enableJMX;
                            nfe = e2;
                            throw SQLError.createSQLException(Messages.getString("LoadBalancedConnectionProxy.badValueForLoadBalanceAutoCommitStatementThreshold", new Object[]{autoCommitSwapThresholdAsString}), SQLError.SQL_STATE_ILLEGAL_ARGUMENT, null);
                        }
                    } catch (NumberFormatException e22) {
                        str = group;
                        z = enableJMX;
                        nfe = e22;
                        throw SQLError.createSQLException(Messages.getString("LoadBalancedConnectionProxy.badValueForLoadBalanceHostRemovalGracePeriod", new Object[]{hostRemovalGracePeriodAsString}), SQLError.SQL_STATE_ILLEGAL_ARGUMENT, null);
                    }
                } catch (NumberFormatException e222) {
                    str = group;
                    z = enableJMX;
                    nfe = e222;
                    throw SQLError.createSQLException(Messages.getString("LoadBalancedConnectionProxy.badValueForLoadBalanceBlacklistTimeout", new Object[]{blacklistTimeoutAsString}), SQLError.SQL_STATE_ILLEGAL_ARGUMENT, null);
                }
            } catch (NumberFormatException e2222) {
                str = group;
                z = enableJMX;
                nfe = e2222;
                throw SQLError.createSQLException(Messages.getString("LoadBalancedConnectionProxy.badValueForRetriesAllDown", new Object[]{retriesAllDownAsString}), SQLError.SQL_STATE_ILLEGAL_ARGUMENT, null);
            }
        } catch (Exception e3) {
            List<String> list = hosts;
            str = group;
            throw SQLError.createSQLException(Messages.getString("LoadBalancedConnectionProxy.badValueForLoadBalanceEnableJMX", new Object[]{enableJMXAsString}), SQLError.SQL_STATE_ILLEGAL_ARGUMENT, null);
        }
    }

    MySQLConnection getNewWrapperForThisAsConnection() throws SQLException {
        if (!Util.isJdbc4()) {
            if (JDBC_4_LB_CONNECTION_CTOR == null) {
                return new LoadBalancedMySQLConnection(this);
            }
        }
        return (MySQLConnection) Util.handleNewInstance(JDBC_4_LB_CONNECTION_CTOR, new Object[]{this}, null);
    }

    protected void propagateProxyDown(MySQLConnection proxyConn) {
        for (ConnectionImpl c : this.liveConnections.values()) {
            c.setProxy(proxyConn);
        }
    }

    boolean shouldExceptionTriggerConnectionSwitch(Throwable t) {
        return (t instanceof SQLException) && this.exceptionChecker.shouldExceptionTriggerFailover((SQLException) t);
    }

    boolean isMasterConnection() {
        return true;
    }

    synchronized void invalidateConnection(MySQLConnection conn) throws SQLException {
        super.invalidateConnection(conn);
        if (isGlobalBlacklistEnabled()) {
            addToGlobalBlacklist((String) this.connectionsToHostsMap.get(conn));
        }
        this.liveConnections.remove(this.connectionsToHostsMap.get(conn));
        Object mappedHost = this.connectionsToHostsMap.remove(conn);
        if (mappedHost != null && this.hostsToListIndexMap.containsKey(mappedHost)) {
            int hostIndex = ((Integer) this.hostsToListIndexMap.get(mappedHost)).intValue();
            synchronized (this.responseTimes) {
                this.responseTimes[hostIndex] = 0;
            }
        }
    }

    synchronized void pickNewConnection() throws SQLException {
        if (!this.isClosed || !this.closedExplicitly) {
            if (this.currentConnection == null) {
                this.currentConnection = this.balancer.pickConnection(this, Collections.unmodifiableList(this.hostList), Collections.unmodifiableMap(this.liveConnections), (long[]) this.responseTimes.clone(), this.retriesAllDown);
                return;
            }
            if (this.currentConnection.isClosed()) {
                invalidateCurrentConnection();
            }
            int pingTimeout = this.currentConnection.getLoadBalancePingTimeout();
            boolean pingBeforeReturn = this.currentConnection.getLoadBalanceValidateConnectionOnSwapServer();
            int hostsTried = 0;
            int hostsToTry = this.hostList.size();
            while (hostsTried < hostsToTry) {
                try {
                    ConnectionImpl newConn = this.balancer.pickConnection(this, Collections.unmodifiableList(this.hostList), Collections.unmodifiableMap(this.liveConnections), (long[]) this.responseTimes.clone(), this.retriesAllDown);
                    if (this.currentConnection != null) {
                        if (pingBeforeReturn) {
                            if (pingTimeout == 0) {
                                newConn.ping();
                            } else {
                                newConn.pingInternal(true, pingTimeout);
                            }
                        }
                        MultiHostConnectionProxy.syncSessionState(this.currentConnection, newConn);
                    }
                    this.currentConnection = newConn;
                    return;
                } catch (SQLException e) {
                    if (shouldExceptionTriggerConnectionSwitch(e) && null != null) {
                        invalidateConnection(null);
                    }
                    hostsTried++;
                }
            }
            this.isClosed = true;
            this.closedReason = "Connection closed after inability to pick valid new connection during load-balance.";
        }
    }

    public synchronized ConnectionImpl createConnectionForHost(String hostPortSpec) throws SQLException {
        ConnectionImpl conn;
        conn = super.createConnectionForHost(hostPortSpec);
        this.liveConnections.put(hostPortSpec, conn);
        this.connectionsToHostsMap.put(conn, hostPortSpec);
        this.totalPhysicalConnections++;
        return conn;
    }

    private synchronized void closeAllConnections() {
        for (ConnectionImpl c : this.liveConnections.values()) {
            try {
                c.close();
            } catch (SQLException e) {
            }
        }
        if (!this.isClosed) {
            this.balancer.destroy();
            if (this.connectionGroup != null) {
                this.connectionGroup.closeConnectionProxy(this);
            }
        }
        this.liveConnections.clear();
        this.connectionsToHostsMap.clear();
    }

    synchronized void doClose() {
        closeAllConnections();
    }

    synchronized void doAbortInternal() {
        for (ConnectionImpl c : this.liveConnections.values()) {
            try {
                c.abortInternal();
            } catch (SQLException e) {
            }
        }
        if (!this.isClosed) {
            this.balancer.destroy();
            if (this.connectionGroup != null) {
                this.connectionGroup.closeConnectionProxy(this);
            }
        }
        this.liveConnections.clear();
        this.connectionsToHostsMap.clear();
    }

    synchronized void doAbort(Executor executor) {
        for (ConnectionImpl c : this.liveConnections.values()) {
            try {
                c.abort(executor);
            } catch (SQLException e) {
            }
        }
        if (!this.isClosed) {
            this.balancer.destroy();
            if (this.connectionGroup != null) {
                this.connectionGroup.closeConnectionProxy(this);
            }
        }
        this.liveConnections.clear();
        this.connectionsToHostsMap.clear();
    }

    public synchronized Object invokeMore(Object proxy, Method method, Object[] args) throws Throwable {
        Object result;
        String str;
        Integer hostIndex;
        Throwable th;
        Object obj;
        InvocationTargetException e;
        Object result2;
        LoadBalancedConnectionProxy this;
        Object obj2;
        Method method2;
        String methodName;
        Object obj3;
        Throwable th2;
        LoadBalancedConnectionProxy this2;
        Method method3;
        String str2;
        Integer hostIndex2;
        Object[] objArr;
        LoadBalancedConnectionProxy loadBalancedConnectionProxy = this;
        Method method4 = method;
        synchronized (this) {
            try {
                String methodName2 = method.getName();
                if (loadBalancedConnectionProxy.isClosed && !allowedOnClosedConnection(method4) && method.getExceptionTypes().length > 0) {
                    if (!loadBalancedConnectionProxy.autoReconnect || loadBalancedConnectionProxy.closedExplicitly) {
                        String reason = "No operations allowed after connection closed.";
                        if (loadBalancedConnectionProxy.closedReason != null) {
                            StringBuilder stringBuilder = new StringBuilder();
                            stringBuilder.append(reason);
                            stringBuilder.append(" ");
                            stringBuilder.append(loadBalancedConnectionProxy.closedReason);
                            reason = stringBuilder.toString();
                        }
                        throw SQLError.createSQLException(reason, SQLError.SQL_STATE_CONNECTION_NOT_OPEN, null);
                    }
                    loadBalancedConnectionProxy.currentConnection = null;
                    pickNewConnection();
                    loadBalancedConnectionProxy.isClosed = false;
                    loadBalancedConnectionProxy.closedReason = null;
                }
                if (!loadBalancedConnectionProxy.inTransaction) {
                    loadBalancedConnectionProxy.inTransaction = true;
                    loadBalancedConnectionProxy.transactionStartTime = System.nanoTime();
                    loadBalancedConnectionProxy.transactionCount++;
                }
                result = null;
                LoadBalancedConnectionProxy this3;
                Method method5;
                String host;
                try {
                    try {
                        result = method4.invoke(loadBalancedConnectionProxy.thisAsConnection, args);
                        if (result != null) {
                            if (result instanceof Statement) {
                                ((Statement) result).setPingTarget(loadBalancedConnectionProxy);
                            }
                            result = proxyIfReturnTypeIsJdbcInterface(method.getReturnType(), result);
                        }
                        this3 = loadBalancedConnectionProxy;
                        if (!"commit".equals(methodName2)) {
                            if (!"rollback".equals(methodName2)) {
                                method5 = method4;
                                str = methodName2;
                            }
                        }
                        this3.inTransaction = false;
                        host = (String) this3.connectionsToHostsMap.get(this3.currentConnection);
                        if (host != null) {
                            synchronized (this3.responseTimes) {
                                try {
                                    hostIndex = (Integer) this3.hostsToListIndexMap.get(host);
                                    if (hostIndex == null || hostIndex.intValue() >= this3.responseTimes.length) {
                                        str = methodName2;
                                    } else {
                                        this3.responseTimes[hostIndex.intValue()] = System.nanoTime() - this3.transactionStartTime;
                                    }
                                } catch (Throwable th3) {
                                    obj = th3;
                                    throw method4;
                                }
                            }
                        }
                        str = methodName2;
                        pickNewConnection();
                    } catch (InvocationTargetException e2) {
                        e = e2;
                        result2 = result;
                        try {
                            dealWithInvocationException(e);
                            this = loadBalancedConnectionProxy;
                            obj2 = proxy;
                            if (!"commit".equals(methodName2)) {
                                if ("rollback".equals(methodName2)) {
                                    method2 = method4;
                                    methodName = methodName2;
                                    method5 = method2;
                                    str = methodName;
                                    obj3 = result2;
                                    this3 = this;
                                    result = obj3;
                                    return result;
                                }
                            }
                            this.inTransaction = false;
                            host = (String) this.connectionsToHostsMap.get(this.currentConnection);
                            if (host == null) {
                                method2 = method4;
                                methodName = methodName2;
                            } else {
                                synchronized (this.responseTimes) {
                                    try {
                                        hostIndex = (Integer) this.hostsToListIndexMap.get(host);
                                        if (hostIndex != null) {
                                        }
                                        method2 = method4;
                                        methodName = methodName2;
                                    } catch (Throwable th32) {
                                        obj = th32;
                                        throw method4;
                                    }
                                }
                            }
                            pickNewConnection();
                            method5 = method2;
                            str = methodName;
                            obj3 = result2;
                            this3 = this;
                            result = obj3;
                            return result;
                        } catch (Throwable th4) {
                            th32 = th4;
                            th2 = th32;
                            this2 = loadBalancedConnectionProxy;
                            if (!"commit".equals(methodName2)) {
                                if ("rollback".equals(methodName2)) {
                                    method3 = method4;
                                    str2 = methodName2;
                                    throw th2;
                                }
                            }
                            this2.inTransaction = false;
                            host = (String) this2.connectionsToHostsMap.get(this2.currentConnection);
                            if (host != null) {
                                synchronized (this2.responseTimes) {
                                    try {
                                        hostIndex2 = (Integer) this2.hostsToListIndexMap.get(host);
                                        if (hostIndex2 != null) {
                                        }
                                        str2 = methodName2;
                                    } catch (Throwable th322) {
                                        obj = th322;
                                        throw method4;
                                    }
                                }
                            }
                            str2 = methodName2;
                            pickNewConnection();
                            throw th2;
                        }
                    } catch (Throwable th5) {
                        th322 = th5;
                        th2 = th322;
                        this2 = loadBalancedConnectionProxy;
                        if ("commit".equals(methodName2)) {
                            if ("rollback".equals(methodName2)) {
                                method3 = method4;
                                str2 = methodName2;
                                throw th2;
                            }
                        }
                        this2.inTransaction = false;
                        host = (String) this2.connectionsToHostsMap.get(this2.currentConnection);
                        if (host != null) {
                            str2 = methodName2;
                        } else {
                            synchronized (this2.responseTimes) {
                                hostIndex2 = (Integer) this2.hostsToListIndexMap.get(host);
                                if (hostIndex2 != null) {
                                }
                                str2 = methodName2;
                            }
                        }
                        pickNewConnection();
                        throw th2;
                    }
                } catch (InvocationTargetException e3) {
                    e = e3;
                    objArr = args;
                    result2 = result;
                    dealWithInvocationException(e);
                    this = loadBalancedConnectionProxy;
                    obj2 = proxy;
                    if ("commit".equals(methodName2)) {
                        if ("rollback".equals(methodName2)) {
                            method2 = method4;
                            methodName = methodName2;
                            method5 = method2;
                            str = methodName;
                            obj3 = result2;
                            this3 = this;
                            result = obj3;
                            return result;
                        }
                    }
                    this.inTransaction = false;
                    host = (String) this.connectionsToHostsMap.get(this.currentConnection);
                    if (host == null) {
                        synchronized (this.responseTimes) {
                            hostIndex = (Integer) this.hostsToListIndexMap.get(host);
                            if (hostIndex != null || hostIndex.intValue() >= this.responseTimes.length) {
                                method2 = method4;
                                methodName = methodName2;
                            } else {
                                method2 = method4;
                                methodName = methodName2;
                                this.responseTimes[hostIndex.intValue()] = System.nanoTime() - this.transactionStartTime;
                            }
                        }
                    } else {
                        method2 = method4;
                        methodName = methodName2;
                    }
                    pickNewConnection();
                    method5 = method2;
                    str = methodName;
                    obj3 = result2;
                    this3 = this;
                    result = obj3;
                    return result;
                } catch (Throwable th6) {
                    th322 = th6;
                    objArr = args;
                    th2 = th322;
                    this2 = loadBalancedConnectionProxy;
                    if ("commit".equals(methodName2)) {
                        if ("rollback".equals(methodName2)) {
                            method3 = method4;
                            str2 = methodName2;
                            throw th2;
                        }
                    }
                    this2.inTransaction = false;
                    host = (String) this2.connectionsToHostsMap.get(this2.currentConnection);
                    if (host != null) {
                        synchronized (this2.responseTimes) {
                            hostIndex2 = (Integer) this2.hostsToListIndexMap.get(host);
                            if (hostIndex2 != null || hostIndex2.intValue() >= this2.responseTimes.length) {
                                str2 = methodName2;
                            } else {
                                this2.responseTimes[hostIndex2.intValue()] = System.nanoTime() - this2.transactionStartTime;
                            }
                        }
                    } else {
                        str2 = methodName2;
                    }
                    pickNewConnection();
                    throw th2;
                }
            } catch (Throwable th3222) {
                Throwable th7 = th3222;
            }
        }
        return result;
    }

    public synchronized void doPing() throws SQLException {
        SQLException se = null;
        boolean foundHost = false;
        int pingTimeout = this.currentConnection.getLoadBalancePingTimeout();
        for (String host : this.hostList) {
            conn = (ConnectionImpl) this.liveConnections.get(host);
            if (conn != null) {
                if (pingTimeout == 0) {
                    try {
                        conn.ping();
                    } catch (SQLException e) {
                        if (host.equals(this.connectionsToHostsMap.get(this.currentConnection))) {
                            closeAllConnections();
                            this.isClosed = true;
                            this.closedReason = "Connection closed because ping of current connection failed.";
                            throw e;
                        }
                        ConnectionImpl conn;
                        if (!e.getMessage().equals(Messages.getString("Connection.exceededConnectionLifetime"))) {
                            se = e;
                            if (isGlobalBlacklistEnabled()) {
                                addToGlobalBlacklist(host);
                            }
                        } else if (se == null) {
                            se = e;
                        }
                        this.liveConnections.remove(this.connectionsToHostsMap.get(conn));
                    }
                } else {
                    conn.pingInternal(true, pingTimeout);
                }
                foundHost = true;
            }
        }
        if (!foundHost) {
            closeAllConnections();
            this.isClosed = true;
            this.closedReason = "Connection closed due to inability to ping any active connections.";
            if (se != null) {
                throw se;
            }
            ((ConnectionImpl) this.currentConnection).throwConnectionClosedException();
        }
    }

    public void addToGlobalBlacklist(String host, long timeout) {
        if (isGlobalBlacklistEnabled()) {
            synchronized (globalBlacklist) {
                globalBlacklist.put(host, Long.valueOf(timeout));
            }
        }
    }

    public void addToGlobalBlacklist(String host) {
        addToGlobalBlacklist(host, System.currentTimeMillis() + ((long) this.globalBlacklistTimeout));
    }

    public boolean isGlobalBlacklistEnabled() {
        return this.globalBlacklistTimeout > 0;
    }

    public synchronized Map<String, Long> getGlobalBlacklist() {
        if (isGlobalBlacklistEnabled()) {
            Map<String, Long> blacklistClone = new HashMap(globalBlacklist.size());
            synchronized (globalBlacklist) {
                blacklistClone.putAll(globalBlacklist);
            }
            Set<String> keys = blacklistClone.keySet();
            keys.retainAll(this.hostList);
            Iterator<String> i = keys.iterator();
            while (i.hasNext()) {
                String host = (String) i.next();
                Long timeout = (Long) globalBlacklist.get(host);
                if (timeout != null && timeout.longValue() < System.currentTimeMillis()) {
                    synchronized (globalBlacklist) {
                        globalBlacklist.remove(host);
                    }
                    i.remove();
                }
            }
            if (keys.size() != this.hostList.size()) {
                return blacklistClone;
            }
            return new HashMap(1);
        } else if (this.hostsToRemove.isEmpty()) {
            return new HashMap(1);
        } else {
            HashMap<String, Long> fakedBlacklist = new HashMap();
            for (String h : this.hostsToRemove) {
                fakedBlacklist.put(h, Long.valueOf(System.currentTimeMillis() + 5000));
            }
            return fakedBlacklist;
        }
    }

    public void removeHostWhenNotInUse(String hostPortPair) throws SQLException {
        if (this.hostRemovalGracePeriod <= 0) {
            removeHost(hostPortPair);
            return;
        }
        int i = 1000;
        if (this.hostRemovalGracePeriod <= 1000) {
            i = this.hostRemovalGracePeriod;
        }
        int timeBetweenChecks = i;
        synchronized (this) {
            addToGlobalBlacklist(hostPortPair, (System.currentTimeMillis() + ((long) this.hostRemovalGracePeriod)) + ((long) timeBetweenChecks));
            long cur = System.currentTimeMillis();
            while (System.currentTimeMillis() < cur + ((long) this.hostRemovalGracePeriod)) {
                this.hostsToRemove.add(hostPortPair);
                if (hostPortPair.equals(this.currentConnection.getHostPortPair())) {
                    try {
                        Thread.sleep((long) timeBetweenChecks);
                    } catch (InterruptedException e) {
                    }
                } else {
                    removeHost(hostPortPair);
                    return;
                }
            }
            removeHost(hostPortPair);
        }
    }

    public synchronized void removeHost(String hostPortPair) throws SQLException {
        if (this.connectionGroup != null && this.connectionGroup.getInitialHosts().size() == 1 && this.connectionGroup.getInitialHosts().contains(hostPortPair)) {
            throw SQLError.createSQLException("Cannot remove only configured host.", null);
        }
        this.hostsToRemove.add(hostPortPair);
        this.connectionsToHostsMap.remove(this.liveConnections.remove(hostPortPair));
        if (this.hostsToListIndexMap.remove(hostPortPair) != null) {
            long[] newResponseTimes = new long[(this.responseTimes.length - 1)];
            int newIdx = 0;
            for (String h : this.hostList) {
                if (!this.hostsToRemove.contains(h)) {
                    Integer idx = (Integer) this.hostsToListIndexMap.get(h);
                    if (idx != null && idx.intValue() < this.responseTimes.length) {
                        newResponseTimes[newIdx] = this.responseTimes[idx.intValue()];
                    }
                    int newIdx2 = newIdx + 1;
                    this.hostsToListIndexMap.put(h, Integer.valueOf(newIdx));
                    newIdx = newIdx2;
                }
            }
            this.responseTimes = newResponseTimes;
        }
        if (hostPortPair.equals(this.currentConnection.getHostPortPair())) {
            invalidateConnection(this.currentConnection);
            pickNewConnection();
        }
    }

    public synchronized boolean addHost(String hostPortPair) {
        if (this.hostsToListIndexMap.containsKey(hostPortPair)) {
            return false;
        }
        long[] newResponseTimes = new long[(this.responseTimes.length + 1)];
        System.arraycopy(this.responseTimes, 0, newResponseTimes, 0, this.responseTimes.length);
        this.responseTimes = newResponseTimes;
        if (!this.hostList.contains(hostPortPair)) {
            this.hostList.add(hostPortPair);
        }
        this.hostsToListIndexMap.put(hostPortPair, Integer.valueOf(this.responseTimes.length - 1));
        this.hostsToRemove.remove(hostPortPair);
        return true;
    }

    public synchronized boolean inTransaction() {
        return this.inTransaction;
    }

    public synchronized long getTransactionCount() {
        return this.transactionCount;
    }

    public synchronized long getActivePhysicalConnectionCount() {
        return (long) this.liveConnections.size();
    }

    public synchronized long getTotalPhysicalConnectionCount() {
        return this.totalPhysicalConnections;
    }

    public synchronized long getConnectionGroupProxyID() {
        return this.connectionGroupProxyID;
    }

    public synchronized String getCurrentActiveHost() {
        MySQLConnection c = this.currentConnection;
        if (c != null) {
            Object o = this.connectionsToHostsMap.get(c);
            if (o != null) {
                return o.toString();
            }
        }
        return null;
    }

    /* JADX WARNING: inconsistent code. */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    public synchronized long getCurrentTransactionDuration() {
        /*
        r6 = this;
        monitor-enter(r6);
        r0 = r6.inTransaction;	 Catch:{ all -> 0x0019 }
        r1 = 0;
        if (r0 == 0) goto L_0x0017;
    L_0x0007:
        r3 = r6.transactionStartTime;	 Catch:{ all -> 0x0019 }
        r0 = (r3 > r1 ? 1 : (r3 == r1 ? 0 : -1));
        if (r0 <= 0) goto L_0x0017;
    L_0x000d:
        r0 = java.lang.System.nanoTime();	 Catch:{ all -> 0x0019 }
        r2 = r6.transactionStartTime;	 Catch:{ all -> 0x0019 }
        r4 = r0 - r2;
        monitor-exit(r6);
        return r4;
    L_0x0017:
        monitor-exit(r6);
        return r1;
    L_0x0019:
        r0 = move-exception;
        monitor-exit(r6);
        throw r0;
        */
        throw new UnsupportedOperationException("Method not decompiled: com.mysql.jdbc.LoadBalancedConnectionProxy.getCurrentTransactionDuration():long");
    }

    static synchronized LoadBalancedConnection getNullLoadBalancedConnectionInstance() {
        LoadBalancedConnection loadBalancedConnection;
        synchronized (LoadBalancedConnectionProxy.class) {
            if (nullLBConnectionInstance == null) {
                nullLBConnectionInstance = (LoadBalancedConnection) Proxy.newProxyInstance(LoadBalancedConnection.class.getClassLoader(), INTERFACES_TO_PROXY, new NullLoadBalancedConnectionProxy());
            }
            loadBalancedConnection = nullLBConnectionInstance;
        }
        return loadBalancedConnection;
    }
}
