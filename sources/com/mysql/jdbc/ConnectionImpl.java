package com.mysql.jdbc;

import android.support.v4.internal.view.SupportMenu;
import com.mysql.jdbc.PreparedStatement.ParseInfo;
import com.mysql.jdbc.log.Log;
import com.mysql.jdbc.log.LogFactory;
import com.mysql.jdbc.log.LogUtils;
import com.mysql.jdbc.log.NullLogger;
import com.mysql.jdbc.profiler.ProfilerEvent;
import com.mysql.jdbc.profiler.ProfilerEventHandler;
import com.mysql.jdbc.util.LRUCache;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.lang.ref.WeakReference;
import java.lang.reflect.Array;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.nio.ByteBuffer;
import java.nio.CharBuffer;
import java.nio.charset.Charset;
import java.nio.charset.CharsetEncoder;
import java.nio.charset.UnsupportedCharsetException;
import java.sql.Blob;
import java.sql.CallableStatement;
import java.sql.DatabaseMetaData;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.sql.SQLPermission;
import java.sql.SQLWarning;
import java.sql.Savepoint;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Enumeration;
import java.util.GregorianCalendar;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Properties;
import java.util.Random;
import java.util.TimeZone;
import java.util.Timer;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.Executor;

public class ConnectionImpl extends ConnectionPropertiesImpl implements MySQLConnection {
    private static final SQLPermission ABORT_PERM = new SQLPermission("abort");
    private static final Object CHARSET_CONVERTER_NOT_AVAILABLE_MARKER = new Object();
    protected static final String DEFAULT_LOGGER_CLASS = "com.mysql.jdbc.log.StandardLogger";
    private static final int DEFAULT_RESULT_SET_CONCURRENCY = 1007;
    private static final int DEFAULT_RESULT_SET_TYPE = 1003;
    private static final int HISTOGRAM_BUCKETS = 20;
    private static final Constructor<?> JDBC_4_CONNECTION_CTOR;
    public static final String JDBC_LOCAL_CHARACTER_SET_RESULTS = "jdbc.local.character_set_results";
    private static final String LOGGER_INSTANCE_NAME = "MySQL";
    private static final Log NULL_LOGGER = new NullLogger(LOGGER_INSTANCE_NAME);
    private static final String SERVER_VERSION_STRING_VAR_NAME = "server_version_string";
    private static final SQLPermission SET_NETWORK_TIMEOUT_PERM = new SQLPermission("setNetworkTimeout");
    public static Map<?, ?> charsetMap = null;
    private static final Map<String, Map<String, Integer>> customCharsetToMblenMapByUrl = new HashMap();
    private static final Map<String, Map<Integer, String>> customIndexToCharsetMapByUrl = new HashMap();
    private static Map<String, Integer> mapTransIsolationNameToValue = null;
    private static final Random random = new Random();
    protected static Map<?, ?> roundRobinStatsMap = null;
    private static final long serialVersionUID = 2877471301981509474L;
    private boolean autoCommit;
    private int autoIncrementIncrement;
    private CacheAdapter<String, ParseInfo> cachedPreparedStatementParams;
    private transient Timer cancelTimer;
    private String characterSetMetadata;
    private String characterSetResultsOnServer;
    private final Map<String, Object> charsetConverterMap;
    private long connectionCreationTimeMillis;
    private long connectionId;
    private List<Extension> connectionLifecycleInterceptors;
    private String database;
    private DatabaseMetaData dbmd;
    private TimeZone defaultTimeZone;
    private String errorMessageEncoding;
    private ProfilerEventHandler eventSink;
    private ExceptionInterceptor exceptionInterceptor;
    private Throwable forceClosedReason;
    private boolean hasIsolationLevels;
    private boolean hasQuotedIdentifiers;
    private boolean hasTriedMasterFlag;
    private String host;
    private String hostPortPair;
    public Map<Integer, String> indexToCustomMysqlCharset;
    private transient MysqlIO io;
    private boolean isClientTzUTC;
    private boolean isClosed;
    private boolean isInGlobalTx;
    private boolean isRunningOnJDK13;
    private boolean isServerTzUTC;
    private int isolationLevel;
    private long lastQueryFinishedTime;
    private transient Log log;
    private long longestQueryTimeMs;
    private boolean lowerCaseTableNames;
    private long maximumNumberTablesAccessed;
    private long metricsLastReportedMs;
    private long minimumNumberTablesAccessed;
    private String myURL;
    private Map<String, Integer> mysqlCharsetToCustomMblen;
    private boolean needsPing;
    private int netBufferLength;
    private boolean noBackslashEscapes;
    private long[] numTablesMetricsHistBreakpoints;
    private int[] numTablesMetricsHistCounts;
    private long numberOfPreparedExecutes;
    private long numberOfPrepares;
    private long numberOfQueriesIssued;
    private long numberOfResultSetsCreated;
    private long[] oldHistBreakpoints;
    private int[] oldHistCounts;
    private final CopyOnWriteArrayList<Statement> openStatements;
    private String origDatabaseToConnectTo;
    private String origHostToConnectTo;
    private int origPortToConnectTo;
    private LRUCache<CompoundCacheKey, CallableStatementParamInfo> parsedCallableStatementCache;
    private boolean parserKnowsUnicode;
    private String password;
    private long[] perfMetricsHistBreakpoints;
    private int[] perfMetricsHistCounts;
    private String pointOfOrigin;
    private int port;
    protected Properties props;
    private MySQLConnection proxy;
    private long queryTimeCount;
    private double queryTimeMean;
    private double queryTimeSum;
    private double queryTimeSumSquares;
    private boolean readInfoMsg;
    private boolean readOnly;
    private InvocationHandler realProxy;
    private boolean requiresEscapingEncoder;
    protected LRUCache<String, CachedResultSetMetaData> resultSetMetadataCache;
    private CacheAdapter<String, Map<String, String>> serverConfigCache;
    private LRUCache<CompoundCacheKey, ServerPreparedStatement> serverSideStatementCache;
    private LRUCache<String, Boolean> serverSideStatementCheckCache;
    private TimeZone serverTimezoneTZ;
    private Map<String, String> serverVariables;
    private Calendar sessionCalendar;
    private int sessionMaxRows;
    private long shortestQueryTimeMs;
    private String statementComment;
    private List<StatementInterceptorV2> statementInterceptors;
    private boolean storesLowerCaseTableName;
    private double totalQueryTimeMs;
    private boolean transactionsSupported;
    private Map<String, Class<?>> typeMap;
    private boolean useAnsiQuotes;
    private boolean usePlatformCharsetConverters;
    private boolean useServerPreparedStmts;
    private String user;
    private Calendar utcCalendar;

    static class CompoundCacheKey {
        final String componentOne;
        final String componentTwo;
        final int hashCode;

        CompoundCacheKey(String partOne, String partTwo) {
            this.componentOne = partOne;
            this.componentTwo = partTwo;
            int i = 0;
            int hc = 31 * ((31 * 17) + (this.componentOne != null ? this.componentOne.hashCode() : 0));
            if (this.componentTwo != null) {
                i = this.componentTwo.hashCode();
            }
            this.hashCode = hc + i;
        }

        /* JADX WARNING: inconsistent code. */
        /* Code decompiled incorrectly, please refer to instructions dump. */
        public boolean equals(java.lang.Object r6) {
            /*
            r5 = this;
            r0 = 1;
            if (r5 != r6) goto L_0x0004;
        L_0x0003:
            return r0;
        L_0x0004:
            r1 = 0;
            if (r6 == 0) goto L_0x003d;
        L_0x0007:
            r2 = com.mysql.jdbc.ConnectionImpl.CompoundCacheKey.class;
            r3 = r6.getClass();
            r2 = r2.isAssignableFrom(r3);
            if (r2 == 0) goto L_0x003d;
        L_0x0013:
            r2 = r6;
            r2 = (com.mysql.jdbc.ConnectionImpl.CompoundCacheKey) r2;
            r3 = r5.componentOne;
            if (r3 != 0) goto L_0x001f;
        L_0x001a:
            r3 = r2.componentOne;
            if (r3 != 0) goto L_0x003d;
        L_0x001e:
            goto L_0x0029;
        L_0x001f:
            r3 = r5.componentOne;
            r4 = r2.componentOne;
            r3 = r3.equals(r4);
            if (r3 == 0) goto L_0x003d;
        L_0x0029:
            r3 = r5.componentTwo;
            if (r3 != 0) goto L_0x0034;
        L_0x002d:
            r3 = r2.componentTwo;
            if (r3 != 0) goto L_0x0032;
        L_0x0031:
            goto L_0x003c;
        L_0x0032:
            r0 = r1;
            goto L_0x003c;
        L_0x0034:
            r0 = r5.componentTwo;
            r1 = r2.componentTwo;
            r0 = r0.equals(r1);
        L_0x003c:
            return r0;
        L_0x003d:
            return r1;
            */
            throw new UnsupportedOperationException("Method not decompiled: com.mysql.jdbc.ConnectionImpl.CompoundCacheKey.equals(java.lang.Object):boolean");
        }

        public int hashCode() {
            return this.hashCode;
        }
    }

    private static class NetworkTimeoutSetter implements Runnable {
        private final WeakReference<ConnectionImpl> connImplRef;
        private final int milliseconds;
        private final WeakReference<MysqlIO> mysqlIoRef;

        public NetworkTimeoutSetter(ConnectionImpl conn, MysqlIO io, int milliseconds) {
            this.connImplRef = new WeakReference(conn);
            this.mysqlIoRef = new WeakReference(io);
            this.milliseconds = milliseconds;
        }

        public void run() {
            try {
                ConnectionImpl conn = (ConnectionImpl) this.connImplRef.get();
                if (conn != null) {
                    synchronized (conn.getConnectionMutex()) {
                        conn.setSocketTimeout(this.milliseconds);
                        MysqlIO io = (MysqlIO) this.mysqlIoRef.get();
                        if (io != null) {
                            io.setSocketTimeout(this.milliseconds);
                        }
                    }
                }
            } catch (SQLException e) {
                throw new RuntimeException(e);
            }
        }
    }

    /* renamed from: com.mysql.jdbc.ConnectionImpl$4 */
    class C04704 implements ExceptionInterceptor {
        public void init(Connection conn, Properties config) throws SQLException {
        }

        public void destroy() {
        }

        C04704() {
        }

        public SQLException interceptException(SQLException sqlEx, Connection conn) {
            if (sqlEx.getSQLState() != null && sqlEx.getSQLState().startsWith("08")) {
                ConnectionImpl.this.serverConfigCache.invalidate(ConnectionImpl.this.getURL());
            }
            return null;
        }
    }

    public class ExceptionInterceptorChain implements ExceptionInterceptor {
        private List<Extension> interceptors;

        ExceptionInterceptorChain(String interceptorClasses) throws SQLException {
            this.interceptors = Util.loadExtensions(ConnectionImpl.this, ConnectionImpl.this.props, interceptorClasses, "Connection.BadExceptionInterceptor", this);
        }

        void addRingZero(ExceptionInterceptor interceptor) throws SQLException {
            this.interceptors.add(0, interceptor);
        }

        public SQLException interceptException(SQLException sqlEx, Connection conn) {
            if (this.interceptors != null) {
                Iterator<Extension> iter = this.interceptors.iterator();
                while (iter.hasNext()) {
                    sqlEx = ((ExceptionInterceptor) iter.next()).interceptException(sqlEx, ConnectionImpl.this);
                }
            }
            return sqlEx;
        }

        public void destroy() {
            if (this.interceptors != null) {
                Iterator<Extension> iter = this.interceptors.iterator();
                while (iter.hasNext()) {
                    ((ExceptionInterceptor) iter.next()).destroy();
                }
            }
        }

        public void init(Connection conn, Properties properties) throws SQLException {
            if (this.interceptors != null) {
                Iterator<Extension> iter = this.interceptors.iterator();
                while (iter.hasNext()) {
                    ((ExceptionInterceptor) iter.next()).init(conn, properties);
                }
            }
        }

        public List<Extension> getInterceptors() {
            return this.interceptors;
        }
    }

    static {
        mapTransIsolationNameToValue = null;
        mapTransIsolationNameToValue = new HashMap(8);
        mapTransIsolationNameToValue.put("READ-UNCOMMITED", Integer.valueOf(1));
        mapTransIsolationNameToValue.put("READ-UNCOMMITTED", Integer.valueOf(1));
        mapTransIsolationNameToValue.put("READ-COMMITTED", Integer.valueOf(2));
        mapTransIsolationNameToValue.put("REPEATABLE-READ", Integer.valueOf(4));
        mapTransIsolationNameToValue.put("SERIALIZABLE", Integer.valueOf(8));
        if (Util.isJdbc4()) {
            try {
                JDBC_4_CONNECTION_CTOR = Class.forName("com.mysql.jdbc.JDBC4Connection").getConstructor(new Class[]{String.class, Integer.TYPE, Properties.class, String.class, String.class});
            } catch (SecurityException e) {
                throw new RuntimeException(e);
            } catch (NoSuchMethodException e2) {
                throw new RuntimeException(e2);
            } catch (ClassNotFoundException e3) {
                throw new RuntimeException(e3);
            }
        }
        JDBC_4_CONNECTION_CTOR = null;
    }

    public String getHost() {
        return this.host;
    }

    public String getHostPortPair() {
        if (this.hostPortPair != null) {
            return this.hostPortPair;
        }
        StringBuilder stringBuilder = new StringBuilder();
        stringBuilder.append(this.host);
        stringBuilder.append(":");
        stringBuilder.append(this.port);
        return stringBuilder.toString();
    }

    public boolean isProxySet() {
        return this.proxy != null;
    }

    public void setProxy(MySQLConnection proxy) {
        this.proxy = proxy;
        this.realProxy = this.proxy instanceof MultiHostMySQLConnection ? ((MultiHostMySQLConnection) proxy).getThisAsProxy() : null;
    }

    private MySQLConnection getProxy() {
        return this.proxy != null ? this.proxy : this;
    }

    @Deprecated
    public MySQLConnection getLoadBalanceSafeProxy() {
        return getMultiHostSafeProxy();
    }

    public MySQLConnection getMultiHostSafeProxy() {
        return getProxy();
    }

    public MySQLConnection getActiveMySQLConnection() {
        return this;
    }

    public Object getConnectionMutex() {
        return this.realProxy != null ? this.realProxy : getProxy();
    }

    protected static SQLException appendMessageToException(SQLException sqlEx, String messageToAppend, ExceptionInterceptor interceptor) {
        SQLException sQLException;
        String str;
        String origMessage = sqlEx.getMessage();
        String sqlState = sqlEx.getSQLState();
        int vendorErrorCode = sqlEx.getErrorCode();
        StringBuilder messageBuf = new StringBuilder(origMessage.length() + messageToAppend.length());
        messageBuf.append(origMessage);
        messageBuf.append(messageToAppend);
        SQLException sqlExceptionWithNewMessage = SQLError.createSQLException(messageBuf.toString(), sqlState, vendorErrorCode, interceptor);
        try {
            Class<?> stackTraceElementArrayClass = Array.newInstance(Class.forName("java.lang.StackTraceElement"), new int[]{0}).getClass();
            try {
                origMessage = Throwable.class.getMethod("getStackTrace", new Class[0]);
                Method setStackTraceMethod = Throwable.class.getMethod("setStackTrace", new Class[]{stackTraceElementArrayClass});
                if (origMessage == null || setStackTraceMethod == null) {
                    sQLException = sqlEx;
                    return sqlExceptionWithNewMessage;
                }
                try {
                    Object theStackTraceAsObject = origMessage.invoke(sqlEx, new Object[0]);
                    setStackTraceMethod.invoke(sqlExceptionWithNewMessage, new Object[]{theStackTraceAsObject});
                } catch (NoClassDefFoundError e) {
                } catch (NoSuchMethodException e2) {
                } catch (Throwable th) {
                    return sqlExceptionWithNewMessage;
                }
                return sqlExceptionWithNewMessage;
            } catch (NoClassDefFoundError e3) {
                sQLException = sqlEx;
            } catch (NoSuchMethodException e4) {
                sQLException = sqlEx;
            } catch (Throwable th2) {
                sQLException = sqlEx;
                return sqlExceptionWithNewMessage;
            }
        } catch (NoClassDefFoundError e5) {
            sQLException = sqlEx;
            str = origMessage;
        } catch (NoSuchMethodException e6) {
            sQLException = sqlEx;
            str = origMessage;
        } catch (Throwable th3) {
            sQLException = sqlEx;
            str = origMessage;
            return sqlExceptionWithNewMessage;
        }
    }

    public Timer getCancelTimer() {
        Timer timer;
        synchronized (getConnectionMutex()) {
            if (this.cancelTimer == null) {
                this.cancelTimer = new Timer("MySQL Statement Cancellation Timer", true);
            }
            timer = this.cancelTimer;
        }
        return timer;
    }

    protected static Connection getInstance(String hostToConnectTo, int portToConnectTo, Properties info, String databaseToConnectTo, String url) throws SQLException {
        if (!Util.isJdbc4()) {
            return new ConnectionImpl(hostToConnectTo, portToConnectTo, info, databaseToConnectTo, url);
        }
        return (Connection) Util.handleNewInstance(JDBC_4_CONNECTION_CTOR, new Object[]{hostToConnectTo, Integer.valueOf(portToConnectTo), info, databaseToConnectTo, url}, null);
    }

    protected static synchronized int getNextRoundRobinHostIndex(String url, List<?> hostList) {
        int index;
        synchronized (ConnectionImpl.class) {
            index = random.nextInt(hostList.size());
        }
        return index;
    }

    private static boolean nullSafeCompare(String s1, String s2) {
        boolean z = true;
        if (s1 == null && s2 == null) {
            return true;
        }
        if (s1 == null && s2 != null) {
            return false;
        }
        if (s1 == null || !s1.equals(s2)) {
            z = false;
        }
        return z;
    }

    protected ConnectionImpl() {
        this.proxy = null;
        this.realProxy = null;
        this.autoCommit = true;
        this.characterSetMetadata = null;
        this.characterSetResultsOnServer = null;
        this.charsetConverterMap = new HashMap(CharsetMapping.getNumberOfCharsetsConfigured());
        this.connectionCreationTimeMillis = 0;
        this.database = null;
        this.dbmd = null;
        this.hasIsolationLevels = false;
        this.hasQuotedIdentifiers = false;
        this.host = null;
        this.indexToCustomMysqlCharset = null;
        this.mysqlCharsetToCustomMblen = null;
        this.io = null;
        this.isClientTzUTC = false;
        this.isClosed = true;
        this.isInGlobalTx = false;
        this.isRunningOnJDK13 = false;
        this.isolationLevel = 2;
        this.isServerTzUTC = false;
        this.lastQueryFinishedTime = 0;
        this.log = NULL_LOGGER;
        this.longestQueryTimeMs = 0;
        this.lowerCaseTableNames = false;
        this.maximumNumberTablesAccessed = 0;
        this.sessionMaxRows = -1;
        this.minimumNumberTablesAccessed = Long.MAX_VALUE;
        this.myURL = null;
        this.needsPing = false;
        this.netBufferLength = 16384;
        this.noBackslashEscapes = false;
        this.numberOfPreparedExecutes = 0;
        this.numberOfPrepares = 0;
        this.numberOfQueriesIssued = 0;
        this.numberOfResultSetsCreated = 0;
        this.oldHistBreakpoints = null;
        this.oldHistCounts = null;
        this.openStatements = new CopyOnWriteArrayList();
        this.parserKnowsUnicode = false;
        this.password = null;
        this.port = 3306;
        this.props = null;
        this.readInfoMsg = false;
        this.readOnly = false;
        this.serverTimezoneTZ = null;
        this.serverVariables = null;
        this.shortestQueryTimeMs = Long.MAX_VALUE;
        this.totalQueryTimeMs = 0.0d;
        this.transactionsSupported = false;
        this.useAnsiQuotes = false;
        this.user = null;
        this.useServerPreparedStmts = false;
        this.errorMessageEncoding = "Cp1252";
        this.hasTriedMasterFlag = false;
        this.statementComment = null;
        this.autoIncrementIncrement = 0;
    }

    public ConnectionImpl(String hostToConnectTo, int portToConnectTo, Properties info, String databaseToConnectTo, String url) throws SQLException {
        this.proxy = null;
        this.realProxy = null;
        boolean z = true;
        this.autoCommit = true;
        this.characterSetMetadata = null;
        this.characterSetResultsOnServer = null;
        this.charsetConverterMap = new HashMap(CharsetMapping.getNumberOfCharsetsConfigured());
        this.connectionCreationTimeMillis = 0;
        this.database = null;
        this.dbmd = null;
        this.hasIsolationLevels = false;
        this.hasQuotedIdentifiers = false;
        this.host = null;
        this.indexToCustomMysqlCharset = null;
        this.mysqlCharsetToCustomMblen = null;
        this.io = null;
        this.isClientTzUTC = false;
        this.isClosed = true;
        this.isInGlobalTx = false;
        this.isRunningOnJDK13 = false;
        this.isolationLevel = 2;
        this.isServerTzUTC = false;
        this.lastQueryFinishedTime = 0;
        this.log = NULL_LOGGER;
        this.longestQueryTimeMs = 0;
        this.lowerCaseTableNames = false;
        this.maximumNumberTablesAccessed = 0;
        this.sessionMaxRows = -1;
        this.minimumNumberTablesAccessed = Long.MAX_VALUE;
        this.myURL = null;
        this.needsPing = false;
        this.netBufferLength = 16384;
        this.noBackslashEscapes = false;
        this.numberOfPreparedExecutes = 0;
        this.numberOfPrepares = 0;
        this.numberOfQueriesIssued = 0;
        this.numberOfResultSetsCreated = 0;
        this.oldHistBreakpoints = null;
        this.oldHistCounts = null;
        this.openStatements = new CopyOnWriteArrayList();
        this.parserKnowsUnicode = false;
        this.password = null;
        this.port = 3306;
        this.props = null;
        this.readInfoMsg = false;
        this.readOnly = false;
        this.serverTimezoneTZ = null;
        this.serverVariables = null;
        this.shortestQueryTimeMs = Long.MAX_VALUE;
        this.totalQueryTimeMs = 0.0d;
        this.transactionsSupported = false;
        this.useAnsiQuotes = false;
        this.user = null;
        this.useServerPreparedStmts = false;
        this.errorMessageEncoding = "Cp1252";
        this.hasTriedMasterFlag = false;
        this.statementComment = null;
        this.autoIncrementIncrement = 0;
        this.connectionCreationTimeMillis = System.currentTimeMillis();
        if (databaseToConnectTo == null) {
            databaseToConnectTo = "";
        }
        this.origHostToConnectTo = hostToConnectTo;
        this.origPortToConnectTo = portToConnectTo;
        this.origDatabaseToConnectTo = databaseToConnectTo;
        try {
            Blob.class.getMethod("truncate", new Class[]{Long.TYPE});
            this.isRunningOnJDK13 = false;
        } catch (NoSuchMethodException e) {
            this.isRunningOnJDK13 = true;
        }
        this.sessionCalendar = new GregorianCalendar();
        this.utcCalendar = new GregorianCalendar();
        this.utcCalendar.setTimeZone(TimeZone.getTimeZone("GMT"));
        this.log = LogFactory.getLogger(getLogger(), LOGGER_INSTANCE_NAME, getExceptionInterceptor());
        if (NonRegisteringDriver.isHostPropertiesList(hostToConnectTo)) {
            Properties hostSpecificProps = NonRegisteringDriver.expandHostKeyValues(hostToConnectTo);
            Enumeration<?> propertyNames = hostSpecificProps.propertyNames();
            while (propertyNames.hasMoreElements()) {
                String propertyName = propertyNames.nextElement().toString();
                info.setProperty(propertyName, hostSpecificProps.getProperty(propertyName));
            }
        } else if (hostToConnectTo == null) {
            this.host = "localhost";
            r0 = new StringBuilder();
            r0.append(this.host);
            r0.append(":");
            r0.append(portToConnectTo);
            this.hostPortPair = r0.toString();
        } else {
            this.host = hostToConnectTo;
            if (hostToConnectTo.indexOf(":") == -1) {
                r0 = new StringBuilder();
                r0.append(this.host);
                r0.append(":");
                r0.append(portToConnectTo);
                this.hostPortPair = r0.toString();
            } else {
                this.hostPortPair = this.host;
            }
        }
        this.port = portToConnectTo;
        this.database = databaseToConnectTo;
        this.myURL = url;
        this.user = info.getProperty(NonRegisteringDriver.USER_PROPERTY_KEY);
        this.password = info.getProperty(NonRegisteringDriver.PASSWORD_PROPERTY_KEY);
        if (this.user == null || this.user.equals("")) {
            this.user = "";
        }
        if (this.password == null) {
            this.password = "";
        }
        this.props = info;
        initializeDriverProperties(info);
        this.defaultTimeZone = TimeUtil.getDefaultTimeZone(getCacheDefaultTimezone());
        if (this.defaultTimeZone.useDaylightTime() || this.defaultTimeZone.getRawOffset() != 0) {
            z = false;
        }
        this.isClientTzUTC = z;
        if (getUseUsageAdvisor()) {
            this.pointOfOrigin = LogUtils.findCallingClassAndMethod(new Throwable());
        } else {
            this.pointOfOrigin = "";
        }
        try {
            this.dbmd = getMetaData(false, false);
            initializeSafeStatementInterceptors();
            createNewIO(false);
            unSafeStatementInterceptors();
            NonRegisteringDriver.trackConnection(this);
        } catch (SQLException ex) {
            cleanup(ex);
            throw ex;
        } catch (Exception ex2) {
            cleanup(ex2);
            StringBuilder mesg = new StringBuilder(128);
            if (getParanoid()) {
                mesg.append("Unable to connect to database.");
            } else {
                mesg.append("Cannot connect to MySQL server on ");
                mesg.append(this.host);
                mesg.append(":");
                mesg.append(this.port);
                mesg.append(".\n\n");
                mesg.append("Make sure that there is a MySQL server ");
                mesg.append("running on the machine/port you are trying ");
                mesg.append("to connect to and that the machine this software is running on ");
                mesg.append("is able to connect to this host/port (i.e. not firewalled). ");
                mesg.append("Also make sure that the server has not been started with the --skip-networking ");
                mesg.append("flag.\n\n");
            }
            SQLException sqlEx = SQLError.createSQLException(mesg.toString(), SQLError.SQL_STATE_COMMUNICATION_LINK_FAILURE, getExceptionInterceptor());
            sqlEx.initCause(ex2);
            throw sqlEx;
        }
    }

    public void unSafeStatementInterceptors() throws SQLException {
        ArrayList<StatementInterceptorV2> unSafedStatementInterceptors = new ArrayList(this.statementInterceptors.size());
        for (int i = 0; i < this.statementInterceptors.size(); i++) {
            unSafedStatementInterceptors.add(((NoSubInterceptorWrapper) this.statementInterceptors.get(i)).getUnderlyingInterceptor());
        }
        this.statementInterceptors = unSafedStatementInterceptors;
        if (this.io != null) {
            this.io.setStatementInterceptors(this.statementInterceptors);
        }
    }

    public void initializeSafeStatementInterceptors() throws SQLException {
        int i = 0;
        this.isClosed = false;
        List<Extension> unwrappedInterceptors = Util.loadExtensions(this, this.props, getStatementInterceptors(), "MysqlIo.BadStatementInterceptor", getExceptionInterceptor());
        this.statementInterceptors = new ArrayList(unwrappedInterceptors.size());
        while (i < unwrappedInterceptors.size()) {
            Extension interceptor = (Extension) unwrappedInterceptors.get(i);
            if (!(interceptor instanceof StatementInterceptor)) {
                this.statementInterceptors.add(new NoSubInterceptorWrapper((StatementInterceptorV2) interceptor));
            } else if (ReflectiveStatementInterceptorAdapter.getV2PostProcessMethod(interceptor.getClass()) != null) {
                this.statementInterceptors.add(new NoSubInterceptorWrapper(new ReflectiveStatementInterceptorAdapter((StatementInterceptor) interceptor)));
            } else {
                this.statementInterceptors.add(new NoSubInterceptorWrapper(new V1toV2StatementInterceptorAdapter((StatementInterceptor) interceptor)));
            }
            i++;
        }
    }

