package com.mysql.jdbc;

import java.sql.SQLException;
import java.util.Iterator;

public abstract class IterateBlock<T> {
    IteratorWithCleanup<T> iteratorWithCleanup;
    Iterator<T> javaIterator;
    boolean stopIterating = false;

    abstract void forEach(T t) throws SQLException;

    IterateBlock(IteratorWithCleanup<T> i) {
        this.iteratorWithCleanup = i;
        this.javaIterator = null;
    }

    IterateBlock(Iterator<T> i) {
        this.javaIterator = i;
        this.iteratorWithCleanup = null;
    }

    public void doForAll() throws SQLException {
        if (this.iteratorWithCleanup != null) {
            do {
                try {
                    if (!this.iteratorWithCleanup.hasNext()) {
                        break;
                    }
                    forEach(this.iteratorWithCleanup.next());
                } catch (Throwable th) {
                    this.iteratorWithCleanup.close();
                }
            } while (!this.stopIterating);
            this.iteratorWithCleanup.close();
            return;
        }
        while (this.javaIterator.hasNext()) {
            forEach(this.javaIterator.next());
            if (this.stopIterating) {
                break;
            }
        }
        IterateBlock iterateBlock = this;
    }

    public final boolean fullIteration() {
        return this.stopIterating ^ 1;
    }
}
