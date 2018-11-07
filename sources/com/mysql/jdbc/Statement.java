package com.mysql.jdbc;

import java.io.InputStream;
import java.sql.SQLException;

public interface Statement extends java.sql.Statement, Wrapper {
    void disableStreamingResults() throws SQLException;

    void enableStreamingResults() throws SQLException;

    ExceptionInterceptor getExceptionInterceptor();

    InputStream getLocalInfileInputStream();

    int getOpenResultSetCount();

    void removeOpenResultSet(ResultSetInternalMethods resultSetInternalMethods);

    void setHoldResultsOpenOverClose(boolean z);

    void setLocalInfileInputStream(InputStream inputStream);

    void setPingTarget(PingTarget pingTarget);
}
