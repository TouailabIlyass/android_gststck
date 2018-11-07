package com.mysql.jdbc;

import android.support.v4.provider.FontsContractCompat.FontRequestCallback;
import android.support.v7.widget.helper.ItemTouchHelper.Callback;
import java.io.UnsupportedEncodingException;
import java.sql.SQLException;
import java.util.regex.PatternSyntaxException;

public class Field {
    private static final int AUTO_INCREMENT_FLAG = 512;
    private static final int NO_CHARSET_INFO = -1;
    private byte[] buffer;
    private int colDecimals;
    private short colFlag;
    private int collationIndex;
    private String collationName;
    private MySQLConnection connection;
    private String databaseName;
    private int databaseNameLength;
    private int databaseNameStart;
    protected int defaultValueLength;
    protected int defaultValueStart;
    private String encoding;
    private String fullName;
    private String fullOriginalName;
    private boolean isImplicitTempTable;
    private boolean isSingleBit;
    private long length;
    private int maxBytesPerChar;
    private int mysqlType;
    private String name;
    private int nameLength;
    private int nameStart;
    private String originalColumnName;
    private int originalColumnNameLength;
    private int originalColumnNameStart;
    private String originalTableName;
    private int originalTableNameLength;
    private int originalTableNameStart;
    private int precisionAdjustFactor;
    private int sqlType;
    private String tableName;
    private int tableNameLength;
    private int tableNameStart;
    private boolean useOldNameMetadata;
    private final boolean valueNeedsQuoting;

