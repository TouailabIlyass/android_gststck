package com.mysql.jdbc;

import java.util.Set;

public interface CacheAdapter<K, V> {
    V get(K k);

    void invalidate(K k);

    void invalidateAll();

    void invalidateAll(Set<K> set);

    void put(K k, V v);
}
