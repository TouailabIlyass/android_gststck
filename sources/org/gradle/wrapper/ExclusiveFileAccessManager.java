package org.gradle.wrapper;

import java.io.Closeable;
import java.io.File;
import java.io.RandomAccessFile;
import java.nio.channels.FileChannel;
import java.nio.channels.FileLock;
import java.util.concurrent.Callable;

public class ExclusiveFileAccessManager {
    public static final String LOCK_FILE_SUFFIX = ".lck";
    private final int pollIntervalMs;
    private final int timeoutMs;

    public ExclusiveFileAccessManager(int timeoutMs, int pollIntervalMs) {
        this.timeoutMs = timeoutMs;
        this.pollIntervalMs = pollIntervalMs;
    }

    public <T> T access(File exclusiveFile, Callable<T> task) {
        File parentFile = exclusiveFile.getParentFile();
        StringBuilder stringBuilder = new StringBuilder();
        stringBuilder.append(exclusiveFile.getName());
        stringBuilder.append(LOCK_FILE_SUFFIX);
        File lockFile = new File(parentFile, stringBuilder.toString());
        lockFile.getParentFile().mkdirs();
        RandomAccessFile randomAccessFile = null;
        FileLock lock = null;
        FileChannel channel = null;
        try {
            long startAt = System.currentTimeMillis();
            while (lock == null && System.currentTimeMillis() < startAt + ((long) this.timeoutMs)) {
                randomAccessFile = new RandomAccessFile(lockFile, "rw");
                channel = randomAccessFile.getChannel();
                lock = channel.tryLock();
                if (lock == null) {
                    maybeCloseQuietly(channel);
                    maybeCloseQuietly(randomAccessFile);
                    Thread.sleep((long) this.pollIntervalMs);
                }
            }
            if (lock == null) {
                StringBuilder stringBuilder2 = new StringBuilder();
                stringBuilder2.append("Timeout of ");
                stringBuilder2.append(this.timeoutMs);
                stringBuilder2.append(" reached waiting for exclusive access to file: ");
                stringBuilder2.append(exclusiveFile.getAbsolutePath());
                throw new RuntimeException(stringBuilder2.toString());
            }
            T call = task.call();
            lock.release();
            maybeCloseQuietly(channel);
            channel = null;
            maybeCloseQuietly(randomAccessFile);
            maybeCloseQuietly(null);
            maybeCloseQuietly(null);
            return call;
        } catch (Exception e) {
            try {
                if (e instanceof RuntimeException) {
                    throw ((RuntimeException) e);
                }
                throw new RuntimeException(e);
            } catch (Throwable th) {
                maybeCloseQuietly(channel);
                maybeCloseQuietly(randomAccessFile);
            }
        } catch (Throwable th2) {
            lock.release();
            maybeCloseQuietly(channel);
            maybeCloseQuietly(randomAccessFile);
        }
    }

    private static void maybeCloseQuietly(Closeable closeable) {
        if (closeable != null) {
            try {
                closeable.close();
            } catch (Exception e) {
            }
        }
    }
}