    Field(MySQLConnection conn, byte[] buffer, int databaseNameStart, int databaseNameLength, int tableNameStart, int tableNameLength, int originalTableNameStart, int originalTableNameLength, int nameStart, int nameLength, int originalColumnNameStart, int originalColumnNameLength, long length, int mysqlType, short colFlag, int colDecimals, int defaultValueStart, int defaultValueLength, int charsetIndex) throws SQLException {
        this.collationIndex = 0;
        this.encoding = null;
        this.collationName = null;
        this.connection = null;
        this.databaseName = null;
        this.databaseNameLength = -1;
        this.databaseNameStart = -1;
        this.defaultValueLength = -1;
        this.defaultValueStart = -1;
        this.fullName = null;
        this.fullOriginalName = null;
        this.isImplicitTempTable = false;
        this.mysqlType = -1;
        this.originalColumnName = null;
        this.originalColumnNameLength = -1;
        this.originalColumnNameStart = -1;
        this.originalTableName = null;
        this.originalTableNameLength = -1;
        this.originalTableNameStart = -1;
        this.precisionAdjustFactor = 0;
        this.sqlType = -1;
        this.useOldNameMetadata = false;
        this.connection = conn;
        this.buffer = buffer;
        this.nameStart = nameStart;
        this.nameLength = nameLength;
        this.tableNameStart = tableNameStart;
        this.tableNameLength = tableNameLength;
        this.length = length;
        this.colFlag = colFlag;
        this.colDecimals = colDecimals;
        this.mysqlType = mysqlType;
        this.databaseNameStart = databaseNameStart;
        this.databaseNameLength = databaseNameLength;
        this.originalTableNameStart = originalTableNameStart;
        this.originalTableNameLength = originalTableNameLength;
        this.originalColumnNameStart = originalColumnNameStart;
        this.originalColumnNameLength = originalColumnNameLength;
        this.defaultValueStart = defaultValueStart;
        this.defaultValueLength = defaultValueLength;
        this.collationIndex = charsetIndex;
        this.sqlType = MysqlDefs.mysqlToJavaType(this.mysqlType);
        checkForImplicitTemporaryTable();
        boolean isFromFunction = this.originalTableNameLength == 0;
        if (r0.mysqlType == MysqlDefs.FIELD_TYPE_BLOB) {
            if (!r0.connection.getBlobsAreStrings()) {
                if (!r0.connection.getFunctionsNeverReturnBlobs() || !isFromFunction) {
                    if (r0.collationIndex != 63) {
                        if (r0.connection.versionMeetsMinimum(4, 1, 0)) {
                            r0.mysqlType = 253;
                            r0.sqlType = -1;
                        }
                    }
                    if (r0.connection.getUseBlobToStoreUTF8OutsideBMP() && shouldSetupForUtf8StringInBlob()) {
                        setupForUtf8StringInBlob();
                    } else {
                        setBlobTypeBasedOnLength();
                        r0.sqlType = MysqlDefs.mysqlToJavaType(r0.mysqlType);
                    }
                }
            }
            r0.sqlType = 12;
            r0.mysqlType = 15;
        }
        if (r0.sqlType == -6 && r0.length == 1 && r0.connection.getTinyInt1isBit() && conn.getTinyInt1isBit()) {
            if (conn.getTransformedBitIsBoolean()) {
                r0.sqlType = 16;
            } else {
                r0.sqlType = -7;
            }
        }
        if (isNativeNumericType() || isNativeDateTimeType()) {
            r0.encoding = "US-ASCII";
        } else {
            r0.encoding = r0.connection.getEncodingForIndex(r0.collationIndex);
            if ("UnicodeBig".equals(r0.encoding)) {
                r0.encoding = "UTF-16";
            }
            if (r0.mysqlType == 245) {
                r0.encoding = "UTF-8";
            }
            boolean isBinary = isBinary();
            if (r0.connection.versionMeetsMinimum(4, 1, 0) && r0.mysqlType == 253 && isBinary && r0.collationIndex == 63) {
                if (r0.connection.getFunctionsNeverReturnBlobs() && isFromFunction) {
                    r0.sqlType = 12;
                    r0.mysqlType = 15;
                } else if (isOpaqueBinary()) {
                    r0.sqlType = -3;
                }
            }
            if (r0.connection.versionMeetsMinimum(4, 1, 0) && r0.mysqlType == 254 && isBinary && r0.collationIndex == 63 && isOpaqueBinary() && !r0.connection.getBlobsAreStrings()) {
                r0.sqlType = -2;
            }
            if (r0.mysqlType == 16) {
                boolean z;
                if (r0.length != 0) {
                    if (r0.length == 1) {
                        if (!r0.connection.versionMeetsMinimum(5, 0, 21)) {
                            if (r0.connection.versionMeetsMinimum(5, 1, 10)) {
                            }
                        }
                    }
                    z = false;
                    r0.isSingleBit = z;
                    if (!r0.isSingleBit) {
                        r0.colFlag = (short) (r0.colFlag | 128);
                        r0.colFlag = (short) (r0.colFlag | 16);
                        isBinary = true;
                    }
                }
                z = true;
                r0.isSingleBit = z;
                if (r0.isSingleBit) {
                    r0.colFlag = (short) (r0.colFlag | 128);
                    r0.colFlag = (short) (r0.colFlag | 16);
                    isBinary = true;
                }
            }
            if (r0.sqlType == -4 && !isBinary) {
                r0.sqlType = -1;
            } else if (r0.sqlType == -3 && !isBinary) {
                r0.sqlType = 12;
            }
        }
        if (!isUnsigned()) {
            int i = r0.mysqlType;
            if (i != 0 && i != 246) {
                switch (i) {
                    case 4:
                    case 5:
                        r0.precisionAdjustFactor = 1;
                        break;
                    default:
                        break;
                }
            }
            r0.precisionAdjustFactor = -1;
        } else {
            switch (r0.mysqlType) {
                case 4:
                case 5:
                    r0.precisionAdjustFactor = 1;
                    break;
                default:
                    break;
            }
        }
        r0.valueNeedsQuoting = determineNeedsQuoting();
    }

