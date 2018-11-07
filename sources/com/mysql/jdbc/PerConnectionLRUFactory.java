package com.mysql.jdbc;

import com.mysql.jdbc.PreparedStatement.ParseInfo;
import com.mysql.jdbc.util.LRUCache;
import java.sql.SQLException;
import java.util.Properties;
import java.util.Set;

public class PerConnectionLRUFactory implements CacheAdapterFactory<String, ParseInfo> {

    class PerConnectionLRU implements CacheAdapter<String, ParseInfo> {
        private final LRUCache<String, ParseInfo> cache;
        private final int cacheSqlLimit;
        private final Connection conn;

        protected PerConnectionLRU(Connection forConnection, int cacheMaxSize, int maxKeySize) {
            int cacheSize = cacheMaxSize;
            this.cacheSqlLimit = maxKeySize;
            this.cache = new LRUCache(cacheSize);
            this.conn = forConnection;
        }

        public ParseInfo get(String key) {
            if (key != null) {
                if (key.length() <= this.cacheSqlLimit) {
                    ParseInfo parseInfo;
                    synchronized (this.conn.getConnectionMutex()) {
                        parseInfo = (ParseInfo) this.cache.get(key);
                    }
                    return parseInfo;
                }
            }
            return null;
        }

        public void put(String key, ParseInfo value) {
            if (key != null) {
                if (key.length() <= this.cacheSqlLimit) {
                    synchronized (this.conn.getConnectionMutex()) {
                        this.cache.put(key, value);
                    }
                }
            }
        }

        public void invalidate(String key) {
            synchronized (this.conn.getConnectionMutex()) {
                this.cache.remove(key);
            }
        }

        public void invalidateAll(Set<String> keys) {
            synchronized (this.conn.getConnectionMutex()) {
                for (String key : keys) {
                    this.cache.remove(key);
                }
            }
        }

        public void invalidateAll() {
            synchronized (this.conn.getConnectionMutex()) {
                this.cache.clear();
            }
        }
    }

    public CacheAdapter<String, ParseInfo> getInstance(Connection forConnection, String url, int cacheMaxSize, int maxKeySize, Properties connectionProperties) throws SQLException {
        return new PerConnectionLRU(forConnection, cacheMaxSize, maxKeySize);
    }
}
