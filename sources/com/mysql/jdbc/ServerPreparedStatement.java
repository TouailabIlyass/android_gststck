package com.mysql.jdbc;

import com.mysql.jdbc.PreparedStatement.ParseInfo;
import com.mysql.jdbc.log.LogUtils;
import com.mysql.jdbc.profiler.ProfilerEvent;
import com.mysql.jdbc.profiler.ProfilerEventHandler;
import java.io.IOException;
import java.io.InputStream;
import java.io.Reader;
import java.io.UnsupportedEncodingException;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.math.BigDecimal;
import java.net.URL;
import java.sql.Array;
import java.sql.Blob;
import java.sql.Clob;
import java.sql.ParameterMetaData;
import java.sql.Ref;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.sql.Time;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.TimeZone;

public class ServerPreparedStatement extends PreparedStatement {
    protected static final int BLOB_STREAM_READ_BUF_SIZE = 8192;
    private static final Constructor<?> JDBC_4_SPS_CTOR;
    private boolean canRewrite = false;
    private Calendar defaultTzCalendar;
    private boolean detectedLongParameterSwitch = false;
    private int fieldCount;
    private boolean hasCheckedRewrite = false;
    private boolean hasOnDuplicateKeyUpdate = false;
    private boolean invalid = false;
    private SQLException invalidationException;
    protected boolean isCached = false;
    private int locationOfOnDuplicateKeyUpdate = -2;
    private Buffer outByteBuffer;
    private BindValue[] parameterBindings;
    private Field[] parameterFields;
    private Field[] resultFields;
    private boolean sendTypesToServer = false;
    private boolean serverNeedsResetBeforeEachExecution;
    private long serverStatementId;
    private Calendar serverTzCalendar;
    private int stringTypeCode = 254;
    private boolean useAutoSlowLog;

    public static class BatchedBindValues {
        public BindValue[] batchedParameterValues;

        BatchedBindValues(BindValue[] paramVals) {
            int numParams = paramVals.length;
            this.batchedParameterValues = new BindValue[numParams];
            for (int i = 0; i < numParams; i++) {
                this.batchedParameterValues[i] = new BindValue(paramVals[i]);
            }
        }
    }

    public static class BindValue {
        public long bindLength;
        public long boundBeforeExecutionNum = 0;
        public int bufferType;
        public double doubleBinding;
        public float floatBinding;
        public boolean isLongData;
        public boolean isNull;
        public boolean isSet = false;
        public long longBinding;
        public Object value;

        BindValue() {
        }

        BindValue(BindValue copyMe) {
            this.value = copyMe.value;
            this.isSet = copyMe.isSet;
            this.isLongData = copyMe.isLongData;
            this.isNull = copyMe.isNull;
            this.bufferType = copyMe.bufferType;
            this.bindLength = copyMe.bindLength;
            this.longBinding = copyMe.longBinding;
            this.floatBinding = copyMe.floatBinding;
            this.doubleBinding = copyMe.doubleBinding;
        }

        void reset() {
            this.isNull = false;
            this.isSet = false;
            this.value = null;
            this.isLongData = false;
            this.longBinding = 0;
            this.floatBinding = 0.0f;
            this.doubleBinding = 0.0d;
        }

        public String toString() {
            return toString(false);
        }

        /* JADX WARNING: inconsistent code. */
        /* Code decompiled incorrectly, please refer to instructions dump. */
        public java.lang.String toString(boolean r3) {
            /*
            r2 = this;
            r0 = r2.isLongData;
            if (r0 == 0) goto L_0x0007;
        L_0x0004:
            r0 = "' STREAM DATA '";
            return r0;
        L_0x0007:
            r0 = r2.isNull;
            if (r0 == 0) goto L_0x000e;
        L_0x000b:
            r0 = "NULL";
            return r0;
        L_0x000e:
            r0 = r2.bufferType;
            r1 = 15;
            if (r0 == r1) goto L_0x0064;
        L_0x0014:
            switch(r0) {
                case 1: goto L_0x005d;
                case 2: goto L_0x005d;
                case 3: goto L_0x005d;
                case 4: goto L_0x0056;
                case 5: goto L_0x004f;
                default: goto L_0x0017;
            };
        L_0x0017:
            switch(r0) {
                case 7: goto L_0x0064;
                case 8: goto L_0x005d;
                default: goto L_0x001a;
            };
        L_0x001a:
            switch(r0) {
                case 10: goto L_0x0064;
                case 11: goto L_0x0064;
                case 12: goto L_0x0064;
                default: goto L_0x001d;
            };
        L_0x001d:
            switch(r0) {
                case 253: goto L_0x0064;
                case 254: goto L_0x0064;
                default: goto L_0x0020;
            };
        L_0x0020:
            r0 = r2.value;
            r0 = r0 instanceof byte[];
            if (r0 == 0) goto L_0x0029;
        L_0x0026:
            r0 = "byte data";
            return r0;
        L_0x0029:
            if (r3 == 0) goto L_0x0048;
        L_0x002b:
            r0 = new java.lang.StringBuilder;
            r0.<init>();
            r1 = "'";
            r0.append(r1);
            r1 = r2.value;
            r1 = java.lang.String.valueOf(r1);
            r0.append(r1);
            r1 = "'";
            r0.append(r1);
            r0 = r0.toString();
            return r0;
        L_0x0048:
            r0 = r2.value;
            r0 = java.lang.String.valueOf(r0);
            return r0;
        L_0x004f:
            r0 = r2.doubleBinding;
            r0 = java.lang.String.valueOf(r0);
            return r0;
        L_0x0056:
            r0 = r2.floatBinding;
            r0 = java.lang.String.valueOf(r0);
            return r0;
        L_0x005d:
            r0 = r2.longBinding;
            r0 = java.lang.String.valueOf(r0);
            return r0;
        L_0x0064:
            if (r3 == 0) goto L_0x0083;
        L_0x0066:
            r0 = new java.lang.StringBuilder;
            r0.<init>();
            r1 = "'";
            r0.append(r1);
            r1 = r2.value;
            r1 = java.lang.String.valueOf(r1);
            r0.append(r1);
            r1 = "'";
            r0.append(r1);
            r0 = r0.toString();
            return r0;
        L_0x0083:
            r0 = r2.value;
            r0 = java.lang.String.valueOf(r0);
            return r0;
            */
            throw new UnsupportedOperationException("Method not decompiled: com.mysql.jdbc.ServerPreparedStatement.BindValue.toString(boolean):java.lang.String");
        }

        /* JADX WARNING: inconsistent code. */
        /* Code decompiled incorrectly, please refer to instructions dump. */
        long getBoundLength() {
            /*
            r7 = this;
            r0 = r7.isNull;
            r1 = 0;
            if (r0 == 0) goto L_0x0007;
        L_0x0006:
            return r1;
        L_0x0007:
            r0 = r7.isLongData;
            if (r0 == 0) goto L_0x000e;
        L_0x000b:
            r0 = r7.bindLength;
            return r0;
        L_0x000e:
            r0 = r7.bufferType;
            r3 = 15;
            if (r0 == r3) goto L_0x003c;
        L_0x0014:
            r3 = 246; // 0xf6 float:3.45E-43 double:1.215E-321;
            if (r0 == r3) goto L_0x003c;
        L_0x0018:
            r3 = 4;
            r5 = 8;
            switch(r0) {
                case 0: goto L_0x003c;
                case 1: goto L_0x0039;
                case 2: goto L_0x0036;
                case 3: goto L_0x0035;
                case 4: goto L_0x0034;
                case 5: goto L_0x0033;
                default: goto L_0x001f;
            };
        L_0x001f:
            switch(r0) {
                case 7: goto L_0x0030;
                case 8: goto L_0x002f;
                default: goto L_0x0022;
            };
        L_0x0022:
            switch(r0) {
                case 10: goto L_0x002c;
                case 11: goto L_0x0029;
                case 12: goto L_0x0030;
                default: goto L_0x0025;
            };
        L_0x0025:
            switch(r0) {
                case 253: goto L_0x003c;
                case 254: goto L_0x003c;
                default: goto L_0x0028;
            };
        L_0x0028:
            return r1;
        L_0x0029:
            r0 = 9;
            return r0;
        L_0x002c:
            r0 = 7;
            return r0;
        L_0x002f:
            return r5;
        L_0x0030:
            r0 = 11;
            return r0;
        L_0x0033:
            return r5;
        L_0x0034:
            return r3;
        L_0x0035:
            return r3;
        L_0x0036:
            r0 = 2;
            return r0;
        L_0x0039:
            r0 = 1;
            return r0;
        L_0x003c:
            r0 = r7.value;
            r0 = r0 instanceof byte[];
            if (r0 == 0) goto L_0x0049;
        L_0x0042:
            r0 = r7.value;
            r0 = (byte[]) r0;
            r0 = r0.length;
            r0 = (long) r0;
            return r0;
        L_0x0049:
            r0 = r7.value;
            r0 = (java.lang.String) r0;
            r0 = r0.length();
            r0 = (long) r0;
            return r0;
            */
            throw new UnsupportedOperationException("Method not decompiled: com.mysql.jdbc.ServerPreparedStatement.BindValue.getBoundLength():long");
        }
    }

    static {
        if (Util.isJdbc4()) {
            try {
                JDBC_4_SPS_CTOR = Class.forName(Util.isJdbc42() ? "com.mysql.jdbc.JDBC42ServerPreparedStatement" : "com.mysql.jdbc.JDBC4ServerPreparedStatement").getConstructor(new Class[]{MySQLConnection.class, String.class, String.class, Integer.TYPE, Integer.TYPE});
                return;
            } catch (SecurityException e) {
                throw new RuntimeException(e);
            } catch (NoSuchMethodException e2) {
                throw new RuntimeException(e2);
            } catch (ClassNotFoundException e3) {
                throw new RuntimeException(e3);
            }
        }
        JDBC_4_SPS_CTOR = null;
    }

    private void storeTime(Buffer intoBuf, Time tm) throws SQLException {
        Throwable th;
        intoBuf.ensureCapacity(9);
        intoBuf.writeByte((byte) 8);
        intoBuf.writeByte((byte) 0);
        intoBuf.writeLong(0);
        Calendar sessionCalendar = getCalendarInstanceForSessionOrNew();
        synchronized (sessionCalendar) {
            try {
                Date oldTime = sessionCalendar.getTime();
                try {
                    sessionCalendar.setTime(tm);
                    intoBuf.writeByte((byte) sessionCalendar.get(11));
                    intoBuf.writeByte((byte) sessionCalendar.get(12));
                    intoBuf.writeByte((byte) sessionCalendar.get(13));
                    sessionCalendar.setTime(oldTime);
                } catch (Throwable th2) {
                    th = th2;
                    throw th;
                }
            } catch (Throwable th3) {
                th = th3;
                ServerPreparedStatement serverPreparedStatement = this;
                Calendar calendar = sessionCalendar;
                throw th;
            }
        }
    }

    protected static ServerPreparedStatement getInstance(MySQLConnection conn, String sql, String catalog, int resultSetType, int resultSetConcurrency) throws SQLException {
        if (!Util.isJdbc4()) {
            return new ServerPreparedStatement(conn, sql, catalog, resultSetType, resultSetConcurrency);
        }
        try {
            return (ServerPreparedStatement) JDBC_4_SPS_CTOR.newInstance(new Object[]{conn, sql, catalog, Integer.valueOf(resultSetType), Integer.valueOf(resultSetConcurrency)});
        } catch (IllegalArgumentException e) {
            throw new SQLException(e.toString(), SQLError.SQL_STATE_GENERAL_ERROR);
        } catch (InstantiationException e2) {
            throw new SQLException(e2.toString(), SQLError.SQL_STATE_GENERAL_ERROR);
        } catch (IllegalAccessException e3) {
            throw new SQLException(e3.toString(), SQLError.SQL_STATE_GENERAL_ERROR);
        } catch (InvocationTargetException e4) {
            Throwable target = e4.getTargetException();
            if (target instanceof SQLException) {
                throw ((SQLException) target);
            }
            throw new SQLException(target.toString(), SQLError.SQL_STATE_GENERAL_ERROR);
        }
    }

    protected ServerPreparedStatement(MySQLConnection conn, String sql, String catalog, int resultSetType, int resultSetConcurrency) throws SQLException {
        String str;
        super(conn, catalog);
        checkNullOrEmptyQuery(sql);
        this.firstCharOfStmt = StringUtils.firstAlphaCharUc(sql, StatementImpl.findStartOfStatement(sql));
        boolean z = this.firstCharOfStmt == 'I' && containsOnDuplicateKeyInString(sql);
        this.hasOnDuplicateKeyUpdate = z;
        if (this.connection.versionMeetsMinimum(5, 0, 0)) {
            this.serverNeedsResetBeforeEachExecution = this.connection.versionMeetsMinimum(5, 0, 3) ^ true;
        } else {
            this.serverNeedsResetBeforeEachExecution = this.connection.versionMeetsMinimum(4, 1, 10) ^ true;
        }
        this.useAutoSlowLog = this.connection.getAutoSlowLog();
        this.useTrueBoolean = this.connection.versionMeetsMinimum(3, 21, 23);
        String statementComment = this.connection.getStatementComment();
        if (statementComment == null) {
            str = sql;
        } else {
            StringBuilder stringBuilder = new StringBuilder();
            stringBuilder.append("/* ");
            stringBuilder.append(statementComment);
            stringBuilder.append(" */ ");
            stringBuilder.append(sql);
            str = stringBuilder.toString();
        }
        this.originalSql = str;
        if (this.connection.versionMeetsMinimum(4, 1, 2)) {
            this.stringTypeCode = 253;
        } else {
            this.stringTypeCode = 254;
        }
        try {
            serverPrepare(sql);
            setResultSetType(resultSetType);
            setResultSetConcurrency(resultSetConcurrency);
            this.parameterTypes = new int[this.parameterCount];
        } catch (SQLException sqlEx) {
            realClose(false, true);
            throw sqlEx;
        } catch (Exception ex) {
            realClose(false, true);
            SQLException sqlEx2 = SQLError.createSQLException(ex.toString(), SQLError.SQL_STATE_GENERAL_ERROR, getExceptionInterceptor());
            sqlEx2.initCause(ex);
            throw sqlEx2;
        }
    }

    public void addBatch() throws SQLException {
        synchronized (checkClosed().getConnectionMutex()) {
            if (this.batchedArgs == null) {
                this.batchedArgs = new ArrayList();
            }
            this.batchedArgs.add(new BatchedBindValues(this.parameterBindings));
        }
    }

    public String asSql(boolean quoteStreamsAndUnknowns) throws SQLException {
        String asSql;
        synchronized (checkClosed().getConnectionMutex()) {
            PreparedStatement pStmtForSub = null;
            try {
                pStmtForSub = PreparedStatement.getInstance(this.connection, this.originalSql, this.currentCatalog);
                int numParameters = pStmtForSub.parameterCount;
                int ourNumParameters = this.parameterCount;
                int i = 0;
                while (i < numParameters && i < ourNumParameters) {
                    if (this.parameterBindings[i] != null) {
                        if (this.parameterBindings[i].isNull) {
                            pStmtForSub.setNull(i + 1, 0);
                        } else {
                            BindValue bindValue = this.parameterBindings[i];
                            int i2 = bindValue.bufferType;
                            if (i2 != 8) {
                                switch (i2) {
                                    case 1:
                                        pStmtForSub.setByte(i + 1, (byte) ((int) bindValue.longBinding));
                                        break;
                                    case 2:
                                        pStmtForSub.setShort(i + 1, (short) ((int) bindValue.longBinding));
                                        break;
                                    case 3:
                                        pStmtForSub.setInt(i + 1, (int) bindValue.longBinding);
                                        break;
                                    case 4:
                                        pStmtForSub.setFloat(i + 1, bindValue.floatBinding);
                                        break;
                                    case 5:
                                        pStmtForSub.setDouble(i + 1, bindValue.doubleBinding);
                                        break;
                                    default:
                                        pStmtForSub.setObject(i + 1, this.parameterBindings[i].value);
                                        break;
                                }
                            }
                            pStmtForSub.setLong(i + 1, bindValue.longBinding);
                        }
                    }
                    i++;
                }
                asSql = pStmtForSub.asSql(quoteStreamsAndUnknowns);
                if (pStmtForSub != null) {
                    try {
                        pStmtForSub.close();
                    } catch (SQLException e) {
                    }
                }
            } catch (Throwable th) {
                ServerPreparedStatement serverPreparedStatement = this;
                if (pStmtForSub != null) {
                    try {
                        pStmtForSub.close();
                    } catch (SQLException e2) {
                    }
                }
            }
        }
        return asSql;
    }

    protected MySQLConnection checkClosed() throws SQLException {
        if (!this.invalid) {
            return super.checkClosed();
        }
        throw this.invalidationException;
    }

    public void clearParameters() throws SQLException {
        synchronized (checkClosed().getConnectionMutex()) {
            clearParametersInternal(true);
        }
    }

    private void clearParametersInternal(boolean clearServerParameters) throws SQLException {
        boolean hadLongData = false;
        if (this.parameterBindings != null) {
            boolean hadLongData2 = false;
            int i = 0;
            while (i < this.parameterCount) {
                if (this.parameterBindings[i] != null && this.parameterBindings[i].isLongData) {
                    hadLongData2 = true;
                }
                this.parameterBindings[i].reset();
                i++;
            }
            hadLongData = hadLongData2;
        }
        if (clearServerParameters && hadLongData) {
            serverResetStatement();
            this.detectedLongParameterSwitch = false;
        }
    }

    protected void setClosed(boolean flag) {
        this.isClosed = flag;
    }

    public void close() throws SQLException {
        MySQLConnection locallyScopedConn = this.connection;
        if (locallyScopedConn != null) {
            synchronized (locallyScopedConn.getConnectionMutex()) {
                if (this.isCached && isPoolable() && !this.isClosed) {
                    clearParameters();
                    this.isClosed = true;
                    this.connection.recachePreparedStatement(this);
                    return;
                }
                this.isClosed = false;
                realClose(true, true);
            }
        }
    }

    private void dumpCloseForTestcase() throws SQLException {
        synchronized (checkClosed().getConnectionMutex()) {
            StringBuilder buf = new StringBuilder();
            this.connection.generateConnectionCommentBlock(buf);
            buf.append("DEALLOCATE PREPARE debug_stmt_");
            buf.append(this.statementId);
            buf.append(";\n");
            this.connection.dumpTestcaseQuery(buf.toString());
        }
    }

    private void dumpExecuteForTestcase() throws SQLException {
        synchronized (checkClosed().getConnectionMutex()) {
            StringBuilder buf = new StringBuilder();
            int i = 0;
            for (int i2 = 0; i2 < this.parameterCount; i2++) {
                this.connection.generateConnectionCommentBlock(buf);
                buf.append("SET @debug_stmt_param");
                buf.append(this.statementId);
                buf.append("_");
                buf.append(i2);
                buf.append("=");
                if (this.parameterBindings[i2].isNull) {
                    buf.append("NULL");
                } else {
                    buf.append(this.parameterBindings[i2].toString(true));
                }
                buf.append(";\n");
            }
            this.connection.generateConnectionCommentBlock(buf);
            buf.append("EXECUTE debug_stmt_");
            buf.append(this.statementId);
            if (this.parameterCount > 0) {
                buf.append(" USING ");
                while (i < this.parameterCount) {
                    if (i > 0) {
                        buf.append(", ");
                    }
                    buf.append("@debug_stmt_param");
                    buf.append(this.statementId);
                    buf.append("_");
                    buf.append(i);
                    i++;
                }
            }
            buf.append(";\n");
            this.connection.dumpTestcaseQuery(buf.toString());
        }
    }

    private void dumpPrepareForTestcase() throws SQLException {
        synchronized (checkClosed().getConnectionMutex()) {
            StringBuilder buf = new StringBuilder(this.originalSql.length() + 64);
            this.connection.generateConnectionCommentBlock(buf);
            buf.append("PREPARE debug_stmt_");
            buf.append(this.statementId);
            buf.append(" FROM \"");
            buf.append(this.originalSql);
            buf.append("\";\n");
            this.connection.dumpTestcaseQuery(buf.toString());
        }
    }