    private boolean shouldSetupForUtf8StringInBlob() throws SQLException {
        SQLException sqlEx;
        String includePattern = this.connection.getUtf8OutsideBmpIncludedColumnNamePattern();
        String excludePattern = this.connection.getUtf8OutsideBmpExcludedColumnNamePattern();
        if (!(excludePattern == null || StringUtils.isEmptyOrWhitespaceOnly(excludePattern))) {
            try {
                if (getOriginalName().matches(excludePattern)) {
                    if (!(includePattern == null || StringUtils.isEmptyOrWhitespaceOnly(includePattern))) {
                        if (getOriginalName().matches(includePattern)) {
                            return true;
                        }
                    }
                    return false;
                }
            } catch (PatternSyntaxException pse) {
                sqlEx = SQLError.createSQLException("Illegal regex specified for \"utf8OutsideBmpIncludedColumnNamePattern\"", SQLError.SQL_STATE_ILLEGAL_ARGUMENT, this.connection.getExceptionInterceptor());
                if (!this.connection.getParanoid()) {
                    sqlEx.initCause(pse);
                }
                throw sqlEx;
            } catch (PatternSyntaxException pse2) {
                sqlEx = SQLError.createSQLException("Illegal regex specified for \"utf8OutsideBmpExcludedColumnNamePattern\"", SQLError.SQL_STATE_ILLEGAL_ARGUMENT, this.connection.getExceptionInterceptor());
                if (!this.connection.getParanoid()) {
                    sqlEx.initCause(pse2);
                }
                throw sqlEx;
            }
        }
        return true;
    }

    private void setupForUtf8StringInBlob() {
        if (this.length != 255) {
            if (this.length != 65535) {
                this.mysqlType = 253;
                this.sqlType = -1;
                this.collationIndex = 33;
            }
        }
        this.mysqlType = 15;
        this.sqlType = 12;
        this.collationIndex = 33;
    }

    Field(MySQLConnection conn, byte[] buffer, int nameStart, int nameLength, int tableNameStart, int tableNameLength, int length, int mysqlType, short colFlag, int colDecimals) throws SQLException {
        this(conn, buffer, -1, -1, tableNameStart, tableNameLength, -1, -1, nameStart, nameLength, -1, -1, (long) length, mysqlType, colFlag, colDecimals, -1, -1, -1);
    }

    Field(String tableName, String columnName, int jdbcType, int length) {
        this.collationIndex = 0;
        this.encoding = null;
        this.collationName = null;
        this.connection = null;
        this.databaseName = null;
        this.databaseNameLength = -1;
        this.databaseNameStart = -1;
        this.defaultValueLength = -1;
        this.defaultValueStart = -1;
        this.fullName = null;
        this.fullOriginalName = null;
        this.isImplicitTempTable = false;
        this.mysqlType = -1;
        this.originalColumnName = null;
        this.originalColumnNameLength = -1;
        this.originalColumnNameStart = -1;
        this.originalTableName = null;
        this.originalTableNameLength = -1;
        this.originalTableNameStart = -1;
        this.precisionAdjustFactor = 0;
        this.sqlType = -1;
        this.useOldNameMetadata = false;
        this.tableName = tableName;
        this.name = columnName;
        this.length = (long) length;
        this.sqlType = jdbcType;
        this.colFlag = (short) 0;
        this.colDecimals = 0;
        this.valueNeedsQuoting = determineNeedsQuoting();
    }

    Field(String tableName, String columnName, int charsetIndex, int jdbcType, int length) {
        this.collationIndex = 0;
        this.encoding = null;
        this.collationName = null;
        this.connection = null;
        this.databaseName = null;
        this.databaseNameLength = -1;
        this.databaseNameStart = -1;
        this.defaultValueLength = -1;
        this.defaultValueStart = -1;
        this.fullName = null;
        this.fullOriginalName = null;
        this.isImplicitTempTable = false;
        this.mysqlType = -1;
        this.originalColumnName = null;
        this.originalColumnNameLength = -1;
        this.originalColumnNameStart = -1;
        this.originalTableName = null;
        this.originalTableNameLength = -1;
        this.originalTableNameStart = -1;
        this.precisionAdjustFactor = 0;
        this.sqlType = -1;
        this.useOldNameMetadata = false;
        this.tableName = tableName;
        this.name = columnName;
        this.length = (long) length;
        this.sqlType = jdbcType;
        this.colFlag = (short) 0;
        this.colDecimals = 0;
        this.collationIndex = charsetIndex;
        this.valueNeedsQuoting = determineNeedsQuoting();
        switch (this.sqlType) {
            case FontRequestCallback.FAIL_REASON_FONT_LOAD_ERROR /*-3*/:
            case -2:
                this.colFlag = (short) (this.colFlag | 128);
                this.colFlag = (short) (this.colFlag | 16);
                return;
            default:
                return;
        }
    }

