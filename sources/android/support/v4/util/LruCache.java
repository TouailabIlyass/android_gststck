package android.support.v4.util;

import java.util.LinkedHashMap;
import java.util.Locale;
import java.util.Map;

public class LruCache<K, V> {
    private int createCount;
    private int evictionCount;
    private int hitCount;
    private final LinkedHashMap<K, V> map;
    private int maxSize;
    private int missCount;
    private int putCount;
    private int size;

    public LruCache(int maxSize) {
        if (maxSize <= 0) {
            throw new IllegalArgumentException("maxSize <= 0");
        }
        this.maxSize = maxSize;
        this.map = new LinkedHashMap(0, 0.75f, true);
    }

    public void resize(int maxSize) {
        if (maxSize <= 0) {
            throw new IllegalArgumentException("maxSize <= 0");
        }
        synchronized (this) {
            this.maxSize = maxSize;
        }
        trimToSize(maxSize);
    }

    /* JADX WARNING: inconsistent code. */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    public final V get(K r6) {
        /*
        r5 = this;
        if (r6 != 0) goto L_0x000a;
    L_0x0002:
        r0 = new java.lang.NullPointerException;
        r1 = "key == null";
        r0.<init>(r1);
        throw r0;
    L_0x000a:
        monitor-enter(r5);
        r0 = 0;
        r1 = r5.map;	 Catch:{ all -> 0x005a }
        r1 = r1.get(r6);	 Catch:{ all -> 0x005a }
        if (r1 == 0) goto L_0x001c;
    L_0x0014:
        r0 = r5.hitCount;	 Catch:{ all -> 0x0060 }
        r0 = r0 + 1;
        r5.hitCount = r0;	 Catch:{ all -> 0x0060 }
        monitor-exit(r5);	 Catch:{ all -> 0x0060 }
        return r1;
    L_0x001c:
        r2 = r5.missCount;	 Catch:{ all -> 0x0060 }
        r2 = r2 + 1;
        r5.missCount = r2;	 Catch:{ all -> 0x0060 }
        monitor-exit(r5);	 Catch:{ all -> 0x0060 }
        r2 = r5.create(r6);
        if (r2 != 0) goto L_0x002a;
    L_0x0029:
        return r0;
    L_0x002a:
        monitor-enter(r5);
        r0 = r5.createCount;	 Catch:{ all -> 0x0057 }
        r0 = r0 + 1;
        r5.createCount = r0;	 Catch:{ all -> 0x0057 }
        r0 = r5.map;	 Catch:{ all -> 0x0057 }
        r0 = r0.put(r6, r2);	 Catch:{ all -> 0x0057 }
        r1 = r0;
        if (r1 == 0) goto L_0x0040;
    L_0x003a:
        r0 = r5.map;	 Catch:{ all -> 0x0057 }
        r0.put(r6, r1);	 Catch:{ all -> 0x0057 }
        goto L_0x0049;
    L_0x0040:
        r0 = r5.size;	 Catch:{ all -> 0x0057 }
        r3 = r5.safeSizeOf(r6, r2);	 Catch:{ all -> 0x0057 }
        r0 = r0 + r3;
        r5.size = r0;	 Catch:{ all -> 0x0057 }
    L_0x0049:
        monitor-exit(r5);	 Catch:{ all -> 0x0057 }
        if (r1 == 0) goto L_0x0051;
    L_0x004c:
        r0 = 0;
        r5.entryRemoved(r0, r6, r2, r1);
        return r1;
    L_0x0051:
        r0 = r5.maxSize;
        r5.trimToSize(r0);
        return r2;
    L_0x0057:
        r0 = move-exception;
        monitor-exit(r5);	 Catch:{ all -> 0x0057 }
        throw r0;
    L_0x005a:
        r1 = move-exception;
        r4 = r1;
        r1 = r0;
        r0 = r4;
    L_0x005e:
        monitor-exit(r5);	 Catch:{ all -> 0x0060 }
        throw r0;
    L_0x0060:
        r0 = move-exception;
        goto L_0x005e;
        */
        throw new UnsupportedOperationException("Method not decompiled: android.support.v4.util.LruCache.get(java.lang.Object):V");
    }

