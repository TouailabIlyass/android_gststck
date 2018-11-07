package com.mysql.jdbc;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

public interface SocketMetadata {

    public static class Helper {
        public static final String IS_LOCAL_HOSTNAME_REPLACEMENT_PROPERTY_NAME = "com.mysql.jdbc.test.isLocalHostnameReplacement";

        /* JADX WARNING: inconsistent code. */
        /* Code decompiled incorrectly, please refer to instructions dump. */
        public static boolean isLocallyConnected(com.mysql.jdbc.ConnectionImpl r24) throws java.sql.SQLException {
            /*
            r1 = r24.getId();
            r3 = r24.getMetadataSafeStatement();
            r4 = 0;
            r5 = 0;
            r6 = "com.mysql.jdbc.test.isLocalHostnameReplacement";
            r6 = java.lang.System.getProperty(r6);
            r7 = 1;
            r8 = 0;
            if (r6 == 0) goto L_0x001d;
        L_0x0014:
            r6 = "com.mysql.jdbc.test.isLocalHostnameReplacement";
            r5 = java.lang.System.getProperty(r6);
        L_0x001a:
            r6 = r24;
            goto L_0x0078;
        L_0x001d:
            r6 = r24.getProperties();
            r9 = "com.mysql.jdbc.test.isLocalHostnameReplacement";
            r6 = r6.getProperty(r9);
            if (r6 == 0) goto L_0x0034;
        L_0x0029:
            r6 = r24.getProperties();
            r9 = "com.mysql.jdbc.test.isLocalHostnameReplacement";
            r5 = r6.getProperty(r9);
            goto L_0x001a;
        L_0x0034:
            r6 = findProcessHost(r1, r3);	 Catch:{ all -> 0x0193 }
            r5 = r6;
            if (r5 != 0) goto L_0x0071;
        L_0x003b:
            r6 = r24.getLog();	 Catch:{ all -> 0x0193 }
            r9 = "Connection id %d not found in \"SHOW PROCESSLIST\", assuming 32-bit overflow, using SELECT CONNECTION_ID() instead";
            r10 = new java.lang.Object[r7];	 Catch:{ all -> 0x0193 }
            r11 = java.lang.Long.valueOf(r1);	 Catch:{ all -> 0x0193 }
            r10[r8] = r11;	 Catch:{ all -> 0x0193 }
            r9 = java.lang.String.format(r9, r10);	 Catch:{ all -> 0x0193 }
            r6.logWarn(r9);	 Catch:{ all -> 0x0193 }
            r6 = "SELECT CONNECTION_ID()";
            r6 = r3.executeQuery(r6);	 Catch:{ all -> 0x0193 }
            r4 = r6;
            r6 = r4.next();	 Catch:{ all -> 0x0193 }
            if (r6 == 0) goto L_0x0068;
        L_0x005d:
            r9 = r4.getLong(r7);	 Catch:{ all -> 0x0193 }
            r1 = r9;
            r6 = findProcessHost(r1, r3);	 Catch:{ all -> 0x0193 }
            r5 = r6;
            goto L_0x0071;
        L_0x0068:
            r6 = r24.getLog();	 Catch:{ all -> 0x0193 }
            r9 = "No rows returned for statement \"SELECT CONNECTION_ID()\", local connection check will most likely be incorrect";
            r6.logError(r9);	 Catch:{ all -> 0x0193 }
            r6 = r24;
            r3.close();
        L_0x0078:
            if (r5 == 0) goto L_0x0177;
        L_0x007a:
            r9 = r6.getLog();
            r10 = "Using 'host' value of '%s' to determine locality of connection";
            r11 = new java.lang.Object[r7];
            r11[r8] = r5;
            r10 = java.lang.String.format(r10, r11);
            r9.logDebug(r10);
            r9 = ":";
            r9 = r5.lastIndexOf(r9);
            r10 = -1;
            if (r9 == r10) goto L_0x015d;
        L_0x0094:
            r5 = r5.substring(r8, r9);
            r10 = 0;
            r11 = java.net.InetAddress.getAllByName(r5);	 Catch:{ UnknownHostException -> 0x0141 }
            r12 = r6.getIO();	 Catch:{ UnknownHostException -> 0x0141 }
            r12 = r12.mysqlConnection;	 Catch:{ UnknownHostException -> 0x0141 }
            r12 = r12.getRemoteSocketAddress();	 Catch:{ UnknownHostException -> 0x0141 }
            r13 = r12 instanceof java.net.InetSocketAddress;	 Catch:{ UnknownHostException -> 0x0141 }
            if (r13 == 0) goto L_0x0124;
        L_0x00ab:
            r13 = r12;
            r13 = (java.net.InetSocketAddress) r13;	 Catch:{ UnknownHostException -> 0x0141 }
            r13 = r13.getAddress();	 Catch:{ UnknownHostException -> 0x0141 }
            r14 = r11;
            r15 = r14.length;	 Catch:{ UnknownHostException -> 0x0141 }
            r16 = r8;
        L_0x00b6:
            r17 = r16;
            r7 = r17;
            if (r7 >= r15) goto L_0x011d;
        L_0x00bc:
            r16 = r14[r7];	 Catch:{ UnknownHostException -> 0x0141 }
            r18 = r16;
            r8 = r18;
            r16 = r8.equals(r13);	 Catch:{ UnknownHostException -> 0x0141 }
            r19 = r3;
            if (r16 == 0) goto L_0x00f6;
        L_0x00ca:
            r3 = r6.getLog();	 Catch:{ UnknownHostException -> 0x00ef }
            r20 = r4;
            r4 = "Locally connected - HostAddress(%s).equals(whereIconnectedTo({%s})";
            r21 = r9;
            r9 = 2;
            r9 = new java.lang.Object[r9];	 Catch:{ UnknownHostException -> 0x013e }
            r16 = 0;
            r9[r16] = r8;	 Catch:{ UnknownHostException -> 0x013e }
            r16 = 1;
            r9[r16] = r13;	 Catch:{ UnknownHostException -> 0x013e }
            r4 = java.lang.String.format(r4, r9);	 Catch:{ UnknownHostException -> 0x013e }
            r3.logDebug(r4);	 Catch:{ UnknownHostException -> 0x013e }
            r3 = 1;
            r10 = r3;
            goto L_0x0123;
        L_0x00ea:
            r0 = move-exception;
            r21 = r9;
            r3 = r0;
            goto L_0x0149;
        L_0x00ef:
            r0 = move-exception;
            r20 = r4;
            r21 = r9;
            r3 = r0;
            goto L_0x0149;
        L_0x00f6:
            r20 = r4;
            r21 = r9;
            r3 = r6.getLog();	 Catch:{ UnknownHostException -> 0x013e }
            r4 = "Attempted locally connected check failed - ! HostAddress(%s).equals(whereIconnectedTo(%s)";
            r9 = 2;
            r9 = new java.lang.Object[r9];	 Catch:{ UnknownHostException -> 0x013e }
            r16 = 0;
            r9[r16] = r8;	 Catch:{ UnknownHostException -> 0x013e }
            r16 = 1;
            r9[r16] = r13;	 Catch:{ UnknownHostException -> 0x013e }
            r4 = java.lang.String.format(r4, r9);	 Catch:{ UnknownHostException -> 0x013e }
            r3.logDebug(r4);	 Catch:{ UnknownHostException -> 0x013e }
            r16 = r7 + 1;
            r3 = r19;
            r4 = r20;
            r9 = r21;
            r7 = 1;
            r8 = 0;
            goto L_0x00b6;
        L_0x011d:
            r19 = r3;
            r20 = r4;
            r21 = r9;
        L_0x0123:
            goto L_0x013d;
        L_0x0124:
            r19 = r3;
            r20 = r4;
            r21 = r9;
            r3 = "Remote socket address %s is not an inet socket address";
            r4 = 1;
            r7 = new java.lang.Object[r4];	 Catch:{ UnknownHostException -> 0x013e }
            r4 = 0;
            r7[r4] = r12;	 Catch:{ UnknownHostException -> 0x013e }
            r3 = java.lang.String.format(r3, r7);	 Catch:{ UnknownHostException -> 0x013e }
            r4 = r6.getLog();	 Catch:{ UnknownHostException -> 0x013e }
            r4.logDebug(r3);	 Catch:{ UnknownHostException -> 0x013e }
        L_0x013d:
            return r10;
        L_0x013e:
            r0 = move-exception;
            r3 = r0;
            goto L_0x0149;
        L_0x0141:
            r0 = move-exception;
            r19 = r3;
            r20 = r4;
            r21 = r9;
            r3 = r0;
        L_0x0149:
            r4 = r6.getLog();
            r7 = "Connection.CantDetectLocalConnect";
            r8 = 1;
            r8 = new java.lang.Object[r8];
            r9 = 0;
            r8[r9] = r5;
            r7 = com.mysql.jdbc.Messages.getString(r7, r8);
            r4.logWarn(r7, r3);
            return r9;
        L_0x015d:
            r19 = r3;
            r20 = r4;
            r21 = r9;
            r9 = r8;
            r8 = r7;
            r3 = r6.getLog();
            r4 = "No port number present in 'host' from SHOW PROCESSLIST '%s', unable to determine whether locally connected";
            r7 = new java.lang.Object[r8];
            r7[r9] = r5;
            r4 = java.lang.String.format(r4, r7);
            r3.logWarn(r4);
            return r9;
        L_0x0177:
            r19 = r3;
            r20 = r4;
            r9 = r8;
            r8 = r7;
            r3 = r6.getLog();
            r4 = "Cannot find process listing for connection %d in SHOW PROCESSLIST output, unable to determine if locally connected";
            r7 = new java.lang.Object[r8];
            r8 = java.lang.Long.valueOf(r1);
            r7[r9] = r8;
            r4 = java.lang.String.format(r4, r7);
            r3.logWarn(r4);
            return r9;
        L_0x0193:
            r0 = move-exception;
            r6 = r5;
            r22 = r1;
            r1 = r0;
            r2 = r4;
            r4 = r22;
            r7 = r24;
            r3.close();
            throw r1;
            */
            throw new UnsupportedOperationException("Method not decompiled: com.mysql.jdbc.SocketMetadata.Helper.isLocallyConnected(com.mysql.jdbc.ConnectionImpl):boolean");
        }

        private static String findProcessHost(long threadId, Statement processListStmt) throws SQLException {
            ResultSet rs = processListStmt.executeQuery("SHOW PROCESSLIST");
            while (rs.next()) {
                if (threadId == rs.getLong(1)) {
                    return rs.getString(3);
                }
            }
            return null;
        }
    }

    boolean isLocallyConnected(ConnectionImpl connectionImpl) throws SQLException;
}
