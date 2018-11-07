package com.mysql.fabric.jdbc;

import com.mysql.fabric.ServerGroup;
import com.mysql.jdbc.MySQLConnection;
import java.sql.SQLException;
import java.util.Set;

public interface FabricMySQLConnection extends MySQLConnection {
    void addQueryTable(String str) throws SQLException;

    void clearQueryTables() throws SQLException;

    void clearServerSelectionCriteria() throws SQLException;

    ServerGroup getCurrentServerGroup();

    Set<String> getQueryTables();

    String getServerGroupName();

    String getShardKey();

    String getShardTable();

    void setServerGroupName(String str) throws SQLException;

    void setShardKey(String str) throws SQLException;

    void setShardTable(String str) throws SQLException;
}
