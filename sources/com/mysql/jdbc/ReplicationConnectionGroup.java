package com.mysql.jdbc;

import java.sql.SQLException;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Set;
import java.util.concurrent.CopyOnWriteArraySet;

public class ReplicationConnectionGroup {
    private long activeConnections = 0;
    private long connections = 0;
    private String groupName;
    private boolean isInitialized = false;
    private Set<String> masterHostList = new CopyOnWriteArraySet();
    private HashMap<Long, ReplicationConnection> replicationConnections = new HashMap();
    private Set<String> slaveHostList = new CopyOnWriteArraySet();
    private long slavesAdded = 0;
    private long slavesPromoted = 0;
    private long slavesRemoved = 0;

    ReplicationConnectionGroup(String groupName) {
        this.groupName = groupName;
    }

    public long getConnectionCount() {
        return this.connections;
    }

    public long registerReplicationConnection(ReplicationConnection conn, List<String> localMasterList, List<String> localSlaveList) {
        long currentConnectionId;
        synchronized (this) {
            if (!this.isInitialized) {
                if (localMasterList != null) {
                    this.masterHostList.addAll(localMasterList);
                }
                if (localSlaveList != null) {
                    this.slaveHostList.addAll(localSlaveList);
                }
                this.isInitialized = true;
            }
            long j = this.connections + 1;
            this.connections = j;
            currentConnectionId = j;
            this.replicationConnections.put(Long.valueOf(currentConnectionId), conn);
        }
        this.activeConnections++;
        return currentConnectionId;
    }

    public String getGroupName() {
        return this.groupName;
    }

    public Collection<String> getMasterHosts() {
        return this.masterHostList;
    }

    public Collection<String> getSlaveHosts() {
        return this.slaveHostList;
    }

    public void addSlaveHost(String hostPortPair) throws SQLException {
        if (this.slaveHostList.add(hostPortPair)) {
            this.slavesAdded++;
            for (ReplicationConnection c : this.replicationConnections.values()) {
                c.addSlaveHost(hostPortPair);
            }
        }
    }

    public void handleCloseConnection(ReplicationConnection conn) {
        this.replicationConnections.remove(Long.valueOf(conn.getConnectionGroupId()));
        this.activeConnections--;
    }

    public void removeSlaveHost(String hostPortPair, boolean closeGently) throws SQLException {
        if (this.slaveHostList.remove(hostPortPair)) {
            this.slavesRemoved++;
            for (ReplicationConnection c : this.replicationConnections.values()) {
                c.removeSlave(hostPortPair, closeGently);
            }
        }
    }

    public void promoteSlaveToMaster(String hostPortPair) throws SQLException {
        if ((this.slaveHostList.remove(hostPortPair) | this.masterHostList.add(hostPortPair)) != 0) {
            this.slavesPromoted++;
            for (ReplicationConnection c : this.replicationConnections.values()) {
                c.promoteSlaveToMaster(hostPortPair);
            }
        }
    }

    public void removeMasterHost(String hostPortPair) throws SQLException {
        removeMasterHost(hostPortPair, true);
    }

    public void removeMasterHost(String hostPortPair, boolean closeGently) throws SQLException {
        if (this.masterHostList.remove(hostPortPair)) {
            for (ReplicationConnection c : this.replicationConnections.values()) {
                c.removeMasterHost(hostPortPair, closeGently);
            }
        }
    }

    public int getConnectionCountWithHostAsSlave(String hostPortPair) {
        int matched = 0;
        for (ReplicationConnection c : this.replicationConnections.values()) {
            if (c.isHostSlave(hostPortPair)) {
                matched++;
            }
        }
        return matched;
    }

    public int getConnectionCountWithHostAsMaster(String hostPortPair) {
        int matched = 0;
        for (ReplicationConnection c : this.replicationConnections.values()) {
            if (c.isHostMaster(hostPortPair)) {
                matched++;
            }
        }
        return matched;
    }

    public long getNumberOfSlavesAdded() {
        return this.slavesAdded;
    }

    public long getNumberOfSlavesRemoved() {
        return this.slavesRemoved;
    }

    public long getNumberOfSlavePromotions() {
        return this.slavesPromoted;
    }

    public long getTotalConnectionCount() {
        return this.connections;
    }

    public long getActiveConnectionCount() {
        return this.activeConnections;
    }

    public String toString() {
        StringBuilder stringBuilder = new StringBuilder();
        stringBuilder.append("ReplicationConnectionGroup[groupName=");
        stringBuilder.append(this.groupName);
        stringBuilder.append(",masterHostList=");
        stringBuilder.append(this.masterHostList);
        stringBuilder.append(",slaveHostList=");
        stringBuilder.append(this.slaveHostList);
        stringBuilder.append("]");
        return stringBuilder.toString();
    }
}