    public List<StatementInterceptorV2> getStatementInterceptorsInstances() {
        return this.statementInterceptors;
    }

    private void addToHistogram(int[] histogramCounts, long[] histogramBreakpoints, long value, int numberOfTimes, long currentLowerBound, long currentUpperBound) {
        if (histogramCounts == null) {
            createInitialHistogram(histogramBreakpoints, currentLowerBound, currentUpperBound);
            return;
        }
        for (int i = 0; i < 20; i++) {
            if (histogramBreakpoints[i] >= value) {
                histogramCounts[i] = histogramCounts[i] + numberOfTimes;
                return;
            }
        }
    }

    private void addToPerformanceHistogram(long value, int numberOfTimes) {
        checkAndCreatePerformanceHistogram();
        addToHistogram(this.perfMetricsHistCounts, this.perfMetricsHistBreakpoints, value, numberOfTimes, this.shortestQueryTimeMs == Long.MAX_VALUE ? 0 : this.shortestQueryTimeMs, this.longestQueryTimeMs);
    }

    private void addToTablesAccessedHistogram(long value, int numberOfTimes) {
        checkAndCreateTablesAccessedHistogram();
        addToHistogram(this.numTablesMetricsHistCounts, this.numTablesMetricsHistBreakpoints, value, numberOfTimes, this.minimumNumberTablesAccessed == Long.MAX_VALUE ? 0 : this.minimumNumberTablesAccessed, this.maximumNumberTablesAccessed);
    }

    private void buildCollationMapping() throws SQLException {
        SQLException sqlEx;
        ConnectionImpl this;
        Map<Integer, String> customCharset = null;
        Map<String, Integer> customMblen = null;
        if (getCacheServerConfiguration()) {
            synchronized (customIndexToCharsetMapByUrl) {
                customCharset = (Map) customIndexToCharsetMapByUrl.get(getURL());
                customMblen = (Map) customCharsetToMblenMapByUrl.get(getURL());
            }
        }
        if (customCharset == null && getDetectCustomCollations() && versionMeetsMinimum(4, 1, 0)) {
            Statement stmt = null;
            ResultSet results = null;
            try {
                customCharset = new HashMap();
                customMblen = new HashMap();
                stmt = getMetadataSafeStatement();
                results = stmt.executeQuery("SHOW COLLATION");
                while (results.next()) {
                    int collationIndex = ((Number) results.getObject(3)).intValue();
                    String charsetName = results.getString(2);
                    if (collationIndex >= 2048 || !charsetName.equals(CharsetMapping.getMysqlCharsetNameForCollationIndex(Integer.valueOf(collationIndex)))) {
                        customCharset.put(Integer.valueOf(collationIndex), charsetName);
                    }
                    if (!CharsetMapping.CHARSET_NAME_TO_CHARSET.containsKey(charsetName)) {
                        customMblen.put(charsetName, null);
                    }
                }
            } catch (SQLException ex) {
                if (ex.getErrorCode() != MysqlErrorNumbers.ER_MUST_CHANGE_PASSWORD || getDisconnectOnExpiredPasswords()) {
                    throw ex;
                }
            } catch (RuntimeException ex2) {
                try {
                    sqlEx = SQLError.createSQLException(ex2.toString(), SQLError.SQL_STATE_ILLEGAL_ARGUMENT, null);
                    sqlEx.initCause(ex2);
                    throw sqlEx;
                } catch (Throwable th) {
                    if (results != null) {
                        try {
                            results.close();
                        } catch (SQLException e) {
                        }
                    }
                    if (stmt != null) {
                        try {
                            stmt.close();
                        } catch (SQLException e2) {
                        }
                    }
                }
            } catch (SQLException ex3) {
                if (ex3.getErrorCode() == MysqlErrorNumbers.ER_MUST_CHANGE_PASSWORD) {
                    if (getDisconnectOnExpiredPasswords()) {
                    }
                }
                throw ex3;
            } catch (SQLException sqlEx2) {
                throw sqlEx2;
            }
            if (customMblen.size() > 0) {
                results = stmt.executeQuery("SHOW CHARACTER SET");
                while (results.next()) {
                    String charsetName2 = results.getString("Charset");
                    if (customMblen.containsKey(charsetName2)) {
                        customMblen.put(charsetName2, Integer.valueOf(results.getInt("Maxlen")));
                    }
                }
            }
            if (getCacheServerConfiguration()) {
                synchronized (customIndexToCharsetMapByUrl) {
                    customIndexToCharsetMapByUrl.put(getURL(), customCharset);
                    customCharsetToMblenMapByUrl.put(getURL(), customMblen);
                }
            }
            ResultSet results2 = results;
            this = this;
            if (results2 != null) {
                try {
                    results2.close();
                } catch (SQLException e3) {
                }
            }
            if (stmt != null) {
                try {
                    stmt.close();
                } catch (SQLException e4) {
                }
            }
        } else {
            this = this;
        }
        if (customCharset != null) {
            this.indexToCustomMysqlCharset = Collections.unmodifiableMap(customCharset);
        }
        if (customMblen != null) {
            this.mysqlCharsetToCustomMblen = Collections.unmodifiableMap(customMblen);
        }
    }

    /* JADX WARNING: inconsistent code. */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    private boolean canHandleAsServerPreparedStatement(java.lang.String r6) throws java.sql.SQLException {
        /*
        r5 = this;
        if (r6 == 0) goto L_0x004c;
    L_0x0002:
        r0 = r6.length();
        if (r0 != 0) goto L_0x0009;
    L_0x0008:
        goto L_0x004c;
    L_0x0009:
        r0 = r5.useServerPreparedStmts;
        if (r0 != 0) goto L_0x000f;
    L_0x000d:
        r0 = 0;
        return r0;
    L_0x000f:
        r0 = r5.getCachePreparedStatements();
        if (r0 == 0) goto L_0x0047;
    L_0x0015:
        r0 = r5.serverSideStatementCheckCache;
        monitor-enter(r0);
        r1 = r5.serverSideStatementCheckCache;	 Catch:{ all -> 0x0044 }
        r1 = r1.get(r6);	 Catch:{ all -> 0x0044 }
        r1 = (java.lang.Boolean) r1;	 Catch:{ all -> 0x0044 }
        if (r1 == 0) goto L_0x0028;
    L_0x0022:
        r2 = r1.booleanValue();	 Catch:{ all -> 0x0044 }
        monitor-exit(r0);	 Catch:{ all -> 0x0044 }
        return r2;
    L_0x0028:
        r2 = r5.canHandleAsServerPreparedStatementNoCache(r6);	 Catch:{ all -> 0x0044 }
        r3 = r6.length();	 Catch:{ all -> 0x0044 }
        r4 = r5.getPreparedStatementCacheSqlLimit();	 Catch:{ all -> 0x0044 }
        if (r3 >= r4) goto L_0x0042;
    L_0x0036:
        r3 = r5.serverSideStatementCheckCache;	 Catch:{ all -> 0x0044 }
        if (r2 == 0) goto L_0x003d;
    L_0x003a:
        r4 = java.lang.Boolean.TRUE;	 Catch:{ all -> 0x0044 }
        goto L_0x003f;
    L_0x003d:
        r4 = java.lang.Boolean.FALSE;	 Catch:{ all -> 0x0044 }
    L_0x003f:
        r3.put(r6, r4);	 Catch:{ all -> 0x0044 }
    L_0x0042:
        monitor-exit(r0);	 Catch:{ all -> 0x0044 }
        return r2;
    L_0x0044:
        r1 = move-exception;
        monitor-exit(r0);	 Catch:{ all -> 0x0044 }
        throw r1;
    L_0x0047:
        r0 = r5.canHandleAsServerPreparedStatementNoCache(r6);
        return r0;
    L_0x004c:
        r0 = 1;
        return r0;
        */
        throw new UnsupportedOperationException("Method not decompiled: com.mysql.jdbc.ConnectionImpl.canHandleAsServerPreparedStatement(java.lang.String):boolean");
    }

    private boolean canHandleAsServerPreparedStatementNoCache(String sql) throws SQLException {
        boolean z = false;
        if (StringUtils.startsWithIgnoreCaseAndNonAlphaNumeric(sql, "CALL")) {
            return false;
        }
        boolean canHandleAsStatement = true;
        if (!versionMeetsMinimum(5, 0, 7) && (StringUtils.startsWithIgnoreCaseAndNonAlphaNumeric(sql, "SELECT") || StringUtils.startsWithIgnoreCaseAndNonAlphaNumeric(sql, "DELETE") || StringUtils.startsWithIgnoreCaseAndNonAlphaNumeric(sql, "INSERT") || StringUtils.startsWithIgnoreCaseAndNonAlphaNumeric(sql, "UPDATE") || StringUtils.startsWithIgnoreCaseAndNonAlphaNumeric(sql, "REPLACE"))) {
            int statementLength = sql.length();
            int lastPosToLook = statementLength - 7;
            boolean allowBackslashEscapes = this.noBackslashEscapes ^ true;
            String quoteChar = this.useAnsiQuotes ? "\"" : "'";
            int currentPos = 0;
            boolean foundLimitWithPlaceholder = false;
            while (currentPos < lastPosToLook) {
                int limitStart = StringUtils.indexOfIgnoreCase(currentPos, sql, "LIMIT ", quoteChar, quoteChar, allowBackslashEscapes ? StringUtils.SEARCH_MODE__ALL : StringUtils.SEARCH_MODE__MRK_COM_WS);
                if (limitStart != -1) {
                    currentPos = limitStart + 7;
                    while (currentPos < statementLength) {
                        char c = sql.charAt(currentPos);
                        if (!Character.isDigit(c) && !Character.isWhitespace(c) && c != ',' && c != '?') {
                            break;
                        } else if (c == '?') {
                            foundLimitWithPlaceholder = true;
                            break;
                        } else {
                            currentPos++;
                        }
                    }
                } else {
                    break;
                }
            }
            if (!foundLimitWithPlaceholder) {
                z = true;
            }
            canHandleAsStatement = z;
        } else if (StringUtils.startsWithIgnoreCaseAndWs(sql, "XA ")) {
            canHandleAsStatement = false;
        } else if (StringUtils.startsWithIgnoreCaseAndWs(sql, "CREATE TABLE")) {
            canHandleAsStatement = false;
        } else if (StringUtils.startsWithIgnoreCaseAndWs(sql, "DO")) {
            canHandleAsStatement = false;
        } else if (StringUtils.startsWithIgnoreCaseAndWs(sql, "SET")) {
            canHandleAsStatement = false;
        } else if (StringUtils.startsWithIgnoreCaseAndWs(sql, "SHOW WARNINGS") && versionMeetsMinimum(5, 7, 2)) {
            canHandleAsStatement = false;
        } else if (sql.startsWith("/* ping */")) {
            canHandleAsStatement = false;
        }
        return canHandleAsStatement;
    }

    public void changeUser(String userName, String newPassword) throws SQLException {
        synchronized (getConnectionMutex()) {
            checkClosed();
            if (userName == null || userName.equals("")) {
                userName = "";
            }
            if (newPassword == null) {
                newPassword = "";
            }
            this.sessionMaxRows = -1;
            try {
                this.io.changeUser(userName, newPassword, this.database);
                this.user = userName;
                this.password = newPassword;
                if (versionMeetsMinimum(4, 1, 0)) {
                    configureClientCharacterSet(true);
                }
                setSessionVariables();
                setupServerForTruncationChecks();
            } catch (SQLException ex) {
                if (versionMeetsMinimum(5, 6, 13) && SQLError.SQL_STATE_INVALID_AUTH_SPEC.equals(ex.getSQLState())) {
                    cleanup(ex);
                }
                throw ex;
            }
        }
    }

    private boolean characterSetNamesMatches(String mysqlEncodingName) {
        return mysqlEncodingName != null && mysqlEncodingName.equalsIgnoreCase((String) this.serverVariables.get("character_set_client")) && mysqlEncodingName.equalsIgnoreCase((String) this.serverVariables.get("character_set_connection"));
    }

    private void checkAndCreatePerformanceHistogram() {
        if (this.perfMetricsHistCounts == null) {
            this.perfMetricsHistCounts = new int[20];
        }
        if (this.perfMetricsHistBreakpoints == null) {
            this.perfMetricsHistBreakpoints = new long[20];
        }
    }

    private void checkAndCreateTablesAccessedHistogram() {
        if (this.numTablesMetricsHistCounts == null) {
            this.numTablesMetricsHistCounts = new int[20];
        }
        if (this.numTablesMetricsHistBreakpoints == null) {
            this.numTablesMetricsHistBreakpoints = new long[20];
        }
    }

    public void checkClosed() throws SQLException {
        if (this.isClosed) {
            throwConnectionClosedException();
        }
    }

    public void throwConnectionClosedException() throws SQLException {
        SQLException ex = SQLError.createSQLException("No operations allowed after connection closed.", SQLError.SQL_STATE_CONNECTION_NOT_OPEN, getExceptionInterceptor());
        if (this.forceClosedReason != null) {
            ex.initCause(this.forceClosedReason);
        }
        throw ex;
    }

    private void checkServerEncoding() throws SQLException {
        if (!getUseUnicode() || getEncoding() == null) {
            String serverCharset = (String) this.serverVariables.get("character_set");
            if (serverCharset == null) {
                serverCharset = (String) this.serverVariables.get("character_set_server");
            }
            String mappedServerEncoding = null;
            if (serverCharset != null) {
                try {
                    mappedServerEncoding = CharsetMapping.getJavaEncodingForMysqlCharset(serverCharset);
                } catch (RuntimeException ex) {
                    SQLException sqlEx = SQLError.createSQLException(ex.toString(), SQLError.SQL_STATE_ILLEGAL_ARGUMENT, null);
                    sqlEx.initCause(ex);
                    throw sqlEx;
                }
            }
            if (getUseUnicode() || mappedServerEncoding == null || getCharsetConverter(mappedServerEncoding) == null) {
                if (serverCharset != null) {
                    if (mappedServerEncoding == null && Character.isLowerCase(serverCharset.charAt(0))) {
                        char[] ach = serverCharset.toCharArray();
                        ach[0] = Character.toUpperCase(serverCharset.charAt(0));
                        setEncoding(new String(ach));
                    }
                    if (mappedServerEncoding == null) {
                        StringBuilder stringBuilder = new StringBuilder();
                        stringBuilder.append("Unknown character encoding on server '");
                        stringBuilder.append(serverCharset);
                        stringBuilder.append("', use 'characterEncoding=' property ");
                        stringBuilder.append(" to provide correct mapping");
                        throw SQLError.createSQLException(stringBuilder.toString(), SQLError.SQL_STATE_INVALID_CONNECTION_ATTRIBUTE, getExceptionInterceptor());
                    }
                    try {
                        StringUtils.getBytes("abc", mappedServerEncoding);
                        setEncoding(mappedServerEncoding);
                        setUseUnicode(true);
                    } catch (UnsupportedEncodingException e) {
                        StringBuilder stringBuilder2 = new StringBuilder();
                        stringBuilder2.append("The driver can not map the character encoding '");
                        stringBuilder2.append(getEncoding());
                        stringBuilder2.append("' that your server is using ");
                        stringBuilder2.append("to a character encoding your JVM understands. You can specify this mapping manually by adding \"useUnicode=true\" ");
                        stringBuilder2.append("as well as \"characterEncoding=[an_encoding_your_jvm_understands]\" to your JDBC URL.");
                        throw SQLError.createSQLException(stringBuilder2.toString(), "0S100", getExceptionInterceptor());
                    }
                }
                return;
            }
            setUseUnicode(true);
            setEncoding(mappedServerEncoding);
        }
    }

    private void checkTransactionIsolationLevel() throws SQLException {
        String txIsolationName = (!versionMeetsMinimum(4, 0, 3) || versionMeetsMinimum(8, 0, 3)) ? "transaction_isolation" : "tx_isolation";
        String s = (String) this.serverVariables.get(txIsolationName);
        if (s != null) {
            Integer intTI = (Integer) mapTransIsolationNameToValue.get(s);
            if (intTI != null) {
                this.isolationLevel = intTI.intValue();
            }
        }
    }

    public void abortInternal() throws SQLException {
        if (this.io != null) {
            try {
                this.io.forceClose();
                this.io.releaseResources();
            } catch (Throwable th) {
            }
            this.io = null;
        }
        this.isClosed = true;
    }

    private void cleanup(Throwable whyCleanedUp) {
        try {
            if (this.io != null) {
                if (isClosed()) {
                    this.io.forceClose();
                } else {
                    realClose(false, false, false, whyCleanedUp);
                }
            }
        } catch (SQLException e) {
        }
        this.isClosed = true;
    }

    @Deprecated
    public void clearHasTriedMaster() {
        this.hasTriedMasterFlag = false;
    }

    public void clearWarnings() throws SQLException {
    }

    public PreparedStatement clientPrepareStatement(String sql) throws SQLException {
        return clientPrepareStatement(sql, 1003, 1007);
    }

    public PreparedStatement clientPrepareStatement(String sql, int autoGenKeyIndex) throws SQLException {
        PreparedStatement pStmt = clientPrepareStatement(sql);
        PreparedStatement preparedStatement = (PreparedStatement) pStmt;
        boolean z = true;
        if (autoGenKeyIndex != 1) {
            z = false;
        }
        preparedStatement.setRetrieveGeneratedKeys(z);
        return pStmt;
    }

    public PreparedStatement clientPrepareStatement(String sql, int resultSetType, int resultSetConcurrency) throws SQLException {
        return clientPrepareStatement(sql, resultSetType, resultSetConcurrency, true);
    }

    public PreparedStatement clientPrepareStatement(String sql, int resultSetType, int resultSetConcurrency, boolean processEscapeCodesIfNeeded) throws SQLException {
        PreparedStatement pStmt;
        checkClosed();
        String nativeSql = (processEscapeCodesIfNeeded && getProcessEscapeCodesForPrepStmts()) ? nativeSQL(sql) : sql;
        if (getCachePreparedStatements()) {
            ParseInfo pStmtInfo = (ParseInfo) this.cachedPreparedStatementParams.get(nativeSql);
            if (pStmtInfo == null) {
                pStmt = PreparedStatement.getInstance(getMultiHostSafeProxy(), nativeSql, this.database);
                this.cachedPreparedStatementParams.put(nativeSql, pStmt.getParseInfo());
            } else {
                pStmt = PreparedStatement.getInstance(getMultiHostSafeProxy(), nativeSql, this.database, pStmtInfo);
            }
        } else {
            pStmt = PreparedStatement.getInstance(getMultiHostSafeProxy(), nativeSql, this.database);
        }
        pStmt.setResultSetType(resultSetType);
        pStmt.setResultSetConcurrency(resultSetConcurrency);
        return pStmt;
    }

    public PreparedStatement clientPrepareStatement(String sql, int[] autoGenKeyIndexes) throws SQLException {
        PreparedStatement pStmt = (PreparedStatement) clientPrepareStatement(sql);
        boolean z = autoGenKeyIndexes != null && autoGenKeyIndexes.length > 0;
        pStmt.setRetrieveGeneratedKeys(z);
        return pStmt;
    }

    public PreparedStatement clientPrepareStatement(String sql, String[] autoGenKeyColNames) throws SQLException {
        PreparedStatement pStmt = (PreparedStatement) clientPrepareStatement(sql);
        boolean z = autoGenKeyColNames != null && autoGenKeyColNames.length > 0;
        pStmt.setRetrieveGeneratedKeys(z);
        return pStmt;
    }

    public PreparedStatement clientPrepareStatement(String sql, int resultSetType, int resultSetConcurrency, int resultSetHoldability) throws SQLException {
        return clientPrepareStatement(sql, resultSetType, resultSetConcurrency, true);
    }

    public void close() throws SQLException {
        synchronized (getConnectionMutex()) {
            if (this.connectionLifecycleInterceptors != null) {
                new IterateBlock<Extension>(this.connectionLifecycleInterceptors.iterator()) {
                    void forEach(Extension each) throws SQLException {
                        ((ConnectionLifecycleInterceptor) each).close();
                    }
                }.doForAll();
            }
            realClose(true, true, false, null);
        }
    }

    private void closeAllOpenStatements() throws SQLException {
        SQLException postponedException = null;
        Iterator i$ = this.openStatements.iterator();
        while (i$.hasNext()) {
            try {
                ((StatementImpl) ((Statement) i$.next())).realClose(false, true);
            } catch (SQLException sqlEx) {
                postponedException = sqlEx;
            }
        }
        if (postponedException != null) {
            throw postponedException;
        }
    }

    private void closeStatement(Statement stmt) {
        if (stmt != null) {
            try {
                stmt.close();
            } catch (SQLException e) {
            }
        }
    }

    public void commit() throws SQLException {
        ConnectionImpl connectionImpl;
        Throwable th;
        synchronized (getConnectionMutex()) {
            try {
                checkClosed();
                try {
                    if (this.connectionLifecycleInterceptors != null) {
                        IterateBlock<Extension> iter = new IterateBlock<Extension>(this.connectionLifecycleInterceptors.iterator()) {
                            void forEach(Extension each) throws SQLException {
                                if (!((ConnectionLifecycleInterceptor) each).commit()) {
                                    this.stopIterating = true;
                                }
                            }
                        };
                        iter.doForAll();
                        if (!iter.fullIteration()) {
                            this.needsPing = getReconnectAtTxEnd();
                            return;
                        }
                    }
                    if (!this.autoCommit || getRelaxAutoCommit()) {
                        if (this.transactionsSupported) {
                            if (getUseLocalTransactionState() && versionMeetsMinimum(5, 0, 0) && !this.io.inTransactionOnServer()) {
                                this.needsPing = getReconnectAtTxEnd();
                                return;
                            }
                            execSQL(null, "commit", -1, null, 1003, 1007, false, this.database, null, false);
                        }
                        try {
                            this.needsPing = getReconnectAtTxEnd();
                            return;
                        } catch (Throwable th2) {
                            Throwable th3 = th2;
                            connectionImpl = this;
                            th = th3;
                            throw th;
                        }
                    }
                    throw SQLError.createSQLException("Can't call commit when autocommit=true", getExceptionInterceptor());
                } catch (SQLException sqlException) {
                    if (SQLError.SQL_STATE_COMMUNICATION_LINK_FAILURE.equals(sqlException.getSQLState())) {
                        throw SQLError.createSQLException("Communications link failure during commit(). Transaction resolution unknown.", SQLError.SQL_STATE_TRANSACTION_RESOLUTION_UNKNOWN, getExceptionInterceptor());
                    }
                    throw sqlException;
                } catch (Throwable th4) {
                    th = th4;
                    throw th;
                }
            } catch (Throwable th5) {
                th = th5;
                connectionImpl = this;
                throw th;
            }
        }
    }

    private void configureCharsetProperties() throws SQLException {
        if (getEncoding() != null) {
            try {
                StringUtils.getBytes("abc", getEncoding());
            } catch (UnsupportedEncodingException e) {
                String oldEncoding = getEncoding();
                try {
                    setEncoding(CharsetMapping.getJavaEncodingForMysqlCharset(oldEncoding));
                    if (getEncoding() == null) {
                        StringBuilder stringBuilder = new StringBuilder();
                        stringBuilder.append("Java does not support the MySQL character encoding '");
                        stringBuilder.append(oldEncoding);
                        stringBuilder.append("'.");
                        throw SQLError.createSQLException(stringBuilder.toString(), SQLError.SQL_STATE_INVALID_CONNECTION_ATTRIBUTE, getExceptionInterceptor());
                    }
                    try {
                        StringUtils.getBytes("abc", getEncoding());
                    } catch (UnsupportedEncodingException e2) {
                        StringBuilder stringBuilder2 = new StringBuilder();
                        stringBuilder2.append("Unsupported character encoding '");
                        stringBuilder2.append(getEncoding());
                        stringBuilder2.append("'.");
                        throw SQLError.createSQLException(stringBuilder2.toString(), SQLError.SQL_STATE_INVALID_CONNECTION_ATTRIBUTE, getExceptionInterceptor());
                    }
                } catch (RuntimeException ex) {
                    SQLException sqlEx = SQLError.createSQLException(ex.toString(), SQLError.SQL_STATE_ILLEGAL_ARGUMENT, null);
                    sqlEx.initCause(ex);
                    throw sqlEx;
                }
            }
        }
    }

