package com.mysql.jdbc;

import java.lang.ref.Reference;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadFactory;

public class AbandonedConnectionCleanupThread implements Runnable {
    private static final ExecutorService cleanupThreadExcecutorService = Executors.newSingleThreadExecutor(new C03381());
    static Thread threadRef = null;

    /* renamed from: com.mysql.jdbc.AbandonedConnectionCleanupThread$1 */
    static class C03381 implements ThreadFactory {
        C03381() {
        }

        public Thread newThread(Runnable r) {
            Thread t = new Thread(r, "Abandoned connection cleanup thread");
            t.setDaemon(true);
            t.setContextClassLoader(AbandonedConnectionCleanupThread.class.getClassLoader());
            AbandonedConnectionCleanupThread.threadRef = t;
            return t;
        }
    }

    static {
        cleanupThreadExcecutorService.execute(new AbandonedConnectionCleanupThread());
    }

    private AbandonedConnectionCleanupThread() {
    }

    public void run() {
        AbandonedConnectionCleanupThread this = this;
        while (true) {
            Reference<? extends ConnectionImpl> ref;
            try {
                checkContextClassLoaders();
                ref = NonRegisteringDriver.refQueue.remove(5000);
                if (ref != null) {
                    ((ConnectionPhantomReference) ref).cleanup();
                    NonRegisteringDriver.connectionPhantomRefs.remove(ref);
                } else {
                    continue;
                }
            } catch (InterruptedException e) {
                threadRef = null;
                return;
            } catch (Exception e2) {
            } catch (Throwable th) {
                NonRegisteringDriver.connectionPhantomRefs.remove(ref);
            }
        }
    }

    private void checkContextClassLoaders() {
        try {
            threadRef.getContextClassLoader().getResource("");
        } catch (Throwable th) {
            uncheckedShutdown();
        }
    }

    private static boolean consistentClassLoaders() {
        ClassLoader callerCtxClassLoader = Thread.currentThread().getContextClassLoader();
        ClassLoader threadCtxClassLoader = threadRef.getContextClassLoader();
        return (callerCtxClassLoader == null || threadCtxClassLoader == null || callerCtxClassLoader != threadCtxClassLoader) ? false : true;
    }

    public static void checkedShutdown() {
        shutdown(true);
    }

    public static void uncheckedShutdown() {
        shutdown(false);
    }

    private static void shutdown(boolean checked) {
        if (!checked || consistentClassLoaders()) {
            cleanupThreadExcecutorService.shutdownNow();
        }
    }

    @Deprecated
    public static void shutdown() {
        checkedShutdown();
    }
}