    protected long[] executeBatchSerially(int r22) throws java.sql.SQLException {
        /* JADX: method processing error */
/*
Error: jadx.core.utils.exceptions.JadxRuntimeException: Unexpected register number in merge insn: ?: MERGE  (r9_11 boolean) = (r9_10 boolean), (r9_25 boolean)
	at jadx.core.dex.visitors.ssa.EliminatePhiNodes.replaceMerge(EliminatePhiNodes.java:84)
	at jadx.core.dex.visitors.ssa.EliminatePhiNodes.replaceMergeInstructions(EliminatePhiNodes.java:68)
	at jadx.core.dex.visitors.ssa.EliminatePhiNodes.visit(EliminatePhiNodes.java:31)
	at jadx.core.dex.visitors.DepthTraversal.visit(DepthTraversal.java:31)
	at jadx.core.dex.visitors.DepthTraversal.visit(DepthTraversal.java:17)
	at jadx.core.ProcessClass.process(ProcessClass.java:34)
	at jadx.core.ProcessClass.processDependencies(ProcessClass.java:56)
	at jadx.core.ProcessClass.process(ProcessClass.java:39)
	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:282)
	at jadx.api.JavaClass.decompile(JavaClass.java:62)
	at jadx.api.JadxDecompiler.lambda$appendSourcesSave$0(JadxDecompiler.java:200)
	at jadx.api.JadxDecompiler$$Lambda$8/1205406622.run(Unknown Source)
*/
        /*
        r21 = this;
        r1 = r21;
        r2 = r22;
        r3 = r21.checkClosed();
        r3 = r3.getConnectionMutex();
        monitor-enter(r3);
        r4 = r1.connection;	 Catch:{ all -> 0x01ce }
        r5 = r4.isReadOnly();	 Catch:{ all -> 0x01ce }
        if (r5 == 0) goto L_0x0040;
    L_0x0015:
        r5 = new java.lang.StringBuilder;	 Catch:{ all -> 0x003b }
        r5.<init>();	 Catch:{ all -> 0x003b }
        r6 = "ServerPreparedStatement.2";	 Catch:{ all -> 0x003b }
        r6 = com.mysql.jdbc.Messages.getString(r6);	 Catch:{ all -> 0x003b }
        r5.append(r6);	 Catch:{ all -> 0x003b }
        r6 = "ServerPreparedStatement.3";	 Catch:{ all -> 0x003b }
        r6 = com.mysql.jdbc.Messages.getString(r6);	 Catch:{ all -> 0x003b }
        r5.append(r6);	 Catch:{ all -> 0x003b }
        r5 = r5.toString();	 Catch:{ all -> 0x003b }
        r6 = "S1009";	 Catch:{ all -> 0x003b }
        r7 = r21.getExceptionInterceptor();	 Catch:{ all -> 0x003b }
        r5 = com.mysql.jdbc.SQLError.createSQLException(r5, r6, r7);	 Catch:{ all -> 0x003b }
        throw r5;	 Catch:{ all -> 0x003b }
    L_0x003b:
        r0 = move-exception;
        r6 = r2;
        r2 = r1;
        goto L_0x01d6;
    L_0x0040:
        r21.clearWarnings();	 Catch:{ all -> 0x01ce }
        r5 = r1.parameterBindings;	 Catch:{ all -> 0x01ce }
        r6 = 0;
        r8 = r1.batchedArgs;	 Catch:{ all -> 0x01bf }
        r9 = 0;	 Catch:{ all -> 0x01bf }
        if (r8 == 0) goto L_0x01a3;	 Catch:{ all -> 0x01bf }
    L_0x004b:
        r8 = r1.batchedArgs;	 Catch:{ all -> 0x01bf }
        r8 = r8.size();	 Catch:{ all -> 0x01bf }
        r10 = new long[r8];	 Catch:{ all -> 0x01bf }
        r6 = r10;	 Catch:{ all -> 0x01bf }
        r10 = r1.retrieveGeneratedKeys;	 Catch:{ all -> 0x01bf }
        if (r10 == 0) goto L_0x0065;
    L_0x0058:
        r10 = new java.util.ArrayList;	 Catch:{ all -> 0x0060 }
        r10.<init>(r8);	 Catch:{ all -> 0x0060 }
        r1.batchedGeneratedKeys = r10;	 Catch:{ all -> 0x0060 }
        goto L_0x0065;	 Catch:{ all -> 0x0060 }
    L_0x0060:
        r0 = move-exception;	 Catch:{ all -> 0x0060 }
        r9 = r2;	 Catch:{ all -> 0x0060 }
        r2 = r1;	 Catch:{ all -> 0x0060 }
        goto L_0x01bd;	 Catch:{ all -> 0x0060 }
    L_0x0065:
        r10 = r9;	 Catch:{ all -> 0x0060 }
    L_0x0066:
        r11 = -3;	 Catch:{ all -> 0x0060 }
        if (r10 >= r8) goto L_0x006f;	 Catch:{ all -> 0x0060 }
    L_0x006a:
        r6[r10] = r11;	 Catch:{ all -> 0x0060 }
        r10 = r10 + 1;
        goto L_0x0066;
    L_0x006f:
        r10 = 0;
        r13 = 0;
        r14 = 0;
        r15 = 0;
        r16 = r4.getEnableQueryTimeouts();	 Catch:{ all -> 0x0187 }
        if (r16 == 0) goto L_0x00a2;
    L_0x0079:
        if (r2 == 0) goto L_0x00a2;
    L_0x007b:
        r11 = 5;
        r11 = r4.versionMeetsMinimum(r11, r9, r9);	 Catch:{ all -> 0x009a }
        if (r11 == 0) goto L_0x00a2;	 Catch:{ all -> 0x009a }
    L_0x0082:
        r11 = new com.mysql.jdbc.StatementImpl$CancelTask;	 Catch:{ all -> 0x009a }
        r11.<init>(r1);	 Catch:{ all -> 0x009a }
        r15 = r11;	 Catch:{ all -> 0x009a }
        r11 = r4.getCancelTimer();	 Catch:{ all -> 0x009a }
        r18 = r10;
        r9 = (long) r2;
        r11.schedule(r15, r9);	 Catch:{ all -> 0x0093 }
        goto L_0x00a4;
    L_0x0093:
        r0 = move-exception;
        r9 = r2;
        r10 = r18;
    L_0x0097:
        r2 = r1;
        goto L_0x0164;
    L_0x009a:
        r0 = move-exception;
        r18 = r10;
        r9 = r2;
        r2 = r1;
        r1 = r0;
        goto L_0x018e;
    L_0x00a2:
        r18 = r10;
    L_0x00a4:
        r9 = 0;
        r13 = r9;
        r10 = r18;
        r9 = r2;
        r2 = r1;
    L_0x00aa:
        if (r13 >= r8) goto L_0x0166;
    L_0x00ac:
        r11 = r2.batchedArgs;	 Catch:{ all -> 0x0163 }
        r11 = r11.get(r13);	 Catch:{ all -> 0x0163 }
        r12 = r11 instanceof java.lang.String;	 Catch:{ SQLException -> 0x0134 }
        if (r12 == 0) goto L_0x00de;	 Catch:{ SQLException -> 0x0134 }
    L_0x00b6:
        r12 = r11;	 Catch:{ SQLException -> 0x0134 }
        r12 = (java.lang.String) r12;	 Catch:{ SQLException -> 0x0134 }
        r7 = r2.retrieveGeneratedKeys;	 Catch:{ SQLException -> 0x0134 }
        r1 = 1;	 Catch:{ SQLException -> 0x0134 }
        r19 = r2.executeUpdateInternal(r12, r1, r7);	 Catch:{ SQLException -> 0x0134 }
        r6[r13] = r19;	 Catch:{ SQLException -> 0x0134 }
        r1 = r2.results;	 Catch:{ SQLException -> 0x0134 }
        r1 = r1.getFirstCharOfQuery();	 Catch:{ SQLException -> 0x0134 }
        r7 = 73;	 Catch:{ SQLException -> 0x0134 }
        if (r1 != r7) goto L_0x00d7;	 Catch:{ SQLException -> 0x0134 }
    L_0x00cc:
        r1 = r11;	 Catch:{ SQLException -> 0x0134 }
        r1 = (java.lang.String) r1;	 Catch:{ SQLException -> 0x0134 }
        r1 = r2.containsOnDuplicateKeyInString(r1);	 Catch:{ SQLException -> 0x0134 }
        if (r1 == 0) goto L_0x00d7;	 Catch:{ SQLException -> 0x0134 }
    L_0x00d5:
        r1 = 1;	 Catch:{ SQLException -> 0x0134 }
        goto L_0x00d8;	 Catch:{ SQLException -> 0x0134 }
    L_0x00d7:
        r1 = 0;	 Catch:{ SQLException -> 0x0134 }
    L_0x00d8:
        r2.getBatchedGeneratedKeys(r1);	 Catch:{ SQLException -> 0x0134 }
        r1 = r2;	 Catch:{ SQLException -> 0x0134 }
        r2 = r9;	 Catch:{ SQLException -> 0x0134 }
        goto L_0x0116;	 Catch:{ SQLException -> 0x0134 }
    L_0x00de:
        r1 = r11;	 Catch:{ SQLException -> 0x0134 }
        r1 = (com.mysql.jdbc.ServerPreparedStatement.BatchedBindValues) r1;	 Catch:{ SQLException -> 0x0134 }
        r1 = r1.batchedParameterValues;	 Catch:{ SQLException -> 0x0134 }
        r2.parameterBindings = r1;	 Catch:{ SQLException -> 0x0134 }
        if (r14 == 0) goto L_0x0100;	 Catch:{ SQLException -> 0x0134 }
    L_0x00e7:
        r1 = 0;	 Catch:{ SQLException -> 0x0134 }
    L_0x00e8:
        r7 = r2.parameterBindings;	 Catch:{ SQLException -> 0x0134 }
        r7 = r7.length;	 Catch:{ SQLException -> 0x0134 }
        if (r1 >= r7) goto L_0x0100;	 Catch:{ SQLException -> 0x0134 }
    L_0x00ed:
        r7 = r2.parameterBindings;	 Catch:{ SQLException -> 0x0134 }
        r7 = r7[r1];	 Catch:{ SQLException -> 0x0134 }
        r7 = r7.bufferType;	 Catch:{ SQLException -> 0x0134 }
        r12 = r14[r1];	 Catch:{ SQLException -> 0x0134 }
        r12 = r12.bufferType;	 Catch:{ SQLException -> 0x0134 }
        if (r7 == r12) goto L_0x00fd;	 Catch:{ SQLException -> 0x0134 }
    L_0x00f9:
        r7 = 1;	 Catch:{ SQLException -> 0x0134 }
        r2.sendTypesToServer = r7;	 Catch:{ SQLException -> 0x0134 }
        goto L_0x0100;
    L_0x00fd:
        r1 = r1 + 1;
        goto L_0x00e8;
    L_0x0100:
        r1 = 0;
        r7 = 1;
        r19 = r2.executeUpdateInternal(r1, r7);	 Catch:{ all -> 0x0124 }
        r6[r13] = r19;	 Catch:{ all -> 0x0124 }
        r1 = r2;
        r2 = r9;
        r7 = r1.parameterBindings;	 Catch:{ SQLException -> 0x0120, all -> 0x011c }
        r14 = r7;	 Catch:{ SQLException -> 0x0120, all -> 0x011c }
        r7 = r1.containsOnDuplicateKeyUpdateInSQL();	 Catch:{ SQLException -> 0x0120, all -> 0x011c }
        r1.getBatchedGeneratedKeys(r7);	 Catch:{ SQLException -> 0x0120, all -> 0x011c }
        r9 = r2;
        r16 = -3;
        r2 = r1;
        goto L_0x014e;
    L_0x011c:
        r0 = move-exception;
        r9 = r2;
        goto L_0x0097;
    L_0x0120:
        r0 = move-exception;
        r9 = r2;
        r2 = r1;
        goto L_0x0135;
    L_0x0124:
        r0 = move-exception;
        r1 = r0;
        r7 = r9;
        r9 = r2.parameterBindings;	 Catch:{ SQLException -> 0x0130, all -> 0x012b }
        r14 = r9;	 Catch:{ SQLException -> 0x0130, all -> 0x012b }
        throw r1;	 Catch:{ SQLException -> 0x0130, all -> 0x012b }
    L_0x012b:
        r0 = move-exception;
        r1 = r0;
        r9 = r7;
        goto L_0x018e;
    L_0x0130:
        r0 = move-exception;
        r1 = r0;
        r9 = r7;
        goto L_0x0136;
    L_0x0134:
        r0 = move-exception;
    L_0x0135:
        r1 = r0;
    L_0x0136:
        r16 = -3;
        r6[r13] = r16;	 Catch:{ all -> 0x0163 }
        r7 = r2.continueBatchOnError;	 Catch:{ all -> 0x0163 }
        if (r7 == 0) goto L_0x0154;	 Catch:{ all -> 0x0163 }
    L_0x013e:
        r7 = r1 instanceof com.mysql.jdbc.exceptions.MySQLTimeoutException;	 Catch:{ all -> 0x0163 }
        if (r7 != 0) goto L_0x0154;	 Catch:{ all -> 0x0163 }
    L_0x0142:
        r7 = r1 instanceof com.mysql.jdbc.exceptions.MySQLStatementCancelledException;	 Catch:{ all -> 0x0163 }
        if (r7 != 0) goto L_0x0154;	 Catch:{ all -> 0x0163 }
    L_0x0146:
        r7 = r2.hasDeadlockOrTimeoutRolledBackTx(r1);	 Catch:{ all -> 0x0163 }
        if (r7 != 0) goto L_0x0154;	 Catch:{ all -> 0x0163 }
    L_0x014c:
        r7 = r1;	 Catch:{ all -> 0x0163 }
        r10 = r7;	 Catch:{ all -> 0x0163 }
    L_0x014e:
        r1 = 1;	 Catch:{ all -> 0x0163 }
        r13 = r13 + r1;	 Catch:{ all -> 0x0163 }
        r1 = r21;	 Catch:{ all -> 0x0163 }
        goto L_0x00aa;	 Catch:{ all -> 0x0163 }
    L_0x0154:
        r7 = new long[r13];	 Catch:{ all -> 0x0163 }
        r12 = 0;	 Catch:{ all -> 0x0163 }
        java.lang.System.arraycopy(r6, r12, r7, r12, r13);	 Catch:{ all -> 0x0163 }
        r12 = r2.getExceptionInterceptor();	 Catch:{ all -> 0x0163 }
        r12 = com.mysql.jdbc.SQLError.createBatchUpdateException(r1, r7, r12);	 Catch:{ all -> 0x0163 }
        throw r12;	 Catch:{ all -> 0x0163 }
    L_0x0163:
        r0 = move-exception;
    L_0x0164:
        r1 = r0;
        goto L_0x018e;
        r1 = r8;
        r7 = r10;
        r8 = r13;
        r10 = r14;
        r11 = r15;
        if (r11 == 0) goto L_0x0178;
    L_0x016e:
        r11.cancel();	 Catch:{ all -> 0x01bc }
        r12 = r4.getCancelTimer();	 Catch:{ all -> 0x01bc }
        r12.purge();	 Catch:{ all -> 0x01bc }
    L_0x0178:
        r2.resetCancelledState();	 Catch:{ all -> 0x01bc }
        if (r7 == 0) goto L_0x01a6;	 Catch:{ all -> 0x01bc }
    L_0x017e:
        r12 = r2.getExceptionInterceptor();	 Catch:{ all -> 0x01bc }
        r12 = com.mysql.jdbc.SQLError.createBatchUpdateException(r7, r6, r12);	 Catch:{ all -> 0x01bc }
        throw r12;	 Catch:{ all -> 0x01bc }
    L_0x0187:
        r0 = move-exception;	 Catch:{ all -> 0x01bc }
        r18 = r10;	 Catch:{ all -> 0x01bc }
        r1 = r0;	 Catch:{ all -> 0x01bc }
        r9 = r2;	 Catch:{ all -> 0x01bc }
        r2 = r21;	 Catch:{ all -> 0x01bc }
    L_0x018e:
        r7 = r8;	 Catch:{ all -> 0x01bc }
        r8 = r10;	 Catch:{ all -> 0x01bc }
        r10 = r13;	 Catch:{ all -> 0x01bc }
        r11 = r14;	 Catch:{ all -> 0x01bc }
        r12 = r15;	 Catch:{ all -> 0x01bc }
        if (r12 == 0) goto L_0x019f;	 Catch:{ all -> 0x01bc }
    L_0x0195:
        r12.cancel();	 Catch:{ all -> 0x01bc }
        r13 = r4.getCancelTimer();	 Catch:{ all -> 0x01bc }
        r13.purge();	 Catch:{ all -> 0x01bc }
    L_0x019f:
        r2.resetCancelledState();	 Catch:{ all -> 0x01bc }
        throw r1;	 Catch:{ all -> 0x01bc }
    L_0x01a3:
        r9 = r2;	 Catch:{ all -> 0x01bc }
        r2 = r21;	 Catch:{ all -> 0x01bc }
    L_0x01a6:
        if (r6 == 0) goto L_0x01aa;	 Catch:{ all -> 0x01bc }
    L_0x01a8:
        r1 = r6;	 Catch:{ all -> 0x01bc }
        goto L_0x01ad;	 Catch:{ all -> 0x01bc }
    L_0x01aa:
        r1 = 0;	 Catch:{ all -> 0x01bc }
        r1 = new long[r1];	 Catch:{ all -> 0x01bc }
    L_0x01ad:
        r7 = r9;
        r2.parameterBindings = r5;	 Catch:{ all -> 0x01b8 }
        r8 = 1;	 Catch:{ all -> 0x01b8 }
        r2.sendTypesToServer = r8;	 Catch:{ all -> 0x01b8 }
        r2.clearBatch();	 Catch:{ all -> 0x01b8 }
        monitor-exit(r3);	 Catch:{ all -> 0x01b8 }
        return r1;
    L_0x01b8:
        r0 = move-exception;
        r1 = r0;
        r6 = r7;
        goto L_0x01d3;
    L_0x01bc:
        r0 = move-exception;
    L_0x01bd:
        r1 = r0;
        goto L_0x01c4;
    L_0x01bf:
        r0 = move-exception;
        r1 = r0;
        r9 = r2;
        r2 = r21;
    L_0x01c4:
        r6 = r9;
        r2.parameterBindings = r5;	 Catch:{ all -> 0x01d5 }
        r7 = 1;	 Catch:{ all -> 0x01d5 }
        r2.sendTypesToServer = r7;	 Catch:{ all -> 0x01d5 }
        r2.clearBatch();	 Catch:{ all -> 0x01d5 }
        throw r1;	 Catch:{ all -> 0x01d5 }
    L_0x01ce:
        r0 = move-exception;	 Catch:{ all -> 0x01d5 }
        r1 = r0;	 Catch:{ all -> 0x01d5 }
        r6 = r2;	 Catch:{ all -> 0x01d5 }
        r2 = r21;	 Catch:{ all -> 0x01d5 }
    L_0x01d3:
        monitor-exit(r3);	 Catch:{ all -> 0x01d5 }
        throw r1;
    L_0x01d5:
        r0 = move-exception;
    L_0x01d6:
        r1 = r0;
        goto L_0x01d3;
        */
        throw new UnsupportedOperationException("Method not decompiled: com.mysql.jdbc.ServerPreparedStatement.executeBatchSerially(int):long[]");
    }

    protected ResultSetInternalMethods executeInternal(int maxRowsToRetrieve, Buffer sendPacket, boolean createStreamingResultSet, boolean queryIsSelectOnly, Field[] metadataFromCache, boolean isBatch) throws SQLException {
        ResultSetInternalMethods serverExecute;
        SQLException sqlEx;
        synchronized (checkClosed().getConnectionMutex()) {
            this.numberOfExecutions++;
            try {
                serverExecute = serverExecute(maxRowsToRetrieve, createStreamingResultSet, metadataFromCache);
            } catch (SQLException e) {
                sqlEx = e;
                if (this.connection.getEnablePacketDebug()) {
                    this.connection.getIO().dumpPacketRingBuffer();
                }
                if (this.connection.getDumpQueriesOnException()) {
                    String extractedSql = toString();
                    StringBuilder messageBuf = new StringBuilder(extractedSql.length() + 32);
                    messageBuf.append("\n\nQuery being executed when exception was thrown:\n");
                    messageBuf.append(extractedSql);
                    messageBuf.append("\n\n");
                    sqlEx = ConnectionImpl.appendMessageToException(sqlEx, messageBuf.toString(), getExceptionInterceptor());
                }
                throw sqlEx;
            } catch (Exception ex) {
                if (this.connection.getEnablePacketDebug()) {
                    this.connection.getIO().dumpPacketRingBuffer();
                }
                sqlEx = SQLError.createSQLException(ex.toString(), SQLError.SQL_STATE_GENERAL_ERROR, getExceptionInterceptor());
                if (this.connection.getDumpQueriesOnException()) {
                    SQLException sqlEx2;
                    String extractedSql2 = toString();
                    StringBuilder messageBuf2 = new StringBuilder(extractedSql2.length() + 32);
                    messageBuf2.append("\n\nQuery being executed when exception was thrown:\n");
                    messageBuf2.append(extractedSql2);
                    messageBuf2.append("\n\n");
                    sqlEx2 = ConnectionImpl.appendMessageToException(sqlEx2, messageBuf2.toString(), getExceptionInterceptor());
                }
                sqlEx2.initCause(ex);
                throw sqlEx2;
            }
        }
        return serverExecute;
    }

