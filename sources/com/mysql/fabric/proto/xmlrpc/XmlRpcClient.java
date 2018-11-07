package com.mysql.fabric.proto.xmlrpc;

import com.mysql.fabric.FabricCommunicationException;
import com.mysql.fabric.FabricStateResponse;
import com.mysql.fabric.Response;
import com.mysql.fabric.Server;
import com.mysql.fabric.ServerGroup;
import com.mysql.fabric.ServerMode;
import com.mysql.fabric.ServerRole;
import com.mysql.fabric.ShardIndex;
import com.mysql.fabric.ShardMapping;
import com.mysql.fabric.ShardMappingFactory;
import com.mysql.fabric.ShardTable;
import com.mysql.fabric.ShardingType;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.concurrent.TimeUnit;

public class XmlRpcClient {
    private static final String FIELD_ADDRESS = "address";
    private static final String FIELD_COLUMN_NAME = "column_name";
    private static final String FIELD_GLOBAL_GROUP_ID = "global_group_id";
    private static final String FIELD_GROUP_ID = "group_id";
    private static final String FIELD_HOST = "host";
    private static final String FIELD_LOWER_BOUND = "lower_bound";
    private static final String FIELD_MAPPING_ID = "mapping_id";
    private static final String FIELD_MODE = "mode";
    private static final String FIELD_PORT = "port";
    private static final String FIELD_RESULT = "result";
    private static final String FIELD_SCHEMA_NAME = "schema_name";
    private static final String FIELD_SERVER_UUID = "server_uuid";
    private static final String FIELD_SHARD_ID = "shard_id";
    private static final String FIELD_STATUS = "status";
    private static final String FIELD_TABLE_NAME = "table_name";
    private static final String FIELD_TYPE_NAME = "type_name";
    private static final String FIELD_WEIGHT = "weight";
    private static final String METHOD_DUMP_FABRIC_NODES = "dump.fabric_nodes";
    private static final String METHOD_DUMP_SERVERS = "dump.servers";
    private static final String METHOD_DUMP_SHARD_INDEX = "dump.shard_index";
    private static final String METHOD_DUMP_SHARD_MAPS = "dump.shard_maps";
    private static final String METHOD_DUMP_SHARD_TABLES = "dump.shard_tables";
    private static final String METHOD_GROUP_ADD = "group.add";
    private static final String METHOD_GROUP_CREATE = "group.create";
    private static final String METHOD_GROUP_DESTROY = "group.destroy";
    private static final String METHOD_GROUP_LOOKUP_GROUPS = "group.lookup_groups";
    private static final String METHOD_GROUP_PROMOTE = "group.promote";
    private static final String METHOD_GROUP_REMOVE = "group.remove";
    private static final String METHOD_SHARDING_ADD_SHARD = "sharding.add_shard";
    private static final String METHOD_SHARDING_ADD_TABLE = "sharding.add_table";
    private static final String METHOD_SHARDING_CREATE_DEFINITION = "sharding.create_definition";
    private static final String METHOD_SHARDING_LOOKUP_SERVERS = "sharding.lookup_servers";
    private static final String METHOD_THREAT_REPORT_ERROR = "threat.report_error";
    private static final String METHOD_THREAT_REPORT_FAILURE = "threat.report_failure";
    private static final String THREAT_REPORTER_NAME = "MySQL Connector/J";
    private XmlRpcMethodCaller methodCaller;

    public XmlRpcClient(String url, String username, String password) throws FabricCommunicationException {
        this.methodCaller = new InternalXmlRpcMethodCaller(url);
        if (username != null && !"".equals(username) && password != null) {
            this.methodCaller = new AuthenticatedXmlRpcMethodCaller(this.methodCaller, url, username, password);
        }
    }

