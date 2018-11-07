package com.mysql.jdbc;

import com.mysql.jdbc.log.Log;
import com.mysql.jdbc.profiler.ProfilerEventHandler;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Calendar;
import java.util.List;
import java.util.Properties;
import java.util.TimeZone;
import java.util.Timer;

public interface MySQLConnection extends Connection, ConnectionProperties {
    void createNewIO(boolean z) throws SQLException;

    void decachePreparedStatement(ServerPreparedStatement serverPreparedStatement) throws SQLException;

    void dumpTestcaseQuery(String str);

    Connection duplicate() throws SQLException;

    ResultSetInternalMethods execSQL(StatementImpl statementImpl, String str, int i, Buffer buffer, int i2, int i3, boolean z, String str2, Field[] fieldArr) throws SQLException;

    ResultSetInternalMethods execSQL(StatementImpl statementImpl, String str, int i, Buffer buffer, int i2, int i3, boolean z, String str2, Field[] fieldArr, boolean z2) throws SQLException;

    String extractSqlFromPacket(String str, Buffer buffer, int i) throws SQLException;

    StringBuilder generateConnectionCommentBlock(StringBuilder stringBuilder);

    MySQLConnection getActiveMySQLConnection();

    int getActiveStatementCount();

    int getAutoIncrementIncrement();

    CachedResultSetMetaData getCachedMetaData(String str);

    Calendar getCalendarInstanceForSessionOrNew();

    Timer getCancelTimer();

    String getCharacterSetMetadata();

    SingleByteCharsetConverter getCharsetConverter(String str) throws SQLException;

    @Deprecated
    String getCharsetNameForIndex(int i) throws SQLException;

    String getConnectionAttributes() throws SQLException;

    TimeZone getDefaultTimeZone();

    String getEncodingForIndex(int i) throws SQLException;

    String getErrorMessageEncoding();

    ExceptionInterceptor getExceptionInterceptor();

    String getHost();

    String getHostPortPair();

    MysqlIO getIO() throws SQLException;

    long getId();

    long getIdleFor();

    @Deprecated
    MySQLConnection getLoadBalanceSafeProxy();

    Log getLog() throws SQLException;

    int getMaxBytesPerChar(Integer num, String str) throws SQLException;

    int getMaxBytesPerChar(String str) throws SQLException;

    Statement getMetadataSafeStatement() throws SQLException;

    MySQLConnection getMultiHostSafeProxy();

    int getNetBufferLength();

    ProfilerEventHandler getProfilerEventHandlerInstance();

    Properties getProperties();

    boolean getRequiresEscapingEncoder();

    String getServerCharset();

    int getServerMajorVersion();

    int getServerMinorVersion();

    int getServerSubMinorVersion();

    TimeZone getServerTimezoneTZ();

    String getServerVariable(String str);

    String getServerVersion();

    Calendar getSessionLockedCalendar();

    String getStatementComment();

    List<StatementInterceptorV2> getStatementInterceptorsInstances();

    String getURL();

    String getUser();

    Calendar getUtcCalendar();

    void incrementNumberOfPreparedExecutes();

    void incrementNumberOfPrepares();

    void incrementNumberOfResultSetsCreated();

    void initializeResultsMetadataFromCache(String str, CachedResultSetMetaData cachedResultSetMetaData, ResultSetInternalMethods resultSetInternalMethods) throws SQLException;

    void initializeSafeStatementInterceptors() throws SQLException;

    boolean isAbonormallyLongQuery(long j);

    boolean isClientTzUTC();

    boolean isCursorFetchEnabled() throws SQLException;

    boolean isProxySet();

    boolean isReadInfoMsgEnabled();

    boolean isReadOnly() throws SQLException;

    boolean isReadOnly(boolean z) throws SQLException;

    boolean isRunningOnJDK13();

    boolean isServerTzUTC();

    boolean lowerCaseTableNames();

    void pingInternal(boolean z, int i) throws SQLException;

    void realClose(boolean z, boolean z2, boolean z3, Throwable th) throws SQLException;

    void recachePreparedStatement(ServerPreparedStatement serverPreparedStatement) throws SQLException;

    void registerQueryExecutionTime(long j);

    void registerStatement(Statement statement);

    void reportNumberOfTablesAccessed(int i);

    boolean serverSupportsConvertFn() throws SQLException;

    void setProfilerEventHandlerInstance(ProfilerEventHandler profilerEventHandler);

    void setProxy(MySQLConnection mySQLConnection);

    void setReadInfoMsgEnabled(boolean z);

    void setReadOnlyInternal(boolean z) throws SQLException;

    void shutdownServer() throws SQLException;

    boolean storesLowerCaseTableName();

    void throwConnectionClosedException() throws SQLException;

    void transactionBegun() throws SQLException;

    void transactionCompleted() throws SQLException;

    void unSafeStatementInterceptors() throws SQLException;

    void unregisterStatement(Statement statement);

    boolean useAnsiQuotedIdentifiers();
}