    protected Buffer fillSendPacket() throws SQLException {
        return null;
    }

    protected Buffer fillSendPacket(byte[][] batchedParameterStrings, InputStream[] batchedParameterStreams, boolean[] batchedIsStream, int[] batchedStreamLengths) throws SQLException {
        return null;
    }

    protected BindValue getBinding(int parameterIndex, boolean forLongData) throws SQLException {
        BindValue bindValue;
        synchronized (checkClosed().getConnectionMutex()) {
            if (this.parameterBindings.length == 0) {
                throw SQLError.createSQLException(Messages.getString("ServerPreparedStatement.8"), SQLError.SQL_STATE_ILLEGAL_ARGUMENT, getExceptionInterceptor());
            }
            parameterIndex--;
            if (parameterIndex >= 0) {
                if (parameterIndex < this.parameterBindings.length) {
                    if (this.parameterBindings[parameterIndex] == null) {
                        this.parameterBindings[parameterIndex] = new BindValue();
                    } else if (this.parameterBindings[parameterIndex].isLongData && !forLongData) {
                        this.detectedLongParameterSwitch = true;
                    }
                    bindValue = this.parameterBindings[parameterIndex];
                }
            }
            StringBuilder stringBuilder = new StringBuilder();
            stringBuilder.append(Messages.getString("ServerPreparedStatement.9"));
            stringBuilder.append(parameterIndex + 1);
            stringBuilder.append(Messages.getString("ServerPreparedStatement.10"));
            stringBuilder.append(this.parameterBindings.length);
            throw SQLError.createSQLException(stringBuilder.toString(), SQLError.SQL_STATE_ILLEGAL_ARGUMENT, getExceptionInterceptor());
        }
        return bindValue;
    }

    public BindValue[] getParameterBindValues() {
        return this.parameterBindings;
    }

    byte[] getBytes(int parameterIndex) throws SQLException {
        synchronized (checkClosed().getConnectionMutex()) {
            BindValue bindValue = getBinding(parameterIndex, false);
            if (bindValue.isNull) {
                return null;
            } else if (bindValue.isLongData) {
                throw SQLError.createSQLFeatureNotSupportedException();
            } else {
                if (this.outByteBuffer == null) {
                    this.outByteBuffer = new Buffer(this.connection.getNetBufferLength());
                }
                this.outByteBuffer.clear();
                int originalPosition = this.outByteBuffer.getPosition();
                storeBinding(this.outByteBuffer, bindValue, this.connection.getIO());
                int length = this.outByteBuffer.getPosition() - originalPosition;
                byte[] valueAsBytes = new byte[length];
                System.arraycopy(this.outByteBuffer.getByteBuffer(), originalPosition, valueAsBytes, 0, length);
                return valueAsBytes;
            }
        }
    }

    public ResultSetMetaData getMetaData() throws SQLException {
        synchronized (checkClosed().getConnectionMutex()) {
            if (this.resultFields == null) {
                return null;
            }
            ResultSetMetaData resultSetMetaData = new ResultSetMetaData(this.resultFields, this.connection.getUseOldAliasMetadataBehavior(), this.connection.getYearIsDateType(), getExceptionInterceptor());
            return resultSetMetaData;
        }
    }

    public ParameterMetaData getParameterMetaData() throws SQLException {
        ParameterMetaData parameterMetaData;
        synchronized (checkClosed().getConnectionMutex()) {
            if (this.parameterMetaData == null) {
                this.parameterMetaData = new MysqlParameterMetadata(this.parameterFields, this.parameterCount, getExceptionInterceptor());
            }
            parameterMetaData = this.parameterMetaData;
        }
        return parameterMetaData;
    }

    boolean isNull(int paramIndex) {
        throw new IllegalArgumentException(Messages.getString("ServerPreparedStatement.7"));
    }

    protected void realClose(boolean calledExplicitly, boolean closeOpenResults) throws SQLException {
        MySQLConnection locallyScopedConn = this.connection;
        if (locallyScopedConn != null) {
            synchronized (locallyScopedConn.getConnectionMutex()) {
                if (this.connection != null) {
                    if (this.connection.getAutoGenerateTestcaseScript()) {
                        dumpCloseForTestcase();
                    }
                    SQLException exceptionDuringClose = null;
                    if (calledExplicitly && !this.connection.isClosed()) {
                        synchronized (this.connection.getConnectionMutex()) {
                            try {
                                MysqlIO mysql = this.connection.getIO();
                                Buffer packet = mysql.getSharedSendPacket();
                                packet.writeByte((byte) 25);
                                packet.writeLong(this.serverStatementId);
                                mysql.sendCommand(25, null, packet, true, null, 0);
                            } catch (SQLException sqlEx) {
                                exceptionDuringClose = sqlEx;
                            }
                        }
                    }
                    if (this.isCached) {
                        this.connection.decachePreparedStatement(this);
                        this.isCached = false;
                    }
                    super.realClose(calledExplicitly, closeOpenResults);
                    clearParametersInternal(false);
                    this.parameterBindings = null;
                    this.parameterFields = null;
                    this.resultFields = null;
                    if (exceptionDuringClose != null) {
                        throw exceptionDuringClose;
                    }
                }
            }
        }
    }

    protected void rePrepare() throws SQLException {
        synchronized (checkClosed().getConnectionMutex()) {
            this.invalidationException = null;
            try {
                serverPrepare(this.originalSql);
            } catch (SQLException sqlEx) {
                this.invalidationException = sqlEx;
            } catch (Exception ex) {
                this.invalidationException = SQLError.createSQLException(ex.toString(), SQLError.SQL_STATE_GENERAL_ERROR, getExceptionInterceptor());
                this.invalidationException.initCause(ex);
            }
            if (this.invalidationException != null) {
                this.invalid = true;
                this.parameterBindings = null;
                this.parameterFields = null;
                this.resultFields = null;
                if (this.results != null) {
                    try {
                        this.results.close();
                    } catch (Exception e) {
                    }
                }
                if (this.generatedKeysResults != null) {
                    try {
                        this.generatedKeysResults.close();
                    } catch (Exception e2) {
                    }
                }
                try {
                    closeAllOpenResults();
                } catch (Exception e3) {
                }
                if (!(this.connection == null || this.connection.getDontTrackOpenResources())) {
                    this.connection.unregisterStatement(this);
                }
            }
        }
    }

    boolean isCursorRequired() throws SQLException {
        return this.resultFields != null && this.connection.isCursorFetchEnabled() && getResultSetType() == 1003 && getResultSetConcurrency() == 1007 && getFetchSize() > 0;
    }

