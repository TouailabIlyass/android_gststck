package com.mysql.jdbc;

import java.sql.SQLException;
import java.util.List;
import java.util.Map;
import java.util.Properties;

public class SequentialBalanceStrategy implements BalanceStrategy {
    private int currentHostIndex = -1;

    public void destroy() {
    }

    public void init(Connection conn, Properties props) throws SQLException {
    }

    public ConnectionImpl pickConnection(LoadBalancedConnectionProxy proxy, List<String> configuredHosts, Map<String, ConnectionImpl> liveConnections, long[] responseTimes, int numRetries) throws SQLException {
        SequentialBalanceStrategy sequentialBalanceStrategy = this;
        LoadBalancedConnectionProxy loadBalancedConnectionProxy = proxy;
        List<String> list = configuredHosts;
        int numHosts = configuredHosts.size();
        Map<String, Long> blackList = proxy.getGlobalBlacklist();
        SQLException ex = null;
        int attempts = 0;
        while (attempts < numRetries) {
            if (numHosts == 1) {
                sequentialBalanceStrategy.currentHostIndex = 0;
            } else if (sequentialBalanceStrategy.currentHostIndex == -1) {
                int random = (int) Math.floor(Math.random() * ((double) numHosts));
                for (i = random; i < numHosts; i++) {
                    if (!blackList.containsKey(list.get(i))) {
                        sequentialBalanceStrategy.currentHostIndex = i;
                        break;
                    }
                }
                if (sequentialBalanceStrategy.currentHostIndex == -1) {
                    for (i = 0; i < random; i++) {
                        if (!blackList.containsKey(list.get(i))) {
                            sequentialBalanceStrategy.currentHostIndex = i;
                            break;
                        }
                    }
                }
                if (sequentialBalanceStrategy.currentHostIndex == -1) {
                    blackList = proxy.getGlobalBlacklist();
                    try {
                        Thread.sleep(250);
                    } catch (InterruptedException e) {
                    }
                }
            } else {
                boolean foundGoodHost = false;
                for (i = sequentialBalanceStrategy.currentHostIndex + 1; i < numHosts; i++) {
                    if (!blackList.containsKey(list.get(i))) {
                        sequentialBalanceStrategy.currentHostIndex = i;
                        foundGoodHost = true;
                        break;
                    }
                }
                if (!foundGoodHost) {
                    for (i = 0; i < sequentialBalanceStrategy.currentHostIndex; i++) {
                        if (!blackList.containsKey(list.get(i))) {
                            sequentialBalanceStrategy.currentHostIndex = i;
                            foundGoodHost = true;
                            break;
                        }
                    }
                }
                if (!foundGoodHost) {
                    blackList = proxy.getGlobalBlacklist();
                    try {
                        Thread.sleep(250);
                    } catch (InterruptedException e2) {
                    }
                }
            }
            String hostPortSpec = (String) list.get(sequentialBalanceStrategy.currentHostIndex);
            ConnectionImpl conn = (ConnectionImpl) liveConnections.get(hostPortSpec);
            if (conn == null) {
                try {
                    conn = loadBalancedConnectionProxy.createConnectionForHost(hostPortSpec);
                } catch (SQLException e3) {
                    SQLException sqlEx = e3;
                    ex = sqlEx;
                    if (loadBalancedConnectionProxy.shouldExceptionTriggerConnectionSwitch(sqlEx)) {
                        loadBalancedConnectionProxy.addToGlobalBlacklist(hostPortSpec);
                        try {
                            Thread.sleep(250);
                        } catch (InterruptedException e4) {
                        }
                    } else {
                        throw sqlEx;
                    }
                }
            }
            return conn;
        }
        Map<String, ConnectionImpl> map = liveConnections;
        if (ex == null) {
            return null;
        }
        throw ex;
    }
}
