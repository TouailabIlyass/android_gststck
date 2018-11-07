package com.mysql.jdbc;

interface WriterWatcher {
    void writerClosed(WatchableWriter watchableWriter);
}