    /* JADX WARNING: inconsistent code. */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    private com.mysql.jdbc.ResultSetInternalMethods serverExecute(int r70, boolean r71, com.mysql.jdbc.Field[] r72) throws java.sql.SQLException {
        /*
        r69 = this;
        r13 = r69;
        r1 = r69.checkClosed();
        r14 = r1.getConnectionMutex();
        monitor-enter(r14);
        r1 = r13.connection;	 Catch:{ all -> 0x0574 }
        r1 = r1.getIO();	 Catch:{ all -> 0x0574 }
        r15 = r1;
        r1 = r15.shouldIntercept();	 Catch:{ all -> 0x0574 }
        r9 = 1;
        if (r1 == 0) goto L_0x0023;
    L_0x0019:
        r1 = r13.originalSql;	 Catch:{ all -> 0x0574 }
        r1 = r15.invokeStatementInterceptorsPre(r1, r13, r9);	 Catch:{ all -> 0x0574 }
        if (r1 == 0) goto L_0x0023;
    L_0x0021:
        monitor-exit(r14);	 Catch:{ all -> 0x0574 }
        return r1;
    L_0x0023:
        r1 = r13.detectedLongParameterSwitch;	 Catch:{ all -> 0x0574 }
        r12 = 0;
        if (r1 == 0) goto L_0x007b;
    L_0x0028:
        r1 = 0;
        r2 = 0;
        r3 = r2;
        r2 = r1;
        r1 = r12;
    L_0x002e:
        r5 = r13.parameterCount;	 Catch:{ all -> 0x0574 }
        r5 = r5 - r9;
        if (r1 >= r5) goto L_0x0078;
    L_0x0033:
        r5 = r13.parameterBindings;	 Catch:{ all -> 0x0574 }
        r5 = r5[r1];	 Catch:{ all -> 0x0574 }
        r5 = r5.isLongData;	 Catch:{ all -> 0x0574 }
        if (r5 == 0) goto L_0x0075;
    L_0x003b:
        if (r2 == 0) goto L_0x006d;
    L_0x003d:
        r5 = r13.parameterBindings;	 Catch:{ all -> 0x0574 }
        r5 = r5[r1];	 Catch:{ all -> 0x0574 }
        r5 = r5.boundBeforeExecutionNum;	 Catch:{ all -> 0x0574 }
        r7 = (r3 > r5 ? 1 : (r3 == r5 ? 0 : -1));
        if (r7 == 0) goto L_0x006d;
    L_0x0047:
        r5 = new java.lang.StringBuilder;	 Catch:{ all -> 0x0574 }
        r5.<init>();	 Catch:{ all -> 0x0574 }
        r6 = "ServerPreparedStatement.11";
        r6 = com.mysql.jdbc.Messages.getString(r6);	 Catch:{ all -> 0x0574 }
        r5.append(r6);	 Catch:{ all -> 0x0574 }
        r6 = "ServerPreparedStatement.12";
        r6 = com.mysql.jdbc.Messages.getString(r6);	 Catch:{ all -> 0x0574 }
        r5.append(r6);	 Catch:{ all -> 0x0574 }
        r5 = r5.toString();	 Catch:{ all -> 0x0574 }
        r6 = "S1C00";
        r7 = r69.getExceptionInterceptor();	 Catch:{ all -> 0x0574 }
        r5 = com.mysql.jdbc.SQLError.createSQLException(r5, r6, r7);	 Catch:{ all -> 0x0574 }
        throw r5;	 Catch:{ all -> 0x0574 }
    L_0x006d:
        r2 = 1;
        r5 = r13.parameterBindings;	 Catch:{ all -> 0x0574 }
        r5 = r5[r1];	 Catch:{ all -> 0x0574 }
        r5 = r5.boundBeforeExecutionNum;	 Catch:{ all -> 0x0574 }
        r3 = r5;
    L_0x0075:
        r1 = r1 + 1;
        goto L_0x002e;
    L_0x0078:
        r69.serverResetStatement();	 Catch:{ all -> 0x0574 }
    L_0x007b:
        r1 = r12;
    L_0x007c:
        r2 = r13.parameterCount;	 Catch:{ all -> 0x0574 }
        if (r1 >= r2) goto L_0x00b6;
    L_0x0080:
        r2 = r13.parameterBindings;	 Catch:{ all -> 0x0574 }
        r2 = r2[r1];	 Catch:{ all -> 0x0574 }
        r2 = r2.isSet;	 Catch:{ all -> 0x0574 }
        if (r2 != 0) goto L_0x00b3;
    L_0x0088:
        r2 = new java.lang.StringBuilder;	 Catch:{ all -> 0x0574 }
        r2.<init>();	 Catch:{ all -> 0x0574 }
        r3 = "ServerPreparedStatement.13";
        r3 = com.mysql.jdbc.Messages.getString(r3);	 Catch:{ all -> 0x0574 }
        r2.append(r3);	 Catch:{ all -> 0x0574 }
        r3 = r1 + 1;
        r2.append(r3);	 Catch:{ all -> 0x0574 }
        r3 = "ServerPreparedStatement.14";
        r3 = com.mysql.jdbc.Messages.getString(r3);	 Catch:{ all -> 0x0574 }
        r2.append(r3);	 Catch:{ all -> 0x0574 }
        r2 = r2.toString();	 Catch:{ all -> 0x0574 }
        r3 = "S1009";
        r4 = r69.getExceptionInterceptor();	 Catch:{ all -> 0x0574 }
        r2 = com.mysql.jdbc.SQLError.createSQLException(r2, r3, r4);	 Catch:{ all -> 0x0574 }
        throw r2;	 Catch:{ all -> 0x0574 }
    L_0x00b3:
        r1 = r1 + 1;
        goto L_0x007c;
    L_0x00b6:
        r1 = r12;
    L_0x00b7:
        r2 = r13.parameterCount;	 Catch:{ all -> 0x0574 }
        if (r1 >= r2) goto L_0x00cd;
    L_0x00bb:
        r2 = r13.parameterBindings;	 Catch:{ all -> 0x0574 }
        r2 = r2[r1];	 Catch:{ all -> 0x0574 }
        r2 = r2.isLongData;	 Catch:{ all -> 0x0574 }
        if (r2 == 0) goto L_0x00ca;
    L_0x00c3:
        r2 = r13.parameterBindings;	 Catch:{ all -> 0x0574 }
        r2 = r2[r1];	 Catch:{ all -> 0x0574 }
        r13.serverLongData(r1, r2);	 Catch:{ all -> 0x0574 }
    L_0x00ca:
        r1 = r1 + 1;
        goto L_0x00b7;
    L_0x00cd:
        r1 = r13.connection;	 Catch:{ all -> 0x0574 }
        r1 = r1.getAutoGenerateTestcaseScript();	 Catch:{ all -> 0x0574 }
        if (r1 == 0) goto L_0x00d8;
    L_0x00d5:
        r69.dumpExecuteForTestcase();	 Catch:{ all -> 0x0574 }
    L_0x00d8:
        r1 = r15.getSharedSendPacket();	 Catch:{ all -> 0x0574 }
        r10 = r1;
        r10.clear();	 Catch:{ all -> 0x0574 }
        r1 = 23;
        r10.writeByte(r1);	 Catch:{ all -> 0x0574 }
        r1 = r13.serverStatementId;	 Catch:{ all -> 0x0574 }
        r10.writeLong(r1);	 Catch:{ all -> 0x0574 }
        r1 = r13.connection;	 Catch:{ all -> 0x0574 }
        r2 = 4;
        r3 = 2;
        r1 = r1.versionMeetsMinimum(r2, r9, r3);	 Catch:{ all -> 0x0574 }
        if (r1 == 0) goto L_0x0106;
    L_0x00f4:
        r1 = r69.isCursorRequired();	 Catch:{ all -> 0x0574 }
        if (r1 == 0) goto L_0x00fe;
    L_0x00fa:
        r10.writeByte(r9);	 Catch:{ all -> 0x0574 }
        goto L_0x0101;
    L_0x00fe:
        r10.writeByte(r12);	 Catch:{ all -> 0x0574 }
    L_0x0101:
        r1 = 1;
        r10.writeLong(r1);	 Catch:{ all -> 0x0574 }
    L_0x0106:
        r1 = r13.parameterCount;	 Catch:{ all -> 0x0574 }
        r1 = r1 + 7;
        r1 = r1 / 8;
        r11 = r1;
        r1 = r10.getPosition();	 Catch:{ all -> 0x0574 }
        r2 = r12;
    L_0x0112:
        if (r2 >= r11) goto L_0x011a;
    L_0x0114:
        r10.writeByte(r12);	 Catch:{ all -> 0x0574 }
        r2 = r2 + 1;
        goto L_0x0112;
    L_0x011a:
        r2 = new byte[r11];	 Catch:{ all -> 0x0574 }
        r8 = r2;
        r2 = r13.sendTypesToServer;	 Catch:{ all -> 0x0574 }
        r10.writeByte(r2);	 Catch:{ all -> 0x0574 }
        r2 = r13.sendTypesToServer;	 Catch:{ all -> 0x0574 }
        if (r2 == 0) goto L_0x0137;
    L_0x0126:
        r2 = r12;
    L_0x0127:
        r3 = r13.parameterCount;	 Catch:{ all -> 0x0574 }
        if (r2 >= r3) goto L_0x0137;
    L_0x012b:
        r3 = r13.parameterBindings;	 Catch:{ all -> 0x0574 }
        r3 = r3[r2];	 Catch:{ all -> 0x0574 }
        r3 = r3.bufferType;	 Catch:{ all -> 0x0574 }
        r10.writeInt(r3);	 Catch:{ all -> 0x0574 }
        r2 = r2 + 1;
        goto L_0x0127;
    L_0x0137:
        r2 = r12;
    L_0x0138:
        r3 = r13.parameterCount;	 Catch:{ all -> 0x0574 }
        if (r2 >= r3) goto L_0x0163;
    L_0x013c:
        r3 = r13.parameterBindings;	 Catch:{ all -> 0x0574 }
        r3 = r3[r2];	 Catch:{ all -> 0x0574 }
        r3 = r3.isLongData;	 Catch:{ all -> 0x0574 }
        if (r3 != 0) goto L_0x0160;
    L_0x0144:
        r3 = r13.parameterBindings;	 Catch:{ all -> 0x0574 }
        r3 = r3[r2];	 Catch:{ all -> 0x0574 }
        r3 = r3.isNull;	 Catch:{ all -> 0x0574 }
        if (r3 != 0) goto L_0x0154;
    L_0x014c:
        r3 = r13.parameterBindings;	 Catch:{ all -> 0x0574 }
        r3 = r3[r2];	 Catch:{ all -> 0x0574 }
        r13.storeBinding(r10, r3, r15);	 Catch:{ all -> 0x0574 }
        goto L_0x0160;
    L_0x0154:
        r3 = r2 / 8;
        r4 = r8[r3];	 Catch:{ all -> 0x0574 }
        r5 = r2 & 7;
        r5 = r9 << r5;
        r4 = r4 | r5;
        r4 = (byte) r4;	 Catch:{ all -> 0x0574 }
        r8[r3] = r4;	 Catch:{ all -> 0x0574 }
    L_0x0160:
        r2 = r2 + 1;
        goto L_0x0138;
    L_0x0163:
        r2 = r10.getPosition();	 Catch:{ all -> 0x0574 }
        r7 = r2;
        r10.setPosition(r1);	 Catch:{ all -> 0x0574 }
        r10.writeBytesNoNull(r8);	 Catch:{ all -> 0x0574 }
        r10.setPosition(r7);	 Catch:{ all -> 0x0574 }
        r2 = 0;
        r4 = r13.connection;	 Catch:{ all -> 0x0574 }
        r4 = r4.getLogSlowQueries();	 Catch:{ all -> 0x0574 }
        r16 = r4;
        r4 = r13.connection;	 Catch:{ all -> 0x0574 }
        r4 = r4.getGatherPerformanceMetrics();	 Catch:{ all -> 0x0574 }
        r17 = r4;
        r4 = r13.profileSQL;	 Catch:{ all -> 0x0574 }
        if (r4 != 0) goto L_0x018f;
    L_0x0187:
        if (r16 != 0) goto L_0x018f;
    L_0x0189:
        if (r17 == 0) goto L_0x018c;
    L_0x018b:
        goto L_0x018f;
    L_0x018c:
        r18 = r2;
        goto L_0x0195;
    L_0x018f:
        r4 = r15.getCurrentTimeNanosOrMillis();	 Catch:{ all -> 0x0574 }
        r2 = r4;
        goto L_0x018c;
    L_0x0195:
        r69.resetCancelledState();	 Catch:{ all -> 0x0574 }
        r2 = 0;
        r3 = "";
        r4 = r13.profileSQL;	 Catch:{ SQLException -> 0x0516, all -> 0x0507 }
        if (r4 != 0) goto L_0x01a6;
    L_0x019f:
        if (r16 != 0) goto L_0x01a6;
    L_0x01a1:
        if (r17 == 0) goto L_0x01a4;
    L_0x01a3:
        goto L_0x01a6;
    L_0x01a4:
        r6 = r3;
        goto L_0x01ac;
    L_0x01a6:
        r4 = r13.asSql(r9);	 Catch:{ SQLException -> 0x0516, all -> 0x0507 }
        r3 = r4;
        goto L_0x01a4;
    L_0x01ac:
        r3 = r13.connection;	 Catch:{ SQLException -> 0x0516, all -> 0x0507 }
        r3 = r3.getEnableQueryTimeouts();	 Catch:{ SQLException -> 0x0516, all -> 0x0507 }
        if (r3 == 0) goto L_0x01f3;
    L_0x01b4:
        r3 = r13.timeoutInMillis;	 Catch:{ SQLException -> 0x01e3, all -> 0x01d4 }
        if (r3 == 0) goto L_0x01f3;
    L_0x01b8:
        r3 = r13.connection;	 Catch:{ SQLException -> 0x01e3, all -> 0x01d4 }
        r4 = 5;
        r3 = r3.versionMeetsMinimum(r4, r12, r12);	 Catch:{ SQLException -> 0x01e3, all -> 0x01d4 }
        if (r3 == 0) goto L_0x01f3;
    L_0x01c1:
        r3 = new com.mysql.jdbc.StatementImpl$CancelTask;	 Catch:{ SQLException -> 0x01e3, all -> 0x01d4 }
        r3.<init>(r13);	 Catch:{ SQLException -> 0x01e3, all -> 0x01d4 }
        r2 = r3;
        r3 = r13.connection;	 Catch:{ SQLException -> 0x01e3, all -> 0x01d4 }
        r3 = r3.getCancelTimer();	 Catch:{ SQLException -> 0x01e3, all -> 0x01d4 }
        r4 = r13.timeoutInMillis;	 Catch:{ SQLException -> 0x01e3, all -> 0x01d4 }
        r4 = (long) r4;	 Catch:{ SQLException -> 0x01e3, all -> 0x01d4 }
        r3.schedule(r2, r4);	 Catch:{ SQLException -> 0x01e3, all -> 0x01d4 }
        goto L_0x01f3;
    L_0x01d4:
        r0 = move-exception;
        r24 = r1;
        r22 = r2;
        r20 = r7;
        r21 = r8;
    L_0x01dd:
        r29 = r10;
        r30 = r11;
        goto L_0x0537;
    L_0x01e3:
        r0 = move-exception;
        r24 = r1;
        r22 = r2;
        r20 = r7;
        r21 = r8;
    L_0x01ec:
        r29 = r10;
        r30 = r11;
    L_0x01f0:
        r1 = r0;
        goto L_0x0524;
    L_0x01f3:
        r5 = r2;
        r69.statementBegins();	 Catch:{ SQLException -> 0x04f7, all -> 0x04e7 }
        r3 = 23;
        r4 = 0;
        r20 = 0;
        r21 = 0;
        r22 = 0;
        r2 = r15;
        r9 = r5;
        r5 = r10;
        r24 = r6;
        r6 = r20;
        r20 = r7;
        r7 = r21;
        r21 = r8;
        r8 = r22;
        r8 = r2.sendCommand(r3, r4, r5, r6, r7, r8);	 Catch:{ SQLException -> 0x04dc, all -> 0x04d0 }
        r2 = 0;
        if (r16 != 0) goto L_0x022a;
    L_0x0217:
        if (r17 != 0) goto L_0x022a;
    L_0x0219:
        r4 = r13.profileSQL;	 Catch:{ SQLException -> 0x0224, all -> 0x021e }
        if (r4 == 0) goto L_0x022f;
    L_0x021d:
        goto L_0x022a;
    L_0x021e:
        r0 = move-exception;
        r24 = r1;
        r22 = r9;
        goto L_0x01dd;
    L_0x0224:
        r0 = move-exception;
        r24 = r1;
        r22 = r9;
        goto L_0x01ec;
    L_0x022a:
        r4 = r15.getCurrentTimeNanosOrMillis();	 Catch:{ SQLException -> 0x04dc, all -> 0x04d0 }
        r2 = r4;
    L_0x022f:
        r25 = r2;
        if (r9 == 0) goto L_0x024a;
    L_0x0233:
        r9.cancel();	 Catch:{ SQLException -> 0x0224, all -> 0x021e }
        r2 = r13.connection;	 Catch:{ SQLException -> 0x0224, all -> 0x021e }
        r2 = r2.getCancelTimer();	 Catch:{ SQLException -> 0x0224, all -> 0x021e }
        r2.purge();	 Catch:{ SQLException -> 0x0224, all -> 0x021e }
        r2 = r9.caughtWhileCancelling;	 Catch:{ SQLException -> 0x0224, all -> 0x021e }
        if (r2 == 0) goto L_0x0246;
    L_0x0243:
        r2 = r9.caughtWhileCancelling;	 Catch:{ SQLException -> 0x0224, all -> 0x021e }
        throw r2;	 Catch:{ SQLException -> 0x0224, all -> 0x021e }
    L_0x0246:
        r2 = 0;
        r22 = r2;
        goto L_0x024c;
    L_0x024a:
        r22 = r9;
    L_0x024c:
        r2 = r13.cancelTimeoutMutex;	 Catch:{ SQLException -> 0x04c7, all -> 0x04be }
        monitor-enter(r2);	 Catch:{ SQLException -> 0x04c7, all -> 0x04be }
        r3 = r13.wasCancelled;	 Catch:{ all -> 0x04ac }
        if (r3 == 0) goto L_0x0274;
    L_0x0253:
        r3 = 0;
        r4 = r13.wasCancelledByTimeout;	 Catch:{ all -> 0x0269 }
        if (r4 == 0) goto L_0x025f;
    L_0x0258:
        r4 = new com.mysql.jdbc.exceptions.MySQLTimeoutException;	 Catch:{ all -> 0x0269 }
        r4.<init>();	 Catch:{ all -> 0x0269 }
        r3 = r4;
        goto L_0x0265;
    L_0x025f:
        r4 = new com.mysql.jdbc.exceptions.MySQLStatementCancelledException;	 Catch:{ all -> 0x0269 }
        r4.<init>();	 Catch:{ all -> 0x0269 }
        r3 = r4;
    L_0x0265:
        r69.resetCancelledState();	 Catch:{ all -> 0x0269 }
        throw r3;	 Catch:{ all -> 0x0269 }
    L_0x0269:
        r0 = move-exception;
        r29 = r10;
        r30 = r11;
        r64 = r24;
        r24 = r1;
        goto L_0x04bc;
    L_0x0274:
        monitor-exit(r2);	 Catch:{ all -> 0x04ac }
        r2 = 0;
        if (r16 != 0) goto L_0x0283;
    L_0x0278:
        if (r17 == 0) goto L_0x027b;
    L_0x027a:
        goto L_0x0283;
    L_0x027b:
        r43 = r1;
        r23 = r2;
        r9 = r24;
        goto L_0x0370;
    L_0x0283:
        r3 = r25 - r18;
        if (r16 == 0) goto L_0x02b5;
    L_0x0287:
        r5 = r13.useAutoSlowLog;	 Catch:{ SQLException -> 0x02b0, all -> 0x02ab }
        if (r5 == 0) goto L_0x029e;
    L_0x028b:
        r5 = r13.connection;	 Catch:{ SQLException -> 0x02b0, all -> 0x02ab }
        r5 = r5.getSlowQueryThresholdMillis();	 Catch:{ SQLException -> 0x02b0, all -> 0x02ab }
        r5 = (long) r5;	 Catch:{ SQLException -> 0x02b0, all -> 0x02ab }
        r7 = (r3 > r5 ? 1 : (r3 == r5 ? 0 : -1));
        if (r7 <= 0) goto L_0x0299;
    L_0x0296:
        r23 = 1;
        goto L_0x029b;
    L_0x0299:
        r23 = r12;
    L_0x029b:
        r2 = r23;
        goto L_0x02b5;
    L_0x029e:
        r5 = r13.connection;	 Catch:{ SQLException -> 0x02b0, all -> 0x02ab }
        r5 = r5.isAbonormallyLongQuery(r3);	 Catch:{ SQLException -> 0x02b0, all -> 0x02ab }
        r2 = r5;
        r5 = r13.connection;	 Catch:{ SQLException -> 0x02b0, all -> 0x02ab }
        r5.reportQueryTime(r3);	 Catch:{ SQLException -> 0x02b0, all -> 0x02ab }
        goto L_0x02b5;
    L_0x02ab:
        r0 = move-exception;
        r24 = r1;
        goto L_0x01dd;
    L_0x02b0:
        r0 = move-exception;
        r24 = r1;
        goto L_0x01ec;
    L_0x02b5:
        if (r2 == 0) goto L_0x034e;
    L_0x02b7:
        r5 = new java.lang.StringBuilder;	 Catch:{ SQLException -> 0x0344, all -> 0x033a }
        r6 = 48;
        r7 = r13.originalSql;	 Catch:{ SQLException -> 0x0344, all -> 0x033a }
        r7 = r7.length();	 Catch:{ SQLException -> 0x0344, all -> 0x033a }
        r6 = r6 + r7;
        r5.<init>(r6);	 Catch:{ SQLException -> 0x0344, all -> 0x033a }
        r6 = "ServerPreparedStatement.15";
        r6 = com.mysql.jdbc.Messages.getString(r6);	 Catch:{ SQLException -> 0x0344, all -> 0x033a }
        r5.append(r6);	 Catch:{ SQLException -> 0x0344, all -> 0x033a }
        r6 = r15.getSlowQueryThreshold();	 Catch:{ SQLException -> 0x0344, all -> 0x033a }
        r5.append(r6);	 Catch:{ SQLException -> 0x0344, all -> 0x033a }
        r6 = "ServerPreparedStatement.15a";
        r6 = com.mysql.jdbc.Messages.getString(r6);	 Catch:{ SQLException -> 0x0344, all -> 0x033a }
        r5.append(r6);	 Catch:{ SQLException -> 0x0344, all -> 0x033a }
        r5.append(r3);	 Catch:{ SQLException -> 0x0344, all -> 0x033a }
        r6 = "ServerPreparedStatement.16";
        r6 = com.mysql.jdbc.Messages.getString(r6);	 Catch:{ SQLException -> 0x0344, all -> 0x033a }
        r5.append(r6);	 Catch:{ SQLException -> 0x0344, all -> 0x033a }
        r6 = "as prepared: ";
        r5.append(r6);	 Catch:{ SQLException -> 0x0344, all -> 0x033a }
        r6 = r13.originalSql;	 Catch:{ SQLException -> 0x0344, all -> 0x033a }
        r5.append(r6);	 Catch:{ SQLException -> 0x0344, all -> 0x033a }
        r6 = "\n\n with parameters bound:\n\n";
        r5.append(r6);	 Catch:{ SQLException -> 0x0344, all -> 0x033a }
        r9 = r24;
        r5.append(r9);	 Catch:{ SQLException -> 0x0344, all -> 0x033a }
        r6 = r13.eventSink;	 Catch:{ SQLException -> 0x0344, all -> 0x033a }
        r7 = new com.mysql.jdbc.profiler.ProfilerEvent;	 Catch:{ SQLException -> 0x0344, all -> 0x033a }
        r28 = 6;
        r29 = "";
        r12 = r13.currentCatalog;	 Catch:{ SQLException -> 0x0344, all -> 0x033a }
        r43 = r1;
        r1 = r13.connection;	 Catch:{ SQLException -> 0x0364, all -> 0x035a }
        r31 = r1.getId();	 Catch:{ SQLException -> 0x0364, all -> 0x035a }
        r33 = r69.getId();	 Catch:{ SQLException -> 0x0364, all -> 0x035a }
        r34 = 0;
        r35 = java.lang.System.currentTimeMillis();	 Catch:{ SQLException -> 0x0364, all -> 0x035a }
        r39 = r15.getQueryTimingUnits();	 Catch:{ SQLException -> 0x0364, all -> 0x035a }
        r40 = 0;
        r1 = new java.lang.Throwable;	 Catch:{ SQLException -> 0x0364, all -> 0x035a }
        r1.<init>();	 Catch:{ SQLException -> 0x0364, all -> 0x035a }
        r41 = com.mysql.jdbc.log.LogUtils.findCallingClassAndMethod(r1);	 Catch:{ SQLException -> 0x0364, all -> 0x035a }
        r42 = r5.toString();	 Catch:{ SQLException -> 0x0364, all -> 0x035a }
        r27 = r7;
        r30 = r12;
        r37 = r3;
        r27.<init>(r28, r29, r30, r31, r33, r34, r35, r37, r39, r40, r41, r42);	 Catch:{ SQLException -> 0x0364, all -> 0x035a }
        r6.consumeEvent(r7);	 Catch:{ SQLException -> 0x0364, all -> 0x035a }
        goto L_0x0352;
    L_0x033a:
        r0 = move-exception;
        r24 = r1;
        r29 = r10;
        r30 = r11;
        r1 = r0;
        goto L_0x0538;
    L_0x0344:
        r0 = move-exception;
        r24 = r1;
        r29 = r10;
        r30 = r11;
        r1 = r0;
        goto L_0x0524;
    L_0x034e:
        r43 = r1;
        r9 = r24;
    L_0x0352:
        if (r17 == 0) goto L_0x036e;
    L_0x0354:
        r1 = r13.connection;	 Catch:{ SQLException -> 0x0364, all -> 0x035a }
        r1.registerQueryExecutionTime(r3);	 Catch:{ SQLException -> 0x0364, all -> 0x035a }
        goto L_0x036e;
    L_0x035a:
        r0 = move-exception;
        r1 = r0;
        r29 = r10;
        r30 = r11;
        r24 = r43;
        goto L_0x0538;
    L_0x0364:
        r0 = move-exception;
        r1 = r0;
        r29 = r10;
        r30 = r11;
        r24 = r43;
        goto L_0x0524;
    L_0x036e:
        r23 = r2;
    L_0x0370:
        r1 = r13.connection;	 Catch:{ SQLException -> 0x04a2, all -> 0x0498 }
        r1.incrementNumberOfPreparedExecutes();	 Catch:{ SQLException -> 0x04a2, all -> 0x0498 }
        r1 = r13.profileSQL;	 Catch:{ SQLException -> 0x04a2, all -> 0x0498 }
        if (r1 == 0) goto L_0x03bc;
    L_0x0379:
        r1 = r13.connection;	 Catch:{ SQLException -> 0x0364, all -> 0x035a }
        r1 = com.mysql.jdbc.ProfilerEventHandlerFactory.getInstance(r1);	 Catch:{ SQLException -> 0x0364, all -> 0x035a }
        r13.eventSink = r1;	 Catch:{ SQLException -> 0x0364, all -> 0x035a }
        r1 = r13.eventSink;	 Catch:{ SQLException -> 0x0364, all -> 0x035a }
        r2 = new com.mysql.jdbc.profiler.ProfilerEvent;	 Catch:{ SQLException -> 0x0364, all -> 0x035a }
        r28 = 4;
        r29 = "";
        r3 = r13.currentCatalog;	 Catch:{ SQLException -> 0x0364, all -> 0x035a }
        r4 = r13.connectionId;	 Catch:{ SQLException -> 0x0364, all -> 0x035a }
        r6 = r13.statementId;	 Catch:{ SQLException -> 0x0364, all -> 0x035a }
        r34 = -1;
        r35 = java.lang.System.currentTimeMillis();	 Catch:{ SQLException -> 0x0364, all -> 0x035a }
        r30 = r15.getCurrentTimeNanosOrMillis();	 Catch:{ SQLException -> 0x0364, all -> 0x035a }
        r37 = r30 - r18;
        r39 = r15.getQueryTimingUnits();	 Catch:{ SQLException -> 0x0364, all -> 0x035a }
        r40 = 0;
        r7 = new java.lang.Throwable;	 Catch:{ SQLException -> 0x0364, all -> 0x035a }
        r7.<init>();	 Catch:{ SQLException -> 0x0364, all -> 0x035a }
        r41 = com.mysql.jdbc.log.LogUtils.findCallingClassAndMethod(r7);	 Catch:{ SQLException -> 0x0364, all -> 0x035a }
        r42 = r13.truncateQueryToLog(r9);	 Catch:{ SQLException -> 0x0364, all -> 0x035a }
        r27 = r2;
        r30 = r3;
        r31 = r4;
        r33 = r6;
        r27.<init>(r28, r29, r30, r31, r33, r34, r35, r37, r39, r40, r41, r42);	 Catch:{ SQLException -> 0x0364, all -> 0x035a }
        r1.consumeEvent(r2);	 Catch:{ SQLException -> 0x0364, all -> 0x035a }
    L_0x03bc:
        r4 = r13.resultSetType;	 Catch:{ SQLException -> 0x04a2, all -> 0x0498 }
        r5 = r13.resultSetConcurrency;	 Catch:{ SQLException -> 0x04a2, all -> 0x0498 }
        r7 = r13.currentCatalog;	 Catch:{ SQLException -> 0x04a2, all -> 0x0498 }
        r12 = 1;
        r1 = r13.fieldCount;	 Catch:{ SQLException -> 0x04a2, all -> 0x0498 }
        r2 = (long) r1;
        r24 = r43;
        r1 = r15;
        r27 = r2;
        r2 = r13;
        r3 = r70;
        r6 = r71;
        r44 = r9;
        r9 = r12;
        r29 = r10;
        r30 = r11;
        r10 = r27;
        r12 = r72;
        r4 = r1.readAllResults(r2, r3, r4, r5, r6, r7, r8, r9, r10, r12);	 Catch:{ SQLException -> 0x04b8 }
        r1 = r15.shouldIntercept();	 Catch:{ SQLException -> 0x04b8 }
        if (r1 == 0) goto L_0x03f3;
    L_0x03e5:
        r2 = r13.originalSql;	 Catch:{ SQLException -> 0x04b8 }
        r5 = 1;
        r6 = 0;
        r1 = r15;
        r3 = r13;
        r1 = r1.invokeStatementInterceptorsPost(r2, r3, r4, r5, r6);	 Catch:{ SQLException -> 0x04b8 }
        if (r1 == 0) goto L_0x03f3;
        goto L_0x03f4;
    L_0x03f3:
        r1 = r4;
    L_0x03f4:
        r2 = r13.profileSQL;	 Catch:{ SQLException -> 0x04b8 }
        if (r2 == 0) goto L_0x0433;
    L_0x03f8:
        r2 = r15.getCurrentTimeNanosOrMillis();	 Catch:{ SQLException -> 0x04b8 }
        r4 = r13.eventSink;	 Catch:{ SQLException -> 0x04b8 }
        r5 = new com.mysql.jdbc.profiler.ProfilerEvent;	 Catch:{ SQLException -> 0x04b8 }
        r46 = 5;
        r47 = "";
        r6 = r13.currentCatalog;	 Catch:{ SQLException -> 0x04b8 }
        r7 = r13.connection;	 Catch:{ SQLException -> 0x04b8 }
        r49 = r7.getId();	 Catch:{ SQLException -> 0x04b8 }
        r51 = r69.getId();	 Catch:{ SQLException -> 0x04b8 }
        r52 = 0;
        r53 = java.lang.System.currentTimeMillis();	 Catch:{ SQLException -> 0x04b8 }
        r55 = r2 - r25;
        r57 = r15.getQueryTimingUnits();	 Catch:{ SQLException -> 0x04b8 }
        r58 = 0;
        r7 = new java.lang.Throwable;	 Catch:{ SQLException -> 0x04b8 }
        r7.<init>();	 Catch:{ SQLException -> 0x04b8 }
        r59 = com.mysql.jdbc.log.LogUtils.findCallingClassAndMethod(r7);	 Catch:{ SQLException -> 0x04b8 }
        r60 = 0;
        r45 = r5;
        r48 = r6;
        r45.<init>(r46, r47, r48, r49, r51, r52, r53, r55, r57, r58, r59, r60);	 Catch:{ SQLException -> 0x04b8 }
        r4.consumeEvent(r5);	 Catch:{ SQLException -> 0x04b8 }
    L_0x0433:
        if (r23 == 0) goto L_0x0447;
    L_0x0435:
        r2 = r13.connection;	 Catch:{ SQLException -> 0x04b8 }
        r2 = r2.getExplainSlowQueries();	 Catch:{ SQLException -> 0x04b8 }
        if (r2 == 0) goto L_0x0447;
    L_0x043d:
        r3 = r44;
        r2 = com.mysql.jdbc.StringUtils.getBytes(r3);	 Catch:{ SQLException -> 0x04b8 }
        r15.explainSlowQuery(r2, r3);	 Catch:{ SQLException -> 0x04b8 }
        goto L_0x0449;
    L_0x0447:
        r3 = r44;
    L_0x0449:
        if (r71 != 0) goto L_0x0452;
    L_0x044b:
        r2 = r13.serverNeedsResetBeforeEachExecution;	 Catch:{ SQLException -> 0x04b8 }
        if (r2 == 0) goto L_0x0452;
    L_0x044f:
        r69.serverResetStatement();	 Catch:{ SQLException -> 0x04b8 }
    L_0x0452:
        r10 = 0;
        r13.sendTypesToServer = r10;	 Catch:{ SQLException -> 0x04b8 }
        r13.results = r1;	 Catch:{ SQLException -> 0x04b8 }
        r2 = r15.hadWarnings();	 Catch:{ SQLException -> 0x04b8 }
        if (r2 == 0) goto L_0x0460;
    L_0x045d:
        r15.scanForAndThrowDataTruncation();	 Catch:{ SQLException -> 0x04b8 }
        r2 = r15;
        r4 = r29;
        r5 = r30;
        r6 = r24;
        r9 = r21;
        r11 = r20;
        r12 = r16;
        r15 = r17;
        r61 = r22;
        r62 = r13;
        r16 = r70;
        r7 = r71;
        r17 = r72;
        r63 = r2;
        r10 = r62;
        r2 = r10.statementExecuting;	 Catch:{ all -> 0x057f }
        r64 = r3;
        r3 = 0;
        r2.set(r3);	 Catch:{ all -> 0x057f }
        r2 = r61;
        if (r2 == 0) goto L_0x0496;
    L_0x048a:
        r2.cancel();	 Catch:{ all -> 0x057f }
        r3 = r10.connection;	 Catch:{ all -> 0x057f }
        r3 = r3.getCancelTimer();	 Catch:{ all -> 0x057f }
        r3.purge();	 Catch:{ all -> 0x057f }
    L_0x0496:
        monitor-exit(r14);	 Catch:{ all -> 0x057f }
        return r1;
    L_0x0498:
        r0 = move-exception;
        r29 = r10;
        r30 = r11;
        r24 = r43;
        r1 = r0;
        goto L_0x0538;
    L_0x04a2:
        r0 = move-exception;
        r29 = r10;
        r30 = r11;
        r24 = r43;
        r1 = r0;
        goto L_0x0524;
    L_0x04ac:
        r0 = move-exception;
        r29 = r10;
        r30 = r11;
        r64 = r24;
        r24 = r1;
        r1 = r0;
    L_0x04b6:
        monitor-exit(r2);	 Catch:{ all -> 0x04bb }
        throw r1;	 Catch:{ SQLException -> 0x04b8 }
    L_0x04b8:
        r0 = move-exception;
        goto L_0x01f0;
    L_0x04bb:
        r0 = move-exception;
    L_0x04bc:
        r1 = r0;
        goto L_0x04b6;
    L_0x04be:
        r0 = move-exception;
        r24 = r1;
        r29 = r10;
        r30 = r11;
        r1 = r0;
        goto L_0x04da;
    L_0x04c7:
        r0 = move-exception;
        r24 = r1;
        r29 = r10;
        r30 = r11;
        r1 = r0;
        goto L_0x04e6;
    L_0x04d0:
        r0 = move-exception;
        r24 = r1;
        r29 = r10;
        r30 = r11;
        r1 = r0;
        r22 = r9;
    L_0x04da:
        goto L_0x0538;
    L_0x04dc:
        r0 = move-exception;
        r24 = r1;
        r29 = r10;
        r30 = r11;
        r1 = r0;
        r22 = r9;
    L_0x04e6:
        goto L_0x0524;
    L_0x04e7:
        r0 = move-exception;
        r24 = r1;
        r9 = r5;
        r20 = r7;
        r21 = r8;
        r29 = r10;
        r30 = r11;
        r1 = r0;
        r22 = r9;
        goto L_0x0538;
    L_0x04f7:
        r0 = move-exception;
        r24 = r1;
        r9 = r5;
        r20 = r7;
        r21 = r8;
        r29 = r10;
        r30 = r11;
        r1 = r0;
        r22 = r9;
        goto L_0x0524;
    L_0x0507:
        r0 = move-exception;
        r24 = r1;
        r20 = r7;
        r21 = r8;
        r29 = r10;
        r30 = r11;
        r1 = r0;
        r22 = r2;
        goto L_0x0538;
    L_0x0516:
        r0 = move-exception;
        r24 = r1;
        r20 = r7;
        r21 = r8;
        r29 = r10;
        r30 = r11;
        r1 = r0;
        r22 = r2;
    L_0x0524:
        r8 = r1;
        r1 = r15.shouldIntercept();	 Catch:{ all -> 0x0536 }
        if (r1 == 0) goto L_0x0535;
    L_0x052b:
        r2 = r13.originalSql;	 Catch:{ all -> 0x0536 }
        r4 = 0;
        r5 = 1;
        r1 = r15;
        r3 = r13;
        r6 = r8;
        r1.invokeStatementInterceptorsPost(r2, r3, r4, r5, r6);	 Catch:{ all -> 0x0536 }
    L_0x0535:
        throw r8;	 Catch:{ all -> 0x0536 }
    L_0x0536:
        r0 = move-exception;
    L_0x0537:
        r1 = r0;
    L_0x0538:
        r2 = r15;
        r3 = r29;
        r4 = r30;
        r5 = r24;
        r6 = r21;
        r8 = r20;
        r9 = r18;
        r11 = r16;
        r12 = r17;
        r15 = r22;
        r65 = r13;
        r16 = r70;
        r7 = r71;
        r17 = r72;
        r66 = r2;
        r67 = r3;
        r2 = r65;
        r3 = r2.statementExecuting;	 Catch:{ all -> 0x0570 }
        r68 = r4;
        r4 = 0;
        r3.set(r4);	 Catch:{ all -> 0x0570 }
        if (r15 == 0) goto L_0x056f;
    L_0x0563:
        r15.cancel();	 Catch:{ all -> 0x0570 }
        r3 = r2.connection;	 Catch:{ all -> 0x0570 }
        r3 = r3.getCancelTimer();	 Catch:{ all -> 0x0570 }
        r3.purge();	 Catch:{ all -> 0x0570 }
    L_0x056f:
        throw r1;	 Catch:{ all -> 0x0570 }
    L_0x0570:
        r0 = move-exception;
        r1 = r0;
        r10 = r2;
        goto L_0x057d;
    L_0x0574:
        r0 = move-exception;
        r16 = r70;
        r7 = r71;
        r17 = r72;
        r1 = r0;
        r10 = r13;
    L_0x057d:
        monitor-exit(r14);	 Catch:{ all -> 0x057f }
        throw r1;
    L_0x057f:
        r0 = move-exception;
        r1 = r0;
        goto L_0x057d;
        */
        throw new UnsupportedOperationException("Method not decompiled: com.mysql.jdbc.ServerPreparedStatement.serverExecute(int, boolean, com.mysql.jdbc.Field[]):com.mysql.jdbc.ResultSetInternalMethods");
    }