    private boolean configureClientCharacterSet(boolean dontCheckServerMatch) throws SQLException {
        boolean characterSetAlreadyConfigured;
        StringBuilder stringBuilder;
        SQLException ex;
        String realJavaEncoding;
        Throwable th;
        Throwable th2;
        String str;
        StringBuilder setBuf;
        String mysqlEncodingName;
        String str2;
        boolean characterSetAlreadyConfigured2;
        CharsetEncoder enc;
        CharBuffer cbuf;
        ConnectionImpl connectionImpl = this;
        String realJavaEncoding2 = getEncoding();
        boolean characterSetAlreadyConfigured3 = false;
        try {
            ByteBuffer bbuf;
            if (versionMeetsMinimum(4, 1, 0)) {
                characterSetAlreadyConfigured = true;
                try {
                    String str3;
                    StringBuilder stringBuilder2;
                    boolean useutf8mb4;
                    StringBuilder setBuf2;
                    setUseUnicode(true);
                    configureCharsetProperties();
                    String realJavaEncoding3 = getEncoding();
                    try {
                        if (!(connectionImpl.props == null || connectionImpl.props.getProperty("com.mysql.jdbc.faultInjection.serverCharsetIndex") == null)) {
                            connectionImpl.io.serverCharsetIndex = Integer.parseInt(connectionImpl.props.getProperty("com.mysql.jdbc.faultInjection.serverCharsetIndex"));
                        }
                        realJavaEncoding2 = CharsetMapping.getJavaEncodingForCollationIndex(Integer.valueOf(connectionImpl.io.serverCharsetIndex));
                        if (realJavaEncoding2 == null || realJavaEncoding2.length() == 0) {
                            if (realJavaEncoding3 != null) {
                                setEncoding(realJavaEncoding3);
                            } else {
                                stringBuilder = new StringBuilder();
                                stringBuilder.append("Unknown initial character set index '");
                                stringBuilder.append(connectionImpl.io.serverCharsetIndex);
                                stringBuilder.append("' received from server. Initial client character set can be forced via the 'characterEncoding' property.");
                                throw SQLError.createSQLException(stringBuilder.toString(), SQLError.SQL_STATE_GENERAL_ERROR, getExceptionInterceptor());
                            }
                        }
                        if (versionMeetsMinimum(4, 1, 0) && "ISO8859_1".equalsIgnoreCase(realJavaEncoding2)) {
                            realJavaEncoding2 = "Cp1252";
                        }
                        if ("UnicodeBig".equalsIgnoreCase(realJavaEncoding2) || "UTF-16".equalsIgnoreCase(realJavaEncoding2) || "UTF-16LE".equalsIgnoreCase(realJavaEncoding2) || "UTF-32".equalsIgnoreCase(realJavaEncoding2)) {
                            realJavaEncoding2 = "UTF-8";
                        }
                        setEncoding(realJavaEncoding2);
                    } catch (SQLException e) {
                        ex = e;
                        if (ex.getErrorCode() != MysqlErrorNumbers.ER_MUST_CHANGE_PASSWORD || getDisconnectOnExpiredPasswords()) {
                            throw ex;
                        }
                    } catch (SQLException e2) {
                        ex = e2;
                        if (ex.getErrorCode() == MysqlErrorNumbers.ER_MUST_CHANGE_PASSWORD) {
                            if (getDisconnectOnExpiredPasswords()) {
                            }
                        }
                        throw ex;
                    } catch (SQLException e22) {
                        ex = e22;
                        if (ex.getErrorCode() == MysqlErrorNumbers.ER_MUST_CHANGE_PASSWORD) {
                            if (getDisconnectOnExpiredPasswords()) {
                            }
                        }
                        throw ex;
                    } catch (ArrayIndexOutOfBoundsException e3) {
                        ArrayIndexOutOfBoundsException outOfBoundsEx = e3;
                        if (realJavaEncoding3 != null) {
                            setEncoding(realJavaEncoding3);
                        } else {
                            realJavaEncoding = realJavaEncoding3;
                            stringBuilder = new StringBuilder();
                            stringBuilder.append("Unknown initial character set index '");
                            stringBuilder.append(connectionImpl.io.serverCharsetIndex);
                            stringBuilder.append("' received from server. Initial client character set can be forced via the 'characterEncoding' property.");
                            throw SQLError.createSQLException(stringBuilder.toString(), SQLError.SQL_STATE_GENERAL_ERROR, getExceptionInterceptor());
                        }
                    } catch (SQLException e222) {
                        throw e222;
                    } catch (RuntimeException e4) {
                        RuntimeException ex2 = e4;
                        SQLException sqlEx = SQLError.createSQLException(ex2.toString(), SQLError.SQL_STATE_ILLEGAL_ARGUMENT, null);
                        sqlEx.initCause(ex2);
                        throw sqlEx;
                    } catch (Throwable th3) {
                        th2 = th3;
                        th = th2;
                        setEncoding(realJavaEncoding);
                        throw th;
                    }
                    if (getEncoding() == null) {
                        setEncoding("ISO8859_1");
                    }
                    if (!getUseUnicode()) {
                        realJavaEncoding = realJavaEncoding3;
                    } else if (realJavaEncoding3 != null) {
                        if (realJavaEncoding3.equalsIgnoreCase("UTF-8")) {
                            realJavaEncoding = realJavaEncoding3;
                        } else if (realJavaEncoding3.equalsIgnoreCase("UTF8")) {
                            realJavaEncoding = realJavaEncoding3;
                        } else {
                            String mysqlCharsetName = CharsetMapping.getMysqlCharsetForJavaEncoding(realJavaEncoding3.toUpperCase(Locale.ENGLISH), connectionImpl);
                            if (mysqlCharsetName != null) {
                                if (!dontCheckServerMatch) {
                                    if (characterSetNamesMatches(mysqlCharsetName)) {
                                        str3 = mysqlCharsetName;
                                        realJavaEncoding = realJavaEncoding3;
                                    }
                                }
                                stringBuilder2 = new StringBuilder();
                                stringBuilder2.append("SET NAMES ");
                                stringBuilder2.append(mysqlCharsetName);
                                str3 = mysqlCharsetName;
                                int i = 1820;
                                i = 5;
                                realJavaEncoding = realJavaEncoding3;
                                execSQL(null, stringBuilder2.toString(), -1, null, 1003, 1007, false, connectionImpl.database, null, null);
                                connectionImpl.serverVariables.put("character_set_client", str3);
                                connectionImpl.serverVariables.put("character_set_connection", str3);
                            } else {
                                realJavaEncoding = realJavaEncoding3;
                            }
                            setEncoding(realJavaEncoding);
                        }
                        boolean utf8mb4Supported = versionMeetsMinimum(5, 5, 2);
                        boolean z = utf8mb4Supported && CharsetMapping.UTF8MB4_INDEXES.contains(Integer.valueOf(connectionImpl.io.serverCharsetIndex));
                        useutf8mb4 = z;
                        if (getUseOldUTF8Behavior()) {
                            execSQL(null, "SET NAMES latin1", -1, null, 1003, 1007, false, connectionImpl.database, null, false);
                            connectionImpl.serverVariables.put("character_set_client", CharsetMapping.NOT_USED);
                            connectionImpl.serverVariables.put("character_set_connection", CharsetMapping.NOT_USED);
                        } else if (dontCheckServerMatch || !characterSetNamesMatches("utf8") || (utf8mb4Supported && !characterSetNamesMatches("utf8mb4"))) {
                            stringBuilder2 = new StringBuilder();
                            stringBuilder2.append("SET NAMES ");
                            stringBuilder2.append(useutf8mb4 ? "utf8mb4" : "utf8");
                            execSQL(null, stringBuilder2.toString(), -1, null, 1003, 1007, false, connectionImpl.database, null, false);
                            connectionImpl.serverVariables.put("character_set_client", useutf8mb4 ? "utf8mb4" : "utf8");
                            connectionImpl.serverVariables.put("character_set_connection", useutf8mb4 ? "utf8mb4" : "utf8");
                        }
                        setEncoding(realJavaEncoding);
                    } else {
                        realJavaEncoding = realJavaEncoding3;
                        if (getEncoding() != null) {
                            realJavaEncoding2 = getServerCharset();
                            if (getUseOldUTF8Behavior()) {
                                realJavaEncoding2 = CharsetMapping.NOT_USED;
                            }
                            characterSetAlreadyConfigured3 = false;
                            if ("ucs2".equalsIgnoreCase(realJavaEncoding2) || "utf16".equalsIgnoreCase(realJavaEncoding2) || "utf16le".equalsIgnoreCase(realJavaEncoding2) || "utf32".equalsIgnoreCase(realJavaEncoding2)) {
                                realJavaEncoding2 = "utf8";
                                characterSetAlreadyConfigured3 = true;
                                if (getCharacterSetResults() == null) {
                                    setCharacterSetResults("UTF-8");
                                }
                            }
                            str3 = realJavaEncoding2;
                            useutf8mb4 = characterSetAlreadyConfigured3;
                            if (dontCheckServerMatch || !characterSetNamesMatches(str3) || useutf8mb4) {
                                stringBuilder2 = new StringBuilder();
                                stringBuilder2.append("SET NAMES ");
                                stringBuilder2.append(str3);
                                execSQL(null, stringBuilder2.toString(), -1, null, 1003, 1007, false, connectionImpl.database, null, false);
                                connectionImpl.serverVariables.put("character_set_client", str3);
                                connectionImpl.serverVariables.put("character_set_connection", str3);
                            }
                            realJavaEncoding = getEncoding();
                        }
                    }
                    realJavaEncoding2 = null;
                    characterSetAlreadyConfigured3 = false;
                    if (connectionImpl.serverVariables != null) {
                        boolean z2;
                        realJavaEncoding2 = (String) connectionImpl.serverVariables.get("character_set_results");
                        if (!(realJavaEncoding2 == null || "NULL".equalsIgnoreCase(realJavaEncoding2))) {
                            if (realJavaEncoding2.length() != 0) {
                                z2 = false;
                                characterSetAlreadyConfigured3 = z2;
                            }
                        }
                        z2 = true;
                        characterSetAlreadyConfigured3 = z2;
                    }
                    str3 = realJavaEncoding2;
                    useutf8mb4 = characterSetAlreadyConfigured3;
                    if (getCharacterSetResults() == null) {
                        if (useutf8mb4) {
                            connectionImpl.serverVariables.put(JDBC_LOCAL_CHARACTER_SET_RESULTS, str3);
                        } else {
                            execSQL(null, "SET character_set_results = NULL", -1, null, 1003, 1007, false, connectionImpl.database, null, false);
                            connectionImpl.serverVariables.put(JDBC_LOCAL_CHARACTER_SET_RESULTS, null);
                        }
                        str = realJavaEncoding;
                    } else {
                        if (getUseOldUTF8Behavior()) {
                            execSQL(null, "SET NAMES latin1", -1, null, 1003, 1007, false, connectionImpl.database, null, false);
                            connectionImpl.serverVariables.put("character_set_client", CharsetMapping.NOT_USED);
                            connectionImpl.serverVariables.put("character_set_connection", CharsetMapping.NOT_USED);
                        }
                        try {
                            String mysqlEncodingName2;
                            realJavaEncoding3 = getCharacterSetResults();
                            if (!"UTF-8".equalsIgnoreCase(realJavaEncoding3)) {
                                if (!"UTF8".equalsIgnoreCase(realJavaEncoding3)) {
                                    if ("null".equalsIgnoreCase(realJavaEncoding3)) {
                                        realJavaEncoding2 = "NULL";
                                    } else {
                                        realJavaEncoding2 = CharsetMapping.getMysqlCharsetForJavaEncoding(realJavaEncoding3.toUpperCase(Locale.ENGLISH), connectionImpl);
                                    }
                                    mysqlEncodingName2 = realJavaEncoding2;
                                    if (mysqlEncodingName2 == null) {
                                        stringBuilder2 = new StringBuilder();
                                        stringBuilder2.append("Can't map ");
                                        stringBuilder2.append(realJavaEncoding3);
                                        stringBuilder2.append(" given for characterSetResults to a supported MySQL encoding.");
                                        throw SQLError.createSQLException(stringBuilder2.toString(), SQLError.SQL_STATE_ILLEGAL_ARGUMENT, getExceptionInterceptor());
                                    } else if (mysqlEncodingName2.equalsIgnoreCase((String) connectionImpl.serverVariables.get("character_set_results"))) {
                                        setBuf = new StringBuilder("SET character_set_results = ".length() + mysqlEncodingName2.length());
                                        setBuf.append("SET character_set_results = ");
                                        setBuf.append(mysqlEncodingName2);
                                        try {
                                            mysqlEncodingName = mysqlEncodingName2;
                                            str = realJavaEncoding;
                                            realJavaEncoding = realJavaEncoding3;
                                            try {
                                                execSQL(null, setBuf.toString(), -1, null, 1003, 1007, false, connectionImpl.database, null, null);
                                            } catch (SQLException e2222) {
                                                ex = e2222;
                                                try {
                                                    if (ex.getErrorCode() != MysqlErrorNumbers.ER_MUST_CHANGE_PASSWORD) {
                                                        if (getDisconnectOnExpiredPasswords()) {
                                                            str2 = mysqlEncodingName;
                                                        }
                                                        connectionImpl.serverVariables.put(JDBC_LOCAL_CHARACTER_SET_RESULTS, mysqlEncodingName);
                                                        if (versionMeetsMinimum(5, 5, 0)) {
                                                            connectionImpl.errorMessageEncoding = realJavaEncoding;
                                                        }
                                                        if (getConnectionCollation() != null) {
                                                            setBuf2 = new StringBuilder("SET collation_connection = ".length() + getConnectionCollation().length());
                                                            setBuf2.append("SET collation_connection = ");
                                                            setBuf2.append(getConnectionCollation());
                                                            execSQL(null, setBuf2.toString(), -1, null, 1003, 1007, false, connectionImpl.database, null, null);
                                                        }
                                                        characterSetAlreadyConfigured3 = dontCheckServerMatch;
                                                        characterSetAlreadyConfigured2 = characterSetAlreadyConfigured;
                                                        setEncoding(str);
                                                        enc = Charset.forName(getEncoding()).newEncoder();
                                                        cbuf = CharBuffer.allocate(1);
                                                        bbuf = ByteBuffer.allocate(1);
                                                        cbuf.put("¥");
                                                        cbuf.position(0);
                                                        enc.encode(cbuf, bbuf, true);
                                                        if (bbuf.get(0) == (byte) 92) {
                                                            cbuf.clear();
                                                            bbuf.clear();
                                                            cbuf.put("₩");
                                                            cbuf.position(0);
                                                            enc.encode(cbuf, bbuf, true);
                                                            if (bbuf.get(0) == (byte) 92) {
                                                                realJavaEncoding.requiresEscapingEncoder = true;
                                                            }
                                                        } else {
                                                            realJavaEncoding.requiresEscapingEncoder = true;
                                                        }
                                                        return characterSetAlreadyConfigured2;
                                                    }
                                                    throw ex;
                                                } catch (SQLException e22222) {
                                                    ex = e22222;
                                                    if (ex.getErrorCode() != MysqlErrorNumbers.ER_MUST_CHANGE_PASSWORD || getDisconnectOnExpiredPasswords()) {
                                                        throw ex;
                                                    }
                                                } catch (Throwable th22) {
                                                    th = th22;
                                                    realJavaEncoding = str;
                                                    setEncoding(realJavaEncoding);
                                                    throw th;
                                                }
                                            }
                                        } catch (SQLException e222222) {
                                            StringBuilder stringBuilder3 = setBuf;
                                            mysqlEncodingName = mysqlEncodingName2;
                                            str = realJavaEncoding;
                                            realJavaEncoding = realJavaEncoding3;
                                            ex = e222222;
                                            if (ex.getErrorCode() != MysqlErrorNumbers.ER_MUST_CHANGE_PASSWORD) {
                                            } else {
                                                if (getDisconnectOnExpiredPasswords()) {
                                                    str2 = mysqlEncodingName;
                                                }
                                                connectionImpl.serverVariables.put(JDBC_LOCAL_CHARACTER_SET_RESULTS, mysqlEncodingName);
                                                if (versionMeetsMinimum(5, 5, 0)) {
                                                    connectionImpl.errorMessageEncoding = realJavaEncoding;
                                                }
                                                if (getConnectionCollation() != null) {
                                                    setBuf2 = new StringBuilder("SET collation_connection = ".length() + getConnectionCollation().length());
                                                    setBuf2.append("SET collation_connection = ");
                                                    setBuf2.append(getConnectionCollation());
                                                    execSQL(null, setBuf2.toString(), -1, null, 1003, 1007, false, connectionImpl.database, null, null);
                                                }
                                                characterSetAlreadyConfigured3 = dontCheckServerMatch;
                                                characterSetAlreadyConfigured2 = characterSetAlreadyConfigured;
                                                setEncoding(str);
                                                enc = Charset.forName(getEncoding()).newEncoder();
                                                cbuf = CharBuffer.allocate(1);
                                                bbuf = ByteBuffer.allocate(1);
                                                cbuf.put("¥");
                                                cbuf.position(0);
                                                enc.encode(cbuf, bbuf, true);
                                                if (bbuf.get(0) == (byte) 92) {
                                                    realJavaEncoding.requiresEscapingEncoder = true;
                                                } else {
                                                    cbuf.clear();
                                                    bbuf.clear();
                                                    cbuf.put("₩");
                                                    cbuf.position(0);
                                                    enc.encode(cbuf, bbuf, true);
                                                    if (bbuf.get(0) == (byte) 92) {
                                                        realJavaEncoding.requiresEscapingEncoder = true;
                                                    }
                                                }
                                                return characterSetAlreadyConfigured2;
                                            }
                                            throw ex;
                                        }
                                        connectionImpl.serverVariables.put(JDBC_LOCAL_CHARACTER_SET_RESULTS, mysqlEncodingName);
                                        if (versionMeetsMinimum(5, 5, 0)) {
                                            connectionImpl.errorMessageEncoding = realJavaEncoding;
                                        }
                                    } else {
                                        str = realJavaEncoding;
                                        connectionImpl.serverVariables.put(JDBC_LOCAL_CHARACTER_SET_RESULTS, str3);
                                    }
                                }
                            }
                            realJavaEncoding2 = "utf8";
                            mysqlEncodingName2 = realJavaEncoding2;
                            if (mysqlEncodingName2 == null) {
                                stringBuilder2 = new StringBuilder();
                                stringBuilder2.append("Can't map ");
                                stringBuilder2.append(realJavaEncoding3);
                                stringBuilder2.append(" given for characterSetResults to a supported MySQL encoding.");
                                throw SQLError.createSQLException(stringBuilder2.toString(), SQLError.SQL_STATE_ILLEGAL_ARGUMENT, getExceptionInterceptor());
                            } else if (mysqlEncodingName2.equalsIgnoreCase((String) connectionImpl.serverVariables.get("character_set_results"))) {
                                str = realJavaEncoding;
                                connectionImpl.serverVariables.put(JDBC_LOCAL_CHARACTER_SET_RESULTS, str3);
                            } else {
                                setBuf = new StringBuilder("SET character_set_results = ".length() + mysqlEncodingName2.length());
                                setBuf.append("SET character_set_results = ");
                                setBuf.append(mysqlEncodingName2);
                                mysqlEncodingName = mysqlEncodingName2;
                                str = realJavaEncoding;
                                realJavaEncoding = realJavaEncoding3;
                                execSQL(null, setBuf.toString(), -1, null, 1003, 1007, false, connectionImpl.database, null, null);
                                connectionImpl.serverVariables.put(JDBC_LOCAL_CHARACTER_SET_RESULTS, mysqlEncodingName);
                                if (versionMeetsMinimum(5, 5, 0)) {
                                    connectionImpl.errorMessageEncoding = realJavaEncoding;
                                }
                            }
                        } catch (Throwable th222) {
                            str = realJavaEncoding;
                            th = th222;
                            setEncoding(realJavaEncoding);
                            throw th;
                        }
                    }
                    if (getConnectionCollation() != null) {
                        setBuf2 = new StringBuilder("SET collation_connection = ".length() + getConnectionCollation().length());
                        setBuf2.append("SET collation_connection = ");
                        setBuf2.append(getConnectionCollation());
                        execSQL(null, setBuf2.toString(), -1, null, 1003, 1007, false, connectionImpl.database, null, null);
                    }
                } catch (Throwable th4) {
                    th222 = th4;
                    realJavaEncoding = realJavaEncoding2;
                    th = th222;
                    setEncoding(realJavaEncoding);
                    throw th;
                }
            }
            characterSetAlreadyConfigured = characterSetAlreadyConfigured3;
            str = getEncoding();
            characterSetAlreadyConfigured3 = dontCheckServerMatch;
            characterSetAlreadyConfigured2 = characterSetAlreadyConfigured;
            setEncoding(str);
            try {
                enc = Charset.forName(getEncoding()).newEncoder();
                cbuf = CharBuffer.allocate(1);
                bbuf = ByteBuffer.allocate(1);
                cbuf.put("¥");
                cbuf.position(0);
                enc.encode(cbuf, bbuf, true);
                if (bbuf.get(0) == (byte) 92) {
                    realJavaEncoding.requiresEscapingEncoder = true;
                } else {
                    cbuf.clear();
                    bbuf.clear();
                    cbuf.put("₩");
                    cbuf.position(0);
                    enc.encode(cbuf, bbuf, true);
                    if (bbuf.get(0) == (byte) 92) {
                        realJavaEncoding.requiresEscapingEncoder = true;
                    }
                }
            } catch (UnsupportedCharsetException e5) {
                UnsupportedCharsetException ucex = e5;
                try {
                    if (StringUtils.getBytes("¥", getEncoding())[0] == (byte) 92) {
                        realJavaEncoding.requiresEscapingEncoder = true;
                    } else if (StringUtils.getBytes("₩", getEncoding())[0] == (byte) 92) {
                        realJavaEncoding.requiresEscapingEncoder = true;
                    }
                } catch (Throwable th2222) {
                    Throwable ueex = th2222;
                    StringBuilder stringBuilder4 = new StringBuilder();
                    stringBuilder4.append("Unable to use encoding: ");
                    stringBuilder4.append(getEncoding());
                    throw SQLError.createSQLException(stringBuilder4.toString(), SQLError.SQL_STATE_GENERAL_ERROR, ueex, getExceptionInterceptor());
                }
            }
            return characterSetAlreadyConfigured2;
        } catch (Throwable th5) {
            th2222 = th5;
            realJavaEncoding = realJavaEncoding2;
            characterSetAlreadyConfigured = characterSetAlreadyConfigured3;
            th = th2222;
            setEncoding(realJavaEncoding);
            throw th;
        }
    }

    private void configureTimezone() throws SQLException {
        String configuredTimeZoneOnServer = (String) this.serverVariables.get("timezone");
        if (configuredTimeZoneOnServer == null) {
            configuredTimeZoneOnServer = (String) this.serverVariables.get("time_zone");
            if ("SYSTEM".equalsIgnoreCase(configuredTimeZoneOnServer)) {
                configuredTimeZoneOnServer = (String) this.serverVariables.get("system_time_zone");
            }
        }
        String canonicalTimezone = getServerTimezone();
        if ((getUseTimezone() || !getUseLegacyDatetimeCode()) && configuredTimeZoneOnServer != null && (canonicalTimezone == null || StringUtils.isEmptyOrWhitespaceOnly(canonicalTimezone))) {
            try {
                canonicalTimezone = TimeUtil.getCanonicalTimezone(configuredTimeZoneOnServer, getExceptionInterceptor());
            } catch (IllegalArgumentException iae) {
                throw SQLError.createSQLException(iae.getMessage(), SQLError.SQL_STATE_GENERAL_ERROR, getExceptionInterceptor());
            }
        }
        if (canonicalTimezone != null && canonicalTimezone.length() > 0) {
            this.serverTimezoneTZ = TimeZone.getTimeZone(canonicalTimezone);
            if (canonicalTimezone.equalsIgnoreCase("GMT") || !this.serverTimezoneTZ.getID().equals("GMT")) {
                boolean z = !this.serverTimezoneTZ.useDaylightTime() && this.serverTimezoneTZ.getRawOffset() == 0;
                this.isServerTzUTC = z;
                return;
            }
            StringBuilder stringBuilder = new StringBuilder();
            stringBuilder.append("No timezone mapping entry for '");
            stringBuilder.append(canonicalTimezone);
            stringBuilder.append("'");
            throw SQLError.createSQLException(stringBuilder.toString(), SQLError.SQL_STATE_ILLEGAL_ARGUMENT, getExceptionInterceptor());
        }
    }

    private void createInitialHistogram(long[] breakpoints, long lowerBound, long upperBound) {
        double bucketSize = ((((double) upperBound) - ((double) lowerBound)) / 20.0d) * 1.25d;
        if (bucketSize < 1.0d) {
            bucketSize = 1.0d;
        }
        for (int i = 0; i < 20; i++) {
            breakpoints[i] = lowerBound;
            lowerBound = (long) (((double) lowerBound) + bucketSize);
        }
    }

    public void createNewIO(boolean isForReconnect) throws SQLException {
        synchronized (getConnectionMutex()) {
            Properties mergedProps = exposeAsProperties(this.props);
            if (getHighAvailability()) {
                connectWithRetries(isForReconnect, mergedProps);
                return;
            }
            connectOneTryOnly(isForReconnect, mergedProps);
        }
    }

    /* JADX WARNING: inconsistent code. */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    private void connectWithRetries(boolean r18, java.util.Properties r19) throws java.sql.SQLException {
        /*
        r17 = this;
        r1 = r17;
        r2 = r17.getInitialTimeout();
        r2 = (double) r2;
        r4 = 0;
        r5 = 0;
        r7 = 0;
        r12 = r5;
        r8 = r7;
        r9 = r8;
        r10 = r9;
        r11 = 0;
        r5 = r4;
        r4 = r10;
    L_0x0011:
        r13 = r17.getMaxReconnects();
        if (r4 >= r13) goto L_0x009e;
    L_0x0017:
        if (r5 != 0) goto L_0x009e;
    L_0x0019:
        r13 = r1.io;	 Catch:{ Exception -> 0x0084 }
        if (r13 == 0) goto L_0x0029;
    L_0x001d:
        r13 = r1.io;	 Catch:{ Exception -> 0x0023 }
        r13.forceClose();	 Catch:{ Exception -> 0x0023 }
        goto L_0x0029;
    L_0x0023:
        r0 = move-exception;
        r13 = r19;
    L_0x0026:
        r6 = r0;
        goto L_0x0089;
    L_0x0029:
        r13 = r19;
        r1.coreConnect(r13);	 Catch:{ Exception -> 0x0082 }
        r1.pingInternal(r7, r7);	 Catch:{ Exception -> 0x0082 }
        r14 = r17.getConnectionMutex();	 Catch:{ Exception -> 0x0082 }
        monitor-enter(r14);	 Catch:{ Exception -> 0x0082 }
        r6 = r1.io;	 Catch:{ all -> 0x0078 }
        r15 = r8;
        r7 = r6.getThreadId();	 Catch:{ all -> 0x0074 }
        r1.connectionId = r7;	 Catch:{ all -> 0x0074 }
        r6 = 0;
        r1.isClosed = r6;	 Catch:{ all -> 0x0074 }
        r6 = r17.getAutoCommit();	 Catch:{ all -> 0x0074 }
        r8 = r6;
        r6 = r1.isolationLevel;	 Catch:{ all -> 0x007f }
        r9 = r6;
        r6 = 0;
        r7 = r1.isReadOnly(r6);	 Catch:{ all -> 0x007f }
        r10 = r7;
        r6 = r17.getCatalog();	 Catch:{ all -> 0x007f }
        r11 = r6;
        r6 = r1.io;	 Catch:{ all -> 0x007f }
        r7 = r1.statementInterceptors;	 Catch:{ all -> 0x007f }
        r6.setStatementInterceptors(r7);	 Catch:{ all -> 0x007f }
        monitor-exit(r14);	 Catch:{ all -> 0x007f }
        r17.initializePropsFromServer();	 Catch:{ Exception -> 0x007d }
        if (r18 == 0) goto L_0x0072;
    L_0x0062:
        r1.setAutoCommit(r8);	 Catch:{ Exception -> 0x007d }
        r6 = r1.hasIsolationLevels;	 Catch:{ Exception -> 0x007d }
        if (r6 == 0) goto L_0x006c;
    L_0x0069:
        r1.setTransactionIsolation(r9);	 Catch:{ Exception -> 0x007d }
    L_0x006c:
        r1.setCatalog(r11);	 Catch:{ Exception -> 0x007d }
        r1.setReadOnly(r10);	 Catch:{ Exception -> 0x007d }
    L_0x0072:
        r5 = 1;
        goto L_0x00a0;
    L_0x0074:
        r0 = move-exception;
        r6 = r0;
        r8 = r15;
        goto L_0x007b;
    L_0x0078:
        r0 = move-exception;
        r15 = r8;
        r6 = r0;
    L_0x007b:
        monitor-exit(r14);	 Catch:{ all -> 0x007f }
        throw r6;	 Catch:{ Exception -> 0x007d }
    L_0x007d:
        r0 = move-exception;
        goto L_0x0026;
    L_0x007f:
        r0 = move-exception;
        r6 = r0;
        goto L_0x007b;
    L_0x0082:
        r0 = move-exception;
        goto L_0x0087;
    L_0x0084:
        r0 = move-exception;
        r13 = r19;
    L_0x0087:
        r15 = r8;
        r6 = r0;
    L_0x0089:
        r12 = r6;
        r5 = 0;
        if (r5 == 0) goto L_0x008e;
    L_0x008d:
        goto L_0x00a0;
    L_0x008e:
        if (r4 <= 0) goto L_0x0099;
    L_0x0090:
        r6 = (long) r2;
        r15 = 1000; // 0x3e8 float:1.401E-42 double:4.94E-321;
        r6 = r6 * r15;
        java.lang.Thread.sleep(r6);	 Catch:{ InterruptedException -> 0x0098 }
        goto L_0x0099;
    L_0x0098:
        r0 = move-exception;
    L_0x0099:
        r4 = r4 + 1;
        r7 = 0;
        goto L_0x0011;
    L_0x009e:
        r13 = r19;
    L_0x00a0:
        if (r5 != 0) goto L_0x00c4;
    L_0x00a2:
        r4 = "Connection.UnableToConnectWithRetries";
        r6 = 1;
        r6 = new java.lang.Object[r6];
        r7 = r17.getMaxReconnects();
        r7 = java.lang.Integer.valueOf(r7);
        r8 = 0;
        r6[r8] = r7;
        r4 = com.mysql.jdbc.Messages.getString(r4, r6);
        r6 = "08001";
        r7 = r17.getExceptionInterceptor();
        r4 = com.mysql.jdbc.SQLError.createSQLException(r4, r6, r7);
        r4.initCause(r12);
        throw r4;
    L_0x00c4:
        r4 = r17.getParanoid();
        if (r4 == 0) goto L_0x00d6;
    L_0x00ca:
        r4 = r17.getHighAvailability();
        if (r4 != 0) goto L_0x00d6;
    L_0x00d0:
        r4 = 0;
        r1.password = r4;
        r1.user = r4;
        goto L_0x00d7;
    L_0x00d6:
        r4 = 0;
    L_0x00d7:
        if (r18 == 0) goto L_0x010e;
    L_0x00d9:
        r6 = r1.openStatements;
        r6 = r6.iterator();
    L_0x00e0:
        r7 = r6.hasNext();
        if (r7 == 0) goto L_0x00fc;
    L_0x00e6:
        r7 = r6.next();
        r7 = (com.mysql.jdbc.Statement) r7;
        r8 = r7 instanceof com.mysql.jdbc.ServerPreparedStatement;
        if (r8 == 0) goto L_0x00fb;
    L_0x00f0:
        if (r4 != 0) goto L_0x00f8;
    L_0x00f2:
        r8 = new java.util.Stack;
        r8.<init>();
        r4 = r8;
    L_0x00f8:
        r4.add(r7);
    L_0x00fb:
        goto L_0x00e0;
    L_0x00fc:
        if (r4 == 0) goto L_0x010e;
    L_0x00fe:
        r7 = r4.isEmpty();
        if (r7 != 0) goto L_0x010e;
    L_0x0104:
        r7 = r4.pop();
        r7 = (com.mysql.jdbc.ServerPreparedStatement) r7;
        r7.rePrepare();
        goto L_0x00fe;
    L_0x010e:
        return;
        */
        throw new UnsupportedOperationException("Method not decompiled: com.mysql.jdbc.ConnectionImpl.connectWithRetries(boolean, java.util.Properties):void");
    }

