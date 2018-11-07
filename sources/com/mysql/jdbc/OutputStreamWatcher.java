package com.mysql.jdbc;

interface OutputStreamWatcher {
    void streamClosed(WatchableOutputStream watchableOutputStream);
}
