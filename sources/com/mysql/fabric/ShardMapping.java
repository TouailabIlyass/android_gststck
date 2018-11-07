package com.mysql.fabric;

import java.util.Collections;
import java.util.Set;

public abstract class ShardMapping {
    private String globalGroupName;
    private int mappingId;
    protected Set<ShardIndex> shardIndices;
    protected Set<ShardTable> shardTables;
    private ShardingType shardingType;

    protected abstract ShardIndex getShardIndexForKey(String str);

    public ShardMapping(int mappingId, ShardingType shardingType, String globalGroupName, Set<ShardTable> shardTables, Set<ShardIndex> shardIndices) {
        this.mappingId = mappingId;
        this.shardingType = shardingType;
        this.globalGroupName = globalGroupName;
        this.shardTables = shardTables;
        this.shardIndices = shardIndices;
    }

    public String getGroupNameForKey(String key) {
        return getShardIndexForKey(key).getGroupName();
    }

    public int getMappingId() {
        return this.mappingId;
    }

    public ShardingType getShardingType() {
        return this.shardingType;
    }

    public String getGlobalGroupName() {
        return this.globalGroupName;
    }

    public Set<ShardTable> getShardTables() {
        return Collections.unmodifiableSet(this.shardTables);
    }

    public Set<ShardIndex> getShardIndices() {
        return Collections.unmodifiableSet(this.shardIndices);
    }
}
