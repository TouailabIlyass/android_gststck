package com.mysql.jdbc;

import com.mysql.jdbc.exceptions.MySQLStatementCancelledException;
import com.mysql.jdbc.exceptions.MySQLTimeoutException;
import com.mysql.jdbc.log.LogUtils;
import com.mysql.jdbc.profiler.ProfilerEvent;
import com.mysql.jdbc.profiler.ProfilerEventHandler;
import java.io.InputStream;
import java.lang.ref.Reference;
import java.lang.ref.WeakReference;
import java.math.BigInteger;
import java.sql.BatchUpdateException;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.SQLWarning;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Enumeration;
import java.util.GregorianCalendar;
import java.util.HashSet;
import java.util.List;
import java.util.Properties;
import java.util.Set;
import java.util.TimerTask;
import java.util.concurrent.atomic.AtomicBoolean;

public class StatementImpl implements Statement {
    protected static final String[] ON_DUPLICATE_KEY_UPDATE_CLAUSE = new String[]{"ON", "DUPLICATE", "KEY", "UPDATE"};
    protected static final String PING_MARKER = "/* ping */";
    public static final byte USES_VARIABLES_FALSE = (byte) 0;
    public static final byte USES_VARIABLES_TRUE = (byte) 1;
    public static final byte USES_VARIABLES_UNKNOWN = (byte) -1;
    static int statementCounter = 1;
    protected List<Object> batchedArgs;
    protected ArrayList<ResultSetRow> batchedGeneratedKeys;
    protected Object cancelTimeoutMutex = new Object();
    protected SingleByteCharsetConverter charConverter = null;
    protected String charEncoding = null;
    protected boolean clearWarningsCalled;
    private boolean closeOnCompletion;
    protected volatile MySQLConnection connection = null;
    protected long connectionId = 0;
    protected boolean continueBatchOnError;
    protected String currentCatalog = null;
    protected boolean doEscapeProcessing;
    protected ProfilerEventHandler eventSink;
    private ExceptionInterceptor exceptionInterceptor;
    private int fetchSize;
    protected ResultSetInternalMethods generatedKeysResults;
    protected boolean holdResultsOpenOverClose;
    protected boolean isClosed;
    private boolean isImplicitlyClosingResults;
    private boolean isPoolable;
    protected long lastInsertId;
    protected boolean lastQueryIsOnDupKeyUpdate;
    private InputStream localInfileInputStream;
    protected int maxFieldSize;
    protected int maxRows;
    protected Set<ResultSetInternalMethods> openResults;
    private int originalFetchSize;
    private int originalResultSetType;
    protected boolean pedantic;
    protected Reference<MySQLConnection> physicalConnection = null;
    protected PingTarget pingTarget;
    protected String pointOfOrigin;
    protected boolean profileSQL;
    protected int resultSetConcurrency;
    protected int resultSetType;
    protected ResultSetInternalMethods results;
    protected boolean retrieveGeneratedKeys;
    protected boolean sendFractionalSeconds;
    protected final AtomicBoolean statementExecuting;
    protected int statementId;
    protected int timeoutInMillis;
    protected long updateCount;
    protected boolean useLegacyDatetimeCode;
    protected boolean useUsageAdvisor;
    protected final boolean version5013OrNewer;
    protected SQLWarning warningChain;
    protected boolean wasCancelled = false;
    protected boolean wasCancelledByTimeout = false;

    class CancelTask extends TimerTask {
        SQLException caughtWhileCancelling = null;
        long origConnId = 0;
        Properties origConnProps = null;
        String origConnURL = "";
        StatementImpl toCancel;

        /* renamed from: com.mysql.jdbc.StatementImpl$CancelTask$1 */
        class C03401 extends Thread {
            C03401() {
            }

            public void run() {
                C03401 this;
                Connection cancelConn = null;
                Statement cancelStmt = null;
                try {
                    MySQLConnection physicalConn = (MySQLConnection) StatementImpl.this.physicalConnection.get();
                    if (physicalConn != null) {
                        if (physicalConn.getQueryTimeoutKillsConnection()) {
                            CancelTask.this.toCancel.wasCancelled = true;
                            CancelTask.this.toCancel.wasCancelledByTimeout = true;
                            physicalConn.realClose(false, false, true, new MySQLStatementCancelledException(Messages.getString("Statement.ConnectionKilledDueToTimeout")));
                        } else {
                            synchronized (StatementImpl.this.cancelTimeoutMutex) {
                                StringBuilder stringBuilder;
                                if (CancelTask.this.origConnURL.equals(physicalConn.getURL())) {
                                    cancelConn = physicalConn.duplicate();
                                    cancelStmt = cancelConn.createStatement();
                                    stringBuilder = new StringBuilder();
                                    stringBuilder.append("KILL QUERY ");
                                    stringBuilder.append(physicalConn.getId());
                                    cancelStmt.execute(stringBuilder.toString());
                                } else {
                                    try {
                                        cancelConn = (Connection) DriverManager.getConnection(CancelTask.this.origConnURL, CancelTask.this.origConnProps);
                                        cancelStmt = cancelConn.createStatement();
                                        stringBuilder = new StringBuilder();
                                        stringBuilder.append("KILL QUERY ");
                                        stringBuilder.append(CancelTask.this.origConnId);
                                        cancelStmt.execute(stringBuilder.toString());
                                    } catch (NullPointerException e) {
                                    }
                                }
                                CancelTask.this.toCancel.wasCancelled = true;
                                CancelTask.this.toCancel.wasCancelledByTimeout = true;
                            }
                        }
                    }
                    this = this;
                    if (cancelStmt != null) {
                        try {
                            cancelStmt.close();
                        } catch (SQLException sqlEx) {
                            throw new RuntimeException(sqlEx.toString());
                        }
                    }
                    if (cancelConn != null) {
                        try {
                            cancelConn.close();
                        } catch (SQLException sqlEx2) {
                            throw new RuntimeException(sqlEx2.toString());
                        }
                    }
                } catch (SQLException sqlEx3) {
                    try {
                        CancelTask.this.caughtWhileCancelling = sqlEx3;
                        this = this;
                        if (cancelStmt != null) {
                            try {
                                cancelStmt.close();
                            } catch (SQLException sqlEx22) {
                                throw new RuntimeException(sqlEx22.toString());
                            }
                        }
                        if (cancelConn != null) {
                            try {
                                cancelConn.close();
                            } catch (SQLException sqlEx222) {
                                throw new RuntimeException(sqlEx222.toString());
                            }
                        }
                    } catch (Throwable th) {
                        C03401 this2 = this;
                        if (cancelStmt != null) {
                            try {
                                cancelStmt.close();
                            } catch (SQLException sqlEx2222) {
                                throw new RuntimeException(sqlEx2222.toString());
                            }
                        }
                        if (cancelConn != null) {
                            try {
                                cancelConn.close();
                            } catch (SQLException sqlEx22222) {
                                throw new RuntimeException(sqlEx22222.toString());
                            }
                        }
                        CancelTask.this.toCancel = null;
                        CancelTask.this.origConnProps = null;
                        CancelTask.this.origConnURL = null;
                    }
                } catch (NullPointerException e2) {
                    this = this;
                    if (cancelStmt != null) {
                        try {
                            cancelStmt.close();
                        } catch (SQLException sqlEx222222) {
                            throw new RuntimeException(sqlEx222222.toString());
                        }
                    }
                    if (cancelConn != null) {
                        try {
                            cancelConn.close();
                        } catch (SQLException sqlEx2222222) {
                            throw new RuntimeException(sqlEx2222222.toString());
                        }
                    }
                }
                CancelTask.this.toCancel = null;
                CancelTask.this.origConnProps = null;
                CancelTask.this.origConnURL = null;
            }
        }

        CancelTask(StatementImpl cancellee) throws SQLException {
            this.toCancel = cancellee;
            this.origConnProps = new Properties();
            Properties props = StatementImpl.this.connection.getProperties();
            Enumeration<?> keys = props.propertyNames();
            while (keys.hasMoreElements()) {
                String key = keys.nextElement().toString();
                this.origConnProps.setProperty(key, props.getProperty(key));
            }
            this.origConnURL = StatementImpl.this.connection.getURL();
            this.origConnId = StatementImpl.this.connection.getId();
        }

        public void run() {
            new C03401().start();
        }
    }

    public StatementImpl(MySQLConnection c, String catalog) throws SQLException {
        boolean profiling = true;
        this.doEscapeProcessing = true;
        this.eventSink = null;
        this.fetchSize = 0;
        this.isClosed = false;
        this.lastInsertId = -1;
        this.maxFieldSize = MysqlIO.getMaxBuf();
        this.maxRows = -1;
        this.openResults = new HashSet();
        this.pedantic = false;
        this.profileSQL = false;
        this.results = null;
        this.generatedKeysResults = null;
        this.resultSetConcurrency = 0;
        this.resultSetType = 0;
        this.timeoutInMillis = 0;
        this.updateCount = -1;
        this.useUsageAdvisor = false;
        this.warningChain = null;
        this.clearWarningsCalled = false;
        this.holdResultsOpenOverClose = false;
        this.batchedGeneratedKeys = null;
        this.retrieveGeneratedKeys = false;
        this.continueBatchOnError = false;
        this.pingTarget = null;
        this.lastQueryIsOnDupKeyUpdate = false;
        this.statementExecuting = new AtomicBoolean(false);
        this.isImplicitlyClosingResults = false;
        this.originalResultSetType = 0;
        this.originalFetchSize = 0;
        this.isPoolable = true;
        this.closeOnCompletion = false;
        if (c != null) {
            if (!c.isClosed()) {
                int i;
                this.connection = c;
                this.connectionId = this.connection.getId();
                this.exceptionInterceptor = this.connection.getExceptionInterceptor();
                this.currentCatalog = catalog;
                this.pedantic = this.connection.getPedantic();
                this.continueBatchOnError = this.connection.getContinueBatchOnError();
                this.useLegacyDatetimeCode = this.connection.getUseLegacyDatetimeCode();
                this.sendFractionalSeconds = this.connection.getSendFractionalSeconds();
                this.doEscapeProcessing = this.connection.getEnableEscapeProcessing();
                if (!this.connection.getDontTrackOpenResources()) {
                    this.connection.registerStatement(this);
                }
                this.maxFieldSize = this.connection.getMaxAllowedPacket();
                int defaultFetchSize = this.connection.getDefaultFetchSize();
                if (defaultFetchSize != 0) {
                    setFetchSize(defaultFetchSize);
                }
                if (this.connection.getUseUnicode()) {
                    this.charEncoding = this.connection.getEncoding();
                    this.charConverter = this.connection.getCharsetConverter(this.charEncoding);
                }
                if (!(this.connection.getProfileSql() || this.connection.getUseUsageAdvisor())) {
                    if (!this.connection.getLogSlowQueries()) {
                        profiling = false;
                    }
                }
                if (this.connection.getAutoGenerateTestcaseScript() || profiling) {
                    i = statementCounter;
                    statementCounter = i + 1;
                    this.statementId = i;
                }
                if (profiling) {
                    this.pointOfOrigin = LogUtils.findCallingClassAndMethod(new Throwable());
                    this.profileSQL = this.connection.getProfileSql();
                    this.useUsageAdvisor = this.connection.getUseUsageAdvisor();
                    this.eventSink = ProfilerEventHandlerFactory.getInstance(this.connection);
                }
                i = this.connection.getMaxRows();
                if (i != -1) {
                    setMaxRows(i);
                }
                this.holdResultsOpenOverClose = this.connection.getHoldResultsOpenOverStatementClose();
                this.version5013OrNewer = this.connection.versionMeetsMinimum(5, 0, 13);
                return;
            }
        }
        throw SQLError.createSQLException(Messages.getString("Statement.0"), SQLError.SQL_STATE_CONNECTION_NOT_OPEN, null);
    }

    public void addBatch(String sql) throws SQLException {
        synchronized (checkClosed().getConnectionMutex()) {
            if (this.batchedArgs == null) {
                this.batchedArgs = new ArrayList();
            }
            if (sql != null) {
                this.batchedArgs.add(sql);
            }
        }
    }

    public List<Object> getBatchedArgs() {
        return this.batchedArgs == null ? null : Collections.unmodifiableList(this.batchedArgs);
    }

    public void cancel() throws SQLException {
        if (this.statementExecuting.get() && !this.isClosed && this.connection != null && this.connection.versionMeetsMinimum(5, 0, 0)) {
            Connection cancelConn = null;
            Statement cancelStmt = null;
            try {
                cancelConn = this.connection.duplicate();
                cancelStmt = cancelConn.createStatement();
                StringBuilder stringBuilder = new StringBuilder();
                stringBuilder.append("KILL QUERY ");
                stringBuilder.append(this.connection.getIO().getThreadId());
                cancelStmt.execute(stringBuilder.toString());
                this.wasCancelled = true;
                if (cancelStmt != null) {
                    cancelStmt.close();
                }
                if (cancelConn != null) {
                    cancelConn.close();
                }
            } catch (Throwable th) {
                if (cancelStmt != null) {
                    cancelStmt.close();
                }
                if (cancelConn != null) {
                    cancelConn.close();
                }
            }
        }
    }

    protected MySQLConnection checkClosed() throws SQLException {
        MySQLConnection c = this.connection;
        if (c != null) {
            return c;
        }
        throw SQLError.createSQLException(Messages.getString("Statement.49"), SQLError.SQL_STATE_ILLEGAL_ARGUMENT, getExceptionInterceptor());
    }

    protected void checkForDml(String sql, char firstStatementChar) throws SQLException {
        if (firstStatementChar == 'I' || firstStatementChar == 'U' || firstStatementChar == 'D' || firstStatementChar == 'A' || firstStatementChar == 'C' || firstStatementChar == 'T' || firstStatementChar == 'R') {
            String noCommentSql = StringUtils.stripComments(sql, "'\"", "'\"", true, false, true, true);
            if (!(StringUtils.startsWithIgnoreCaseAndWs(noCommentSql, "INSERT") || StringUtils.startsWithIgnoreCaseAndWs(noCommentSql, "UPDATE") || StringUtils.startsWithIgnoreCaseAndWs(noCommentSql, "DELETE") || StringUtils.startsWithIgnoreCaseAndWs(noCommentSql, "DROP") || StringUtils.startsWithIgnoreCaseAndWs(noCommentSql, "CREATE") || StringUtils.startsWithIgnoreCaseAndWs(noCommentSql, "ALTER") || StringUtils.startsWithIgnoreCaseAndWs(noCommentSql, "TRUNCATE"))) {
                if (!StringUtils.startsWithIgnoreCaseAndWs(noCommentSql, "RENAME")) {
                    return;
                }
            }
            throw SQLError.createSQLException(Messages.getString("Statement.57"), SQLError.SQL_STATE_ILLEGAL_ARGUMENT, getExceptionInterceptor());
        }
    }

    protected void checkNullOrEmptyQuery(String sql) throws SQLException {
        if (sql == null) {
            throw SQLError.createSQLException(Messages.getString("Statement.59"), SQLError.SQL_STATE_ILLEGAL_ARGUMENT, getExceptionInterceptor());
        } else if (sql.length() == 0) {
            throw SQLError.createSQLException(Messages.getString("Statement.61"), SQLError.SQL_STATE_ILLEGAL_ARGUMENT, getExceptionInterceptor());
        }
    }

    public void clearBatch() throws SQLException {
        synchronized (checkClosed().getConnectionMutex()) {
            if (this.batchedArgs != null) {
                this.batchedArgs.clear();
            }
        }
    }

    public void clearWarnings() throws SQLException {
        synchronized (checkClosed().getConnectionMutex()) {
            this.clearWarningsCalled = true;
            this.warningChain = null;
        }
    }

