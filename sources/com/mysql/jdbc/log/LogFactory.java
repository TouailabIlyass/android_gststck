package com.mysql.jdbc.log;

public class LogFactory {
    /* JADX WARNING: inconsistent code. */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    public static com.mysql.jdbc.log.Log getLogger(java.lang.String r5, java.lang.String r6, com.mysql.jdbc.ExceptionInterceptor r7) throws java.sql.SQLException {
        /*
        if (r5 != 0) goto L_0x000b;
    L_0x0002:
        r0 = "Logger class can not be NULL";
        r1 = "S1009";
        r0 = com.mysql.jdbc.SQLError.createSQLException(r0, r1, r7);
        throw r0;
    L_0x000b:
        if (r6 != 0) goto L_0x0016;
    L_0x000d:
        r0 = "Logger instance name can not be NULL";
        r1 = "S1009";
        r0 = com.mysql.jdbc.SQLError.createSQLException(r0, r1, r7);
        throw r0;
    L_0x0016:
        r0 = 0;
        r1 = java.lang.Class.forName(r5);	 Catch:{ ClassNotFoundException -> 0x002a, NoSuchMethodException -> 0x0027, InstantiationException -> 0x0024, InvocationTargetException -> 0x0021, IllegalAccessException -> 0x001f, ClassCastException -> 0x001d }
        r0 = r1;
        goto L_0x004a;
    L_0x001d:
        r0 = move-exception;
        goto L_0x0061;
    L_0x001f:
        r0 = move-exception;
        goto L_0x0090;
    L_0x0021:
        r0 = move-exception;
        goto L_0x00b1;
    L_0x0024:
        r0 = move-exception;
        goto L_0x00d2;
    L_0x0027:
        r0 = move-exception;
        goto L_0x00f3;
    L_0x002a:
        r1 = move-exception;
        r2 = new java.lang.StringBuilder;	 Catch:{ ClassNotFoundException -> 0x0100, NoSuchMethodException -> 0x0027, InstantiationException -> 0x0024, InvocationTargetException -> 0x0021, IllegalAccessException -> 0x001f, ClassCastException -> 0x001d }
        r2.<init>();	 Catch:{ ClassNotFoundException -> 0x0100, NoSuchMethodException -> 0x0027, InstantiationException -> 0x0024, InvocationTargetException -> 0x0021, IllegalAccessException -> 0x001f, ClassCastException -> 0x001d }
        r3 = com.mysql.jdbc.log.Log.class;
        r3 = com.mysql.jdbc.Util.getPackageName(r3);	 Catch:{ ClassNotFoundException -> 0x0100, NoSuchMethodException -> 0x0027, InstantiationException -> 0x0024, InvocationTargetException -> 0x0021, IllegalAccessException -> 0x001f, ClassCastException -> 0x001d }
        r2.append(r3);	 Catch:{ ClassNotFoundException -> 0x0100, NoSuchMethodException -> 0x0027, InstantiationException -> 0x0024, InvocationTargetException -> 0x0021, IllegalAccessException -> 0x001f, ClassCastException -> 0x001d }
        r3 = ".";
        r2.append(r3);	 Catch:{ ClassNotFoundException -> 0x0100, NoSuchMethodException -> 0x0027, InstantiationException -> 0x0024, InvocationTargetException -> 0x0021, IllegalAccessException -> 0x001f, ClassCastException -> 0x001d }
        r2.append(r5);	 Catch:{ ClassNotFoundException -> 0x0100, NoSuchMethodException -> 0x0027, InstantiationException -> 0x0024, InvocationTargetException -> 0x0021, IllegalAccessException -> 0x001f, ClassCastException -> 0x001d }
        r2 = r2.toString();	 Catch:{ ClassNotFoundException -> 0x0100, NoSuchMethodException -> 0x0027, InstantiationException -> 0x0024, InvocationTargetException -> 0x0021, IllegalAccessException -> 0x001f, ClassCastException -> 0x001d }
        r2 = java.lang.Class.forName(r2);	 Catch:{ ClassNotFoundException -> 0x0100, NoSuchMethodException -> 0x0027, InstantiationException -> 0x0024, InvocationTargetException -> 0x0021, IllegalAccessException -> 0x001f, ClassCastException -> 0x001d }
        r0 = r2;
    L_0x004a:
        r1 = 1;
        r2 = new java.lang.Class[r1];	 Catch:{ ClassNotFoundException -> 0x0100, NoSuchMethodException -> 0x0027, InstantiationException -> 0x0024, InvocationTargetException -> 0x0021, IllegalAccessException -> 0x001f, ClassCastException -> 0x001d }
        r3 = java.lang.String.class;
        r4 = 0;
        r2[r4] = r3;	 Catch:{ ClassNotFoundException -> 0x0100, NoSuchMethodException -> 0x0027, InstantiationException -> 0x0024, InvocationTargetException -> 0x0021, IllegalAccessException -> 0x001f, ClassCastException -> 0x001d }
        r2 = r0.getConstructor(r2);	 Catch:{ ClassNotFoundException -> 0x0100, NoSuchMethodException -> 0x0027, InstantiationException -> 0x0024, InvocationTargetException -> 0x0021, IllegalAccessException -> 0x001f, ClassCastException -> 0x001d }
        r1 = new java.lang.Object[r1];	 Catch:{ ClassNotFoundException -> 0x0100, NoSuchMethodException -> 0x0027, InstantiationException -> 0x0024, InvocationTargetException -> 0x0021, IllegalAccessException -> 0x001f, ClassCastException -> 0x001d }
        r1[r4] = r6;	 Catch:{ ClassNotFoundException -> 0x0100, NoSuchMethodException -> 0x0027, InstantiationException -> 0x0024, InvocationTargetException -> 0x0021, IllegalAccessException -> 0x001f, ClassCastException -> 0x001d }
        r1 = r2.newInstance(r1);	 Catch:{ ClassNotFoundException -> 0x0100, NoSuchMethodException -> 0x0027, InstantiationException -> 0x0024, InvocationTargetException -> 0x0021, IllegalAccessException -> 0x001f, ClassCastException -> 0x001d }
        r1 = (com.mysql.jdbc.log.Log) r1;	 Catch:{ ClassNotFoundException -> 0x0100, NoSuchMethodException -> 0x0027, InstantiationException -> 0x0024, InvocationTargetException -> 0x0021, IllegalAccessException -> 0x001f, ClassCastException -> 0x001d }
        return r1;
        r1 = new java.lang.StringBuilder;
        r1.<init>();
        r2 = "Logger class '";
        r1.append(r2);
        r1.append(r5);
        r2 = "' does not implement the '";
        r1.append(r2);
        r2 = com.mysql.jdbc.log.Log.class;
        r2 = r2.getName();
        r1.append(r2);
        r2 = "' interface";
        r1.append(r2);
        r1 = r1.toString();
        r2 = "S1009";
        r1 = com.mysql.jdbc.SQLError.createSQLException(r1, r2, r7);
        r1.initCause(r0);
        throw r1;
        r1 = new java.lang.StringBuilder;
        r1.<init>();
        r2 = "Unable to instantiate logger class '";
        r1.append(r2);
        r1.append(r5);
        r2 = "', constructor not public";
        r1.append(r2);
        r1 = r1.toString();
        r2 = "S1009";
        r1 = com.mysql.jdbc.SQLError.createSQLException(r1, r2, r7);
        r1.initCause(r0);
        throw r1;
        r1 = new java.lang.StringBuilder;
        r1.<init>();
        r2 = "Unable to instantiate logger class '";
        r1.append(r2);
        r1.append(r5);
        r2 = "', exception in constructor?";
        r1.append(r2);
        r1 = r1.toString();
        r2 = "S1009";
        r1 = com.mysql.jdbc.SQLError.createSQLException(r1, r2, r7);
        r1.initCause(r0);
        throw r1;
        r1 = new java.lang.StringBuilder;
        r1.<init>();
        r2 = "Unable to instantiate logger class '";
        r1.append(r2);
        r1.append(r5);
        r2 = "', exception in constructor?";
        r1.append(r2);
        r1 = r1.toString();
        r2 = "S1009";
        r1 = com.mysql.jdbc.SQLError.createSQLException(r1, r2, r7);
        r1.initCause(r0);
        throw r1;
        r1 = "Logger class does not have a single-arg constructor that takes an instance name";
        r2 = "S1009";
        r1 = com.mysql.jdbc.SQLError.createSQLException(r1, r2, r7);
        r1.initCause(r0);
        throw r1;
    L_0x0100:
        r0 = move-exception;
        r1 = new java.lang.StringBuilder;
        r1.<init>();
        r2 = "Unable to load class for logger '";
        r1.append(r2);
        r1.append(r5);
        r2 = "'";
        r1.append(r2);
        r1 = r1.toString();
        r2 = "S1009";
        r1 = com.mysql.jdbc.SQLError.createSQLException(r1, r2, r7);
        r1.initCause(r0);
        throw r1;
        */
        throw new UnsupportedOperationException("Method not decompiled: com.mysql.jdbc.log.LogFactory.getLogger(java.lang.String, java.lang.String, com.mysql.jdbc.ExceptionInterceptor):com.mysql.jdbc.log.Log");
    }
}
