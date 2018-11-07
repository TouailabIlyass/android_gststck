package com.mysql.jdbc;

import com.mysql.jdbc.profiler.ProfilerEvent;
import com.mysql.jdbc.profiler.ProfilerEventHandler;
import java.io.InputStream;
import java.io.Reader;
import java.math.BigDecimal;
import java.sql.Blob;
import java.sql.Clob;
import java.sql.DatabaseMetaData;
import java.sql.Date;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Time;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.TreeMap;

public class UpdatableResultSet extends ResultSetImpl {
    static final byte[] STREAM_DATA_MARKER = StringUtils.getBytes("** STREAM DATA **");
    protected SingleByteCharsetConverter charConverter;
    private String charEncoding;
    private Map<String, Map<String, Map<String, Integer>>> databasesUsedToTablesUsed = null;
    private byte[][] defaultColumnValue;
    private String deleteSQL = null;
    private PreparedStatement deleter = null;
    private boolean initializedCharConverter = false;
    private String insertSQL = null;
    protected PreparedStatement inserter = null;
    private boolean isUpdatable = false;
    private String notUpdatableReason = null;
    private boolean populateInserterWithDefaultValues = false;
    private List<Integer> primaryKeyIndicies = null;
    private String qualifiedAndQuotedTableName;
    private String quotedIdChar = null;
    private String refreshSQL = null;
    private PreparedStatement refresher;
    private ResultSetRow savedCurrentRow;
    private String updateSQL = null;
    protected PreparedStatement updater = null;

    protected UpdatableResultSet(String catalog, Field[] fields, RowData tuples, MySQLConnection conn, StatementImpl creatorStmt) throws SQLException {
        super(catalog, fields, tuples, conn, creatorStmt);
        checkUpdatability();
        this.populateInserterWithDefaultValues = this.connection.getPopulateInsertRowWithDefaultValues();
    }

    public boolean absolute(int row) throws SQLException {
        return super.absolute(row);
    }

    public void afterLast() throws SQLException {
        super.afterLast();
    }

    public void beforeFirst() throws SQLException {
        super.beforeFirst();
    }

    public void cancelRowUpdates() throws SQLException {
        synchronized (checkClosed().getConnectionMutex()) {
            if (this.doingUpdates) {
                this.doingUpdates = false;
                this.updater.clearParameters();
            }
        }
    }

    protected void checkRowPos() throws SQLException {
        synchronized (checkClosed().getConnectionMutex()) {
            if (!this.onInsertRow) {
                super.checkRowPos();
            }
        }
    }

    protected void checkUpdatability() throws SQLException {
        UpdatableResultSet this;
        SQLException sqlEx;
        UpdatableResultSet this2;
        try {
            if (this.fields != null) {
                int primaryKeyCount = 0;
                if (this.catalog == null || this.catalog.length() == 0) {
                    this.catalog = this.fields[0].getDatabaseName();
                    if (this.catalog != null) {
                        if (this.catalog.length() == 0) {
                        }
                    }
                    throw SQLError.createSQLException(Messages.getString("UpdatableResultSet.43"), SQLError.SQL_STATE_ILLEGAL_ARGUMENT, getExceptionInterceptor());
                }
                if (this.fields.length > 0) {
                    String singleTableName = this.fields[0].getOriginalTableName();
                    String catalogName = this.fields[0].getDatabaseName();
                    if (singleTableName == null) {
                        singleTableName = this.fields[0].getTableName();
                        catalogName = this.catalog;
                    }
                    if (singleTableName == null || singleTableName.length() != 0) {
                        if (this.fields[0].isPrimaryKey()) {
                            primaryKeyCount = 0 + 1;
                        }
                        int primaryKeyCount2 = primaryKeyCount;
                        primaryKeyCount = 1;
                        while (primaryKeyCount < this.fields.length) {
                            String otherTableName = this.fields[primaryKeyCount].getOriginalTableName();
                            String otherCatalogName = this.fields[primaryKeyCount].getDatabaseName();
                            if (otherTableName == null) {
                                otherTableName = this.fields[primaryKeyCount].getTableName();
                                otherCatalogName = this.catalog;
                            }
                            if (otherTableName == null || otherTableName.length() != 0) {
                                if (singleTableName != null) {
                                    if (otherTableName.equals(singleTableName)) {
                                        if (catalogName != null) {
                                            if (otherCatalogName.equals(catalogName)) {
                                                if (this.fields[primaryKeyCount].isPrimaryKey()) {
                                                    primaryKeyCount2++;
                                                }
                                                primaryKeyCount++;
                                            }
                                        }
                                        this.isUpdatable = false;
                                        this.notUpdatableReason = Messages.getString("NotUpdatableReason.1");
                                        return;
                                    }
                                }
                                this.isUpdatable = false;
                                this.notUpdatableReason = Messages.getString("NotUpdatableReason.0");
                                return;
                            }
                            this.isUpdatable = false;
                            this.notUpdatableReason = Messages.getString("NotUpdatableReason.3");
                            return;
                        }
                        if (singleTableName != null) {
                            if (singleTableName.length() != 0) {
                                if (this.connection.getStrictUpdates()) {
                                    DatabaseMetaData dbmd = this.connection.getMetaData();
                                    HashMap<String, String> primaryKeyNames = new HashMap();
                                    try {
                                        ResultSet rs = dbmd.getPrimaryKeys(catalogName, null, singleTableName);
                                        while (rs.next()) {
                                            String keyName = rs.getString(4).toUpperCase();
                                            primaryKeyNames.put(keyName, keyName);
                                        }
                                        this = this;
                                        if (rs != null) {
                                            try {
                                                rs.close();
                                            } catch (Exception ex) {
                                                try {
                                                    AssertionFailedException.shouldNotHappen(ex);
                                                } catch (SQLException e) {
                                                    sqlEx = e;
                                                    this.isUpdatable = false;
                                                    this.notUpdatableReason = sqlEx.getMessage();
                                                }
                                            }
                                        }
                                        int existingPrimaryKeysCount = primaryKeyNames.size();
                                        if (existingPrimaryKeysCount == 0) {
                                            this.isUpdatable = false;
                                            this.notUpdatableReason = Messages.getString("NotUpdatableReason.5");
                                            return;
                                        }
                                        int i = 0;
                                        while (i < this.fields.length) {
                                            if (this.fields[i].isPrimaryKey() && primaryKeyNames.remove(this.fields[i].getName().toUpperCase()) == null) {
                                                String originalName = this.fields[i].getOriginalName();
                                                if (originalName != null && primaryKeyNames.remove(originalName.toUpperCase()) == null) {
                                                    this.isUpdatable = false;
                                                    this.notUpdatableReason = Messages.getString("NotUpdatableReason.6", new Object[]{originalName});
                                                    return;
                                                }
                                            }
                                            i++;
                                        }
                                        this.isUpdatable = primaryKeyNames.isEmpty();
                                        if (!this.isUpdatable) {
                                            if (existingPrimaryKeysCount > 1) {
                                                this.notUpdatableReason = Messages.getString("NotUpdatableReason.7");
                                            } else {
                                                this.notUpdatableReason = Messages.getString("NotUpdatableReason.4");
                                            }
                                            return;
                                        }
                                    } catch (SQLException e2) {
                                        sqlEx = e2;
                                        this = this2;
                                        this.isUpdatable = false;
                                        this.notUpdatableReason = sqlEx.getMessage();
                                    } catch (Throwable th) {
                                        ResultSet rs2 = null;
                                        this2 = this;
                                        if (rs2 != null) {
                                            try {
                                                rs2.close();
                                            } catch (Exception ex2) {
                                                AssertionFailedException.shouldNotHappen(ex2);
                                            }
                                        }
                                    }
                                }
                                this = this;
                                if (primaryKeyCount2 == 0) {
                                    this.isUpdatable = false;
                                    this.notUpdatableReason = Messages.getString("NotUpdatableReason.4");
                                    return;
                                }
                                this.isUpdatable = true;
                                this.notUpdatableReason = null;
                                return;
                            }
                        }
                        this.isUpdatable = false;
                        this.notUpdatableReason = Messages.getString("NotUpdatableReason.2");
                        return;
                    }
                    this.isUpdatable = false;
                    this.notUpdatableReason = Messages.getString("NotUpdatableReason.3");
                    return;
                }
                this.isUpdatable = false;
                this.notUpdatableReason = Messages.getString("NotUpdatableReason.3");
            }
        } catch (SQLException e3) {
            sqlEx = e3;
            this = this;
            this.isUpdatable = false;
            this.notUpdatableReason = sqlEx.getMessage();
        }
    }