    private void serverLongData(int parameterIndex, BindValue longData) throws SQLException {
        synchronized (checkClosed().getConnectionMutex()) {
            MysqlIO mysql = this.connection.getIO();
            Buffer packet = mysql.getSharedSendPacket();
            Object value = longData.value;
            if (value instanceof byte[]) {
                packet.clear();
                packet.writeByte((byte) 24);
                packet.writeLong(this.serverStatementId);
                packet.writeInt(parameterIndex);
                packet.writeBytesNoNull((byte[]) longData.value);
                mysql.sendCommand(24, null, packet, true, null, 0);
            } else if (value instanceof InputStream) {
                storeStream(mysql, parameterIndex, packet, (InputStream) value);
            } else if (value instanceof Blob) {
                storeStream(mysql, parameterIndex, packet, ((Blob) value).getBinaryStream());
            } else if (value instanceof Reader) {
                storeReader(mysql, parameterIndex, packet, (Reader) value);
            } else {
                StringBuilder stringBuilder = new StringBuilder();
                stringBuilder.append(Messages.getString("ServerPreparedStatement.18"));
                stringBuilder.append(value.getClass().getName());
                stringBuilder.append("'");
                throw SQLError.createSQLException(stringBuilder.toString(), SQLError.SQL_STATE_ILLEGAL_ARGUMENT, getExceptionInterceptor());
            }
        }
    }

    private void serverPrepare(String sql) throws SQLException {
        Throwable th;
        Throwable th2;
        ServerPreparedStatement serverPreparedStatement;
        SQLException e;
        SQLException sqlEx;
        StringBuilder messageBuf;
        Throwable th3;
        String sql2;
        ServerPreparedStatement serverPreparedStatement2 = this;
        synchronized (checkClosed().getConnectionMutex()) {
            try {
                String str;
                MysqlIO mysql = serverPreparedStatement2.connection.getIO();
                if (serverPreparedStatement2.connection.getAutoGenerateTestcaseScript()) {
                    try {
                        dumpPrepareForTestcase();
                    } catch (Throwable th22) {
                        str = sql;
                        th = th22;
                        serverPreparedStatement = serverPreparedStatement2;
                        throw th;
                    }
                }
                long begin = 0;
                String str2;
                try {
                    str2 = sql;
                    try {
                        int i;
                        if (StringUtils.startsWithIgnoreCaseAndWs(str2, "LOAD DATA")) {
                            serverPreparedStatement2.isLoadDataQuery = true;
                        } else {
                            serverPreparedStatement2.isLoadDataQuery = false;
                        }
                        if (serverPreparedStatement2.connection.getProfileSql()) {
                            begin = System.currentTimeMillis();
                        }
                        long begin2 = begin;
                        String characterEncoding = null;
                        String connectionEncoding = serverPreparedStatement2.connection.getEncoding();
                        if (!(serverPreparedStatement2.isLoadDataQuery || !serverPreparedStatement2.connection.getUseUnicode() || connectionEncoding == null)) {
                            characterEncoding = connectionEncoding;
                        }
                        Buffer prepareResultPacket = mysql.sendCommand(22, str2, null, false, characterEncoding, 0);
                        if (serverPreparedStatement2.connection.versionMeetsMinimum(4, 1, 1)) {
                            prepareResultPacket.setPosition(1);
                        } else {
                            prepareResultPacket.setPosition(0);
                        }
                        serverPreparedStatement2.serverStatementId = prepareResultPacket.readLong();
                        serverPreparedStatement2.fieldCount = prepareResultPacket.readInt();
                        serverPreparedStatement2.parameterCount = prepareResultPacket.readInt();
                        serverPreparedStatement2.parameterBindings = new BindValue[serverPreparedStatement2.parameterCount];
                        for (int i2 = 0; i2 < serverPreparedStatement2.parameterCount; i2++) {
                            serverPreparedStatement2.parameterBindings[i2] = new BindValue();
                        }
                        serverPreparedStatement2.connection.incrementNumberOfPrepares();
                        if (serverPreparedStatement2.profileSQL) {
                            ProfilerEventHandler profilerEventHandler = serverPreparedStatement2.eventSink;
                            String str3 = serverPreparedStatement2.currentCatalog;
                            long j = serverPreparedStatement2.connectionId;
                            profilerEventHandler.consumeEvent(new ProfilerEvent((byte) 2, "", str3, j, serverPreparedStatement2.statementId, -1, System.currentTimeMillis(), mysql.getCurrentTimeNanosOrMillis() - begin2, mysql.getQueryTimingUnits(), null, LogUtils.findCallingClassAndMethod(new Throwable()), truncateQueryToLog(sql)));
                        }
                        boolean checkEOF = mysql.isEOFDeprecated() ^ true;
                        if (serverPreparedStatement2.parameterCount > 0 && serverPreparedStatement2.connection.versionMeetsMinimum(4, 1, 2) && !mysql.isVersion(5, 0, 0)) {
                            serverPreparedStatement2.parameterFields = new Field[serverPreparedStatement2.parameterCount];
                            for (i = 0; i < serverPreparedStatement2.parameterCount; i++) {
                                serverPreparedStatement2.parameterFields[i] = mysql.unpackField(mysql.readPacket(), false);
                            }
                            if (checkEOF) {
                                mysql.readPacket();
                            }
                        }
                        if (serverPreparedStatement2.fieldCount > 0) {
                            serverPreparedStatement2.resultFields = new Field[serverPreparedStatement2.fieldCount];
                            for (i = 0; i < serverPreparedStatement2.fieldCount; i++) {
                                serverPreparedStatement2.resultFields[i] = mysql.unpackField(mysql.readPacket(), false);
                            }
                            if (checkEOF) {
                                mysql.readPacket();
                            }
                        }
                        str = str2;
                        try {
                            serverPreparedStatement2.connection.getIO().clearInputStream();
                        } catch (Throwable th222) {
                            th = th222;
                            throw th;
                        }
                    } catch (SQLException e2) {
                        e = e2;
                        sqlEx = e;
                        try {
                            if (serverPreparedStatement2.connection.getDumpQueriesOnException()) {
                                messageBuf = new StringBuilder(serverPreparedStatement2.originalSql.length() + 32);
                                messageBuf.append("\n\nQuery being prepared when exception was thrown:\n\n");
                                messageBuf.append(serverPreparedStatement2.originalSql);
                                sqlEx = ConnectionImpl.appendMessageToException(sqlEx, messageBuf.toString(), getExceptionInterceptor());
                            }
                            throw sqlEx;
                        } catch (Throwable th4) {
                            th222 = th4;
                            th3 = th222;
                            sql2 = str2;
                            try {
                                serverPreparedStatement2.connection.getIO().clearInputStream();
                                throw th3;
                            } catch (Throwable th2222) {
                                th = th2222;
                                serverPreparedStatement = this;
                                str = sql2;
                                throw th;
                            }
                        }
                    }
                } catch (SQLException e3) {
                    e = e3;
                    str2 = sql;
                    sqlEx = e;
                    if (serverPreparedStatement2.connection.getDumpQueriesOnException()) {
                        messageBuf = new StringBuilder(serverPreparedStatement2.originalSql.length() + 32);
                        messageBuf.append("\n\nQuery being prepared when exception was thrown:\n\n");
                        messageBuf.append(serverPreparedStatement2.originalSql);
                        sqlEx = ConnectionImpl.appendMessageToException(sqlEx, messageBuf.toString(), getExceptionInterceptor());
                    }
                    throw sqlEx;
                } catch (Throwable th5) {
                    th2222 = th5;
                    str2 = sql;
                    th3 = th2222;
                    sql2 = str2;
                    serverPreparedStatement2.connection.getIO().clearInputStream();
                    throw th3;
                }
            } catch (Throwable th22222) {
                th = th22222;
                serverPreparedStatement = serverPreparedStatement2;
                throw th;
            }
        }
    }

    private String truncateQueryToLog(String sql) throws SQLException {
        String query;
        synchronized (checkClosed().getConnectionMutex()) {
            if (sql.length() > this.connection.getMaxQuerySizeToLog()) {
                StringBuilder queryBuf = new StringBuilder(this.connection.getMaxQuerySizeToLog() + 12);
                queryBuf.append(sql.substring(0, this.connection.getMaxQuerySizeToLog()));
                queryBuf.append(Messages.getString("MysqlIO.25"));
                query = queryBuf.toString();
            } else {
                query = sql;
            }
        }
        return query;
    }

    private void serverResetStatement() throws SQLException {
        Throwable th;
        synchronized (checkClosed().getConnectionMutex()) {
            try {
                MysqlIO mysql = this.connection.getIO();
                Buffer packet = mysql.getSharedSendPacket();
                packet.clear();
                packet.writeByte((byte) 26);
                packet.writeLong(this.serverStatementId);
                try {
                    mysql.sendCommand(26, null, packet, this.connection.versionMeetsMinimum(4, 1, 2) ^ 1, null, 0);
                    mysql.clearInputStream();
                } catch (SQLException sqlEx) {
                    throw sqlEx;
                } catch (Exception ex) {
                    SQLException sqlEx2 = SQLError.createSQLException(ex.toString(), SQLError.SQL_STATE_GENERAL_ERROR, getExceptionInterceptor());
                    sqlEx2.initCause(ex);
                    throw sqlEx2;
                } catch (Throwable th2) {
                    th = th2;
                    throw th;
                }
            } catch (Throwable th3) {
                th = th3;
                ServerPreparedStatement serverPreparedStatement = this;
                throw th;
            }
        }
    }

    public void setArray(int i, Array x) throws SQLException {
        throw SQLError.createSQLFeatureNotSupportedException();
    }

    public void setAsciiStream(int parameterIndex, InputStream x, int length) throws SQLException {
        synchronized (checkClosed().getConnectionMutex()) {
            if (x == null) {
                setNull(parameterIndex, -2);
            } else {
                BindValue binding = getBinding(parameterIndex, true);
                resetToType(binding, MysqlDefs.FIELD_TYPE_BLOB);
                binding.value = x;
                binding.isLongData = true;
                if (this.connection.getUseStreamLengthsInPrepStmts()) {
                    binding.bindLength = (long) length;
                } else {
                    binding.bindLength = -1;
                }
            }
        }
    }

    public void setBigDecimal(int parameterIndex, BigDecimal x) throws SQLException {
        synchronized (checkClosed().getConnectionMutex()) {
            if (x == null) {
                setNull(parameterIndex, 3);
            } else {
                BindValue binding = getBinding(parameterIndex, false);
                if (this.connection.versionMeetsMinimum(5, 0, 3)) {
                    resetToType(binding, 246);
                } else {
                    resetToType(binding, this.stringTypeCode);
                }
                binding.value = StringUtils.fixDecimalExponent(StringUtils.consistentToString(x));
            }
        }
    }

    public void setBinaryStream(int parameterIndex, InputStream x, int length) throws SQLException {
        synchronized (checkClosed().getConnectionMutex()) {
            if (x == null) {
                setNull(parameterIndex, -2);
            } else {
                BindValue binding = getBinding(parameterIndex, true);
                resetToType(binding, MysqlDefs.FIELD_TYPE_BLOB);
                binding.value = x;
                binding.isLongData = true;
                if (this.connection.getUseStreamLengthsInPrepStmts()) {
                    binding.bindLength = (long) length;
                } else {
                    binding.bindLength = -1;
                }
            }
        }
    }

    public void setBlob(int parameterIndex, Blob x) throws SQLException {
        synchronized (checkClosed().getConnectionMutex()) {
            if (x == null) {
                setNull(parameterIndex, -2);
            } else {
                BindValue binding = getBinding(parameterIndex, true);
                resetToType(binding, MysqlDefs.FIELD_TYPE_BLOB);
                binding.value = x;
                binding.isLongData = true;
                if (this.connection.getUseStreamLengthsInPrepStmts()) {
                    binding.bindLength = x.length();
                } else {
                    binding.bindLength = -1;
                }
            }
        }
    }

    public void setBoolean(int parameterIndex, boolean x) throws SQLException {
        setByte(parameterIndex, x);
    }

    public void setByte(int parameterIndex, byte x) throws SQLException {
        checkClosed();
        BindValue binding = getBinding(parameterIndex, null);
        resetToType(binding, 1);
        binding.longBinding = (long) x;
    }

    public void setBytes(int parameterIndex, byte[] x) throws SQLException {
        checkClosed();
        if (x == null) {
            setNull(parameterIndex, -2);
            return;
        }
        BindValue binding = getBinding(parameterIndex, null);
        resetToType(binding, 253);
        binding.value = x;
    }

    public void setCharacterStream(int parameterIndex, Reader reader, int length) throws SQLException {
        synchronized (checkClosed().getConnectionMutex()) {
            if (reader == null) {
                setNull(parameterIndex, -2);
            } else {
                BindValue binding = getBinding(parameterIndex, true);
                resetToType(binding, MysqlDefs.FIELD_TYPE_BLOB);
                binding.value = reader;
                binding.isLongData = true;
                if (this.connection.getUseStreamLengthsInPrepStmts()) {
                    binding.bindLength = (long) length;
                } else {
                    binding.bindLength = -1;
                }
            }
        }
    }

    public void setClob(int parameterIndex, Clob x) throws SQLException {
        synchronized (checkClosed().getConnectionMutex()) {
            if (x == null) {
                setNull(parameterIndex, -2);
            } else {
                BindValue binding = getBinding(parameterIndex, true);
                resetToType(binding, MysqlDefs.FIELD_TYPE_BLOB);
                binding.value = x.getCharacterStream();
                binding.isLongData = true;
                if (this.connection.getUseStreamLengthsInPrepStmts()) {
                    binding.bindLength = x.length();
                } else {
                    binding.bindLength = -1;
                }
            }
        }
    }

    public void setDate(int parameterIndex, java.sql.Date x) throws SQLException {
        setDate(parameterIndex, x, null);
    }