    private static Server unmarshallServer(Map<String, ?> serverData) throws FabricCommunicationException {
        Exception ex;
        Object obj;
        try {
            ServerMode mode;
            ServerRole role;
            String host;
            int port;
            if (Integer.class.equals(serverData.get(FIELD_MODE).getClass())) {
                mode = ServerMode.getFromConstant((Integer) serverData.get(FIELD_MODE));
                try {
                    role = ServerRole.getFromConstant((Integer) serverData.get("status"));
                    try {
                        host = (String) serverData.get(FIELD_HOST);
                        port = ((Integer) serverData.get(FIELD_PORT)).intValue();
                    } catch (Exception e) {
                        ex = e;
                        throw new FabricCommunicationException("Unable to parse server definition", ex);
                    }
                } catch (Exception e2) {
                    ex = e2;
                    obj = null;
                    throw new FabricCommunicationException("Unable to parse server definition", ex);
                }
            }
            mode = ServerMode.valueOf((String) serverData.get(FIELD_MODE));
            role = ServerRole.valueOf((String) serverData.get("status"));
            String[] hostnameAndPort = ((String) serverData.get(FIELD_ADDRESS)).split(":");
            host = hostnameAndPort[0];
            port = Integer.valueOf(hostnameAndPort[1]).intValue();
            try {
                return new Server((String) serverData.get(FIELD_GROUP_ID), (String) serverData.get(FIELD_SERVER_UUID), host, port, mode, role, ((Double) serverData.get(FIELD_WEIGHT)).doubleValue());
            } catch (Exception e3) {
                ex = e3;
                throw new FabricCommunicationException("Unable to parse server definition", ex);
            }
        } catch (Exception e4) {
            ex = e4;
            obj = null;
            throw new FabricCommunicationException("Unable to parse server definition", ex);
        }
    }

    private static Set<Server> toServerSet(List<Map<String, ?>> l) throws FabricCommunicationException {
        Set<Server> servers = new HashSet();
        for (Map<String, ?> serverData : l) {
            servers.add(unmarshallServer(serverData));
        }
        return servers;
    }

    private Response errorSafeCallMethod(String methodName, Object[] args) throws FabricCommunicationException {
        Response response = new Response(this.methodCaller.call(methodName, args));
        if (response.getErrorMessage() == null) {
            return response;
        }
        StringBuilder stringBuilder = new StringBuilder();
        stringBuilder.append("Call failed to method `");
        stringBuilder.append(methodName);
        stringBuilder.append("':\n");
        stringBuilder.append(response.getErrorMessage());
        throw new FabricCommunicationException(stringBuilder.toString());
    }

    public Set<String> getFabricNames() throws FabricCommunicationException {
        Response resp = errorSafeCallMethod(METHOD_DUMP_FABRIC_NODES, new Object[0]);
        Set<String> names = new HashSet();
        for (Map<String, ?> node : resp.getResultSet()) {
            StringBuilder stringBuilder = new StringBuilder();
            stringBuilder.append(node.get(FIELD_HOST));
            stringBuilder.append(":");
            stringBuilder.append(node.get(FIELD_PORT));
            names.add(stringBuilder.toString());
        }
        return names;
    }

    public Set<String> getGroupNames() throws FabricCommunicationException {
        Set<String> groupNames = new HashSet();
        for (Map<String, ?> row : errorSafeCallMethod(METHOD_GROUP_LOOKUP_GROUPS, null).getResultSet()) {
            groupNames.add((String) row.get(FIELD_GROUP_ID));
        }
        return groupNames;
    }

    public ServerGroup getServerGroup(String groupName) throws FabricCommunicationException {
        Set<ServerGroup> groups = (Set) getServerGroups(groupName).getData();
        if (groups.size() == 1) {
            return (ServerGroup) groups.iterator().next();
        }
        return null;
    }

    public Set<Server> getServersForKey(String tableName, int key) throws FabricCommunicationException {
        return toServerSet(errorSafeCallMethod(METHOD_SHARDING_LOOKUP_SERVERS, new Object[]{tableName, Integer.valueOf(key)}).getResultSet());
    }