    public void deleteRow() throws SQLException {
        synchronized (checkClosed().getConnectionMutex()) {
            if (!this.isUpdatable) {
                throw new NotUpdatable(this.notUpdatableReason);
            } else if (this.onInsertRow) {
                throw SQLError.createSQLException(Messages.getString("UpdatableResultSet.1"), getExceptionInterceptor());
            } else if (this.rowData.size() == 0) {
                throw SQLError.createSQLException(Messages.getString("UpdatableResultSet.2"), getExceptionInterceptor());
            } else if (isBeforeFirst()) {
                throw SQLError.createSQLException(Messages.getString("UpdatableResultSet.3"), getExceptionInterceptor());
            } else if (isAfterLast()) {
                throw SQLError.createSQLException(Messages.getString("UpdatableResultSet.4"), getExceptionInterceptor());
            } else {
                if (this.deleter == null) {
                    if (this.deleteSQL == null) {
                        generateStatements();
                    }
                    this.deleter = (PreparedStatement) this.connection.clientPrepareStatement(this.deleteSQL);
                }
                this.deleter.clearParameters();
                int numKeys = this.primaryKeyIndicies.size();
                int i = 0;
                if (numKeys == 1) {
                    i = ((Integer) this.primaryKeyIndicies.get(0)).intValue();
                    setParamValue(this.deleter, 1, this.thisRow, i, this.fields[i].getSQLType());
                } else {
                    while (i < numKeys) {
                        int index = ((Integer) this.primaryKeyIndicies.get(i)).intValue();
                        setParamValue(this.deleter, i + 1, this.thisRow, index, this.fields[index].getSQLType());
                        i++;
                    }
                }
                this.deleter.executeUpdate();
                this.rowData.removeRow(this.rowData.getCurrentRowNumber());
                previous();
            }
        }
    }

    /* JADX WARNING: inconsistent code. */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    private void setParamValue(com.mysql.jdbc.PreparedStatement r11, int r12, com.mysql.jdbc.ResultSetRow r13, int r14, int r15) throws java.sql.SQLException {
        /*
        r10 = this;
        r0 = r13.getColumnValue(r14);
        r1 = 0;
        if (r0 != 0) goto L_0x000b;
    L_0x0007:
        r11.setNull(r12, r1);
        return;
    L_0x000b:
        r2 = 12;
        if (r15 == r2) goto L_0x0072;
    L_0x000f:
        r2 = 16;
        if (r15 == r2) goto L_0x006e;
    L_0x0013:
        switch(r15) {
            case -6: goto L_0x0066;
            case -5: goto L_0x005e;
            default: goto L_0x0016;
        };
    L_0x0016:
        switch(r15) {
            case -1: goto L_0x0072;
            case 0: goto L_0x005a;
            case 1: goto L_0x0072;
            case 2: goto L_0x0072;
            case 3: goto L_0x0072;
            case 4: goto L_0x0066;
            case 5: goto L_0x0066;
            case 6: goto L_0x006e;
            case 7: goto L_0x006e;
            case 8: goto L_0x006e;
            default: goto L_0x0019;
        };
    L_0x0019:
        switch(r15) {
            case 91: goto L_0x004c;
            case 92: goto L_0x0036;
            case 93: goto L_0x0020;
            default: goto L_0x001c;
        };
    L_0x001c:
        r11.setBytes(r12, r0);
        goto L_0x007e;
    L_0x0020:
        r5 = r10.fastDefaultCal;
        r1 = r10.connection;
        r6 = r1.getDefaultTimeZone();
        r7 = 0;
        r8 = r10.connection;
        r3 = r13;
        r4 = r14;
        r9 = r10;
        r1 = r3.getTimestampFast(r4, r5, r6, r7, r8, r9);
        r11.setTimestamp(r12, r1);
        goto L_0x007e;
    L_0x0036:
        r4 = r10.fastDefaultCal;
        r1 = r10.connection;
        r5 = r1.getDefaultTimeZone();
        r6 = 0;
        r7 = r10.connection;
        r2 = r13;
        r3 = r14;
        r8 = r10;
        r1 = r2.getTimeFast(r3, r4, r5, r6, r7, r8);
        r11.setTime(r12, r1);
        goto L_0x007e;
    L_0x004c:
        r1 = r10.connection;
        r2 = r10.fastDefaultCal;
        r1 = r13.getDateFast(r14, r1, r10, r2);
        r2 = r10.fastDefaultCal;
        r11.setDate(r12, r1, r2);
        goto L_0x007e;
    L_0x005a:
        r11.setNull(r12, r1);
        goto L_0x007e;
    L_0x005e:
        r1 = r13.getLong(r14);
        r11.setLong(r12, r1);
        goto L_0x007e;
    L_0x0066:
        r1 = r13.getInt(r14);
        r11.setInt(r12, r1);
        goto L_0x007e;
    L_0x006e:
        r11.setBytesNoEscapeNoQuotes(r12, r0);
        goto L_0x007e;
    L_0x0072:
        r1 = r10.charEncoding;
        r2 = r10.connection;
        r1 = r13.getString(r14, r1, r2);
        r11.setString(r12, r1);
    L_0x007e:
        return;
        */
        throw new UnsupportedOperationException("Method not decompiled: com.mysql.jdbc.UpdatableResultSet.setParamValue(com.mysql.jdbc.PreparedStatement, int, com.mysql.jdbc.ResultSetRow, int, int):void");
    }