    public void setDate(int parameterIndex, java.sql.Date x, Calendar cal) throws SQLException {
        if (x == null) {
            setNull(parameterIndex, 91);
            return;
        }
        BindValue binding = getBinding(parameterIndex, null);
        resetToType(binding, 10);
        binding.value = x;
    }

    public void setDouble(int parameterIndex, double x) throws SQLException {
        synchronized (checkClosed().getConnectionMutex()) {
            if (this.connection.getAllowNanAndInf() || !(x == Double.POSITIVE_INFINITY || x == Double.NEGATIVE_INFINITY || Double.isNaN(x))) {
                BindValue binding = getBinding(parameterIndex, null);
                resetToType(binding, 5);
                binding.doubleBinding = x;
            } else {
                StringBuilder stringBuilder = new StringBuilder();
                stringBuilder.append("'");
                stringBuilder.append(x);
                stringBuilder.append("' is not a valid numeric or approximate numeric value");
                throw SQLError.createSQLException(stringBuilder.toString(), SQLError.SQL_STATE_ILLEGAL_ARGUMENT, getExceptionInterceptor());
            }
        }
    }

    public void setFloat(int parameterIndex, float x) throws SQLException {
        checkClosed();
        BindValue binding = getBinding(parameterIndex, null);
        resetToType(binding, 4);
        binding.floatBinding = x;
    }

    public void setInt(int parameterIndex, int x) throws SQLException {
        checkClosed();
        BindValue binding = getBinding(parameterIndex, null);
        resetToType(binding, 3);
        binding.longBinding = (long) x;
    }

    public void setLong(int parameterIndex, long x) throws SQLException {
        checkClosed();
        BindValue binding = getBinding(parameterIndex, null);
        resetToType(binding, 8);
        binding.longBinding = x;
    }

    public void setNull(int parameterIndex, int sqlType) throws SQLException {
        checkClosed();
        BindValue binding = getBinding(parameterIndex, null);
        resetToType(binding, 6);
        binding.isNull = true;
    }

    public void setNull(int parameterIndex, int sqlType, String typeName) throws SQLException {
        checkClosed();
        BindValue binding = getBinding(parameterIndex, null);
        resetToType(binding, 6);
        binding.isNull = true;
    }

    public void setRef(int i, Ref x) throws SQLException {
        throw SQLError.createSQLFeatureNotSupportedException();
    }

    public void setShort(int parameterIndex, short x) throws SQLException {
        checkClosed();
        BindValue binding = getBinding(parameterIndex, null);
        resetToType(binding, 2);
        binding.longBinding = (long) x;
    }

    public void setString(int parameterIndex, String x) throws SQLException {
        checkClosed();
        if (x == null) {
            setNull(parameterIndex, 1);
            return;
        }
        BindValue binding = getBinding(parameterIndex, null);
        resetToType(binding, this.stringTypeCode);
        binding.value = x;
    }

    public void setTime(int parameterIndex, Time x) throws SQLException {
        synchronized (checkClosed().getConnectionMutex()) {
            setTimeInternal(parameterIndex, x, null, this.connection.getDefaultTimeZone(), false);
        }
    }

    public void setTime(int parameterIndex, Time x, Calendar cal) throws SQLException {
        synchronized (checkClosed().getConnectionMutex()) {
            setTimeInternal(parameterIndex, x, cal, cal.getTimeZone(), true);
        }
    }

    private void setTimeInternal(int parameterIndex, Time x, Calendar targetCalendar, TimeZone tz, boolean rollForward) throws SQLException {
        if (x == null) {
            setNull(parameterIndex, 92);
            return;
        }
        BindValue binding = getBinding(parameterIndex, null);
        resetToType(binding, 11);
        if (this.useLegacyDatetimeCode) {
            Calendar sessionCalendar = getCalendarInstanceForSessionOrNew();
            binding.value = TimeUtil.changeTimezone(this.connection, sessionCalendar, targetCalendar, x, tz, this.connection.getServerTimezoneTZ(), rollForward);
            return;
        }
        binding.value = x;
    }

    public void setTimestamp(int parameterIndex, Timestamp x) throws SQLException {
        synchronized (checkClosed().getConnectionMutex()) {
            setTimestampInternal(parameterIndex, x, null, this.connection.getDefaultTimeZone(), false);
        }
    }

    public void setTimestamp(int parameterIndex, Timestamp x, Calendar cal) throws SQLException {
        synchronized (checkClosed().getConnectionMutex()) {
            setTimestampInternal(parameterIndex, x, cal, cal.getTimeZone(), true);
        }
    }

    private void setTimestampInternal(int parameterIndex, Timestamp x, Calendar targetCalendar, TimeZone tz, boolean rollForward) throws SQLException {
        if (x == null) {
            setNull(parameterIndex, 93);
            return;
        }
        BindValue binding = getBinding(parameterIndex, null);
        resetToType(binding, 12);
        if (!this.sendFractionalSeconds) {
            x = TimeUtil.truncateFractionalSeconds(x);
        }
        if (this.useLegacyDatetimeCode) {
            binding.value = TimeUtil.changeTimezone(this.connection, this.connection.getUseJDBCCompliantTimezoneShift() ? this.connection.getUtcCalendar() : getCalendarInstanceForSessionOrNew(), targetCalendar, x, tz, this.connection.getServerTimezoneTZ(), rollForward);
            return;
        }
        binding.value = x;
    }

    protected void resetToType(BindValue oldValue, int bufferType) throws SQLException {
        synchronized (checkClosed().getConnectionMutex()) {
            oldValue.reset();
            if (bufferType != 6 || oldValue.bufferType == 0) {
                if (oldValue.bufferType != bufferType) {
                    this.sendTypesToServer = true;
                    oldValue.bufferType = bufferType;
                }
            }
            oldValue.isSet = true;
            oldValue.boundBeforeExecutionNum = (long) this.numberOfExecutions;
        }
    }

    @Deprecated
    public void setUnicodeStream(int parameterIndex, InputStream x, int length) throws SQLException {
        checkClosed();
        throw SQLError.createSQLFeatureNotSupportedException();
    }

    public void setURL(int parameterIndex, URL x) throws SQLException {
        checkClosed();
        setString(parameterIndex, x.toString());
    }

    /* JADX WARNING: inconsistent code. */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    private void storeBinding(com.mysql.jdbc.Buffer r11, com.mysql.jdbc.ServerPreparedStatement.BindValue r12, com.mysql.jdbc.MysqlIO r13) throws java.sql.SQLException {
        /*
        r10 = this;
        r0 = r10.checkClosed();
        r0 = r0.getConnectionMutex();
        monitor-enter(r0);
        r1 = r12.value;	 Catch:{ UnsupportedEncodingException -> 0x00af }
        r2 = r12.bufferType;	 Catch:{ UnsupportedEncodingException -> 0x00af }
        r3 = 15;
        if (r2 == r3) goto L_0x0078;
    L_0x0011:
        r3 = 246; // 0xf6 float:3.45E-43 double:1.215E-321;
        if (r2 == r3) goto L_0x0078;
    L_0x0015:
        r3 = 4;
        r4 = 8;
        switch(r2) {
            case 0: goto L_0x0078;
            case 1: goto L_0x006f;
            case 2: goto L_0x0063;
            case 3: goto L_0x0057;
            case 4: goto L_0x004d;
            case 5: goto L_0x0043;
            default: goto L_0x001b;
        };
    L_0x001b:
        switch(r2) {
            case 7: goto L_0x0039;
            case 8: goto L_0x002f;
            default: goto L_0x001e;
        };
    L_0x001e:
        switch(r2) {
            case 10: goto L_0x0039;
            case 11: goto L_0x0027;
            case 12: goto L_0x0039;
            default: goto L_0x0021;
        };
    L_0x0021:
        switch(r2) {
            case 253: goto L_0x0078;
            case 254: goto L_0x0078;
            default: goto L_0x0024;
        };
        monitor-exit(r0);	 Catch:{ all -> 0x00ad }
        return;
    L_0x0027:
        r2 = r1;
        r2 = (java.sql.Time) r2;	 Catch:{ UnsupportedEncodingException -> 0x00af }
        r10.storeTime(r11, r2);	 Catch:{ UnsupportedEncodingException -> 0x00af }
        monitor-exit(r0);	 Catch:{ all -> 0x00ad }
        return;
    L_0x002f:
        r11.ensureCapacity(r4);	 Catch:{ UnsupportedEncodingException -> 0x00af }
        r2 = r12.longBinding;	 Catch:{ UnsupportedEncodingException -> 0x00af }
        r11.writeLongLong(r2);	 Catch:{ UnsupportedEncodingException -> 0x00af }
        monitor-exit(r0);	 Catch:{ all -> 0x00ad }
        return;
    L_0x0039:
        r2 = r1;
        r2 = (java.util.Date) r2;	 Catch:{ UnsupportedEncodingException -> 0x00af }
        r3 = r12.bufferType;	 Catch:{ UnsupportedEncodingException -> 0x00af }
        r10.storeDateTime(r11, r2, r13, r3);	 Catch:{ UnsupportedEncodingException -> 0x00af }
        monitor-exit(r0);	 Catch:{ all -> 0x00ad }
        return;
    L_0x0043:
        r11.ensureCapacity(r4);	 Catch:{ UnsupportedEncodingException -> 0x00af }
        r2 = r12.doubleBinding;	 Catch:{ UnsupportedEncodingException -> 0x00af }
        r11.writeDouble(r2);	 Catch:{ UnsupportedEncodingException -> 0x00af }
        monitor-exit(r0);	 Catch:{ all -> 0x00ad }
        return;
    L_0x004d:
        r11.ensureCapacity(r3);	 Catch:{ UnsupportedEncodingException -> 0x00af }
        r2 = r12.floatBinding;	 Catch:{ UnsupportedEncodingException -> 0x00af }
        r11.writeFloat(r2);	 Catch:{ UnsupportedEncodingException -> 0x00af }
        monitor-exit(r0);	 Catch:{ all -> 0x00ad }
        return;
    L_0x0057:
        r11.ensureCapacity(r3);	 Catch:{ UnsupportedEncodingException -> 0x00af }
        r2 = r12.longBinding;	 Catch:{ UnsupportedEncodingException -> 0x00af }
        r2 = (int) r2;	 Catch:{ UnsupportedEncodingException -> 0x00af }
        r2 = (long) r2;	 Catch:{ UnsupportedEncodingException -> 0x00af }
        r11.writeLong(r2);	 Catch:{ UnsupportedEncodingException -> 0x00af }
        monitor-exit(r0);	 Catch:{ all -> 0x00ad }
        return;
    L_0x0063:
        r2 = 2;
        r11.ensureCapacity(r2);	 Catch:{ UnsupportedEncodingException -> 0x00af }
        r2 = r12.longBinding;	 Catch:{ UnsupportedEncodingException -> 0x00af }
        r2 = (int) r2;	 Catch:{ UnsupportedEncodingException -> 0x00af }
        r11.writeInt(r2);	 Catch:{ UnsupportedEncodingException -> 0x00af }
        monitor-exit(r0);	 Catch:{ all -> 0x00ad }
        return;
    L_0x006f:
        r2 = r12.longBinding;	 Catch:{ UnsupportedEncodingException -> 0x00af }
        r2 = (int) r2;	 Catch:{ UnsupportedEncodingException -> 0x00af }
        r2 = (byte) r2;	 Catch:{ UnsupportedEncodingException -> 0x00af }
        r11.writeByte(r2);	 Catch:{ UnsupportedEncodingException -> 0x00af }
        monitor-exit(r0);	 Catch:{ all -> 0x00ad }
        return;
    L_0x0078:
        r2 = r1 instanceof byte[];	 Catch:{ UnsupportedEncodingException -> 0x00af }
        if (r2 == 0) goto L_0x0083;
    L_0x007c:
        r2 = r1;
        r2 = (byte[]) r2;	 Catch:{ UnsupportedEncodingException -> 0x00af }
        r11.writeLenBytes(r2);	 Catch:{ UnsupportedEncodingException -> 0x00af }
        goto L_0x00ab;
    L_0x0083:
        r2 = r10.isLoadDataQuery;	 Catch:{ UnsupportedEncodingException -> 0x00af }
        if (r2 != 0) goto L_0x00a1;
    L_0x0087:
        r4 = r1;
        r4 = (java.lang.String) r4;	 Catch:{ UnsupportedEncodingException -> 0x00af }
        r5 = r10.charEncoding;	 Catch:{ UnsupportedEncodingException -> 0x00af }
        r2 = r10.connection;	 Catch:{ UnsupportedEncodingException -> 0x00af }
        r6 = r2.getServerCharset();	 Catch:{ UnsupportedEncodingException -> 0x00af }
        r7 = r10.charConverter;	 Catch:{ UnsupportedEncodingException -> 0x00af }
        r2 = r10.connection;	 Catch:{ UnsupportedEncodingException -> 0x00af }
        r8 = r2.parserKnowsUnicode();	 Catch:{ UnsupportedEncodingException -> 0x00af }
        r9 = r10.connection;	 Catch:{ UnsupportedEncodingException -> 0x00af }
        r3 = r11;
        r3.writeLenString(r4, r5, r6, r7, r8, r9);	 Catch:{ UnsupportedEncodingException -> 0x00af }
        goto L_0x00ab;
    L_0x00a1:
        r2 = r1;
        r2 = (java.lang.String) r2;	 Catch:{ UnsupportedEncodingException -> 0x00af }
        r2 = com.mysql.jdbc.StringUtils.getBytes(r2);	 Catch:{ UnsupportedEncodingException -> 0x00af }
        r11.writeLenBytes(r2);	 Catch:{ UnsupportedEncodingException -> 0x00af }
    L_0x00ab:
        monitor-exit(r0);	 Catch:{ all -> 0x00ad }
        return;
    L_0x00ad:
        r1 = move-exception;
        goto L_0x00db;
    L_0x00af:
        r1 = move-exception;
        r2 = new java.lang.StringBuilder;	 Catch:{ all -> 0x00ad }
        r2.<init>();	 Catch:{ all -> 0x00ad }
        r3 = "ServerPreparedStatement.22";
        r3 = com.mysql.jdbc.Messages.getString(r3);	 Catch:{ all -> 0x00ad }
        r2.append(r3);	 Catch:{ all -> 0x00ad }
        r3 = r10.connection;	 Catch:{ all -> 0x00ad }
        r3 = r3.getEncoding();	 Catch:{ all -> 0x00ad }
        r2.append(r3);	 Catch:{ all -> 0x00ad }
        r3 = "'";
        r2.append(r3);	 Catch:{ all -> 0x00ad }
        r2 = r2.toString();	 Catch:{ all -> 0x00ad }
        r3 = "S1000";
        r4 = r10.getExceptionInterceptor();	 Catch:{ all -> 0x00ad }
        r2 = com.mysql.jdbc.SQLError.createSQLException(r2, r3, r4);	 Catch:{ all -> 0x00ad }
        throw r2;	 Catch:{ all -> 0x00ad }
    L_0x00db:
        monitor-exit(r0);	 Catch:{ all -> 0x00ad }
        throw r1;
        */
        throw new UnsupportedOperationException("Method not decompiled: com.mysql.jdbc.ServerPreparedStatement.storeBinding(com.mysql.jdbc.Buffer, com.mysql.jdbc.ServerPreparedStatement$BindValue, com.mysql.jdbc.MysqlIO):void");
    }

    /* JADX WARNING: inconsistent code. */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    private void storeDateTime412AndOlder(com.mysql.jdbc.Buffer r8, java.util.Date r9, int r10) throws java.sql.SQLException {
        /*
        r7 = this;
        r0 = r7.checkClosed();
        r0 = r0.getConnectionMutex();
        monitor-enter(r0);
        r1 = 0;
        r2 = r7.useLegacyDatetimeCode;	 Catch:{ all -> 0x009f }
        if (r2 != 0) goto L_0x001e;
    L_0x000e:
        r2 = 10;
        if (r10 != r2) goto L_0x0018;
    L_0x0012:
        r2 = r7.getDefaultTzCalendar();	 Catch:{ all -> 0x009f }
        r1 = r2;
        goto L_0x0036;
    L_0x0018:
        r2 = r7.getServerTzCalendar();	 Catch:{ all -> 0x009f }
        r1 = r2;
        goto L_0x0036;
    L_0x001e:
        r2 = r9 instanceof java.sql.Timestamp;	 Catch:{ all -> 0x009f }
        if (r2 == 0) goto L_0x0031;
    L_0x0022:
        r2 = r7.connection;	 Catch:{ all -> 0x009f }
        r2 = r2.getUseJDBCCompliantTimezoneShift();	 Catch:{ all -> 0x009f }
        if (r2 == 0) goto L_0x0031;
    L_0x002a:
        r2 = r7.connection;	 Catch:{ all -> 0x009f }
        r2 = r2.getUtcCalendar();	 Catch:{ all -> 0x009f }
        goto L_0x0035;
    L_0x0031:
        r2 = r7.getCalendarInstanceForSessionOrNew();	 Catch:{ all -> 0x009f }
    L_0x0035:
        r1 = r2;
    L_0x0036:
        r2 = r1.getTime();	 Catch:{ all -> 0x009f }
        r3 = 8;
        r8.ensureCapacity(r3);	 Catch:{ all -> 0x0096 }
        r3 = 7;
        r8.writeByte(r3);	 Catch:{ all -> 0x0096 }
        r1.setTime(r9);	 Catch:{ all -> 0x0096 }
        r3 = 1;
        r4 = r1.get(r3);	 Catch:{ all -> 0x0096 }
        r5 = 2;
        r5 = r1.get(r5);	 Catch:{ all -> 0x0096 }
        r5 = r5 + r3;
        r3 = 5;
        r3 = r1.get(r3);	 Catch:{ all -> 0x0096 }
        r8.writeInt(r4);	 Catch:{ all -> 0x0096 }
        r6 = (byte) r5;	 Catch:{ all -> 0x0096 }
        r8.writeByte(r6);	 Catch:{ all -> 0x0096 }
        r6 = (byte) r3;	 Catch:{ all -> 0x0096 }
        r8.writeByte(r6);	 Catch:{ all -> 0x0096 }
        r6 = r9 instanceof java.sql.Date;	 Catch:{ all -> 0x0096 }
        if (r6 == 0) goto L_0x0070;
    L_0x0065:
        r6 = 0;
        r8.writeByte(r6);	 Catch:{ all -> 0x0096 }
        r8.writeByte(r6);	 Catch:{ all -> 0x0096 }
        r8.writeByte(r6);	 Catch:{ all -> 0x0096 }
        goto L_0x008e;
    L_0x0070:
        r6 = 11;
        r6 = r1.get(r6);	 Catch:{ all -> 0x0096 }
        r6 = (byte) r6;	 Catch:{ all -> 0x0096 }
        r8.writeByte(r6);	 Catch:{ all -> 0x0096 }
        r6 = 12;
        r6 = r1.get(r6);	 Catch:{ all -> 0x0096 }
        r6 = (byte) r6;	 Catch:{ all -> 0x0096 }
        r8.writeByte(r6);	 Catch:{ all -> 0x0096 }
        r6 = 13;
        r6 = r1.get(r6);	 Catch:{ all -> 0x0096 }
        r6 = (byte) r6;	 Catch:{ all -> 0x0096 }
        r8.writeByte(r6);	 Catch:{ all -> 0x0096 }
        r3 = r7;
        r1.setTime(r2);	 Catch:{ all -> 0x00a3 }
        monitor-exit(r0);	 Catch:{ all -> 0x00a3 }
        return;
    L_0x0096:
        r3 = move-exception;
        r4 = r7;
        r1.setTime(r2);	 Catch:{ all -> 0x009c }
        throw r3;	 Catch:{ all -> 0x009c }
    L_0x009c:
        r1 = move-exception;
        r3 = r4;
        goto L_0x00a1;
    L_0x009f:
        r1 = move-exception;
        r3 = r7;
    L_0x00a1:
        monitor-exit(r0);	 Catch:{ all -> 0x00a3 }
        throw r1;
    L_0x00a3:
        r1 = move-exception;
        goto L_0x00a1;
        */
        throw new UnsupportedOperationException("Method not decompiled: com.mysql.jdbc.ServerPreparedStatement.storeDateTime412AndOlder(com.mysql.jdbc.Buffer, java.util.Date, int):void");
    }

