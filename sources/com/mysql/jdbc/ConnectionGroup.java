package com.mysql.jdbc;

import java.sql.SQLException;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class ConnectionGroup {
    private long activeConnections = 0;
    private int activeHosts = 0;
    private Set<String> closedHosts = new HashSet();
    private long closedProxyTotalPhysicalConnections = 0;
    private long closedProxyTotalTransactions = 0;
    private HashMap<Long, LoadBalancedConnectionProxy> connectionProxies = new HashMap();
    private long connections = 0;
    private String groupName;
    private Set<String> hostList = new HashSet();
    private boolean isInitialized = false;

    ConnectionGroup(String groupName) {
        this.groupName = groupName;
    }

    public long registerConnectionProxy(LoadBalancedConnectionProxy proxy, List<String> localHostList) {
        long currentConnectionId;
        synchronized (this) {
            if (!this.isInitialized) {
                this.hostList.addAll(localHostList);
                this.isInitialized = true;
                this.activeHosts = localHostList.size();
            }
            long j = this.connections + 1;
            this.connections = j;
            currentConnectionId = j;
            this.connectionProxies.put(Long.valueOf(currentConnectionId), proxy);
        }
        this.activeConnections++;
        return currentConnectionId;
    }

    public String getGroupName() {
        return this.groupName;
    }

    public Collection<String> getInitialHosts() {
        return this.hostList;
    }

    public int getActiveHostCount() {
        return this.activeHosts;
    }

    public Collection<String> getClosedHosts() {
        return this.closedHosts;
    }

    public long getTotalLogicalConnectionCount() {
        return this.connections;
    }

    public long getActiveLogicalConnectionCount() {
        return this.activeConnections;
    }

    public long getActivePhysicalConnectionCount() {
        long result = 0;
        Map<Long, LoadBalancedConnectionProxy> proxyMap = new HashMap();
        synchronized (this.connectionProxies) {
            proxyMap.putAll(this.connectionProxies);
        }
        for (LoadBalancedConnectionProxy proxy : proxyMap.values()) {
            result += proxy.getActivePhysicalConnectionCount();
        }
        return result;
    }

    public long getTotalPhysicalConnectionCount() {
        long allConnections = this.closedProxyTotalPhysicalConnections;
        Map<Long, LoadBalancedConnectionProxy> proxyMap = new HashMap();
        synchronized (this.connectionProxies) {
            proxyMap.putAll(this.connectionProxies);
        }
        for (LoadBalancedConnectionProxy proxy : proxyMap.values()) {
            allConnections += proxy.getTotalPhysicalConnectionCount();
        }
        return allConnections;
    }

    public long getTotalTransactionCount() {
        long transactions = this.closedProxyTotalTransactions;
        Map<Long, LoadBalancedConnectionProxy> proxyMap = new HashMap();
        synchronized (this.connectionProxies) {
            proxyMap.putAll(this.connectionProxies);
        }
        for (LoadBalancedConnectionProxy proxy : proxyMap.values()) {
            transactions += proxy.getTransactionCount();
        }
        return transactions;
    }

    public void closeConnectionProxy(LoadBalancedConnectionProxy proxy) {
        this.activeConnections--;
        this.connectionProxies.remove(Long.valueOf(proxy.getConnectionGroupProxyID()));
        this.closedProxyTotalPhysicalConnections += proxy.getTotalPhysicalConnectionCount();
        this.closedProxyTotalTransactions += proxy.getTransactionCount();
    }

    public void removeHost(String hostPortPair) throws SQLException {
        removeHost(hostPortPair, false);
    }

    public void removeHost(String hostPortPair, boolean removeExisting) throws SQLException {
        removeHost(hostPortPair, removeExisting, true);
    }

    public synchronized void removeHost(String hostPortPair, boolean removeExisting, boolean waitForGracefulFailover) throws SQLException {
        if (this.activeHosts == 1) {
            throw SQLError.createSQLException("Cannot remove host, only one configured host active.", null);
        } else if (this.hostList.remove(hostPortPair)) {
            this.activeHosts--;
            if (removeExisting) {
                Map<Long, LoadBalancedConnectionProxy> proxyMap = new HashMap();
                synchronized (this.connectionProxies) {
                    proxyMap.putAll(this.connectionProxies);
                }
                for (LoadBalancedConnectionProxy proxy : proxyMap.values()) {
                    if (waitForGracefulFailover) {
                        proxy.removeHostWhenNotInUse(hostPortPair);
                    } else {
                        proxy.removeHost(hostPortPair);
                    }
                }
            }
            this.closedHosts.add(hostPortPair);
        } else {
            StringBuilder stringBuilder = new StringBuilder();
            stringBuilder.append("Host is not configured: ");
            stringBuilder.append(hostPortPair);
            throw SQLError.createSQLException(stringBuilder.toString(), null);
        }
    }

    public void addHost(String hostPortPair) {
        addHost(hostPortPair, false);
    }

    public void addHost(String hostPortPair, boolean forExisting) {
        synchronized (this) {
            if (this.hostList.add(hostPortPair)) {
                this.activeHosts++;
            }
        }
        if (forExisting) {
            Map<Long, LoadBalancedConnectionProxy> proxyMap = new HashMap();
            synchronized (this.connectionProxies) {
                proxyMap.putAll(this.connectionProxies);
            }
            for (LoadBalancedConnectionProxy proxy : proxyMap.values()) {
                proxy.addHost(hostPortPair);
            }
        }
    }
}
