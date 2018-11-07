package com.mysql.jdbc;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.sql.SQLException;
import java.util.List;
import java.util.Properties;
import java.util.concurrent.Executor;

public abstract class MultiHostConnectionProxy implements InvocationHandler {
    private static Constructor<?> JDBC_4_MS_CONNECTION_CTOR = null;
    private static final String METHOD_ABORT = "abort";
    private static final String METHOD_ABORT_INTERNAL = "abortInternal";
    private static final String METHOD_CLOSE = "close";
    private static final String METHOD_EQUALS = "equals";
    private static final String METHOD_GET_AUTO_COMMIT = "getAutoCommit";
    private static final String METHOD_GET_CATALOG = "getCatalog";
    private static final String METHOD_GET_MULTI_HOST_SAFE_PROXY = "getMultiHostSafeProxy";
    private static final String METHOD_GET_SESSION_MAX_ROWS = "getSessionMaxRows";
    private static final String METHOD_GET_TRANSACTION_ISOLATION = "getTransactionIsolation";
    private static final String METHOD_HASH_CODE = "hashCode";
    private static final String METHOD_IS_CLOSED = "isClosed";
    boolean autoReconnect;
    boolean closedExplicitly;
    String closedReason;
    MySQLConnection currentConnection;
    List<String> hostList;
    boolean isClosed;
    protected Throwable lastExceptionDealtWith;
    Properties localProps;
    MySQLConnection proxyConnection;
    MySQLConnection thisAsConnection;

    class JdbcInterfaceProxy implements InvocationHandler {
        Object invokeOn = null;

        JdbcInterfaceProxy(Object toInvokeOn) {
            this.invokeOn = toInvokeOn;
        }