    private void extractDefaultValues() throws SQLException {
        DatabaseMetaData dbmd = this.connection.getMetaData();
        this.defaultColumnValue = new byte[this.fields.length][];
        ResultSet columnsResultSet = null;
        DatabaseMetaData dbmd2 = dbmd;
        UpdatableResultSet this = this;
        for (Entry<String, Map<String, Map<String, Integer>>> dbEntry : this.databasesUsedToTablesUsed.entrySet()) {
            for (Entry<String, Map<String, Integer>> tableEntry : ((Map) dbEntry.getValue()).entrySet()) {
                Map<String, Integer> columnNamesToIndices = (Map) tableEntry.getValue();
                try {
                    columnsResultSet = dbmd2.getColumns(this.catalog, null, (String) tableEntry.getKey(), "%");
                    while (columnsResultSet.next()) {
                        String columnName = columnsResultSet.getString("COLUMN_NAME");
                        byte[] defaultValue = columnsResultSet.getBytes("COLUMN_DEF");
                        if (columnNamesToIndices.containsKey(columnName)) {
                            this.defaultColumnValue[((Integer) columnNamesToIndices.get(columnName)).intValue()] = defaultValue;
                        }
                    }
                    if (columnsResultSet != null) {
                        columnsResultSet.close();
                        columnsResultSet = null;
                    }
                } catch (Throwable th) {
                    if (columnsResultSet != null) {
                        columnsResultSet.close();
                    }
                }
            }
        }
    }

    public boolean first() throws SQLException {
        return super.first();
    }

    protected void generateStatements() throws SQLException {
        int i = 0;
        if (this.isUpdatable) {
            Map<String, String> tableNamesSoFar;
            Map<String, String> tableNamesSoFar2;
            Map<Integer, String> columnIndicesToTable;
            StringBuilder columnNames;
            String quotedId = getQuotedIdChar();
            if (r0.connection.lowerCaseTableNames()) {
                tableNamesSoFar = new TreeMap(String.CASE_INSENSITIVE_ORDER);
                r0.databasesUsedToTablesUsed = new TreeMap(String.CASE_INSENSITIVE_ORDER);
            } else {
                tableNamesSoFar = new TreeMap();
                r0.databasesUsedToTablesUsed = new TreeMap();
            }
            r0.primaryKeyIndicies = new ArrayList();
            StringBuilder fieldValues = new StringBuilder();
            StringBuilder keyValues = new StringBuilder();
            StringBuilder columnNames2 = new StringBuilder();
            StringBuilder insertPlaceHolders = new StringBuilder();
            StringBuilder allTablesBuf = new StringBuilder();
            Map<Integer, String> columnIndicesToTable2 = new HashMap();
            boolean firstTime = true;
            boolean keysFirstTime = true;
            String equalsStr = r0.connection.versionMeetsMinimum(3, 23, 0) ? "<=>" : "=";
            while (i < r0.fields.length) {
                String databaseName;
                Map<String, Integer> updColumnNameToIndex;
                StringBuilder insertPlaceHolders2;
                StringBuilder stringBuilder;
                String tableOnlyName;
                String fqTableName;
                String tableName;
                char databaseName2;
                StringBuilder tableNameBuffer = new StringBuilder();
                Map<String, Integer> updColumnNameToIndex2 = null;
                if (r0.fields[i].getOriginalTableName() != null) {
                    databaseName = r0.fields[i].getDatabaseName();
                    if (databaseName != null && databaseName.length() > 0) {
                        tableNameBuffer.append(quotedId);
                        tableNameBuffer.append(databaseName);
                        tableNameBuffer.append(quotedId);
                        tableNameBuffer.append('.');
                    }
                    updColumnNameToIndex = r0.fields[i].getOriginalTableName();
                    tableNameBuffer.append(quotedId);
                    tableNameBuffer.append(updColumnNameToIndex);
                    tableNameBuffer.append(quotedId);
                    insertPlaceHolders2 = insertPlaceHolders;
                    insertPlaceHolders = tableNameBuffer.toString();
                    if (tableNamesSoFar.containsKey(insertPlaceHolders)) {
                        stringBuilder = columnNames2;
                    } else {
                        if (tableNamesSoFar.isEmpty()) {
                            stringBuilder = columnNames2;
                        } else {
                            stringBuilder = columnNames2;
                            allTablesBuf.append(',');
                        }
                        allTablesBuf.append(insertPlaceHolders);
                        tableNamesSoFar.put(insertPlaceHolders, insertPlaceHolders);
                    }
                    columnIndicesToTable2.put(Integer.valueOf(i), insertPlaceHolders);
                    updColumnNameToIndex = getColumnsToIndexMapForTableAndDB(databaseName, updColumnNameToIndex);
                } else {
                    stringBuilder = columnNames2;
                    insertPlaceHolders2 = insertPlaceHolders;
                    tableOnlyName = r0.fields[i].getTableName();
                    if (tableOnlyName != null) {
                        tableNameBuffer.append(quotedId);
                        tableNameBuffer.append(tableOnlyName);
                        tableNameBuffer.append(quotedId);
                        fqTableName = tableNameBuffer.toString();
                        if (!tableNamesSoFar.containsKey(fqTableName)) {
                            if (!tableNamesSoFar.isEmpty()) {
                                allTablesBuf.append(',');
                            }
                            allTablesBuf.append(fqTableName);
                            tableNamesSoFar.put(fqTableName, fqTableName);
                        }
                        columnIndicesToTable2.put(Integer.valueOf(i), fqTableName);
                        updColumnNameToIndex = getColumnsToIndexMapForTableAndDB(r0.catalog, tableOnlyName);
                    } else {
                        updColumnNameToIndex = updColumnNameToIndex2;
                    }
                }
                tableOnlyName = r0.fields[i].getOriginalName();
                if (!r0.connection.getIO().hasLongColumnInfo() || tableOnlyName == null || tableOnlyName.length() <= 0) {
                    fqTableName = r0.fields[i].getName();
                } else {
                    fqTableName = tableOnlyName;
                }
                if (!(updColumnNameToIndex == null || fqTableName == null)) {
                    updColumnNameToIndex.put(fqTableName, Integer.valueOf(i));
                }
                databaseName = r0.fields[i].getOriginalTableName();
                tableNamesSoFar2 = tableNamesSoFar;
                if (!r0.connection.getIO().hasLongColumnInfo() || databaseName == null || databaseName.length() <= 0) {
                    tableName = r0.fields[i].getTableName();
                } else {
                    tableName = databaseName;
                }
                columnNames2 = new StringBuilder();
                columnIndicesToTable = columnIndicesToTable2;
                String databaseName3 = r0.fields[i].getDatabaseName();
                if (databaseName3 == null || databaseName3.length() <= 0) {
                    databaseName2 = '.';
                } else {
                    columnNames2.append(quotedId);
                    columnNames2.append(databaseName3);
                    columnNames2.append(quotedId);
                    String str = databaseName3;
                    databaseName2 = '.';
                    columnNames2.append('.');
                }
                columnNames2.append(quotedId);
                columnNames2.append(tableName);
                columnNames2.append(quotedId);
                columnNames2.append(databaseName2);
                columnNames2.append(quotedId);
                columnNames2.append(fqTableName);
                columnNames2.append(quotedId);
                databaseName3 = columnNames2.toString();
                String quotedId2 = quotedId;
                if (r0.fields[i].isPrimaryKey() != null) {
                    r0.primaryKeyIndicies.add(Integer.valueOf(i));
                    if (keysFirstTime) {
                        keysFirstTime = false;
                    } else {
                        keyValues.append(" AND ");
                    }
                    keyValues.append(databaseName3);
                    keyValues.append(equalsStr);
                    keyValues.append("?");
                }
                if (firstTime) {
                    fieldValues.append("SET ");
                    firstTime = null;
                    StringBuilder stringBuilder2 = columnNames2;
                    columnNames2 = insertPlaceHolders2;
                    columnNames = stringBuilder;
                } else {
                    fieldValues.append(",");
                    columnNames = stringBuilder;
                    columnNames.append(",");
                    columnNames2 = insertPlaceHolders2;
                    columnNames2.append(",");
                }
                columnNames2.append("?");
                columnNames.append(databaseName3);
                fieldValues.append(databaseName3);
                fieldValues.append("=?");
                i++;
                insertPlaceHolders = columnNames2;
                columnIndicesToTable2 = columnIndicesToTable;
                quotedId = quotedId2;
                columnNames2 = columnNames;
                tableNamesSoFar = tableNamesSoFar2;
            }
            tableNamesSoFar2 = tableNamesSoFar;
            columnNames = columnNames2;
            columnNames2 = insertPlaceHolders;
            columnIndicesToTable = columnIndicesToTable2;
            r0.qualifiedAndQuotedTableName = allTablesBuf.toString();
            StringBuilder stringBuilder3 = new StringBuilder();
            stringBuilder3.append("UPDATE ");
            stringBuilder3.append(r0.qualifiedAndQuotedTableName);
            stringBuilder3.append(" ");
            stringBuilder3.append(fieldValues.toString());
            stringBuilder3.append(" WHERE ");
            stringBuilder3.append(keyValues.toString());
            r0.updateSQL = stringBuilder3.toString();
            stringBuilder3 = new StringBuilder();
            stringBuilder3.append("INSERT INTO ");
            stringBuilder3.append(r0.qualifiedAndQuotedTableName);
            stringBuilder3.append(" (");
            stringBuilder3.append(columnNames.toString());
            stringBuilder3.append(") VALUES (");
            stringBuilder3.append(columnNames2.toString());
            stringBuilder3.append(")");
            r0.insertSQL = stringBuilder3.toString();
            stringBuilder3 = new StringBuilder();
            stringBuilder3.append("SELECT ");
            stringBuilder3.append(columnNames.toString());
            stringBuilder3.append(" FROM ");
            stringBuilder3.append(r0.qualifiedAndQuotedTableName);
            stringBuilder3.append(" WHERE ");
            stringBuilder3.append(keyValues.toString());
            r0.refreshSQL = stringBuilder3.toString();
            stringBuilder3 = new StringBuilder();
            stringBuilder3.append("DELETE FROM ");
            stringBuilder3.append(r0.qualifiedAndQuotedTableName);
            stringBuilder3.append(" WHERE ");
            stringBuilder3.append(keyValues.toString());
            r0.deleteSQL = stringBuilder3.toString();
            return;
        }
        r0.doingUpdates = false;
        r0.onInsertRow = false;
        throw new NotUpdatable(r0.notUpdatableReason);
    }