    public void close() throws SQLException {
        realClose(true, true);
    }

    protected void closeAllOpenResults() throws SQLException {
        MySQLConnection locallyScopedConn = this.connection;
        if (locallyScopedConn != null) {
            synchronized (locallyScopedConn.getConnectionMutex()) {
                if (this.openResults != null) {
                    for (ResultSetInternalMethods element : this.openResults) {
                        try {
                            element.realClose(false);
                        } catch (SQLException sqlEx) {
                            AssertionFailedException.shouldNotHappen(sqlEx);
                        }
                    }
                    this.openResults.clear();
                }
            }
        }
    }

    protected void implicitlyCloseAllOpenResults() throws SQLException {
        this.isImplicitlyClosingResults = true;
        try {
            if (!(this.connection.getHoldResultsOpenOverStatementClose() || this.connection.getDontTrackOpenResources() || this.holdResultsOpenOverClose)) {
                if (this.results != null) {
                    this.results.realClose(false);
                }
                if (this.generatedKeysResults != null) {
                    this.generatedKeysResults.realClose(false);
                }
                closeAllOpenResults();
            }
            this.isImplicitlyClosingResults = false;
        } catch (Throwable th) {
            this.isImplicitlyClosingResults = false;
        }
    }

    public void removeOpenResultSet(ResultSetInternalMethods rs) {
        try {
            synchronized (checkClosed().getConnectionMutex()) {
                if (this.openResults != null) {
                    this.openResults.remove(rs);
                }
                boolean hasMoreResults = rs.getNextResultSet() != null;
                if (this.results == rs && !hasMoreResults) {
                    this.results = null;
                }
                if (this.generatedKeysResults == rs) {
                    this.generatedKeysResults = null;
                }
                if (!(this.isImplicitlyClosingResults || hasMoreResults)) {
                    checkAndPerformCloseOnCompletionAction();
                }
            }
        } catch (SQLException e) {
        }
    }

    public int getOpenResultSetCount() {
        try {
            synchronized (checkClosed().getConnectionMutex()) {
                if (this.openResults != null) {
                    int size = this.openResults.size();
                    return size;
                }
                return 0;
            }
        } catch (SQLException e) {
            return 0;
        }
    }

    private void checkAndPerformCloseOnCompletionAction() {
        try {
            synchronized (checkClosed().getConnectionMutex()) {
                if (isCloseOnCompletion() && !this.connection.getDontTrackOpenResources() && getOpenResultSetCount() == 0 && ((this.results == null || !this.results.reallyResult() || this.results.isClosed()) && (this.generatedKeysResults == null || !this.generatedKeysResults.reallyResult() || this.generatedKeysResults.isClosed()))) {
                    realClose(false, false);
                }
            }
        } catch (SQLException e) {
        }
    }

    private ResultSetInternalMethods createResultSetUsingServerFetch(String sql) throws SQLException {
        ResultSetInternalMethods rs;
        synchronized (checkClosed().getConnectionMutex()) {
            PreparedStatement pStmt = this.connection.prepareStatement(sql, this.resultSetType, this.resultSetConcurrency);
            pStmt.setFetchSize(this.fetchSize);
            if (this.maxRows > -1) {
                pStmt.setMaxRows(this.maxRows);
            }
            statementBegins();
            pStmt.execute();
            rs = ((StatementImpl) pStmt).getResultSetInternal();
            rs.setStatementUsedForFetchingRows((PreparedStatement) pStmt);
            this.results = rs;
        }
        return rs;
    }

    protected boolean createStreamingResultSet() {
        return this.resultSetType == 1003 && this.resultSetConcurrency == 1007 && this.fetchSize == Integer.MIN_VALUE;
    }

    public void enableStreamingResults() throws SQLException {
        synchronized (checkClosed().getConnectionMutex()) {
            this.originalResultSetType = this.resultSetType;
            this.originalFetchSize = this.fetchSize;
            setFetchSize(Integer.MIN_VALUE);
            setResultSetType(1003);
        }
    }

    public void disableStreamingResults() throws SQLException {
        synchronized (checkClosed().getConnectionMutex()) {
            if (this.fetchSize == Integer.MIN_VALUE && this.resultSetType == 1003) {
                setFetchSize(this.originalFetchSize);
                setResultSetType(this.originalResultSetType);
            }
        }
    }

    protected void setupStreamingTimeout(MySQLConnection con) throws SQLException {
        if (createStreamingResultSet() && con.getNetTimeoutForStreamingResults() > 0) {
            StringBuilder stringBuilder = new StringBuilder();
            stringBuilder.append("SET net_write_timeout=");
            stringBuilder.append(con.getNetTimeoutForStreamingResults());
            executeSimpleNonQuery(con, stringBuilder.toString());
        }
    }

    public boolean execute(String sql) throws SQLException {
        return executeInternal(sql, false);
    }

    /* JADX WARNING: inconsistent code. */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    private boolean executeInternal(java.lang.String r28, boolean r29) throws java.sql.SQLException {
        /*
        r27 = this;
        r11 = r27;
        r1 = r28;
        r12 = r29;
        r13 = r27.checkClosed();
        r14 = r13.getConnectionMutex();
        monitor-enter(r14);
        r27.checkClosed();	 Catch:{ all -> 0x02c3 }
        r27.checkNullOrEmptyQuery(r28);	 Catch:{ all -> 0x02c3 }
        r27.resetCancelledState();	 Catch:{ all -> 0x02c3 }
        r27.implicitlyCloseAllOpenResults();	 Catch:{ all -> 0x02c3 }
        r15 = 0;
        r2 = r1.charAt(r15);	 Catch:{ all -> 0x02c3 }
        r3 = 47;
        r10 = 1;
        if (r2 != r3) goto L_0x0032;
    L_0x0025:
        r2 = "/* ping */";
        r2 = r1.startsWith(r2);	 Catch:{ all -> 0x02c3 }
        if (r2 == 0) goto L_0x0032;
    L_0x002d:
        r27.doPingInstead();	 Catch:{ all -> 0x02c3 }
        monitor-exit(r14);	 Catch:{ all -> 0x02c3 }
        return r10;
    L_0x0032:
        r2 = findStartOfStatement(r28);	 Catch:{ all -> 0x02c3 }
        r2 = com.mysql.jdbc.StringUtils.firstAlphaCharUc(r1, r2);	 Catch:{ all -> 0x02c3 }
        r9 = r2;
        r2 = 83;
        if (r9 != r2) goto L_0x0041;
    L_0x003f:
        r2 = r10;
        goto L_0x0042;
    L_0x0041:
        r2 = r15;
    L_0x0042:
        r16 = r2;
        r11.retrieveGeneratedKeys = r12;	 Catch:{ all -> 0x02c3 }
        if (r12 == 0) goto L_0x0054;
    L_0x0048:
        r2 = 73;
        if (r9 != r2) goto L_0x0054;
    L_0x004c:
        r2 = r27.containsOnDuplicateKeyInString(r28);	 Catch:{ all -> 0x02c3 }
        if (r2 == 0) goto L_0x0054;
    L_0x0052:
        r2 = r10;
        goto L_0x0055;
    L_0x0054:
        r2 = r15;
    L_0x0055:
        r11.lastQueryIsOnDupKeyUpdate = r2;	 Catch:{ all -> 0x02c3 }
        if (r16 != 0) goto L_0x0085;
    L_0x0059:
        r2 = r13.isReadOnly();	 Catch:{ all -> 0x02c3 }
        if (r2 == 0) goto L_0x0085;
    L_0x005f:
        r2 = new java.lang.StringBuilder;	 Catch:{ all -> 0x02c3 }
        r2.<init>();	 Catch:{ all -> 0x02c3 }
        r3 = "Statement.27";
        r3 = com.mysql.jdbc.Messages.getString(r3);	 Catch:{ all -> 0x02c3 }
        r2.append(r3);	 Catch:{ all -> 0x02c3 }
        r3 = "Statement.28";
        r3 = com.mysql.jdbc.Messages.getString(r3);	 Catch:{ all -> 0x02c3 }
        r2.append(r3);	 Catch:{ all -> 0x02c3 }
        r2 = r2.toString();	 Catch:{ all -> 0x02c3 }
        r3 = "S1009";
        r4 = r27.getExceptionInterceptor();	 Catch:{ all -> 0x02c3 }
        r2 = com.mysql.jdbc.SQLError.createSQLException(r2, r3, r4);	 Catch:{ all -> 0x02c3 }
        throw r2;	 Catch:{ all -> 0x02c3 }
    L_0x0085:
        r2 = r13.isReadInfoMsgEnabled();	 Catch:{ all -> 0x02c3 }
        r17 = r2;
        if (r12 == 0) goto L_0x0094;
    L_0x008d:
        r2 = 82;
        if (r9 != r2) goto L_0x0094;
    L_0x0091:
        r13.setReadInfoMsgEnabled(r10);	 Catch:{ all -> 0x02c3 }
    L_0x0094:
        r11.setupStreamingTimeout(r13);	 Catch:{ all -> 0x02aa }
        r2 = r11.doEscapeProcessing;	 Catch:{ all -> 0x02aa }
        if (r2 == 0) goto L_0x00ba;
    L_0x009b:
        r2 = r13.serverSupportsConvertFn();	 Catch:{ all -> 0x00b3 }
        r2 = com.mysql.jdbc.EscapeProcessor.escapeSQL(r1, r2, r13);	 Catch:{ all -> 0x00b3 }
        r3 = r2 instanceof java.lang.String;	 Catch:{ all -> 0x00b3 }
        if (r3 == 0) goto L_0x00ac;
    L_0x00a7:
        r3 = r2;
        r3 = (java.lang.String) r3;	 Catch:{ all -> 0x00b3 }
        r1 = r3;
        goto L_0x00ba;
    L_0x00ac:
        r3 = r2;
        r3 = (com.mysql.jdbc.EscapeProcessorResult) r3;	 Catch:{ all -> 0x00b3 }
        r3 = r3.escapedSql;	 Catch:{ all -> 0x00b3 }
        r1 = r3;
        goto L_0x00ba;
    L_0x00b3:
        r0 = move-exception;
        r4 = r1;
        r5 = r9;
        r2 = r11;
        r1 = r0;
        goto L_0x02af;
    L_0x00ba:
        r8 = r1;
        r1 = 0;
        r18 = 0;
        r7 = 0;
        r11.batchedGeneratedKeys = r7;	 Catch:{ all -> 0x02a1 }
        r2 = r27.useServerFetch();	 Catch:{ all -> 0x02a1 }
        if (r2 == 0) goto L_0x00d9;
    L_0x00c7:
        r2 = r11.createResultSetUsingServerFetch(r8);	 Catch:{ all -> 0x00d2 }
        r4 = r8;
        r26 = r9;
        r22 = r10;
        goto L_0x01e4;
    L_0x00d2:
        r0 = move-exception;
        r1 = r0;
        r4 = r8;
        r5 = r9;
    L_0x00d6:
        r2 = r11;
        goto L_0x02af;
    L_0x00d9:
        r2 = 0;
        r3 = r7;
        r4 = r13.getEnableQueryTimeouts();	 Catch:{ all -> 0x0271 }
        if (r4 == 0) goto L_0x0109;
    L_0x00e1:
        r4 = r11.timeoutInMillis;	 Catch:{ all -> 0x00fd }
        if (r4 == 0) goto L_0x0109;
    L_0x00e5:
        r4 = 5;
        r4 = r13.versionMeetsMinimum(r4, r15, r15);	 Catch:{ all -> 0x00fd }
        if (r4 == 0) goto L_0x0109;
    L_0x00ec:
        r4 = new com.mysql.jdbc.StatementImpl$CancelTask;	 Catch:{ all -> 0x00fd }
        r4.<init>(r11);	 Catch:{ all -> 0x00fd }
        r2 = r4;
        r4 = r13.getCancelTimer();	 Catch:{ all -> 0x00fd }
        r5 = r11.timeoutInMillis;	 Catch:{ all -> 0x00fd }
        r5 = (long) r5;	 Catch:{ all -> 0x00fd }
        r4.schedule(r2, r5);	 Catch:{ all -> 0x00fd }
        goto L_0x0109;
    L_0x00fd:
        r0 = move-exception;
        r21 = r1;
        r15 = r2;
        r19 = r3;
    L_0x0103:
        r24 = r8;
        r5 = r9;
        r1 = r0;
        goto L_0x027b;
    L_0x0109:
        r6 = r2;
        r2 = r13.getCatalog();	 Catch:{ all -> 0x0266 }
        r4 = r11.currentCatalog;	 Catch:{ all -> 0x0266 }
        r2 = r2.equals(r4);	 Catch:{ all -> 0x0266 }
        if (r2 != 0) goto L_0x0128;
    L_0x0116:
        r2 = r13.getCatalog();	 Catch:{ all -> 0x0121 }
        r3 = r2;
        r2 = r11.currentCatalog;	 Catch:{ all -> 0x0121 }
        r13.setCatalog(r2);	 Catch:{ all -> 0x0121 }
        goto L_0x0128;
    L_0x0121:
        r0 = move-exception;
        r21 = r1;
        r19 = r3;
    L_0x0126:
        r15 = r6;
        goto L_0x0103;
    L_0x0128:
        r19 = r3;
        r2 = 0;
        r3 = r13.getCacheResultSetMetadata();	 Catch:{ all -> 0x025e }
        if (r3 == 0) goto L_0x0140;
    L_0x0131:
        r3 = r13.getCachedMetaData(r8);	 Catch:{ all -> 0x013c }
        r1 = r3;
        if (r1 == 0) goto L_0x0140;
    L_0x0138:
        r3 = r1.fields;	 Catch:{ all -> 0x013c }
        r2 = r3;
        goto L_0x0140;
    L_0x013c:
        r0 = move-exception;
        r21 = r1;
        goto L_0x0126;
    L_0x0140:
        r21 = r1;
        r20 = r2;
        if (r16 == 0) goto L_0x0151;
    L_0x0146:
        r1 = r11.maxRows;	 Catch:{ all -> 0x0149 }
        goto L_0x0152;
    L_0x0149:
        r0 = move-exception;
        r1 = r0;
        r15 = r6;
        r24 = r8;
        r5 = r9;
        goto L_0x027b;
    L_0x0151:
        r1 = -1;
    L_0x0152:
        r13.setSessionMaxRows(r1);	 Catch:{ all -> 0x0258 }
        r27.statementBegins();	 Catch:{ all -> 0x0258 }
        r4 = r11.maxRows;	 Catch:{ all -> 0x0258 }
        r5 = 0;
        r3 = r11.resultSetType;	 Catch:{ all -> 0x0258 }
        r2 = r11.resultSetConcurrency;	 Catch:{ all -> 0x0258 }
        r22 = r27.createStreamingResultSet();	 Catch:{ all -> 0x0258 }
        r1 = r11.currentCatalog;	 Catch:{ all -> 0x0258 }
        r23 = r1;
        r1 = r13;
        r24 = r2;
        r2 = r11;
        r25 = r3;
        r3 = r8;
        r15 = r6;
        r6 = r25;
        r7 = r24;
        r24 = r8;
        r8 = r22;
        r26 = r9;
        r9 = r23;
        r22 = r10;
        r10 = r20;
        r1 = r1.execSQL(r2, r3, r4, r5, r6, r7, r8, r9, r10);	 Catch:{ all -> 0x0253 }
        r18 = r1;
        if (r15 == 0) goto L_0x019a;
    L_0x0187:
        r1 = r15.caughtWhileCancelling;	 Catch:{ all -> 0x0194 }
        if (r1 == 0) goto L_0x018e;
    L_0x018b:
        r1 = r15.caughtWhileCancelling;	 Catch:{ all -> 0x0194 }
        throw r1;	 Catch:{ all -> 0x0194 }
    L_0x018e:
        r15.cancel();	 Catch:{ all -> 0x0194 }
        r1 = 0;
        r2 = r1;
        goto L_0x019b;
    L_0x0194:
        r0 = move-exception;
        r1 = r0;
        r5 = r26;
        goto L_0x027b;
    L_0x019a:
        r2 = r15;
    L_0x019b:
        r1 = r11.cancelTimeoutMutex;	 Catch:{ all -> 0x024d }
        monitor-enter(r1);	 Catch:{ all -> 0x024d }
        r3 = r11.wasCancelled;	 Catch:{ all -> 0x0240 }
        if (r3 == 0) goto L_0x01be;
    L_0x01a2:
        r3 = 0;
        r4 = r11.wasCancelledByTimeout;	 Catch:{ all -> 0x01b8 }
        if (r4 == 0) goto L_0x01ae;
    L_0x01a7:
        r4 = new com.mysql.jdbc.exceptions.MySQLTimeoutException;	 Catch:{ all -> 0x01b8 }
        r4.<init>();	 Catch:{ all -> 0x01b8 }
        r3 = r4;
        goto L_0x01b4;
    L_0x01ae:
        r4 = new com.mysql.jdbc.exceptions.MySQLStatementCancelledException;	 Catch:{ all -> 0x01b8 }
        r4.<init>();	 Catch:{ all -> 0x01b8 }
        r3 = r4;
    L_0x01b4:
        r27.resetCancelledState();	 Catch:{ all -> 0x01b8 }
        throw r3;	 Catch:{ all -> 0x01b8 }
    L_0x01b8:
        r0 = move-exception;
        r3 = r0;
        r5 = r26;
        goto L_0x0244;
    L_0x01be:
        monitor-exit(r1);	 Catch:{ all -> 0x0240 }
        r1 = r2;
        r2 = r19;
        r3 = r21;
        r4 = r24;
        if (r1 == 0) goto L_0x01db;
    L_0x01c9:
        r1.cancel();	 Catch:{ all -> 0x01d4 }
        r5 = r13.getCancelTimer();	 Catch:{ all -> 0x01d4 }
        r5.purge();	 Catch:{ all -> 0x01d4 }
        goto L_0x01db;
    L_0x01d4:
        r0 = move-exception;
        r1 = r0;
        r2 = r11;
        r5 = r26;
        goto L_0x02af;
    L_0x01db:
        if (r2 == 0) goto L_0x01e0;
    L_0x01dd:
        r13.setCatalog(r2);	 Catch:{ all -> 0x01d4 }
        r1 = r3;
        r2 = r18;
    L_0x01e4:
        if (r2 == 0) goto L_0x0217;
    L_0x01e6:
        r5 = r2.getUpdateID();	 Catch:{ all -> 0x0210 }
        r11.lastInsertId = r5;	 Catch:{ all -> 0x0210 }
        r11.results = r2;	 Catch:{ all -> 0x0210 }
        r5 = r26;
        r2.setFirstCharOfQuery(r5);	 Catch:{ all -> 0x0222 }
        r3 = r2.reallyResult();	 Catch:{ all -> 0x0222 }
        if (r3 == 0) goto L_0x0219;
    L_0x01f9:
        if (r1 == 0) goto L_0x0201;
    L_0x01fb:
        r3 = r11.results;	 Catch:{ all -> 0x0222 }
        r13.initializeResultsMetadataFromCache(r4, r1, r3);	 Catch:{ all -> 0x0222 }
        goto L_0x0219;
    L_0x0201:
        r3 = r11.connection;	 Catch:{ all -> 0x0222 }
        r3 = r3.getCacheResultSetMetadata();	 Catch:{ all -> 0x0222 }
        if (r3 == 0) goto L_0x0219;
    L_0x0209:
        r3 = r11.results;	 Catch:{ all -> 0x0222 }
        r6 = 0;
        r13.initializeResultsMetadataFromCache(r4, r6, r3);	 Catch:{ all -> 0x0222 }
        goto L_0x0219;
    L_0x0210:
        r0 = move-exception;
        r5 = r26;
        r1 = r0;
        r2 = r11;
        goto L_0x02af;
    L_0x0217:
        r5 = r26;
    L_0x0219:
        if (r2 == 0) goto L_0x0226;
    L_0x021b:
        r3 = r2.reallyResult();	 Catch:{ all -> 0x0222 }
        if (r3 == 0) goto L_0x0226;
    L_0x0221:
        goto L_0x0228;
    L_0x0222:
        r0 = move-exception;
        r1 = r0;
        goto L_0x00d6;
    L_0x0226:
        r22 = 0;
    L_0x0228:
        r3 = r5;
        r5 = r16;
        r6 = r17;
        r7 = r11;
        r8 = r12;
        r13.setReadInfoMsgEnabled(r6);	 Catch:{ all -> 0x023a }
        r9 = r7.statementExecuting;	 Catch:{ all -> 0x023a }
        r10 = 0;
        r9.set(r10);	 Catch:{ all -> 0x023a }
        monitor-exit(r14);	 Catch:{ all -> 0x023a }
        return r22;
    L_0x023a:
        r0 = move-exception;
        r1 = r0;
        r2 = r7;
        r12 = r8;
        goto L_0x02c7;
    L_0x0240:
        r0 = move-exception;
        r5 = r26;
        r3 = r0;
    L_0x0244:
        monitor-exit(r1);	 Catch:{ all -> 0x024a }
        throw r3;	 Catch:{ all -> 0x0246 }
    L_0x0246:
        r0 = move-exception;
        r1 = r0;
        r15 = r2;
        goto L_0x027b;
    L_0x024a:
        r0 = move-exception;
        r3 = r0;
        goto L_0x0244;
    L_0x024d:
        r0 = move-exception;
        r5 = r26;
        r1 = r0;
        r15 = r2;
        goto L_0x0257;
    L_0x0253:
        r0 = move-exception;
        r5 = r26;
        r1 = r0;
    L_0x0257:
        goto L_0x027b;
    L_0x0258:
        r0 = move-exception;
        r15 = r6;
        r24 = r8;
        r5 = r9;
        goto L_0x026f;
    L_0x025e:
        r0 = move-exception;
        r15 = r6;
        r24 = r8;
        r5 = r9;
        r21 = r1;
        goto L_0x026f;
    L_0x0266:
        r0 = move-exception;
        r15 = r6;
        r24 = r8;
        r5 = r9;
        r21 = r1;
        r19 = r3;
    L_0x026f:
        r1 = r0;
        goto L_0x027b;
    L_0x0271:
        r0 = move-exception;
        r24 = r8;
        r5 = r9;
        r21 = r1;
        r15 = r2;
        r19 = r3;
        r1 = r0;
    L_0x027b:
        r2 = r15;
        r3 = r19;
        r4 = r21;
        r6 = r18;
        r9 = r5;
        r5 = r11;
        r7 = r24;
        r8 = r12;
        if (r2 == 0) goto L_0x029b;
    L_0x0289:
        r2.cancel();	 Catch:{ all -> 0x0294 }
        r10 = r13.getCancelTimer();	 Catch:{ all -> 0x0294 }
        r10.purge();	 Catch:{ all -> 0x0294 }
        goto L_0x029b;
    L_0x0294:
        r0 = move-exception;
        r1 = r0;
        r2 = r5;
        r4 = r7;
        r12 = r8;
        r5 = r9;
        goto L_0x02af;
    L_0x029b:
        if (r3 == 0) goto L_0x02a0;
    L_0x029d:
        r13.setCatalog(r3);	 Catch:{ all -> 0x0294 }
    L_0x02a0:
        throw r1;	 Catch:{ all -> 0x0294 }
    L_0x02a1:
        r0 = move-exception;
        r24 = r8;
        r5 = r9;
        r1 = r0;
        r2 = r11;
        r4 = r24;
        goto L_0x02af;
    L_0x02aa:
        r0 = move-exception;
        r5 = r9;
        r4 = r1;
        r2 = r11;
        r1 = r0;
    L_0x02af:
        r3 = r5;
        r5 = r16;
        r6 = r17;
        r7 = r12;
        r13.setReadInfoMsgEnabled(r6);	 Catch:{ all -> 0x02bf }
        r8 = r2.statementExecuting;	 Catch:{ all -> 0x02bf }
        r9 = 0;
        r8.set(r9);	 Catch:{ all -> 0x02bf }
        throw r1;	 Catch:{ all -> 0x02bf }
    L_0x02bf:
        r0 = move-exception;
        r1 = r0;
        r12 = r7;
        goto L_0x02c7;
    L_0x02c3:
        r0 = move-exception;
        r4 = r1;
        r2 = r11;
    L_0x02c6:
        r1 = r0;
    L_0x02c7:
        monitor-exit(r14);	 Catch:{ all -> 0x02c9 }
        throw r1;
    L_0x02c9:
        r0 = move-exception;
        goto L_0x02c6;
        */
        throw new UnsupportedOperationException("Method not decompiled: com.mysql.jdbc.StatementImpl.executeInternal(java.lang.String, boolean):boolean");
    }

