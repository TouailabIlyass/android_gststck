package com.mysql.jdbc;

import java.sql.SQLException;
import java.util.Properties;

public class LoadBalancedAutoCommitInterceptor implements StatementInterceptorV2 {
    private ConnectionImpl conn;
    private int matchingAfterStatementCount = 0;
    private String matchingAfterStatementRegex;
    private int matchingAfterStatementThreshold = 0;
    private LoadBalancedConnectionProxy proxy = null;

    public void destroy() {
    }

    public boolean executeTopLevelOnly() {
        return false;
    }

    public void init(Connection connection, Properties props) throws SQLException {
        this.conn = (ConnectionImpl) connection;
        try {
            this.matchingAfterStatementThreshold = Integer.parseInt(props.getProperty("loadBalanceAutoCommitStatementThreshold", "0"));
        } catch (NumberFormatException e) {
        }
        String autoCommitSwapRegex = props.getProperty("loadBalanceAutoCommitStatementRegex", "");
        if (!"".equals(autoCommitSwapRegex)) {
            this.matchingAfterStatementRegex = autoCommitSwapRegex;
        }
    }

    public ResultSetInternalMethods postProcess(String sql, Statement interceptedStatement, ResultSetInternalMethods originalResultSet, Connection connection, int warningCount, boolean noIndexUsed, boolean noGoodIndexUsed, SQLException statementException) throws SQLException {
        if (this.conn.getAutoCommit()) {
            if (this.proxy == null && this.conn.isProxySet()) {
                MySQLConnection lcl_proxy = this.conn.getMultiHostSafeProxy();
                while (lcl_proxy != null && !(lcl_proxy instanceof LoadBalancedMySQLConnection)) {
                    lcl_proxy = lcl_proxy.getMultiHostSafeProxy();
                }
                if (lcl_proxy != null) {
                    this.proxy = ((LoadBalancedMySQLConnection) lcl_proxy).getThisAsProxy();
                }
            }
            if (this.proxy != null && (this.matchingAfterStatementRegex == null || sql.matches(this.matchingAfterStatementRegex))) {
                this.matchingAfterStatementCount++;
            }
            if (this.matchingAfterStatementCount >= this.matchingAfterStatementThreshold) {
                this.matchingAfterStatementCount = 0;
                try {
                    if (this.proxy != null) {
                        this.proxy.pickNewConnection();
                    }
                } catch (SQLException e) {
                }
            }
        } else {
            this.matchingAfterStatementCount = 0;
        }
        return originalResultSet;
    }

    public ResultSetInternalMethods preProcess(String sql, Statement interceptedStatement, Connection connection) throws SQLException {
        return null;
    }
}
