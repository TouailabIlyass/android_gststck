package com.mysql.jdbc;

import java.sql.SQLException;
import java.util.List;
import java.util.Map;
import java.util.Properties;

public class BestResponseTimeBalanceStrategy implements BalanceStrategy {
    public void destroy() {
    }

    public void init(Connection conn, Properties props) throws SQLException {
    }

    public ConnectionImpl pickConnection(LoadBalancedConnectionProxy proxy, List<String> configuredHosts, Map<String, ConnectionImpl> liveConnections, long[] responseTimes, int numRetries) throws SQLException {
        LoadBalancedConnectionProxy loadBalancedConnectionProxy = proxy;
        List list = configuredHosts;
        long[] jArr = responseTimes;
        SQLException ex = null;
        Map blackList = proxy.getGlobalBlacklist();
        int attempts = 0;
        while (attempts < numRetries) {
            Map<String, Long> blackList2;
            if (blackList2.size() == configuredHosts.size()) {
                blackList2 = proxy.getGlobalBlacklist();
            }
            int bestHostIndex = 0;
            long minResponseTime = Long.MAX_VALUE;
            int i = 0;
            while (i < jArr.length) {
                long candidateResponseTime = jArr[i];
                if (candidateResponseTime < minResponseTime && !blackList.containsKey(list.get(i))) {
                    if (candidateResponseTime == 0) {
                        bestHostIndex = i;
                        break;
                    }
                    minResponseTime = candidateResponseTime;
                    bestHostIndex = i;
                }
                i++;
            }
            String bestHost = (String) list.get(bestHostIndex);
            ConnectionImpl conn = (ConnectionImpl) liveConnections.get(bestHost);
            if (conn == null) {
                try {
                    conn = loadBalancedConnectionProxy.createConnectionForHost(bestHost);
                } catch (SQLException e) {
                    SQLException sqlEx = e;
                    ex = sqlEx;
                    if (loadBalancedConnectionProxy.shouldExceptionTriggerConnectionSwitch(sqlEx)) {
                        loadBalancedConnectionProxy.addToGlobalBlacklist(bestHost);
                        blackList.put(bestHost, null);
                        if (blackList.size() == configuredHosts.size()) {
                            attempts++;
                            try {
                                Thread.sleep(250);
                            } catch (InterruptedException e2) {
                            }
                            blackList = proxy.getGlobalBlacklist();
                        }
                        List<String> blackList3 = configuredHosts;
                        jArr = responseTimes;
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
