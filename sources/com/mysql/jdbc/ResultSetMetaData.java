package com.mysql.jdbc;

import android.support.v4.provider.FontsContractCompat.FontRequestCallback;
import android.support.v7.widget.helper.ItemTouchHelper.Callback;
import java.sql.SQLException;

public class ResultSetMetaData implements java.sql.ResultSetMetaData {
    private ExceptionInterceptor exceptionInterceptor;
    Field[] fields;
    boolean treatYearAsDate = true;
    boolean useOldAliasBehavior = false;

    private static int clampedGetLength(Field f) {
        long fieldLength = f.getLength();
        if (fieldLength > 2147483647L) {
            fieldLength = 2147483647L;
        }
        return (int) fieldLength;
    }

    private static final boolean isDecimalType(int type) {
        switch (type) {
            case -7:
            case -6:
            case -5:
                break;
            default:
                switch (type) {
                    case 2:
                    case 3:
                    case 4:
                    case 5:
                    case 6:
                    case 7:
                    case 8:
                        break;
                    default:
                        return false;
                }
        }
        return true;
    }

    public ResultSetMetaData(Field[] fields, boolean useOldAliasBehavior, boolean treatYearAsDate, ExceptionInterceptor exceptionInterceptor) {
        this.fields = fields;
        this.useOldAliasBehavior = useOldAliasBehavior;
        this.treatYearAsDate = treatYearAsDate;
        this.exceptionInterceptor = exceptionInterceptor;
    }

    public String getCatalogName(int column) throws SQLException {
        String database = getField(column).getDatabaseName();
        return database == null ? "" : database;
    }

    public String getColumnCharacterEncoding(int column) throws SQLException {
        String mysqlName = getColumnCharacterSet(column);
        if (mysqlName == null) {
            return null;
        }
        try {
            return CharsetMapping.getJavaEncodingForMysqlCharset(mysqlName);
        } catch (RuntimeException ex) {
            SQLException sqlEx = SQLError.createSQLException(ex.toString(), SQLError.SQL_STATE_ILLEGAL_ARGUMENT, null);
            sqlEx.initCause(ex);
            throw sqlEx;
        }
    }

    public String getColumnCharacterSet(int column) throws SQLException {
        return getField(column).getEncoding();
    }

    public String getColumnClassName(int column) throws SQLException {
        boolean z;
        Field f = getField(column);
        int sQLType = f.getSQLType();
        boolean isUnsigned = f.isUnsigned();
        int mysqlType = f.getMysqlType();
        if (!f.isBinary()) {
            if (!f.isBlob()) {
                z = false;
                return getClassNameForJavaType(sQLType, isUnsigned, mysqlType, z, f.isOpaqueBinary(), this.treatYearAsDate);
            }
        }
        z = true;
        return getClassNameForJavaType(sQLType, isUnsigned, mysqlType, z, f.isOpaqueBinary(), this.treatYearAsDate);
    }

    public int getColumnCount() throws SQLException {
        return this.fields.length;
    }

    public int getColumnDisplaySize(int column) throws SQLException {
        Field f = getField(column);
        return clampedGetLength(f) / f.getMaxBytesPerCharacter();
    }

    public String getColumnLabel(int column) throws SQLException {
        if (this.useOldAliasBehavior) {
            return getColumnName(column);
        }
        return getField(column).getColumnLabel();
    }

    public String getColumnName(int column) throws SQLException {
        if (this.useOldAliasBehavior) {
            return getField(column).getName();
        }
        String name = getField(column).getNameNoAliases();
        if (name == null || name.length() != 0) {
            return name;
        }
        return getField(column).getName();
    }

    public int getColumnType(int column) throws SQLException {
        return getField(column).getSQLType();
    }

