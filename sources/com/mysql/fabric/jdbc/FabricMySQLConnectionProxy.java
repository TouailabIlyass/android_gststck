package com.mysql.fabric.jdbc;

import com.mysql.fabric.FabricConnection;
import com.mysql.fabric.Server;
import com.mysql.fabric.ServerGroup;
import com.mysql.fabric.ShardMapping;
import com.mysql.jdbc.Buffer;
import com.mysql.jdbc.CachedResultSetMetaData;
import com.mysql.jdbc.Connection;
import com.mysql.jdbc.ConnectionPropertiesImpl;
import com.mysql.jdbc.ExceptionInterceptor;
import com.mysql.jdbc.Extension;
import com.mysql.jdbc.Field;
import com.mysql.jdbc.MySQLConnection;
import com.mysql.jdbc.MysqlIO;
import com.mysql.jdbc.NonRegisteringDriver;
import com.mysql.jdbc.ReplicationConnection;
import com.mysql.jdbc.ReplicationConnectionGroup;
import com.mysql.jdbc.ReplicationConnectionGroupManager;
import com.mysql.jdbc.ReplicationConnectionProxy;
import com.mysql.jdbc.ResultSetInternalMethods;
import com.mysql.jdbc.SQLError;
import com.mysql.jdbc.ServerPreparedStatement;
import com.mysql.jdbc.SingleByteCharsetConverter;
import com.mysql.jdbc.StatementImpl;
import com.mysql.jdbc.StatementInterceptorV2;
import com.mysql.jdbc.Util;
import com.mysql.jdbc.log.Log;
import com.mysql.jdbc.log.LogFactory;
import com.mysql.jdbc.profiler.ProfilerEventHandler;
import java.sql.CallableStatement;
import java.sql.DatabaseMetaData;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.SQLWarning;
import java.sql.Savepoint;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.Set;
import java.util.TimeZone;
import java.util.Timer;
import java.util.concurrent.Executor;

public class FabricMySQLConnectionProxy extends ConnectionPropertiesImpl implements FabricMySQLConnection, FabricMySQLConnectionProperties {
    private static final Class<?> JDBC4_NON_TRANSIENT_CONN_EXCEPTION;
    private static final Set<String> replConnGroupLocks = Collections.synchronizedSet(new HashSet());
    private static final long serialVersionUID = 5845485979107347258L;
    protected boolean autoCommit = true;
    protected boolean closed = false;
    protected ReplicationConnection currentConnection;
    protected String database;
    protected FabricConnection fabricConnection;
    private String fabricPassword;
    private String fabricProtocol;
    private String fabricServerGroup;
    private String fabricShardKey;
    private String fabricShardTable;
    private String fabricUsername;
    protected String host;
    private Log log;
    protected String password;
    protected String port;
    protected Set<String> queryTables = new HashSet();
    protected boolean readOnly = false;
    private boolean reportErrors = false;
    protected Map<ServerGroup, ReplicationConnection> serverConnections = new HashMap();
    protected ServerGroup serverGroup;
    protected String serverGroupName;
    protected String shardKey;
    protected ShardMapping shardMapping;
    protected String shardTable;
    protected boolean transactionInProgress = false;
    protected int transactionIsolation = 4;
    protected String username;

    static {
        Class<?> clazz = null;
        try {
            if (Util.isJdbc4()) {
                clazz = Class.forName("com.mysql.jdbc.exceptions.jdbc4.MySQLNonTransientConnectionException");
            }
        } catch (ClassNotFoundException e) {
        }
        JDBC4_NON_TRANSIENT_CONN_EXCEPTION = clazz;
    }

    public FabricMySQLConnectionProxy(Properties props) throws SQLException {
        StringBuilder stringBuilder;
        this.fabricShardKey = props.getProperty(FabricMySQLDriver.FABRIC_SHARD_KEY_PROPERTY_KEY);
        this.fabricShardTable = props.getProperty(FabricMySQLDriver.FABRIC_SHARD_TABLE_PROPERTY_KEY);
        this.fabricServerGroup = props.getProperty(FabricMySQLDriver.FABRIC_SERVER_GROUP_PROPERTY_KEY);
        this.fabricProtocol = props.getProperty(FabricMySQLDriver.FABRIC_PROTOCOL_PROPERTY_KEY);
        this.fabricUsername = props.getProperty(FabricMySQLDriver.FABRIC_USERNAME_PROPERTY_KEY);
        this.fabricPassword = props.getProperty(FabricMySQLDriver.FABRIC_PASSWORD_PROPERTY_KEY);
        this.reportErrors = Boolean.valueOf(props.getProperty(FabricMySQLDriver.FABRIC_REPORT_ERRORS_PROPERTY_KEY)).booleanValue();
        props.remove(FabricMySQLDriver.FABRIC_SHARD_KEY_PROPERTY_KEY);
        props.remove(FabricMySQLDriver.FABRIC_SHARD_TABLE_PROPERTY_KEY);
        props.remove(FabricMySQLDriver.FABRIC_SERVER_GROUP_PROPERTY_KEY);
        props.remove(FabricMySQLDriver.FABRIC_PROTOCOL_PROPERTY_KEY);
        props.remove(FabricMySQLDriver.FABRIC_USERNAME_PROPERTY_KEY);
        props.remove(FabricMySQLDriver.FABRIC_PASSWORD_PROPERTY_KEY);
        props.remove(FabricMySQLDriver.FABRIC_REPORT_ERRORS_PROPERTY_KEY);
        this.host = props.getProperty(NonRegisteringDriver.HOST_PROPERTY_KEY);
        this.port = props.getProperty(NonRegisteringDriver.PORT_PROPERTY_KEY);
        this.username = props.getProperty(NonRegisteringDriver.USER_PROPERTY_KEY);
        this.password = props.getProperty(NonRegisteringDriver.PASSWORD_PROPERTY_KEY);
        this.database = props.getProperty(NonRegisteringDriver.DBNAME_PROPERTY_KEY);
        if (this.username == null) {
            this.username = "";
        }
        if (this.password == null) {
            this.password = "";
        }
        String exceptionInterceptors = props.getProperty("exceptionInterceptors");
        if (exceptionInterceptors != null) {
            if (!"null".equals("exceptionInterceptors")) {
                stringBuilder = new StringBuilder();
                stringBuilder.append(exceptionInterceptors);
                stringBuilder.append(",");
                exceptionInterceptors = stringBuilder.toString();
                stringBuilder = new StringBuilder();
                stringBuilder.append(exceptionInterceptors);
                stringBuilder.append("com.mysql.fabric.jdbc.ErrorReportingExceptionInterceptor");
                props.setProperty("exceptionInterceptors", stringBuilder.toString());
                initializeProperties(props);
                if (this.fabricServerGroup != null || this.fabricShardTable == null) {
                    String url = new StringBuilder();
                    url.append(this.fabricProtocol);
                    url.append("://");
                    url.append(this.host);
                    url.append(":");
                    url.append(this.port);
                    this.fabricConnection = new FabricConnection(url.toString(), this.fabricUsername, this.fabricPassword);
                    this.log = LogFactory.getLogger(getLogger(), "FabricMySQLConnectionProxy", null);
                    setShardTable(this.fabricShardTable);
                    setShardKey(this.fabricShardKey);
                    setServerGroupName(this.fabricServerGroup);
                }
                throw SQLError.createSQLException("Server group and shard table are mutually exclusive. Only one may be provided.", SQLError.SQL_STATE_CONNECTION_REJECTED, null, getExceptionInterceptor(), (Connection) this);
            }
        }
        exceptionInterceptors = "";
        stringBuilder = new StringBuilder();
        stringBuilder.append(exceptionInterceptors);
        stringBuilder.append("com.mysql.fabric.jdbc.ErrorReportingExceptionInterceptor");
        props.setProperty("exceptionInterceptors", stringBuilder.toString());
        initializeProperties(props);
        if (this.fabricServerGroup != null) {
        }
        try {
            String url2 = new StringBuilder();
            url2.append(this.fabricProtocol);
            url2.append("://");
            url2.append(this.host);
            url2.append(":");
            url2.append(this.port);
            this.fabricConnection = new FabricConnection(url2.toString(), this.fabricUsername, this.fabricPassword);
            this.log = LogFactory.getLogger(getLogger(), "FabricMySQLConnectionProxy", null);
            setShardTable(this.fabricShardTable);
            setShardKey(this.fabricShardKey);
            setServerGroupName(this.fabricServerGroup);
        } catch (Throwable ex) {
            throw SQLError.createSQLException("Unable to establish connection to the Fabric server", SQLError.SQL_STATE_CONNECTION_REJECTED, ex, getExceptionInterceptor(), (Connection) this);
        }
    }

