package com.mysql.fabric.jdbc;

import com.mysql.jdbc.ConnectionProperties;

public interface FabricMySQLConnectionProperties extends ConnectionProperties {
    String getFabricPassword();

    String getFabricProtocol();

    boolean getFabricReportErrors();

    String getFabricServerGroup();

    String getFabricShardKey();

    String getFabricShardTable();

    String getFabricUsername();

    void setFabricPassword(String str);

    void setFabricProtocol(String str);

    void setFabricReportErrors(boolean z);

    void setFabricServerGroup(String str);

    void setFabricShardKey(String str);

    void setFabricShardTable(String str);

    void setFabricUsername(String str);
}