    private void coreConnect(Properties mergedProps) throws SQLException, IOException {
        int newPort = 3306;
        String newHost = "localhost";
        String protocol = mergedProps.getProperty(NonRegisteringDriver.PROTOCOL_PROPERTY_KEY);
        if (protocol == null) {
            String[] parsedHostPortPair = NonRegisteringDriver.parseHostPortPair(this.hostPortPair);
            newHost = normalizeHost(parsedHostPortPair[0]);
            if (parsedHostPortPair[1] != null) {
                newPort = parsePortNumber(parsedHostPortPair[1]);
            }
        } else if ("tcp".equalsIgnoreCase(protocol)) {
            newHost = normalizeHost(mergedProps.getProperty(NonRegisteringDriver.HOST_PROPERTY_KEY));
            newPort = parsePortNumber(mergedProps.getProperty(NonRegisteringDriver.PORT_PROPERTY_KEY, "3306"));
        } else if ("pipe".equalsIgnoreCase(protocol)) {
            setSocketFactoryClassName(NamedPipeSocketFactory.class.getName());
            String path = mergedProps.getProperty(NonRegisteringDriver.PATH_PROPERTY_KEY);
            if (path != null) {
                mergedProps.setProperty(NamedPipeSocketFactory.NAMED_PIPE_PROP_NAME, path);
            }
        } else {
            newHost = normalizeHost(mergedProps.getProperty(NonRegisteringDriver.HOST_PROPERTY_KEY));
            newPort = parsePortNumber(mergedProps.getProperty(NonRegisteringDriver.PORT_PROPERTY_KEY, "3306"));
        }
        this.port = newPort;
        this.host = newHost;
        this.sessionMaxRows = -1;
        this.serverVariables = new HashMap();
        this.serverVariables.put("character_set_server", "utf8");
        this.io = new MysqlIO(newHost, newPort, mergedProps, getSocketFactoryClassName(), getProxy(), getSocketTimeout(), this.largeRowSizeThreshold.getValueAsInt());
        this.io.doHandshake(this.user, this.password, this.database);
        if (versionMeetsMinimum(5, 5, 0)) {
            this.errorMessageEncoding = this.io.getEncodingForHandshake();
        }
    }

    private String normalizeHost(String hostname) {
        if (hostname != null) {
            if (!StringUtils.isEmptyOrWhitespaceOnly(hostname)) {
                return hostname;
            }
        }
        return "localhost";
    }

    private int parsePortNumber(String portAsString) throws SQLException {
        try {
            return Integer.parseInt(portAsString);
        } catch (NumberFormatException e) {
            StringBuilder stringBuilder = new StringBuilder();
            stringBuilder.append("Illegal connection port value '");
            stringBuilder.append(portAsString);
            stringBuilder.append("'");
            throw SQLError.createSQLException(stringBuilder.toString(), SQLError.SQL_STATE_INVALID_CONNECTION_ATTRIBUTE, getExceptionInterceptor());
        }
    }

    private void connectOneTryOnly(boolean isForReconnect, Properties mergedProps) throws SQLException {
        try {
            coreConnect(mergedProps);
            this.connectionId = this.io.getThreadId();
            this.isClosed = false;
            boolean oldAutoCommit = getAutoCommit();
            int oldIsolationLevel = this.isolationLevel;
            boolean oldReadOnly = isReadOnly(false);
            String oldCatalog = getCatalog();
            this.io.setStatementInterceptors(this.statementInterceptors);
            initializePropsFromServer();
            if (isForReconnect) {
                setAutoCommit(oldAutoCommit);
                if (this.hasIsolationLevels) {
                    setTransactionIsolation(oldIsolationLevel);
                }
                setCatalog(oldCatalog);
                setReadOnly(oldReadOnly);
            }
        } catch (Exception EEE) {
            if (!(EEE instanceof SQLException) || ((SQLException) EEE).getErrorCode() != MysqlErrorNumbers.ER_MUST_CHANGE_PASSWORD || getDisconnectOnExpiredPasswords()) {
                if (this.io != null) {
                    this.io.forceClose();
                }
                Exception connectionNotEstablishedBecause = EEE;
                if (EEE instanceof SQLException) {
                    throw ((SQLException) EEE);
                }
                SQLException chainedEx = SQLError.createSQLException(Messages.getString("Connection.UnableToConnect"), SQLError.SQL_STATE_UNABLE_TO_CONNECT_TO_DATASOURCE, getExceptionInterceptor());
                chainedEx.initCause(connectionNotEstablishedBecause);
                throw chainedEx;
            }
        }
    }

    private void createPreparedStatementCaches() throws SQLException {
        synchronized (getConnectionMutex()) {
            int cacheSize = getPreparedStatementCacheSize();
            try {
                this.cachedPreparedStatementParams = ((CacheAdapterFactory) Class.forName(getParseInfoCacheFactory()).newInstance()).getInstance(this, this.myURL, getPreparedStatementCacheSize(), getPreparedStatementCacheSqlLimit(), this.props);
                if (getUseServerPreparedStmts()) {
                    this.serverSideStatementCheckCache = new LRUCache(cacheSize);
                    this.serverSideStatementCache = new LRUCache<CompoundCacheKey, ServerPreparedStatement>(cacheSize) {
                        private static final long serialVersionUID = 7692318650375988114L;

                        protected boolean removeEldestEntry(Entry<CompoundCacheKey, ServerPreparedStatement> eldest) {
                            if (this.maxElements <= 1) {
                                return false;
                            }
                            boolean removeIt = super.removeEldestEntry(eldest);
                            if (removeIt) {
                                ServerPreparedStatement ps = (ServerPreparedStatement) eldest.getValue();
                                ps.isCached = false;
                                ps.setClosed(false);
                                try {
                                    ps.close();
                                } catch (SQLException e) {
                                }
                            }
                            return removeIt;
                        }
                    };
                }
            } catch (ClassNotFoundException e) {
                SQLException sqlEx = SQLError.createSQLException(Messages.getString("Connection.CantFindCacheFactory", new Object[]{getParseInfoCacheFactory(), "parseInfoCacheFactory"}), getExceptionInterceptor());
                sqlEx.initCause(e);
                throw sqlEx;
            } catch (InstantiationException e2) {
                sqlEx = SQLError.createSQLException(Messages.getString("Connection.CantLoadCacheFactory", new Object[]{getParseInfoCacheFactory(), "parseInfoCacheFactory"}), getExceptionInterceptor());
                sqlEx.initCause(e2);
                throw sqlEx;
            } catch (IllegalAccessException e3) {
                sqlEx = SQLError.createSQLException(Messages.getString("Connection.CantLoadCacheFactory", new Object[]{getParseInfoCacheFactory(), "parseInfoCacheFactory"}), getExceptionInterceptor());
                sqlEx.initCause(e3);
                throw sqlEx;
            }
        }
    }

    public Statement createStatement() throws SQLException {
        return createStatement(1003, 1007);
    }

    public Statement createStatement(int resultSetType, int resultSetConcurrency) throws SQLException {
        checkClosed();
        StatementImpl stmt = new StatementImpl(getMultiHostSafeProxy(), this.database);
        stmt.setResultSetType(resultSetType);
        stmt.setResultSetConcurrency(resultSetConcurrency);
        return stmt;
    }

    public Statement createStatement(int resultSetType, int resultSetConcurrency, int resultSetHoldability) throws SQLException {
        if (!getPedantic() || resultSetHoldability == 1) {
            return createStatement(resultSetType, resultSetConcurrency);
        }
        throw SQLError.createSQLException("HOLD_CUSRORS_OVER_COMMIT is only supported holdability level", SQLError.SQL_STATE_ILLEGAL_ARGUMENT, getExceptionInterceptor());
    }

    public void dumpTestcaseQuery(String query) {
        System.err.println(query);
    }

    public Connection duplicate() throws SQLException {
        return new ConnectionImpl(this.origHostToConnectTo, this.origPortToConnectTo, this.props, this.origDatabaseToConnectTo, this.myURL);
    }

    public ResultSetInternalMethods execSQL(StatementImpl callingStatement, String sql, int maxRows, Buffer packet, int resultSetType, int resultSetConcurrency, boolean streamResults, String catalog, Field[] cachedMetadata) throws SQLException {
        return execSQL(callingStatement, sql, maxRows, packet, resultSetType, resultSetConcurrency, streamResults, catalog, cachedMetadata, false);
    }

    public com.mysql.jdbc.ResultSetInternalMethods execSQL(com.mysql.jdbc.StatementImpl r44, java.lang.String r45, int r46, com.mysql.jdbc.Buffer r47, int r48, int r49, boolean r50, java.lang.String r51, com.mysql.jdbc.Field[] r52, boolean r53) throws java.sql.SQLException {
        /* JADX: method processing error */
/*
Error: jadx.core.utils.exceptions.JadxRuntimeException: Exception block dominator not found, method:com.mysql.jdbc.ConnectionImpl.execSQL(com.mysql.jdbc.StatementImpl, java.lang.String, int, com.mysql.jdbc.Buffer, int, int, boolean, java.lang.String, com.mysql.jdbc.Field[], boolean):com.mysql.jdbc.ResultSetInternalMethods. bs: [B:39:0x0063, B:81:0x014c, B:113:0x01cf]
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
        r43 = this;
        r1 = r43;
        r13 = r47;
        r14 = r43.getConnectionMutex();
        monitor-enter(r14);
        r2 = 0;
        r4 = 0;
        if (r13 == 0) goto L_0x002b;
    L_0x000e:
        r5 = r47.getPosition();	 Catch:{ all -> 0x0014 }
        r4 = r5;
        goto L_0x002b;
    L_0x0014:
        r0 = move-exception;
        r34 = r44;
        r35 = r45;
        r36 = r46;
        r11 = r48;
        r12 = r49;
        r3 = r50;
        r15 = r51;
        r16 = r52;
        r18 = r53;
        r2 = r0;
        r7 = r1;
        goto L_0x02fa;
    L_0x002b:
        r15 = r4;
        r4 = r43.getGatherPerformanceMetrics();	 Catch:{ all -> 0x02e3 }
        if (r4 == 0) goto L_0x0037;
    L_0x0032:
        r4 = java.lang.System.currentTimeMillis();	 Catch:{ all -> 0x0014 }
        r2 = r4;
    L_0x0037:
        r16 = r2;
        r2 = 0;
        r1.lastQueryFinishedTime = r2;	 Catch:{ all -> 0x02e3 }
        r2 = r43.getHighAvailability();	 Catch:{ all -> 0x02e3 }
        r12 = 1;
        if (r2 == 0) goto L_0x0060;
    L_0x0044:
        r2 = r1.autoCommit;	 Catch:{ all -> 0x0014 }
        if (r2 != 0) goto L_0x004e;	 Catch:{ all -> 0x0014 }
    L_0x0048:
        r2 = r43.getAutoReconnectForPools();	 Catch:{ all -> 0x0014 }
        if (r2 == 0) goto L_0x0060;	 Catch:{ all -> 0x0014 }
    L_0x004e:
        r2 = r1.needsPing;	 Catch:{ all -> 0x0014 }
        if (r2 == 0) goto L_0x0060;
    L_0x0052:
        if (r53 != 0) goto L_0x0060;
    L_0x0054:
        r2 = 0;
        r1.pingInternal(r2, r2);	 Catch:{ Exception -> 0x005b }
        r1.needsPing = r2;	 Catch:{ Exception -> 0x005b }
        goto L_0x0060;
    L_0x005b:
        r0 = move-exception;
        r2 = r0;
        r1.createNewIO(r12);	 Catch:{ all -> 0x0014 }
    L_0x0060:
        if (r13 != 0) goto L_0x0139;
    L_0x0062:
        r2 = 0;
        r3 = r43.getUseUnicode();	 Catch:{ SQLException -> 0x0134, Exception -> 0x0130 }
        if (r3 == 0) goto L_0x006e;	 Catch:{ SQLException -> 0x0134, Exception -> 0x0130 }
    L_0x0069:
        r3 = r43.getEncoding();	 Catch:{ SQLException -> 0x0134, Exception -> 0x0130 }
        r2 = r3;	 Catch:{ SQLException -> 0x0134, Exception -> 0x0130 }
    L_0x006e:
        r3 = r1.io;	 Catch:{ SQLException -> 0x0134, Exception -> 0x0130 }
        r23 = 0;	 Catch:{ SQLException -> 0x0134, Exception -> 0x0130 }
        r19 = r3;	 Catch:{ SQLException -> 0x0134, Exception -> 0x0130 }
        r20 = r44;	 Catch:{ SQLException -> 0x0134, Exception -> 0x0130 }
        r21 = r45;	 Catch:{ SQLException -> 0x0134, Exception -> 0x0130 }
        r22 = r2;	 Catch:{ SQLException -> 0x0134, Exception -> 0x0130 }
        r24 = r46;	 Catch:{ SQLException -> 0x0134, Exception -> 0x0130 }
        r25 = r48;	 Catch:{ SQLException -> 0x0134, Exception -> 0x0130 }
        r26 = r49;	 Catch:{ SQLException -> 0x0134, Exception -> 0x0130 }
        r27 = r50;	 Catch:{ SQLException -> 0x0134, Exception -> 0x0130 }
        r28 = r51;	 Catch:{ SQLException -> 0x0134, Exception -> 0x0130 }
        r29 = r52;	 Catch:{ SQLException -> 0x0134, Exception -> 0x0130 }
        r3 = r19.sqlQueryDirect(r20, r21, r22, r23, r24, r25, r26, r27, r28, r29);	 Catch:{ SQLException -> 0x0134, Exception -> 0x0130 }
        r4 = r16;
        r6 = r15;
        r7 = r1;
        r8 = r44;
        r9 = r45;
        r10 = r46;
        r11 = r13;
        r12 = r48;
        r13 = r49;
        r15 = r50;
        r16 = r51;
        r17 = r52;
        r18 = r53;
        r19 = r7.getMaintainTimeStats();	 Catch:{ all -> 0x010f }
        if (r19 == 0) goto L_0x00c7;
    L_0x00a7:
        r30 = r8;
        r31 = r9;
        r8 = java.lang.System.currentTimeMillis();	 Catch:{ all -> 0x00b2 }
        r7.lastQueryFinishedTime = r8;	 Catch:{ all -> 0x00b2 }
        goto L_0x00cb;
    L_0x00b2:
        r0 = move-exception;
        r2 = r0;
        r36 = r10;
        r3 = r15;
        r15 = r16;
        r16 = r17;
        r34 = r30;
        r35 = r31;
        r42 = r13;
        r13 = r11;
        r11 = r12;
        r12 = r42;
        goto L_0x02fa;
    L_0x00c7:
        r30 = r8;
        r31 = r9;
    L_0x00cb:
        r8 = r7.getGatherPerformanceMetrics();	 Catch:{ all -> 0x00f8 }
        if (r8 == 0) goto L_0x00f2;	 Catch:{ all -> 0x00f8 }
    L_0x00d1:
        r8 = java.lang.System.currentTimeMillis();	 Catch:{ all -> 0x00f8 }
        r32 = r10;
        r33 = r11;
        r10 = r8 - r4;
        r7.registerQueryExecutionTime(r10);	 Catch:{ all -> 0x00df }
        goto L_0x00f6;	 Catch:{ all -> 0x00df }
    L_0x00df:
        r0 = move-exception;	 Catch:{ all -> 0x00df }
        r2 = r0;	 Catch:{ all -> 0x00df }
        r11 = r12;	 Catch:{ all -> 0x00df }
        r12 = r13;	 Catch:{ all -> 0x00df }
        r3 = r15;	 Catch:{ all -> 0x00df }
        r15 = r16;	 Catch:{ all -> 0x00df }
        r16 = r17;	 Catch:{ all -> 0x00df }
        r34 = r30;	 Catch:{ all -> 0x00df }
        r35 = r31;	 Catch:{ all -> 0x00df }
        r36 = r32;	 Catch:{ all -> 0x00df }
        r13 = r33;	 Catch:{ all -> 0x00df }
        goto L_0x02fa;	 Catch:{ all -> 0x00df }
    L_0x00f2:
        r32 = r10;	 Catch:{ all -> 0x00df }
        r33 = r11;	 Catch:{ all -> 0x00df }
    L_0x00f6:
        monitor-exit(r14);	 Catch:{ all -> 0x00df }
        return r3;
    L_0x00f8:
        r0 = move-exception;
        r32 = r10;
        r33 = r11;
        r2 = r0;
        r11 = r12;
        r12 = r13;
        r3 = r15;
        r15 = r16;
        r16 = r17;
        r34 = r30;
        r35 = r31;
        r36 = r32;
        r13 = r33;
        goto L_0x02fa;
    L_0x010f:
        r0 = move-exception;
        r30 = r8;
        r31 = r9;
        r32 = r10;
        r33 = r11;
        r2 = r0;
        r11 = r12;
        r12 = r13;
        r3 = r15;
        r15 = r16;
        r16 = r17;
        r34 = r30;
        r35 = r31;
        r36 = r32;
        r13 = r33;
        goto L_0x02fa;
    L_0x012a:
        r0 = move-exception;
        r4 = r45;
    L_0x012d:
        r2 = r0;
        goto L_0x0261;
    L_0x0130:
        r0 = move-exception;
        r2 = r0;
        goto L_0x01ce;
    L_0x0134:
        r0 = move-exception;
        r2 = r0;
        r3 = r12;
        goto L_0x0200;
    L_0x0139:
        r2 = r1.io;	 Catch:{ SQLException -> 0x01fd, Exception -> 0x0130 }
        r4 = 0;
        r5 = 0;
        r3 = r44;
        r6 = r13;
        r7 = r46;
        r8 = r48;
        r9 = r49;
        r10 = r50;
        r11 = r51;
        r12 = r52;
        r2 = r2.sqlQueryDirect(r3, r4, r5, r6, r7, r8, r9, r10, r11, r12);	 Catch:{ SQLException -> 0x01ca, Exception -> 0x0130 }
        r3 = r16;
        r5 = r15;
        r6 = r1;
        r7 = r44;
        r8 = r45;
        r9 = r46;
        r10 = r13;
        r11 = r48;
        r12 = r49;
        r13 = r50;
        r15 = r51;
        r16 = r52;
        r17 = r53;
        r18 = r6.getMaintainTimeStats();	 Catch:{ all -> 0x01b8 }
        if (r18 == 0) goto L_0x0182;
    L_0x016d:
        r34 = r7;
        r35 = r8;
        r7 = java.lang.System.currentTimeMillis();	 Catch:{ all -> 0x0178 }
        r6.lastQueryFinishedTime = r7;	 Catch:{ all -> 0x0178 }
        goto L_0x0186;
    L_0x0178:
        r0 = move-exception;
        r2 = r0;
        r7 = r6;
        r36 = r9;
        r3 = r13;
        r18 = r17;
        goto L_0x0293;
    L_0x0182:
        r34 = r7;
        r35 = r8;
    L_0x0186:
        r7 = r6.getGatherPerformanceMetrics();	 Catch:{ all -> 0x01aa }
        if (r7 == 0) goto L_0x01a4;	 Catch:{ all -> 0x01aa }
    L_0x018c:
        r7 = java.lang.System.currentTimeMillis();	 Catch:{ all -> 0x01aa }
        r36 = r9;
        r37 = r10;
        r9 = r7 - r3;
        r6.registerQueryExecutionTime(r9);	 Catch:{ all -> 0x019a }
        goto L_0x01a8;	 Catch:{ all -> 0x019a }
    L_0x019a:
        r0 = move-exception;	 Catch:{ all -> 0x019a }
        r2 = r0;	 Catch:{ all -> 0x019a }
        r7 = r6;	 Catch:{ all -> 0x019a }
        r3 = r13;	 Catch:{ all -> 0x019a }
        r18 = r17;	 Catch:{ all -> 0x019a }
        r13 = r37;	 Catch:{ all -> 0x019a }
        goto L_0x02fa;	 Catch:{ all -> 0x019a }
    L_0x01a4:
        r36 = r9;	 Catch:{ all -> 0x019a }
        r37 = r10;	 Catch:{ all -> 0x019a }
    L_0x01a8:
        monitor-exit(r14);	 Catch:{ all -> 0x019a }
        return r2;
    L_0x01aa:
        r0 = move-exception;
        r36 = r9;
        r37 = r10;
        r2 = r0;
        r7 = r6;
        r3 = r13;
        r18 = r17;
        r13 = r37;
        goto L_0x02fa;
    L_0x01b8:
        r0 = move-exception;
        r34 = r7;
        r35 = r8;
        r36 = r9;
        r37 = r10;
        r2 = r0;
        r7 = r6;
        r3 = r13;
        r18 = r17;
        r13 = r37;
        goto L_0x02fa;
    L_0x01ca:
        r0 = move-exception;
        r2 = r0;
        r3 = 1;
        goto L_0x0200;
        r3 = r43.getHighAvailability();	 Catch:{ all -> 0x012a }
        if (r3 == 0) goto L_0x01e2;	 Catch:{ all -> 0x012a }
    L_0x01d5:
        r3 = r2 instanceof java.io.IOException;	 Catch:{ all -> 0x012a }
        if (r3 == 0) goto L_0x01de;	 Catch:{ all -> 0x012a }
    L_0x01d9:
        r3 = r1.io;	 Catch:{ all -> 0x012a }
        r3.forceClose();	 Catch:{ all -> 0x012a }
    L_0x01de:
        r3 = 1;	 Catch:{ all -> 0x012a }
        r1.needsPing = r3;	 Catch:{ all -> 0x012a }
        goto L_0x01e9;	 Catch:{ all -> 0x012a }
    L_0x01e2:
        r3 = r2 instanceof java.io.IOException;	 Catch:{ all -> 0x012a }
        if (r3 == 0) goto L_0x01e9;	 Catch:{ all -> 0x012a }
    L_0x01e6:
        r1.cleanup(r2);	 Catch:{ all -> 0x012a }
    L_0x01e9:
        r3 = "Connection.UnexpectedException";	 Catch:{ all -> 0x012a }
        r3 = com.mysql.jdbc.Messages.getString(r3);	 Catch:{ all -> 0x012a }
        r4 = "S1000";	 Catch:{ all -> 0x012a }
        r5 = r43.getExceptionInterceptor();	 Catch:{ all -> 0x012a }
        r3 = com.mysql.jdbc.SQLError.createSQLException(r3, r4, r5);	 Catch:{ all -> 0x012a }
        r3.initCause(r2);	 Catch:{ all -> 0x012a }
        throw r3;	 Catch:{ all -> 0x012a }
    L_0x01fd:
        r0 = move-exception;	 Catch:{ all -> 0x012a }
        r3 = r12;	 Catch:{ all -> 0x012a }
        r2 = r0;	 Catch:{ all -> 0x012a }
    L_0x0200:
        r4 = r43.getDumpQueriesOnException();	 Catch:{ all -> 0x012a }
        if (r4 == 0) goto L_0x0235;
    L_0x0206:
        r4 = r45;
        r5 = r1.extractSqlFromPacket(r4, r13, r15);	 Catch:{ all -> 0x0232 }
        r6 = new java.lang.StringBuilder;	 Catch:{ all -> 0x0232 }
        r7 = r5.length();	 Catch:{ all -> 0x0232 }
        r7 = r7 + 32;	 Catch:{ all -> 0x0232 }
        r6.<init>(r7);	 Catch:{ all -> 0x0232 }
        r7 = "\n\nQuery being executed when exception was thrown:\n";	 Catch:{ all -> 0x0232 }
        r6.append(r7);	 Catch:{ all -> 0x0232 }
        r6.append(r5);	 Catch:{ all -> 0x0232 }
        r7 = "\n\n";	 Catch:{ all -> 0x0232 }
        r6.append(r7);	 Catch:{ all -> 0x0232 }
        r7 = r6.toString();	 Catch:{ all -> 0x0232 }
        r8 = r43.getExceptionInterceptor();	 Catch:{ all -> 0x0232 }
        r7 = appendMessageToException(r2, r7, r8);	 Catch:{ all -> 0x0232 }
        r2 = r7;	 Catch:{ all -> 0x0232 }
        goto L_0x0237;	 Catch:{ all -> 0x0232 }
    L_0x0232:
        r0 = move-exception;	 Catch:{ all -> 0x0232 }
        goto L_0x012d;	 Catch:{ all -> 0x0232 }
    L_0x0235:
        r4 = r45;	 Catch:{ all -> 0x0232 }
    L_0x0237:
        r5 = r43.getHighAvailability();	 Catch:{ all -> 0x0232 }
        if (r5 == 0) goto L_0x0251;	 Catch:{ all -> 0x0232 }
    L_0x023d:
        r5 = "08S01";	 Catch:{ all -> 0x0232 }
        r6 = r2.getSQLState();	 Catch:{ all -> 0x0232 }
        r5 = r5.equals(r6);	 Catch:{ all -> 0x0232 }
        if (r5 == 0) goto L_0x024e;	 Catch:{ all -> 0x0232 }
    L_0x0249:
        r5 = r1.io;	 Catch:{ all -> 0x0232 }
        r5.forceClose();	 Catch:{ all -> 0x0232 }
    L_0x024e:
        r1.needsPing = r3;	 Catch:{ all -> 0x0232 }
        goto L_0x0260;	 Catch:{ all -> 0x0232 }
    L_0x0251:
        r3 = "08S01";	 Catch:{ all -> 0x0232 }
        r5 = r2.getSQLState();	 Catch:{ all -> 0x0232 }
        r3 = r3.equals(r5);	 Catch:{ all -> 0x0232 }
        if (r3 == 0) goto L_0x0260;	 Catch:{ all -> 0x0232 }
    L_0x025d:
        r1.cleanup(r2);	 Catch:{ all -> 0x0232 }
    L_0x0260:
        throw r2;	 Catch:{ all -> 0x0232 }
        r5 = r16;
        r3 = r15;
        r7 = r1;
        r8 = r44;
        r9 = r46;
        r10 = r13;
        r11 = r48;
        r12 = r49;
        r13 = r50;
        r15 = r51;
        r16 = r52;
        r17 = r53;
        r18 = r7.getMaintainTimeStats();	 Catch:{ all -> 0x02d0 }
        if (r18 == 0) goto L_0x0296;
    L_0x027d:
        r38 = r3;
        r39 = r4;
        r3 = java.lang.System.currentTimeMillis();	 Catch:{ all -> 0x0288 }
        r7.lastQueryFinishedTime = r3;	 Catch:{ all -> 0x0288 }
        goto L_0x029a;
    L_0x0288:
        r0 = move-exception;
        r2 = r0;
        r34 = r8;
        r36 = r9;
        r3 = r13;
        r18 = r17;
        r35 = r39;
    L_0x0293:
        r13 = r10;
        goto L_0x02fa;
    L_0x0296:
        r38 = r3;
        r39 = r4;
    L_0x029a:
        r3 = r7.getGatherPerformanceMetrics();	 Catch:{ all -> 0x02bf }
        if (r3 == 0) goto L_0x02ba;	 Catch:{ all -> 0x02bf }
    L_0x02a0:
        r3 = java.lang.System.currentTimeMillis();	 Catch:{ all -> 0x02bf }
        r40 = r8;
        r41 = r9;
        r8 = r3 - r5;
        r7.registerQueryExecutionTime(r8);	 Catch:{ all -> 0x02ae }
        goto L_0x02be;	 Catch:{ all -> 0x02ae }
    L_0x02ae:
        r0 = move-exception;	 Catch:{ all -> 0x02ae }
        r2 = r0;	 Catch:{ all -> 0x02ae }
        r3 = r13;	 Catch:{ all -> 0x02ae }
        r18 = r17;	 Catch:{ all -> 0x02ae }
        r35 = r39;	 Catch:{ all -> 0x02ae }
        r34 = r40;	 Catch:{ all -> 0x02ae }
        r36 = r41;	 Catch:{ all -> 0x02ae }
        goto L_0x0293;	 Catch:{ all -> 0x02ae }
    L_0x02ba:
        r40 = r8;	 Catch:{ all -> 0x02ae }
        r41 = r9;	 Catch:{ all -> 0x02ae }
    L_0x02be:
        throw r2;	 Catch:{ all -> 0x02ae }
    L_0x02bf:
        r0 = move-exception;
        r40 = r8;
        r41 = r9;
        r2 = r0;
        r3 = r13;
        r18 = r17;
        r35 = r39;
        r34 = r40;
        r36 = r41;
        r13 = r10;
        goto L_0x02fa;
    L_0x02d0:
        r0 = move-exception;
        r39 = r4;
        r40 = r8;
        r41 = r9;
        r2 = r0;
        r3 = r13;
        r18 = r17;
        r35 = r39;
        r34 = r40;
        r36 = r41;
        r13 = r10;
        goto L_0x02fa;
    L_0x02e3:
        r0 = move-exception;
        r4 = r45;
        r34 = r44;
        r36 = r46;
        r11 = r48;
        r12 = r49;
        r3 = r50;
        r15 = r51;
        r16 = r52;
        r18 = r53;
        r2 = r0;
        r7 = r1;
        r35 = r4;
    L_0x02fa:
        monitor-exit(r14);	 Catch:{ all -> 0x02fc }
        throw r2;
    L_0x02fc:
        r0 = move-exception;
        r2 = r0;
        goto L_0x02fa;
        */
        throw new UnsupportedOperationException("Method not decompiled: com.mysql.jdbc.ConnectionImpl.execSQL(com.mysql.jdbc.StatementImpl, java.lang.String, int, com.mysql.jdbc.Buffer, int, int, boolean, java.lang.String, com.mysql.jdbc.Field[], boolean):com.mysql.jdbc.ResultSetInternalMethods");
    }

