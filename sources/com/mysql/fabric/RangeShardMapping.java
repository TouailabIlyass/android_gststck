package com.mysql.fabric;

import java.util.Comparator;
import java.util.Set;
import java.util.TreeSet;

public class RangeShardMapping extends ShardMapping {

    private static class RangeShardIndexSorter implements Comparator<ShardIndex> {
        public static final RangeShardIndexSorter instance = new RangeShardIndexSorter();

        private RangeShardIndexSorter() {
        }

        public int compare(ShardIndex i1, ShardIndex i2) {
            return Integer.valueOf(Integer.parseInt(i2.getBound())).compareTo(Integer.valueOf(Integer.parseInt(i1.getBound())));
        }
    }

    public RangeShardMapping(int mappingId, ShardingType shardingType, String globalGroupName, Set<ShardTable> shardTables, Set<ShardIndex> shardIndices) {
        super(mappingId, shardingType, globalGroupName, shardTables, new TreeSet(RangeShardIndexSorter.instance));
        this.shardIndices.addAll(shardIndices);
    }

    protected ShardIndex getShardIndexForKey(String stringKey) {
        Integer key = Integer.valueOf(-1);
        key = Integer.valueOf(Integer.parseInt(stringKey));
        for (ShardIndex i : this.shardIndices) {
            if (key.intValue() >= Integer.valueOf(i.getBound()).intValue()) {
                return i;
            }
        }
        return null;
    }
}