    synchronized java.sql.SQLException interceptException(java.sql.SQLException r6, com.mysql.jdbc.Connection r7, java.lang.String r8, java.lang.String r9, java.lang.String r10) throws com.mysql.fabric.FabricCommunicationException {
        /* JADX: method processing error */
/*
Error: jadx.core.utils.exceptions.JadxRuntimeException: Exception block dominator not found, method:com.mysql.fabric.jdbc.FabricMySQLConnectionProxy.interceptException(java.sql.SQLException, com.mysql.jdbc.Connection, java.lang.String, java.lang.String, java.lang.String):java.sql.SQLException. bs: [B:32:0x0095, B:49:0x00be]
	at jadx.core.dex.visitors.regions.ProcessTryCatchRegions.searchTryCatchDominators(ProcessTryCatchRegions.java:86)
	at jadx.core.dex.visitors.regions.ProcessTryCatchRegions.process(ProcessTryCatchRegions.java:45)
	at jadx.core.dex.visitors.regions.RegionMakerVisitor.postProcessRegions(RegionMakerVisitor.java:63)
	at jadx.core.dex.visitors.regions.RegionMakerVisitor.visit(RegionMakerVisitor.java:58)
	at jadx.core.dex.visitors.DepthTraversal.visit(DepthTraversal.java:31)
	at jadx.core.dex.visitors.DepthTraversal.visit(DepthTraversal.java:17)
	at jadx.core.ProcessClass.process(ProcessClass.java:34)
	at jadx.core.ProcessClass.processDependencies(ProcessClass.java:56)
	at jadx.core.ProcessClass.process(ProcessClass.java:39)
	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:282)
	at jadx.api.JavaClass.decompile(JavaClass.java:62)
	at jadx.api.JadxDecompiler.lambda$appendSourcesSave$0(JadxDecompiler.java:200)
	at jadx.api.JadxDecompiler$$Lambda$8/1205406622.run(Unknown Source)
*/
        /*
        r5 = this;
        monitor-enter(r5);
        r0 = r6.getSQLState();	 Catch:{ all -> 0x00eb }
        r1 = 0;	 Catch:{ all -> 0x00eb }
        if (r0 == 0) goto L_0x0014;	 Catch:{ all -> 0x00eb }
    L_0x0008:
        r0 = r6.getSQLState();	 Catch:{ all -> 0x00eb }
        r2 = "08";	 Catch:{ all -> 0x00eb }
        r0 = r0.startsWith(r2);	 Catch:{ all -> 0x00eb }
        if (r0 != 0) goto L_0x0030;	 Catch:{ all -> 0x00eb }
    L_0x0014:
        r0 = com.mysql.jdbc.exceptions.MySQLNonTransientConnectionException.class;	 Catch:{ all -> 0x00eb }
        r2 = r6.getClass();	 Catch:{ all -> 0x00eb }
        r0 = r0.isAssignableFrom(r2);	 Catch:{ all -> 0x00eb }
        if (r0 != 0) goto L_0x0030;	 Catch:{ all -> 0x00eb }
    L_0x0020:
        r0 = JDBC4_NON_TRANSIENT_CONN_EXCEPTION;	 Catch:{ all -> 0x00eb }
        if (r0 == 0) goto L_0x0046;	 Catch:{ all -> 0x00eb }
    L_0x0024:
        r0 = JDBC4_NON_TRANSIENT_CONN_EXCEPTION;	 Catch:{ all -> 0x00eb }
        r2 = r6.getClass();	 Catch:{ all -> 0x00eb }
        r0 = r0.isAssignableFrom(r2);	 Catch:{ all -> 0x00eb }
        if (r0 == 0) goto L_0x0046;	 Catch:{ all -> 0x00eb }
    L_0x0030:
        r0 = r6.getCause();	 Catch:{ all -> 0x00eb }
        if (r0 == 0) goto L_0x0048;	 Catch:{ all -> 0x00eb }
    L_0x0036:
        r0 = com.mysql.fabric.FabricCommunicationException.class;	 Catch:{ all -> 0x00eb }
        r2 = r6.getCause();	 Catch:{ all -> 0x00eb }
        r2 = r2.getClass();	 Catch:{ all -> 0x00eb }
        r0 = r0.isAssignableFrom(r2);	 Catch:{ all -> 0x00eb }
        if (r0 == 0) goto L_0x0048;
    L_0x0046:
        monitor-exit(r5);
        return r1;
    L_0x0048:
        r0 = r5.serverGroup;	 Catch:{ all -> 0x00eb }
        r2 = new java.lang.StringBuilder;	 Catch:{ all -> 0x00eb }
        r2.<init>();	 Catch:{ all -> 0x00eb }
        r2.append(r9);	 Catch:{ all -> 0x00eb }
        r3 = ":";	 Catch:{ all -> 0x00eb }
        r2.append(r3);	 Catch:{ all -> 0x00eb }
        r2.append(r10);	 Catch:{ all -> 0x00eb }
        r2 = r2.toString();	 Catch:{ all -> 0x00eb }
        r0 = r0.getServer(r2);	 Catch:{ all -> 0x00eb }
        if (r0 != 0) goto L_0x0066;
    L_0x0064:
        monitor-exit(r5);
        return r1;
    L_0x0066:
        r2 = r5.reportErrors;	 Catch:{ all -> 0x00eb }
        if (r2 == 0) goto L_0x0078;	 Catch:{ all -> 0x00eb }
    L_0x006a:
        r2 = r5.fabricConnection;	 Catch:{ all -> 0x00eb }
        r2 = r2.getClient();	 Catch:{ all -> 0x00eb }
        r3 = r6.toString();	 Catch:{ all -> 0x00eb }
        r4 = 1;	 Catch:{ all -> 0x00eb }
        r2.reportServerError(r0, r3, r4);	 Catch:{ all -> 0x00eb }
    L_0x0078:
        r2 = replConnGroupLocks;	 Catch:{ all -> 0x00eb }
        r3 = r5.serverGroup;	 Catch:{ all -> 0x00eb }
        r3 = r3.getName();	 Catch:{ all -> 0x00eb }
        r2 = r2.add(r3);	 Catch:{ all -> 0x00eb }
        if (r2 == 0) goto L_0x00e1;
    L_0x0086:
        r2 = r5.fabricConnection;	 Catch:{ SQLException -> 0x00bd }
        r2.refreshStatePassive();	 Catch:{ SQLException -> 0x00bd }
        r2 = r5.serverGroup;	 Catch:{ SQLException -> 0x00bd }
        r2 = r2.getName();	 Catch:{ SQLException -> 0x00bd }
        r5.setCurrentServerGroup(r2);	 Catch:{ SQLException -> 0x00bd }
        r2 = com.mysql.jdbc.ReplicationConnectionGroupManager.getConnectionGroup(r8);	 Catch:{ SQLException -> 0x00ac }
        r5.syncGroupServersToReplicationConnectionGroup(r2);	 Catch:{ SQLException -> 0x00ac }
        r2 = replConnGroupLocks;	 Catch:{ all -> 0x00eb }
        r3 = r5.serverGroup;	 Catch:{ all -> 0x00eb }
        r3 = r3.getName();	 Catch:{ all -> 0x00eb }
        r2.remove(r3);	 Catch:{ all -> 0x00eb }
        monitor-exit(r5);
        return r1;
    L_0x00ac:
        r1 = move-exception;
        r2 = replConnGroupLocks;	 Catch:{ all -> 0x00eb }
        r3 = r5.serverGroup;	 Catch:{ all -> 0x00eb }
        r3 = r3.getName();	 Catch:{ all -> 0x00eb }
        r2.remove(r3);	 Catch:{ all -> 0x00eb }
        monitor-exit(r5);
        return r1;
    L_0x00bb:
        r1 = move-exception;
        goto L_0x00d3;
    L_0x00bd:
        r2 = move-exception;
        r3 = "Unable to refresh Fabric state. Failover impossible";	 Catch:{ all -> 0x00bb }
        r4 = "08006";	 Catch:{ all -> 0x00bb }
        r1 = com.mysql.jdbc.SQLError.createSQLException(r3, r4, r2, r1);	 Catch:{ all -> 0x00bb }
        r3 = replConnGroupLocks;	 Catch:{ all -> 0x00eb }
        r4 = r5.serverGroup;	 Catch:{ all -> 0x00eb }
        r4 = r4.getName();	 Catch:{ all -> 0x00eb }
        r3.remove(r4);	 Catch:{ all -> 0x00eb }
        monitor-exit(r5);
        return r1;
        r2 = r5;
        r3 = replConnGroupLocks;	 Catch:{ all -> 0x00eb }
        r4 = r2.serverGroup;	 Catch:{ all -> 0x00eb }
        r4 = r4.getName();	 Catch:{ all -> 0x00eb }
        r3.remove(r4);	 Catch:{ all -> 0x00eb }
        throw r1;	 Catch:{ all -> 0x00eb }
    L_0x00e1:
        r2 = "Fabric state syncing already in progress in another thread.";	 Catch:{ all -> 0x00eb }
        r3 = "08006";	 Catch:{ all -> 0x00eb }
        r1 = com.mysql.jdbc.SQLError.createSQLException(r2, r3, r6, r1);	 Catch:{ all -> 0x00eb }
        monitor-exit(r5);
        return r1;
    L_0x00eb:
        r6 = move-exception;
        monitor-exit(r5);
        throw r6;
        */
        throw new UnsupportedOperationException("Method not decompiled: com.mysql.fabric.jdbc.FabricMySQLConnectionProxy.interceptException(java.sql.SQLException, com.mysql.jdbc.Connection, java.lang.String, java.lang.String, java.lang.String):java.sql.SQLException");
    }

    private void refreshStateIfNecessary() throws SQLException {
        if (this.fabricConnection.isStateExpired()) {
            this.fabricConnection.refreshStatePassive();
            if (this.serverGroup != null) {
                setCurrentServerGroup(this.serverGroup.getName());
            }
        }
    }

    public void setShardKey(String shardKey) throws SQLException {
        ensureNoTransactionInProgress();
        this.currentConnection = null;
        if (shardKey != null) {
            if (this.serverGroupName != null) {
                throw SQLError.createSQLException("Shard key cannot be provided when server group is chosen directly.", SQLError.SQL_STATE_ILLEGAL_ARGUMENT, null, getExceptionInterceptor(), (Connection) this);
            } else if (this.shardTable == null) {
                throw SQLError.createSQLException("Shard key cannot be provided without a shard table.", SQLError.SQL_STATE_ILLEGAL_ARGUMENT, null, getExceptionInterceptor(), (Connection) this);
            } else {
                setCurrentServerGroup(this.shardMapping.getGroupNameForKey(shardKey));
            }
        } else if (this.shardTable != null) {
            setCurrentServerGroup(this.shardMapping.getGlobalGroupName());
        }
        this.shardKey = shardKey;
    }

    public String getShardKey() {
        return this.shardKey;
    }

    public void setShardTable(String shardTable) throws SQLException {
        ensureNoTransactionInProgress();
        this.currentConnection = null;
        if (this.serverGroupName != null) {
            throw SQLError.createSQLException("Server group and shard table are mutually exclusive. Only one may be provided.", SQLError.SQL_STATE_ILLEGAL_ARGUMENT, null, getExceptionInterceptor(), (Connection) this);
        }
        this.shardKey = null;
        this.serverGroup = null;
        this.shardTable = shardTable;
        if (shardTable == null) {
            this.shardMapping = null;
            return;
        }
        String table = shardTable;
        String db = this.database;
        if (shardTable.contains(".")) {
            String[] pair = shardTable.split("\\.");
            db = pair[0];
            table = pair[1];
        }
        this.shardMapping = this.fabricConnection.getShardMapping(db, table);
        if (this.shardMapping == null) {
            StringBuilder stringBuilder = new StringBuilder();
            stringBuilder.append("Shard mapping not found for table `");
            stringBuilder.append(shardTable);
            stringBuilder.append("'");
            throw SQLError.createSQLException(stringBuilder.toString(), SQLError.SQL_STATE_ILLEGAL_ARGUMENT, null, getExceptionInterceptor(), (Connection) this);
        }
        setCurrentServerGroup(this.shardMapping.getGlobalGroupName());
    }

    public String getShardTable() {
        return this.shardTable;
    }

    public void setServerGroupName(String serverGroupName) throws SQLException {
        ensureNoTransactionInProgress();
        this.currentConnection = null;
        if (serverGroupName != null) {
            setCurrentServerGroup(serverGroupName);
        }
        this.serverGroupName = serverGroupName;
    }

    public String getServerGroupName() {
        return this.serverGroupName;
    }

    public void clearServerSelectionCriteria() throws SQLException {
        ensureNoTransactionInProgress();
        this.shardTable = null;
        this.shardKey = null;
        this.serverGroupName = null;
        this.serverGroup = null;
        this.queryTables.clear();
        this.currentConnection = null;
    }

    public ServerGroup getCurrentServerGroup() {
        return this.serverGroup;
    }

    public void clearQueryTables() throws SQLException {
        ensureNoTransactionInProgress();
        this.currentConnection = null;
        this.queryTables.clear();
        setShardTable(null);
    }

    public void addQueryTable(String tableName) throws SQLException {
        ensureNoTransactionInProgress();
        this.currentConnection = null;
        if (this.shardMapping != null) {
            ShardMapping mappingForTableName = this.fabricConnection.getShardMapping(this.database, tableName);
            if (!(mappingForTableName == null || mappingForTableName.equals(this.shardMapping))) {
                throw SQLError.createSQLException("Cross-shard query not allowed", SQLError.SQL_STATE_ILLEGAL_ARGUMENT, null, getExceptionInterceptor(), (Connection) this);
            }
        } else if (this.fabricConnection.getShardMapping(this.database, tableName) != null) {
            setShardTable(tableName);
        }
        this.queryTables.add(tableName);
    }

    public Set<String> getQueryTables() {
        return this.queryTables;
    }

    protected void setCurrentServerGroup(String serverGroupName) throws SQLException {
        this.serverGroup = this.fabricConnection.getServerGroup(serverGroupName);
        if (this.serverGroup == null) {
            StringBuilder stringBuilder = new StringBuilder();
            stringBuilder.append("Cannot find server group: `");
            stringBuilder.append(serverGroupName);
            stringBuilder.append("'");
            throw SQLError.createSQLException(stringBuilder.toString(), SQLError.SQL_STATE_ILLEGAL_ARGUMENT, null, getExceptionInterceptor(), (Connection) this);
        }
        ReplicationConnectionGroup replConnGroup = ReplicationConnectionGroupManager.getConnectionGroup(serverGroupName);
        if (replConnGroup != null && replConnGroupLocks.add(this.serverGroup.getName())) {
            try {
                syncGroupServersToReplicationConnectionGroup(replConnGroup);
            } finally {
                replConnGroupLocks.remove(this.serverGroup.getName());
            }
        }
    }

    protected MySQLConnection getActiveMySQLConnectionChecked() throws SQLException {
        return (MySQLConnection) ((ReplicationConnection) getActiveConnection()).getCurrentConnection();
    }

    public MySQLConnection getActiveMySQLConnection() {
        try {
            return getActiveMySQLConnectionChecked();
        } catch (SQLException ex) {
            throw new IllegalStateException("Unable to determine active connection", ex);
        }
    }

    protected Connection getActiveConnectionPassive() {
        try {
            return getActiveConnection();
        } catch (SQLException ex) {
            throw new IllegalStateException("Unable to determine active connection", ex);
        }
    }

