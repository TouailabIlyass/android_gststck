package com.mysql.jdbc;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Map;

public interface ResultSetInternalMethods extends ResultSet {
    void buildIndexMapping() throws SQLException;

    void clearNextResult();

    ResultSetInternalMethods copy() throws SQLException;

    int getBytesSize() throws SQLException;

    char getFirstCharOfQuery();

    ResultSetInternalMethods getNextResultSet();

    Object getObjectStoredProc(int i, int i2) throws SQLException;

    Object getObjectStoredProc(int i, Map<Object, Object> map, int i2) throws SQLException;

    Object getObjectStoredProc(String str, int i) throws SQLException;

    Object getObjectStoredProc(String str, Map<Object, Object> map, int i) throws SQLException;

    String getServerInfo();

    long getUpdateCount();

    long getUpdateID();

    void initializeFromCachedMetaData(CachedResultSetMetaData cachedResultSetMetaData);

    void initializeWithMetadata() throws SQLException;

    boolean isClosed() throws SQLException;

    void populateCachedMetaData(CachedResultSetMetaData cachedResultSetMetaData) throws SQLException;

    void realClose(boolean z) throws SQLException;

    boolean reallyResult();

    void redefineFieldsForDBMD(Field[] fieldArr);

    void setFirstCharOfQuery(char c);

    void setOwningStatement(StatementImpl statementImpl);

    void setStatementUsedForFetchingRows(PreparedStatement preparedStatement);

    void setWrapperStatement(Statement statement);
}