    public final V put(K key, V value) {
        if (key != null) {
            if (value != null) {
                V previous;
                synchronized (this) {
                    this.putCount++;
                    this.size += safeSizeOf(key, value);
                    previous = this.map.put(key, value);
                    if (previous != null) {
                        this.size -= safeSizeOf(key, previous);
                    }
                }
                if (previous != null) {
                    entryRemoved(false, key, previous, value);
                }
                trimToSize(this.maxSize);
                return previous;
            }
        }
        throw new NullPointerException("key == null || value == null");
    }

    /* JADX WARNING: inconsistent code. */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    public void trimToSize(int r7) {
        /*
        r6 = this;
        r0 = 0;
        r1 = r0;
        r2 = r1;
    L_0x0003:
        monitor-enter(r6);
        r3 = r6.size;	 Catch:{ all -> 0x0076 }
        if (r3 < 0) goto L_0x0057;
    L_0x0008:
        r3 = r6.map;	 Catch:{ all -> 0x0076 }
        r3 = r3.isEmpty();	 Catch:{ all -> 0x0076 }
        if (r3 == 0) goto L_0x0015;
    L_0x0010:
        r3 = r6.size;	 Catch:{ all -> 0x0076 }
        if (r3 == 0) goto L_0x0015;
    L_0x0014:
        goto L_0x0057;
    L_0x0015:
        r3 = r6.size;	 Catch:{ all -> 0x0076 }
        if (r3 <= r7) goto L_0x0055;
    L_0x0019:
        r3 = r6.map;	 Catch:{ all -> 0x0076 }
        r3 = r3.isEmpty();	 Catch:{ all -> 0x0076 }
        if (r3 == 0) goto L_0x0022;
    L_0x0021:
        goto L_0x0055;
    L_0x0022:
        r3 = r6.map;	 Catch:{ all -> 0x0076 }
        r3 = r3.entrySet();	 Catch:{ all -> 0x0076 }
        r3 = r3.iterator();	 Catch:{ all -> 0x0076 }
        r3 = r3.next();	 Catch:{ all -> 0x0076 }
        r3 = (java.util.Map.Entry) r3;	 Catch:{ all -> 0x0076 }
        r4 = r3.getKey();	 Catch:{ all -> 0x0076 }
        r1 = r4;
        r4 = r3.getValue();	 Catch:{ all -> 0x0076 }
        r2 = r4;
        r4 = r6.map;	 Catch:{ all -> 0x0076 }
        r4.remove(r1);	 Catch:{ all -> 0x0076 }
        r4 = r6.size;	 Catch:{ all -> 0x0076 }
        r5 = r6.safeSizeOf(r1, r2);	 Catch:{ all -> 0x0076 }
        r4 = r4 - r5;
        r6.size = r4;	 Catch:{ all -> 0x0076 }
        r4 = r6.evictionCount;	 Catch:{ all -> 0x0076 }
        r5 = 1;
        r4 = r4 + r5;
        r6.evictionCount = r4;	 Catch:{ all -> 0x0076 }
        monitor-exit(r6);	 Catch:{ all -> 0x0076 }
        r6.entryRemoved(r5, r1, r2, r0);
        goto L_0x0003;
    L_0x0055:
        monitor-exit(r6);	 Catch:{ all -> 0x0076 }
        return;
    L_0x0057:
        r0 = new java.lang.IllegalStateException;	 Catch:{ all -> 0x0076 }
        r3 = new java.lang.StringBuilder;	 Catch:{ all -> 0x0076 }
        r3.<init>();	 Catch:{ all -> 0x0076 }
        r4 = r6.getClass();	 Catch:{ all -> 0x0076 }
        r4 = r4.getName();	 Catch:{ all -> 0x0076 }
        r3.append(r4);	 Catch:{ all -> 0x0076 }
        r4 = ".sizeOf() is reporting inconsistent results!";
        r3.append(r4);	 Catch:{ all -> 0x0076 }
        r3 = r3.toString();	 Catch:{ all -> 0x0076 }
        r0.<init>(r3);	 Catch:{ all -> 0x0076 }
        throw r0;	 Catch:{ all -> 0x0076 }
    L_0x0076:
        r0 = move-exception;
        monitor-exit(r6);	 Catch:{ all -> 0x0076 }
        throw r0;
        */
        throw new UnsupportedOperationException("Method not decompiled: android.support.v4.util.LruCache.trimToSize(int):void");
    }