    public String extractSqlFromPacket(String possibleSqlQuery, Buffer queryPacket, int endOfQueryPacketPosition) throws SQLException {
        String extractedSql = null;
        if (possibleSqlQuery != null) {
            if (possibleSqlQuery.length() > getMaxQuerySizeToLog()) {
                StringBuilder truncatedQueryBuf = new StringBuilder(possibleSqlQuery.substring(0, getMaxQuerySizeToLog()));
                truncatedQueryBuf.append(Messages.getString("MysqlIO.25"));
                extractedSql = truncatedQueryBuf.toString();
            } else {
                extractedSql = possibleSqlQuery;
            }
        }
        if (extractedSql != null) {
            return extractedSql;
        }
        int extractPosition = endOfQueryPacketPosition;
        boolean truncated = false;
        if (endOfQueryPacketPosition > getMaxQuerySizeToLog()) {
            extractPosition = getMaxQuerySizeToLog();
            truncated = true;
        }
        extractedSql = StringUtils.toString(queryPacket.getByteBuffer(), 5, extractPosition - 5);
        if (!truncated) {
            return extractedSql;
        }
        StringBuilder stringBuilder = new StringBuilder();
        stringBuilder.append(extractedSql);
        stringBuilder.append(Messages.getString("MysqlIO.25"));
        return stringBuilder.toString();
    }

    public StringBuilder generateConnectionCommentBlock(StringBuilder buf) {
        buf.append("/* conn id ");
        buf.append(getId());
        buf.append(" clock: ");
        buf.append(System.currentTimeMillis());
        buf.append(" */ ");
        return buf;
    }

    public int getActiveStatementCount() {
        return this.openStatements.size();
    }

    public boolean getAutoCommit() throws SQLException {
        boolean z;
        synchronized (getConnectionMutex()) {
            z = this.autoCommit;
        }
        return z;
    }

    public Calendar getCalendarInstanceForSessionOrNew() {
        if (getDynamicCalendars()) {
            return Calendar.getInstance();
        }
        return getSessionLockedCalendar();
    }

    public String getCatalog() throws SQLException {
        String str;
        synchronized (getConnectionMutex()) {
            str = this.database;
        }
        return str;
    }

    public String getCharacterSetMetadata() {
        String str;
        synchronized (getConnectionMutex()) {
            str = this.characterSetMetadata;
        }
        return str;
    }

    public SingleByteCharsetConverter getCharsetConverter(String javaEncodingName) throws SQLException {
        if (javaEncodingName == null || this.usePlatformCharsetConverters) {
            return null;
        }
        synchronized (this.charsetConverterMap) {
            Object asObject = this.charsetConverterMap.get(javaEncodingName);
            if (asObject == CHARSET_CONVERTER_NOT_AVAILABLE_MARKER) {
                return null;
            }
            SingleByteCharsetConverter converter = (SingleByteCharsetConverter) asObject;
            if (converter == null) {
                try {
                    converter = SingleByteCharsetConverter.getInstance(javaEncodingName, this);
                    if (converter == null) {
                        this.charsetConverterMap.put(javaEncodingName, CHARSET_CONVERTER_NOT_AVAILABLE_MARKER);
                    } else {
                        this.charsetConverterMap.put(javaEncodingName, converter);
                    }
                } catch (UnsupportedEncodingException e) {
                    this.charsetConverterMap.put(javaEncodingName, CHARSET_CONVERTER_NOT_AVAILABLE_MARKER);
                    converter = null;
                }
            }
        }
        return converter;
    }

    @Deprecated
    public String getCharsetNameForIndex(int charsetIndex) throws SQLException {
        return getEncodingForIndex(charsetIndex);
    }

    public String getEncodingForIndex(int charsetIndex) throws SQLException {
        String javaEncoding = null;
        if (getUseOldUTF8Behavior()) {
            return getEncoding();
        }
        if (charsetIndex != -1) {
            try {
                if (this.indexToCustomMysqlCharset != null) {
                    String cs = (String) this.indexToCustomMysqlCharset.get(Integer.valueOf(charsetIndex));
                    if (cs != null) {
                        javaEncoding = CharsetMapping.getJavaEncodingForMysqlCharset(cs, getEncoding());
                    }
                }
                if (javaEncoding == null) {
                    javaEncoding = CharsetMapping.getJavaEncodingForCollationIndex(Integer.valueOf(charsetIndex), getEncoding());
                }
                if (javaEncoding == null) {
                    javaEncoding = getEncoding();
                }
            } catch (ArrayIndexOutOfBoundsException e) {
                StringBuilder stringBuilder = new StringBuilder();
                stringBuilder.append("Unknown character set index for field '");
                stringBuilder.append(charsetIndex);
                stringBuilder.append("' received from server.");
                throw SQLError.createSQLException(stringBuilder.toString(), SQLError.SQL_STATE_GENERAL_ERROR, getExceptionInterceptor());
            } catch (RuntimeException ex) {
                SQLException sqlEx = SQLError.createSQLException(ex.toString(), SQLError.SQL_STATE_ILLEGAL_ARGUMENT, null);
                sqlEx.initCause(ex);
                throw sqlEx;
            }
        }
        javaEncoding = getEncoding();
        return javaEncoding;
    }

    public TimeZone getDefaultTimeZone() {
        return getCacheDefaultTimezone() ? this.defaultTimeZone : TimeUtil.getDefaultTimeZone(false);
    }

    public String getErrorMessageEncoding() {
        return this.errorMessageEncoding;
    }

    public int getHoldability() throws SQLException {
        return 2;
    }

    public long getId() {
        return this.connectionId;
    }

    public long getIdleFor() {
        synchronized (getConnectionMutex()) {
            if (this.lastQueryFinishedTime == 0) {
                return 0;
            }
            long idleTime = System.currentTimeMillis() - this.lastQueryFinishedTime;
            return idleTime;
        }
    }

    public MysqlIO getIO() throws SQLException {
        if (this.io != null) {
            if (!this.isClosed) {
                return this.io;
            }
        }
        throw SQLError.createSQLException("Operation not allowed on closed connection", SQLError.SQL_STATE_CONNECTION_NOT_OPEN, getExceptionInterceptor());
    }

    public Log getLog() throws SQLException {
        return this.log;
    }

    public int getMaxBytesPerChar(String javaCharsetName) throws SQLException {
        return getMaxBytesPerChar(null, javaCharsetName);
    }

    public int getMaxBytesPerChar(Integer charsetIndex, String javaCharsetName) throws SQLException {
        String charset = null;
        int res = 1;
        try {
            if (this.indexToCustomMysqlCharset != null) {
                charset = (String) this.indexToCustomMysqlCharset.get(charsetIndex);
            }
            if (charset == null) {
                charset = CharsetMapping.getMysqlCharsetNameForCollationIndex(charsetIndex);
            }
            if (charset == null) {
                charset = CharsetMapping.getMysqlCharsetForJavaEncoding(javaCharsetName, this);
            }
            Integer mblen = null;
            if (this.mysqlCharsetToCustomMblen != null) {
                mblen = (Integer) this.mysqlCharsetToCustomMblen.get(charset);
            }
            if (mblen == null) {
                mblen = Integer.valueOf(CharsetMapping.getMblen(charset));
            }
            if (mblen != null) {
                res = mblen.intValue();
            }
            return res;
        } catch (SQLException ex) {
            throw ex;
        } catch (RuntimeException ex2) {
            SQLException sqlEx = SQLError.createSQLException(ex2.toString(), SQLError.SQL_STATE_ILLEGAL_ARGUMENT, null);
            sqlEx.initCause(ex2);
            throw sqlEx;
        }
    }

    public DatabaseMetaData getMetaData() throws SQLException {
        return getMetaData(true, true);
    }

    private DatabaseMetaData getMetaData(boolean checkClosed, boolean checkForInfoSchema) throws SQLException {
        if (checkClosed) {
            checkClosed();
        }
        return DatabaseMetaData.getInstance(getMultiHostSafeProxy(), this.database, checkForInfoSchema);
    }

    public Statement getMetadataSafeStatement() throws SQLException {
        Statement stmt = createStatement();
        if (stmt.getMaxRows() != 0) {
            stmt.setMaxRows(0);
        }
        stmt.setEscapeProcessing(false);
        if (stmt.getFetchSize() != 0) {
            stmt.setFetchSize(0);
        }
        return stmt;
    }

    public int getNetBufferLength() {
        return this.netBufferLength;
    }

    @Deprecated
    public String getServerCharacterEncoding() {
        return getServerCharset();
    }

    public String getServerCharset() {
        if (!this.io.versionMeetsMinimum(4, 1, 0)) {
            return (String) this.serverVariables.get("character_set");
        }
        String charset = null;
        if (this.indexToCustomMysqlCharset != null) {
            charset = (String) this.indexToCustomMysqlCharset.get(Integer.valueOf(this.io.serverCharsetIndex));
        }
        if (charset == null) {
            charset = CharsetMapping.getMysqlCharsetNameForCollationIndex(Integer.valueOf(this.io.serverCharsetIndex));
        }
        return charset != null ? charset : (String) this.serverVariables.get("character_set_server");
    }

    public int getServerMajorVersion() {
        return this.io.getServerMajorVersion();
    }

    public int getServerMinorVersion() {
        return this.io.getServerMinorVersion();
    }

    public int getServerSubMinorVersion() {
        return this.io.getServerSubMinorVersion();
    }

    public TimeZone getServerTimezoneTZ() {
        return this.serverTimezoneTZ;
    }

    public String getServerVariable(String variableName) {
        if (this.serverVariables != null) {
            return (String) this.serverVariables.get(variableName);
        }
        return null;
    }

    public String getServerVersion() {
        return this.io.getServerVersion();
    }

    public Calendar getSessionLockedCalendar() {
        return this.sessionCalendar;
    }

    public int getTransactionIsolation() throws SQLException {
        Throwable th;
        synchronized (getConnectionMutex()) {
            try {
                if (!this.hasIsolationLevels || getUseLocalSessionState()) {
                    int i = this.isolationLevel;
                    return i;
                }
                Statement stmt = null;
                ResultSet rs = null;
                try {
                    String query;
                    stmt = getMetadataSafeStatement();
                    int offset = 1;
                    if (versionMeetsMinimum(8, 0, 3)) {
                        query = "SELECT @@session.transaction_isolation";
                    } else if (versionMeetsMinimum(4, 0, 3)) {
                        query = "SELECT @@session.tx_isolation";
                    } else {
                        query = "SHOW VARIABLES LIKE 'transaction_isolation'";
                        offset = 2;
                    }
                    rs = stmt.executeQuery(query);
                    if (rs.next()) {
                        String s = rs.getString(offset);
                        if (s != null) {
                            Integer intTI = (Integer) mapTransIsolationNameToValue.get(s);
                            if (intTI != null) {
                                int intValue = intTI.intValue();
                                if (rs != null) {
                                    try {
                                        rs.close();
                                    } catch (Exception e) {
                                    }
                                }
                                if (stmt != null) {
                                    try {
                                        stmt.close();
                                    } catch (Exception e2) {
                                    }
                                }
                                return intValue;
                            }
                        }
                        StringBuilder stringBuilder = new StringBuilder();
                        stringBuilder.append("Could not map transaction isolation '");
                        stringBuilder.append(s);
                        stringBuilder.append(" to a valid JDBC level.");
                        throw SQLError.createSQLException(stringBuilder.toString(), SQLError.SQL_STATE_GENERAL_ERROR, getExceptionInterceptor());
                    }
                    throw SQLError.createSQLException("Could not retrieve transaction isolation level from server", SQLError.SQL_STATE_GENERAL_ERROR, getExceptionInterceptor());
                } catch (Throwable th2) {
                    th = th2;
                    throw th;
                }
            } catch (Throwable th3) {
                th = th3;
                ConnectionImpl connectionImpl = this;
                throw th;
            }
        }
    }

    public Map<String, Class<?>> getTypeMap() throws SQLException {
        Map<String, Class<?>> map;
        synchronized (getConnectionMutex()) {
            if (this.typeMap == null) {
                this.typeMap = new HashMap();
            }
            map = this.typeMap;
        }
        return map;
    }

    public String getURL() {
        return this.myURL;
    }

    public String getUser() {
        return this.user;
    }

    public Calendar getUtcCalendar() {
        return this.utcCalendar;
    }

    public SQLWarning getWarnings() throws SQLException {
        return null;
    }

    public boolean hasSameProperties(Connection c) {
        return this.props.equals(c.getProperties());
    }

    public Properties getProperties() {
        return this.props;
    }

    @Deprecated
    public boolean hasTriedMaster() {
        return this.hasTriedMasterFlag;
    }

    public void incrementNumberOfPreparedExecutes() {
        if (getGatherPerformanceMetrics()) {
            this.numberOfPreparedExecutes++;
            this.numberOfQueriesIssued++;
        }
    }

    public void incrementNumberOfPrepares() {
        if (getGatherPerformanceMetrics()) {
            this.numberOfPrepares++;
        }
    }

    public void incrementNumberOfResultSetsCreated() {
        if (getGatherPerformanceMetrics()) {
            this.numberOfResultSetsCreated++;
        }
    }

    private void initializeDriverProperties(Properties info) throws SQLException {
        initializeProperties(info);
        String exceptionInterceptorClasses = getExceptionInterceptors();
        if (!(exceptionInterceptorClasses == null || "".equals(exceptionInterceptorClasses))) {
            this.exceptionInterceptor = new ExceptionInterceptorChain(exceptionInterceptorClasses);
        }
        this.usePlatformCharsetConverters = getUseJvmCharsetConverters();
        this.log = LogFactory.getLogger(getLogger(), LOGGER_INSTANCE_NAME, getExceptionInterceptor());
        if (getProfileSql() || getUseUsageAdvisor()) {
            this.eventSink = ProfilerEventHandlerFactory.getInstance(getMultiHostSafeProxy());
        }
        if (getCachePreparedStatements()) {
            createPreparedStatementCaches();
        }
        if (getNoDatetimeStringSync() && getUseTimezone()) {
            throw SQLError.createSQLException("Can't enable noDatetimeStringSync and useTimezone configuration properties at the same time", SQLError.SQL_STATE_INVALID_CONNECTION_ATTRIBUTE, getExceptionInterceptor());
        }
        if (getCacheCallableStatements()) {
            this.parsedCallableStatementCache = new LRUCache(getCallableStatementCacheSize());
        }
        if (getAllowMultiQueries()) {
            setCacheResultSetMetadata(false);
        }
        if (getCacheResultSetMetadata()) {
            this.resultSetMetadataCache = new LRUCache(getMetadataCacheSize());
        }
        if (getSocksProxyHost() != null) {
            setSocketFactoryClassName("com.mysql.jdbc.SocksProxySocketFactory");
        }
    }

    private void initializePropsFromServer() throws SQLException {
        String collationServer;
        SQLException ex;
        String connectionInterceptorClasses = getConnectionLifecycleInterceptors();
        this.connectionLifecycleInterceptors = null;
        if (connectionInterceptorClasses != null) {
            this.connectionLifecycleInterceptors = Util.loadExtensions(this, this.props, connectionInterceptorClasses, "Connection.badLifecycleInterceptor", getExceptionInterceptor());
        }
        setSessionVariables();
        if (!versionMeetsMinimum(4, 1, 0)) {
            setTransformedBitIsBoolean(false);
        }
        this.parserKnowsUnicode = versionMeetsMinimum(4, 1, 0);
        if (getUseServerPreparedStmts() && versionMeetsMinimum(4, 1, 0)) {
            this.useServerPreparedStmts = true;
            if (versionMeetsMinimum(5, 0, 0) && !versionMeetsMinimum(5, 0, 3)) {
                this.useServerPreparedStmts = false;
            }
        }
        if (versionMeetsMinimum(3, 21, 22)) {
            int i;
            boolean z;
            int allowedBlobSendChunkSize;
            StringBuilder stringBuilder;
            String sqlModeAsString;
            loadServerVariables();
            if (versionMeetsMinimum(5, 0, 2)) {
                this.autoIncrementIncrement = getServerVariableAsInt("auto_increment_increment", 1);
            } else {
                this.autoIncrementIncrement = 1;
            }
            buildCollationMapping();
            if (this.io.serverCharsetIndex == 0) {
                collationServer = (String) this.serverVariables.get("collation_server");
                if (collationServer != null) {
                    for (i = 1; i < CharsetMapping.COLLATION_INDEX_TO_COLLATION_NAME.length; i++) {
                        if (CharsetMapping.COLLATION_INDEX_TO_COLLATION_NAME[i].equals(collationServer)) {
                            this.io.serverCharsetIndex = i;
                            break;
                        }
                    }
                } else {
                    this.io.serverCharsetIndex = 45;
                }
            }
            LicenseConfiguration.checkLicenseType(this.serverVariables);
            collationServer = (String) this.serverVariables.get("lower_case_table_names");
            if (!("on".equalsIgnoreCase(collationServer) || "1".equalsIgnoreCase(collationServer))) {
                if (!"2".equalsIgnoreCase(collationServer)) {
                    z = false;
                    this.lowerCaseTableNames = z;
                    if (!"1".equalsIgnoreCase(collationServer)) {
                        if ("on".equalsIgnoreCase(collationServer)) {
                            z = false;
                            this.storesLowerCaseTableName = z;
                            configureTimezone();
                            if (this.serverVariables.containsKey("max_allowed_packet")) {
                                i = getServerVariableAsInt("max_allowed_packet", -1);
                                if (i == -1 && (i < getMaxAllowedPacket() || getMaxAllowedPacket() <= 0)) {
                                    setMaxAllowedPacket(i);
                                } else if (i == -1 && getMaxAllowedPacket() == -1) {
                                    setMaxAllowedPacket(SupportMenu.USER_MASK);
                                }
                                if (getUseServerPrepStmts()) {
                                    allowedBlobSendChunkSize = Math.min(getBlobSendChunkSize(), getMaxAllowedPacket()) - 8203;
                                    if (allowedBlobSendChunkSize > 0) {
                                        stringBuilder = new StringBuilder();
                                        stringBuilder.append("Connection setting too low for 'maxAllowedPacket'. When 'useServerPrepStmts=true', 'maxAllowedPacket' must be higher than ");
                                        stringBuilder.append(8203);
                                        stringBuilder.append(". Check also 'max_allowed_packet' in MySQL configuration files.");
                                        throw SQLError.createSQLException(stringBuilder.toString(), SQLError.SQL_STATE_INVALID_CONNECTION_ATTRIBUTE, getExceptionInterceptor());
                                    }
                                    setBlobSendChunkSize(String.valueOf(allowedBlobSendChunkSize));
                                }
                            }
                            if (this.serverVariables.containsKey("net_buffer_length")) {
                                this.netBufferLength = getServerVariableAsInt("net_buffer_length", 16384);
                            }
                            checkTransactionIsolationLevel();
                            if (!versionMeetsMinimum(4, 1, 0)) {
                                checkServerEncoding();
                            }
                            this.io.checkForCharsetMismatch();
                            if (this.serverVariables.containsKey("sql_mode")) {
                                sqlModeAsString = (String) this.serverVariables.get("sql_mode");
                                if (!StringUtils.isStrictlyNumeric(sqlModeAsString)) {
                                    this.useAnsiQuotes = (Integer.parseInt(sqlModeAsString) & 4) <= 0;
                                } else if (sqlModeAsString != null) {
                                    this.useAnsiQuotes = sqlModeAsString.indexOf("ANSI_QUOTES") == -1;
                                    this.noBackslashEscapes = sqlModeAsString.indexOf("NO_BACKSLASH_ESCAPES") == -1;
                                }
                            }
                        }
                    }
                    z = true;
                    this.storesLowerCaseTableName = z;
                    configureTimezone();
                    if (this.serverVariables.containsKey("max_allowed_packet")) {
                        i = getServerVariableAsInt("max_allowed_packet", -1);
                        if (i == -1) {
                        }
                        setMaxAllowedPacket(SupportMenu.USER_MASK);
                        if (getUseServerPrepStmts()) {
                            allowedBlobSendChunkSize = Math.min(getBlobSendChunkSize(), getMaxAllowedPacket()) - 8203;
                            if (allowedBlobSendChunkSize > 0) {
                                setBlobSendChunkSize(String.valueOf(allowedBlobSendChunkSize));
                            } else {
                                stringBuilder = new StringBuilder();
                                stringBuilder.append("Connection setting too low for 'maxAllowedPacket'. When 'useServerPrepStmts=true', 'maxAllowedPacket' must be higher than ");
                                stringBuilder.append(8203);
                                stringBuilder.append(". Check also 'max_allowed_packet' in MySQL configuration files.");
                                throw SQLError.createSQLException(stringBuilder.toString(), SQLError.SQL_STATE_INVALID_CONNECTION_ATTRIBUTE, getExceptionInterceptor());
                            }
                        }
                    }
                    if (this.serverVariables.containsKey("net_buffer_length")) {
                        this.netBufferLength = getServerVariableAsInt("net_buffer_length", 16384);
                    }
                    checkTransactionIsolationLevel();
                    if (versionMeetsMinimum(4, 1, 0)) {
                        checkServerEncoding();
                    }
                    this.io.checkForCharsetMismatch();
                    if (this.serverVariables.containsKey("sql_mode")) {
                        sqlModeAsString = (String) this.serverVariables.get("sql_mode");
                        if (!StringUtils.isStrictlyNumeric(sqlModeAsString)) {
                            if ((Integer.parseInt(sqlModeAsString) & 4) <= 0) {
                            }
                            this.useAnsiQuotes = (Integer.parseInt(sqlModeAsString) & 4) <= 0;
                        } else if (sqlModeAsString != null) {
                            if (sqlModeAsString.indexOf("ANSI_QUOTES") == -1) {
                            }
                            this.useAnsiQuotes = sqlModeAsString.indexOf("ANSI_QUOTES") == -1;
                            if (sqlModeAsString.indexOf("NO_BACKSLASH_ESCAPES") == -1) {
                            }
                            this.noBackslashEscapes = sqlModeAsString.indexOf("NO_BACKSLASH_ESCAPES") == -1;
                        }
                    }
                }
            }
            z = true;
            this.lowerCaseTableNames = z;
            if ("1".equalsIgnoreCase(collationServer)) {
                if ("on".equalsIgnoreCase(collationServer)) {
                    z = false;
                    this.storesLowerCaseTableName = z;
                    configureTimezone();
                    if (this.serverVariables.containsKey("max_allowed_packet")) {
                        i = getServerVariableAsInt("max_allowed_packet", -1);
                        if (i == -1) {
                        }
                        setMaxAllowedPacket(SupportMenu.USER_MASK);
                        if (getUseServerPrepStmts()) {
                            allowedBlobSendChunkSize = Math.min(getBlobSendChunkSize(), getMaxAllowedPacket()) - 8203;
                            if (allowedBlobSendChunkSize > 0) {
                                stringBuilder = new StringBuilder();
                                stringBuilder.append("Connection setting too low for 'maxAllowedPacket'. When 'useServerPrepStmts=true', 'maxAllowedPacket' must be higher than ");
                                stringBuilder.append(8203);
                                stringBuilder.append(". Check also 'max_allowed_packet' in MySQL configuration files.");
                                throw SQLError.createSQLException(stringBuilder.toString(), SQLError.SQL_STATE_INVALID_CONNECTION_ATTRIBUTE, getExceptionInterceptor());
                            }
                            setBlobSendChunkSize(String.valueOf(allowedBlobSendChunkSize));
                        }
                    }
                    if (this.serverVariables.containsKey("net_buffer_length")) {
                        this.netBufferLength = getServerVariableAsInt("net_buffer_length", 16384);
                    }
                    checkTransactionIsolationLevel();
                    if (versionMeetsMinimum(4, 1, 0)) {
                        checkServerEncoding();
                    }
                    this.io.checkForCharsetMismatch();
                    if (this.serverVariables.containsKey("sql_mode")) {
                        sqlModeAsString = (String) this.serverVariables.get("sql_mode");
                        if (!StringUtils.isStrictlyNumeric(sqlModeAsString)) {
                            if ((Integer.parseInt(sqlModeAsString) & 4) <= 0) {
                            }
                            this.useAnsiQuotes = (Integer.parseInt(sqlModeAsString) & 4) <= 0;
                        } else if (sqlModeAsString != null) {
                            if (sqlModeAsString.indexOf("ANSI_QUOTES") == -1) {
                            }
                            this.useAnsiQuotes = sqlModeAsString.indexOf("ANSI_QUOTES") == -1;
                            if (sqlModeAsString.indexOf("NO_BACKSLASH_ESCAPES") == -1) {
                            }
                            this.noBackslashEscapes = sqlModeAsString.indexOf("NO_BACKSLASH_ESCAPES") == -1;
                        }
                    }
                }
            }
            z = true;
            this.storesLowerCaseTableName = z;
            configureTimezone();
            if (this.serverVariables.containsKey("max_allowed_packet")) {
                i = getServerVariableAsInt("max_allowed_packet", -1);
                if (i == -1) {
                }
                setMaxAllowedPacket(SupportMenu.USER_MASK);
                if (getUseServerPrepStmts()) {
                    allowedBlobSendChunkSize = Math.min(getBlobSendChunkSize(), getMaxAllowedPacket()) - 8203;
                    if (allowedBlobSendChunkSize > 0) {
                        setBlobSendChunkSize(String.valueOf(allowedBlobSendChunkSize));
                    } else {
                        stringBuilder = new StringBuilder();
                        stringBuilder.append("Connection setting too low for 'maxAllowedPacket'. When 'useServerPrepStmts=true', 'maxAllowedPacket' must be higher than ");
                        stringBuilder.append(8203);
                        stringBuilder.append(". Check also 'max_allowed_packet' in MySQL configuration files.");
                        throw SQLError.createSQLException(stringBuilder.toString(), SQLError.SQL_STATE_INVALID_CONNECTION_ATTRIBUTE, getExceptionInterceptor());
                    }
                }
            }
            if (this.serverVariables.containsKey("net_buffer_length")) {
                this.netBufferLength = getServerVariableAsInt("net_buffer_length", 16384);
            }
            checkTransactionIsolationLevel();
            if (versionMeetsMinimum(4, 1, 0)) {
                checkServerEncoding();
            }
            this.io.checkForCharsetMismatch();
            if (this.serverVariables.containsKey("sql_mode")) {
                sqlModeAsString = (String) this.serverVariables.get("sql_mode");
                if (!StringUtils.isStrictlyNumeric(sqlModeAsString)) {
                    if ((Integer.parseInt(sqlModeAsString) & 4) <= 0) {
                    }
                    this.useAnsiQuotes = (Integer.parseInt(sqlModeAsString) & 4) <= 0;
                } else if (sqlModeAsString != null) {
                    if (sqlModeAsString.indexOf("ANSI_QUOTES") == -1) {
                    }
                    this.useAnsiQuotes = sqlModeAsString.indexOf("ANSI_QUOTES") == -1;
                    if (sqlModeAsString.indexOf("NO_BACKSLASH_ESCAPES") == -1) {
                    }
                    this.noBackslashEscapes = sqlModeAsString.indexOf("NO_BACKSLASH_ESCAPES") == -1;
                }
            }
        }
        configureClientCharacterSet(false);
        try {
            this.errorMessageEncoding = CharsetMapping.getCharacterEncodingForErrorMessages(this);
            if (versionMeetsMinimum(3, 23, 15)) {
                this.transactionsSupported = true;
                handleAutoCommitDefaults();
            } else {
                this.transactionsSupported = false;
            }
            if (versionMeetsMinimum(3, 23, 36)) {
                this.hasIsolationLevels = true;
            } else {
                this.hasIsolationLevels = false;
            }
            this.hasQuotedIdentifiers = versionMeetsMinimum(3, 23, 6);
            this.io.resetMaxBuf();
            if (this.io.versionMeetsMinimum(4, 1, 0)) {
                String defaultMetadataCharset;
                String characterSetResultsOnServerMysql = (String) this.serverVariables.get(JDBC_LOCAL_CHARACTER_SET_RESULTS);
                if (!(characterSetResultsOnServerMysql == null || StringUtils.startsWithIgnoreCaseAndWs(characterSetResultsOnServerMysql, "NULL"))) {
                    if (characterSetResultsOnServerMysql.length() != 0) {
                        this.characterSetResultsOnServer = CharsetMapping.getJavaEncodingForMysqlCharset(characterSetResultsOnServerMysql);
                        this.characterSetMetadata = this.characterSetResultsOnServer;
                    }
                }
                collationServer = (String) this.serverVariables.get("character_set_system");
                if (collationServer != null) {
                    defaultMetadataCharset = CharsetMapping.getJavaEncodingForMysqlCharset(collationServer);
                } else {
                    defaultMetadataCharset = "UTF-8";
                }
                this.characterSetMetadata = defaultMetadataCharset;
            } else {
                this.characterSetMetadata = getEncoding();
            }
            if (versionMeetsMinimum(4, 1, 0) && !versionMeetsMinimum(4, 1, 10) && getAllowMultiQueries() && isQueryCacheEnabled()) {
                setAllowMultiQueries(false);
            }
            if (versionMeetsMinimum(5, 0, 0) && ((getUseLocalTransactionState() || getElideSetAutoCommits()) && isQueryCacheEnabled() && !versionMeetsMinimum(5, 1, 32))) {
                setUseLocalTransactionState(false);
                setElideSetAutoCommits(false);
            }
            setupServerForTruncationChecks();
        } catch (SQLException ex2) {
            throw ex2;
        } catch (RuntimeException ex3) {
            ex2 = SQLError.createSQLException(ex3.toString(), SQLError.SQL_STATE_ILLEGAL_ARGUMENT, null);
            ex2.initCause(ex3);
            throw ex2;
        }
    }

