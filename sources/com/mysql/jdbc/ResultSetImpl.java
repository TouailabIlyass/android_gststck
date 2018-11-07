package com.mysql.jdbc;

import android.support.v4.provider.FontsContractCompat.FontRequestCallback;
import android.support.v7.widget.helper.ItemTouchHelper.Callback;
import com.mysql.jdbc.log.LogUtils;
import com.mysql.jdbc.profiler.ProfilerEvent;
import com.mysql.jdbc.profiler.ProfilerEventHandler;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectInputStream;
import java.io.Reader;
import java.io.StringReader;
import java.io.UnsupportedEncodingException;
import java.lang.reflect.Constructor;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.net.MalformedURLException;
import java.net.URL;
import java.sql.Array;
import java.sql.Blob;
import java.sql.Clob;
import java.sql.Date;
import java.sql.Ref;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.sql.SQLWarning;
import java.sql.Statement;
import java.sql.Time;
import java.sql.Timestamp;
import java.util.Calendar;
import java.util.GregorianCalendar;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.StringTokenizer;
import java.util.TimeZone;
import java.util.TreeMap;

public class ResultSetImpl implements ResultSetInternalMethods {
    static final char[] EMPTY_SPACE = new char[255];
    private static final Constructor<?> JDBC_4_RS_4_ARG_CTOR;
    private static final Constructor<?> JDBC_4_RS_5_ARG_CTOR;
    private static final Constructor<?> JDBC_4_UPD_RS_5_ARG_CTOR;
    protected static final double MAX_DIFF_PREC = (((double) Float.parseFloat(Float.toString(Float.MAX_VALUE))) - Double.parseDouble(Float.toString(Float.MAX_VALUE)));
    protected static final double MIN_DIFF_PREC = (((double) Float.parseFloat(Float.toString(Float.MIN_VALUE))) - Double.parseDouble(Float.toString(Float.MIN_VALUE)));
    static int resultCounter = 1;
    protected String catalog = null;
    protected Map<String, Integer> columnLabelToIndex = null;
    protected Map<String, Integer> columnNameToIndex = null;
    protected Map<String, Integer> columnToIndexCache = null;
    protected boolean[] columnUsed = null;
    protected volatile MySQLConnection connection;
    protected long connectionId = 0;
    protected int currentRow = -1;
    protected boolean doingUpdates = false;
    protected ProfilerEventHandler eventSink = null;
    private ExceptionInterceptor exceptionInterceptor;
    Calendar fastClientCal = null;
    Calendar fastDefaultCal = null;
    protected int fetchDirection = 1000;
    protected int fetchSize = 0;
    protected Field[] fields;
    protected char firstCharOfQuery;
    protected Map<String, Integer> fullColumnNameToIndex = null;
    protected Calendar gmtCalendar = null;
    protected boolean hasBuiltIndexMapping = false;
    private String invalidRowReason = null;
    protected boolean isBinaryEncoded = false;
    protected boolean isClosed = false;
    private boolean jdbcCompliantTruncationForReads;
    protected ResultSetInternalMethods nextResultSet = null;
    protected boolean onInsertRow = false;
    private boolean onValidRow = false;
    protected StatementImpl owningStatement;
    private boolean padCharsWithSpace = false;
    protected String pointOfOrigin;
    protected boolean profileSql = false;
    protected boolean reallyResult = false;
    protected int resultId;
    protected int resultSetConcurrency = 0;
    protected int resultSetType = 0;
    protected boolean retainOwningStatement;
    protected RowData rowData;
    protected String serverInfo = null;
    private TimeZone serverTimeZoneTz;
    PreparedStatement statementUsedForFetchingRows;
    protected ResultSetRow thisRow = null;
    protected long updateCount;
    protected long updateId = -1;
    private boolean useColumnNamesInFindColumn;
    protected boolean useFastDateParsing = false;
    private boolean useFastIntParsing = true;
    protected boolean useLegacyDatetimeCode;
    private boolean useStrictFloatingPoint = false;
    protected boolean useUsageAdvisor = false;
    protected SQLWarning warningChain = null;
    protected boolean wasNullFlag = false;
    protected Statement wrapperStatement;

    static {
        int i = 0;
        if (Util.isJdbc4()) {
            try {
                String jdbc4ClassName = Util.isJdbc42() ? "com.mysql.jdbc.JDBC42ResultSet" : "com.mysql.jdbc.JDBC4ResultSet";
                JDBC_4_RS_4_ARG_CTOR = Class.forName(jdbc4ClassName).getConstructor(new Class[]{Long.TYPE, Long.TYPE, MySQLConnection.class, StatementImpl.class});
                JDBC_4_RS_5_ARG_CTOR = Class.forName(jdbc4ClassName).getConstructor(new Class[]{String.class, Field[].class, RowData.class, MySQLConnection.class, StatementImpl.class});
                JDBC_4_UPD_RS_5_ARG_CTOR = Class.forName(Util.isJdbc42() ? "com.mysql.jdbc.JDBC42UpdatableResultSet" : "com.mysql.jdbc.JDBC4UpdatableResultSet").getConstructor(new Class[]{String.class, Field[].class, RowData.class, MySQLConnection.class, StatementImpl.class});
            } catch (SecurityException e) {
                throw new RuntimeException(e);
            } catch (NoSuchMethodException e2) {
                throw new RuntimeException(e2);
            } catch (ClassNotFoundException e3) {
                throw new RuntimeException(e3);
            }
        }
        JDBC_4_RS_4_ARG_CTOR = null;
        JDBC_4_RS_5_ARG_CTOR = null;
        JDBC_4_UPD_RS_5_ARG_CTOR = null;
        while (true) {
            int i2 = i;
            if (i2 < EMPTY_SPACE.length) {
                EMPTY_SPACE[i2] = ' ';
                i = i2 + 1;
            } else {
                return;
            }
        }
    }

    protected static BigInteger convertLongToUlong(long longVal) {
        return new BigInteger(1, new byte[]{(byte) ((int) (longVal & 255)), (byte) ((int) (longVal >>> 8)), (byte) ((int) (longVal >>> 16)), (byte) ((int) (longVal >>> 24)), (byte) ((int) (longVal >>> 32)), (byte) ((int) (longVal >>> 40)), (byte) ((int) (longVal >>> 48)), (byte) ((int) (longVal >>> 56))});
    }

    protected static ResultSetImpl getInstance(long updateCount, long updateID, MySQLConnection conn, StatementImpl creatorStmt) throws SQLException {
        if (!Util.isJdbc4()) {
            return new ResultSetImpl(updateCount, updateID, conn, creatorStmt);
        }
        return (ResultSetImpl) Util.handleNewInstance(JDBC_4_RS_4_ARG_CTOR, new Object[]{Long.valueOf(updateCount), Long.valueOf(updateID), conn, creatorStmt}, conn.getExceptionInterceptor());
    }

    protected static ResultSetImpl getInstance(String catalog, Field[] fields, RowData tuples, MySQLConnection conn, StatementImpl creatorStmt, boolean isUpdatable) throws SQLException {
        if (Util.isJdbc4()) {
            if (isUpdatable) {
                return (ResultSetImpl) Util.handleNewInstance(JDBC_4_UPD_RS_5_ARG_CTOR, new Object[]{catalog, fields, tuples, conn, creatorStmt}, conn.getExceptionInterceptor());
            }
            return (ResultSetImpl) Util.handleNewInstance(JDBC_4_RS_5_ARG_CTOR, new Object[]{catalog, fields, tuples, conn, creatorStmt}, conn.getExceptionInterceptor());
        } else if (isUpdatable) {
            return new UpdatableResultSet(catalog, fields, tuples, conn, creatorStmt);
        } else {
            return new ResultSetImpl(catalog, fields, tuples, conn, creatorStmt);
        }
    }

    public ResultSetImpl(long updateCount, long updateID, MySQLConnection conn, StatementImpl creatorStmt) {
        this.updateCount = updateCount;
        this.updateId = updateID;
        this.reallyResult = false;
        this.fields = new Field[0];
        this.connection = conn;
        this.owningStatement = creatorStmt;
        this.retainOwningStatement = false;
        if (this.connection != null) {
            this.exceptionInterceptor = this.connection.getExceptionInterceptor();
            this.retainOwningStatement = this.connection.getRetainStatementAfterResultSetClose();
            this.connectionId = this.connection.getId();
            this.serverTimeZoneTz = this.connection.getServerTimezoneTZ();
            this.padCharsWithSpace = this.connection.getPadCharsWithSpace();
            this.useLegacyDatetimeCode = this.connection.getUseLegacyDatetimeCode();
        }
    }

    public ResultSetImpl(String catalog, Field[] fields, RowData tuples, MySQLConnection conn, StatementImpl creatorStmt) throws SQLException {
        this.connection = conn;
        this.retainOwningStatement = false;
        if (this.connection != null) {
            this.exceptionInterceptor = this.connection.getExceptionInterceptor();
            this.useStrictFloatingPoint = this.connection.getStrictFloatingPoint();
            this.connectionId = this.connection.getId();
            this.useFastDateParsing = this.connection.getUseFastDateParsing();
            this.profileSql = this.connection.getProfileSql();
            this.retainOwningStatement = this.connection.getRetainStatementAfterResultSetClose();
            this.jdbcCompliantTruncationForReads = this.connection.getJdbcCompliantTruncationForReads();
            this.useFastIntParsing = this.connection.getUseFastIntParsing();
            this.serverTimeZoneTz = this.connection.getServerTimezoneTZ();
            this.padCharsWithSpace = this.connection.getPadCharsWithSpace();
        }
        this.owningStatement = creatorStmt;
        this.catalog = catalog;
        this.fields = fields;
        this.rowData = tuples;
        this.updateCount = (long) this.rowData.size();
        this.reallyResult = true;
        if (this.rowData.size() <= 0) {
            this.thisRow = null;
        } else if (this.updateCount == 1 && this.thisRow == null) {
            this.rowData.close();
            this.updateCount = -1;
        }
        this.rowData.setOwner(this);
        if (this.fields != null) {
            initializeWithMetadata();
        }
        this.useLegacyDatetimeCode = this.connection.getUseLegacyDatetimeCode();
        this.useColumnNamesInFindColumn = this.connection.getUseColumnNamesInFindColumn();
        setRowPositionValidity();
    }

    public void initializeWithMetadata() throws SQLException {
        synchronized (checkClosed().getConnectionMutex()) {
            this.rowData.setMetadata(this.fields);
            this.columnToIndexCache = new HashMap();
            if (this.profileSql || this.connection.getUseUsageAdvisor()) {
                this.columnUsed = new boolean[this.fields.length];
                this.pointOfOrigin = LogUtils.findCallingClassAndMethod(new Throwable());
                int i = resultCounter;
                resultCounter = i + 1;
                this.resultId = i;
                this.useUsageAdvisor = this.connection.getUseUsageAdvisor();
                this.eventSink = ProfilerEventHandlerFactory.getInstance(this.connection);
            }
            if (this.connection.getGatherPerformanceMetrics()) {
                this.connection.incrementNumberOfResultSetsCreated();
                Set<String> tableNamesSet = new HashSet();
                for (Field f : this.fields) {
                    String tableName = f.getOriginalTableName();
                    if (tableName == null) {
                        tableName = f.getTableName();
                    }
                    if (tableName != null) {
                        if (this.connection.lowerCaseTableNames()) {
                            tableName = tableName.toLowerCase();
                        }
                        tableNamesSet.add(tableName);
                    }
                }
                this.connection.reportNumberOfTablesAccessed(tableNamesSet.size());
            }
        }
    }

    private synchronized Calendar getFastDefaultCalendar() {
        if (this.fastDefaultCal == null) {
            this.fastDefaultCal = new GregorianCalendar(Locale.US);
            this.fastDefaultCal.setTimeZone(getDefaultTimeZone());
        }
        return this.fastDefaultCal;
    }

    private synchronized Calendar getFastClientCalendar() {
        if (this.fastClientCal == null) {
            this.fastClientCal = new GregorianCalendar(Locale.US);
        }
        return this.fastClientCal;
    }

    public boolean absolute(int row) throws SQLException {
        boolean b;
        synchronized (checkClosed().getConnectionMutex()) {
            boolean b2 = true;
            if (this.rowData.size() == 0) {
                b2 = false;
            } else {
                if (this.onInsertRow) {
                    this.onInsertRow = false;
                }
                if (this.doingUpdates) {
                    this.doingUpdates = false;
                }
                if (this.thisRow != null) {
                    this.thisRow.closeOpenStreams();
                }
                if (row == 0) {
                    beforeFirst();
                    b2 = false;
                } else if (row == 1) {
                    b2 = first();
                } else if (row == -1) {
                    b2 = last();
                } else if (row > this.rowData.size()) {
                    afterLast();
                    b2 = false;
                } else {
                    if (row < 0) {
                        int newRowPosition = (this.rowData.size() + row) + 1;
                        if (newRowPosition <= 0) {
                            beforeFirst();
                            b2 = false;
                        } else {
                            b2 = absolute(newRowPosition);
                        }
                    } else {
                        row--;
                        this.rowData.setCurrentRow(row);
                        this.thisRow = this.rowData.getAt(row);
                    }
                    b = b2;
                    setRowPositionValidity();
                }
            }
            b = b2;
            setRowPositionValidity();
        }
        return b;
    }

    public void afterLast() throws SQLException {
        synchronized (checkClosed().getConnectionMutex()) {
            if (this.onInsertRow) {
                this.onInsertRow = false;
            }
            if (this.doingUpdates) {
                this.doingUpdates = false;
            }
            if (this.thisRow != null) {
                this.thisRow.closeOpenStreams();
            }
            if (this.rowData.size() != 0) {
                this.rowData.afterLast();
                this.thisRow = null;
            }
            setRowPositionValidity();
        }
    }

    public void beforeFirst() throws SQLException {
        synchronized (checkClosed().getConnectionMutex()) {
            if (this.onInsertRow) {
                this.onInsertRow = false;
            }
            if (this.doingUpdates) {
                this.doingUpdates = false;
            }
            if (this.rowData.size() == 0) {
                return;
            }
            if (this.thisRow != null) {
                this.thisRow.closeOpenStreams();
            }
            this.rowData.beforeFirst();
            this.thisRow = null;
            setRowPositionValidity();
        }
    }

    public void buildIndexMapping() throws SQLException {
        int numFields = this.fields.length;
        this.columnLabelToIndex = new TreeMap(String.CASE_INSENSITIVE_ORDER);
        this.fullColumnNameToIndex = new TreeMap(String.CASE_INSENSITIVE_ORDER);
        this.columnNameToIndex = new TreeMap(String.CASE_INSENSITIVE_ORDER);
        for (int i = numFields - 1; i >= 0; i--) {
            Integer index = Integer.valueOf(i);
            String columnName = this.fields[i].getOriginalName();
            String columnLabel = this.fields[i].getName();
            String fullColumnName = this.fields[i].getFullName();
            if (columnLabel != null) {
                this.columnLabelToIndex.put(columnLabel, index);
            }
            if (fullColumnName != null) {
                this.fullColumnNameToIndex.put(fullColumnName, index);
            }
            if (columnName != null) {
                this.columnNameToIndex.put(columnName, index);
            }
        }
        this.hasBuiltIndexMapping = true;
    }

    public void cancelRowUpdates() throws SQLException {
        throw new NotUpdatable();
    }

    protected final MySQLConnection checkClosed() throws SQLException {
        MySQLConnection c = this.connection;
        if (c != null) {
            return c;
        }
        throw SQLError.createSQLException(Messages.getString("ResultSet.Operation_not_allowed_after_ResultSet_closed_144"), SQLError.SQL_STATE_GENERAL_ERROR, getExceptionInterceptor());
    }

    protected final void checkColumnBounds(int columnIndex) throws SQLException {
        synchronized (checkClosed().getConnectionMutex()) {
            if (columnIndex < 1) {
                throw SQLError.createSQLException(Messages.getString("ResultSet.Column_Index_out_of_range_low", new Object[]{Integer.valueOf(columnIndex), Integer.valueOf(this.fields.length)}), SQLError.SQL_STATE_ILLEGAL_ARGUMENT, getExceptionInterceptor());
            } else if (columnIndex > this.fields.length) {
                throw SQLError.createSQLException(Messages.getString("ResultSet.Column_Index_out_of_range_high", new Object[]{Integer.valueOf(columnIndex), Integer.valueOf(this.fields.length)}), SQLError.SQL_STATE_ILLEGAL_ARGUMENT, getExceptionInterceptor());
            } else {
                if (this.profileSql || this.useUsageAdvisor) {
                    this.columnUsed[columnIndex - 1] = true;
                }
            }
        }
    }

    protected void checkRowPos() throws SQLException {
        checkClosed();
        if (!this.onValidRow) {
            throw SQLError.createSQLException(this.invalidRowReason, SQLError.SQL_STATE_GENERAL_ERROR, getExceptionInterceptor());
        }
    }

    private void setRowPositionValidity() throws SQLException {
        if (!this.rowData.isDynamic() && this.rowData.size() == 0) {
            this.invalidRowReason = Messages.getString("ResultSet.Illegal_operation_on_empty_result_set");
            this.onValidRow = false;
        } else if (this.rowData.isBeforeFirst()) {
            this.invalidRowReason = Messages.getString("ResultSet.Before_start_of_result_set_146");
            this.onValidRow = false;
        } else if (this.rowData.isAfterLast()) {
            this.invalidRowReason = Messages.getString("ResultSet.After_end_of_result_set_148");
            this.onValidRow = false;
        } else {
            this.onValidRow = true;
            this.invalidRowReason = null;
        }
    }

    public synchronized void clearNextResult() {
        this.nextResultSet = null;
    }

    public void clearWarnings() throws SQLException {
        synchronized (checkClosed().getConnectionMutex()) {
            this.warningChain = null;
        }
    }

    public void close() throws SQLException {
        realClose(true);
    }

    private int convertToZeroWithEmptyCheck() throws SQLException {
        if (this.connection.getEmptyStringsConvertToZero()) {
            return 0;
        }
        throw SQLError.createSQLException("Can't convert empty string ('') to numeric", SQLError.SQL_STATE_INVALID_CHARACTER_VALUE_FOR_CAST, getExceptionInterceptor());
    }

    private String convertToZeroLiteralStringWithEmptyCheck() throws SQLException {
        if (this.connection.getEmptyStringsConvertToZero()) {
            return "0";
        }
        throw SQLError.createSQLException("Can't convert empty string ('') to numeric", SQLError.SQL_STATE_INVALID_CHARACTER_VALUE_FOR_CAST, getExceptionInterceptor());
    }

    public ResultSetInternalMethods copy() throws SQLException {
        ResultSetImpl rs;
        synchronized (checkClosed().getConnectionMutex()) {
            rs = getInstance(this.catalog, this.fields, this.rowData, this.connection, this.owningStatement, false);
            if (this.isBinaryEncoded) {
                rs.setBinaryEncoded();
            }
        }
        return rs;
    }

    public void redefineFieldsForDBMD(Field[] f) {
        this.fields = f;
        for (int i = 0; i < this.fields.length; i++) {
            this.fields[i].setUseOldNameMetadata(true);
            this.fields[i].setConnection(this.connection);
        }
    }

    public void populateCachedMetaData(CachedResultSetMetaData cachedMetaData) throws SQLException {
        cachedMetaData.fields = this.fields;
        cachedMetaData.columnNameToIndex = this.columnLabelToIndex;
        cachedMetaData.fullColumnNameToIndex = this.fullColumnNameToIndex;
        cachedMetaData.metadata = getMetaData();
    }

    public void initializeFromCachedMetaData(CachedResultSetMetaData cachedMetaData) {
        this.fields = cachedMetaData.fields;
        this.columnLabelToIndex = cachedMetaData.columnNameToIndex;
        this.fullColumnNameToIndex = cachedMetaData.fullColumnNameToIndex;
        this.hasBuiltIndexMapping = true;
    }

    public void deleteRow() throws SQLException {
        throw new NotUpdatable();
    }

    private String extractStringFromNativeColumn(int columnIndex, int mysqlType) throws SQLException {
        int columnIndexMinusOne = columnIndex - 1;
        this.wasNullFlag = false;
        if (this.thisRow.isNull(columnIndexMinusOne)) {
            this.wasNullFlag = true;
            return null;
        }
        this.wasNullFlag = false;
        return this.thisRow.getString(columnIndex - 1, this.fields[columnIndexMinusOne].getEncoding(), this.connection);
    }

    protected Date fastDateCreate(Calendar cal, int year, int month, int day) throws SQLException {
        synchronized (checkClosed().getConnectionMutex()) {
            Calendar targetCalendar = cal;
            if (cal == null) {
                if (this.connection.getNoTimezoneConversionForDateType()) {
                    targetCalendar = getFastClientCalendar();
                } else {
                    targetCalendar = getFastDefaultCalendar();
                }
            }
            if (this.useLegacyDatetimeCode) {
                boolean z = cal == null && !this.connection.getNoTimezoneConversionForDateType() && this.connection.getUseGmtMillisForDatetimes();
                boolean useGmtMillis = z;
                Date fastDateCreate = TimeUtil.fastDateCreate(useGmtMillis, useGmtMillis ? getGmtCalendar() : targetCalendar, targetCalendar, year, month, day);
                return fastDateCreate;
            }
            fastDateCreate = TimeUtil.fastDateCreate(year, month, day, targetCalendar);
            return fastDateCreate;
        }
    }

    protected Time fastTimeCreate(Calendar cal, int hour, int minute, int second) throws SQLException {
        synchronized (checkClosed().getConnectionMutex()) {
            if (this.useLegacyDatetimeCode) {
                if (cal == null) {
                    cal = getFastDefaultCalendar();
                }
                Time fastTimeCreate = TimeUtil.fastTimeCreate(cal, hour, minute, second, getExceptionInterceptor());
                return fastTimeCreate;
            }
            fastTimeCreate = TimeUtil.fastTimeCreate(hour, minute, second, cal, getExceptionInterceptor());
            return fastTimeCreate;
        }
    }

    protected Timestamp fastTimestampCreate(Calendar cal, int year, int month, int day, int hour, int minute, int seconds, int secondsPart) throws SQLException {
        Calendar cal2;
        Throwable th;
        Throwable th2;
        synchronized (checkClosed().getConnectionMutex()) {
            try {
                if (r1.useLegacyDatetimeCode) {
                    if (cal == null) {
                        cal2 = getFastDefaultCalendar();
                    } else {
                        cal2 = cal;
                    }
                    try {
                        boolean useGmtMillis = r1.connection.getUseGmtMillisForDatetimes();
                        Timestamp fastTimestampCreate = TimeUtil.fastTimestampCreate(useGmtMillis, useGmtMillis ? getGmtCalendar() : null, cal2, year, month, day, hour, minute, seconds, secondsPart);
                        return fastTimestampCreate;
                    } catch (Throwable th3) {
                        th = th3;
                        Calendar calendar = cal2;
                        while (true) {
                            th2 = th;
                            try {
                                break;
                            } catch (Throwable th4) {
                                th = th4;
                            }
                        }
                        throw th2;
                    }
                }
                Timestamp fastTimestampCreate2 = TimeUtil.fastTimestampCreate(cal.getTimeZone(), year, month, day, hour, minute, seconds, secondsPart);
                return fastTimestampCreate2;
            } catch (Throwable th5) {
                th = th5;
                while (true) {
                    th2 = th;
                    break;
                }
                throw th2;
            }
        }
    }

    public int findColumn(String columnName) throws SQLException {
        synchronized (checkClosed().getConnectionMutex()) {
            if (!this.hasBuiltIndexMapping) {
                buildIndexMapping();
            }
            Integer index = (Integer) this.columnToIndexCache.get(columnName);
            if (index != null) {
                int intValue = index.intValue() + 1;
                return intValue;
            }
            index = (Integer) this.columnLabelToIndex.get(columnName);
            if (index == null && this.useColumnNamesInFindColumn) {
                index = (Integer) this.columnNameToIndex.get(columnName);
            }
            if (index == null) {
                index = (Integer) this.fullColumnNameToIndex.get(columnName);
            }
            if (index != null) {
                this.columnToIndexCache.put(columnName, index);
                intValue = index.intValue() + 1;
                return intValue;
            }
            intValue = 0;
            while (intValue < this.fields.length) {
                int i;
                if (this.fields[intValue].getName().equalsIgnoreCase(columnName)) {
                    i = intValue + 1;
                    return i;
                } else if (this.fields[intValue].getFullName().equalsIgnoreCase(columnName)) {
                    i = intValue + 1;
                    return i;
                } else {
                    intValue++;
                }
            }
            StringBuilder stringBuilder = new StringBuilder();
            stringBuilder.append(Messages.getString("ResultSet.Column____112"));
            stringBuilder.append(columnName);
            stringBuilder.append(Messages.getString("ResultSet.___not_found._113"));
            throw SQLError.createSQLException(stringBuilder.toString(), SQLError.SQL_STATE_COLUMN_NOT_FOUND, getExceptionInterceptor());
        }
    }

    public boolean first() throws SQLException {
        boolean b;
        synchronized (checkClosed().getConnectionMutex()) {
            b = true;
            if (this.rowData.isEmpty()) {
                b = false;
            } else {
                if (this.onInsertRow) {
                    this.onInsertRow = false;
                }
                if (this.doingUpdates) {
                    this.doingUpdates = false;
                }
                this.rowData.beforeFirst();
                this.thisRow = this.rowData.next();
            }
            setRowPositionValidity();
        }
        return b;
    }

    public Array getArray(int i) throws SQLException {
        checkColumnBounds(i);
        throw SQLError.createSQLFeatureNotSupportedException();
    }

    public Array getArray(String colName) throws SQLException {
        return getArray(findColumn(colName));
    }

    public InputStream getAsciiStream(int columnIndex) throws SQLException {
        checkRowPos();
        if (this.isBinaryEncoded) {
            return getNativeBinaryStream(columnIndex);
        }
        return getBinaryStream(columnIndex);
    }

    public InputStream getAsciiStream(String columnName) throws SQLException {
        return getAsciiStream(findColumn(columnName));
    }

    public BigDecimal getBigDecimal(int columnIndex) throws SQLException {
        if (this.isBinaryEncoded) {
            return getNativeBigDecimal(columnIndex);
        }
        String stringVal = getString(columnIndex);
        if (stringVal == null) {
            return null;
        }
        if (stringVal.length() == 0) {
            return new BigDecimal(convertToZeroLiteralStringWithEmptyCheck());
        }
        try {
            return new BigDecimal(stringVal);
        } catch (NumberFormatException e) {
            throw SQLError.createSQLException(Messages.getString("ResultSet.Bad_format_for_BigDecimal", new Object[]{stringVal, Integer.valueOf(columnIndex)}), SQLError.SQL_STATE_ILLEGAL_ARGUMENT, getExceptionInterceptor());
        }
    }

    @Deprecated
    public BigDecimal getBigDecimal(int columnIndex, int scale) throws SQLException {
        if (this.isBinaryEncoded) {
            return getNativeBigDecimal(columnIndex, scale);
        }
        String stringVal = getString(columnIndex);
        if (stringVal == null) {
            return null;
        }
        if (stringVal.length() == 0) {
            BigDecimal val = new BigDecimal(convertToZeroLiteralStringWithEmptyCheck());
            try {
                return val.setScale(scale);
            } catch (ArithmeticException e) {
                try {
                    return val.setScale(scale, 4);
                } catch (ArithmeticException e2) {
                    throw SQLError.createSQLException(Messages.getString("ResultSet.Bad_format_for_BigDecimal", new Object[]{stringVal, Integer.valueOf(columnIndex)}), SQLError.SQL_STATE_ILLEGAL_ARGUMENT, getExceptionInterceptor());
                }
            }
        }
        NumberFormatException ex;
        try {
            ex = new BigDecimal(stringVal);
        } catch (NumberFormatException e3) {
            if (this.fields[columnIndex - 1].getMysqlType() == 16) {
                ex = new BigDecimal(getNumericRepresentationOfSQLBitType(columnIndex));
            } else {
                throw SQLError.createSQLException(Messages.getString("ResultSet.Bad_format_for_BigDecimal", new Object[]{Integer.valueOf(columnIndex), stringVal}), SQLError.SQL_STATE_ILLEGAL_ARGUMENT, getExceptionInterceptor());
            }
        }
        try {
            return ex.setScale(scale);
        } catch (ArithmeticException e4) {
            try {
                return ex.setScale(scale, 4);
            } catch (ArithmeticException e5) {
                throw SQLError.createSQLException(Messages.getString("ResultSet.Bad_format_for_BigDecimal", new Object[]{Integer.valueOf(columnIndex), stringVal}), SQLError.SQL_STATE_ILLEGAL_ARGUMENT, getExceptionInterceptor());
            }
        }
    }

    public BigDecimal getBigDecimal(String columnName) throws SQLException {
        return getBigDecimal(findColumn(columnName));
    }

    @Deprecated
    public BigDecimal getBigDecimal(String columnName, int scale) throws SQLException {
        return getBigDecimal(findColumn(columnName), scale);
    }

    private final BigDecimal getBigDecimalFromString(String stringVal, int columnIndex, int scale) throws SQLException {
        if (stringVal == null) {
            return null;
        }
        if (stringVal.length() == 0) {
            BigDecimal bdVal = new BigDecimal(convertToZeroLiteralStringWithEmptyCheck());
            try {
                return bdVal.setScale(scale);
            } catch (ArithmeticException e) {
                try {
                    return bdVal.setScale(scale, 4);
                } catch (ArithmeticException e2) {
                    throw new SQLException(Messages.getString("ResultSet.Bad_format_for_BigDecimal", new Object[]{stringVal, Integer.valueOf(columnIndex)}), SQLError.SQL_STATE_ILLEGAL_ARGUMENT);
                }
            }
        }
        try {
            return new BigDecimal(stringVal).setScale(scale);
        } catch (ArithmeticException e3) {
            try {
                return new BigDecimal(stringVal).setScale(scale, 4);
            } catch (ArithmeticException e4) {
                throw new SQLException(Messages.getString("ResultSet.Bad_format_for_BigDecimal", new Object[]{stringVal, Integer.valueOf(columnIndex)}), SQLError.SQL_STATE_ILLEGAL_ARGUMENT);
            } catch (NumberFormatException e5) {
                if (this.fields[columnIndex - 1].getMysqlType() == 16) {
                    long valueAsLong = getNumericRepresentationOfSQLBitType(columnIndex);
                    try {
                        return new BigDecimal(valueAsLong).setScale(scale);
                    } catch (ArithmeticException e6) {
                        try {
                            return new BigDecimal(valueAsLong).setScale(scale, 4);
                        } catch (ArithmeticException e7) {
                            throw new SQLException(Messages.getString("ResultSet.Bad_format_for_BigDecimal", new Object[]{stringVal, Integer.valueOf(columnIndex)}), SQLError.SQL_STATE_ILLEGAL_ARGUMENT);
                        }
                    }
                } else if (this.fields[columnIndex - 1].getMysqlType() == 1 && this.connection.getTinyInt1isBit() && this.fields[columnIndex - 1].getLength() == 1) {
                    return new BigDecimal(stringVal.equalsIgnoreCase("true")).setScale(scale);
                } else {
                    throw new SQLException(Messages.getString("ResultSet.Bad_format_for_BigDecimal", new Object[]{stringVal, Integer.valueOf(columnIndex)}), SQLError.SQL_STATE_ILLEGAL_ARGUMENT);
                }
            }
        }
    }

