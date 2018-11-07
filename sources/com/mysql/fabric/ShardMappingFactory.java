package com.mysql.fabric;

import java.util.Set;

public class ShardMappingFactory {
    public ShardMapping createShardMapping(int mappingId, ShardingType shardingType, String globalGroupName, Set<ShardTable> shardTables, Set<ShardIndex> shardIndices) {
        switch (shardingType) {
            case RANGE:
                return new RangeShardMapping(mappingId, shardingType, globalGroupName, shardTables, shardIndices);
            case HASH:
                return new HashShardMapping(mappingId, shardingType, globalGroupName, shardTables, shardIndices);
            default:
                throw new IllegalArgumentException("Invalid ShardingType");
        }
    }
}