    protected void statementBegins() {
        this.clearWarningsCalled = false;
        this.statementExecuting.set(true);
        MySQLConnection physicalConn = this.connection.getMultiHostSafeProxy().getActiveMySQLConnection();
        while (!(physicalConn instanceof ConnectionImpl)) {
            physicalConn = physicalConn.getMultiHostSafeProxy().getActiveMySQLConnection();
        }
        this.physicalConnection = new WeakReference(physicalConn);
    }

    protected void resetCancelledState() throws SQLException {
        synchronized (checkClosed().getConnectionMutex()) {
            if (this.cancelTimeoutMutex == null) {
                return;
            }
            synchronized (this.cancelTimeoutMutex) {
                this.wasCancelled = false;
                this.wasCancelledByTimeout = false;
            }
        }
    }

    public boolean execute(String sql, int returnGeneratedKeys) throws SQLException {
        boolean z = true;
        if (returnGeneratedKeys != 1) {
            z = false;
        }
        return executeInternal(sql, z);
    }

    public boolean execute(String sql, int[] generatedKeyIndices) throws SQLException {
        boolean z = generatedKeyIndices != null && generatedKeyIndices.length > 0;
        return executeInternal(sql, z);
    }

    public boolean execute(String sql, String[] generatedKeyNames) throws SQLException {
        boolean z = generatedKeyNames != null && generatedKeyNames.length > 0;
        return executeInternal(sql, z);
    }

    public int[] executeBatch() throws SQLException {
        return Util.truncateAndConvertToInt(executeBatchInternal());
    }