    private void checkForImplicitTemporaryTable() {
        boolean z = true;
        if (this.tableNameLength <= 5 || this.buffer[this.tableNameStart] != (byte) 35 || this.buffer[this.tableNameStart + 1] != (byte) 115 || this.buffer[this.tableNameStart + 2] != (byte) 113 || this.buffer[this.tableNameStart + 3] != (byte) 108 || this.buffer[this.tableNameStart + 4] != (byte) 95) {
            z = false;
        }
        this.isImplicitTempTable = z;
    }

    public String getEncoding() throws SQLException {
        return this.encoding;
    }

    public void setEncoding(String javaEncodingName, Connection conn) throws SQLException {
        this.encoding = javaEncodingName;
        try {
            this.collationIndex = CharsetMapping.getCollationIndexForJavaEncoding(javaEncodingName, conn);
        } catch (RuntimeException ex) {
            SQLException sqlEx = SQLError.createSQLException(ex.toString(), SQLError.SQL_STATE_ILLEGAL_ARGUMENT, null);
            sqlEx.initCause(ex);
            throw sqlEx;
        }
    }

    public synchronized java.lang.String getCollation() throws java.sql.SQLException {
        /* JADX: method processing error */
/*
Error: jadx.core.utils.exceptions.JadxRuntimeException: Exception block dominator not found, method:com.mysql.jdbc.Field.getCollation():java.lang.String. bs: [B:25:0x0084, B:49:0x00cf]
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
        r10 = this;
        monitor-enter(r10);
        r0 = r10.collationName;	 Catch:{ all -> 0x00ec }
        if (r0 != 0) goto L_0x00e7;	 Catch:{ all -> 0x00ec }
    L_0x0005:
        r0 = r10.connection;	 Catch:{ all -> 0x00ec }
        if (r0 == 0) goto L_0x00e7;	 Catch:{ all -> 0x00ec }
    L_0x0009:
        r0 = r10.connection;	 Catch:{ all -> 0x00ec }
        r1 = 4;	 Catch:{ all -> 0x00ec }
        r2 = 1;	 Catch:{ all -> 0x00ec }
        r3 = 0;	 Catch:{ all -> 0x00ec }
        r0 = r0.versionMeetsMinimum(r1, r2, r3);	 Catch:{ all -> 0x00ec }
        if (r0 == 0) goto L_0x00e7;	 Catch:{ all -> 0x00ec }
    L_0x0014:
        r0 = r10.connection;	 Catch:{ all -> 0x00ec }
        r0 = r0.getUseDynamicCharsetInfo();	 Catch:{ all -> 0x00ec }
        r1 = 0;	 Catch:{ all -> 0x00ec }
        if (r0 == 0) goto L_0x00cf;	 Catch:{ all -> 0x00ec }
    L_0x001d:
        r0 = r10.connection;	 Catch:{ all -> 0x00ec }
        r0 = r0.getMetaData();	 Catch:{ all -> 0x00ec }
        r2 = r0.getIdentifierQuoteString();	 Catch:{ all -> 0x00ec }
        r3 = " ";	 Catch:{ all -> 0x00ec }
        r3 = r3.equals(r2);	 Catch:{ all -> 0x00ec }
        if (r3 == 0) goto L_0x0032;	 Catch:{ all -> 0x00ec }
    L_0x002f:
        r3 = "";	 Catch:{ all -> 0x00ec }
        r2 = r3;	 Catch:{ all -> 0x00ec }
    L_0x0032:
        r3 = r10.getDatabaseName();	 Catch:{ all -> 0x00ec }
        r4 = r10.getOriginalTableName();	 Catch:{ all -> 0x00ec }
        r5 = r10.getOriginalName();	 Catch:{ all -> 0x00ec }
        if (r3 == 0) goto L_0x00cd;	 Catch:{ all -> 0x00ec }
    L_0x0040:
        r6 = r3.length();	 Catch:{ all -> 0x00ec }
        if (r6 == 0) goto L_0x00cd;	 Catch:{ all -> 0x00ec }
    L_0x0046:
        if (r4 == 0) goto L_0x00cd;	 Catch:{ all -> 0x00ec }
    L_0x0048:
        r6 = r4.length();	 Catch:{ all -> 0x00ec }
        if (r6 == 0) goto L_0x00cd;	 Catch:{ all -> 0x00ec }
    L_0x004e:
        if (r5 == 0) goto L_0x00cd;	 Catch:{ all -> 0x00ec }
    L_0x0050:
        r6 = r5.length();	 Catch:{ all -> 0x00ec }
        if (r6 == 0) goto L_0x00cd;	 Catch:{ all -> 0x00ec }
    L_0x0056:
        r6 = new java.lang.StringBuilder;	 Catch:{ all -> 0x00ec }
        r7 = r3.length();	 Catch:{ all -> 0x00ec }
        r8 = r4.length();	 Catch:{ all -> 0x00ec }
        r7 = r7 + r8;	 Catch:{ all -> 0x00ec }
        r7 = r7 + 28;	 Catch:{ all -> 0x00ec }
        r6.<init>(r7);	 Catch:{ all -> 0x00ec }
        r7 = "SHOW FULL COLUMNS FROM ";	 Catch:{ all -> 0x00ec }
        r6.append(r7);	 Catch:{ all -> 0x00ec }
        r6.append(r2);	 Catch:{ all -> 0x00ec }
        r6.append(r3);	 Catch:{ all -> 0x00ec }
        r6.append(r2);	 Catch:{ all -> 0x00ec }
        r7 = ".";	 Catch:{ all -> 0x00ec }
        r6.append(r7);	 Catch:{ all -> 0x00ec }
        r6.append(r2);	 Catch:{ all -> 0x00ec }
        r6.append(r4);	 Catch:{ all -> 0x00ec }
        r6.append(r2);	 Catch:{ all -> 0x00ec }
        r7 = 0;
        r8 = r10.connection;	 Catch:{ all -> 0x00be }
        r8 = r8.createStatement();	 Catch:{ all -> 0x00be }
        r7 = r8;	 Catch:{ all -> 0x00be }
        r8 = r6.toString();	 Catch:{ all -> 0x00be }
        r8 = r7.executeQuery(r8);	 Catch:{ all -> 0x00be }
        r1 = r8;	 Catch:{ all -> 0x00be }
    L_0x0094:
        r8 = r1.next();	 Catch:{ all -> 0x00be }
        if (r8 == 0) goto L_0x00af;	 Catch:{ all -> 0x00be }
    L_0x009a:
        r8 = "Field";	 Catch:{ all -> 0x00be }
        r8 = r1.getString(r8);	 Catch:{ all -> 0x00be }
        r8 = r5.equals(r8);	 Catch:{ all -> 0x00be }
        if (r8 == 0) goto L_0x0094;	 Catch:{ all -> 0x00be }
    L_0x00a6:
        r8 = "Collation";	 Catch:{ all -> 0x00be }
        r8 = r1.getString(r8);	 Catch:{ all -> 0x00be }
        r10.collationName = r8;	 Catch:{ all -> 0x00be }
        r8 = r10;
        if (r1 == 0) goto L_0x00b7;
    L_0x00b3:
        r1.close();	 Catch:{ all -> 0x00ec }
        r1 = 0;	 Catch:{ all -> 0x00ec }
    L_0x00b7:
        if (r7 == 0) goto L_0x00bd;	 Catch:{ all -> 0x00ec }
    L_0x00b9:
        r7.close();	 Catch:{ all -> 0x00ec }
        r7 = 0;	 Catch:{ all -> 0x00ec }
    L_0x00bd:
        goto L_0x00ce;	 Catch:{ all -> 0x00ec }
    L_0x00be:
        r8 = move-exception;	 Catch:{ all -> 0x00ec }
        r9 = r10;	 Catch:{ all -> 0x00ec }
        if (r1 == 0) goto L_0x00c6;	 Catch:{ all -> 0x00ec }
    L_0x00c2:
        r1.close();	 Catch:{ all -> 0x00ec }
        r1 = 0;	 Catch:{ all -> 0x00ec }
    L_0x00c6:
        if (r7 == 0) goto L_0x00cc;	 Catch:{ all -> 0x00ec }
    L_0x00c8:
        r7.close();	 Catch:{ all -> 0x00ec }
        r7 = 0;	 Catch:{ all -> 0x00ec }
    L_0x00cc:
        throw r8;	 Catch:{ all -> 0x00ec }
    L_0x00cd:
        r8 = r10;
    L_0x00ce:
        goto L_0x00e8;
    L_0x00cf:
        r0 = com.mysql.jdbc.CharsetMapping.COLLATION_INDEX_TO_COLLATION_NAME;	 Catch:{ RuntimeException -> 0x00d8 }
        r2 = r10.collationIndex;	 Catch:{ RuntimeException -> 0x00d8 }
        r0 = r0[r2];	 Catch:{ RuntimeException -> 0x00d8 }
        r10.collationName = r0;	 Catch:{ RuntimeException -> 0x00d8 }
        goto L_0x00e7;
    L_0x00d8:
        r0 = move-exception;
        r2 = r0.toString();	 Catch:{ all -> 0x00ec }
        r3 = "S1009";	 Catch:{ all -> 0x00ec }
        r1 = com.mysql.jdbc.SQLError.createSQLException(r2, r3, r1);	 Catch:{ all -> 0x00ec }
        r1.initCause(r0);	 Catch:{ all -> 0x00ec }
        throw r1;	 Catch:{ all -> 0x00ec }
    L_0x00e7:
        r8 = r10;	 Catch:{ all -> 0x00ec }
    L_0x00e8:
        r0 = r8.collationName;	 Catch:{ all -> 0x00ec }
        monitor-exit(r10);
        return r0;
    L_0x00ec:
        r0 = move-exception;
        monitor-exit(r10);
        throw r0;
        */
        throw new UnsupportedOperationException("Method not decompiled: com.mysql.jdbc.Field.getCollation():java.lang.String");
    }

