package com.mysql.jdbc.jmx;

import java.sql.SQLException;

public interface ReplicationGroupManagerMBean {
    void addSlaveHost(String str, String str2) throws SQLException;

    long getActiveLogicalConnectionCount(String str);

    int getActiveMasterHostCount(String str);

    int getActiveSlaveHostCount(String str);

    String getMasterHostsList(String str);

    String getRegisteredConnectionGroups();

    String getSlaveHostsList(String str);

    int getSlavePromotionCount(String str);

    long getTotalLogicalConnectionCount(String str);

    void promoteSlaveToMaster(String str, String str2) throws SQLException;

    void removeMasterHost(String str, String str2) throws SQLException;

    void removeSlaveHost(String str, String str2) throws SQLException;
}