    /* JADX WARNING: inconsistent code. */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    protected long[] executeBatchInternal() throws java.sql.SQLException {
        /*
        r19 = this;
        r1 = r19;
        r2 = r19.checkClosed();
        r3 = r2.getConnectionMutex();
        monitor-enter(r3);
        r4 = r2.isReadOnly();	 Catch:{ all -> 0x01b0 }
        if (r4 == 0) goto L_0x0037;
    L_0x0011:
        r4 = new java.lang.StringBuilder;	 Catch:{ all -> 0x01b0 }
        r4.<init>();	 Catch:{ all -> 0x01b0 }
        r5 = "Statement.34";
        r5 = com.mysql.jdbc.Messages.getString(r5);	 Catch:{ all -> 0x01b0 }
        r4.append(r5);	 Catch:{ all -> 0x01b0 }
        r5 = "Statement.35";
        r5 = com.mysql.jdbc.Messages.getString(r5);	 Catch:{ all -> 0x01b0 }
        r4.append(r5);	 Catch:{ all -> 0x01b0 }
        r4 = r4.toString();	 Catch:{ all -> 0x01b0 }
        r5 = "S1009";
        r6 = r19.getExceptionInterceptor();	 Catch:{ all -> 0x01b0 }
        r4 = com.mysql.jdbc.SQLError.createSQLException(r4, r5, r6);	 Catch:{ all -> 0x01b0 }
        throw r4;	 Catch:{ all -> 0x01b0 }
    L_0x0037:
        r19.implicitlyCloseAllOpenResults();	 Catch:{ all -> 0x01b0 }
        r4 = r1.batchedArgs;	 Catch:{ all -> 0x01b0 }
        r5 = 0;
        if (r4 == 0) goto L_0x01ab;
    L_0x003f:
        r4 = r1.batchedArgs;	 Catch:{ all -> 0x01b0 }
        r4 = r4.size();	 Catch:{ all -> 0x01b0 }
        if (r4 != 0) goto L_0x0049;
    L_0x0047:
        goto L_0x01ab;
    L_0x0049:
        r4 = r1.timeoutInMillis;	 Catch:{ all -> 0x01b0 }
        r1.timeoutInMillis = r5;	 Catch:{ all -> 0x01b0 }
        r6 = 0;
        r19.resetCancelledState();	 Catch:{ all -> 0x018c }
        r19.statementBegins();	 Catch:{ all -> 0x018c }
        r7 = 1;
        r1.retrieveGeneratedKeys = r7;	 Catch:{ all -> 0x0180 }
        r8 = 0;
        r9 = r1.batchedArgs;	 Catch:{ all -> 0x0180 }
        if (r9 == 0) goto L_0x0140;
    L_0x005c:
        r9 = r1.batchedArgs;	 Catch:{ all -> 0x0180 }
        r9 = r9.size();	 Catch:{ all -> 0x0180 }
        r10 = new java.util.ArrayList;	 Catch:{ all -> 0x0180 }
        r11 = r1.batchedArgs;	 Catch:{ all -> 0x0180 }
        r11 = r11.size();	 Catch:{ all -> 0x0180 }
        r10.<init>(r11);	 Catch:{ all -> 0x0180 }
        r1.batchedGeneratedKeys = r10;	 Catch:{ all -> 0x0180 }
        r10 = r2.getAllowMultiQueries();	 Catch:{ all -> 0x0180 }
        r11 = 4;
        r12 = r2.versionMeetsMinimum(r11, r7, r7);	 Catch:{ all -> 0x0180 }
        if (r12 == 0) goto L_0x00a4;
    L_0x007a:
        if (r10 != 0) goto L_0x0084;
    L_0x007c:
        r12 = r2.getRewriteBatchedStatements();	 Catch:{ all -> 0x0180 }
        if (r12 == 0) goto L_0x00a4;
    L_0x0082:
        if (r9 <= r11) goto L_0x00a4;
    L_0x0084:
        r7 = r1.executeBatchUsingMultiQueries(r10, r9, r4);	 Catch:{ all -> 0x0180 }
        r11 = r1.statementExecuting;	 Catch:{ all -> 0x018c }
        r11.set(r5);	 Catch:{ all -> 0x018c }
        if (r6 == 0) goto L_0x009a;
    L_0x0090:
        r6.cancel();	 Catch:{ all -> 0x01b0 }
        r5 = r2.getCancelTimer();	 Catch:{ all -> 0x01b0 }
        r5.purge();	 Catch:{ all -> 0x01b0 }
    L_0x009a:
        r19.resetCancelledState();	 Catch:{ all -> 0x01b0 }
        r1.timeoutInMillis = r4;	 Catch:{ all -> 0x01b0 }
        r19.clearBatch();	 Catch:{ all -> 0x01b0 }
        monitor-exit(r3);	 Catch:{ all -> 0x01b0 }
        return r7;
    L_0x00a4:
        r11 = r2.getEnableQueryTimeouts();	 Catch:{ all -> 0x0180 }
        if (r11 == 0) goto L_0x00c1;
    L_0x00aa:
        if (r4 == 0) goto L_0x00c1;
    L_0x00ac:
        r11 = 5;
        r11 = r2.versionMeetsMinimum(r11, r5, r5);	 Catch:{ all -> 0x0180 }
        if (r11 == 0) goto L_0x00c1;
    L_0x00b3:
        r11 = new com.mysql.jdbc.StatementImpl$CancelTask;	 Catch:{ all -> 0x0180 }
        r11.<init>(r1);	 Catch:{ all -> 0x0180 }
        r6 = r11;
        r11 = r2.getCancelTimer();	 Catch:{ all -> 0x0180 }
        r12 = (long) r4;	 Catch:{ all -> 0x0180 }
        r11.schedule(r6, r12);	 Catch:{ all -> 0x0180 }
    L_0x00c1:
        r11 = new long[r9];	 Catch:{ all -> 0x0180 }
        r8 = r11;
        r11 = r5;
    L_0x00c5:
        r12 = -3;
        if (r11 >= r9) goto L_0x00ce;
    L_0x00c9:
        r8[r11] = r12;	 Catch:{ all -> 0x0180 }
        r11 = r11 + 1;
        goto L_0x00c5;
    L_0x00ce:
        r11 = 0;
        r14 = 0;
        r14 = 0;
    L_0x00d1:
        if (r14 >= r9) goto L_0x0135;
    L_0x00d3:
        r15 = r1.batchedArgs;	 Catch:{ SQLException -> 0x00f8 }
        r15 = r15.get(r14);	 Catch:{ SQLException -> 0x00f8 }
        r15 = (java.lang.String) r15;	 Catch:{ SQLException -> 0x00f8 }
        r16 = r1.executeUpdateInternal(r15, r7, r7);	 Catch:{ SQLException -> 0x00f8 }
        r8[r14] = r16;	 Catch:{ SQLException -> 0x00f8 }
        r7 = r1.results;	 Catch:{ SQLException -> 0x00f8 }
        r7 = r7.getFirstCharOfQuery();	 Catch:{ SQLException -> 0x00f8 }
        r5 = 73;
        if (r7 != r5) goto L_0x00f3;
    L_0x00eb:
        r5 = r1.containsOnDuplicateKeyInString(r15);	 Catch:{ SQLException -> 0x00f8 }
        if (r5 == 0) goto L_0x00f3;
    L_0x00f1:
        r5 = 1;
        goto L_0x00f4;
    L_0x00f3:
        r5 = 0;
    L_0x00f4:
        r1.getBatchedGeneratedKeys(r5);	 Catch:{ SQLException -> 0x00f8 }
        goto L_0x0110;
    L_0x00f8:
        r0 = move-exception;
        r5 = r0;
        r8[r14] = r12;	 Catch:{ all -> 0x0180 }
        r7 = r1.continueBatchOnError;	 Catch:{ all -> 0x0180 }
        if (r7 == 0) goto L_0x0115;
    L_0x0100:
        r7 = r5 instanceof com.mysql.jdbc.exceptions.MySQLTimeoutException;	 Catch:{ all -> 0x0180 }
        if (r7 != 0) goto L_0x0115;
    L_0x0104:
        r7 = r5 instanceof com.mysql.jdbc.exceptions.MySQLStatementCancelledException;	 Catch:{ all -> 0x0180 }
        if (r7 != 0) goto L_0x0115;
    L_0x0108:
        r7 = r1.hasDeadlockOrTimeoutRolledBackTx(r5);	 Catch:{ all -> 0x0180 }
        if (r7 != 0) goto L_0x0115;
    L_0x010e:
        r7 = r5;
        r11 = r7;
    L_0x0110:
        r14 = r14 + 1;
        r5 = 0;
        r7 = 1;
        goto L_0x00d1;
    L_0x0115:
        r7 = new long[r14];	 Catch:{ all -> 0x0180 }
        r15 = r1.hasDeadlockOrTimeoutRolledBackTx(r5);	 Catch:{ all -> 0x0180 }
        if (r15 == 0) goto L_0x0128;
    L_0x011d:
        r15 = 0;
    L_0x011e:
        r12 = r7.length;	 Catch:{ all -> 0x0180 }
        if (r15 >= r12) goto L_0x012c;
    L_0x0121:
        r12 = -3;
        r7[r15] = r12;	 Catch:{ all -> 0x0180 }
        r15 = r15 + 1;
        goto L_0x011e;
    L_0x0128:
        r12 = 0;
        java.lang.System.arraycopy(r8, r12, r7, r12, r14);	 Catch:{ all -> 0x0180 }
    L_0x012c:
        r12 = r19.getExceptionInterceptor();	 Catch:{ all -> 0x0180 }
        r12 = com.mysql.jdbc.SQLError.createBatchUpdateException(r5, r7, r12);	 Catch:{ all -> 0x0180 }
        throw r12;	 Catch:{ all -> 0x0180 }
    L_0x0135:
        if (r11 == 0) goto L_0x0140;
    L_0x0137:
        r5 = r19.getExceptionInterceptor();	 Catch:{ all -> 0x0180 }
        r5 = com.mysql.jdbc.SQLError.createBatchUpdateException(r11, r8, r5);	 Catch:{ all -> 0x0180 }
        throw r5;	 Catch:{ all -> 0x0180 }
    L_0x0140:
        if (r6 == 0) goto L_0x0155;
    L_0x0142:
        r5 = r6.caughtWhileCancelling;	 Catch:{ all -> 0x0180 }
        if (r5 == 0) goto L_0x0149;
    L_0x0146:
        r5 = r6.caughtWhileCancelling;	 Catch:{ all -> 0x0180 }
        throw r5;	 Catch:{ all -> 0x0180 }
    L_0x0149:
        r6.cancel();	 Catch:{ all -> 0x0180 }
        r5 = r2.getCancelTimer();	 Catch:{ all -> 0x0180 }
        r5.purge();	 Catch:{ all -> 0x0180 }
        r5 = 0;
        r6 = r5;
    L_0x0155:
        if (r8 == 0) goto L_0x0159;
    L_0x0157:
        r7 = r8;
        goto L_0x015c;
    L_0x0159:
        r5 = 0;
        r7 = new long[r5];	 Catch:{ all -> 0x0180 }
    L_0x015c:
        r5 = r1;
        r9 = r5.statementExecuting;	 Catch:{ all -> 0x017d }
        r10 = 0;
        r9.set(r10);	 Catch:{ all -> 0x017d }
        if (r6 == 0) goto L_0x0173;
    L_0x0166:
        r6.cancel();	 Catch:{ all -> 0x0171 }
        r9 = r2.getCancelTimer();	 Catch:{ all -> 0x0171 }
        r9.purge();	 Catch:{ all -> 0x0171 }
        goto L_0x0173;
    L_0x0171:
        r0 = move-exception;
        goto L_0x01b2;
    L_0x0173:
        r5.resetCancelledState();	 Catch:{ all -> 0x0171 }
        r5.timeoutInMillis = r4;	 Catch:{ all -> 0x0171 }
        r5.clearBatch();	 Catch:{ all -> 0x0171 }
        monitor-exit(r3);	 Catch:{ all -> 0x0171 }
        return r7;
    L_0x017d:
        r0 = move-exception;
        r7 = r5;
        goto L_0x018e;
    L_0x0180:
        r0 = move-exception;
        r5 = r0;
        r7 = r1;
        r8 = r7.statementExecuting;	 Catch:{ all -> 0x018a }
        r9 = 0;
        r8.set(r9);	 Catch:{ all -> 0x018a }
        throw r5;	 Catch:{ all -> 0x018a }
    L_0x018a:
        r0 = move-exception;
        goto L_0x018e;
    L_0x018c:
        r0 = move-exception;
        r7 = r1;
    L_0x018e:
        r5 = r2;
        r2 = r0;
        if (r6 == 0) goto L_0x01a2;
    L_0x0192:
        r6.cancel();	 Catch:{ all -> 0x019d }
        r8 = r5.getCancelTimer();	 Catch:{ all -> 0x019d }
        r8.purge();	 Catch:{ all -> 0x019d }
        goto L_0x01a2;
    L_0x019d:
        r0 = move-exception;
        r2 = r0;
        r4 = r5;
        r5 = r7;
        goto L_0x01b4;
    L_0x01a2:
        r7.resetCancelledState();	 Catch:{ all -> 0x019d }
        r7.timeoutInMillis = r4;	 Catch:{ all -> 0x019d }
        r7.clearBatch();	 Catch:{ all -> 0x019d }
        throw r2;	 Catch:{ all -> 0x019d }
    L_0x01ab:
        r4 = 0;
        r4 = new long[r4];	 Catch:{ all -> 0x01b0 }
        monitor-exit(r3);	 Catch:{ all -> 0x01b0 }
        return r4;
    L_0x01b0:
        r0 = move-exception;
        r5 = r1;
    L_0x01b2:
        r4 = r2;
    L_0x01b3:
        r2 = r0;
    L_0x01b4:
        monitor-exit(r3);	 Catch:{ all -> 0x01b6 }
        throw r2;
    L_0x01b6:
        r0 = move-exception;
        goto L_0x01b3;
        */
        throw new UnsupportedOperationException("Method not decompiled: com.mysql.jdbc.StatementImpl.executeBatchInternal():long[]");
    }

    protected final boolean hasDeadlockOrTimeoutRolledBackTx(SQLException ex) {
        int vendorCode = ex.getErrorCode();
        if (vendorCode != MysqlErrorNumbers.ER_LOCK_DEADLOCK) {
            switch (vendorCode) {
                case MysqlErrorNumbers.ER_LOCK_WAIT_TIMEOUT /*1205*/:
                    return this.version5013OrNewer ^ true;
                case MysqlErrorNumbers.ER_LOCK_TABLE_FULL /*1206*/:
                    break;
                default:
                    return false;
            }
        }
        return true;
    }

    private long[] executeBatchUsingMultiQueries(boolean multiQueriesEnabled, int nbrCommands, int individualStatementTimeout) throws SQLException {
        int argumentSetsInBatchSoFar;
        boolean multiQueriesEnabled2;
        StatementImpl statementImpl = this;
        int nbrCommands2 = nbrCommands;
        int i = individualStatementTimeout;
        MySQLConnection locallyScopedConn = checkClosed();
        synchronized (locallyScopedConn.getConnectionMutex()) {
            if (!multiQueriesEnabled) {
                try {
                    locallyScopedConn.getIO().enableMultiQueries();
                } catch (Throwable th) {
                    Throwable th2 = th;
                    StatementImpl this = statementImpl;
                    int individualStatementTimeout2 = i;
                    Throwable th3 = th2;
                    try {
                    } catch (Throwable th4) {
                        th2 = th4;
                        Throwable th32 = th2;
                        throw th32;
                    }
                    throw th32;
                }
            }
            Statement batchStmt = null;
            CancelTask cancelTask = null;
            long[] updateCounts;
            int i2;
            SQLException sqlEx;
            int i3;
            try {
                int numberOfBytesPerChar;
                updateCounts = new long[nbrCommands2];
                for (i2 = 0; i2 < nbrCommands2; i2++) {
                    updateCounts[i2] = -3;
                }
                StringBuilder queryBuf = new StringBuilder();
                batchStmt = locallyScopedConn.createStatement();
                if (locallyScopedConn.getEnableQueryTimeouts() && i != 0 && locallyScopedConn.versionMeetsMinimum(5, 0, 0)) {
                    cancelTask = new CancelTask((StatementImpl) batchStmt);
                    locallyScopedConn.getCancelTimer().schedule(cancelTask, (long) i);
                }
                int counter = 0;
                int numberOfBytesPerChar2 = 1;
                String connectionEncoding = locallyScopedConn.getEncoding();
                if (StringUtils.startsWithIgnoreCase(connectionEncoding, "utf")) {
                    numberOfBytesPerChar2 = 3;
                } else if (CharsetMapping.isMultibyteCharset(connectionEncoding)) {
                    numberOfBytesPerChar2 = 2;
                }
                int escapeAdjust = 1;
                batchStmt.setEscapeProcessing(statementImpl.doEscapeProcessing);
                if (statementImpl.doEscapeProcessing) {
                    individualStatementTimeout2 = 2;
                } else {
                    individualStatementTimeout2 = escapeAdjust;
                }
                sqlEx = null;
                i2 = 0;
                argumentSetsInBatchSoFar = 0;
                while (i2 < nbrCommands2) {
                    String nextQuery = (String) statementImpl.batchedArgs.get(i2);
                    int escapeAdjust2 = individualStatementTimeout2;
                    numberOfBytesPerChar = numberOfBytesPerChar2;
                    if ((((((queryBuf.length() + nextQuery.length()) * numberOfBytesPerChar2) + 1) + 4) * individualStatementTimeout2) + 32 > statementImpl.connection.getMaxAllowedPacket()) {
                        batchStmt.execute(queryBuf.toString(), 1);
                        individualStatementTimeout2 = processMultiCountsAndKeys((StatementImpl) batchStmt, counter, updateCounts);
                        queryBuf = new StringBuilder();
                        argumentSetsInBatchSoFar = 0;
                        counter = individualStatementTimeout2;
                    }
                    queryBuf.append(nextQuery);
                    queryBuf.append(";");
                    argumentSetsInBatchSoFar++;
                    i2++;
                    individualStatementTimeout2 = escapeAdjust2;
                    numberOfBytesPerChar2 = numberOfBytesPerChar;
                    i = individualStatementTimeout;
                }
                numberOfBytesPerChar = numberOfBytesPerChar2;
                if (queryBuf.length() > 0) {
                    try {
                        batchStmt.execute(queryBuf.toString(), 1);
                    } catch (SQLException e) {
                        sqlEx = handleExceptionForBatch(i2 - 1, argumentSetsInBatchSoFar, updateCounts, e);
                    }
                    counter = processMultiCountsAndKeys((StatementImpl) batchStmt, counter, updateCounts);
                }
                SQLException sqlEx2 = sqlEx;
                if (cancelTask != null) {
                    if (cancelTask.caughtWhileCancelling != null) {
                        throw cancelTask.caughtWhileCancelling;
                    }
                    cancelTask.cancel();
                    locallyScopedConn.getCancelTimer().purge();
                    cancelTask = null;
                }
                if (sqlEx2 != null) {
                    throw SQLError.createBatchUpdateException(sqlEx2, updateCounts, getExceptionInterceptor());
                }
                long[] jArr = updateCounts != null ? updateCounts : new long[0];
                this = statementImpl;
                multiQueriesEnabled2 = multiQueriesEnabled;
                int individualStatementTimeout3 = individualStatementTimeout;
                if (cancelTask != null) {
                    try {
                        cancelTask.cancel();
                        i3 = nbrCommands2;
                        locallyScopedConn.getCancelTimer().purge();
                    } catch (Throwable th22) {
                        th32 = th22;
                        individualStatementTimeout2 = individualStatementTimeout3;
                        throw th32;
                    }
                }
                resetCancelledState();
                if (batchStmt != null) {
                    batchStmt.close();
                }
                if (!multiQueriesEnabled2) {
                    locallyScopedConn.getIO().disableMultiQueries();
                }
                return jArr;
            } catch (SQLException e2) {
                sqlEx = handleExceptionForBatch(i2, argumentSetsInBatchSoFar, updateCounts, e2);
            } catch (Throwable th222) {
                Throwable th5 = th222;
                StatementImpl this2 = statementImpl;
                multiQueriesEnabled2 = multiQueriesEnabled;
                if (cancelTask != null) {
                    try {
                        cancelTask.cancel();
                        locallyScopedConn.getCancelTimer().purge();
                    } catch (Throwable th6) {
                        if (!multiQueriesEnabled2) {
                            locallyScopedConn.getIO().disableMultiQueries();
                        }
                    }
                }
                resetCancelledState();
                if (batchStmt != null) {
                    batchStmt.close();
                }
                if (!multiQueriesEnabled2) {
                    locallyScopedConn.getIO().disableMultiQueries();
                }
            }
        }
    }

