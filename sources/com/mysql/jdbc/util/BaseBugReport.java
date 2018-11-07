package com.mysql.jdbc.util;

import com.mysql.jdbc.Driver;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.Properties;

public abstract class BaseBugReport {
    private Connection conn;
    private Driver driver;

    public abstract void runTest() throws Exception;

    public abstract void setUp() throws Exception;

    public abstract void tearDown() throws Exception;

    public BaseBugReport() {
        try {
            this.driver = new Driver();
        } catch (SQLException ex) {
            throw new RuntimeException(ex.toString());
        }
    }

    public final void run() throws Exception {
        try {
            setUp();
            runTest();
        } finally {
            tearDown();
        }
    }

    protected final void assertTrue(String message, boolean condition) throws Exception {
        if (!condition) {
            StringBuilder stringBuilder = new StringBuilder();
            stringBuilder.append("Assertion failed: ");
            stringBuilder.append(message);
            throw new Exception(stringBuilder.toString());
        }
    }

    protected final void assertTrue(boolean condition) throws Exception {
        assertTrue("(no message given)", condition);
    }

    public String getUrl() {
        return "jdbc:mysql:///test";
    }

    public final synchronized Connection getConnection() throws SQLException {
        if (this.conn == null || this.conn.isClosed()) {
            this.conn = getNewConnection();
        }
        return this.conn;
    }

    public final synchronized Connection getNewConnection() throws SQLException {
        return getConnection(getUrl());
    }

    public final synchronized Connection getConnection(String url) throws SQLException {
        return getConnection(url, null);
    }

    public final synchronized Connection getConnection(String url, Properties props) throws SQLException {
        return this.driver.connect(url, props);
    }
}