    public InputStream getBinaryStream(int columnIndex) throws SQLException {
        checkRowPos();
        if (this.isBinaryEncoded) {
            return getNativeBinaryStream(columnIndex);
        }
        checkColumnBounds(columnIndex);
        int columnIndexMinusOne = columnIndex - 1;
        if (this.thisRow.isNull(columnIndexMinusOne)) {
            this.wasNullFlag = true;
            return null;
        }
        this.wasNullFlag = false;
        return this.thisRow.getBinaryInputStream(columnIndexMinusOne);
    }

    public InputStream getBinaryStream(String columnName) throws SQLException {
        return getBinaryStream(findColumn(columnName));
    }

    public Blob getBlob(int columnIndex) throws SQLException {
        if (this.isBinaryEncoded) {
            return getNativeBlob(columnIndex);
        }
        checkRowPos();
        checkColumnBounds(columnIndex);
        int columnIndexMinusOne = columnIndex - 1;
        if (this.thisRow.isNull(columnIndexMinusOne)) {
            this.wasNullFlag = true;
        } else {
            this.wasNullFlag = false;
        }
        if (this.wasNullFlag) {
            return null;
        }
        if (this.connection.getEmulateLocators()) {
            return new BlobFromLocator(this, columnIndex, getExceptionInterceptor());
        }
        return new Blob(this.thisRow.getColumnValue(columnIndexMinusOne), getExceptionInterceptor());
    }

    public Blob getBlob(String colName) throws SQLException {
        return getBlob(findColumn(colName));
    }

    public boolean getBoolean(int columnIndex) throws SQLException {
        checkColumnBounds(columnIndex);
        int columnIndexMinusOne = columnIndex - 1;
        Field field = this.fields[columnIndexMinusOne];
        if (field.getMysqlType() == 16) {
            return byteArrayToBoolean(columnIndexMinusOne);
        }
        boolean z = false;
        this.wasNullFlag = false;
        int sqlType = field.getSQLType();
        long boolVal;
        if (sqlType != 16) {
            switch (sqlType) {
                case -7:
                case -6:
                case -5:
                    break;
                default:
                    switch (sqlType) {
                        case 2:
                        case 3:
                        case 4:
                        case 5:
                        case 6:
                        case 7:
                        case 8:
                            break;
                        default:
                            if (this.connection.getPedantic()) {
                                if (!(sqlType == 70 || sqlType == 2000)) {
                                    switch (sqlType) {
                                        case FontRequestCallback.FAIL_REASON_SECURITY_VIOLATION /*-4*/:
                                        case FontRequestCallback.FAIL_REASON_FONT_LOAD_ERROR /*-3*/:
                                        case -2:
                                            break;
                                        default:
                                            switch (sqlType) {
                                                case 91:
                                                case 92:
                                                case 93:
                                                    break;
                                                default:
                                                    switch (sqlType) {
                                                        case 2002:
                                                        case 2003:
                                                        case 2004:
                                                        case 2005:
                                                        case 2006:
                                                            break;
                                                        default:
                                                            break;
                                                    }
                                            }
                                    }
                                }
                                throw SQLError.createSQLException("Required type conversion not allowed", SQLError.SQL_STATE_INVALID_CHARACTER_VALUE_FOR_CAST, getExceptionInterceptor());
                            }
                            if (!(sqlType == -2 || sqlType == -3 || sqlType == -4)) {
                                if (sqlType != 2004) {
                                    if (this.useUsageAdvisor) {
                                        issueConversionViaParsingWarning("getBoolean()", columnIndex, this.thisRow.getColumnValue(columnIndexMinusOne), this.fields[columnIndex], new int[]{16, 5, 1, 2, 3, 8, 4});
                                    }
                                    return getBooleanFromString(getString(columnIndex));
                                }
                            }
                            return byteArrayToBoolean(columnIndexMinusOne);
                    }
            }
            boolVal = getLong(columnIndex, false);
            if (boolVal != -1) {
                if (boolVal <= 0) {
                    return z;
                }
            }
            z = true;
            return z;
        } else if (field.getMysqlType() == -1) {
            return getBooleanFromString(getString(columnIndex));
        } else {
            boolVal = getLong(columnIndex, false);
            if (boolVal != -1) {
                if (boolVal <= 0) {
                    return z;
                }
            }
            z = true;
            return z;
        }
    }

    private boolean byteArrayToBoolean(int columnIndexMinusOne) throws SQLException {
        Object value = this.thisRow.getColumnValue(columnIndexMinusOne);
        boolean z = true;
        if (value == null) {
            this.wasNullFlag = true;
            return false;
        }
        this.wasNullFlag = false;
        if (((byte[]) value).length == 0) {
            return false;
        }
        byte boolVal = ((byte[]) value)[0];
        if (boolVal == (byte) 49) {
            return true;
        }
        if (boolVal == (byte) 48) {
            return false;
        }
        if (boolVal != (byte) -1) {
            if (boolVal <= (byte) 0) {
                z = false;
            }
        }
        return z;
    }

    public boolean getBoolean(String columnName) throws SQLException {
        return getBoolean(findColumn(columnName));
    }

    private final boolean getBooleanFromString(String stringVal) throws SQLException {
        boolean z = false;
        if (stringVal == null || stringVal.length() <= 0) {
            return false;
        }
        int c = Character.toLowerCase(stringVal.charAt(0));
        if (!(c == 116 || c == 121 || c == 49)) {
            if (!stringVal.equals("-1")) {
                return z;
            }
        }
        z = true;
        return z;
    }

    public byte getByte(int columnIndex) throws SQLException {
        if (this.isBinaryEncoded) {
            return getNativeByte(columnIndex);
        }
        String stringVal = getString(columnIndex);
        if (!this.wasNullFlag) {
            if (stringVal != null) {
                return getByteFromString(stringVal, columnIndex);
            }
        }
        return (byte) 0;
    }

    public byte getByte(String columnName) throws SQLException {
        return getByte(findColumn(columnName));
    }

    private final byte getByteFromString(String stringVal, int columnIndex) throws SQLException {
        if (stringVal != null && stringVal.length() == 0) {
            return (byte) convertToZeroWithEmptyCheck();
        }
        if (stringVal == null) {
            return (byte) 0;
        }
        stringVal = stringVal.trim();
        try {
            if (stringVal.indexOf(".") != -1) {
                double valueAsDouble = Double.parseDouble(stringVal);
                if (this.jdbcCompliantTruncationForReads && (valueAsDouble < -128.0d || valueAsDouble > 127.0d)) {
                    throwRangeException(stringVal, columnIndex, -6);
                }
                return (byte) ((int) valueAsDouble);
            }
            long valueAsLong = Long.parseLong(stringVal);
            if (this.jdbcCompliantTruncationForReads && (valueAsLong < -128 || valueAsLong > 127)) {
                throwRangeException(String.valueOf(valueAsLong), columnIndex, -6);
            }
            return (byte) ((int) valueAsLong);
        } catch (NumberFormatException e) {
            StringBuilder stringBuilder = new StringBuilder();
            stringBuilder.append(Messages.getString("ResultSet.Value____173"));
            stringBuilder.append(stringVal);
            stringBuilder.append(Messages.getString("ResultSet.___is_out_of_range_[-127,127]_174"));
            throw SQLError.createSQLException(stringBuilder.toString(), SQLError.SQL_STATE_ILLEGAL_ARGUMENT, getExceptionInterceptor());
        }
    }

    public byte[] getBytes(int columnIndex) throws SQLException {
        return getBytes(columnIndex, false);
    }

    protected byte[] getBytes(int columnIndex, boolean noConversion) throws SQLException {
        if (this.isBinaryEncoded) {
            return getNativeBytes(columnIndex, noConversion);
        }
        checkRowPos();
        checkColumnBounds(columnIndex);
        int columnIndexMinusOne = columnIndex - 1;
        if (this.thisRow.isNull(columnIndexMinusOne)) {
            this.wasNullFlag = true;
        } else {
            this.wasNullFlag = false;
        }
        if (this.wasNullFlag) {
            return null;
        }
        return this.thisRow.getColumnValue(columnIndexMinusOne);
    }

    public byte[] getBytes(String columnName) throws SQLException {
        return getBytes(findColumn(columnName));
    }

    private final byte[] getBytesFromString(String stringVal) throws SQLException {
        if (stringVal == null) {
            return null;
        }
        return StringUtils.getBytes(stringVal, this.connection.getEncoding(), this.connection.getServerCharset(), this.connection.parserKnowsUnicode(), this.connection, getExceptionInterceptor());
    }

    public int getBytesSize() throws SQLException {
        RowData localRowData = this.rowData;
        checkClosed();
        if (!(localRowData instanceof RowDataStatic)) {
            return -1;
        }
        int bytesSize = 0;
        for (int i = 0; i < localRowData.size(); i++) {
            bytesSize += localRowData.getAt(i).getBytesSize();
        }
        return bytesSize;
    }

    protected Calendar getCalendarInstanceForSessionOrNew() throws SQLException {
        synchronized (checkClosed().getConnectionMutex()) {
            if (this.connection != null) {
                Calendar calendarInstanceForSessionOrNew = this.connection.getCalendarInstanceForSessionOrNew();
                return calendarInstanceForSessionOrNew;
            }
            calendarInstanceForSessionOrNew = new GregorianCalendar();
            return calendarInstanceForSessionOrNew;
        }
    }

    public Reader getCharacterStream(int columnIndex) throws SQLException {
        if (this.isBinaryEncoded) {
            return getNativeCharacterStream(columnIndex);
        }
        checkColumnBounds(columnIndex);
        int columnIndexMinusOne = columnIndex - 1;
        if (this.thisRow.isNull(columnIndexMinusOne)) {
            this.wasNullFlag = true;
            return null;
        }
        this.wasNullFlag = false;
        return this.thisRow.getReader(columnIndexMinusOne);
    }

    public Reader getCharacterStream(String columnName) throws SQLException {
        return getCharacterStream(findColumn(columnName));
    }

    private final Reader getCharacterStreamFromString(String stringVal) throws SQLException {
        if (stringVal != null) {
            return new StringReader(stringVal);
        }
        return null;
    }

    public Clob getClob(int i) throws SQLException {
        if (this.isBinaryEncoded) {
            return getNativeClob(i);
        }
        String asString = getStringForClob(i);
        if (asString == null) {
            return null;
        }
        return new Clob(asString, getExceptionInterceptor());
    }

    public Clob getClob(String colName) throws SQLException {
        return getClob(findColumn(colName));
    }

    private final Clob getClobFromString(String stringVal) throws SQLException {
        return new Clob(stringVal, getExceptionInterceptor());
    }

    public int getConcurrency() throws SQLException {
        return 1007;
    }

    public String getCursorName() throws SQLException {
        throw SQLError.createSQLException(Messages.getString("ResultSet.Positioned_Update_not_supported"), SQLError.SQL_STATE_DRIVER_NOT_CAPABLE, getExceptionInterceptor());
    }

    public Date getDate(int columnIndex) throws SQLException {
        return getDate(columnIndex, null);
    }

    public Date getDate(int columnIndex, Calendar cal) throws SQLException {
        if (this.isBinaryEncoded) {
            return getNativeDate(columnIndex, cal);
        }
        if (this.useFastDateParsing) {
            checkColumnBounds(columnIndex);
            int columnIndexMinusOne = columnIndex - 1;
            Date tmpDate = this.thisRow.getDateFast(columnIndexMinusOne, this.connection, this, cal);
            if (!this.thisRow.isNull(columnIndexMinusOne)) {
                if (tmpDate != null) {
                    this.wasNullFlag = false;
                    return tmpDate;
                }
            }
            this.wasNullFlag = true;
            return null;
        }
        String stringVal = getStringInternal(columnIndex, false);
        if (stringVal == null) {
            return null;
        }
        return getDateFromString(stringVal, columnIndex, cal);
    }

    public Date getDate(String columnName) throws SQLException {
        return getDate(findColumn(columnName));
    }

    public Date getDate(String columnName, Calendar cal) throws SQLException {
        return getDate(findColumn(columnName), cal);
    }

    private final Date getDateFromString(String stringVal, int columnIndex, Calendar targetCalendar) throws SQLException {
        SQLException e;
        int month;
        Exception e2;
        int day;
        SQLException sqlEx;
        Calendar calendar = targetCalendar;
        int month2 = 0;
        String stringVal2;
        int year;
        int year2;
        try {
            this.wasNullFlag = false;
            if (stringVal == null) {
                r1.wasNullFlag = true;
                return null;
            }
            stringVal2 = stringVal.trim();
            try {
                int dec = stringVal2.indexOf(".");
                if (dec > -1) {
                    stringVal2 = stringVal2.substring(0, dec);
                }
                if (!(stringVal2.equals("0") || stringVal2.equals("0000-00-00") || stringVal2.equals("0000-00-00 00:00:00") || stringVal2.equals("00000000000000"))) {
                    if (!stringVal2.equals("0")) {
                        if (r1.fields[columnIndex - 1].getMysqlType() == 7) {
                            int length = stringVal2.length();
                            if (length == 2) {
                                year = Integer.parseInt(stringVal2.substring(0, 2));
                                if (year <= 69) {
                                    year += 100;
                                }
                                return fastDateCreate(calendar, year + MysqlErrorNumbers.ER_SLAVE_SQL_THREAD_MUST_STOP, 1, 1);
                            } else if (length != 4) {
                                if (length != 6) {
                                    if (length != 8) {
                                        if (!(length == 10 || length == 12)) {
                                            if (length != 14) {
                                                if (length == 19 || length == 21) {
                                                    return fastDateCreate(calendar, Integer.parseInt(stringVal2.substring(0, 4)), Integer.parseInt(stringVal2.substring(5, 7)), Integer.parseInt(stringVal2.substring(8, 10)));
                                                }
                                                throw SQLError.createSQLException(Messages.getString("ResultSet.Bad_format_for_Date", new Object[]{stringVal2, Integer.valueOf(columnIndex)}), SQLError.SQL_STATE_ILLEGAL_ARGUMENT, getExceptionInterceptor());
                                            }
                                        }
                                    }
                                    return fastDateCreate(calendar, Integer.parseInt(stringVal2.substring(0, 4)), Integer.parseInt(stringVal2.substring(4, 6)), Integer.parseInt(stringVal2.substring(6, 8)));
                                }
                                year = Integer.parseInt(stringVal2.substring(0, 2));
                                if (year <= 69) {
                                    year += 100;
                                }
                                return fastDateCreate(calendar, year + MysqlErrorNumbers.ER_SLAVE_SQL_THREAD_MUST_STOP, Integer.parseInt(stringVal2.substring(2, 4)), Integer.parseInt(stringVal2.substring(4, 6)));
                            } else {
                                year = Integer.parseInt(stringVal2.substring(0, 4));
                                if (year <= 69) {
                                    year += 100;
                                }
                                return fastDateCreate(calendar, year + MysqlErrorNumbers.ER_SLAVE_SQL_THREAD_MUST_STOP, Integer.parseInt(stringVal2.substring(2, 4)), 1);
                            }
                        } else if (r1.fields[columnIndex - 1].getMysqlType() == 13) {
                            if (stringVal2.length() != 2) {
                                if (stringVal2.length() != 1) {
                                    year = Integer.parseInt(stringVal2.substring(0, 4));
                                    return fastDateCreate(calendar, year, 1, 1);
                                }
                            }
                            year = Integer.parseInt(stringVal2);
                            if (year <= 69) {
                                year += 100;
                            }
                            year += MysqlErrorNumbers.ER_SLAVE_SQL_THREAD_MUST_STOP;
                            return fastDateCreate(calendar, year, 1, 1);
                        } else if (r1.fields[columnIndex - 1].getMysqlType() == 11) {
                            return fastDateCreate(calendar, 1970, 1, 1);
                        } else {
                            if (stringVal2.length() >= 10) {
                                if (stringVal2.length() != 18) {
                                    year = Integer.parseInt(stringVal2.substring(0, 4));
                                    year2 = Integer.parseInt(stringVal2.substring(5, 7));
                                    month2 = Integer.parseInt(stringVal2.substring(8, 10));
                                } else {
                                    StringTokenizer st = new StringTokenizer(stringVal2, "- ");
                                    year = Integer.parseInt(st.nextToken());
                                    year2 = Integer.parseInt(st.nextToken());
                                    month2 = Integer.parseInt(st.nextToken());
                                }
                                return fastDateCreate(calendar, year, year2, month2);
                            } else if (stringVal2.length() == 8) {
                                return fastDateCreate(calendar, 1970, 1, 1);
                            } else {
                                throw SQLError.createSQLException(Messages.getString("ResultSet.Bad_format_for_Date", new Object[]{stringVal2, Integer.valueOf(columnIndex)}), SQLError.SQL_STATE_ILLEGAL_ARGUMENT, getExceptionInterceptor());
                            }
                        }
                    }
                }
                if ("convertToNull".equals(r1.connection.getZeroDateTimeBehavior())) {
                    r1.wasNullFlag = true;
                    return null;
                } else if (!"exception".equals(r1.connection.getZeroDateTimeBehavior())) {
                    return fastDateCreate(calendar, 1, 1, 1);
                } else {
                    StringBuilder stringBuilder = new StringBuilder();
                    stringBuilder.append("Value '");
                    stringBuilder.append(stringVal2);
                    stringBuilder.append("' can not be represented as java.sql.Date");
                    throw SQLError.createSQLException(stringBuilder.toString(), SQLError.SQL_STATE_ILLEGAL_ARGUMENT, getExceptionInterceptor());
                }
            } catch (SQLException e3) {
                e = e3;
                month = 0;
                year2 = 0;
                throw e;
            } catch (Exception e4) {
                e2 = e4;
                day = month2;
                month2 = 0;
                year2 = 0;
                year = e2;
                sqlEx = SQLError.createSQLException(Messages.getString("ResultSet.Bad_format_for_Date", new Object[]{stringVal2, Integer.valueOf(columnIndex)}), SQLError.SQL_STATE_ILLEGAL_ARGUMENT, getExceptionInterceptor());
                sqlEx.initCause(year);
                throw sqlEx;
            }
        } catch (SQLException e5) {
            e = e5;
            stringVal2 = stringVal;
            month = 0;
            year2 = 0;
            throw e;
        } catch (Exception e6) {
            e2 = e6;
            stringVal2 = stringVal;
            day = month2;
            month2 = 0;
            year2 = 0;
            year = e2;
            sqlEx = SQLError.createSQLException(Messages.getString("ResultSet.Bad_format_for_Date", new Object[]{stringVal2, Integer.valueOf(columnIndex)}), SQLError.SQL_STATE_ILLEGAL_ARGUMENT, getExceptionInterceptor());
            sqlEx.initCause(year);
            throw sqlEx;
        }
    }

    private TimeZone getDefaultTimeZone() {
        return this.useLegacyDatetimeCode ? this.connection.getDefaultTimeZone() : this.serverTimeZoneTz;
    }

    public double getDouble(int columnIndex) throws SQLException {
        if (this.isBinaryEncoded) {
            return getNativeDouble(columnIndex);
        }
        return getDoubleInternal(columnIndex);
    }

    public double getDouble(String columnName) throws SQLException {
        return getDouble(findColumn(columnName));
    }

    private final double getDoubleFromString(String stringVal, int columnIndex) throws SQLException {
        return getDoubleInternal(stringVal, columnIndex);
    }

    protected double getDoubleInternal(int colIndex) throws SQLException {
        return getDoubleInternal(getString(colIndex), colIndex);
    }

    protected double getDoubleInternal(String stringVal, int colIndex) throws SQLException {
        if (stringVal == null) {
            return 0.0d;
        }
        try {
            if (stringVal.length() == 0) {
                return (double) convertToZeroWithEmptyCheck();
            }
            double d = Double.parseDouble(stringVal);
            if (this.useStrictFloatingPoint) {
                if (d == 2.147483648E9d) {
                    d = 2.147483647E9d;
                } else if (d == 1.0000000036275E-15d) {
                    d = 1.0E-15d;
                } else if (d == 9.999999869911E14d) {
                    d = 9.99999999999999E14d;
                } else if (d == 1.4012984643248E-45d) {
                    d = 1.4E-45d;
                } else if (d == 1.4013E-45d) {
                    d = 1.4E-45d;
                } else if (d == 3.4028234663853E37d) {
                    d = 3.4028235E37d;
                } else if (d == -2.14748E9d) {
                    d = -2.147483648E9d;
                } else if (d == 3.40282E37d) {
                    d = 3.4028235E37d;
                }
            }
            return d;
        } catch (NumberFormatException e) {
            if (this.fields[colIndex - 1].getMysqlType() == 16) {
                return (double) getNumericRepresentationOfSQLBitType(colIndex);
            }
            throw SQLError.createSQLException(Messages.getString("ResultSet.Bad_format_for_number", new Object[]{stringVal, Integer.valueOf(colIndex)}), SQLError.SQL_STATE_ILLEGAL_ARGUMENT, getExceptionInterceptor());
        }
    }

    public int getFetchDirection() throws SQLException {
        int i;
        synchronized (checkClosed().getConnectionMutex()) {
            i = this.fetchDirection;
        }
        return i;
    }

    public int getFetchSize() throws SQLException {
        int i;
        synchronized (checkClosed().getConnectionMutex()) {
            i = this.fetchSize;
        }
        return i;
    }