    private void storeDateTime(Buffer intoBuf, Date dt, MysqlIO mysql, int bufferType) throws SQLException {
        synchronized (checkClosed().getConnectionMutex()) {
            if (this.connection.versionMeetsMinimum(4, 1, 3)) {
                storeDateTime413AndNewer(intoBuf, dt, bufferType);
            } else {
                storeDateTime412AndOlder(intoBuf, dt, bufferType);
            }
        }
    }

    private void storeDateTime413AndNewer(Buffer intoBuf, Date dt, int bufferType) throws SQLException {
        ServerPreparedStatement this;
        synchronized (checkClosed().getConnectionMutex()) {
            Calendar sessionCalendar;
            try {
                if (this.useLegacyDatetimeCode) {
                    Calendar utcCalendar = ((dt instanceof Timestamp) && this.connection.getUseJDBCCompliantTimezoneShift()) ? this.connection.getUtcCalendar() : getCalendarInstanceForSessionOrNew();
                    sessionCalendar = utcCalendar;
                } else if (bufferType == 10) {
                    sessionCalendar = getDefaultTzCalendar();
                } else {
                    sessionCalendar = getServerTzCalendar();
                }
                Date oldTime = sessionCalendar.getTime();
                int i;
                try {
                    sessionCalendar.setTime(dt);
                    i = 13;
                    if (dt instanceof java.sql.Date) {
                        sessionCalendar.set(11, 0);
                        sessionCalendar.set(12, 0);
                        sessionCalendar.set(13, 0);
                    }
                    byte length = (byte) 7;
                    if (dt instanceof Timestamp) {
                        length = (byte) 11;
                    }
                    intoBuf.ensureCapacity(length);
                    intoBuf.writeByte(length);
                    int year = sessionCalendar.get(1);
                    int month = sessionCalendar.get(2) + 1;
                    int date = sessionCalendar.get(5);
                    intoBuf.writeInt(year);
                    intoBuf.writeByte((byte) month);
                    intoBuf.writeByte((byte) date);
                    if (dt instanceof java.sql.Date) {
                        intoBuf.writeByte((byte) 0);
                        intoBuf.writeByte((byte) 0);
                        intoBuf.writeByte((byte) 0);
                    } else {
                        intoBuf.writeByte((byte) sessionCalendar.get(11));
                        intoBuf.writeByte((byte) sessionCalendar.get(12));
                        i = (byte) sessionCalendar.get(13);
                        intoBuf.writeByte(i);
                    }
                    if (length == (byte) 11) {
                        intoBuf.writeLong((long) (((Timestamp) i).getNanos() / 1000));
                    }
                } catch (Throwable th) {
                    sessionCalendar = th;
                    throw sessionCalendar;
                } finally {
                    while (true) {
                        this = 
/*
Method generation error in method: com.mysql.jdbc.ServerPreparedStatement.storeDateTime413AndNewer(com.mysql.jdbc.Buffer, java.util.Date, int):void, dex: classes.dex
jadx.core.utils.exceptions.CodegenException: Error generate insn: ?: MERGE  (r12_3 'this' com.mysql.jdbc.ServerPreparedStatement) = (r12_2 'this' com.mysql.jdbc.ServerPreparedStatement), (r14_0 'dt' java.util.Date) in method: com.mysql.jdbc.ServerPreparedStatement.storeDateTime413AndNewer(com.mysql.jdbc.Buffer, java.util.Date, int):void, dex: classes.dex
	at jadx.core.codegen.InsnGen.makeInsn(InsnGen.java:226)
	at jadx.core.codegen.InsnGen.makeInsn(InsnGen.java:203)
	at jadx.core.codegen.RegionGen.makeSimpleBlock(RegionGen.java:100)
	at jadx.core.codegen.RegionGen.makeRegion(RegionGen.java:50)
	at jadx.core.codegen.RegionGen.makeSimpleRegion(RegionGen.java:87)
	at jadx.core.codegen.RegionGen.makeRegion(RegionGen.java:53)
	at jadx.core.codegen.RegionGen.makeRegionIndent(RegionGen.java:93)
	at jadx.core.codegen.RegionGen.makeLoop(RegionGen.java:174)
	at jadx.core.codegen.RegionGen.makeRegion(RegionGen.java:61)
	at jadx.core.codegen.RegionGen.makeSimpleRegion(RegionGen.java:87)
	at jadx.core.codegen.RegionGen.makeRegion(RegionGen.java:53)
	at jadx.core.codegen.RegionGen.makeRegionIndent(RegionGen.java:93)
	at jadx.core.codegen.RegionGen.makeTryCatch(RegionGen.java:299)
	at jadx.core.codegen.RegionGen.makeRegion(RegionGen.java:63)
	at jadx.core.codegen.RegionGen.makeSimpleRegion(RegionGen.java:87)
	at jadx.core.codegen.RegionGen.makeRegion(RegionGen.java:53)
	at jadx.core.codegen.RegionGen.makeRegionIndent(RegionGen.java:93)
	at jadx.core.codegen.RegionGen.makeTryCatch(RegionGen.java:279)
	at jadx.core.codegen.RegionGen.makeRegion(RegionGen.java:63)
	at jadx.core.codegen.RegionGen.makeSimpleRegion(RegionGen.java:87)
	at jadx.core.codegen.RegionGen.makeRegion(RegionGen.java:53)
	at jadx.core.codegen.RegionGen.makeSimpleRegion(RegionGen.java:87)
	at jadx.core.codegen.RegionGen.makeRegion(RegionGen.java:53)
	at jadx.core.codegen.RegionGen.makeRegionIndent(RegionGen.java:93)
	at jadx.core.codegen.RegionGen.makeSynchronizedRegion(RegionGen.java:229)
	at jadx.core.codegen.RegionGen.makeRegion(RegionGen.java:65)
	at jadx.core.codegen.RegionGen.makeSimpleRegion(RegionGen.java:87)
	at jadx.core.codegen.RegionGen.makeRegion(RegionGen.java:53)
	at jadx.core.codegen.MethodGen.addInstructions(MethodGen.java:187)
	at jadx.core.codegen.ClassGen.addMethod(ClassGen.java:320)
	at jadx.core.codegen.ClassGen.addMethods(ClassGen.java:257)
	at jadx.core.codegen.ClassGen.addClassBody(ClassGen.java:220)
	at jadx.core.codegen.ClassGen.addClassCode(ClassGen.java:110)
	at jadx.core.codegen.ClassGen.makeClass(ClassGen.java:75)
	at jadx.core.codegen.CodeGen.visit(CodeGen.java:12)
	at jadx.core.ProcessClass.process(ProcessClass.java:40)
	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:282)
	at jadx.api.JavaClass.decompile(JavaClass.java:62)
	at jadx.api.JadxDecompiler.lambda$appendSourcesSave$0(JadxDecompiler.java:200)
	at jadx.api.JadxDecompiler$$Lambda$8/1205406622.run(Unknown Source)
Caused by: jadx.core.utils.exceptions.CodegenException: MERGE can be used only in fallback mode
	at jadx.core.codegen.InsnGen.fallbackOnlyInsn(InsnGen.java:537)
	at jadx.core.codegen.InsnGen.makeInsnBody(InsnGen.java:509)
	at jadx.core.codegen.InsnGen.makeInsn(InsnGen.java:220)
	... 39 more

*/

                        private Calendar getServerTzCalendar() throws SQLException {
                            Calendar calendar;
                            synchronized (checkClosed().getConnectionMutex()) {
                                if (this.serverTzCalendar == null) {
                                    this.serverTzCalendar = new GregorianCalendar(this.connection.getServerTimezoneTZ());
                                }
                                calendar = this.serverTzCalendar;
                            }
                            return calendar;
                        }

                        private Calendar getDefaultTzCalendar() throws SQLException {
                            Calendar calendar;
                            synchronized (checkClosed().getConnectionMutex()) {
                                if (this.defaultTzCalendar == null) {
                                    this.defaultTzCalendar = new GregorianCalendar(TimeZone.getDefault());
                                }
                                calendar = this.defaultTzCalendar;
                            }
                            return calendar;
                        }

                        private void storeReader(MysqlIO mysql, int parameterIndex, Buffer packet, Reader inStream) throws SQLException {
                            Throwable th;
                            MysqlIO mysqlIO;
                            ServerPreparedStatement serverPreparedStatement;
                            Throwable th2;
                            int totalBytesRead;
                            int i;
                            IOException iOException;
                            int i2;
                            StringBuilder stringBuilder;
                            SQLException sqlEx;
                            MysqlIO mysql2;
                            Reader inStream2;
                            Reader inStream3;
                            int parameterIndex2;
                            ServerPreparedStatement serverPreparedStatement2 = this;
                            int parameterIndex3 = parameterIndex;
                            Buffer packet2 = packet;
                            synchronized (checkClosed().getConnectionMutex()) {
                                int maxBytesChar;
                                int bytesReadAtLastSend;
                                try {
                                    String encoding;
                                    Reader reader;
                                    String forcedEncoding = serverPreparedStatement2.connection.getClobCharacterEncoding();
                                    if (forcedEncoding == null) {
                                        try {
                                            encoding = serverPreparedStatement2.connection.getEncoding();
                                        } catch (Throwable th3) {
                                            th = th3;
                                            mysqlIO = mysql;
                                            reader = inStream;
                                            serverPreparedStatement = serverPreparedStatement2;
                                            th2 = th;
                                            try {
                                            } catch (Throwable th4) {
                                                th = th4;
                                                th2 = th;
                                                throw th2;
                                            }
                                            throw th2;
                                        }
                                    }
                                    encoding = forcedEncoding;
                                    String clobEncoding = encoding;
                                    maxBytesChar = 2;
                                    if (clobEncoding != null) {
                                        if (clobEncoding.equals("UTF-16")) {
                                            maxBytesChar = 4;
                                        } else {
                                            maxBytesChar = serverPreparedStatement2.connection.getMaxBytesPerChar(clobEncoding);
                                            if (maxBytesChar == 1) {
                                                maxBytesChar = 2;
                                            }
                                        }
                                    }
                                    int maxBytesChar2 = maxBytesChar;
                                    char[] buf = new char[(8192 / maxBytesChar2)];
                                    int packetIsFullAt = serverPreparedStatement2.connection.getBlobSendChunkSize();
                                    int numRead;
                                    int packetIsFullAt2;
                                    char[] buf2;
                                    String clobEncoding2;
                                    try {
                                        boolean readAny;
                                        int totalBytesRead2;
                                        packet.clear();
                                        packet2.writeByte((byte) 24);
                                        packet2.writeLong(serverPreparedStatement2.serverStatementId);
                                        packet2.writeInt(parameterIndex3);
                                        numRead = 0;
                                        int bytesInPacket = 0;
                                        boolean readAny2 = false;
                                        totalBytesRead = 0;
                                        bytesReadAtLastSend = 0;
                                        while (true) {
                                            readAny = readAny2;
                                            try {
                                                maxBytesChar = inStream.read(buf);
                                                numRead = maxBytesChar;
                                                if (maxBytesChar == -1) {
                                                    break;
                                                }
                                                try {
                                                    readAny = StringUtils.getBytes(buf, null, clobEncoding, serverPreparedStatement2.connection.getServerCharset(), 0, numRead, serverPreparedStatement2.connection.parserKnowsUnicode(), getExceptionInterceptor());
                                                    packet2.writeBytesNoNull(readAny, 0, readAny.length);
                                                    int bytesInPacket2 = bytesInPacket + readAny.length;
                                                    try {
                                                        byte b;
                                                        totalBytesRead2 = readAny.length + totalBytesRead;
                                                        if (bytesInPacket2 >= packetIsFullAt) {
                                                            i = totalBytesRead2;
                                                            bytesInPacket = 0;
                                                            packetIsFullAt2 = packetIsFullAt;
                                                            buf2 = buf;
                                                            clobEncoding2 = clobEncoding;
                                                            try {
                                                                mysql.sendCommand(24, 0, packet2, true, null, null);
                                                                try {
                                                                    packet.clear();
                                                                    b = (byte) 24;
                                                                    packet2.writeByte((byte) 24);
                                                                    packet2.writeLong(serverPreparedStatement2.serverStatementId);
                                                                    packet2.writeInt(parameterIndex3);
                                                                    bytesInPacket2 = 0;
                                                                    bytesReadAtLastSend = i;
                                                                } catch (IOException e) {
                                                                    iOException = e;
                                                                } catch (Throwable th5) {
                                                                    maxBytesChar = th5;
                                                                }
                                                            } catch (IOException e2) {
                                                                iOException = e2;
                                                                i2 = bytesInPacket2;
                                                            } catch (Throwable th52) {
                                                                maxBytesChar = th52;
                                                                i2 = bytesInPacket2;
                                                            }
                                                        } else {
                                                            bytesInPacket = 0;
                                                            packetIsFullAt2 = packetIsFullAt;
                                                            buf2 = buf;
                                                            clobEncoding2 = clobEncoding;
                                                            b = (byte) 24;
                                                        }
                                                        bytesInPacket = bytesInPacket2;
                                                        totalBytesRead = totalBytesRead2;
                                                        buf = buf2;
                                                        clobEncoding = clobEncoding2;
                                                        packetIsFullAt = packetIsFullAt2;
                                                        byte totalBytesRead3 = b;
                                                        readAny2 = true;
                                                    } catch (IOException e22) {
                                                        packetIsFullAt2 = packetIsFullAt;
                                                        buf2 = buf;
                                                        clobEncoding2 = clobEncoding;
                                                        iOException = e22;
                                                        i2 = bytesInPacket2;
                                                    } catch (Throwable th522) {
                                                        packetIsFullAt2 = packetIsFullAt;
                                                        buf2 = buf;
                                                        clobEncoding2 = clobEncoding;
                                                        maxBytesChar = th522;
                                                        i2 = bytesInPacket2;
                                                    }
                                                } catch (IOException e222) {
                                                    packetIsFullAt2 = packetIsFullAt;
                                                    buf2 = buf;
                                                    clobEncoding2 = clobEncoding;
                                                    iOException = e222;
                                                    i2 = bytesInPacket;
                                                } catch (Throwable th5222) {
                                                    packetIsFullAt2 = packetIsFullAt;
                                                    buf2 = buf;
                                                    clobEncoding2 = clobEncoding;
                                                    maxBytesChar = th5222;
                                                    i2 = bytesInPacket;
                                                }
                                            } catch (IOException e2222) {
                                                i = totalBytesRead;
                                                packetIsFullAt2 = packetIsFullAt;
                                                buf2 = buf;
                                                clobEncoding2 = clobEncoding;
                                                iOException = e2222;
                                                bytesReadAtLastSend = i;
                                            } catch (Throwable th52222) {
                                                i = totalBytesRead;
                                                packetIsFullAt2 = packetIsFullAt;
                                                buf2 = buf;
                                                clobEncoding2 = clobEncoding;
                                                maxBytesChar = th52222;
                                                totalBytesRead = bytesReadAtLastSend;
                                                bytesReadAtLastSend = i;
                                                i2 = bytesInPacket;
                                            }
                                        }
                                        packetIsFullAt2 = packetIsFullAt;
                                        buf2 = buf;
                                        clobEncoding2 = clobEncoding;
                                        if (totalBytesRead != bytesReadAtLastSend) {
                                            totalBytesRead2 = bytesReadAtLastSend;
                                            i = totalBytesRead;
                                            try {
                                                mysql.sendCommand(24, null, packet2, true, null, 0);
                                            } catch (IOException e22222) {
                                                iOException = e22222;
                                                totalBytesRead = totalBytesRead2;
                                                bytesReadAtLastSend = i;
                                                i2 = bytesInPacket;
                                                try {
                                                    stringBuilder = new StringBuilder();
                                                    stringBuilder.append(Messages.getString("ServerPreparedStatement.24"));
                                                    stringBuilder.append(iOException.toString());
                                                    sqlEx = SQLError.createSQLException(stringBuilder.toString(), SQLError.SQL_STATE_GENERAL_ERROR, getExceptionInterceptor());
                                                    sqlEx.initCause(iOException);
                                                    throw sqlEx;
                                                } catch (Throwable th522222) {
                                                    maxBytesChar = th522222;
                                                    mysql2 = mysql;
                                                    inStream2 = inStream;
                                                    try {
                                                        if (serverPreparedStatement2.connection.getAutoClosePStmtStreams()) {
                                                            inStream3 = inStream2;
                                                        } else {
                                                            inStream3 = inStream2;
                                                            if (inStream3 != null) {
                                                                try {
                                                                    inStream3.close();
                                                                } catch (IOException e3) {
                                                                }
                                                            }
                                                        }
                                                        try {
                                                            throw maxBytesChar;
                                                        } catch (Throwable th6) {
                                                            th522222 = th6;
                                                            serverPreparedStatement = this;
                                                            mysqlIO = mysql2;
                                                            reader = inStream3;
                                                        }
                                                    } catch (Throwable th5222222) {
                                                        serverPreparedStatement = this;
                                                        mysqlIO = mysql2;
                                                        reader = inStream2;
                                                        th2 = th5222222;
                                                        throw th2;
                                                    }
                                                }
                                            } catch (Throwable th52222222) {
                                                maxBytesChar = th52222222;
                                                totalBytesRead = totalBytesRead2;
                                                bytesReadAtLastSend = i;
                                                i2 = bytesInPacket;
                                                mysql2 = mysql;
                                                inStream2 = inStream;
                                                if (serverPreparedStatement2.connection.getAutoClosePStmtStreams()) {
                                                    inStream3 = inStream2;
                                                    if (inStream3 != null) {
                                                        inStream3.close();
                                                    }
                                                } else {
                                                    inStream3 = inStream2;
                                                }
                                                throw maxBytesChar;
                                            }
                                        } else {
                                            totalBytesRead2 = bytesReadAtLastSend;
                                            i = totalBytesRead;
                                        }
                                        if (!readAny) {
                                            mysql.sendCommand(24, null, packet2, true, null, 0);
                                        }
                                        bytesReadAtLastSend = maxBytesChar2;
                                        totalBytesRead = buf2;
                                        packetIsFullAt = bytesInPacket;
                                        buf = i;
                                        clobEncoding = totalBytesRead2;
                                        forcedEncoding = packetIsFullAt2;
                                        reader = inStream;
                                        parameterIndex2 = parameterIndex3;
                                    } catch (IOException e222222) {
                                        packetIsFullAt2 = packetIsFullAt;
                                        buf2 = buf;
                                        clobEncoding2 = clobEncoding;
                                        numRead = 0;
                                        iOException = e222222;
                                        stringBuilder = new StringBuilder();
                                        stringBuilder.append(Messages.getString("ServerPreparedStatement.24"));
                                        stringBuilder.append(iOException.toString());
                                        sqlEx = SQLError.createSQLException(stringBuilder.toString(), SQLError.SQL_STATE_GENERAL_ERROR, getExceptionInterceptor());
                                        sqlEx.initCause(iOException);
                                        throw sqlEx;
                                    } catch (Throwable th522222222) {
                                        packetIsFullAt2 = packetIsFullAt;
                                        buf2 = buf;
                                        clobEncoding2 = clobEncoding;
                                        numRead = 0;
                                        maxBytesChar = th522222222;
                                        mysql2 = mysql;
                                        inStream2 = inStream;
                                        if (serverPreparedStatement2.connection.getAutoClosePStmtStreams()) {
                                            inStream3 = inStream2;
                                            if (inStream3 != null) {
                                                inStream3.close();
                                            }
                                        } else {
                                            inStream3 = inStream2;
                                        }
                                        throw maxBytesChar;
                                    }
                                    try {
                                        if (serverPreparedStatement2.connection.getAutoClosePStmtStreams() && reader != null) {
                                            try {
                                                reader.close();
                                            } catch (IOException e4) {
                                            }
                                        }
                                        return;
                                    } catch (Throwable th5222222222) {
                                        th2 = th5222222222;
                                        parameterIndex3 = parameterIndex2;
                                        throw th2;
                                    }
                                } catch (Throwable th52222222222) {
                                    th2 = th52222222222;
                                    throw th2;
                                }
                            }
                            totalBytesRead = i;
                            mysql2 = mysql;
                            inStream2 = inStream;
                            if (serverPreparedStatement2.connection.getAutoClosePStmtStreams()) {
                                inStream3 = inStream2;
                            } else {
                                inStream3 = inStream2;
                                if (inStream3 != null) {
                                    inStream3.close();
                                }
                            }
                            throw maxBytesChar;
                            int i3 = totalBytesRead;
                            bytesReadAtLastSend = i3;
                            mysql2 = mysql;
                            inStream2 = inStream;
                            if (serverPreparedStatement2.connection.getAutoClosePStmtStreams()) {
                                inStream3 = inStream2;
                                if (inStream3 != null) {
                                    inStream3.close();
                                }
                            } else {
                                inStream3 = inStream2;
                            }
                            throw maxBytesChar;
                            i3 = totalBytesRead;
                            totalBytesRead = bytesReadAtLastSend;
                            bytesReadAtLastSend = i3;
                            stringBuilder = new StringBuilder();
                            stringBuilder.append(Messages.getString("ServerPreparedStatement.24"));
                            stringBuilder.append(iOException.toString());
                            sqlEx = SQLError.createSQLException(stringBuilder.toString(), SQLError.SQL_STATE_GENERAL_ERROR, getExceptionInterceptor());
                            sqlEx.initCause(iOException);
                            throw sqlEx;
                            totalBytesRead = i;
                            stringBuilder = new StringBuilder();
                            stringBuilder.append(Messages.getString("ServerPreparedStatement.24"));
                            stringBuilder.append(iOException.toString());
                            sqlEx = SQLError.createSQLException(stringBuilder.toString(), SQLError.SQL_STATE_GENERAL_ERROR, getExceptionInterceptor());
                            sqlEx.initCause(iOException);
                            throw sqlEx;
                        }

                        private void storeStream(MysqlIO mysql, int parameterIndex, Buffer packet, InputStream inStream) throws SQLException {
                            IOException e;
                            IOException ioEx;
                            Throwable th;
                            Throwable th2;
                            InputStream inputStream;
                            Throwable th3;
                            MysqlIO mysql2;
                            Buffer packet2;
                            ServerPreparedStatement serverPreparedStatement;
                            ServerPreparedStatement serverPreparedStatement2 = this;
                            int i = parameterIndex;
                            Buffer buffer = packet;
                            synchronized (checkClosed().getConnectionMutex()) {
                                int numRead;
                                Buffer packet3;
                                try {
                                    byte[] buf = new byte[8192];
                                    numRead = 0;
                                    int i2;
                                    try {
                                        boolean readAny;
                                        int bytesReadAtLastSend;
                                        int packetIsFullAt = serverPreparedStatement2.connection.getBlobSendChunkSize();
                                        packet.clear();
                                        buffer.writeByte((byte) 24);
                                        buffer.writeLong(serverPreparedStatement2.serverStatementId);
                                        buffer.writeInt(i);
                                        int bytesInPacket = 0;
                                        int totalBytesRead = 0;
                                        int bytesReadAtLastSend2 = 0;
                                        int numRead2 = numRead;
                                        boolean readAny2 = false;
                                        while (true) {
                                            readAny = readAny2;
                                            try {
                                                numRead = inStream.read(buf);
                                                int numRead3 = numRead;
                                                if (numRead == -1) {
                                                    break;
                                                }
                                                try {
                                                    buffer.writeBytesNoNull(buf, 0, numRead3);
                                                    int bytesInPacket2 = bytesInPacket + numRead3;
                                                    bytesInPacket = totalBytesRead + numRead3;
                                                    if (bytesInPacket2 >= packetIsFullAt) {
                                                        bytesReadAtLastSend = bytesInPacket;
                                                        i2 = numRead3;
                                                        try {
                                                            mysql.sendCommand(24, null, buffer, true, null, 0);
                                                            packet.clear();
                                                            buffer.writeByte((byte) 24);
                                                            buffer.writeLong(serverPreparedStatement2.serverStatementId);
                                                            buffer.writeInt(i);
                                                            totalBytesRead = bytesInPacket;
                                                            bytesReadAtLastSend2 = bytesReadAtLastSend;
                                                            numRead2 = i2;
                                                            bytesInPacket = 0;
                                                            readAny2 = true;
                                                        } catch (IOException e2) {
                                                            e = e2;
                                                        }
                                                    } else {
                                                        i2 = numRead3;
                                                        totalBytesRead = bytesInPacket;
                                                        readAny2 = true;
                                                        bytesInPacket = bytesInPacket2;
                                                        numRead2 = i2;
                                                    }
                                                } catch (IOException e3) {
                                                    i2 = numRead3;
                                                    ioEx = e3;
                                                } catch (Throwable th22) {
                                                    i2 = numRead3;
                                                    th = th22;
                                                }
                                            } catch (IOException e32) {
                                                ioEx = e32;
                                                i2 = numRead2;
                                            } catch (Throwable th222) {
                                                th = th222;
                                                i2 = numRead2;
                                            }
                                        }
                                        if (totalBytesRead != bytesReadAtLastSend2) {
                                            bytesReadAtLastSend = bytesReadAtLastSend2;
                                            mysql.sendCommand(24, null, buffer, true, null, 0);
                                        } else {
                                            bytesReadAtLastSend = bytesReadAtLastSend2;
                                            int i3 = totalBytesRead;
                                        }
                                        if (!readAny) {
                                            mysql.sendCommand(24, null, buffer, true, null, 0);
                                        }
                                        packet3 = buffer;
                                        InputStream inStream2 = inStream;
                                        try {
                                            if (serverPreparedStatement2.connection.getAutoClosePStmtStreams() && inStream2 != null) {
                                                try {
                                                    inStream2.close();
                                                } catch (IOException e4) {
                                                }
                                            }
                                        } catch (Throwable th4) {
                                            th222 = th4;
                                            numRead = i;
                                            inputStream = inStream2;
                                            while (true) {
                                                th3 = th222;
                                                try {
                                                    break;
                                                } catch (Throwable th5) {
                                                    th222 = th5;
                                                }
                                            }
                                            throw th3;
                                        }
                                    } catch (IOException e5) {
                                        e32 = e5;
                                        i2 = numRead;
                                        ioEx = e32;
                                        try {
                                            StringBuilder stringBuilder = new StringBuilder();
                                            stringBuilder.append(Messages.getString("ServerPreparedStatement.25"));
                                            stringBuilder.append(ioEx.toString());
                                            SQLException sqlEx = SQLError.createSQLException(stringBuilder.toString(), SQLError.SQL_STATE_GENERAL_ERROR, getExceptionInterceptor());
                                            sqlEx.initCause(ioEx);
                                            throw sqlEx;
                                        } catch (Throwable th6) {
                                            th222 = th6;
                                            th = th222;
                                            mysql2 = mysql;
                                            packet2 = buffer;
                                            inputStream = inStream;
                                            try {
                                                if (serverPreparedStatement2.connection.getAutoClosePStmtStreams() && inputStream != null) {
                                                    try {
                                                        inputStream.close();
                                                    } catch (IOException e6) {
                                                    }
                                                }
                                                throw th;
                                            } catch (Throwable th7) {
                                                th222 = th7;
                                                numRead = i;
                                                serverPreparedStatement = this;
                                                MysqlIO mysqlIO = mysql2;
                                                packet3 = packet2;
                                            }
                                        }
                                    } catch (Throwable th8) {
                                        th222 = th8;
                                        i2 = numRead;
                                        th = th222;
                                        mysql2 = mysql;
                                        packet2 = buffer;
                                        inputStream = inStream;
                                        inputStream.close();
                                        throw th;
                                    }
                                } catch (Throwable th9) {
                                    th222 = th9;
                                    inputStream = inStream;
                                    serverPreparedStatement = serverPreparedStatement2;
                                    numRead = i;
                                    packet3 = buffer;
                                    while (true) {
                                        th3 = th222;
                                        break;
                                    }
                                    throw th3;
                                }
                            }
                        }

                        public String toString() {
                            StringBuilder toStringBuf = new StringBuilder();
                            toStringBuf.append("com.mysql.jdbc.ServerPreparedStatement[");
                            toStringBuf.append(this.serverStatementId);
                            toStringBuf.append("] - ");
                            try {
                                toStringBuf.append(asSql());
                            } catch (SQLException sqlEx) {
                                toStringBuf.append(Messages.getString("ServerPreparedStatement.6"));
                                toStringBuf.append(sqlEx);
                            }
                            return toStringBuf.toString();
                        }

                        protected long getServerStatementId() {
                            return this.serverStatementId;
                        }

                        public boolean canRewriteAsMultiValueInsertAtSqlLevel() throws SQLException {
                            boolean z;
                            synchronized (checkClosed().getConnectionMutex()) {
                                if (!this.hasCheckedRewrite) {
                                    this.hasCheckedRewrite = true;
                                    this.canRewrite = PreparedStatement.canRewrite(this.originalSql, isOnDuplicateKeyUpdate(), getLocationOfOnDuplicateKeyUpdate(), 0);
                                    this.parseInfo = new ParseInfo(this.originalSql, this.connection, this.connection.getMetaData(), this.charEncoding, this.charConverter);
                                }
                                z = this.canRewrite;
                            }
                            return z;
                        }

                        public boolean canRewriteAsMultivalueInsertStatement() throws SQLException {
                            synchronized (checkClosed().getConnectionMutex()) {
                                if (canRewriteAsMultiValueInsertAtSqlLevel()) {
                                    int nbrCommands = this.batchedArgs.size();
                                    BindValue[] currentBindValues = null;
                                    for (int commandIndex = 0; commandIndex < nbrCommands; commandIndex++) {
                                        Object arg = this.batchedArgs.get(commandIndex);
                                        if (!(arg instanceof String)) {
                                            currentBindValues = ((BatchedBindValues) arg).batchedParameterValues;
                                            if (null != null) {
                                                for (int j = 0; j < this.parameterBindings.length; j++) {
                                                    if (currentBindValues[j].bufferType != null[j].bufferType) {
                                                        return false;
                                                    }
                                                }
                                                continue;
                                            } else {
                                                continue;
                                            }
                                        }
                                    }
                                    return true;
                                }
                                return false;
                            }
                        }

                        protected int getLocationOfOnDuplicateKeyUpdate() throws SQLException {
                            int i;
                            synchronized (checkClosed().getConnectionMutex()) {
                                if (this.locationOfOnDuplicateKeyUpdate == -2) {
                                    this.locationOfOnDuplicateKeyUpdate = StatementImpl.getOnDuplicateKeyLocation(this.originalSql, this.connection.getDontCheckOnDuplicateKeyUpdateInSQL(), this.connection.getRewriteBatchedStatements(), this.connection.isNoBackslashEscapesSet());
                                }
                                i = this.locationOfOnDuplicateKeyUpdate;
                            }
                            return i;
                        }

                        protected boolean isOnDuplicateKeyUpdate() throws SQLException {
                            boolean z;
                            synchronized (checkClosed().getConnectionMutex()) {
                                z = getLocationOfOnDuplicateKeyUpdate() != -1;
                            }
                            return z;
                        }

                        protected long[] computeMaxParameterSetSizeAndBatchSize(int numBatchedArgs) throws SQLException {
                            long[] jArr;
                            ServerPreparedStatement serverPreparedStatement = this;
                            synchronized (checkClosed().getConnectionMutex()) {
                                long maxSizeOfParameterSet = 0;
                                long sizeOfEntireBatch = 10;
                                int i = 0;
                                while (i < numBatchedArgs) {
                                    try {
                                        long size;
                                        BindValue[] paramArg = ((BatchedBindValues) serverPreparedStatement.batchedArgs.get(i)).batchedParameterValues;
                                        long sizeOfParameterSet = (0 + ((long) ((serverPreparedStatement.parameterCount + 7) / 8))) + ((long) (serverPreparedStatement.parameterCount * 2));
                                        for (int j = 0; j < serverPreparedStatement.parameterBindings.length; j++) {
                                            if (!paramArg[j].isNull) {
                                                long j2;
                                                size = paramArg[j].getBoundLength();
                                                if (!paramArg[j].isLongData) {
                                                    j2 = sizeOfParameterSet + size;
                                                } else if (size != -1) {
                                                    j2 = sizeOfParameterSet + size;
                                                }
                                                sizeOfParameterSet = j2;
                                            }
                                        }
                                        size = sizeOfEntireBatch + sizeOfParameterSet;
                                        if (sizeOfParameterSet > maxSizeOfParameterSet) {
                                            maxSizeOfParameterSet = sizeOfParameterSet;
                                        }
                                        i++;
                                        sizeOfEntireBatch = size;
                                    } catch (Throwable th) {
                                        Throwable th2 = th;
                                    }
                                }
                                jArr = new long[]{maxSizeOfParameterSet, sizeOfEntireBatch};
                            }
                            return jArr;
                        }

                        /* JADX WARNING: inconsistent code. */
                        /* Code decompiled incorrectly, please refer to instructions dump. */
                        protected int setOneBatchedParameterSet(java.sql.PreparedStatement r9, int r10, java.lang.Object r11) throws java.sql.SQLException {
                            /*
                            r8 = this;
                            r0 = r11;
                            r0 = (com.mysql.jdbc.ServerPreparedStatement.BatchedBindValues) r0;
                            r0 = r0.batchedParameterValues;
                            r1 = 0;
                            r2 = 0;
                            r3 = r2;
                            r2 = r10;
                            r10 = r1;
                        L_0x000a:
                            r4 = r0.length;
                            if (r10 >= r4) goto L_0x010d;
                        L_0x000d:
                            r4 = r0[r10];
                            r4 = r4.isNull;
                            if (r4 == 0) goto L_0x001b;
                        L_0x0013:
                            r4 = r2 + 1;
                            r9.setNull(r2, r1);
                        L_0x0018:
                            r2 = r4;
                            goto L_0x0109;
                        L_0x001b:
                            r4 = r0[r10];
                            r4 = r4.isLongData;
                            if (r4 == 0) goto L_0x0046;
                        L_0x0021:
                            r3 = r0[r10];
                            r3 = r3.value;
                            r4 = r3 instanceof java.io.InputStream;
                            if (r4 == 0) goto L_0x0037;
                        L_0x0029:
                            r4 = r2 + 1;
                            r5 = r3;
                            r5 = (java.io.InputStream) r5;
                            r6 = r0[r10];
                            r6 = r6.bindLength;
                            r6 = (int) r6;
                            r9.setBinaryStream(r2, r5, r6);
                        L_0x0036:
                            goto L_0x0045;
                        L_0x0037:
                            r4 = r2 + 1;
                            r5 = r3;
                            r5 = (java.io.Reader) r5;
                            r6 = r0[r10];
                            r6 = r6.bindLength;
                            r6 = (int) r6;
                            r9.setCharacterStream(r2, r5, r6);
                            goto L_0x0036;
                        L_0x0045:
                            goto L_0x0018;
                        L_0x0046:
                            r4 = r0[r10];
                            r4 = r4.bufferType;
                            r5 = 15;
                            if (r4 == r5) goto L_0x00e0;
                        L_0x004e:
                            r5 = 246; // 0xf6 float:3.45E-43 double:1.215E-321;
                            if (r4 == r5) goto L_0x00e0;
                        L_0x0052:
                            switch(r4) {
                                case 0: goto L_0x00e0;
                                case 1: goto L_0x00d3;
                                case 2: goto L_0x00c6;
                                case 3: goto L_0x00ba;
                                case 4: goto L_0x00af;
                                case 5: goto L_0x00a4;
                                default: goto L_0x0055;
                            };
                        L_0x0055:
                            switch(r4) {
                                case 7: goto L_0x0097;
                                case 8: goto L_0x008d;
                                default: goto L_0x0058;
                            };
                        L_0x0058:
                            switch(r4) {
                                case 10: goto L_0x0081;
                                case 11: goto L_0x0075;
                                case 12: goto L_0x0097;
                                default: goto L_0x005b;
                            };
                        L_0x005b:
                            switch(r4) {
                                case 253: goto L_0x00e0;
                                case 254: goto L_0x00e0;
                                default: goto L_0x005e;
                            };
                        L_0x005e:
                            r1 = new java.lang.IllegalArgumentException;
                            r4 = new java.lang.StringBuilder;
                            r4.<init>();
                            r5 = "Unknown type when re-binding parameter into batched statement for parameter index ";
                            r4.append(r5);
                            r4.append(r2);
                            r4 = r4.toString();
                            r1.<init>(r4);
                            throw r1;
                        L_0x0075:
                            r4 = r2 + 1;
                            r5 = r0[r10];
                            r5 = r5.value;
                            r5 = (java.sql.Time) r5;
                            r9.setTime(r2, r5);
                            goto L_0x0018;
                        L_0x0081:
                            r4 = r2 + 1;
                            r5 = r0[r10];
                            r5 = r5.value;
                            r5 = (java.sql.Date) r5;
                            r9.setDate(r2, r5);
                            goto L_0x0018;
                        L_0x008d:
                            r4 = r2 + 1;
                            r5 = r0[r10];
                            r5 = r5.longBinding;
                            r9.setLong(r2, r5);
                            goto L_0x0018;
                        L_0x0097:
                            r4 = r2 + 1;
                            r5 = r0[r10];
                            r5 = r5.value;
                            r5 = (java.sql.Timestamp) r5;
                            r9.setTimestamp(r2, r5);
                            goto L_0x0018;
                        L_0x00a4:
                            r4 = r2 + 1;
                            r5 = r0[r10];
                            r5 = r5.doubleBinding;
                            r9.setDouble(r2, r5);
                            goto L_0x0018;
                        L_0x00af:
                            r4 = r2 + 1;
                            r5 = r0[r10];
                            r5 = r5.floatBinding;
                            r9.setFloat(r2, r5);
                            goto L_0x0018;
                        L_0x00ba:
                            r4 = r2 + 1;
                            r5 = r0[r10];
                            r5 = r5.longBinding;
                            r5 = (int) r5;
                            r9.setInt(r2, r5);
                            goto L_0x0018;
                        L_0x00c6:
                            r4 = r2 + 1;
                            r5 = r0[r10];
                            r5 = r5.longBinding;
                            r5 = (int) r5;
                            r5 = (short) r5;
                            r9.setShort(r2, r5);
                            goto L_0x0018;
                        L_0x00d3:
                            r4 = r2 + 1;
                            r5 = r0[r10];
                            r5 = r5.longBinding;
                            r5 = (int) r5;
                            r5 = (byte) r5;
                            r9.setByte(r2, r5);
                            goto L_0x0018;
                        L_0x00e0:
                            r3 = r0[r10];
                            r3 = r3.value;
                            r4 = r3 instanceof byte[];
                            if (r4 == 0) goto L_0x00ef;
                        L_0x00e8:
                            r4 = r3;
                            r4 = (byte[]) r4;
                            r9.setBytes(r2, r4);
                            goto L_0x00f5;
                        L_0x00ef:
                            r4 = r3;
                            r4 = (java.lang.String) r4;
                            r9.setString(r2, r4);
                        L_0x00f5:
                            r4 = r9 instanceof com.mysql.jdbc.ServerPreparedStatement;
                            if (r4 == 0) goto L_0x0106;
                        L_0x00f9:
                            r4 = r9;
                            r4 = (com.mysql.jdbc.ServerPreparedStatement) r4;
                            r4 = r4.getBinding(r2, r1);
                            r5 = r0[r10];
                            r5 = r5.bufferType;
                            r4.bufferType = r5;
                        L_0x0106:
                            r2 = r2 + 1;
                        L_0x0109:
                            r10 = r10 + 1;
                            goto L_0x000a;
                        L_0x010d:
                            return r2;
                            */
                            throw new UnsupportedOperationException("Method not decompiled: com.mysql.jdbc.ServerPreparedStatement.setOneBatchedParameterSet(java.sql.PreparedStatement, int, java.lang.Object):int");
                        }

                        protected boolean containsOnDuplicateKeyUpdateInSQL() {
                            return this.hasOnDuplicateKeyUpdate;
                        }

                        protected PreparedStatement prepareBatchedInsertSQL(MySQLConnection localConn, int numBatches) throws SQLException {
                            PreparedStatement pstmt;
                            synchronized (checkClosed().getConnectionMutex()) {
                                try {
                                    pstmt = (PreparedStatement) ((Wrapper) localConn.prepareStatement(this.parseInfo.getSqlForBatch(numBatches), this.resultSetType, this.resultSetConcurrency)).unwrap(PreparedStatement.class);
                                    pstmt.setRetrieveGeneratedKeys(this.retrieveGeneratedKeys);
                                } catch (UnsupportedEncodingException e) {
                                    SQLException sqlEx = SQLError.createSQLException("Unable to prepare batch statement", SQLError.SQL_STATE_GENERAL_ERROR, getExceptionInterceptor());
                                    sqlEx.initCause(e);
                                    throw sqlEx;
                                }
                            }
                            return pstmt;
                        }

                        public void setPoolable(boolean poolable) throws SQLException {
                            if (!poolable) {
                                this.connection.decachePreparedStatement(this);
                            }
                            super.setPoolable(poolable);
                        }
                    }
