package com.mysql.jdbc.log;

public interface Log {
    boolean isDebugEnabled();

    boolean isErrorEnabled();

    boolean isFatalEnabled();

    boolean isInfoEnabled();

    boolean isTraceEnabled();

    boolean isWarnEnabled();

    void logDebug(Object obj);

    void logDebug(Object obj, Throwable th);

    void logError(Object obj);

    void logError(Object obj, Throwable th);

    void logFatal(Object obj);

    void logFatal(Object obj, Throwable th);

    void logInfo(Object obj);

    void logInfo(Object obj, Throwable th);

    void logTrace(Object obj);

    void logTrace(Object obj, Throwable th);

    void logWarn(Object obj);

    void logWarn(Object obj, Throwable th);
}