    public char getFirstCharOfQuery() {
        try {
            char c;
            synchronized (checkClosed().getConnectionMutex()) {
                c = this.firstCharOfQuery;
            }
            return c;
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    public float getFloat(int columnIndex) throws SQLException {
        if (this.isBinaryEncoded) {
            return getNativeFloat(columnIndex);
        }
        return getFloatFromString(getString(columnIndex), columnIndex);
    }

    public float getFloat(String columnName) throws SQLException {
        return getFloat(findColumn(columnName));
    }

    private final float getFloatFromString(String val, int columnIndex) throws SQLException {
        if (val == null) {
            return 0.0f;
        }
        try {
            if (val.length() == 0) {
                return (float) convertToZeroWithEmptyCheck();
            }
            float f = Float.parseFloat(val);
            if (this.jdbcCompliantTruncationForReads && (f == Float.MIN_VALUE || f == Float.MAX_VALUE)) {
                double valAsDouble = Double.parseDouble(val);
                if (valAsDouble < 1.401298464324817E-45d - MIN_DIFF_PREC || valAsDouble > 3.4028234663852886E38d - MAX_DIFF_PREC) {
                    throwRangeException(String.valueOf(valAsDouble), columnIndex, 6);
                }
            }
            return f;
        } catch (NumberFormatException e) {
            try {
                Double valueAsDouble = new Double(val);
                float valueAsFloat = valueAsDouble.floatValue();
                if (this.jdbcCompliantTruncationForReads && ((this.jdbcCompliantTruncationForReads && valueAsFloat == Float.NEGATIVE_INFINITY) || valueAsFloat == Float.POSITIVE_INFINITY)) {
                    throwRangeException(valueAsDouble.toString(), columnIndex, 6);
                }
                return valueAsFloat;
            } catch (NumberFormatException e2) {
                StringBuilder stringBuilder = new StringBuilder();
                stringBuilder.append(Messages.getString("ResultSet.Invalid_value_for_getFloat()_-____200"));
                stringBuilder.append(val);
                stringBuilder.append(Messages.getString("ResultSet.___in_column__201"));
                stringBuilder.append(columnIndex);
                throw SQLError.createSQLException(stringBuilder.toString(), SQLError.SQL_STATE_ILLEGAL_ARGUMENT, getExceptionInterceptor());
            }
        }
    }

    public int getInt(int columnIndex) throws SQLException {
        checkRowPos();
        checkColumnBounds(columnIndex);
        if (this.isBinaryEncoded) {
            return getNativeInt(columnIndex);
        }
        int columnIndexMinusOne = columnIndex - 1;
        if (this.thisRow.isNull(columnIndexMinusOne)) {
            this.wasNullFlag = true;
            return 0;
        }
        this.wasNullFlag = false;
        if (this.fields[columnIndexMinusOne].getMysqlType() == 16) {
            long valueAsLong = getNumericRepresentationOfSQLBitType(columnIndex);
            if (this.jdbcCompliantTruncationForReads && (valueAsLong < -2147483648L || valueAsLong > 2147483647L)) {
                throwRangeException(String.valueOf(valueAsLong), columnIndex, 4);
            }
            return (int) valueAsLong;
        }
        if (this.useFastIntParsing) {
            if (this.thisRow.length(columnIndexMinusOne) == 0) {
                return convertToZeroWithEmptyCheck();
            }
            if (!this.thisRow.isFloatingPointNumber(columnIndexMinusOne)) {
                try {
                    return getIntWithOverflowCheck(columnIndexMinusOne);
                } catch (NumberFormatException e) {
                    try {
                        return parseIntAsDouble(columnIndex, this.thisRow.getString(columnIndexMinusOne, this.fields[columnIndexMinusOne].getEncoding(), this.connection));
                    } catch (NumberFormatException e2) {
                        StringBuilder stringBuilder = new StringBuilder();
                        stringBuilder.append(Messages.getString("ResultSet.Invalid_value_for_getInt()_-____74"));
                        stringBuilder.append(this.thisRow.getString(columnIndexMinusOne, this.fields[columnIndexMinusOne].getEncoding(), this.connection));
                        stringBuilder.append("'");
                        throw SQLError.createSQLException(stringBuilder.toString(), SQLError.SQL_STATE_ILLEGAL_ARGUMENT, getExceptionInterceptor());
                    }
                }
            }
        }
        String val = null;
        try {
            val = getString(columnIndex);
            if (val == null) {
                return 0;
            }
            if (val.length() == 0) {
                return convertToZeroWithEmptyCheck();
            }
            int intVal;
            if (val.indexOf("e") == -1 && val.indexOf("E") == -1 && val.indexOf(".") == -1) {
                intVal = Integer.parseInt(val);
                checkForIntegerTruncation(columnIndexMinusOne, null, intVal);
                return intVal;
            }
            intVal = parseIntAsDouble(columnIndex, val);
            checkForIntegerTruncation(columnIndex, null, intVal);
            return intVal;
        } catch (NumberFormatException e3) {
            try {
                return parseIntAsDouble(columnIndex, val);
            } catch (NumberFormatException e4) {
                StringBuilder stringBuilder2 = new StringBuilder();
                stringBuilder2.append(Messages.getString("ResultSet.Invalid_value_for_getInt()_-____74"));
                stringBuilder2.append(val);
                stringBuilder2.append("'");
                throw SQLError.createSQLException(stringBuilder2.toString(), SQLError.SQL_STATE_ILLEGAL_ARGUMENT, getExceptionInterceptor());
            }
        }
    }

    public int getInt(String columnName) throws SQLException {
        return getInt(findColumn(columnName));
    }

    private final int getIntFromString(String val, int columnIndex) throws SQLException {
        if (val == null) {
            return 0;
        }
        try {
            if (val.length() == 0) {
                return convertToZeroWithEmptyCheck();
            }
            if (val.indexOf("e") == -1 && val.indexOf("E") == -1 && val.indexOf(".") == -1) {
                val = val.trim();
                int valueAsInt = Integer.parseInt(val);
                if (this.jdbcCompliantTruncationForReads && (valueAsInt == Integer.MIN_VALUE || valueAsInt == ActivityChooserViewAdapter.MAX_ACTIVITY_COUNT_UNLIMITED)) {
                    long valueAsLong = Long.parseLong(val);
                    if (valueAsLong < -2147483648L || valueAsLong > 2147483647L) {
                        throwRangeException(String.valueOf(valueAsLong), columnIndex, 4);
                    }
                }
                return valueAsInt;
            }
            double valueAsDouble = Double.parseDouble(val);
            if (this.jdbcCompliantTruncationForReads && (valueAsDouble < -2.147483648E9d || valueAsDouble > 2.147483647E9d)) {
                throwRangeException(String.valueOf(valueAsDouble), columnIndex, 4);
            }
            return (int) valueAsDouble;
        } catch (NumberFormatException e) {
            try {
                double valueAsDouble2 = Double.parseDouble(val);
                if (this.jdbcCompliantTruncationForReads && (valueAsDouble2 < -2.147483648E9d || valueAsDouble2 > 2.147483647E9d)) {
                    throwRangeException(String.valueOf(valueAsDouble2), columnIndex, 4);
                }
                return (int) valueAsDouble2;
            } catch (NumberFormatException e2) {
                StringBuilder stringBuilder = new StringBuilder();
                stringBuilder.append(Messages.getString("ResultSet.Invalid_value_for_getInt()_-____206"));
                stringBuilder.append(val);
                stringBuilder.append(Messages.getString("ResultSet.___in_column__207"));
                stringBuilder.append(columnIndex);
                throw SQLError.createSQLException(stringBuilder.toString(), SQLError.SQL_STATE_ILLEGAL_ARGUMENT, getExceptionInterceptor());
            }
        }
    }

    public long getLong(int columnIndex) throws SQLException {
        return getLong(columnIndex, true);
    }

    private long getLong(int columnIndex, boolean overflowCheck) throws SQLException {
        checkRowPos();
        checkColumnBounds(columnIndex);
        if (this.isBinaryEncoded) {
            return getNativeLong(columnIndex, overflowCheck, true);
        }
        int columnIndexMinusOne = columnIndex - 1;
        if (this.thisRow.isNull(columnIndexMinusOne)) {
            this.wasNullFlag = true;
            return 0;
        }
        this.wasNullFlag = false;
        if (this.fields[columnIndexMinusOne].getMysqlType() == 16) {
            return getNumericRepresentationOfSQLBitType(columnIndex);
        }
        if (this.useFastIntParsing) {
            if (this.thisRow.length(columnIndexMinusOne) == 0) {
                return (long) convertToZeroWithEmptyCheck();
            }
            if (!this.thisRow.isFloatingPointNumber(columnIndexMinusOne)) {
                try {
                    return getLongWithOverflowCheck(columnIndexMinusOne, overflowCheck);
                } catch (NumberFormatException e) {
                    try {
                        return parseLongAsDouble(columnIndexMinusOne, this.thisRow.getString(columnIndexMinusOne, this.fields[columnIndexMinusOne].getEncoding(), this.connection));
                    } catch (NumberFormatException e2) {
                        StringBuilder stringBuilder = new StringBuilder();
                        stringBuilder.append(Messages.getString("ResultSet.Invalid_value_for_getLong()_-____79"));
                        stringBuilder.append(this.thisRow.getString(columnIndexMinusOne, this.fields[columnIndexMinusOne].getEncoding(), this.connection));
                        stringBuilder.append("'");
                        throw SQLError.createSQLException(stringBuilder.toString(), SQLError.SQL_STATE_ILLEGAL_ARGUMENT, getExceptionInterceptor());
                    }
                }
            }
        }
        String val = null;
        try {
            val = getString(columnIndex);
            if (val == null) {
                return 0;
            }
            if (val.length() == 0) {
                return (long) convertToZeroWithEmptyCheck();
            }
            if (val.indexOf("e") == -1 && val.indexOf("E") == -1) {
                return parseLongWithOverflowCheck(columnIndexMinusOne, null, val, overflowCheck);
            }
            return parseLongAsDouble(columnIndexMinusOne, val);
        } catch (NumberFormatException e3) {
            try {
                return parseLongAsDouble(columnIndexMinusOne, val);
            } catch (NumberFormatException e4) {
                stringBuilder = new StringBuilder();
                stringBuilder.append(Messages.getString("ResultSet.Invalid_value_for_getLong()_-____79"));
                stringBuilder.append(val);
                stringBuilder.append("'");
                throw SQLError.createSQLException(stringBuilder.toString(), SQLError.SQL_STATE_ILLEGAL_ARGUMENT, getExceptionInterceptor());
            }
        }
    }

    public long getLong(String columnName) throws SQLException {
        return getLong(findColumn(columnName));
    }

    private final long getLongFromString(String val, int columnIndexZeroBased) throws SQLException {
        if (val == null) {
            return 0;
        }
        try {
            if (val.length() == 0) {
                return (long) convertToZeroWithEmptyCheck();
            }
            if (val.indexOf("e") == -1 && val.indexOf("E") == -1) {
                return parseLongWithOverflowCheck(columnIndexZeroBased, null, val, true);
            }
            return parseLongAsDouble(columnIndexZeroBased, val);
        } catch (NumberFormatException e) {
            try {
                return parseLongAsDouble(columnIndexZeroBased, val);
            } catch (NumberFormatException e2) {
                StringBuilder stringBuilder = new StringBuilder();
                stringBuilder.append(Messages.getString("ResultSet.Invalid_value_for_getLong()_-____211"));
                stringBuilder.append(val);
                stringBuilder.append(Messages.getString("ResultSet.___in_column__212"));
                stringBuilder.append(columnIndexZeroBased + 1);
                throw SQLError.createSQLException(stringBuilder.toString(), SQLError.SQL_STATE_ILLEGAL_ARGUMENT, getExceptionInterceptor());
            }
        }
    }

    public ResultSetMetaData getMetaData() throws SQLException {
        checkClosed();
        return new ResultSetMetaData(this.fields, this.connection.getUseOldAliasMetadataBehavior(), this.connection.getYearIsDateType(), getExceptionInterceptor());
    }

    protected Array getNativeArray(int i) throws SQLException {
        throw SQLError.createSQLFeatureNotSupportedException();
    }

    protected InputStream getNativeAsciiStream(int columnIndex) throws SQLException {
        checkRowPos();
        return getNativeBinaryStream(columnIndex);
    }

    protected BigDecimal getNativeBigDecimal(int columnIndex) throws SQLException {
        checkColumnBounds(columnIndex);
        return getNativeBigDecimal(columnIndex, this.fields[columnIndex - 1].getDecimals());
    }

    protected BigDecimal getNativeBigDecimal(int columnIndex, int scale) throws SQLException {
        checkColumnBounds(columnIndex);
        Field f = this.fields[columnIndex - 1];
        Object value = this.thisRow.getColumnValue(columnIndex - 1);
        if (value == null) {
            this.wasNullFlag = true;
            return null;
        }
        String stringVal;
        this.wasNullFlag = false;
        switch (f.getSQLType()) {
            case 2:
            case 3:
                stringVal = StringUtils.toAsciiString((byte[]) value);
                break;
            default:
                stringVal = getNativeString(columnIndex);
                break;
        }
        return getBigDecimalFromString(stringVal, columnIndex, scale);
    }

    protected InputStream getNativeBinaryStream(int columnIndex) throws SQLException {
        checkRowPos();
        int columnIndexMinusOne = columnIndex - 1;
        if (this.thisRow.isNull(columnIndexMinusOne)) {
            this.wasNullFlag = true;
            return null;
        }
        this.wasNullFlag = false;
        int sQLType = this.fields[columnIndexMinusOne].getSQLType();
        if (!(sQLType == -7 || sQLType == 2004)) {
            switch (sQLType) {
                case FontRequestCallback.FAIL_REASON_SECURITY_VIOLATION /*-4*/:
                case FontRequestCallback.FAIL_REASON_FONT_LOAD_ERROR /*-3*/:
                case -2:
                    break;
                default:
                    byte[] b = getNativeBytes(columnIndex, false);
                    if (b != null) {
                        return new ByteArrayInputStream(b);
                    }
                    return null;
            }
        }
        return this.thisRow.getBinaryInputStream(columnIndexMinusOne);
    }

    protected Blob getNativeBlob(int columnIndex) throws SQLException {
        checkRowPos();
        checkColumnBounds(columnIndex);
        Object value = this.thisRow.getColumnValue(columnIndex - 1);
        if (value == null) {
            this.wasNullFlag = true;
        } else {
            this.wasNullFlag = false;
        }
        if (this.wasNullFlag) {
            return null;
        }
        byte[] dataAsBytes;
        switch (this.fields[columnIndex - 1].getMysqlType()) {
            case 249:
            case Callback.DEFAULT_SWIPE_ANIMATION_DURATION /*250*/:
            case 251:
            case MysqlDefs.FIELD_TYPE_BLOB /*252*/:
                dataAsBytes = (byte[]) value;
                break;
            default:
                dataAsBytes = getNativeBytes(columnIndex, false);
                break;
        }
        if (this.connection.getEmulateLocators()) {
            return new BlobFromLocator(this, columnIndex, getExceptionInterceptor());
        }
        return new Blob(dataAsBytes, getExceptionInterceptor());
    }

    public static boolean arraysEqual(byte[] left, byte[] right) {
        boolean z = true;
        if (left == null) {
            if (right != null) {
                z = false;
            }
            return z;
        } else if (right == null || left.length != right.length) {
            return false;
        } else {
            for (int i = 0; i < left.length; i++) {
                if (left[i] != right[i]) {
                    return false;
                }
            }
            return true;
        }
    }

    protected byte getNativeByte(int columnIndex) throws SQLException {
        return getNativeByte(columnIndex, true);
    }

    /* JADX WARNING: inconsistent code. */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    protected byte getNativeByte(int r16, boolean r17) throws java.sql.SQLException {
        /*
        r15 = this;
        r6 = r15;
        r6.checkRowPos();
        r15.checkColumnBounds(r16);
        r1 = r6.thisRow;
        r2 = r16 + -1;
        r7 = r1.getColumnValue(r2);
        r1 = 0;
        r2 = 1;
        if (r7 != 0) goto L_0x0016;
    L_0x0013:
        r6.wasNullFlag = r2;
        return r1;
    L_0x0016:
        r6.wasNullFlag = r1;
        r8 = r16 + -1;
        r0 = r6.fields;
        r9 = r0[r8];
        r0 = r9.getMysqlType();
        r3 = 13;
        r4 = -128; // 0xffffffffffffff80 float:NaN double:NaN;
        r5 = 127; // 0x7f float:1.78E-43 double:6.27E-322;
        r10 = -6;
        if (r0 == r3) goto L_0x012f;
    L_0x002b:
        r3 = 16;
        r11 = 127; // 0x7f float:1.78E-43 double:6.27E-322;
        r13 = -128; // 0xffffffffffffff80 float:NaN double:NaN;
        if (r0 == r3) goto L_0x010f;
    L_0x0033:
        switch(r0) {
            case 1: goto L_0x00e9;
            case 2: goto L_0x012f;
            case 3: goto L_0x00ce;
            case 4: goto L_0x00aa;
            case 5: goto L_0x0083;
            default: goto L_0x0036;
        };
    L_0x0036:
        switch(r0) {
            case 8: goto L_0x0063;
            case 9: goto L_0x00ce;
            default: goto L_0x0039;
        };
    L_0x0039:
        r0 = r6.useUsageAdvisor;
        if (r0 == 0) goto L_0x0056;
    L_0x003d:
        r1 = "getByte()";
        r0 = r6.thisRow;
        r2 = r8 + -1;
        r3 = r0.getColumnValue(r2);
        r0 = r6.fields;
        r4 = r0[r8];
        r0 = 6;
        r5 = new int[r0];
        r5 = {5, 1, 2, 3, 8, 4};
        r0 = r6;
        r2 = r8;
        r0.issueConversionViaParsingWarning(r1, r2, r3, r4, r5);
    L_0x0056:
        r0 = r8 + 1;
        r0 = r6.getNativeString(r0);
        r1 = r8 + 1;
        r0 = r6.getByteFromString(r0, r1);
        return r0;
    L_0x0063:
        r0 = r8 + 1;
        r0 = r6.getNativeLong(r0, r1, r2);
        if (r17 == 0) goto L_0x0080;
    L_0x006b:
        r2 = r6.jdbcCompliantTruncationForReads;
        if (r2 == 0) goto L_0x0080;
    L_0x006f:
        r2 = (r0 > r13 ? 1 : (r0 == r13 ? 0 : -1));
        if (r2 < 0) goto L_0x0077;
    L_0x0073:
        r2 = (r0 > r11 ? 1 : (r0 == r11 ? 0 : -1));
        if (r2 <= 0) goto L_0x0080;
    L_0x0077:
        r2 = java.lang.String.valueOf(r0);
        r3 = r8 + 1;
        r6.throwRangeException(r2, r3, r10);
    L_0x0080:
        r2 = (int) r0;
        r2 = (byte) r2;
        return r2;
    L_0x0083:
        r0 = r8 + 1;
        r0 = r6.getNativeDouble(r0);
        if (r17 == 0) goto L_0x00a7;
    L_0x008b:
        r2 = r6.jdbcCompliantTruncationForReads;
        if (r2 == 0) goto L_0x00a7;
    L_0x008f:
        r2 = -4584664420663164928; // 0xc060000000000000 float:0.0 double:-128.0;
        r4 = (r0 > r2 ? 1 : (r0 == r2 ? 0 : -1));
        if (r4 < 0) goto L_0x009e;
    L_0x0095:
        r2 = 4638637247447433216; // 0x405fc00000000000 float:0.0 double:127.0;
        r4 = (r0 > r2 ? 1 : (r0 == r2 ? 0 : -1));
        if (r4 <= 0) goto L_0x00a7;
    L_0x009e:
        r2 = java.lang.String.valueOf(r0);
        r3 = r8 + 1;
        r6.throwRangeException(r2, r3, r10);
    L_0x00a7:
        r2 = (int) r0;
        r2 = (byte) r2;
        return r2;
    L_0x00aa:
        r0 = r8 + 1;
        r0 = r6.getNativeFloat(r0);
        if (r17 == 0) goto L_0x00cb;
    L_0x00b2:
        r1 = r6.jdbcCompliantTruncationForReads;
        if (r1 == 0) goto L_0x00cb;
    L_0x00b6:
        r1 = -1023410176; // 0xffffffffc3000000 float:-128.0 double:NaN;
        r1 = (r0 > r1 ? 1 : (r0 == r1 ? 0 : -1));
        if (r1 < 0) goto L_0x00c2;
    L_0x00bc:
        r1 = 1123942400; // 0x42fe0000 float:127.0 double:5.553013277E-315;
        r1 = (r0 > r1 ? 1 : (r0 == r1 ? 0 : -1));
        if (r1 <= 0) goto L_0x00cb;
    L_0x00c2:
        r1 = java.lang.String.valueOf(r0);
        r2 = r8 + 1;
        r6.throwRangeException(r1, r2, r10);
    L_0x00cb:
        r1 = (int) r0;
        r1 = (byte) r1;
        return r1;
    L_0x00ce:
        r0 = r8 + 1;
        r0 = r6.getNativeInt(r0, r1);
        if (r17 == 0) goto L_0x00e7;
    L_0x00d6:
        r1 = r6.jdbcCompliantTruncationForReads;
        if (r1 == 0) goto L_0x00e7;
    L_0x00da:
        if (r0 < r4) goto L_0x00de;
    L_0x00dc:
        if (r0 <= r5) goto L_0x00e7;
    L_0x00de:
        r1 = java.lang.String.valueOf(r0);
        r2 = r8 + 1;
        r6.throwRangeException(r1, r2, r10);
    L_0x00e7:
        r1 = (byte) r0;
        return r1;
    L_0x00e9:
        r0 = r7;
        r0 = (byte[]) r0;
        r0 = r0[r1];
        r1 = r9.isUnsigned();
        if (r1 != 0) goto L_0x00f5;
    L_0x00f4:
        return r0;
    L_0x00f5:
        if (r0 < 0) goto L_0x00f9;
    L_0x00f7:
        r1 = (short) r0;
        goto L_0x00fc;
    L_0x00f9:
        r1 = r0 + 256;
        r1 = (short) r1;
    L_0x00fc:
        if (r17 == 0) goto L_0x010d;
    L_0x00fe:
        r2 = r6.jdbcCompliantTruncationForReads;
        if (r2 == 0) goto L_0x010d;
    L_0x0102:
        if (r1 <= r5) goto L_0x010d;
    L_0x0104:
        r2 = java.lang.String.valueOf(r1);
        r3 = r8 + 1;
        r6.throwRangeException(r2, r3, r10);
    L_0x010d:
        r2 = (byte) r1;
        return r2;
    L_0x010f:
        r0 = r8 + 1;
        r0 = r6.getNumericRepresentationOfSQLBitType(r0);
        if (r17 == 0) goto L_0x012c;
    L_0x0117:
        r2 = r6.jdbcCompliantTruncationForReads;
        if (r2 == 0) goto L_0x012c;
    L_0x011b:
        r2 = (r0 > r13 ? 1 : (r0 == r13 ? 0 : -1));
        if (r2 < 0) goto L_0x0123;
    L_0x011f:
        r2 = (r0 > r11 ? 1 : (r0 == r11 ? 0 : -1));
        if (r2 <= 0) goto L_0x012c;
    L_0x0123:
        r2 = java.lang.String.valueOf(r0);
        r3 = r8 + 1;
        r6.throwRangeException(r2, r3, r10);
    L_0x012c:
        r2 = (int) r0;
        r2 = (byte) r2;
        return r2;
    L_0x012f:
        r0 = r8 + 1;
        r0 = r6.getNativeShort(r0);
        if (r17 == 0) goto L_0x0148;
    L_0x0137:
        r1 = r6.jdbcCompliantTruncationForReads;
        if (r1 == 0) goto L_0x0148;
    L_0x013b:
        if (r0 < r4) goto L_0x013f;
    L_0x013d:
        if (r0 <= r5) goto L_0x0148;
    L_0x013f:
        r1 = java.lang.String.valueOf(r0);
        r2 = r8 + 1;
        r6.throwRangeException(r1, r2, r10);
    L_0x0148:
        r1 = (byte) r0;
        return r1;
        */
        throw new UnsupportedOperationException("Method not decompiled: com.mysql.jdbc.ResultSetImpl.getNativeByte(int, boolean):byte");
    }

    /* JADX WARNING: inconsistent code. */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    protected byte[] getNativeBytes(int r6, boolean r7) throws java.sql.SQLException {
        /*
        r5 = this;
        r5.checkRowPos();
        r5.checkColumnBounds(r6);
        r0 = r5.thisRow;
        r1 = r6 + -1;
        r0 = r0.getColumnValue(r1);
        if (r0 != 0) goto L_0x0014;
    L_0x0010:
        r1 = 1;
        r5.wasNullFlag = r1;
        goto L_0x0017;
    L_0x0014:
        r1 = 0;
        r5.wasNullFlag = r1;
    L_0x0017:
        r1 = r5.wasNullFlag;
        if (r1 == 0) goto L_0x001d;
    L_0x001b:
        r1 = 0;
        return r1;
    L_0x001d:
        r1 = r5.fields;
        r2 = r6 + -1;
        r1 = r1[r2];
        r2 = r1.getMysqlType();
        if (r7 == 0) goto L_0x002b;
    L_0x0029:
        r2 = 252; // 0xfc float:3.53E-43 double:1.245E-321;
    L_0x002b:
        switch(r2) {
            case 15: goto L_0x0036;
            case 16: goto L_0x0032;
            default: goto L_0x002e;
        };
    L_0x002e:
        switch(r2) {
            case 249: goto L_0x0032;
            case 250: goto L_0x0032;
            case 251: goto L_0x0032;
            case 252: goto L_0x0032;
            case 253: goto L_0x0036;
            case 254: goto L_0x0036;
            default: goto L_0x0031;
        };
    L_0x0031:
        goto L_0x003e;
    L_0x0032:
        r3 = r0;
        r3 = (byte[]) r3;
        return r3;
    L_0x0036:
        r3 = r0 instanceof byte[];
        if (r3 == 0) goto L_0x003e;
    L_0x003a:
        r3 = r0;
        r3 = (byte[]) r3;
        return r3;
    L_0x003e:
        r3 = r1.getSQLType();
        r4 = -3;
        if (r3 == r4) goto L_0x0052;
    L_0x0045:
        r4 = -2;
        if (r3 != r4) goto L_0x0049;
    L_0x0048:
        goto L_0x0052;
    L_0x0049:
        r4 = r5.getNativeString(r6);
        r4 = r5.getBytesFromString(r4);
        return r4;
    L_0x0052:
        r4 = r0;
        r4 = (byte[]) r4;
        return r4;
        */
        throw new UnsupportedOperationException("Method not decompiled: com.mysql.jdbc.ResultSetImpl.getNativeBytes(int, boolean):byte[]");
    }

    protected Reader getNativeCharacterStream(int columnIndex) throws SQLException {
        int columnIndexMinusOne = columnIndex - 1;
        int sQLType = this.fields[columnIndexMinusOne].getSQLType();
        if (sQLType != -1 && sQLType != 1 && sQLType != 12 && sQLType != 2005) {
            String asString = getStringForClob(columnIndex);
            if (asString == null) {
                return null;
            }
            return getCharacterStreamFromString(asString);
        } else if (this.thisRow.isNull(columnIndexMinusOne)) {
            this.wasNullFlag = true;
            return null;
        } else {
            this.wasNullFlag = false;
            return this.thisRow.getReader(columnIndexMinusOne);
        }
    }

    protected Clob getNativeClob(int columnIndex) throws SQLException {
        String stringVal = getStringForClob(columnIndex);
        if (stringVal == null) {
            return null;
        }
        return getClobFromString(stringVal);
    }

    private java.lang.String getNativeConvertToString(int r12, com.mysql.jdbc.Field r13) throws java.sql.SQLException {
        /* JADX: method processing error */
/*
Error: jadx.core.utils.exceptions.JadxRuntimeException: Exception block dominator not found, method:com.mysql.jdbc.ResultSetImpl.getNativeConvertToString(int, com.mysql.jdbc.Field):java.lang.String. bs: [B:152:0x01b1, B:185:0x0214]
	at jadx.core.dex.visitors.regions.ProcessTryCatchRegions.searchTryCatchDominators(ProcessTryCatchRegions.java:86)
	at jadx.core.dex.visitors.regions.ProcessTryCatchRegions.process(ProcessTryCatchRegions.java:45)
	at jadx.core.dex.visitors.regions.RegionMakerVisitor.postProcessRegions(RegionMakerVisitor.java:63)
	at jadx.core.dex.visitors.regions.RegionMakerVisitor.visit(RegionMakerVisitor.java:58)
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
        r11 = this;
        r0 = r11.checkClosed();
        r0 = r0.getConnectionMutex();
        monitor-enter(r0);
        r1 = r13.getSQLType();	 Catch:{ all -> 0x02d1 }
        r2 = r13.getMysqlType();	 Catch:{ all -> 0x02d1 }
        r3 = 12;	 Catch:{ all -> 0x02d1 }
        if (r1 == r3) goto L_0x02cb;	 Catch:{ all -> 0x02d1 }
    L_0x0015:
        r3 = 16;	 Catch:{ all -> 0x02d1 }
        r4 = 0;	 Catch:{ all -> 0x02d1 }
        if (r1 == r3) goto L_0x02bb;	 Catch:{ all -> 0x02d1 }
    L_0x001a:
        r3 = 2;	 Catch:{ all -> 0x02d1 }
        r5 = 1;	 Catch:{ all -> 0x02d1 }
        r6 = 0;	 Catch:{ all -> 0x02d1 }
        switch(r1) {
            case -7: goto L_0x02b1;
            case -6: goto L_0x028f;
            case -5: goto L_0x0265;
            case -4: goto L_0x01de;
            case -3: goto L_0x01de;
            case -2: goto L_0x01de;
            case -1: goto L_0x02cb;
            default: goto L_0x0020;
        };	 Catch:{ all -> 0x02d1 }
    L_0x0020:
        switch(r1) {
            case 1: goto L_0x02cb;
            case 2: goto L_0x0190;
            case 3: goto L_0x0190;
            case 4: goto L_0x0161;
            case 5: goto L_0x013e;
            case 6: goto L_0x012e;
            case 7: goto L_0x011e;
            case 8: goto L_0x012e;
            default: goto L_0x0023;
        };	 Catch:{ all -> 0x02d1 }
    L_0x0023:
        r7 = 3;	 Catch:{ all -> 0x02d1 }
        switch(r1) {
            case 91: goto L_0x00a6;
            case 92: goto L_0x0092;
            case 93: goto L_0x002d;
            default: goto L_0x0027;
        };	 Catch:{ all -> 0x02d1 }
    L_0x0027:
        r3 = r11.extractStringFromNativeColumn(r12, r2);	 Catch:{ all -> 0x02d1 }
        monitor-exit(r0);	 Catch:{ all -> 0x02d1 }
        return r3;	 Catch:{ all -> 0x02d1 }
    L_0x002d:
        r8 = r11.connection;	 Catch:{ all -> 0x02d1 }
        r8 = r8.getNoDatetimeStringSync();	 Catch:{ all -> 0x02d1 }
        if (r8 == 0) goto L_0x005d;	 Catch:{ all -> 0x02d1 }
    L_0x0035:
        r8 = r11.getNativeBytes(r12, r5);	 Catch:{ all -> 0x02d1 }
        if (r8 != 0) goto L_0x003d;	 Catch:{ all -> 0x02d1 }
    L_0x003b:
        monitor-exit(r0);	 Catch:{ all -> 0x02d1 }
        return r4;	 Catch:{ all -> 0x02d1 }
    L_0x003d:
        r9 = r8.length;	 Catch:{ all -> 0x02d1 }
        if (r9 != 0) goto L_0x0044;	 Catch:{ all -> 0x02d1 }
    L_0x0040:
        r3 = "0000-00-00 00:00:00";	 Catch:{ all -> 0x02d1 }
        monitor-exit(r0);	 Catch:{ all -> 0x02d1 }
        return r3;	 Catch:{ all -> 0x02d1 }
    L_0x0044:
        r9 = r8[r6];	 Catch:{ all -> 0x02d1 }
        r9 = r9 & 255;	 Catch:{ all -> 0x02d1 }
        r5 = r8[r5];	 Catch:{ all -> 0x02d1 }
        r5 = r5 & 255;	 Catch:{ all -> 0x02d1 }
        r5 = r5 << 8;	 Catch:{ all -> 0x02d1 }
        r5 = r5 | r9;	 Catch:{ all -> 0x02d1 }
        r9 = r8[r3];	 Catch:{ all -> 0x02d1 }
        r7 = r8[r7];	 Catch:{ all -> 0x02d1 }
        if (r5 != 0) goto L_0x005d;	 Catch:{ all -> 0x02d1 }
    L_0x0055:
        if (r9 != 0) goto L_0x005d;	 Catch:{ all -> 0x02d1 }
    L_0x0057:
        if (r7 != 0) goto L_0x005d;	 Catch:{ all -> 0x02d1 }
    L_0x0059:
        r3 = "0000-00-00 00:00:00";	 Catch:{ all -> 0x02d1 }
        monitor-exit(r0);	 Catch:{ all -> 0x02d1 }
        return r3;	 Catch:{ all -> 0x02d1 }
    L_0x005d:
        r5 = r11.connection;	 Catch:{ all -> 0x02d1 }
        r5 = r5.getDefaultTimeZone();	 Catch:{ all -> 0x02d1 }
        r5 = r11.getNativeTimestamp(r12, r4, r5, r6);	 Catch:{ all -> 0x02d1 }
        if (r5 != 0) goto L_0x006b;	 Catch:{ all -> 0x02d1 }
    L_0x0069:
        monitor-exit(r0);	 Catch:{ all -> 0x02d1 }
        return r4;	 Catch:{ all -> 0x02d1 }
    L_0x006b:
        r4 = java.lang.String.valueOf(r5);	 Catch:{ all -> 0x02d1 }
        r7 = r11.connection;	 Catch:{ all -> 0x02d1 }
        r7 = r7.getNoDatetimeStringSync();	 Catch:{ all -> 0x02d1 }
        if (r7 != 0) goto L_0x0079;	 Catch:{ all -> 0x02d1 }
    L_0x0077:
        monitor-exit(r0);	 Catch:{ all -> 0x02d1 }
        return r4;	 Catch:{ all -> 0x02d1 }
    L_0x0079:
        r7 = ".0";	 Catch:{ all -> 0x02d1 }
        r7 = r4.endsWith(r7);	 Catch:{ all -> 0x02d1 }
        if (r7 == 0) goto L_0x008c;	 Catch:{ all -> 0x02d1 }
    L_0x0081:
        r7 = r4.length();	 Catch:{ all -> 0x02d1 }
        r7 = r7 - r3;	 Catch:{ all -> 0x02d1 }
        r3 = r4.substring(r6, r7);	 Catch:{ all -> 0x02d1 }
        monitor-exit(r0);	 Catch:{ all -> 0x02d1 }
        return r3;	 Catch:{ all -> 0x02d1 }
    L_0x008c:
        r3 = r11.extractStringFromNativeColumn(r12, r2);	 Catch:{ all -> 0x02d1 }
        monitor-exit(r0);	 Catch:{ all -> 0x02d1 }
        return r3;	 Catch:{ all -> 0x02d1 }
    L_0x0092:
        r3 = r11.connection;	 Catch:{ all -> 0x02d1 }
        r3 = r3.getDefaultTimeZone();	 Catch:{ all -> 0x02d1 }
        r3 = r11.getNativeTime(r12, r4, r3, r6);	 Catch:{ all -> 0x02d1 }
        if (r3 != 0) goto L_0x00a0;	 Catch:{ all -> 0x02d1 }
    L_0x009e:
        monitor-exit(r0);	 Catch:{ all -> 0x02d1 }
        return r4;	 Catch:{ all -> 0x02d1 }
    L_0x00a0:
        r4 = java.lang.String.valueOf(r3);	 Catch:{ all -> 0x02d1 }
        monitor-exit(r0);	 Catch:{ all -> 0x02d1 }
        return r4;	 Catch:{ all -> 0x02d1 }
    L_0x00a6:
        r8 = 13;	 Catch:{ all -> 0x02d1 }
        if (r2 != r8) goto L_0x00e0;	 Catch:{ all -> 0x02d1 }
    L_0x00aa:
        r3 = r11.getNativeShort(r12);	 Catch:{ all -> 0x02d1 }
        r6 = r11.connection;	 Catch:{ all -> 0x02d1 }
        r6 = r6.getYearIsDateType();	 Catch:{ all -> 0x02d1 }
        if (r6 != 0) goto L_0x00c2;	 Catch:{ all -> 0x02d1 }
    L_0x00b6:
        r5 = r11.wasNullFlag;	 Catch:{ all -> 0x02d1 }
        if (r5 == 0) goto L_0x00bc;	 Catch:{ all -> 0x02d1 }
    L_0x00ba:
        monitor-exit(r0);	 Catch:{ all -> 0x02d1 }
        return r4;	 Catch:{ all -> 0x02d1 }
    L_0x00bc:
        r4 = java.lang.String.valueOf(r3);	 Catch:{ all -> 0x02d1 }
        monitor-exit(r0);	 Catch:{ all -> 0x02d1 }
        return r4;	 Catch:{ all -> 0x02d1 }
    L_0x00c2:
        r6 = r13.getLength();	 Catch:{ all -> 0x02d1 }
        r8 = 2;	 Catch:{ all -> 0x02d1 }
        r10 = (r6 > r8 ? 1 : (r6 == r8 ? 0 : -1));	 Catch:{ all -> 0x02d1 }
        if (r10 != 0) goto L_0x00d6;	 Catch:{ all -> 0x02d1 }
    L_0x00cc:
        r6 = 69;	 Catch:{ all -> 0x02d1 }
        if (r3 > r6) goto L_0x00d3;	 Catch:{ all -> 0x02d1 }
    L_0x00d0:
        r6 = r3 + 100;	 Catch:{ all -> 0x02d1 }
        r3 = (short) r6;	 Catch:{ all -> 0x02d1 }
    L_0x00d3:
        r6 = r3 + 1900;	 Catch:{ all -> 0x02d1 }
        r3 = (short) r6;	 Catch:{ all -> 0x02d1 }
    L_0x00d6:
        r4 = r11.fastDateCreate(r4, r3, r5, r5);	 Catch:{ all -> 0x02d1 }
        r4 = r4.toString();	 Catch:{ all -> 0x02d1 }
        monitor-exit(r0);	 Catch:{ all -> 0x02d1 }
        return r4;	 Catch:{ all -> 0x02d1 }
    L_0x00e0:
        r8 = r11.connection;	 Catch:{ all -> 0x02d1 }
        r8 = r8.getNoDatetimeStringSync();	 Catch:{ all -> 0x02d1 }
        if (r8 == 0) goto L_0x0110;	 Catch:{ all -> 0x02d1 }
    L_0x00e8:
        r8 = r11.getNativeBytes(r12, r5);	 Catch:{ all -> 0x02d1 }
        if (r8 != 0) goto L_0x00f0;	 Catch:{ all -> 0x02d1 }
    L_0x00ee:
        monitor-exit(r0);	 Catch:{ all -> 0x02d1 }
        return r4;	 Catch:{ all -> 0x02d1 }
    L_0x00f0:
        r9 = r8.length;	 Catch:{ all -> 0x02d1 }
        if (r9 != 0) goto L_0x00f7;	 Catch:{ all -> 0x02d1 }
    L_0x00f3:
        r3 = "0000-00-00";	 Catch:{ all -> 0x02d1 }
        monitor-exit(r0);	 Catch:{ all -> 0x02d1 }
        return r3;	 Catch:{ all -> 0x02d1 }
    L_0x00f7:
        r6 = r8[r6];	 Catch:{ all -> 0x02d1 }
        r6 = r6 & 255;	 Catch:{ all -> 0x02d1 }
        r5 = r8[r5];	 Catch:{ all -> 0x02d1 }
        r5 = r5 & 255;	 Catch:{ all -> 0x02d1 }
        r5 = r5 << 8;	 Catch:{ all -> 0x02d1 }
        r5 = r5 | r6;	 Catch:{ all -> 0x02d1 }
        r3 = r8[r3];	 Catch:{ all -> 0x02d1 }
        r6 = r8[r7];	 Catch:{ all -> 0x02d1 }
        if (r5 != 0) goto L_0x0110;	 Catch:{ all -> 0x02d1 }
    L_0x0108:
        if (r3 != 0) goto L_0x0110;	 Catch:{ all -> 0x02d1 }
    L_0x010a:
        if (r6 != 0) goto L_0x0110;	 Catch:{ all -> 0x02d1 }
    L_0x010c:
        r4 = "0000-00-00";	 Catch:{ all -> 0x02d1 }
        monitor-exit(r0);	 Catch:{ all -> 0x02d1 }
        return r4;	 Catch:{ all -> 0x02d1 }
    L_0x0110:
        r3 = r11.getNativeDate(r12);	 Catch:{ all -> 0x02d1 }
        if (r3 != 0) goto L_0x0118;	 Catch:{ all -> 0x02d1 }
    L_0x0116:
        monitor-exit(r0);	 Catch:{ all -> 0x02d1 }
        return r4;	 Catch:{ all -> 0x02d1 }
    L_0x0118:
        r4 = java.lang.String.valueOf(r3);	 Catch:{ all -> 0x02d1 }
        monitor-exit(r0);	 Catch:{ all -> 0x02d1 }
        return r4;	 Catch:{ all -> 0x02d1 }
    L_0x011e:
        r3 = r11.getNativeFloat(r12);	 Catch:{ all -> 0x02d1 }
        r5 = r11.wasNullFlag;	 Catch:{ all -> 0x02d1 }
        if (r5 == 0) goto L_0x0128;	 Catch:{ all -> 0x02d1 }
    L_0x0126:
        monitor-exit(r0);	 Catch:{ all -> 0x02d1 }
        return r4;	 Catch:{ all -> 0x02d1 }
    L_0x0128:
        r4 = java.lang.String.valueOf(r3);	 Catch:{ all -> 0x02d1 }
        monitor-exit(r0);	 Catch:{ all -> 0x02d1 }
        return r4;	 Catch:{ all -> 0x02d1 }
    L_0x012e:
        r5 = r11.getNativeDouble(r12);	 Catch:{ all -> 0x02d1 }
        r3 = r11.wasNullFlag;	 Catch:{ all -> 0x02d1 }
        if (r3 == 0) goto L_0x0138;	 Catch:{ all -> 0x02d1 }
    L_0x0136:
        monitor-exit(r0);	 Catch:{ all -> 0x02d1 }
        return r4;	 Catch:{ all -> 0x02d1 }
    L_0x0138:
        r3 = java.lang.String.valueOf(r5);	 Catch:{ all -> 0x02d1 }
        monitor-exit(r0);	 Catch:{ all -> 0x02d1 }
        return r3;	 Catch:{ all -> 0x02d1 }
    L_0x013e:
        r3 = r11.getNativeInt(r12, r6);	 Catch:{ all -> 0x02d1 }
        r5 = r11.wasNullFlag;	 Catch:{ all -> 0x02d1 }
        if (r5 == 0) goto L_0x0148;	 Catch:{ all -> 0x02d1 }
    L_0x0146:
        monitor-exit(r0);	 Catch:{ all -> 0x02d1 }
        return r4;	 Catch:{ all -> 0x02d1 }
    L_0x0148:
        r4 = r13.isUnsigned();	 Catch:{ all -> 0x02d1 }
        if (r4 == 0) goto L_0x015b;	 Catch:{ all -> 0x02d1 }
    L_0x014e:
        if (r3 < 0) goto L_0x0151;	 Catch:{ all -> 0x02d1 }
    L_0x0150:
        goto L_0x015b;	 Catch:{ all -> 0x02d1 }
    L_0x0151:
        r4 = 65535; // 0xffff float:9.1834E-41 double:3.23786E-319;	 Catch:{ all -> 0x02d1 }
        r3 = r3 & r4;	 Catch:{ all -> 0x02d1 }
        r4 = java.lang.String.valueOf(r3);	 Catch:{ all -> 0x02d1 }
        monitor-exit(r0);	 Catch:{ all -> 0x02d1 }
        return r4;	 Catch:{ all -> 0x02d1 }
    L_0x015b:
        r4 = java.lang.String.valueOf(r3);	 Catch:{ all -> 0x02d1 }
        monitor-exit(r0);	 Catch:{ all -> 0x02d1 }
        return r4;	 Catch:{ all -> 0x02d1 }
    L_0x0161:
        r3 = r11.getNativeInt(r12, r6);	 Catch:{ all -> 0x02d1 }
        r5 = r11.wasNullFlag;	 Catch:{ all -> 0x02d1 }
        if (r5 == 0) goto L_0x016b;	 Catch:{ all -> 0x02d1 }
    L_0x0169:
        monitor-exit(r0);	 Catch:{ all -> 0x02d1 }
        return r4;	 Catch:{ all -> 0x02d1 }
    L_0x016b:
        r4 = r13.isUnsigned();	 Catch:{ all -> 0x02d1 }
        if (r4 == 0) goto L_0x018a;	 Catch:{ all -> 0x02d1 }
    L_0x0171:
        if (r3 >= 0) goto L_0x018a;	 Catch:{ all -> 0x02d1 }
    L_0x0173:
        r4 = r13.getMysqlType();	 Catch:{ all -> 0x02d1 }
        r5 = 9;	 Catch:{ all -> 0x02d1 }
        if (r4 != r5) goto L_0x017c;	 Catch:{ all -> 0x02d1 }
    L_0x017b:
        goto L_0x018a;	 Catch:{ all -> 0x02d1 }
    L_0x017c:
        r4 = (long) r3;	 Catch:{ all -> 0x02d1 }
        r6 = 4294967295; // 0xffffffff float:NaN double:2.1219957905E-314;	 Catch:{ all -> 0x02d1 }
        r8 = r4 & r6;	 Catch:{ all -> 0x02d1 }
        r4 = java.lang.String.valueOf(r8);	 Catch:{ all -> 0x02d1 }
        monitor-exit(r0);	 Catch:{ all -> 0x02d1 }
        return r4;	 Catch:{ all -> 0x02d1 }
    L_0x018a:
        r4 = java.lang.String.valueOf(r3);	 Catch:{ all -> 0x02d1 }
        monitor-exit(r0);	 Catch:{ all -> 0x02d1 }
        return r4;	 Catch:{ all -> 0x02d1 }
    L_0x0190:
        r7 = r11.thisRow;	 Catch:{ all -> 0x02d1 }
        r8 = r12 + -1;	 Catch:{ all -> 0x02d1 }
        r7 = r7.getColumnValue(r8);	 Catch:{ all -> 0x02d1 }
        r7 = com.mysql.jdbc.StringUtils.toAsciiString(r7);	 Catch:{ all -> 0x02d1 }
        if (r7 == 0) goto L_0x01da;	 Catch:{ all -> 0x02d1 }
    L_0x019e:
        r11.wasNullFlag = r6;	 Catch:{ all -> 0x02d1 }
        r4 = r7.length();	 Catch:{ all -> 0x02d1 }
        if (r4 != 0) goto L_0x01b1;	 Catch:{ all -> 0x02d1 }
    L_0x01a6:
        r3 = new java.math.BigDecimal;	 Catch:{ all -> 0x02d1 }
        r3.<init>(r6);	 Catch:{ all -> 0x02d1 }
        r4 = r3.toString();	 Catch:{ all -> 0x02d1 }
        monitor-exit(r0);	 Catch:{ all -> 0x02d1 }
        return r4;
    L_0x01b1:
        r4 = new java.math.BigDecimal;	 Catch:{ NumberFormatException -> 0x01be }
        r4.<init>(r7);	 Catch:{ NumberFormatException -> 0x01be }
        r3 = r4;
        r4 = r3.toString();	 Catch:{ all -> 0x02d1 }
        monitor-exit(r0);	 Catch:{ all -> 0x02d1 }
        return r4;	 Catch:{ all -> 0x02d1 }
    L_0x01be:
        r4 = move-exception;	 Catch:{ all -> 0x02d1 }
        r8 = "ResultSet.Bad_format_for_BigDecimal";	 Catch:{ all -> 0x02d1 }
        r3 = new java.lang.Object[r3];	 Catch:{ all -> 0x02d1 }
        r3[r6] = r7;	 Catch:{ all -> 0x02d1 }
        r6 = java.lang.Integer.valueOf(r12);	 Catch:{ all -> 0x02d1 }
        r3[r5] = r6;	 Catch:{ all -> 0x02d1 }
        r3 = com.mysql.jdbc.Messages.getString(r8, r3);	 Catch:{ all -> 0x02d1 }
        r5 = "S1009";	 Catch:{ all -> 0x02d1 }
        r6 = r11.getExceptionInterceptor();	 Catch:{ all -> 0x02d1 }
        r3 = com.mysql.jdbc.SQLError.createSQLException(r3, r5, r6);	 Catch:{ all -> 0x02d1 }
        throw r3;	 Catch:{ all -> 0x02d1 }
    L_0x01da:
        r11.wasNullFlag = r5;	 Catch:{ all -> 0x02d1 }
        monitor-exit(r0);	 Catch:{ all -> 0x02d1 }
        return r4;	 Catch:{ all -> 0x02d1 }
    L_0x01de:
        r4 = r13.isBlob();	 Catch:{ all -> 0x02d1 }
        if (r4 != 0) goto L_0x01ea;	 Catch:{ all -> 0x02d1 }
    L_0x01e4:
        r3 = r11.extractStringFromNativeColumn(r12, r2);	 Catch:{ all -> 0x02d1 }
        monitor-exit(r0);	 Catch:{ all -> 0x02d1 }
        return r3;	 Catch:{ all -> 0x02d1 }
    L_0x01ea:
        r4 = r13.isBinary();	 Catch:{ all -> 0x02d1 }
        if (r4 != 0) goto L_0x01f6;	 Catch:{ all -> 0x02d1 }
    L_0x01f0:
        r3 = r11.extractStringFromNativeColumn(r12, r2);	 Catch:{ all -> 0x02d1 }
        monitor-exit(r0);	 Catch:{ all -> 0x02d1 }
        return r3;	 Catch:{ all -> 0x02d1 }
    L_0x01f6:
        r4 = r11.getBytes(r12);	 Catch:{ all -> 0x02d1 }
        r7 = r4;	 Catch:{ all -> 0x02d1 }
        r8 = r11.connection;	 Catch:{ all -> 0x02d1 }
        r8 = r8.getAutoDeserialize();	 Catch:{ all -> 0x02d1 }
        if (r8 == 0) goto L_0x025f;	 Catch:{ all -> 0x02d1 }
    L_0x0203:
        if (r4 == 0) goto L_0x025f;	 Catch:{ all -> 0x02d1 }
    L_0x0205:
        r8 = r4.length;	 Catch:{ all -> 0x02d1 }
        if (r8 < r3) goto L_0x025f;	 Catch:{ all -> 0x02d1 }
    L_0x0208:
        r3 = r4[r6];	 Catch:{ all -> 0x02d1 }
        r6 = -84;	 Catch:{ all -> 0x02d1 }
        if (r3 != r6) goto L_0x0259;	 Catch:{ all -> 0x02d1 }
    L_0x020e:
        r3 = r4[r5];	 Catch:{ all -> 0x02d1 }
        r5 = -19;
        if (r3 != r5) goto L_0x0259;
    L_0x0214:
        r3 = new java.io.ByteArrayInputStream;	 Catch:{ ClassNotFoundException -> 0x022d, IOException -> 0x022a }
        r3.<init>(r4);	 Catch:{ ClassNotFoundException -> 0x022d, IOException -> 0x022a }
        r5 = new java.io.ObjectInputStream;	 Catch:{ ClassNotFoundException -> 0x022d, IOException -> 0x022a }
        r5.<init>(r3);	 Catch:{ ClassNotFoundException -> 0x022d, IOException -> 0x022a }
        r6 = r5.readObject();	 Catch:{ ClassNotFoundException -> 0x022d, IOException -> 0x022a }
        r7 = r6;	 Catch:{ ClassNotFoundException -> 0x022d, IOException -> 0x022a }
        r5.close();	 Catch:{ ClassNotFoundException -> 0x022d, IOException -> 0x022a }
        r3.close();	 Catch:{ ClassNotFoundException -> 0x022d, IOException -> 0x022a }
        goto L_0x0259;
    L_0x022a:
        r3 = move-exception;
        r7 = r4;
        goto L_0x0259;
    L_0x022d:
        r3 = move-exception;
        r5 = new java.lang.StringBuilder;	 Catch:{ all -> 0x02d1 }
        r5.<init>();	 Catch:{ all -> 0x02d1 }
        r6 = "ResultSet.Class_not_found___91";	 Catch:{ all -> 0x02d1 }
        r6 = com.mysql.jdbc.Messages.getString(r6);	 Catch:{ all -> 0x02d1 }
        r5.append(r6);	 Catch:{ all -> 0x02d1 }
        r6 = r3.toString();	 Catch:{ all -> 0x02d1 }
        r5.append(r6);	 Catch:{ all -> 0x02d1 }
        r6 = "ResultSet._while_reading_serialized_object_92";	 Catch:{ all -> 0x02d1 }
        r6 = com.mysql.jdbc.Messages.getString(r6);	 Catch:{ all -> 0x02d1 }
        r5.append(r6);	 Catch:{ all -> 0x02d1 }
        r5 = r5.toString();	 Catch:{ all -> 0x02d1 }
        r6 = r11.getExceptionInterceptor();	 Catch:{ all -> 0x02d1 }
        r5 = com.mysql.jdbc.SQLError.createSQLException(r5, r6);	 Catch:{ all -> 0x02d1 }
        throw r5;	 Catch:{ all -> 0x02d1 }
    L_0x0259:
        r3 = r7.toString();	 Catch:{ all -> 0x02d1 }
        monitor-exit(r0);	 Catch:{ all -> 0x02d1 }
        return r3;	 Catch:{ all -> 0x02d1 }
    L_0x025f:
        r3 = r11.extractStringFromNativeColumn(r12, r2);	 Catch:{ all -> 0x02d1 }
        monitor-exit(r0);	 Catch:{ all -> 0x02d1 }
        return r3;	 Catch:{ all -> 0x02d1 }
    L_0x0265:
        r3 = r13.isUnsigned();	 Catch:{ all -> 0x02d1 }
        if (r3 != 0) goto L_0x027b;	 Catch:{ all -> 0x02d1 }
    L_0x026b:
        r5 = r11.getNativeLong(r12, r6, r5);	 Catch:{ all -> 0x02d1 }
        r3 = r11.wasNullFlag;	 Catch:{ all -> 0x02d1 }
        if (r3 == 0) goto L_0x0275;	 Catch:{ all -> 0x02d1 }
    L_0x0273:
        monitor-exit(r0);	 Catch:{ all -> 0x02d1 }
        return r4;	 Catch:{ all -> 0x02d1 }
    L_0x0275:
        r3 = java.lang.String.valueOf(r5);	 Catch:{ all -> 0x02d1 }
        monitor-exit(r0);	 Catch:{ all -> 0x02d1 }
        return r3;	 Catch:{ all -> 0x02d1 }
    L_0x027b:
        r5 = r11.getNativeLong(r12, r6, r6);	 Catch:{ all -> 0x02d1 }
        r3 = r11.wasNullFlag;	 Catch:{ all -> 0x02d1 }
        if (r3 == 0) goto L_0x0285;	 Catch:{ all -> 0x02d1 }
    L_0x0283:
        monitor-exit(r0);	 Catch:{ all -> 0x02d1 }
        return r4;	 Catch:{ all -> 0x02d1 }
    L_0x0285:
        r3 = convertLongToUlong(r5);	 Catch:{ all -> 0x02d1 }
        r3 = java.lang.String.valueOf(r3);	 Catch:{ all -> 0x02d1 }
        monitor-exit(r0);	 Catch:{ all -> 0x02d1 }
        return r3;	 Catch:{ all -> 0x02d1 }
    L_0x028f:
        r3 = r11.getNativeByte(r12, r6);	 Catch:{ all -> 0x02d1 }
        r5 = r11.wasNullFlag;	 Catch:{ all -> 0x02d1 }
        if (r5 == 0) goto L_0x0299;	 Catch:{ all -> 0x02d1 }
    L_0x0297:
        monitor-exit(r0);	 Catch:{ all -> 0x02d1 }
        return r4;	 Catch:{ all -> 0x02d1 }
    L_0x0299:
        r4 = r13.isUnsigned();	 Catch:{ all -> 0x02d1 }
        if (r4 == 0) goto L_0x02ab;	 Catch:{ all -> 0x02d1 }
    L_0x029f:
        if (r3 < 0) goto L_0x02a2;	 Catch:{ all -> 0x02d1 }
    L_0x02a1:
        goto L_0x02ab;	 Catch:{ all -> 0x02d1 }
    L_0x02a2:
        r4 = r3 & 255;	 Catch:{ all -> 0x02d1 }
        r4 = (short) r4;	 Catch:{ all -> 0x02d1 }
        r5 = java.lang.String.valueOf(r4);	 Catch:{ all -> 0x02d1 }
        monitor-exit(r0);	 Catch:{ all -> 0x02d1 }
        return r5;	 Catch:{ all -> 0x02d1 }
    L_0x02ab:
        r4 = java.lang.String.valueOf(r3);	 Catch:{ all -> 0x02d1 }
        monitor-exit(r0);	 Catch:{ all -> 0x02d1 }
        return r4;	 Catch:{ all -> 0x02d1 }
    L_0x02b1:
        r3 = r11.getNumericRepresentationOfSQLBitType(r12);	 Catch:{ all -> 0x02d1 }
        r3 = java.lang.String.valueOf(r3);	 Catch:{ all -> 0x02d1 }
        monitor-exit(r0);	 Catch:{ all -> 0x02d1 }
        return r3;	 Catch:{ all -> 0x02d1 }
    L_0x02bb:
        r3 = r11.getBoolean(r12);	 Catch:{ all -> 0x02d1 }
        r5 = r11.wasNullFlag;	 Catch:{ all -> 0x02d1 }
        if (r5 == 0) goto L_0x02c5;	 Catch:{ all -> 0x02d1 }
    L_0x02c3:
        monitor-exit(r0);	 Catch:{ all -> 0x02d1 }
        return r4;	 Catch:{ all -> 0x02d1 }
    L_0x02c5:
        r4 = java.lang.String.valueOf(r3);	 Catch:{ all -> 0x02d1 }
        monitor-exit(r0);	 Catch:{ all -> 0x02d1 }
        return r4;	 Catch:{ all -> 0x02d1 }
    L_0x02cb:
        r3 = r11.extractStringFromNativeColumn(r12, r2);	 Catch:{ all -> 0x02d1 }
        monitor-exit(r0);	 Catch:{ all -> 0x02d1 }
        return r3;	 Catch:{ all -> 0x02d1 }
    L_0x02d1:
        r1 = move-exception;	 Catch:{ all -> 0x02d1 }
        monitor-exit(r0);	 Catch:{ all -> 0x02d1 }
        throw r1;
        */
        throw new UnsupportedOperationException("Method not decompiled: com.mysql.jdbc.ResultSetImpl.getNativeConvertToString(int, com.mysql.jdbc.Field):java.lang.String");
    }

    protected Date getNativeDate(int columnIndex) throws SQLException {
        return getNativeDate(columnIndex, null);
    }

    protected Date getNativeDate(int columnIndex, Calendar cal) throws SQLException {
        Date dateToReturn;
        boolean z;
        Calendar calendar = cal;
        checkRowPos();
        checkColumnBounds(columnIndex);
        int columnIndexMinusOne = columnIndex - 1;
        int mysqlType = this.fields[columnIndexMinusOne].getMysqlType();
        if (mysqlType == 10) {
            dateToReturn = r9.thisRow.getNativeDate(columnIndexMinusOne, r9.connection, r9, calendar);
            z = true;
        } else {
            TimeZone tz = calendar != null ? cal.getTimeZone() : getDefaultTimeZone();
            boolean rollForward = (tz == null || tz.equals(getDefaultTimeZone())) ? false : true;
            z = true;
            dateToReturn = (Date) r9.thisRow.getNativeDateTimeValue(columnIndexMinusOne, null, 91, mysqlType, tz, rollForward, r9.connection, r9);
        }
        if (dateToReturn == null) {
            r9.wasNullFlag = z;
            return null;
        }
        r9.wasNullFlag = false;
        return dateToReturn;
    }

    Date getNativeDateViaParseConversion(int columnIndex) throws SQLException {
        if (this.useUsageAdvisor) {
            issueConversionViaParsingWarning("getDate()", columnIndex, this.thisRow.getColumnValue(columnIndex - 1), this.fields[columnIndex - 1], new int[]{10});
        }
        return getDateFromString(getNativeString(columnIndex), columnIndex, null);
    }

    /* JADX WARNING: inconsistent code. */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    protected double getNativeDouble(int r9) throws java.sql.SQLException {
        /*
        r8 = this;
        r8.checkRowPos();
        r8.checkColumnBounds(r9);
        r9 = r9 + -1;
        r0 = r8.thisRow;
        r0 = r0.isNull(r9);
        if (r0 == 0) goto L_0x0016;
    L_0x0010:
        r0 = 1;
        r8.wasNullFlag = r0;
        r0 = 0;
        return r0;
    L_0x0016:
        r0 = 0;
        r8.wasNullFlag = r0;
        r0 = r8.fields;
        r6 = r0[r9];
        r0 = r6.getMysqlType();
        r1 = 13;
        if (r0 == r1) goto L_0x00ac;
    L_0x0025:
        r1 = 16;
        if (r0 == r1) goto L_0x00a4;
    L_0x0029:
        switch(r0) {
            case 1: goto L_0x008e;
            case 2: goto L_0x00ac;
            case 3: goto L_0x0078;
            case 4: goto L_0x0070;
            case 5: goto L_0x0069;
            default: goto L_0x002c;
        };
    L_0x002c:
        switch(r0) {
            case 8: goto L_0x0052;
            case 9: goto L_0x0078;
            default: goto L_0x002f;
        };
    L_0x002f:
        r0 = r9 + 1;
        r7 = r8.getNativeString(r0);
        r0 = r8.useUsageAdvisor;
        if (r0 == 0) goto L_0x004b;
    L_0x0039:
        r1 = "getDouble()";
        r0 = r8.fields;
        r4 = r0[r9];
        r0 = 6;
        r5 = new int[r0];
        r5 = {5, 1, 2, 3, 8, 4};
        r0 = r8;
        r2 = r9;
        r3 = r7;
        r0.issueConversionViaParsingWarning(r1, r2, r3, r4, r5);
    L_0x004b:
        r0 = r9 + 1;
        r0 = r8.getDoubleFromString(r7, r0);
        return r0;
    L_0x0052:
        r0 = r9 + 1;
        r0 = r8.getNativeLong(r0);
        r2 = r6.isUnsigned();
        if (r2 != 0) goto L_0x0060;
    L_0x005e:
        r2 = (double) r0;
        return r2;
    L_0x0060:
        r2 = convertLongToUlong(r0);
        r3 = r2.doubleValue();
        return r3;
    L_0x0069:
        r0 = r8.thisRow;
        r0 = r0.getNativeDouble(r9);
        return r0;
    L_0x0070:
        r0 = r9 + 1;
        r0 = r8.getNativeFloat(r0);
        r0 = (double) r0;
        return r0;
    L_0x0078:
        r0 = r6.isUnsigned();
        if (r0 != 0) goto L_0x0086;
    L_0x007e:
        r0 = r9 + 1;
        r0 = r8.getNativeInt(r0);
        r0 = (double) r0;
        return r0;
    L_0x0086:
        r0 = r9 + 1;
        r0 = r8.getNativeLong(r0);
        r0 = (double) r0;
        return r0;
    L_0x008e:
        r0 = r6.isUnsigned();
        if (r0 != 0) goto L_0x009c;
    L_0x0094:
        r0 = r9 + 1;
        r0 = r8.getNativeByte(r0);
        r0 = (double) r0;
        return r0;
    L_0x009c:
        r0 = r9 + 1;
        r0 = r8.getNativeShort(r0);
        r0 = (double) r0;
        return r0;
    L_0x00a4:
        r0 = r9 + 1;
        r0 = r8.getNumericRepresentationOfSQLBitType(r0);
        r0 = (double) r0;
        return r0;
    L_0x00ac:
        r0 = r6.isUnsigned();
        if (r0 != 0) goto L_0x00ba;
    L_0x00b2:
        r0 = r9 + 1;
        r0 = r8.getNativeShort(r0);
        r0 = (double) r0;
        return r0;
    L_0x00ba:
        r0 = r9 + 1;
        r0 = r8.getNativeInt(r0);
        r0 = (double) r0;
        return r0;
        */
        throw new UnsupportedOperationException("Method not decompiled: com.mysql.jdbc.ResultSetImpl.getNativeDouble(int):double");
    }

    /* JADX WARNING: inconsistent code. */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    protected float getNativeFloat(int r9) throws java.sql.SQLException {
        /*
        r8 = this;
        r8.checkRowPos();
        r8.checkColumnBounds(r9);
        r9 = r9 + -1;
        r0 = r8.thisRow;
        r0 = r0.isNull(r9);
        if (r0 == 0) goto L_0x0015;
    L_0x0010:
        r0 = 1;
        r8.wasNullFlag = r0;
        r0 = 0;
        return r0;
    L_0x0015:
        r0 = 0;
        r8.wasNullFlag = r0;
        r0 = r8.fields;
        r6 = r0[r9];
        r0 = r6.getMysqlType();
        r1 = 13;
        if (r0 == r1) goto L_0x00d4;
    L_0x0024:
        r1 = 16;
        if (r0 == r1) goto L_0x00cc;
    L_0x0028:
        r1 = 6;
        switch(r0) {
            case 1: goto L_0x00b6;
            case 2: goto L_0x00d4;
            case 3: goto L_0x00a0;
            case 4: goto L_0x0099;
            case 5: goto L_0x0069;
            default: goto L_0x002c;
        };
    L_0x002c:
        switch(r0) {
            case 8: goto L_0x0052;
            case 9: goto L_0x00a0;
            default: goto L_0x002f;
        };
    L_0x002f:
        r0 = r9 + 1;
        r7 = r8.getNativeString(r0);
        r0 = r8.useUsageAdvisor;
        if (r0 == 0) goto L_0x004b;
    L_0x0039:
        r2 = "getFloat()";
        r0 = r8.fields;
        r4 = r0[r9];
        r5 = new int[r1];
        r5 = {5, 1, 2, 3, 8, 4};
        r0 = r8;
        r1 = r2;
        r2 = r9;
        r3 = r7;
        r0.issueConversionViaParsingWarning(r1, r2, r3, r4, r5);
    L_0x004b:
        r0 = r9 + 1;
        r0 = r8.getFloatFromString(r7, r0);
        return r0;
    L_0x0052:
        r0 = r9 + 1;
        r0 = r8.getNativeLong(r0);
        r2 = r6.isUnsigned();
        if (r2 != 0) goto L_0x0060;
    L_0x005e:
        r2 = (float) r0;
        return r2;
    L_0x0060:
        r2 = convertLongToUlong(r0);
        r3 = r2.floatValue();
        return r3;
    L_0x0069:
        r0 = new java.lang.Double;
        r2 = r9 + 1;
        r2 = r8.getNativeDouble(r2);
        r0.<init>(r2);
        r2 = r0.floatValue();
        r3 = r8.jdbcCompliantTruncationForReads;
        if (r3 == 0) goto L_0x0082;
    L_0x007c:
        r3 = -8388608; // 0xffffffffff800000 float:-Infinity double:NaN;
        r3 = (r2 > r3 ? 1 : (r2 == r3 ? 0 : -1));
        if (r3 == 0) goto L_0x0088;
    L_0x0082:
        r3 = 2139095040; // 0x7f800000 float:Infinity double:1.0568533725E-314;
        r3 = (r2 > r3 ? 1 : (r2 == r3 ? 0 : -1));
        if (r3 != 0) goto L_0x0091;
    L_0x0088:
        r3 = r0.toString();
        r4 = r9 + 1;
        r8.throwRangeException(r3, r4, r1);
    L_0x0091:
        r1 = r9 + 1;
        r3 = r8.getNativeDouble(r1);
        r1 = (float) r3;
        return r1;
    L_0x0099:
        r0 = r8.thisRow;
        r0 = r0.getNativeFloat(r9);
        return r0;
    L_0x00a0:
        r0 = r6.isUnsigned();
        if (r0 != 0) goto L_0x00ae;
    L_0x00a6:
        r0 = r9 + 1;
        r0 = r8.getNativeInt(r0);
        r0 = (float) r0;
        return r0;
    L_0x00ae:
        r0 = r9 + 1;
        r0 = r8.getNativeLong(r0);
        r0 = (float) r0;
        return r0;
    L_0x00b6:
        r0 = r6.isUnsigned();
        if (r0 != 0) goto L_0x00c4;
    L_0x00bc:
        r0 = r9 + 1;
        r0 = r8.getNativeByte(r0);
        r0 = (float) r0;
        return r0;
    L_0x00c4:
        r0 = r9 + 1;
        r0 = r8.getNativeShort(r0);
        r0 = (float) r0;
        return r0;
    L_0x00cc:
        r0 = r9 + 1;
        r0 = r8.getNumericRepresentationOfSQLBitType(r0);
        r2 = (float) r0;
        return r2;
    L_0x00d4:
        r0 = r6.isUnsigned();
        if (r0 != 0) goto L_0x00e2;
    L_0x00da:
        r0 = r9 + 1;
        r0 = r8.getNativeShort(r0);
        r0 = (float) r0;
        return r0;
    L_0x00e2:
        r0 = r9 + 1;
        r0 = r8.getNativeInt(r0);
        r0 = (float) r0;
        return r0;
        */
        throw new UnsupportedOperationException("Method not decompiled: com.mysql.jdbc.ResultSetImpl.getNativeFloat(int):float");
    }

    protected int getNativeInt(int columnIndex) throws SQLException {
        return getNativeInt(columnIndex, true);
    }

    /* JADX WARNING: inconsistent code. */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    protected int getNativeInt(int r17, boolean r18) throws java.sql.SQLException {
        /*
        r16 = this;
        r6 = r16;
        r16.checkRowPos();
        r16.checkColumnBounds(r17);
        r7 = r17 + -1;
        r0 = r6.thisRow;
        r0 = r0.isNull(r7);
        r1 = 0;
        r2 = 1;
        if (r0 == 0) goto L_0x0017;
    L_0x0014:
        r6.wasNullFlag = r2;
        return r1;
    L_0x0017:
        r6.wasNullFlag = r1;
        r0 = r6.fields;
        r8 = r0[r7];
        r0 = r8.getMysqlType();
        r3 = 13;
        if (r0 == r3) goto L_0x0120;
    L_0x0025:
        r3 = 16;
        r4 = -2147483648; // 0xffffffff80000000 float:-0.0 double:NaN;
        r9 = 2147483647; // 0x7fffffff float:NaN double:1.060997895E-314;
        r11 = 4;
        if (r0 == r3) goto L_0x0101;
    L_0x0030:
        r12 = 4746794007244308480; // 0x41dfffffffc00000 float:NaN double:2.147483647E9;
        r14 = -4476578029606273024; // 0xc1e0000000000000 float:0.0 double:-2.147483648E9;
        switch(r0) {
            case 1: goto L_0x00ee;
            case 2: goto L_0x0120;
            case 3: goto L_0x00be;
            case 4: goto L_0x009e;
            case 5: goto L_0x007f;
            default: goto L_0x003a;
        };
    L_0x003a:
        switch(r0) {
            case 8: goto L_0x0060;
            case 9: goto L_0x00be;
            default: goto L_0x003d;
        };
    L_0x003d:
        r0 = r7 + 1;
        r9 = r6.getNativeString(r0);
        r0 = r6.useUsageAdvisor;
        if (r0 == 0) goto L_0x0059;
    L_0x0047:
        r1 = "getInt()";
        r0 = r6.fields;
        r4 = r0[r7];
        r0 = 6;
        r5 = new int[r0];
        r5 = {5, 1, 2, 3, 8, 4};
        r0 = r6;
        r2 = r7;
        r3 = r9;
        r0.issueConversionViaParsingWarning(r1, r2, r3, r4, r5);
    L_0x0059:
        r0 = r7 + 1;
        r0 = r6.getIntFromString(r9, r0);
        return r0;
    L_0x0060:
        r0 = r7 + 1;
        r0 = r6.getNativeLong(r0, r1, r2);
        if (r18 == 0) goto L_0x007d;
    L_0x0068:
        r2 = r6.jdbcCompliantTruncationForReads;
        if (r2 == 0) goto L_0x007d;
    L_0x006c:
        r2 = (r0 > r4 ? 1 : (r0 == r4 ? 0 : -1));
        if (r2 < 0) goto L_0x0074;
    L_0x0070:
        r2 = (r0 > r9 ? 1 : (r0 == r9 ? 0 : -1));
        if (r2 <= 0) goto L_0x007d;
    L_0x0074:
        r2 = java.lang.String.valueOf(r0);
        r3 = r7 + 1;
        r6.throwRangeException(r2, r3, r11);
    L_0x007d:
        r2 = (int) r0;
        return r2;
    L_0x007f:
        r0 = r7 + 1;
        r0 = r6.getNativeDouble(r0);
        if (r18 == 0) goto L_0x009c;
    L_0x0087:
        r2 = r6.jdbcCompliantTruncationForReads;
        if (r2 == 0) goto L_0x009c;
    L_0x008b:
        r2 = (r0 > r14 ? 1 : (r0 == r14 ? 0 : -1));
        if (r2 < 0) goto L_0x0093;
    L_0x008f:
        r2 = (r0 > r12 ? 1 : (r0 == r12 ? 0 : -1));
        if (r2 <= 0) goto L_0x009c;
    L_0x0093:
        r2 = java.lang.String.valueOf(r0);
        r3 = r7 + 1;
        r6.throwRangeException(r2, r3, r11);
    L_0x009c:
        r2 = (int) r0;
        return r2;
    L_0x009e:
        r0 = r7 + 1;
        r0 = r6.getNativeFloat(r0);
        r0 = (double) r0;
        if (r18 == 0) goto L_0x00bc;
    L_0x00a7:
        r2 = r6.jdbcCompliantTruncationForReads;
        if (r2 == 0) goto L_0x00bc;
    L_0x00ab:
        r2 = (r0 > r14 ? 1 : (r0 == r14 ? 0 : -1));
        if (r2 < 0) goto L_0x00b3;
    L_0x00af:
        r2 = (r0 > r12 ? 1 : (r0 == r12 ? 0 : -1));
        if (r2 <= 0) goto L_0x00bc;
    L_0x00b3:
        r2 = java.lang.String.valueOf(r0);
        r3 = r7 + 1;
        r6.throwRangeException(r2, r3, r11);
    L_0x00bc:
        r2 = (int) r0;
        return r2;
    L_0x00be:
        r0 = r6.thisRow;
        r0 = r0.getNativeInt(r7);
        r1 = r8.isUnsigned();
        if (r1 != 0) goto L_0x00cb;
    L_0x00ca:
        return r0;
    L_0x00cb:
        if (r0 < 0) goto L_0x00d0;
    L_0x00cd:
        r1 = (long) r0;
        r12 = r1;
        goto L_0x00d8;
    L_0x00d0:
        r1 = (long) r0;
        r3 = 4294967296; // 0x100000000 float:0.0 double:2.121995791E-314;
        r12 = r1 + r3;
    L_0x00d8:
        r1 = r12;
        if (r18 == 0) goto L_0x00ec;
    L_0x00db:
        r3 = r6.jdbcCompliantTruncationForReads;
        if (r3 == 0) goto L_0x00ec;
    L_0x00df:
        r3 = (r1 > r9 ? 1 : (r1 == r9 ? 0 : -1));
        if (r3 <= 0) goto L_0x00ec;
    L_0x00e3:
        r3 = java.lang.String.valueOf(r1);
        r4 = r7 + 1;
        r6.throwRangeException(r3, r4, r11);
    L_0x00ec:
        r3 = (int) r1;
        return r3;
    L_0x00ee:
        r0 = r7 + 1;
        r0 = r6.getNativeByte(r0, r1);
        r1 = r8.isUnsigned();
        if (r1 == 0) goto L_0x0100;
    L_0x00fa:
        if (r0 < 0) goto L_0x00fd;
    L_0x00fc:
        goto L_0x0100;
    L_0x00fd:
        r1 = r0 + 256;
        return r1;
    L_0x0100:
        return r0;
    L_0x0101:
        r0 = r7 + 1;
        r0 = r6.getNumericRepresentationOfSQLBitType(r0);
        if (r18 == 0) goto L_0x011e;
    L_0x0109:
        r2 = r6.jdbcCompliantTruncationForReads;
        if (r2 == 0) goto L_0x011e;
    L_0x010d:
        r2 = (r0 > r4 ? 1 : (r0 == r4 ? 0 : -1));
        if (r2 < 0) goto L_0x0115;
    L_0x0111:
        r2 = (r0 > r9 ? 1 : (r0 == r9 ? 0 : -1));
        if (r2 <= 0) goto L_0x011e;
    L_0x0115:
        r2 = java.lang.String.valueOf(r0);
        r3 = r7 + 1;
        r6.throwRangeException(r2, r3, r11);
    L_0x011e:
        r2 = (int) r0;
        return r2;
    L_0x0120:
        r0 = r7 + 1;
        r0 = r6.getNativeShort(r0, r1);
        r1 = r8.isUnsigned();
        if (r1 == 0) goto L_0x0133;
    L_0x012c:
        if (r0 < 0) goto L_0x012f;
    L_0x012e:
        goto L_0x0133;
    L_0x012f:
        r1 = 65536; // 0x10000 float:9.18355E-41 double:3.2379E-319;
        r1 = r1 + r0;
        return r1;
    L_0x0133:
        return r0;
        */
        throw new UnsupportedOperationException("Method not decompiled: com.mysql.jdbc.ResultSetImpl.getNativeInt(int, boolean):int");
    }

    protected long getNativeLong(int columnIndex) throws SQLException {
        return getNativeLong(columnIndex, true, true);
    }

    protected long getNativeLong(int columnIndex, boolean overflowCheck, boolean expandUnsignedLong) throws SQLException {
        checkRowPos();
        checkColumnBounds(columnIndex);
        columnIndex--;
        if (this.thisRow.isNull(columnIndex)) {
            this.wasNullFlag = true;
            return 0;
        }
        this.wasNullFlag = false;
        Field f = this.fields[columnIndex];
        int mysqlType = f.getMysqlType();
        if (mysqlType == 13) {
            return (long) getNativeShort(columnIndex + 1);
        }
        if (mysqlType == 16) {
            return getNumericRepresentationOfSQLBitType(columnIndex + 1);
        }
        double valueAsDouble;
        switch (mysqlType) {
            case 1:
                if (f.isUnsigned()) {
                    return (long) getNativeInt(columnIndex + 1);
                }
                return (long) getNativeByte(columnIndex + 1);
            case 2:
                if (f.isUnsigned()) {
                    return (long) getNativeInt(columnIndex + 1, false);
                }
                return (long) getNativeShort(columnIndex + 1);
            case 3:
                break;
            case 4:
                valueAsDouble = (double) getNativeFloat(columnIndex + 1);
                if (overflowCheck && this.jdbcCompliantTruncationForReads && (valueAsDouble < -9.223372036854776E18d || valueAsDouble > 9.223372036854776E18d)) {
                    throwRangeException(String.valueOf(valueAsDouble), columnIndex + 1, -5);
                }
                return (long) valueAsDouble;
            case 5:
                valueAsDouble = getNativeDouble(columnIndex + 1);
                if (overflowCheck && this.jdbcCompliantTruncationForReads && (valueAsDouble < -9.223372036854776E18d || valueAsDouble > 9.223372036854776E18d)) {
                    throwRangeException(String.valueOf(valueAsDouble), columnIndex + 1, -5);
                }
                return (long) valueAsDouble;
            default:
                switch (mysqlType) {
                    case 8:
                        long valueAsLong = this.thisRow.getNativeLong(columnIndex);
                        if (f.isUnsigned()) {
                            if (expandUnsignedLong) {
                                BigInteger asBigInt = convertLongToUlong(valueAsLong);
                                if (overflowCheck && this.jdbcCompliantTruncationForReads && (asBigInt.compareTo(new BigInteger(String.valueOf(Long.MAX_VALUE))) > 0 || asBigInt.compareTo(new BigInteger(String.valueOf(Long.MIN_VALUE))) < 0)) {
                                    throwRangeException(asBigInt.toString(), columnIndex + 1, -5);
                                }
                                return getLongFromString(asBigInt.toString(), columnIndex);
                            }
                        }
                        return valueAsLong;
                    case 9:
                        break;
                    default:
                        String stringVal = getNativeString(columnIndex + 1);
                        if (this.useUsageAdvisor) {
                            issueConversionViaParsingWarning("getLong()", columnIndex, stringVal, this.fields[columnIndex], new int[]{5, 1, 2, 3, 8, 4});
                        }
                        return getLongFromString(stringVal, columnIndex + 1);
                }
        }
        int asInt = getNativeInt(columnIndex + 1, false);
        if (f.isUnsigned()) {
            if (asInt < 0) {
                return ((long) asInt) + 4294967296L;
            }
        }
        return (long) asInt;
    }

    protected Ref getNativeRef(int i) throws SQLException {
        throw SQLError.createSQLFeatureNotSupportedException();
    }

    protected short getNativeShort(int columnIndex) throws SQLException {
        return getNativeShort(columnIndex, true);
    }

    /* JADX WARNING: inconsistent code. */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    protected short getNativeShort(int r12, boolean r13) throws java.sql.SQLException {
        /*
        r11 = this;
        r11.checkRowPos();
        r11.checkColumnBounds(r12);
        r12 = r12 + -1;
        r0 = r11.thisRow;
        r0 = r0.isNull(r12);
        r1 = 0;
        r2 = 1;
        if (r0 == 0) goto L_0x0015;
    L_0x0012:
        r11.wasNullFlag = r2;
        return r1;
    L_0x0015:
        r11.wasNullFlag = r1;
        r0 = r11.fields;
        r6 = r0[r12];
        r0 = r6.getMysqlType();
        r3 = 13;
        r4 = 32767; // 0x7fff float:4.5916E-41 double:1.6189E-319;
        r5 = 5;
        if (r0 == r3) goto L_0x017a;
    L_0x0026:
        r3 = 16;
        r7 = -32768; // 0xffffffffffff8000 float:NaN double:NaN;
        r9 = 32767; // 0x7fff float:4.5916E-41 double:1.6189E-319;
        if (r0 == r3) goto L_0x015a;
    L_0x002e:
        r3 = -32768; // 0xffffffffffff8000 float:NaN double:NaN;
        switch(r0) {
            case 1: goto L_0x0145;
            case 2: goto L_0x017a;
            case 3: goto L_0x0108;
            case 4: goto L_0x00e3;
            case 5: goto L_0x00bc;
            default: goto L_0x0033;
        };
    L_0x0033:
        switch(r0) {
            case 8: goto L_0x0059;
            case 9: goto L_0x0108;
            default: goto L_0x0036;
        };
    L_0x0036:
        r0 = r12 + 1;
        r7 = r11.getNativeString(r0);
        r0 = r11.useUsageAdvisor;
        if (r0 == 0) goto L_0x0052;
    L_0x0040:
        r1 = "getShort()";
        r0 = r11.fields;
        r4 = r0[r12];
        r0 = 6;
        r5 = new int[r0];
        r5 = {5, 1, 2, 3, 8, 4};
        r0 = r11;
        r2 = r12;
        r3 = r7;
        r0.issueConversionViaParsingWarning(r1, r2, r3, r4, r5);
    L_0x0052:
        r0 = r12 + 1;
        r0 = r11.getShortFromString(r7, r0);
        return r0;
    L_0x0059:
        r0 = r12 + 1;
        r0 = r11.getNativeLong(r0, r1, r1);
        r2 = r6.isUnsigned();
        if (r2 != 0) goto L_0x007f;
    L_0x0065:
        if (r13 == 0) goto L_0x007c;
    L_0x0067:
        r2 = r11.jdbcCompliantTruncationForReads;
        if (r2 == 0) goto L_0x007c;
    L_0x006b:
        r2 = (r0 > r7 ? 1 : (r0 == r7 ? 0 : -1));
        if (r2 < 0) goto L_0x0073;
    L_0x006f:
        r2 = (r0 > r9 ? 1 : (r0 == r9 ? 0 : -1));
        if (r2 <= 0) goto L_0x007c;
    L_0x0073:
        r2 = java.lang.String.valueOf(r0);
        r3 = r12 + 1;
        r11.throwRangeException(r2, r3, r5);
    L_0x007c:
        r2 = (int) r0;
        r2 = (short) r2;
        return r2;
    L_0x007f:
        r2 = convertLongToUlong(r0);
        if (r13 == 0) goto L_0x00b0;
    L_0x0085:
        r7 = r11.jdbcCompliantTruncationForReads;
        if (r7 == 0) goto L_0x00b0;
    L_0x0089:
        r7 = new java.math.BigInteger;
        r4 = java.lang.String.valueOf(r4);
        r7.<init>(r4);
        r4 = r2.compareTo(r7);
        if (r4 > 0) goto L_0x00a7;
    L_0x0098:
        r4 = new java.math.BigInteger;
        r3 = java.lang.String.valueOf(r3);
        r4.<init>(r3);
        r3 = r2.compareTo(r4);
        if (r3 >= 0) goto L_0x00b0;
    L_0x00a7:
        r3 = r2.toString();
        r4 = r12 + 1;
        r11.throwRangeException(r3, r4, r5);
    L_0x00b0:
        r3 = r2.toString();
        r4 = r12 + 1;
        r3 = r11.getIntFromString(r3, r4);
        r3 = (short) r3;
        return r3;
    L_0x00bc:
        r0 = r12 + 1;
        r0 = r11.getNativeDouble(r0);
        if (r13 == 0) goto L_0x00e0;
    L_0x00c4:
        r2 = r11.jdbcCompliantTruncationForReads;
        if (r2 == 0) goto L_0x00e0;
    L_0x00c8:
        r2 = -4548635623644200960; // 0xc0e0000000000000 float:0.0 double:-32768.0;
        r4 = (r0 > r2 ? 1 : (r0 == r2 ? 0 : -1));
        if (r4 < 0) goto L_0x00d7;
    L_0x00ce:
        r2 = 4674736138332667904; // 0x40dfffc000000000 float:0.0 double:32767.0;
        r4 = (r0 > r2 ? 1 : (r0 == r2 ? 0 : -1));
        if (r4 <= 0) goto L_0x00e0;
    L_0x00d7:
        r2 = java.lang.String.valueOf(r0);
        r3 = r12 + 1;
        r11.throwRangeException(r2, r3, r5);
    L_0x00e0:
        r2 = (int) r0;
        r2 = (short) r2;
        return r2;
    L_0x00e3:
        r0 = r12 + 1;
        r0 = r11.getNativeFloat(r0);
        if (r13 == 0) goto L_0x0105;
    L_0x00eb:
        r1 = r11.jdbcCompliantTruncationForReads;
        if (r1 == 0) goto L_0x0105;
    L_0x00ef:
        r1 = -956301312; // 0xffffffffc7000000 float:-32768.0 double:NaN;
        r1 = (r0 > r1 ? 1 : (r0 == r1 ? 0 : -1));
        if (r1 < 0) goto L_0x00fc;
    L_0x00f5:
        r1 = 1191181824; // 0x46fffe00 float:32767.0 double:5.88522017E-315;
        r1 = (r0 > r1 ? 1 : (r0 == r1 ? 0 : -1));
        if (r1 <= 0) goto L_0x0105;
    L_0x00fc:
        r1 = java.lang.String.valueOf(r0);
        r2 = r12 + 1;
        r11.throwRangeException(r1, r2, r5);
    L_0x0105:
        r1 = (int) r0;
        r1 = (short) r1;
        return r1;
    L_0x0108:
        r0 = r6.isUnsigned();
        if (r0 != 0) goto L_0x0129;
    L_0x010e:
        r0 = r12 + 1;
        r0 = r11.getNativeInt(r0, r1);
        if (r13 == 0) goto L_0x011c;
    L_0x0116:
        r1 = r11.jdbcCompliantTruncationForReads;
        if (r1 == 0) goto L_0x011c;
    L_0x011a:
        if (r0 > r4) goto L_0x011e;
    L_0x011c:
        if (r0 >= r3) goto L_0x0127;
    L_0x011e:
        r1 = java.lang.String.valueOf(r0);
        r2 = r12 + 1;
        r11.throwRangeException(r1, r2, r5);
    L_0x0127:
        r1 = (short) r0;
        return r1;
    L_0x0129:
        r0 = r12 + 1;
        r0 = r11.getNativeLong(r0, r1, r2);
        if (r13 == 0) goto L_0x0142;
    L_0x0131:
        r2 = r11.jdbcCompliantTruncationForReads;
        if (r2 == 0) goto L_0x0142;
    L_0x0135:
        r2 = (r0 > r9 ? 1 : (r0 == r9 ? 0 : -1));
        if (r2 <= 0) goto L_0x0142;
    L_0x0139:
        r2 = java.lang.String.valueOf(r0);
        r3 = r12 + 1;
        r11.throwRangeException(r2, r3, r5);
    L_0x0142:
        r2 = (int) r0;
        r2 = (short) r2;
        return r2;
    L_0x0145:
        r0 = r12 + 1;
        r0 = r11.getNativeByte(r0, r1);
        r1 = r6.isUnsigned();
        if (r1 == 0) goto L_0x0158;
    L_0x0151:
        if (r0 < 0) goto L_0x0154;
    L_0x0153:
        goto L_0x0158;
    L_0x0154:
        r1 = r0 + 256;
        r1 = (short) r1;
        return r1;
    L_0x0158:
        r1 = (short) r0;
        return r1;
    L_0x015a:
        r0 = r12 + 1;
        r0 = r11.getNumericRepresentationOfSQLBitType(r0);
        if (r13 == 0) goto L_0x0177;
    L_0x0162:
        r2 = r11.jdbcCompliantTruncationForReads;
        if (r2 == 0) goto L_0x0177;
    L_0x0166:
        r2 = (r0 > r7 ? 1 : (r0 == r7 ? 0 : -1));
        if (r2 < 0) goto L_0x016e;
    L_0x016a:
        r2 = (r0 > r9 ? 1 : (r0 == r9 ? 0 : -1));
        if (r2 <= 0) goto L_0x0177;
    L_0x016e:
        r2 = java.lang.String.valueOf(r0);
        r3 = r12 + 1;
        r11.throwRangeException(r2, r3, r5);
    L_0x0177:
        r2 = (int) r0;
        r2 = (short) r2;
        return r2;
    L_0x017a:
        r0 = r11.thisRow;
        r0 = r0.getNativeShort(r12);
        r1 = r6.isUnsigned();
        if (r1 != 0) goto L_0x0187;
    L_0x0186:
        return r0;
    L_0x0187:
        r1 = 65535; // 0xffff float:9.1834E-41 double:3.23786E-319;
        r1 = r1 & r0;
        if (r13 == 0) goto L_0x019c;
    L_0x018d:
        r2 = r11.jdbcCompliantTruncationForReads;
        if (r2 == 0) goto L_0x019c;
    L_0x0191:
        if (r1 <= r4) goto L_0x019c;
    L_0x0193:
        r2 = java.lang.String.valueOf(r1);
        r3 = r12 + 1;
        r11.throwRangeException(r2, r3, r5);
    L_0x019c:
        r2 = (short) r1;
        return r2;
        */
        throw new UnsupportedOperationException("Method not decompiled: com.mysql.jdbc.ResultSetImpl.getNativeShort(int, boolean):short");
    }

    protected String getNativeString(int columnIndex) throws SQLException {
        checkRowPos();
        checkColumnBounds(columnIndex);
        if (this.fields == null) {
            throw SQLError.createSQLException(Messages.getString("ResultSet.Query_generated_no_fields_for_ResultSet_133"), SQLError.SQL_STATE_INVALID_COLUMN_NUMBER, getExceptionInterceptor());
        } else if (this.thisRow.isNull(columnIndex - 1)) {
            this.wasNullFlag = true;
            return null;
        } else {
            this.wasNullFlag = false;
            Field field = this.fields[columnIndex - 1];
            String stringVal = getNativeConvertToString(columnIndex, field);
            int mysqlType = field.getMysqlType();
            if (!(mysqlType == 7 || mysqlType == 10 || !field.isZeroFill() || stringVal == null)) {
                int origLength = stringVal.length();
                StringBuilder zeroFillBuf = new StringBuilder(origLength);
                long numZeros = field.getLength() - ((long) origLength);
                for (long i = 0; i < numZeros; i++) {
                    zeroFillBuf.append('0');
                }
                zeroFillBuf.append(stringVal);
                stringVal = zeroFillBuf.toString();
            }
            return stringVal;
        }
    }

    private Time getNativeTime(int columnIndex, Calendar targetCalendar, TimeZone tz, boolean rollForward) throws SQLException {
        Time timeVal;
        checkRowPos();
        checkColumnBounds(columnIndex);
        int columnIndexMinusOne = columnIndex - 1;
        int mysqlType = this.fields[columnIndexMinusOne].getMysqlType();
        if (mysqlType == 11) {
            timeVal = r9.thisRow.getNativeTime(columnIndexMinusOne, targetCalendar, tz, rollForward, r9.connection, r9);
        } else {
            timeVal = (Time) r9.thisRow.getNativeDateTimeValue(columnIndexMinusOne, null, 92, mysqlType, tz, rollForward, r9.connection, r9);
        }
        if (timeVal == null) {
            r9.wasNullFlag = true;
            return null;
        }
        r9.wasNullFlag = false;
        return timeVal;
    }

    Time getNativeTimeViaParseConversion(int columnIndex, Calendar targetCalendar, TimeZone tz, boolean rollForward) throws SQLException {
        if (this.useUsageAdvisor) {
            issueConversionViaParsingWarning("getTime()", columnIndex, this.thisRow.getColumnValue(columnIndex - 1), this.fields[columnIndex - 1], new int[]{11});
        }
        return getTimeFromString(getNativeString(columnIndex), targetCalendar, columnIndex, tz, rollForward);
    }

    private Timestamp getNativeTimestamp(int columnIndex, Calendar targetCalendar, TimeZone tz, boolean rollForward) throws SQLException {
        Timestamp tsVal;
        checkRowPos();
        checkColumnBounds(columnIndex);
        int columnIndexMinusOne = columnIndex - 1;
        int mysqlType = this.fields[columnIndexMinusOne].getMysqlType();
        if (mysqlType == 7 || mysqlType == 12) {
            tsVal = r9.thisRow.getNativeTimestamp(columnIndexMinusOne, targetCalendar, tz, rollForward, r9.connection, r9);
        } else {
            tsVal = (Timestamp) r9.thisRow.getNativeDateTimeValue(columnIndexMinusOne, null, 93, mysqlType, tz, rollForward, r9.connection, r9);
        }
        if (tsVal == null) {
            r9.wasNullFlag = true;
            return null;
        }
        r9.wasNullFlag = false;
        return tsVal;
    }

    Timestamp getNativeTimestampViaParseConversion(int columnIndex, Calendar targetCalendar, TimeZone tz, boolean rollForward) throws SQLException {
        if (this.useUsageAdvisor) {
            issueConversionViaParsingWarning("getTimestamp()", columnIndex, this.thisRow.getColumnValue(columnIndex - 1), this.fields[columnIndex - 1], new int[]{7, 12});
        }
        return getTimestampFromString(columnIndex, targetCalendar, getNativeString(columnIndex), tz, rollForward);
    }

    protected InputStream getNativeUnicodeStream(int columnIndex) throws SQLException {
        checkRowPos();
        return getBinaryStream(columnIndex);
    }

    protected URL getNativeURL(int colIndex) throws SQLException {
        String val = getString(colIndex);
        if (val == null) {
            return null;
        }
        try {
            return new URL(val);
        } catch (MalformedURLException e) {
            StringBuilder stringBuilder = new StringBuilder();
            stringBuilder.append(Messages.getString("ResultSet.Malformed_URL____141"));
            stringBuilder.append(val);
            stringBuilder.append("'");
            throw SQLError.createSQLException(stringBuilder.toString(), SQLError.SQL_STATE_ILLEGAL_ARGUMENT, getExceptionInterceptor());
        }
    }

    public synchronized ResultSetInternalMethods getNextResultSet() {
        return this.nextResultSet;
    }

    public Object getObject(int columnIndex) throws SQLException {
        checkRowPos();
        checkColumnBounds(columnIndex);
        int columnIndexMinusOne = columnIndex - 1;
        if (this.thisRow.isNull(columnIndexMinusOne)) {
            this.wasNullFlag = true;
            return null;
        }
        this.wasNullFlag = false;
        Field field = this.fields[columnIndexMinusOne];
        int sQLType = field.getSQLType();
        if (sQLType != 12) {
            if (sQLType == 16) {
                return Boolean.valueOf(getBoolean(columnIndex));
            }
            String stringVal;
            switch (sQLType) {
                case -7:
                    if (field.getMysqlType() != 16 || field.isSingleBit()) {
                        return Boolean.valueOf(getBoolean(columnIndex));
                    }
                    return getObjectDeserializingIfNeeded(columnIndex);
                case -6:
                    if (field.isUnsigned()) {
                        return Integer.valueOf(getInt(columnIndex));
                    }
                    return Integer.valueOf(getByte(columnIndex));
                case -5:
                    if (!field.isUnsigned()) {
                        return Long.valueOf(getLong(columnIndex));
                    }
                    stringVal = getString(columnIndex);
                    if (stringVal == null) {
                        return null;
                    }
                    try {
                        return new BigInteger(stringVal);
                    } catch (NumberFormatException e) {
                        throw SQLError.createSQLException(Messages.getString("ResultSet.Bad_format_for_BigInteger", new Object[]{Integer.valueOf(columnIndex), stringVal}), SQLError.SQL_STATE_ILLEGAL_ARGUMENT, getExceptionInterceptor());
                    }
                case FontRequestCallback.FAIL_REASON_SECURITY_VIOLATION /*-4*/:
                case FontRequestCallback.FAIL_REASON_FONT_LOAD_ERROR /*-3*/:
                case -2:
                    if (field.getMysqlType() == 255) {
                        return getBytes(columnIndex);
                    }
                    return getObjectDeserializingIfNeeded(columnIndex);
                case -1:
                    if (field.isOpaqueBinary()) {
                        return getBytes(columnIndex);
                    }
                    return getStringForClob(columnIndex);
                default:
                    switch (sQLType) {
                        case 1:
                            break;
                        case 2:
                        case 3:
                            stringVal = getString(columnIndex);
                            if (stringVal == null) {
                                return null;
                            }
                            if (stringVal.length() == 0) {
                                return new BigDecimal(0);
                            }
                            try {
                                return new BigDecimal(stringVal);
                            } catch (NumberFormatException e2) {
                                throw SQLError.createSQLException(Messages.getString("ResultSet.Bad_format_for_BigDecimal", new Object[]{stringVal, Integer.valueOf(columnIndex)}), SQLError.SQL_STATE_ILLEGAL_ARGUMENT, getExceptionInterceptor());
                            }
                        case 4:
                            if (field.isUnsigned()) {
                                if (field.getMysqlType() != 9) {
                                    return Long.valueOf(getLong(columnIndex));
                                }
                            }
                            return Integer.valueOf(getInt(columnIndex));
                        case 5:
                            return Integer.valueOf(getInt(columnIndex));
                        case 6:
                        case 8:
                            return new Double(getDouble(columnIndex));
                        case 7:
                            return new Float(getFloat(columnIndex));
                        default:
                            switch (sQLType) {
                                case 91:
                                    if (field.getMysqlType() != 13 || this.connection.getYearIsDateType()) {
                                        return getDate(columnIndex);
                                    }
                                    return Short.valueOf(getShort(columnIndex));
                                case 92:
                                    return getTime(columnIndex);
                                case 93:
                                    return getTimestamp(columnIndex);
                                default:
                                    return getString(columnIndex);
                            }
                    }
            }
        }
        if (field.isOpaqueBinary()) {
            return getBytes(columnIndex);
        }
        return getString(columnIndex);
    }

    private Object getObjectDeserializingIfNeeded(int columnIndex) throws SQLException {
        Field field = this.fields[columnIndex - 1];
        if (!field.isBinary()) {
            if (!field.isBlob()) {
                return getBytes(columnIndex);
            }
        }
        Object data = getBytes(columnIndex);
        if (!this.connection.getAutoDeserialize()) {
            return data;
        }
        Object obj = data;
        if (data != null && data.length >= 2) {
            if (data[0] != (byte) -84 || data[1] != (byte) -19) {
                return getString(columnIndex);
            }
            try {
                ByteArrayInputStream bytesIn = new ByteArrayInputStream(data);
                ObjectInputStream objIn = new ObjectInputStream(bytesIn);
                obj = objIn.readObject();
                objIn.close();
                bytesIn.close();
            } catch (ClassNotFoundException cnfe) {
                StringBuilder stringBuilder = new StringBuilder();
                stringBuilder.append(Messages.getString("ResultSet.Class_not_found___91"));
                stringBuilder.append(cnfe.toString());
                stringBuilder.append(Messages.getString("ResultSet._while_reading_serialized_object_92"));
                throw SQLError.createSQLException(stringBuilder.toString(), getExceptionInterceptor());
            } catch (IOException e) {
                obj = data;
            }
        }
        return obj;
    }

    public <T> T getObject(int columnIndex, Class<T> type) throws SQLException {
        if (type == null) {
            throw SQLError.createSQLException("Type parameter can not be null", SQLError.SQL_STATE_ILLEGAL_ARGUMENT, getExceptionInterceptor());
        } else if (type.equals(String.class)) {
            return getString(columnIndex);
        } else {
            if (type.equals(BigDecimal.class)) {
                return getBigDecimal(columnIndex);
            }
            if (!type.equals(Boolean.class)) {
                if (!type.equals(Boolean.TYPE)) {
                    if (!type.equals(Integer.class)) {
                        if (!type.equals(Integer.TYPE)) {
                            if (!type.equals(Long.class)) {
                                if (!type.equals(Long.TYPE)) {
                                    if (!type.equals(Float.class)) {
                                        if (!type.equals(Float.TYPE)) {
                                            if (!type.equals(Double.class)) {
                                                if (!type.equals(Double.TYPE)) {
                                                    if (type.equals(byte[].class)) {
                                                        return getBytes(columnIndex);
                                                    }
                                                    if (type.equals(Date.class)) {
                                                        return getDate(columnIndex);
                                                    }
                                                    if (type.equals(Time.class)) {
                                                        return getTime(columnIndex);
                                                    }
                                                    if (type.equals(Timestamp.class)) {
                                                        return getTimestamp(columnIndex);
                                                    }
                                                    if (type.equals(Clob.class)) {
                                                        return getClob(columnIndex);
                                                    }
                                                    if (type.equals(Blob.class)) {
                                                        return getBlob(columnIndex);
                                                    }
                                                    if (type.equals(Array.class)) {
                                                        return getArray(columnIndex);
                                                    }
                                                    if (type.equals(Ref.class)) {
                                                        return getRef(columnIndex);
                                                    }
                                                    if (type.equals(URL.class)) {
                                                        return getURL(columnIndex);
                                                    }
                                                    if (this.connection.getAutoDeserialize()) {
                                                        try {
                                                            return type.cast(getObject(columnIndex));
                                                        } catch (ClassCastException cce) {
                                                            StringBuilder stringBuilder = new StringBuilder();
                                                            stringBuilder.append("Conversion not supported for type ");
                                                            stringBuilder.append(type.getName());
                                                            SQLException sqlEx = SQLError.createSQLException(stringBuilder.toString(), SQLError.SQL_STATE_ILLEGAL_ARGUMENT, getExceptionInterceptor());
                                                            sqlEx.initCause(cce);
                                                            throw sqlEx;
                                                        }
                                                    }
                                                    StringBuilder stringBuilder2 = new StringBuilder();
                                                    stringBuilder2.append("Conversion not supported for type ");
                                                    stringBuilder2.append(type.getName());
                                                    throw SQLError.createSQLException(stringBuilder2.toString(), SQLError.SQL_STATE_ILLEGAL_ARGUMENT, getExceptionInterceptor());
                                                }
                                            }
                                            return Double.valueOf(getDouble(columnIndex));
                                        }
                                    }
                                    return Float.valueOf(getFloat(columnIndex));
                                }
                            }
                            return Long.valueOf(getLong(columnIndex));
                        }
                    }
                    return Integer.valueOf(getInt(columnIndex));
                }
            }
            return Boolean.valueOf(getBoolean(columnIndex));
        }
    }

    public <T> T getObject(String columnLabel, Class<T> type) throws SQLException {
        return getObject(findColumn(columnLabel), (Class) type);
    }

    public Object getObject(int i, Map<String, Class<?>> map) throws SQLException {
        return getObject(i);
    }

    public Object getObject(String columnName) throws SQLException {
        return getObject(findColumn(columnName));
    }

    public Object getObject(String colName, Map<String, Class<?>> map) throws SQLException {
        return getObject(findColumn(colName), (Map) map);
    }

    public Object getObjectStoredProc(int columnIndex, int desiredSqlType) throws SQLException {
        checkRowPos();
        checkColumnBounds(columnIndex);
        if (this.thisRow.getColumnValue(columnIndex - 1) == null) {
            this.wasNullFlag = true;
            return null;
        }
        this.wasNullFlag = false;
        Field field = this.fields[columnIndex - 1];
        if (desiredSqlType != 12) {
            if (desiredSqlType != 16) {
                switch (desiredSqlType) {
                    case -7:
                        break;
                    case -6:
                        return Integer.valueOf(getInt(columnIndex));
                    case -5:
                        if (field.isUnsigned()) {
                            return getBigDecimal(columnIndex);
                        }
                        return Long.valueOf(getLong(columnIndex));
                    case FontRequestCallback.FAIL_REASON_SECURITY_VIOLATION /*-4*/:
                    case FontRequestCallback.FAIL_REASON_FONT_LOAD_ERROR /*-3*/:
                    case -2:
                        return getBytes(columnIndex);
                    case -1:
                        return getStringForClob(columnIndex);
                    default:
                        switch (desiredSqlType) {
                            case 1:
                                break;
                            case 2:
                            case 3:
                                String stringVal = getString(columnIndex);
                                if (stringVal == null) {
                                    return null;
                                }
                                if (stringVal.length() == 0) {
                                    return new BigDecimal(0);
                                }
                                try {
                                    return new BigDecimal(stringVal);
                                } catch (NumberFormatException e) {
                                    throw SQLError.createSQLException(Messages.getString("ResultSet.Bad_format_for_BigDecimal", new Object[]{stringVal, Integer.valueOf(columnIndex)}), SQLError.SQL_STATE_ILLEGAL_ARGUMENT, getExceptionInterceptor());
                                }
                            case 4:
                                if (field.isUnsigned()) {
                                    if (field.getMysqlType() != 9) {
                                        return Long.valueOf(getLong(columnIndex));
                                    }
                                }
                                return Integer.valueOf(getInt(columnIndex));
                            case 5:
                                return Integer.valueOf(getInt(columnIndex));
                            case 6:
                                if (this.connection.getRunningCTS13()) {
                                    return new Float(getFloat(columnIndex));
                                }
                                return new Double((double) getFloat(columnIndex));
                            case 7:
                                return new Float(getFloat(columnIndex));
                            case 8:
                                return new Double(getDouble(columnIndex));
                            default:
                                switch (desiredSqlType) {
                                    case 91:
                                        if (field.getMysqlType() != 13 || this.connection.getYearIsDateType()) {
                                            return getDate(columnIndex);
                                        }
                                        return Short.valueOf(getShort(columnIndex));
                                    case 92:
                                        return getTime(columnIndex);
                                    case 93:
                                        return getTimestamp(columnIndex);
                                    default:
                                        return getString(columnIndex);
                                }
                        }
                }
            }
            return Boolean.valueOf(getBoolean(columnIndex));
        }
        return getString(columnIndex);
    }

    public Object getObjectStoredProc(int i, Map<Object, Object> map, int desiredSqlType) throws SQLException {
        return getObjectStoredProc(i, desiredSqlType);
    }

    public Object getObjectStoredProc(String columnName, int desiredSqlType) throws SQLException {
        return getObjectStoredProc(findColumn(columnName), desiredSqlType);
    }

    public Object getObjectStoredProc(String colName, Map<Object, Object> map, int desiredSqlType) throws SQLException {
        return getObjectStoredProc(findColumn(colName), (Map) map, desiredSqlType);
    }

    public Ref getRef(int i) throws SQLException {
        checkColumnBounds(i);
        throw SQLError.createSQLFeatureNotSupportedException();
    }

    public Ref getRef(String colName) throws SQLException {
        return getRef(findColumn(colName));
    }

    public int getRow() throws SQLException {
        checkClosed();
        int currentRowNumber = this.rowData.getCurrentRowNumber();
        if (this.rowData.isDynamic()) {
            return currentRowNumber + 1;
        }
        if (currentRowNumber >= 0 && !this.rowData.isAfterLast()) {
            if (!this.rowData.isEmpty()) {
                return currentRowNumber + 1;
            }
        }
        return 0;
    }

    public String getServerInfo() {
        try {
            String str;
            synchronized (checkClosed().getConnectionMutex()) {
                str = this.serverInfo;
            }
            return str;
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    private long getNumericRepresentationOfSQLBitType(int columnIndex) throws SQLException {
        Object value = this.thisRow.getColumnValue(columnIndex - 1);
        int i = 0;
        if (!this.fields[columnIndex - 1].isSingleBit()) {
            if (((byte[]) value).length != 1) {
                byte[] asBytes = (byte[]) value;
                int shift = 0;
                long[] steps = new long[asBytes.length];
                int i2 = asBytes.length - 1;
                while (true) {
                    int i3 = i2;
                    if (i3 < 0) {
                        break;
                    }
                    steps[i3] = ((long) (asBytes[i3] & 255)) << shift;
                    shift += 8;
                    i2 = i3 - 1;
                }
                long valueAsLong = 0;
                while (i < asBytes.length) {
                    i++;
                    valueAsLong |= steps[i];
                }
                return valueAsLong;
            }
        }
        return (long) ((byte[]) value)[0];
    }

    public short getShort(int columnIndex) throws SQLException {
        checkRowPos();
        checkColumnBounds(columnIndex);
        if (this.isBinaryEncoded) {
            return getNativeShort(columnIndex);
        }
        if (this.thisRow.isNull(columnIndex - 1)) {
            this.wasNullFlag = true;
            return (short) 0;
        }
        this.wasNullFlag = false;
        if (this.fields[columnIndex - 1].getMysqlType() == 16) {
            long valueAsLong = getNumericRepresentationOfSQLBitType(columnIndex);
            if (this.jdbcCompliantTruncationForReads && (valueAsLong < -32768 || valueAsLong > 32767)) {
                throwRangeException(String.valueOf(valueAsLong), columnIndex, 5);
            }
            return (short) ((int) valueAsLong);
        }
        if (this.useFastIntParsing) {
            byte[] shortAsBytes = this.thisRow.getColumnValue(columnIndex - 1);
            if (shortAsBytes.length == 0) {
                return (short) convertToZeroWithEmptyCheck();
            }
            boolean needsFullParse = false;
            int i = 0;
            while (i < shortAsBytes.length) {
                if (((char) shortAsBytes[i]) != 'e') {
                    if (((char) shortAsBytes[i]) != 'E') {
                        i++;
                    }
                }
                needsFullParse = true;
            }
            if (!needsFullParse) {
                try {
                    return parseShortWithOverflowCheck(columnIndex, shortAsBytes, null);
                } catch (NumberFormatException e) {
                    try {
                        return parseShortAsDouble(columnIndex, StringUtils.toString(shortAsBytes));
                    } catch (NumberFormatException e2) {
                        StringBuilder stringBuilder = new StringBuilder();
                        stringBuilder.append(Messages.getString("ResultSet.Invalid_value_for_getShort()_-____96"));
                        stringBuilder.append(StringUtils.toString(shortAsBytes));
                        stringBuilder.append("'");
                        throw SQLError.createSQLException(stringBuilder.toString(), SQLError.SQL_STATE_ILLEGAL_ARGUMENT, getExceptionInterceptor());
                    }
                }
            }
        }
        String val = null;
        try {
            val = getString(columnIndex);
            if (val == null) {
                return (short) 0;
            }
            if (val.length() == 0) {
                return (short) convertToZeroWithEmptyCheck();
            }
            if (val.indexOf("e") == -1 && val.indexOf("E") == -1 && val.indexOf(".") == -1) {
                return parseShortWithOverflowCheck(columnIndex, null, val);
            }
            return parseShortAsDouble(columnIndex, val);
        } catch (NumberFormatException e3) {
            try {
                return parseShortAsDouble(columnIndex, val);
            } catch (NumberFormatException e4) {
                stringBuilder = new StringBuilder();
                stringBuilder.append(Messages.getString("ResultSet.Invalid_value_for_getShort()_-____96"));
                stringBuilder.append(val);
                stringBuilder.append("'");
                throw SQLError.createSQLException(stringBuilder.toString(), SQLError.SQL_STATE_ILLEGAL_ARGUMENT, getExceptionInterceptor());
            }
        }
    }

    public short getShort(String columnName) throws SQLException {
        return getShort(findColumn(columnName));
    }

    private final short getShortFromString(String val, int columnIndex) throws SQLException {
        if (val == null) {
            return (short) 0;
        }
        try {
            if (val.length() == 0) {
                return (short) convertToZeroWithEmptyCheck();
            }
            if (val.indexOf("e") == -1 && val.indexOf("E") == -1 && val.indexOf(".") == -1) {
                return parseShortWithOverflowCheck(columnIndex, null, val);
            }
            return parseShortAsDouble(columnIndex, val);
        } catch (NumberFormatException e) {
            try {
                return parseShortAsDouble(columnIndex, val);
            } catch (NumberFormatException e2) {
                StringBuilder stringBuilder = new StringBuilder();
                stringBuilder.append(Messages.getString("ResultSet.Invalid_value_for_getShort()_-____217"));
                stringBuilder.append(val);
                stringBuilder.append(Messages.getString("ResultSet.___in_column__218"));
                stringBuilder.append(columnIndex);
                throw SQLError.createSQLException(stringBuilder.toString(), SQLError.SQL_STATE_ILLEGAL_ARGUMENT, getExceptionInterceptor());
            }
        }
    }

    public Statement getStatement() throws SQLException {
        try {
            synchronized (checkClosed().getConnectionMutex()) {
                Statement statement;
                if (this.wrapperStatement != null) {
                    statement = this.wrapperStatement;
                    return statement;
                }
                statement = this.owningStatement;
                return statement;
            }
        } catch (SQLException e) {
            if (!this.retainOwningStatement) {
                throw SQLError.createSQLException("Operation not allowed on closed ResultSet. Statements can be retained over result set closure by setting the connection property \"retainStatementAfterResultSetClose\" to \"true\".", SQLError.SQL_STATE_GENERAL_ERROR, getExceptionInterceptor());
            } else if (this.wrapperStatement != null) {
                return this.wrapperStatement;
            } else {
                return this.owningStatement;
            }
        }
    }

    public String getString(int columnIndex) throws SQLException {
        String stringVal = getStringInternal(columnIndex, true);
        if (!this.padCharsWithSpace || stringVal == null) {
            return stringVal;
        }
        Field f = this.fields[columnIndex - 1];
        if (f.getMysqlType() != 254) {
            return stringVal;
        }
        int fieldLength = ((int) f.getLength()) / f.getMaxBytesPerCharacter();
        int currentLength = stringVal.length();
        if (currentLength >= fieldLength) {
            return stringVal;
        }
        StringBuilder paddedBuf = new StringBuilder(fieldLength);
        paddedBuf.append(stringVal);
        paddedBuf.append(EMPTY_SPACE, 0, fieldLength - currentLength);
        return paddedBuf.toString();
    }

    public String getString(String columnName) throws SQLException {
        return getString(findColumn(columnName));
    }

    private String getStringForClob(int columnIndex) throws SQLException {
        String asString = null;
        String forcedEncoding = this.connection.getClobCharacterEncoding();
        if (forcedEncoding != null) {
            try {
                byte[] asBytes;
                if (this.isBinaryEncoded) {
                    asBytes = getNativeBytes(columnIndex, true);
                } else {
                    asBytes = getBytes(columnIndex);
                }
                if (asBytes != null) {
                    asString = StringUtils.toString(asBytes, forcedEncoding);
                }
                return asString;
            } catch (UnsupportedEncodingException e) {
                StringBuilder stringBuilder = new StringBuilder();
                stringBuilder.append("Unsupported character encoding ");
                stringBuilder.append(forcedEncoding);
                throw SQLError.createSQLException(stringBuilder.toString(), SQLError.SQL_STATE_ILLEGAL_ARGUMENT, getExceptionInterceptor());
            }
        } else if (this.isBinaryEncoded) {
            return getNativeString(columnIndex);
        } else {
            return getString(columnIndex);
        }
    }

    protected String getStringInternal(int columnIndex, boolean checkDateTypes) throws SQLException {
        int i = columnIndex;
        if (this.isBinaryEncoded) {
            return getNativeString(columnIndex);
        }
        checkRowPos();
        checkColumnBounds(columnIndex);
        if (r6.fields == null) {
            throw SQLError.createSQLException(Messages.getString("ResultSet.Query_generated_no_fields_for_ResultSet_99"), SQLError.SQL_STATE_INVALID_COLUMN_NUMBER, getExceptionInterceptor());
        }
        int internalColumnIndex = i - 1;
        if (r6.thisRow.isNull(internalColumnIndex)) {
            r6.wasNullFlag = true;
            return null;
        }
        r6.wasNullFlag = false;
        Field metadata = r6.fields[internalColumnIndex];
        if (metadata.getMysqlType() != 16) {
            String stringVal = r6.thisRow.getString(internalColumnIndex, metadata.getEncoding(), r6.connection);
            Date dt;
            if (metadata.getMysqlType() != 13) {
                if (checkDateTypes && !r6.connection.getNoDatetimeStringSync()) {
                    switch (metadata.getSQLType()) {
                        case 91:
                            dt = getDateFromString(stringVal, i, null);
                            if (dt == null) {
                                r6.wasNullFlag = true;
                                return null;
                            }
                            r6.wasNullFlag = false;
                            return dt.toString();
                        case 92:
                            Time tm = getTimeFromString(stringVal, null, i, getDefaultTimeZone(), false);
                            if (tm == null) {
                                r6.wasNullFlag = true;
                                return null;
                            }
                            r6.wasNullFlag = false;
                            return tm.toString();
                        case 93:
                            Timestamp ts = getTimestampFromString(i, null, stringVal, getDefaultTimeZone(), false);
                            if (ts == null) {
                                r6.wasNullFlag = true;
                                return null;
                            }
                            r6.wasNullFlag = false;
                            return ts.toString();
                        default:
                            break;
                    }
                }
                return stringVal;
            } else if (!r6.connection.getYearIsDateType()) {
                return stringVal;
            } else {
                dt = getDateFromString(stringVal, i, null);
                if (dt == null) {
                    r6.wasNullFlag = true;
                    return null;
                }
                r6.wasNullFlag = false;
                return dt.toString();
            }
        } else if (!metadata.isSingleBit()) {
            return String.valueOf(getNumericRepresentationOfSQLBitType(columnIndex));
        } else {
            byte[] value = r6.thisRow.getColumnValue(internalColumnIndex);
            if (value.length == 0) {
                return String.valueOf(convertToZeroWithEmptyCheck());
            }
            return String.valueOf(value[0]);
        }
    }

    public Time getTime(int columnIndex) throws SQLException {
        return getTimeInternal(columnIndex, null, getDefaultTimeZone(), false);
    }

    public Time getTime(int columnIndex, Calendar cal) throws SQLException {
        return getTimeInternal(columnIndex, cal, cal != null ? cal.getTimeZone() : getDefaultTimeZone(), true);
    }

    public Time getTime(String columnName) throws SQLException {
        return getTime(findColumn(columnName));
    }

    public Time getTime(String columnName, Calendar cal) throws SQLException {
        return getTime(findColumn(columnName), cal);
    }

    private Time getTimeFromString(String timeAsString, Calendar targetCalendar, int columnIndex, TimeZone tz, boolean rollForward) throws SQLException {
        int sec;
        int min;
        ResultSetImpl resultSetImpl = this;
        Calendar calendar = targetCalendar;
        int i = columnIndex;
        synchronized (checkClosed().getConnectionMutex()) {
            int sec2 = 0;
            if (timeAsString == null) {
                try {
                    resultSetImpl.wasNullFlag = true;
                } catch (RuntimeException e) {
                    RuntimeException e2 = e;
                    String str = timeAsString;
                    int hr = 0;
                    RuntimeException ex = e2;
                    SQLException sqlEx = SQLError.createSQLException(ex.toString(), SQLError.SQL_STATE_ILLEGAL_ARGUMENT, getExceptionInterceptor());
                    sqlEx.initCause(ex);
                    throw sqlEx;
                }
                try {
                    return null;
                } catch (Throwable th) {
                    Throwable th2 = th;
                    Throwable th3 = th2;
                    throw th3;
                }
            }
            String timeAsString2 = timeAsString.trim();
            try {
                int dec = timeAsString2.indexOf(".");
                if (dec > -1) {
                    str = timeAsString2.substring(0, dec);
                } else {
                    str = timeAsString2;
                }
            } catch (RuntimeException e3) {
                e2 = e3;
                hr = 0;
                str = timeAsString2;
                RuntimeException ex2 = e2;
                SQLException sqlEx2 = SQLError.createSQLException(ex2.toString(), SQLError.SQL_STATE_ILLEGAL_ARGUMENT, getExceptionInterceptor());
                sqlEx2.initCause(ex2);
                throw sqlEx2;
            } catch (Throwable th22) {
                th3 = th22;
                str = timeAsString2;
                throw th3;
            }
            try {
                StringBuilder stringBuilder;
                Time fastTimeCreate;
                if (!(str.equals("0") || str.equals("0000-00-00") || str.equals("0000-00-00 00:00:00"))) {
                    if (!str.equals("00000000000000")) {
                        int hr2;
                        int min2;
                        int min3;
                        int sec3;
                        Calendar sessionCalendar;
                        Time changeTimezone;
                        resultSetImpl.wasNullFlag = false;
                        Field timeColField = resultSetImpl.fields[i - 1];
                        SQLWarning precisionLost;
                        if (timeColField.getMysqlType() == 7) {
                            int sec4;
                            int length = str.length();
                            if (length == 10) {
                                hr2 = Integer.parseInt(str.substring(6, 8));
                                min2 = Integer.parseInt(str.substring(8, 10));
                                sec4 = 0;
                            } else if (length == 12 || length == 14) {
                                hr2 = Integer.parseInt(str.substring(length - 6, length - 4));
                                min2 = Integer.parseInt(str.substring(length - 4, length - 2));
                                sec4 = Integer.parseInt(str.substring(length - 2, length));
                            } else if (length != 19) {
                                stringBuilder = new StringBuilder();
                                stringBuilder.append(Messages.getString("ResultSet.Timestamp_too_small_to_convert_to_Time_value_in_column__257"));
                                stringBuilder.append(i);
                                stringBuilder.append("(");
                                stringBuilder.append(resultSetImpl.fields[i - 1]);
                                stringBuilder.append(").");
                                throw SQLError.createSQLException(stringBuilder.toString(), SQLError.SQL_STATE_ILLEGAL_ARGUMENT, getExceptionInterceptor());
                            } else {
                                hr2 = Integer.parseInt(str.substring(length - 8, length - 6));
                                min2 = Integer.parseInt(str.substring(length - 5, length - 3));
                                sec4 = Integer.parseInt(str.substring(length - 2, length));
                            }
                            sec2 = sec4;
                            StringBuilder stringBuilder2 = new StringBuilder();
                            stringBuilder2.append(Messages.getString("ResultSet.Precision_lost_converting_TIMESTAMP_to_Time_with_getTime()_on_column__261"));
                            stringBuilder2.append(i);
                            stringBuilder2.append("(");
                            stringBuilder2.append(resultSetImpl.fields[i - 1]);
                            stringBuilder2.append(").");
                            precisionLost = new SQLWarning(stringBuilder2.toString());
                            if (resultSetImpl.warningChain == null) {
                                resultSetImpl.warningChain = precisionLost;
                            } else {
                                resultSetImpl.warningChain.setNextWarning(precisionLost);
                            }
                        } else if (timeColField.getMysqlType() == 12) {
                            hr2 = Integer.parseInt(str.substring(11, 13));
                            min2 = Integer.parseInt(str.substring(14, 16));
                            sec2 = Integer.parseInt(str.substring(17, 19));
                            StringBuilder stringBuilder3 = new StringBuilder();
                            stringBuilder3.append(Messages.getString("ResultSet.Precision_lost_converting_DATETIME_to_Time_with_getTime()_on_column__264"));
                            stringBuilder3.append(i);
                            stringBuilder3.append("(");
                            stringBuilder3.append(resultSetImpl.fields[i - 1]);
                            stringBuilder3.append(").");
                            precisionLost = new SQLWarning(stringBuilder3.toString());
                            if (resultSetImpl.warningChain == null) {
                                resultSetImpl.warningChain = precisionLost;
                            } else {
                                resultSetImpl.warningChain.setNextWarning(precisionLost);
                            }
                        } else if (timeColField.getMysqlType() == 10) {
                            fastTimeCreate = fastTimeCreate(calendar, 0, 0, 0);
                            return fastTimeCreate;
                        } else if (str.length() == 5 || str.length() == 8) {
                            hr2 = Integer.parseInt(str.substring(0, 2));
                            hr = hr2;
                            min3 = Integer.parseInt(str.substring(3, 5));
                            sec3 = str.length() == 5 ? 0 : Integer.parseInt(str.substring(6));
                            sessionCalendar = getCalendarInstanceForSessionOrNew();
                            sec = sec3;
                            min = min3;
                            changeTimezone = TimeUtil.changeTimezone(resultSetImpl.connection, sessionCalendar, calendar, fastTimeCreate(sessionCalendar, hr, min3, sec3), resultSetImpl.connection.getServerTimezoneTZ(), tz, rollForward);
                            return changeTimezone;
                        } else {
                            stringBuilder = new StringBuilder();
                            stringBuilder.append(Messages.getString("ResultSet.Bad_format_for_Time____267"));
                            stringBuilder.append(str);
                            stringBuilder.append(Messages.getString("ResultSet.___in_column__268"));
                            stringBuilder.append(i);
                            throw SQLError.createSQLException(stringBuilder.toString(), SQLError.SQL_STATE_ILLEGAL_ARGUMENT, getExceptionInterceptor());
                        }
                        hr = hr2;
                        min3 = min2;
                        sec3 = sec2;
                        try {
                            sessionCalendar = getCalendarInstanceForSessionOrNew();
                            sec = sec3;
                            min = min3;
                        } catch (RuntimeException e22) {
                            ex2 = e22;
                            sec2 = sec3;
                            min2 = min3;
                            SQLException sqlEx22 = SQLError.createSQLException(ex2.toString(), SQLError.SQL_STATE_ILLEGAL_ARGUMENT, getExceptionInterceptor());
                            sqlEx22.initCause(ex2);
                            throw sqlEx22;
                        }
                        try {
                            changeTimezone = TimeUtil.changeTimezone(resultSetImpl.connection, sessionCalendar, calendar, fastTimeCreate(sessionCalendar, hr, min3, sec3), resultSetImpl.connection.getServerTimezoneTZ(), tz, rollForward);
                            return changeTimezone;
                        } catch (RuntimeException e222) {
                            ex2 = e222;
                            sec2 = sec;
                            min2 = min;
                            SQLException sqlEx222 = SQLError.createSQLException(ex2.toString(), SQLError.SQL_STATE_ILLEGAL_ARGUMENT, getExceptionInterceptor());
                            sqlEx222.initCause(ex2);
                            throw sqlEx222;
                        }
                    }
                }
                if ("convertToNull".equals(resultSetImpl.connection.getZeroDateTimeBehavior())) {
                    resultSetImpl.wasNullFlag = true;
                    return null;
                } else if ("exception".equals(resultSetImpl.connection.getZeroDateTimeBehavior())) {
                    stringBuilder = new StringBuilder();
                    stringBuilder.append("Value '");
                    stringBuilder.append(str);
                    stringBuilder.append("' can not be represented as java.sql.Time");
                    throw SQLError.createSQLException(stringBuilder.toString(), SQLError.SQL_STATE_ILLEGAL_ARGUMENT, getExceptionInterceptor());
                } else {
                    fastTimeCreate = fastTimeCreate(calendar, 0, 0, 0);
                    return fastTimeCreate;
                }
            } catch (RuntimeException e4) {
                e222 = e4;
                int hr3 = 0;
                RuntimeException ex22 = e222;
                SQLException sqlEx2222 = SQLError.createSQLException(ex22.toString(), SQLError.SQL_STATE_ILLEGAL_ARGUMENT, getExceptionInterceptor());
                sqlEx2222.initCause(ex22);
                throw sqlEx2222;
            } catch (Throwable th4) {
                th22 = th4;
                Throwable th32 = th22;
                throw th32;
            }
        }
    }

    private Time getTimeInternal(int columnIndex, Calendar targetCalendar, TimeZone tz, boolean rollForward) throws SQLException {
        checkRowPos();
        if (this.isBinaryEncoded) {
            return getNativeTime(columnIndex, targetCalendar, tz, rollForward);
        }
        if (!this.useFastDateParsing) {
            return getTimeFromString(getStringInternal(columnIndex, false), targetCalendar, columnIndex, tz, rollForward);
        }
        checkColumnBounds(columnIndex);
        int columnIndexMinusOne = columnIndex - 1;
        if (this.thisRow.isNull(columnIndexMinusOne)) {
            this.wasNullFlag = true;
            return null;
        }
        this.wasNullFlag = false;
        return this.thisRow.getTimeFast(columnIndexMinusOne, targetCalendar, tz, rollForward, this.connection, this);
    }

    public Timestamp getTimestamp(int columnIndex) throws SQLException {
        return getTimestampInternal(columnIndex, null, getDefaultTimeZone(), false);
    }

    public Timestamp getTimestamp(int columnIndex, Calendar cal) throws SQLException {
        return getTimestampInternal(columnIndex, cal, cal != null ? cal.getTimeZone() : getDefaultTimeZone(), true);
    }

    public Timestamp getTimestamp(String columnName) throws SQLException {
        return getTimestamp(findColumn(columnName));
    }

    public Timestamp getTimestamp(String columnName, Calendar cal) throws SQLException {
        return getTimestamp(findColumn(columnName), cal);
    }

    private Timestamp getTimestampFromString(int columnIndex, Calendar targetCalendar, String timestampValue, TimeZone tz, boolean rollForward) throws SQLException {
        RuntimeException e;
        RuntimeException e2;
        int i;
        StringBuilder stringBuilder;
        SQLException sqlEx;
        int i2 = columnIndex;
        String timestampValue2;
        ResultSetImpl resultSetImpl;
        try {
            this.wasNullFlag = false;
            if (timestampValue == null) {
                try {
                    r15.wasNullFlag = true;
                    return null;
                } catch (RuntimeException e3) {
                    e = e3;
                    timestampValue2 = timestampValue;
                    e2 = e;
                    i = i2;
                    resultSetImpl = r15;
                    stringBuilder = new StringBuilder();
                    stringBuilder.append("Cannot convert value '");
                    stringBuilder.append(timestampValue2);
                    stringBuilder.append("' from column ");
                    stringBuilder.append(i);
                    stringBuilder.append(" to TIMESTAMP.");
                    sqlEx = SQLError.createSQLException(stringBuilder.toString(), SQLError.SQL_STATE_ILLEGAL_ARGUMENT, getExceptionInterceptor());
                    sqlEx.initCause(e2);
                    throw sqlEx;
                }
            }
            timestampValue2 = timestampValue.trim();
            try {
                Calendar utcCalendar;
                i = timestampValue2.length();
                if (r15.connection.getUseJDBCCompliantTimezoneShift()) {
                    try {
                        utcCalendar = r15.connection.getUtcCalendar();
                    } catch (RuntimeException e4) {
                        e = e4;
                        e2 = e;
                        i = i2;
                        resultSetImpl = r15;
                        stringBuilder = new StringBuilder();
                        stringBuilder.append("Cannot convert value '");
                        stringBuilder.append(timestampValue2);
                        stringBuilder.append("' from column ");
                        stringBuilder.append(i);
                        stringBuilder.append(" to TIMESTAMP.");
                        sqlEx = SQLError.createSQLException(stringBuilder.toString(), SQLError.SQL_STATE_ILLEGAL_ARGUMENT, getExceptionInterceptor());
                        sqlEx.initCause(e2);
                        throw sqlEx;
                    }
                }
                utcCalendar = getCalendarInstanceForSessionOrNew();
                Calendar sessionCalendar = utcCalendar;
                if (i > 0) {
                    if (timestampValue2.charAt(0) == '0' && (timestampValue2.equals("0000-00-00") || timestampValue2.equals("0000-00-00 00:00:00") || timestampValue2.equals("00000000000000") || timestampValue2.equals("0"))) {
                        if ("convertToNull".equals(r15.connection.getZeroDateTimeBehavior())) {
                            r15.wasNullFlag = true;
                            return null;
                        } else if (!"exception".equals(r15.connection.getZeroDateTimeBehavior())) {
                            return fastTimestampCreate(null, 1, 1, 1, 0, 0, 0, 0);
                        } else {
                            StringBuilder stringBuilder2 = new StringBuilder();
                            stringBuilder2.append("Value '");
                            stringBuilder2.append(timestampValue2);
                            stringBuilder2.append("' can not be represented as java.sql.Timestamp");
                            throw SQLError.createSQLException(stringBuilder2.toString(), SQLError.SQL_STATE_ILLEGAL_ARGUMENT, getExceptionInterceptor());
                        }
                    }
                }
                if (r15.fields[i2 - 1].getMysqlType() == 13) {
                    try {
                        if (r15.useLegacyDatetimeCode) {
                            int i3 = i2;
                            try {
                                resultSetImpl = r7;
                                i = i3;
                                try {
                                    return TimeUtil.changeTimezone(r15.connection, sessionCalendar, targetCalendar, fastTimestampCreate(sessionCalendar, Integer.parseInt(timestampValue2.substring(0, 4)), 1, 1, 0, 0, 0, 0), r7.connection.getServerTimezoneTZ(), tz, rollForward);
                                } catch (RuntimeException e5) {
                                    e = e5;
                                    timestampValue2 = timestampValue2;
                                    e2 = e;
                                    stringBuilder = new StringBuilder();
                                    stringBuilder.append("Cannot convert value '");
                                    stringBuilder.append(timestampValue2);
                                    stringBuilder.append("' from column ");
                                    stringBuilder.append(i);
                                    stringBuilder.append(" to TIMESTAMP.");
                                    sqlEx = SQLError.createSQLException(stringBuilder.toString(), SQLError.SQL_STATE_ILLEGAL_ARGUMENT, getExceptionInterceptor());
                                    sqlEx.initCause(e2);
                                    throw sqlEx;
                                }
                            } catch (RuntimeException e6) {
                                e = e6;
                                resultSetImpl = r15;
                                i = i3;
                                timestampValue2 = timestampValue2;
                                e2 = e;
                                stringBuilder = new StringBuilder();
                                stringBuilder.append("Cannot convert value '");
                                stringBuilder.append(timestampValue2);
                                stringBuilder.append("' from column ");
                                stringBuilder.append(i);
                                stringBuilder.append(" to TIMESTAMP.");
                                sqlEx = SQLError.createSQLException(stringBuilder.toString(), SQLError.SQL_STATE_ILLEGAL_ARGUMENT, getExceptionInterceptor());
                                sqlEx.initCause(e2);
                                throw sqlEx;
                            }
                        }
                        return TimeUtil.fastTimestampCreate(tz, Integer.parseInt(timestampValue2.substring(0, 4)), 1, 1, 0, 0, 0, 0);
                    } catch (RuntimeException e7) {
                        String str = timestampValue2;
                        i = i2;
                        resultSetImpl = r15;
                        e2 = e7;
                        stringBuilder = new StringBuilder();
                        stringBuilder.append("Cannot convert value '");
                        stringBuilder.append(timestampValue2);
                        stringBuilder.append("' from column ");
                        stringBuilder.append(i);
                        stringBuilder.append(" to TIMESTAMP.");
                        sqlEx = SQLError.createSQLException(stringBuilder.toString(), SQLError.SQL_STATE_ILLEGAL_ARGUMENT, getExceptionInterceptor());
                        sqlEx.initCause(e2);
                        throw sqlEx;
                    }
                }
                int length = i;
                i = i2;
                resultSetImpl = r15;
                try {
                    int i4;
                    int nanos;
                    int nanos2;
                    int month;
                    int day;
                    int day2;
                    int month2;
                    int hour;
                    int year;
                    int minutes;
                    int decimalIndex = timestampValue2.indexOf(".");
                    int i5;
                    int i6;
                    if (decimalIndex == length - 1) {
                        i4 = 0;
                        i5 = 0;
                        i6 = 0;
                        nanos = 0;
                        i2 = length - 1;
                    } else if (decimalIndex == -1) {
                        i4 = 0;
                        i5 = 0;
                        i6 = 0;
                        nanos = 0;
                        i2 = length;
                    } else if (decimalIndex + 2 <= length) {
                        nanos2 = Integer.parseInt(timestampValue2.substring(decimalIndex + 1));
                        i2 = length - (decimalIndex + 1);
                        if (i2 < 9) {
                            i4 = 0;
                            i5 = 0;
                            i6 = 0;
                            nanos2 *= (int) Math.pow(0, (double) (9 - i2));
                        } else {
                            i4 = 0;
                            i5 = 0;
                            i6 = 0;
                        }
                        nanos = nanos2;
                        i2 = decimalIndex;
                    } else {
                        i4 = 0;
                        i5 = 0;
                        i6 = 0;
                        throw new IllegalArgumentException();
                    }
                    if (i2 == 2) {
                        nanos2 = Integer.parseInt(timestampValue2.substring(0, 2));
                        if (nanos2 <= 69) {
                            nanos2 += 100;
                        }
                        nanos2 += MysqlErrorNumbers.ER_SLAVE_SQL_THREAD_MUST_STOP;
                        month = 1;
                        day = 1;
                    } else if (i2 == 4) {
                        nanos2 = Integer.parseInt(timestampValue2.substring(0, 2));
                        if (nanos2 <= 69) {
                            nanos2 += 100;
                        }
                        nanos2 += MysqlErrorNumbers.ER_SLAVE_SQL_THREAD_MUST_STOP;
                        month = Integer.parseInt(timestampValue2.substring(2, 4));
                        day = 1;
                    } else if (i2 != 6) {
                        if (i2 != 8) {
                            int year2;
                            int minutes2;
                            if (i2 != 10) {
                                int hour2;
                                if (i2 == 12) {
                                    year2 = Integer.parseInt(timestampValue2.substring(0, 2));
                                    if (year2 <= 69) {
                                        year2 += 100;
                                    }
                                    year2 += MysqlErrorNumbers.ER_SLAVE_SQL_THREAD_MUST_STOP;
                                    month = Integer.parseInt(timestampValue2.substring(2, 4));
                                    nanos2 = Integer.parseInt(timestampValue2.substring(4, 6));
                                    day = Integer.parseInt(timestampValue2.substring(6, 8));
                                    minutes2 = Integer.parseInt(timestampValue2.substring(8, 10));
                                    hour2 = Integer.parseInt(timestampValue2.substring(10, 12));
                                    day2 = nanos2;
                                    month2 = month;
                                    hour = day;
                                    year = year2;
                                    minutes = minutes2;
                                } else if (i2 != 14) {
                                    switch (i2) {
                                        case 19:
                                        case 20:
                                        case 21:
                                        case 22:
                                        case 23:
                                        case 24:
                                        case 25:
                                        case 26:
                                            nanos2 = Integer.parseInt(timestampValue2.substring(0, 4));
                                            day = Integer.parseInt(timestampValue2.substring(5, 7));
                                            year2 = Integer.parseInt(timestampValue2.substring(8, 10));
                                            minutes2 = Integer.parseInt(timestampValue2.substring(11, 13));
                                            month = Integer.parseInt(timestampValue2.substring(14, 16));
                                            hour2 = Integer.parseInt(timestampValue2.substring(17, 19));
                                            year = nanos2;
                                            minutes = month;
                                            month2 = day;
                                            day2 = year2;
                                            hour = minutes2;
                                            break;
                                        default:
                                            stringBuilder = new StringBuilder();
                                            stringBuilder.append("Bad format for Timestamp '");
                                            stringBuilder.append(timestampValue2);
                                            stringBuilder.append("' in column ");
                                            stringBuilder.append(i);
                                            stringBuilder.append(".");
                                            throw new SQLException(stringBuilder.toString(), SQLError.SQL_STATE_ILLEGAL_ARGUMENT);
                                    }
                                } else {
                                    month = Integer.parseInt(timestampValue2.substring(0, 4));
                                    year2 = Integer.parseInt(timestampValue2.substring(4, 6));
                                    day = Integer.parseInt(timestampValue2.substring(6, 8));
                                    minutes2 = Integer.parseInt(timestampValue2.substring(8, 10));
                                    hour2 = Integer.parseInt(timestampValue2.substring(10, 12));
                                    i4 = Integer.parseInt(timestampValue2.substring(12, 14));
                                    year = month;
                                    day2 = day;
                                    month2 = year2;
                                    hour = minutes2;
                                    minutes = hour2;
                                }
                                i4 = hour2;
                            } else {
                                if (resultSetImpl.fields[i - 1].getMysqlType() != 10) {
                                    if (timestampValue2.indexOf("-") == -1) {
                                        nanos2 = Integer.parseInt(timestampValue2.substring(0, 2));
                                        if (nanos2 <= 69) {
                                            nanos2 += 100;
                                        }
                                        month = Integer.parseInt(timestampValue2.substring(2, 4));
                                        year2 = Integer.parseInt(timestampValue2.substring(4, 6));
                                        day = Integer.parseInt(timestampValue2.substring(6, 8));
                                        minutes2 = Integer.parseInt(timestampValue2.substring(8, 10));
                                        year = nanos2 + MysqlErrorNumbers.ER_SLAVE_SQL_THREAD_MUST_STOP;
                                        month2 = month;
                                        hour = day;
                                        day2 = year2;
                                        minutes = minutes2;
                                        i4 = 0;
                                    }
                                }
                                nanos2 = Integer.parseInt(timestampValue2.substring(0, 4));
                                minutes2 = 0;
                                year = nanos2;
                                month2 = Integer.parseInt(timestampValue2.substring(5, 7));
                                day2 = Integer.parseInt(timestampValue2.substring(8, 10));
                                hour = 0;
                                minutes = minutes2;
                                i4 = 0;
                            }
                        } else if (timestampValue2.indexOf(":") != -1) {
                            nanos2 = Integer.parseInt(timestampValue2.substring(0, 2));
                            hour = nanos2;
                            minutes = Integer.parseInt(timestampValue2.substring(3, 5));
                            i4 = Integer.parseInt(timestampValue2.substring(6, 8));
                            year = 1970;
                            month2 = 1;
                            day2 = 1;
                        } else {
                            month = Integer.parseInt(timestampValue2.substring(0, 4));
                            nanos2 = Integer.parseInt(timestampValue2.substring(4, 6));
                            day = Integer.parseInt(timestampValue2.substring(6, 8));
                            month2 = nanos2 - 1;
                            year = month - 1900;
                            day2 = day;
                            hour = 0;
                            minutes = 0;
                            i4 = 0;
                        }
                        if (!resultSetImpl.useLegacyDatetimeCode) {
                            return TimeUtil.fastTimestampCreate(tz, year, month2, day2, hour, minutes, i4, nanos);
                        }
                        return TimeUtil.changeTimezone(resultSetImpl.connection, sessionCalendar, targetCalendar, fastTimestampCreate(sessionCalendar, year, month2, day2, hour, minutes, i4, nanos), resultSetImpl.connection.getServerTimezoneTZ(), tz, rollForward);
                    } else {
                        nanos2 = Integer.parseInt(timestampValue2.substring(0, 2));
                        if (nanos2 <= 69) {
                            nanos2 += 100;
                        }
                        nanos2 += MysqlErrorNumbers.ER_SLAVE_SQL_THREAD_MUST_STOP;
                        month = Integer.parseInt(timestampValue2.substring(2, 4));
                        day = Integer.parseInt(timestampValue2.substring(4, 6));
                    }
                    year = nanos2;
                    month2 = month;
                    day2 = day;
                    hour = 0;
                    minutes = 0;
                    i4 = 0;
                    if (!resultSetImpl.useLegacyDatetimeCode) {
                        return TimeUtil.fastTimestampCreate(tz, year, month2, day2, hour, minutes, i4, nanos);
                    }
                    return TimeUtil.changeTimezone(resultSetImpl.connection, sessionCalendar, targetCalendar, fastTimestampCreate(sessionCalendar, year, month2, day2, hour, minutes, i4, nanos), resultSetImpl.connection.getServerTimezoneTZ(), tz, rollForward);
                } catch (RuntimeException e8) {
                    e7 = e8;
                    e2 = e7;
                    stringBuilder = new StringBuilder();
                    stringBuilder.append("Cannot convert value '");
                    stringBuilder.append(timestampValue2);
                    stringBuilder.append("' from column ");
                    stringBuilder.append(i);
                    stringBuilder.append(" to TIMESTAMP.");
                    sqlEx = SQLError.createSQLException(stringBuilder.toString(), SQLError.SQL_STATE_ILLEGAL_ARGUMENT, getExceptionInterceptor());
                    sqlEx.initCause(e2);
                    throw sqlEx;
                }
            } catch (RuntimeException e9) {
                e7 = e9;
                i = i2;
                resultSetImpl = r15;
                e2 = e7;
                stringBuilder = new StringBuilder();
                stringBuilder.append("Cannot convert value '");
                stringBuilder.append(timestampValue2);
                stringBuilder.append("' from column ");
                stringBuilder.append(i);
                stringBuilder.append(" to TIMESTAMP.");
                sqlEx = SQLError.createSQLException(stringBuilder.toString(), SQLError.SQL_STATE_ILLEGAL_ARGUMENT, getExceptionInterceptor());
                sqlEx.initCause(e2);
                throw sqlEx;
            }
        } catch (RuntimeException e10) {
            e7 = e10;
            i = i2;
            resultSetImpl = r15;
            timestampValue2 = timestampValue;
            e2 = e7;
            stringBuilder = new StringBuilder();
            stringBuilder.append("Cannot convert value '");
            stringBuilder.append(timestampValue2);
            stringBuilder.append("' from column ");
            stringBuilder.append(i);
            stringBuilder.append(" to TIMESTAMP.");
            sqlEx = SQLError.createSQLException(stringBuilder.toString(), SQLError.SQL_STATE_ILLEGAL_ARGUMENT, getExceptionInterceptor());
            sqlEx.initCause(e2);
            throw sqlEx;
        }
    }

    private Timestamp getTimestampInternal(int columnIndex, Calendar targetCalendar, TimeZone tz, boolean rollForward) throws SQLException {
        if (this.isBinaryEncoded) {
            return getNativeTimestamp(columnIndex, targetCalendar, tz, rollForward);
        }
        Timestamp tsVal;
        if (this.useFastDateParsing) {
            checkClosed();
            checkRowPos();
            checkColumnBounds(columnIndex);
            tsVal = this.thisRow.getTimestampFast(columnIndex - 1, targetCalendar, tz, rollForward, this.connection, this);
        } else {
            tsVal = getTimestampFromString(columnIndex, targetCalendar, getStringInternal(columnIndex, false), tz, rollForward);
        }
        if (tsVal == null) {
            this.wasNullFlag = true;
        } else {
            this.wasNullFlag = false;
        }
        return tsVal;
    }

    public int getType() throws SQLException {
        return this.resultSetType;
    }

    @Deprecated
    public InputStream getUnicodeStream(int columnIndex) throws SQLException {
        if (this.isBinaryEncoded) {
            return getNativeBinaryStream(columnIndex);
        }
        checkRowPos();
        return getBinaryStream(columnIndex);
    }

    @Deprecated
    public InputStream getUnicodeStream(String columnName) throws SQLException {
        return getUnicodeStream(findColumn(columnName));
    }

    public long getUpdateCount() {
        return this.updateCount;
    }

    public long getUpdateID() {
        return this.updateId;
    }

    public URL getURL(int colIndex) throws SQLException {
        String val = getString(colIndex);
        if (val == null) {
            return null;
        }
        try {
            return new URL(val);
        } catch (MalformedURLException e) {
            StringBuilder stringBuilder = new StringBuilder();
            stringBuilder.append(Messages.getString("ResultSet.Malformed_URL____104"));
            stringBuilder.append(val);
            stringBuilder.append("'");
            throw SQLError.createSQLException(stringBuilder.toString(), SQLError.SQL_STATE_ILLEGAL_ARGUMENT, getExceptionInterceptor());
        }
    }

    public URL getURL(String colName) throws SQLException {
        String val = getString(colName);
        if (val == null) {
            return null;
        }
        try {
            return new URL(val);
        } catch (MalformedURLException e) {
            StringBuilder stringBuilder = new StringBuilder();
            stringBuilder.append(Messages.getString("ResultSet.Malformed_URL____107"));
            stringBuilder.append(val);
            stringBuilder.append("'");
            throw SQLError.createSQLException(stringBuilder.toString(), SQLError.SQL_STATE_ILLEGAL_ARGUMENT, getExceptionInterceptor());
        }
    }

    public SQLWarning getWarnings() throws SQLException {
        SQLWarning sQLWarning;
        synchronized (checkClosed().getConnectionMutex()) {
            sQLWarning = this.warningChain;
        }
        return sQLWarning;
    }

    public void insertRow() throws SQLException {
        throw new NotUpdatable();
    }

    public boolean isAfterLast() throws SQLException {
        boolean b;
        synchronized (checkClosed().getConnectionMutex()) {
            b = this.rowData.isAfterLast();
        }
        return b;
    }

    public boolean isBeforeFirst() throws SQLException {
        boolean isBeforeFirst;
        synchronized (checkClosed().getConnectionMutex()) {
            isBeforeFirst = this.rowData.isBeforeFirst();
        }
        return isBeforeFirst;
    }

    public boolean isFirst() throws SQLException {
        boolean isFirst;
        synchronized (checkClosed().getConnectionMutex()) {
            isFirst = this.rowData.isFirst();
        }
        return isFirst;
    }

    public boolean isLast() throws SQLException {
        boolean isLast;
        synchronized (checkClosed().getConnectionMutex()) {
            isLast = this.rowData.isLast();
        }
        return isLast;
    }

    private void issueConversionViaParsingWarning(String methodName, int columnIndex, Object value, Field fieldInfo, int[] typesWithNoParseConversion) throws SQLException {
        ResultSetImpl resultSetImpl = this;
        int[] iArr = typesWithNoParseConversion;
        synchronized (checkClosed().getConnectionMutex()) {
            try {
                String name;
                StringBuilder originalQueryBuf = new StringBuilder();
                if (resultSetImpl.owningStatement == null || !(resultSetImpl.owningStatement instanceof PreparedStatement)) {
                    originalQueryBuf.append(".");
                } else {
                    originalQueryBuf.append(Messages.getString("ResultSet.CostlyConversionCreatedFromQuery"));
                    originalQueryBuf.append(((PreparedStatement) resultSetImpl.owningStatement).originalSql);
                    originalQueryBuf.append("\n\n");
                }
                StringBuilder convertibleTypesBuf = new StringBuilder();
                for (int typeToName : iArr) {
                    convertibleTypesBuf.append(MysqlDefs.typeToName(typeToName));
                    convertibleTypesBuf.append("\n");
                }
                String str = "ResultSet.CostlyConversion";
                Object[] objArr = new Object[8];
                objArr[0] = methodName;
                objArr[1] = Integer.valueOf(columnIndex + 1);
                objArr[2] = fieldInfo.getOriginalName();
                objArr[3] = fieldInfo.getOriginalTableName();
                objArr[4] = originalQueryBuf.toString();
                if (value != null) {
                    name = value.getClass().getName();
                } else {
                    boolean z;
                    int sQLType = fieldInfo.getSQLType();
                    boolean isUnsigned = fieldInfo.isUnsigned();
                    int mysqlType = fieldInfo.getMysqlType();
                    if (!fieldInfo.isBinary()) {
                        if (!fieldInfo.isBlob()) {
                            z = false;
                            name = ResultSetMetaData.getClassNameForJavaType(sQLType, isUnsigned, mysqlType, z, fieldInfo.isOpaqueBinary(), resultSetImpl.connection.getYearIsDateType());
                        }
                    }
                    z = true;
                    name = ResultSetMetaData.getClassNameForJavaType(sQLType, isUnsigned, mysqlType, z, fieldInfo.isOpaqueBinary(), resultSetImpl.connection.getYearIsDateType());
                }
                objArr[5] = name;
                objArr[6] = MysqlDefs.typeToName(fieldInfo.getMysqlType());
                objArr[7] = convertibleTypesBuf.toString();
                resultSetImpl.eventSink.consumeEvent(new ProfilerEvent((byte) 0, "", resultSetImpl.owningStatement == null ? "N/A" : resultSetImpl.owningStatement.currentCatalog, resultSetImpl.connectionId, resultSetImpl.owningStatement == null ? -1 : resultSetImpl.owningStatement.getId(), resultSetImpl.resultId, System.currentTimeMillis(), 0, Constants.MILLIS_I18N, null, resultSetImpl.pointOfOrigin, Messages.getString(str, objArr)));
            } catch (Throwable th) {
                Throwable th2 = th;
            }
        }
    }

    public boolean last() throws SQLException {
        boolean b;
        synchronized (checkClosed().getConnectionMutex()) {
            b = true;
            if (this.rowData.size() == 0) {
                b = false;
            } else {
                if (this.onInsertRow) {
                    this.onInsertRow = false;
                }
                if (this.doingUpdates) {
                    this.doingUpdates = false;
                }
                if (this.thisRow != null) {
                    this.thisRow.closeOpenStreams();
                }
                this.rowData.beforeLast();
                this.thisRow = this.rowData.next();
            }
            setRowPositionValidity();
        }
        return b;
    }

    public void moveToCurrentRow() throws SQLException {
        throw new NotUpdatable();
    }

    public void moveToInsertRow() throws SQLException {
        throw new NotUpdatable();
    }

    public boolean next() throws SQLException {
        boolean b;
        synchronized (checkClosed().getConnectionMutex()) {
            if (this.onInsertRow) {
                this.onInsertRow = false;
            }
            if (this.doingUpdates) {
                this.doingUpdates = false;
            }
            if (reallyResult()) {
                if (this.thisRow != null) {
                    this.thisRow.closeOpenStreams();
                }
                if (this.rowData.size() == 0) {
                    b = false;
                } else {
                    this.thisRow = this.rowData.next();
                    if (this.thisRow == null) {
                        b = false;
                    } else {
                        clearWarnings();
                        b = true;
                        setRowPositionValidity();
                    }
                }
                setRowPositionValidity();
            } else {
                throw SQLError.createSQLException(Messages.getString("ResultSet.ResultSet_is_from_UPDATE._No_Data_115"), SQLError.SQL_STATE_GENERAL_ERROR, getExceptionInterceptor());
            }
        }
        return b;
    }

    private int parseIntAsDouble(int columnIndex, String val) throws NumberFormatException, SQLException {
        if (val == null) {
            return 0;
        }
        double valueAsDouble = Double.parseDouble(val);
        if (this.jdbcCompliantTruncationForReads && (valueAsDouble < -2.147483648E9d || valueAsDouble > 2.147483647E9d)) {
            throwRangeException(String.valueOf(valueAsDouble), columnIndex, 4);
        }
        return (int) valueAsDouble;
    }

    private int getIntWithOverflowCheck(int columnIndex) throws SQLException {
        int intValue = this.thisRow.getInt(columnIndex);
        checkForIntegerTruncation(columnIndex, null, intValue);
        return intValue;
    }

    private void checkForIntegerTruncation(int columnIndex, byte[] valueAsBytes, int intValue) throws SQLException {
        if (!this.jdbcCompliantTruncationForReads) {
            return;
        }
        if (intValue == Integer.MIN_VALUE || intValue == ActivityChooserViewAdapter.MAX_ACTIVITY_COUNT_UNLIMITED) {
            String valueAsString = null;
            if (valueAsBytes == null) {
                valueAsString = this.thisRow.getString(columnIndex, this.fields[columnIndex].getEncoding(), this.connection);
            }
            long valueAsLong = Long.parseLong(valueAsString == null ? StringUtils.toString(valueAsBytes) : valueAsString);
            if (valueAsLong < -2147483648L || valueAsLong > 2147483647L) {
                throwRangeException(valueAsString == null ? StringUtils.toString(valueAsBytes) : valueAsString, columnIndex + 1, 4);
            }
        }
    }

    private long parseLongAsDouble(int columnIndexZeroBased, String val) throws NumberFormatException, SQLException {
        if (val == null) {
            return 0;
        }
        double valueAsDouble = Double.parseDouble(val);
        if (this.jdbcCompliantTruncationForReads && (valueAsDouble < -9.223372036854776E18d || valueAsDouble > 9.223372036854776E18d)) {
            throwRangeException(val, columnIndexZeroBased + 1, -5);
        }
        return (long) valueAsDouble;
    }

    private long getLongWithOverflowCheck(int columnIndexZeroBased, boolean doOverflowCheck) throws SQLException {
        long longValue = this.thisRow.getLong(columnIndexZeroBased);
        if (doOverflowCheck) {
            checkForLongTruncation(columnIndexZeroBased, null, longValue);
        }
        return longValue;
    }

    private long parseLongWithOverflowCheck(int columnIndexZeroBased, byte[] valueAsBytes, String valueAsString, boolean doCheck) throws NumberFormatException, SQLException {
        if (valueAsBytes == null && valueAsString == null) {
            return 0;
        }
        long longValue;
        if (valueAsBytes != null) {
            longValue = StringUtils.getLong(valueAsBytes);
        } else {
            longValue = Long.parseLong(valueAsString.trim());
        }
        if (doCheck && this.jdbcCompliantTruncationForReads) {
            checkForLongTruncation(columnIndexZeroBased, valueAsBytes, longValue);
        }
        return longValue;
    }

    private void checkForLongTruncation(int columnIndexZeroBased, byte[] valueAsBytes, long longValue) throws SQLException {
        if (longValue == Long.MIN_VALUE || longValue == Long.MAX_VALUE) {
            String valueAsString = null;
            if (valueAsBytes == null) {
                valueAsString = this.thisRow.getString(columnIndexZeroBased, this.fields[columnIndexZeroBased].getEncoding(), this.connection);
            }
            double valueAsDouble = Double.parseDouble(valueAsString == null ? StringUtils.toString(valueAsBytes) : valueAsString);
            if (valueAsDouble < -9.223372036854776E18d || valueAsDouble > 9.223372036854776E18d) {
                throwRangeException(valueAsString == null ? StringUtils.toString(valueAsBytes) : valueAsString, columnIndexZeroBased + 1, -5);
            }
        }
    }

    private short parseShortAsDouble(int columnIndex, String val) throws NumberFormatException, SQLException {
        if (val == null) {
            return (short) 0;
        }
        double valueAsDouble = Double.parseDouble(val);
        if (this.jdbcCompliantTruncationForReads && (valueAsDouble < -32768.0d || valueAsDouble > 32767.0d)) {
            throwRangeException(String.valueOf(valueAsDouble), columnIndex, 5);
        }
        return (short) ((int) valueAsDouble);
    }

    private short parseShortWithOverflowCheck(int columnIndex, byte[] valueAsBytes, String valueAsString) throws NumberFormatException, SQLException {
        if (valueAsBytes == null && valueAsString == null) {
            return (short) 0;
        }
        short shortValue;
        if (valueAsBytes != null) {
            shortValue = StringUtils.getShort(valueAsBytes);
        } else {
            valueAsString = valueAsString.trim();
            shortValue = Short.parseShort(valueAsString);
        }
        if (this.jdbcCompliantTruncationForReads && (shortValue == Short.MIN_VALUE || shortValue == Short.MAX_VALUE)) {
            long valueAsLong = Long.parseLong(valueAsString == null ? StringUtils.toString(valueAsBytes) : valueAsString);
            if (valueAsLong < -32768 || valueAsLong > 32767) {
                throwRangeException(valueAsString == null ? StringUtils.toString(valueAsBytes) : valueAsString, columnIndex, 5);
            }
        }
        return shortValue;
    }

    public boolean prev() throws SQLException {
        boolean b;
        synchronized (checkClosed().getConnectionMutex()) {
            int rowIndex = this.rowData.getCurrentRowNumber();
            if (this.thisRow != null) {
                this.thisRow.closeOpenStreams();
            }
            if (rowIndex - 1 >= 0) {
                rowIndex--;
                this.rowData.setCurrentRow(rowIndex);
                this.thisRow = this.rowData.getAt(rowIndex);
                b = true;
            } else if (rowIndex - 1 == -1) {
                this.rowData.setCurrentRow(rowIndex - 1);
                this.thisRow = null;
                b = false;
            } else {
                b = false;
            }
            setRowPositionValidity();
        }
        return b;
    }

    public boolean previous() throws SQLException {
        boolean prev;
        synchronized (checkClosed().getConnectionMutex()) {
            if (this.onInsertRow) {
                this.onInsertRow = false;
            }
            if (this.doingUpdates) {
                this.doingUpdates = false;
            }
            prev = prev();
        }
        return prev;
    }

    public void realClose(boolean calledExplicitly) throws SQLException {
        Throwable th;
        boolean z;
        ResultSetImpl resultSetImpl;
        MySQLConnection mySQLConnection;
        Object obj;
        MySQLConnection locallyScopedConn;
        MySQLConnection locallyScopedConn2;
        SQLException exceptionDuringClose;
        SQLException exceptionDuringClose2;
        MySQLConnection locallyScopedConn3 = this.connection;
        if (locallyScopedConn3 != null) {
            synchronized (locallyScopedConn3.getConnectionMutex()) {
                try {
                    if (r1.isClosed) {
                        try {
                            return;
                        } catch (Throwable th2) {
                            th = th2;
                            z = calledExplicitly;
                            resultSetImpl = r1;
                            mySQLConnection = locallyScopedConn3;
                            obj = th;
                            try {
                            } catch (Throwable th3) {
                                th = th3;
                                obj = th;
                                throw locallyScopedConn3;
                            }
                            throw locallyScopedConn3;
                        }
                    }
                    if (r1.useUsageAdvisor) {
                        boolean calledExplicitly2;
                        if (!calledExplicitly) {
                            try {
                                ProfilerEventHandler profilerEventHandler = r1.eventSink;
                                ProfilerEvent profilerEvent = r10;
                                ProfilerEvent profilerEvent2 = new ProfilerEvent((byte) 0, "", r1.owningStatement == null ? "N/A" : r1.owningStatement.currentCatalog, r1.connectionId, r1.owningStatement == null ? -1 : r1.owningStatement.getId(), r1.resultId, System.currentTimeMillis(), 0, Constants.MILLIS_I18N, null, r1.pointOfOrigin, Messages.getString("ResultSet.ResultSet_implicitly_closed_by_driver"));
                                profilerEventHandler.consumeEvent(profilerEvent);
                            } catch (Throwable th4) {
                                th = th4;
                                locallyScopedConn = locallyScopedConn3;
                                locallyScopedConn3 = th;
                                calledExplicitly2 = calledExplicitly;
                                locallyScopedConn2 = locallyScopedConn;
                                try {
                                    resultSetImpl.owningStatement.removeOpenResultSet(resultSetImpl);
                                    exceptionDuringClose = null;
                                    if (resultSetImpl.rowData != null) {
                                        try {
                                            resultSetImpl.rowData.close();
                                        } catch (SQLException e) {
                                            exceptionDuringClose = e;
                                        }
                                    }
                                    if (resultSetImpl.statementUsedForFetchingRows != null) {
                                        resultSetImpl.statementUsedForFetchingRows.realClose(true, false);
                                    }
                                } catch (SQLException e2) {
                                    SQLException sqlEx = e2;
                                    if (exceptionDuringClose != null) {
                                        exceptionDuringClose.setNextException(sqlEx);
                                    } else {
                                        exceptionDuringClose = sqlEx;
                                    }
                                } catch (Throwable th5) {
                                    locallyScopedConn3 = th5;
                                    z = calledExplicitly2;
                                    calledExplicitly2 = locallyScopedConn2;
                                    throw locallyScopedConn3;
                                }
                                resultSetImpl.rowData = null;
                                resultSetImpl.fields = null;
                                resultSetImpl.columnLabelToIndex = null;
                                resultSetImpl.fullColumnNameToIndex = null;
                                resultSetImpl.columnToIndexCache = null;
                                resultSetImpl.eventSink = null;
                                resultSetImpl.warningChain = null;
                                if (!resultSetImpl.retainOwningStatement) {
                                    resultSetImpl.owningStatement = null;
                                }
                                resultSetImpl.catalog = null;
                                resultSetImpl.serverInfo = null;
                                resultSetImpl.thisRow = null;
                                resultSetImpl.fastDefaultCal = null;
                                resultSetImpl.fastClientCal = null;
                                resultSetImpl.connection = null;
                                resultSetImpl.isClosed = true;
                                if (exceptionDuringClose != null) {
                                    throw locallyScopedConn3;
                                }
                                throw exceptionDuringClose;
                            }
                        }
                        try {
                            int i;
                            if (r1.rowData instanceof RowDataStatic) {
                                if (r1.rowData.size() > r1.connection.getResultSetSizeThreshold()) {
                                    r1.eventSink.consumeEvent(new ProfilerEvent((byte) 0, "", r1.owningStatement == null ? Messages.getString("ResultSet.N/A_159") : r1.owningStatement.currentCatalog, r1.connectionId, r1.owningStatement == null ? -1 : r1.owningStatement.getId(), r1.resultId, System.currentTimeMillis(), 0, Constants.MILLIS_I18N, null, r1.pointOfOrigin, Messages.getString("ResultSet.Too_Large_Result_Set", new Object[]{Integer.valueOf(r1.rowData.size()), Integer.valueOf(r1.connection.getResultSetSizeThreshold())})));
                                }
                                if (!(isLast() || isAfterLast() || r1.rowData.size() == 0)) {
                                    ProfilerEventHandler profilerEventHandler2 = r1.eventSink;
                                    String str = "";
                                    String string = r1.owningStatement == null ? Messages.getString("ResultSet.N/A_159") : r1.owningStatement.currentCatalog;
                                    long j = r1.connectionId;
                                    int id = r1.owningStatement == null ? -1 : r1.owningStatement.getId();
                                    int i2 = r1.resultId;
                                    long currentTimeMillis = System.currentTimeMillis();
                                    String str2 = Constants.MILLIS_I18N;
                                    String str3 = r1.pointOfOrigin;
                                    String str4 = "ResultSet.Possible_incomplete_traversal_of_result_set";
                                    Object[] objArr = new Object[2];
                                    locallyScopedConn = locallyScopedConn3;
                                    try {
                                        objArr[0] = Integer.valueOf(getRow());
                                        objArr[1] = Integer.valueOf(r1.rowData.size());
                                        profilerEventHandler2.consumeEvent(new ProfilerEvent(null, str, string, j, id, i2, currentTimeMillis, 0, str2, null, str3, Messages.getString(str4, objArr)));
                                        if (r1.columnUsed.length > null && r1.rowData.wasEmpty() == null) {
                                            locallyScopedConn3 = new StringBuilder(Messages.getString("ResultSet.The_following_columns_were_never_referenced"));
                                            calledExplicitly2 = false;
                                            for (i = 0; i < r1.columnUsed.length; i++) {
                                                if (r1.columnUsed[i]) {
                                                    if (calledExplicitly2) {
                                                        calledExplicitly2 = true;
                                                    } else {
                                                        locallyScopedConn3.append(", ");
                                                    }
                                                    locallyScopedConn3.append(r1.fields[i].getFullName());
                                                }
                                            }
                                            if (calledExplicitly2) {
                                                r1.eventSink.consumeEvent(new ProfilerEvent((byte) 0, "", r1.owningStatement != null ? "N/A" : r1.owningStatement.currentCatalog, r1.connectionId, r1.owningStatement != null ? -1 : r1.owningStatement.getId(), 0, System.currentTimeMillis(), 0, Constants.MILLIS_I18N, null, r1.pointOfOrigin, locallyScopedConn3.toString()));
                                            }
                                        }
                                    } catch (Throwable th6) {
                                        th5 = th6;
                                        locallyScopedConn3 = th5;
                                        calledExplicitly2 = calledExplicitly;
                                        locallyScopedConn2 = locallyScopedConn;
                                        if (r1.owningStatement != null && calledExplicitly2) {
                                            resultSetImpl.owningStatement.removeOpenResultSet(resultSetImpl);
                                        }
                                        exceptionDuringClose = null;
                                        if (resultSetImpl.rowData != null) {
                                            resultSetImpl.rowData.close();
                                        }
                                        if (resultSetImpl.statementUsedForFetchingRows != null) {
                                            resultSetImpl.statementUsedForFetchingRows.realClose(true, false);
                                        }
                                        resultSetImpl.rowData = null;
                                        resultSetImpl.fields = null;
                                        resultSetImpl.columnLabelToIndex = null;
                                        resultSetImpl.fullColumnNameToIndex = null;
                                        resultSetImpl.columnToIndexCache = null;
                                        resultSetImpl.eventSink = null;
                                        resultSetImpl.warningChain = null;
                                        if (resultSetImpl.retainOwningStatement) {
                                            resultSetImpl.owningStatement = null;
                                        }
                                        resultSetImpl.catalog = null;
                                        resultSetImpl.serverInfo = null;
                                        resultSetImpl.thisRow = null;
                                        resultSetImpl.fastDefaultCal = null;
                                        resultSetImpl.fastClientCal = null;
                                        resultSetImpl.connection = null;
                                        resultSetImpl.isClosed = true;
                                        if (exceptionDuringClose != null) {
                                            throw exceptionDuringClose;
                                        }
                                        throw locallyScopedConn3;
                                    }
                                }
                            }
                            locallyScopedConn = locallyScopedConn3;
                            locallyScopedConn3 = new StringBuilder(Messages.getString("ResultSet.The_following_columns_were_never_referenced"));
                            calledExplicitly2 = false;
                            for (i = 0; i < r1.columnUsed.length; i++) {
                                if (r1.columnUsed[i]) {
                                    if (calledExplicitly2) {
                                        locallyScopedConn3.append(", ");
                                    } else {
                                        calledExplicitly2 = true;
                                    }
                                    locallyScopedConn3.append(r1.fields[i].getFullName());
                                }
                            }
                            if (calledExplicitly2) {
                                if (r1.owningStatement != null) {
                                }
                                if (r1.owningStatement != null) {
                                }
                                r1.eventSink.consumeEvent(new ProfilerEvent((byte) 0, "", r1.owningStatement != null ? "N/A" : r1.owningStatement.currentCatalog, r1.connectionId, r1.owningStatement != null ? -1 : r1.owningStatement.getId(), 0, System.currentTimeMillis(), 0, Constants.MILLIS_I18N, null, r1.pointOfOrigin, locallyScopedConn3.toString()));
                            }
                        } catch (Throwable th52) {
                            locallyScopedConn = locallyScopedConn3;
                            locallyScopedConn3 = th52;
                            calledExplicitly2 = calledExplicitly;
                            locallyScopedConn2 = locallyScopedConn;
                            resultSetImpl.owningStatement.removeOpenResultSet(resultSetImpl);
                            exceptionDuringClose = null;
                            if (resultSetImpl.rowData != null) {
                                resultSetImpl.rowData.close();
                            }
                            if (resultSetImpl.statementUsedForFetchingRows != null) {
                                resultSetImpl.statementUsedForFetchingRows.realClose(true, false);
                            }
                            resultSetImpl.rowData = null;
                            resultSetImpl.fields = null;
                            resultSetImpl.columnLabelToIndex = null;
                            resultSetImpl.fullColumnNameToIndex = null;
                            resultSetImpl.columnToIndexCache = null;
                            resultSetImpl.eventSink = null;
                            resultSetImpl.warningChain = null;
                            if (resultSetImpl.retainOwningStatement) {
                                resultSetImpl.owningStatement = null;
                            }
                            resultSetImpl.catalog = null;
                            resultSetImpl.serverInfo = null;
                            resultSetImpl.thisRow = null;
                            resultSetImpl.fastDefaultCal = null;
                            resultSetImpl.fastClientCal = null;
                            resultSetImpl.connection = null;
                            resultSetImpl.isClosed = true;
                            if (exceptionDuringClose != null) {
                                throw exceptionDuringClose;
                            }
                            throw locallyScopedConn3;
                        }
                    }
                    locallyScopedConn = locallyScopedConn3;
                    boolean calledExplicitly3 = calledExplicitly;
                    try {
                        if (r1.owningStatement != null && calledExplicitly3) {
                            locallyScopedConn.owningStatement.removeOpenResultSet(locallyScopedConn);
                        }
                        exceptionDuringClose2 = null;
                        if (locallyScopedConn.rowData != null) {
                            try {
                                locallyScopedConn.rowData.close();
                            } catch (SQLException e22) {
                                exceptionDuringClose2 = e22;
                            }
                        }
                        if (locallyScopedConn.statementUsedForFetchingRows != null) {
                            locallyScopedConn.statementUsedForFetchingRows.realClose(true, false);
                        }
                    } catch (SQLException e222) {
                        exceptionDuringClose = e222;
                        if (exceptionDuringClose2 != null) {
                            exceptionDuringClose2.setNextException(exceptionDuringClose);
                        } else {
                            exceptionDuringClose2 = exceptionDuringClose;
                        }
                    } catch (Throwable th7) {
                        th52 = th7;
                        z = calledExplicitly3;
                        calledExplicitly3 = locallyScopedConn;
                        obj = th52;
                        throw locallyScopedConn3;
                    }
                    locallyScopedConn.rowData = null;
                    locallyScopedConn.fields = null;
                    locallyScopedConn.columnLabelToIndex = null;
                    locallyScopedConn.fullColumnNameToIndex = null;
                    locallyScopedConn.columnToIndexCache = null;
                    locallyScopedConn.eventSink = null;
                    locallyScopedConn.warningChain = null;
                    if (!locallyScopedConn.retainOwningStatement) {
                        locallyScopedConn.owningStatement = null;
                    }
                    locallyScopedConn.catalog = null;
                    locallyScopedConn.serverInfo = null;
                    locallyScopedConn.thisRow = null;
                    locallyScopedConn.fastDefaultCal = null;
                    locallyScopedConn.fastClientCal = null;
                    locallyScopedConn.connection = null;
                    locallyScopedConn.isClosed = true;
                    if (exceptionDuringClose2 != null) {
                        throw exceptionDuringClose2;
                    }
                } catch (Throwable th522) {
                    locallyScopedConn = locallyScopedConn3;
                    locallyScopedConn3 = th522;
                    resultSetImpl = r1;
                    mySQLConnection = locallyScopedConn;
                    throw locallyScopedConn3;
                }
            }
        }
    }

    public boolean isClosed() throws SQLException {
        return this.isClosed;
    }

    public boolean reallyResult() {
        if (this.rowData != null) {
            return true;
        }
        return this.reallyResult;
    }

    public void refreshRow() throws SQLException {
        throw new NotUpdatable();
    }

    /* JADX WARNING: inconsistent code. */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    public boolean relative(int r5) throws java.sql.SQLException {
        /*
        r4 = this;
        r0 = r4.checkClosed();
        r0 = r0.getConnectionMutex();
        monitor-enter(r0);
        r1 = r4.rowData;	 Catch:{ all -> 0x004a }
        r1 = r1.size();	 Catch:{ all -> 0x004a }
        r2 = 0;
        if (r1 != 0) goto L_0x0017;
    L_0x0012:
        r4.setRowPositionValidity();	 Catch:{ all -> 0x004a }
        monitor-exit(r0);	 Catch:{ all -> 0x004a }
        return r2;
    L_0x0017:
        r1 = r4.thisRow;	 Catch:{ all -> 0x004a }
        if (r1 == 0) goto L_0x0020;
    L_0x001b:
        r1 = r4.thisRow;	 Catch:{ all -> 0x004a }
        r1.closeOpenStreams();	 Catch:{ all -> 0x004a }
    L_0x0020:
        r1 = r4.rowData;	 Catch:{ all -> 0x004a }
        r1.moveRowRelative(r5);	 Catch:{ all -> 0x004a }
        r1 = r4.rowData;	 Catch:{ all -> 0x004a }
        r3 = r4.rowData;	 Catch:{ all -> 0x004a }
        r3 = r3.getCurrentRowNumber();	 Catch:{ all -> 0x004a }
        r1 = r1.getAt(r3);	 Catch:{ all -> 0x004a }
        r4.thisRow = r1;	 Catch:{ all -> 0x004a }
        r4.setRowPositionValidity();	 Catch:{ all -> 0x004a }
        r1 = r4.rowData;	 Catch:{ all -> 0x004a }
        r1 = r1.isAfterLast();	 Catch:{ all -> 0x004a }
        if (r1 != 0) goto L_0x0048;
    L_0x003e:
        r1 = r4.rowData;	 Catch:{ all -> 0x004a }
        r1 = r1.isBeforeFirst();	 Catch:{ all -> 0x004a }
        if (r1 != 0) goto L_0x0048;
    L_0x0046:
        r2 = 1;
    L_0x0048:
        monitor-exit(r0);	 Catch:{ all -> 0x004a }
        return r2;
    L_0x004a:
        r1 = move-exception;
        monitor-exit(r0);	 Catch:{ all -> 0x004a }
        throw r1;
        */
        throw new UnsupportedOperationException("Method not decompiled: com.mysql.jdbc.ResultSetImpl.relative(int):boolean");
    }

    public boolean rowDeleted() throws SQLException {
        throw SQLError.createSQLFeatureNotSupportedException();
    }

    public boolean rowInserted() throws SQLException {
        throw SQLError.createSQLFeatureNotSupportedException();
    }

    public boolean rowUpdated() throws SQLException {
        throw SQLError.createSQLFeatureNotSupportedException();
    }

    protected void setBinaryEncoded() {
        this.isBinaryEncoded = true;
    }

    public void setFetchDirection(int direction) throws SQLException {
        synchronized (checkClosed().getConnectionMutex()) {
            if (direction == 1000 || direction == 1001 || direction == 1002) {
                this.fetchDirection = direction;
            } else {
                throw SQLError.createSQLException(Messages.getString("ResultSet.Illegal_value_for_fetch_direction_64"), SQLError.SQL_STATE_ILLEGAL_ARGUMENT, getExceptionInterceptor());
            }
        }
    }

    public void setFetchSize(int rows) throws SQLException {
        synchronized (checkClosed().getConnectionMutex()) {
            if (rows < 0) {
                throw SQLError.createSQLException(Messages.getString("ResultSet.Value_must_be_between_0_and_getMaxRows()_66"), SQLError.SQL_STATE_ILLEGAL_ARGUMENT, getExceptionInterceptor());
            }
            this.fetchSize = rows;
        }
    }

    public void setFirstCharOfQuery(char c) {
        try {
            synchronized (checkClosed().getConnectionMutex()) {
                this.firstCharOfQuery = c;
            }
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    protected synchronized void setNextResultSet(ResultSetInternalMethods nextResultSet) {
        this.nextResultSet = nextResultSet;
    }

    public void setOwningStatement(StatementImpl owningStatement) {
        try {
            synchronized (checkClosed().getConnectionMutex()) {
                this.owningStatement = owningStatement;
            }
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    protected synchronized void setResultSetConcurrency(int concurrencyFlag) {
        try {
            synchronized (checkClosed().getConnectionMutex()) {
                this.resultSetConcurrency = concurrencyFlag;
            }
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    protected synchronized void setResultSetType(int typeFlag) {
        try {
            synchronized (checkClosed().getConnectionMutex()) {
                this.resultSetType = typeFlag;
            }
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    protected void setServerInfo(String info) {
        try {
            synchronized (checkClosed().getConnectionMutex()) {
                this.serverInfo = info;
            }
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    public synchronized void setStatementUsedForFetchingRows(PreparedStatement stmt) {
        try {
            synchronized (checkClosed().getConnectionMutex()) {
                this.statementUsedForFetchingRows = stmt;
            }
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    public synchronized void setWrapperStatement(Statement wrapperStatement) {
        try {
            synchronized (checkClosed().getConnectionMutex()) {
                this.wrapperStatement = wrapperStatement;
            }
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    private void throwRangeException(String valueAsString, int columnIndex, int jdbcType) throws SQLException {
        String datatype;
        StringBuilder stringBuilder;
        switch (jdbcType) {
            case -6:
                datatype = "TINYINT";
                break;
            case -5:
                datatype = "BIGINT";
                break;
            default:
                switch (jdbcType) {
                    case 3:
                        datatype = "DECIMAL";
                        break;
                    case 4:
                        datatype = "INTEGER";
                        break;
                    case 5:
                        datatype = "SMALLINT";
                        break;
                    case 6:
                        datatype = "FLOAT";
                        break;
                    case 7:
                        datatype = "REAL";
                        break;
                    case 8:
                        datatype = "DOUBLE";
                        break;
                    default:
                        stringBuilder = new StringBuilder();
                        stringBuilder.append(" (JDBC type '");
                        stringBuilder.append(jdbcType);
                        stringBuilder.append("')");
                        datatype = stringBuilder.toString();
                        break;
                }
        }
        stringBuilder = new StringBuilder();
        stringBuilder.append("'");
        stringBuilder.append(valueAsString);
        stringBuilder.append("' in column '");
        stringBuilder.append(columnIndex);
        stringBuilder.append("' is outside valid range for the datatype ");
        stringBuilder.append(datatype);
        stringBuilder.append(".");
        throw SQLError.createSQLException(stringBuilder.toString(), SQLError.SQL_STATE_NUMERIC_VALUE_OUT_OF_RANGE, getExceptionInterceptor());
    }

    public String toString() {
        if (this.reallyResult) {
            return super.toString();
        }
        StringBuilder stringBuilder = new StringBuilder();
        stringBuilder.append("Result set representing update count of ");
        stringBuilder.append(this.updateCount);
        return stringBuilder.toString();
    }

    public void updateArray(int arg0, Array arg1) throws SQLException {
        throw SQLError.createSQLFeatureNotSupportedException();
    }

    public void updateArray(String arg0, Array arg1) throws SQLException {
        throw SQLError.createSQLFeatureNotSupportedException();
    }

    public void updateAsciiStream(int columnIndex, InputStream x, int length) throws SQLException {
        throw new NotUpdatable();
    }

    public void updateAsciiStream(String columnName, InputStream x, int length) throws SQLException {
        updateAsciiStream(findColumn(columnName), x, length);
    }

    public void updateBigDecimal(int columnIndex, BigDecimal x) throws SQLException {
        throw new NotUpdatable();
    }

    public void updateBigDecimal(String columnName, BigDecimal x) throws SQLException {
        updateBigDecimal(findColumn(columnName), x);
    }

    public void updateBinaryStream(int columnIndex, InputStream x, int length) throws SQLException {
        throw new NotUpdatable();
    }

    public void updateBinaryStream(String columnName, InputStream x, int length) throws SQLException {
        updateBinaryStream(findColumn(columnName), x, length);
    }

    public void updateBlob(int arg0, Blob arg1) throws SQLException {
        throw new NotUpdatable();
    }

    public void updateBlob(String arg0, Blob arg1) throws SQLException {
        throw new NotUpdatable();
    }

    public void updateBoolean(int columnIndex, boolean x) throws SQLException {
        throw new NotUpdatable();
    }

    public void updateBoolean(String columnName, boolean x) throws SQLException {
        updateBoolean(findColumn(columnName), x);
    }

    public void updateByte(int columnIndex, byte x) throws SQLException {
        throw new NotUpdatable();
    }

    public void updateByte(String columnName, byte x) throws SQLException {
        updateByte(findColumn(columnName), x);
    }

    public void updateBytes(int columnIndex, byte[] x) throws SQLException {
        throw new NotUpdatable();
    }

    public void updateBytes(String columnName, byte[] x) throws SQLException {
        updateBytes(findColumn(columnName), x);
    }

    public void updateCharacterStream(int columnIndex, Reader x, int length) throws SQLException {
        throw new NotUpdatable();
    }

    public void updateCharacterStream(String columnName, Reader reader, int length) throws SQLException {
        updateCharacterStream(findColumn(columnName), reader, length);
    }

    public void updateClob(int arg0, Clob arg1) throws SQLException {
        throw SQLError.createSQLFeatureNotSupportedException();
    }

    public void updateClob(String columnName, Clob clob) throws SQLException {
        updateClob(findColumn(columnName), clob);
    }

    public void updateDate(int columnIndex, Date x) throws SQLException {
        throw new NotUpdatable();
    }

    public void updateDate(String columnName, Date x) throws SQLException {
        updateDate(findColumn(columnName), x);
    }

    public void updateDouble(int columnIndex, double x) throws SQLException {
        throw new NotUpdatable();
    }

    public void updateDouble(String columnName, double x) throws SQLException {
        updateDouble(findColumn(columnName), x);
    }

    public void updateFloat(int columnIndex, float x) throws SQLException {
        throw new NotUpdatable();
    }

    public void updateFloat(String columnName, float x) throws SQLException {
        updateFloat(findColumn(columnName), x);
    }

    public void updateInt(int columnIndex, int x) throws SQLException {
        throw new NotUpdatable();
    }

    public void updateInt(String columnName, int x) throws SQLException {
        updateInt(findColumn(columnName), x);
    }

    public void updateLong(int columnIndex, long x) throws SQLException {
        throw new NotUpdatable();
    }

    public void updateLong(String columnName, long x) throws SQLException {
        updateLong(findColumn(columnName), x);
    }

    public void updateNull(int columnIndex) throws SQLException {
        throw new NotUpdatable();
    }

    public void updateNull(String columnName) throws SQLException {
        updateNull(findColumn(columnName));
    }

    public void updateObject(int columnIndex, Object x) throws SQLException {
        throw new NotUpdatable();
    }

    public void updateObject(int columnIndex, Object x, int scale) throws SQLException {
        throw new NotUpdatable();
    }

    public void updateObject(String columnName, Object x) throws SQLException {
        updateObject(findColumn(columnName), x);
    }

    public void updateObject(String columnName, Object x, int scale) throws SQLException {
        updateObject(findColumn(columnName), x);
    }

    public void updateRef(int arg0, Ref arg1) throws SQLException {
        throw SQLError.createSQLFeatureNotSupportedException();
    }

    public void updateRef(String arg0, Ref arg1) throws SQLException {
        throw SQLError.createSQLFeatureNotSupportedException();
    }

    public void updateRow() throws SQLException {
        throw new NotUpdatable();
    }

    public void updateShort(int columnIndex, short x) throws SQLException {
        throw new NotUpdatable();
    }

    public void updateShort(String columnName, short x) throws SQLException {
        updateShort(findColumn(columnName), x);
    }

    public void updateString(int columnIndex, String x) throws SQLException {
        throw new NotUpdatable();
    }

    public void updateString(String columnName, String x) throws SQLException {
        updateString(findColumn(columnName), x);
    }

    public void updateTime(int columnIndex, Time x) throws SQLException {
        throw new NotUpdatable();
    }

    public void updateTime(String columnName, Time x) throws SQLException {
        updateTime(findColumn(columnName), x);
    }

    public void updateTimestamp(int columnIndex, Timestamp x) throws SQLException {
        throw new NotUpdatable();
    }

    public void updateTimestamp(String columnName, Timestamp x) throws SQLException {
        updateTimestamp(findColumn(columnName), x);
    }

    public boolean wasNull() throws SQLException {
        return this.wasNullFlag;
    }

    protected Calendar getGmtCalendar() {
        if (this.gmtCalendar == null) {
            this.gmtCalendar = Calendar.getInstance(TimeZone.getTimeZone("GMT"));
        }
        return this.gmtCalendar;
    }

    protected ExceptionInterceptor getExceptionInterceptor() {
        return this.exceptionInterceptor;
    }
}