    private void syncGroupServersToReplicationConnectionGroup(ReplicationConnectionGroup replConnGroup) throws SQLException {
        String currentMasterString = null;
        if (replConnGroup.getMasterHosts().size() == 1) {
            currentMasterString = (String) replConnGroup.getMasterHosts().iterator().next();
        }
        if (currentMasterString != null && (this.serverGroup.getMaster() == null || !currentMasterString.equals(this.serverGroup.getMaster().getHostPortString()))) {
            try {
                replConnGroup.removeMasterHost(currentMasterString, false);
            } catch (SQLException ex) {
                Log log = getLog();
                StringBuilder stringBuilder = new StringBuilder();
                stringBuilder.append("Unable to remove master: ");
                stringBuilder.append(currentMasterString);
                log.logWarn(stringBuilder.toString(), ex);
            }
        }
        Server newMaster = this.serverGroup.getMaster();
        if (newMaster != null && replConnGroup.getMasterHosts().size() == 0) {
            log = getLog();
            stringBuilder = new StringBuilder();
            stringBuilder.append("Changing master for group '");
            stringBuilder.append(replConnGroup.getGroupName());
            stringBuilder.append("' to: ");
            stringBuilder.append(newMaster);
            log.logInfo(stringBuilder.toString());
            try {
                if (!replConnGroup.getSlaveHosts().contains(newMaster.getHostPortString())) {
                    replConnGroup.addSlaveHost(newMaster.getHostPortString());
                }
                replConnGroup.promoteSlaveToMaster(newMaster.getHostPortString());
            } catch (Throwable ex2) {
                StringBuilder stringBuilder2 = new StringBuilder();
                stringBuilder2.append("Unable to promote new master '");
                stringBuilder2.append(newMaster.toString());
                stringBuilder2.append("'");
                throw SQLError.createSQLException(stringBuilder2.toString(), ex2.getSQLState(), ex2, null);
            }
        }
        for (Server s : this.serverGroup.getServers()) {
            if (s.isSlave()) {
                try {
                    replConnGroup.addSlaveHost(s.getHostPortString());
                } catch (SQLException ex3) {
                    Log log2 = getLog();
                    StringBuilder stringBuilder3 = new StringBuilder();
                    stringBuilder3.append("Unable to add slave: ");
                    stringBuilder3.append(s.toString());
                    log2.logWarn(stringBuilder3.toString(), ex3);
                }
            }
        }
        for (String hostPortString : replConnGroup.getSlaveHosts()) {
            Server fabServer = this.serverGroup.getServer(hostPortString);
            if (fabServer == null || !fabServer.isSlave()) {
                try {
                    replConnGroup.removeSlaveHost(hostPortString, true);
                } catch (SQLException ex4) {
                    Log log3 = getLog();
                    StringBuilder stringBuilder4 = new StringBuilder();
                    stringBuilder4.append("Unable to remove slave: ");
                    stringBuilder4.append(hostPortString);
                    log3.logWarn(stringBuilder4.toString(), ex4);
                }
            }
        }
    }

    protected Connection getActiveConnection() throws SQLException {
        if (!this.transactionInProgress) {
            refreshStateIfNecessary();
        }
        if (this.currentConnection != null) {
            return this.currentConnection;
        }
        if (getCurrentServerGroup() == null) {
            throw SQLError.createSQLException("No server group selected.", SQLError.SQL_STATE_CONNECTION_REJECTED, null, getExceptionInterceptor(), (Connection) this);
        }
        this.currentConnection = (ReplicationConnection) this.serverConnections.get(this.serverGroup);
        if (this.currentConnection != null) {
            return this.currentConnection;
        }
        List<String> masterHost = new ArrayList();
        List<String> slaveHosts = new ArrayList();
        for (Server s : this.serverGroup.getServers()) {
            if (s.isMaster()) {
                masterHost.add(s.getHostPortString());
            } else if (s.isSlave()) {
                slaveHosts.add(s.getHostPortString());
            }
        }
        Properties info = exposeAsProperties(null);
        ReplicationConnectionGroup replConnGroup = ReplicationConnectionGroupManager.getConnectionGroup(this.serverGroup.getName());
        if (replConnGroup != null && replConnGroupLocks.add(this.serverGroup.getName())) {
            try {
                syncGroupServersToReplicationConnectionGroup(replConnGroup);
            } finally {
                replConnGroupLocks.remove(this.serverGroup.getName());
            }
        }
        info.put("replicationConnectionGroup", this.serverGroup.getName());
        info.setProperty(NonRegisteringDriver.USER_PROPERTY_KEY, this.username);
        info.setProperty(NonRegisteringDriver.PASSWORD_PROPERTY_KEY, this.password);
        info.setProperty(NonRegisteringDriver.DBNAME_PROPERTY_KEY, getCatalog());
        StringBuilder stringBuilder = new StringBuilder();
        stringBuilder.append("fabricHaGroup:");
        stringBuilder.append(this.serverGroup.getName());
        info.setProperty("connectionAttributes", stringBuilder.toString());
        info.setProperty("retriesAllDown", "1");
        info.setProperty("allowMasterDownConnections", "true");
        info.setProperty("allowSlaveDownConnections", "true");
        info.setProperty("readFromMasterWhenNoSlaves", "true");
        this.currentConnection = ReplicationConnectionProxy.createProxyInstance(masterHost, info, slaveHosts, info);
        this.serverConnections.put(this.serverGroup, this.currentConnection);
        this.currentConnection.setProxy(this);
        this.currentConnection.setAutoCommit(this.autoCommit);
        this.currentConnection.setReadOnly(this.readOnly);
        this.currentConnection.setTransactionIsolation(this.transactionIsolation);
        return this.currentConnection;
    }

    private void ensureOpen() throws SQLException {
        if (this.closed) {
            throw SQLError.createSQLException("No operations allowed after connection closed.", SQLError.SQL_STATE_CONNECTION_NOT_OPEN, getExceptionInterceptor());
        }
    }

    private void ensureNoTransactionInProgress() throws SQLException {
        ensureOpen();
        if (this.transactionInProgress && !this.autoCommit) {
            throw SQLError.createSQLException("Not allow while a transaction is active.", SQLError.SQL_STATE_INVALID_TRANSACTION_STATE, getExceptionInterceptor());
        }
    }

    public void close() throws SQLException {
        this.closed = true;
        for (ReplicationConnection c : this.serverConnections.values()) {
            try {
                c.close();
            } catch (SQLException e) {
            }
        }
    }

    public boolean isClosed() {
        return this.closed;
    }

    public boolean isValid(int timeout) throws SQLException {
        return this.closed ^ 1;
    }

    public void setReadOnly(boolean readOnly) throws SQLException {
        this.readOnly = readOnly;
        for (ReplicationConnection conn : this.serverConnections.values()) {
            conn.setReadOnly(readOnly);
        }
    }

    public boolean isReadOnly() throws SQLException {
        return this.readOnly;
    }

    public boolean isReadOnly(boolean useSessionStatus) throws SQLException {
        return this.readOnly;
    }

    public void setCatalog(String catalog) throws SQLException {
        this.database = catalog;
        for (ReplicationConnection c : this.serverConnections.values()) {
            c.setCatalog(catalog);
        }
    }

    public String getCatalog() {
        return this.database;
    }

    public void rollback() throws SQLException {
        getActiveConnection().rollback();
        transactionCompleted();
    }

    public void rollback(Savepoint savepoint) throws SQLException {
        getActiveConnection().rollback();
        transactionCompleted();
    }

    public void commit() throws SQLException {
        getActiveConnection().commit();
        transactionCompleted();
    }

    public void setAutoCommit(boolean autoCommit) throws SQLException {
        this.autoCommit = autoCommit;
        for (ReplicationConnection c : this.serverConnections.values()) {
            c.setAutoCommit(this.autoCommit);
        }
    }

    public void transactionBegun() throws SQLException {
        if (!this.autoCommit) {
            this.transactionInProgress = true;
        }
    }

    public void transactionCompleted() throws SQLException {
        this.transactionInProgress = false;
        refreshStateIfNecessary();
    }

    public boolean getAutoCommit() {
        return this.autoCommit;
    }

    @Deprecated
    public MySQLConnection getLoadBalanceSafeProxy() {
        return getMultiHostSafeProxy();
    }

    public MySQLConnection getMultiHostSafeProxy() {
        return getActiveMySQLConnection();
    }

    public void setTransactionIsolation(int level) throws SQLException {
        this.transactionIsolation = level;
        for (ReplicationConnection c : this.serverConnections.values()) {
            c.setTransactionIsolation(level);
        }
    }

    public void setTypeMap(Map<String, Class<?>> map) throws SQLException {
        for (ReplicationConnection c : this.serverConnections.values()) {
            c.setTypeMap(map);
        }
    }

    public void setHoldability(int holdability) throws SQLException {
        for (ReplicationConnection c : this.serverConnections.values()) {
            c.setHoldability(holdability);
        }
    }

    public void setProxy(MySQLConnection proxy) {
    }

    public Savepoint setSavepoint() throws SQLException {
        return getActiveConnection().setSavepoint();
    }

    public Savepoint setSavepoint(String name) throws SQLException {
        this.transactionInProgress = true;
        return getActiveConnection().setSavepoint(name);
    }

    public void releaseSavepoint(Savepoint savepoint) {
    }

    public CallableStatement prepareCall(String sql) throws SQLException {
        transactionBegun();
        return getActiveConnection().prepareCall(sql);
    }

    public CallableStatement prepareCall(String sql, int resultSetType, int resultSetConcurrency) throws SQLException {
        transactionBegun();
        return getActiveConnection().prepareCall(sql, resultSetType, resultSetConcurrency);
    }

    public CallableStatement prepareCall(String sql, int resultSetType, int resultSetConcurrency, int resultSetHoldability) throws SQLException {
        transactionBegun();
        return getActiveConnection().prepareCall(sql, resultSetType, resultSetConcurrency, resultSetHoldability);
    }

    public PreparedStatement prepareStatement(String sql) throws SQLException {
        transactionBegun();
        return getActiveConnection().prepareStatement(sql);
    }

    public PreparedStatement prepareStatement(String sql, int autoGeneratedKeys) throws SQLException {
        transactionBegun();
        return getActiveConnection().prepareStatement(sql, autoGeneratedKeys);
    }

    public PreparedStatement prepareStatement(String sql, int[] columnIndexes) throws SQLException {
        transactionBegun();
        return getActiveConnection().prepareStatement(sql, columnIndexes);
    }

    public PreparedStatement prepareStatement(String sql, int resultSetType, int resultSetConcurrency) throws SQLException {
        transactionBegun();
        return getActiveConnection().prepareStatement(sql, resultSetType, resultSetConcurrency);
    }

    public PreparedStatement prepareStatement(String sql, int resultSetType, int resultSetConcurrency, int resultSetHoldability) throws SQLException {
        transactionBegun();
        return getActiveConnection().prepareStatement(sql, resultSetType, resultSetConcurrency, resultSetHoldability);
    }

    public PreparedStatement prepareStatement(String sql, String[] columnNames) throws SQLException {
        transactionBegun();
        return getActiveConnection().prepareStatement(sql, columnNames);
    }

    public PreparedStatement clientPrepareStatement(String sql) throws SQLException {
        transactionBegun();
        return getActiveConnection().clientPrepareStatement(sql);
    }

    public PreparedStatement clientPrepareStatement(String sql, int autoGenKeyIndex) throws SQLException {
        transactionBegun();
        return getActiveConnection().clientPrepareStatement(sql, autoGenKeyIndex);
    }

    public PreparedStatement clientPrepareStatement(String sql, int resultSetType, int resultSetConcurrency) throws SQLException {
        transactionBegun();
        return getActiveConnection().clientPrepareStatement(sql, resultSetType, resultSetConcurrency);
    }

    public PreparedStatement clientPrepareStatement(String sql, int[] autoGenKeyIndexes) throws SQLException {
        transactionBegun();
        return getActiveConnection().clientPrepareStatement(sql, autoGenKeyIndexes);
    }

    public PreparedStatement clientPrepareStatement(String sql, int resultSetType, int resultSetConcurrency, int resultSetHoldability) throws SQLException {
        transactionBegun();
        return getActiveConnection().clientPrepareStatement(sql, resultSetType, resultSetConcurrency, resultSetHoldability);
    }

    public PreparedStatement clientPrepareStatement(String sql, String[] autoGenKeyColNames) throws SQLException {
        transactionBegun();
        return getActiveConnection().clientPrepareStatement(sql, autoGenKeyColNames);
    }

    public PreparedStatement serverPrepareStatement(String sql) throws SQLException {
        transactionBegun();
        return getActiveConnection().serverPrepareStatement(sql);
    }

    public PreparedStatement serverPrepareStatement(String sql, int autoGenKeyIndex) throws SQLException {
        transactionBegun();
        return getActiveConnection().serverPrepareStatement(sql, autoGenKeyIndex);
    }

    public PreparedStatement serverPrepareStatement(String sql, int resultSetType, int resultSetConcurrency) throws SQLException {
        transactionBegun();
        return getActiveConnection().serverPrepareStatement(sql, resultSetType, resultSetConcurrency);
    }

    public PreparedStatement serverPrepareStatement(String sql, int resultSetType, int resultSetConcurrency, int resultSetHoldability) throws SQLException {
        transactionBegun();
        return getActiveConnection().serverPrepareStatement(sql, resultSetType, resultSetConcurrency, resultSetHoldability);
    }

    public PreparedStatement serverPrepareStatement(String sql, int[] autoGenKeyIndexes) throws SQLException {
        transactionBegun();
        return getActiveConnection().serverPrepareStatement(sql, autoGenKeyIndexes);
    }

