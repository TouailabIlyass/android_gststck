package com.mysql.jdbc;

import com.mysql.jdbc.log.Log;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.Properties;
import java.util.TimeZone;
import java.util.concurrent.Executor;

public interface Connection extends java.sql.Connection, ConnectionProperties {
    void abort(Executor executor) throws SQLException;

    void abortInternal() throws SQLException;

    void changeUser(String str, String str2) throws SQLException;

    void checkClosed() throws SQLException;

    @Deprecated
    void clearHasTriedMaster();

    PreparedStatement clientPrepareStatement(String str) throws SQLException;

    PreparedStatement clientPrepareStatement(String str, int i) throws SQLException;

    PreparedStatement clientPrepareStatement(String str, int i, int i2) throws SQLException;

    PreparedStatement clientPrepareStatement(String str, int i, int i2, int i3) throws SQLException;

    PreparedStatement clientPrepareStatement(String str, int[] iArr) throws SQLException;

    PreparedStatement clientPrepareStatement(String str, String[] strArr) throws SQLException;

    int getActiveStatementCount();

    int getAutoIncrementIncrement();

    Object getConnectionMutex();

    String getHost();

    long getIdleFor();

    Log getLog() throws SQLException;

    int getNetworkTimeout() throws SQLException;

    Properties getProperties();

    String getSchema() throws SQLException;

    @Deprecated
    String getServerCharacterEncoding();

    String getServerCharset();

    TimeZone getServerTimezoneTZ();

    int getSessionMaxRows();

    String getStatementComment();

    boolean hasSameProperties(Connection connection);

    @Deprecated
    boolean hasTriedMaster();

    void initializeExtension(Extension extension) throws SQLException;

    boolean isAbonormallyLongQuery(long j);

    boolean isInGlobalTx();

    boolean isMasterConnection();

    boolean isNoBackslashEscapesSet();

    boolean isSameResource(Connection connection);

    boolean isServerLocal() throws SQLException;

    boolean lowerCaseTableNames();

    boolean parserKnowsUnicode();

    void ping() throws SQLException;

    void reportQueryTime(long j);

    void resetServerState() throws SQLException;

    PreparedStatement serverPrepareStatement(String str) throws SQLException;

    PreparedStatement serverPrepareStatement(String str, int i) throws SQLException;

    PreparedStatement serverPrepareStatement(String str, int i, int i2) throws SQLException;

    PreparedStatement serverPrepareStatement(String str, int i, int i2, int i3) throws SQLException;

    PreparedStatement serverPrepareStatement(String str, int[] iArr) throws SQLException;

    PreparedStatement serverPrepareStatement(String str, String[] strArr) throws SQLException;

    void setFailedOver(boolean z);

    void setInGlobalTx(boolean z);

    void setNetworkTimeout(Executor executor, int i) throws SQLException;

    @Deprecated
    void setPreferSlaveDuringFailover(boolean z);

    void setProxy(MySQLConnection mySQLConnection);

    void setSchema(String str) throws SQLException;

    void setSessionMaxRows(int i) throws SQLException;

    void setStatementComment(String str);

    void shutdownServer() throws SQLException;

    boolean supportsIsolationLevel();

    boolean supportsQuotedIdentifiers();

    boolean supportsTransactions();

    boolean versionMeetsMinimum(int i, int i2, int i3) throws SQLException;
}