    protected int processMultiCountsAndKeys(StatementImpl batchedStatement, int updateCountCounter, long[] updateCounts) throws SQLException {
        Throwable th;
        synchronized (checkClosed().getConnectionMutex()) {
            int updateCountCounter2 = updateCountCounter + 1;
            try {
                updateCounts[updateCountCounter] = batchedStatement.getLargeUpdateCount();
                boolean doGenKeys = this.batchedGeneratedKeys != null;
                byte[][] row = null;
                if (doGenKeys) {
                    this.batchedGeneratedKeys.add(new ByteArrayRow(new byte[][]{StringUtils.getBytes(Long.toString(batchedStatement.getLastInsertID()))}, getExceptionInterceptor()));
                }
                while (true) {
                    if (!batchedStatement.getMoreResults()) {
                        if (batchedStatement.getLargeUpdateCount() == -1) {
                            return updateCountCounter2;
                        }
                    }
                    int updateCountCounter3 = updateCountCounter2 + 1;
                    try {
                        updateCounts[updateCountCounter2] = batchedStatement.getLargeUpdateCount();
                        if (doGenKeys) {
                            this.batchedGeneratedKeys.add(new ByteArrayRow(new byte[][]{StringUtils.getBytes(Long.toString(batchedStatement.getLastInsertID()))}, getExceptionInterceptor()));
                        }
                        updateCountCounter2 = updateCountCounter3;
                    } catch (Throwable th2) {
                        th = th2;
                        updateCountCounter2 = updateCountCounter3;
                    }
                }
            } catch (Throwable th3) {
                th = th3;
                throw th;
            }
        }
    }

    protected SQLException handleExceptionForBatch(int endOfBatchIndex, int numValuesPerBatch, long[] updateCounts, SQLException ex) throws BatchUpdateException, SQLException {
        for (int j = endOfBatchIndex; j > endOfBatchIndex - numValuesPerBatch; j--) {
            updateCounts[j] = -3;
        }
        if (this.continueBatchOnError && !(ex instanceof MySQLTimeoutException) && !(ex instanceof MySQLStatementCancelledException) && !hasDeadlockOrTimeoutRolledBackTx(ex)) {
            return ex;
        }
        long[] newUpdateCounts = new long[endOfBatchIndex];
        System.arraycopy(updateCounts, 0, newUpdateCounts, 0, endOfBatchIndex);
        throw SQLError.createBatchUpdateException(ex, newUpdateCounts, getExceptionInterceptor());
    }

    public ResultSet executeQuery(String sql) throws SQLException {
        Throwable th;
        String oldCatalog;
        MySQLConnection locallyScopedConn;
        CancelTask timeoutTask;
        String oldCatalog2;
        String sql2;
        Throwable th2;
        StatementImpl statementImpl;
        StatementImpl statementImpl2 = this;
        String str = sql;
        synchronized (checkClosed().getConnectionMutex()) {
            String str2;
            try {
                MySQLConnection locallyScopedConn2 = statementImpl2.connection;
                statementImpl2.retrieveGeneratedKeys = false;
                checkNullOrEmptyQuery(sql);
                resetCancelledState();
                implicitlyCloseAllOpenResults();
                ResultSet resultSet;
                if (str.charAt(0) == '/' && str.startsWith(PING_MARKER)) {
                    doPingInstead();
                    resultSet = statementImpl2.results;
                    return resultSet;
                }
                setupStreamingTimeout(locallyScopedConn2);
                if (statementImpl2.doEscapeProcessing) {
                    Object escapedSqlResult = EscapeProcessor.escapeSQL(str, locallyScopedConn2.serverSupportsConvertFn(), statementImpl2.connection);
                    if (escapedSqlResult instanceof String) {
                        str = (String) escapedSqlResult;
                    } else {
                        str = ((EscapeProcessorResult) escapedSqlResult).escapedSql;
                    }
                }
                String sql3 = str;
                try {
                    char firstStatementChar = StringUtils.firstAlphaCharUc(sql3, findStartOfStatement(sql3));
                    checkForDml(sql3, firstStatementChar);
                    CachedResultSetMetaData cachedMetaData = null;
                    if (useServerFetch()) {
                        statementImpl2.results = createResultSetUsingServerFetch(sql3);
                        resultSet = statementImpl2.results;
                        return resultSet;
                    }
                    CancelTask timeoutTask2 = null;
                    String oldCatalog3 = null;
                    CachedResultSetMetaData cachedMetaData2;
                    CancelTask timeoutTask3;
                    char firstStatementChar2;
                    try {
                        if (locallyScopedConn2.getEnableQueryTimeouts()) {
                            try {
                                if (statementImpl2.timeoutInMillis != 0 && locallyScopedConn2.versionMeetsMinimum(5, 0, 0)) {
                                    timeoutTask2 = new CancelTask(statementImpl2);
                                    locallyScopedConn2.getCancelTimer().schedule(timeoutTask2, (long) statementImpl2.timeoutInMillis);
                                }
                            } catch (Throwable th3) {
                                th = th3;
                                cachedMetaData2 = null;
                                timeoutTask3 = null;
                                oldCatalog = oldCatalog3;
                                firstStatementChar2 = firstStatementChar;
                                cachedMetaData = th;
                                locallyScopedConn = locallyScopedConn2;
                                timeoutTask = timeoutTask3;
                                oldCatalog2 = oldCatalog;
                                sql2 = sql3;
                                try {
                                    statementImpl2.statementExecuting.set(false);
                                    if (timeoutTask != null) {
                                        timeoutTask.cancel();
                                        locallyScopedConn.getCancelTimer().purge();
                                    }
                                    if (oldCatalog2 != null) {
                                        locallyScopedConn.setCatalog(oldCatalog2);
                                    }
                                    throw cachedMetaData;
                                } catch (Throwable th4) {
                                    th2 = th4;
                                    str2 = sql2;
                                    try {
                                    } catch (Throwable th5) {
                                        th4 = th5;
                                        th2 = th4;
                                        throw th2;
                                    }
                                    throw th2;
                                }
                            }
                        }
                        CancelTask timeoutTask4 = timeoutTask2;
                        try {
                            if (!locallyScopedConn2.getCatalog().equals(statementImpl2.currentCatalog)) {
                                try {
                                    oldCatalog3 = locallyScopedConn2.getCatalog();
                                    locallyScopedConn2.setCatalog(statementImpl2.currentCatalog);
                                } catch (Throwable th6) {
                                    th4 = th6;
                                    cachedMetaData2 = null;
                                    oldCatalog = oldCatalog3;
                                    timeoutTask3 = timeoutTask4;
                                    firstStatementChar2 = firstStatementChar;
                                    cachedMetaData = th4;
                                    locallyScopedConn = locallyScopedConn2;
                                    timeoutTask = timeoutTask3;
                                    oldCatalog2 = oldCatalog;
                                    sql2 = sql3;
                                    statementImpl2.statementExecuting.set(false);
                                    if (timeoutTask != null) {
                                        timeoutTask.cancel();
                                        locallyScopedConn.getCancelTimer().purge();
                                    }
                                    if (oldCatalog2 != null) {
                                        locallyScopedConn.setCatalog(oldCatalog2);
                                    }
                                    throw cachedMetaData;
                                }
                            }
                            oldCatalog = oldCatalog3;
                            Field[] fieldArr = null;
                            try {
                                if (locallyScopedConn2.getCacheResultSetMetadata()) {
                                    try {
                                        cachedMetaData = locallyScopedConn2.getCachedMetaData(sql3);
                                        if (cachedMetaData != null) {
                                            fieldArr = cachedMetaData.fields;
                                        }
                                    } catch (Throwable th7) {
                                        th4 = th7;
                                        cachedMetaData2 = null;
                                        timeoutTask3 = timeoutTask4;
                                        firstStatementChar2 = firstStatementChar;
                                        cachedMetaData = th4;
                                        locallyScopedConn = locallyScopedConn2;
                                        timeoutTask = timeoutTask3;
                                        oldCatalog2 = oldCatalog;
                                        sql2 = sql3;
                                        statementImpl2.statementExecuting.set(false);
                                        if (timeoutTask != null) {
                                            timeoutTask.cancel();
                                            locallyScopedConn.getCancelTimer().purge();
                                        }
                                        if (oldCatalog2 != null) {
                                            locallyScopedConn.setCatalog(oldCatalog2);
                                        }
                                        throw cachedMetaData;
                                    }
                                }
                                cachedMetaData2 = cachedMetaData;
                                Field[] cachedFields = fieldArr;
                                try {
                                    locallyScopedConn2.setSessionMaxRows(statementImpl2.maxRows);
                                    statementBegins();
                                    timeoutTask3 = timeoutTask4;
                                    firstStatementChar2 = firstStatementChar;
                                    try {
                                        statementImpl2.results = locallyScopedConn2.execSQL(statementImpl2, sql3, statementImpl2.maxRows, null, statementImpl2.resultSetType, statementImpl2.resultSetConcurrency, createStreamingResultSet(), statementImpl2.currentCatalog, cachedFields);
                                        if (timeoutTask3 == null) {
                                            timeoutTask2 = timeoutTask3;
                                        } else if (timeoutTask3.caughtWhileCancelling != null) {
                                            throw timeoutTask3.caughtWhileCancelling;
                                        } else {
                                            timeoutTask3.cancel();
                                            locallyScopedConn2.getCancelTimer().purge();
                                            timeoutTask2 = null;
                                        }
                                        try {
                                            synchronized (statementImpl2.cancelTimeoutMutex) {
                                                if (statementImpl2.wasCancelled) {
                                                    SQLException cause;
                                                    if (statementImpl2.wasCancelledByTimeout) {
                                                        cause = new MySQLTimeoutException();
                                                    } else {
                                                        cause = new MySQLStatementCancelledException();
                                                    }
                                                    resetCancelledState();
                                                    throw cause;
                                                }
                                            }
                                            cachedMetaData = cachedMetaData2;
                                            oldCatalog3 = oldCatalog;
                                            str2 = sql3;
                                        } catch (Throwable th42) {
                                            cachedMetaData = th42;
                                            timeoutTask3 = timeoutTask2;
                                        }
                                    } catch (Throwable th8) {
                                        th42 = th8;
                                        cachedMetaData = th42;
                                        locallyScopedConn = locallyScopedConn2;
                                        timeoutTask = timeoutTask3;
                                        oldCatalog2 = oldCatalog;
                                        sql2 = sql3;
                                        statementImpl2.statementExecuting.set(false);
                                        if (timeoutTask != null) {
                                            timeoutTask.cancel();
                                            locallyScopedConn.getCancelTimer().purge();
                                        }
                                        if (oldCatalog2 != null) {
                                            locallyScopedConn.setCatalog(oldCatalog2);
                                        }
                                        throw cachedMetaData;
                                    }
                                } catch (Throwable th9) {
                                    th42 = th9;
                                    timeoutTask3 = timeoutTask4;
                                    firstStatementChar2 = firstStatementChar;
                                    cachedMetaData = th42;
                                    locallyScopedConn = locallyScopedConn2;
                                    timeoutTask = timeoutTask3;
                                    oldCatalog2 = oldCatalog;
                                    sql2 = sql3;
                                    statementImpl2.statementExecuting.set(false);
                                    if (timeoutTask != null) {
                                        timeoutTask.cancel();
                                        locallyScopedConn.getCancelTimer().purge();
                                    }
                                    if (oldCatalog2 != null) {
                                        locallyScopedConn.setCatalog(oldCatalog2);
                                    }
                                    throw cachedMetaData;
                                }
                            } catch (Throwable th10) {
                                th42 = th10;
                                timeoutTask3 = timeoutTask4;
                                firstStatementChar2 = firstStatementChar;
                                cachedMetaData2 = null;
                                cachedMetaData = th42;
                                locallyScopedConn = locallyScopedConn2;
                                timeoutTask = timeoutTask3;
                                oldCatalog2 = oldCatalog;
                                sql2 = sql3;
                                statementImpl2.statementExecuting.set(false);
                                if (timeoutTask != null) {
                                    timeoutTask.cancel();
                                    locallyScopedConn.getCancelTimer().purge();
                                }
                                if (oldCatalog2 != null) {
                                    locallyScopedConn.setCatalog(oldCatalog2);
                                }
                                throw cachedMetaData;
                            }
                        } catch (Throwable th11) {
                            th42 = th11;
                            timeoutTask3 = timeoutTask4;
                            firstStatementChar2 = firstStatementChar;
                            cachedMetaData2 = null;
                            oldCatalog = oldCatalog3;
                            cachedMetaData = th42;
                            locallyScopedConn = locallyScopedConn2;
                            timeoutTask = timeoutTask3;
                            oldCatalog2 = oldCatalog;
                            sql2 = sql3;
                            statementImpl2.statementExecuting.set(false);
                            if (timeoutTask != null) {
                                timeoutTask.cancel();
                                locallyScopedConn.getCancelTimer().purge();
                            }
                            if (oldCatalog2 != null) {
                                locallyScopedConn.setCatalog(oldCatalog2);
                            }
                            throw cachedMetaData;
                        }
                    } catch (Throwable th422) {
                        firstStatementChar2 = firstStatementChar;
                        cachedMetaData2 = null;
                        timeoutTask3 = null;
                        oldCatalog = oldCatalog3;
                        cachedMetaData = th422;
                        locallyScopedConn = locallyScopedConn2;
                        timeoutTask = timeoutTask3;
                        oldCatalog2 = oldCatalog;
                        sql2 = sql3;
                        statementImpl2.statementExecuting.set(false);
                        if (timeoutTask != null) {
                            timeoutTask.cancel();
                            locallyScopedConn.getCancelTimer().purge();
                        }
                        if (oldCatalog2 != null) {
                            locallyScopedConn.setCatalog(oldCatalog2);
                        }
                        throw cachedMetaData;
                    }
                    try {
                        statementImpl2.statementExecuting.set(false);
                        if (timeoutTask2 != null) {
                            timeoutTask2.cancel();
                            locallyScopedConn2.getCancelTimer().purge();
                        }
                        if (oldCatalog3 != null) {
                            locallyScopedConn2.setCatalog(oldCatalog3);
                        }
                        statementImpl2.lastInsertId = statementImpl2.results.getUpdateID();
                        if (cachedMetaData != null) {
                            locallyScopedConn2.initializeResultsMetadataFromCache(str2, cachedMetaData, statementImpl2.results);
                        } else if (statementImpl2.connection.getCacheResultSetMetadata()) {
                            locallyScopedConn2.initializeResultsMetadataFromCache(str2, null, statementImpl2.results);
                        }
                        ResultSet resultSet2 = statementImpl2.results;
                        return resultSet2;
                    } catch (Throwable th4222) {
                        th2 = th4222;
                        statementImpl = statementImpl2;
                        throw th2;
                    }
                } catch (Throwable th42222) {
                    th2 = th42222;
                    statementImpl = statementImpl2;
                    str2 = sql3;
                    throw th2;
                }
            } catch (Throwable th12) {
                th42222 = th12;
                str2 = str;
                statementImpl = statementImpl2;
                th2 = th42222;
                throw th2;
            }
        }
    }

    protected void doPingInstead() throws SQLException {
        synchronized (checkClosed().getConnectionMutex()) {
            if (this.pingTarget != null) {
                this.pingTarget.doPing();
            } else {
                this.connection.ping();
            }
            this.results = generatePingResultSet();
        }
    }