    public PreparedStatement serverPrepareStatement(String sql, String[] autoGenKeyColNames) throws SQLException {
        transactionBegun();
        return getActiveConnection().serverPrepareStatement(sql, autoGenKeyColNames);
    }

    public Statement createStatement() throws SQLException {
        transactionBegun();
        return getActiveConnection().createStatement();
    }

    public Statement createStatement(int resultSetType, int resultSetConcurrency) throws SQLException {
        transactionBegun();
        return getActiveConnection().createStatement(resultSetType, resultSetConcurrency);
    }

    public Statement createStatement(int resultSetType, int resultSetConcurrency, int resultSetHoldability) throws SQLException {
        transactionBegun();
        return getActiveConnection().createStatement(resultSetType, resultSetConcurrency, resultSetHoldability);
    }

    public ResultSetInternalMethods execSQL(StatementImpl callingStatement, String sql, int maxRows, Buffer packet, int resultSetType, int resultSetConcurrency, boolean streamResults, String catalog, Field[] cachedMetadata) throws SQLException {
        return getActiveMySQLConnectionChecked().execSQL(callingStatement, sql, maxRows, packet, resultSetType, resultSetConcurrency, streamResults, catalog, cachedMetadata);
    }

    public ResultSetInternalMethods execSQL(StatementImpl callingStatement, String sql, int maxRows, Buffer packet, int resultSetType, int resultSetConcurrency, boolean streamResults, String catalog, Field[] cachedMetadata, boolean isBatch) throws SQLException {
        return getActiveMySQLConnectionChecked().execSQL(callingStatement, sql, maxRows, packet, resultSetType, resultSetConcurrency, streamResults, catalog, cachedMetadata, isBatch);
    }

    public String extractSqlFromPacket(String possibleSqlQuery, Buffer queryPacket, int endOfQueryPacketPosition) throws SQLException {
        return getActiveMySQLConnectionChecked().extractSqlFromPacket(possibleSqlQuery, queryPacket, endOfQueryPacketPosition);
    }

    public StringBuilder generateConnectionCommentBlock(StringBuilder buf) {
        return getActiveMySQLConnection().generateConnectionCommentBlock(buf);
    }

    public MysqlIO getIO() throws SQLException {
        return getActiveMySQLConnectionChecked().getIO();
    }

    public Calendar getCalendarInstanceForSessionOrNew() {
        return getActiveMySQLConnection().getCalendarInstanceForSessionOrNew();
    }

    @Deprecated
    public String getServerCharacterEncoding() {
        return getServerCharset();
    }

    public String getServerCharset() {
        return getActiveMySQLConnection().getServerCharset();
    }

    public TimeZone getServerTimezoneTZ() {
        return getActiveMySQLConnection().getServerTimezoneTZ();
    }

    public boolean versionMeetsMinimum(int major, int minor, int subminor) throws SQLException {
        return getActiveConnection().versionMeetsMinimum(major, minor, subminor);
    }

    public boolean supportsIsolationLevel() {
        return getActiveConnectionPassive().supportsIsolationLevel();
    }

    public boolean supportsQuotedIdentifiers() {
        return getActiveConnectionPassive().supportsQuotedIdentifiers();
    }

    public DatabaseMetaData getMetaData() throws SQLException {
        return getActiveConnection().getMetaData();
    }

    public String getCharacterSetMetadata() {
        return getActiveMySQLConnection().getCharacterSetMetadata();
    }

    public Statement getMetadataSafeStatement() throws SQLException {
        return getActiveMySQLConnectionChecked().getMetadataSafeStatement();
    }

    public boolean isWrapperFor(Class<?> cls) {
        return false;
    }

    public <T> T unwrap(Class<T> cls) {
        return null;
    }

    public void unSafeStatementInterceptors() throws SQLException {
    }

    public boolean supportsTransactions() {
        return true;
    }

    public boolean isRunningOnJDK13() {
        return false;
    }

    public void createNewIO(boolean isForReconnect) throws SQLException {
        throw SQLError.createSQLFeatureNotSupportedException();
    }

    public void dumpTestcaseQuery(String query) {
    }

    public void abortInternal() throws SQLException {
    }

    public boolean isServerLocal() throws SQLException {
        return false;
    }

    public void shutdownServer() throws SQLException {
        throw SQLError.createSQLFeatureNotSupportedException();
    }

    @Deprecated
    public void clearHasTriedMaster() {
    }

    @Deprecated
    public boolean hasTriedMaster() {
        return false;
    }

    public boolean isInGlobalTx() {
        return false;
    }

    public void setInGlobalTx(boolean flag) {
        throw new RuntimeException("Global transactions not supported.");
    }

    public void changeUser(String userName, String newPassword) throws SQLException {
        throw SQLError.createSQLException("User change not allowed.", getExceptionInterceptor());
    }

    public void setFabricShardKey(String value) {
        this.fabricShardKey = value;
    }

    public String getFabricShardKey() {
        return this.fabricShardKey;
    }

    public void setFabricShardTable(String value) {
        this.fabricShardTable = value;
    }

    public String getFabricShardTable() {
        return this.fabricShardTable;
    }

    public void setFabricServerGroup(String value) {
        this.fabricServerGroup = value;
    }

    public String getFabricServerGroup() {
        return this.fabricServerGroup;
    }

    public void setFabricProtocol(String value) {
        this.fabricProtocol = value;
    }

    public String getFabricProtocol() {
        return this.fabricProtocol;
    }

    public void setFabricUsername(String value) {
        this.fabricUsername = value;
    }

    public String getFabricUsername() {
        return this.fabricUsername;
    }

    public void setFabricPassword(String value) {
        this.fabricPassword = value;
    }

    public String getFabricPassword() {
        return this.fabricPassword;
    }

    public void setFabricReportErrors(boolean value) {
        this.reportErrors = value;
    }

    public boolean getFabricReportErrors() {
        return this.reportErrors;
    }

    public void setAllowLoadLocalInfile(boolean property) {
        super.setAllowLoadLocalInfile(property);
        for (ReplicationConnection cp : this.serverConnections.values()) {
            cp.setAllowLoadLocalInfile(property);
        }
    }

    public void setAllowMultiQueries(boolean property) {
        super.setAllowMultiQueries(property);
        for (ReplicationConnection cp : this.serverConnections.values()) {
            cp.setAllowMultiQueries(property);
        }
    }

    public void setAllowNanAndInf(boolean flag) {
        super.setAllowNanAndInf(flag);
        for (ReplicationConnection cp : this.serverConnections.values()) {
            cp.setAllowNanAndInf(flag);
        }
    }

    public void setAllowUrlInLocalInfile(boolean flag) {
        super.setAllowUrlInLocalInfile(flag);
        for (ReplicationConnection cp : this.serverConnections.values()) {
            cp.setAllowUrlInLocalInfile(flag);
        }
    }

    public void setAlwaysSendSetIsolation(boolean flag) {
        super.setAlwaysSendSetIsolation(flag);
        for (ReplicationConnection cp : this.serverConnections.values()) {
            cp.setAlwaysSendSetIsolation(flag);
        }
    }

    public void setAutoDeserialize(boolean flag) {
        super.setAutoDeserialize(flag);
        for (ReplicationConnection cp : this.serverConnections.values()) {
            cp.setAutoDeserialize(flag);
        }
    }

    public void setAutoGenerateTestcaseScript(boolean flag) {
        super.setAutoGenerateTestcaseScript(flag);
        for (ReplicationConnection cp : this.serverConnections.values()) {
            cp.setAutoGenerateTestcaseScript(flag);
        }
    }

    public void setAutoReconnect(boolean flag) {
        super.setAutoReconnect(flag);
        for (ReplicationConnection cp : this.serverConnections.values()) {
            cp.setAutoReconnect(flag);
        }
    }

    public void setAutoReconnectForConnectionPools(boolean property) {
        super.setAutoReconnectForConnectionPools(property);
        for (ReplicationConnection cp : this.serverConnections.values()) {
            cp.setAutoReconnectForConnectionPools(property);
        }
    }

    public void setAutoReconnectForPools(boolean flag) {
        super.setAutoReconnectForPools(flag);
        for (ReplicationConnection cp : this.serverConnections.values()) {
            cp.setAutoReconnectForPools(flag);
        }
    }

    public void setBlobSendChunkSize(String value) throws SQLException {
        super.setBlobSendChunkSize(value);
        for (ReplicationConnection cp : this.serverConnections.values()) {
            cp.setBlobSendChunkSize(value);
        }
    }

    public void setCacheCallableStatements(boolean flag) {
        super.setCacheCallableStatements(flag);
        for (ReplicationConnection cp : this.serverConnections.values()) {
            cp.setCacheCallableStatements(flag);
        }
    }

    public void setCachePreparedStatements(boolean flag) {
        super.setCachePreparedStatements(flag);
        for (ReplicationConnection cp : this.serverConnections.values()) {
            cp.setCachePreparedStatements(flag);
        }
    }

    public void setCacheResultSetMetadata(boolean property) {
        super.setCacheResultSetMetadata(property);
        for (ReplicationConnection cp : this.serverConnections.values()) {
            cp.setCacheResultSetMetadata(property);
        }
    }

    public void setCacheServerConfiguration(boolean flag) {
        super.setCacheServerConfiguration(flag);
        for (ReplicationConnection cp : this.serverConnections.values()) {
            cp.setCacheServerConfiguration(flag);
        }
    }

    public void setCallableStatementCacheSize(int size) throws SQLException {
        super.setCallableStatementCacheSize(size);
        for (ReplicationConnection cp : this.serverConnections.values()) {
            cp.setCallableStatementCacheSize(size);
        }
    }

    public void setCapitalizeDBMDTypes(boolean property) {
        super.setCapitalizeDBMDTypes(property);
        for (ReplicationConnection cp : this.serverConnections.values()) {
            cp.setCapitalizeDBMDTypes(property);
        }
    }

    public void setCapitalizeTypeNames(boolean flag) {
        super.setCapitalizeTypeNames(flag);
        for (ReplicationConnection cp : this.serverConnections.values()) {
            cp.setCapitalizeTypeNames(flag);
        }
    }

    public void setCharacterEncoding(String encoding) {
        super.setCharacterEncoding(encoding);
        for (ReplicationConnection cp : this.serverConnections.values()) {
            cp.setCharacterEncoding(encoding);
        }
    }

    public void setCharacterSetResults(String characterSet) {
        super.setCharacterSetResults(characterSet);
        for (ReplicationConnection cp : this.serverConnections.values()) {
            cp.setCharacterSetResults(characterSet);
        }
    }

    public void setClobberStreamingResults(boolean flag) {
        super.setClobberStreamingResults(flag);
        for (ReplicationConnection cp : this.serverConnections.values()) {
            cp.setClobberStreamingResults(flag);
        }
    }

    public void setClobCharacterEncoding(String encoding) {
        super.setClobCharacterEncoding(encoding);
        for (ReplicationConnection cp : this.serverConnections.values()) {
            cp.setClobCharacterEncoding(encoding);
        }
    }

    public void setConnectionCollation(String collation) {
        super.setConnectionCollation(collation);
        for (ReplicationConnection cp : this.serverConnections.values()) {
            cp.setConnectionCollation(collation);
        }
    }

    public void setConnectTimeout(int timeoutMs) throws SQLException {
        super.setConnectTimeout(timeoutMs);
        for (ReplicationConnection cp : this.serverConnections.values()) {
            cp.setConnectTimeout(timeoutMs);
        }
    }

    public void setContinueBatchOnError(boolean property) {
        super.setContinueBatchOnError(property);
        for (ReplicationConnection cp : this.serverConnections.values()) {
            cp.setContinueBatchOnError(property);
        }
    }

    public void setCreateDatabaseIfNotExist(boolean flag) {
        super.setCreateDatabaseIfNotExist(flag);
        for (ReplicationConnection cp : this.serverConnections.values()) {
            cp.setCreateDatabaseIfNotExist(flag);
        }
    }

    public void setDefaultFetchSize(int n) throws SQLException {
        super.setDefaultFetchSize(n);
        for (ReplicationConnection cp : this.serverConnections.values()) {
            cp.setDefaultFetchSize(n);
        }
    }

    public void setDetectServerPreparedStmts(boolean property) {
        super.setDetectServerPreparedStmts(property);
        for (ReplicationConnection cp : this.serverConnections.values()) {
            cp.setDetectServerPreparedStmts(property);
        }
    }

    public void setDontTrackOpenResources(boolean flag) {
        super.setDontTrackOpenResources(flag);
        for (ReplicationConnection cp : this.serverConnections.values()) {
            cp.setDontTrackOpenResources(flag);
        }
    }

