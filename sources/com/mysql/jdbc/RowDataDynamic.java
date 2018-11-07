package com.mysql.jdbc;

import java.sql.SQLException;

public class RowDataDynamic implements RowData {
    private int columnCount;
    private ExceptionInterceptor exceptionInterceptor;
    private int index = -1;
    private MysqlIO io;
    private boolean isAfterEnd = false;
    private boolean isBinaryEncoded = false;
    private Field[] metadata;
    private boolean moreResultsExisted;
    private ResultSetRow nextRow;
    private boolean noMoreRows = false;
    private ResultSetImpl owner;
    private boolean streamerClosed = false;
    private boolean useBufferRowExplicit;
    private boolean wasEmpty = false;

    public RowDataDynamic(MysqlIO io, int colCount, Field[] fields, boolean isBinaryEncoded) throws SQLException {
        this.io = io;
        this.columnCount = colCount;
        this.isBinaryEncoded = isBinaryEncoded;
        this.metadata = fields;
        this.exceptionInterceptor = this.io.getExceptionInterceptor();
        this.useBufferRowExplicit = MysqlIO.useBufferRowExplicit(this.metadata);
    }

    public void addRow(ResultSetRow row) throws SQLException {
        notSupported();
    }

    public void afterLast() throws SQLException {
        notSupported();
    }

    public void beforeFirst() throws SQLException {
        notSupported();
    }

    public void beforeLast() throws SQLException {
        notSupported();
    }