    private Map<String, Integer> getColumnsToIndexMapForTableAndDB(String databaseName, String tableName) {
        Map<String, Map<String, Integer>> tablesUsedToColumnsMap = (Map) this.databasesUsedToTablesUsed.get(databaseName);
        if (tablesUsedToColumnsMap == null) {
            if (this.connection.lowerCaseTableNames()) {
                tablesUsedToColumnsMap = new TreeMap(String.CASE_INSENSITIVE_ORDER);
            } else {
                tablesUsedToColumnsMap = new TreeMap();
            }
            this.databasesUsedToTablesUsed.put(databaseName, tablesUsedToColumnsMap);
        }
        Map<String, Integer> nameToIndex = (Map) tablesUsedToColumnsMap.get(tableName);
        if (nameToIndex != null) {
            return nameToIndex;
        }
        HashMap nameToIndex2 = new HashMap();
        tablesUsedToColumnsMap.put(tableName, nameToIndex2);
        return nameToIndex2;
    }

    private SingleByteCharsetConverter getCharConverter() throws SQLException {
        if (!this.initializedCharConverter) {
            this.initializedCharConverter = true;
            if (this.connection.getUseUnicode()) {
                this.charEncoding = this.connection.getEncoding();
                this.charConverter = this.connection.getCharsetConverter(this.charEncoding);
            }
        }
        return this.charConverter;
    }

    public int getConcurrency() throws SQLException {
        int i;
        synchronized (checkClosed().getConnectionMutex()) {
            i = this.isUpdatable ? 1008 : 1007;
        }
        return i;
    }

    private String getQuotedIdChar() throws SQLException {
        if (this.quotedIdChar == null) {
            if (this.connection.supportsQuotedIdentifiers()) {
                this.quotedIdChar = this.connection.getMetaData().getIdentifierQuoteString();
            } else {
                this.quotedIdChar = "";
            }
        }
        return this.quotedIdChar;
    }

