package com.mysql.jdbc;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;

public class RandomBalanceStrategy implements BalanceStrategy {
    public void destroy() {
    }

    public void init(Connection conn, Properties props) throws SQLException {
    }

    public ConnectionImpl pickConnection(LoadBalancedConnectionProxy proxy, List<String> configuredHosts, Map<String, ConnectionImpl> liveConnections, long[] responseTimes, int numRetries) throws SQLException {
        Map<String, Long> map;
        LoadBalancedConnectionProxy loadBalancedConnectionProxy = proxy;
        List<String> list = configuredHosts;
        int numHosts = configuredHosts.size();
        SQLException ex = null;
        List<String> whiteList = new ArrayList(numHosts);
        whiteList.addAll(list);
        Map<String, Long> blackList = proxy.getGlobalBlacklist();
        whiteList.removeAll(blackList.keySet());
        Map<String, Integer> whiteListMap = getArrayIndexMap(whiteList);
        int attempts = 0;
        while (attempts < numRetries) {
            int random = (int) Math.floor(Math.random() * ((double) whiteList.size()));
            if (whiteList.size() == 0) {
                throw SQLError.createSQLException("No hosts configured", null);
            }
            String hostPortSpec = (String) whiteList.get(random);
            ConnectionImpl conn = (ConnectionImpl) liveConnections.get(hostPortSpec);
            if (conn == null) {
                try {
                    conn = loadBalancedConnectionProxy.createConnectionForHost(hostPortSpec);
                    map = blackList;
                } catch (SQLException e) {
                    SQLException sqlEx = e;
                    ex = sqlEx;
                    if (loadBalancedConnectionProxy.shouldExceptionTriggerConnectionSwitch(sqlEx)) {
                        SQLException ex2 = ex;
                        Integer ex3 = (Integer) whiteListMap.get(hostPortSpec);
                        if (ex3 != null) {
                            map = blackList;
                            whiteList.remove(ex3.intValue());
                            whiteListMap = getArrayIndexMap(whiteList);
                        } else {
                            map = blackList;
                        }
                        loadBalancedConnectionProxy.addToGlobalBlacklist(hostPortSpec);
                        if (whiteList.size() == 0) {
                            attempts++;
                            try {
                                Thread.sleep(250);
                            } catch (InterruptedException e2) {
                            }
                            Map<String, Integer> whiteListMap2 = new HashMap(numHosts);
                            whiteList.addAll(list);
                            whiteListMap = proxy.getGlobalBlacklist();
                            whiteList.removeAll(whiteListMap.keySet());
                            blackList = whiteListMap;
                            whiteListMap = getArrayIndexMap(whiteList);
                        } else {
                            Map<String, Integer> map2 = whiteListMap;
                            blackList = map;
                        }
                        ex = ex2;
                        loadBalancedConnectionProxy = proxy;
                    } else {
                        map = blackList;
                        throw sqlEx;
                    }
                }
            }
            return conn;
        }
        Map<String, ConnectionImpl> map3 = liveConnections;
        map = blackList;
        if (ex == null) {
            return null;
        }
        throw ex;
    }

    private Map<String, Integer> getArrayIndexMap(List<String> l) {
        Map<String, Integer> m = new HashMap(l.size());
        for (int i = 0; i < l.size(); i++) {
            m.put(l.get(i), Integer.valueOf(i));
        }
        return m;
    }
}
