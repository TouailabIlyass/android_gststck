package com.mysql.fabric;

import java.util.concurrent.TimeUnit;

public class FabricStateResponse<T> {
    private T data;
    private long expireTimeMillis;
    private int secsTtl;

    public FabricStateResponse(T data, int secsTtl) {
        this.data = data;
        this.secsTtl = secsTtl;
        this.expireTimeMillis = System.currentTimeMillis() + TimeUnit.SECONDS.toMillis((long) secsTtl);
    }

    public FabricStateResponse(T data, int secsTtl, long presetExpireTimeMillis) {
        this.data = data;
        this.secsTtl = secsTtl;
        this.expireTimeMillis = presetExpireTimeMillis;
    }

    public T getData() {
        return this.data;
    }

    public int getTtl() {
        return this.secsTtl;
    }

    public long getExpireTimeMillis() {
        return this.expireTimeMillis;
    }
}