    public String getColumnTypeName(int column) throws SQLException {
        Field field = getField(column);
        int mysqlType = field.getMysqlType();
        int jdbcType = field.getSQLType();
        switch (mysqlType) {
            case 0:
                break;
            case 1:
                return field.isUnsigned() ? "TINYINT UNSIGNED" : "TINYINT";
            case 2:
                return field.isUnsigned() ? "SMALLINT UNSIGNED" : "SMALLINT";
            case 3:
                return field.isUnsigned() ? "INT UNSIGNED" : "INT";
            case 4:
                return field.isUnsigned() ? "FLOAT UNSIGNED" : "FLOAT";
            case 5:
                return field.isUnsigned() ? "DOUBLE UNSIGNED" : "DOUBLE";
            case 6:
                return "NULL";
            case 7:
                return "TIMESTAMP";
            case 8:
                return field.isUnsigned() ? "BIGINT UNSIGNED" : "BIGINT";
            case 9:
                return field.isUnsigned() ? "MEDIUMINT UNSIGNED" : "MEDIUMINT";
            case 10:
                return "DATE";
            case 11:
                return "TIME";
            case 12:
                return "DATETIME";
            case 13:
                return "YEAR";
            default:
                switch (mysqlType) {
                    case 15:
                        return "VARCHAR";
                    case 16:
                        return "BIT";
                    default:
                        switch (mysqlType) {
                            case 245:
                                return "JSON";
                            case 246:
                                break;
                            case 247:
                                return "ENUM";
                            case 248:
                                return "SET";
                            case 249:
                                return "TINYBLOB";
                            case Callback.DEFAULT_SWIPE_ANIMATION_DURATION /*250*/:
                                return "MEDIUMBLOB";
                            case 251:
                                return "LONGBLOB";
                            case MysqlDefs.FIELD_TYPE_BLOB /*252*/:
                                if (getField(column).isBinary()) {
                                    return "BLOB";
                                }
                                return "TEXT";
                            case 253:
                                if (jdbcType == -3) {
                                    return "VARBINARY";
                                }
                                return "VARCHAR";
                            case 254:
                                if (jdbcType == -2) {
                                    return "BINARY";
                                }
                                return "CHAR";
                            case 255:
                                return "GEOMETRY";
                            default:
                                return "UNKNOWN";
                        }
                }
        }
        return field.isUnsigned() ? "DECIMAL UNSIGNED" : "DECIMAL";
    }

    protected Field getField(int columnIndex) throws SQLException {
        if (columnIndex >= 1) {
            if (columnIndex <= this.fields.length) {
                return this.fields[columnIndex - 1];
            }
        }
        throw SQLError.createSQLException(Messages.getString("ResultSetMetaData.46"), SQLError.SQL_STATE_INVALID_COLUMN_NUMBER, this.exceptionInterceptor);
    }

    public int getPrecision(int column) throws SQLException {
        Field f = getField(column);
        if (!isDecimalType(f.getSQLType())) {
            switch (f.getMysqlType()) {
                case 249:
                case Callback.DEFAULT_SWIPE_ANIMATION_DURATION /*250*/:
                case 251:
                case MysqlDefs.FIELD_TYPE_BLOB /*252*/:
                    return clampedGetLength(f);
                default:
                    return clampedGetLength(f) / f.getMaxBytesPerCharacter();
            }
        } else if (f.getDecimals() > 0) {
            return (clampedGetLength(f) - 1) + f.getPrecisionAdjustFactor();
        } else {
            return clampedGetLength(f) + f.getPrecisionAdjustFactor();
        }
    }

    public int getScale(int column) throws SQLException {
        Field f = getField(column);
        if (isDecimalType(f.getSQLType())) {
            return f.getDecimals();
        }
        return 0;
    }

    public String getSchemaName(int column) throws SQLException {
        return "";
    }

    public String getTableName(int column) throws SQLException {
        if (this.useOldAliasBehavior) {
            return getField(column).getTableName();
        }
        return getField(column).getTableNameNoAliases();
    }

    public boolean isAutoIncrement(int column) throws SQLException {
        return getField(column).isAutoIncrement();
    }

    public boolean isCaseSensitive(int column) throws SQLException {
        Field field = getField(column);
        int sqlType = field.getSQLType();
        boolean z = false;
        if (sqlType != -1 && sqlType != 1 && sqlType != 12) {
            switch (sqlType) {
                case -7:
                case -6:
                case -5:
                    break;
                default:
                    switch (sqlType) {
                        case 4:
                        case 5:
                        case 6:
                        case 7:
                        case 8:
                            break;
                        default:
                            switch (sqlType) {
                                case 91:
                                case 92:
                                case 93:
                                    break;
                                default:
                                    return true;
                            }
                    }
            }
            return false;
        } else if (field.isBinary()) {
            return true;
        } else {
            String collationName = field.getCollation();
            if (!(collationName == null || collationName.endsWith("_ci"))) {
                z = true;
            }
            return z;
        }
    }