    public String getColumnLabel() throws SQLException {
        return getName();
    }

    public String getDatabaseName() throws SQLException {
        if (!(this.databaseName != null || this.databaseNameStart == -1 || this.databaseNameLength == -1)) {
            this.databaseName = getStringFromBytes(this.databaseNameStart, this.databaseNameLength);
        }
        return this.databaseName;
    }

    int getDecimals() {
        return this.colDecimals;
    }

    public String getFullName() throws SQLException {
        if (this.fullName == null) {
            StringBuilder fullNameBuf = new StringBuilder((getTableName().length() + 1) + getName().length());
            fullNameBuf.append(this.tableName);
            fullNameBuf.append('.');
            fullNameBuf.append(this.name);
            this.fullName = fullNameBuf.toString();
        }
        return this.fullName;
    }

    public String getFullOriginalName() throws SQLException {
        getOriginalName();
        if (this.originalColumnName == null) {
            return null;
        }
        if (this.fullName == null) {
            StringBuilder fullOriginalNameBuf = new StringBuilder((getOriginalTableName().length() + 1) + getOriginalName().length());
            fullOriginalNameBuf.append(this.originalTableName);
            fullOriginalNameBuf.append('.');
            fullOriginalNameBuf.append(this.originalColumnName);
            this.fullOriginalName = fullOriginalNameBuf.toString();
        }
        return this.fullOriginalName;
    }