    protected ResultSetInternalMethods generatePingResultSet() throws SQLException {
        ResultSetInternalMethods resultSetInternalMethods;
        synchronized (checkClosed().getConnectionMutex()) {
            Field[] fields = new Field[]{new Field(null, "1", -5, 1)};
            ArrayList<ResultSetRow> rows = new ArrayList();
            byte[] colVal = new byte[]{(byte) 49};
            rows.add(new ByteArrayRow(new byte[][]{colVal}, getExceptionInterceptor()));
            resultSetInternalMethods = (ResultSetInternalMethods) DatabaseMetaData.buildResultSet(fields, rows, this.connection);
        }
        return resultSetInternalMethods;
    }

    protected void executeSimpleNonQuery(MySQLConnection c, String nonQuery) throws SQLException {
        c.execSQL(this, nonQuery, -1, null, 1003, 1007, false, this.currentCatalog, null, false).close();
    }

    public int executeUpdate(String sql) throws SQLException {
        return Util.truncateAndConvertToInt(executeLargeUpdate(sql));
    }

    /* JADX WARNING: inconsistent code. */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    protected long executeUpdateInternal(java.lang.String r26, boolean r27, boolean r28) throws java.sql.SQLException {
        /*
        r25 = this;
        r12 = r25;
        r1 = r26;
        r13 = r28;
        r2 = r25.checkClosed();
        r14 = r2.getConnectionMutex();
        monitor-enter(r14);
        r2 = r12.connection;	 Catch:{ all -> 0x0236 }
        r15 = r2;
        r25.checkNullOrEmptyQuery(r26);	 Catch:{ all -> 0x0236 }
        r25.resetCancelledState();	 Catch:{ all -> 0x0236 }
        r2 = findStartOfStatement(r26);	 Catch:{ all -> 0x0236 }
        r2 = com.mysql.jdbc.StringUtils.firstAlphaCharUc(r1, r2);	 Catch:{ all -> 0x0236 }
        r11 = r2;
        r12.retrieveGeneratedKeys = r13;	 Catch:{ all -> 0x0236 }
        r2 = 1;
        r10 = 0;
        if (r13 == 0) goto L_0x0038;
    L_0x0027:
        r3 = 73;
        if (r11 != r3) goto L_0x0038;
    L_0x002b:
        r3 = r25.containsOnDuplicateKeyInString(r26);	 Catch:{ all -> 0x0033 }
        if (r3 == 0) goto L_0x0038;
    L_0x0031:
        r3 = r2;
        goto L_0x0039;
    L_0x0033:
        r0 = move-exception;
        r6 = r27;
        goto L_0x023b;
    L_0x0038:
        r3 = r10;
    L_0x0039:
        r12.lastQueryIsOnDupKeyUpdate = r3;	 Catch:{ all -> 0x0236 }
        r16 = 0;
        r3 = r12.doEscapeProcessing;	 Catch:{ all -> 0x0236 }
        if (r3 == 0) goto L_0x005c;
    L_0x0041:
        r3 = r12.connection;	 Catch:{ all -> 0x0033 }
        r3 = r3.serverSupportsConvertFn();	 Catch:{ all -> 0x0033 }
        r4 = r12.connection;	 Catch:{ all -> 0x0033 }
        r3 = com.mysql.jdbc.EscapeProcessor.escapeSQL(r1, r3, r4);	 Catch:{ all -> 0x0033 }
        r4 = r3 instanceof java.lang.String;	 Catch:{ all -> 0x0033 }
        if (r4 == 0) goto L_0x0056;
    L_0x0051:
        r4 = r3;
        r4 = (java.lang.String) r4;	 Catch:{ all -> 0x0033 }
        r1 = r4;
        goto L_0x005c;
    L_0x0056:
        r4 = r3;
        r4 = (com.mysql.jdbc.EscapeProcessorResult) r4;	 Catch:{ all -> 0x0033 }
        r4 = r4.escapedSql;	 Catch:{ all -> 0x0033 }
        r1 = r4;
    L_0x005c:
        r9 = r1;
        r1 = r15.isReadOnly(r10);	 Catch:{ all -> 0x022c }
        if (r1 == 0) goto L_0x0090;
    L_0x0063:
        r1 = new java.lang.StringBuilder;	 Catch:{ all -> 0x0089 }
        r1.<init>();	 Catch:{ all -> 0x0089 }
        r2 = "Statement.42";
        r2 = com.mysql.jdbc.Messages.getString(r2);	 Catch:{ all -> 0x0089 }
        r1.append(r2);	 Catch:{ all -> 0x0089 }
        r2 = "Statement.43";
        r2 = com.mysql.jdbc.Messages.getString(r2);	 Catch:{ all -> 0x0089 }
        r1.append(r2);	 Catch:{ all -> 0x0089 }
        r1 = r1.toString();	 Catch:{ all -> 0x0089 }
        r2 = "S1009";
        r3 = r25.getExceptionInterceptor();	 Catch:{ all -> 0x0089 }
        r1 = com.mysql.jdbc.SQLError.createSQLException(r1, r2, r3);	 Catch:{ all -> 0x0089 }
        throw r1;	 Catch:{ all -> 0x0089 }
    L_0x0089:
        r0 = move-exception;
        r6 = r27;
        r1 = r0;
        r10 = r12;
        goto L_0x023e;
    L_0x0090:
        r1 = "select";
        r1 = com.mysql.jdbc.StringUtils.startsWithIgnoreCaseAndWs(r9, r1);	 Catch:{ all -> 0x022c }
        if (r1 == 0) goto L_0x00a9;
    L_0x0098:
        r1 = "Statement.46";
        r1 = com.mysql.jdbc.Messages.getString(r1);	 Catch:{ all -> 0x0089 }
        r2 = "01S03";
        r3 = r25.getExceptionInterceptor();	 Catch:{ all -> 0x0089 }
        r1 = com.mysql.jdbc.SQLError.createSQLException(r1, r2, r3);	 Catch:{ all -> 0x0089 }
        throw r1;	 Catch:{ all -> 0x0089 }
    L_0x00a9:
        r25.implicitlyCloseAllOpenResults();	 Catch:{ all -> 0x022c }
        r1 = 0;
        r3 = 0;
        r4 = r15.isReadInfoMsgEnabled();	 Catch:{ all -> 0x022c }
        r8 = r4;
        if (r13 == 0) goto L_0x00bc;
    L_0x00b5:
        r4 = 82;
        if (r11 != r4) goto L_0x00bc;
    L_0x00b9:
        r15.setReadInfoMsgEnabled(r2);	 Catch:{ all -> 0x0089 }
    L_0x00bc:
        r2 = r15.getEnableQueryTimeouts();	 Catch:{ all -> 0x01f9 }
        if (r2 == 0) goto L_0x00e8;
    L_0x00c2:
        r2 = r12.timeoutInMillis;	 Catch:{ all -> 0x00de }
        if (r2 == 0) goto L_0x00e8;
    L_0x00c6:
        r2 = 5;
        r2 = r15.versionMeetsMinimum(r2, r10, r10);	 Catch:{ all -> 0x00de }
        if (r2 == 0) goto L_0x00e8;
    L_0x00cd:
        r2 = new com.mysql.jdbc.StatementImpl$CancelTask;	 Catch:{ all -> 0x00de }
        r2.<init>(r12);	 Catch:{ all -> 0x00de }
        r1 = r2;
        r2 = r15.getCancelTimer();	 Catch:{ all -> 0x00de }
        r4 = r12.timeoutInMillis;	 Catch:{ all -> 0x00de }
        r4 = (long) r4;	 Catch:{ all -> 0x00de }
        r2.schedule(r1, r4);	 Catch:{ all -> 0x00de }
        goto L_0x00e8;
    L_0x00de:
        r0 = move-exception;
        r2 = r1;
        r5 = r8;
        r18 = r9;
        r8 = r10;
        r13 = r11;
    L_0x00e5:
        r1 = r0;
        goto L_0x0201;
    L_0x00e8:
        r7 = r1;
        r1 = r15.getCatalog();	 Catch:{ all -> 0x01f0 }
        r2 = r12.currentCatalog;	 Catch:{ all -> 0x01f0 }
        r1 = r1.equals(r2);	 Catch:{ all -> 0x01f0 }
        if (r1 != 0) goto L_0x010a;
    L_0x00f5:
        r1 = r15.getCatalog();	 Catch:{ all -> 0x0100 }
        r3 = r1;
        r1 = r12.currentCatalog;	 Catch:{ all -> 0x0100 }
        r15.setCatalog(r1);	 Catch:{ all -> 0x0100 }
        goto L_0x010a;
    L_0x0100:
        r0 = move-exception;
        r1 = r0;
        r2 = r7;
        r5 = r8;
        r18 = r9;
        r8 = r10;
        r13 = r11;
        goto L_0x0201;
    L_0x010a:
        r17 = r3;
        r1 = -1;
        r15.setSessionMaxRows(r1);	 Catch:{ all -> 0x01e5 }
        r25.statementBegins();	 Catch:{ all -> 0x01e5 }
        r4 = -1;
        r5 = 0;
        r6 = 1003; // 0x3eb float:1.406E-42 double:4.955E-321;
        r18 = 1007; // 0x3ef float:1.411E-42 double:4.975E-321;
        r19 = 0;
        r3 = r12.currentCatalog;	 Catch:{ all -> 0x01e5 }
        r20 = 0;
        r1 = r15;
        r2 = r12;
        r21 = r3;
        r3 = r9;
        r22 = r7;
        r7 = r18;
        r23 = r8;
        r8 = r19;
        r18 = r9;
        r9 = r21;
        r10 = r20;
        r13 = r11;
        r11 = r27;
        r1 = r1.execSQL(r2, r3, r4, r5, r6, r7, r8, r9, r10, r11);	 Catch:{ all -> 0x01db }
        r2 = r22;
        if (r2 == 0) goto L_0x0159;
    L_0x013d:
        r3 = r2.caughtWhileCancelling;	 Catch:{ all -> 0x0150 }
        if (r3 == 0) goto L_0x0144;
    L_0x0141:
        r3 = r2.caughtWhileCancelling;	 Catch:{ all -> 0x0150 }
        throw r3;	 Catch:{ all -> 0x0150 }
    L_0x0144:
        r2.cancel();	 Catch:{ all -> 0x0150 }
        r3 = r15.getCancelTimer();	 Catch:{ all -> 0x0150 }
        r3.purge();	 Catch:{ all -> 0x0150 }
        r2 = 0;
        goto L_0x0159;
    L_0x0150:
        r0 = move-exception;
        r16 = r1;
        r3 = r17;
        r5 = r23;
        r8 = 0;
        goto L_0x00e5;
    L_0x0159:
        r3 = r12.cancelTimeoutMutex;	 Catch:{ all -> 0x01d1 }
        monitor-enter(r3);	 Catch:{ all -> 0x01d1 }
        r4 = r12.wasCancelled;	 Catch:{ all -> 0x01c0 }
        if (r4 == 0) goto L_0x017c;
    L_0x0160:
        r4 = 0;
        r5 = r12.wasCancelledByTimeout;	 Catch:{ all -> 0x0176 }
        if (r5 == 0) goto L_0x016c;
    L_0x0165:
        r5 = new com.mysql.jdbc.exceptions.MySQLTimeoutException;	 Catch:{ all -> 0x0176 }
        r5.<init>();	 Catch:{ all -> 0x0176 }
        r4 = r5;
        goto L_0x0172;
    L_0x016c:
        r5 = new com.mysql.jdbc.exceptions.MySQLStatementCancelledException;	 Catch:{ all -> 0x0176 }
        r5.<init>();	 Catch:{ all -> 0x0176 }
        r4 = r5;
    L_0x0172:
        r25.resetCancelledState();	 Catch:{ all -> 0x0176 }
        throw r4;	 Catch:{ all -> 0x0176 }
    L_0x0176:
        r0 = move-exception;
        r4 = r0;
        r5 = r23;
        r8 = 0;
        goto L_0x01c5;
    L_0x017c:
        monitor-exit(r3);	 Catch:{ all -> 0x01c0 }
        r3 = r17;
        r4 = r18;
        r5 = r23;
        r15.setReadInfoMsgEnabled(r5);	 Catch:{ all -> 0x01b6 }
        if (r2 == 0) goto L_0x0193;
    L_0x0189:
        r2.cancel();	 Catch:{ all -> 0x01b6 }
        r6 = r15.getCancelTimer();	 Catch:{ all -> 0x01b6 }
        r6.purge();	 Catch:{ all -> 0x01b6 }
    L_0x0193:
        if (r3 == 0) goto L_0x0198;
    L_0x0195:
        r15.setCatalog(r3);	 Catch:{ all -> 0x01b6 }
    L_0x0198:
        if (r27 != 0) goto L_0x01a0;
    L_0x019a:
        r7 = r12.statementExecuting;	 Catch:{ all -> 0x01b6 }
        r8 = 0;
        r7.set(r8);	 Catch:{ all -> 0x01b6 }
        r12.results = r1;	 Catch:{ all -> 0x01b6 }
        r1.setFirstCharOfQuery(r13);	 Catch:{ all -> 0x01b6 }
        r7 = r1.getUpdateCount();	 Catch:{ all -> 0x01b6 }
        r12.updateCount = r7;	 Catch:{ all -> 0x01b6 }
        r7 = r1.getUpdateID();	 Catch:{ all -> 0x01b6 }
        r12.lastInsertId = r7;	 Catch:{ all -> 0x01b6 }
        r7 = r12.updateCount;	 Catch:{ all -> 0x01b6 }
        monitor-exit(r14);	 Catch:{ all -> 0x01b6 }
        return r7;
    L_0x01b6:
        r0 = move-exception;
        r6 = r27;
        r1 = r0;
        r9 = r4;
        r10 = r12;
        r13 = r28;
        goto L_0x023e;
    L_0x01c0:
        r0 = move-exception;
        r5 = r23;
        r8 = 0;
        r4 = r0;
    L_0x01c5:
        monitor-exit(r3);	 Catch:{ all -> 0x01ce }
        throw r4;	 Catch:{ all -> 0x01c7 }
    L_0x01c7:
        r0 = move-exception;
        r16 = r1;
        r3 = r17;
        goto L_0x00e5;
    L_0x01ce:
        r0 = move-exception;
        r4 = r0;
        goto L_0x01c5;
    L_0x01d1:
        r0 = move-exception;
        r5 = r23;
        r8 = 0;
        r16 = r1;
        r3 = r17;
        r1 = r0;
        goto L_0x0201;
    L_0x01db:
        r0 = move-exception;
        r2 = r22;
        r5 = r23;
        r8 = 0;
        r1 = r0;
        r3 = r17;
        goto L_0x0201;
    L_0x01e5:
        r0 = move-exception;
        r2 = r7;
        r5 = r8;
        r18 = r9;
        r8 = r10;
        r13 = r11;
        r1 = r0;
        r3 = r17;
        goto L_0x01f8;
    L_0x01f0:
        r0 = move-exception;
        r2 = r7;
        r5 = r8;
        r18 = r9;
        r8 = r10;
        r13 = r11;
        r1 = r0;
    L_0x01f8:
        goto L_0x0201;
    L_0x01f9:
        r0 = move-exception;
        r5 = r8;
        r18 = r9;
        r8 = r10;
        r13 = r11;
        r2 = r1;
        r1 = r0;
    L_0x0201:
        r4 = r15;
        r7 = r13;
        r9 = r16;
        r10 = r12;
        r11 = r18;
        r6 = r27;
        r13 = r28;
        r4.setReadInfoMsgEnabled(r5);	 Catch:{ all -> 0x0228 }
        if (r2 == 0) goto L_0x021b;
    L_0x0211:
        r2.cancel();	 Catch:{ all -> 0x0228 }
        r15 = r4.getCancelTimer();	 Catch:{ all -> 0x0228 }
        r15.purge();	 Catch:{ all -> 0x0228 }
    L_0x021b:
        if (r3 == 0) goto L_0x0220;
    L_0x021d:
        r4.setCatalog(r3);	 Catch:{ all -> 0x0228 }
    L_0x0220:
        if (r6 != 0) goto L_0x0227;
    L_0x0222:
        r15 = r10.statementExecuting;	 Catch:{ all -> 0x0228 }
        r15.set(r8);	 Catch:{ all -> 0x0228 }
    L_0x0227:
        throw r1;	 Catch:{ all -> 0x0228 }
    L_0x0228:
        r0 = move-exception;
        r1 = r0;
        r9 = r11;
        goto L_0x023e;
    L_0x022c:
        r0 = move-exception;
        r18 = r9;
        r6 = r27;
        r13 = r28;
        r1 = r0;
        r10 = r12;
        goto L_0x023e;
    L_0x0236:
        r0 = move-exception;
        r6 = r27;
        r13 = r28;
    L_0x023b:
        r9 = r1;
        r10 = r12;
    L_0x023d:
        r1 = r0;
    L_0x023e:
        monitor-exit(r14);	 Catch:{ all -> 0x0240 }
        throw r1;
    L_0x0240:
        r0 = move-exception;
        goto L_0x023d;
        */
        throw new UnsupportedOperationException("Method not decompiled: com.mysql.jdbc.StatementImpl.executeUpdateInternal(java.lang.String, boolean, boolean):long");
    }