    public void setDumpQueriesOnException(boolean flag) {
        super.setDumpQueriesOnException(flag);
        for (ReplicationConnection cp : this.serverConnections.values()) {
            cp.setDumpQueriesOnException(flag);
        }
    }

    public void setDynamicCalendars(boolean flag) {
        super.setDynamicCalendars(flag);
        for (ReplicationConnection cp : this.serverConnections.values()) {
            cp.setDynamicCalendars(flag);
        }
    }

    public void setElideSetAutoCommits(boolean flag) {
        super.setElideSetAutoCommits(flag);
        for (ReplicationConnection cp : this.serverConnections.values()) {
            cp.setElideSetAutoCommits(flag);
        }
    }

    public void setEmptyStringsConvertToZero(boolean flag) {
        super.setEmptyStringsConvertToZero(flag);
        for (ReplicationConnection cp : this.serverConnections.values()) {
            cp.setEmptyStringsConvertToZero(flag);
        }
    }

    public void setEmulateLocators(boolean property) {
        super.setEmulateLocators(property);
        for (ReplicationConnection cp : this.serverConnections.values()) {
            cp.setEmulateLocators(property);
        }
    }

    public void setEmulateUnsupportedPstmts(boolean flag) {
        super.setEmulateUnsupportedPstmts(flag);
        for (ReplicationConnection cp : this.serverConnections.values()) {
            cp.setEmulateUnsupportedPstmts(flag);
        }
    }

    public void setEnablePacketDebug(boolean flag) {
        super.setEnablePacketDebug(flag);
        for (ReplicationConnection cp : this.serverConnections.values()) {
            cp.setEnablePacketDebug(flag);
        }
    }

    public void setEncoding(String property) {
        super.setEncoding(property);
        for (ReplicationConnection cp : this.serverConnections.values()) {
            cp.setEncoding(property);
        }
    }

    public void setExplainSlowQueries(boolean flag) {
        super.setExplainSlowQueries(flag);
        for (ReplicationConnection cp : this.serverConnections.values()) {
            cp.setExplainSlowQueries(flag);
        }
    }

    public void setFailOverReadOnly(boolean flag) {
        super.setFailOverReadOnly(flag);
        for (ReplicationConnection cp : this.serverConnections.values()) {
            cp.setFailOverReadOnly(flag);
        }
    }

    public void setGatherPerformanceMetrics(boolean flag) {
        super.setGatherPerformanceMetrics(flag);
        for (ReplicationConnection cp : this.serverConnections.values()) {
            cp.setGatherPerformanceMetrics(flag);
        }
    }

    public void setHoldResultsOpenOverStatementClose(boolean flag) {
        super.setHoldResultsOpenOverStatementClose(flag);
        for (ReplicationConnection cp : this.serverConnections.values()) {
            cp.setHoldResultsOpenOverStatementClose(flag);
        }
    }

    public void setIgnoreNonTxTables(boolean property) {
        super.setIgnoreNonTxTables(property);
        for (ReplicationConnection cp : this.serverConnections.values()) {
            cp.setIgnoreNonTxTables(property);
        }
    }

    public void setInitialTimeout(int property) throws SQLException {
        super.setInitialTimeout(property);
        for (ReplicationConnection cp : this.serverConnections.values()) {
            cp.setInitialTimeout(property);
        }
    }

    public void setIsInteractiveClient(boolean property) {
        super.setIsInteractiveClient(property);
        for (ReplicationConnection cp : this.serverConnections.values()) {
            cp.setIsInteractiveClient(property);
        }
    }

    public void setJdbcCompliantTruncation(boolean flag) {
        super.setJdbcCompliantTruncation(flag);
        for (ReplicationConnection cp : this.serverConnections.values()) {
            cp.setJdbcCompliantTruncation(flag);
        }
    }

    public void setLocatorFetchBufferSize(String value) throws SQLException {
        super.setLocatorFetchBufferSize(value);
        for (ReplicationConnection cp : this.serverConnections.values()) {
            cp.setLocatorFetchBufferSize(value);
        }
    }

    public void setLogger(String property) {
        super.setLogger(property);
        for (ReplicationConnection cp : this.serverConnections.values()) {
            cp.setLogger(property);
        }
    }

    public void setLoggerClassName(String className) {
        super.setLoggerClassName(className);
        for (ReplicationConnection cp : this.serverConnections.values()) {
            cp.setLoggerClassName(className);
        }
    }

    public void setLogSlowQueries(boolean flag) {
        super.setLogSlowQueries(flag);
        for (ReplicationConnection cp : this.serverConnections.values()) {
            cp.setLogSlowQueries(flag);
        }
    }

    public void setMaintainTimeStats(boolean flag) {
        super.setMaintainTimeStats(flag);
        for (ReplicationConnection cp : this.serverConnections.values()) {
            cp.setMaintainTimeStats(flag);
        }
    }

    public void setMaxQuerySizeToLog(int sizeInBytes) throws SQLException {
        super.setMaxQuerySizeToLog(sizeInBytes);
        for (ReplicationConnection cp : this.serverConnections.values()) {
            cp.setMaxQuerySizeToLog(sizeInBytes);
        }
    }

    public void setMaxReconnects(int property) throws SQLException {
        super.setMaxReconnects(property);
        for (ReplicationConnection cp : this.serverConnections.values()) {
            cp.setMaxReconnects(property);
        }
    }

    public void setMaxRows(int property) throws SQLException {
        super.setMaxRows(property);
        for (ReplicationConnection cp : this.serverConnections.values()) {
            cp.setMaxRows(property);
        }
    }

    public void setMetadataCacheSize(int value) throws SQLException {
        super.setMetadataCacheSize(value);
        for (ReplicationConnection cp : this.serverConnections.values()) {
            cp.setMetadataCacheSize(value);
        }
    }

    public void setNoDatetimeStringSync(boolean flag) {
        super.setNoDatetimeStringSync(flag);
        for (ReplicationConnection cp : this.serverConnections.values()) {
            cp.setNoDatetimeStringSync(flag);
        }
    }

    public void setNullCatalogMeansCurrent(boolean value) {
        super.setNullCatalogMeansCurrent(value);
        for (ReplicationConnection cp : this.serverConnections.values()) {
            cp.setNullCatalogMeansCurrent(value);
        }
    }

    public void setNullNamePatternMatchesAll(boolean value) {
        super.setNullNamePatternMatchesAll(value);
        for (ReplicationConnection cp : this.serverConnections.values()) {
            cp.setNullNamePatternMatchesAll(value);
        }
    }

    public void setPacketDebugBufferSize(int size) throws SQLException {
        super.setPacketDebugBufferSize(size);
        for (ReplicationConnection cp : this.serverConnections.values()) {
            cp.setPacketDebugBufferSize(size);
        }
    }

    public void setParanoid(boolean property) {
        super.setParanoid(property);
        for (ReplicationConnection cp : this.serverConnections.values()) {
            cp.setParanoid(property);
        }
    }

    public void setPedantic(boolean property) {
        super.setPedantic(property);
        for (ReplicationConnection cp : this.serverConnections.values()) {
            cp.setPedantic(property);
        }
    }

    public void setPreparedStatementCacheSize(int cacheSize) throws SQLException {
        super.setPreparedStatementCacheSize(cacheSize);
        for (ReplicationConnection cp : this.serverConnections.values()) {
            cp.setPreparedStatementCacheSize(cacheSize);
        }
    }

    public void setPreparedStatementCacheSqlLimit(int cacheSqlLimit) throws SQLException {
        super.setPreparedStatementCacheSqlLimit(cacheSqlLimit);
        for (ReplicationConnection cp : this.serverConnections.values()) {
            cp.setPreparedStatementCacheSqlLimit(cacheSqlLimit);
        }
    }

    public void setProfileSql(boolean property) {
        super.setProfileSql(property);
        for (ReplicationConnection cp : this.serverConnections.values()) {
            cp.setProfileSql(property);
        }
    }

    public void setProfileSQL(boolean flag) {
        super.setProfileSQL(flag);
        for (ReplicationConnection cp : this.serverConnections.values()) {
            cp.setProfileSQL(flag);
        }
    }

    public void setPropertiesTransform(String value) {
        super.setPropertiesTransform(value);
        for (ReplicationConnection cp : this.serverConnections.values()) {
            cp.setPropertiesTransform(value);
        }
    }

    public void setQueriesBeforeRetryMaster(int property) throws SQLException {
        super.setQueriesBeforeRetryMaster(property);
        for (ReplicationConnection cp : this.serverConnections.values()) {
            cp.setQueriesBeforeRetryMaster(property);
        }
    }

    public void setReconnectAtTxEnd(boolean property) {
        super.setReconnectAtTxEnd(property);
        for (ReplicationConnection cp : this.serverConnections.values()) {
            cp.setReconnectAtTxEnd(property);
        }
    }

    public void setRelaxAutoCommit(boolean property) {
        super.setRelaxAutoCommit(property);
        for (ReplicationConnection cp : this.serverConnections.values()) {
            cp.setRelaxAutoCommit(property);
        }
    }

    public void setReportMetricsIntervalMillis(int millis) throws SQLException {
        super.setReportMetricsIntervalMillis(millis);
        for (ReplicationConnection cp : this.serverConnections.values()) {
            cp.setReportMetricsIntervalMillis(millis);
        }
    }

    public void setRequireSSL(boolean property) {
        super.setRequireSSL(property);
        for (ReplicationConnection cp : this.serverConnections.values()) {
            cp.setRequireSSL(property);
        }
    }

    public void setRetainStatementAfterResultSetClose(boolean flag) {
        super.setRetainStatementAfterResultSetClose(flag);
        for (ReplicationConnection cp : this.serverConnections.values()) {
            cp.setRetainStatementAfterResultSetClose(flag);
        }
    }

    public void setRollbackOnPooledClose(boolean flag) {
        super.setRollbackOnPooledClose(flag);
        for (ReplicationConnection cp : this.serverConnections.values()) {
            cp.setRollbackOnPooledClose(flag);
        }
    }

    public void setRoundRobinLoadBalance(boolean flag) {
        super.setRoundRobinLoadBalance(flag);
        for (ReplicationConnection cp : this.serverConnections.values()) {
            cp.setRoundRobinLoadBalance(flag);
        }
    }

    public void setRunningCTS13(boolean flag) {
        super.setRunningCTS13(flag);
        for (ReplicationConnection cp : this.serverConnections.values()) {
            cp.setRunningCTS13(flag);
        }
    }

    public void setSecondsBeforeRetryMaster(int property) throws SQLException {
        super.setSecondsBeforeRetryMaster(property);
        for (ReplicationConnection cp : this.serverConnections.values()) {
            cp.setSecondsBeforeRetryMaster(property);
        }
    }

    public void setServerTimezone(String property) {
        super.setServerTimezone(property);
        for (ReplicationConnection cp : this.serverConnections.values()) {
            cp.setServerTimezone(property);
        }
    }

    public void setSessionVariables(String variables) {
        super.setSessionVariables(variables);
        for (ReplicationConnection cp : this.serverConnections.values()) {
            cp.setSessionVariables(variables);
        }
    }

    public void setSlowQueryThresholdMillis(int millis) throws SQLException {
        super.setSlowQueryThresholdMillis(millis);
        for (ReplicationConnection cp : this.serverConnections.values()) {
            cp.setSlowQueryThresholdMillis(millis);
        }
    }

    public void setSocketFactoryClassName(String property) {
        super.setSocketFactoryClassName(property);
        for (ReplicationConnection cp : this.serverConnections.values()) {
            cp.setSocketFactoryClassName(property);
        }
    }

    public void setSocketTimeout(int property) throws SQLException {
        super.setSocketTimeout(property);
        for (ReplicationConnection cp : this.serverConnections.values()) {
            cp.setSocketTimeout(property);
        }
    }

    public void setStrictFloatingPoint(boolean property) {
        super.setStrictFloatingPoint(property);
        for (ReplicationConnection cp : this.serverConnections.values()) {
            cp.setStrictFloatingPoint(property);
        }
    }

    public void setStrictUpdates(boolean property) {
        super.setStrictUpdates(property);
        for (ReplicationConnection cp : this.serverConnections.values()) {
            cp.setStrictUpdates(property);
        }
    }

    public void setTinyInt1isBit(boolean flag) {
        super.setTinyInt1isBit(flag);
        for (ReplicationConnection cp : this.serverConnections.values()) {
            cp.setTinyInt1isBit(flag);
        }
    }

    public void setTraceProtocol(boolean flag) {
        super.setTraceProtocol(flag);
        for (ReplicationConnection cp : this.serverConnections.values()) {
            cp.setTraceProtocol(flag);
        }
    }

