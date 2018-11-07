package com.mysql.jdbc;

import com.mysql.jdbc.log.Log;
import com.mysql.jdbc.profiler.ProfilerEventHandler;
import java.sql.SQLException;

public class ProfilerEventHandlerFactory {
    protected Log log = null;
    private Connection ownerConnection = null;

    public static synchronized ProfilerEventHandler getInstance(MySQLConnection conn) throws SQLException {
        ProfilerEventHandler handler;
        synchronized (ProfilerEventHandlerFactory.class) {
            handler = conn.getProfilerEventHandlerInstance();
            if (handler == null) {
                handler = (ProfilerEventHandler) Util.getInstance(conn.getProfilerEventHandler(), new Class[0], new Object[0], conn.getExceptionInterceptor());
                conn.initializeExtension(handler);
                conn.setProfilerEventHandlerInstance(handler);
            }
        }
        return handler;
    }

    public static synchronized void removeInstance(MySQLConnection conn) {
        synchronized (ProfilerEventHandlerFactory.class) {
            ProfilerEventHandler handler = conn.getProfilerEventHandlerInstance();
            if (handler != null) {
                handler.destroy();
            }
        }
    }

    private ProfilerEventHandlerFactory(Connection conn) {
        this.ownerConnection = conn;
        try {
            this.log = this.ownerConnection.getLog();
        } catch (SQLException e) {
            throw new RuntimeException("Unable to get logger from connection");
        }
    }
}