    public long getLength() {
        return this.length;
    }

    public synchronized int getMaxBytesPerCharacter() throws SQLException {
        if (this.maxBytesPerChar == 0) {
            this.maxBytesPerChar = this.connection.getMaxBytesPerChar(Integer.valueOf(this.collationIndex), getEncoding());
        }
        return this.maxBytesPerChar;
    }

    public int getMysqlType() {
        return this.mysqlType;
    }

    public String getName() throws SQLException {
        if (this.name == null) {
            this.name = getStringFromBytes(this.nameStart, this.nameLength);
        }
        return this.name;
    }

    public String getNameNoAliases() throws SQLException {
        if (this.useOldNameMetadata) {
            return getName();
        }
        if (this.connection == null || !this.connection.versionMeetsMinimum(4, 1, 0)) {
            return getName();
        }
        return getOriginalName();
    }

    public String getOriginalName() throws SQLException {
        if (!(this.originalColumnName != null || this.originalColumnNameStart == -1 || this.originalColumnNameLength == -1)) {
            this.originalColumnName = getStringFromBytes(this.originalColumnNameStart, this.originalColumnNameLength);
        }
        return this.originalColumnName;
    }

    public String getOriginalTableName() throws SQLException {
        if (!(this.originalTableName != null || this.originalTableNameStart == -1 || this.originalTableNameLength == -1)) {
            this.originalTableName = getStringFromBytes(this.originalTableNameStart, this.originalTableNameLength);
        }
        return this.originalTableName;
    }

