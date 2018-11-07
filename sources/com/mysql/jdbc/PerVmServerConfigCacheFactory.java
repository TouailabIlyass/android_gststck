package com.mysql.jdbc;

import java.sql.SQLException;
import java.util.Map;
import java.util.Properties;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

public class PerVmServerConfigCacheFactory implements CacheAdapterFactory<String, Map<String, String>> {
    static final ConcurrentHashMap<String, Map<String, String>> serverConfigByUrl = new ConcurrentHashMap();
    private static final CacheAdapter<String, Map<String, String>> serverConfigCache = new C04501();

    /* renamed from: com.mysql.jdbc.PerVmServerConfigCacheFactory$1 */
    static class C04501 implements CacheAdapter<String, Map<String, String>> {
        public Map<String, String> get(String key) {
            return (Map) PerVmServerConfigCacheFactory.serverConfigByUrl.get(key);
        }

        public void put(String key, Map<String, String> value) {
            PerVmServerConfigCacheFactory.serverConfigByUrl.putIfAbsent(key, value);
        }

        public void invalidate(String key) {
            PerVmServerConfigCacheFactory.serverConfigByUrl.remove(key);
        }

        public void invalidateAll(Set<String> keys) {
            for (String key : keys) {
                PerVmServerConfigCacheFactory.serverConfigByUrl.remove(key);
            }
        }

        C04501() {
        }

        public void invalidateAll() {
            PerVmServerConfigCacheFactory.serverConfigByUrl.clear();
        }
    }

    public CacheAdapter<String, Map<String, String>> getInstance(Connection forConn, String url, int cacheMaxSize, int maxKeySize, Properties connectionProperties) throws SQLException {
        return serverConfigCache;
    }
}
