package com.mysql.jdbc;

import android.support.v4.internal.view.SupportMenu;
import android.support.v4.provider.FontsContractCompat.FontRequestCallback;
import android.support.v4.view.ViewCompat;
import java.io.UnsupportedEncodingException;
import java.lang.reflect.Constructor;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.SortedMap;
import java.util.StringTokenizer;
import java.util.TreeMap;
import java.util.TreeSet;

public class DatabaseMetaData implements java.sql.DatabaseMetaData {
    private static final int DEFERRABILITY = 13;
    private static final int DELETE_RULE = 10;
    private static final int FKCOLUMN_NAME = 7;
    private static final int FKTABLE_CAT = 4;
    private static final int FKTABLE_NAME = 6;
    private static final int FKTABLE_SCHEM = 5;
    private static final int FK_NAME = 11;
    private static final Constructor<?> JDBC_4_DBMD_IS_CTOR;
    private static final Constructor<?> JDBC_4_DBMD_SHOW_CTOR;
    private static final int KEY_SEQ = 8;
    protected static final int MAX_IDENTIFIER_LENGTH = 64;
    private static final String[] MYSQL_KEYWORDS = new String[]{"ACCESSIBLE", "ADD", "ALL", "ALTER", "ANALYZE", "AND", "AS", "ASC", "ASENSITIVE", "BEFORE", "BETWEEN", "BIGINT", "BINARY", "BLOB", "BOTH", "BY", "CALL", "CASCADE", "CASE", "CHANGE", "CHAR", "CHARACTER", "CHECK", "COLLATE", "COLUMN", "CONDITION", "CONSTRAINT", "CONTINUE", "CONVERT", "CREATE", "CROSS", "CURRENT_DATE", "CURRENT_TIME", "CURRENT_TIMESTAMP", "CURRENT_USER", "CURSOR", "DATABASE", "DATABASES", "DAY_HOUR", "DAY_MICROSECOND", "DAY_MINUTE", "DAY_SECOND", "DEC", "DECIMAL", "DECLARE", "DEFAULT", "DELAYED", "DELETE", "DESC", "DESCRIBE", "DETERMINISTIC", "DISTINCT", "DISTINCTROW", "DIV", "DOUBLE", "DROP", "DUAL", "EACH", "ELSE", "ELSEIF", "ENCLOSED", "ESCAPED", "EXISTS", "EXIT", "EXPLAIN", "FALSE", "FETCH", "FLOAT", "FLOAT4", "FLOAT8", "FOR", "FORCE", "FOREIGN", "FROM", "FULLTEXT", "GENERATED", "GET", "GRANT", "GROUP", "HAVING", "HIGH_PRIORITY", "HOUR_MICROSECOND", "HOUR_MINUTE", "HOUR_SECOND", "IF", "IGNORE", "IN", "INDEX", "INFILE", "INNER", "INOUT", "INSENSITIVE", "INSERT", "INT", "INT1", "INT2", "INT3", "INT4", "INT8", "INTEGER", "INTERVAL", "INTO", "IO_AFTER_GTIDS", "IO_BEFORE_GTIDS", "IS", "ITERATE", "JOIN", "KEY", "KEYS", "KILL", "LEADING", "LEAVE", "LEFT", "LIKE", "LIMIT", "LINEAR", "LINES", "LOAD", "LOCALTIME", "LOCALTIMESTAMP", "LOCK", "LONG", "LONGBLOB", "LONGTEXT", "LOOP", "LOW_PRIORITY", "MASTER_BIND", "MASTER_SSL_VERIFY_SERVER_CERT", "MATCH", "MAXVALUE", "MEDIUMBLOB", "MEDIUMINT", "MEDIUMTEXT", "MIDDLEINT", "MINUTE_MICROSECOND", "MINUTE_SECOND", "MOD", "MODIFIES", "NATURAL", "NOT", "NO_WRITE_TO_BINLOG", "NULL", "NUMERIC", "ON", "OPTIMIZE", "OPTIMIZER_COSTS", "OPTION", "OPTIONALLY", "OR", "ORDER", "OUT", "OUTER", "OUTFILE", "PARTITION", "PRECISION", "PRIMARY", "PROCEDURE", "PURGE", "RANGE", "READ", "READS", "READ_WRITE", "REAL", "REFERENCES", "REGEXP", "RELEASE", "RENAME", "REPEAT", "REPLACE", "REQUIRE", "RESIGNAL", "RESTRICT", "RETURN", "REVOKE", "RIGHT", "RLIKE", "SCHEMA", "SCHEMAS", "SECOND_MICROSECOND", "SELECT", "SENSITIVE", "SEPARATOR", "SET", "SHOW", "SIGNAL", "SMALLINT", "SPATIAL", "SPECIFIC", "SQL", "SQLEXCEPTION", "SQLSTATE", "SQLWARNING", "SQL_BIG_RESULT", "SQL_CALC_FOUND_ROWS", "SQL_SMALL_RESULT", "SSL", "STARTING", "STORED", "STRAIGHT_JOIN", "TABLE", "TERMINATED", "THEN", "TINYBLOB", "TINYINT", "TINYTEXT", "TO", "TRAILING", "TRIGGER", "TRUE", "UNDO", "UNION", "UNIQUE", "UNLOCK", "UNSIGNED", "UPDATE", "USAGE", "USE", "USING", "UTC_DATE", "UTC_TIME", "UTC_TIMESTAMP", "VALUES", "VARBINARY", "VARCHAR", "VARCHARACTER", "VARYING", "VIRTUAL", "WHEN", "WHERE", "WHILE", "WITH", "WRITE", "XOR", "YEAR_MONTH", "ZEROFILL"};
    private static final int PKCOLUMN_NAME = 3;
    private static final int PKTABLE_CAT = 0;
    private static final int PKTABLE_NAME = 2;
    private static final int PKTABLE_SCHEM = 1;
    private static final int PK_NAME = 12;
    private static final String[] SQL2003_KEYWORDS = new String[]{"ABS", "ALL", "ALLOCATE", "ALTER", "AND", "ANY", "ARE", "ARRAY", "AS", "ASENSITIVE", "ASYMMETRIC", "AT", "ATOMIC", "AUTHORIZATION", "AVG", "BEGIN", "BETWEEN", "BIGINT", "BINARY", "BLOB", "BOOLEAN", "BOTH", "BY", "CALL", "CALLED", "CARDINALITY", "CASCADED", "CASE", "CAST", "CEIL", "CEILING", "CHAR", "CHARACTER", "CHARACTER_LENGTH", "CHAR_LENGTH", "CHECK", "CLOB", "CLOSE", "COALESCE", "COLLATE", "COLLECT", "COLUMN", "COMMIT", "CONDITION", "CONNECT", "CONSTRAINT", "CONVERT", "CORR", "CORRESPONDING", "COUNT", "COVAR_POP", "COVAR_SAMP", "CREATE", "CROSS", "CUBE", "CUME_DIST", "CURRENT", "CURRENT_DATE", "CURRENT_DEFAULT_TRANSFORM_GROUP", "CURRENT_PATH", "CURRENT_ROLE", "CURRENT_TIME", "CURRENT_TIMESTAMP", "CURRENT_TRANSFORM_GROUP_FOR_TYPE", "CURRENT_USER", "CURSOR", "CYCLE", "DATE", "DAY", "DEALLOCATE", "DEC", "DECIMAL", "DECLARE", "DEFAULT", "DELETE", "DENSE_RANK", "DEREF", "DESCRIBE", "DETERMINISTIC", "DISCONNECT", "DISTINCT", "DOUBLE", "DROP", "DYNAMIC", "EACH", "ELEMENT", "ELSE", "END", "END-EXEC", "ESCAPE", "EVERY", "EXCEPT", "EXEC", "EXECUTE", "EXISTS", "EXP", "EXTERNAL", "EXTRACT", "FALSE", "FETCH", "FILTER", "FLOAT", "FLOOR", "FOR", "FOREIGN", "FREE", "FROM", "FULL", "FUNCTION", "FUSION", "GET", "GLOBAL", "GRANT", "GROUP", "GROUPING", "HAVING", "HOLD", "HOUR", "IDENTITY", "IN", "INDICATOR", "INNER", "INOUT", "INSENSITIVE", "INSERT", "INT", "INTEGER", "INTERSECT", "INTERSECTION", "INTERVAL", "INTO", "IS", "JOIN", "LANGUAGE", "LARGE", "LATERAL", "LEADING", "LEFT", "LIKE", "LN", "LOCAL", "LOCALTIME", "LOCALTIMESTAMP", "LOWER", "MATCH", "MAX", "MEMBER", "MERGE", "METHOD", "MIN", "MINUTE", "MOD", "MODIFIES", "MODULE", "MONTH", "MULTISET", "NATIONAL", "NATURAL", "NCHAR", "NCLOB", "NEW", "NO", "NONE", "NORMALIZE", "NOT", "NULL", "NULLIF", "NUMERIC", "OCTET_LENGTH", "OF", "OLD", "ON", "ONLY", "OPEN", "OR", "ORDER", "OUT", "OUTER", "OVER", "OVERLAPS", "OVERLAY", "PARAMETER", "PARTITION", "PERCENTILE_CONT", "PERCENTILE_DISC", "PERCENT_RANK", "POSITION", "POWER", "PRECISION", "PREPARE", "PRIMARY", "PROCEDURE", "RANGE", "RANK", "READS", "REAL", "RECURSIVE", "REF", "REFERENCES", "REFERENCING", "REGR_AVGX", "REGR_AVGY", "REGR_COUNT", "REGR_INTERCEPT", "REGR_R2", "REGR_SLOPE", "REGR_SXX", "REGR_SXY", "REGR_SYY", "RELEASE", "RESULT", "RETURN", "RETURNS", "REVOKE", "RIGHT", "ROLLBACK", "ROLLUP", "ROW", "ROWS", "ROW_NUMBER", "SAVEPOINT", "SCOPE", "SCROLL", "SEARCH", "SECOND", "SELECT", "SENSITIVE", "SESSION_USER", "SET", "SIMILAR", "SMALLINT", "SOME", "SPECIFIC", "SPECIFICTYPE", "SQL", "SQLEXCEPTION", "SQLSTATE", "SQLWARNING", "SQRT", "START", "STATIC", "STDDEV_POP", "STDDEV_SAMP", "SUBMULTISET", "SUBSTRING", "SUM", "SYMMETRIC", "SYSTEM", "SYSTEM_USER", "TABLE", "TABLESAMPLE", "THEN", "TIME", "TIMESTAMP", "TIMEZONE_HOUR", "TIMEZONE_MINUTE", "TO", "TRAILING", "TRANSLATE", "TRANSLATION", "TREAT", "TRIGGER", "TRIM", "TRUE", "UESCAPE", "UNION", "UNIQUE", "UNKNOWN", "UNNEST", "UPDATE", "UPPER", "USER", "USING", "VALUE", "VALUES", "VARCHAR", "VARYING", "VAR_POP", "VAR_SAMP", "WHEN", "WHENEVER", "WHERE", "WIDTH_BUCKET", "WINDOW", "WITH", "WITHIN", "WITHOUT", "YEAR"};
    private static final String[] SQL92_KEYWORDS = new String[]{"ABSOLUTE", "ACTION", "ADD", "ALL", "ALLOCATE", "ALTER", "AND", "ANY", "ARE", "AS", "ASC", "ASSERTION", "AT", "AUTHORIZATION", "AVG", "BEGIN", "BETWEEN", "BIT", "BIT_LENGTH", "BOTH", "BY", "CASCADE", "CASCADED", "CASE", "CAST", "CATALOG", "CHAR", "CHARACTER", "CHARACTER_LENGTH", "CHAR_LENGTH", "CHECK", "CLOSE", "COALESCE", "COLLATE", "COLLATION", "COLUMN", "COMMIT", "CONNECT", "CONNECTION", "CONSTRAINT", "CONSTRAINTS", "CONTINUE", "CONVERT", "CORRESPONDING", "COUNT", "CREATE", "CROSS", "CURRENT", "CURRENT_DATE", "CURRENT_TIME", "CURRENT_TIMESTAMP", "CURRENT_USER", "CURSOR", "DATE", "DAY", "DEALLOCATE", "DEC", "DECIMAL", "DECLARE", "DEFAULT", "DEFERRABLE", "DEFERRED", "DELETE", "DESC", "DESCRIBE", "DESCRIPTOR", "DIAGNOSTICS", "DISCONNECT", "DISTINCT", "DOMAIN", "DOUBLE", "DROP", "ELSE", "END", "END-EXEC", "ESCAPE", "EXCEPT", "EXCEPTION", "EXEC", "EXECUTE", "EXISTS", "EXTERNAL", "EXTRACT", "FALSE", "FETCH", "FIRST", "FLOAT", "FOR", "FOREIGN", "FOUND", "FROM", "FULL", "GET", "GLOBAL", "GO", "GOTO", "GRANT", "GROUP", "HAVING", "HOUR", "IDENTITY", "IMMEDIATE", "IN", "INDICATOR", "INITIALLY", "INNER", "INPUT", "INSENSITIVE", "INSERT", "INT", "INTEGER", "INTERSECT", "INTERVAL", "INTO", "IS", "ISOLATION", "JOIN", "KEY", "LANGUAGE", "LAST", "LEADING", "LEFT", "LEVEL", "LIKE", "LOCAL", "LOWER", "MATCH", "MAX", "MIN", "MINUTE", "MODULE", "MONTH", "NAMES", "NATIONAL", "NATURAL", "NCHAR", "NEXT", "NO", "NOT", "NULL", "NULLIF", "NUMERIC", "OCTET_LENGTH", "OF", "ON", "ONLY", "OPEN", "OPTION", "OR", "ORDER", "OUTER", "OUTPUT", "OVERLAPS", "PAD", "PARTIAL", "POSITION", "PRECISION", "PREPARE", "PRESERVE", "PRIMARY", "PRIOR", "PRIVILEGES", "PROCEDURE", "PUBLIC", "READ", "REAL", "REFERENCES", "RELATIVE", "RESTRICT", "REVOKE", "RIGHT", "ROLLBACK", "ROWS", "SCHEMA", "SCROLL", "SECOND", "SECTION", "SELECT", "SESSION", "SESSION_USER", "SET", "SIZE", "SMALLINT", "SOME", "SPACE", "SQL", "SQLCODE", "SQLERROR", "SQLSTATE", "SUBSTRING", "SUM", "SYSTEM_USER", "TABLE", "TEMPORARY", "THEN", "TIME", "TIMESTAMP", "TIMEZONE_HOUR", "TIMEZONE_MINUTE", "TO", "TRAILING", "TRANSACTION", "TRANSLATE", "TRANSLATION", "TRIM", "TRUE", "UNION", "UNIQUE", "UNKNOWN", "UPDATE", "UPPER", "USAGE", "USER", "USING", "VALUE", "VALUES", "VARCHAR", "VARYING", "VIEW", "WHEN", "WHENEVER", "WHERE", "WITH", "WORK", "WRITE", "YEAR", "ZONE"};
    private static final String SUPPORTS_FK = "SUPPORTS_FK";
    protected static final byte[] SYSTEM_TABLE_AS_BYTES = "SYSTEM TABLE".getBytes();
    protected static final byte[] TABLE_AS_BYTES = "TABLE".getBytes();
    private static final int UPDATE_RULE = 9;
    protected static final byte[] VIEW_AS_BYTES = "VIEW".getBytes();
    private static volatile String mysqlKeywords = null;
    protected MySQLConnection conn;
    protected String database = null;
    private ExceptionInterceptor exceptionInterceptor;
    protected final String quotedId;

    protected class ComparableWrapper<K extends Comparable<? super K>, V> implements Comparable<ComparableWrapper<K, V>> {
        static final /* synthetic */ boolean $assertionsDisabled = false;
        K key;
        V value;

        static {
            Class cls = DatabaseMetaData.class;
        }

        public ComparableWrapper(K key, V value) {
            this.key = key;
            this.value = value;
        }

        public K getKey() {
            return this.key;
        }

        public V getValue() {
            return this.value;
        }

        public int compareTo(ComparableWrapper<K, V> other) {
            return ((Comparable) getKey()).compareTo(other.getKey());
        }

        public boolean equals(Object obj) {
            if (obj == null) {
                return false;
            }
            if (obj == this) {
                return true;
            }
            if (!(obj instanceof ComparableWrapper)) {
                return false;
            }
            return this.key.equals(((ComparableWrapper) obj).getKey());
        }

        public int hashCode() {
            return 0;
        }

        public String toString() {
            StringBuilder stringBuilder = new StringBuilder();
            stringBuilder.append("{KEY:");
            stringBuilder.append(this.key);
            stringBuilder.append("; VALUE:");
            stringBuilder.append(this.value);
            stringBuilder.append("}");
            return stringBuilder.toString();
        }
    }

    protected class IndexMetaDataKey implements Comparable<IndexMetaDataKey> {
        static final /* synthetic */ boolean $assertionsDisabled = false;
        String columnIndexName;
        Boolean columnNonUnique;
        Short columnOrdinalPosition;
        Short columnType;

        static {
            Class cls = DatabaseMetaData.class;
        }

        IndexMetaDataKey(boolean columnNonUnique, short columnType, String columnIndexName, short columnOrdinalPosition) {
            this.columnNonUnique = Boolean.valueOf(columnNonUnique);
            this.columnType = Short.valueOf(columnType);
            this.columnIndexName = columnIndexName;
            this.columnOrdinalPosition = Short.valueOf(columnOrdinalPosition);
        }

        public int compareTo(IndexMetaDataKey indexInfoKey) {
            int compareTo = this.columnNonUnique.compareTo(indexInfoKey.columnNonUnique);
            int compareResult = compareTo;
            if (compareTo != 0) {
                return compareResult;
            }
            compareTo = this.columnType.compareTo(indexInfoKey.columnType);
            compareResult = compareTo;
            if (compareTo != 0) {
                return compareResult;
            }
            compareTo = this.columnIndexName.compareTo(indexInfoKey.columnIndexName);
            compareResult = compareTo;
            if (compareTo != 0) {
                return compareResult;
            }
            return this.columnOrdinalPosition.compareTo(indexInfoKey.columnOrdinalPosition);
        }

        public boolean equals(Object obj) {
            boolean z = false;
            if (obj == null) {
                return false;
            }
            if (obj == this) {
                return true;
            }
            if (!(obj instanceof IndexMetaDataKey)) {
                return false;
            }
            if (compareTo((IndexMetaDataKey) obj) == 0) {
                z = true;
            }
            return z;
        }

        public int hashCode() {
            return 0;
        }
    }

    protected abstract class IteratorWithCleanup<T> {
        abstract void close() throws SQLException;

        abstract boolean hasNext() throws SQLException;

        abstract T next() throws SQLException;

        protected IteratorWithCleanup() {
        }
    }

    class LocalAndReferencedColumns {
        String constraintName;
        List<String> localColumnsList;
        String referencedCatalog;
        List<String> referencedColumnsList;
        String referencedTable;

        LocalAndReferencedColumns(List<String> localColumns, List<String> refColumns, String constName, String refCatalog, String refTable) {
            this.localColumnsList = localColumns;
            this.referencedColumnsList = refColumns;
            this.constraintName = constName;
            this.referencedTable = refTable;
            this.referencedCatalog = refCatalog;
        }
    }

    protected enum ProcedureType {
        PROCEDURE,
        FUNCTION
    }

    protected class TableMetaDataKey implements Comparable<TableMetaDataKey> {
        static final /* synthetic */ boolean $assertionsDisabled = false;
        String tableCat;
        String tableName;
        String tableSchem;
        String tableType;

        static {
            Class cls = DatabaseMetaData.class;
        }

        TableMetaDataKey(String tableType, String tableCat, String tableSchem, String tableName) {
            this.tableType = tableType == null ? "" : tableType;
            this.tableCat = tableCat == null ? "" : tableCat;
            this.tableSchem = tableSchem == null ? "" : tableSchem;
            this.tableName = tableName == null ? "" : tableName;
        }

        public int compareTo(TableMetaDataKey tablesKey) {
            int compareTo = this.tableType.compareTo(tablesKey.tableType);
            int compareResult = compareTo;
            if (compareTo != 0) {
                return compareResult;
            }
            compareTo = this.tableCat.compareTo(tablesKey.tableCat);
            compareResult = compareTo;
            if (compareTo != 0) {
                return compareResult;
            }
            compareTo = this.tableSchem.compareTo(tablesKey.tableSchem);
            compareResult = compareTo;
            if (compareTo != 0) {
                return compareResult;
            }
            return this.tableName.compareTo(tablesKey.tableName);
        }

        public boolean equals(Object obj) {
            boolean z = false;
            if (obj == null) {
                return false;
            }
            if (obj == this) {
                return true;
            }
            if (!(obj instanceof TableMetaDataKey)) {
                return false;
            }
            if (compareTo((TableMetaDataKey) obj) == 0) {
                z = true;
            }
            return z;
        }

        public int hashCode() {
            return 0;
        }
    }

    protected enum TableType {
        LOCAL_TEMPORARY("LOCAL TEMPORARY"),
        SYSTEM_TABLE("SYSTEM TABLE"),
        SYSTEM_VIEW("SYSTEM VIEW"),
        TABLE("TABLE", new String[]{"BASE TABLE"}),
        VIEW("VIEW"),
        UNKNOWN("UNKNOWN");
        
        private String name;
        private byte[] nameAsBytes;
        private String[] synonyms;

        private TableType(String tableTypeName) {
            this(r2, r3, tableTypeName, null);
        }

        private TableType(String tableTypeName, String[] tableTypeSynonyms) {
            this.name = tableTypeName;
            this.nameAsBytes = tableTypeName.getBytes();
            this.synonyms = tableTypeSynonyms;
        }

        String getName() {
            return this.name;
        }

        byte[] asBytes() {
            return this.nameAsBytes;
        }

        boolean equalsTo(String tableTypeName) {
            return this.name.equalsIgnoreCase(tableTypeName);
        }

        static TableType getTableTypeEqualTo(String tableTypeName) {
            for (TableType tableType : values()) {
                if (tableType.equalsTo(tableTypeName)) {
                    return tableType;
                }
            }
            return UNKNOWN;
        }

        boolean compliesWith(String tableTypeName) {
            if (equalsTo(tableTypeName)) {
                return true;
            }
            if (this.synonyms != null) {
                for (String synonym : this.synonyms) {
                    if (synonym.equalsIgnoreCase(tableTypeName)) {
                        return true;
                    }
                }
            }
            return false;
        }

        static TableType getTableTypeCompliantWith(String tableTypeName) {
            for (TableType tableType : values()) {
                if (tableType.compliesWith(tableTypeName)) {
                    return tableType;
                }
            }
            return UNKNOWN;
        }
    }

    class TypeDescriptor {
        int bufferLength;
        int charOctetLength;
        Integer columnSize;
        short dataType;
        Integer decimalDigits;
        String isNullable;
        int nullability;
        int numPrecRadix = 10;
        final /* synthetic */ DatabaseMetaData this$0;
        String typeName;

        TypeDescriptor(DatabaseMetaData databaseMetaData, String typeInfo, String nullabilityInfo) throws SQLException {
            DatabaseMetaData databaseMetaData2 = databaseMetaData;
            String str = typeInfo;
            String str2 = nullabilityInfo;
            this.this$0 = databaseMetaData2;
            if (str == null) {
                throw SQLError.createSQLException("NULL typeinfo not supported.", SQLError.SQL_STATE_ILLEGAL_ARGUMENT, databaseMetaData.getExceptionInterceptor());
            }
            String fullMysqlType;
            String mysqlType = "";
            if (str.indexOf("(") != -1) {
                mysqlType = str.substring(0, str.indexOf("(")).trim();
            } else {
                mysqlType = str;
            }
            int indexOfUnsignedInMysqlType = StringUtils.indexOfIgnoreCase(mysqlType, "unsigned");
            if (indexOfUnsignedInMysqlType != -1) {
                mysqlType = mysqlType.substring(0, indexOfUnsignedInMysqlType - 1);
            }
            boolean isUnsigned = false;
            if (StringUtils.indexOfIgnoreCase(str, "unsigned") == -1 || StringUtils.indexOfIgnoreCase(str, "set") == 0 || StringUtils.indexOfIgnoreCase(str, "enum") == 0) {
                fullMysqlType = mysqlType;
            } else {
                StringBuilder stringBuilder = new StringBuilder();
                stringBuilder.append(mysqlType);
                stringBuilder.append(" unsigned");
                fullMysqlType = stringBuilder.toString();
                isUnsigned = true;
            }
            if (databaseMetaData2.conn.getCapitalizeTypeNames()) {
                fullMysqlType = fullMysqlType.toUpperCase(Locale.ENGLISH);
            }
            r0.dataType = (short) MysqlDefs.mysqlToJavaType(mysqlType);
            r0.typeName = fullMysqlType;
            StringTokenizer tokenizer;
            int maxLength;
            if (StringUtils.startsWithIgnoreCase(str, "enum")) {
                tokenizer = new StringTokenizer(str.substring(str.indexOf("("), str.lastIndexOf(")")), ",");
                maxLength = 0;
                while (tokenizer.hasMoreTokens()) {
                    maxLength = Math.max(maxLength, tokenizer.nextToken().length() - 2);
                }
                r0.columnSize = Integer.valueOf(maxLength);
                r0.decimalDigits = null;
            } else if (StringUtils.startsWithIgnoreCase(str, "set")) {
                tokenizer = new StringTokenizer(str.substring(str.indexOf("(") + 1, str.lastIndexOf(")")), ",");
                maxLength = 0;
                int numElements = tokenizer.countTokens();
                if (numElements > 0) {
                    maxLength = 0 + (numElements - 1);
                }
                while (tokenizer.hasMoreTokens()) {
                    String setMember = tokenizer.nextToken().trim();
                    if (setMember.startsWith("'") && setMember.endsWith("'")) {
                        maxLength += setMember.length() - 2;
                    } else {
                        maxLength += setMember.length();
                    }
                }
                r0.columnSize = Integer.valueOf(maxLength);
                r0.decimalDigits = null;
            } else if (str.indexOf(",") != -1) {
                r0.columnSize = Integer.valueOf(str.substring(str.indexOf("(") + 1, str.indexOf(",")).trim());
                r0.decimalDigits = Integer.valueOf(str.substring(str.indexOf(",") + 1, str.indexOf(")")).trim());
            } else {
                r0.columnSize = null;
                r0.decimalDigits = null;
                int endParenIndex;
                if ((StringUtils.indexOfIgnoreCase(str, "char") != -1 || StringUtils.indexOfIgnoreCase(str, "text") != -1 || StringUtils.indexOfIgnoreCase(str, "blob") != -1 || StringUtils.indexOfIgnoreCase(str, "binary") != -1 || StringUtils.indexOfIgnoreCase(str, "bit") != -1) && str.indexOf("(") != -1) {
                    endParenIndex = str.indexOf(")");
                    if (endParenIndex == -1) {
                        endParenIndex = typeInfo.length();
                    }
                    r0.columnSize = Integer.valueOf(str.substring(str.indexOf("(") + 1, endParenIndex).trim());
                    if (databaseMetaData2.conn.getTinyInt1isBit() && r0.columnSize.intValue() == 1 && StringUtils.startsWithIgnoreCase(str, 0, "tinyint")) {
                        if (databaseMetaData2.conn.getTransformedBitIsBoolean()) {
                            r0.dataType = (short) 16;
                            r0.typeName = "BOOLEAN";
                        } else {
                            r0.dataType = (short) -7;
                            r0.typeName = "BIT";
                        }
                    }
                } else if (StringUtils.startsWithIgnoreCaseAndWs(str, "tinyint")) {
                    if (!databaseMetaData2.conn.getTinyInt1isBit() || str.indexOf("(1)") == -1) {
                        r0.columnSize = Integer.valueOf(3);
                        r0.decimalDigits = Integer.valueOf(0);
                    } else if (databaseMetaData2.conn.getTransformedBitIsBoolean()) {
                        r0.dataType = (short) 16;
                        r0.typeName = "BOOLEAN";
                    } else {
                        r0.dataType = (short) -7;
                        r0.typeName = "BIT";
                    }
                } else if (StringUtils.startsWithIgnoreCaseAndWs(str, "smallint")) {
                    r0.columnSize = Integer.valueOf(5);
                    r0.decimalDigits = Integer.valueOf(0);
                } else {
                    endParenIndex = 8;
                    if (StringUtils.startsWithIgnoreCaseAndWs(str, "mediumint")) {
                        if (!isUnsigned) {
                            endParenIndex = 7;
                        }
                        r0.columnSize = Integer.valueOf(endParenIndex);
                        r0.decimalDigits = Integer.valueOf(0);
                    } else if (StringUtils.startsWithIgnoreCaseAndWs(str, "int")) {
                        r0.columnSize = Integer.valueOf(10);
                        r0.decimalDigits = Integer.valueOf(0);
                    } else if (StringUtils.startsWithIgnoreCaseAndWs(str, "integer")) {
                        r0.columnSize = Integer.valueOf(10);
                        r0.decimalDigits = Integer.valueOf(0);
                    } else {
                        int i = 19;
                        if (StringUtils.startsWithIgnoreCaseAndWs(str, "bigint")) {
                            if (isUnsigned) {
                                i = 20;
                            }
                            r0.columnSize = Integer.valueOf(i);
                            r0.decimalDigits = Integer.valueOf(0);
                        } else if (StringUtils.startsWithIgnoreCaseAndWs(str, "int24")) {
                            r0.columnSize = Integer.valueOf(19);
                            r0.decimalDigits = Integer.valueOf(0);
                        } else if (StringUtils.startsWithIgnoreCaseAndWs(str, "real")) {
                            r0.columnSize = Integer.valueOf(12);
                        } else if (StringUtils.startsWithIgnoreCaseAndWs(str, "float")) {
                            r0.columnSize = Integer.valueOf(12);
                        } else if (StringUtils.startsWithIgnoreCaseAndWs(str, "decimal")) {
                            r0.columnSize = Integer.valueOf(12);
                        } else if (StringUtils.startsWithIgnoreCaseAndWs(str, "numeric")) {
                            r0.columnSize = Integer.valueOf(12);
                        } else if (StringUtils.startsWithIgnoreCaseAndWs(str, "double")) {
                            r0.columnSize = Integer.valueOf(22);
                        } else if (StringUtils.startsWithIgnoreCaseAndWs(str, "char")) {
                            r0.columnSize = Integer.valueOf(1);
                        } else if (StringUtils.startsWithIgnoreCaseAndWs(str, "varchar")) {
                            r0.columnSize = Integer.valueOf(255);
                        } else if (StringUtils.startsWithIgnoreCaseAndWs(str, "timestamp")) {
                            r0.columnSize = Integer.valueOf(19);
                        } else if (StringUtils.startsWithIgnoreCaseAndWs(str, "datetime")) {
                            r0.columnSize = Integer.valueOf(19);
                        } else if (StringUtils.startsWithIgnoreCaseAndWs(str, "date")) {
                            r0.columnSize = Integer.valueOf(10);
                        } else if (StringUtils.startsWithIgnoreCaseAndWs(str, "time")) {
                            r0.columnSize = Integer.valueOf(8);
                        } else if (StringUtils.startsWithIgnoreCaseAndWs(str, "tinyblob")) {
                            r0.columnSize = Integer.valueOf(255);
                        } else if (StringUtils.startsWithIgnoreCaseAndWs(str, "blob")) {
                            r0.columnSize = Integer.valueOf(SupportMenu.USER_MASK);
                        } else if (StringUtils.startsWithIgnoreCaseAndWs(str, "mediumblob")) {
                            r0.columnSize = Integer.valueOf(ViewCompat.MEASURED_SIZE_MASK);
                        } else if (StringUtils.startsWithIgnoreCaseAndWs(str, "longblob")) {
                            r0.columnSize = Integer.valueOf(ActivityChooserViewAdapter.MAX_ACTIVITY_COUNT_UNLIMITED);
                        } else if (StringUtils.startsWithIgnoreCaseAndWs(str, "tinytext")) {
                            r0.columnSize = Integer.valueOf(255);
                        } else if (StringUtils.startsWithIgnoreCaseAndWs(str, "text")) {
                            r0.columnSize = Integer.valueOf(SupportMenu.USER_MASK);
                        } else if (StringUtils.startsWithIgnoreCaseAndWs(str, "mediumtext")) {
                            r0.columnSize = Integer.valueOf(ViewCompat.MEASURED_SIZE_MASK);
                        } else if (StringUtils.startsWithIgnoreCaseAndWs(str, "longtext")) {
                            r0.columnSize = Integer.valueOf(ActivityChooserViewAdapter.MAX_ACTIVITY_COUNT_UNLIMITED);
                        } else if (StringUtils.startsWithIgnoreCaseAndWs(str, "enum")) {
                            r0.columnSize = Integer.valueOf(255);
                        } else if (StringUtils.startsWithIgnoreCaseAndWs(str, "set")) {
                            r0.columnSize = Integer.valueOf(255);
                        }
                    }
                }
            }
            r0.bufferLength = MysqlIO.getMaxBuf();
            r0.numPrecRadix = 10;
            if (str2 == null) {
                r0.nullability = 0;
                r0.isNullable = "NO";
            } else if (str2.equals("YES")) {
                r0.nullability = 1;
                r0.isNullable = "YES";
            } else if (str2.equals("UNKNOWN")) {
                r0.nullability = 2;
                r0.isNullable = "";
            } else {
                r0.nullability = 0;
                r0.isNullable = "NO";
            }
        }
    }

    protected class ResultSetIterator extends IteratorWithCleanup<String> {
        int colIndex;
        ResultSet resultSet;

        ResultSetIterator(ResultSet rs, int index) {
            super();
            this.resultSet = rs;
            this.colIndex = index;
        }

        void close() throws SQLException {
            this.resultSet.close();
        }

        boolean hasNext() throws SQLException {
            return this.resultSet.next();
        }

        String next() throws SQLException {
            return this.resultSet.getObject(this.colIndex).toString();
        }
    }

    protected class SingleStringIterator extends IteratorWithCleanup<String> {
        boolean onFirst = true;
        String value;

        SingleStringIterator(String s) {
            super();
            this.value = s;
        }

        void close() throws SQLException {
        }

        boolean hasNext() throws SQLException {
            return this.onFirst;
        }

        String next() throws SQLException {
            this.onFirst = false;
            return this.value;
        }
    }

    static {
        if (Util.isJdbc4()) {
            try {
                JDBC_4_DBMD_SHOW_CTOR = Class.forName("com.mysql.jdbc.JDBC4DatabaseMetaData").getConstructor(new Class[]{MySQLConnection.class, String.class});
                JDBC_4_DBMD_IS_CTOR = Class.forName("com.mysql.jdbc.JDBC4DatabaseMetaDataUsingInfoSchema").getConstructor(new Class[]{MySQLConnection.class, String.class});
            } catch (SecurityException e) {
                throw new RuntimeException(e);
            } catch (NoSuchMethodException e2) {
                throw new RuntimeException(e2);
            } catch (ClassNotFoundException e3) {
                throw new RuntimeException(e3);
            }
        }
        JDBC_4_DBMD_IS_CTOR = null;
        JDBC_4_DBMD_SHOW_CTOR = null;
    }

    protected static DatabaseMetaData getInstance(MySQLConnection connToSet, String databaseToSet, boolean checkForInfoSchema) throws SQLException {
        if (Util.isJdbc4()) {
            if (checkForInfoSchema && connToSet.getUseInformationSchema() && connToSet.versionMeetsMinimum(5, 0, 7)) {
                return (DatabaseMetaData) Util.handleNewInstance(JDBC_4_DBMD_IS_CTOR, new Object[]{connToSet, databaseToSet}, connToSet.getExceptionInterceptor());
            }
            return (DatabaseMetaData) Util.handleNewInstance(JDBC_4_DBMD_SHOW_CTOR, new Object[]{connToSet, databaseToSet}, connToSet.getExceptionInterceptor());
        } else if (checkForInfoSchema && connToSet.getUseInformationSchema() && connToSet.versionMeetsMinimum(5, 0, 7)) {
            return new DatabaseMetaDataUsingInfoSchema(connToSet, databaseToSet);
        } else {
            return new DatabaseMetaData(connToSet, databaseToSet);
        }
    }

    protected DatabaseMetaData(MySQLConnection connToSet, String databaseToSet) {
        String identifierQuote = null;
        this.conn = connToSet;
        this.database = databaseToSet;
        this.exceptionInterceptor = this.conn.getExceptionInterceptor();
        try {
            identifierQuote = getIdentifierQuoteString();
        } catch (SQLException sqlEx) {
            AssertionFailedException.shouldNotHappen(sqlEx);
        } catch (Throwable th) {
            this.quotedId = null;
        }
        this.quotedId = identifierQuote;
    }

    public boolean allProceduresAreCallable() throws SQLException {
        return false;
    }

    public boolean allTablesAreSelectable() throws SQLException {
        return false;
    }

    private ResultSet buildResultSet(Field[] fields, ArrayList<ResultSetRow> rows) throws SQLException {
        return buildResultSet(fields, rows, this.conn);
    }

    static ResultSet buildResultSet(Field[] fields, ArrayList<ResultSetRow> rows, MySQLConnection c) throws SQLException {
        int fieldsLength = fields.length;
        for (int i = 0; i < fieldsLength; i++) {
            int jdbcType = fields[i].getSQLType();
            if (jdbcType == -1 || jdbcType == 1 || jdbcType == 12) {
                fields[i].setEncoding(c.getCharacterSetMetadata(), c);
            }
            fields[i].setConnection(c);
            fields[i].setUseOldNameMetadata(true);
        }
        return ResultSetImpl.getInstance(c.getCatalog(), fields, new RowDataStatic(rows), c, null, false);
    }

    protected void convertToJdbcFunctionList(String catalog, ResultSet proceduresRs, boolean needsClientFiltering, String db, List<ComparableWrapper<String, ResultSetRow>> procedureRows, int nameIndex, Field[] fields) throws SQLException {
        List<ComparableWrapper<String, ResultSetRow>> list;
        int i;
        DatabaseMetaData databaseMetaData = this;
        String str = catalog;
        ResultSet resultSet = proceduresRs;
        String str2 = db;
        Field[] fieldArr = fields;
        while (proceduresRs.next()) {
            boolean shouldAdd = true;
            if (needsClientFiltering) {
                shouldAdd = false;
                String procDb = resultSet.getString(1);
                if (str2 == null && procDb == null) {
                    shouldAdd = true;
                } else if (str2 != null && str2.equals(procDb)) {
                    shouldAdd = true;
                }
            }
            if (shouldAdd) {
                String functionName = resultSet.getString(nameIndex);
                byte[][] rowData = null;
                if (fieldArr == null || fieldArr.length != 9) {
                    rowData = new byte[6][];
                    rowData[0] = str == null ? null : s2b(catalog);
                    rowData[1] = null;
                    rowData[2] = s2b(functionName);
                    rowData[3] = s2b(resultSet.getString("comment"));
                    rowData[4] = s2b(Integer.toString(getJDBC4FunctionNoTableConstant()));
                    rowData[5] = s2b(functionName);
                } else {
                    rowData = new byte[9][];
                    rowData[0] = str == null ? null : s2b(catalog);
                    rowData[1] = null;
                    rowData[2] = s2b(functionName);
                    rowData[3] = null;
                    rowData[4] = null;
                    rowData[5] = null;
                    rowData[6] = s2b(resultSet.getString("comment"));
                    rowData[7] = s2b(Integer.toString(2));
                    rowData[8] = s2b(functionName);
                }
                procedureRows.add(new ComparableWrapper(getFullyQualifiedName(str, functionName), new ByteArrayRow(rowData, getExceptionInterceptor())));
            } else {
                list = procedureRows;
                i = nameIndex;
            }
        }
        list = procedureRows;
        i = nameIndex;
    }

    protected String getFullyQualifiedName(String catalog, String entity) {
        StringBuilder fullyQualifiedName = new StringBuilder(StringUtils.quoteIdentifier(catalog == null ? "" : catalog, this.quotedId, this.conn.getPedantic()));
        fullyQualifiedName.append('.');
        fullyQualifiedName.append(StringUtils.quoteIdentifier(entity, this.quotedId, this.conn.getPedantic()));
        return fullyQualifiedName.toString();
    }

    protected int getJDBC4FunctionNoTableConstant() {
        return 0;
    }

    protected void convertToJdbcProcedureList(boolean fromSelect, String catalog, ResultSet proceduresRs, boolean needsClientFiltering, String db, List<ComparableWrapper<String, ResultSetRow>> procedureRows, int nameIndex) throws SQLException {
        while (proceduresRs.next()) {
            boolean shouldAdd = true;
            if (needsClientFiltering) {
                shouldAdd = false;
                String procDb = proceduresRs.getString(1);
                if (db == null && procDb == null) {
                    shouldAdd = true;
                } else if (db != null && db.equals(procDb)) {
                    shouldAdd = true;
                }
            }
            if (shouldAdd) {
                procDb = proceduresRs.getString(nameIndex);
                byte[][] rowData = new byte[9][];
                boolean z = false;
                rowData[0] = catalog == null ? null : s2b(catalog);
                rowData[1] = null;
                rowData[2] = s2b(procDb);
                rowData[3] = null;
                rowData[4] = null;
                rowData[5] = null;
                rowData[6] = s2b(proceduresRs.getString("comment"));
                if (fromSelect) {
                    z = "FUNCTION".equalsIgnoreCase(proceduresRs.getString("type"));
                }
                rowData[7] = s2b(z ? Integer.toString(2) : Integer.toString(1));
                rowData[8] = s2b(procDb);
                procedureRows.add(new ComparableWrapper(getFullyQualifiedName(catalog, procDb), new ByteArrayRow(rowData, getExceptionInterceptor())));
            }
        }
    }

    private ResultSetRow convertTypeDescriptorToProcedureRow(byte[] procNameAsBytes, byte[] procCatAsBytes, String paramName, boolean isOutParam, boolean isInParam, boolean isReturnParam, TypeDescriptor typeDesc, boolean forGetFunctionColumns, int ordinal) throws SQLException {
        DatabaseMetaData databaseMetaData = this;
        TypeDescriptor typeDescriptor = typeDesc;
        boolean z = forGetFunctionColumns;
        byte[][] row = z ? new byte[17][] : new byte[20][];
        row[0] = procCatAsBytes;
        row[1] = null;
        row[2] = procNameAsBytes;
        row[3] = s2b(paramName);
        row[4] = s2b(String.valueOf(getColumnType(isOutParam, isInParam, isReturnParam, z)));
        row[5] = s2b(Short.toString(typeDescriptor.dataType));
        row[6] = s2b(typeDescriptor.typeName);
        row[7] = typeDescriptor.columnSize == null ? null : s2b(typeDescriptor.columnSize.toString());
        row[8] = row[7];
        row[9] = typeDescriptor.decimalDigits == null ? null : s2b(typeDescriptor.decimalDigits.toString());
        row[10] = s2b(Integer.toString(typeDescriptor.numPrecRadix));
        switch (typeDescriptor.nullability) {
            case 0:
                row[11] = s2b(String.valueOf(0));
                break;
            case 1:
                row[11] = s2b(String.valueOf(1));
                break;
            case 2:
                row[11] = s2b(String.valueOf(2));
                break;
            default:
                throw SQLError.createSQLException("Internal error while parsing callable statement metadata (unknown nullability value fount)", SQLError.SQL_STATE_GENERAL_ERROR, getExceptionInterceptor());
        }
        row[12] = null;
        if (z) {
            row[13] = null;
            row[14] = s2b(String.valueOf(ordinal));
            row[15] = s2b(typeDescriptor.isNullable);
            row[16] = procNameAsBytes;
        } else {
            row[13] = null;
            row[14] = null;
            row[15] = null;
            row[16] = null;
            row[17] = s2b(String.valueOf(ordinal));
            row[18] = s2b(typeDescriptor.isNullable);
            row[19] = procNameAsBytes;
        }
        return new ByteArrayRow(row, getExceptionInterceptor());
    }

    protected int getColumnType(boolean isOutParam, boolean isInParam, boolean isReturnParam, boolean forGetFunctionColumns) {
        if (isInParam && isOutParam) {
            return 2;
        }
        if (isInParam) {
            return 1;
        }
        if (isOutParam) {
            return 4;
        }
        if (isReturnParam) {
            return 5;
        }
        return 0;
    }

    protected ExceptionInterceptor getExceptionInterceptor() {
        return this.exceptionInterceptor;
    }

    public boolean dataDefinitionCausesTransactionCommit() throws SQLException {
        return true;
    }

    public boolean dataDefinitionIgnoredInTransactions() throws SQLException {
        return false;
    }

    public boolean deletesAreDetected(int type) throws SQLException {
        return false;
    }

    public boolean doesMaxRowSizeIncludeBlobs() throws SQLException {
        return true;
    }

    public List<ResultSetRow> extractForeignKeyForTable(ArrayList<ResultSetRow> rows, ResultSet rs, String catalog) throws SQLException {
        String str;
        ArrayList<ResultSetRow> arrayList = rows;
        ResultSet resultSet = rs;
        row = new byte[3][];
        int i = 0;
        row[0] = resultSet.getBytes(1);
        row[1] = s2b(SUPPORTS_FK);
        StringTokenizer lineTokenizer = new StringTokenizer(resultSet.getString(2), "\n");
        StringBuilder commentBuf = new StringBuilder("comment; ");
        boolean firstTime = true;
        while (lineTokenizer.hasMoreTokens()) {
            int endPos;
            Object obj;
            String line = lineTokenizer.nextToken().trim();
            String constraintName = null;
            if (StringUtils.startsWithIgnoreCase(line, "CONSTRAINT")) {
                boolean usingBackTicks = true;
                int beginPos = StringUtils.indexOfQuoteDoubleAware(line, r0.quotedId, i);
                if (beginPos == -1) {
                    beginPos = line.indexOf("\"");
                    usingBackTicks = false;
                }
                if (beginPos != -1) {
                    if (usingBackTicks) {
                        endPos = StringUtils.indexOfQuoteDoubleAware(line, r0.quotedId, beginPos + 1);
                    } else {
                        endPos = StringUtils.indexOfQuoteDoubleAware(line, "\"", beginPos + 1);
                    }
                    if (endPos != -1) {
                        constraintName = line.substring(beginPos + 1, endPos);
                        line = line.substring(endPos + 1, line.length()).trim();
                    }
                }
            }
            if (line.startsWith("FOREIGN KEY")) {
                int afterFk;
                String str2;
                String localColumnName;
                String localColumnName2;
                String referencedCatalogName;
                String indexOfFK;
                if (line.endsWith(",")) {
                    line = line.substring(0, line.length() - 1);
                }
                endPos = line.indexOf("FOREIGN KEY");
                String referencedCatalogName2 = StringUtils.quoteIdentifier(catalog, r0.quotedId, r0.conn.getPedantic());
                String referencedTableName = null;
                String referencedColumnName = null;
                if (endPos != -1) {
                    afterFk = "FOREIGN KEY".length() + endPos;
                    str2 = r0.quotedId;
                    int indexOfRef = StringUtils.indexOfIgnoreCase(afterFk, line, "REFERENCES", str2, (String) r0.quotedId, StringUtils.SEARCH_MODE__ALL);
                    if (indexOfRef != -1) {
                        endPos = line.indexOf(40, afterFk);
                        localColumnName = null;
                        localColumnName2 = r0.quotedId;
                        referencedCatalogName = referencedCatalogName2;
                        localColumnName2 = StringUtils.indexOfIgnoreCase(endPos, line, ")", localColumnName2, r0.quotedId, StringUtils.SEARCH_MODE__ALL);
                        int indexOfParenOpen;
                        int indexOfParenClose;
                        if (endPos != -1) {
                            referencedCatalogName2 = line.substring(endPos + 1, localColumnName2);
                            indexOfParenOpen = endPos;
                            endPos = "REFERENCES".length() + indexOfRef;
                            str2 = r0.quotedId;
                            indexOfParenClose = localColumnName2;
                            indexOfRef = StringUtils.indexOfIgnoreCase(endPos, line, "(", str2, r0.quotedId, StringUtils.SEARCH_MODE__ALL);
                        } else {
                            referencedCatalogName2 = line.substring(endPos + 1, localColumnName2);
                            indexOfParenOpen = endPos;
                            endPos = "REFERENCES".length() + indexOfRef;
                            str2 = r0.quotedId;
                            indexOfParenClose = localColumnName2;
                            indexOfRef = StringUtils.indexOfIgnoreCase(endPos, line, "(", str2, r0.quotedId, StringUtils.SEARCH_MODE__ALL);
                        }
                        if (indexOfRef != -1) {
                            localColumnName2 = line.substring(endPos, indexOfRef);
                            referencedTableName = r0.quotedId;
                            int afterRef = endPos;
                            endPos = StringUtils.indexOfIgnoreCase(indexOfRef + 1, line, ")", referencedTableName, (String) r0.quotedId, StringUtils.SEARCH_MODE__ALL);
                            if (endPos != -1) {
                                referencedColumnName = line.substring(indexOfRef + 1, endPos);
                            }
                            referencedTableName = r0.quotedId;
                            indexOfRef = StringUtils.indexOfIgnoreCase(0, localColumnName2, ".", referencedTableName, r0.quotedId, StringUtils.SEARCH_MODE__ALL);
                            if (indexOfRef != -1) {
                                String referencedCatalogName3 = localColumnName2.substring(0, indexOfRef);
                                referencedTableName = localColumnName2.substring(indexOfRef + 1);
                                indexOfFK = referencedCatalogName3;
                                localColumnName2 = referencedColumnName;
                            } else {
                                referencedTableName = localColumnName2;
                                localColumnName2 = referencedColumnName;
                                indexOfFK = referencedCatalogName;
                            }
                            if (firstTime) {
                                firstTime = false;
                            } else {
                                commentBuf.append("; ");
                            }
                            if (constraintName != null) {
                                commentBuf.append(constraintName);
                            } else {
                                commentBuf.append("not_available");
                            }
                            commentBuf.append("(");
                            commentBuf.append(referencedCatalogName2);
                            commentBuf.append(") REFER ");
                            commentBuf.append(indexOfFK);
                            commentBuf.append("/");
                            commentBuf.append(referencedTableName);
                            commentBuf.append("(");
                            commentBuf.append(localColumnName2);
                            commentBuf.append(")");
                            afterFk = line.lastIndexOf(")");
                            obj = 1;
                            if (afterFk == line.length() - 1) {
                                str2 = line.substring(afterFk + 1);
                                commentBuf.append(" ");
                                commentBuf.append(str2);
                            }
                        } else {
                            localColumnName2 = null;
                            indexOfFK = referencedCatalogName;
                            if (firstTime) {
                                firstTime = false;
                            } else {
                                commentBuf.append("; ");
                            }
                            if (constraintName != null) {
                                commentBuf.append("not_available");
                            } else {
                                commentBuf.append(constraintName);
                            }
                            commentBuf.append("(");
                            commentBuf.append(referencedCatalogName2);
                            commentBuf.append(") REFER ");
                            commentBuf.append(indexOfFK);
                            commentBuf.append("/");
                            commentBuf.append(referencedTableName);
                            commentBuf.append("(");
                            commentBuf.append(localColumnName2);
                            commentBuf.append(")");
                            afterFk = line.lastIndexOf(")");
                            obj = 1;
                            if (afterFk == line.length() - 1) {
                                str2 = line.substring(afterFk + 1);
                                commentBuf.append(" ");
                                commentBuf.append(str2);
                            }
                        }
                    } else {
                        localColumnName = null;
                        referencedCatalogName = referencedCatalogName2;
                    }
                } else {
                    localColumnName = null;
                    referencedCatalogName = referencedCatalogName2;
                }
                localColumnName2 = null;
                referencedCatalogName2 = localColumnName;
                indexOfFK = referencedCatalogName;
                if (firstTime) {
                    commentBuf.append("; ");
                } else {
                    firstTime = false;
                }
                if (constraintName != null) {
                    commentBuf.append(constraintName);
                } else {
                    commentBuf.append("not_available");
                }
                commentBuf.append("(");
                commentBuf.append(referencedCatalogName2);
                commentBuf.append(") REFER ");
                commentBuf.append(indexOfFK);
                commentBuf.append("/");
                commentBuf.append(referencedTableName);
                commentBuf.append("(");
                commentBuf.append(localColumnName2);
                commentBuf.append(")");
                afterFk = line.lastIndexOf(")");
                obj = 1;
                if (afterFk == line.length() - 1) {
                    str2 = line.substring(afterFk + 1);
                    commentBuf.append(" ");
                    commentBuf.append(str2);
                }
            } else {
                str = catalog;
                obj = 1;
            }
            Object obj2 = obj;
            resultSet = rs;
            i = 0;
        }
        str = catalog;
        row[2] = s2b(commentBuf.toString());
        arrayList.add(new ByteArrayRow(row, getExceptionInterceptor()));
        return arrayList;
    }

    public ResultSet extractForeignKeyFromCreateTable(String catalog, String tableName) throws SQLException {
        DatabaseMetaData databaseMetaData;
        String catalog2;
        String str;
        DatabaseMetaData this;
        Throwable th;
        ResultSet rs;
        Throwable th2;
        DatabaseMetaData databaseMetaData2;
        String str2;
        String tableName2 = tableName;
        ArrayList<String> tableList = new ArrayList();
        ResultSet rs2 = null;
        if (tableName2 != null) {
            tableList.add(tableName2);
            databaseMetaData = this;
            catalog2 = catalog;
        } else {
            try {
                str = catalog;
                try {
                    rs2 = getTables(str, "", "%", new String[]{"TABLE"});
                    while (rs2.next()) {
                        tableList.add(rs2.getString("TABLE_NAME"));
                    }
                    this = databaseMetaData;
                    catalog2 = str;
                    if (rs2 != null) {
                        rs2.close();
                    }
                    rs2 = null;
                } catch (Throwable th3) {
                    th = th3;
                    rs = null;
                    th2 = th;
                    databaseMetaData2 = databaseMetaData;
                    str2 = str;
                    if (rs != null) {
                        rs.close();
                    }
                    throw th2;
                }
            } catch (Throwable th4) {
                th = th4;
                databaseMetaData = this;
                str = catalog;
                rs = null;
                th2 = th;
                databaseMetaData2 = databaseMetaData;
                str2 = str;
                if (rs != null) {
                    rs.close();
                }
                throw th2;
            }
        }
        ArrayList<ResultSetRow> rows = new ArrayList();
        Field[] fields = new Field[]{new Field("", "Name", 1, ActivityChooserViewAdapter.MAX_ACTIVITY_COUNT_UNLIMITED), new Field("", "Type", 1, 255), new Field("", "Comment", 1, ActivityChooserViewAdapter.MAX_ACTIVITY_COUNT_UNLIMITED)};
        int numTables = tableList.size();
        Statement stmt = this.conn.getMetadataSafeStatement();
        int i = 0;
        while (true) {
            int i2 = i;
            if (i2 >= numTables) {
                break;
            }
            String tableToExtract = (String) tableList.get(i2);
            String query = new StringBuilder("SHOW CREATE TABLE ");
            query.append(getFullyQualifiedName(catalog2, tableToExtract));
            query = query.toString();
            try {
                rs2 = stmt.executeQuery(query);
                while (rs2.next()) {
                    extractForeignKeyForTable(rows, rs2, catalog2);
                }
            } catch (SQLException e) {
                sqlEx = e;
                SQLException sqlEx;
                if (SQLError.SQL_STATE_BASE_TABLE_OR_VIEW_NOT_FOUND.equals(sqlEx.getSQLState())) {
                } else {
                    if (sqlEx.getErrorCode() != MysqlErrorNumbers.ER_NO_SUCH_TABLE) {
                        throw sqlEx;
                    }
                }
            } catch (Throwable th5) {
                rs = rs2;
                th2 = th5;
                ResultSet rs3;
                if (rs3 != null) {
                    rs3.close();
                }
                if (stmt != null) {
                    stmt.close();
                }
            }
            i = i2 + 1;
        }
        databaseMetaData2 = this;
        ArrayList<ResultSetRow> rows2 = rows;
        Field[] fields2 = fields;
        if (rs2 != null) {
            rs2.close();
        }
        if (stmt != null) {
            stmt.close();
        }
        return buildResultSet(fields2, rows2);
    }

    public ResultSet getAttributes(String arg0, String arg1, String arg2, String arg3) throws SQLException {
        return buildResultSet(new Field[]{new Field("", "TYPE_CAT", 1, 32), new Field("", "TYPE_SCHEM", 1, 32), new Field("", "TYPE_NAME", 1, 32), new Field("", "ATTR_NAME", 1, 32), new Field("", "DATA_TYPE", 5, 32), new Field("", "ATTR_TYPE_NAME", 1, 32), new Field("", "ATTR_SIZE", 4, 32), new Field("", "DECIMAL_DIGITS", 4, 32), new Field("", "NUM_PREC_RADIX", 4, 32), new Field("", "NULLABLE ", 4, 32), new Field("", "REMARKS", 1, 32), new Field("", "ATTR_DEF", 1, 32), new Field("", "SQL_DATA_TYPE", 4, 32), new Field("", "SQL_DATETIME_SUB", 4, 32), new Field("", "CHAR_OCTET_LENGTH", 4, 32), new Field("", "ORDINAL_POSITION", 4, 32), new Field("", "IS_NULLABLE", 1, 32), new Field("", "SCOPE_CATALOG", 1, 32), new Field("", "SCOPE_SCHEMA", 1, 32), new Field("", "SCOPE_TABLE", 1, 32), new Field("", "SOURCE_DATA_TYPE", 5, 32)}, new ArrayList());
    }

    public ResultSet getBestRowIdentifier(String catalog, String schema, String table, int scope, boolean nullable) throws SQLException {
        if (table == null) {
            throw SQLError.createSQLException("Table not specified.", SQLError.SQL_STATE_ILLEGAL_ARGUMENT, getExceptionInterceptor());
        }
        Field[] fields = new Field[]{new Field("", "SCOPE", 5, 5), new Field("", "COLUMN_NAME", 1, 32), new Field("", "DATA_TYPE", 4, 32), new Field("", "TYPE_NAME", 1, 32), new Field("", "COLUMN_SIZE", 4, 10), new Field("", "BUFFER_LENGTH", 4, 10), new Field("", "DECIMAL_DIGITS", 5, 10), new Field("", "PSEUDO_COLUMN", 5, 5)};
        ArrayList<ResultSetRow> rows = new ArrayList();
        Statement stmt = this.conn.getMetadataSafeStatement();
        try {
            final String str = table;
            final Statement statement = stmt;
            final ArrayList<ResultSetRow> arrayList = rows;
            new IterateBlock<String>(getCatalogIterator(catalog)) {
                void forEach(String catalogStr) throws SQLException {
                    ResultSet results = null;
                    try {
                        StringBuilder queryBuf = new StringBuilder("SHOW COLUMNS FROM ");
                        queryBuf.append(StringUtils.quoteIdentifier(str, DatabaseMetaData.this.quotedId, DatabaseMetaData.this.conn.getPedantic()));
                        queryBuf.append(" FROM ");
                        queryBuf.append(StringUtils.quoteIdentifier(catalogStr, DatabaseMetaData.this.quotedId, DatabaseMetaData.this.conn.getPedantic()));
                        results = statement.executeQuery(queryBuf.toString());
                        while (results.next()) {
                            String keyType = results.getString("Key");
                            if (keyType != null && StringUtils.startsWithIgnoreCase(keyType, "PRI")) {
                                byte[][] rowVal = new byte[8][];
                                int maxLength = 0;
                                rowVal[0] = Integer.toString(2).getBytes();
                                rowVal[1] = results.getBytes("Field");
                                String type = results.getString("Type");
                                int size = MysqlIO.getMaxBuf();
                                int decimals = 0;
                                if (type.indexOf("enum") != -1) {
                                    StringTokenizer tokenizer = new StringTokenizer(type.substring(type.indexOf("("), type.indexOf(")")), ",");
                                    while (tokenizer.hasMoreTokens()) {
                                        maxLength = Math.max(maxLength, tokenizer.nextToken().length() - 2);
                                    }
                                    size = maxLength;
                                    decimals = 0;
                                    type = "enum";
                                } else if (type.indexOf("(") != -1) {
                                    if (type.indexOf(",") != -1) {
                                        size = Integer.parseInt(type.substring(type.indexOf("(") + 1, type.indexOf(",")));
                                        decimals = Integer.parseInt(type.substring(type.indexOf(",") + 1, type.indexOf(")")));
                                    } else {
                                        size = Integer.parseInt(type.substring(type.indexOf("(") + 1, type.indexOf(")")));
                                    }
                                    type = type.substring(0, type.indexOf("("));
                                }
                                rowVal[2] = DatabaseMetaData.this.s2b(String.valueOf(MysqlDefs.mysqlToJavaType(type)));
                                rowVal[3] = DatabaseMetaData.this.s2b(type);
                                rowVal[4] = Integer.toString(size + decimals).getBytes();
                                rowVal[5] = Integer.toString(size + decimals).getBytes();
                                rowVal[6] = Integer.toString(decimals).getBytes();
                                rowVal[7] = Integer.toString(1).getBytes();
                                arrayList.add(new ByteArrayRow(rowVal, DatabaseMetaData.this.getExceptionInterceptor()));
                            }
                        }
                        if (results != null) {
                            try {
                                results.close();
                            } catch (Exception e) {
                            }
                        }
                    } catch (SQLException sqlEx) {
                        if (SQLError.SQL_STATE_BASE_TABLE_OR_VIEW_NOT_FOUND.equals(sqlEx.getSQLState())) {
                            if (results != null) {
                                try {
                                    results.close();
                                } catch (Exception e2) {
                                }
                            }
                        } else {
                            throw sqlEx;
                        }
                    } catch (Throwable th) {
                        if (results != null) {
                            try {
                                results.close();
                            } catch (Exception e3) {
                            }
                        }
                    }
                }
            }.doForAll();
            if (stmt != null) {
                stmt.close();
            }
            return buildResultSet(fields, rows);
        } catch (Throwable th) {
            Statement stmt2 = stmt;
            if (stmt2 != null) {
                stmt2.close();
            }
        }
    }

    private void getCallStmtParameterTypes(String catalog, String quotedProcName, ProcedureType procType, String parameterNamePattern, List<ResultSetRow> resultRows, boolean forGetFunctionColumns) throws SQLException {
        String parameterNamePattern2;
        ResultSet resultSet;
        Throwable th;
        ResultSet rs;
        List<ResultSetRow> resultRows2;
        boolean forGetFunctionColumns2;
        String catalog2;
        String parameterNamePattern3;
        DatabaseMetaData databaseMetaData;
        Statement paramRetrievalStmt;
        List<ResultSetRow> list;
        Throwable th2;
        SQLException sqlExRethrow;
        String catalog3;
        String catalog4;
        Statement paramRetrievalStmt2;
        byte[] procCatAsBytes;
        ResultSet resultSet2;
        DatabaseMetaData databaseMetaData2 = this;
        String str = catalog;
        String quotedProcName2 = quotedProcName;
        ProcedureType procType2 = procType;
        ResultSet paramRetrievalRs = null;
        if (parameterNamePattern != null) {
            parameterNamePattern2 = parameterNamePattern;
        } else if (databaseMetaData2.conn.getNullNamePatternMatchesAll()) {
            parameterNamePattern2 = "%";
        } else {
            throw SQLError.createSQLException("Parameter/Column name pattern can not be NULL or empty.", SQLError.SQL_STATE_ILLEGAL_ARGUMENT, getExceptionInterceptor());
        }
        String parameterDef = null;
        boolean isProcedureInAnsiMode = false;
        String storageDefnDelims = null;
        String storageDefnClosures = null;
        byte[] procNameAsBytes;
        byte[] procCatAsBytes2;
        String catalog5;
        String parameterNamePattern4;
        Statement paramRetrievalStmt3;
        String parameterDef2;
        try {
            String oldCatalog;
            DatabaseMetaData this;
            int dotIndex;
            int dotIndex2;
            byte[] procNameAsBytes2;
            String tmpProcName;
            byte[] procCatAsBytes3;
            StringBuilder procNameBuf;
            StringBuilder stringBuilder;
            StringBuilder stringBuilder2;
            String fieldName;
            ResultSet paramRetrievalRs2;
            String procedureDef;
            String fieldName2;
            StringBuilder procNameBuf2;
            String dbName;
            String identifierMarkers;
            StringBuilder stringBuilder3;
            String identifierAndStringMarkers;
            String identifierMarkers2;
            int openParenIndex;
            int endOfParamDeclarationIndex;
            int openParenIndex2;
            int declarationStart;
            int returnsIndex;
            int openParenIndex3;
            int endOfParamDeclarationIndex2;
            String procedureDef2;
            int i;
            int i2;
            String str2;
            StringBuilder paramRetrievalStmt4;
            String str3;
            String str4;
            int endOfParamDeclarationIndex3;
            String paramRetrievalStmt5;
            boolean forGetFunctionColumns3;
            byte[] procNameAsBytes3;
            byte[] procCatAsBytes4;
            SQLException sqlExRethrow2;
            int ordinal;
            String quotedProcName3;
            int parseListLen;
            ResultSet procType3;
            String str5;
            ResultSet resultSet3;
            byte[] bArr;
            String declaration;
            ResultSet resultSet4;
            String str6;
            boolean isOutParam;
            boolean isOutParam2;
            String parameterDef3;
            boolean isInParam;
            boolean isOutParam3;
            boolean isInParam2;
            boolean isOutParam4;
            boolean isInParam3;
            String possibleParamName;
            TypeDescriptor typeDesc;
            String typeInfoBuf;
            byte[] bArr2;
            Statement paramRetrievalStmt6 = databaseMetaData2.conn.getMetadataSafeStatement();
            try {
                oldCatalog = databaseMetaData2.conn.getCatalog();
                if (databaseMetaData2.conn.lowerCaseTableNames() && str != null) {
                    try {
                        if (!(catalog.length() == 0 || oldCatalog == null || oldCatalog.length() == 0)) {
                            ResultSet rs2;
                            try {
                                resultSet = null;
                                try {
                                    databaseMetaData2.conn.setCatalog(StringUtils.unQuoteIdentifier(str, databaseMetaData2.quotedId));
                                    rs2 = paramRetrievalStmt6.executeQuery("SELECT DATABASE()");
                                    try {
                                        rs2.next();
                                        str = rs2.getString(1);
                                        databaseMetaData2.conn.setCatalog(oldCatalog);
                                        if (rs2 != null) {
                                            rs2.close();
                                        }
                                    } catch (Throwable th3) {
                                        th = th3;
                                        resultSet = rs2;
                                        rs2 = th;
                                        rs = resultSet;
                                        resultRows2 = resultRows;
                                        forGetFunctionColumns2 = forGetFunctionColumns;
                                        catalog2 = str;
                                        parameterNamePattern3 = parameterNamePattern2;
                                        try {
                                            databaseMetaData2.conn.setCatalog(oldCatalog);
                                            if (rs != null) {
                                                rs.close();
                                            }
                                            throw rs2;
                                        } catch (Throwable th4) {
                                            databaseMetaData = this;
                                            paramRetrievalStmt = paramRetrievalStmt6;
                                            procNameAsBytes = null;
                                            procCatAsBytes2 = null;
                                            list = resultRows2;
                                            catalog5 = catalog2;
                                            parameterNamePattern4 = parameterNamePattern3;
                                            th2 = th4;
                                            parameterNamePattern3 = quotedProcName2;
                                            paramRetrievalStmt3 = paramRetrievalStmt;
                                            parameterDef2 = null;
                                            sqlExRethrow = null;
                                            if (paramRetrievalRs != null) {
                                                try {
                                                    paramRetrievalRs.close();
                                                } catch (SQLException e) {
                                                    sqlExRethrow = e;
                                                }
                                            }
                                            if (paramRetrievalStmt3 != null) {
                                                try {
                                                    paramRetrievalStmt3.close();
                                                } catch (SQLException e2) {
                                                    sqlExRethrow = e2;
                                                }
                                            }
                                            if (sqlExRethrow != null) {
                                                throw sqlExRethrow;
                                            }
                                            throw th2;
                                        }
                                    }
                                } catch (Throwable th5) {
                                    th4 = th5;
                                    rs2 = th4;
                                    rs = resultSet;
                                    resultRows2 = resultRows;
                                    forGetFunctionColumns2 = forGetFunctionColumns;
                                    catalog2 = str;
                                    parameterNamePattern3 = parameterNamePattern2;
                                    databaseMetaData2.conn.setCatalog(oldCatalog);
                                    if (rs != null) {
                                        rs.close();
                                    }
                                    throw rs2;
                                }
                            } catch (Throwable th42) {
                                resultSet = null;
                                rs2 = th42;
                                rs = resultSet;
                                resultRows2 = resultRows;
                                forGetFunctionColumns2 = forGetFunctionColumns;
                                catalog2 = str;
                                parameterNamePattern3 = parameterNamePattern2;
                                databaseMetaData2.conn.setCatalog(oldCatalog);
                                if (rs != null) {
                                    rs.close();
                                }
                                throw rs2;
                            }
                        }
                    } catch (Throwable th422) {
                        list = resultRows;
                        forGetFunctionColumns2 = forGetFunctionColumns;
                        catalog5 = str;
                        parameterNamePattern4 = parameterNamePattern2;
                        paramRetrievalStmt = paramRetrievalStmt6;
                        parameterNamePattern3 = quotedProcName2;
                        procNameAsBytes = null;
                        procCatAsBytes2 = null;
                        databaseMetaData = databaseMetaData2;
                        th2 = th422;
                        paramRetrievalStmt3 = paramRetrievalStmt;
                        parameterDef2 = null;
                        sqlExRethrow = null;
                        if (paramRetrievalRs != null) {
                            paramRetrievalRs.close();
                        }
                        if (paramRetrievalStmt3 != null) {
                            paramRetrievalStmt3.close();
                        }
                        if (sqlExRethrow != null) {
                            throw sqlExRethrow;
                        }
                        throw th2;
                    }
                }
                catalog3 = str;
                parameterNamePattern4 = parameterNamePattern2;
            } catch (Throwable th4222) {
                list = resultRows;
                paramRetrievalStmt = paramRetrievalStmt6;
                forGetFunctionColumns2 = forGetFunctionColumns;
                catalog5 = str;
                parameterNamePattern4 = parameterNamePattern2;
                parameterNamePattern3 = quotedProcName2;
                procNameAsBytes = null;
                databaseMetaData = databaseMetaData2;
                procCatAsBytes2 = null;
                th2 = th4222;
                paramRetrievalStmt3 = paramRetrievalStmt;
                parameterDef2 = null;
                sqlExRethrow = null;
                if (paramRetrievalRs != null) {
                    paramRetrievalRs.close();
                }
                if (paramRetrievalStmt3 != null) {
                    paramRetrievalStmt3.close();
                }
                if (sqlExRethrow != null) {
                    throw th2;
                }
                throw sqlExRethrow;
            }
            try {
                int i3;
                if (paramRetrievalStmt6.getMaxRows() != 0) {
                    try {
                        paramRetrievalStmt6.setMaxRows(0);
                    } catch (Throwable th42222) {
                        forGetFunctionColumns2 = forGetFunctionColumns;
                        th2 = th42222;
                        catalog5 = catalog3;
                        paramRetrievalStmt = paramRetrievalStmt6;
                        parameterNamePattern3 = quotedProcName2;
                        procNameAsBytes = null;
                        procCatAsBytes2 = null;
                        databaseMetaData = databaseMetaData2;
                        list = resultRows;
                        paramRetrievalStmt3 = paramRetrievalStmt;
                        parameterDef2 = null;
                        sqlExRethrow = null;
                        if (paramRetrievalRs != null) {
                            paramRetrievalRs.close();
                        }
                        if (paramRetrievalStmt3 != null) {
                            paramRetrievalStmt3.close();
                        }
                        if (sqlExRethrow != null) {
                            throw sqlExRethrow;
                        }
                        throw th2;
                    }
                }
                if (" ".equals(databaseMetaData2.quotedId)) {
                    catalog4 = catalog3;
                    String str7 = oldCatalog;
                    paramRetrievalStmt2 = paramRetrievalStmt6;
                    procCatAsBytes = null;
                    i3 = 0;
                    try {
                        dotIndex = quotedProcName2.indexOf(".");
                    } catch (Throwable th422222) {
                        list = resultRows;
                        catalog5 = catalog4;
                        paramRetrievalStmt = paramRetrievalStmt2;
                        forGetFunctionColumns2 = forGetFunctionColumns;
                        th2 = th422222;
                        parameterNamePattern3 = quotedProcName2;
                        procNameAsBytes = null;
                        databaseMetaData = databaseMetaData2;
                        procCatAsBytes2 = procCatAsBytes;
                        paramRetrievalStmt3 = paramRetrievalStmt;
                        parameterDef2 = null;
                        sqlExRethrow = null;
                        if (paramRetrievalRs != null) {
                            paramRetrievalRs.close();
                        }
                        if (paramRetrievalStmt3 != null) {
                            paramRetrievalStmt3.close();
                        }
                        if (sqlExRethrow != null) {
                            throw sqlExRethrow;
                        }
                        throw th2;
                    }
                }
                try {
                    procCatAsBytes = null;
                    i3 = 0;
                    catalog4 = catalog3;
                    paramRetrievalStmt2 = paramRetrievalStmt6;
                } catch (Throwable th4222222) {
                    catalog4 = catalog3;
                    list = resultRows;
                    forGetFunctionColumns2 = forGetFunctionColumns;
                    th2 = th4222222;
                    parameterNamePattern3 = quotedProcName2;
                    procNameAsBytes = null;
                    databaseMetaData = databaseMetaData2;
                    procCatAsBytes2 = null;
                    catalog5 = catalog4;
                    paramRetrievalStmt = paramRetrievalStmt6;
                    paramRetrievalStmt3 = paramRetrievalStmt;
                    parameterDef2 = null;
                    sqlExRethrow = null;
                    if (paramRetrievalRs != null) {
                        paramRetrievalRs.close();
                    }
                    if (paramRetrievalStmt3 != null) {
                        paramRetrievalStmt3.close();
                    }
                    if (sqlExRethrow != null) {
                        throw sqlExRethrow;
                    }
                    throw th2;
                }
                try {
                    dotIndex = StringUtils.indexOfIgnoreCase(0, quotedProcName2, ".", databaseMetaData2.quotedId, databaseMetaData2.quotedId, databaseMetaData2.conn.isNoBackslashEscapesSet() ? StringUtils.SEARCH_MODE__MRK_COM_WS : StringUtils.SEARCH_MODE__ALL);
                } catch (Throwable th42222222) {
                    list = resultRows;
                    forGetFunctionColumns2 = forGetFunctionColumns;
                    th2 = th42222222;
                    parameterNamePattern3 = quotedProcName2;
                    procNameAsBytes = null;
                    databaseMetaData = databaseMetaData2;
                    procCatAsBytes2 = procCatAsBytes;
                    catalog5 = catalog4;
                    paramRetrievalStmt = paramRetrievalStmt2;
                    paramRetrievalStmt3 = paramRetrievalStmt;
                    parameterDef2 = null;
                    sqlExRethrow = null;
                    if (paramRetrievalRs != null) {
                        paramRetrievalRs.close();
                    }
                    if (paramRetrievalStmt3 != null) {
                        paramRetrievalStmt3.close();
                    }
                    if (sqlExRethrow != null) {
                        throw sqlExRethrow;
                    }
                    throw th2;
                }
                dotIndex2 = dotIndex;
                if (dotIndex2 != -1) {
                    if (dotIndex2 + 1 < quotedProcName.length()) {
                        parameterNamePattern2 = quotedProcName2.substring(i3, dotIndex2);
                        quotedProcName2 = quotedProcName2.substring(dotIndex2 + 1);
                        catalog3 = catalog4;
                        try {
                            str = StringUtils.unQuoteIdentifier(quotedProcName2, databaseMetaData2.quotedId);
                            procNameAsBytes2 = StringUtils.getBytes(str, "UTF-8");
                        } catch (UnsupportedEncodingException e3) {
                            UnsupportedEncodingException ueEx = e3;
                            procNameAsBytes2 = s2b(str);
                        } catch (Throwable th422222222) {
                            catalog5 = catalog3;
                            parameterNamePattern3 = quotedProcName2;
                            paramRetrievalStmt = paramRetrievalStmt2;
                            list = resultRows;
                            forGetFunctionColumns2 = forGetFunctionColumns;
                            th2 = th422222222;
                            procNameAsBytes = null;
                            databaseMetaData = databaseMetaData2;
                            procCatAsBytes2 = procCatAsBytes;
                            paramRetrievalStmt3 = paramRetrievalStmt;
                            parameterDef2 = null;
                            sqlExRethrow = null;
                            if (paramRetrievalRs != null) {
                                paramRetrievalRs.close();
                            }
                            if (paramRetrievalStmt3 != null) {
                                paramRetrievalStmt3.close();
                            }
                            if (sqlExRethrow != null) {
                                throw sqlExRethrow;
                            }
                            throw th2;
                        }
                        procNameAsBytes = procNameAsBytes2;
                        try {
                            tmpProcName = StringUtils.unQuoteIdentifier(parameterNamePattern2, databaseMetaData2.quotedId);
                            procCatAsBytes3 = StringUtils.getBytes(tmpProcName, "UTF-8");
                        } catch (UnsupportedEncodingException e32) {
                            UnsupportedEncodingException ueEx2 = e32;
                            procCatAsBytes3 = s2b(tmpProcName);
                        } catch (Throwable th4222222222) {
                            catalog5 = catalog3;
                            parameterNamePattern3 = quotedProcName2;
                            paramRetrievalStmt = paramRetrievalStmt2;
                            list = resultRows;
                            forGetFunctionColumns2 = forGetFunctionColumns;
                            th2 = th4222222222;
                            databaseMetaData = databaseMetaData2;
                            procCatAsBytes2 = procCatAsBytes;
                            paramRetrievalStmt3 = paramRetrievalStmt;
                            parameterDef2 = null;
                            sqlExRethrow = null;
                            if (paramRetrievalRs != null) {
                                paramRetrievalRs.close();
                            }
                            if (paramRetrievalStmt3 != null) {
                                paramRetrievalStmt3.close();
                            }
                            if (sqlExRethrow != null) {
                                throw th2;
                            }
                            throw sqlExRethrow;
                        }
                        procCatAsBytes2 = procCatAsBytes3;
                        try {
                            procNameBuf = new StringBuilder();
                            procNameBuf.append(parameterNamePattern2);
                            procNameBuf.append('.');
                            procNameBuf.append(quotedProcName2);
                            if (procType2 != ProcedureType.PROCEDURE) {
                                try {
                                    stringBuilder = new StringBuilder();
                                    stringBuilder.append("SHOW CREATE PROCEDURE ");
                                    stringBuilder.append(procNameBuf.toString());
                                    oldCatalog = stringBuilder.toString();
                                    paramRetrievalStmt3 = paramRetrievalStmt2;
                                } catch (Throwable th42222222222) {
                                    forGetFunctionColumns2 = forGetFunctionColumns;
                                    th2 = th42222222222;
                                    catalog5 = catalog3;
                                    parameterNamePattern3 = quotedProcName2;
                                    paramRetrievalStmt = paramRetrievalStmt2;
                                    databaseMetaData = databaseMetaData2;
                                    list = resultRows;
                                    paramRetrievalStmt3 = paramRetrievalStmt;
                                    parameterDef2 = null;
                                    sqlExRethrow = null;
                                    if (paramRetrievalRs != null) {
                                        paramRetrievalRs.close();
                                    }
                                    if (paramRetrievalStmt3 != null) {
                                        paramRetrievalStmt3.close();
                                    }
                                    if (sqlExRethrow != null) {
                                        throw sqlExRethrow;
                                    }
                                    throw th2;
                                }
                                try {
                                    paramRetrievalRs = paramRetrievalStmt3.executeQuery(oldCatalog);
                                    str = "Create Procedure";
                                } catch (Throwable th422222222222) {
                                    forGetFunctionColumns2 = forGetFunctionColumns;
                                    th2 = th422222222222;
                                    catalog5 = catalog3;
                                    parameterNamePattern3 = quotedProcName2;
                                    paramRetrievalStmt = paramRetrievalStmt3;
                                    databaseMetaData = databaseMetaData2;
                                    list = resultRows;
                                    paramRetrievalStmt3 = paramRetrievalStmt;
                                    parameterDef2 = null;
                                    sqlExRethrow = null;
                                    if (paramRetrievalRs != null) {
                                        paramRetrievalRs.close();
                                    }
                                    if (paramRetrievalStmt3 != null) {
                                        paramRetrievalStmt3.close();
                                    }
                                    if (sqlExRethrow != null) {
                                        throw sqlExRethrow;
                                    }
                                    throw th2;
                                }
                            }
                            paramRetrievalStmt3 = paramRetrievalStmt2;
                            try {
                                stringBuilder2 = new StringBuilder();
                                fieldName = null;
                                stringBuilder2.append("SHOW CREATE FUNCTION ");
                                stringBuilder2.append(procNameBuf.toString());
                                paramRetrievalRs = paramRetrievalStmt3.executeQuery(stringBuilder2.toString());
                                str = "Create Function";
                            } catch (Throwable th4222222222222) {
                                catalog5 = catalog3;
                                parameterNamePattern3 = quotedProcName2;
                                paramRetrievalStmt = paramRetrievalStmt3;
                                list = resultRows;
                                forGetFunctionColumns2 = forGetFunctionColumns;
                                th2 = th4222222222222;
                                databaseMetaData = databaseMetaData2;
                                paramRetrievalStmt3 = paramRetrievalStmt;
                                parameterDef2 = null;
                                sqlExRethrow = null;
                                if (paramRetrievalRs != null) {
                                    paramRetrievalRs.close();
                                }
                                if (paramRetrievalStmt3 != null) {
                                    paramRetrievalStmt3.close();
                                }
                                if (sqlExRethrow != null) {
                                    throw th2;
                                }
                                throw sqlExRethrow;
                            }
                            paramRetrievalRs2 = paramRetrievalRs;
                        } catch (Throwable th42222222222222) {
                            catalog5 = catalog3;
                            parameterNamePattern3 = quotedProcName2;
                            paramRetrievalStmt = paramRetrievalStmt2;
                            list = resultRows;
                            forGetFunctionColumns2 = forGetFunctionColumns;
                            th2 = th42222222222222;
                            databaseMetaData = databaseMetaData2;
                            paramRetrievalStmt3 = paramRetrievalStmt;
                            parameterDef2 = null;
                            sqlExRethrow = null;
                            if (paramRetrievalRs != null) {
                                paramRetrievalRs.close();
                            }
                            if (paramRetrievalStmt3 != null) {
                                paramRetrievalStmt3.close();
                            }
                            if (sqlExRethrow != null) {
                                throw th2;
                            }
                            throw sqlExRethrow;
                        }
                        try {
                            if (paramRetrievalRs2.next()) {
                                procedureDef = paramRetrievalRs2.getString(str);
                                fieldName2 = str;
                                if (databaseMetaData2.conn.getNoAccessToProcedureBodies()) {
                                    if (procedureDef != null) {
                                        try {
                                            if (procedureDef.length() == 0) {
                                                procNameBuf2 = procNameBuf;
                                                dbName = parameterNamePattern2;
                                            }
                                        } catch (Throwable th422222222222222) {
                                            forGetFunctionColumns2 = forGetFunctionColumns;
                                            th2 = th422222222222222;
                                            catalog5 = catalog3;
                                            paramRetrievalRs = paramRetrievalRs2;
                                        }
                                    }
                                    throw SQLError.createSQLException("User does not have access to metadata required to determine stored procedure parameter types. If rights can not be granted, configure connection with \"noAccessToProcedureBodies=true\" to have driver generate parameters that represent INOUT strings irregardless of actual parameter types.", SQLError.SQL_STATE_GENERAL_ERROR, getExceptionInterceptor());
                                }
                                procNameBuf2 = procNameBuf;
                                dbName = parameterNamePattern2;
                                try {
                                    if (StringUtils.indexOfIgnoreCase(paramRetrievalRs2.getString("sql_mode"), "ANSI") != -1) {
                                        isProcedureInAnsiMode = true;
                                    }
                                } catch (SQLException e4) {
                                }
                                identifierMarkers = isProcedureInAnsiMode ? "`\"" : "`";
                                stringBuilder3 = new StringBuilder();
                                stringBuilder3.append("'");
                                stringBuilder3.append(identifierMarkers);
                                identifierAndStringMarkers = stringBuilder3.toString();
                                stringBuilder3 = new StringBuilder();
                                stringBuilder3.append("(");
                                stringBuilder3.append(identifierMarkers);
                                storageDefnDelims = stringBuilder3.toString();
                                stringBuilder3 = new StringBuilder();
                                stringBuilder3.append(")");
                                stringBuilder3.append(identifierMarkers);
                                storageDefnClosures = stringBuilder3.toString();
                                if (!(procedureDef == null || procedureDef.length() == 0)) {
                                    parameterNamePattern2 = StringUtils.stripComments(procedureDef, identifierAndStringMarkers, identifierAndStringMarkers, true, false, true, true);
                                    identifierMarkers2 = identifierMarkers;
                                    openParenIndex = StringUtils.indexOfIgnoreCase(0, parameterNamePattern2, "(", databaseMetaData2.quotedId, databaseMetaData2.quotedId, databaseMetaData2.conn.isNoBackslashEscapesSet() ? StringUtils.SEARCH_MODE__MRK_COM_WS : StringUtils.SEARCH_MODE__ALL);
                                    dotIndex = endPositionOfParameterDeclaration(openParenIndex, parameterNamePattern2, databaseMetaData2.quotedId);
                                    if (procType2 != ProcedureType.FUNCTION) {
                                        endOfParamDeclarationIndex = dotIndex;
                                        openParenIndex2 = openParenIndex;
                                        openParenIndex = StringUtils.indexOfIgnoreCase(0, parameterNamePattern2, " RETURNS ", databaseMetaData2.quotedId, (String) databaseMetaData2.quotedId, databaseMetaData2.conn.isNoBackslashEscapesSet() ? StringUtils.SEARCH_MODE__MRK_COM_WS : StringUtils.SEARCH_MODE__ALL);
                                        dotIndex = findEndOfReturnsClause(parameterNamePattern2, openParenIndex);
                                        declarationStart = "RETURNS ".length() + openParenIndex;
                                        while (true) {
                                            returnsIndex = openParenIndex;
                                            if (declarationStart < parameterNamePattern2.length()) {
                                                if (Character.isWhitespace(parameterNamePattern2.charAt(declarationStart))) {
                                                    declarationStart++;
                                                    openParenIndex = returnsIndex;
                                                }
                                            }
                                            break;
                                        }
                                        identifierMarkers = parameterNamePattern2.substring(declarationStart, dotIndex).trim();
                                        catalog4 = dotIndex;
                                        openParenIndex3 = openParenIndex2;
                                        endOfParamDeclarationIndex2 = endOfParamDeclarationIndex;
                                        procedureDef2 = parameterNamePattern2;
                                        catalog5 = catalog3;
                                        resultSet2 = paramRetrievalRs2;
                                        parameterNamePattern3 = quotedProcName2;
                                        paramRetrievalStmt = paramRetrievalStmt3;
                                        i = 0;
                                        try {
                                            catalog3 = resultRows;
                                            catalog3.add(convertTypeDescriptorToProcedureRow(procNameAsBytes, procCatAsBytes2, "", false, false, true, new TypeDescriptor(databaseMetaData2, identifierMarkers, "YES"), forGetFunctionColumns, null));
                                        } catch (Throwable th6) {
                                            th422222222222222 = th6;
                                            forGetFunctionColumns2 = forGetFunctionColumns;
                                            th2 = th422222222222222;
                                            databaseMetaData = databaseMetaData2;
                                            paramRetrievalRs = resultSet2;
                                            paramRetrievalStmt3 = paramRetrievalStmt;
                                            parameterDef2 = null;
                                            sqlExRethrow = null;
                                            if (paramRetrievalRs != null) {
                                                paramRetrievalRs.close();
                                            }
                                            if (paramRetrievalStmt3 != null) {
                                                paramRetrievalStmt3.close();
                                            }
                                            if (sqlExRethrow != null) {
                                                throw th2;
                                            }
                                            throw sqlExRethrow;
                                        }
                                    }
                                    endOfParamDeclarationIndex2 = dotIndex;
                                    openParenIndex3 = openParenIndex;
                                    procedureDef2 = parameterNamePattern2;
                                    catalog5 = catalog3;
                                    resultSet2 = paramRetrievalRs2;
                                    i2 = dotIndex2;
                                    parameterNamePattern3 = quotedProcName2;
                                    str2 = tmpProcName;
                                    paramRetrievalStmt = paramRetrievalStmt3;
                                    procCatAsBytes = fieldName2;
                                    paramRetrievalStmt4 = procNameBuf2;
                                    str3 = dbName;
                                    str4 = identifierMarkers2;
                                    i = 0;
                                    catalog3 = resultRows;
                                    dotIndex = openParenIndex3;
                                    if (dotIndex == -1) {
                                        endOfParamDeclarationIndex3 = endOfParamDeclarationIndex2;
                                        if (endOfParamDeclarationIndex3 != -1) {
                                            oldCatalog = procedureDef2;
                                        } else {
                                            parameterDef = procedureDef2.substring(dotIndex + 1, endOfParamDeclarationIndex3);
                                            this = databaseMetaData2;
                                            identifierMarkers = catalog5;
                                            parameterNamePattern2 = parameterNamePattern3;
                                            paramRetrievalRs2 = procType2;
                                            paramRetrievalStmt5 = parameterNamePattern4;
                                            forGetFunctionColumns3 = forGetFunctionColumns;
                                            quotedProcName2 = paramRetrievalStmt;
                                            paramRetrievalRs = resultSet2;
                                            procNameAsBytes3 = parameterDef;
                                            procCatAsBytes4 = isProcedureInAnsiMode;
                                            parameterDef2 = storageDefnDelims;
                                            parameterDef = storageDefnClosures;
                                            sqlExRethrow2 = null;
                                            if (paramRetrievalRs != null) {
                                                try {
                                                    paramRetrievalRs.close();
                                                } catch (SQLException e22) {
                                                    sqlExRethrow2 = e22;
                                                }
                                                paramRetrievalRs = null;
                                            }
                                            if (quotedProcName2 != null) {
                                                try {
                                                    quotedProcName2.close();
                                                } catch (SQLException e222) {
                                                    sqlExRethrow2 = e222;
                                                }
                                                quotedProcName2 = null;
                                            }
                                            if (sqlExRethrow2 == null) {
                                                throw sqlExRethrow2;
                                            } else if (procNameAsBytes3 == null) {
                                                ordinal = 1;
                                                storageDefnDelims = StringUtils.split(procNameAsBytes3, ",", parameterDef2, parameterDef, true);
                                                identifierMarkers = storageDefnDelims.size();
                                                while (true) {
                                                    quotedProcName3 = parameterNamePattern2;
                                                    parameterNamePattern2 = i;
                                                    if (parameterNamePattern2 >= identifierMarkers) {
                                                        parseListLen = identifierMarkers;
                                                        identifierMarkers = (String) storageDefnDelims.get(parameterNamePattern2);
                                                        procType3 = paramRetrievalRs2;
                                                        if (identifierMarkers.trim().length() != null) {
                                                            str5 = quotedProcName2;
                                                            resultSet3 = paramRetrievalRs;
                                                            bArr = procNameAsBytes3;
                                                            return;
                                                        }
                                                        str5 = quotedProcName2;
                                                        identifierMarkers = identifierMarkers.replaceAll("[\\t\\n\\x0B\\f\\r]", " ");
                                                        paramRetrievalRs2 = new StringTokenizer(identifierMarkers, " \t");
                                                        parameterNamePattern4 = null;
                                                        if (paramRetrievalRs2.hasMoreTokens()) {
                                                            declaration = identifierMarkers;
                                                            resultSet4 = paramRetrievalRs2;
                                                            str6 = null;
                                                            resultSet3 = paramRetrievalRs;
                                                            bArr = procNameAsBytes3;
                                                            throw SQLError.createSQLException("Internal error when parsing callable statement metadata (unknown output from 'SHOW CREATE PROCEDURE')", SQLError.SQL_STATE_GENERAL_ERROR, getExceptionInterceptor());
                                                        }
                                                        declaration = identifierMarkers;
                                                        identifierMarkers = paramRetrievalRs2.nextToken();
                                                        str6 = null;
                                                        if (identifierMarkers.equalsIgnoreCase("OUT") == null) {
                                                            quotedProcName2 = true;
                                                            if (paramRetrievalRs2.hasMoreTokens()) {
                                                                isOutParam = true;
                                                                resultSet3 = paramRetrievalRs;
                                                                bArr = procNameAsBytes3;
                                                                throw SQLError.createSQLException("Internal error when parsing callable statement metadata (missing parameter name)", SQLError.SQL_STATE_GENERAL_ERROR, getExceptionInterceptor());
                                                            }
                                                            isOutParam2 = paramRetrievalRs2.nextToken();
                                                            resultSet3 = paramRetrievalRs;
                                                            parameterDef3 = procNameAsBytes3;
                                                        } else {
                                                            resultSet3 = paramRetrievalRs;
                                                            parameterDef3 = procNameAsBytes3;
                                                            if (identifierMarkers.equalsIgnoreCase("INOUT") == null) {
                                                                quotedProcName2 = true;
                                                                isInParam = true;
                                                                if (paramRetrievalRs2.hasMoreTokens() == null) {
                                                                    isOutParam2 = paramRetrievalRs2.nextToken();
                                                                } else {
                                                                    isOutParam3 = true;
                                                                    isInParam2 = true;
                                                                    throw SQLError.createSQLException("Internal error when parsing callable statement metadata (missing parameter name)", SQLError.SQL_STATE_GENERAL_ERROR, getExceptionInterceptor());
                                                                }
                                                            } else if (identifierMarkers.equalsIgnoreCase("IN") == null) {
                                                                quotedProcName2 = null;
                                                                isInParam = true;
                                                                if (paramRetrievalRs2.hasMoreTokens() == null) {
                                                                    isOutParam2 = paramRetrievalRs2.nextToken();
                                                                } else {
                                                                    isOutParam4 = false;
                                                                    isInParam3 = true;
                                                                    throw SQLError.createSQLException("Internal error when parsing callable statement metadata (missing parameter name)", SQLError.SQL_STATE_GENERAL_ERROR, getExceptionInterceptor());
                                                                }
                                                            } else {
                                                                quotedProcName2 = null;
                                                                isInParam = true;
                                                                isOutParam2 = identifierMarkers;
                                                            }
                                                            parameterNamePattern4 = isInParam;
                                                        }
                                                        isInParam = quotedProcName2;
                                                        quotedProcName2 = isOutParam2;
                                                        if (paramRetrievalRs2.hasMoreTokens() == null) {
                                                            possibleParamName = identifierMarkers;
                                                            typeDesc = null;
                                                            identifierMarkers = new StringBuilder(paramRetrievalRs2.nextToken());
                                                            while (paramRetrievalRs2.hasMoreTokens() != null) {
                                                                identifierMarkers.append(" ");
                                                                identifierMarkers.append(paramRetrievalRs2.nextToken());
                                                            }
                                                            typeInfoBuf = identifierMarkers;
                                                            resultSet4 = paramRetrievalRs2;
                                                            catalog5 = new TypeDescriptor(this, identifierMarkers.toString(), "YES");
                                                            if ((quotedProcName2.startsWith("`") == null && quotedProcName2.endsWith("`") != null) || (procCatAsBytes4 != null && quotedProcName2.startsWith("\"") != null && quotedProcName2.endsWith("\"") != null)) {
                                                                quotedProcName2 = quotedProcName2.substring(1, quotedProcName2.length() - 1);
                                                            }
                                                            if (StringUtils.wildCompareIgnoreCase(quotedProcName2, paramRetrievalStmt5) == null) {
                                                                identifierMarkers = ordinal + 1;
                                                                catalog3.add(convertTypeDescriptorToProcedureRow(procNameAsBytes, procCatAsBytes2, quotedProcName2, isInParam, parameterNamePattern4, false, catalog5, forGetFunctionColumns3, ordinal));
                                                                ordinal = identifierMarkers;
                                                            }
                                                            i = parameterNamePattern2 + 1;
                                                            parameterNamePattern2 = quotedProcName3;
                                                            identifierMarkers = parseListLen;
                                                            paramRetrievalRs2 = procType3;
                                                            quotedProcName2 = str5;
                                                            paramRetrievalRs = resultSet3;
                                                            procNameAsBytes3 = parameterDef3;
                                                        } else {
                                                            possibleParamName = identifierMarkers;
                                                            resultSet4 = paramRetrievalRs2;
                                                            bArr2 = null;
                                                            throw SQLError.createSQLException("Internal error when parsing callable statement metadata (missing parameter type)", SQLError.SQL_STATE_GENERAL_ERROR, getExceptionInterceptor());
                                                        }
                                                    }
                                                    procType3 = paramRetrievalRs2;
                                                    str5 = quotedProcName2;
                                                    resultSet3 = paramRetrievalRs;
                                                    bArr = procNameAsBytes3;
                                                    return;
                                                }
                                            } else {
                                                quotedProcName3 = parameterNamePattern2;
                                                procType3 = paramRetrievalRs2;
                                                str5 = quotedProcName2;
                                                resultSet3 = paramRetrievalRs;
                                                bArr = procNameAsBytes3;
                                            }
                                        }
                                    }
                                    oldCatalog = procedureDef2;
                                    endOfParamDeclarationIndex3 = endOfParamDeclarationIndex2;
                                    throw SQLError.createSQLException("Internal error when parsing callable statement metadata", SQLError.SQL_STATE_GENERAL_ERROR, getExceptionInterceptor());
                                }
                            }
                            catalog5 = catalog3;
                            resultSet2 = paramRetrievalRs2;
                            parameterNamePattern3 = quotedProcName2;
                            paramRetrievalStmt = paramRetrievalStmt3;
                            i = 0;
                            catalog3 = resultRows;
                            this = databaseMetaData2;
                            identifierMarkers = catalog5;
                            parameterNamePattern2 = parameterNamePattern3;
                            paramRetrievalRs2 = procType2;
                            paramRetrievalStmt5 = parameterNamePattern4;
                            forGetFunctionColumns3 = forGetFunctionColumns;
                            quotedProcName2 = paramRetrievalStmt;
                            paramRetrievalRs = resultSet2;
                            procNameAsBytes3 = parameterDef;
                            procCatAsBytes4 = isProcedureInAnsiMode;
                            parameterDef2 = storageDefnDelims;
                            parameterDef = storageDefnClosures;
                            sqlExRethrow2 = null;
                            if (paramRetrievalRs != null) {
                                paramRetrievalRs.close();
                                paramRetrievalRs = null;
                            }
                            if (quotedProcName2 != null) {
                                quotedProcName2.close();
                                quotedProcName2 = null;
                            }
                            if (sqlExRethrow2 == null) {
                                throw sqlExRethrow2;
                            } else if (procNameAsBytes3 == null) {
                                quotedProcName3 = parameterNamePattern2;
                                procType3 = paramRetrievalRs2;
                                str5 = quotedProcName2;
                                resultSet3 = paramRetrievalRs;
                                bArr = procNameAsBytes3;
                            } else {
                                ordinal = 1;
                                storageDefnDelims = StringUtils.split(procNameAsBytes3, ",", parameterDef2, parameterDef, true);
                                identifierMarkers = storageDefnDelims.size();
                                while (true) {
                                    quotedProcName3 = parameterNamePattern2;
                                    parameterNamePattern2 = i;
                                    if (parameterNamePattern2 >= identifierMarkers) {
                                        procType3 = paramRetrievalRs2;
                                        str5 = quotedProcName2;
                                        resultSet3 = paramRetrievalRs;
                                        bArr = procNameAsBytes3;
                                        return;
                                    }
                                    parseListLen = identifierMarkers;
                                    identifierMarkers = (String) storageDefnDelims.get(parameterNamePattern2);
                                    procType3 = paramRetrievalRs2;
                                    if (identifierMarkers.trim().length() != null) {
                                        str5 = quotedProcName2;
                                        identifierMarkers = identifierMarkers.replaceAll("[\\t\\n\\x0B\\f\\r]", " ");
                                        paramRetrievalRs2 = new StringTokenizer(identifierMarkers, " \t");
                                        parameterNamePattern4 = null;
                                        if (paramRetrievalRs2.hasMoreTokens()) {
                                            declaration = identifierMarkers;
                                            resultSet4 = paramRetrievalRs2;
                                            str6 = null;
                                            resultSet3 = paramRetrievalRs;
                                            bArr = procNameAsBytes3;
                                            throw SQLError.createSQLException("Internal error when parsing callable statement metadata (unknown output from 'SHOW CREATE PROCEDURE')", SQLError.SQL_STATE_GENERAL_ERROR, getExceptionInterceptor());
                                        }
                                        declaration = identifierMarkers;
                                        identifierMarkers = paramRetrievalRs2.nextToken();
                                        str6 = null;
                                        if (identifierMarkers.equalsIgnoreCase("OUT") == null) {
                                            resultSet3 = paramRetrievalRs;
                                            parameterDef3 = procNameAsBytes3;
                                            if (identifierMarkers.equalsIgnoreCase("INOUT") == null) {
                                                quotedProcName2 = true;
                                                isInParam = true;
                                                if (paramRetrievalRs2.hasMoreTokens() == null) {
                                                    isOutParam3 = true;
                                                    isInParam2 = true;
                                                    throw SQLError.createSQLException("Internal error when parsing callable statement metadata (missing parameter name)", SQLError.SQL_STATE_GENERAL_ERROR, getExceptionInterceptor());
                                                }
                                                isOutParam2 = paramRetrievalRs2.nextToken();
                                            } else if (identifierMarkers.equalsIgnoreCase("IN") == null) {
                                                quotedProcName2 = null;
                                                isInParam = true;
                                                isOutParam2 = identifierMarkers;
                                            } else {
                                                quotedProcName2 = null;
                                                isInParam = true;
                                                if (paramRetrievalRs2.hasMoreTokens() == null) {
                                                    isOutParam4 = false;
                                                    isInParam3 = true;
                                                    throw SQLError.createSQLException("Internal error when parsing callable statement metadata (missing parameter name)", SQLError.SQL_STATE_GENERAL_ERROR, getExceptionInterceptor());
                                                }
                                                isOutParam2 = paramRetrievalRs2.nextToken();
                                            }
                                            parameterNamePattern4 = isInParam;
                                        } else {
                                            quotedProcName2 = true;
                                            if (paramRetrievalRs2.hasMoreTokens()) {
                                                isOutParam = true;
                                                resultSet3 = paramRetrievalRs;
                                                bArr = procNameAsBytes3;
                                                throw SQLError.createSQLException("Internal error when parsing callable statement metadata (missing parameter name)", SQLError.SQL_STATE_GENERAL_ERROR, getExceptionInterceptor());
                                            }
                                            isOutParam2 = paramRetrievalRs2.nextToken();
                                            resultSet3 = paramRetrievalRs;
                                            parameterDef3 = procNameAsBytes3;
                                        }
                                        isInParam = quotedProcName2;
                                        quotedProcName2 = isOutParam2;
                                        if (paramRetrievalRs2.hasMoreTokens() == null) {
                                            possibleParamName = identifierMarkers;
                                            resultSet4 = paramRetrievalRs2;
                                            bArr2 = null;
                                            throw SQLError.createSQLException("Internal error when parsing callable statement metadata (missing parameter type)", SQLError.SQL_STATE_GENERAL_ERROR, getExceptionInterceptor());
                                        }
                                        possibleParamName = identifierMarkers;
                                        typeDesc = null;
                                        identifierMarkers = new StringBuilder(paramRetrievalRs2.nextToken());
                                        while (paramRetrievalRs2.hasMoreTokens() != null) {
                                            identifierMarkers.append(" ");
                                            identifierMarkers.append(paramRetrievalRs2.nextToken());
                                        }
                                        typeInfoBuf = identifierMarkers;
                                        resultSet4 = paramRetrievalRs2;
                                        catalog5 = new TypeDescriptor(this, identifierMarkers.toString(), "YES");
                                        if (quotedProcName2.startsWith("`") == null) {
                                        }
                                        if (StringUtils.wildCompareIgnoreCase(quotedProcName2, paramRetrievalStmt5) == null) {
                                            identifierMarkers = ordinal + 1;
                                            catalog3.add(convertTypeDescriptorToProcedureRow(procNameAsBytes, procCatAsBytes2, quotedProcName2, isInParam, parameterNamePattern4, false, catalog5, forGetFunctionColumns3, ordinal));
                                            ordinal = identifierMarkers;
                                        }
                                        i = parameterNamePattern2 + 1;
                                        parameterNamePattern2 = quotedProcName3;
                                        identifierMarkers = parseListLen;
                                        paramRetrievalRs2 = procType3;
                                        quotedProcName2 = str5;
                                        paramRetrievalRs = resultSet3;
                                        procNameAsBytes3 = parameterDef3;
                                    } else {
                                        str5 = quotedProcName2;
                                        resultSet3 = paramRetrievalRs;
                                        bArr = procNameAsBytes3;
                                        return;
                                    }
                                }
                            }
                        } catch (Throwable th4222222222222222) {
                            catalog5 = catalog3;
                            parameterNamePattern3 = quotedProcName2;
                            paramRetrievalStmt = paramRetrievalStmt3;
                            list = resultRows;
                            forGetFunctionColumns2 = forGetFunctionColumns;
                            th2 = th4222222222222222;
                            databaseMetaData = databaseMetaData2;
                            paramRetrievalRs = paramRetrievalRs2;
                            paramRetrievalStmt3 = paramRetrievalStmt;
                            parameterDef2 = null;
                            sqlExRethrow = null;
                            if (paramRetrievalRs != null) {
                                paramRetrievalRs.close();
                            }
                            if (paramRetrievalStmt3 != null) {
                                paramRetrievalStmt3.close();
                            }
                            if (sqlExRethrow != null) {
                                throw th2;
                            }
                            throw sqlExRethrow;
                        }
                    }
                }
                catalog3 = catalog4;
            } catch (Throwable th42222222222222222) {
                catalog5 = catalog3;
                paramRetrievalStmt = paramRetrievalStmt6;
                list = resultRows;
                forGetFunctionColumns2 = forGetFunctionColumns;
                th2 = th42222222222222222;
                parameterNamePattern3 = quotedProcName2;
                procNameAsBytes = null;
                databaseMetaData = databaseMetaData2;
                procCatAsBytes2 = null;
                paramRetrievalStmt3 = paramRetrievalStmt;
                parameterDef2 = null;
                sqlExRethrow = null;
                if (paramRetrievalRs != null) {
                    paramRetrievalRs.close();
                }
                if (paramRetrievalStmt3 != null) {
                    paramRetrievalStmt3.close();
                }
                if (sqlExRethrow != null) {
                    throw th2;
                }
                throw sqlExRethrow;
            }
            try {
                parameterNamePattern2 = StringUtils.quoteIdentifier(catalog3, databaseMetaData2.quotedId, databaseMetaData2.conn.getPedantic());
                str = StringUtils.unQuoteIdentifier(quotedProcName2, databaseMetaData2.quotedId);
                procNameAsBytes2 = StringUtils.getBytes(str, "UTF-8");
                procNameAsBytes = procNameAsBytes2;
                tmpProcName = StringUtils.unQuoteIdentifier(parameterNamePattern2, databaseMetaData2.quotedId);
                procCatAsBytes3 = StringUtils.getBytes(tmpProcName, "UTF-8");
                procCatAsBytes2 = procCatAsBytes3;
                procNameBuf = new StringBuilder();
                procNameBuf.append(parameterNamePattern2);
                procNameBuf.append('.');
                procNameBuf.append(quotedProcName2);
                if (procType2 != ProcedureType.PROCEDURE) {
                    paramRetrievalStmt3 = paramRetrievalStmt2;
                    stringBuilder2 = new StringBuilder();
                    fieldName = null;
                    stringBuilder2.append("SHOW CREATE FUNCTION ");
                    stringBuilder2.append(procNameBuf.toString());
                    paramRetrievalRs = paramRetrievalStmt3.executeQuery(stringBuilder2.toString());
                    str = "Create Function";
                } else {
                    stringBuilder = new StringBuilder();
                    stringBuilder.append("SHOW CREATE PROCEDURE ");
                    stringBuilder.append(procNameBuf.toString());
                    oldCatalog = stringBuilder.toString();
                    paramRetrievalStmt3 = paramRetrievalStmt2;
                    paramRetrievalRs = paramRetrievalStmt3.executeQuery(oldCatalog);
                    str = "Create Procedure";
                }
                paramRetrievalRs2 = paramRetrievalRs;
                if (paramRetrievalRs2.next()) {
                    procedureDef = paramRetrievalRs2.getString(str);
                    fieldName2 = str;
                    if (databaseMetaData2.conn.getNoAccessToProcedureBodies()) {
                        procNameBuf2 = procNameBuf;
                        dbName = parameterNamePattern2;
                    } else {
                        if (procedureDef != null) {
                            if (procedureDef.length() == 0) {
                                procNameBuf2 = procNameBuf;
                                dbName = parameterNamePattern2;
                            }
                        }
                        throw SQLError.createSQLException("User does not have access to metadata required to determine stored procedure parameter types. If rights can not be granted, configure connection with \"noAccessToProcedureBodies=true\" to have driver generate parameters that represent INOUT strings irregardless of actual parameter types.", SQLError.SQL_STATE_GENERAL_ERROR, getExceptionInterceptor());
                    }
                    if (StringUtils.indexOfIgnoreCase(paramRetrievalRs2.getString("sql_mode"), "ANSI") != -1) {
                        isProcedureInAnsiMode = true;
                    }
                    if (isProcedureInAnsiMode) {
                    }
                    identifierMarkers = isProcedureInAnsiMode ? "`\"" : "`";
                    stringBuilder3 = new StringBuilder();
                    stringBuilder3.append("'");
                    stringBuilder3.append(identifierMarkers);
                    identifierAndStringMarkers = stringBuilder3.toString();
                    stringBuilder3 = new StringBuilder();
                    stringBuilder3.append("(");
                    stringBuilder3.append(identifierMarkers);
                    storageDefnDelims = stringBuilder3.toString();
                    stringBuilder3 = new StringBuilder();
                    stringBuilder3.append(")");
                    stringBuilder3.append(identifierMarkers);
                    storageDefnClosures = stringBuilder3.toString();
                    parameterNamePattern2 = StringUtils.stripComments(procedureDef, identifierAndStringMarkers, identifierAndStringMarkers, true, false, true, true);
                    identifierMarkers2 = identifierMarkers;
                    if (databaseMetaData2.conn.isNoBackslashEscapesSet()) {
                    }
                    openParenIndex = StringUtils.indexOfIgnoreCase(0, parameterNamePattern2, "(", databaseMetaData2.quotedId, databaseMetaData2.quotedId, databaseMetaData2.conn.isNoBackslashEscapesSet() ? StringUtils.SEARCH_MODE__MRK_COM_WS : StringUtils.SEARCH_MODE__ALL);
                    dotIndex = endPositionOfParameterDeclaration(openParenIndex, parameterNamePattern2, databaseMetaData2.quotedId);
                    if (procType2 != ProcedureType.FUNCTION) {
                        endOfParamDeclarationIndex2 = dotIndex;
                        openParenIndex3 = openParenIndex;
                        procedureDef2 = parameterNamePattern2;
                        catalog5 = catalog3;
                        resultSet2 = paramRetrievalRs2;
                        i2 = dotIndex2;
                        parameterNamePattern3 = quotedProcName2;
                        str2 = tmpProcName;
                        paramRetrievalStmt = paramRetrievalStmt3;
                        procCatAsBytes = fieldName2;
                        paramRetrievalStmt4 = procNameBuf2;
                        str3 = dbName;
                        str4 = identifierMarkers2;
                        i = 0;
                        catalog3 = resultRows;
                    } else {
                        endOfParamDeclarationIndex = dotIndex;
                        openParenIndex2 = openParenIndex;
                        if (databaseMetaData2.conn.isNoBackslashEscapesSet()) {
                        }
                        openParenIndex = StringUtils.indexOfIgnoreCase(0, parameterNamePattern2, " RETURNS ", databaseMetaData2.quotedId, (String) databaseMetaData2.quotedId, databaseMetaData2.conn.isNoBackslashEscapesSet() ? StringUtils.SEARCH_MODE__MRK_COM_WS : StringUtils.SEARCH_MODE__ALL);
                        dotIndex = findEndOfReturnsClause(parameterNamePattern2, openParenIndex);
                        declarationStart = "RETURNS ".length() + openParenIndex;
                        while (true) {
                            returnsIndex = openParenIndex;
                            if (declarationStart < parameterNamePattern2.length()) {
                                if (Character.isWhitespace(parameterNamePattern2.charAt(declarationStart))) {
                                    declarationStart++;
                                    openParenIndex = returnsIndex;
                                }
                            }
                            break;
                        }
                        identifierMarkers = parameterNamePattern2.substring(declarationStart, dotIndex).trim();
                        catalog4 = dotIndex;
                        openParenIndex3 = openParenIndex2;
                        endOfParamDeclarationIndex2 = endOfParamDeclarationIndex;
                        procedureDef2 = parameterNamePattern2;
                        catalog5 = catalog3;
                        resultSet2 = paramRetrievalRs2;
                        parameterNamePattern3 = quotedProcName2;
                        paramRetrievalStmt = paramRetrievalStmt3;
                        i = 0;
                        catalog3 = resultRows;
                        catalog3.add(convertTypeDescriptorToProcedureRow(procNameAsBytes, procCatAsBytes2, "", false, false, true, new TypeDescriptor(databaseMetaData2, identifierMarkers, "YES"), forGetFunctionColumns, null));
                    }
                    dotIndex = openParenIndex3;
                    if (dotIndex == -1) {
                        oldCatalog = procedureDef2;
                        endOfParamDeclarationIndex3 = endOfParamDeclarationIndex2;
                    } else {
                        endOfParamDeclarationIndex3 = endOfParamDeclarationIndex2;
                        if (endOfParamDeclarationIndex3 != -1) {
                            parameterDef = procedureDef2.substring(dotIndex + 1, endOfParamDeclarationIndex3);
                            this = databaseMetaData2;
                            identifierMarkers = catalog5;
                            parameterNamePattern2 = parameterNamePattern3;
                            paramRetrievalRs2 = procType2;
                            paramRetrievalStmt5 = parameterNamePattern4;
                            forGetFunctionColumns3 = forGetFunctionColumns;
                            quotedProcName2 = paramRetrievalStmt;
                            paramRetrievalRs = resultSet2;
                            procNameAsBytes3 = parameterDef;
                            procCatAsBytes4 = isProcedureInAnsiMode;
                            parameterDef2 = storageDefnDelims;
                            parameterDef = storageDefnClosures;
                            sqlExRethrow2 = null;
                            if (paramRetrievalRs != null) {
                                paramRetrievalRs.close();
                                paramRetrievalRs = null;
                            }
                            if (quotedProcName2 != null) {
                                quotedProcName2.close();
                                quotedProcName2 = null;
                            }
                            if (sqlExRethrow2 == null) {
                                throw sqlExRethrow2;
                            } else if (procNameAsBytes3 == null) {
                                ordinal = 1;
                                storageDefnDelims = StringUtils.split(procNameAsBytes3, ",", parameterDef2, parameterDef, true);
                                identifierMarkers = storageDefnDelims.size();
                                while (true) {
                                    quotedProcName3 = parameterNamePattern2;
                                    parameterNamePattern2 = i;
                                    if (parameterNamePattern2 >= identifierMarkers) {
                                        parseListLen = identifierMarkers;
                                        identifierMarkers = (String) storageDefnDelims.get(parameterNamePattern2);
                                        procType3 = paramRetrievalRs2;
                                        if (identifierMarkers.trim().length() != null) {
                                            str5 = quotedProcName2;
                                            resultSet3 = paramRetrievalRs;
                                            bArr = procNameAsBytes3;
                                            return;
                                        }
                                        str5 = quotedProcName2;
                                        identifierMarkers = identifierMarkers.replaceAll("[\\t\\n\\x0B\\f\\r]", " ");
                                        paramRetrievalRs2 = new StringTokenizer(identifierMarkers, " \t");
                                        parameterNamePattern4 = null;
                                        if (paramRetrievalRs2.hasMoreTokens()) {
                                            declaration = identifierMarkers;
                                            identifierMarkers = paramRetrievalRs2.nextToken();
                                            str6 = null;
                                            if (identifierMarkers.equalsIgnoreCase("OUT") == null) {
                                                quotedProcName2 = true;
                                                if (paramRetrievalRs2.hasMoreTokens()) {
                                                    isOutParam2 = paramRetrievalRs2.nextToken();
                                                    resultSet3 = paramRetrievalRs;
                                                    parameterDef3 = procNameAsBytes3;
                                                } else {
                                                    isOutParam = true;
                                                    resultSet3 = paramRetrievalRs;
                                                    bArr = procNameAsBytes3;
                                                    throw SQLError.createSQLException("Internal error when parsing callable statement metadata (missing parameter name)", SQLError.SQL_STATE_GENERAL_ERROR, getExceptionInterceptor());
                                                }
                                            }
                                            resultSet3 = paramRetrievalRs;
                                            parameterDef3 = procNameAsBytes3;
                                            if (identifierMarkers.equalsIgnoreCase("INOUT") == null) {
                                                quotedProcName2 = true;
                                                isInParam = true;
                                                if (paramRetrievalRs2.hasMoreTokens() == null) {
                                                    isOutParam2 = paramRetrievalRs2.nextToken();
                                                } else {
                                                    isOutParam3 = true;
                                                    isInParam2 = true;
                                                    throw SQLError.createSQLException("Internal error when parsing callable statement metadata (missing parameter name)", SQLError.SQL_STATE_GENERAL_ERROR, getExceptionInterceptor());
                                                }
                                            } else if (identifierMarkers.equalsIgnoreCase("IN") == null) {
                                                quotedProcName2 = null;
                                                isInParam = true;
                                                if (paramRetrievalRs2.hasMoreTokens() == null) {
                                                    isOutParam2 = paramRetrievalRs2.nextToken();
                                                } else {
                                                    isOutParam4 = false;
                                                    isInParam3 = true;
                                                    throw SQLError.createSQLException("Internal error when parsing callable statement metadata (missing parameter name)", SQLError.SQL_STATE_GENERAL_ERROR, getExceptionInterceptor());
                                                }
                                            } else {
                                                quotedProcName2 = null;
                                                isInParam = true;
                                                isOutParam2 = identifierMarkers;
                                            }
                                            parameterNamePattern4 = isInParam;
                                            isInParam = quotedProcName2;
                                            quotedProcName2 = isOutParam2;
                                            if (paramRetrievalRs2.hasMoreTokens() == null) {
                                                possibleParamName = identifierMarkers;
                                                typeDesc = null;
                                                identifierMarkers = new StringBuilder(paramRetrievalRs2.nextToken());
                                                while (paramRetrievalRs2.hasMoreTokens() != null) {
                                                    identifierMarkers.append(" ");
                                                    identifierMarkers.append(paramRetrievalRs2.nextToken());
                                                }
                                                typeInfoBuf = identifierMarkers;
                                                resultSet4 = paramRetrievalRs2;
                                                catalog5 = new TypeDescriptor(this, identifierMarkers.toString(), "YES");
                                                if (quotedProcName2.startsWith("`") == null) {
                                                }
                                                if (StringUtils.wildCompareIgnoreCase(quotedProcName2, paramRetrievalStmt5) == null) {
                                                    identifierMarkers = ordinal + 1;
                                                    catalog3.add(convertTypeDescriptorToProcedureRow(procNameAsBytes, procCatAsBytes2, quotedProcName2, isInParam, parameterNamePattern4, false, catalog5, forGetFunctionColumns3, ordinal));
                                                    ordinal = identifierMarkers;
                                                }
                                                i = parameterNamePattern2 + 1;
                                                parameterNamePattern2 = quotedProcName3;
                                                identifierMarkers = parseListLen;
                                                paramRetrievalRs2 = procType3;
                                                quotedProcName2 = str5;
                                                paramRetrievalRs = resultSet3;
                                                procNameAsBytes3 = parameterDef3;
                                            } else {
                                                possibleParamName = identifierMarkers;
                                                resultSet4 = paramRetrievalRs2;
                                                bArr2 = null;
                                                throw SQLError.createSQLException("Internal error when parsing callable statement metadata (missing parameter type)", SQLError.SQL_STATE_GENERAL_ERROR, getExceptionInterceptor());
                                            }
                                        }
                                        declaration = identifierMarkers;
                                        resultSet4 = paramRetrievalRs2;
                                        str6 = null;
                                        resultSet3 = paramRetrievalRs;
                                        bArr = procNameAsBytes3;
                                        throw SQLError.createSQLException("Internal error when parsing callable statement metadata (unknown output from 'SHOW CREATE PROCEDURE')", SQLError.SQL_STATE_GENERAL_ERROR, getExceptionInterceptor());
                                    }
                                    procType3 = paramRetrievalRs2;
                                    str5 = quotedProcName2;
                                    resultSet3 = paramRetrievalRs;
                                    bArr = procNameAsBytes3;
                                    return;
                                }
                            } else {
                                quotedProcName3 = parameterNamePattern2;
                                procType3 = paramRetrievalRs2;
                                str5 = quotedProcName2;
                                resultSet3 = paramRetrievalRs;
                                bArr = procNameAsBytes3;
                            }
                        }
                        oldCatalog = procedureDef2;
                    }
                    throw SQLError.createSQLException("Internal error when parsing callable statement metadata", SQLError.SQL_STATE_GENERAL_ERROR, getExceptionInterceptor());
                }
                catalog5 = catalog3;
                resultSet2 = paramRetrievalRs2;
                parameterNamePattern3 = quotedProcName2;
                paramRetrievalStmt = paramRetrievalStmt3;
                i = 0;
                catalog3 = resultRows;
                this = databaseMetaData2;
                identifierMarkers = catalog5;
                parameterNamePattern2 = parameterNamePattern3;
                paramRetrievalRs2 = procType2;
                paramRetrievalStmt5 = parameterNamePattern4;
                forGetFunctionColumns3 = forGetFunctionColumns;
                quotedProcName2 = paramRetrievalStmt;
                paramRetrievalRs = resultSet2;
                procNameAsBytes3 = parameterDef;
                procCatAsBytes4 = isProcedureInAnsiMode;
                parameterDef2 = storageDefnDelims;
                parameterDef = storageDefnClosures;
                sqlExRethrow2 = null;
                if (paramRetrievalRs != null) {
                    paramRetrievalRs.close();
                    paramRetrievalRs = null;
                }
                if (quotedProcName2 != null) {
                    quotedProcName2.close();
                    quotedProcName2 = null;
                }
                if (sqlExRethrow2 == null) {
                    throw sqlExRethrow2;
                } else if (procNameAsBytes3 == null) {
                    quotedProcName3 = parameterNamePattern2;
                    procType3 = paramRetrievalRs2;
                    str5 = quotedProcName2;
                    resultSet3 = paramRetrievalRs;
                    bArr = procNameAsBytes3;
                } else {
                    ordinal = 1;
                    storageDefnDelims = StringUtils.split(procNameAsBytes3, ",", parameterDef2, parameterDef, true);
                    identifierMarkers = storageDefnDelims.size();
                    while (true) {
                        quotedProcName3 = parameterNamePattern2;
                        parameterNamePattern2 = i;
                        if (parameterNamePattern2 >= identifierMarkers) {
                            procType3 = paramRetrievalRs2;
                            str5 = quotedProcName2;
                            resultSet3 = paramRetrievalRs;
                            bArr = procNameAsBytes3;
                            return;
                        }
                        parseListLen = identifierMarkers;
                        identifierMarkers = (String) storageDefnDelims.get(parameterNamePattern2);
                        procType3 = paramRetrievalRs2;
                        if (identifierMarkers.trim().length() != null) {
                            str5 = quotedProcName2;
                            identifierMarkers = identifierMarkers.replaceAll("[\\t\\n\\x0B\\f\\r]", " ");
                            paramRetrievalRs2 = new StringTokenizer(identifierMarkers, " \t");
                            parameterNamePattern4 = null;
                            if (paramRetrievalRs2.hasMoreTokens()) {
                                declaration = identifierMarkers;
                                resultSet4 = paramRetrievalRs2;
                                str6 = null;
                                resultSet3 = paramRetrievalRs;
                                bArr = procNameAsBytes3;
                                throw SQLError.createSQLException("Internal error when parsing callable statement metadata (unknown output from 'SHOW CREATE PROCEDURE')", SQLError.SQL_STATE_GENERAL_ERROR, getExceptionInterceptor());
                            }
                            declaration = identifierMarkers;
                            identifierMarkers = paramRetrievalRs2.nextToken();
                            str6 = null;
                            if (identifierMarkers.equalsIgnoreCase("OUT") == null) {
                                resultSet3 = paramRetrievalRs;
                                parameterDef3 = procNameAsBytes3;
                                if (identifierMarkers.equalsIgnoreCase("INOUT") == null) {
                                    quotedProcName2 = true;
                                    isInParam = true;
                                    if (paramRetrievalRs2.hasMoreTokens() == null) {
                                        isOutParam3 = true;
                                        isInParam2 = true;
                                        throw SQLError.createSQLException("Internal error when parsing callable statement metadata (missing parameter name)", SQLError.SQL_STATE_GENERAL_ERROR, getExceptionInterceptor());
                                    }
                                    isOutParam2 = paramRetrievalRs2.nextToken();
                                } else if (identifierMarkers.equalsIgnoreCase("IN") == null) {
                                    quotedProcName2 = null;
                                    isInParam = true;
                                    isOutParam2 = identifierMarkers;
                                } else {
                                    quotedProcName2 = null;
                                    isInParam = true;
                                    if (paramRetrievalRs2.hasMoreTokens() == null) {
                                        isOutParam4 = false;
                                        isInParam3 = true;
                                        throw SQLError.createSQLException("Internal error when parsing callable statement metadata (missing parameter name)", SQLError.SQL_STATE_GENERAL_ERROR, getExceptionInterceptor());
                                    }
                                    isOutParam2 = paramRetrievalRs2.nextToken();
                                }
                                parameterNamePattern4 = isInParam;
                            } else {
                                quotedProcName2 = true;
                                if (paramRetrievalRs2.hasMoreTokens()) {
                                    isOutParam = true;
                                    resultSet3 = paramRetrievalRs;
                                    bArr = procNameAsBytes3;
                                    throw SQLError.createSQLException("Internal error when parsing callable statement metadata (missing parameter name)", SQLError.SQL_STATE_GENERAL_ERROR, getExceptionInterceptor());
                                }
                                isOutParam2 = paramRetrievalRs2.nextToken();
                                resultSet3 = paramRetrievalRs;
                                parameterDef3 = procNameAsBytes3;
                            }
                            isInParam = quotedProcName2;
                            quotedProcName2 = isOutParam2;
                            if (paramRetrievalRs2.hasMoreTokens() == null) {
                                possibleParamName = identifierMarkers;
                                resultSet4 = paramRetrievalRs2;
                                bArr2 = null;
                                throw SQLError.createSQLException("Internal error when parsing callable statement metadata (missing parameter type)", SQLError.SQL_STATE_GENERAL_ERROR, getExceptionInterceptor());
                            }
                            possibleParamName = identifierMarkers;
                            typeDesc = null;
                            identifierMarkers = new StringBuilder(paramRetrievalRs2.nextToken());
                            while (paramRetrievalRs2.hasMoreTokens() != null) {
                                identifierMarkers.append(" ");
                                identifierMarkers.append(paramRetrievalRs2.nextToken());
                            }
                            typeInfoBuf = identifierMarkers;
                            resultSet4 = paramRetrievalRs2;
                            catalog5 = new TypeDescriptor(this, identifierMarkers.toString(), "YES");
                            if (quotedProcName2.startsWith("`") == null) {
                            }
                            if (StringUtils.wildCompareIgnoreCase(quotedProcName2, paramRetrievalStmt5) == null) {
                                identifierMarkers = ordinal + 1;
                                catalog3.add(convertTypeDescriptorToProcedureRow(procNameAsBytes, procCatAsBytes2, quotedProcName2, isInParam, parameterNamePattern4, false, catalog5, forGetFunctionColumns3, ordinal));
                                ordinal = identifierMarkers;
                            }
                            i = parameterNamePattern2 + 1;
                            parameterNamePattern2 = quotedProcName3;
                            identifierMarkers = parseListLen;
                            paramRetrievalRs2 = procType3;
                            quotedProcName2 = str5;
                            paramRetrievalRs = resultSet3;
                            procNameAsBytes3 = parameterDef3;
                        } else {
                            str5 = quotedProcName2;
                            resultSet3 = paramRetrievalRs;
                            bArr = procNameAsBytes3;
                            return;
                        }
                    }
                }
            } catch (Throwable th422222222222222222) {
                catalog5 = catalog3;
                paramRetrievalStmt = paramRetrievalStmt2;
                list = resultRows;
                forGetFunctionColumns2 = forGetFunctionColumns;
                th2 = th422222222222222222;
                parameterNamePattern3 = quotedProcName2;
                procNameAsBytes = null;
                databaseMetaData = databaseMetaData2;
                procCatAsBytes2 = procCatAsBytes;
                paramRetrievalStmt3 = paramRetrievalStmt;
                parameterDef2 = null;
                sqlExRethrow = null;
                if (paramRetrievalRs != null) {
                    paramRetrievalRs.close();
                }
                if (paramRetrievalStmt3 != null) {
                    paramRetrievalStmt3.close();
                }
                if (sqlExRethrow != null) {
                    throw th2;
                }
                throw sqlExRethrow;
            }
        } catch (Throwable th4222222222222222222) {
            forGetFunctionColumns2 = forGetFunctionColumns;
            catalog5 = str;
            paramRetrievalStmt = null;
            parameterNamePattern4 = parameterNamePattern2;
            parameterNamePattern3 = quotedProcName2;
            procNameAsBytes = null;
            databaseMetaData = databaseMetaData2;
            procCatAsBytes2 = null;
            th2 = th4222222222222222222;
            paramRetrievalStmt3 = paramRetrievalStmt;
            parameterDef2 = null;
            sqlExRethrow = null;
            if (paramRetrievalRs != null) {
                paramRetrievalRs.close();
            }
            if (paramRetrievalStmt3 != null) {
                paramRetrievalStmt3.close();
            }
            if (sqlExRethrow != null) {
                throw th2;
            }
            throw sqlExRethrow;
        }
    }

    private int endPositionOfParameterDeclaration(int beginIndex, String procedureDef, String quoteChar) throws SQLException {
        int currentPos = beginIndex + 1;
        int parenDepth = 1;
        while (parenDepth > 0 && currentPos < procedureDef.length()) {
            int closedParenIndex = StringUtils.indexOfIgnoreCase(currentPos, procedureDef, ")", quoteChar, quoteChar, this.conn.isNoBackslashEscapesSet() ? StringUtils.SEARCH_MODE__MRK_COM_WS : StringUtils.SEARCH_MODE__ALL);
            if (closedParenIndex != -1) {
                int nextOpenParenIndex = StringUtils.indexOfIgnoreCase(currentPos, procedureDef, "(", quoteChar, quoteChar, this.conn.isNoBackslashEscapesSet() ? StringUtils.SEARCH_MODE__MRK_COM_WS : StringUtils.SEARCH_MODE__ALL);
                if (nextOpenParenIndex == -1 || nextOpenParenIndex >= closedParenIndex) {
                    parenDepth--;
                    currentPos = closedParenIndex;
                } else {
                    parenDepth++;
                    currentPos = closedParenIndex + 1;
                }
            } else {
                throw SQLError.createSQLException("Internal error when parsing callable statement metadata", SQLError.SQL_STATE_GENERAL_ERROR, getExceptionInterceptor());
            }
        }
        return currentPos;
    }

    private int findEndOfReturnsClause(String procedureDefn, int positionOfReturnKeyword) throws SQLException {
        String openingMarkers = new StringBuilder();
        openingMarkers.append(this.quotedId);
        openingMarkers.append("(");
        openingMarkers = openingMarkers.toString();
        String closingMarkers = new StringBuilder();
        closingMarkers.append(this.quotedId);
        closingMarkers.append(")");
        closingMarkers = closingMarkers.toString();
        r2 = new String[11];
        int i = 0;
        r2[0] = "LANGUAGE";
        r2[1] = "NOT";
        r2[2] = "DETERMINISTIC";
        r2[3] = "CONTAINS";
        r2[4] = "NO";
        r2[5] = "READ";
        r2[6] = "MODIFIES";
        r2[7] = "SQL";
        r2[8] = "COMMENT";
        r2[9] = "BEGIN";
        r2[10] = "RETURN";
        String[] tokens = r2;
        int startLookingAt = ("RETURNS".length() + positionOfReturnKeyword) + 1;
        int endOfReturn = -1;
        while (true) {
            int i2 = i;
            if (i2 >= tokens.length) {
                break;
            }
            int endOfReturn2 = StringUtils.indexOfIgnoreCase(startLookingAt, procedureDefn, tokens[i2], openingMarkers, closingMarkers, this.conn.isNoBackslashEscapesSet() ? StringUtils.SEARCH_MODE__MRK_COM_WS : StringUtils.SEARCH_MODE__ALL);
            if (endOfReturn2 != -1 && (endOfReturn == -1 || endOfReturn2 < endOfReturn)) {
                endOfReturn = endOfReturn2;
            }
            i = i2 + 1;
        }
        if (endOfReturn != -1) {
            return endOfReturn;
        }
        endOfReturn2 = StringUtils.indexOfIgnoreCase(startLookingAt, procedureDefn, ":", openingMarkers, closingMarkers, this.conn.isNoBackslashEscapesSet() ? StringUtils.SEARCH_MODE__MRK_COM_WS : StringUtils.SEARCH_MODE__ALL);
        if (endOfReturn2 != -1) {
            for (int i3 = endOfReturn2; i3 > 0; i3--) {
                if (Character.isWhitespace(procedureDefn.charAt(i3))) {
                    return i3;
                }
            }
        }
        throw SQLError.createSQLException("Internal error when parsing callable statement metadata", SQLError.SQL_STATE_GENERAL_ERROR, getExceptionInterceptor());
    }

    private int getCascadeDeleteOption(String cascadeOptions) {
        int onDeletePos = cascadeOptions.indexOf("ON DELETE");
        if (onDeletePos != -1) {
            String deleteOptions = cascadeOptions.substring(onDeletePos, cascadeOptions.length());
            if (deleteOptions.startsWith("ON DELETE CASCADE")) {
                return 0;
            }
            if (deleteOptions.startsWith("ON DELETE SET NULL")) {
                return 2;
            }
            if (deleteOptions.startsWith("ON DELETE RESTRICT")) {
                return 1;
            }
            if (deleteOptions.startsWith("ON DELETE NO ACTION")) {
                return 3;
            }
        }
        return 3;
    }

    private int getCascadeUpdateOption(String cascadeOptions) {
        int onUpdatePos = cascadeOptions.indexOf("ON UPDATE");
        if (onUpdatePos != -1) {
            String updateOptions = cascadeOptions.substring(onUpdatePos, cascadeOptions.length());
            if (updateOptions.startsWith("ON UPDATE CASCADE")) {
                return 0;
            }
            if (updateOptions.startsWith("ON UPDATE SET NULL")) {
                return 2;
            }
            if (updateOptions.startsWith("ON UPDATE RESTRICT")) {
                return 1;
            }
            if (updateOptions.startsWith("ON UPDATE NO ACTION")) {
                return 3;
            }
        }
        return 3;
    }

    protected IteratorWithCleanup<String> getCatalogIterator(String catalogSpec) throws SQLException {
        IteratorWithCleanup<String> allCatalogsIter;
        if (catalogSpec != null) {
            if (catalogSpec.equals("")) {
                allCatalogsIter = new SingleStringIterator(this.database);
            } else if (this.conn.getPedantic()) {
                allCatalogsIter = new SingleStringIterator(catalogSpec);
            } else {
                allCatalogsIter = new SingleStringIterator(StringUtils.unQuoteIdentifier(catalogSpec, this.quotedId));
            }
        } else if (!this.conn.getNullCatalogMeansCurrent()) {
            return new ResultSetIterator(getCatalogs(), 1);
        } else {
            allCatalogsIter = new SingleStringIterator(this.database);
        }
        return allCatalogsIter;
    }

    public ResultSet getCatalogs() throws SQLException {
        ResultSet results = null;
        Statement stmt = null;
        try {
            stmt = this.conn.getMetadataSafeStatement();
            results = stmt.executeQuery("SHOW DATABASES");
            int catalogsCount = 0;
            if (results.last()) {
                catalogsCount = results.getRow();
                results.beforeFirst();
            }
            List<String> resultsAsList = new ArrayList(catalogsCount);
            while (results.next()) {
                resultsAsList.add(results.getString(1));
            }
            Collections.sort(resultsAsList);
            Field[] fields = new Field[]{new Field("", "TABLE_CAT", 12, results.getMetaData().getColumnDisplaySize(1))};
            ArrayList<ResultSetRow> tuples = new ArrayList(catalogsCount);
            Iterator i$ = resultsAsList.iterator();
            while (i$.hasNext()) {
                tuples.add(new ByteArrayRow(new byte[][]{s2b((String) i$.next())}, getExceptionInterceptor()));
            }
            ResultSet buildResultSet = buildResultSet(fields, tuples);
            if (results != null) {
                try {
                    results.close();
                } catch (SQLException sqlEx) {
                    AssertionFailedException.shouldNotHappen(sqlEx);
                }
            }
            if (stmt != null) {
                try {
                    stmt.close();
                } catch (SQLException sqlEx2) {
                    AssertionFailedException.shouldNotHappen(sqlEx2);
                }
            }
            return buildResultSet;
        } catch (Throwable th) {
            if (results != null) {
                try {
                    results.close();
                } catch (SQLException sqlEx3) {
                    AssertionFailedException.shouldNotHappen(sqlEx3);
                }
            }
            if (stmt != null) {
                try {
                    stmt.close();
                } catch (SQLException sqlEx32) {
                    AssertionFailedException.shouldNotHappen(sqlEx32);
                }
            }
        }
    }

    public String getCatalogSeparator() throws SQLException {
        return ".";
    }

    public String getCatalogTerm() throws SQLException {
        return "database";
    }

    public ResultSet getColumnPrivileges(String catalog, String schema, String table, String columnNamePattern) throws SQLException {
        Throwable th;
        Throwable th2;
        DatabaseMetaData this;
        PreparedStatement pStmt;
        ResultSet results;
        DatabaseMetaData databaseMetaData = this;
        String table2 = table;
        Field[] fields = new Field[8];
        int i = 1;
        fields[0] = new Field("", "TABLE_CAT", 1, 64);
        fields[1] = new Field("", "TABLE_SCHEM", 1, 1);
        int i2 = 2;
        fields[2] = new Field("", "TABLE_NAME", 1, 64);
        int i3 = 3;
        fields[3] = new Field("", "COLUMN_NAME", 1, 64);
        int i4 = 4;
        fields[4] = new Field("", "GRANTOR", 1, 77);
        fields[5] = new Field("", "GRANTEE", 1, 77);
        int i5 = 6;
        fields[6] = new Field("", "PRIVILEGE", 1, 64);
        int i6 = 7;
        fields[7] = new Field("", "IS_GRANTABLE", 1, 3);
        String grantQuery = "SELECT c.host, c.db, t.grantor, c.user, c.table_name, c.column_name, c.column_priv FROM mysql.columns_priv c, mysql.tables_priv t WHERE c.host = t.host AND c.db = t.db AND c.table_name = t.table_name AND c.db LIKE ? AND c.table_name = ? AND c.column_name LIKE ?";
        PreparedStatement pStmt2 = null;
        ArrayList<ResultSetRow> grantRows = new ArrayList();
        String str;
        try {
            pStmt2 = prepareMetaDataSafeStatement(grantQuery);
            str = (catalog == null || catalog.length() == 0) ? "%" : catalog;
            pStmt2.setString(1, str);
            pStmt2.setString(2, table2);
            try {
                pStmt2.setString(3, columnNamePattern);
                ResultSet results2 = pStmt2.executeQuery();
                while (results2.next()) {
                    int i7;
                    int i8;
                    String host = results2.getString(i);
                    String db = results2.getString(i2);
                    String grantor = results2.getString(i3);
                    String user = results2.getString(i4);
                    if (user == null || user.length() == 0) {
                        user = "%";
                    }
                    StringBuilder fullUser = new StringBuilder(user);
                    String host2 = host;
                    if (host2 != null && databaseMetaData.conn.getUseHostsInPrivileges()) {
                        fullUser.append("@");
                        fullUser.append(host2);
                    }
                    String columnName = results2.getString(i5);
                    String allPrivileges = results2.getString(i6);
                    if (allPrivileges != null) {
                        String allPrivileges2 = allPrivileges.toUpperCase(Locale.ENGLISH);
                        StringTokenizer st = new StringTokenizer(allPrivileges2, ",");
                        while (st.hasMoreTokens()) {
                            String privilege = st.nextToken().trim();
                            String host3 = host2;
                            String allPrivileges3 = allPrivileges2;
                            byte[][] tuple = new byte[8][];
                            host2 = db;
                            tuple[0] = s2b(host2);
                            tuple[1] = null;
                            tuple[2] = s2b(table2);
                            tuple[3] = s2b(columnName);
                            String db2 = host2;
                            host2 = grantor;
                            if (host2 != null) {
                                tuple[4] = s2b(host2);
                            } else {
                                tuple[4] = null;
                            }
                            String grantor2 = host2;
                            tuple[5] = s2b(fullUser.toString());
                            tuple[6] = s2b(privilege);
                            tuple[7] = null;
                            grantRows.add(new ByteArrayRow(tuple, getExceptionInterceptor()));
                            host2 = host3;
                            allPrivileges2 = allPrivileges3;
                            db = db2;
                            grantor = grantor2;
                        }
                        i7 = 1;
                        i8 = 6;
                    } else {
                        i8 = i5;
                        i7 = 1;
                    }
                    i = i7;
                    i5 = i8;
                    i4 = 4;
                    i2 = 2;
                    i3 = 3;
                    i6 = 7;
                }
                DatabaseMetaData this2 = databaseMetaData;
                PreparedStatement pStmt3 = pStmt2;
                ResultSet results3 = results2;
                ArrayList<ResultSetRow> grantRows2 = grantRows;
                if (results3 != null) {
                    try {
                        results3.close();
                    } catch (Exception e) {
                    }
                }
                if (pStmt3 != null) {
                    try {
                        pStmt3.close();
                    } catch (Exception e2) {
                    }
                }
                return buildResultSet(fields, grantRows2);
            } catch (Throwable th3) {
                th = th3;
                th2 = th;
                this = databaseMetaData;
                pStmt = pStmt2;
                results = null;
                if (results != null) {
                    try {
                        results.close();
                    } catch (Exception e3) {
                    }
                }
                if (pStmt != null) {
                    try {
                        pStmt.close();
                    } catch (Exception e4) {
                    }
                }
                throw th2;
            }
        } catch (Throwable th4) {
            th = th4;
            str = columnNamePattern;
            th2 = th;
            this = databaseMetaData;
            pStmt = pStmt2;
            results = null;
            if (results != null) {
                results.close();
            }
            if (pStmt != null) {
                pStmt.close();
            }
            throw th2;
        }
    }

    public ResultSet getColumns(String catalog, String schemaPattern, String tableNamePattern, String columnNamePattern) throws SQLException {
        String columnNamePattern2;
        DatabaseMetaData databaseMetaData = this;
        if (columnNamePattern != null) {
            columnNamePattern2 = columnNamePattern;
        } else if (databaseMetaData.conn.getNullNamePatternMatchesAll()) {
            columnNamePattern2 = "%";
        } else {
            throw SQLError.createSQLException("Column name pattern can not be NULL or empty.", SQLError.SQL_STATE_ILLEGAL_ARGUMENT, getExceptionInterceptor());
        }
        final String colPattern = columnNamePattern2;
        Field[] fields = createColumnsFields();
        ArrayList<ResultSetRow> rows = new ArrayList();
        Statement stmt = databaseMetaData.conn.getMetadataSafeStatement();
        try {
            final String str = tableNamePattern;
            final String str2 = schemaPattern;
            final Statement statement = stmt;
            final ArrayList<ResultSetRow> arrayList = rows;
            new IterateBlock<String>(getCatalogIterator(catalog)) {
                void forEach(String catalogStr) throws SQLException {
                    ResultSet tables;
                    C04422 this;
                    Exception e;
                    ResultSet tables2;
                    Exception E;
                    int ordPos;
                    Integer realOrdinal;
                    String extra;
                    String catalogStr2 = catalogStr;
                    ArrayList<String> tableNameList = new ArrayList();
                    int i = 0;
                    ResultSet resultSet = null;
                    C04422 c04422;
                    if (str == null) {
                        tables = null;
                        try {
                            tables = DatabaseMetaData.this.getTables(catalogStr2, str2, "%", new String[0]);
                            while (tables.next()) {
                                tableNameList.add(tables.getString("TABLE_NAME"));
                            }
                            this = c04422;
                            if (tables != null) {
                                try {
                                    tables.close();
                                } catch (Exception e2) {
                                    AssertionFailedException.shouldNotHappen(e2);
                                }
                            }
                        } catch (Throwable th) {
                            tables2 = tables;
                            Throwable tables3 = th;
                            C04422 this2 = c04422;
                            if (tables2 != null) {
                                try {
                                    tables2.close();
                                } catch (Exception e22) {
                                    AssertionFailedException.shouldNotHappen(e22);
                                }
                            }
                        }
                    } else {
                        tables = null;
                        try {
                            tables = DatabaseMetaData.this.getTables(catalogStr2, str2, str, new String[0]);
                            while (tables.next()) {
                                tableNameList.add(tables.getString("TABLE_NAME"));
                            }
                            this = c04422;
                            if (tables != null) {
                                try {
                                    tables.close();
                                } catch (SQLException e3) {
                                    AssertionFailedException.shouldNotHappen(e3);
                                }
                            }
                        } catch (Throwable th2) {
                            Throwable th3 = th2;
                            if (tables != null) {
                                try {
                                    tables.close();
                                } catch (SQLException e32) {
                                    AssertionFailedException.shouldNotHappen(e32);
                                }
                            }
                        }
                    }
                    tables = tableNameList.iterator();
                    while (tables.hasNext()) {
                        String tableName = (String) tables.next();
                        ResultSet results = resultSet;
                        try {
                            int fullOrdinalPos;
                            StringBuilder queryBuf = new StringBuilder("SHOW ");
                            int i2 = 1;
                            if (DatabaseMetaData.this.conn.versionMeetsMinimum(4, 1, i)) {
                                queryBuf.append("FULL ");
                            }
                            queryBuf.append("COLUMNS FROM ");
                            queryBuf.append(StringUtils.quoteIdentifier(tableName, DatabaseMetaData.this.quotedId, DatabaseMetaData.this.conn.getPedantic()));
                            queryBuf.append(" FROM ");
                            queryBuf.append(StringUtils.quoteIdentifier(catalogStr2, DatabaseMetaData.this.quotedId, DatabaseMetaData.this.conn.getPedantic()));
                            queryBuf.append(" LIKE ");
                            queryBuf.append(StringUtils.quoteIdentifier(colPattern, "'", true));
                            boolean fixUpOrdinalsRequired = false;
                            Map<String, Integer> ordinalFixUpMap = null;
                            if (!colPattern.equals("%")) {
                                fixUpOrdinalsRequired = true;
                                StringBuilder fullColumnQueryBuf = new StringBuilder("SHOW ");
                                if (DatabaseMetaData.this.conn.versionMeetsMinimum(4, 1, i)) {
                                    fullColumnQueryBuf.append("FULL ");
                                }
                                fullColumnQueryBuf.append("COLUMNS FROM ");
                                fullColumnQueryBuf.append(StringUtils.quoteIdentifier(tableName, DatabaseMetaData.this.quotedId, DatabaseMetaData.this.conn.getPedantic()));
                                fullColumnQueryBuf.append(" FROM ");
                                fullColumnQueryBuf.append(StringUtils.quoteIdentifier(catalogStr2, DatabaseMetaData.this.quotedId, DatabaseMetaData.this.conn.getPedantic()));
                                results = statement.executeQuery(fullColumnQueryBuf.toString());
                                ordinalFixUpMap = new HashMap();
                                int fullOrdinalPos2 = 1;
                                while (results.next()) {
                                    fullOrdinalPos = fullOrdinalPos2 + 1;
                                    ordinalFixUpMap.put(results.getString("Field"), Integer.valueOf(fullOrdinalPos2));
                                    fullOrdinalPos2 = fullOrdinalPos;
                                }
                            }
                            results = statement.executeQuery(queryBuf.toString());
                            int ordPos2 = 1;
                            while (results.next()) {
                                byte[][] rowVal = new byte[24][];
                                rowVal[i] = DatabaseMetaData.this.s2b(catalogStr2);
                                rowVal[i2] = null;
                                rowVal[2] = DatabaseMetaData.this.s2b(tableName);
                                rowVal[3] = results.getBytes("Field");
                                StringBuilder queryBuf2 = queryBuf;
                                TypeDescriptor typeDesc = new TypeDescriptor(DatabaseMetaData.this, results.getString("Type"), results.getString("Null"));
                                rowVal[4] = Short.toString(typeDesc.dataType).getBytes();
                                rowVal[5] = DatabaseMetaData.this.s2b(typeDesc.typeName);
                                if (typeDesc.columnSize == null) {
                                    rowVal[6] = null;
                                } else {
                                    byte[] s2b;
                                    String collation = results.getString("Collation");
                                    fullOrdinalPos = 1;
                                    if (collation != null && ("TEXT".equals(typeDesc.typeName) || "TINYTEXT".equals(typeDesc.typeName) || "MEDIUMTEXT".equals(typeDesc.typeName))) {
                                        if (collation.indexOf("ucs2") <= -1) {
                                            if (collation.indexOf("utf16") <= -1) {
                                                if (collation.indexOf("utf32") > -1) {
                                                    fullOrdinalPos = 4;
                                                }
                                            }
                                        }
                                        fullOrdinalPos = 2;
                                    }
                                    i2 = fullOrdinalPos;
                                    if (i2 == 1) {
                                        s2b = DatabaseMetaData.this.s2b(typeDesc.columnSize.toString());
                                    } else {
                                        s2b = DatabaseMetaData.this.s2b(Integer.valueOf(typeDesc.columnSize.intValue() / i2).toString());
                                    }
                                    rowVal[6] = s2b;
                                }
                                rowVal[7] = DatabaseMetaData.this.s2b(Integer.toString(typeDesc.bufferLength));
                                rowVal[8] = typeDesc.decimalDigits == null ? null : DatabaseMetaData.this.s2b(typeDesc.decimalDigits.toString());
                                rowVal[9] = DatabaseMetaData.this.s2b(Integer.toString(typeDesc.numPrecRadix));
                                rowVal[10] = DatabaseMetaData.this.s2b(Integer.toString(typeDesc.nullability));
                                try {
                                    try {
                                        if (DatabaseMetaData.this.conn.versionMeetsMinimum(4, 1, 0)) {
                                            rowVal[11] = results.getBytes("Comment");
                                        } else {
                                            rowVal[11] = results.getBytes("Extra");
                                        }
                                    } catch (Exception e4) {
                                        e22 = e4;
                                        E = e22;
                                        rowVal[11] = new byte[0];
                                        rowVal[12] = results.getBytes("Default");
                                        rowVal[13] = new byte[]{(byte) 48};
                                        rowVal[14] = new byte[]{(byte) 48};
                                        if (StringUtils.indexOfIgnoreCase(typeDesc.typeName, "BINARY") == -1) {
                                            rowVal[15] = rowVal[6];
                                            if (fixUpOrdinalsRequired) {
                                                ordPos = ordPos2 + 1;
                                                rowVal[16] = Integer.toString(ordPos2).getBytes();
                                                ordPos2 = ordPos;
                                            } else {
                                                realOrdinal = (Integer) ordinalFixUpMap.get(results.getString("Field"));
                                                if (realOrdinal == null) {
                                                    rowVal[16] = realOrdinal.toString().getBytes();
                                                } else {
                                                    throw SQLError.createSQLException("Can not find column in full column list to determine true ordinal position.", SQLError.SQL_STATE_GENERAL_ERROR, DatabaseMetaData.this.getExceptionInterceptor());
                                                }
                                            }
                                            rowVal[17] = DatabaseMetaData.this.s2b(typeDesc.isNullable);
                                            rowVal[18] = null;
                                            rowVal[19] = null;
                                            rowVal[20] = null;
                                            rowVal[21] = null;
                                            rowVal[22] = DatabaseMetaData.this.s2b("");
                                            extra = results.getString("Extra");
                                            if (extra != null) {
                                                rowVal[22] = DatabaseMetaData.this.s2b(StringUtils.indexOfIgnoreCase(extra, "auto_increment") == -1 ? "YES" : "NO");
                                                rowVal[23] = DatabaseMetaData.this.s2b(StringUtils.indexOfIgnoreCase(extra, "generated") == -1 ? "YES" : "NO");
                                            }
                                            arrayList.add(new ByteArrayRow(rowVal, DatabaseMetaData.this.getExceptionInterceptor()));
                                            i = 0;
                                            queryBuf = queryBuf2;
                                            c04422 = this;
                                            i2 = 1;
                                        } else {
                                            rowVal[15] = null;
                                            if (fixUpOrdinalsRequired) {
                                                realOrdinal = (Integer) ordinalFixUpMap.get(results.getString("Field"));
                                                if (realOrdinal == null) {
                                                    throw SQLError.createSQLException("Can not find column in full column list to determine true ordinal position.", SQLError.SQL_STATE_GENERAL_ERROR, DatabaseMetaData.this.getExceptionInterceptor());
                                                }
                                                rowVal[16] = realOrdinal.toString().getBytes();
                                            } else {
                                                ordPos = ordPos2 + 1;
                                                rowVal[16] = Integer.toString(ordPos2).getBytes();
                                                ordPos2 = ordPos;
                                            }
                                            rowVal[17] = DatabaseMetaData.this.s2b(typeDesc.isNullable);
                                            rowVal[18] = null;
                                            rowVal[19] = null;
                                            rowVal[20] = null;
                                            rowVal[21] = null;
                                            rowVal[22] = DatabaseMetaData.this.s2b("");
                                            extra = results.getString("Extra");
                                            if (extra != null) {
                                                if (StringUtils.indexOfIgnoreCase(extra, "auto_increment") == -1) {
                                                }
                                                rowVal[22] = DatabaseMetaData.this.s2b(StringUtils.indexOfIgnoreCase(extra, "auto_increment") == -1 ? "YES" : "NO");
                                                if (StringUtils.indexOfIgnoreCase(extra, "generated") == -1) {
                                                }
                                                rowVal[23] = DatabaseMetaData.this.s2b(StringUtils.indexOfIgnoreCase(extra, "generated") == -1 ? "YES" : "NO");
                                            }
                                            arrayList.add(new ByteArrayRow(rowVal, DatabaseMetaData.this.getExceptionInterceptor()));
                                            i = 0;
                                            queryBuf = queryBuf2;
                                            c04422 = this;
                                            i2 = 1;
                                        }
                                    }
                                } catch (Exception e5) {
                                    e22 = e5;
                                    E = e22;
                                    rowVal[11] = new byte[0];
                                    rowVal[12] = results.getBytes("Default");
                                    rowVal[13] = new byte[]{(byte) 48};
                                    rowVal[14] = new byte[]{(byte) 48};
                                    if (StringUtils.indexOfIgnoreCase(typeDesc.typeName, "BINARY") == -1) {
                                        rowVal[15] = null;
                                        if (fixUpOrdinalsRequired) {
                                            ordPos = ordPos2 + 1;
                                            rowVal[16] = Integer.toString(ordPos2).getBytes();
                                            ordPos2 = ordPos;
                                        } else {
                                            realOrdinal = (Integer) ordinalFixUpMap.get(results.getString("Field"));
                                            if (realOrdinal == null) {
                                                rowVal[16] = realOrdinal.toString().getBytes();
                                            } else {
                                                throw SQLError.createSQLException("Can not find column in full column list to determine true ordinal position.", SQLError.SQL_STATE_GENERAL_ERROR, DatabaseMetaData.this.getExceptionInterceptor());
                                            }
                                        }
                                        rowVal[17] = DatabaseMetaData.this.s2b(typeDesc.isNullable);
                                        rowVal[18] = null;
                                        rowVal[19] = null;
                                        rowVal[20] = null;
                                        rowVal[21] = null;
                                        rowVal[22] = DatabaseMetaData.this.s2b("");
                                        extra = results.getString("Extra");
                                        if (extra != null) {
                                            if (StringUtils.indexOfIgnoreCase(extra, "auto_increment") == -1) {
                                            }
                                            rowVal[22] = DatabaseMetaData.this.s2b(StringUtils.indexOfIgnoreCase(extra, "auto_increment") == -1 ? "YES" : "NO");
                                            if (StringUtils.indexOfIgnoreCase(extra, "generated") == -1) {
                                            }
                                            rowVal[23] = DatabaseMetaData.this.s2b(StringUtils.indexOfIgnoreCase(extra, "generated") == -1 ? "YES" : "NO");
                                        }
                                        arrayList.add(new ByteArrayRow(rowVal, DatabaseMetaData.this.getExceptionInterceptor()));
                                        i = 0;
                                        queryBuf = queryBuf2;
                                        c04422 = this;
                                        i2 = 1;
                                    } else {
                                        rowVal[15] = rowVal[6];
                                        if (fixUpOrdinalsRequired) {
                                            realOrdinal = (Integer) ordinalFixUpMap.get(results.getString("Field"));
                                            if (realOrdinal == null) {
                                                throw SQLError.createSQLException("Can not find column in full column list to determine true ordinal position.", SQLError.SQL_STATE_GENERAL_ERROR, DatabaseMetaData.this.getExceptionInterceptor());
                                            }
                                            rowVal[16] = realOrdinal.toString().getBytes();
                                        } else {
                                            ordPos = ordPos2 + 1;
                                            rowVal[16] = Integer.toString(ordPos2).getBytes();
                                            ordPos2 = ordPos;
                                        }
                                        rowVal[17] = DatabaseMetaData.this.s2b(typeDesc.isNullable);
                                        rowVal[18] = null;
                                        rowVal[19] = null;
                                        rowVal[20] = null;
                                        rowVal[21] = null;
                                        rowVal[22] = DatabaseMetaData.this.s2b("");
                                        extra = results.getString("Extra");
                                        if (extra != null) {
                                            if (StringUtils.indexOfIgnoreCase(extra, "auto_increment") == -1) {
                                            }
                                            rowVal[22] = DatabaseMetaData.this.s2b(StringUtils.indexOfIgnoreCase(extra, "auto_increment") == -1 ? "YES" : "NO");
                                            if (StringUtils.indexOfIgnoreCase(extra, "generated") == -1) {
                                            }
                                            rowVal[23] = DatabaseMetaData.this.s2b(StringUtils.indexOfIgnoreCase(extra, "generated") == -1 ? "YES" : "NO");
                                        }
                                        arrayList.add(new ByteArrayRow(rowVal, DatabaseMetaData.this.getExceptionInterceptor()));
                                        i = 0;
                                        queryBuf = queryBuf2;
                                        c04422 = this;
                                        i2 = 1;
                                    }
                                }
                                rowVal[12] = results.getBytes("Default");
                                rowVal[13] = new byte[]{(byte) 48};
                                rowVal[14] = new byte[]{(byte) 48};
                                if (StringUtils.indexOfIgnoreCase(typeDesc.typeName, "CHAR") == -1 && StringUtils.indexOfIgnoreCase(typeDesc.typeName, "BLOB") == -1 && StringUtils.indexOfIgnoreCase(typeDesc.typeName, "TEXT") == -1) {
                                    if (StringUtils.indexOfIgnoreCase(typeDesc.typeName, "BINARY") == -1) {
                                        rowVal[15] = null;
                                        if (fixUpOrdinalsRequired) {
                                            ordPos = ordPos2 + 1;
                                            rowVal[16] = Integer.toString(ordPos2).getBytes();
                                            ordPos2 = ordPos;
                                        } else {
                                            realOrdinal = (Integer) ordinalFixUpMap.get(results.getString("Field"));
                                            if (realOrdinal == null) {
                                                rowVal[16] = realOrdinal.toString().getBytes();
                                            } else {
                                                throw SQLError.createSQLException("Can not find column in full column list to determine true ordinal position.", SQLError.SQL_STATE_GENERAL_ERROR, DatabaseMetaData.this.getExceptionInterceptor());
                                            }
                                        }
                                        rowVal[17] = DatabaseMetaData.this.s2b(typeDesc.isNullable);
                                        rowVal[18] = null;
                                        rowVal[19] = null;
                                        rowVal[20] = null;
                                        rowVal[21] = null;
                                        rowVal[22] = DatabaseMetaData.this.s2b("");
                                        extra = results.getString("Extra");
                                        if (extra != null) {
                                            if (StringUtils.indexOfIgnoreCase(extra, "auto_increment") == -1) {
                                            }
                                            rowVal[22] = DatabaseMetaData.this.s2b(StringUtils.indexOfIgnoreCase(extra, "auto_increment") == -1 ? "YES" : "NO");
                                            if (StringUtils.indexOfIgnoreCase(extra, "generated") == -1) {
                                            }
                                            rowVal[23] = DatabaseMetaData.this.s2b(StringUtils.indexOfIgnoreCase(extra, "generated") == -1 ? "YES" : "NO");
                                        }
                                        arrayList.add(new ByteArrayRow(rowVal, DatabaseMetaData.this.getExceptionInterceptor()));
                                        i = 0;
                                        queryBuf = queryBuf2;
                                        c04422 = this;
                                        i2 = 1;
                                    }
                                }
                                rowVal[15] = rowVal[6];
                                if (fixUpOrdinalsRequired) {
                                    realOrdinal = (Integer) ordinalFixUpMap.get(results.getString("Field"));
                                    if (realOrdinal == null) {
                                        throw SQLError.createSQLException("Can not find column in full column list to determine true ordinal position.", SQLError.SQL_STATE_GENERAL_ERROR, DatabaseMetaData.this.getExceptionInterceptor());
                                    }
                                    rowVal[16] = realOrdinal.toString().getBytes();
                                } else {
                                    ordPos = ordPos2 + 1;
                                    rowVal[16] = Integer.toString(ordPos2).getBytes();
                                    ordPos2 = ordPos;
                                }
                                rowVal[17] = DatabaseMetaData.this.s2b(typeDesc.isNullable);
                                rowVal[18] = null;
                                rowVal[19] = null;
                                rowVal[20] = null;
                                rowVal[21] = null;
                                rowVal[22] = DatabaseMetaData.this.s2b("");
                                extra = results.getString("Extra");
                                if (extra != null) {
                                    if (StringUtils.indexOfIgnoreCase(extra, "auto_increment") == -1) {
                                    }
                                    rowVal[22] = DatabaseMetaData.this.s2b(StringUtils.indexOfIgnoreCase(extra, "auto_increment") == -1 ? "YES" : "NO");
                                    if (StringUtils.indexOfIgnoreCase(extra, "generated") == -1) {
                                    }
                                    rowVal[23] = DatabaseMetaData.this.s2b(StringUtils.indexOfIgnoreCase(extra, "generated") == -1 ? "YES" : "NO");
                                }
                                arrayList.add(new ByteArrayRow(rowVal, DatabaseMetaData.this.getExceptionInterceptor()));
                                i = 0;
                                queryBuf = queryBuf2;
                                c04422 = this;
                                i2 = 1;
                            }
                            fullOrdinalPos = i;
                            ResultSet results2 = results;
                            if (results2 != null) {
                                try {
                                    results2.close();
                                } catch (Exception e6) {
                                }
                            }
                            i = fullOrdinalPos;
                            c04422 = this;
                            resultSet = null;
                        } catch (Throwable th22) {
                            th3 = th22;
                            tables2 = results;
                            if (tables2 != null) {
                                try {
                                    tables2.close();
                                } catch (Exception e7) {
                                }
                            }
                        }
                    }
                }
            }.doForAll();
            String str3 = columnNamePattern2;
            if (stmt != null) {
                stmt.close();
            }
            return buildResultSet(fields, rows);
        } catch (Throwable th) {
            Throwable th2 = th;
            DatabaseMetaData this = databaseMetaData;
            Statement stmt2 = stmt;
            if (stmt2 != null) {
                stmt2.close();
            }
        }
    }

    protected Field[] createColumnsFields() {
        return new Field[]{new Field("", "TABLE_CAT", 1, 255), new Field("", "TABLE_SCHEM", 1, 0), new Field("", "TABLE_NAME", 1, 255), new Field("", "COLUMN_NAME", 1, 32), new Field("", "DATA_TYPE", 4, 5), new Field("", "TYPE_NAME", 1, 16), new Field("", "COLUMN_SIZE", 4, Integer.toString(ActivityChooserViewAdapter.MAX_ACTIVITY_COUNT_UNLIMITED).length()), new Field("", "BUFFER_LENGTH", 4, 10), new Field("", "DECIMAL_DIGITS", 4, 10), new Field("", "NUM_PREC_RADIX", 4, 10), new Field("", "NULLABLE", 4, 10), new Field("", "REMARKS", 1, 0), new Field("", "COLUMN_DEF", 1, 0), new Field("", "SQL_DATA_TYPE", 4, 10), new Field("", "SQL_DATETIME_SUB", 4, 10), new Field("", "CHAR_OCTET_LENGTH", 4, Integer.toString(ActivityChooserViewAdapter.MAX_ACTIVITY_COUNT_UNLIMITED).length()), new Field("", "ORDINAL_POSITION", 4, 10), new Field("", "IS_NULLABLE", 1, 3), new Field("", "SCOPE_CATALOG", 1, 255), new Field("", "SCOPE_SCHEMA", 1, 255), new Field("", "SCOPE_TABLE", 1, 255), new Field("", "SOURCE_DATA_TYPE", 5, 10), new Field("", "IS_AUTOINCREMENT", 1, 3), new Field("", "IS_GENERATEDCOLUMN", 1, 3)};
    }

    public Connection getConnection() throws SQLException {
        return this.conn;
    }

    public ResultSet getCrossReference(String primaryCatalog, String primarySchema, String primaryTable, String foreignCatalog, String foreignSchema, String foreignTable) throws SQLException {
        Statement stmt;
        Throwable th;
        Statement stmt2;
        DatabaseMetaData this;
        DatabaseMetaData databaseMetaData = this;
        if (primaryTable == null) {
            throw SQLError.createSQLException("Table not specified.", SQLError.SQL_STATE_ILLEGAL_ARGUMENT, getExceptionInterceptor());
        }
        Field[] fields;
        Field[] fields2 = createFkMetadataFields();
        ArrayList<ResultSetRow> tuples = new ArrayList();
        if (databaseMetaData.conn.versionMeetsMinimum(3, 23, 0)) {
            Statement stmt3 = databaseMetaData.conn.getMetadataSafeStatement();
            try {
                String str = foreignCatalog;
                IteratorWithCleanup catalogIterator = getCatalogIterator(str);
                C04433 c04433 = c04433;
                final Statement statement = stmt3;
                final String str2 = foreignTable;
                final String str3 = primaryTable;
                final String str4 = str;
                final String str5 = foreignSchema;
                str = primaryCatalog;
                fields = fields2;
                C04433 c044332 = c04433;
                final String str6 = primarySchema;
                stmt = stmt3;
                final ArrayList<ResultSetRow> stmt4 = tuples;
                try {
                    c04433 = new IterateBlock<String>(catalogIterator) {
                        void forEach(String catalogStr) throws SQLException {
                            String catalogStr2 = catalogStr;
                            String str = null;
                            ResultSet fkresults = null;
                            try {
                                if (DatabaseMetaData.this.conn.versionMeetsMinimum(3, 23, 50)) {
                                    fkresults = DatabaseMetaData.this.extractForeignKeyFromCreateTable(catalogStr2, null);
                                } else {
                                    StringBuilder queryBuf = new StringBuilder("SHOW TABLE STATUS FROM ");
                                    queryBuf.append(StringUtils.quoteIdentifier(catalogStr2, DatabaseMetaData.this.quotedId, DatabaseMetaData.this.conn.getPedantic()));
                                    fkresults = statement.executeQuery(queryBuf.toString());
                                }
                                String foreignTableWithCase = DatabaseMetaData.this.getTableNameWithCase(str2);
                                String primaryTableWithCase = DatabaseMetaData.this.getTableNameWithCase(str3);
                                while (fkresults.next()) {
                                    String str2;
                                    String foreignTableWithCase2;
                                    String tableType = fkresults.getString("Type");
                                    if (tableType != null) {
                                        if (!tableType.equalsIgnoreCase("innodb")) {
                                            if (!tableType.equalsIgnoreCase(DatabaseMetaData.SUPPORTS_FK)) {
                                                str2 = str;
                                                foreignTableWithCase2 = foreignTableWithCase;
                                                str = str2;
                                                foreignTableWithCase = foreignTableWithCase2;
                                            }
                                        }
                                        String comment = fkresults.getString("Comment").trim();
                                        if (comment != null) {
                                            StringTokenizer commentTokens = new StringTokenizer(comment, ";", false);
                                            if (commentTokens.hasMoreTokens()) {
                                                commentTokens.nextToken();
                                            }
                                            while (commentTokens.hasMoreTokens()) {
                                                String tableType2;
                                                String comment2;
                                                String keys = commentTokens.nextToken();
                                                LocalAndReferencedColumns parsedInfo = DatabaseMetaData.this.parseTableStatusIntoLocalAndReferencedColumns(keys);
                                                int keySeq = 0;
                                                Iterator<String> referencedColumns = parsedInfo.referencedColumnsList.iterator();
                                                for (String referencingColumn : parsedInfo.localColumnsList) {
                                                    byte[] bArr;
                                                    String referencingColumn2 = StringUtils.unQuoteIdentifier(referencingColumn2, DatabaseMetaData.this.quotedId);
                                                    byte[][] tuple = new byte[14][];
                                                    tableType2 = tableType;
                                                    if (str4 == null) {
                                                        comment2 = comment;
                                                        bArr = null;
                                                    } else {
                                                        comment2 = comment;
                                                        bArr = DatabaseMetaData.this.s2b(str4);
                                                    }
                                                    tuple[4] = bArr;
                                                    tuple[5] = str5 == null ? null : DatabaseMetaData.this.s2b(str5);
                                                    tableType = fkresults.getString("Name");
                                                    if (tableType.compareTo(foreignTableWithCase) != 0) {
                                                        foreignTableWithCase2 = foreignTableWithCase;
                                                    } else {
                                                        tuple[6] = DatabaseMetaData.this.s2b(tableType);
                                                        foreignTableWithCase2 = foreignTableWithCase;
                                                        tuple[7] = DatabaseMetaData.this.s2b(referencingColumn2);
                                                        tuple[0] = str == null ? null : DatabaseMetaData.this.s2b(str);
                                                        tuple[1] = str6 == null ? null : DatabaseMetaData.this.s2b(str6);
                                                        if (parsedInfo.referencedTable.compareTo(primaryTableWithCase) == 0) {
                                                            tuple[2] = DatabaseMetaData.this.s2b(parsedInfo.referencedTable);
                                                            Iterator<String> referencedColumns2 = referencedColumns;
                                                            tuple[3] = DatabaseMetaData.this.s2b(StringUtils.unQuoteIdentifier((String) referencedColumns.next(), DatabaseMetaData.this.quotedId));
                                                            tuple[8] = Integer.toString(keySeq).getBytes();
                                                            int[] actions = DatabaseMetaData.this.getForeignKeyActions(keys);
                                                            tuple[9] = Integer.toString(actions[1]).getBytes();
                                                            tuple[10] = Integer.toString(actions[0]).getBytes();
                                                            tuple[11] = null;
                                                            tuple[12] = null;
                                                            tuple[13] = Integer.toString(7).getBytes();
                                                            stmt4.add(new ByteArrayRow(tuple, DatabaseMetaData.this.getExceptionInterceptor()));
                                                            keySeq++;
                                                            tableType = tableType2;
                                                            comment = comment2;
                                                            foreignTableWithCase = foreignTableWithCase2;
                                                            referencedColumns = referencedColumns2;
                                                        }
                                                    }
                                                    tableType = tableType2;
                                                    comment = comment2;
                                                    foreignTableWithCase = foreignTableWithCase2;
                                                }
                                                tableType2 = tableType;
                                                comment2 = comment;
                                                str = null;
                                            }
                                        }
                                    }
                                    str2 = str;
                                    foreignTableWithCase2 = foreignTableWithCase;
                                    str = str2;
                                    foreignTableWithCase = foreignTableWithCase2;
                                }
                                C04433 this = r1;
                                if (fkresults != null) {
                                    try {
                                        fkresults.close();
                                    } catch (Exception e) {
                                        AssertionFailedException.shouldNotHappen(e);
                                    }
                                }
                            } catch (Throwable th) {
                                Throwable th2 = th;
                                C04433 this2 = r1;
                                if (fkresults != null) {
                                    try {
                                        fkresults.close();
                                    } catch (Exception e2) {
                                        AssertionFailedException.shouldNotHappen(e2);
                                    }
                                }
                                throw th2;
                            }
                        }
                    };
                    c044332.doForAll();
                    if (stmt != null) {
                        stmt.close();
                    }
                } catch (Throwable th2) {
                    th = th2;
                    stmt2 = stmt;
                    this = databaseMetaData;
                    if (stmt2 != null) {
                        stmt2.close();
                    }
                    throw th;
                }
            } catch (Throwable th22) {
                stmt = stmt3;
                fields = fields2;
                th = th22;
                stmt2 = stmt;
                this = databaseMetaData;
                if (stmt2 != null) {
                    stmt2.close();
                }
                throw th;
            }
        }
        fields = fields2;
        return buildResultSet(fields, tuples);
    }

    protected Field[] createFkMetadataFields() {
        return new Field[]{new Field("", "PKTABLE_CAT", 1, 255), new Field("", "PKTABLE_SCHEM", 1, 0), new Field("", "PKTABLE_NAME", 1, 255), new Field("", "PKCOLUMN_NAME", 1, 32), new Field("", "FKTABLE_CAT", 1, 255), new Field("", "FKTABLE_SCHEM", 1, 0), new Field("", "FKTABLE_NAME", 1, 255), new Field("", "FKCOLUMN_NAME", 1, 32), new Field("", "KEY_SEQ", 5, 2), new Field("", "UPDATE_RULE", 5, 2), new Field("", "DELETE_RULE", 5, 2), new Field("", "FK_NAME", 1, 0), new Field("", "PK_NAME", 1, 0), new Field("", "DEFERRABILITY", 5, 2)};
    }

    public int getDatabaseMajorVersion() throws SQLException {
        return this.conn.getServerMajorVersion();
    }

    public int getDatabaseMinorVersion() throws SQLException {
        return this.conn.getServerMinorVersion();
    }

    public String getDatabaseProductName() throws SQLException {
        return "MySQL";
    }

    public String getDatabaseProductVersion() throws SQLException {
        return this.conn.getServerVersion();
    }

    public int getDefaultTransactionIsolation() throws SQLException {
        if (this.conn.supportsIsolationLevel()) {
            return 2;
        }
        return 0;
    }

    public int getDriverMajorVersion() {
        return NonRegisteringDriver.getMajorVersionInternal();
    }

    public int getDriverMinorVersion() {
        return NonRegisteringDriver.getMinorVersionInternal();
    }

    public String getDriverName() throws SQLException {
        return NonRegisteringDriver.NAME;
    }

    public String getDriverVersion() throws SQLException {
        return "mysql-connector-java-5.1.45 ( Revision: 9131eefa398531c7dc98776e8a3fe839e544c5b2 )";
    }

    public ResultSet getExportedKeys(String catalog, String schema, String table) throws SQLException {
        if (table == null) {
            throw SQLError.createSQLException("Table not specified.", SQLError.SQL_STATE_ILLEGAL_ARGUMENT, getExceptionInterceptor());
        }
        Field[] fields = createFkMetadataFields();
        ArrayList<ResultSetRow> rows = new ArrayList();
        if (this.conn.versionMeetsMinimum(3, 23, 0)) {
            Statement stmt = this.conn.getMetadataSafeStatement();
            try {
                final Statement statement = stmt;
                final String str = table;
                final ArrayList<ResultSetRow> arrayList = rows;
                new IterateBlock<String>(getCatalogIterator(catalog)) {
                    void forEach(String catalogStr) throws SQLException {
                        ResultSet fkresults = null;
                        try {
                            ResultSet extractForeignKeyFromCreateTable;
                            if (DatabaseMetaData.this.conn.versionMeetsMinimum(3, 23, 50)) {
                                extractForeignKeyFromCreateTable = DatabaseMetaData.this.extractForeignKeyFromCreateTable(catalogStr, null);
                            } else {
                                StringBuilder queryBuf = new StringBuilder("SHOW TABLE STATUS FROM ");
                                queryBuf.append(StringUtils.quoteIdentifier(catalogStr, DatabaseMetaData.this.quotedId, DatabaseMetaData.this.conn.getPedantic()));
                                extractForeignKeyFromCreateTable = statement.executeQuery(queryBuf.toString());
                            }
                            fkresults = extractForeignKeyFromCreateTable;
                            String tableNameWithCase = DatabaseMetaData.this.getTableNameWithCase(str);
                            while (fkresults.next()) {
                                String tableType = fkresults.getString("Type");
                                if (tableType != null && (tableType.equalsIgnoreCase("innodb") || tableType.equalsIgnoreCase(DatabaseMetaData.SUPPORTS_FK))) {
                                    String comment = fkresults.getString("Comment").trim();
                                    if (comment != null) {
                                        StringTokenizer commentTokens = new StringTokenizer(comment, ";", false);
                                        if (commentTokens.hasMoreTokens()) {
                                            commentTokens.nextToken();
                                            while (commentTokens.hasMoreTokens()) {
                                                String keys = commentTokens.nextToken();
                                                DatabaseMetaData.this.getExportKeyResults(catalogStr, tableNameWithCase, keys, arrayList, fkresults.getString("Name"));
                                            }
                                        }
                                    }
                                }
                            }
                            if (fkresults != null) {
                                try {
                                    fkresults.close();
                                } catch (SQLException sqlEx) {
                                    AssertionFailedException.shouldNotHappen(sqlEx);
                                }
                            }
                        } catch (Throwable th) {
                            if (fkresults != null) {
                                try {
                                    fkresults.close();
                                } catch (SQLException sqlEx2) {
                                    AssertionFailedException.shouldNotHappen(sqlEx2);
                                }
                            }
                        }
                    }
                }.doForAll();
                if (stmt != null) {
                    stmt.close();
                }
            } catch (Throwable th) {
                Statement stmt2 = stmt;
                if (stmt2 != null) {
                    stmt2.close();
                }
            }
        }
        return buildResultSet(fields, rows);
    }

    protected void getExportKeyResults(String catalog, String exportingTable, String keysComment, List<ResultSetRow> tuples, String fkTableName) throws SQLException {
        getResultsImpl(catalog, exportingTable, keysComment, tuples, fkTableName, true);
    }

    public String getExtraNameCharacters() throws SQLException {
        return "#@";
    }

    protected int[] getForeignKeyActions(String commentString) {
        int[] actions = new int[]{3, 3};
        int lastParenIndex = commentString.lastIndexOf(")");
        if (lastParenIndex != commentString.length() - 1) {
            String cascadeOptions = commentString.substring(lastParenIndex + 1).trim().toUpperCase(Locale.ENGLISH);
            actions[0] = getCascadeDeleteOption(cascadeOptions);
            actions[1] = getCascadeUpdateOption(cascadeOptions);
        }
        return actions;
    }

    public String getIdentifierQuoteString() throws SQLException {
        if (!this.conn.supportsQuotedIdentifiers()) {
            return " ";
        }
        return this.conn.useAnsiQuotedIdentifiers() ? "\"" : "`";
    }

    public ResultSet getImportedKeys(String catalog, String schema, String table) throws SQLException {
        if (table == null) {
            throw SQLError.createSQLException("Table not specified.", SQLError.SQL_STATE_ILLEGAL_ARGUMENT, getExceptionInterceptor());
        }
        Field[] fields = createFkMetadataFields();
        ArrayList<ResultSetRow> rows = new ArrayList();
        if (this.conn.versionMeetsMinimum(3, 23, 0)) {
            Statement stmt = this.conn.getMetadataSafeStatement();
            try {
                final String str = table;
                final Statement statement = stmt;
                final ArrayList<ResultSetRow> arrayList = rows;
                new IterateBlock<String>(getCatalogIterator(catalog)) {
                    void forEach(String catalogStr) throws SQLException {
                        ResultSet fkresults = null;
                        try {
                            if (DatabaseMetaData.this.conn.versionMeetsMinimum(3, 23, 50)) {
                                fkresults = DatabaseMetaData.this.extractForeignKeyFromCreateTable(catalogStr, str);
                            } else {
                                StringBuilder queryBuf = new StringBuilder("SHOW TABLE STATUS ");
                                queryBuf.append(" FROM ");
                                queryBuf.append(StringUtils.quoteIdentifier(catalogStr, DatabaseMetaData.this.quotedId, DatabaseMetaData.this.conn.getPedantic()));
                                queryBuf.append(" LIKE ");
                                queryBuf.append(StringUtils.quoteIdentifier(str, "'", true));
                                fkresults = statement.executeQuery(queryBuf.toString());
                            }
                            while (fkresults.next()) {
                                String tableType = fkresults.getString("Type");
                                if (tableType != null && (tableType.equalsIgnoreCase("innodb") || tableType.equalsIgnoreCase(DatabaseMetaData.SUPPORTS_FK))) {
                                    String comment = fkresults.getString("Comment").trim();
                                    if (comment != null) {
                                        StringTokenizer commentTokens = new StringTokenizer(comment, ";", false);
                                        if (commentTokens.hasMoreTokens()) {
                                            commentTokens.nextToken();
                                            while (commentTokens.hasMoreTokens()) {
                                                DatabaseMetaData.this.getImportKeyResults(catalogStr, str, commentTokens.nextToken(), arrayList);
                                            }
                                        }
                                    }
                                }
                            }
                            if (fkresults != null) {
                                try {
                                    fkresults.close();
                                } catch (SQLException sqlEx) {
                                    AssertionFailedException.shouldNotHappen(sqlEx);
                                }
                            }
                        } catch (Throwable th) {
                            if (fkresults != null) {
                                try {
                                    fkresults.close();
                                } catch (SQLException sqlEx2) {
                                    AssertionFailedException.shouldNotHappen(sqlEx2);
                                }
                            }
                        }
                    }
                }.doForAll();
                if (stmt != null) {
                    stmt.close();
                }
            } catch (Throwable th) {
                Statement stmt2 = stmt;
                if (stmt2 != null) {
                    stmt2.close();
                }
            }
        }
        return buildResultSet(fields, rows);
    }

    protected void getImportKeyResults(String catalog, String importingTable, String keysComment, List<ResultSetRow> tuples) throws SQLException {
        getResultsImpl(catalog, importingTable, keysComment, tuples, null, false);
    }

    public ResultSet getIndexInfo(String catalog, String schema, String table, boolean unique, boolean approximate) throws SQLException {
        Field[] fields = createIndexInfoFields();
        SortedMap sortedRows = new TreeMap();
        ArrayList<ResultSetRow> rows = new ArrayList();
        Statement stmt = this.conn.getMetadataSafeStatement();
        try {
            final String str = table;
            final Statement statement = stmt;
            final boolean z = unique;
            final SortedMap sortedMap = sortedRows;
            new IterateBlock<String>(getCatalogIterator(catalog)) {
                void forEach(String catalogStr) throws SQLException {
                    C04466 c04466 = this;
                    String catalogStr2 = catalogStr;
                    ResultSet results = null;
                    try {
                        StringBuilder queryBuf = new StringBuilder("SHOW INDEX FROM ");
                        queryBuf.append(StringUtils.quoteIdentifier(str, DatabaseMetaData.this.quotedId, DatabaseMetaData.this.conn.getPedantic()));
                        queryBuf.append(" FROM ");
                        queryBuf.append(StringUtils.quoteIdentifier(catalogStr2, DatabaseMetaData.this.quotedId, DatabaseMetaData.this.conn.getPedantic()));
                        results = statement.executeQuery(queryBuf.toString());
                    } catch (SQLException e) {
                        SQLException sqlEx;
                        sqlEx = e;
                        int errorCode = sqlEx.getErrorCode();
                        if (!(SQLError.SQL_STATE_BASE_TABLE_OR_VIEW_NOT_FOUND.equals(sqlEx.getSQLState()) || errorCode == MysqlErrorNumbers.ER_NO_SUCH_TABLE)) {
                            throw sqlEx;
                        }
                    } catch (Throwable th) {
                        Throwable th2 = th;
                        C04466 this = c04466;
                        if (results != null) {
                            try {
                                results.close();
                            } catch (Exception e2) {
                            }
                        }
                    }
                    while (results != null && results.next()) {
                        DatabaseMetaData databaseMetaData;
                        String str;
                        byte[][] row = new byte[14][];
                        boolean z = false;
                        row[0] = catalogStr2 == null ? new byte[0] : DatabaseMetaData.this.s2b(catalogStr2);
                        row[1] = null;
                        row[2] = results.getBytes("Table");
                        boolean indexIsUnique = results.getInt("Non_unique") == 0;
                        if (indexIsUnique) {
                            databaseMetaData = DatabaseMetaData.this;
                            str = "false";
                        } else {
                            databaseMetaData = DatabaseMetaData.this;
                            str = "true";
                        }
                        row[3] = databaseMetaData.s2b(str);
                        row[4] = new byte[0];
                        row[5] = results.getBytes("Key_name");
                        row[6] = Integer.toString(3).getBytes();
                        row[7] = results.getBytes("Seq_in_index");
                        row[8] = results.getBytes("Column_name");
                        row[9] = results.getBytes("Collation");
                        long cardinality = results.getLong("Cardinality");
                        if (!Util.isJdbc42() && cardinality > 2147483647L) {
                            cardinality = 2147483647L;
                        }
                        long cardinality2 = cardinality;
                        row[10] = DatabaseMetaData.this.s2b(String.valueOf(cardinality2));
                        row[11] = DatabaseMetaData.this.s2b("0");
                        row[12] = null;
                        DatabaseMetaData databaseMetaData2 = DatabaseMetaData.this;
                        if (!indexIsUnique) {
                            z = true;
                        }
                        IndexMetaDataKey indexInfoKey = new IndexMetaDataKey(z, (short) 3, results.getString("Key_name").toLowerCase(), results.getShort("Seq_in_index"));
                        if (!z) {
                            sortedMap.put(indexInfoKey, new ByteArrayRow(row, DatabaseMetaData.this.getExceptionInterceptor()));
                        } else if (indexIsUnique) {
                            sortedMap.put(indexInfoKey, new ByteArrayRow(row, DatabaseMetaData.this.getExceptionInterceptor()));
                        }
                    }
                    C04466 this2 = c04466;
                    if (results != null) {
                        try {
                            results.close();
                        } catch (Exception e3) {
                        }
                    }
                }
            }.doForAll();
            for (Object add : sortedRows.values()) {
                rows.add(add);
            }
            ResultSet indexInfo = buildResultSet(fields, rows);
            if (stmt != null) {
                stmt.close();
            }
            return indexInfo;
        } catch (Throwable th) {
            Throwable th2 = th;
            DatabaseMetaData this = r8;
            if (stmt != null) {
                stmt.close();
            }
        }
    }

    protected Field[] createIndexInfoFields() {
        Field[] fields = new Field[13];
        fields[0] = new Field("", "TABLE_CAT", 1, 255);
        fields[1] = new Field("", "TABLE_SCHEM", 1, 0);
        fields[2] = new Field("", "TABLE_NAME", 1, 255);
        fields[3] = new Field("", "NON_UNIQUE", 16, 4);
        fields[4] = new Field("", "INDEX_QUALIFIER", 1, 1);
        fields[5] = new Field("", "INDEX_NAME", 1, 32);
        fields[6] = new Field("", "TYPE", 5, 32);
        fields[7] = new Field("", "ORDINAL_POSITION", 5, 5);
        fields[8] = new Field("", "COLUMN_NAME", 1, 32);
        fields[9] = new Field("", "ASC_OR_DESC", 1, 1);
        if (Util.isJdbc42()) {
            fields[10] = new Field("", "CARDINALITY", -5, 20);
            fields[11] = new Field("", "PAGES", -5, 20);
        } else {
            fields[10] = new Field("", "CARDINALITY", 4, 20);
            fields[11] = new Field("", "PAGES", 4, 10);
        }
        fields[12] = new Field("", "FILTER_CONDITION", 1, 32);
        return fields;
    }

    public int getJDBCMajorVersion() throws SQLException {
        return 4;
    }

    public int getJDBCMinorVersion() throws SQLException {
        return 0;
    }

    public int getMaxBinaryLiteralLength() throws SQLException {
        return 16777208;
    }

    public int getMaxCatalogNameLength() throws SQLException {
        return 32;
    }

    public int getMaxCharLiteralLength() throws SQLException {
        return 16777208;
    }

    public int getMaxColumnNameLength() throws SQLException {
        return 64;
    }

    public int getMaxColumnsInGroupBy() throws SQLException {
        return 64;
    }

    public int getMaxColumnsInIndex() throws SQLException {
        return 16;
    }

    public int getMaxColumnsInOrderBy() throws SQLException {
        return 64;
    }

    public int getMaxColumnsInSelect() throws SQLException {
        return 256;
    }

    public int getMaxColumnsInTable() throws SQLException {
        return 512;
    }

    public int getMaxConnections() throws SQLException {
        return 0;
    }

    public int getMaxCursorNameLength() throws SQLException {
        return 64;
    }

    public int getMaxIndexLength() throws SQLException {
        return 256;
    }

    public int getMaxProcedureNameLength() throws SQLException {
        return 0;
    }

    public int getMaxRowSize() throws SQLException {
        return 2147483639;
    }

    public int getMaxSchemaNameLength() throws SQLException {
        return 0;
    }

    public int getMaxStatementLength() throws SQLException {
        return MysqlIO.getMaxBuf() - 4;
    }

    public int getMaxStatements() throws SQLException {
        return 0;
    }

    public int getMaxTableNameLength() throws SQLException {
        return 64;
    }

    public int getMaxTablesInSelect() throws SQLException {
        return 256;
    }

    public int getMaxUserNameLength() throws SQLException {
        return 16;
    }

    public String getNumericFunctions() throws SQLException {
        return "ABS,ACOS,ASIN,ATAN,ATAN2,BIT_COUNT,CEILING,COS,COT,DEGREES,EXP,FLOOR,LOG,LOG10,MAX,MIN,MOD,PI,POW,POWER,RADIANS,RAND,ROUND,SIN,SQRT,TAN,TRUNCATE";
    }

    public ResultSet getPrimaryKeys(String catalog, String schema, String table) throws SQLException {
        Field[] fields = new Field[]{new Field("", "TABLE_CAT", 1, 255), new Field("", "TABLE_SCHEM", 1, 0), new Field("", "TABLE_NAME", 1, 255), new Field("", "COLUMN_NAME", 1, 32), new Field("", "KEY_SEQ", 5, 5), new Field("", "PK_NAME", 1, 32)};
        if (table == null) {
            throw SQLError.createSQLException("Table not specified.", SQLError.SQL_STATE_ILLEGAL_ARGUMENT, getExceptionInterceptor());
        }
        ArrayList<ResultSetRow> rows = new ArrayList();
        Statement stmt = this.conn.getMetadataSafeStatement();
        try {
            final String str = table;
            final Statement statement = stmt;
            final ArrayList<ResultSetRow> arrayList = rows;
            new IterateBlock<String>(getCatalogIterator(catalog)) {
                void forEach(String catalogStr) throws SQLException {
                    ResultSet rs = null;
                    try {
                        StringBuilder queryBuf = new StringBuilder("SHOW KEYS FROM ");
                        queryBuf.append(StringUtils.quoteIdentifier(str, DatabaseMetaData.this.quotedId, DatabaseMetaData.this.conn.getPedantic()));
                        queryBuf.append(" FROM ");
                        queryBuf.append(StringUtils.quoteIdentifier(catalogStr, DatabaseMetaData.this.quotedId, DatabaseMetaData.this.conn.getPedantic()));
                        rs = statement.executeQuery(queryBuf.toString());
                        TreeMap<String, byte[][]> sortMap = new TreeMap();
                        while (rs.next()) {
                            String keyType = rs.getString("Key_name");
                            if (keyType != null && (keyType.equalsIgnoreCase("PRIMARY") || keyType.equalsIgnoreCase("PRI"))) {
                                byte[][] tuple = new byte[6][];
                                tuple[0] = catalogStr == null ? new byte[0] : DatabaseMetaData.this.s2b(catalogStr);
                                tuple[1] = null;
                                tuple[2] = DatabaseMetaData.this.s2b(str);
                                String columnName = rs.getString("Column_name");
                                tuple[3] = DatabaseMetaData.this.s2b(columnName);
                                tuple[4] = DatabaseMetaData.this.s2b(rs.getString("Seq_in_index"));
                                tuple[5] = DatabaseMetaData.this.s2b(keyType);
                                sortMap.put(columnName, tuple);
                            }
                        }
                        for (byte[][] byteArrayRow : sortMap.values()) {
                            arrayList.add(new ByteArrayRow(byteArrayRow, DatabaseMetaData.this.getExceptionInterceptor()));
                        }
                        if (rs != null) {
                            try {
                                rs.close();
                            } catch (Exception e) {
                            }
                        }
                    } catch (Throwable th) {
                        if (rs != null) {
                            try {
                                rs.close();
                            } catch (Exception e2) {
                            }
                        }
                    }
                }
            }.doForAll();
            if (stmt != null) {
                stmt.close();
            }
            return buildResultSet(fields, rows);
        } catch (Throwable th) {
            Statement stmt2 = stmt;
            if (stmt2 != null) {
                stmt2.close();
            }
        }
    }

    public ResultSet getProcedureColumns(String catalog, String schemaPattern, String procedureNamePattern, String columnNamePattern) throws SQLException {
        return getProcedureOrFunctionColumns(createProcedureColumnsFields(), catalog, schemaPattern, procedureNamePattern, columnNamePattern, true, true);
    }

    protected Field[] createProcedureColumnsFields() {
        return new Field[]{new Field("", "PROCEDURE_CAT", 1, 512), new Field("", "PROCEDURE_SCHEM", 1, 512), new Field("", "PROCEDURE_NAME", 1, 512), new Field("", "COLUMN_NAME", 1, 512), new Field("", "COLUMN_TYPE", 1, 64), new Field("", "DATA_TYPE", 5, 6), new Field("", "TYPE_NAME", 1, 64), new Field("", "PRECISION", 4, 12), new Field("", "LENGTH", 4, 12), new Field("", "SCALE", 5, 12), new Field("", "RADIX", 5, 6), new Field("", "NULLABLE", 5, 6), new Field("", "REMARKS", 1, 512), new Field("", "COLUMN_DEF", 1, 512), new Field("", "SQL_DATA_TYPE", 4, 12), new Field("", "SQL_DATETIME_SUB", 4, 12), new Field("", "CHAR_OCTET_LENGTH", 4, 12), new Field("", "ORDINAL_POSITION", 4, 12), new Field("", "IS_NULLABLE", 1, 512), new Field("", "SPECIFIC_NAME", 1, 512)};
    }

    protected ResultSet getProcedureOrFunctionColumns(Field[] fields, String catalog, String schemaPattern, String procedureOrFunctionNamePattern, String columnNamePattern, boolean returnProcedures, boolean returnFunctions) throws SQLException {
        String tmpCatalog;
        String str;
        String schemaPattern2;
        String columnNamePattern2;
        DatabaseMetaData this;
        Field[] fields2;
        DatabaseMetaData databaseMetaData = this;
        String str2 = procedureOrFunctionNamePattern;
        ArrayList procsOrFuncsToExtractList = new ArrayList();
        ResultSet procsAndOrFuncsRs = null;
        String str3;
        if (supportsStoredProcedures()) {
            String procedureOrFunctionNamePattern2;
            String tmpProcedureOrFunctionNamePattern = null;
            if (str2 != null) {
                try {
                    if (!str2.equals("%")) {
                        tmpProcedureOrFunctionNamePattern = StringUtils.sanitizeProcOrFuncName(procedureOrFunctionNamePattern);
                    }
                } catch (Throwable th) {
                    Throwable th2 = th;
                    DatabaseMetaData this2 = databaseMetaData;
                    procedureOrFunctionNamePattern2 = str2;
                    SQLException rethrowSqlEx = null;
                    if (procsAndOrFuncsRs != null) {
                        try {
                            procsAndOrFuncsRs.close();
                        } catch (SQLException e) {
                            rethrowSqlEx = e;
                        }
                    }
                    if (rethrowSqlEx != null) {
                    }
                }
            }
            if (tmpProcedureOrFunctionNamePattern == null) {
                tmpProcedureOrFunctionNamePattern = str2;
            } else {
                List<String> parseList = StringUtils.splitDBdotName(tmpProcedureOrFunctionNamePattern, catalog, databaseMetaData.quotedId, databaseMetaData.conn.isNoBackslashEscapesSet());
                if (parseList.size() == 2) {
                    tmpCatalog = (String) parseList.get(0);
                    tmpProcedureOrFunctionNamePattern = (String) parseList.get(1);
                }
            }
            procsAndOrFuncsRs = getProceduresAndOrFunctions(createFieldMetadataForGetProcedures(), catalog, schemaPattern, tmpProcedureOrFunctionNamePattern, returnProcedures, returnFunctions);
            boolean hasResults = false;
            while (procsAndOrFuncsRs.next()) {
                procsOrFuncsToExtractList.add(new ComparableWrapper(getFullyQualifiedName(procsAndOrFuncsRs.getString(1), procsAndOrFuncsRs.getString(3)), procsAndOrFuncsRs.getShort(8) == (short) 1 ? ProcedureType.PROCEDURE : ProcedureType.FUNCTION));
                hasResults = true;
            }
            if (hasResults) {
                Collections.sort(procsOrFuncsToExtractList);
            }
            DatabaseMetaData this3 = databaseMetaData;
            Field[] fields3 = fields;
            str = catalog;
            schemaPattern2 = schemaPattern;
            String procedureOrFunctionNamePattern3 = str2;
            procedureOrFunctionNamePattern2 = columnNamePattern;
            boolean returnProcedures2 = returnProcedures;
            boolean returnFunctions2 = returnFunctions;
            SQLException rethrowSqlEx2 = null;
            if (procsAndOrFuncsRs != null) {
                try {
                    procsAndOrFuncsRs.close();
                } catch (SQLException e2) {
                    rethrowSqlEx2 = e2;
                }
            }
            if (rethrowSqlEx2 != null) {
                throw rethrowSqlEx2;
            }
            String str4 = schemaPattern2;
            str3 = procedureOrFunctionNamePattern3;
            columnNamePattern2 = procedureOrFunctionNamePattern2;
            boolean z = returnProcedures2;
            boolean z2 = returnFunctions2;
            ResultSet resultSet = procsAndOrFuncsRs;
            this = this3;
            fields2 = fields3;
        } else {
            str = catalog;
            columnNamePattern2 = columnNamePattern;
            str3 = str2;
            fields2 = fields;
            this = databaseMetaData;
        }
        ArrayList<ResultSetRow> resultRows = new ArrayList();
        int idx = 0;
        tmpCatalog = "";
        Iterator i$ = procsOrFuncsToExtractList.iterator();
        while (true) {
            Iterator i$2 = i$;
            if (i$2.hasNext()) {
                String catalog2;
                String procNameToCall;
                ComparableWrapper<String, ProcedureType> procOrFunc = (ComparableWrapper) i$2.next();
                schemaPattern2 = (String) procOrFunc.getKey();
                ProcedureType procType = (ProcedureType) procOrFunc.getValue();
                if (" ".equals(this.quotedId)) {
                    idx = schemaPattern2.indexOf(".");
                } else {
                    idx = StringUtils.indexOfIgnoreCase(0, schemaPattern2, ".", this.quotedId, this.quotedId, (Set) this.conn.isNoBackslashEscapesSet() != 0 ? StringUtils.SEARCH_MODE__MRK_COM_WS : StringUtils.SEARCH_MODE__ALL);
                }
                int idx2 = idx;
                if (idx2 > 0) {
                    catalog2 = StringUtils.unQuoteIdentifier(schemaPattern2.substring(0, idx2), this.quotedId);
                    procNameToCall = schemaPattern2;
                } else {
                    procNameToCall = schemaPattern2;
                    catalog2 = str;
                }
                Iterator i$3 = i$2;
                ArrayList<ResultSetRow> resultRows2 = resultRows;
                getCallStmtParameterTypes(catalog2, procNameToCall, procType, columnNamePattern2, resultRows, fields2.length == 17 ? true : null);
                resultRows = resultRows2;
                idx = idx2;
                str = catalog2;
                tmpCatalog = procNameToCall;
                i$ = i$3;
                databaseMetaData = this;
            } else {
                int i = idx;
                return buildResultSet(fields2, resultRows);
            }
        }
    }

    public ResultSet getProcedures(String catalog, String schemaPattern, String procedureNamePattern) throws SQLException {
        return getProceduresAndOrFunctions(createFieldMetadataForGetProcedures(), catalog, schemaPattern, procedureNamePattern, true, true);
    }

    protected Field[] createFieldMetadataForGetProcedures() {
        return new Field[]{new Field("", "PROCEDURE_CAT", 1, 255), new Field("", "PROCEDURE_SCHEM", 1, 255), new Field("", "PROCEDURE_NAME", 1, 255), new Field("", "reserved1", 1, 0), new Field("", "reserved2", 1, 0), new Field("", "reserved3", 1, 0), new Field("", "REMARKS", 1, 255), new Field("", "PROCEDURE_TYPE", 5, 6), new Field("", "SPECIFIC_NAME", 1, 255)};
    }

    protected ResultSet getProceduresAndOrFunctions(Field[] fields, String catalog, String schemaPattern, String procedureNamePattern, boolean returnProcedures, boolean returnFunctions) throws SQLException {
        String procedureNamePattern2;
        ArrayList<ResultSetRow> procedureRows;
        DatabaseMetaData databaseMetaData = this;
        if (procedureNamePattern != null) {
            if (procedureNamePattern.length() != 0) {
                procedureNamePattern2 = procedureNamePattern;
                procedureRows = new ArrayList();
                if (supportsStoredProcedures()) {
                    String str = catalog;
                } else {
                    final String procNamePattern = procedureNamePattern2;
                    List<ComparableWrapper<String, ResultSetRow>> procedureRowsToSort = new ArrayList();
                    final boolean z = returnProcedures;
                    final boolean z2 = returnFunctions;
                    final List list = procedureRowsToSort;
                    final Field[] fieldArr = fields;
                    new IterateBlock<String>(getCatalogIterator(catalog)) {
                        /* JADX WARNING: inconsistent code. */
                        /* Code decompiled incorrectly, please refer to instructions dump. */
                        void forEach(java.lang.String r26) throws java.sql.SQLException {
                            /*
                            r25 = this;
                            r1 = r25;
                            r2 = r26;
                            r3 = 0;
                            r4 = 1;
                            r5 = new java.lang.StringBuilder;
                            r5.<init>();
                            r6 = "SELECT name, type, comment FROM mysql.proc WHERE ";
                            r5.append(r6);
                            r6 = r3;
                            if (r6 == 0) goto L_0x001e;
                        L_0x0014:
                            r6 = r4;
                            if (r6 != 0) goto L_0x001e;
                        L_0x0018:
                            r6 = "type = 'PROCEDURE' AND ";
                            r5.append(r6);
                            goto L_0x002b;
                        L_0x001e:
                            r6 = r3;
                            if (r6 != 0) goto L_0x002b;
                        L_0x0022:
                            r6 = r4;
                            if (r6 == 0) goto L_0x002b;
                        L_0x0026:
                            r6 = "type = 'FUNCTION' AND ";
                            r5.append(r6);
                        L_0x002b:
                            r6 = "name LIKE ? AND db <=> ? ORDER BY name, type";
                            r5.append(r6);
                            r6 = com.mysql.jdbc.DatabaseMetaData.this;
                            r7 = r5.toString();
                            r6 = r6.prepareMetaDataSafeStatement(r7);
                            r7 = 2;
                            if (r2 == 0) goto L_0x0056;
                        L_0x003d:
                            r8 = com.mysql.jdbc.DatabaseMetaData.this;	 Catch:{ all -> 0x0050 }
                            r8 = r8.conn;	 Catch:{ all -> 0x0050 }
                            r8 = r8.lowerCaseTableNames();	 Catch:{ all -> 0x0050 }
                            if (r8 == 0) goto L_0x004c;
                        L_0x0047:
                            r8 = r2.toLowerCase();	 Catch:{ all -> 0x0050 }
                            r2 = r8;
                        L_0x004c:
                            r6.setString(r7, r2);	 Catch:{ all -> 0x0050 }
                            goto L_0x005b;
                        L_0x0050:
                            r0 = move-exception;
                            r12 = r3;
                        L_0x0052:
                            r3 = r2;
                        L_0x0053:
                            r2 = r0;
                            goto L_0x013a;
                        L_0x0056:
                            r8 = 12;
                            r6.setNull(r7, r8);	 Catch:{ all -> 0x0050 }
                        L_0x005b:
                            r8 = 1;
                            r9 = r5;	 Catch:{ all -> 0x0050 }
                            r15 = 1;
                            r6.setString(r15, r9);	 Catch:{ all -> 0x0050 }
                            r12 = r6.executeQuery();	 Catch:{ SQLException -> 0x00a2 }
                            r3 = 0;
                            r4 = r3;	 Catch:{ SQLException -> 0x009e, all -> 0x009b }
                            if (r4 == 0) goto L_0x007b;
                        L_0x006b:
                            r9 = com.mysql.jdbc.DatabaseMetaData.this;	 Catch:{ SQLException -> 0x009e, all -> 0x009b }
                            r10 = 1;
                            r4 = r6;	 Catch:{ SQLException -> 0x009e, all -> 0x009b }
                            r11 = r2;
                            r13 = r3;
                            r14 = r2;
                            r7 = r15;
                            r15 = r4;
                            r16 = r8;
                            r9.convertToJdbcProcedureList(r10, r11, r12, r13, r14, r15, r16);	 Catch:{ SQLException -> 0x0099, all -> 0x009b }
                            goto L_0x007c;
                        L_0x007b:
                            r7 = r15;
                        L_0x007c:
                            r4 = r4;	 Catch:{ SQLException -> 0x0099, all -> 0x009b }
                            if (r4 == 0) goto L_0x0095;
                        L_0x0080:
                            r13 = com.mysql.jdbc.DatabaseMetaData.this;	 Catch:{ SQLException -> 0x0099, all -> 0x009b }
                            r4 = r6;	 Catch:{ SQLException -> 0x0099, all -> 0x009b }
                            r9 = r7;	 Catch:{ SQLException -> 0x0099, all -> 0x009b }
                            r14 = r2;
                            r15 = r12;
                            r16 = r3;
                            r17 = r2;
                            r18 = r4;
                            r19 = r8;
                            r20 = r9;
                            r13.convertToJdbcFunctionList(r14, r15, r16, r17, r18, r19, r20);	 Catch:{ SQLException -> 0x0099, all -> 0x009b }
                            r4 = r3;
                            goto L_0x011a;
                        L_0x0099:
                            r0 = move-exception;
                            goto L_0x00a0;
                        L_0x009b:
                            r0 = move-exception;
                            r4 = r3;
                            goto L_0x0052;
                        L_0x009e:
                            r0 = move-exception;
                            r7 = r15;
                        L_0x00a0:
                            r4 = r3;
                            goto L_0x00a5;
                        L_0x00a2:
                            r0 = move-exception;
                            r7 = r15;
                            r12 = r3;
                        L_0x00a5:
                            r3 = r0;
                            r9 = com.mysql.jdbc.DatabaseMetaData.this;	 Catch:{ all -> 0x0137 }
                            r9 = r9.conn;	 Catch:{ all -> 0x0137 }
                            r10 = 5;
                            r11 = 0;
                            r9 = r9.versionMeetsMinimum(r10, r11, r7);	 Catch:{ all -> 0x0137 }
                            if (r9 == 0) goto L_0x00b4;
                        L_0x00b2:
                            r15 = 2;
                            goto L_0x00b5;
                        L_0x00b4:
                            r15 = r7;
                        L_0x00b5:
                            r8 = r4;	 Catch:{ all -> 0x0137 }
                            if (r8 == 0) goto L_0x00e3;
                        L_0x00b9:
                            r6.close();	 Catch:{ all -> 0x0137 }
                            r8 = com.mysql.jdbc.DatabaseMetaData.this;	 Catch:{ all -> 0x0137 }
                            r9 = "SHOW FUNCTION STATUS LIKE ?";
                            r8 = r8.prepareMetaDataSafeStatement(r9);	 Catch:{ all -> 0x0137 }
                            r6 = r8;
                            r8 = r5;	 Catch:{ all -> 0x0137 }
                            r6.setString(r7, r8);	 Catch:{ all -> 0x0137 }
                            r11 = r6.executeQuery();	 Catch:{ all -> 0x0137 }
                            r9 = com.mysql.jdbc.DatabaseMetaData.this;	 Catch:{ all -> 0x00de }
                            r14 = r6;	 Catch:{ all -> 0x00de }
                            r8 = r7;	 Catch:{ all -> 0x00de }
                            r10 = r2;
                            r12 = r4;
                            r13 = r2;
                            r16 = r8;
                            r9.convertToJdbcFunctionList(r10, r11, r12, r13, r14, r15, r16);	 Catch:{ all -> 0x00de }
                            r12 = r11;
                            goto L_0x00e3;
                        L_0x00de:
                            r0 = move-exception;
                            r3 = r2;
                            r12 = r11;
                            goto L_0x0053;
                        L_0x00e3:
                            r8 = r3;	 Catch:{ all -> 0x0137 }
                            if (r8 == 0) goto L_0x011a;
                        L_0x00e7:
                            r6.close();	 Catch:{ all -> 0x0137 }
                            r8 = com.mysql.jdbc.DatabaseMetaData.this;	 Catch:{ all -> 0x0137 }
                            r9 = "SHOW PROCEDURE STATUS LIKE ?";
                            r8 = r8.prepareMetaDataSafeStatement(r9);	 Catch:{ all -> 0x0137 }
                            r6 = r8;
                            r8 = r5;	 Catch:{ all -> 0x0137 }
                            r6.setString(r7, r8);	 Catch:{ all -> 0x0137 }
                            r19 = r6.executeQuery();	 Catch:{ all -> 0x0137 }
                            r7 = com.mysql.jdbc.DatabaseMetaData.this;	 Catch:{ all -> 0x0114 }
                            r17 = 0;
                            r8 = r6;	 Catch:{ all -> 0x0114 }
                            r16 = r7;
                            r18 = r2;
                            r20 = r4;
                            r21 = r2;
                            r22 = r8;
                            r23 = r15;
                            r16.convertToJdbcProcedureList(r17, r18, r19, r20, r21, r22, r23);	 Catch:{ all -> 0x0114 }
                            r12 = r19;
                            goto L_0x011a;
                        L_0x0114:
                            r0 = move-exception;
                            r3 = r2;
                            r12 = r19;
                            goto L_0x0053;
                            r3 = r1;
                            r7 = r26;
                            r8 = r12;
                            r9 = 0;
                            if (r8 == 0) goto L_0x0129;
                        L_0x0122:
                            r8.close();	 Catch:{ SQLException -> 0x0126 }
                            goto L_0x0129;
                        L_0x0126:
                            r0 = move-exception;
                            r10 = r0;
                            r9 = r10;
                        L_0x0129:
                            if (r6 == 0) goto L_0x0132;
                        L_0x012b:
                            r6.close();	 Catch:{ SQLException -> 0x012f }
                            goto L_0x0132;
                        L_0x012f:
                            r0 = move-exception;
                            r10 = r0;
                            r9 = r10;
                        L_0x0132:
                            if (r9 == 0) goto L_0x0135;
                        L_0x0134:
                            throw r9;
                            return;
                        L_0x0137:
                            r0 = move-exception;
                            goto L_0x0052;
                        L_0x013a:
                            r7 = r1;
                            r8 = r26;
                            r9 = r12;
                            r10 = 0;
                            if (r9 == 0) goto L_0x0148;
                        L_0x0141:
                            r9.close();	 Catch:{ SQLException -> 0x0145 }
                            goto L_0x0148;
                        L_0x0145:
                            r0 = move-exception;
                            r11 = r0;
                            r10 = r11;
                        L_0x0148:
                            if (r6 == 0) goto L_0x0151;
                        L_0x014a:
                            r6.close();	 Catch:{ SQLException -> 0x014e }
                            goto L_0x0151;
                        L_0x014e:
                            r0 = move-exception;
                            r11 = r0;
                            r10 = r11;
                        L_0x0151:
                            if (r10 == 0) goto L_0x0154;
                        L_0x0153:
                            throw r10;
                            throw r2;
                            */
                            throw new UnsupportedOperationException("Method not decompiled: com.mysql.jdbc.DatabaseMetaData.8.forEach(java.lang.String):void");
                        }
                    }.doForAll();
                    Collections.sort(procedureRowsToSort);
                    for (ComparableWrapper<String, ResultSetRow> procRow : procedureRowsToSort) {
                        procedureRows.add(procRow.getValue());
                    }
                }
                return buildResultSet(fields, procedureRows);
            }
        }
        if (databaseMetaData.conn.getNullNamePatternMatchesAll()) {
            procedureNamePattern2 = "%";
            procedureRows = new ArrayList();
            if (supportsStoredProcedures()) {
                String str2 = catalog;
            } else {
                final String procNamePattern2 = procedureNamePattern2;
                List<ComparableWrapper<String, ResultSetRow>> procedureRowsToSort2 = new ArrayList();
                final boolean z3 = returnProcedures;
                final boolean z22 = returnFunctions;
                final List list2 = procedureRowsToSort2;
                final Field[] fieldArr2 = fields;
                /* anonymous class already generated */.doForAll();
                Collections.sort(procedureRowsToSort2);
                while (i$.hasNext()) {
                    procedureRows.add(procRow.getValue());
                }
            }
            return buildResultSet(fields, procedureRows);
        }
        Field[] fieldArr3 = fields;
        str2 = catalog;
        throw SQLError.createSQLException("Procedure name pattern can not be NULL or empty.", SQLError.SQL_STATE_ILLEGAL_ARGUMENT, getExceptionInterceptor());
    }

    public String getProcedureTerm() throws SQLException {
        return "PROCEDURE";
    }

    public int getResultSetHoldability() throws SQLException {
        return 1;
    }

    private void getResultsImpl(String catalog, String table, String keysComment, List<ResultSetRow> tuples, String fkTableName, boolean isExport) throws SQLException {
        String str = keysComment;
        LocalAndReferencedColumns parsedInfo = parseTableStatusIntoLocalAndReferencedColumns(str);
        String str2;
        if (isExport) {
            str2 = table;
            if (!parsedInfo.referencedTable.equals(str2)) {
                return;
            }
        }
        str2 = table;
        if (parsedInfo.localColumnsList.size() != parsedInfo.referencedColumnsList.size()) {
            throw SQLError.createSQLException("Error parsing foreign keys definition, number of local and referenced columns is not the same.", SQLError.SQL_STATE_GENERAL_ERROR, getExceptionInterceptor());
        }
        Iterator<String> referColumnNames = parsedInfo.referencedColumnsList.iterator();
        int keySeqIndex = 1;
        for (String lColumnName : parsedInfo.localColumnsList) {
            byte[][] tuple = new byte[14][];
            String lColumnName2 = StringUtils.unQuoteIdentifier(lColumnName2, r0.quotedId);
            String rColumnName = StringUtils.unQuoteIdentifier((String) referColumnNames.next(), r0.quotedId);
            tuple[4] = catalog == null ? new byte[0] : s2b(catalog);
            tuple[5] = null;
            tuple[6] = s2b(isExport ? fkTableName : str2);
            tuple[7] = s2b(lColumnName2);
            tuple[0] = s2b(parsedInfo.referencedCatalog);
            tuple[1] = null;
            tuple[2] = s2b(isExport ? str2 : parsedInfo.referencedTable);
            tuple[3] = s2b(rColumnName);
            int keySeqIndex2 = keySeqIndex + 1;
            tuple[8] = s2b(Integer.toString(keySeqIndex));
            int[] actions = getForeignKeyActions(str);
            tuple[9] = s2b(Integer.toString(actions[1]));
            tuple[10] = s2b(Integer.toString(actions[0]));
            tuple[11] = s2b(parsedInfo.constraintName);
            tuple[12] = null;
            tuple[13] = s2b(Integer.toString(7));
            tuples.add(new ByteArrayRow(tuple, getExceptionInterceptor()));
            keySeqIndex = keySeqIndex2;
        }
        List<ResultSetRow> list = tuples;
    }

    public ResultSet getSchemas() throws SQLException {
        return buildResultSet(new Field[]{new Field("", "TABLE_SCHEM", 1, 0), new Field("", "TABLE_CATALOG", 1, 0)}, new ArrayList());
    }

    public String getSchemaTerm() throws SQLException {
        return "";
    }

    public String getSearchStringEscape() throws SQLException {
        return "\\";
    }

    public String getSQLKeywords() throws SQLException {
        if (mysqlKeywords != null) {
            return mysqlKeywords;
        }
        synchronized (DatabaseMetaData.class) {
            if (mysqlKeywords != null) {
                String str = mysqlKeywords;
                return str;
            }
            Set<String> mysqlKeywordSet = new TreeSet();
            StringBuilder mysqlKeywordsBuffer = new StringBuilder();
            Collections.addAll(mysqlKeywordSet, MYSQL_KEYWORDS);
            mysqlKeywordSet.removeAll(Arrays.asList(Util.isJdbc4() ? SQL2003_KEYWORDS : SQL92_KEYWORDS));
            for (String keyword : mysqlKeywordSet) {
                mysqlKeywordsBuffer.append(",");
                mysqlKeywordsBuffer.append(keyword);
            }
            mysqlKeywords = mysqlKeywordsBuffer.substring(1);
            String str2 = mysqlKeywords;
            return str2;
        }
    }

    public int getSQLStateType() throws SQLException {
        return (this.conn.versionMeetsMinimum(4, 1, 0) || this.conn.getUseSqlStateCodes()) ? 2 : 1;
    }

    public String getStringFunctions() throws SQLException {
        return "ASCII,BIN,BIT_LENGTH,CHAR,CHARACTER_LENGTH,CHAR_LENGTH,CONCAT,CONCAT_WS,CONV,ELT,EXPORT_SET,FIELD,FIND_IN_SET,HEX,INSERT,INSTR,LCASE,LEFT,LENGTH,LOAD_FILE,LOCATE,LOCATE,LOWER,LPAD,LTRIM,MAKE_SET,MATCH,MID,OCT,OCTET_LENGTH,ORD,POSITION,QUOTE,REPEAT,REPLACE,REVERSE,RIGHT,RPAD,RTRIM,SOUNDEX,SPACE,STRCMP,SUBSTRING,SUBSTRING,SUBSTRING,SUBSTRING,SUBSTRING_INDEX,TRIM,UCASE,UPPER";
    }

    public ResultSet getSuperTables(String arg0, String arg1, String arg2) throws SQLException {
        return buildResultSet(new Field[]{new Field("", "TABLE_CAT", 1, 32), new Field("", "TABLE_SCHEM", 1, 32), new Field("", "TABLE_NAME", 1, 32), new Field("", "SUPERTABLE_NAME", 1, 32)}, new ArrayList());
    }

    public ResultSet getSuperTypes(String arg0, String arg1, String arg2) throws SQLException {
        return buildResultSet(new Field[]{new Field("", "TYPE_CAT", 1, 32), new Field("", "TYPE_SCHEM", 1, 32), new Field("", "TYPE_NAME", 1, 32), new Field("", "SUPERTYPE_CAT", 1, 32), new Field("", "SUPERTYPE_SCHEM", 1, 32), new Field("", "SUPERTYPE_NAME", 1, 32)}, new ArrayList());
    }

    public String getSystemFunctions() throws SQLException {
        return "DATABASE,USER,SYSTEM_USER,SESSION_USER,PASSWORD,ENCRYPT,LAST_INSERT_ID,VERSION";
    }

    protected String getTableNameWithCase(String table) {
        return this.conn.lowerCaseTableNames() ? table.toLowerCase() : table;
    }

    public ResultSet getTablePrivileges(String catalog, String schemaPattern, String tableNamePattern) throws SQLException {
        String tableNamePattern2;
        ResultSet columnResults;
        Throwable th;
        ArrayList<ResultSetRow> arrayList;
        PreparedStatement preparedStatement;
        ResultSet results;
        PreparedStatement pStmt;
        String pStmt2;
        String tableNamePattern3;
        Field[] fields;
        DatabaseMetaData databaseMetaData = this;
        if (tableNamePattern != null) {
            tableNamePattern2 = tableNamePattern;
        } else if (databaseMetaData.conn.getNullNamePatternMatchesAll()) {
            tableNamePattern2 = "%";
        } else {
            throw SQLError.createSQLException("Table name pattern can not be NULL or empty.", SQLError.SQL_STATE_ILLEGAL_ARGUMENT, getExceptionInterceptor());
        }
        Field[] fields2 = new Field[7];
        String catalog2 = true;
        fields2[0] = new Field("", "TABLE_CAT", 1, 64);
        fields2[1] = new Field("", "TABLE_SCHEM", 1, 1);
        int i = 2;
        fields2[2] = new Field("", "TABLE_NAME", 1, 64);
        int i2 = 3;
        fields2[3] = new Field("", "GRANTOR", 1, 77);
        int i3 = 4;
        fields2[4] = new Field("", "GRANTEE", 1, 77);
        int i4 = 5;
        fields2[5] = new Field("", "PRIVILEGE", 1, 64);
        int i5 = 6;
        fields2[6] = new Field("", "IS_GRANTABLE", 1, 3);
        String grantQuery = "SELECT host,db,table_name,grantor,user,table_priv FROM mysql.tables_priv WHERE db LIKE ? AND table_name LIKE ?";
        ResultSet results2 = null;
        ArrayList<ResultSetRow> grantRows = new ArrayList();
        String str = null;
        PreparedStatement pStmt3 = null;
        String catalog3;
        PreparedStatement pStmt4;
        Field[] fields3;
        ArrayList<ResultSetRow> pStmt5;
        String grantRows2;
        Throwable th2;
        try {
            String str2;
            String schemaPattern2;
            DatabaseMetaData this;
            String host;
            String db;
            String table;
            String grantor;
            String user;
            StringBuilder fullUser;
            String host2;
            String allPrivileges;
            StringTokenizer st;
            ArrayList<ResultSetRow> grantRows3;
            DatabaseMetaData this2;
            PreparedStatement pStmt6;
            ResultSet results3;
            String schemaPattern3;
            ResultSet columnResults2;
            Field[] fields4;
            String schemaPattern4;
            String tableNamePattern4;
            ArrayList<ResultSetRow> schemaPattern5;
            PreparedStatement tableNamePattern5;
            Field[] fields5;
            Object grantRows4;
            String str3;
            int i6;
            int i7;
            int i8;
            int i9;
            int i10;
            ResultSet results4;
            ArrayList<ResultSetRow> grantRows5;
            PreparedStatement pStmt7;
            pStmt3 = prepareMetaDataSafeStatement(grantQuery);
            if (catalog != null) {
                try {
                    if (catalog.length() != 0) {
                        str2 = catalog;
                        pStmt3.setString(1, str2);
                        pStmt3.setString(2, tableNamePattern2);
                        catalog3 = catalog;
                        results2 = pStmt3.executeQuery();
                        pStmt4 = pStmt3;
                        fields3 = fields2;
                        pStmt5 = grantRows;
                        schemaPattern2 = schemaPattern;
                        grantRows2 = grantQuery;
                        grantQuery = tableNamePattern2;
                        this = databaseMetaData;
                        while (results2.next()) {
                            try {
                                host = results2.getString(catalog2);
                                db = results2.getString(i);
                                table = results2.getString(i2);
                                grantor = results2.getString(i3);
                                user = results2.getString(i4);
                                if (user == null || user.length() == 0) {
                                    user = "%";
                                }
                                fullUser = new StringBuilder(user);
                                host2 = host;
                                if (host2 != null && this.conn.getUseHostsInPrivileges()) {
                                    fullUser.append("@");
                                    fullUser.append(host2);
                                }
                                allPrivileges = results2.getString(i5);
                                if (allPrivileges == null) {
                                    st = new StringTokenizer(allPrivileges.toUpperCase(Locale.ENGLISH), ",");
                                    grantRows3 = pStmt5;
                                    catalog2 = catalog3;
                                    pStmt3 = fields3;
                                    catalog3 = grantRows2;
                                    this2 = this;
                                    grantRows = grantQuery;
                                    tableNamePattern2 = db;
                                    grantQuery = grantor;
                                    pStmt6 = pStmt4;
                                    results3 = results2;
                                    schemaPattern3 = schemaPattern2;
                                    schemaPattern2 = table;
                                    while (st.hasMoreTokens()) {
                                        try {
                                            str = st.nextToken().trim();
                                            columnResults2 = null;
                                            try {
                                                columnResults = getColumns(catalog2, schemaPattern3, schemaPattern2, "%");
                                                while (columnResults.next()) {
                                                    try {
                                                        fields4 = pStmt3;
                                                        pStmt3 = new byte[8][];
                                                        pStmt3[0] = s2b(tableNamePattern2);
                                                        pStmt3[1] = null;
                                                        pStmt3[2] = s2b(schemaPattern2);
                                                        if (grantQuery == null) {
                                                            try {
                                                                pStmt3[3] = s2b(grantQuery);
                                                            } catch (Throwable th3) {
                                                                th = th3;
                                                                columnResults2 = columnResults;
                                                                schemaPattern4 = schemaPattern3;
                                                                tableNamePattern4 = grantRows;
                                                                schemaPattern3 = grantRows3;
                                                            }
                                                        } else {
                                                            try {
                                                                pStmt3[3] = null;
                                                            } catch (Throwable th4) {
                                                                schemaPattern4 = schemaPattern3;
                                                                tableNamePattern4 = grantRows;
                                                                schemaPattern3 = grantRows3;
                                                                columnResults2 = columnResults;
                                                                th2 = th4;
                                                            }
                                                        }
                                                        tableNamePattern4 = grantRows;
                                                        try {
                                                            pStmt3[4] = s2b(fullUser.toString());
                                                            pStmt3[5] = s2b(str);
                                                            pStmt3[6] = null;
                                                            schemaPattern4 = schemaPattern3;
                                                            try {
                                                                schemaPattern5 = grantRows3;
                                                                try {
                                                                    schemaPattern5.add(new ByteArrayRow(pStmt3, getExceptionInterceptor()));
                                                                    grantRows3 = schemaPattern5;
                                                                    pStmt3 = fields4;
                                                                    grantRows = tableNamePattern4;
                                                                    schemaPattern3 = schemaPattern4;
                                                                } catch (Throwable th5) {
                                                                    th4 = th5;
                                                                    columnResults2 = columnResults;
                                                                }
                                                            } catch (Throwable th42) {
                                                                schemaPattern3 = grantRows3;
                                                                columnResults2 = columnResults;
                                                                th2 = th42;
                                                            }
                                                        } catch (Throwable th422) {
                                                            schemaPattern4 = schemaPattern3;
                                                            schemaPattern3 = grantRows3;
                                                            columnResults2 = columnResults;
                                                            th2 = th422;
                                                        }
                                                    } catch (Throwable th6) {
                                                        th422 = th6;
                                                        schemaPattern4 = schemaPattern3;
                                                        tableNamePattern4 = grantRows;
                                                        fields4 = pStmt3;
                                                        schemaPattern3 = grantRows3;
                                                        columnResults2 = columnResults;
                                                    }
                                                }
                                                tableNamePattern5 = grantRows;
                                                fields4 = pStmt3;
                                                grantRows2 = str;
                                                str = schemaPattern3;
                                                pStmt3 = tableNamePattern5;
                                                fields5 = fields4;
                                                grantRows3 = grantRows3;
                                                if (columnResults != null) {
                                                    try {
                                                        columnResults.close();
                                                    } catch (Exception e) {
                                                    } catch (Throwable th4222) {
                                                        th2 = th4222;
                                                        this = this2;
                                                        schemaPattern2 = str;
                                                        PreparedStatement preparedStatement2 = pStmt3;
                                                        grantRows2 = catalog3;
                                                        results2 = results3;
                                                        pStmt4 = pStmt6;
                                                        this2 = fields5;
                                                        pStmt5 = grantRows3;
                                                        catalog3 = catalog2;
                                                    }
                                                }
                                                schemaPattern3 = str;
                                                grantRows4 = pStmt3;
                                                str = null;
                                                pStmt3 = fields5;
                                                databaseMetaData = this;
                                            } catch (Throwable th7) {
                                                th4222 = th7;
                                                schemaPattern4 = schemaPattern3;
                                                tableNamePattern4 = grantRows;
                                                fields4 = pStmt3;
                                                schemaPattern3 = grantRows3;
                                            }
                                        } catch (Throwable th42222) {
                                            schemaPattern4 = schemaPattern3;
                                            arrayList = grantRows;
                                            preparedStatement = pStmt3;
                                            th2 = th42222;
                                            pStmt5 = grantRows3;
                                            grantRows2 = catalog3;
                                            results2 = results3;
                                            pStmt4 = pStmt6;
                                            fields3 = preparedStatement;
                                            ArrayList<ResultSetRow> grantQuery2 = arrayList;
                                            fields2 = schemaPattern4;
                                            catalog3 = catalog2;
                                        }
                                    }
                                    schemaPattern4 = schemaPattern3;
                                    arrayList = grantRows;
                                    str3 = str;
                                    preparedStatement = pStmt3;
                                    host = true;
                                    i6 = 4;
                                    i7 = 2;
                                    i8 = 3;
                                    i9 = 5;
                                    i10 = 6;
                                    this = this2;
                                    pStmt5 = grantRows3;
                                    grantRows2 = catalog3;
                                    results2 = results3;
                                    pStmt4 = pStmt6;
                                    fields3 = preparedStatement;
                                    grantQuery = arrayList;
                                    schemaPattern2 = schemaPattern4;
                                    catalog3 = catalog2;
                                } else {
                                    host = catalog2;
                                    i10 = i5;
                                    str3 = str;
                                    i6 = 4;
                                    i7 = 2;
                                    i8 = 3;
                                    i9 = 5;
                                }
                                catalog2 = host;
                                i3 = i6;
                                i = i7;
                                i2 = i8;
                                i4 = i9;
                                str = str3;
                                i5 = i10;
                                databaseMetaData = this;
                            } catch (Throwable th8) {
                                th42222 = th8;
                            }
                        }
                        databaseMetaData = this;
                        tableNamePattern2 = catalog3;
                        allPrivileges = grantRows2;
                        results4 = results2;
                        grantRows5 = pStmt5;
                        pStmt7 = pStmt4;
                        if (results4 != null) {
                            try {
                                results4.close();
                            } catch (Exception e2) {
                            }
                        }
                        if (pStmt7 != null) {
                            try {
                                pStmt7.close();
                            } catch (Exception e3) {
                            }
                        }
                        return buildResultSet(fields3, grantRows5);
                    }
                } catch (Throwable th9) {
                    th42222 = th9;
                    catalog3 = catalog;
                    pStmt4 = pStmt3;
                    fields2 = schemaPattern;
                    pStmt5 = grantRows;
                    grantRows2 = grantQuery;
                    grantQuery = tableNamePattern2;
                    this = databaseMetaData;
                    th2 = th42222;
                    results = results2;
                    pStmt = pStmt4;
                    if (results != null) {
                        try {
                            results.close();
                        } catch (Exception e4) {
                        }
                    }
                    if (pStmt != null) {
                        try {
                            pStmt.close();
                        } catch (Exception e5) {
                        }
                    }
                    throw th2;
                }
            }
            str2 = "%";
            pStmt3.setString(1, str2);
            pStmt3.setString(2, tableNamePattern2);
            catalog3 = catalog;
            results2 = pStmt3.executeQuery();
            pStmt4 = pStmt3;
            fields3 = fields2;
            pStmt5 = grantRows;
            schemaPattern2 = schemaPattern;
            grantRows2 = grantQuery;
            grantQuery = tableNamePattern2;
            this = databaseMetaData;
            while (results2.next()) {
                host = results2.getString(catalog2);
                db = results2.getString(i);
                table = results2.getString(i2);
                grantor = results2.getString(i3);
                user = results2.getString(i4);
                user = "%";
                fullUser = new StringBuilder(user);
                host2 = host;
                fullUser.append("@");
                fullUser.append(host2);
                allPrivileges = results2.getString(i5);
                if (allPrivileges == null) {
                    host = catalog2;
                    i10 = i5;
                    str3 = str;
                    i6 = 4;
                    i7 = 2;
                    i8 = 3;
                    i9 = 5;
                } else {
                    st = new StringTokenizer(allPrivileges.toUpperCase(Locale.ENGLISH), ",");
                    grantRows3 = pStmt5;
                    catalog2 = catalog3;
                    pStmt3 = fields3;
                    catalog3 = grantRows2;
                    this2 = this;
                    grantRows = grantQuery;
                    tableNamePattern2 = db;
                    grantQuery = grantor;
                    pStmt6 = pStmt4;
                    results3 = results2;
                    schemaPattern3 = schemaPattern2;
                    schemaPattern2 = table;
                    while (st.hasMoreTokens()) {
                        str = st.nextToken().trim();
                        columnResults2 = null;
                        columnResults = getColumns(catalog2, schemaPattern3, schemaPattern2, "%");
                        while (columnResults.next()) {
                            fields4 = pStmt3;
                            pStmt3 = new byte[8][];
                            pStmt3[0] = s2b(tableNamePattern2);
                            pStmt3[1] = null;
                            pStmt3[2] = s2b(schemaPattern2);
                            if (grantQuery == null) {
                                pStmt3[3] = null;
                            } else {
                                pStmt3[3] = s2b(grantQuery);
                            }
                            tableNamePattern4 = grantRows;
                            pStmt3[4] = s2b(fullUser.toString());
                            pStmt3[5] = s2b(str);
                            pStmt3[6] = null;
                            schemaPattern4 = schemaPattern3;
                            schemaPattern5 = grantRows3;
                            schemaPattern5.add(new ByteArrayRow(pStmt3, getExceptionInterceptor()));
                            grantRows3 = schemaPattern5;
                            pStmt3 = fields4;
                            grantRows = tableNamePattern4;
                            schemaPattern3 = schemaPattern4;
                        }
                        tableNamePattern5 = grantRows;
                        fields4 = pStmt3;
                        grantRows2 = str;
                        str = schemaPattern3;
                        pStmt3 = tableNamePattern5;
                        fields5 = fields4;
                        grantRows3 = grantRows3;
                        if (columnResults != null) {
                            columnResults.close();
                        }
                        schemaPattern3 = str;
                        grantRows4 = pStmt3;
                        str = null;
                        pStmt3 = fields5;
                        databaseMetaData = this;
                    }
                    schemaPattern4 = schemaPattern3;
                    arrayList = grantRows;
                    str3 = str;
                    preparedStatement = pStmt3;
                    host = true;
                    i6 = 4;
                    i7 = 2;
                    i8 = 3;
                    i9 = 5;
                    i10 = 6;
                    this = this2;
                    pStmt5 = grantRows3;
                    grantRows2 = catalog3;
                    results2 = results3;
                    pStmt4 = pStmt6;
                    fields3 = preparedStatement;
                    grantQuery = arrayList;
                    schemaPattern2 = schemaPattern4;
                    catalog3 = catalog2;
                }
                catalog2 = host;
                i3 = i6;
                i = i7;
                i2 = i8;
                i4 = i9;
                str = str3;
                i5 = i10;
                databaseMetaData = this;
            }
            databaseMetaData = this;
            tableNamePattern2 = catalog3;
            allPrivileges = grantRows2;
            results4 = results2;
            grantRows5 = pStmt5;
            pStmt7 = pStmt4;
            if (results4 != null) {
                results4.close();
            }
            if (pStmt7 != null) {
                pStmt7.close();
            }
            return buildResultSet(fields3, grantRows5);
            ResultSet columnResults3;
            th2 = th42222;
            grantRows2 = str;
            columnResults3 = columnResults2;
            pStmt2 = schemaPattern4;
            tableNamePattern3 = tableNamePattern4;
            fields = fields4;
            if (columnResults3 != null) {
                try {
                    columnResults3.close();
                } catch (Exception e6) {
                }
            }
            try {
                throw th2;
            } catch (Throwable th422222) {
                th2 = th422222;
                this = this2;
                schemaPattern2 = pStmt2;
                grantRows2 = catalog3;
                grantQuery = tableNamePattern3;
                this2 = fields;
                catalog3 = catalog2;
                pStmt5 = schemaPattern3;
                results2 = results3;
                pStmt4 = pStmt6;
            }
            grantRows2 = str;
            columnResults3 = columnResults2;
            pStmt2 = schemaPattern4;
            tableNamePattern3 = tableNamePattern4;
            fields = fields4;
            if (columnResults3 != null) {
                columnResults3.close();
            }
            throw th2;
            th2 = th422222;
            grantRows2 = str;
            columnResults3 = columnResults2;
            pStmt2 = schemaPattern4;
            tableNamePattern3 = tableNamePattern4;
            fields = fields4;
            if (columnResults3 != null) {
                columnResults3.close();
            }
            throw th2;
            throw th2;
        } catch (Throwable th4222222) {
            catalog3 = catalog;
            th2 = th4222222;
            fields3 = fields2;
            pStmt4 = pStmt3;
            pStmt5 = grantRows;
            grantRows2 = grantQuery;
            results = results2;
            pStmt = pStmt4;
            if (results != null) {
                results.close();
            }
            if (pStmt != null) {
                pStmt.close();
            }
            throw th2;
        }
    }

    public ResultSet getTables(String catalog, String schemaPattern, String tableNamePattern, String[] types) throws SQLException {
        String tableNamePattern2;
        String tmpCat;
        List<String> parseList;
        String tableNamePat;
        Throwable th;
        DatabaseMetaData this;
        Statement stmt;
        DatabaseMetaData databaseMetaData = this;
        if (tableNamePattern != null) {
            tableNamePattern2 = tableNamePattern;
        } else if (databaseMetaData.conn.getNullNamePatternMatchesAll()) {
            tableNamePattern2 = "%";
        } else {
            throw SQLError.createSQLException("Table name pattern can not be NULL or empty.", SQLError.SQL_STATE_ILLEGAL_ARGUMENT, getExceptionInterceptor());
        }
        SortedMap<TableMetaDataKey, ResultSetRow> sortedRows = new TreeMap();
        ArrayList<ResultSetRow> tuples = new ArrayList();
        Statement stmt2 = databaseMetaData.conn.getMetadataSafeStatement();
        String tmpCat2 = "";
        if (catalog != null) {
            if (catalog.length() != 0) {
                tmpCat2 = catalog;
                tmpCat = tmpCat2;
                parseList = StringUtils.splitDBdotName(tableNamePattern2, tmpCat, databaseMetaData.quotedId, databaseMetaData.conn.isNoBackslashEscapesSet());
                if (parseList.size() != 2) {
                    tableNamePat = (String) parseList.get(1);
                } else {
                    tableNamePat = tableNamePattern2;
                }
                C04499 c04499 = c04499;
                final Statement statement = stmt2;
                final String[] strArr = types;
                List<String> parseList2 = parseList;
                C04499 c044992 = c04499;
                final SortedMap sortedMap = sortedRows;
                c04499 = new IterateBlock<String>(getCatalogIterator(catalog)) {
                    /* JADX WARNING: inconsistent code. */
                    /* Code decompiled incorrectly, please refer to instructions dump. */
                    void forEach(java.lang.String r28) throws java.sql.SQLException {
                        /*
                        r27 = this;
                        r1 = r27;
                        r8 = r28;
                        r2 = "information_schema";
                        r2 = r2.equalsIgnoreCase(r8);
                        r9 = 0;
                        r10 = 1;
                        if (r2 != 0) goto L_0x0021;
                    L_0x000e:
                        r2 = "mysql";
                        r2 = r2.equalsIgnoreCase(r8);
                        if (r2 != 0) goto L_0x0021;
                    L_0x0016:
                        r2 = "performance_schema";
                        r2 = r2.equalsIgnoreCase(r8);
                        if (r2 == 0) goto L_0x001f;
                    L_0x001e:
                        goto L_0x0021;
                    L_0x001f:
                        r2 = r9;
                        goto L_0x0022;
                    L_0x0021:
                        r2 = r10;
                    L_0x0022:
                        r11 = r2;
                        r12 = 0;
                        r2 = r12;
                        r3 = r4;	 Catch:{ SQLException -> 0x0340 }
                        r4 = new java.lang.StringBuilder;	 Catch:{ SQLException -> 0x0340 }
                        r4.<init>();	 Catch:{ SQLException -> 0x0340 }
                        r5 = com.mysql.jdbc.DatabaseMetaData.this;	 Catch:{ SQLException -> 0x0340 }
                        r5 = r5.conn;	 Catch:{ SQLException -> 0x0340 }
                        r13 = 2;
                        r14 = 5;
                        r5 = r5.versionMeetsMinimum(r14, r9, r13);	 Catch:{ SQLException -> 0x0340 }
                        if (r5 != 0) goto L_0x003b;
                    L_0x0038:
                        r5 = "SHOW TABLES FROM ";
                        goto L_0x003d;
                    L_0x003b:
                        r5 = "SHOW FULL TABLES FROM ";
                    L_0x003d:
                        r4.append(r5);	 Catch:{ SQLException -> 0x0340 }
                        r5 = com.mysql.jdbc.DatabaseMetaData.this;	 Catch:{ SQLException -> 0x0340 }
                        r5 = r5.quotedId;	 Catch:{ SQLException -> 0x0340 }
                        r6 = com.mysql.jdbc.DatabaseMetaData.this;	 Catch:{ SQLException -> 0x0340 }
                        r6 = r6.conn;	 Catch:{ SQLException -> 0x0340 }
                        r6 = r6.getPedantic();	 Catch:{ SQLException -> 0x0340 }
                        r5 = com.mysql.jdbc.StringUtils.quoteIdentifier(r8, r5, r6);	 Catch:{ SQLException -> 0x0340 }
                        r4.append(r5);	 Catch:{ SQLException -> 0x0340 }
                        r5 = " LIKE ";
                        r4.append(r5);	 Catch:{ SQLException -> 0x0340 }
                        r5 = r5;	 Catch:{ SQLException -> 0x0340 }
                        r6 = "'";
                        r5 = com.mysql.jdbc.StringUtils.quoteIdentifier(r5, r6, r10);	 Catch:{ SQLException -> 0x0340 }
                        r4.append(r5);	 Catch:{ SQLException -> 0x0340 }
                        r4 = r4.toString();	 Catch:{ SQLException -> 0x0340 }
                        r3 = r3.executeQuery(r4);	 Catch:{ SQLException -> 0x0340 }
                        r15 = r3;
                        r2 = 0;
                        r3 = 0;
                        r4 = 0;
                        r5 = 0;
                        r6 = 0;
                        r7 = r6;	 Catch:{ all -> 0x033a }
                        if (r7 == 0) goto L_0x00d9;
                    L_0x0076:
                        r7 = r6;	 Catch:{ all -> 0x033a }
                        r7 = r7.length;	 Catch:{ all -> 0x033a }
                        if (r7 != 0) goto L_0x007d;
                    L_0x007b:
                        goto L_0x00d9;
                    L_0x007d:
                        r7 = r6;
                        r6 = r5;
                        r5 = r4;
                        r4 = r3;
                        r3 = r2;
                        r2 = r9;
                    L_0x0083:
                        r10 = r6;	 Catch:{ all -> 0x033a }
                        r10 = r10.length;	 Catch:{ all -> 0x033a }
                        if (r2 >= r10) goto L_0x00d2;
                    L_0x0088:
                        r10 = com.mysql.jdbc.DatabaseMetaData.TableType.TABLE;	 Catch:{ all -> 0x033a }
                        r12 = r6;	 Catch:{ all -> 0x033a }
                        r12 = r12[r2];	 Catch:{ all -> 0x033a }
                        r10 = r10.equalsTo(r12);	 Catch:{ all -> 0x033a }
                        if (r10 == 0) goto L_0x0096;
                    L_0x0094:
                        r3 = 1;
                        goto L_0x00cd;
                    L_0x0096:
                        r10 = com.mysql.jdbc.DatabaseMetaData.TableType.VIEW;	 Catch:{ all -> 0x033a }
                        r12 = r6;	 Catch:{ all -> 0x033a }
                        r12 = r12[r2];	 Catch:{ all -> 0x033a }
                        r10 = r10.equalsTo(r12);	 Catch:{ all -> 0x033a }
                        if (r10 == 0) goto L_0x00a4;
                    L_0x00a2:
                        r4 = 1;
                        goto L_0x00cd;
                    L_0x00a4:
                        r10 = com.mysql.jdbc.DatabaseMetaData.TableType.SYSTEM_TABLE;	 Catch:{ all -> 0x033a }
                        r12 = r6;	 Catch:{ all -> 0x033a }
                        r12 = r12[r2];	 Catch:{ all -> 0x033a }
                        r10 = r10.equalsTo(r12);	 Catch:{ all -> 0x033a }
                        if (r10 == 0) goto L_0x00b2;
                    L_0x00b0:
                        r5 = 1;
                        goto L_0x00cd;
                    L_0x00b2:
                        r10 = com.mysql.jdbc.DatabaseMetaData.TableType.SYSTEM_VIEW;	 Catch:{ all -> 0x033a }
                        r12 = r6;	 Catch:{ all -> 0x033a }
                        r12 = r12[r2];	 Catch:{ all -> 0x033a }
                        r10 = r10.equalsTo(r12);	 Catch:{ all -> 0x033a }
                        if (r10 == 0) goto L_0x00c0;
                    L_0x00be:
                        r6 = 1;
                        goto L_0x00cd;
                    L_0x00c0:
                        r10 = com.mysql.jdbc.DatabaseMetaData.TableType.LOCAL_TEMPORARY;	 Catch:{ all -> 0x033a }
                        r12 = r6;	 Catch:{ all -> 0x033a }
                        r12 = r12[r2];	 Catch:{ all -> 0x033a }
                        r10 = r10.equalsTo(r12);	 Catch:{ all -> 0x033a }
                        if (r10 == 0) goto L_0x00cd;
                    L_0x00cc:
                        r7 = 1;
                    L_0x00cd:
                        r2 = r2 + 1;
                        r10 = 1;
                        r12 = 0;
                        goto L_0x0083;
                    L_0x00d2:
                        r10 = r3;
                        r12 = r4;
                        r18 = r5;
                        r19 = r6;
                        goto L_0x00e4;
                    L_0x00d9:
                        r2 = 1;
                        r3 = 1;
                        r4 = 1;
                        r5 = 1;
                        r7 = 1;
                        r10 = r2;
                        r12 = r3;
                        r18 = r4;
                        r19 = r5;
                    L_0x00e4:
                        r20 = r7;
                        r2 = 1;
                        r3 = 0;
                        r4 = com.mysql.jdbc.DatabaseMetaData.this;	 Catch:{ all -> 0x033a }
                        r4 = r4.conn;	 Catch:{ all -> 0x033a }
                        r4 = r4.versionMeetsMinimum(r14, r9, r13);	 Catch:{ all -> 0x033a }
                        if (r4 == 0) goto L_0x0109;
                    L_0x00f2:
                        r4 = "table_type";
                        r4 = r15.findColumn(r4);	 Catch:{ SQLException -> 0x00fb }
                        r2 = r4;
                        r3 = 1;
                        goto L_0x0109;
                    L_0x00fb:
                        r0 = move-exception;
                        r4 = r0;
                        r5 = "Type";
                        r5 = r15.findColumn(r5);	 Catch:{ SQLException -> 0x0106 }
                        r2 = r5;
                        r3 = 1;
                        goto L_0x0109;
                    L_0x0106:
                        r0 = move-exception;
                        r5 = r0;
                        r3 = 0;
                    L_0x0109:
                        r7 = r2;
                        r21 = r3;
                        r23 = r9;
                        r22 = 0;
                    L_0x0110:
                        r2 = r15.next();	 Catch:{ all -> 0x033a }
                        if (r2 == 0) goto L_0x032b;
                    L_0x0116:
                        r2 = 10;
                        r2 = new byte[r2][];	 Catch:{ all -> 0x033a }
                        r6 = r2;
                        if (r8 != 0) goto L_0x011f;
                    L_0x011d:
                        r2 = 0;
                        goto L_0x0125;
                    L_0x011f:
                        r2 = com.mysql.jdbc.DatabaseMetaData.this;	 Catch:{ all -> 0x033a }
                        r2 = r2.s2b(r8);	 Catch:{ all -> 0x033a }
                    L_0x0125:
                        r6[r9] = r2;	 Catch:{ all -> 0x033a }
                        r2 = 1;
                        r3 = 0;
                        r6[r2] = r3;	 Catch:{ all -> 0x033a }
                        r3 = r15.getBytes(r2);	 Catch:{ all -> 0x033a }
                        r6[r13] = r3;	 Catch:{ all -> 0x033a }
                        r2 = 4;
                        r3 = new byte[r9];	 Catch:{ all -> 0x033a }
                        r6[r2] = r3;	 Catch:{ all -> 0x033a }
                        r17 = 0;
                        r6[r14] = r17;	 Catch:{ all -> 0x033a }
                        r2 = 6;
                        r6[r2] = r17;	 Catch:{ all -> 0x033a }
                        r2 = 7;
                        r6[r2] = r17;	 Catch:{ all -> 0x033a }
                        r2 = 8;
                        r6[r2] = r17;	 Catch:{ all -> 0x033a }
                        r2 = 9;
                        r6[r2] = r17;	 Catch:{ all -> 0x033a }
                        r2 = 3;
                        if (r21 == 0) goto L_0x02ed;
                    L_0x014b:
                        r3 = r15.getString(r7);	 Catch:{ all -> 0x033a }
                        r5 = r3;
                        r3 = com.mysql.jdbc.DatabaseMetaData.AnonymousClass11.$SwitchMap$com$mysql$jdbc$DatabaseMetaData$TableType;	 Catch:{ all -> 0x033a }
                        r4 = com.mysql.jdbc.DatabaseMetaData.TableType.getTableTypeCompliantWith(r5);	 Catch:{ all -> 0x033a }
                        r4 = r4.ordinal();	 Catch:{ all -> 0x033a }
                        r3 = r3[r4];	 Catch:{ all -> 0x033a }
                        switch(r3) {
                            case 1: goto L_0x0250;
                            case 2: goto L_0x021a;
                            case 3: goto L_0x01e4;
                            case 4: goto L_0x01ae;
                            case 5: goto L_0x0168;
                            default: goto L_0x015f;
                        };	 Catch:{ all -> 0x033a }
                    L_0x015f:
                        r26 = r5;
                        r9 = r6;
                        r24 = r7;
                        r3 = com.mysql.jdbc.DatabaseMetaData.TableType.TABLE;	 Catch:{ all -> 0x033a }
                        goto L_0x02bb;
                    L_0x0168:
                        if (r20 == 0) goto L_0x01a9;
                    L_0x016a:
                        r3 = com.mysql.jdbc.DatabaseMetaData.TableType.LOCAL_TEMPORARY;	 Catch:{ all -> 0x033a }
                        r3 = r3.asBytes();	 Catch:{ all -> 0x033a }
                        r6[r2] = r3;	 Catch:{ all -> 0x033a }
                        r4 = r7;	 Catch:{ all -> 0x033a }
                        r3 = new com.mysql.jdbc.DatabaseMetaData$TableMetaDataKey;	 Catch:{ all -> 0x033a }
                        r2 = com.mysql.jdbc.DatabaseMetaData.this;	 Catch:{ all -> 0x033a }
                        r9 = com.mysql.jdbc.DatabaseMetaData.TableType.LOCAL_TEMPORARY;	 Catch:{ all -> 0x033a }
                        r9 = r9.getName();	 Catch:{ all -> 0x033a }
                        r24 = 0;
                        r13 = 1;
                        r25 = r15.getString(r13);	 Catch:{ all -> 0x033a }
                        r13 = r2;
                        r2 = r3;
                        r14 = r3;
                        r3 = r13;
                        r13 = r4;
                        r4 = r9;
                        r9 = r5;
                        r5 = r8;
                        r26 = r9;
                        r9 = r6;
                        r6 = r24;
                        r24 = r7;
                        r7 = r25;
                        r2.<init>(r4, r5, r6, r7);	 Catch:{ all -> 0x033a }
                        r2 = new com.mysql.jdbc.ByteArrayRow;	 Catch:{ all -> 0x033a }
                        r3 = com.mysql.jdbc.DatabaseMetaData.this;	 Catch:{ all -> 0x033a }
                        r3 = r3.getExceptionInterceptor();	 Catch:{ all -> 0x033a }
                        r2.<init>(r9, r3);	 Catch:{ all -> 0x033a }
                        r13.put(r14, r2);	 Catch:{ all -> 0x033a }
                        goto L_0x02e6;
                    L_0x01a9:
                        r9 = r6;
                        r24 = r7;
                        goto L_0x02e6;
                    L_0x01ae:
                        r26 = r5;
                        r9 = r6;
                        r24 = r7;
                        if (r19 == 0) goto L_0x02e6;
                    L_0x01b5:
                        r3 = com.mysql.jdbc.DatabaseMetaData.TableType.SYSTEM_VIEW;	 Catch:{ all -> 0x033a }
                        r3 = r3.asBytes();	 Catch:{ all -> 0x033a }
                        r9[r2] = r3;	 Catch:{ all -> 0x033a }
                        r13 = r7;	 Catch:{ all -> 0x033a }
                        r14 = new com.mysql.jdbc.DatabaseMetaData$TableMetaDataKey;	 Catch:{ all -> 0x033a }
                        r3 = com.mysql.jdbc.DatabaseMetaData.this;	 Catch:{ all -> 0x033a }
                        r2 = com.mysql.jdbc.DatabaseMetaData.TableType.SYSTEM_VIEW;	 Catch:{ all -> 0x033a }
                        r4 = r2.getName();	 Catch:{ all -> 0x033a }
                        r6 = 0;
                        r2 = 1;
                        r7 = r15.getString(r2);	 Catch:{ all -> 0x033a }
                        r2 = r14;
                        r5 = r8;
                        r2.<init>(r4, r5, r6, r7);	 Catch:{ all -> 0x033a }
                        r2 = new com.mysql.jdbc.ByteArrayRow;	 Catch:{ all -> 0x033a }
                        r3 = com.mysql.jdbc.DatabaseMetaData.this;	 Catch:{ all -> 0x033a }
                        r3 = r3.getExceptionInterceptor();	 Catch:{ all -> 0x033a }
                        r2.<init>(r9, r3);	 Catch:{ all -> 0x033a }
                        r13.put(r14, r2);	 Catch:{ all -> 0x033a }
                        goto L_0x02e6;
                    L_0x01e4:
                        r26 = r5;
                        r9 = r6;
                        r24 = r7;
                        if (r18 == 0) goto L_0x02e6;
                    L_0x01eb:
                        r3 = com.mysql.jdbc.DatabaseMetaData.TableType.SYSTEM_TABLE;	 Catch:{ all -> 0x033a }
                        r3 = r3.asBytes();	 Catch:{ all -> 0x033a }
                        r9[r2] = r3;	 Catch:{ all -> 0x033a }
                        r13 = r7;	 Catch:{ all -> 0x033a }
                        r14 = new com.mysql.jdbc.DatabaseMetaData$TableMetaDataKey;	 Catch:{ all -> 0x033a }
                        r3 = com.mysql.jdbc.DatabaseMetaData.this;	 Catch:{ all -> 0x033a }
                        r2 = com.mysql.jdbc.DatabaseMetaData.TableType.SYSTEM_TABLE;	 Catch:{ all -> 0x033a }
                        r4 = r2.getName();	 Catch:{ all -> 0x033a }
                        r6 = 0;
                        r2 = 1;
                        r7 = r15.getString(r2);	 Catch:{ all -> 0x033a }
                        r2 = r14;
                        r5 = r8;
                        r2.<init>(r4, r5, r6, r7);	 Catch:{ all -> 0x033a }
                        r2 = new com.mysql.jdbc.ByteArrayRow;	 Catch:{ all -> 0x033a }
                        r3 = com.mysql.jdbc.DatabaseMetaData.this;	 Catch:{ all -> 0x033a }
                        r3 = r3.getExceptionInterceptor();	 Catch:{ all -> 0x033a }
                        r2.<init>(r9, r3);	 Catch:{ all -> 0x033a }
                        r13.put(r14, r2);	 Catch:{ all -> 0x033a }
                        goto L_0x02e6;
                    L_0x021a:
                        r26 = r5;
                        r9 = r6;
                        r24 = r7;
                        if (r12 == 0) goto L_0x02e6;
                    L_0x0221:
                        r3 = com.mysql.jdbc.DatabaseMetaData.TableType.VIEW;	 Catch:{ all -> 0x033a }
                        r3 = r3.asBytes();	 Catch:{ all -> 0x033a }
                        r9[r2] = r3;	 Catch:{ all -> 0x033a }
                        r13 = r7;	 Catch:{ all -> 0x033a }
                        r14 = new com.mysql.jdbc.DatabaseMetaData$TableMetaDataKey;	 Catch:{ all -> 0x033a }
                        r3 = com.mysql.jdbc.DatabaseMetaData.this;	 Catch:{ all -> 0x033a }
                        r2 = com.mysql.jdbc.DatabaseMetaData.TableType.VIEW;	 Catch:{ all -> 0x033a }
                        r4 = r2.getName();	 Catch:{ all -> 0x033a }
                        r6 = 0;
                        r2 = 1;
                        r7 = r15.getString(r2);	 Catch:{ all -> 0x033a }
                        r2 = r14;
                        r5 = r8;
                        r2.<init>(r4, r5, r6, r7);	 Catch:{ all -> 0x033a }
                        r2 = new com.mysql.jdbc.ByteArrayRow;	 Catch:{ all -> 0x033a }
                        r3 = com.mysql.jdbc.DatabaseMetaData.this;	 Catch:{ all -> 0x033a }
                        r3 = r3.getExceptionInterceptor();	 Catch:{ all -> 0x033a }
                        r2.<init>(r9, r3);	 Catch:{ all -> 0x033a }
                        r13.put(r14, r2);	 Catch:{ all -> 0x033a }
                        goto L_0x02e6;
                    L_0x0250:
                        r26 = r5;
                        r9 = r6;
                        r24 = r7;
                        r13 = 0;
                        r14 = 0;
                        if (r11 == 0) goto L_0x027f;
                    L_0x0259:
                        if (r18 == 0) goto L_0x027f;
                    L_0x025b:
                        r3 = com.mysql.jdbc.DatabaseMetaData.TableType.SYSTEM_TABLE;	 Catch:{ all -> 0x033a }
                        r3 = r3.asBytes();	 Catch:{ all -> 0x033a }
                        r9[r2] = r3;	 Catch:{ all -> 0x033a }
                        r22 = new com.mysql.jdbc.DatabaseMetaData$TableMetaDataKey;	 Catch:{ all -> 0x033a }
                        r3 = com.mysql.jdbc.DatabaseMetaData.this;	 Catch:{ all -> 0x033a }
                        r2 = com.mysql.jdbc.DatabaseMetaData.TableType.SYSTEM_TABLE;	 Catch:{ all -> 0x033a }
                        r4 = r2.getName();	 Catch:{ all -> 0x033a }
                        r6 = 0;
                        r2 = 1;
                        r7 = r15.getString(r2);	 Catch:{ all -> 0x033a }
                        r2 = r22;
                        r5 = r8;
                        r2.<init>(r4, r5, r6, r7);	 Catch:{ all -> 0x033a }
                        r2 = r22;
                        r3 = 1;
                    L_0x027c:
                        r23 = r3;
                        goto L_0x02a8;
                    L_0x027f:
                        if (r11 != 0) goto L_0x02a5;
                    L_0x0281:
                        if (r10 == 0) goto L_0x02a5;
                    L_0x0283:
                        r3 = com.mysql.jdbc.DatabaseMetaData.TableType.TABLE;	 Catch:{ all -> 0x033a }
                        r3 = r3.asBytes();	 Catch:{ all -> 0x033a }
                        r9[r2] = r3;	 Catch:{ all -> 0x033a }
                        r22 = new com.mysql.jdbc.DatabaseMetaData$TableMetaDataKey;	 Catch:{ all -> 0x033a }
                        r3 = com.mysql.jdbc.DatabaseMetaData.this;	 Catch:{ all -> 0x033a }
                        r2 = com.mysql.jdbc.DatabaseMetaData.TableType.TABLE;	 Catch:{ all -> 0x033a }
                        r4 = r2.getName();	 Catch:{ all -> 0x033a }
                        r6 = 0;
                        r2 = 1;
                        r7 = r15.getString(r2);	 Catch:{ all -> 0x033a }
                        r2 = r22;
                        r5 = r8;
                        r2.<init>(r4, r5, r6, r7);	 Catch:{ all -> 0x033a }
                        r2 = r22;
                        r3 = 1;
                        goto L_0x027c;
                    L_0x02a5:
                        r23 = r13;
                        r2 = r14;
                    L_0x02a8:
                        if (r23 == 0) goto L_0x02e8;
                    L_0x02aa:
                        r3 = r7;	 Catch:{ all -> 0x033a }
                        r4 = new com.mysql.jdbc.ByteArrayRow;	 Catch:{ all -> 0x033a }
                        r5 = com.mysql.jdbc.DatabaseMetaData.this;	 Catch:{ all -> 0x033a }
                        r5 = r5.getExceptionInterceptor();	 Catch:{ all -> 0x033a }
                        r4.<init>(r9, r5);	 Catch:{ all -> 0x033a }
                        r3.put(r2, r4);	 Catch:{ all -> 0x033a }
                        goto L_0x02e8;
                    L_0x02bb:
                        r3 = r3.asBytes();	 Catch:{ all -> 0x033a }
                        r9[r2] = r3;	 Catch:{ all -> 0x033a }
                        r13 = r7;	 Catch:{ all -> 0x033a }
                        r14 = new com.mysql.jdbc.DatabaseMetaData$TableMetaDataKey;	 Catch:{ all -> 0x033a }
                        r3 = com.mysql.jdbc.DatabaseMetaData.this;	 Catch:{ all -> 0x033a }
                        r2 = com.mysql.jdbc.DatabaseMetaData.TableType.TABLE;	 Catch:{ all -> 0x033a }
                        r4 = r2.getName();	 Catch:{ all -> 0x033a }
                        r6 = 0;
                        r2 = 1;
                        r7 = r15.getString(r2);	 Catch:{ all -> 0x033a }
                        r2 = r14;
                        r5 = r8;
                        r2.<init>(r4, r5, r6, r7);	 Catch:{ all -> 0x033a }
                        r2 = new com.mysql.jdbc.ByteArrayRow;	 Catch:{ all -> 0x033a }
                        r3 = com.mysql.jdbc.DatabaseMetaData.this;	 Catch:{ all -> 0x033a }
                        r3 = r3.getExceptionInterceptor();	 Catch:{ all -> 0x033a }
                        r2.<init>(r9, r3);	 Catch:{ all -> 0x033a }
                        r13.put(r14, r2);	 Catch:{ all -> 0x033a }
                    L_0x02e6:
                        r2 = r22;
                    L_0x02e8:
                        r22 = r2;
                    L_0x02ea:
                        r25 = 1;
                        goto L_0x0323;
                    L_0x02ed:
                        r9 = r6;
                        r24 = r7;
                        if (r10 == 0) goto L_0x02ea;
                    L_0x02f2:
                        r3 = com.mysql.jdbc.DatabaseMetaData.TableType.TABLE;	 Catch:{ all -> 0x033a }
                        r3 = r3.asBytes();	 Catch:{ all -> 0x033a }
                        r9[r2] = r3;	 Catch:{ all -> 0x033a }
                        r13 = r7;	 Catch:{ all -> 0x033a }
                        r14 = new com.mysql.jdbc.DatabaseMetaData$TableMetaDataKey;	 Catch:{ all -> 0x033a }
                        r3 = com.mysql.jdbc.DatabaseMetaData.this;	 Catch:{ all -> 0x033a }
                        r2 = com.mysql.jdbc.DatabaseMetaData.TableType.TABLE;	 Catch:{ all -> 0x033a }
                        r4 = r2.getName();	 Catch:{ all -> 0x033a }
                        r6 = 0;
                        r7 = 1;
                        r16 = r15.getString(r7);	 Catch:{ all -> 0x033a }
                        r2 = r14;
                        r5 = r8;
                        r25 = r7;
                        r7 = r16;
                        r2.<init>(r4, r5, r6, r7);	 Catch:{ all -> 0x033a }
                        r2 = new com.mysql.jdbc.ByteArrayRow;	 Catch:{ all -> 0x033a }
                        r3 = com.mysql.jdbc.DatabaseMetaData.this;	 Catch:{ all -> 0x033a }
                        r3 = r3.getExceptionInterceptor();	 Catch:{ all -> 0x033a }
                        r2.<init>(r9, r3);	 Catch:{ all -> 0x033a }
                        r13.put(r14, r2);	 Catch:{ all -> 0x033a }
                        r7 = r24;
                        r9 = 0;
                        r13 = 2;
                        r14 = 5;
                        goto L_0x0110;
                        r2 = r1;
                        r3 = r8;
                        r4 = r11;
                        r5 = r15;
                        if (r5 == 0) goto L_0x0338;
                    L_0x0332:
                        r5.close();	 Catch:{ Exception -> 0x0336 }
                        goto L_0x0337;
                    L_0x0336:
                        r0 = move-exception;
                    L_0x0337:
                        r5 = 0;
                        return;
                    L_0x033a:
                        r0 = move-exception;
                        goto L_0x033e;
                    L_0x033c:
                        r0 = move-exception;
                        r15 = r2;
                    L_0x033e:
                        r2 = r0;
                        goto L_0x035c;
                    L_0x0340:
                        r0 = move-exception;
                        r3 = r0;
                        r4 = "08S01";
                        r5 = r3.getSQLState();	 Catch:{ all -> 0x033c }
                        r4 = r4.equals(r5);	 Catch:{ all -> 0x033c }
                        if (r4 == 0) goto L_0x034f;
                    L_0x034e:
                        throw r3;	 Catch:{ all -> 0x033c }
                        r4 = r1;
                        r5 = r8;
                        r6 = r11;
                        if (r2 == 0) goto L_0x035b;
                    L_0x0355:
                        r2.close();	 Catch:{ Exception -> 0x0359 }
                        goto L_0x035a;
                    L_0x0359:
                        r0 = move-exception;
                    L_0x035a:
                        r2 = 0;
                    L_0x035b:
                        return;
                        r3 = r1;
                        r4 = r8;
                        r5 = r11;
                        r6 = r15;
                        if (r6 == 0) goto L_0x0369;
                    L_0x0363:
                        r6.close();	 Catch:{ Exception -> 0x0367 }
                        goto L_0x0368;
                    L_0x0367:
                        r0 = move-exception;
                    L_0x0368:
                        r6 = 0;
                    L_0x0369:
                        throw r2;
                        */
                        throw new UnsupportedOperationException("Method not decompiled: com.mysql.jdbc.DatabaseMetaData.9.forEach(java.lang.String):void");
                    }
                };
                c044992.doForAll();
                if (stmt2 != null) {
                    stmt2.close();
                }
                tuples.addAll(sortedRows.values());
                return buildResultSet(createTablesFields(), tuples);
            }
        }
        if (databaseMetaData.conn.getNullCatalogMeansCurrent()) {
            tmpCat2 = databaseMetaData.database;
        }
        tmpCat = tmpCat2;
        parseList = StringUtils.splitDBdotName(tableNamePattern2, tmpCat, databaseMetaData.quotedId, databaseMetaData.conn.isNoBackslashEscapesSet());
        if (parseList.size() != 2) {
            tableNamePat = tableNamePattern2;
        } else {
            tableNamePat = (String) parseList.get(1);
        }
        try {
            C04499 c044993 = c044993;
            final Statement statement2 = stmt2;
            final String[] strArr2 = types;
            List<String> parseList22 = parseList;
            C04499 c0449922 = c044993;
            final SortedMap sortedMap2 = sortedRows;
            try {
                c044993 = /* anonymous class already generated */;
                c0449922.doForAll();
                if (stmt2 != null) {
                    stmt2.close();
                }
                tuples.addAll(sortedRows.values());
                return buildResultSet(createTablesFields(), tuples);
            } catch (Throwable th2) {
                th = th2;
                this = databaseMetaData;
                stmt = stmt2;
                if (stmt != null) {
                    stmt.close();
                }
                throw th;
            }
        } catch (Throwable th22) {
            parseList22 = parseList;
            th = th22;
            this = databaseMetaData;
            stmt = stmt2;
            if (stmt != null) {
                stmt.close();
            }
            throw th;
        }
    }

    protected Field[] createTablesFields() {
        return new Field[]{new Field("", "TABLE_CAT", 12, 255), new Field("", "TABLE_SCHEM", 12, 0), new Field("", "TABLE_NAME", 12, 255), new Field("", "TABLE_TYPE", 12, 5), new Field("", "REMARKS", 12, 0), new Field("", "TYPE_CAT", 12, 0), new Field("", "TYPE_SCHEM", 12, 0), new Field("", "TYPE_NAME", 12, 0), new Field("", "SELF_REFERENCING_COL_NAME", 12, 0), new Field("", "REF_GENERATION", 12, 0)};
    }

    public ResultSet getTableTypes() throws SQLException {
        ArrayList<ResultSetRow> tuples = new ArrayList();
        Field[] fields = new Field[]{new Field("", "TABLE_TYPE", 12, 256)};
        boolean minVersion5_0_1 = this.conn.versionMeetsMinimum(5, 0, 1);
        tuples.add(new ByteArrayRow(new byte[][]{TableType.LOCAL_TEMPORARY.asBytes()}, getExceptionInterceptor()));
        tuples.add(new ByteArrayRow(new byte[][]{TableType.SYSTEM_TABLE.asBytes()}, getExceptionInterceptor()));
        if (minVersion5_0_1) {
            tuples.add(new ByteArrayRow(new byte[][]{TableType.SYSTEM_VIEW.asBytes()}, getExceptionInterceptor()));
        }
        tuples.add(new ByteArrayRow(new byte[][]{TableType.TABLE.asBytes()}, getExceptionInterceptor()));
        if (minVersion5_0_1) {
            tuples.add(new ByteArrayRow(new byte[][]{TableType.VIEW.asBytes()}, getExceptionInterceptor()));
        }
        return buildResultSet(fields, tuples);
    }

    public String getTimeDateFunctions() throws SQLException {
        return "DAYOFWEEK,WEEKDAY,DAYOFMONTH,DAYOFYEAR,MONTH,DAYNAME,MONTHNAME,QUARTER,WEEK,YEAR,HOUR,MINUTE,SECOND,PERIOD_ADD,PERIOD_DIFF,TO_DAYS,FROM_DAYS,DATE_FORMAT,TIME_FORMAT,CURDATE,CURRENT_DATE,CURTIME,CURRENT_TIME,NOW,SYSDATE,CURRENT_TIMESTAMP,UNIX_TIMESTAMP,FROM_UNIXTIME,SEC_TO_TIME,TIME_TO_SEC";
    }

    public ResultSet getTypeInfo() throws SQLException {
        Field[] fields = new Field[]{new Field("", "TYPE_NAME", 1, 32), new Field("", "DATA_TYPE", 4, 5), new Field("", "PRECISION", 4, 10), new Field("", "LITERAL_PREFIX", 1, 4), new Field("", "LITERAL_SUFFIX", 1, 4), new Field("", "CREATE_PARAMS", 1, 32), new Field("", "NULLABLE", 5, 5), new Field("", "CASE_SENSITIVE", 16, 3), new Field("", "SEARCHABLE", 5, 3), new Field("", "UNSIGNED_ATTRIBUTE", 16, 3), new Field("", "FIXED_PREC_SCALE", 16, 3), new Field("", "AUTO_INCREMENT", 16, 3), new Field("", "LOCAL_TYPE_NAME", 1, 32), new Field("", "MINIMUM_SCALE", 5, 5), new Field("", "MAXIMUM_SCALE", 5, 5), new Field("", "SQL_DATA_TYPE", 4, 10), new Field("", "SQL_DATETIME_SUB", 4, 10), new Field("", "NUM_PREC_RADIX", 4, 10)};
        byte[][] rowVal = null;
        ArrayList<ResultSetRow> tuples = new ArrayList();
        tuples.add(new ByteArrayRow(new byte[][]{s2b("BIT"), Integer.toString(-7).getBytes(), s2b("1"), s2b(""), s2b(""), s2b(""), Integer.toString(1).getBytes(), s2b("true"), Integer.toString(3).getBytes(), s2b("false"), s2b("false"), s2b("false"), s2b("BIT"), s2b("0"), s2b("0"), s2b("0"), s2b("0"), s2b("10")}, getExceptionInterceptor()));
        tuples.add(new ByteArrayRow(new byte[][]{s2b("BOOL"), Integer.toString(-7).getBytes(), s2b("1"), s2b(""), s2b(""), s2b(""), Integer.toString(1).getBytes(), s2b("true"), Integer.toString(3).getBytes(), s2b("false"), s2b("false"), s2b("false"), s2b("BOOL"), s2b("0"), s2b("0"), s2b("0"), s2b("0"), s2b("10")}, getExceptionInterceptor()));
        tuples.add(new ByteArrayRow(new byte[][]{s2b("TINYINT"), Integer.toString(-6).getBytes(), s2b("3"), s2b(""), s2b(""), s2b("[(M)] [UNSIGNED] [ZEROFILL]"), Integer.toString(1).getBytes(), s2b("false"), Integer.toString(3).getBytes(), s2b("true"), s2b("false"), s2b("true"), s2b("TINYINT"), s2b("0"), s2b("0"), s2b("0"), s2b("0"), s2b("10")}, getExceptionInterceptor()));
        tuples.add(new ByteArrayRow(new byte[][]{s2b("TINYINT UNSIGNED"), Integer.toString(-6).getBytes(), s2b("3"), s2b(""), s2b(""), s2b("[(M)] [UNSIGNED] [ZEROFILL]"), Integer.toString(1).getBytes(), s2b("false"), Integer.toString(3).getBytes(), s2b("true"), s2b("false"), s2b("true"), s2b("TINYINT UNSIGNED"), s2b("0"), s2b("0"), s2b("0"), s2b("0"), s2b("10")}, getExceptionInterceptor()));
        tuples.add(new ByteArrayRow(new byte[][]{s2b("BIGINT"), Integer.toString(-5).getBytes(), s2b("19"), s2b(""), s2b(""), s2b("[(M)] [UNSIGNED] [ZEROFILL]"), Integer.toString(1).getBytes(), s2b("false"), Integer.toString(3).getBytes(), s2b("true"), s2b("false"), s2b("true"), s2b("BIGINT"), s2b("0"), s2b("0"), s2b("0"), s2b("0"), s2b("10")}, getExceptionInterceptor()));
        tuples.add(new ByteArrayRow(new byte[][]{s2b("BIGINT UNSIGNED"), Integer.toString(-5).getBytes(), s2b("20"), s2b(""), s2b(""), s2b("[(M)] [ZEROFILL]"), Integer.toString(1).getBytes(), s2b("false"), Integer.toString(3).getBytes(), s2b("true"), s2b("false"), s2b("true"), s2b("BIGINT UNSIGNED"), s2b("0"), s2b("0"), s2b("0"), s2b("0"), s2b("10")}, getExceptionInterceptor()));
        tuples.add(new ByteArrayRow(new byte[][]{s2b("LONG VARBINARY"), Integer.toString(-4).getBytes(), s2b("16777215"), s2b("'"), s2b("'"), s2b(""), Integer.toString(1).getBytes(), s2b("true"), Integer.toString(3).getBytes(), s2b("false"), s2b("false"), s2b("false"), s2b("LONG VARBINARY"), s2b("0"), s2b("0"), s2b("0"), s2b("0"), s2b("10")}, getExceptionInterceptor()));
        tuples.add(new ByteArrayRow(new byte[][]{s2b("MEDIUMBLOB"), Integer.toString(-4).getBytes(), s2b("16777215"), s2b("'"), s2b("'"), s2b(""), Integer.toString(1).getBytes(), s2b("true"), Integer.toString(3).getBytes(), s2b("false"), s2b("false"), s2b("false"), s2b("MEDIUMBLOB"), s2b("0"), s2b("0"), s2b("0"), s2b("0"), s2b("10")}, getExceptionInterceptor()));
        tuples.add(new ByteArrayRow(new byte[][]{s2b("LONGBLOB"), Integer.toString(-4).getBytes(), Integer.toString(ActivityChooserViewAdapter.MAX_ACTIVITY_COUNT_UNLIMITED).getBytes(), s2b("'"), s2b("'"), s2b(""), Integer.toString(1).getBytes(), s2b("true"), Integer.toString(3).getBytes(), s2b("false"), s2b("false"), s2b("false"), s2b("LONGBLOB"), s2b("0"), s2b("0"), s2b("0"), s2b("0"), s2b("10")}, getExceptionInterceptor()));
        tuples.add(new ByteArrayRow(new byte[][]{s2b("BLOB"), Integer.toString(-4).getBytes(), s2b("65535"), s2b("'"), s2b("'"), s2b(""), Integer.toString(1).getBytes(), s2b("true"), Integer.toString(3).getBytes(), s2b("false"), s2b("false"), s2b("false"), s2b("BLOB"), s2b("0"), s2b("0"), s2b("0"), s2b("0"), s2b("10")}, getExceptionInterceptor()));
        tuples.add(new ByteArrayRow(new byte[][]{s2b("TINYBLOB"), Integer.toString(-4).getBytes(), s2b("255"), s2b("'"), s2b("'"), s2b(""), Integer.toString(1).getBytes(), s2b("true"), Integer.toString(3).getBytes(), s2b("false"), s2b("false"), s2b("false"), s2b("TINYBLOB"), s2b("0"), s2b("0"), s2b("0"), s2b("0"), s2b("10")}, getExceptionInterceptor()));
        rowVal = new byte[18][];
        rowVal[0] = s2b("VARBINARY");
        rowVal[1] = Integer.toString(-3).getBytes();
        rowVal[2] = s2b(this.conn.versionMeetsMinimum(5, 0, 3) ? "65535" : "255");
        rowVal[3] = s2b("'");
        rowVal[4] = s2b("'");
        rowVal[5] = s2b("(M)");
        rowVal[6] = Integer.toString(1).getBytes();
        rowVal[7] = s2b("true");
        rowVal[8] = Integer.toString(3).getBytes();
        rowVal[9] = s2b("false");
        rowVal[10] = s2b("false");
        rowVal[11] = s2b("false");
        rowVal[12] = s2b("VARBINARY");
        rowVal[13] = s2b("0");
        rowVal[14] = s2b("0");
        rowVal[15] = s2b("0");
        rowVal[16] = s2b("0");
        rowVal[17] = s2b("10");
        tuples.add(new ByteArrayRow(rowVal, getExceptionInterceptor()));
        tuples.add(new ByteArrayRow(new byte[][]{s2b("BINARY"), Integer.toString(-2).getBytes(), s2b("255"), s2b("'"), s2b("'"), s2b("(M)"), Integer.toString(1).getBytes(), s2b("true"), Integer.toString(3).getBytes(), s2b("false"), s2b("false"), s2b("false"), s2b("BINARY"), s2b("0"), s2b("0"), s2b("0"), s2b("0"), s2b("10")}, getExceptionInterceptor()));
        tuples.add(new ByteArrayRow(new byte[][]{s2b("LONG VARCHAR"), Integer.toString(-1).getBytes(), s2b("16777215"), s2b("'"), s2b("'"), s2b(""), Integer.toString(1).getBytes(), s2b("false"), Integer.toString(3).getBytes(), s2b("false"), s2b("false"), s2b("false"), s2b("LONG VARCHAR"), s2b("0"), s2b("0"), s2b("0"), s2b("0"), s2b("10")}, getExceptionInterceptor()));
        tuples.add(new ByteArrayRow(new byte[][]{s2b("MEDIUMTEXT"), Integer.toString(-1).getBytes(), s2b("16777215"), s2b("'"), s2b("'"), s2b(""), Integer.toString(1).getBytes(), s2b("false"), Integer.toString(3).getBytes(), s2b("false"), s2b("false"), s2b("false"), s2b("MEDIUMTEXT"), s2b("0"), s2b("0"), s2b("0"), s2b("0"), s2b("10")}, getExceptionInterceptor()));
        tuples.add(new ByteArrayRow(new byte[][]{s2b("LONGTEXT"), Integer.toString(-1).getBytes(), Integer.toString(ActivityChooserViewAdapter.MAX_ACTIVITY_COUNT_UNLIMITED).getBytes(), s2b("'"), s2b("'"), s2b(""), Integer.toString(1).getBytes(), s2b("false"), Integer.toString(3).getBytes(), s2b("false"), s2b("false"), s2b("false"), s2b("LONGTEXT"), s2b("0"), s2b("0"), s2b("0"), s2b("0"), s2b("10")}, getExceptionInterceptor()));
        tuples.add(new ByteArrayRow(new byte[][]{s2b("TEXT"), Integer.toString(-1).getBytes(), s2b("65535"), s2b("'"), s2b("'"), s2b(""), Integer.toString(1).getBytes(), s2b("false"), Integer.toString(3).getBytes(), s2b("false"), s2b("false"), s2b("false"), s2b("TEXT"), s2b("0"), s2b("0"), s2b("0"), s2b("0"), s2b("10")}, getExceptionInterceptor()));
        tuples.add(new ByteArrayRow(new byte[][]{s2b("TINYTEXT"), Integer.toString(-1).getBytes(), s2b("255"), s2b("'"), s2b("'"), s2b(""), Integer.toString(1).getBytes(), s2b("false"), Integer.toString(3).getBytes(), s2b("false"), s2b("false"), s2b("false"), s2b("TINYTEXT"), s2b("0"), s2b("0"), s2b("0"), s2b("0"), s2b("10")}, getExceptionInterceptor()));
        tuples.add(new ByteArrayRow(new byte[][]{s2b("CHAR"), Integer.toString(1).getBytes(), s2b("255"), s2b("'"), s2b("'"), s2b("(M)"), Integer.toString(1).getBytes(), s2b("false"), Integer.toString(3).getBytes(), s2b("false"), s2b("false"), s2b("false"), s2b("CHAR"), s2b("0"), s2b("0"), s2b("0"), s2b("0"), s2b("10")}, getExceptionInterceptor()));
        int decimalPrecision = 254;
        if (r0.conn.versionMeetsMinimum(5, 0, 3)) {
            if (r0.conn.versionMeetsMinimum(5, 0, 6)) {
                decimalPrecision = 65;
            } else {
                decimalPrecision = 64;
            }
        }
        tuples.add(new ByteArrayRow(new byte[][]{s2b("NUMERIC"), Integer.toString(2).getBytes(), s2b(String.valueOf(decimalPrecision)), s2b(""), s2b(""), s2b("[(M[,D])] [ZEROFILL]"), Integer.toString(1).getBytes(), s2b("false"), Integer.toString(3).getBytes(), s2b("false"), s2b("false"), s2b("true"), s2b("NUMERIC"), s2b("-308"), s2b("308"), s2b("0"), s2b("0"), s2b("10")}, getExceptionInterceptor()));
        tuples.add(new ByteArrayRow(new byte[][]{s2b("DECIMAL"), Integer.toString(3).getBytes(), s2b(String.valueOf(decimalPrecision)), s2b(""), s2b(""), s2b("[(M[,D])] [ZEROFILL]"), Integer.toString(1).getBytes(), s2b("false"), Integer.toString(3).getBytes(), s2b("false"), s2b("false"), s2b("true"), s2b("DECIMAL"), s2b("-308"), s2b("308"), s2b("0"), s2b("0"), s2b("10")}, getExceptionInterceptor()));
        tuples.add(new ByteArrayRow(new byte[][]{s2b("INTEGER"), Integer.toString(4).getBytes(), s2b("10"), s2b(""), s2b(""), s2b("[(M)] [UNSIGNED] [ZEROFILL]"), Integer.toString(1).getBytes(), s2b("false"), Integer.toString(3).getBytes(), s2b("true"), s2b("false"), s2b("true"), s2b("INTEGER"), s2b("0"), s2b("0"), s2b("0"), s2b("0"), s2b("10")}, getExceptionInterceptor()));
        tuples.add(new ByteArrayRow(new byte[][]{s2b("INTEGER UNSIGNED"), Integer.toString(4).getBytes(), s2b("10"), s2b(""), s2b(""), s2b("[(M)] [ZEROFILL]"), Integer.toString(1).getBytes(), s2b("false"), Integer.toString(3).getBytes(), s2b("true"), s2b("false"), s2b("true"), s2b("INTEGER UNSIGNED"), s2b("0"), s2b("0"), s2b("0"), s2b("0"), s2b("10")}, getExceptionInterceptor()));
        tuples.add(new ByteArrayRow(new byte[][]{s2b("INT"), Integer.toString(4).getBytes(), s2b("10"), s2b(""), s2b(""), s2b("[(M)] [UNSIGNED] [ZEROFILL]"), Integer.toString(1).getBytes(), s2b("false"), Integer.toString(3).getBytes(), s2b("true"), s2b("false"), s2b("true"), s2b("INT"), s2b("0"), s2b("0"), s2b("0"), s2b("0"), s2b("10")}, getExceptionInterceptor()));
        tuples.add(new ByteArrayRow(new byte[][]{s2b("INT UNSIGNED"), Integer.toString(4).getBytes(), s2b("10"), s2b(""), s2b(""), s2b("[(M)] [ZEROFILL]"), Integer.toString(1).getBytes(), s2b("false"), Integer.toString(3).getBytes(), s2b("true"), s2b("false"), s2b("true"), s2b("INT UNSIGNED"), s2b("0"), s2b("0"), s2b("0"), s2b("0"), s2b("10")}, getExceptionInterceptor()));
        tuples.add(new ByteArrayRow(new byte[][]{s2b("MEDIUMINT"), Integer.toString(4).getBytes(), s2b("7"), s2b(""), s2b(""), s2b("[(M)] [UNSIGNED] [ZEROFILL]"), Integer.toString(1).getBytes(), s2b("false"), Integer.toString(3).getBytes(), s2b("true"), s2b("false"), s2b("true"), s2b("MEDIUMINT"), s2b("0"), s2b("0"), s2b("0"), s2b("0"), s2b("10")}, getExceptionInterceptor()));
        tuples.add(new ByteArrayRow(new byte[][]{s2b("MEDIUMINT UNSIGNED"), Integer.toString(4).getBytes(), s2b("8"), s2b(""), s2b(""), s2b("[(M)] [ZEROFILL]"), Integer.toString(1).getBytes(), s2b("false"), Integer.toString(3).getBytes(), s2b("true"), s2b("false"), s2b("true"), s2b("MEDIUMINT UNSIGNED"), s2b("0"), s2b("0"), s2b("0"), s2b("0"), s2b("10")}, getExceptionInterceptor()));
        tuples.add(new ByteArrayRow(new byte[][]{s2b("SMALLINT"), Integer.toString(5).getBytes(), s2b("5"), s2b(""), s2b(""), s2b("[(M)] [UNSIGNED] [ZEROFILL]"), Integer.toString(1).getBytes(), s2b("false"), Integer.toString(3).getBytes(), s2b("true"), s2b("false"), s2b("true"), s2b("SMALLINT"), s2b("0"), s2b("0"), s2b("0"), s2b("0"), s2b("10")}, getExceptionInterceptor()));
        tuples.add(new ByteArrayRow(new byte[][]{s2b("SMALLINT UNSIGNED"), Integer.toString(5).getBytes(), s2b("5"), s2b(""), s2b(""), s2b("[(M)] [ZEROFILL]"), Integer.toString(1).getBytes(), s2b("false"), Integer.toString(3).getBytes(), s2b("true"), s2b("false"), s2b("true"), s2b("SMALLINT UNSIGNED"), s2b("0"), s2b("0"), s2b("0"), s2b("0"), s2b("10")}, getExceptionInterceptor()));
        tuples.add(new ByteArrayRow(new byte[][]{s2b("FLOAT"), Integer.toString(7).getBytes(), s2b("10"), s2b(""), s2b(""), s2b("[(M,D)] [ZEROFILL]"), Integer.toString(1).getBytes(), s2b("false"), Integer.toString(3).getBytes(), s2b("false"), s2b("false"), s2b("true"), s2b("FLOAT"), s2b("-38"), s2b("38"), s2b("0"), s2b("0"), s2b("10")}, getExceptionInterceptor()));
        tuples.add(new ByteArrayRow(new byte[][]{s2b("DOUBLE"), Integer.toString(8).getBytes(), s2b("17"), s2b(""), s2b(""), s2b("[(M,D)] [ZEROFILL]"), Integer.toString(1).getBytes(), s2b("false"), Integer.toString(3).getBytes(), s2b("false"), s2b("false"), s2b("true"), s2b("DOUBLE"), s2b("-308"), s2b("308"), s2b("0"), s2b("0"), s2b("10")}, getExceptionInterceptor()));
        tuples.add(new ByteArrayRow(new byte[][]{s2b("DOUBLE PRECISION"), Integer.toString(8).getBytes(), s2b("17"), s2b(""), s2b(""), s2b("[(M,D)] [ZEROFILL]"), Integer.toString(1).getBytes(), s2b("false"), Integer.toString(3).getBytes(), s2b("false"), s2b("false"), s2b("true"), s2b("DOUBLE PRECISION"), s2b("-308"), s2b("308"), s2b("0"), s2b("0"), s2b("10")}, getExceptionInterceptor()));
        tuples.add(new ByteArrayRow(new byte[][]{s2b("REAL"), Integer.toString(8).getBytes(), s2b("17"), s2b(""), s2b(""), s2b("[(M,D)] [ZEROFILL]"), Integer.toString(1).getBytes(), s2b("false"), Integer.toString(3).getBytes(), s2b("false"), s2b("false"), s2b("true"), s2b("REAL"), s2b("-308"), s2b("308"), s2b("0"), s2b("0"), s2b("10")}, getExceptionInterceptor()));
        rowVal = new byte[18][];
        rowVal[0] = s2b("VARCHAR");
        rowVal[1] = Integer.toString(12).getBytes();
        rowVal[2] = s2b(r0.conn.versionMeetsMinimum(5, 0, 3) ? "65535" : "255");
        rowVal[3] = s2b("'");
        rowVal[4] = s2b("'");
        rowVal[5] = s2b("(M)");
        rowVal[6] = Integer.toString(1).getBytes();
        rowVal[7] = s2b("false");
        rowVal[8] = Integer.toString(3).getBytes();
        rowVal[9] = s2b("false");
        rowVal[10] = s2b("false");
        rowVal[11] = s2b("false");
        rowVal[12] = s2b("VARCHAR");
        rowVal[13] = s2b("0");
        rowVal[14] = s2b("0");
        rowVal[15] = s2b("0");
        rowVal[16] = s2b("0");
        rowVal[17] = s2b("10");
        tuples.add(new ByteArrayRow(rowVal, getExceptionInterceptor()));
        tuples.add(new ByteArrayRow(new byte[][]{s2b("ENUM"), Integer.toString(12).getBytes(), s2b("65535"), s2b("'"), s2b("'"), s2b(""), Integer.toString(1).getBytes(), s2b("false"), Integer.toString(3).getBytes(), s2b("false"), s2b("false"), s2b("false"), s2b("ENUM"), s2b("0"), s2b("0"), s2b("0"), s2b("0"), s2b("10")}, getExceptionInterceptor()));
        tuples.add(new ByteArrayRow(new byte[][]{s2b("SET"), Integer.toString(12).getBytes(), s2b("64"), s2b("'"), s2b("'"), s2b(""), Integer.toString(1).getBytes(), s2b("false"), Integer.toString(3).getBytes(), s2b("false"), s2b("false"), s2b("false"), s2b("SET"), s2b("0"), s2b("0"), s2b("0"), s2b("0"), s2b("10")}, getExceptionInterceptor()));
        tuples.add(new ByteArrayRow(new byte[][]{s2b("DATE"), Integer.toString(91).getBytes(), s2b("0"), s2b("'"), s2b("'"), s2b(""), Integer.toString(1).getBytes(), s2b("false"), Integer.toString(3).getBytes(), s2b("false"), s2b("false"), s2b("false"), s2b("DATE"), s2b("0"), s2b("0"), s2b("0"), s2b("0"), s2b("10")}, getExceptionInterceptor()));
        tuples.add(new ByteArrayRow(new byte[][]{s2b("TIME"), Integer.toString(92).getBytes(), s2b("0"), s2b("'"), s2b("'"), s2b(""), Integer.toString(1).getBytes(), s2b("false"), Integer.toString(3).getBytes(), s2b("false"), s2b("false"), s2b("false"), s2b("TIME"), s2b("0"), s2b("0"), s2b("0"), s2b("0"), s2b("10")}, getExceptionInterceptor()));
        tuples.add(new ByteArrayRow(new byte[][]{s2b("DATETIME"), Integer.toString(93).getBytes(), s2b("0"), s2b("'"), s2b("'"), s2b(""), Integer.toString(1).getBytes(), s2b("false"), Integer.toString(3).getBytes(), s2b("false"), s2b("false"), s2b("false"), s2b("DATETIME"), s2b("0"), s2b("0"), s2b("0"), s2b("0"), s2b("10")}, getExceptionInterceptor()));
        tuples.add(new ByteArrayRow(new byte[][]{s2b("TIMESTAMP"), Integer.toString(93).getBytes(), s2b("0"), s2b("'"), s2b("'"), s2b("[(M)]"), Integer.toString(1).getBytes(), s2b("false"), Integer.toString(3).getBytes(), s2b("false"), s2b("false"), s2b("false"), s2b("TIMESTAMP"), s2b("0"), s2b("0"), s2b("0"), s2b("0"), s2b("10")}, getExceptionInterceptor()));
        return buildResultSet(fields, tuples);
    }

    public ResultSet getUDTs(String catalog, String schemaPattern, String typeNamePattern, int[] types) throws SQLException {
        return buildResultSet(new Field[]{new Field("", "TYPE_CAT", 12, 32), new Field("", "TYPE_SCHEM", 12, 32), new Field("", "TYPE_NAME", 12, 32), new Field("", "CLASS_NAME", 12, 32), new Field("", "DATA_TYPE", 4, 10), new Field("", "REMARKS", 12, 32), new Field("", "BASE_TYPE", 5, 10)}, new ArrayList());
    }

    public String getURL() throws SQLException {
        return this.conn.getURL();
    }

    public String getUserName() throws SQLException {
        if (!this.conn.getUseHostsInPrivileges()) {
            return this.conn.getUser();
        }
        Statement stmt = null;
        ResultSet rs = null;
        try {
            stmt = this.conn.getMetadataSafeStatement();
            rs = stmt.executeQuery("SELECT USER()");
            rs.next();
            String string = rs.getString(1);
            if (rs != null) {
                try {
                    rs.close();
                } catch (Exception ex) {
                    AssertionFailedException.shouldNotHappen(ex);
                }
            }
            if (stmt != null) {
                try {
                    stmt.close();
                } catch (Exception ex2) {
                    AssertionFailedException.shouldNotHappen(ex2);
                }
            }
            return string;
        } catch (Throwable th) {
            if (rs != null) {
                try {
                    rs.close();
                } catch (Exception ex3) {
                    AssertionFailedException.shouldNotHappen(ex3);
                }
            }
            if (stmt != null) {
                try {
                    stmt.close();
                } catch (Exception ex32) {
                    AssertionFailedException.shouldNotHappen(ex32);
                }
            }
        }
    }

    public ResultSet getVersionColumns(String catalog, String schema, String table) throws SQLException {
        if (table == null) {
            throw SQLError.createSQLException("Table not specified.", SQLError.SQL_STATE_ILLEGAL_ARGUMENT, getExceptionInterceptor());
        }
        Field[] fields = new Field[]{new Field("", "SCOPE", 5, 5), new Field("", "COLUMN_NAME", 1, 32), new Field("", "DATA_TYPE", 4, 5), new Field("", "TYPE_NAME", 1, 16), new Field("", "COLUMN_SIZE", 4, 16), new Field("", "BUFFER_LENGTH", 4, 16), new Field("", "DECIMAL_DIGITS", 5, 16), new Field("", "PSEUDO_COLUMN", 5, 5)};
        ArrayList<ResultSetRow> rows = new ArrayList();
        Statement stmt = this.conn.getMetadataSafeStatement();
        try {
            final String str = table;
            final Statement statement = stmt;
            final ArrayList<ResultSetRow> arrayList = rows;
            new IterateBlock<String>(getCatalogIterator(catalog)) {
                void forEach(String catalogStr) throws SQLException {
                    ResultSet results;
                    SQLException e;
                    Throwable th;
                    String catalogStr2 = catalogStr;
                    ResultSet resultSet = null;
                    boolean with_where = DatabaseMetaData.this.conn.versionMeetsMinimum(5, 0, 0);
                    try {
                        StringBuilder whereBuf = new StringBuilder(" Extra LIKE '%on update CURRENT_TIMESTAMP%'");
                        List<String> rsFields = new ArrayList();
                        int i = 2;
                        if (!DatabaseMetaData.this.conn.versionMeetsMinimum(5, 1, 23)) {
                            whereBuf = new StringBuilder();
                            boolean firstTime = true;
                            String query = new StringBuilder("SHOW CREATE TABLE ");
                            query.append(DatabaseMetaData.this.getFullyQualifiedName(catalogStr2, str));
                            resultSet = statement.executeQuery(query.toString());
                            while (resultSet.next()) {
                                try {
                                    StringTokenizer lineTokenizer = new StringTokenizer(resultSet.getString(i), "\n");
                                    while (lineTokenizer.hasMoreTokens()) {
                                        String line = lineTokenizer.nextToken().trim();
                                        if (StringUtils.indexOfIgnoreCase(line, "on update CURRENT_TIMESTAMP") > -1) {
                                            boolean usingBackTicks = true;
                                            int beginPos = line.indexOf(DatabaseMetaData.this.quotedId);
                                            if (beginPos == -1) {
                                                beginPos = line.indexOf("\"");
                                                usingBackTicks = false;
                                            }
                                            if (beginPos != -1) {
                                                if (usingBackTicks) {
                                                    results = resultSet;
                                                    try {
                                                        resultSet = line.indexOf(DatabaseMetaData.this.quotedId, beginPos + 1);
                                                    } catch (SQLException e2) {
                                                        e = e2;
                                                    }
                                                } else {
                                                    results = resultSet;
                                                    resultSet = line.indexOf("\"", beginPos + 1);
                                                }
                                                if (resultSet != -1) {
                                                    if (with_where) {
                                                        if (firstTime) {
                                                            firstTime = false;
                                                        } else {
                                                            whereBuf.append(" or");
                                                        }
                                                        whereBuf.append(" Field='");
                                                        whereBuf.append(line.substring(beginPos + 1, resultSet));
                                                        whereBuf.append("'");
                                                    } else {
                                                        rsFields.add(line.substring(beginPos + 1, resultSet));
                                                    }
                                                }
                                                resultSet = results;
                                            }
                                        }
                                        results = resultSet;
                                        resultSet = results;
                                    }
                                    i = 2;
                                } catch (SQLException e3) {
                                    results = resultSet;
                                    SQLException sqlEx = e3;
                                } catch (Throwable th2) {
                                    results = resultSet;
                                    Throwable th3 = th2;
                                }
                            }
                        }
                        if (whereBuf.length() > 0 || rsFields.size() > 0) {
                            StringBuilder queryBuf = new StringBuilder("SHOW COLUMNS FROM ");
                            queryBuf.append(StringUtils.quoteIdentifier(str, DatabaseMetaData.this.quotedId, DatabaseMetaData.this.conn.getPedantic()));
                            queryBuf.append(" FROM ");
                            queryBuf.append(StringUtils.quoteIdentifier(catalogStr2, DatabaseMetaData.this.quotedId, DatabaseMetaData.this.conn.getPedantic()));
                            if (with_where) {
                                queryBuf.append(" WHERE");
                                queryBuf.append(whereBuf.toString());
                            }
                            resultSet = statement.executeQuery(queryBuf.toString());
                            while (resultSet.next()) {
                                if (with_where || rsFields.contains(resultSet.getString("Field"))) {
                                    TypeDescriptor typeDesc = new TypeDescriptor(DatabaseMetaData.this, resultSet.getString("Type"), resultSet.getString("Null"));
                                    byte[][] rowVal = new byte[8][];
                                    byte[] bArr = null;
                                    rowVal[0] = null;
                                    rowVal[1] = resultSet.getBytes("Field");
                                    rowVal[2] = Short.toString(typeDesc.dataType).getBytes();
                                    rowVal[3] = DatabaseMetaData.this.s2b(typeDesc.typeName);
                                    rowVal[4] = typeDesc.columnSize == null ? null : DatabaseMetaData.this.s2b(typeDesc.columnSize.toString());
                                    rowVal[5] = DatabaseMetaData.this.s2b(Integer.toString(typeDesc.bufferLength));
                                    if (typeDesc.decimalDigits != null) {
                                        bArr = DatabaseMetaData.this.s2b(typeDesc.decimalDigits.toString());
                                    }
                                    rowVal[6] = bArr;
                                    rowVal[7] = Integer.toString(1).getBytes();
                                    arrayList.add(new ByteArrayRow(rowVal, DatabaseMetaData.this.getExceptionInterceptor()));
                                }
                            }
                        }
                        AnonymousClass10 this = r1;
                        if (resultSet != null) {
                            try {
                                resultSet.close();
                            } catch (Exception e4) {
                            }
                        }
                    } catch (SQLException e5) {
                        e3 = e5;
                        results = resultSet;
                    } catch (Throwable th4) {
                        th2 = th4;
                        results = resultSet;
                    }
                    return;
                    sqlEx = e3;
                    try {
                        if (SQLError.SQL_STATE_BASE_TABLE_OR_VIEW_NOT_FOUND.equals(sqlEx.getSQLState())) {
                            throw sqlEx;
                        }
                        this = r1;
                        resultSet = results;
                        if (resultSet != null) {
                            try {
                                resultSet.close();
                            } catch (Exception e6) {
                            }
                        }
                        return;
                    } catch (Throwable th5) {
                        th2 = th5;
                        th3 = th2;
                        this = r1;
                        results = results;
                        ResultSet results2;
                        if (results2 != null) {
                            try {
                                results2.close();
                            } catch (Exception e7) {
                            }
                        }
                        throw th3;
                    }
                    if (SQLError.SQL_STATE_BASE_TABLE_OR_VIEW_NOT_FOUND.equals(sqlEx.getSQLState())) {
                        this = r1;
                        resultSet = results;
                        if (resultSet != null) {
                            resultSet.close();
                        }
                        return;
                    }
                    throw sqlEx;
                }
            }.doForAll();
            if (stmt != null) {
                stmt.close();
            }
            return buildResultSet(fields, rows);
        } catch (Throwable th) {
            Statement stmt2 = stmt;
            if (stmt2 != null) {
                stmt2.close();
            }
        }
    }

    public boolean insertsAreDetected(int type) throws SQLException {
        return false;
    }

    public boolean isCatalogAtStart() throws SQLException {
        return true;
    }

    public boolean isReadOnly() throws SQLException {
        return false;
    }

    public boolean locatorsUpdateCopy() throws SQLException {
        return this.conn.getEmulateLocators() ^ 1;
    }

    public boolean nullPlusNonNullIsNull() throws SQLException {
        return true;
    }

    public boolean nullsAreSortedAtEnd() throws SQLException {
        return false;
    }

    public boolean nullsAreSortedAtStart() throws SQLException {
        return this.conn.versionMeetsMinimum(4, 0, 2) && !this.conn.versionMeetsMinimum(4, 0, 11);
    }

    public boolean nullsAreSortedHigh() throws SQLException {
        return false;
    }

    public boolean nullsAreSortedLow() throws SQLException {
        return nullsAreSortedHigh() ^ 1;
    }

    public boolean othersDeletesAreVisible(int type) throws SQLException {
        return false;
    }

    public boolean othersInsertsAreVisible(int type) throws SQLException {
        return false;
    }

    public boolean othersUpdatesAreVisible(int type) throws SQLException {
        return false;
    }

    public boolean ownDeletesAreVisible(int type) throws SQLException {
        return false;
    }

    public boolean ownInsertsAreVisible(int type) throws SQLException {
        return false;
    }

    public boolean ownUpdatesAreVisible(int type) throws SQLException {
        return false;
    }

    protected LocalAndReferencedColumns parseTableStatusIntoLocalAndReferencedColumns(String keysComment) throws SQLException {
        String str = keysComment;
        String columnsDelimitter = ",";
        int indexOfOpenParenLocalColumns = StringUtils.indexOfIgnoreCase(0, str, "(", this.quotedId, this.quotedId, StringUtils.SEARCH_MODE__ALL);
        if (indexOfOpenParenLocalColumns == -1) {
            throw SQLError.createSQLException("Error parsing foreign keys definition, couldn't find start of local columns list.", SQLError.SQL_STATE_GENERAL_ERROR, getExceptionInterceptor());
        }
        String constraintName = StringUtils.unQuoteIdentifier(str.substring(0, indexOfOpenParenLocalColumns).trim(), r7.quotedId);
        str = str.substring(indexOfOpenParenLocalColumns, keysComment.length()).trim();
        String str2 = str;
        int indexOfCloseParenLocalColumns = StringUtils.indexOfIgnoreCase(0, str2, ")", r7.quotedId, r7.quotedId, StringUtils.SEARCH_MODE__ALL);
        if (indexOfCloseParenLocalColumns == -1) {
            throw SQLError.createSQLException("Error parsing foreign keys definition, couldn't find end of local columns list.", SQLError.SQL_STATE_GENERAL_ERROR, getExceptionInterceptor());
        }
        String localColumnNamesString = str.substring(1, indexOfCloseParenLocalColumns);
        str2 = str;
        int indexOfRefer = StringUtils.indexOfIgnoreCase(0, str2, "REFER ", r7.quotedId, r7.quotedId, StringUtils.SEARCH_MODE__ALL);
        if (indexOfRefer == -1) {
            throw SQLError.createSQLException("Error parsing foreign keys definition, couldn't find start of referenced tables list.", SQLError.SQL_STATE_GENERAL_ERROR, getExceptionInterceptor());
        }
        int i = indexOfRefer;
        str2 = str;
        int indexOfOpenParenReferCol = StringUtils.indexOfIgnoreCase(i, str2, "(", r7.quotedId, r7.quotedId, StringUtils.SEARCH_MODE__MRK_COM_WS);
        if (indexOfOpenParenReferCol == -1) {
            throw SQLError.createSQLException("Error parsing foreign keys definition, couldn't find start of referenced columns list.", SQLError.SQL_STATE_GENERAL_ERROR, getExceptionInterceptor());
        }
        String referCatalogTableString = str.substring("REFER ".length() + indexOfRefer, indexOfOpenParenReferCol);
        String str3 = r7.quotedId;
        String str4 = str3;
        int indexOfSlash = StringUtils.indexOfIgnoreCase(0, referCatalogTableString, "/", str4, r7.quotedId, StringUtils.SEARCH_MODE__MRK_COM_WS);
        if (indexOfSlash == -1) {
            throw SQLError.createSQLException("Error parsing foreign keys definition, couldn't find name of referenced catalog.", SQLError.SQL_STATE_GENERAL_ERROR, getExceptionInterceptor());
        }
        String referCatalog = StringUtils.unQuoteIdentifier(referCatalogTableString.substring(0, indexOfSlash), r7.quotedId);
        String referTable = StringUtils.unQuoteIdentifier(referCatalogTableString.substring(indexOfSlash + 1).trim(), r7.quotedId);
        String str5 = r7.quotedId;
        str4 = r7.quotedId;
        i = StringUtils.indexOfIgnoreCase(indexOfOpenParenReferCol, str, ")", str5, str4, StringUtils.SEARCH_MODE__ALL);
        if (i == -1) {
            throw SQLError.createSQLException("Error parsing foreign keys definition, couldn't find end of referenced columns list.", SQLError.SQL_STATE_GENERAL_ERROR, getExceptionInterceptor());
        }
        List<String> referColumnsList = StringUtils.split(str.substring(indexOfOpenParenReferCol + 1, i), columnsDelimitter, r7.quotedId, r7.quotedId, false);
        int indexOfSlash2 = indexOfSlash;
        return new LocalAndReferencedColumns(StringUtils.split(localColumnNamesString, columnsDelimitter, r7.quotedId, r7.quotedId, false), referColumnsList, constraintName, referCatalog, referTable);
    }

    protected byte[] s2b(String s) throws SQLException {
        if (s == null) {
            return null;
        }
        return StringUtils.getBytes(s, this.conn.getCharacterSetMetadata(), this.conn.getServerCharset(), this.conn.parserKnowsUnicode(), this.conn, getExceptionInterceptor());
    }

    public boolean storesLowerCaseIdentifiers() throws SQLException {
        return this.conn.storesLowerCaseTableName();
    }

    public boolean storesLowerCaseQuotedIdentifiers() throws SQLException {
        return this.conn.storesLowerCaseTableName();
    }

    public boolean storesMixedCaseIdentifiers() throws SQLException {
        return this.conn.storesLowerCaseTableName() ^ 1;
    }

    public boolean storesMixedCaseQuotedIdentifiers() throws SQLException {
        return this.conn.storesLowerCaseTableName() ^ 1;
    }

    public boolean storesUpperCaseIdentifiers() throws SQLException {
        return false;
    }

    public boolean storesUpperCaseQuotedIdentifiers() throws SQLException {
        return true;
    }

    public boolean supportsAlterTableWithAddColumn() throws SQLException {
        return true;
    }

    public boolean supportsAlterTableWithDropColumn() throws SQLException {
        return true;
    }

    public boolean supportsANSI92EntryLevelSQL() throws SQLException {
        return true;
    }

    public boolean supportsANSI92FullSQL() throws SQLException {
        return false;
    }

    public boolean supportsANSI92IntermediateSQL() throws SQLException {
        return false;
    }

    public boolean supportsBatchUpdates() throws SQLException {
        return true;
    }

    public boolean supportsCatalogsInDataManipulation() throws SQLException {
        return this.conn.versionMeetsMinimum(3, 22, 0);
    }

    public boolean supportsCatalogsInIndexDefinitions() throws SQLException {
        return this.conn.versionMeetsMinimum(3, 22, 0);
    }

    public boolean supportsCatalogsInPrivilegeDefinitions() throws SQLException {
        return this.conn.versionMeetsMinimum(3, 22, 0);
    }

    public boolean supportsCatalogsInProcedureCalls() throws SQLException {
        return this.conn.versionMeetsMinimum(3, 22, 0);
    }

    public boolean supportsCatalogsInTableDefinitions() throws SQLException {
        return this.conn.versionMeetsMinimum(3, 22, 0);
    }

    public boolean supportsColumnAliasing() throws SQLException {
        return true;
    }

    public boolean supportsConvert() throws SQLException {
        return false;
    }

    public boolean supportsConvert(int fromType, int toType) throws SQLException {
        if (fromType != 12) {
            if (fromType != MysqlErrorNumbers.ER_INVALID_GROUP_FUNC_USE) {
                switch (fromType) {
                    case -7:
                        return false;
                    case -6:
                    case -5:
                    case 2:
                    case 3:
                    case 4:
                    case 5:
                    case 6:
                    case 7:
                    case 8:
                        switch (toType) {
                            case -6:
                            case -5:
                            case FontRequestCallback.FAIL_REASON_SECURITY_VIOLATION /*-4*/:
                            case FontRequestCallback.FAIL_REASON_FONT_LOAD_ERROR /*-3*/:
                            case -2:
                            case -1:
                            case 1:
                            case 2:
                            case 3:
                            case 4:
                            case 5:
                            case 6:
                            case 7:
                            case 8:
                            case 12:
                                return true;
                            default:
                                return false;
                        }
                    case FontRequestCallback.FAIL_REASON_SECURITY_VIOLATION /*-4*/:
                    case FontRequestCallback.FAIL_REASON_FONT_LOAD_ERROR /*-3*/:
                    case -2:
                    case -1:
                    case 1:
                        break;
                    case 0:
                        return false;
                    default:
                        switch (fromType) {
                            case 91:
                                if (!(toType == 1 || toType == 12)) {
                                    switch (toType) {
                                        case FontRequestCallback.FAIL_REASON_SECURITY_VIOLATION /*-4*/:
                                        case FontRequestCallback.FAIL_REASON_FONT_LOAD_ERROR /*-3*/:
                                        case -2:
                                        case -1:
                                            break;
                                        default:
                                            return false;
                                    }
                                }
                                return true;
                            case 92:
                                if (!(toType == 1 || toType == 12)) {
                                    switch (toType) {
                                        case FontRequestCallback.FAIL_REASON_SECURITY_VIOLATION /*-4*/:
                                        case FontRequestCallback.FAIL_REASON_FONT_LOAD_ERROR /*-3*/:
                                        case -2:
                                        case -1:
                                            break;
                                        default:
                                            return false;
                                    }
                                }
                                return true;
                            case 93:
                                if (!(toType == 1 || toType == 12)) {
                                    switch (toType) {
                                        case FontRequestCallback.FAIL_REASON_SECURITY_VIOLATION /*-4*/:
                                        case FontRequestCallback.FAIL_REASON_FONT_LOAD_ERROR /*-3*/:
                                        case -2:
                                        case -1:
                                            break;
                                        default:
                                            switch (toType) {
                                                case 91:
                                                case 92:
                                                    break;
                                                default:
                                                    return false;
                                            }
                                    }
                                }
                                return true;
                            default:
                                return false;
                        }
                }
            }
            if (!(toType == 1 || toType == 12)) {
                switch (toType) {
                    case FontRequestCallback.FAIL_REASON_SECURITY_VIOLATION /*-4*/:
                    case FontRequestCallback.FAIL_REASON_FONT_LOAD_ERROR /*-3*/:
                    case -2:
                    case -1:
                        break;
                    default:
                        return false;
                }
            }
            return true;
        }
        if (!(toType == 12 || toType == MysqlErrorNumbers.ER_INVALID_GROUP_FUNC_USE)) {
            switch (toType) {
                case -6:
                case -5:
                case FontRequestCallback.FAIL_REASON_SECURITY_VIOLATION /*-4*/:
                case FontRequestCallback.FAIL_REASON_FONT_LOAD_ERROR /*-3*/:
                case -2:
                case -1:
                    break;
                default:
                    switch (toType) {
                        case 1:
                        case 2:
                        case 3:
                        case 4:
                        case 5:
                        case 6:
                        case 7:
                        case 8:
                            break;
                        default:
                            switch (toType) {
                                case 91:
                                case 92:
                                case 93:
                                    break;
                                default:
                                    return false;
                            }
                    }
            }
        }
        return true;
    }

    public boolean supportsCoreSQLGrammar() throws SQLException {
        return true;
    }

    public boolean supportsCorrelatedSubqueries() throws SQLException {
        return this.conn.versionMeetsMinimum(4, 1, 0);
    }

    public boolean supportsDataDefinitionAndDataManipulationTransactions() throws SQLException {
        return false;
    }

    public boolean supportsDataManipulationTransactionsOnly() throws SQLException {
        return false;
    }

    public boolean supportsDifferentTableCorrelationNames() throws SQLException {
        return true;
    }

    public boolean supportsExpressionsInOrderBy() throws SQLException {
        return true;
    }

    public boolean supportsExtendedSQLGrammar() throws SQLException {
        return false;
    }

    public boolean supportsFullOuterJoins() throws SQLException {
        return false;
    }

    public boolean supportsGetGeneratedKeys() {
        return true;
    }

    public boolean supportsGroupBy() throws SQLException {
        return true;
    }

    public boolean supportsGroupByBeyondSelect() throws SQLException {
        return true;
    }

    public boolean supportsGroupByUnrelated() throws SQLException {
        return true;
    }

    public boolean supportsIntegrityEnhancementFacility() throws SQLException {
        if (this.conn.getOverrideSupportsIntegrityEnhancementFacility()) {
            return true;
        }
        return false;
    }

    public boolean supportsLikeEscapeClause() throws SQLException {
        return true;
    }

    public boolean supportsLimitedOuterJoins() throws SQLException {
        return true;
    }

    public boolean supportsMinimumSQLGrammar() throws SQLException {
        return true;
    }

    public boolean supportsMixedCaseIdentifiers() throws SQLException {
        return this.conn.lowerCaseTableNames() ^ 1;
    }

    public boolean supportsMixedCaseQuotedIdentifiers() throws SQLException {
        return this.conn.lowerCaseTableNames() ^ 1;
    }

    public boolean supportsMultipleOpenResults() throws SQLException {
        return true;
    }

    public boolean supportsMultipleResultSets() throws SQLException {
        return this.conn.versionMeetsMinimum(4, 1, 0);
    }

    public boolean supportsMultipleTransactions() throws SQLException {
        return true;
    }

    public boolean supportsNamedParameters() throws SQLException {
        return false;
    }

    public boolean supportsNonNullableColumns() throws SQLException {
        return true;
    }

    public boolean supportsOpenCursorsAcrossCommit() throws SQLException {
        return false;
    }

    public boolean supportsOpenCursorsAcrossRollback() throws SQLException {
        return false;
    }

    public boolean supportsOpenStatementsAcrossCommit() throws SQLException {
        return false;
    }

    public boolean supportsOpenStatementsAcrossRollback() throws SQLException {
        return false;
    }

    public boolean supportsOrderByUnrelated() throws SQLException {
        return false;
    }

    public boolean supportsOuterJoins() throws SQLException {
        return true;
    }

    public boolean supportsPositionedDelete() throws SQLException {
        return false;
    }

    public boolean supportsPositionedUpdate() throws SQLException {
        return false;
    }

    public boolean supportsResultSetConcurrency(int type, int concurrency) throws SQLException {
        switch (type) {
            case 1003:
                if (concurrency != 1007) {
                    if (concurrency != 1008) {
                        throw SQLError.createSQLException("Illegal arguments to supportsResultSetConcurrency()", SQLError.SQL_STATE_ILLEGAL_ARGUMENT, getExceptionInterceptor());
                    }
                }
                return true;
            case 1004:
                if (concurrency != 1007) {
                    if (concurrency != 1008) {
                        throw SQLError.createSQLException("Illegal arguments to supportsResultSetConcurrency()", SQLError.SQL_STATE_ILLEGAL_ARGUMENT, getExceptionInterceptor());
                    }
                }
                return true;
            case MysqlErrorNumbers.ER_CANT_CREATE_TABLE /*1005*/:
                return false;
            default:
                throw SQLError.createSQLException("Illegal arguments to supportsResultSetConcurrency()", SQLError.SQL_STATE_ILLEGAL_ARGUMENT, getExceptionInterceptor());
        }
    }

    public boolean supportsResultSetHoldability(int holdability) throws SQLException {
        return holdability == 1;
    }

    public boolean supportsResultSetType(int type) throws SQLException {
        return type == 1004;
    }

    public boolean supportsSavepoints() throws SQLException {
        if (!this.conn.versionMeetsMinimum(4, 0, 14)) {
            if (!this.conn.versionMeetsMinimum(4, 1, 1)) {
                return false;
            }
        }
        return true;
    }

    public boolean supportsSchemasInDataManipulation() throws SQLException {
        return false;
    }

    public boolean supportsSchemasInIndexDefinitions() throws SQLException {
        return false;
    }

    public boolean supportsSchemasInPrivilegeDefinitions() throws SQLException {
        return false;
    }

    public boolean supportsSchemasInProcedureCalls() throws SQLException {
        return false;
    }

    public boolean supportsSchemasInTableDefinitions() throws SQLException {
        return false;
    }

    public boolean supportsSelectForUpdate() throws SQLException {
        return this.conn.versionMeetsMinimum(4, 0, 0);
    }

    public boolean supportsStatementPooling() throws SQLException {
        return false;
    }

    public boolean supportsStoredProcedures() throws SQLException {
        return this.conn.versionMeetsMinimum(5, 0, 0);
    }

    public boolean supportsSubqueriesInComparisons() throws SQLException {
        return this.conn.versionMeetsMinimum(4, 1, 0);
    }

    public boolean supportsSubqueriesInExists() throws SQLException {
        return this.conn.versionMeetsMinimum(4, 1, 0);
    }

    public boolean supportsSubqueriesInIns() throws SQLException {
        return this.conn.versionMeetsMinimum(4, 1, 0);
    }

    public boolean supportsSubqueriesInQuantifieds() throws SQLException {
        return this.conn.versionMeetsMinimum(4, 1, 0);
    }

    public boolean supportsTableCorrelationNames() throws SQLException {
        return true;
    }

    public boolean supportsTransactionIsolationLevel(int level) throws SQLException {
        if (!this.conn.supportsIsolationLevel()) {
            return false;
        }
        if (!(level == 4 || level == 8)) {
            switch (level) {
                case 1:
                case 2:
                    break;
                default:
                    return false;
            }
        }
        return true;
    }

    public boolean supportsTransactions() throws SQLException {
        return this.conn.supportsTransactions();
    }

    public boolean supportsUnion() throws SQLException {
        return this.conn.versionMeetsMinimum(4, 0, 0);
    }

    public boolean supportsUnionAll() throws SQLException {
        return this.conn.versionMeetsMinimum(4, 0, 0);
    }

    public boolean updatesAreDetected(int type) throws SQLException {
        return false;
    }

    public boolean usesLocalFilePerTable() throws SQLException {
        return false;
    }

    public boolean usesLocalFiles() throws SQLException {
        return false;
    }

    public ResultSet getClientInfoProperties() throws SQLException {
        return buildResultSet(new Field[]{new Field("", "NAME", 12, 255), new Field("", "MAX_LEN", 4, 10), new Field("", "DEFAULT_VALUE", 12, 255), new Field("", "DESCRIPTION", 12, 255)}, new ArrayList(), this.conn);
    }

    public ResultSet getFunctionColumns(String catalog, String schemaPattern, String functionNamePattern, String columnNamePattern) throws SQLException {
        return getProcedureOrFunctionColumns(createFunctionColumnsFields(), catalog, schemaPattern, functionNamePattern, columnNamePattern, false, true);
    }

    protected Field[] createFunctionColumnsFields() {
        return new Field[]{new Field("", "FUNCTION_CAT", 12, 512), new Field("", "FUNCTION_SCHEM", 12, 512), new Field("", "FUNCTION_NAME", 12, 512), new Field("", "COLUMN_NAME", 12, 512), new Field("", "COLUMN_TYPE", 12, 64), new Field("", "DATA_TYPE", 5, 6), new Field("", "TYPE_NAME", 12, 64), new Field("", "PRECISION", 4, 12), new Field("", "LENGTH", 4, 12), new Field("", "SCALE", 5, 12), new Field("", "RADIX", 5, 6), new Field("", "NULLABLE", 5, 6), new Field("", "REMARKS", 12, 512), new Field("", "CHAR_OCTET_LENGTH", 4, 32), new Field("", "ORDINAL_POSITION", 4, 32), new Field("", "IS_NULLABLE", 12, 12), new Field("", "SPECIFIC_NAME", 12, 64)};
    }

    public ResultSet getFunctions(String catalog, String schemaPattern, String functionNamePattern) throws SQLException {
        return getProceduresAndOrFunctions(new Field[]{new Field("", "FUNCTION_CAT", 1, 255), new Field("", "FUNCTION_SCHEM", 1, 255), new Field("", "FUNCTION_NAME", 1, 255), new Field("", "REMARKS", 1, 255), new Field("", "FUNCTION_TYPE", 5, 6), new Field("", "SPECIFIC_NAME", 1, 255)}, catalog, schemaPattern, functionNamePattern, false, true);
    }

    public boolean providesQueryObjectGenerator() throws SQLException {
        return false;
    }

    public ResultSet getSchemas(String catalog, String schemaPattern) throws SQLException {
        return buildResultSet(new Field[]{new Field("", "TABLE_SCHEM", 12, 255), new Field("", "TABLE_CATALOG", 12, 255)}, new ArrayList());
    }

    public boolean supportsStoredFunctionsUsingCallSyntax() throws SQLException {
        return true;
    }

    protected PreparedStatement prepareMetaDataSafeStatement(String sql) throws SQLException {
        PreparedStatement pStmt = this.conn.clientPrepareStatement(sql);
        if (pStmt.getMaxRows() != 0) {
            pStmt.setMaxRows(0);
        }
        ((Statement) pStmt).setHoldResultsOpenOverClose(true);
        return pStmt;
    }

    public ResultSet getPseudoColumns(String catalog, String schemaPattern, String tableNamePattern, String columnNamePattern) throws SQLException {
        return buildResultSet(new Field[]{new Field("", "TABLE_CAT", 12, 512), new Field("", "TABLE_SCHEM", 12, 512), new Field("", "TABLE_NAME", 12, 512), new Field("", "COLUMN_NAME", 12, 512), new Field("", "DATA_TYPE", 4, 12), new Field("", "COLUMN_SIZE", 4, 12), new Field("", "DECIMAL_DIGITS", 4, 12), new Field("", "NUM_PREC_RADIX", 4, 12), new Field("", "COLUMN_USAGE", 12, 512), new Field("", "REMARKS", 12, 512), new Field("", "CHAR_OCTET_LENGTH", 4, 12), new Field("", "IS_NULLABLE", 12, 512)}, new ArrayList());
    }

    public boolean generatedKeyAlwaysReturned() throws SQLException {
        return true;
    }
}