    public void setTransformedBitIsBoolean(boolean flag) {
        super.setTransformedBitIsBoolean(flag);
        for (ReplicationConnection cp : this.serverConnections.values()) {
            cp.setTransformedBitIsBoolean(flag);
        }
    }

    public void setUseCompression(boolean property) {
        super.setUseCompression(property);
        for (ReplicationConnection cp : this.serverConnections.values()) {
            cp.setUseCompression(property);
        }
    }

    public void setUseFastIntParsing(boolean flag) {
        super.setUseFastIntParsing(flag);
        for (ReplicationConnection cp : this.serverConnections.values()) {
            cp.setUseFastIntParsing(flag);
        }
    }

    public void setUseHostsInPrivileges(boolean property) {
        super.setUseHostsInPrivileges(property);
        for (ReplicationConnection cp : this.serverConnections.values()) {
            cp.setUseHostsInPrivileges(property);
        }
    }

    public void setUseInformationSchema(boolean flag) {
        super.setUseInformationSchema(flag);
        for (ReplicationConnection cp : this.serverConnections.values()) {
            cp.setUseInformationSchema(flag);
        }
    }

    public void setUseLocalSessionState(boolean flag) {
        super.setUseLocalSessionState(flag);
        for (ReplicationConnection cp : this.serverConnections.values()) {
            cp.setUseLocalSessionState(flag);
        }
    }

    public void setUseOldUTF8Behavior(boolean flag) {
        super.setUseOldUTF8Behavior(flag);
        for (ReplicationConnection cp : this.serverConnections.values()) {
            cp.setUseOldUTF8Behavior(flag);
        }
    }

    public void setUseOnlyServerErrorMessages(boolean flag) {
        super.setUseOnlyServerErrorMessages(flag);
        for (ReplicationConnection cp : this.serverConnections.values()) {
            cp.setUseOnlyServerErrorMessages(flag);
        }
    }

    public void setUseReadAheadInput(boolean flag) {
        super.setUseReadAheadInput(flag);
        for (ReplicationConnection cp : this.serverConnections.values()) {
            cp.setUseReadAheadInput(flag);
        }
    }

    public void setUseServerPreparedStmts(boolean flag) {
        super.setUseServerPreparedStmts(flag);
        for (ReplicationConnection cp : this.serverConnections.values()) {
            cp.setUseServerPreparedStmts(flag);
        }
    }

    public void setUseSqlStateCodes(boolean flag) {
        super.setUseSqlStateCodes(flag);
        for (ReplicationConnection cp : this.serverConnections.values()) {
            cp.setUseSqlStateCodes(flag);
        }
    }

    public void setUseSSL(boolean property) {
        super.setUseSSL(property);
        for (ReplicationConnection cp : this.serverConnections.values()) {
            cp.setUseSSL(property);
        }
    }

    public void setUseStreamLengthsInPrepStmts(boolean property) {
        super.setUseStreamLengthsInPrepStmts(property);
        for (ReplicationConnection cp : this.serverConnections.values()) {
            cp.setUseStreamLengthsInPrepStmts(property);
        }
    }

    public void setUseTimezone(boolean property) {
        super.setUseTimezone(property);
        for (ReplicationConnection cp : this.serverConnections.values()) {
            cp.setUseTimezone(property);
        }
    }

    public void setUseUltraDevWorkAround(boolean property) {
        super.setUseUltraDevWorkAround(property);
        for (ReplicationConnection cp : this.serverConnections.values()) {
            cp.setUseUltraDevWorkAround(property);
        }
    }

    public void setUseUnbufferedInput(boolean flag) {
        super.setUseUnbufferedInput(flag);
        for (ReplicationConnection cp : this.serverConnections.values()) {
            cp.setUseUnbufferedInput(flag);
        }
    }

    public void setUseUnicode(boolean flag) {
        super.setUseUnicode(flag);
        for (ReplicationConnection cp : this.serverConnections.values()) {
            cp.setUseUnicode(flag);
        }
    }

    public void setUseUsageAdvisor(boolean useUsageAdvisorFlag) {
        super.setUseUsageAdvisor(useUsageAdvisorFlag);
        for (ReplicationConnection cp : this.serverConnections.values()) {
            cp.setUseUsageAdvisor(useUsageAdvisorFlag);
        }
    }

    public void setYearIsDateType(boolean flag) {
        super.setYearIsDateType(flag);
        for (ReplicationConnection cp : this.serverConnections.values()) {
            cp.setYearIsDateType(flag);
        }
    }

    public void setZeroDateTimeBehavior(String behavior) {
        super.setZeroDateTimeBehavior(behavior);
        for (ReplicationConnection cp : this.serverConnections.values()) {
            cp.setZeroDateTimeBehavior(behavior);
        }
    }

    public void setUseCursorFetch(boolean flag) {
        super.setUseCursorFetch(flag);
        for (ReplicationConnection cp : this.serverConnections.values()) {
            cp.setUseCursorFetch(flag);
        }
    }

    public void setOverrideSupportsIntegrityEnhancementFacility(boolean flag) {
        super.setOverrideSupportsIntegrityEnhancementFacility(flag);
        for (ReplicationConnection cp : this.serverConnections.values()) {
            cp.setOverrideSupportsIntegrityEnhancementFacility(flag);
        }
    }

    public void setNoTimezoneConversionForTimeType(boolean flag) {
        super.setNoTimezoneConversionForTimeType(flag);
        for (ReplicationConnection cp : this.serverConnections.values()) {
            cp.setNoTimezoneConversionForTimeType(flag);
        }
    }

    public void setUseJDBCCompliantTimezoneShift(boolean flag) {
        super.setUseJDBCCompliantTimezoneShift(flag);
        for (ReplicationConnection cp : this.serverConnections.values()) {
            cp.setUseJDBCCompliantTimezoneShift(flag);
        }
    }

    public void setAutoClosePStmtStreams(boolean flag) {
        super.setAutoClosePStmtStreams(flag);
        for (ReplicationConnection cp : this.serverConnections.values()) {
            cp.setAutoClosePStmtStreams(flag);
        }
    }

    public void setProcessEscapeCodesForPrepStmts(boolean flag) {
        super.setProcessEscapeCodesForPrepStmts(flag);
        for (ReplicationConnection cp : this.serverConnections.values()) {
            cp.setProcessEscapeCodesForPrepStmts(flag);
        }
    }

    public void setUseGmtMillisForDatetimes(boolean flag) {
        super.setUseGmtMillisForDatetimes(flag);
        for (ReplicationConnection cp : this.serverConnections.values()) {
            cp.setUseGmtMillisForDatetimes(flag);
        }
    }

    public void setDumpMetadataOnColumnNotFound(boolean flag) {
        super.setDumpMetadataOnColumnNotFound(flag);
        for (ReplicationConnection cp : this.serverConnections.values()) {
            cp.setDumpMetadataOnColumnNotFound(flag);
        }
    }

    public void setResourceId(String resourceId) {
        super.setResourceId(resourceId);
        for (ReplicationConnection cp : this.serverConnections.values()) {
            cp.setResourceId(resourceId);
        }
    }

    public void setRewriteBatchedStatements(boolean flag) {
        super.setRewriteBatchedStatements(flag);
        for (ReplicationConnection cp : this.serverConnections.values()) {
            cp.setRewriteBatchedStatements(flag);
        }
    }

    public void setJdbcCompliantTruncationForReads(boolean jdbcCompliantTruncationForReads) {
        super.setJdbcCompliantTruncationForReads(jdbcCompliantTruncationForReads);
        for (ReplicationConnection cp : this.serverConnections.values()) {
            cp.setJdbcCompliantTruncationForReads(jdbcCompliantTruncationForReads);
        }
    }

    public void setUseJvmCharsetConverters(boolean flag) {
        super.setUseJvmCharsetConverters(flag);
        for (ReplicationConnection cp : this.serverConnections.values()) {
            cp.setUseJvmCharsetConverters(flag);
        }
    }

    public void setPinGlobalTxToPhysicalConnection(boolean flag) {
        super.setPinGlobalTxToPhysicalConnection(flag);
        for (ReplicationConnection cp : this.serverConnections.values()) {
            cp.setPinGlobalTxToPhysicalConnection(flag);
        }
    }

    public void setGatherPerfMetrics(boolean flag) {
        super.setGatherPerfMetrics(flag);
        for (ReplicationConnection cp : this.serverConnections.values()) {
            cp.setGatherPerfMetrics(flag);
        }
    }

    public void setUltraDevHack(boolean flag) {
        super.setUltraDevHack(flag);
        for (ReplicationConnection cp : this.serverConnections.values()) {
            cp.setUltraDevHack(flag);
        }
    }

    public void setInteractiveClient(boolean property) {
        super.setInteractiveClient(property);
        for (ReplicationConnection cp : this.serverConnections.values()) {
            cp.setInteractiveClient(property);
        }
    }

    public void setSocketFactory(String name) {
        super.setSocketFactory(name);
        for (ReplicationConnection cp : this.serverConnections.values()) {
            cp.setSocketFactory(name);
        }
    }

    public void setUseServerPrepStmts(boolean flag) {
        super.setUseServerPrepStmts(flag);
        for (ReplicationConnection cp : this.serverConnections.values()) {
            cp.setUseServerPrepStmts(flag);
        }
    }

    public void setCacheCallableStmts(boolean flag) {
        super.setCacheCallableStmts(flag);
        for (ReplicationConnection cp : this.serverConnections.values()) {
            cp.setCacheCallableStmts(flag);
        }
    }

    public void setCachePrepStmts(boolean flag) {
        super.setCachePrepStmts(flag);
        for (ReplicationConnection cp : this.serverConnections.values()) {
            cp.setCachePrepStmts(flag);
        }
    }

    public void setCallableStmtCacheSize(int cacheSize) throws SQLException {
        super.setCallableStmtCacheSize(cacheSize);
        for (ReplicationConnection cp : this.serverConnections.values()) {
            cp.setCallableStmtCacheSize(cacheSize);
        }
    }

    public void setPrepStmtCacheSize(int cacheSize) throws SQLException {
        super.setPrepStmtCacheSize(cacheSize);
        for (ReplicationConnection cp : this.serverConnections.values()) {
            cp.setPrepStmtCacheSize(cacheSize);
        }
    }

    public void setPrepStmtCacheSqlLimit(int sqlLimit) throws SQLException {
        super.setPrepStmtCacheSqlLimit(sqlLimit);
        for (ReplicationConnection cp : this.serverConnections.values()) {
            cp.setPrepStmtCacheSqlLimit(sqlLimit);
        }
    }

    public void setNoAccessToProcedureBodies(boolean flag) {
        super.setNoAccessToProcedureBodies(flag);
        for (ReplicationConnection cp : this.serverConnections.values()) {
            cp.setNoAccessToProcedureBodies(flag);
        }
    }

    public void setUseOldAliasMetadataBehavior(boolean flag) {
        super.setUseOldAliasMetadataBehavior(flag);
        for (ReplicationConnection cp : this.serverConnections.values()) {
            cp.setUseOldAliasMetadataBehavior(flag);
        }
    }

    public void setClientCertificateKeyStorePassword(String value) {
        super.setClientCertificateKeyStorePassword(value);
        for (ReplicationConnection cp : this.serverConnections.values()) {
            cp.setClientCertificateKeyStorePassword(value);
        }
    }

    public void setClientCertificateKeyStoreType(String value) {
        super.setClientCertificateKeyStoreType(value);
        for (ReplicationConnection cp : this.serverConnections.values()) {
            cp.setClientCertificateKeyStoreType(value);
        }
    }

    public void setClientCertificateKeyStoreUrl(String value) {
        super.setClientCertificateKeyStoreUrl(value);
        for (ReplicationConnection cp : this.serverConnections.values()) {
            cp.setClientCertificateKeyStoreUrl(value);
        }
    }

    public void setTrustCertificateKeyStorePassword(String value) {
        super.setTrustCertificateKeyStorePassword(value);
        for (ReplicationConnection cp : this.serverConnections.values()) {
            cp.setTrustCertificateKeyStorePassword(value);
        }
    }

    public void setTrustCertificateKeyStoreType(String value) {
        super.setTrustCertificateKeyStoreType(value);
        for (ReplicationConnection cp : this.serverConnections.values()) {
            cp.setTrustCertificateKeyStoreType(value);
        }
    }

    public void setTrustCertificateKeyStoreUrl(String value) {
        super.setTrustCertificateKeyStoreUrl(value);
        for (ReplicationConnection cp : this.serverConnections.values()) {
            cp.setTrustCertificateKeyStoreUrl(value);
        }
    }