    public FabricStateResponse<Set<ServerGroup>> getServerGroups(String groupPattern) throws FabricCommunicationException {
        Response response = errorSafeCallMethod(METHOD_DUMP_SERVERS, new Object[]{Integer.valueOf(0), groupPattern});
        Map<String, Set<Server>> serversByGroupName = new HashMap();
        for (Map<String, ?> server : response.getResultSet()) {
            Server s = unmarshallServer(server);
            if (serversByGroupName.get(s.getGroupName()) == null) {
                serversByGroupName.put(s.getGroupName(), new HashSet());
            }
            ((Set) serversByGroupName.get(s.getGroupName())).add(s);
        }
        Set<ServerGroup> serverGroups = new HashSet();
        for (Entry<String, Set<Server>> entry : serversByGroupName.entrySet()) {
            serverGroups.add(new ServerGroup((String) entry.getKey(), (Set) entry.getValue()));
        }
        return new FabricStateResponse(serverGroups, response.getTtl());
    }

    public FabricStateResponse<Set<ServerGroup>> getServerGroups() throws FabricCommunicationException {
        return getServerGroups("");
    }

    private FabricStateResponse<Set<ShardTable>> getShardTables(int shardMappingId) throws FabricCommunicationException {
        Response tablesResponse = errorSafeCallMethod(METHOD_DUMP_SHARD_TABLES, new Object[]{Integer.valueOf(0), String.valueOf(shardMappingId)});
        Set<ShardTable> tables = new HashSet();
        for (Map<String, ?> rawTable : tablesResponse.getResultSet()) {
            tables.add(new ShardTable((String) rawTable.get(FIELD_SCHEMA_NAME), (String) rawTable.get(FIELD_TABLE_NAME), (String) rawTable.get(FIELD_COLUMN_NAME)));
        }
        return new FabricStateResponse(tables, tablesResponse.getTtl());
    }

    private FabricStateResponse<Set<ShardIndex>> getShardIndices(int shardMappingId) throws FabricCommunicationException {
        Response indexResponse = errorSafeCallMethod(METHOD_DUMP_SHARD_INDEX, new Object[]{Integer.valueOf(0), String.valueOf(shardMappingId)});
        Set<ShardIndex> indices = new HashSet();
        for (Map<String, ?> rawIndexEntry : indexResponse.getResultSet()) {
            String groupName = (String) rawIndexEntry.get(FIELD_GROUP_ID);
            indices.add(new ShardIndex((String) rawIndexEntry.get(FIELD_LOWER_BOUND), Integer.valueOf(((Integer) rawIndexEntry.get(FIELD_SHARD_ID)).intValue()), groupName));
        }
        return new FabricStateResponse(indices, indexResponse.getTtl());
    }

    public FabricStateResponse<Set<ShardMapping>> getShardMappings(String shardMappingIdPattern) throws FabricCommunicationException {
        Response mapsResponse = errorSafeCallMethod(METHOD_DUMP_SHARD_MAPS, new Object[]{Integer.valueOf(0), shardMappingIdPattern});
        long minExpireTimeMillis = System.currentTimeMillis() + TimeUnit.SECONDS.toMillis((long) mapsResponse.getTtl());
        int baseTtl = mapsResponse.getTtl();
        Set<ShardMapping> mappings = new HashSet();
        for (Map<String, ?> rawMapping : mapsResponse.getResultSet()) {
            int mappingId = ((Integer) rawMapping.get(FIELD_MAPPING_ID)).intValue();
            ShardingType shardingType = ShardingType.valueOf((String) rawMapping.get(FIELD_TYPE_NAME));
            String globalGroupName = (String) rawMapping.get(FIELD_GLOBAL_GROUP_ID);
            FabricStateResponse<Set<ShardTable>> tables = getShardTables(mappingId);
            FabricStateResponse<Set<ShardIndex>> indices = getShardIndices(mappingId);
            if (tables.getExpireTimeMillis() < minExpireTimeMillis) {
                minExpireTimeMillis = tables.getExpireTimeMillis();
            }
            if (indices.getExpireTimeMillis() < minExpireTimeMillis) {
                minExpireTimeMillis = indices.getExpireTimeMillis();
            }
            mappings.add(new ShardMappingFactory().createShardMapping(mappingId, shardingType, globalGroupName, (Set) tables.getData(), (Set) indices.getData()));
        }
        return new FabricStateResponse(mappings, baseTtl, minExpireTimeMillis);
    }