    public boolean isCurrency(int column) throws SQLException {
        return false;
    }

    public boolean isDefinitelyWritable(int column) throws SQLException {
        return isWritable(column);
    }

    public int isNullable(int column) throws SQLException {
        if (getField(column).isNotNull()) {
            return 0;
        }
        return 1;
    }

    public boolean isReadOnly(int column) throws SQLException {
        return getField(column).isReadOnly();
    }

    public boolean isSearchable(int column) throws SQLException {
        return true;
    }

    public boolean isSigned(int column) throws SQLException {
        Field f = getField(column);
        int sqlType = f.getSQLType();
        switch (sqlType) {
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
                        switch (sqlType) {
                            case 91:
                            case 92:
                            case 93:
                                return false;
                            default:
                                return false;
                        }
                }
        }
        return f.isUnsigned() ^ 1;
    }

    public boolean isWritable(int column) throws SQLException {
        return isReadOnly(column) ^ 1;
    }

    public String toString() {
        StringBuilder toStringBuf = new StringBuilder();
        toStringBuf.append(super.toString());
        toStringBuf.append(" - Field level information: ");
        for (Field field : this.fields) {
            toStringBuf.append("\n\t");
            toStringBuf.append(field.toString());
        }
        return toStringBuf.toString();
    }

    static String getClassNameForJavaType(int javaType, boolean isUnsigned, int mysqlTypeIfKnown, boolean isBinaryOrBlob, boolean isOpaqueBinary, boolean treatYearAsDate) {
        if (javaType != 12) {
            if (javaType != 16) {
                switch (javaType) {
                    case -7:
                        break;
                    case -6:
                        if (isUnsigned) {
                            return "java.lang.Integer";
                        }
                        return "java.lang.Integer";
                    case -5:
                        if (isUnsigned) {
                            return "java.math.BigInteger";
                        }
                        return "java.lang.Long";
                    case FontRequestCallback.FAIL_REASON_SECURITY_VIOLATION /*-4*/:
                    case FontRequestCallback.FAIL_REASON_FONT_LOAD_ERROR /*-3*/:
                    case -2:
                        if (mysqlTypeIfKnown == 255) {
                            return "[B";
                        }
                        if (isBinaryOrBlob) {
                            return "[B";
                        }
                        return "java.lang.String";
                    case -1:
                        break;
                    default:
                        switch (javaType) {
                            case 1:
                                break;
                            case 2:
                            case 3:
                                return "java.math.BigDecimal";
                            case 4:
                                if (isUnsigned) {
                                    if (mysqlTypeIfKnown != 9) {
                                        return "java.lang.Long";
                                    }
                                }
                                return "java.lang.Integer";
                            case 5:
                                if (isUnsigned) {
                                    return "java.lang.Integer";
                                }
                                return "java.lang.Integer";
                            case 6:
                            case 8:
                                return "java.lang.Double";
                            case 7:
                                return "java.lang.Float";
                            default:
                                switch (javaType) {
                                    case 91:
                                        String str;
                                        if (!treatYearAsDate) {
                                            if (mysqlTypeIfKnown == 13) {
                                                str = "java.lang.Short";
                                                return str;
                                            }
                                        }
                                        str = "java.sql.Date";
                                        return str;
                                    case 92:
                                        return "java.sql.Time";
                                    case 93:
                                        return "java.sql.Timestamp";
                                    default:
                                        return "java.lang.Object";
                                }
                        }
                }
            }
            return "java.lang.Boolean";
        }
        if (isOpaqueBinary) {
            return "[B";
        }
        return "java.lang.String";
    }

    public boolean isWrapperFor(Class<?> iface) throws SQLException {
        return iface.isInstance(this);
    }

    public <T> T unwrap(Class<T> iface) throws SQLException {
        try {
            return iface.cast(this);
        } catch (ClassCastException e) {
            StringBuilder stringBuilder = new StringBuilder();
            stringBuilder.append("Unable to unwrap to ");
            stringBuilder.append(iface.toString());
            throw SQLError.createSQLException(stringBuilder.toString(), SQLError.SQL_STATE_ILLEGAL_ARGUMENT, this.exceptionInterceptor);
        }
    }
}