    /* JADX WARNING: inconsistent code. */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    public void close() throws java.sql.SQLException {
        /*
        r27 = this;
        r1 = r27;
        r2 = r1;
        r3 = 0;
        r4 = r1.owner;
        if (r4 == 0) goto L_0x0012;
    L_0x0008:
        r4 = r1.owner;
        r3 = r4.connection;
        if (r3 == 0) goto L_0x0012;
    L_0x000e:
        r2 = r3.getConnectionMutex();
    L_0x0012:
        r4 = 0;
        r5 = 0;
        monitor-enter(r2);
    L_0x0015:
        r6 = r27.next();	 Catch:{ all -> 0x011b }
        if (r6 == 0) goto L_0x0026;
    L_0x001b:
        r4 = 1;
        r5 = r5 + 1;
        r6 = r5 % 100;
        if (r6 != 0) goto L_0x0015;
    L_0x0022:
        java.lang.Thread.yield();	 Catch:{ all -> 0x011b }
        goto L_0x0015;
    L_0x0026:
        r6 = 0;
        if (r3 == 0) goto L_0x010e;
    L_0x0029:
        r7 = r3.getClobberStreamingResults();	 Catch:{ all -> 0x011b }
        if (r7 != 0) goto L_0x007e;
    L_0x002f:
        r7 = r3.getNetTimeoutForStreamingResults();	 Catch:{ all -> 0x011b }
        if (r7 <= 0) goto L_0x007e;
    L_0x0035:
        r7 = "net_write_timeout";
        r7 = r3.getServerVariable(r7);	 Catch:{ all -> 0x011b }
        if (r7 == 0) goto L_0x0043;
    L_0x003d:
        r8 = r7.length();	 Catch:{ all -> 0x011b }
        if (r8 != 0) goto L_0x0046;
    L_0x0043:
        r8 = "60";
        r7 = r8;
    L_0x0046:
        r8 = r1.io;	 Catch:{ all -> 0x011b }
        r8.clearInputStream();	 Catch:{ all -> 0x011b }
        r8 = r6;
        r9 = r3.createStatement();	 Catch:{ all -> 0x0070 }
        r8 = r9;
        r9 = r8;
        r9 = (com.mysql.jdbc.StatementImpl) r9;	 Catch:{ all -> 0x0070 }
        r10 = new java.lang.StringBuilder;	 Catch:{ all -> 0x0070 }
        r10.<init>();	 Catch:{ all -> 0x0070 }
        r11 = "SET net_write_timeout=";
        r10.append(r11);	 Catch:{ all -> 0x0070 }
        r10.append(r7);	 Catch:{ all -> 0x0070 }
        r10 = r10.toString();	 Catch:{ all -> 0x0070 }
        r9.executeSimpleNonQuery(r3, r10);	 Catch:{ all -> 0x0070 }
        r9 = r2;
        if (r8 == 0) goto L_0x006f;
    L_0x006c:
        r8.close();	 Catch:{ all -> 0x0116 }
    L_0x006f:
        goto L_0x007f;
    L_0x0070:
        r0 = move-exception;
        r6 = r0;
        r9 = r1;
        r10 = r2;
        if (r8 == 0) goto L_0x007d;
    L_0x0076:
        r8.close();	 Catch:{ all -> 0x007a }
        goto L_0x007d;
    L_0x007a:
        r0 = move-exception;
        goto L_0x011e;
    L_0x007d:
        throw r6;	 Catch:{ all -> 0x007a }
    L_0x007e:
        r9 = r2;
    L_0x007f:
        r7 = r3.getUseUsageAdvisor();	 Catch:{ all -> 0x0116 }
        if (r7 == 0) goto L_0x010f;
    L_0x0085:
        if (r4 == 0) goto L_0x010f;
    L_0x0087:
        r7 = com.mysql.jdbc.ProfilerEventHandlerFactory.getInstance(r3);	 Catch:{ all -> 0x0116 }
        r8 = new com.mysql.jdbc.profiler.ProfilerEvent;	 Catch:{ all -> 0x0116 }
        r11 = 0;
        r12 = "";
        r10 = r1.owner;	 Catch:{ all -> 0x0116 }
        r10 = r10.owningStatement;	 Catch:{ all -> 0x0116 }
        if (r10 != 0) goto L_0x009a;
    L_0x0096:
        r10 = "N/A";
    L_0x0098:
        r13 = r10;
        goto L_0x00a1;
    L_0x009a:
        r10 = r1.owner;	 Catch:{ all -> 0x0116 }
        r10 = r10.owningStatement;	 Catch:{ all -> 0x0116 }
        r10 = r10.currentCatalog;	 Catch:{ all -> 0x0116 }
        goto L_0x0098;
    L_0x00a1:
        r10 = r1.owner;	 Catch:{ all -> 0x0116 }
        r14 = r10.connectionId;	 Catch:{ all -> 0x0116 }
        r10 = r1.owner;	 Catch:{ all -> 0x0116 }
        r10 = r10.owningStatement;	 Catch:{ all -> 0x0116 }
        if (r10 != 0) goto L_0x00af;
    L_0x00ab:
        r10 = -1;
    L_0x00ac:
        r16 = r10;
        goto L_0x00b8;
    L_0x00af:
        r10 = r1.owner;	 Catch:{ all -> 0x0116 }
        r10 = r10.owningStatement;	 Catch:{ all -> 0x0116 }
        r10 = r10.getId();	 Catch:{ all -> 0x0116 }
        goto L_0x00ac;
    L_0x00b8:
        r17 = -1;
        r18 = java.lang.System.currentTimeMillis();	 Catch:{ all -> 0x0116 }
        r20 = 0;
        r22 = com.mysql.jdbc.Constants.MILLIS_I18N;	 Catch:{ all -> 0x0116 }
        r23 = 0;
        r24 = 0;
        r10 = new java.lang.StringBuilder;	 Catch:{ all -> 0x0116 }
        r10.<init>();	 Catch:{ all -> 0x0116 }
        r6 = "RowDataDynamic.2";
        r6 = com.mysql.jdbc.Messages.getString(r6);	 Catch:{ all -> 0x0116 }
        r10.append(r6);	 Catch:{ all -> 0x0116 }
        r10.append(r5);	 Catch:{ all -> 0x0116 }
        r6 = "RowDataDynamic.3";
        r6 = com.mysql.jdbc.Messages.getString(r6);	 Catch:{ all -> 0x0116 }
        r10.append(r6);	 Catch:{ all -> 0x0116 }
        r6 = "RowDataDynamic.4";
        r6 = com.mysql.jdbc.Messages.getString(r6);	 Catch:{ all -> 0x0116 }
        r10.append(r6);	 Catch:{ all -> 0x0116 }
        r6 = "RowDataDynamic.5";
        r6 = com.mysql.jdbc.Messages.getString(r6);	 Catch:{ all -> 0x0116 }
        r10.append(r6);	 Catch:{ all -> 0x0116 }
        r6 = "RowDataDynamic.6";
        r6 = com.mysql.jdbc.Messages.getString(r6);	 Catch:{ all -> 0x0116 }
        r10.append(r6);	 Catch:{ all -> 0x0116 }
        r6 = r1.owner;	 Catch:{ all -> 0x0116 }
        r6 = r6.pointOfOrigin;	 Catch:{ all -> 0x0116 }
        r10.append(r6);	 Catch:{ all -> 0x0116 }
        r25 = r10.toString();	 Catch:{ all -> 0x0116 }
        r10 = r8;
        r10.<init>(r11, r12, r13, r14, r16, r17, r18, r20, r22, r23, r24, r25);	 Catch:{ all -> 0x0116 }
        r7.consumeEvent(r8);	 Catch:{ all -> 0x0116 }
        goto L_0x010f;
    L_0x010e:
        r9 = r2;
    L_0x010f:
        monitor-exit(r2);	 Catch:{ all -> 0x0116 }
        r2 = 0;
        r1.metadata = r2;
        r1.owner = r2;
        return;
    L_0x0116:
        r0 = move-exception;
        r6 = r5;
        r10 = r9;
        r9 = r1;
        goto L_0x011f;
    L_0x011b:
        r0 = move-exception;
        r9 = r1;
        r10 = r2;
    L_0x011e:
        r6 = r5;
    L_0x011f:
        r5 = r4;
        r4 = r3;
    L_0x0121:
        r3 = r0;
        monitor-exit(r2);	 Catch:{ all -> 0x0124 }
        throw r3;
    L_0x0124:
        r0 = move-exception;
        goto L_0x0121;
        */
        throw new UnsupportedOperationException("Method not decompiled: com.mysql.jdbc.RowDataDynamic.close():void");
    }