    public int executeUpdate(String sql, int autoGeneratedKeys) throws SQLException {
        return Util.truncateAndConvertToInt(executeLargeUpdate(sql, autoGeneratedKeys));
    }

    public int executeUpdate(String sql, int[] columnIndexes) throws SQLException {
        return Util.truncateAndConvertToInt(executeLargeUpdate(sql, columnIndexes));
    }

    public int executeUpdate(String sql, String[] columnNames) throws SQLException {
        return Util.truncateAndConvertToInt(executeLargeUpdate(sql, columnNames));
    }

    protected Calendar getCalendarInstanceForSessionOrNew() throws SQLException {
        synchronized (checkClosed().getConnectionMutex()) {
            if (this.connection != null) {
                Calendar calendarInstanceForSessionOrNew = this.connection.getCalendarInstanceForSessionOrNew();
                return calendarInstanceForSessionOrNew;
            }
            calendarInstanceForSessionOrNew = new GregorianCalendar();
            return calendarInstanceForSessionOrNew;
        }
    }

    public Connection getConnection() throws SQLException {
        Connection connection;
        synchronized (checkClosed().getConnectionMutex()) {
            connection = this.connection;
        }
        return connection;
    }

    public int getFetchDirection() throws SQLException {
        return 1000;
    }

    public int getFetchSize() throws SQLException {
        int i;
        synchronized (checkClosed().getConnectionMutex()) {
            i = this.fetchSize;
        }
        return i;
    }

    public ResultSet getGeneratedKeys() throws SQLException {
        synchronized (checkClosed().getConnectionMutex()) {
            if (!this.retrieveGeneratedKeys) {
                throw SQLError.createSQLException(Messages.getString("Statement.GeneratedKeysNotRequested"), SQLError.SQL_STATE_ILLEGAL_ARGUMENT, getExceptionInterceptor());
            } else if (this.batchedGeneratedKeys != null) {
                Field[] fields = new Field[]{new Field("", "GENERATED_KEY", -5, 20)};
                fields[0].setConnection(this.connection);
                this.generatedKeysResults = ResultSetImpl.getInstance(this.currentCatalog, fields, new RowDataStatic(this.batchedGeneratedKeys), this.connection, this, false);
                ResultSet resultSet = this.generatedKeysResults;
                return resultSet;
            } else if (this.lastQueryIsOnDupKeyUpdate) {
                r1 = getGeneratedKeysInternal(1);
                this.generatedKeysResults = r1;
                return r1;
            } else {
                r1 = getGeneratedKeysInternal();
                this.generatedKeysResults = r1;
                return r1;
            }
        }
    }

    protected ResultSetInternalMethods getGeneratedKeysInternal() throws SQLException {
        return getGeneratedKeysInternal(getLargeUpdateCount());
    }

    protected ResultSetInternalMethods getGeneratedKeysInternal(long numKeys) throws SQLException {
        StatementImpl statementImpl = this;
        synchronized (checkClosed().getConnectionMutex()) {
            int i = 1;
            long numKeys2;
            try {
                ResultSetImpl gkRs;
                Field[] fields = new Field[1];
                int i2 = 0;
                fields[0] = new Field("", "GENERATED_KEY", -5, 20);
                fields[0].setConnection(statementImpl.connection);
                fields[0].setUseOldNameMetadata(true);
                ArrayList<ResultSetRow> rowSet = new ArrayList();
                long beginAt = getLastInsertID();
                if (beginAt < 0) {
                    fields[0].setUnsigned();
                }
                if (statementImpl.results != null) {
                    String serverInfo = statementImpl.results.getServerInfo();
                    if (numKeys <= 0 || statementImpl.results.getFirstCharOfQuery() != 'R' || serverInfo == null || serverInfo.length() <= 0) {
                        numKeys2 = numKeys;
                    } else {
                        numKeys2 = getRecordCountFromInfo(serverInfo);
                    }
                    if (beginAt != 0 && numKeys2 > 0) {
                        long beginAt2 = beginAt;
                        int i3 = 0;
                        while (((long) i3) < numKeys2) {
                            String serverInfo2;
                            int i4;
                            byte[][] row = new byte[i][];
                            if (beginAt2 > 0) {
                                row[i2] = StringUtils.getBytes(Long.toString(beginAt2));
                                serverInfo2 = serverInfo;
                                i4 = i3;
                                serverInfo = i;
                                i3 = i2;
                            } else {
                                asBytes = new byte[8];
                                i4 = i3;
                                asBytes[7] = (byte) ((int) (beginAt2 & 255));
                                serverInfo2 = serverInfo;
                                asBytes[6] = (byte) ((int) (beginAt2 >>> 8));
                                asBytes[5] = (byte) ((int) (beginAt2 >>> 16));
                                asBytes[4] = (byte) ((int) (beginAt2 >>> 24));
                                asBytes[3] = (byte) ((int) (beginAt2 >>> 32));
                                asBytes[2] = (byte) ((int) (beginAt2 >>> 40));
                                asBytes[1] = (byte) ((int) (beginAt2 >>> 48));
                                asBytes[0] = (byte) ((int) (beginAt2 >>> 56));
                                i3 = 0;
                                row[0] = new BigInteger(1, asBytes).toString().getBytes();
                            }
                            rowSet.add(new ByteArrayRow(row, getExceptionInterceptor()));
                            i2 = i3;
                            beginAt2 += (long) statementImpl.connection.getAutoIncrementIncrement();
                            serverInfo = serverInfo2;
                            i3 = i4 + 1;
                            i = 1;
                        }
                        gkRs = ResultSetImpl.getInstance(statementImpl.currentCatalog, fields, new RowDataStatic(rowSet), statementImpl.connection, statementImpl, false);
                        return gkRs;
                    }
                }
                gkRs = ResultSetImpl.getInstance(statementImpl.currentCatalog, fields, new RowDataStatic(rowSet), statementImpl.connection, statementImpl, false);
                return gkRs;
            } catch (Throwable th) {
                th = th;
            }
        }
        Throwable th2;
        Throwable th3 = th2;
        throw th3;
    }

    protected int getId() {
        return this.statementId;
    }