    /* JADX WARNING: inconsistent code. */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    public final V remove(K r6) {
        /*
        r5 = this;
        if (r6 != 0) goto L_0x000a;
    L_0x0002:
        r0 = new java.lang.NullPointerException;
        r1 = "key == null";
        r0.<init>(r1);
        throw r0;
    L_0x000a:
        monitor-enter(r5);
        r0 = 0;
        r1 = r5.map;	 Catch:{ all -> 0x0025 }
        r1 = r1.remove(r6);	 Catch:{ all -> 0x0025 }
        if (r1 == 0) goto L_0x001d;
    L_0x0014:
        r2 = r5.size;	 Catch:{ all -> 0x002b }
        r3 = r5.safeSizeOf(r6, r1);	 Catch:{ all -> 0x002b }
        r2 = r2 - r3;
        r5.size = r2;	 Catch:{ all -> 0x002b }
    L_0x001d:
        monitor-exit(r5);	 Catch:{ all -> 0x002b }
        if (r1 == 0) goto L_0x0024;
    L_0x0020:
        r2 = 0;
        r5.entryRemoved(r2, r6, r1, r0);
    L_0x0024:
        return r1;
    L_0x0025:
        r1 = move-exception;
        r4 = r1;
        r1 = r0;
        r0 = r4;
    L_0x0029:
        monitor-exit(r5);	 Catch:{ all -> 0x002b }
        throw r0;
    L_0x002b:
        r0 = move-exception;
        goto L_0x0029;
        */
        throw new UnsupportedOperationException("Method not decompiled: android.support.v4.util.LruCache.remove(java.lang.Object):V");
    }

    protected void entryRemoved(boolean evicted, K k, V v, V v2) {
    }

    protected V create(K k) {
        return null;
    }

    private int safeSizeOf(K key, V value) {
        int result = sizeOf(key, value);
        if (result >= 0) {
            return result;
        }
        StringBuilder stringBuilder = new StringBuilder();
        stringBuilder.append("Negative size: ");
        stringBuilder.append(key);
        stringBuilder.append("=");
        stringBuilder.append(value);
        throw new IllegalStateException(stringBuilder.toString());
    }

    protected int sizeOf(K k, V v) {
        return 1;
    }

    public final void evictAll() {
        trimToSize(-1);
    }

    public final synchronized int size() {
        return this.size;
    }

    public final synchronized int maxSize() {
        return this.maxSize;
    }

    public final synchronized int hitCount() {
        return this.hitCount;
    }

    public final synchronized int missCount() {
        return this.missCount;
    }

    public final synchronized int createCount() {
        return this.createCount;
    }

    public final synchronized int putCount() {
        return this.putCount;
    }

    public final synchronized int evictionCount() {
        return this.evictionCount;
    }

    public final synchronized Map<K, V> snapshot() {
        return new LinkedHashMap(this.map);
    }

    public final synchronized String toString() {
        int hitPercent;
        int accesses = this.hitCount + this.missCount;
        hitPercent = accesses != 0 ? (100 * this.hitCount) / accesses : 0;
        return String.format(Locale.US, "LruCache[maxSize=%d,hits=%d,misses=%d,hitRate=%d%%]", new Object[]{Integer.valueOf(this.maxSize), Integer.valueOf(this.hitCount), Integer.valueOf(this.missCount), Integer.valueOf(hitPercent)});
    }
}