    public boolean isQueryCacheEnabled() {
        return "ON".equalsIgnoreCase((String) this.serverVariables.get("query_cache_type")) && !"0".equalsIgnoreCase((String) this.serverVariables.get("query_cache_size"));
    }

    private int getServerVariableAsInt(String variableName, int fallbackValue) throws SQLException {
        try {
            return Integer.parseInt((String) this.serverVariables.get(variableName));
        } catch (NumberFormatException e) {
            getLog().logWarn(Messages.getString("Connection.BadValueInServerVariables", new Object[]{variableName, this.serverVariables.get(variableName), Integer.valueOf(fallbackValue)}));
            return fallbackValue;
        }
    }

    private void handleAutoCommitDefaults() throws SQLException {
        ConnectionImpl this;
        boolean resetAutoCommitDefault = false;
        if (getElideSetAutoCommits()) {
            if (getIO().isSetNeededForAutoCommitMode(true)) {
                this.autoCommit = false;
                resetAutoCommitDefault = true;
            }
            this = this;
        } else {
            String initConnectValue = (String) this.serverVariables.get("init_connect");
            if (!versionMeetsMinimum(4, 1, 2) || initConnectValue == null || initConnectValue.length() <= 0) {
                resetAutoCommitDefault = true;
                this = this;
            } else {
                ResultSet rs = null;
                Statement stmt = null;
                try {
                    stmt = getMetadataSafeStatement();
                    rs = stmt.executeQuery("SELECT @@session.autocommit");
                    if (rs.next()) {
                        this.autoCommit = rs.getBoolean(1);
                        resetAutoCommitDefault = this.autoCommit ^ 1;
                    }
                    this = this;
                    if (rs != null) {
                        try {
                            rs.close();
                        } catch (SQLException e) {
                        }
                    }
                    if (stmt != null) {
                        try {
                            stmt.close();
                        } catch (SQLException e2) {
                        }
                    }
                } catch (Throwable th) {
                    this = this;
                    if (rs != null) {
                        try {
                            rs.close();
                        } catch (SQLException e3) {
                        }
                    }
                    if (stmt != null) {
                        try {
                            stmt.close();
                        } catch (SQLException e4) {
                        }
                    }
                }
            }
        }
        if (resetAutoCommitDefault) {
            try {
                setAutoCommit(true);
            } catch (SQLException ex) {
                if (ex.getErrorCode() != MysqlErrorNumbers.ER_MUST_CHANGE_PASSWORD || getDisconnectOnExpiredPasswords()) {
                    throw ex;
                }
            }
        }
    }

    public boolean isClientTzUTC() {
        return this.isClientTzUTC;
    }

    public boolean isClosed() {
        return this.isClosed;
    }

    public boolean isCursorFetchEnabled() throws SQLException {
        return versionMeetsMinimum(5, 0, 2) && getUseCursorFetch();
    }

    public boolean isInGlobalTx() {
        return this.isInGlobalTx;
    }

    public boolean isMasterConnection() {
        return false;
    }

    public boolean isNoBackslashEscapesSet() {
        return this.noBackslashEscapes;
    }

    public boolean isReadInfoMsgEnabled() {
        return this.readInfoMsg;
    }

    public boolean isReadOnly() throws SQLException {
        return isReadOnly(true);
    }

    public boolean isReadOnly(boolean useSessionStatus) throws SQLException {
        ConnectionImpl this;
        if (useSessionStatus && !this.isClosed && versionMeetsMinimum(5, 6, 5) && !getUseLocalSessionState() && getReadOnlyPropagatesToServer()) {
            Statement stmt = null;
            ResultSet rs = null;
            try {
                stmt = getMetadataSafeStatement();
                rs = stmt.executeQuery(versionMeetsMinimum(8, 0, 3) ? "select @@session.transaction_read_only" : "select @@session.tx_read_only");
                if (rs.next()) {
                    boolean z = true;
                    if (rs.getInt(1) == 0) {
                        z = false;
                    }
                    if (rs != null) {
                        try {
                            rs.close();
                        } catch (Exception e) {
                        }
                    }
                    if (stmt != null) {
                        try {
                            stmt.close();
                        } catch (Exception e2) {
                        }
                    }
                    return z;
                }
                this = this;
                if (rs != null) {
                    try {
                        rs.close();
                    } catch (Exception e3) {
                    }
                }
                if (stmt != null) {
                    try {
                        stmt.close();
                    } catch (Exception e4) {
                    }
                }
            } catch (Throwable ex1) {
                if (ex1.getErrorCode() == MysqlErrorNumbers.ER_MUST_CHANGE_PASSWORD) {
                    if (getDisconnectOnExpiredPasswords()) {
                    }
                }
                throw SQLError.createSQLException("Could not retrieve transaction read-only status from server", SQLError.SQL_STATE_GENERAL_ERROR, ex1, getExceptionInterceptor());
            } catch (Throwable th) {
                if (rs != null) {
                    try {
                        rs.close();
                    } catch (Exception e5) {
                    }
                }
                if (stmt != null) {
                    try {
                        stmt.close();
                    } catch (Exception e6) {
                    }
                }
            }
        } else {
            this = this;
        }
        return this.readOnly;
    }

    public boolean isRunningOnJDK13() {
        return this.isRunningOnJDK13;
    }

    /* JADX WARNING: inconsistent code. */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    public boolean isSameResource(com.mysql.jdbc.Connection r11) {
        /*
        r10 = this;
        r0 = r10.getConnectionMutex();
        monitor-enter(r0);
        r1 = 0;
        if (r11 != 0) goto L_0x000d;
    L_0x0008:
        monitor-exit(r0);	 Catch:{ all -> 0x000a }
        return r1;
    L_0x000a:
        r1 = move-exception;
        goto L_0x0079;
    L_0x000d:
        r2 = 1;
        r3 = r11;
        r3 = (com.mysql.jdbc.ConnectionImpl) r3;	 Catch:{ all -> 0x000a }
        r3 = r3.origHostToConnectTo;	 Catch:{ all -> 0x000a }
        r4 = r11;
        r4 = (com.mysql.jdbc.ConnectionImpl) r4;	 Catch:{ all -> 0x000a }
        r4 = r4.origDatabaseToConnectTo;	 Catch:{ all -> 0x000a }
        r5 = r11;
        r5 = (com.mysql.jdbc.ConnectionImpl) r5;	 Catch:{ all -> 0x000a }
        r5 = r5.database;	 Catch:{ all -> 0x000a }
        r6 = r10.origHostToConnectTo;	 Catch:{ all -> 0x000a }
        r6 = nullSafeCompare(r3, r6);	 Catch:{ all -> 0x000a }
        r7 = 1;
        if (r6 != 0) goto L_0x0028;
    L_0x0026:
        r2 = 0;
        goto L_0x0048;
    L_0x0028:
        if (r3 == 0) goto L_0x0048;
    L_0x002a:
        r6 = 44;
        r6 = r3.indexOf(r6);	 Catch:{ all -> 0x000a }
        r8 = -1;
        if (r6 != r8) goto L_0x0048;
    L_0x0033:
        r6 = 58;
        r6 = r3.indexOf(r6);	 Catch:{ all -> 0x000a }
        if (r6 != r8) goto L_0x0048;
    L_0x003b:
        r6 = r11;
        r6 = (com.mysql.jdbc.ConnectionImpl) r6;	 Catch:{ all -> 0x000a }
        r6 = r6.origPortToConnectTo;	 Catch:{ all -> 0x000a }
        r8 = r10.origPortToConnectTo;	 Catch:{ all -> 0x000a }
        if (r6 != r8) goto L_0x0046;
    L_0x0044:
        r6 = r7;
        goto L_0x0047;
    L_0x0046:
        r6 = r1;
    L_0x0047:
        r2 = r6;
    L_0x0048:
        if (r2 == 0) goto L_0x005b;
    L_0x004a:
        r6 = r10.origDatabaseToConnectTo;	 Catch:{ all -> 0x000a }
        r6 = nullSafeCompare(r4, r6);	 Catch:{ all -> 0x000a }
        if (r6 == 0) goto L_0x005a;
    L_0x0052:
        r6 = r10.database;	 Catch:{ all -> 0x000a }
        r6 = nullSafeCompare(r5, r6);	 Catch:{ all -> 0x000a }
        if (r6 != 0) goto L_0x005b;
    L_0x005a:
        r2 = 0;
    L_0x005b:
        if (r2 == 0) goto L_0x005f;
    L_0x005d:
        monitor-exit(r0);	 Catch:{ all -> 0x000a }
        return r7;
    L_0x005f:
        r6 = r11;
        r6 = (com.mysql.jdbc.ConnectionImpl) r6;	 Catch:{ all -> 0x000a }
        r6 = r6.getResourceId();	 Catch:{ all -> 0x000a }
        r8 = r10.getResourceId();	 Catch:{ all -> 0x000a }
        if (r6 != 0) goto L_0x006e;
    L_0x006c:
        if (r8 == 0) goto L_0x0077;
    L_0x006e:
        r9 = nullSafeCompare(r6, r8);	 Catch:{ all -> 0x000a }
        r2 = r9;
        if (r2 == 0) goto L_0x0077;
    L_0x0075:
        monitor-exit(r0);	 Catch:{ all -> 0x000a }
        return r7;
    L_0x0077:
        monitor-exit(r0);	 Catch:{ all -> 0x000a }
        return r1;
    L_0x0079:
        monitor-exit(r0);	 Catch:{ all -> 0x000a }
        throw r1;
        */
        throw new UnsupportedOperationException("Method not decompiled: com.mysql.jdbc.ConnectionImpl.isSameResource(com.mysql.jdbc.Connection):boolean");
    }

    public boolean isServerTzUTC() {
        return this.isServerTzUTC;
    }

    private void createConfigCacheIfNeeded() throws SQLException {
        SQLException sqlEx;
        synchronized (getConnectionMutex()) {
            if (this.serverConfigCache != null) {
                return;
            }
            try {
                this.serverConfigCache = ((CacheAdapterFactory) Class.forName(getServerConfigCacheFactory()).newInstance()).getInstance(this, this.myURL, ActivityChooserViewAdapter.MAX_ACTIVITY_COUNT_UNLIMITED, ActivityChooserViewAdapter.MAX_ACTIVITY_COUNT_UNLIMITED, this.props);
                ExceptionInterceptor evictOnCommsError = new C04704();
                if (this.exceptionInterceptor == null) {
                    this.exceptionInterceptor = evictOnCommsError;
                } else {
                    ((ExceptionInterceptorChain) this.exceptionInterceptor).addRingZero(evictOnCommsError);
                }
            } catch (ClassNotFoundException e) {
                sqlEx = SQLError.createSQLException(Messages.getString("Connection.CantFindCacheFactory", new Object[]{getParseInfoCacheFactory(), "parseInfoCacheFactory"}), getExceptionInterceptor());
                sqlEx.initCause(e);
                throw sqlEx;
            } catch (InstantiationException e2) {
                sqlEx = SQLError.createSQLException(Messages.getString("Connection.CantLoadCacheFactory", new Object[]{getParseInfoCacheFactory(), "parseInfoCacheFactory"}), getExceptionInterceptor());
                sqlEx.initCause(e2);
                throw sqlEx;
            } catch (IllegalAccessException e3) {
                sqlEx = SQLError.createSQLException(Messages.getString("Connection.CantLoadCacheFactory", new Object[]{getParseInfoCacheFactory(), "parseInfoCacheFactory"}), getExceptionInterceptor());
                sqlEx.initCause(e3);
                throw sqlEx;
            }
        }
    }

    private void loadServerVariables() throws SQLException {
        ConnectionImpl version;
        if (getCacheServerConfiguration()) {
            createConfigCacheIfNeeded();
            Map<String, String> cachedVariableMap = (Map) this.serverConfigCache.get(getURL());
            if (cachedVariableMap != null) {
                String cachedServerVersion = (String) cachedVariableMap.get(SERVER_VERSION_STRING_VAR_NAME);
                if (cachedServerVersion == null || this.io.getServerVersion() == null || !cachedServerVersion.equals(this.io.getServerVersion())) {
                    this.serverConfigCache.invalidate(getURL());
                } else {
                    this.serverVariables = cachedVariableMap;
                    return;
                }
            }
        }
        Statement stmt = null;
        ResultSet results = null;
        try {
            int i;
            String versionComment;
            StringBuilder stringBuilder;
            StringBuilder queryBuf;
            ResultSetMetaData rsmd;
            int i2;
            ResultSetMetaData rsmd2;
            int i3;
            stmt = getMetadataSafeStatement();
            String version2 = this.dbmd.getDriverVersion();
            if (!(version2 == null || version2.indexOf(42) == -1)) {
                StringBuilder buf = new StringBuilder(version2.length() + 10);
                for (i = 0; i < version2.length(); i++) {
                    char c = version2.charAt(i);
                    if (c == '*') {
                        buf.append("[star]");
                    } else {
                        buf.append(c);
                    }
                }
                version2 = buf.toString();
            }
            if (!getParanoid()) {
                if (version2 != null) {
                    StringBuilder stringBuilder2 = new StringBuilder();
                    stringBuilder2.append("/* ");
                    stringBuilder2.append(version2);
                    stringBuilder2.append(" */");
                    versionComment = stringBuilder2.toString();
                    this.serverVariables = new HashMap();
                    i = 1;
                    if (versionMeetsMinimum(5, 1, 0)) {
                        stringBuilder = new StringBuilder();
                        stringBuilder.append(versionComment);
                        stringBuilder.append("SHOW VARIABLES");
                        results = stmt.executeQuery(stringBuilder.toString());
                        while (results.next()) {
                            this.serverVariables.put(results.getString(1), results.getString(2));
                        }
                    } else {
                        queryBuf = new StringBuilder(versionComment);
                        queryBuf.append("SELECT");
                        queryBuf.append("  @@session.auto_increment_increment AS auto_increment_increment");
                        queryBuf.append(", @@character_set_client AS character_set_client");
                        queryBuf.append(", @@character_set_connection AS character_set_connection");
                        queryBuf.append(", @@character_set_results AS character_set_results");
                        queryBuf.append(", @@character_set_server AS character_set_server");
                        queryBuf.append(", @@collation_server AS collation_server");
                        queryBuf.append(", @@init_connect AS init_connect");
                        queryBuf.append(", @@interactive_timeout AS interactive_timeout");
                        if (!versionMeetsMinimum(5, 5, 0)) {
                            queryBuf.append(", @@language AS language");
                        }
                        queryBuf.append(", @@license AS license");
                        queryBuf.append(", @@lower_case_table_names AS lower_case_table_names");
                        queryBuf.append(", @@max_allowed_packet AS max_allowed_packet");
                        queryBuf.append(", @@net_buffer_length AS net_buffer_length");
                        queryBuf.append(", @@net_write_timeout AS net_write_timeout");
                        if (versionMeetsMinimum(8, 0, 3)) {
                            queryBuf.append(", @@query_cache_size AS query_cache_size");
                            queryBuf.append(", @@query_cache_type AS query_cache_type");
                        } else {
                            queryBuf.append(", @@have_query_cache AS have_query_cache");
                        }
                        queryBuf.append(", @@sql_mode AS sql_mode");
                        queryBuf.append(", @@system_time_zone AS system_time_zone");
                        queryBuf.append(", @@time_zone AS time_zone");
                        if (versionMeetsMinimum(8, 0, 3)) {
                            queryBuf.append(", @@tx_isolation AS tx_isolation");
                        } else {
                            queryBuf.append(", @@transaction_isolation AS transaction_isolation");
                        }
                        queryBuf.append(", @@wait_timeout AS wait_timeout");
                        results = stmt.executeQuery(queryBuf.toString());
                        if (results.next()) {
                            rsmd = results.getMetaData();
                            for (i2 = 1; i2 <= rsmd.getColumnCount(); i2++) {
                                this.serverVariables.put(rsmd.getColumnLabel(i2), results.getString(i2));
                            }
                        }
                        if (versionMeetsMinimum(8, 0, 3) && "YES".equalsIgnoreCase((String) this.serverVariables.get("have_query_cache"))) {
                            results.close();
                            results = stmt.executeQuery("SELECT @@query_cache_size AS query_cache_size, @@query_cache_type AS query_cache_type");
                            if (results.next()) {
                                rsmd2 = results.getMetaData();
                                while (true) {
                                    i3 = i;
                                    if (i3 <= rsmd2.getColumnCount()) {
                                        break;
                                    }
                                    this.serverVariables.put(rsmd2.getColumnLabel(i3), results.getString(i3));
                                    i = i3 + 1;
                                }
                            }
                        }
                    }
                    results.close();
                    results = null;
                    if (getCacheServerConfiguration()) {
                        this.serverVariables.put(SERVER_VERSION_STRING_VAR_NAME, this.io.getServerVersion());
                        this.serverConfigCache.put(getURL(), this.serverVariables);
                    }
                    version = this;
                    if (results != null) {
                        try {
                            results.close();
                        } catch (SQLException e) {
                        }
                    }
                    if (stmt != null) {
                        try {
                            stmt.close();
                        } catch (SQLException e2) {
                        }
                    }
                }
            }
            versionComment = "";
            this.serverVariables = new HashMap();
            i = 1;
            if (versionMeetsMinimum(5, 1, 0)) {
                stringBuilder = new StringBuilder();
                stringBuilder.append(versionComment);
                stringBuilder.append("SHOW VARIABLES");
                results = stmt.executeQuery(stringBuilder.toString());
                while (results.next()) {
                    this.serverVariables.put(results.getString(1), results.getString(2));
                }
            } else {
                queryBuf = new StringBuilder(versionComment);
                queryBuf.append("SELECT");
                queryBuf.append("  @@session.auto_increment_increment AS auto_increment_increment");
                queryBuf.append(", @@character_set_client AS character_set_client");
                queryBuf.append(", @@character_set_connection AS character_set_connection");
                queryBuf.append(", @@character_set_results AS character_set_results");
                queryBuf.append(", @@character_set_server AS character_set_server");
                queryBuf.append(", @@collation_server AS collation_server");
                queryBuf.append(", @@init_connect AS init_connect");
                queryBuf.append(", @@interactive_timeout AS interactive_timeout");
                if (versionMeetsMinimum(5, 5, 0)) {
                    queryBuf.append(", @@language AS language");
                }
                queryBuf.append(", @@license AS license");
                queryBuf.append(", @@lower_case_table_names AS lower_case_table_names");
                queryBuf.append(", @@max_allowed_packet AS max_allowed_packet");
                queryBuf.append(", @@net_buffer_length AS net_buffer_length");
                queryBuf.append(", @@net_write_timeout AS net_write_timeout");
                if (versionMeetsMinimum(8, 0, 3)) {
                    queryBuf.append(", @@query_cache_size AS query_cache_size");
                    queryBuf.append(", @@query_cache_type AS query_cache_type");
                } else {
                    queryBuf.append(", @@have_query_cache AS have_query_cache");
                }
                queryBuf.append(", @@sql_mode AS sql_mode");
                queryBuf.append(", @@system_time_zone AS system_time_zone");
                queryBuf.append(", @@time_zone AS time_zone");
                if (versionMeetsMinimum(8, 0, 3)) {
                    queryBuf.append(", @@tx_isolation AS tx_isolation");
                } else {
                    queryBuf.append(", @@transaction_isolation AS transaction_isolation");
                }
                queryBuf.append(", @@wait_timeout AS wait_timeout");
                results = stmt.executeQuery(queryBuf.toString());
                if (results.next()) {
                    rsmd = results.getMetaData();
                    for (i2 = 1; i2 <= rsmd.getColumnCount(); i2++) {
                        this.serverVariables.put(rsmd.getColumnLabel(i2), results.getString(i2));
                    }
                }
                results.close();
                results = stmt.executeQuery("SELECT @@query_cache_size AS query_cache_size, @@query_cache_type AS query_cache_type");
                if (results.next()) {
                    rsmd2 = results.getMetaData();
                    while (true) {
                        i3 = i;
                        if (i3 <= rsmd2.getColumnCount()) {
                            break;
                        }
                        this.serverVariables.put(rsmd2.getColumnLabel(i3), results.getString(i3));
                        i = i3 + 1;
                    }
                }
            }
            results.close();
            results = null;
        } catch (SQLException ex) {
            if (ex.getErrorCode() == MysqlErrorNumbers.ER_MUST_CHANGE_PASSWORD) {
                if (getDisconnectOnExpiredPasswords()) {
                }
            }
            throw ex;
        } catch (SQLException e3) {
            try {
                throw e3;
            } catch (Throwable th) {
                if (results != null) {
                    try {
                        results.close();
                    } catch (SQLException e4) {
                    }
                }
                if (stmt != null) {
                    try {
                        stmt.close();
                    } catch (SQLException e5) {
                    }
                }
            }
        }
        if (getCacheServerConfiguration()) {
            this.serverVariables.put(SERVER_VERSION_STRING_VAR_NAME, this.io.getServerVersion());
            this.serverConfigCache.put(getURL(), this.serverVariables);
        }
        version = this;
        if (results != null) {
            results.close();
        }
        if (stmt != null) {
            stmt.close();
        }
    }

    public int getAutoIncrementIncrement() {
        return this.autoIncrementIncrement;
    }

    public boolean lowerCaseTableNames() {
        return this.lowerCaseTableNames;
    }

    public String nativeSQL(String sql) throws SQLException {
        if (sql == null) {
            return null;
        }
        Object escapedSqlResult = EscapeProcessor.escapeSQL(sql, serverSupportsConvertFn(), getMultiHostSafeProxy());
        if (escapedSqlResult instanceof String) {
            return (String) escapedSqlResult;
        }
        return ((EscapeProcessorResult) escapedSqlResult).escapedSql;
    }

    private CallableStatement parseCallableStatement(String sql) throws SQLException {
        String parsedSql;
        boolean isFunctionCall;
        String escapedSqlResult = EscapeProcessor.escapeSQL(sql, serverSupportsConvertFn(), getMultiHostSafeProxy());
        if (escapedSqlResult instanceof EscapeProcessorResult) {
            parsedSql = ((EscapeProcessorResult) escapedSqlResult).escapedSql;
            isFunctionCall = ((EscapeProcessorResult) escapedSqlResult).callingStoredFunction;
        } else {
            parsedSql = escapedSqlResult;
            isFunctionCall = false;
        }
        return CallableStatement.getInstance(getMultiHostSafeProxy(), parsedSql, this.database, isFunctionCall);
    }

    public boolean parserKnowsUnicode() {
        return this.parserKnowsUnicode;
    }

    public void ping() throws SQLException {
        pingInternal(true, 0);
    }

    public void pingInternal(boolean checkForClosedConnection, int timeoutMillis) throws SQLException {
        if (checkForClosedConnection) {
            checkClosed();
        }
        long pingMillisLifetime = (long) getSelfDestructOnPingSecondsLifetime();
        int pingMaxOperations = getSelfDestructOnPingMaxOperations();
        if ((pingMillisLifetime <= 0 || System.currentTimeMillis() - this.connectionCreationTimeMillis <= pingMillisLifetime) && (pingMaxOperations <= 0 || pingMaxOperations > this.io.getCommandCount())) {
            this.io.sendCommand(14, null, null, false, null, timeoutMillis);
        } else {
            close();
            throw SQLError.createSQLException(Messages.getString("Connection.exceededConnectionLifetime"), SQLError.SQL_STATE_COMMUNICATION_LINK_FAILURE, getExceptionInterceptor());
        }
    }