    public void insertRow() throws SQLException {
        synchronized (checkClosed().getConnectionMutex()) {
            if (this.onInsertRow) {
                this.inserter.executeUpdate();
                long autoIncrementId = this.inserter.getLastInsertID();
                int numFields = this.fields.length;
                byte[][] newRow = new byte[numFields][];
                for (int i = 0; i < numFields; i++) {
                    if (this.inserter.isNull(i)) {
                        newRow[i] = null;
                    } else {
                        newRow[i] = this.inserter.getBytesRepresentation(i);
                    }
                    if (this.fields[i].isAutoIncrement() && autoIncrementId > 0) {
                        newRow[i] = StringUtils.getBytes(String.valueOf(autoIncrementId));
                        this.inserter.setBytesNoEscapeNoQuotes(i + 1, newRow[i]);
                    }
                }
                ResultSetRow resultSetRow = new ByteArrayRow(newRow, getExceptionInterceptor());
                refreshRow(this.inserter, resultSetRow);
                this.rowData.addRow(resultSetRow);
                resetInserter();
            } else {
                throw SQLError.createSQLException(Messages.getString("UpdatableResultSet.7"), getExceptionInterceptor());
            }
        }
    }

    public boolean isAfterLast() throws SQLException {
        return super.isAfterLast();
    }

    public boolean isBeforeFirst() throws SQLException {
        return super.isBeforeFirst();
    }

    public boolean isFirst() throws SQLException {
        return super.isFirst();
    }

    public boolean isLast() throws SQLException {
        return super.isLast();
    }

    boolean isUpdatable() {
        return this.isUpdatable;
    }

    public boolean last() throws SQLException {
        return super.last();
    }

    public void moveToCurrentRow() throws SQLException {
        synchronized (checkClosed().getConnectionMutex()) {
            if (this.isUpdatable) {
                if (this.onInsertRow) {
                    this.onInsertRow = false;
                    this.thisRow = this.savedCurrentRow;
                }
            } else {
                throw new NotUpdatable(this.notUpdatableReason);
            }
        }
    }

    public void moveToInsertRow() throws SQLException {
        synchronized (checkClosed().getConnectionMutex()) {
            if (this.isUpdatable) {
                if (this.inserter == null) {
                    if (this.insertSQL == null) {
                        generateStatements();
                    }
                    this.inserter = (PreparedStatement) this.connection.clientPrepareStatement(this.insertSQL);
                    if (this.populateInserterWithDefaultValues) {
                        extractDefaultValues();
                    }
                    resetInserter();
                } else {
                    resetInserter();
                }
                int numFields = this.fields.length;
                this.onInsertRow = true;
                this.doingUpdates = false;
                this.savedCurrentRow = this.thisRow;
                byte[][] newRowData = new byte[numFields][];
                this.thisRow = new ByteArrayRow(newRowData, getExceptionInterceptor());
                this.thisRow.setMetadata(this.fields);
                byte[][] newRowData2 = newRowData;
                int i = 0;
                while (i < numFields) {
                    if (!this.populateInserterWithDefaultValues) {
                        this.inserter.setBytesNoEscapeNoQuotes(i + 1, StringUtils.getBytes("DEFAULT"));
                        newRowData2 = (byte[][]) null;
                    } else if (this.defaultColumnValue[i] != null) {
                        int mysqlType = this.fields[i].getMysqlType();
                        if (!(mysqlType == 7 || mysqlType == 14)) {
                            switch (mysqlType) {
                                case 10:
                                case 11:
                                case 12:
                                    break;
                                default:
                                    this.inserter.setBytes(i + 1, this.defaultColumnValue[i], false, false);
                                    break;
                            }
                        }
                        if (this.defaultColumnValue[i].length > 7 && this.defaultColumnValue[i][0] == (byte) 67 && this.defaultColumnValue[i][1] == (byte) 85 && this.defaultColumnValue[i][2] == (byte) 82 && this.defaultColumnValue[i][3] == (byte) 82 && this.defaultColumnValue[i][4] == (byte) 69 && this.defaultColumnValue[i][5] == (byte) 78 && this.defaultColumnValue[i][6] == (byte) 84 && this.defaultColumnValue[i][7] == (byte) 95) {
                            this.inserter.setBytesNoEscapeNoQuotes(i + 1, this.defaultColumnValue[i]);
                        } else {
                            this.inserter.setBytes(i + 1, this.defaultColumnValue[i], false, false);
                        }
                        byte[] defaultValueCopy = new byte[this.defaultColumnValue[i].length];
                        System.arraycopy(this.defaultColumnValue[i], 0, defaultValueCopy, 0, defaultValueCopy.length);
                        newRowData2[i] = defaultValueCopy;
                    } else {
                        this.inserter.setNull(i + 1, 0);
                        newRowData2[i] = null;
                    }
                    i++;
                }
            } else {
                throw new NotUpdatable(this.notUpdatableReason);
            }
        }
    }

    public boolean next() throws SQLException {
        return super.next();
    }

    public boolean prev() throws SQLException {
        return super.prev();
    }

    public boolean previous() throws SQLException {
        return super.previous();
    }

    public void realClose(boolean calledExplicitly) throws SQLException {
        if (this.connection != null) {
            synchronized (checkClosed().getConnectionMutex()) {
                SQLException sqlEx = null;
                try {
                    if (r1.useUsageAdvisor && r1.deleter == null && r1.inserter == null && r1.refresher == null && r1.updater == null) {
                        r1.eventSink = ProfilerEventHandlerFactory.getInstance(r1.connection);
                        String message = Messages.getString("UpdatableResultSet.34");
                        ProfilerEventHandler profilerEventHandler = r1.eventSink;
                        ProfilerEvent profilerEvent = r6;
                        ProfilerEvent profilerEvent2 = new ProfilerEvent((byte) 0, "", r1.owningStatement == null ? "N/A" : r1.owningStatement.currentCatalog, r1.connectionId, r1.owningStatement == null ? -1 : r1.owningStatement.getId(), r1.resultId, System.currentTimeMillis(), 0, Constants.MILLIS_I18N, null, r1.pointOfOrigin, message);
                        profilerEventHandler.consumeEvent(profilerEvent);
                    }
                    try {
                        if (r1.deleter != null) {
                            r1.deleter.close();
                        }
                    } catch (SQLException e) {
                        sqlEx = e;
                    }
                    try {
                        if (r1.inserter != null) {
                            r1.inserter.close();
                        }
                    } catch (SQLException e2) {
                        sqlEx = e2;
                    }
                    try {
                        if (r1.refresher != null) {
                            r1.refresher.close();
                        }
                    } catch (SQLException e22) {
                        sqlEx = e22;
                    }
                    try {
                        if (r1.updater != null) {
                            r1.updater.close();
                        }
                    } catch (SQLException e222) {
                        sqlEx = e222;
                    }
                    super.realClose(calledExplicitly);
                    if (sqlEx != null) {
                        throw sqlEx;
                    }
                } catch (Throwable th) {
                    Throwable th2 = th;
                }
            }
        }
    }

