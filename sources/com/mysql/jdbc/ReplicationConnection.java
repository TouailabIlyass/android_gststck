package com.mysql.jdbc;

import java.sql.SQLException;

public interface ReplicationConnection extends MySQLConnection {
    void addSlaveHost(String str) throws SQLException;

    long getConnectionGroupId();

    Connection getCurrentConnection();

    Connection getMasterConnection();

    Connection getSlavesConnection();

    boolean isHostMaster(String str);

    boolean isHostSlave(String str);

    void promoteSlaveToMaster(String str) throws SQLException;

    void removeMasterHost(String str) throws SQLException;

    void removeMasterHost(String str, boolean z) throws SQLException;

    void removeSlave(String str) throws SQLException;

    void removeSlave(String str, boolean z) throws SQLException;
}