    public void setUseSSPSCompatibleTimezoneShift(boolean flag) {
        super.setUseSSPSCompatibleTimezoneShift(flag);
        for (ReplicationConnection cp : this.serverConnections.values()) {
            cp.setUseSSPSCompatibleTimezoneShift(flag);
        }
    }

    public void setTreatUtilDateAsTimestamp(boolean flag) {
        super.setTreatUtilDateAsTimestamp(flag);
        for (ReplicationConnection cp : this.serverConnections.values()) {
            cp.setTreatUtilDateAsTimestamp(flag);
        }
    }

    public void setUseFastDateParsing(boolean flag) {
        super.setUseFastDateParsing(flag);
        for (ReplicationConnection cp : this.serverConnections.values()) {
            cp.setUseFastDateParsing(flag);
        }
    }

    public void setLocalSocketAddress(String address) {
        super.setLocalSocketAddress(address);
        for (ReplicationConnection cp : this.serverConnections.values()) {
            cp.setLocalSocketAddress(address);
        }
    }

    public void setUseConfigs(String configs) {
        super.setUseConfigs(configs);
        for (ReplicationConnection cp : this.serverConnections.values()) {
            cp.setUseConfigs(configs);
        }
    }

    public void setGenerateSimpleParameterMetadata(boolean flag) {
        super.setGenerateSimpleParameterMetadata(flag);
        for (ReplicationConnection cp : this.serverConnections.values()) {
            cp.setGenerateSimpleParameterMetadata(flag);
        }
    }

    public void setLogXaCommands(boolean flag) {
        super.setLogXaCommands(flag);
        for (ReplicationConnection cp : this.serverConnections.values()) {
            cp.setLogXaCommands(flag);
        }
    }

    public void setResultSetSizeThreshold(int threshold) throws SQLException {
        super.setResultSetSizeThreshold(threshold);
        for (ReplicationConnection cp : this.serverConnections.values()) {
            cp.setResultSetSizeThreshold(threshold);
        }
    }

    public void setNetTimeoutForStreamingResults(int value) throws SQLException {
        super.setNetTimeoutForStreamingResults(value);
        for (ReplicationConnection cp : this.serverConnections.values()) {
            cp.setNetTimeoutForStreamingResults(value);
        }
    }

    public void setEnableQueryTimeouts(boolean flag) {
        super.setEnableQueryTimeouts(flag);
        for (ReplicationConnection cp : this.serverConnections.values()) {
            cp.setEnableQueryTimeouts(flag);
        }
    }

    public void setPadCharsWithSpace(boolean flag) {
        super.setPadCharsWithSpace(flag);
        for (ReplicationConnection cp : this.serverConnections.values()) {
            cp.setPadCharsWithSpace(flag);
        }
    }

    public void setUseDynamicCharsetInfo(boolean flag) {
        super.setUseDynamicCharsetInfo(flag);
        for (ReplicationConnection cp : this.serverConnections.values()) {
            cp.setUseDynamicCharsetInfo(flag);
        }
    }

    public void setClientInfoProvider(String classname) {
        super.setClientInfoProvider(classname);
        for (ReplicationConnection cp : this.serverConnections.values()) {
            cp.setClientInfoProvider(classname);
        }
    }

    public void setPopulateInsertRowWithDefaultValues(boolean flag) {
        super.setPopulateInsertRowWithDefaultValues(flag);
        for (ReplicationConnection cp : this.serverConnections.values()) {
            cp.setPopulateInsertRowWithDefaultValues(flag);
        }
    }

    public void setLoadBalanceStrategy(String strategy) {
        super.setLoadBalanceStrategy(strategy);
        for (ReplicationConnection cp : this.serverConnections.values()) {
            cp.setLoadBalanceStrategy(strategy);
        }
    }

    public void setTcpNoDelay(boolean flag) {
        super.setTcpNoDelay(flag);
        for (ReplicationConnection cp : this.serverConnections.values()) {
            cp.setTcpNoDelay(flag);
        }
    }

    public void setTcpKeepAlive(boolean flag) {
        super.setTcpKeepAlive(flag);
        for (ReplicationConnection cp : this.serverConnections.values()) {
            cp.setTcpKeepAlive(flag);
        }
    }

    public void setTcpRcvBuf(int bufSize) throws SQLException {
        super.setTcpRcvBuf(bufSize);
        for (ReplicationConnection cp : this.serverConnections.values()) {
            cp.setTcpRcvBuf(bufSize);
        }
    }

    public void setTcpSndBuf(int bufSize) throws SQLException {
        super.setTcpSndBuf(bufSize);
        for (ReplicationConnection cp : this.serverConnections.values()) {
            cp.setTcpSndBuf(bufSize);
        }
    }

    public void setTcpTrafficClass(int classFlags) throws SQLException {
        super.setTcpTrafficClass(classFlags);
        for (ReplicationConnection cp : this.serverConnections.values()) {
            cp.setTcpTrafficClass(classFlags);
        }
    }

    public void setUseNanosForElapsedTime(boolean flag) {
        super.setUseNanosForElapsedTime(flag);
        for (ReplicationConnection cp : this.serverConnections.values()) {
            cp.setUseNanosForElapsedTime(flag);
        }
    }

    public void setSlowQueryThresholdNanos(long nanos) throws SQLException {
        super.setSlowQueryThresholdNanos(nanos);
        for (ReplicationConnection cp : this.serverConnections.values()) {
            cp.setSlowQueryThresholdNanos(nanos);
        }
    }

    public void setStatementInterceptors(String value) {
        super.setStatementInterceptors(value);
        for (ReplicationConnection cp : this.serverConnections.values()) {
            cp.setStatementInterceptors(value);
        }
    }

    public void setUseDirectRowUnpack(boolean flag) {
        super.setUseDirectRowUnpack(flag);
        for (ReplicationConnection cp : this.serverConnections.values()) {
            cp.setUseDirectRowUnpack(flag);
        }
    }

    public void setLargeRowSizeThreshold(String value) throws SQLException {
        super.setLargeRowSizeThreshold(value);
        for (ReplicationConnection cp : this.serverConnections.values()) {
            cp.setLargeRowSizeThreshold(value);
        }
    }

    public void setUseBlobToStoreUTF8OutsideBMP(boolean flag) {
        super.setUseBlobToStoreUTF8OutsideBMP(flag);
        for (ReplicationConnection cp : this.serverConnections.values()) {
            cp.setUseBlobToStoreUTF8OutsideBMP(flag);
        }
    }

    public void setUtf8OutsideBmpExcludedColumnNamePattern(String regexPattern) {
        super.setUtf8OutsideBmpExcludedColumnNamePattern(regexPattern);
        for (ReplicationConnection cp : this.serverConnections.values()) {
            cp.setUtf8OutsideBmpExcludedColumnNamePattern(regexPattern);
        }
    }

    public void setUtf8OutsideBmpIncludedColumnNamePattern(String regexPattern) {
        super.setUtf8OutsideBmpIncludedColumnNamePattern(regexPattern);
        for (ReplicationConnection cp : this.serverConnections.values()) {
            cp.setUtf8OutsideBmpIncludedColumnNamePattern(regexPattern);
        }
    }

    public void setIncludeInnodbStatusInDeadlockExceptions(boolean flag) {
        super.setIncludeInnodbStatusInDeadlockExceptions(flag);
        for (ReplicationConnection cp : this.serverConnections.values()) {
            cp.setIncludeInnodbStatusInDeadlockExceptions(flag);
        }
    }

    public void setIncludeThreadDumpInDeadlockExceptions(boolean flag) {
        super.setIncludeThreadDumpInDeadlockExceptions(flag);
        for (ReplicationConnection cp : this.serverConnections.values()) {
            cp.setIncludeThreadDumpInDeadlockExceptions(flag);
        }
    }

    public void setIncludeThreadNamesAsStatementComment(boolean flag) {
        super.setIncludeThreadNamesAsStatementComment(flag);
        for (ReplicationConnection cp : this.serverConnections.values()) {
            cp.setIncludeThreadNamesAsStatementComment(flag);
        }
    }

    public void setBlobsAreStrings(boolean flag) {
        super.setBlobsAreStrings(flag);
        for (ReplicationConnection cp : this.serverConnections.values()) {
            cp.setBlobsAreStrings(flag);
        }
    }

    public void setFunctionsNeverReturnBlobs(boolean flag) {
        super.setFunctionsNeverReturnBlobs(flag);
        for (ReplicationConnection cp : this.serverConnections.values()) {
            cp.setFunctionsNeverReturnBlobs(flag);
        }
    }

    public void setAutoSlowLog(boolean flag) {
        super.setAutoSlowLog(flag);
        for (ReplicationConnection cp : this.serverConnections.values()) {
            cp.setAutoSlowLog(flag);
        }
    }

    public void setConnectionLifecycleInterceptors(String interceptors) {
        super.setConnectionLifecycleInterceptors(interceptors);
        for (ReplicationConnection cp : this.serverConnections.values()) {
            cp.setConnectionLifecycleInterceptors(interceptors);
        }
    }

    public void setProfilerEventHandler(String handler) {
        super.setProfilerEventHandler(handler);
        for (ReplicationConnection cp : this.serverConnections.values()) {
            cp.setProfilerEventHandler(handler);
        }
    }

    public void setVerifyServerCertificate(boolean flag) {
        super.setVerifyServerCertificate(flag);
        for (ReplicationConnection cp : this.serverConnections.values()) {
            cp.setVerifyServerCertificate(flag);
        }
    }

    public void setUseLegacyDatetimeCode(boolean flag) {
        super.setUseLegacyDatetimeCode(flag);
        for (ReplicationConnection cp : this.serverConnections.values()) {
            cp.setUseLegacyDatetimeCode(flag);
        }
    }

    public void setSelfDestructOnPingSecondsLifetime(int seconds) throws SQLException {
        super.setSelfDestructOnPingSecondsLifetime(seconds);
        for (ReplicationConnection cp : this.serverConnections.values()) {
            cp.setSelfDestructOnPingSecondsLifetime(seconds);
        }
    }

    public void setSelfDestructOnPingMaxOperations(int maxOperations) throws SQLException {
        super.setSelfDestructOnPingMaxOperations(maxOperations);
        for (ReplicationConnection cp : this.serverConnections.values()) {
            cp.setSelfDestructOnPingMaxOperations(maxOperations);
        }
    }

    public void setUseColumnNamesInFindColumn(boolean flag) {
        super.setUseColumnNamesInFindColumn(flag);
        for (ReplicationConnection cp : this.serverConnections.values()) {
            cp.setUseColumnNamesInFindColumn(flag);
        }
    }

    public void setUseLocalTransactionState(boolean flag) {
        super.setUseLocalTransactionState(flag);
        for (ReplicationConnection cp : this.serverConnections.values()) {
            cp.setUseLocalTransactionState(flag);
        }
    }

    public void setCompensateOnDuplicateKeyUpdateCounts(boolean flag) {
        super.setCompensateOnDuplicateKeyUpdateCounts(flag);
        for (ReplicationConnection cp : this.serverConnections.values()) {
            cp.setCompensateOnDuplicateKeyUpdateCounts(flag);
        }
    }

    public void setUseAffectedRows(boolean flag) {
        super.setUseAffectedRows(flag);
        for (ReplicationConnection cp : this.serverConnections.values()) {
            cp.setUseAffectedRows(flag);
        }
    }

    public void setPasswordCharacterEncoding(String characterSet) {
        super.setPasswordCharacterEncoding(characterSet);
        for (ReplicationConnection cp : this.serverConnections.values()) {
            cp.setPasswordCharacterEncoding(characterSet);
        }
    }

    public void setLoadBalanceBlacklistTimeout(int loadBalanceBlacklistTimeout) throws SQLException {
        super.setLoadBalanceBlacklistTimeout(loadBalanceBlacklistTimeout);
        for (ReplicationConnection cp : this.serverConnections.values()) {
            cp.setLoadBalanceBlacklistTimeout(loadBalanceBlacklistTimeout);
        }
    }

    public void setRetriesAllDown(int retriesAllDown) throws SQLException {
        super.setRetriesAllDown(retriesAllDown);
        for (ReplicationConnection cp : this.serverConnections.values()) {
            cp.setRetriesAllDown(retriesAllDown);
        }
    }

    public void setExceptionInterceptors(String exceptionInterceptors) {
        super.setExceptionInterceptors(exceptionInterceptors);
        for (ReplicationConnection cp : this.serverConnections.values()) {
            cp.setExceptionInterceptors(exceptionInterceptors);
        }
    }

    public void setQueryTimeoutKillsConnection(boolean queryTimeoutKillsConnection) {
        super.setQueryTimeoutKillsConnection(queryTimeoutKillsConnection);
        for (ReplicationConnection cp : this.serverConnections.values()) {
            cp.setQueryTimeoutKillsConnection(queryTimeoutKillsConnection);
        }
    }