    public CallableStatement prepareCall(String sql) throws SQLException {
        return prepareCall(sql, 1003, 1007);
    }

    public CallableStatement prepareCall(String sql, int resultSetType, int resultSetConcurrency) throws SQLException {
        if (versionMeetsMinimum(5, 0, 0)) {
            CallableStatement cStmt;
            if (getCacheCallableStatements()) {
                synchronized (this.parsedCallableStatementCache) {
                    CompoundCacheKey key = new CompoundCacheKey(getCatalog(), sql);
                    CallableStatementParamInfo cachedParamInfo = (CallableStatementParamInfo) this.parsedCallableStatementCache.get(key);
                    if (cachedParamInfo != null) {
                        cStmt = CallableStatement.getInstance(getMultiHostSafeProxy(), cachedParamInfo);
                    } else {
                        cStmt = parseCallableStatement(sql);
                        synchronized (cStmt) {
                            cachedParamInfo = cStmt.paramInfo;
                        }
                        this.parsedCallableStatementCache.put(key, cachedParamInfo);
                    }
                }
            } else {
                cStmt = parseCallableStatement(sql);
            }
            cStmt.setResultSetType(resultSetType);
            cStmt.setResultSetConcurrency(resultSetConcurrency);
            return cStmt;
        }
        throw SQLError.createSQLException("Callable statements not supported.", SQLError.SQL_STATE_DRIVER_NOT_CAPABLE, getExceptionInterceptor());
    }

    public CallableStatement prepareCall(String sql, int resultSetType, int resultSetConcurrency, int resultSetHoldability) throws SQLException {
        if (!getPedantic() || resultSetHoldability == 1) {
            return (CallableStatement) prepareCall(sql, resultSetType, resultSetConcurrency);
        }
        throw SQLError.createSQLException("HOLD_CUSRORS_OVER_COMMIT is only supported holdability level", SQLError.SQL_STATE_ILLEGAL_ARGUMENT, getExceptionInterceptor());
    }

    public PreparedStatement prepareStatement(String sql) throws SQLException {
        return prepareStatement(sql, 1003, 1007);
    }

    public PreparedStatement prepareStatement(String sql, int autoGenKeyIndex) throws SQLException {
        PreparedStatement pStmt = prepareStatement(sql);
        PreparedStatement preparedStatement = (PreparedStatement) pStmt;
        boolean z = true;
        if (autoGenKeyIndex != 1) {
            z = false;
        }
        preparedStatement.setRetrieveGeneratedKeys(z);
        return pStmt;
    }

    public PreparedStatement prepareStatement(String sql, int resultSetType, int resultSetConcurrency) throws SQLException {
        PreparedStatement pStmt;
        synchronized (getConnectionMutex()) {
            checkClosed();
            boolean canServerPrepare = true;
            String nativeSql = getProcessEscapeCodesForPrepStmts() ? nativeSQL(sql) : sql;
            if (this.useServerPreparedStmts && getEmulateUnsupportedPstmts()) {
                canServerPrepare = canHandleAsServerPreparedStatement(nativeSql);
            }
            if (!this.useServerPreparedStmts || !canServerPrepare) {
                pStmt = (PreparedStatement) clientPrepareStatement(nativeSql, resultSetType, resultSetConcurrency, false);
            } else if (getCachePreparedStatements()) {
                synchronized (this.serverSideStatementCache) {
                    pStmt = (PreparedStatement) this.serverSideStatementCache.remove(new CompoundCacheKey(this.database, sql));
                    if (pStmt != null) {
                        ((ServerPreparedStatement) pStmt).setClosed(false);
                        pStmt.clearParameters();
                    }
                    if (pStmt == null) {
                        try {
                            pStmt = ServerPreparedStatement.getInstance(getMultiHostSafeProxy(), nativeSql, this.database, resultSetType, resultSetConcurrency);
                            if (sql.length() < getPreparedStatementCacheSqlLimit()) {
                                ((ServerPreparedStatement) pStmt).isCached = true;
                            }
                            pStmt.setResultSetType(resultSetType);
                            pStmt.setResultSetConcurrency(resultSetConcurrency);
                        } catch (SQLException sqlEx) {
                            if (getEmulateUnsupportedPstmts()) {
                                pStmt = (PreparedStatement) clientPrepareStatement(nativeSql, resultSetType, resultSetConcurrency, false);
                                if (sql.length() < getPreparedStatementCacheSqlLimit()) {
                                    this.serverSideStatementCheckCache.put(sql, Boolean.FALSE);
                                }
                            } else {
                                throw sqlEx;
                            }
                        }
                    }
                }
            } else {
                try {
                    pStmt = ServerPreparedStatement.getInstance(getMultiHostSafeProxy(), nativeSql, this.database, resultSetType, resultSetConcurrency);
                    pStmt.setResultSetType(resultSetType);
                    pStmt.setResultSetConcurrency(resultSetConcurrency);
                } catch (SQLException sqlEx2) {
                    if (getEmulateUnsupportedPstmts()) {
                        pStmt = (PreparedStatement) clientPrepareStatement(nativeSql, resultSetType, resultSetConcurrency, false);
                    } else {
                        throw sqlEx2;
                    }
                }
            }
        }
        return pStmt;
    }

    public PreparedStatement prepareStatement(String sql, int resultSetType, int resultSetConcurrency, int resultSetHoldability) throws SQLException {
        if (!getPedantic() || resultSetHoldability == 1) {
            return prepareStatement(sql, resultSetType, resultSetConcurrency);
        }
        throw SQLError.createSQLException("HOLD_CUSRORS_OVER_COMMIT is only supported holdability level", SQLError.SQL_STATE_ILLEGAL_ARGUMENT, getExceptionInterceptor());
    }

    public PreparedStatement prepareStatement(String sql, int[] autoGenKeyIndexes) throws SQLException {
        PreparedStatement pStmt = prepareStatement(sql);
        PreparedStatement preparedStatement = (PreparedStatement) pStmt;
        boolean z = autoGenKeyIndexes != null && autoGenKeyIndexes.length > 0;
        preparedStatement.setRetrieveGeneratedKeys(z);
        return pStmt;
    }

    public PreparedStatement prepareStatement(String sql, String[] autoGenKeyColNames) throws SQLException {
        PreparedStatement pStmt = prepareStatement(sql);
        PreparedStatement preparedStatement = (PreparedStatement) pStmt;
        boolean z = autoGenKeyColNames != null && autoGenKeyColNames.length > 0;
        preparedStatement.setRetrieveGeneratedKeys(z);
        return pStmt;
    }

    public void realClose(boolean calledExplicitly, boolean issueRollback, boolean skipLocalTeardown, Throwable reason) throws SQLException {
        Throwable e;
        Throwable th;
        ConnectionImpl connectionImpl = this;
        SQLException sqlEx = null;
        if (!isClosed()) {
            int i;
            SQLException sqlEx2;
            Object obj;
            Throwable reason2 = reason;
            connectionImpl.forceClosedReason = reason2;
            if (skipLocalTeardown) {
                connectionImpl.io.forceClose();
                if (connectionImpl.statementInterceptors != null) {
                    for (i = 0; i < connectionImpl.statementInterceptors.size(); i++) {
                        ((StatementInterceptorV2) connectionImpl.statementInterceptors.get(i)).destroy();
                    }
                }
                if (connectionImpl.exceptionInterceptor != null) {
                    connectionImpl.exceptionInterceptor.destroy();
                }
                sqlEx2 = sqlEx;
                connectionImpl.openStatements.clear();
                if (connectionImpl.io != null) {
                    obj = null;
                } else {
                    sqlEx.io.releaseResources();
                    obj = null;
                    sqlEx.io = null;
                }
                sqlEx.statementInterceptors = obj;
                sqlEx.exceptionInterceptor = obj;
                ProfilerEventHandlerFactory.removeInstance(sqlEx);
                synchronized (getConnectionMutex()) {
                    if (sqlEx.cancelTimer != null) {
                        sqlEx.cancelTimer.cancel();
                    }
                }
                sqlEx.isClosed = true;
                if (sqlEx2 == null) {
                    throw sqlEx2;
                }
            }
            if (!getAutoCommit() && issueRollback) {
                try {
                    rollback();
                } catch (SQLException e2) {
                    e = e2;
                    sqlEx = e;
                }
            }
            try {
                reportMetrics();
                if (getUseUsageAdvisor()) {
                    if (!calledExplicitly) {
                        String message = "Connection implicitly closed by Driver. You should call Connection.close() from your code to free resources more efficiently and avoid resource leaks.";
                        ProfilerEventHandler profilerEventHandler = connectionImpl.eventSink;
                        String str = "";
                        String catalog = getCatalog();
                        long id = getId();
                        long currentTimeMillis = System.currentTimeMillis();
                        String str2 = Constants.MILLIS_I18N;
                        String str3 = connectionImpl.pointOfOrigin;
                    }
                }
                closeAllOpenStatements();
                if (connectionImpl.io != null) {
                    try {
                        connectionImpl.io.quit();
                    } catch (Exception e3) {
                    }
                }
                if (connectionImpl.statementInterceptors != null) {
                    for (i = 0; i < connectionImpl.statementInterceptors.size(); i++) {
                        ((StatementInterceptorV2) connectionImpl.statementInterceptors.get(i)).destroy();
                    }
                }
                if (connectionImpl.exceptionInterceptor != null) {
                    connectionImpl.exceptionInterceptor.destroy();
                }
                sqlEx2 = sqlEx;
                connectionImpl.openStatements.clear();
                if (connectionImpl.io != null) {
                    sqlEx.io.releaseResources();
                    obj = null;
                    sqlEx.io = null;
                } else {
                    obj = null;
                }
                sqlEx.statementInterceptors = obj;
                sqlEx.exceptionInterceptor = obj;
                ProfilerEventHandlerFactory.removeInstance(sqlEx);
                synchronized (getConnectionMutex()) {
                    try {
                        if (sqlEx.cancelTimer != null) {
                            sqlEx.cancelTimer.cancel();
                        }
                    } catch (Throwable e4) {
                        while (true) {
                            th = e4;
                        }
                    }
                }
                sqlEx.isClosed = true;
                if (sqlEx2 == null) {
                    throw sqlEx2;
                }
            } catch (Throwable th2) {
                Object obj2;
                e4 = th2;
                SQLException sQLException = sqlEx;
                connectionImpl.openStatements.clear();
                if (connectionImpl.io != null) {
                    this.io.releaseResources();
                    obj2 = null;
                    this.io = null;
                } else {
                    obj2 = null;
                }
                this.statementInterceptors = obj2;
                this.exceptionInterceptor = obj2;
                ProfilerEventHandlerFactory.removeInstance(this);
                synchronized (getConnectionMutex()) {
                    if (this.cancelTimer != null) {
                        this.cancelTimer.cancel();
                    }
                    this.isClosed = true;
                }
            } finally {
                th = e4;
            }
            if (System.currentTimeMillis() - connectionImpl.connectionCreationTimeMillis < 500) {
                connectionImpl.eventSink.consumeEvent(new ProfilerEvent((byte) 0, "", getCatalog(), getId(), -1, -1, System.currentTimeMillis(), 0, Constants.MILLIS_I18N, null, connectionImpl.pointOfOrigin, "Connection lifetime of < .5 seconds. You might be un-necessarily creating short-lived connections and should investigate connection pooling to be more efficient."));
            }
            try {
                closeAllOpenStatements();
            } catch (SQLException e5) {
                sqlEx = e5;
            }
            if (connectionImpl.io != null) {
                connectionImpl.io.quit();
            }
            if (connectionImpl.statementInterceptors != null) {
                for (i = 0; i < connectionImpl.statementInterceptors.size(); i++) {
                    ((StatementInterceptorV2) connectionImpl.statementInterceptors.get(i)).destroy();
                }
            }
            if (connectionImpl.exceptionInterceptor != null) {
                connectionImpl.exceptionInterceptor.destroy();
            }
            sqlEx2 = sqlEx;
            connectionImpl.openStatements.clear();
            if (connectionImpl.io != null) {
                sqlEx.io.releaseResources();
                obj = null;
                sqlEx.io = null;
            } else {
                obj = null;
            }
            sqlEx.statementInterceptors = obj;
            sqlEx.exceptionInterceptor = obj;
            ProfilerEventHandlerFactory.removeInstance(sqlEx);
            synchronized (getConnectionMutex()) {
                if (sqlEx.cancelTimer != null) {
                    sqlEx.cancelTimer.cancel();
                }
            }
            sqlEx.isClosed = true;
            if (sqlEx2 == null) {
                throw sqlEx2;
            }
        }
    }

    public void recachePreparedStatement(ServerPreparedStatement pstmt) throws SQLException {
        synchronized (getConnectionMutex()) {
            if (getCachePreparedStatements() && pstmt.isPoolable()) {
                synchronized (this.serverSideStatementCache) {
                    ServerPreparedStatement oldServerPrepStmt = this.serverSideStatementCache.put(new CompoundCacheKey(pstmt.currentCatalog, pstmt.originalSql), pstmt);
                    if (!(oldServerPrepStmt == null || oldServerPrepStmt == pstmt)) {
                        oldServerPrepStmt.isCached = false;
                        oldServerPrepStmt.setClosed(false);
                        oldServerPrepStmt.realClose(true, true);
                    }
                }
            }
        }
    }

    public void decachePreparedStatement(ServerPreparedStatement pstmt) throws SQLException {
        synchronized (getConnectionMutex()) {
            if (getCachePreparedStatements() && pstmt.isPoolable()) {
                synchronized (this.serverSideStatementCache) {
                    this.serverSideStatementCache.remove(new CompoundCacheKey(pstmt.currentCatalog, pstmt.originalSql));
                }
            }
        }
    }

    public void registerQueryExecutionTime(long queryTimeMs) {
        if (queryTimeMs > this.longestQueryTimeMs) {
            this.longestQueryTimeMs = queryTimeMs;
            repartitionPerformanceHistogram();
        }
        addToPerformanceHistogram(queryTimeMs, 1);
        if (queryTimeMs < this.shortestQueryTimeMs) {
            this.shortestQueryTimeMs = queryTimeMs == 0 ? 1 : queryTimeMs;
        }
        this.numberOfQueriesIssued++;
        this.totalQueryTimeMs += (double) queryTimeMs;
    }

    public void registerStatement(Statement stmt) {
        this.openStatements.addIfAbsent(stmt);
    }

    public void releaseSavepoint(Savepoint arg0) throws SQLException {
    }

    private void repartitionHistogram(int[] histCounts, long[] histBreakpoints, long currentLowerBound, long currentUpperBound) {
        Object obj = histCounts;
        Object obj2 = histBreakpoints;
        if (this.oldHistCounts == null) {
            r10.oldHistCounts = new int[obj.length];
            r10.oldHistBreakpoints = new long[obj2.length];
        }
        int i = 0;
        System.arraycopy(obj, 0, r10.oldHistCounts, 0, obj.length);
        System.arraycopy(obj2, 0, r10.oldHistBreakpoints, 0, obj2.length);
        createInitialHistogram(obj2, currentLowerBound, currentUpperBound);
        while (true) {
            int i2 = i;
            if (i2 < 20) {
                addToHistogram(obj, obj2, r10.oldHistBreakpoints[i2], r10.oldHistCounts[i2], currentLowerBound, currentUpperBound);
                i = i2 + 1;
            } else {
                return;
            }
        }
    }

    private void repartitionPerformanceHistogram() {
        checkAndCreatePerformanceHistogram();
        repartitionHistogram(this.perfMetricsHistCounts, this.perfMetricsHistBreakpoints, this.shortestQueryTimeMs == Long.MAX_VALUE ? 0 : this.shortestQueryTimeMs, this.longestQueryTimeMs);
    }

    private void repartitionTablesAccessedHistogram() {
        checkAndCreateTablesAccessedHistogram();
        repartitionHistogram(this.numTablesMetricsHistCounts, this.numTablesMetricsHistBreakpoints, this.minimumNumberTablesAccessed == Long.MAX_VALUE ? 0 : this.minimumNumberTablesAccessed, this.maximumNumberTablesAccessed);
    }

    private void reportMetrics() {
        if (getGatherPerformanceMetrics()) {
            int highestCount;
            int i;
            int numPointsToGraph;
            StringBuilder logMessage = new StringBuilder(256);
            logMessage.append("** Performance Metrics Report **\n");
            StringBuilder stringBuilder = new StringBuilder();
            stringBuilder.append("\nLongest reported query: ");
            stringBuilder.append(this.longestQueryTimeMs);
            stringBuilder.append(" ms");
            logMessage.append(stringBuilder.toString());
            stringBuilder = new StringBuilder();
            stringBuilder.append("\nShortest reported query: ");
            stringBuilder.append(this.shortestQueryTimeMs);
            stringBuilder.append(" ms");
            logMessage.append(stringBuilder.toString());
            stringBuilder = new StringBuilder();
            stringBuilder.append("\nAverage query execution time: ");
            stringBuilder.append(this.totalQueryTimeMs / ((double) this.numberOfQueriesIssued));
            stringBuilder.append(" ms");
            logMessage.append(stringBuilder.toString());
            stringBuilder = new StringBuilder();
            stringBuilder.append("\nNumber of statements executed: ");
            stringBuilder.append(this.numberOfQueriesIssued);
            logMessage.append(stringBuilder.toString());
            stringBuilder = new StringBuilder();
            stringBuilder.append("\nNumber of result sets created: ");
            stringBuilder.append(this.numberOfResultSetsCreated);
            logMessage.append(stringBuilder.toString());
            stringBuilder = new StringBuilder();
            stringBuilder.append("\nNumber of statements prepared: ");
            stringBuilder.append(this.numberOfPrepares);
            logMessage.append(stringBuilder.toString());
            stringBuilder = new StringBuilder();
            stringBuilder.append("\nNumber of prepared statement executions: ");
            stringBuilder.append(this.numberOfPreparedExecutes);
            logMessage.append(stringBuilder.toString());
            if (this.perfMetricsHistBreakpoints != null) {
                logMessage.append("\n\n\tTiming Histogram:\n");
                highestCount = Integer.MIN_VALUE;
                for (i = 0; i < 20; i++) {
                    if (this.perfMetricsHistCounts[i] > highestCount) {
                        highestCount = this.perfMetricsHistCounts[i];
                    }
                }
                if (highestCount == 0) {
                    highestCount = 1;
                }
                for (i = 0; i < 19; i++) {
                    StringBuilder stringBuilder2;
                    if (i == 0) {
                        stringBuilder2 = new StringBuilder();
                        stringBuilder2.append("\n\tless than ");
                        stringBuilder2.append(this.perfMetricsHistBreakpoints[i + 1]);
                        stringBuilder2.append(" ms: \t");
                        stringBuilder2.append(this.perfMetricsHistCounts[i]);
                        logMessage.append(stringBuilder2.toString());
                    } else {
                        stringBuilder2 = new StringBuilder();
                        stringBuilder2.append("\n\tbetween ");
                        stringBuilder2.append(this.perfMetricsHistBreakpoints[i]);
                        stringBuilder2.append(" and ");
                        stringBuilder2.append(this.perfMetricsHistBreakpoints[i + 1]);
                        stringBuilder2.append(" ms: \t");
                        stringBuilder2.append(this.perfMetricsHistCounts[i]);
                        logMessage.append(stringBuilder2.toString());
                    }
                    logMessage.append("\t");
                    numPointsToGraph = (int) (((double) 20) * (((double) this.perfMetricsHistCounts[i]) / ((double) highestCount)));
                    for (int j = 0; j < numPointsToGraph; j++) {
                        logMessage.append("*");
                    }
                    if (this.longestQueryTimeMs < ((long) this.perfMetricsHistCounts[i + 1])) {
                        break;
                    }
                }
                if (this.perfMetricsHistBreakpoints[18] < this.longestQueryTimeMs) {
                    logMessage.append("\n\tbetween ");
                    logMessage.append(this.perfMetricsHistBreakpoints[18]);
                    logMessage.append(" and ");
                    logMessage.append(this.perfMetricsHistBreakpoints[19]);
                    logMessage.append(" ms: \t");
                    logMessage.append(this.perfMetricsHistCounts[19]);
                }
            }
            if (this.numTablesMetricsHistBreakpoints != null) {
                logMessage.append("\n\n\tTable Join Histogram:\n");
                highestCount = Integer.MIN_VALUE;
                for (i = 0; i < 20; i++) {
                    if (this.numTablesMetricsHistCounts[i] > highestCount) {
                        highestCount = this.numTablesMetricsHistCounts[i];
                    }
                }
                if (highestCount == 0) {
                    highestCount = 1;
                }
                for (int i2 = 0; i2 < 19; i2++) {
                    StringBuilder stringBuilder3;
                    if (i2 == 0) {
                        stringBuilder3 = new StringBuilder();
                        stringBuilder3.append("\n\t");
                        stringBuilder3.append(this.numTablesMetricsHistBreakpoints[i2 + 1]);
                        stringBuilder3.append(" tables or less: \t\t");
                        stringBuilder3.append(this.numTablesMetricsHistCounts[i2]);
                        logMessage.append(stringBuilder3.toString());
                    } else {
                        stringBuilder3 = new StringBuilder();
                        stringBuilder3.append("\n\tbetween ");
                        stringBuilder3.append(this.numTablesMetricsHistBreakpoints[i2]);
                        stringBuilder3.append(" and ");
                        stringBuilder3.append(this.numTablesMetricsHistBreakpoints[i2 + 1]);
                        stringBuilder3.append(" tables: \t");
                        stringBuilder3.append(this.numTablesMetricsHistCounts[i2]);
                        logMessage.append(stringBuilder3.toString());
                    }
                    logMessage.append("\t");
                    i = (int) (((double) 20) * (((double) this.numTablesMetricsHistCounts[i2]) / ((double) highestCount)));
                    for (numPointsToGraph = 0; numPointsToGraph < i; numPointsToGraph++) {
                        logMessage.append("*");
                    }
                    if (this.maximumNumberTablesAccessed < this.numTablesMetricsHistBreakpoints[i2 + 1]) {
                        break;
                    }
                }
                if (this.numTablesMetricsHistBreakpoints[18] < this.maximumNumberTablesAccessed) {
                    logMessage.append("\n\tbetween ");
                    logMessage.append(this.numTablesMetricsHistBreakpoints[18]);
                    logMessage.append(" and ");
                    logMessage.append(this.numTablesMetricsHistBreakpoints[19]);
                    logMessage.append(" tables: ");
                    logMessage.append(this.numTablesMetricsHistCounts[19]);
                }
            }
            this.log.logInfo(logMessage);
            this.metricsLastReportedMs = System.currentTimeMillis();
        }
    }

    protected void reportMetricsIfNeeded() {
        if (getGatherPerformanceMetrics() && System.currentTimeMillis() - this.metricsLastReportedMs > ((long) getReportMetricsIntervalMillis())) {
            reportMetrics();
        }
    }

    public void reportNumberOfTablesAccessed(int numTablesAccessed) {
        if (((long) numTablesAccessed) < this.minimumNumberTablesAccessed) {
            this.minimumNumberTablesAccessed = (long) numTablesAccessed;
        }
        if (((long) numTablesAccessed) > this.maximumNumberTablesAccessed) {
            this.maximumNumberTablesAccessed = (long) numTablesAccessed;
            repartitionTablesAccessedHistogram();
        }
        addToTablesAccessedHistogram((long) numTablesAccessed, 1);
    }

    public void resetServerState() throws SQLException {
        if (!getParanoid() && this.io != null && versionMeetsMinimum(4, 0, 6)) {
            changeUser(this.user, this.password);
        }
    }

    public void rollback() throws SQLException {
        ConnectionImpl connectionImpl;
        Throwable th;
        synchronized (getConnectionMutex()) {
            try {
                checkClosed();
                if (this.connectionLifecycleInterceptors != null) {
                    IterateBlock<Extension> iter = new IterateBlock<Extension>(this.connectionLifecycleInterceptors.iterator()) {
                        void forEach(Extension each) throws SQLException {
                            if (!((ConnectionLifecycleInterceptor) each).rollback()) {
                                this.stopIterating = true;
                            }
                        }
                    };
                    iter.doForAll();
                    if (!iter.fullIteration()) {
                        this.needsPing = getReconnectAtTxEnd();
                        return;
                    }
                }
                if (!this.autoCommit || getRelaxAutoCommit()) {
                    if (this.transactionsSupported) {
                        rollbackNoChecks();
                    }
                    try {
                        this.needsPing = getReconnectAtTxEnd();
                        return;
                    } catch (Throwable th2) {
                        Throwable th3 = th2;
                        connectionImpl = this;
                        th = th3;
                        throw th;
                    }
                }
                throw SQLError.createSQLException("Can't call rollback when autocommit=true", SQLError.SQL_STATE_CONNECTION_NOT_OPEN, getExceptionInterceptor());
            } catch (SQLException sqlEx) {
                if (getIgnoreNonTxTables() && sqlEx.getErrorCode() == MysqlErrorNumbers.ER_WARNING_NOT_COMPLETE_ROLLBACK) {
                    this.needsPing = getReconnectAtTxEnd();
                    return;
                }
                throw sqlEx;
            } catch (SQLException sqlEx2) {
                try {
                    if (SQLError.SQL_STATE_COMMUNICATION_LINK_FAILURE.equals(sqlEx2.getSQLState())) {
                        throw SQLError.createSQLException("Communications link failure during rollback(). Transaction resolution unknown.", SQLError.SQL_STATE_TRANSACTION_RESOLUTION_UNKNOWN, getExceptionInterceptor());
                    }
                    throw sqlEx2;
                } catch (Throwable th4) {
                    th = th4;
                    throw th;
                }
            } catch (Throwable th5) {
                th = th5;
                connectionImpl = this;
                throw th;
            }
        }
    }

    public void rollback(final Savepoint savepoint) throws SQLException {
        Throwable th;
        ConnectionImpl this;
        ConnectionImpl this2;
        synchronized (getConnectionMutex()) {
            try {
                if (!versionMeetsMinimum(4, 0, 14)) {
                    if (!versionMeetsMinimum(4, 1, 1)) {
                        throw SQLError.createSQLFeatureNotSupportedException();
                    }
                }
                checkClosed();
                try {
                    if (this.connectionLifecycleInterceptors != null) {
                        IterateBlock<Extension> iter = new IterateBlock<Extension>(this.connectionLifecycleInterceptors.iterator()) {
                            void forEach(Extension each) throws SQLException {
                                if (!((ConnectionLifecycleInterceptor) each).rollback(savepoint)) {
                                    this.stopIterating = true;
                                }
                            }
                        };
                        iter.doForAll();
                        if (!iter.fullIteration()) {
                            this.needsPing = getReconnectAtTxEnd();
                            return;
                        }
                    }
                    StringBuilder rollbackQuery = new StringBuilder("ROLLBACK TO SAVEPOINT ");
                    rollbackQuery.append('`');
                    rollbackQuery.append(savepoint.getSavepointName());
                    rollbackQuery.append('`');
                    Statement stmt = null;
                    try {
                        stmt = getMetadataSafeStatement();
                        stmt.executeUpdate(rollbackQuery.toString());
                        closeStatement(stmt);
                        this.needsPing = getReconnectAtTxEnd();
                    } catch (SQLException sqlEx) {
                        int errno = sqlEx.getErrorCode();
                        if (errno == MysqlErrorNumbers.ER_ERROR_DURING_ROLLBACK) {
                            String msg = sqlEx.getMessage();
                            if (!(msg == null || msg.indexOf("153") == -1)) {
                                StringBuilder stringBuilder = new StringBuilder();
                                stringBuilder.append("Savepoint '");
                                stringBuilder.append(savepoint.getSavepointName());
                                stringBuilder.append("' does not exist");
                                throw SQLError.createSQLException(stringBuilder.toString(), SQLError.SQL_STATE_ILLEGAL_ARGUMENT, errno, getExceptionInterceptor());
                            }
                        }
                        if (getIgnoreNonTxTables() && sqlEx.getErrorCode() != MysqlErrorNumbers.ER_WARNING_NOT_COMPLETE_ROLLBACK) {
                            throw sqlEx;
                        } else if (SQLError.SQL_STATE_COMMUNICATION_LINK_FAILURE.equals(sqlEx.getSQLState())) {
                            throw SQLError.createSQLException("Communications link failure during rollback(). Transaction resolution unknown.", SQLError.SQL_STATE_TRANSACTION_RESOLUTION_UNKNOWN, getExceptionInterceptor());
                        } else {
                            throw sqlEx;
                        }
                    } catch (Throwable th2) {
                        th = th2;
                        this2.needsPing = getReconnectAtTxEnd();
                        throw th;
                    }
                } catch (Throwable th3) {
                    th = th3;
                    this = this;
                    this2.needsPing = getReconnectAtTxEnd();
                    throw th;
                }
            } catch (Throwable th4) {
                th = th4;
                throw th;
            }
        }
    }