    public long getLastInsertID() {
        try {
            long j;
            synchronized (checkClosed().getConnectionMutex()) {
                j = this.lastInsertId;
            }
            return j;
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    public long getLongUpdateCount() {
        try {
            synchronized (checkClosed().getConnectionMutex()) {
                if (this.results == null) {
                    return -1;
                } else if (this.results.reallyResult()) {
                    return -1;
                } else {
                    long j = this.updateCount;
                    return j;
                }
            }
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    public int getMaxFieldSize() throws SQLException {
        int i;
        synchronized (checkClosed().getConnectionMutex()) {
            i = this.maxFieldSize;
        }
        return i;
    }

    public int getMaxRows() throws SQLException {
        synchronized (checkClosed().getConnectionMutex()) {
            if (this.maxRows <= 0) {
                return 0;
            }
            int i = this.maxRows;
            return i;
        }
    }

    public boolean getMoreResults() throws SQLException {
        return getMoreResults(1);
    }

    /* JADX WARNING: inconsistent code. */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    public boolean getMoreResults(int r8) throws java.sql.SQLException {
        /*
        r7 = this;
        r0 = r7.checkClosed();
        r0 = r0.getConnectionMutex();
        monitor-enter(r0);
        r1 = r7.results;	 Catch:{ all -> 0x00cb }
        r2 = 0;
        if (r1 != 0) goto L_0x0010;
    L_0x000e:
        monitor-exit(r0);	 Catch:{ all -> 0x00cb }
        return r2;
    L_0x0010:
        r1 = r7.createStreamingResultSet();	 Catch:{ all -> 0x00cb }
        if (r1 == 0) goto L_0x0027;
    L_0x0016:
        r3 = r7.results;	 Catch:{ all -> 0x00cb }
        r3 = r3.reallyResult();	 Catch:{ all -> 0x00cb }
        if (r3 == 0) goto L_0x0027;
    L_0x001e:
        r3 = r7.results;	 Catch:{ all -> 0x00cb }
        r3 = r3.next();	 Catch:{ all -> 0x00cb }
        if (r3 == 0) goto L_0x0027;
    L_0x0026:
        goto L_0x001e;
    L_0x0027:
        r3 = r7.results;	 Catch:{ all -> 0x00cb }
        r3 = r3.getNextResultSet();	 Catch:{ all -> 0x00cb }
        switch(r8) {
            case 1: goto L_0x0065;
            case 2: goto L_0x0050;
            case 3: goto L_0x0034;
            default: goto L_0x0030;
        };	 Catch:{ all -> 0x00cb }
    L_0x0030:
        r2 = "Statement.19";
        goto L_0x00bc;
    L_0x0034:
        r4 = r7.results;	 Catch:{ all -> 0x00cb }
        if (r4 == 0) goto L_0x004c;
    L_0x0038:
        if (r1 != 0) goto L_0x0047;
    L_0x003a:
        r4 = r7.connection;	 Catch:{ all -> 0x00cb }
        r4 = r4.getDontTrackOpenResources();	 Catch:{ all -> 0x00cb }
        if (r4 != 0) goto L_0x0047;
    L_0x0042:
        r4 = r7.results;	 Catch:{ all -> 0x00cb }
        r4.realClose(r2);	 Catch:{ all -> 0x00cb }
    L_0x0047:
        r4 = r7.results;	 Catch:{ all -> 0x00cb }
        r4.clearNextResult();	 Catch:{ all -> 0x00cb }
    L_0x004c:
        r7.closeAllOpenResults();	 Catch:{ all -> 0x00cb }
        goto L_0x007d;
    L_0x0050:
        r4 = r7.connection;	 Catch:{ all -> 0x00cb }
        r4 = r4.getDontTrackOpenResources();	 Catch:{ all -> 0x00cb }
        if (r4 != 0) goto L_0x005f;
    L_0x0058:
        r4 = r7.openResults;	 Catch:{ all -> 0x00cb }
        r5 = r7.results;	 Catch:{ all -> 0x00cb }
        r4.add(r5);	 Catch:{ all -> 0x00cb }
    L_0x005f:
        r4 = r7.results;	 Catch:{ all -> 0x00cb }
        r4.clearNextResult();	 Catch:{ all -> 0x00cb }
        goto L_0x007d;
    L_0x0065:
        r4 = r7.results;	 Catch:{ all -> 0x00cb }
        if (r4 == 0) goto L_0x007d;
    L_0x0069:
        if (r1 != 0) goto L_0x0078;
    L_0x006b:
        r4 = r7.connection;	 Catch:{ all -> 0x00cb }
        r4 = r4.getDontTrackOpenResources();	 Catch:{ all -> 0x00cb }
        if (r4 != 0) goto L_0x0078;
    L_0x0073:
        r4 = r7.results;	 Catch:{ all -> 0x00cb }
        r4.realClose(r2);	 Catch:{ all -> 0x00cb }
    L_0x0078:
        r4 = r7.results;	 Catch:{ all -> 0x00cb }
        r4.clearNextResult();	 Catch:{ all -> 0x00cb }
    L_0x007d:
        r7.results = r3;	 Catch:{ all -> 0x00cb }
        r4 = r7.results;	 Catch:{ all -> 0x00cb }
        r5 = -1;
        if (r4 != 0) goto L_0x008a;
    L_0x0085:
        r7.updateCount = r5;	 Catch:{ all -> 0x00cb }
        r7.lastInsertId = r5;	 Catch:{ all -> 0x00cb }
        goto L_0x00a7;
    L_0x008a:
        r4 = r7.results;	 Catch:{ all -> 0x00cb }
        r4 = r4.reallyResult();	 Catch:{ all -> 0x00cb }
        if (r4 == 0) goto L_0x0097;
    L_0x0092:
        r7.updateCount = r5;	 Catch:{ all -> 0x00cb }
        r7.lastInsertId = r5;	 Catch:{ all -> 0x00cb }
        goto L_0x00a7;
    L_0x0097:
        r4 = r7.results;	 Catch:{ all -> 0x00cb }
        r4 = r4.getUpdateCount();	 Catch:{ all -> 0x00cb }
        r7.updateCount = r4;	 Catch:{ all -> 0x00cb }
        r4 = r7.results;	 Catch:{ all -> 0x00cb }
        r4 = r4.getUpdateID();	 Catch:{ all -> 0x00cb }
        r7.lastInsertId = r4;	 Catch:{ all -> 0x00cb }
    L_0x00a7:
        r4 = r7.results;	 Catch:{ all -> 0x00cb }
        if (r4 == 0) goto L_0x00b5;
    L_0x00ab:
        r4 = r7.results;	 Catch:{ all -> 0x00cb }
        r4 = r4.reallyResult();	 Catch:{ all -> 0x00cb }
        if (r4 == 0) goto L_0x00b5;
    L_0x00b3:
        r2 = 1;
    L_0x00b5:
        if (r2 != 0) goto L_0x00ba;
    L_0x00b7:
        r7.checkAndPerformCloseOnCompletionAction();	 Catch:{ all -> 0x00cb }
    L_0x00ba:
        monitor-exit(r0);	 Catch:{ all -> 0x00cb }
        return r2;
    L_0x00bc:
        r2 = com.mysql.jdbc.Messages.getString(r2);	 Catch:{ all -> 0x00cb }
        r4 = "S1009";
        r5 = r7.getExceptionInterceptor();	 Catch:{ all -> 0x00cb }
        r2 = com.mysql.jdbc.SQLError.createSQLException(r2, r4, r5);	 Catch:{ all -> 0x00cb }
        throw r2;	 Catch:{ all -> 0x00cb }
    L_0x00cb:
        r1 = move-exception;
        monitor-exit(r0);	 Catch:{ all -> 0x00cb }
        throw r1;
        */
        throw new UnsupportedOperationException("Method not decompiled: com.mysql.jdbc.StatementImpl.getMoreResults(int):boolean");
    }

    public int getQueryTimeout() throws SQLException {
        int i;
        synchronized (checkClosed().getConnectionMutex()) {
            i = this.timeoutInMillis / 1000;
        }
        return i;
    }

    private long getRecordCountFromInfo(String serverInfo) {
        StringBuilder recordsBuf = new StringBuilder();
        char c = '\u0000';
        int length = serverInfo.length();
        int i = 0;
        while (i < length) {
            c = serverInfo.charAt(i);
            if (Character.isDigit(c)) {
                break;
            }
            i++;
        }
        recordsBuf.append(c);
        i++;
        while (i < length) {
            c = serverInfo.charAt(i);
            if (!Character.isDigit(c)) {
                break;
            }
            recordsBuf.append(c);
            i++;
        }
        long recordsCount = Long.parseLong(recordsBuf.toString());
        StringBuilder duplicatesBuf = new StringBuilder();
        while (i < length) {
            c = serverInfo.charAt(i);
            if (Character.isDigit(c)) {
                break;
            }
            i++;
        }
        duplicatesBuf.append(c);
        for (i++; i < length; i++) {
            c = serverInfo.charAt(i);
            if (!Character.isDigit(c)) {
                break;
            }
            duplicatesBuf.append(c);
        }
        return recordsCount - Long.parseLong(duplicatesBuf.toString());
    }

    public ResultSet getResultSet() throws SQLException {
        ResultSet resultSet;
        synchronized (checkClosed().getConnectionMutex()) {
            resultSet = (this.results == null || !this.results.reallyResult()) ? null : this.results;
        }
        return resultSet;
    }

    public int getResultSetConcurrency() throws SQLException {
        int i;
        synchronized (checkClosed().getConnectionMutex()) {
            i = this.resultSetConcurrency;
        }
        return i;
    }

    public int getResultSetHoldability() throws SQLException {
        return 1;
    }

    protected ResultSetInternalMethods getResultSetInternal() {
        try {
            ResultSetInternalMethods resultSetInternalMethods;
            synchronized (checkClosed().getConnectionMutex()) {
                resultSetInternalMethods = this.results;
            }
            return resultSetInternalMethods;
        } catch (SQLException e) {
            return this.results;
        }
    }

    public int getResultSetType() throws SQLException {
        int i;
        synchronized (checkClosed().getConnectionMutex()) {
            i = this.resultSetType;
        }
        return i;
    }

    public int getUpdateCount() throws SQLException {
        return Util.truncateAndConvertToInt(getLargeUpdateCount());
    }

    public SQLWarning getWarnings() throws SQLException {
        synchronized (checkClosed().getConnectionMutex()) {
            if (this.clearWarningsCalled) {
                return null;
            } else if (this.connection.versionMeetsMinimum(4, 1, 0)) {
                pendingWarningsFromServer = SQLError.convertShowWarningsToSQLWarnings(this.connection);
                if (this.warningChain != null) {
                    this.warningChain.setNextWarning(pendingWarningsFromServer);
                } else {
                    this.warningChain = pendingWarningsFromServer;
                }
                SQLWarning sQLWarning = this.warningChain;
                return sQLWarning;
            } else {
                pendingWarningsFromServer = this.warningChain;
                return pendingWarningsFromServer;
            }
        }
    }

    protected void realClose(boolean calledExplicitly, boolean closeOpenResults) throws SQLException {
        MySQLConnection locallyScopedConn = this.connection;
        if (locallyScopedConn != null) {
            if (!r1.isClosed) {
                boolean closeOpenResults2;
                if (!locallyScopedConn.getDontTrackOpenResources()) {
                    locallyScopedConn.unregisterStatement(r1);
                }
                if (r1.useUsageAdvisor && !calledExplicitly) {
                    String message = new StringBuilder();
                    message.append(Messages.getString("Statement.63"));
                    message.append(Messages.getString("Statement.64"));
                    message = message.toString();
                    ProfilerEventHandler profilerEventHandler = r1.eventSink;
                    ProfilerEvent profilerEvent = r6;
                    ProfilerEvent profilerEvent2 = new ProfilerEvent((byte) 0, "", r1.currentCatalog, r1.connectionId, getId(), -1, System.currentTimeMillis(), 0, Constants.MILLIS_I18N, null, r1.pointOfOrigin, message);
                    profilerEventHandler.consumeEvent(profilerEvent);
                }
                if (closeOpenResults) {
                    boolean z = (r1.holdResultsOpenOverClose || r1.connection.getDontTrackOpenResources()) ? false : true;
                    closeOpenResults2 = z;
                } else {
                    closeOpenResults2 = closeOpenResults;
                }
                if (closeOpenResults2) {
                    if (r1.results != null) {
                        try {
                            r1.results.close();
                        } catch (Exception e) {
                        }
                    }
                    if (r1.generatedKeysResults != null) {
                        try {
                            r1.generatedKeysResults.close();
                        } catch (Exception e2) {
                        }
                    }
                    closeAllOpenResults();
                }
                r1.isClosed = true;
                r1.results = null;
                r1.generatedKeysResults = null;
                r1.connection = null;
                r1.warningChain = null;
                r1.openResults = null;
                r1.batchedGeneratedKeys = null;
                r1.localInfileInputStream = null;
                r1.pingTarget = null;
            }
        }
    }

    public void setCursorName(String name) throws SQLException {
    }

    public void setEscapeProcessing(boolean enable) throws SQLException {
        synchronized (checkClosed().getConnectionMutex()) {
            this.doEscapeProcessing = enable;
        }
    }

    public void setFetchDirection(int direction) throws SQLException {
        switch (direction) {
            case 1000:
            case 1001:
            case 1002:
                return;
            default:
                throw SQLError.createSQLException(Messages.getString("Statement.5"), SQLError.SQL_STATE_ILLEGAL_ARGUMENT, getExceptionInterceptor());
        }
    }

    public void setFetchSize(int rows) throws SQLException {
        synchronized (checkClosed().getConnectionMutex()) {
            if (rows >= 0 || rows == Integer.MIN_VALUE) {
                if (this.maxRows <= 0 || rows <= getMaxRows()) {
                    this.fetchSize = rows;
                }
            }
            throw SQLError.createSQLException(Messages.getString("Statement.7"), SQLError.SQL_STATE_ILLEGAL_ARGUMENT, getExceptionInterceptor());
        }
    }

    public void setHoldResultsOpenOverClose(boolean holdResultsOpenOverClose) {
        try {
            synchronized (checkClosed().getConnectionMutex()) {
                this.holdResultsOpenOverClose = holdResultsOpenOverClose;
            }
        } catch (SQLException e) {
        }
    }

    public void setMaxFieldSize(int max) throws SQLException {
        synchronized (checkClosed().getConnectionMutex()) {
            if (max < 0) {
                throw SQLError.createSQLException(Messages.getString("Statement.11"), SQLError.SQL_STATE_ILLEGAL_ARGUMENT, getExceptionInterceptor());
            }
            if (max > (this.connection != null ? this.connection.getMaxAllowedPacket() : MysqlIO.getMaxBuf())) {
                throw SQLError.createSQLException(Messages.getString("Statement.13", new Object[]{Long.valueOf((long) (this.connection != null ? this.connection.getMaxAllowedPacket() : MysqlIO.getMaxBuf()))}), SQLError.SQL_STATE_ILLEGAL_ARGUMENT, getExceptionInterceptor());
            }
            this.maxFieldSize = max;
        }
    }

    public void setMaxRows(int max) throws SQLException {
        setLargeMaxRows((long) max);
    }

    public void setQueryTimeout(int seconds) throws SQLException {
        synchronized (checkClosed().getConnectionMutex()) {
            if (seconds < 0) {
                throw SQLError.createSQLException(Messages.getString("Statement.21"), SQLError.SQL_STATE_ILLEGAL_ARGUMENT, getExceptionInterceptor());
            }
            this.timeoutInMillis = seconds * 1000;
        }
    }

    void setResultSetConcurrency(int concurrencyFlag) {
        try {
            synchronized (checkClosed().getConnectionMutex()) {
                this.resultSetConcurrency = concurrencyFlag;
            }
        } catch (SQLException e) {
        }
    }

    void setResultSetType(int typeFlag) {
        try {
            synchronized (checkClosed().getConnectionMutex()) {
                this.resultSetType = typeFlag;
            }
        } catch (SQLException e) {
        }
    }

    protected void getBatchedGeneratedKeys(Statement batchedStatement) throws SQLException {
        StatementImpl this;
        Throwable th;
        synchronized (checkClosed().getConnectionMutex()) {
            StatementImpl statementImpl;
            try {
                if (this.retrieveGeneratedKeys) {
                    ResultSet rs = null;
                    try {
                        rs = batchedStatement.getGeneratedKeys();
                        while (rs.next()) {
                            this.batchedGeneratedKeys.add(new ByteArrayRow(new byte[][]{rs.getBytes(1)}, getExceptionInterceptor()));
                        }
                        if (rs != null) {
                            rs.close();
                        }
                    } catch (Throwable th2) {
                        th = th2;
                        statementImpl = this;
                        throw th;
                    }
                }
                statementImpl = this;
            } catch (Throwable th3) {
                th = th3;
                throw th;
            }
        }
    }

    protected void getBatchedGeneratedKeys(int maxKeys) throws SQLException {
        Throwable th;
        StatementImpl statementImpl;
        synchronized (checkClosed().getConnectionMutex()) {
            try {
                if (this.retrieveGeneratedKeys) {
                    ResultSet rs = null;
                    if (maxKeys == 0) {
                        try {
                            rs = getGeneratedKeysInternal();
                        } catch (Throwable th2) {
                            this.isImplicitlyClosingResults = false;
                        }
                    } else {
                        rs = getGeneratedKeysInternal((long) maxKeys);
                    }
                    while (rs.next()) {
                        this.batchedGeneratedKeys.add(new ByteArrayRow(new byte[][]{rs.getBytes(1)}, getExceptionInterceptor()));
                    }
                    try {
                        this.isImplicitlyClosingResults = true;
                        if (rs != null) {
                            rs.close();
                        }
                        statementImpl.isImplicitlyClosingResults = false;
                    } catch (Throwable th3) {
                        th = th3;
                        throw th;
                    }
                }
                statementImpl = this;
            } catch (Throwable th4) {
                th = th4;
                statementImpl = this;
                throw th;
            }
        }
    }

    private boolean useServerFetch() throws SQLException {
        boolean z;
        synchronized (checkClosed().getConnectionMutex()) {
            z = this.connection.isCursorFetchEnabled() && this.fetchSize > 0 && this.resultSetConcurrency == 1007 && this.resultSetType == 1003;
        }
        return z;
    }

    public boolean isClosed() throws SQLException {
        MySQLConnection locallyScopedConn = this.connection;
        if (locallyScopedConn == null) {
            return true;
        }
        boolean z;
        synchronized (locallyScopedConn.getConnectionMutex()) {
            z = this.isClosed;
        }
        return z;
    }

    public boolean isPoolable() throws SQLException {
        return this.isPoolable;
    }

    public void setPoolable(boolean poolable) throws SQLException {
        this.isPoolable = poolable;
    }

    public boolean isWrapperFor(Class<?> iface) throws SQLException {
        checkClosed();
        return iface.isInstance(this);
    }

    public <T> T unwrap(Class<T> iface) throws SQLException {
        try {
            return iface.cast(this);
        } catch (ClassCastException e) {
            StringBuilder stringBuilder = new StringBuilder();
            stringBuilder.append("Unable to unwrap to ");
            stringBuilder.append(iface.toString());
            throw SQLError.createSQLException(stringBuilder.toString(), SQLError.SQL_STATE_ILLEGAL_ARGUMENT, getExceptionInterceptor());
        }
    }

    protected static int findStartOfStatement(String sql) {
        int statementStartPos;
        if (StringUtils.startsWithIgnoreCaseAndWs(sql, "/*")) {
            statementStartPos = sql.indexOf("*/");
            if (statementStartPos == -1) {
                return 0;
            }
            return statementStartPos + 2;
        } else if (!StringUtils.startsWithIgnoreCaseAndWs(sql, "--") && !StringUtils.startsWithIgnoreCaseAndWs(sql, "#")) {
            return 0;
        } else {
            statementStartPos = sql.indexOf(10);
            if (statementStartPos != -1) {
                return statementStartPos;
            }
            statementStartPos = sql.indexOf(13);
            if (statementStartPos == -1) {
                return 0;
            }
            return statementStartPos;
        }
    }

    public InputStream getLocalInfileInputStream() {
        return this.localInfileInputStream;
    }

    public void setLocalInfileInputStream(InputStream stream) {
        this.localInfileInputStream = stream;
    }

    public void setPingTarget(PingTarget pingTarget) {
        this.pingTarget = pingTarget;
    }

    public ExceptionInterceptor getExceptionInterceptor() {
        return this.exceptionInterceptor;
    }

    protected boolean containsOnDuplicateKeyInString(String sql) {
        return getOnDuplicateKeyLocation(sql, this.connection.getDontCheckOnDuplicateKeyUpdateInSQL(), this.connection.getRewriteBatchedStatements(), this.connection.isNoBackslashEscapesSet()) != -1;
    }

    protected static int getOnDuplicateKeyLocation(String sql, boolean dontCheckOnDuplicateKeyUpdateInSQL, boolean rewriteBatchedStatements, boolean noBackslashEscapes) {
        if (dontCheckOnDuplicateKeyUpdateInSQL && !rewriteBatchedStatements) {
            return -1;
        }
        return StringUtils.indexOfIgnoreCase(0, sql, ON_DUPLICATE_KEY_UPDATE_CLAUSE, "\"'`", "\"'`", noBackslashEscapes ? StringUtils.SEARCH_MODE__MRK_COM_WS : StringUtils.SEARCH_MODE__ALL);
    }

    public void closeOnCompletion() throws SQLException {
        synchronized (checkClosed().getConnectionMutex()) {
            this.closeOnCompletion = true;
        }
    }

    public boolean isCloseOnCompletion() throws SQLException {
        boolean z;
        synchronized (checkClosed().getConnectionMutex()) {
            z = this.closeOnCompletion;
        }
        return z;
    }

    public long[] executeLargeBatch() throws SQLException {
        return executeBatchInternal();
    }

    public long executeLargeUpdate(String sql) throws SQLException {
        return executeUpdateInternal(sql, false, false);
    }

    public long executeLargeUpdate(String sql, int autoGeneratedKeys) throws SQLException {
        boolean z = true;
        if (autoGeneratedKeys != 1) {
            z = false;
        }
        return executeUpdateInternal(sql, false, z);
    }

    public long executeLargeUpdate(String sql, int[] columnIndexes) throws SQLException {
        boolean z = columnIndexes != null && columnIndexes.length > 0;
        return executeUpdateInternal(sql, false, z);
    }

    public long executeLargeUpdate(String sql, String[] columnNames) throws SQLException {
        boolean z = columnNames != null && columnNames.length > 0;
        return executeUpdateInternal(sql, false, z);
    }

    public long getLargeMaxRows() throws SQLException {
        return (long) getMaxRows();
    }

    public long getLargeUpdateCount() throws SQLException {
        synchronized (checkClosed().getConnectionMutex()) {
            if (this.results == null) {
                return -1;
            } else if (this.results.reallyResult()) {
                return -1;
            } else {
                long updateCount = this.results.getUpdateCount();
                return updateCount;
            }
        }
    }

    public void setLargeMaxRows(long max) throws SQLException {
        synchronized (checkClosed().getConnectionMutex()) {
            if (max <= 50000000) {
                if (max >= 0) {
                    if (max == 0) {
                        max = -1;
                    }
                    this.maxRows = (int) max;
                }
            }
            StringBuilder stringBuilder = new StringBuilder();
            stringBuilder.append(Messages.getString("Statement.15"));
            stringBuilder.append(max);
            stringBuilder.append(" > ");
            stringBuilder.append(50000000);
            stringBuilder.append(".");
            throw SQLError.createSQLException(stringBuilder.toString(), SQLError.SQL_STATE_ILLEGAL_ARGUMENT, getExceptionInterceptor());
        }
    }

    boolean isCursorRequired() throws SQLException {
        return false;
    }
}
