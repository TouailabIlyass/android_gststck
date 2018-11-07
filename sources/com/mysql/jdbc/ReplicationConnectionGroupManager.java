package com.mysql.jdbc;

import com.mysql.jdbc.jmx.ReplicationGroupManager;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Set;

public class ReplicationConnectionGroupManager {
    private static HashMap<String, ReplicationConnectionGroup> GROUP_MAP = new HashMap();
    private static boolean hasRegisteredJmx = false;
    private static ReplicationGroupManager mbean = new ReplicationGroupManager();

    public static synchronized ReplicationConnectionGroup getConnectionGroupInstance(String groupName) {
        synchronized (ReplicationConnectionGroupManager.class) {
            if (GROUP_MAP.containsKey(groupName)) {
                ReplicationConnectionGroup replicationConnectionGroup = (ReplicationConnectionGroup) GROUP_MAP.get(groupName);
                return replicationConnectionGroup;
            }
            replicationConnectionGroup = new ReplicationConnectionGroup(groupName);
            GROUP_MAP.put(groupName, replicationConnectionGroup);
            return replicationConnectionGroup;
        }
    }

    public static void registerJmx() throws SQLException {
        if (!hasRegisteredJmx) {
            mbean.registerJmx();
            hasRegisteredJmx = true;
        }
    }

    public static ReplicationConnectionGroup getConnectionGroup(String groupName) {
        return (ReplicationConnectionGroup) GROUP_MAP.get(groupName);
    }

    public static Collection<ReplicationConnectionGroup> getGroupsMatching(String group) {
        Set<ReplicationConnectionGroup> s;
        if (group != null) {
            if (!group.equals("")) {
                s = new HashSet();
                ReplicationConnectionGroup o = (ReplicationConnectionGroup) GROUP_MAP.get(group);
                if (o != null) {
                    s.add(o);
                }
                return s;
            }
        }
        s = new HashSet();
        s.addAll(GROUP_MAP.values());
        return s;
    }

    public static void addSlaveHost(String group, String hostPortPair) throws SQLException {
        for (ReplicationConnectionGroup cg : getGroupsMatching(group)) {
            cg.addSlaveHost(hostPortPair);
        }
    }

    public static void removeSlaveHost(String group, String hostPortPair) throws SQLException {
        removeSlaveHost(group, hostPortPair, true);
    }

    public static void removeSlaveHost(String group, String hostPortPair, boolean closeGently) throws SQLException {
        for (ReplicationConnectionGroup cg : getGroupsMatching(group)) {
            cg.removeSlaveHost(hostPortPair, closeGently);
        }
    }

    public static void promoteSlaveToMaster(String group, String hostPortPair) throws SQLException {
        for (ReplicationConnectionGroup cg : getGroupsMatching(group)) {
            cg.promoteSlaveToMaster(hostPortPair);
        }
    }

    public static long getSlavePromotionCount(String group) throws SQLException {
        long promoted = 0;
        for (ReplicationConnectionGroup cg : getGroupsMatching(group)) {
            long tmp = cg.getNumberOfSlavePromotions();
            if (tmp > promoted) {
                promoted = tmp;
            }
        }
        return promoted;
    }

    public static void removeMasterHost(String group, String hostPortPair) throws SQLException {
        removeMasterHost(group, hostPortPair, true);
    }

    public static void removeMasterHost(String group, String hostPortPair, boolean closeGently) throws SQLException {
        for (ReplicationConnectionGroup cg : getGroupsMatching(group)) {
            cg.removeMasterHost(hostPortPair, closeGently);
        }
    }

    public static String getRegisteredReplicationConnectionGroups() {
        Collection<ReplicationConnectionGroup> s = getGroupsMatching(null);
        StringBuilder sb = new StringBuilder();
        String sep = "";
        for (ReplicationConnectionGroup cg : s) {
            String group = cg.getGroupName();
            sb.append(sep);
            sb.append(group);
            sep = ",";
        }
        return sb.toString();
    }

    public static int getNumberOfMasterPromotion(String groupFilter) {
        int total = 0;
        for (ReplicationConnectionGroup cg : getGroupsMatching(groupFilter)) {
            total = (int) (((long) total) + cg.getNumberOfSlavePromotions());
        }
        return total;
    }

    public static int getConnectionCountWithHostAsSlave(String groupFilter, String hostPortPair) {
        int total = 0;
        for (ReplicationConnectionGroup cg : getGroupsMatching(groupFilter)) {
            total += cg.getConnectionCountWithHostAsSlave(hostPortPair);
        }
        return total;
    }

    public static int getConnectionCountWithHostAsMaster(String groupFilter, String hostPortPair) {
        int total = 0;
        for (ReplicationConnectionGroup cg : getGroupsMatching(groupFilter)) {
            total += cg.getConnectionCountWithHostAsMaster(hostPortPair);
        }
        return total;
    }

    public static Collection<String> getSlaveHosts(String groupFilter) {
        Collection<ReplicationConnectionGroup> s = getGroupsMatching(groupFilter);
        Collection<String> hosts = new ArrayList();
        for (ReplicationConnectionGroup cg : s) {
            hosts.addAll(cg.getSlaveHosts());
        }
        return hosts;
    }

    public static Collection<String> getMasterHosts(String groupFilter) {
        Collection<ReplicationConnectionGroup> s = getGroupsMatching(groupFilter);
        Collection<String> hosts = new ArrayList();
        for (ReplicationConnectionGroup cg : s) {
            hosts.addAll(cg.getMasterHosts());
        }
        return hosts;
    }

    public static long getTotalConnectionCount(String group) {
        long connections = 0;
        for (ReplicationConnectionGroup cg : getGroupsMatching(group)) {
            connections += cg.getTotalConnectionCount();
        }
        return connections;
    }

    public static long getActiveConnectionCount(String group) {
        long connections = 0;
        for (ReplicationConnectionGroup cg : getGroupsMatching(group)) {
            connections += cg.getActiveConnectionCount();
        }
        return connections;
    }
}