    public FabricStateResponse<Set<ShardMapping>> getShardMappings() throws FabricCommunicationException {
        return getShardMappings("");
    }

    public void createGroup(String groupName) throws FabricCommunicationException {
        errorSafeCallMethod(METHOD_GROUP_CREATE, new Object[]{groupName});
    }

    public void destroyGroup(String groupName) throws FabricCommunicationException {
        errorSafeCallMethod(METHOD_GROUP_DESTROY, new Object[]{groupName});
    }

    public void createServerInGroup(String groupName, String hostname, int port) throws FabricCommunicationException {
        String str = METHOD_GROUP_ADD;
        Object[] objArr = new Object[2];
        objArr[0] = groupName;
        StringBuilder stringBuilder = new StringBuilder();
        stringBuilder.append(hostname);
        stringBuilder.append(":");
        stringBuilder.append(port);
        objArr[1] = stringBuilder.toString();
        errorSafeCallMethod(str, objArr);
    }

    public int createShardMapping(ShardingType type, String globalGroupName) throws FabricCommunicationException {
        return ((Integer) ((Map) errorSafeCallMethod(METHOD_SHARDING_CREATE_DEFINITION, new Object[]{type.toString(), globalGroupName}).getResultSet().get(0)).get(FIELD_RESULT)).intValue();
    }

    public void createShardTable(int shardMappingId, String database, String table, String column) throws FabricCommunicationException {
        String str = METHOD_SHARDING_ADD_TABLE;
        Object[] objArr = new Object[3];
        objArr[0] = Integer.valueOf(shardMappingId);
        StringBuilder stringBuilder = new StringBuilder();
        stringBuilder.append(database);
        stringBuilder.append(".");
        stringBuilder.append(table);
        objArr[1] = stringBuilder.toString();
        objArr[2] = column;
        errorSafeCallMethod(str, objArr);
    }

    public void createShardIndex(int shardMappingId, String groupNameLowerBoundList) throws FabricCommunicationException {
        errorSafeCallMethod(METHOD_SHARDING_ADD_SHARD, new Object[]{Integer.valueOf(shardMappingId), groupNameLowerBoundList, "ENABLED"});
    }

    public void addServerToGroup(String groupName, String hostname, int port) throws FabricCommunicationException {
        String str = METHOD_GROUP_ADD;
        Object[] objArr = new Object[2];
        objArr[0] = groupName;
        StringBuilder stringBuilder = new StringBuilder();
        stringBuilder.append(hostname);
        stringBuilder.append(":");
        stringBuilder.append(port);
        objArr[1] = stringBuilder.toString();
        errorSafeCallMethod(str, objArr);
    }

    public void removeServerFromGroup(String groupName, String hostname, int port) throws FabricCommunicationException {
        String str = METHOD_GROUP_REMOVE;
        Object[] objArr = new Object[2];
        objArr[0] = groupName;
        StringBuilder stringBuilder = new StringBuilder();
        stringBuilder.append(hostname);
        stringBuilder.append(":");
        stringBuilder.append(port);
        objArr[1] = stringBuilder.toString();
        errorSafeCallMethod(str, objArr);
    }

    public void promoteServerInGroup(String groupName, String hostname, int port) throws FabricCommunicationException {
        for (Server s : getServerGroup(groupName).getServers()) {
            if (s.getHostname().equals(hostname) && s.getPort() == port) {
                errorSafeCallMethod(METHOD_GROUP_PROMOTE, new Object[]{groupName, s.getUuid()});
                return;
            }
        }
    }

    public void reportServerError(Server server, String errorDescription, boolean forceFaulty) throws FabricCommunicationException {
        String reporter = THREAT_REPORTER_NAME;
        String command = METHOD_THREAT_REPORT_ERROR;
        if (forceFaulty) {
            command = METHOD_THREAT_REPORT_FAILURE;
        }
        errorSafeCallMethod(command, new Object[]{server.getUuid(), reporter, errorDescription});
    }
}