    public int getPrecisionAdjustFactor() {
        return this.precisionAdjustFactor;
    }

    public int getSQLType() {
        return this.sqlType;
    }

    private String getStringFromBytes(int stringStart, int stringLength) throws SQLException {
        if (stringStart != -1) {
            if (stringLength != -1) {
                if (stringLength == 0) {
                    return "";
                }
                String stringVal;
                if (this.connection == null) {
                    stringVal = StringUtils.toAsciiString(this.buffer, stringStart, stringLength);
                } else if (this.connection.getUseUnicode()) {
                    String javaEncoding = this.connection.getCharacterSetMetadata();
                    if (javaEncoding == null) {
                        javaEncoding = this.connection.getEncoding();
                    }
                    if (javaEncoding != null) {
                        SingleByteCharsetConverter converter = null;
                        if (this.connection != null) {
                            converter = this.connection.getCharsetConverter(javaEncoding);
                        }
                        if (converter != null) {
                            stringVal = converter.toString(this.buffer, stringStart, stringLength);
                        } else {
                            try {
                                stringVal = StringUtils.toString(this.buffer, stringStart, stringLength, javaEncoding);
                            } catch (UnsupportedEncodingException e) {
                                StringBuilder stringBuilder = new StringBuilder();
                                stringBuilder.append(Messages.getString("Field.12"));
                                stringBuilder.append(javaEncoding);
                                stringBuilder.append(Messages.getString("Field.13"));
                                throw new RuntimeException(stringBuilder.toString());
                            }
                        }
                    }
                    stringVal = StringUtils.toAsciiString(this.buffer, stringStart, stringLength);
                } else {
                    stringVal = StringUtils.toAsciiString(this.buffer, stringStart, stringLength);
                }
                return stringVal;
            }
        }
        return null;
    }

    public String getTable() throws SQLException {
        return getTableName();
    }

    public String getTableName() throws SQLException {
        if (this.tableName == null) {
            this.tableName = getStringFromBytes(this.tableNameStart, this.tableNameLength);
        }
        return this.tableName;
    }

    public String getTableNameNoAliases() throws SQLException {
        if (this.connection.versionMeetsMinimum(4, 1, 0)) {
            return getOriginalTableName();
        }
        return getTableName();
    }

    public boolean isAutoIncrement() {
        return (this.colFlag & 512) > 0;
    }

    public boolean isBinary() {
        return (this.colFlag & 128) > 0;
    }

    public boolean isBlob() {
        return (this.colFlag & 16) > 0;
    }

    private boolean isImplicitTemporaryTable() {
        return this.isImplicitTempTable;
    }

    public boolean isMultipleKey() {
        return (this.colFlag & 8) > 0;
    }

    boolean isNotNull() {
        return (this.colFlag & 1) > 0;
    }

    boolean isOpaqueBinary() throws SQLException {
        boolean z = true;
        if (this.collationIndex != 63 || !isBinary() || (getMysqlType() != 254 && getMysqlType() != 253)) {
            if (!this.connection.versionMeetsMinimum(4, 1, 0) || !"binary".equalsIgnoreCase(getEncoding())) {
                z = false;
            }
            return z;
        } else if (this.originalTableNameLength != 0 || this.connection == null || this.connection.versionMeetsMinimum(5, 0, 25)) {
            return isImplicitTemporaryTable() ^ true;
        } else {
            return false;
        }
    }

    public boolean isPrimaryKey() {
        return (this.colFlag & 2) > 0;
    }

    boolean isReadOnly() throws SQLException {
        boolean z = true;
        if (!this.connection.versionMeetsMinimum(4, 1, 0)) {
            return false;
        }
        String orgColumnName = getOriginalName();
        String orgTableName = getOriginalTableName();
        if (!(orgColumnName == null || orgColumnName.length() <= 0 || orgTableName == null)) {
            if (orgTableName.length() > 0) {
                z = false;
            }
        }
        return z;
    }