    public void setLoadBalancePingTimeout(int loadBalancePingTimeout) throws SQLException {
        super.setLoadBalancePingTimeout(loadBalancePingTimeout);
        for (ReplicationConnection cp : this.serverConnections.values()) {
            cp.setLoadBalancePingTimeout(loadBalancePingTimeout);
        }
    }

    public void setLoadBalanceValidateConnectionOnSwapServer(boolean loadBalanceValidateConnectionOnSwapServer) {
        super.setLoadBalanceValidateConnectionOnSwapServer(loadBalanceValidateConnectionOnSwapServer);
        for (ReplicationConnection cp : this.serverConnections.values()) {
            cp.setLoadBalanceValidateConnectionOnSwapServer(loadBalanceValidateConnectionOnSwapServer);
        }
    }

    public void setLoadBalanceConnectionGroup(String loadBalanceConnectionGroup) {
        super.setLoadBalanceConnectionGroup(loadBalanceConnectionGroup);
        for (ReplicationConnection cp : this.serverConnections.values()) {
            cp.setLoadBalanceConnectionGroup(loadBalanceConnectionGroup);
        }
    }

    public void setLoadBalanceExceptionChecker(String loadBalanceExceptionChecker) {
        super.setLoadBalanceExceptionChecker(loadBalanceExceptionChecker);
        for (ReplicationConnection cp : this.serverConnections.values()) {
            cp.setLoadBalanceExceptionChecker(loadBalanceExceptionChecker);
        }
    }

    public void setLoadBalanceSQLStateFailover(String loadBalanceSQLStateFailover) {
        super.setLoadBalanceSQLStateFailover(loadBalanceSQLStateFailover);
        for (ReplicationConnection cp : this.serverConnections.values()) {
            cp.setLoadBalanceSQLStateFailover(loadBalanceSQLStateFailover);
        }
    }

    public void setLoadBalanceSQLExceptionSubclassFailover(String loadBalanceSQLExceptionSubclassFailover) {
        super.setLoadBalanceSQLExceptionSubclassFailover(loadBalanceSQLExceptionSubclassFailover);
        for (ReplicationConnection cp : this.serverConnections.values()) {
            cp.setLoadBalanceSQLExceptionSubclassFailover(loadBalanceSQLExceptionSubclassFailover);
        }
    }

    public void setLoadBalanceEnableJMX(boolean loadBalanceEnableJMX) {
        super.setLoadBalanceEnableJMX(loadBalanceEnableJMX);
        for (ReplicationConnection cp : this.serverConnections.values()) {
            cp.setLoadBalanceEnableJMX(loadBalanceEnableJMX);
        }
    }

    public void setLoadBalanceAutoCommitStatementThreshold(int loadBalanceAutoCommitStatementThreshold) throws SQLException {
        super.setLoadBalanceAutoCommitStatementThreshold(loadBalanceAutoCommitStatementThreshold);
        for (ReplicationConnection cp : this.serverConnections.values()) {
            cp.setLoadBalanceAutoCommitStatementThreshold(loadBalanceAutoCommitStatementThreshold);
        }
    }

    public void setLoadBalanceAutoCommitStatementRegex(String loadBalanceAutoCommitStatementRegex) {
        super.setLoadBalanceAutoCommitStatementRegex(loadBalanceAutoCommitStatementRegex);
        for (ReplicationConnection cp : this.serverConnections.values()) {
            cp.setLoadBalanceAutoCommitStatementRegex(loadBalanceAutoCommitStatementRegex);
        }
    }

    public void setAuthenticationPlugins(String authenticationPlugins) {
        super.setAuthenticationPlugins(authenticationPlugins);
        for (ReplicationConnection cp : this.serverConnections.values()) {
            cp.setAuthenticationPlugins(authenticationPlugins);
        }
    }

    public void setDisabledAuthenticationPlugins(String disabledAuthenticationPlugins) {
        super.setDisabledAuthenticationPlugins(disabledAuthenticationPlugins);
        for (ReplicationConnection cp : this.serverConnections.values()) {
            cp.setDisabledAuthenticationPlugins(disabledAuthenticationPlugins);
        }
    }

    public void setDefaultAuthenticationPlugin(String defaultAuthenticationPlugin) {
        super.setDefaultAuthenticationPlugin(defaultAuthenticationPlugin);
        for (ReplicationConnection cp : this.serverConnections.values()) {
            cp.setDefaultAuthenticationPlugin(defaultAuthenticationPlugin);
        }
    }

    public void setParseInfoCacheFactory(String factoryClassname) {
        super.setParseInfoCacheFactory(factoryClassname);
        for (ReplicationConnection cp : this.serverConnections.values()) {
            cp.setParseInfoCacheFactory(factoryClassname);
        }
    }

    public void setServerConfigCacheFactory(String factoryClassname) {
        super.setServerConfigCacheFactory(factoryClassname);
        for (ReplicationConnection cp : this.serverConnections.values()) {
            cp.setServerConfigCacheFactory(factoryClassname);
        }
    }

    public void setDisconnectOnExpiredPasswords(boolean disconnectOnExpiredPasswords) {
        super.setDisconnectOnExpiredPasswords(disconnectOnExpiredPasswords);
        for (ReplicationConnection cp : this.serverConnections.values()) {
            cp.setDisconnectOnExpiredPasswords(disconnectOnExpiredPasswords);
        }
    }

    public void setGetProceduresReturnsFunctions(boolean getProcedureReturnsFunctions) {
        super.setGetProceduresReturnsFunctions(getProcedureReturnsFunctions);
    }

    public int getActiveStatementCount() {
        return -1;
    }

    public long getIdleFor() {
        return -1;
    }

    public Log getLog() {
        return this.log;
    }

    public boolean isMasterConnection() {
        return false;
    }

    public boolean isNoBackslashEscapesSet() {
        return false;
    }

    public boolean isSameResource(Connection c) {
        return false;
    }

    public boolean parserKnowsUnicode() {
        return false;
    }

    public void ping() throws SQLException {
    }

    public void resetServerState() throws SQLException {
    }

    public void setFailedOver(boolean flag) {
    }

    @Deprecated
    public void setPreferSlaveDuringFailover(boolean flag) {
    }

    public void setStatementComment(String comment) {
    }

    public void reportQueryTime(long millisOrNanos) {
    }

    public boolean isAbonormallyLongQuery(long millisOrNanos) {
        return false;
    }

    public void initializeExtension(Extension ex) throws SQLException {
    }

    public int getAutoIncrementIncrement() {
        return -1;
    }

    public boolean hasSameProperties(Connection c) {
        return false;
    }

    public Properties getProperties() {
        return null;
    }

    public void setSchema(String schema) throws SQLException {
    }

    public String getSchema() throws SQLException {
        return null;
    }

    public void abort(Executor executor) throws SQLException {
    }

    public void setNetworkTimeout(Executor executor, int milliseconds) throws SQLException {
    }

    public int getNetworkTimeout() throws SQLException {
        return -1;
    }

    public void checkClosed() throws SQLException {
    }

    public Object getConnectionMutex() {
        return this;
    }

    public void setSessionMaxRows(int max) throws SQLException {
        for (ReplicationConnection c : this.serverConnections.values()) {
            c.setSessionMaxRows(max);
        }
    }

    public int getSessionMaxRows() {
        return getActiveConnectionPassive().getSessionMaxRows();
    }

    public boolean isProxySet() {
        return false;
    }

    public Connection duplicate() throws SQLException {
        return null;
    }

    public CachedResultSetMetaData getCachedMetaData(String sql) {
        return null;
    }

    public Timer getCancelTimer() {
        return null;
    }

    public SingleByteCharsetConverter getCharsetConverter(String javaEncodingName) throws SQLException {
        return null;
    }

    @Deprecated
    public String getCharsetNameForIndex(int charsetIndex) throws SQLException {
        return getEncodingForIndex(charsetIndex);
    }

    public String getEncodingForIndex(int charsetIndex) throws SQLException {
        return null;
    }

    public TimeZone getDefaultTimeZone() {
        return null;
    }

    public String getErrorMessageEncoding() {
        return null;
    }

    public ExceptionInterceptor getExceptionInterceptor() {
        if (this.currentConnection == null) {
            return null;
        }
        return this.currentConnection.getExceptionInterceptor();
    }

    public String getHost() {
        return null;
    }

    public String getHostPortPair() {
        return getActiveMySQLConnection().getHostPortPair();
    }

    public long getId() {
        return -1;
    }

    public int getMaxBytesPerChar(String javaCharsetName) throws SQLException {
        return -1;
    }

    public int getMaxBytesPerChar(Integer charsetIndex, String javaCharsetName) throws SQLException {
        return -1;
    }

    public int getNetBufferLength() {
        return -1;
    }

    public boolean getRequiresEscapingEncoder() {
        return false;
    }

    public int getServerMajorVersion() {
        return -1;
    }

    public int getServerMinorVersion() {
        return -1;
    }

    public int getServerSubMinorVersion() {
        return -1;
    }

    public String getServerVariable(String variableName) {
        return null;
    }

    public String getServerVersion() {
        return null;
    }

    public Calendar getSessionLockedCalendar() {
        return null;
    }

    public String getStatementComment() {
        return null;
    }

    public List<StatementInterceptorV2> getStatementInterceptorsInstances() {
        return null;
    }

    public String getURL() {
        return null;
    }

    public String getUser() {
        return null;
    }

    public Calendar getUtcCalendar() {
        return null;
    }

    public void incrementNumberOfPreparedExecutes() {
    }

    public void incrementNumberOfPrepares() {
    }

    public void incrementNumberOfResultSetsCreated() {
    }

    public void initializeResultsMetadataFromCache(String sql, CachedResultSetMetaData cachedMetaData, ResultSetInternalMethods resultSet) throws SQLException {
    }

    public void initializeSafeStatementInterceptors() throws SQLException {
    }

    public boolean isClientTzUTC() {
        return false;
    }

    public boolean isCursorFetchEnabled() throws SQLException {
        return false;
    }

    public boolean isReadInfoMsgEnabled() {
        return false;
    }

    public boolean isServerTzUTC() {
        return false;
    }

    public boolean lowerCaseTableNames() {
        return getActiveMySQLConnection().lowerCaseTableNames();
    }

    public void maxRowsChanged(com.mysql.jdbc.Statement stmt) {
    }

    public void pingInternal(boolean checkForClosedConnection, int timeoutMillis) throws SQLException {
    }

    public void realClose(boolean calledExplicitly, boolean issueRollback, boolean skipLocalTeardown, Throwable reason) throws SQLException {
    }

    public void recachePreparedStatement(ServerPreparedStatement pstmt) throws SQLException {
    }

    public void registerQueryExecutionTime(long queryTimeMs) {
    }

    public void registerStatement(com.mysql.jdbc.Statement stmt) {
    }

    public void reportNumberOfTablesAccessed(int numTablesAccessed) {
    }

    public boolean serverSupportsConvertFn() throws SQLException {
        return getActiveMySQLConnectionChecked().serverSupportsConvertFn();
    }

    public void setReadInfoMsgEnabled(boolean flag) {
    }

    public void setReadOnlyInternal(boolean readOnlyFlag) throws SQLException {
    }

    public boolean storesLowerCaseTableName() {
        return getActiveMySQLConnection().storesLowerCaseTableName();
    }

    public void throwConnectionClosedException() throws SQLException {
    }

    public void unregisterStatement(com.mysql.jdbc.Statement stmt) {
    }

    public void unsetMaxRows(com.mysql.jdbc.Statement stmt) throws SQLException {
    }

    public boolean useAnsiQuotedIdentifiers() {
        return false;
    }

    public boolean useMaxRows() {
        return false;
    }

    public void clearWarnings() {
    }

    public Properties getClientInfo() {
        return null;
    }

    public String getClientInfo(String name) {
        return null;
    }

    public int getHoldability() {
        return -1;
    }

    public int getTransactionIsolation() {
        return -1;
    }

    public Map<String, Class<?>> getTypeMap() {
        return null;
    }

    public SQLWarning getWarnings() throws SQLException {
        return getActiveMySQLConnectionChecked().getWarnings();
    }

    public String nativeSQL(String sql) throws SQLException {
        return getActiveMySQLConnectionChecked().nativeSQL(sql);
    }

    public ProfilerEventHandler getProfilerEventHandlerInstance() {
        return null;
    }

    public void setProfilerEventHandlerInstance(ProfilerEventHandler h) {
    }

    public void decachePreparedStatement(ServerPreparedStatement pstmt) throws SQLException {
    }
}