    public void refreshRow() throws SQLException {
        synchronized (checkClosed().getConnectionMutex()) {
            if (!this.isUpdatable) {
                throw new NotUpdatable();
            } else if (this.onInsertRow) {
                throw SQLError.createSQLException(Messages.getString("UpdatableResultSet.8"), getExceptionInterceptor());
            } else if (this.rowData.size() == 0) {
                throw SQLError.createSQLException(Messages.getString("UpdatableResultSet.9"), getExceptionInterceptor());
            } else if (isBeforeFirst()) {
                throw SQLError.createSQLException(Messages.getString("UpdatableResultSet.10"), getExceptionInterceptor());
            } else if (isAfterLast()) {
                throw SQLError.createSQLException(Messages.getString("UpdatableResultSet.11"), getExceptionInterceptor());
            } else {
                refreshRow(this.updater, this.thisRow);
            }
        }
    }

    private void refreshRow(PreparedStatement updateInsertStmt, ResultSetRow rowToRefresh) throws SQLException {
        int index;
        if (this.refresher == null) {
            if (this.refreshSQL == null) {
                generateStatements();
            }
            this.refresher = (PreparedStatement) this.connection.clientPrepareStatement(this.refreshSQL);
        }
        this.refresher.clearParameters();
        int numKeys = this.primaryKeyIndicies.size();
        int i = 0;
        byte[] dataFrom;
        if (numKeys == 1) {
            index = ((Integer) this.primaryKeyIndicies.get(0)).intValue();
            if (this.doingUpdates || this.onInsertRow) {
                dataFrom = updateInsertStmt.getBytesRepresentation(index);
                if (!updateInsertStmt.isNull(index)) {
                    if (dataFrom.length != 0) {
                        dataFrom = stripBinaryPrefix(dataFrom);
                    }
                }
                dataFrom = rowToRefresh.getColumnValue(index);
            } else {
                dataFrom = rowToRefresh.getColumnValue(index);
            }
            if (this.fields[index].getvalueNeedsQuoting()) {
                this.refresher.setBytesNoEscape(1, dataFrom);
            } else {
                this.refresher.setBytesNoEscapeNoQuotes(1, dataFrom);
            }
        } else {
            for (int i2 = 0; i2 < numKeys; i2++) {
                index = ((Integer) this.primaryKeyIndicies.get(i2)).intValue();
                if (this.doingUpdates || this.onInsertRow) {
                    dataFrom = updateInsertStmt.getBytesRepresentation(index);
                    if (!updateInsertStmt.isNull(index)) {
                        if (dataFrom.length != 0) {
                            dataFrom = stripBinaryPrefix(dataFrom);
                        }
                    }
                    dataFrom = rowToRefresh.getColumnValue(index);
                } else {
                    dataFrom = rowToRefresh.getColumnValue(index);
                }
                this.refresher.setBytesNoEscape(i2 + 1, dataFrom);
            }
        }
        ResultSet rs = null;
        try {
            rs = this.refresher.executeQuery();
            index = rs.getMetaData().getColumnCount();
            if (rs.next()) {
                while (i < index) {
                    if (rs.getBytes(i + 1) != null) {
                        if (!rs.wasNull()) {
                            rowToRefresh.setColumnValue(i, rs.getBytes(i + 1));
                            i++;
                        }
                    }
                    rowToRefresh.setColumnValue(i, null);
                    i++;
                }
                ResultSet rs2 = rs;
                if (rs2 != null) {
                    try {
                        rs2.close();
                    } catch (SQLException e) {
                    }
                }
                return;
            }
            throw SQLError.createSQLException(Messages.getString("UpdatableResultSet.12"), SQLError.SQL_STATE_GENERAL_ERROR, getExceptionInterceptor());
        } catch (Throwable th) {
            if (rs != null) {
                try {
                    rs.close();
                } catch (SQLException e2) {
                }
            }
        }
    }

    public boolean relative(int rows) throws SQLException {
        return super.relative(rows);
    }

