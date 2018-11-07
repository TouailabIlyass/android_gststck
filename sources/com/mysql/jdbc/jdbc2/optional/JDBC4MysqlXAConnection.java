package com.mysql.jdbc.jdbc2.optional;

import com.mysql.jdbc.Connection;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.Map;
import javax.sql.StatementEvent;
import javax.sql.StatementEventListener;

public class JDBC4MysqlXAConnection extends MysqlXAConnection {
    private final Map<StatementEventListener, StatementEventListener> statementEventListeners = new HashMap();

    public JDBC4MysqlXAConnection(Connection connection, boolean logXaCommands) throws SQLException {
        super(connection, logXaCommands);
    }

    public synchronized void close() throws SQLException {
        super.close();
        this.statementEventListeners.clear();
    }

    public void addStatementEventListener(StatementEventListener listener) {
        synchronized (this.statementEventListeners) {
            this.statementEventListeners.put(listener, listener);
        }
    }

    public void removeStatementEventListener(StatementEventListener listener) {
        synchronized (this.statementEventListeners) {
            this.statementEventListeners.remove(listener);
        }
    }

    void fireStatementEvent(StatementEvent event) throws SQLException {
        synchronized (this.statementEventListeners) {
            for (StatementEventListener listener : this.statementEventListeners.keySet()) {
                listener.statementClosed(event);
            }
        }
    }
}
