package com.mysql.jdbc.jmx;

import java.sql.SQLException;

public interface LoadBalanceConnectionGroupManagerMBean {
    void addHost(String str, String str2, boolean z);

    int getActiveHostCount(String str);

    String getActiveHostsList(String str);

    long getActiveLogicalConnectionCount(String str);

    long getActivePhysicalConnectionCount(String str);

    String getRegisteredConnectionGroups();

    int getTotalHostCount(String str);

    long getTotalLogicalConnectionCount(String str);

    long getTotalPhysicalConnectionCount(String str);

    long getTotalTransactionCount(String str);

    void removeHost(String str, String str2) throws SQLException;

    void stopNewConnectionsToHost(String str, String str2) throws SQLException;
}