    private void resetInserter() throws SQLException {
        this.inserter.clearParameters();
        for (int i = 0; i < this.fields.length; i++) {
            this.inserter.setNull(i + 1, 0);
        }
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

    protected void setResultSetConcurrency(int concurrencyFlag) {
        super.setResultSetConcurrency(concurrencyFlag);
    }

    private byte[] stripBinaryPrefix(byte[] dataFrom) {
        return StringUtils.stripEnclosure(dataFrom, "_binary'", "'");
    }

    protected void syncUpdate() throws SQLException {
        int i;
        if (this.updater == null) {
            if (this.updateSQL == null) {
                generateStatements();
            }
            this.updater = (PreparedStatement) this.connection.clientPrepareStatement(this.updateSQL);
        }
        int i2 = 0;
        int numFields = this.fields.length;
        this.updater.clearParameters();
        for (i = 0; i < numFields; i++) {
            if (this.thisRow.getColumnValue(i) == null) {
                this.updater.setNull(i + 1, 0);
            } else if (this.fields[i].getvalueNeedsQuoting()) {
                this.updater.setBytes(i + 1, this.thisRow.getColumnValue(i), this.fields[i].isBinary(), false);
            } else {
                this.updater.setBytesNoEscapeNoQuotes(i + 1, this.thisRow.getColumnValue(i));
            }
        }
        i = this.primaryKeyIndicies.size();
        if (i == 1) {
            i2 = ((Integer) this.primaryKeyIndicies.get(0)).intValue();
            setParamValue(this.updater, numFields + 1, this.thisRow, i2, this.fields[i2].getSQLType());
            return;
        }
        while (i2 < i) {
            int idx = ((Integer) this.primaryKeyIndicies.get(i2)).intValue();
            setParamValue(this.updater, (numFields + i2) + 1, this.thisRow, idx, this.fields[idx].getSQLType());
            i2++;
        }
    }

    public void updateAsciiStream(int columnIndex, InputStream x, int length) throws SQLException {
        synchronized (checkClosed().getConnectionMutex()) {
            if (this.onInsertRow) {
                this.inserter.setAsciiStream(columnIndex, x, length);
                this.thisRow.setColumnValue(columnIndex - 1, STREAM_DATA_MARKER);
            } else {
                if (!this.doingUpdates) {
                    this.doingUpdates = true;
                    syncUpdate();
                }
                this.updater.setAsciiStream(columnIndex, x, length);
            }
        }
    }

    public void updateAsciiStream(String columnName, InputStream x, int length) throws SQLException {
        updateAsciiStream(findColumn(columnName), x, length);
    }

    public void updateBigDecimal(int columnIndex, BigDecimal x) throws SQLException {
        synchronized (checkClosed().getConnectionMutex()) {
            if (this.onInsertRow) {
                this.inserter.setBigDecimal(columnIndex, x);
                if (x == null) {
                    this.thisRow.setColumnValue(columnIndex - 1, null);
                } else {
                    this.thisRow.setColumnValue(columnIndex - 1, StringUtils.getBytes(x.toString()));
                }
            } else {
                if (!this.doingUpdates) {
                    this.doingUpdates = true;
                    syncUpdate();
                }
                this.updater.setBigDecimal(columnIndex, x);
            }
        }
    }

    public void updateBigDecimal(String columnName, BigDecimal x) throws SQLException {
        updateBigDecimal(findColumn(columnName), x);
    }

    public void updateBinaryStream(int columnIndex, InputStream x, int length) throws SQLException {
        synchronized (checkClosed().getConnectionMutex()) {
            if (this.onInsertRow) {
                this.inserter.setBinaryStream(columnIndex, x, length);
                if (x == null) {
                    this.thisRow.setColumnValue(columnIndex - 1, null);
                } else {
                    this.thisRow.setColumnValue(columnIndex - 1, STREAM_DATA_MARKER);
                }
            } else {
                if (!this.doingUpdates) {
                    this.doingUpdates = true;
                    syncUpdate();
                }
                this.updater.setBinaryStream(columnIndex, x, length);
            }
        }
    }

    public void updateBinaryStream(String columnName, InputStream x, int length) throws SQLException {
        updateBinaryStream(findColumn(columnName), x, length);
    }

    public void updateBlob(int columnIndex, Blob blob) throws SQLException {
        synchronized (checkClosed().getConnectionMutex()) {
            if (this.onInsertRow) {
                this.inserter.setBlob(columnIndex, blob);
                if (blob == null) {
                    this.thisRow.setColumnValue(columnIndex - 1, null);
                } else {
                    this.thisRow.setColumnValue(columnIndex - 1, STREAM_DATA_MARKER);
                }
            } else {
                if (!this.doingUpdates) {
                    this.doingUpdates = true;
                    syncUpdate();
                }
                this.updater.setBlob(columnIndex, blob);
            }
        }
    }

    public void updateBlob(String columnName, Blob blob) throws SQLException {
        updateBlob(findColumn(columnName), blob);
    }

    public void updateBoolean(int columnIndex, boolean x) throws SQLException {
        synchronized (checkClosed().getConnectionMutex()) {
            if (this.onInsertRow) {
                this.inserter.setBoolean(columnIndex, x);
                this.thisRow.setColumnValue(columnIndex - 1, this.inserter.getBytesRepresentation(columnIndex - 1));
            } else {
                if (!this.doingUpdates) {
                    this.doingUpdates = true;
                    syncUpdate();
                }
                this.updater.setBoolean(columnIndex, x);
            }
        }
    }

    public void updateBoolean(String columnName, boolean x) throws SQLException {
        updateBoolean(findColumn(columnName), x);
    }

    public void updateByte(int columnIndex, byte x) throws SQLException {
        synchronized (checkClosed().getConnectionMutex()) {
            if (this.onInsertRow) {
                this.inserter.setByte(columnIndex, x);
                this.thisRow.setColumnValue(columnIndex - 1, this.inserter.getBytesRepresentation(columnIndex - 1));
            } else {
                if (!this.doingUpdates) {
                    this.doingUpdates = true;
                    syncUpdate();
                }
                this.updater.setByte(columnIndex, x);
            }
        }
    }

    public void updateByte(String columnName, byte x) throws SQLException {
        updateByte(findColumn(columnName), x);
    }

    public void updateBytes(int columnIndex, byte[] x) throws SQLException {
        synchronized (checkClosed().getConnectionMutex()) {
            if (this.onInsertRow) {
                this.inserter.setBytes(columnIndex, x);
                this.thisRow.setColumnValue(columnIndex - 1, x);
            } else {
                if (!this.doingUpdates) {
                    this.doingUpdates = true;
                    syncUpdate();
                }
                this.updater.setBytes(columnIndex, x);
            }
        }
    }

    public void updateBytes(String columnName, byte[] x) throws SQLException {
        updateBytes(findColumn(columnName), x);
    }

    public void updateCharacterStream(int columnIndex, Reader x, int length) throws SQLException {
        synchronized (checkClosed().getConnectionMutex()) {
            if (this.onInsertRow) {
                this.inserter.setCharacterStream(columnIndex, x, length);
                if (x == null) {
                    this.thisRow.setColumnValue(columnIndex - 1, null);
                } else {
                    this.thisRow.setColumnValue(columnIndex - 1, STREAM_DATA_MARKER);
                }
            } else {
                if (!this.doingUpdates) {
                    this.doingUpdates = true;
                    syncUpdate();
                }
                this.updater.setCharacterStream(columnIndex, x, length);
            }
        }
    }

    public void updateCharacterStream(String columnName, Reader reader, int length) throws SQLException {
        updateCharacterStream(findColumn(columnName), reader, length);
    }

    public void updateClob(int columnIndex, Clob clob) throws SQLException {
        synchronized (checkClosed().getConnectionMutex()) {
            if (clob == null) {
                updateNull(columnIndex);
            } else {
                updateCharacterStream(columnIndex, clob.getCharacterStream(), (int) clob.length());
            }
        }
    }

    public void updateDate(int columnIndex, Date x) throws SQLException {
        synchronized (checkClosed().getConnectionMutex()) {
            if (this.onInsertRow) {
                this.inserter.setDate(columnIndex, x);
                this.thisRow.setColumnValue(columnIndex - 1, this.inserter.getBytesRepresentation(columnIndex - 1));
            } else {
                if (!this.doingUpdates) {
                    this.doingUpdates = true;
                    syncUpdate();
                }
                this.updater.setDate(columnIndex, x);
            }
        }
    }

    public void updateDate(String columnName, Date x) throws SQLException {
        updateDate(findColumn(columnName), x);
    }

    public void updateDouble(int columnIndex, double x) throws SQLException {
        synchronized (checkClosed().getConnectionMutex()) {
            if (this.onInsertRow) {
                this.inserter.setDouble(columnIndex, x);
                this.thisRow.setColumnValue(columnIndex - 1, this.inserter.getBytesRepresentation(columnIndex - 1));
            } else {
                if (!this.doingUpdates) {
                    this.doingUpdates = true;
                    syncUpdate();
                }
                this.updater.setDouble(columnIndex, x);
            }
        }
    }

    public void updateDouble(String columnName, double x) throws SQLException {
        updateDouble(findColumn(columnName), x);
    }

    public void updateFloat(int columnIndex, float x) throws SQLException {
        synchronized (checkClosed().getConnectionMutex()) {
            if (this.onInsertRow) {
                this.inserter.setFloat(columnIndex, x);
                this.thisRow.setColumnValue(columnIndex - 1, this.inserter.getBytesRepresentation(columnIndex - 1));
            } else {
                if (!this.doingUpdates) {
                    this.doingUpdates = true;
                    syncUpdate();
                }
                this.updater.setFloat(columnIndex, x);
            }
        }
    }

    public void updateFloat(String columnName, float x) throws SQLException {
        updateFloat(findColumn(columnName), x);
    }

    public void updateInt(int columnIndex, int x) throws SQLException {
        synchronized (checkClosed().getConnectionMutex()) {
            if (this.onInsertRow) {
                this.inserter.setInt(columnIndex, x);
                this.thisRow.setColumnValue(columnIndex - 1, this.inserter.getBytesRepresentation(columnIndex - 1));
            } else {
                if (!this.doingUpdates) {
                    this.doingUpdates = true;
                    syncUpdate();
                }
                this.updater.setInt(columnIndex, x);
            }
        }
    }

    public void updateInt(String columnName, int x) throws SQLException {
        updateInt(findColumn(columnName), x);
    }

    public void updateLong(int columnIndex, long x) throws SQLException {
        synchronized (checkClosed().getConnectionMutex()) {
            if (this.onInsertRow) {
                this.inserter.setLong(columnIndex, x);
                this.thisRow.setColumnValue(columnIndex - 1, this.inserter.getBytesRepresentation(columnIndex - 1));
            } else {
                if (!this.doingUpdates) {
                    this.doingUpdates = true;
                    syncUpdate();
                }
                this.updater.setLong(columnIndex, x);
            }
        }
    }

    public void updateLong(String columnName, long x) throws SQLException {
        updateLong(findColumn(columnName), x);
    }

    public void updateNull(int columnIndex) throws SQLException {
        synchronized (checkClosed().getConnectionMutex()) {
            if (this.onInsertRow) {
                this.inserter.setNull(columnIndex, 0);
                this.thisRow.setColumnValue(columnIndex - 1, null);
            } else {
                if (!this.doingUpdates) {
                    this.doingUpdates = true;
                    syncUpdate();
                }
                this.updater.setNull(columnIndex, 0);
            }
        }
    }

    public void updateNull(String columnName) throws SQLException {
        updateNull(findColumn(columnName));
    }

    public void updateObject(int columnIndex, Object x) throws SQLException {
        updateObjectInternal(columnIndex, x, null, 0);
    }

    public void updateObject(int columnIndex, Object x, int scale) throws SQLException {
        updateObjectInternal(columnIndex, x, null, scale);
    }

    protected void updateObjectInternal(int columnIndex, Object x, Integer targetType, int scaleOrLength) throws SQLException {
        synchronized (checkClosed().getConnectionMutex()) {
            if (this.onInsertRow) {
                if (targetType == null) {
                    this.inserter.setObject(columnIndex, x);
                } else {
                    this.inserter.setObject(columnIndex, x, targetType.intValue());
                }
                this.thisRow.setColumnValue(columnIndex - 1, this.inserter.getBytesRepresentation(columnIndex - 1));
            } else {
                if (!this.doingUpdates) {
                    this.doingUpdates = true;
                    syncUpdate();
                }
                if (targetType == null) {
                    this.updater.setObject(columnIndex, x);
                } else {
                    this.updater.setObject(columnIndex, x, targetType.intValue());
                }
            }
        }
    }

    public void updateObject(String columnName, Object x) throws SQLException {
        updateObject(findColumn(columnName), x);
    }

    public void updateObject(String columnName, Object x, int scale) throws SQLException {
        updateObject(findColumn(columnName), x);
    }

    public void updateRow() throws SQLException {
        synchronized (checkClosed().getConnectionMutex()) {
            if (this.isUpdatable) {
                if (this.doingUpdates) {
                    this.updater.executeUpdate();
                    refreshRow();
                    this.doingUpdates = false;
                } else if (this.onInsertRow) {
                    throw SQLError.createSQLException(Messages.getString("UpdatableResultSet.44"), getExceptionInterceptor());
                }
                syncUpdate();
            } else {
                throw new NotUpdatable(this.notUpdatableReason);
            }
        }
    }

    public void updateShort(int columnIndex, short x) throws SQLException {
        synchronized (checkClosed().getConnectionMutex()) {
            if (this.onInsertRow) {
                this.inserter.setShort(columnIndex, x);
                this.thisRow.setColumnValue(columnIndex - 1, this.inserter.getBytesRepresentation(columnIndex - 1));
            } else {
                if (!this.doingUpdates) {
                    this.doingUpdates = true;
                    syncUpdate();
                }
                this.updater.setShort(columnIndex, x);
            }
        }
    }

    public void updateShort(String columnName, short x) throws SQLException {
        updateShort(findColumn(columnName), x);
    }

    public void updateString(int columnIndex, String x) throws SQLException {
        synchronized (checkClosed().getConnectionMutex()) {
            if (this.onInsertRow) {
                this.inserter.setString(columnIndex, x);
                if (x == null) {
                    this.thisRow.setColumnValue(columnIndex - 1, null);
                } else if (getCharConverter() != null) {
                    this.thisRow.setColumnValue(columnIndex - 1, StringUtils.getBytes(x, this.charConverter, this.charEncoding, this.connection.getServerCharset(), this.connection.parserKnowsUnicode(), getExceptionInterceptor()));
                } else {
                    this.thisRow.setColumnValue(columnIndex - 1, StringUtils.getBytes(x));
                }
            } else {
                if (!this.doingUpdates) {
                    this.doingUpdates = true;
                    syncUpdate();
                }
                this.updater.setString(columnIndex, x);
            }
        }
    }

    public void updateString(String columnName, String x) throws SQLException {
        updateString(findColumn(columnName), x);
    }

    public void updateTime(int columnIndex, Time x) throws SQLException {
        synchronized (checkClosed().getConnectionMutex()) {
            if (this.onInsertRow) {
                this.inserter.setTime(columnIndex, x);
                this.thisRow.setColumnValue(columnIndex - 1, this.inserter.getBytesRepresentation(columnIndex - 1));
            } else {
                if (!this.doingUpdates) {
                    this.doingUpdates = true;
                    syncUpdate();
                }
                this.updater.setTime(columnIndex, x);
            }
        }
    }

    public void updateTime(String columnName, Time x) throws SQLException {
        updateTime(findColumn(columnName), x);
    }

    public void updateTimestamp(int columnIndex, Timestamp x) throws SQLException {
        synchronized (checkClosed().getConnectionMutex()) {
            if (this.onInsertRow) {
                this.inserter.setTimestamp(columnIndex, x);
                this.thisRow.setColumnValue(columnIndex - 1, this.inserter.getBytesRepresentation(columnIndex - 1));
            } else {
                if (!this.doingUpdates) {
                    this.doingUpdates = true;
                    syncUpdate();
                }
                this.updater.setTimestamp(columnIndex, x);
            }
        }
    }

    public void updateTimestamp(String columnName, Timestamp x) throws SQLException {
        updateTimestamp(findColumn(columnName), x);
    }
}