    public ResultSetRow getAt(int ind) throws SQLException {
        notSupported();
        return null;
    }

    public int getCurrentRowNumber() throws SQLException {
        notSupported();
        return -1;
    }

    public ResultSetInternalMethods getOwner() {
        return this.owner;
    }

    public boolean hasNext() throws SQLException {
        boolean hasNext = this.nextRow != null;
        if (!(hasNext || this.streamerClosed)) {
            this.io.closeStreamer(this);
            this.streamerClosed = true;
        }
        return hasNext;
    }

    public boolean isAfterLast() throws SQLException {
        return this.isAfterEnd;
    }

    public boolean isBeforeFirst() throws SQLException {
        return this.index < 0;
    }

    public boolean isDynamic() {
        return true;
    }

    public boolean isEmpty() throws SQLException {
        notSupported();
        return false;
    }

    public boolean isFirst() throws SQLException {
        notSupported();
        return false;
    }

    public boolean isLast() throws SQLException {
        notSupported();
        return false;
    }

    public void moveRowRelative(int rows) throws SQLException {
        notSupported();
    }

    public ResultSetRow next() throws SQLException {
        nextRecord();
        if (!(this.nextRow != null || this.streamerClosed || this.moreResultsExisted)) {
            this.io.closeStreamer(this);
            this.streamerClosed = true;
        }
        if (!(this.nextRow == null || this.index == ActivityChooserViewAdapter.MAX_ACTIVITY_COUNT_UNLIMITED)) {
            this.index++;
        }
        return this.nextRow;
    }

    private void nextRecord() throws SQLException {
        try {
            if (this.noMoreRows) {
                this.nextRow = null;
                this.isAfterEnd = true;
            } else {
                this.nextRow = this.io.nextRow(this.metadata, this.columnCount, this.isBinaryEncoded, 1007, true, this.useBufferRowExplicit, true, null);
                if (this.nextRow == null) {
                    this.noMoreRows = true;
                    this.isAfterEnd = true;
                    this.moreResultsExisted = this.io.tackOnMoreStreamingResults(this.owner);
                    if (this.index == -1) {
                        this.wasEmpty = true;
                    }
                }
            }
        } catch (SQLException sqlEx) {
            if (sqlEx instanceof StreamingNotifiable) {
                ((StreamingNotifiable) sqlEx).setWasStreamingResults();
            }
            this.noMoreRows = true;
            throw sqlEx;
        } catch (Exception ex) {
            String exceptionType = ex.getClass().getName();
            String exceptionMessage = ex.getMessage();
            StringBuilder stringBuilder = new StringBuilder();
            stringBuilder.append(exceptionMessage);
            stringBuilder.append(Messages.getString("RowDataDynamic.7"));
            exceptionMessage = stringBuilder.toString();
            stringBuilder = new StringBuilder();
            stringBuilder.append(exceptionMessage);
            stringBuilder.append(Util.stackTraceToString(ex));
            exceptionMessage = stringBuilder.toString();
            stringBuilder = new StringBuilder();
            stringBuilder.append(Messages.getString("RowDataDynamic.8"));
            stringBuilder.append(exceptionType);
            stringBuilder.append(Messages.getString("RowDataDynamic.9"));
            stringBuilder.append(exceptionMessage);
            SQLException sqlEx2 = SQLError.createSQLException(stringBuilder.toString(), SQLError.SQL_STATE_GENERAL_ERROR, this.exceptionInterceptor);
            sqlEx2.initCause(ex);
            throw sqlEx2;
        }
    }

    private void notSupported() throws SQLException {
        throw new OperationNotSupportedException();
    }

    public void removeRow(int ind) throws SQLException {
        notSupported();
    }

    public void setCurrentRow(int rowNumber) throws SQLException {
        notSupported();
    }

    public void setOwner(ResultSetImpl rs) {
        this.owner = rs;
    }

    public int size() {
        return -1;
    }

    public boolean wasEmpty() {
        return this.wasEmpty;
    }

    public void setMetadata(Field[] metadata) {
        this.metadata = metadata;
    }
}