    public boolean isUniqueKey() {
        return (this.colFlag & 4) > 0;
    }

    public boolean isUnsigned() {
        return (this.colFlag & 32) > 0;
    }

    public void setUnsigned() {
        this.colFlag = (short) (this.colFlag | 32);
    }

    public boolean isZeroFill() {
        return (this.colFlag & 64) > 0;
    }

    private void setBlobTypeBasedOnLength() {
        if (this.length == 255) {
            this.mysqlType = 249;
        } else if (this.length == 65535) {
            this.mysqlType = MysqlDefs.FIELD_TYPE_BLOB;
        } else if (this.length == 16777215) {
            this.mysqlType = Callback.DEFAULT_SWIPE_ANIMATION_DURATION;
        } else if (this.length == 4294967295L) {
            this.mysqlType = 251;
        }
    }

    private boolean isNativeNumericType() {
        if ((this.mysqlType < 1 || this.mysqlType > 5) && this.mysqlType != 8) {
            return this.mysqlType == 13;
        } else {
            return true;
        }
    }

    private boolean isNativeDateTimeType() {
        if (!(this.mysqlType == 10 || this.mysqlType == 14 || this.mysqlType == 12 || this.mysqlType == 11)) {
            if (this.mysqlType != 7) {
                return false;
            }
        }
        return true;
    }

    public void setConnection(MySQLConnection conn) {
        this.connection = conn;
        if (this.encoding == null || this.collationIndex == 0) {
            this.encoding = this.connection.getEncoding();
        }
    }

    void setMysqlType(int type) {
        this.mysqlType = type;
        this.sqlType = MysqlDefs.mysqlToJavaType(this.mysqlType);
    }

    protected void setUseOldNameMetadata(boolean useOldNameMetadata) {
        this.useOldNameMetadata = useOldNameMetadata;
    }

    public String toString() {
        try {
            StringBuilder asString = new StringBuilder();
            asString.append(super.toString());
            asString.append("[");
            asString.append("catalog=");
            asString.append(getDatabaseName());
            asString.append(",tableName=");
            asString.append(getTableName());
            asString.append(",originalTableName=");
            asString.append(getOriginalTableName());
            asString.append(",columnName=");
            asString.append(getName());
            asString.append(",originalColumnName=");
            asString.append(getOriginalName());
            asString.append(",mysqlType=");
            asString.append(getMysqlType());
            asString.append("(");
            asString.append(MysqlDefs.typeToName(getMysqlType()));
            asString.append(")");
            asString.append(",flags=");
            if (isAutoIncrement()) {
                asString.append(" AUTO_INCREMENT");
            }
            if (isPrimaryKey()) {
                asString.append(" PRIMARY_KEY");
            }
            if (isUniqueKey()) {
                asString.append(" UNIQUE_KEY");
            }
            if (isBinary()) {
                asString.append(" BINARY");
            }
            if (isBlob()) {
                asString.append(" BLOB");
            }
            if (isMultipleKey()) {
                asString.append(" MULTI_KEY");
            }
            if (isUnsigned()) {
                asString.append(" UNSIGNED");
            }
            if (isZeroFill()) {
                asString.append(" ZEROFILL");
            }
            asString.append(", charsetIndex=");
            asString.append(this.collationIndex);
            asString.append(", charsetName=");
            asString.append(this.encoding);
            asString.append("]");
            return asString.toString();
        } catch (Throwable th) {
            return super.toString();
        }
    }

    protected boolean isSingleBit() {
        return this.isSingleBit;
    }

    protected boolean getvalueNeedsQuoting() {
        return this.valueNeedsQuoting;
    }

    private boolean determineNeedsQuoting() {
        int i = this.sqlType;
        switch (i) {
            case -7:
            case -6:
            case -5:
                break;
            default:
                switch (i) {
                    case 2:
                    case 3:
                    case 4:
                    case 5:
                    case 6:
                    case 7:
                    case 8:
                        break;
                    default:
                        return true;
                }
        }
        return false;
    }
}