        public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
            if (MultiHostConnectionProxy.METHOD_EQUALS.equals(method.getName())) {
                return Boolean.valueOf(args[0].equals(this));
            }
            Object obj;
            synchronized (MultiHostConnectionProxy.this) {
                obj = null;
                try {
                    obj = method.invoke(this.invokeOn, args);
                    obj = MultiHostConnectionProxy.this.proxyIfReturnTypeIsJdbcInterface(method.getReturnType(), obj);
                } catch (InvocationTargetException e) {
                    MultiHostConnectionProxy.this.dealWithInvocationException(e);
                }
            }
            return obj;
        }
    }

    abstract void doAbort(Executor executor) throws SQLException;

    abstract void doAbortInternal() throws SQLException;

    abstract void doClose() throws SQLException;

    abstract Object invokeMore(Object obj, Method method, Object[] objArr) throws Throwable;

    abstract boolean isMasterConnection();

    abstract void pickNewConnection() throws SQLException;

    abstract boolean shouldExceptionTriggerConnectionSwitch(Throwable th);

    static {
        if (Util.isJdbc4()) {
            try {
                JDBC_4_MS_CONNECTION_CTOR = Class.forName("com.mysql.jdbc.JDBC4MultiHostMySQLConnection").getConstructor(new Class[]{MultiHostConnectionProxy.class});
            } catch (SecurityException e) {
                throw new RuntimeException(e);
            } catch (NoSuchMethodException e2) {
                throw new RuntimeException(e2);
            } catch (ClassNotFoundException e3) {
                throw new RuntimeException(e3);
            }
        }
    }

    MultiHostConnectionProxy() throws SQLException {
        this.autoReconnect = false;
        this.thisAsConnection = null;
        this.proxyConnection = null;
        this.currentConnection = null;
        this.isClosed = false;
        this.closedExplicitly = false;
        this.closedReason = null;
        this.lastExceptionDealtWith = null;
        this.thisAsConnection = getNewWrapperForThisAsConnection();
    }

    MultiHostConnectionProxy(List<String> hosts, Properties props) throws SQLException {
        this();
        initializeHostsSpecs(hosts, props);
    }

    int initializeHostsSpecs(List<String> hosts, Properties props) {
        boolean z;
        int numHosts;
        int i = 0;
        if (!"true".equalsIgnoreCase(props.getProperty("autoReconnect"))) {
            if (!"true".equalsIgnoreCase(props.getProperty("autoReconnectForPools"))) {
                z = false;
                this.autoReconnect = z;
                this.hostList = hosts;
                numHosts = this.hostList.size();
                this.localProps = (Properties) props.clone();
                this.localProps.remove(NonRegisteringDriver.HOST_PROPERTY_KEY);
                this.localProps.remove(NonRegisteringDriver.PORT_PROPERTY_KEY);
                while (i < numHosts) {
                    Properties properties = this.localProps;
                    StringBuilder stringBuilder = new StringBuilder();
                    stringBuilder.append("HOST.");
                    stringBuilder.append(i + 1);
                    properties.remove(stringBuilder.toString());
                    properties = this.localProps;
                    stringBuilder = new StringBuilder();
                    stringBuilder.append("PORT.");
                    stringBuilder.append(i + 1);
                    properties.remove(stringBuilder.toString());
                    i++;
                }
                this.localProps.remove(NonRegisteringDriver.NUM_HOSTS_PROPERTY_KEY);
                this.localProps.setProperty("useLocalSessionState", "true");
                return numHosts;
            }
        }
        z = true;
        this.autoReconnect = z;
        this.hostList = hosts;
        numHosts = this.hostList.size();
        this.localProps = (Properties) props.clone();
        this.localProps.remove(NonRegisteringDriver.HOST_PROPERTY_KEY);
        this.localProps.remove(NonRegisteringDriver.PORT_PROPERTY_KEY);
        while (i < numHosts) {
            Properties properties2 = this.localProps;
            StringBuilder stringBuilder2 = new StringBuilder();
            stringBuilder2.append("HOST.");
            stringBuilder2.append(i + 1);
            properties2.remove(stringBuilder2.toString());
            properties2 = this.localProps;
            stringBuilder2 = new StringBuilder();
            stringBuilder2.append("PORT.");
            stringBuilder2.append(i + 1);
            properties2.remove(stringBuilder2.toString());
            i++;
        }
        this.localProps.remove(NonRegisteringDriver.NUM_HOSTS_PROPERTY_KEY);
        this.localProps.setProperty("useLocalSessionState", "true");
        return numHosts;
    }

    MySQLConnection getNewWrapperForThisAsConnection() throws SQLException {
        if (!Util.isJdbc4()) {
            if (JDBC_4_MS_CONNECTION_CTOR == null) {
                return new MultiHostMySQLConnection(this);
            }
        }
        return (MySQLConnection) Util.handleNewInstance(JDBC_4_MS_CONNECTION_CTOR, new Object[]{this}, null);
    }

    protected MySQLConnection getProxy() {
        return this.proxyConnection != null ? this.proxyConnection : this.thisAsConnection;
    }

    protected final void setProxy(MySQLConnection proxyConn) {
        this.proxyConnection = proxyConn;
        propagateProxyDown(proxyConn);
    }

    protected void propagateProxyDown(MySQLConnection proxyConn) {
        this.currentConnection.setProxy(proxyConn);
    }

    Object proxyIfReturnTypeIsJdbcInterface(Class<?> returnType, Object toProxy) {
        if (toProxy == null || !Util.isJdbcInterface(returnType)) {
            return toProxy;
        }
        Class<?> toProxyClass = toProxy.getClass();
        return Proxy.newProxyInstance(toProxyClass.getClassLoader(), Util.getImplementedInterfaces(toProxyClass), getNewJdbcInterfaceProxy(toProxy));
    }

    InvocationHandler getNewJdbcInterfaceProxy(Object toProxy) {
        return new JdbcInterfaceProxy(toProxy);
    }

    void dealWithInvocationException(InvocationTargetException e) throws SQLException, Throwable, InvocationTargetException {
        Throwable t = e.getTargetException();
        if (t != null) {
            if (this.lastExceptionDealtWith != t && shouldExceptionTriggerConnectionSwitch(t)) {
                invalidateCurrentConnection();
                pickNewConnection();
                this.lastExceptionDealtWith = t;
            }
            throw t;
        }
        throw e;
    }

    synchronized void invalidateCurrentConnection() throws SQLException {
        invalidateConnection(this.currentConnection);
    }

    synchronized void invalidateConnection(MySQLConnection conn) throws SQLException {
        if (conn != null) {
            try {
                if (!conn.isClosed()) {
                    conn.realClose(true, conn.getAutoCommit() ^ true, true, null);
                }
            } catch (SQLException e) {
            }
        }
    }

    synchronized ConnectionImpl createConnectionForHost(String hostPortSpec) throws SQLException {
        ConnectionImpl conn;
        Properties connProps = (Properties) this.localProps.clone();
        String[] hostPortPair = NonRegisteringDriver.parseHostPortPair(hostPortSpec);
        String hostName = hostPortPair[null];
        String portNumber = hostPortPair[1];
        String dbName = connProps.getProperty(NonRegisteringDriver.DBNAME_PROPERTY_KEY);
        if (hostName == null) {
            throw new SQLException("Could not find a hostname to start a connection to");
        }
        if (portNumber == null) {
            portNumber = "3306";
        }
        connProps.setProperty(NonRegisteringDriver.HOST_PROPERTY_KEY, hostName);
        connProps.setProperty(NonRegisteringDriver.PORT_PROPERTY_KEY, portNumber);
        connProps.setProperty("HOST.1", hostName);
        connProps.setProperty("PORT.1", portNumber);
        connProps.setProperty(NonRegisteringDriver.NUM_HOSTS_PROPERTY_KEY, "1");
        connProps.setProperty("roundRobinLoadBalance", "false");
        int parseInt = Integer.parseInt(portNumber);
        StringBuilder stringBuilder = new StringBuilder();
        stringBuilder.append("jdbc:mysql://");
        stringBuilder.append(hostName);
        stringBuilder.append(":");
        stringBuilder.append(portNumber);
        stringBuilder.append("/");
        conn = (ConnectionImpl) ConnectionImpl.getInstance(hostName, parseInt, connProps, dbName, stringBuilder.toString());
        conn.setProxy(getProxy());
        return conn;
    }

    static void syncSessionState(Connection source, Connection target) throws SQLException {
        if (source != null) {
            if (target != null) {
                syncSessionState(source, target, source.isReadOnly());
            }
        }
    }

    static void syncSessionState(Connection source, Connection target, boolean readOnly) throws SQLException {
        if (target != null) {
            target.setReadOnly(readOnly);
        }
        if (source != null) {
            if (target != null) {
                target.setAutoCommit(source.getAutoCommit());
                target.setCatalog(source.getCatalog());
                target.setTransactionIsolation(source.getTransactionIsolation());
                target.setSessionMaxRows(source.getSessionMaxRows());
            }
        }
    }

    public synchronized Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
        int i$;
        int len$;
        String methodName = method.getName();
        if (METHOD_GET_MULTI_HOST_SAFE_PROXY.equals(methodName)) {
            return this.thisAsConnection;
        }
        i$ = 0;
        if (METHOD_EQUALS.equals(methodName)) {
            return Boolean.valueOf(args[0].equals(this));
        } else if (METHOD_HASH_CODE.equals(methodName)) {
            return Integer.valueOf(hashCode());
        } else if (METHOD_CLOSE.equals(methodName)) {
            doClose();
            this.isClosed = true;
            this.closedReason = "Connection explicitly closed.";
            this.closedExplicitly = true;
            return null;
        } else if (METHOD_ABORT_INTERNAL.equals(methodName)) {
            doAbortInternal();
            this.currentConnection.abortInternal();
            this.isClosed = true;
            this.closedReason = "Connection explicitly closed.";
            return null;
        } else if (METHOD_ABORT.equals(methodName) && args.length == 1) {
            doAbort((Executor) args[0]);
            this.isClosed = true;
            this.closedReason = "Connection explicitly closed.";
            return null;
        } else if (METHOD_IS_CLOSED.equals(methodName)) {
            return Boolean.valueOf(this.isClosed);
        } else {
            try {
                return invokeMore(proxy, method, args);
            } catch (Throwable e) {
                throw (e.getCause() != null ? e.getCause() : e);
            } catch (Exception e2) {
                arr$ = method.getExceptionTypes();
                len$ = arr$.length;
                while (i$ < len$) {
                    Class<?>[] arr$;
                    if (arr$[i$].isAssignableFrom(e2.getClass())) {
                        throw e2;
                    }
                    i$++;
                }
                throw new IllegalStateException(e2.getMessage(), e2);
            }
        }
    }

    protected boolean allowedOnClosedConnection(Method method) {
        String methodName = method.getName();
        if (!(methodName.equals(METHOD_GET_AUTO_COMMIT) || methodName.equals(METHOD_GET_CATALOG) || methodName.equals(METHOD_GET_TRANSACTION_ISOLATION))) {
            if (!methodName.equals(METHOD_GET_SESSION_MAX_ROWS)) {
                return false;
            }
        }
        return true;
    }
}
