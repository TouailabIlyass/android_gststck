package com.mysql.fabric.hibernate;

import com.mysql.fabric.FabricCommunicationException;
import com.mysql.fabric.FabricConnection;
import com.mysql.fabric.Server;
import com.mysql.fabric.ServerGroup;
import com.mysql.fabric.ServerMode;
import com.mysql.fabric.ShardMapping;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import org.hibernate.service.jdbc.connections.spi.MultiTenantConnectionProvider;

public class FabricMultiTenantConnectionProvider implements MultiTenantConnectionProvider {
    private static final long serialVersionUID = 1;
    private String database;
    private FabricConnection fabricConnection;
    private ServerGroup globalGroup;
    private String password;
    private ShardMapping shardMapping;
    private String table;
    private String user;

    public FabricMultiTenantConnectionProvider(String fabricUrl, String database, String table, String user, String password, String fabricUser, String fabricPassword) {
        try {
            this.fabricConnection = new FabricConnection(fabricUrl, fabricUser, fabricPassword);
            this.database = database;
            this.table = table;
            this.user = user;
            this.password = password;
            this.shardMapping = this.fabricConnection.getShardMapping(this.database, this.table);
            this.globalGroup = this.fabricConnection.getServerGroup(this.shardMapping.getGlobalGroupName());
        } catch (FabricCommunicationException ex) {
            throw new RuntimeException(ex);
        }
    }

    private Connection getReadWriteConnectionFromServerGroup(ServerGroup serverGroup) throws SQLException {
        for (Server s : serverGroup.getServers()) {
            if (ServerMode.READ_WRITE.equals(s.getMode())) {
                return DriverManager.getConnection(String.format("jdbc:mysql://%s:%s/%s", new Object[]{((Server) r0.next()).getHostname(), Integer.valueOf(((Server) r0.next()).getPort()), this.database}), this.user, this.password);
            }
        }
        StringBuilder stringBuilder = new StringBuilder();
        stringBuilder.append("Unable to find r/w server for chosen shard mapping in group ");
        stringBuilder.append(serverGroup.getName());
        throw new SQLException(stringBuilder.toString());
    }

    public Connection getAnyConnection() throws SQLException {
        return getReadWriteConnectionFromServerGroup(this.globalGroup);
    }

    public Connection getConnection(String tenantIdentifier) throws SQLException {
        return getReadWriteConnectionFromServerGroup(this.fabricConnection.getServerGroup(this.shardMapping.getGroupNameForKey(tenantIdentifier)));
    }

    public void releaseAnyConnection(Connection connection) throws SQLException {
        try {
            connection.close();
        } catch (Exception ex) {
            throw new RuntimeException(ex);
        }
    }

    public void releaseConnection(String tenantIdentifier, Connection connection) throws SQLException {
        releaseAnyConnection(connection);
    }

    public boolean supportsAggressiveRelease() {
        return false;
    }

    public boolean isUnwrappableAs(Class unwrapType) {
        return false;
    }

    public <T> T unwrap(Class<T> cls) {
        return null;
    }
}