    private void rollbackNoChecks() throws SQLException {
        if (!getUseLocalTransactionState() || !versionMeetsMinimum(5, 0, 0) || this.io.inTransactionOnServer()) {
            execSQL(null, "rollback", -1, null, 1003, 1007, false, this.database, null, false);
        }
    }

    public PreparedStatement serverPrepareStatement(String sql) throws SQLException {
        return ServerPreparedStatement.getInstance(getMultiHostSafeProxy(), getProcessEscapeCodesForPrepStmts() ? nativeSQL(sql) : sql, getCatalog(), 1003, 1007);
    }

    public PreparedStatement serverPrepareStatement(String sql, int autoGenKeyIndex) throws SQLException {
        PreparedStatement pStmt = ServerPreparedStatement.getInstance(getMultiHostSafeProxy(), getProcessEscapeCodesForPrepStmts() ? nativeSQL(sql) : sql, getCatalog(), 1003, 1007);
        boolean z = true;
        if (autoGenKeyIndex != 1) {
            z = false;
        }
        pStmt.setRetrieveGeneratedKeys(z);
        return pStmt;
    }

    public PreparedStatement serverPrepareStatement(String sql, int resultSetType, int resultSetConcurrency) throws SQLException {
        return ServerPreparedStatement.getInstance(getMultiHostSafeProxy(), getProcessEscapeCodesForPrepStmts() ? nativeSQL(sql) : sql, getCatalog(), resultSetType, resultSetConcurrency);
    }

    public PreparedStatement serverPrepareStatement(String sql, int resultSetType, int resultSetConcurrency, int resultSetHoldability) throws SQLException {
        if (!getPedantic() || resultSetHoldability == 1) {
            return serverPrepareStatement(sql, resultSetType, resultSetConcurrency);
        }
        throw SQLError.createSQLException("HOLD_CUSRORS_OVER_COMMIT is only supported holdability level", SQLError.SQL_STATE_ILLEGAL_ARGUMENT, getExceptionInterceptor());
    }

    public PreparedStatement serverPrepareStatement(String sql, int[] autoGenKeyIndexes) throws SQLException {
        PreparedStatement pStmt = (PreparedStatement) serverPrepareStatement(sql);
        boolean z = autoGenKeyIndexes != null && autoGenKeyIndexes.length > 0;
        pStmt.setRetrieveGeneratedKeys(z);
        return pStmt;
    }

    public PreparedStatement serverPrepareStatement(String sql, String[] autoGenKeyColNames) throws SQLException {
        PreparedStatement pStmt = (PreparedStatement) serverPrepareStatement(sql);
        boolean z = autoGenKeyColNames != null && autoGenKeyColNames.length > 0;
        pStmt.setRetrieveGeneratedKeys(z);
        return pStmt;
    }

    public boolean serverSupportsConvertFn() throws SQLException {
        return versionMeetsMinimum(4, 0, 2);
    }

    /* JADX WARNING: inconsistent code. */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    public void setAutoCommit(final boolean r15) throws java.sql.SQLException {
        /*
        r14 = this;
        r0 = r14.getConnectionMutex();
        monitor-enter(r0);
        r14.checkClosed();	 Catch:{ all -> 0x009e }
        r1 = r14.connectionLifecycleInterceptors;	 Catch:{ all -> 0x009e }
        if (r1 == 0) goto L_0x0022;
    L_0x000c:
        r1 = new com.mysql.jdbc.ConnectionImpl$7;	 Catch:{ all -> 0x009e }
        r2 = r14.connectionLifecycleInterceptors;	 Catch:{ all -> 0x009e }
        r2 = r2.iterator();	 Catch:{ all -> 0x009e }
        r1.<init>(r2, r15);	 Catch:{ all -> 0x009e }
        r1.doForAll();	 Catch:{ all -> 0x009e }
        r2 = r1.fullIteration();	 Catch:{ all -> 0x009e }
        if (r2 != 0) goto L_0x0022;
    L_0x0020:
        monitor-exit(r0);	 Catch:{ all -> 0x009e }
        return;
    L_0x0022:
        r1 = r14.getAutoReconnectForPools();	 Catch:{ all -> 0x009e }
        if (r1 == 0) goto L_0x002c;
    L_0x0028:
        r1 = 1;
        r14.setHighAvailability(r1);	 Catch:{ all -> 0x009e }
    L_0x002c:
        r1 = 0;
        r2 = r14.transactionsSupported;	 Catch:{ all -> 0x008f }
        if (r2 == 0) goto L_0x006a;
    L_0x0031:
        r2 = 1;
        r3 = r14.getUseLocalSessionState();	 Catch:{ all -> 0x008f }
        if (r3 == 0) goto L_0x003e;
    L_0x0038:
        r3 = r14.autoCommit;	 Catch:{ all -> 0x008f }
        if (r3 != r15) goto L_0x003e;
    L_0x003c:
        r2 = 0;
        goto L_0x004d;
    L_0x003e:
        r3 = r14.getHighAvailability();	 Catch:{ all -> 0x008f }
        if (r3 != 0) goto L_0x004d;
    L_0x0044:
        r3 = r14.getIO();	 Catch:{ all -> 0x008f }
        r3 = r3.isSetNeededForAutoCommitMode(r15);	 Catch:{ all -> 0x008f }
        r2 = r3;
    L_0x004d:
        r14.autoCommit = r15;	 Catch:{ all -> 0x008f }
        if (r2 == 0) goto L_0x0069;
    L_0x0051:
        r4 = 0;
        if (r15 == 0) goto L_0x0057;
    L_0x0054:
        r3 = "SET autocommit=1";
        goto L_0x0059;
    L_0x0057:
        r3 = "SET autocommit=0";
    L_0x0059:
        r5 = r3;
        r6 = -1;
        r7 = 0;
        r8 = 1003; // 0x3eb float:1.406E-42 double:4.955E-321;
        r9 = 1007; // 0x3ef float:1.411E-42 double:4.975E-321;
        r10 = 0;
        r11 = r14.database;	 Catch:{ all -> 0x008f }
        r12 = 0;
        r13 = 0;
        r3 = r14;
        r3.execSQL(r4, r5, r6, r7, r8, r9, r10, r11, r12, r13);	 Catch:{ all -> 0x008f }
    L_0x0069:
        goto L_0x0081;
    L_0x006a:
        if (r15 != 0) goto L_0x007f;
    L_0x006c:
        r2 = r14.getRelaxAutoCommit();	 Catch:{ all -> 0x008f }
        if (r2 != 0) goto L_0x007f;
    L_0x0072:
        r2 = "MySQL Versions Older than 3.23.15 do not support transactions";
        r3 = "08003";
        r4 = r14.getExceptionInterceptor();	 Catch:{ all -> 0x008f }
        r2 = com.mysql.jdbc.SQLError.createSQLException(r2, r3, r4);	 Catch:{ all -> 0x008f }
        throw r2;	 Catch:{ all -> 0x008f }
    L_0x007f:
        r14.autoCommit = r15;	 Catch:{ all -> 0x008f }
        r2 = r14;
        r3 = r2.getAutoReconnectForPools();	 Catch:{ all -> 0x00a2 }
        if (r3 == 0) goto L_0x008c;
    L_0x0089:
        r2.setHighAvailability(r1);	 Catch:{ all -> 0x00a2 }
        monitor-exit(r0);	 Catch:{ all -> 0x00a2 }
        return;
    L_0x008f:
        r2 = move-exception;
        r3 = r14;
        r4 = r3.getAutoReconnectForPools();	 Catch:{ all -> 0x009b }
        if (r4 == 0) goto L_0x009a;
    L_0x0097:
        r3.setHighAvailability(r1);	 Catch:{ all -> 0x009b }
    L_0x009a:
        throw r2;	 Catch:{ all -> 0x009b }
    L_0x009b:
        r1 = move-exception;
        r2 = r3;
        goto L_0x00a0;
    L_0x009e:
        r1 = move-exception;
        r2 = r14;
    L_0x00a0:
        monitor-exit(r0);	 Catch:{ all -> 0x00a2 }
        throw r1;
    L_0x00a2:
        r1 = move-exception;
        goto L_0x00a0;
        */
        throw new UnsupportedOperationException("Method not decompiled: com.mysql.jdbc.ConnectionImpl.setAutoCommit(boolean):void");
    }

    public void setCatalog(String catalog) throws SQLException {
        ConnectionImpl connectionImpl = this;
        final String str = catalog;
        synchronized (getConnectionMutex()) {
            try {
                checkClosed();
                if (str == null) {
                    throw SQLError.createSQLException("Catalog can not be null", SQLError.SQL_STATE_ILLEGAL_ARGUMENT, getExceptionInterceptor());
                }
                if (connectionImpl.connectionLifecycleInterceptors != null) {
                    IterateBlock<Extension> iter = new IterateBlock<Extension>(connectionImpl.connectionLifecycleInterceptors.iterator()) {
                        void forEach(Extension each) throws SQLException {
                            if (!((ConnectionLifecycleInterceptor) each).setCatalog(str)) {
                                this.stopIterating = true;
                            }
                        }
                    };
                    iter.doForAll();
                    if (!iter.fullIteration()) {
                        return;
                    }
                }
                if (getUseLocalSessionState()) {
                    if (connectionImpl.lowerCaseTableNames) {
                        if (connectionImpl.database.equalsIgnoreCase(str)) {
                            return;
                        }
                    } else if (connectionImpl.database.equals(str)) {
                        return;
                    }
                }
                String quotedId = connectionImpl.dbmd.getIdentifierQuoteString();
                if (quotedId == null || quotedId.equals(" ")) {
                    quotedId = "";
                }
                String quotedId2 = quotedId;
                StringBuilder query = new StringBuilder("USE ");
                query.append(StringUtils.quoteIdentifier(str, quotedId2, getPedantic()));
                execSQL(null, query.toString(), -1, null, 1003, 1007, false, connectionImpl.database, null, null);
                connectionImpl.database = str;
            } catch (Throwable th) {
                Throwable th2 = th;
            }
        }
    }

    public void setFailedOver(boolean flag) {
    }

    public void setHoldability(int arg0) throws SQLException {
    }

    public void setInGlobalTx(boolean flag) {
        this.isInGlobalTx = flag;
    }

    @Deprecated
    public void setPreferSlaveDuringFailover(boolean flag) {
    }

    public void setReadInfoMsgEnabled(boolean flag) {
        this.readInfoMsg = flag;
    }

    public void setReadOnly(boolean readOnlyFlag) throws SQLException {
        checkClosed();
        setReadOnlyInternal(readOnlyFlag);
    }

    public void setReadOnlyInternal(boolean readOnlyFlag) throws SQLException {
        if (getReadOnlyPropagatesToServer() && versionMeetsMinimum(5, 6, 5) && !(getUseLocalSessionState() && readOnlyFlag == this.readOnly)) {
            StringBuilder stringBuilder = new StringBuilder();
            stringBuilder.append("set session transaction ");
            stringBuilder.append(readOnlyFlag ? "read only" : "read write");
            execSQL(null, stringBuilder.toString(), -1, null, 1003, 1007, false, this.database, null, false);
        }
        this.readOnly = readOnlyFlag;
    }

    public Savepoint setSavepoint() throws SQLException {
        MysqlSavepoint savepoint = new MysqlSavepoint(getExceptionInterceptor());
        setSavepoint(savepoint);
        return savepoint;
    }

    private void setSavepoint(MysqlSavepoint savepoint) throws SQLException {
        Throwable th;
        synchronized (getConnectionMutex()) {
            try {
                if (!versionMeetsMinimum(4, 0, 14)) {
                    if (!versionMeetsMinimum(4, 1, 1)) {
                        throw SQLError.createSQLFeatureNotSupportedException();
                    }
                }
                checkClosed();
                StringBuilder savePointQuery = new StringBuilder("SAVEPOINT ");
                savePointQuery.append('`');
                savePointQuery.append(savepoint.getSavepointName());
                savePointQuery.append('`');
                Statement stmt = null;
                try {
                    stmt = getMetadataSafeStatement();
                    stmt.executeUpdate(savePointQuery.toString());
                    closeStatement(stmt);
                } catch (Throwable th2) {
                    th = th2;
                    throw th;
                }
            } catch (Throwable th3) {
                th = th3;
                ConnectionImpl connectionImpl = this;
                throw th;
            }
        }
    }

    public Savepoint setSavepoint(String name) throws SQLException {
        MysqlSavepoint savepoint;
        synchronized (getConnectionMutex()) {
            savepoint = new MysqlSavepoint(name, getExceptionInterceptor());
            setSavepoint(savepoint);
        }
        return savepoint;
    }

    private void setSessionVariables() throws SQLException {
        if (versionMeetsMinimum(4, 0, 0) && getSessionVariables() != null) {
            String str;
            List<String> variablesToSet = new ArrayList();
            for (String str2 : StringUtils.split(getSessionVariables(), ",", "\"'(", "\"')", "\"'", true)) {
                variablesToSet.addAll(StringUtils.split(str2, ";", "\"'(", "\"')", "\"'", true));
            }
            if (!variablesToSet.isEmpty()) {
                Statement stmt = null;
                try {
                    stmt = getMetadataSafeStatement();
                    StringBuilder query = new StringBuilder("SET ");
                    str2 = "";
                    for (String variableToSet : variablesToSet) {
                        if (variableToSet.length() > 0) {
                            query.append(str2);
                            if (!variableToSet.startsWith("@")) {
                                query.append("SESSION ");
                            }
                            query.append(variableToSet);
                            str2 = ",";
                        }
                    }
                    stmt.executeUpdate(query.toString());
                    return;
                } finally {
                    if (stmt != null) {
                        stmt.close();
                    }
                }
            }
        }
        ConnectionImpl connectionImpl = this;
    }

    public void setTransactionIsolation(int level) throws SQLException {
        synchronized (getConnectionMutex()) {
            checkClosed();
            if (this.hasIsolationLevels) {
                boolean shouldSendSet = false;
                if (getAlwaysSendSetIsolation()) {
                    shouldSendSet = true;
                } else if (level != this.isolationLevel) {
                    shouldSendSet = true;
                }
                if (getUseLocalSessionState()) {
                    shouldSendSet = this.isolationLevel != level;
                }
                if (shouldSendSet) {
                    String sql;
                    if (level == 4) {
                        sql = "SET SESSION TRANSACTION ISOLATION LEVEL REPEATABLE READ";
                    } else if (level != 8) {
                        switch (level) {
                            case 0:
                                throw SQLError.createSQLException("Transaction isolation level NONE not supported by MySQL", getExceptionInterceptor());
                            case 1:
                                sql = "SET SESSION TRANSACTION ISOLATION LEVEL READ UNCOMMITTED";
                                break;
                            case 2:
                                sql = "SET SESSION TRANSACTION ISOLATION LEVEL READ COMMITTED";
                                break;
                            default:
                                StringBuilder stringBuilder = new StringBuilder();
                                stringBuilder.append("Unsupported transaction isolation level '");
                                stringBuilder.append(level);
                                stringBuilder.append("'");
                                throw SQLError.createSQLException(stringBuilder.toString(), SQLError.SQL_STATE_DRIVER_NOT_CAPABLE, getExceptionInterceptor());
                        }
                    } else {
                        sql = "SET SESSION TRANSACTION ISOLATION LEVEL SERIALIZABLE";
                    }
                    execSQL(null, sql, -1, null, 1003, 1007, false, this.database, null, false);
                    this.isolationLevel = level;
                }
            } else {
                throw SQLError.createSQLException("Transaction Isolation Levels are not supported on MySQL versions older than 3.23.36.", SQLError.SQL_STATE_DRIVER_NOT_CAPABLE, getExceptionInterceptor());
            }
        }
    }

    public void setTypeMap(Map<String, Class<?>> map) throws SQLException {
        synchronized (getConnectionMutex()) {
            this.typeMap = map;
        }
    }

    private void setupServerForTruncationChecks() throws SQLException {
        ConnectionImpl connectionImpl = this;
        if (getJdbcCompliantTruncation() && versionMeetsMinimum(5, 0, 2)) {
            String currentSqlMode = (String) connectionImpl.serverVariables.get("sql_mode");
            boolean strictTransTablesIsSet = StringUtils.indexOfIgnoreCase(currentSqlMode, "STRICT_TRANS_TABLES") != -1;
            if (!(currentSqlMode == null || currentSqlMode.length() == 0)) {
                if (strictTransTablesIsSet) {
                    if (strictTransTablesIsSet) {
                        setJdbcCompliantTruncation(false);
                        return;
                    }
                    return;
                }
            }
            StringBuilder commandBuf = new StringBuilder("SET sql_mode='");
            if (currentSqlMode != null && currentSqlMode.length() > 0) {
                commandBuf.append(currentSqlMode);
                commandBuf.append(",");
            }
            commandBuf.append("STRICT_TRANS_TABLES'");
            execSQL(null, commandBuf.toString(), -1, null, 1003, 1007, false, connectionImpl.database, null, false);
            setJdbcCompliantTruncation(false);
        }
    }

    public void shutdownServer() throws SQLException {
        try {
            if (versionMeetsMinimum(5, 7, 9)) {
                execSQL(null, "SHUTDOWN", -1, null, 1003, 1007, false, this.database, null, false);
            } else {
                this.io.sendCommand(8, null, null, false, null, 0);
            }
        } catch (Exception ex) {
            SQLException sqlEx = SQLError.createSQLException(Messages.getString("Connection.UnhandledExceptionDuringShutdown"), SQLError.SQL_STATE_GENERAL_ERROR, getExceptionInterceptor());
            sqlEx.initCause(ex);
            throw sqlEx;
        }
    }

    public boolean supportsIsolationLevel() {
        return this.hasIsolationLevels;
    }

    public boolean supportsQuotedIdentifiers() {
        return this.hasQuotedIdentifiers;
    }

    public boolean supportsTransactions() {
        return this.transactionsSupported;
    }

    public void unregisterStatement(Statement stmt) {
        this.openStatements.remove(stmt);
    }

    public boolean useAnsiQuotedIdentifiers() {
        boolean z;
        synchronized (getConnectionMutex()) {
            z = this.useAnsiQuotes;
        }
        return z;
    }

    public boolean versionMeetsMinimum(int major, int minor, int subminor) throws SQLException {
        checkClosed();
        return this.io.versionMeetsMinimum(major, minor, subminor);
    }

    public CachedResultSetMetaData getCachedMetaData(String sql) {
        if (this.resultSetMetadataCache == null) {
            return null;
        }
        CachedResultSetMetaData cachedResultSetMetaData;
        synchronized (this.resultSetMetadataCache) {
            cachedResultSetMetaData = (CachedResultSetMetaData) this.resultSetMetadataCache.get(sql);
        }
        return cachedResultSetMetaData;
    }

    public void initializeResultsMetadataFromCache(String sql, CachedResultSetMetaData cachedMetaData, ResultSetInternalMethods resultSet) throws SQLException {
        if (cachedMetaData == null) {
            cachedMetaData = new CachedResultSetMetaData();
            resultSet.buildIndexMapping();
            resultSet.initializeWithMetadata();
            if (resultSet instanceof UpdatableResultSet) {
                ((UpdatableResultSet) resultSet).checkUpdatability();
            }
            resultSet.populateCachedMetaData(cachedMetaData);
            this.resultSetMetadataCache.put(sql, cachedMetaData);
            return;
        }
        resultSet.initializeFromCachedMetaData(cachedMetaData);
        resultSet.initializeWithMetadata();
        if (resultSet instanceof UpdatableResultSet) {
            ((UpdatableResultSet) resultSet).checkUpdatability();
        }
    }

    public String getStatementComment() {
        return this.statementComment;
    }

    public void setStatementComment(String comment) {
        this.statementComment = comment;
    }

    public void reportQueryTime(long millisOrNanos) {
        synchronized (getConnectionMutex()) {
            this.queryTimeCount++;
            this.queryTimeSum += (double) millisOrNanos;
            this.queryTimeSumSquares += (double) (millisOrNanos * millisOrNanos);
            this.queryTimeMean = ((this.queryTimeMean * ((double) (this.queryTimeCount - 1))) + ((double) millisOrNanos)) / ((double) this.queryTimeCount);
        }
    }

    /* JADX WARNING: inconsistent code. */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    public boolean isAbonormallyLongQuery(long r11) {
        /*
        r10 = this;
        r0 = r10.getConnectionMutex();
        monitor-enter(r0);
        r1 = r10.queryTimeCount;	 Catch:{ all -> 0x0037 }
        r3 = 15;
        r5 = (r1 > r3 ? 1 : (r1 == r3 ? 0 : -1));
        r1 = 0;
        if (r5 >= 0) goto L_0x0010;
    L_0x000e:
        monitor-exit(r0);	 Catch:{ all -> 0x0037 }
        return r1;
    L_0x0010:
        r2 = r10.queryTimeSumSquares;	 Catch:{ all -> 0x0037 }
        r4 = r10.queryTimeSum;	 Catch:{ all -> 0x0037 }
        r6 = r10.queryTimeSum;	 Catch:{ all -> 0x0037 }
        r4 = r4 * r6;
        r6 = r10.queryTimeCount;	 Catch:{ all -> 0x0037 }
        r6 = (double) r6;	 Catch:{ all -> 0x0037 }
        r4 = r4 / r6;
        r2 = r2 - r4;
        r4 = r10.queryTimeCount;	 Catch:{ all -> 0x0037 }
        r6 = 1;
        r8 = r4 - r6;
        r4 = (double) r8;	 Catch:{ all -> 0x0037 }
        r2 = r2 / r4;
        r2 = java.lang.Math.sqrt(r2);	 Catch:{ all -> 0x0037 }
        r4 = (double) r11;	 Catch:{ all -> 0x0037 }
        r6 = r10.queryTimeMean;	 Catch:{ all -> 0x0037 }
        r8 = 4617315517961601024; // 0x4014000000000000 float:0.0 double:5.0;
        r8 = r8 * r2;
        r6 = r6 + r8;
        r8 = (r4 > r6 ? 1 : (r4 == r6 ? 0 : -1));
        if (r8 <= 0) goto L_0x0035;
    L_0x0033:
        r1 = 1;
    L_0x0035:
        monitor-exit(r0);	 Catch:{ all -> 0x0037 }
        return r1;
    L_0x0037:
        r1 = move-exception;
        monitor-exit(r0);	 Catch:{ all -> 0x0037 }
        throw r1;
        */
        throw new UnsupportedOperationException("Method not decompiled: com.mysql.jdbc.ConnectionImpl.isAbonormallyLongQuery(long):boolean");
    }

    public void initializeExtension(Extension ex) throws SQLException {
        ex.init(this, this.props);
    }

    public void transactionBegun() throws SQLException {
        synchronized (getConnectionMutex()) {
            if (this.connectionLifecycleInterceptors != null) {
                new IterateBlock<Extension>(this.connectionLifecycleInterceptors.iterator()) {
                    void forEach(Extension each) throws SQLException {
                        ((ConnectionLifecycleInterceptor) each).transactionBegun();
                    }
                }.doForAll();
            }
        }
    }

    public void transactionCompleted() throws SQLException {
        synchronized (getConnectionMutex()) {
            if (this.connectionLifecycleInterceptors != null) {
                new IterateBlock<Extension>(this.connectionLifecycleInterceptors.iterator()) {
                    void forEach(Extension each) throws SQLException {
                        ((ConnectionLifecycleInterceptor) each).transactionCompleted();
                    }
                }.doForAll();
            }
        }
    }

    public boolean storesLowerCaseTableName() {
        return this.storesLowerCaseTableName;
    }

    public ExceptionInterceptor getExceptionInterceptor() {
        return this.exceptionInterceptor;
    }

    public boolean getRequiresEscapingEncoder() {
        return this.requiresEscapingEncoder;
    }

    public boolean isServerLocal() throws SQLException {
        synchronized (getConnectionMutex()) {
            SocketFactory factory = getIO().socketFactory;
            if (factory instanceof SocketMetadata) {
                boolean isLocallyConnected = ((SocketMetadata) factory).isLocallyConnected(this);
                return isLocallyConnected;
            }
            getLog().logWarn(Messages.getString("Connection.NoMetadataOnSocketFactory"));
            return false;
        }
    }

    public int getSessionMaxRows() {
        int i;
        synchronized (getConnectionMutex()) {
            i = this.sessionMaxRows;
        }
        return i;
    }

    public void setSessionMaxRows(int max) throws SQLException {
        synchronized (getConnectionMutex()) {
            if (this.sessionMaxRows != max) {
                this.sessionMaxRows = max;
                StringBuilder stringBuilder = new StringBuilder();
                stringBuilder.append("SET SQL_SELECT_LIMIT=");
                stringBuilder.append(this.sessionMaxRows == -1 ? "DEFAULT" : Integer.valueOf(this.sessionMaxRows));
                execSQL(null, stringBuilder.toString(), -1, null, 1003, 1007, false, this.database, null, false);
            }
        }
    }

    public void setSchema(String schema) throws SQLException {
        synchronized (getConnectionMutex()) {
            checkClosed();
        }
    }

    public String getSchema() throws SQLException {
        synchronized (getConnectionMutex()) {
            checkClosed();
        }
        return null;
    }

    public void abort(Executor executor) throws SQLException {
        SecurityManager sec = System.getSecurityManager();
        if (sec != null) {
            sec.checkPermission(ABORT_PERM);
        }
        if (executor == null) {
            throw SQLError.createSQLException("Executor can not be null", SQLError.SQL_STATE_ILLEGAL_ARGUMENT, getExceptionInterceptor());
        }
        executor.execute(new Runnable() {
            public void run() {
                try {
                    ConnectionImpl.this.abortInternal();
                } catch (SQLException e) {
                    throw new RuntimeException(e);
                }
            }
        });
    }

    public void setNetworkTimeout(Executor executor, int milliseconds) throws SQLException {
        synchronized (getConnectionMutex()) {
            SecurityManager sec = System.getSecurityManager();
            if (sec != null) {
                sec.checkPermission(SET_NETWORK_TIMEOUT_PERM);
            }
            if (executor == null) {
                throw SQLError.createSQLException("Executor can not be null", SQLError.SQL_STATE_ILLEGAL_ARGUMENT, getExceptionInterceptor());
            }
            checkClosed();
            executor.execute(new NetworkTimeoutSetter(this, this.io, milliseconds));
        }
    }

    public int getNetworkTimeout() throws SQLException {
        int socketTimeout;
        synchronized (getConnectionMutex()) {
            checkClosed();
            socketTimeout = getSocketTimeout();
        }
        return socketTimeout;
    }

    public ProfilerEventHandler getProfilerEventHandlerInstance() {
        return this.eventSink;
    }

    public void setProfilerEventHandlerInstance(ProfilerEventHandler h) {
        this.eventSink = h;
    }
}
