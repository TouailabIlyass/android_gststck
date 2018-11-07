package com.mysql.jdbc;

import java.util.HashMap;
import java.util.Map;

public final class MysqlDefs {
    static final int COM_BINLOG_DUMP = 18;
    static final int COM_CHANGE_USER = 17;
    static final int COM_CLOSE_STATEMENT = 25;
    static final int COM_CONNECT_OUT = 20;
    static final int COM_END = 29;
    static final int COM_EXECUTE = 23;
    static final int COM_FETCH = 28;
    static final int COM_LONG_DATA = 24;
    static final int COM_PREPARE = 22;
    static final int COM_REGISTER_SLAVE = 21;
    static final int COM_RESET_STMT = 26;
    static final int COM_SET_OPTION = 27;
    static final int COM_TABLE_DUMP = 19;
    static final int CONNECT = 11;
    static final int CREATE_DB = 5;
    static final int DEBUG = 13;
    static final int DELAYED_INSERT = 16;
    static final int DROP_DB = 6;
    static final int FIELD_LIST = 4;
    static final int FIELD_TYPE_BIT = 16;
    public static final int FIELD_TYPE_BLOB = 252;
    static final int FIELD_TYPE_DATE = 10;
    static final int FIELD_TYPE_DATETIME = 12;
    static final int FIELD_TYPE_DECIMAL = 0;
    static final int FIELD_TYPE_DOUBLE = 5;
    static final int FIELD_TYPE_ENUM = 247;
    static final int FIELD_TYPE_FLOAT = 4;
    static final int FIELD_TYPE_GEOMETRY = 255;
    static final int FIELD_TYPE_INT24 = 9;
    static final int FIELD_TYPE_JSON = 245;
    static final int FIELD_TYPE_LONG = 3;
    static final int FIELD_TYPE_LONGLONG = 8;
    static final int FIELD_TYPE_LONG_BLOB = 251;
    static final int FIELD_TYPE_MEDIUM_BLOB = 250;
    static final int FIELD_TYPE_NEWDATE = 14;
    static final int FIELD_TYPE_NEW_DECIMAL = 246;
    static final int FIELD_TYPE_NULL = 6;
    static final int FIELD_TYPE_SET = 248;
    static final int FIELD_TYPE_SHORT = 2;
    static final int FIELD_TYPE_STRING = 254;
    static final int FIELD_TYPE_TIME = 11;
    static final int FIELD_TYPE_TIMESTAMP = 7;
    static final int FIELD_TYPE_TINY = 1;
    static final int FIELD_TYPE_TINY_BLOB = 249;
    static final int FIELD_TYPE_VARCHAR = 15;
    static final int FIELD_TYPE_VAR_STRING = 253;
    static final int FIELD_TYPE_YEAR = 13;
    static final int INIT_DB = 2;
    static final long LENGTH_BLOB = 65535;
    static final long LENGTH_LONGBLOB = 4294967295L;
    static final long LENGTH_MEDIUMBLOB = 16777215;
    static final long LENGTH_TINYBLOB = 255;
    static final int MAX_ROWS = 50000000;
    public static final int NO_CHARSET_INFO = -1;
    static final byte OPEN_CURSOR_FLAG = (byte) 1;
    static final int PING = 14;
    static final int PROCESS_INFO = 10;
    static final int PROCESS_KILL = 12;
    static final int QUERY = 3;
    static final int QUIT = 1;
    static final int RELOAD = 7;
    static final int SHUTDOWN = 8;
    static final int SLEEP = 0;
    static final int STATISTICS = 9;
    static final int TIME = 15;
    private static Map<String, Integer> mysqlToJdbcTypesMap = new HashMap();

    /* JADX WARNING: inconsistent code. */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    static int mysqlToJavaType(int r1) {
        /*
        switch(r1) {
            case 0: goto L_0x0041;
            case 1: goto L_0x003f;
            case 2: goto L_0x003d;
            case 3: goto L_0x003b;
            case 4: goto L_0x0039;
            case 5: goto L_0x0036;
            case 6: goto L_0x0034;
            case 7: goto L_0x0031;
            case 8: goto L_0x002f;
            case 9: goto L_0x002d;
            case 10: goto L_0x002a;
            case 11: goto L_0x0027;
            case 12: goto L_0x0024;
            case 13: goto L_0x0021;
            case 14: goto L_0x001e;
            case 15: goto L_0x001b;
            case 16: goto L_0x0019;
            default: goto L_0x0003;
        };
    L_0x0003:
        switch(r1) {
            case 245: goto L_0x0017;
            case 246: goto L_0x0041;
            case 247: goto L_0x0015;
            case 248: goto L_0x0013;
            case 249: goto L_0x0011;
            case 250: goto L_0x000f;
            case 251: goto L_0x000d;
            case 252: goto L_0x000b;
            case 253: goto L_0x001b;
            case 254: goto L_0x0017;
            case 255: goto L_0x0009;
            default: goto L_0x0006;
        };
    L_0x0006:
        r0 = 12;
        goto L_0x0043;
    L_0x0009:
        r0 = -2;
        goto L_0x0043;
    L_0x000b:
        r0 = -4;
        goto L_0x0043;
    L_0x000d:
        r0 = -4;
        goto L_0x0043;
    L_0x000f:
        r0 = -4;
        goto L_0x0043;
    L_0x0011:
        r0 = -3;
        goto L_0x0043;
    L_0x0013:
        r0 = 1;
        goto L_0x0043;
    L_0x0015:
        r0 = 1;
        goto L_0x0043;
    L_0x0017:
        r0 = 1;
        goto L_0x0043;
    L_0x0019:
        r0 = -7;
        goto L_0x0043;
    L_0x001b:
        r0 = 12;
        goto L_0x0043;
    L_0x001e:
        r0 = 91;
        goto L_0x0043;
    L_0x0021:
        r0 = 91;
        goto L_0x0043;
    L_0x0024:
        r0 = 93;
        goto L_0x0043;
    L_0x0027:
        r0 = 92;
        goto L_0x0043;
    L_0x002a:
        r0 = 91;
        goto L_0x0043;
    L_0x002d:
        r0 = 4;
        goto L_0x0043;
    L_0x002f:
        r0 = -5;
        goto L_0x0043;
    L_0x0031:
        r0 = 93;
        goto L_0x0043;
    L_0x0034:
        r0 = 0;
        goto L_0x0043;
    L_0x0036:
        r0 = 8;
        goto L_0x0043;
    L_0x0039:
        r0 = 7;
        goto L_0x0043;
    L_0x003b:
        r0 = 4;
        goto L_0x0043;
    L_0x003d:
        r0 = 5;
        goto L_0x0043;
    L_0x003f:
        r0 = -6;
        goto L_0x0043;
    L_0x0041:
        r0 = 3;
    L_0x0043:
        return r0;
        */
        throw new UnsupportedOperationException("Method not decompiled: com.mysql.jdbc.MysqlDefs.mysqlToJavaType(int):int");
    }

    static int mysqlToJavaType(String mysqlType) {
        if (mysqlType.equalsIgnoreCase("BIT")) {
            return mysqlToJavaType(16);
        }
        if (mysqlType.equalsIgnoreCase("TINYINT")) {
            return mysqlToJavaType(1);
        }
        if (mysqlType.equalsIgnoreCase("SMALLINT")) {
            return mysqlToJavaType(2);
        }
        if (mysqlType.equalsIgnoreCase("MEDIUMINT")) {
            return mysqlToJavaType(9);
        }
        if (!mysqlType.equalsIgnoreCase("INT")) {
            if (!mysqlType.equalsIgnoreCase("INTEGER")) {
                if (mysqlType.equalsIgnoreCase("BIGINT")) {
                    return mysqlToJavaType(8);
                }
                if (mysqlType.equalsIgnoreCase("INT24")) {
                    return mysqlToJavaType(9);
                }
                if (mysqlType.equalsIgnoreCase("REAL")) {
                    return mysqlToJavaType(5);
                }
                if (mysqlType.equalsIgnoreCase("FLOAT")) {
                    return mysqlToJavaType(4);
                }
                if (mysqlType.equalsIgnoreCase("DECIMAL")) {
                    return mysqlToJavaType(0);
                }
                if (mysqlType.equalsIgnoreCase("NUMERIC")) {
                    return mysqlToJavaType(0);
                }
                if (mysqlType.equalsIgnoreCase("DOUBLE")) {
                    return mysqlToJavaType(5);
                }
                if (mysqlType.equalsIgnoreCase("CHAR")) {
                    return mysqlToJavaType((int) FIELD_TYPE_STRING);
                }
                if (mysqlType.equalsIgnoreCase("VARCHAR")) {
                    return mysqlToJavaType((int) FIELD_TYPE_VAR_STRING);
                }
                if (mysqlType.equalsIgnoreCase("DATE")) {
                    return mysqlToJavaType(10);
                }
                if (mysqlType.equalsIgnoreCase("TIME")) {
                    return mysqlToJavaType(11);
                }
                if (mysqlType.equalsIgnoreCase("YEAR")) {
                    return mysqlToJavaType(13);
                }
                if (mysqlType.equalsIgnoreCase("TIMESTAMP")) {
                    return mysqlToJavaType(7);
                }
                if (mysqlType.equalsIgnoreCase("DATETIME")) {
                    return mysqlToJavaType(12);
                }
                if (mysqlType.equalsIgnoreCase("TINYBLOB")) {
                    return -2;
                }
                if (mysqlType.equalsIgnoreCase("BLOB") || mysqlType.equalsIgnoreCase("MEDIUMBLOB") || mysqlType.equalsIgnoreCase("LONGBLOB")) {
                    return -4;
                }
                if (mysqlType.equalsIgnoreCase("TINYTEXT")) {
                    return 12;
                }
                if (mysqlType.equalsIgnoreCase("TEXT") || mysqlType.equalsIgnoreCase("MEDIUMTEXT") || mysqlType.equalsIgnoreCase("LONGTEXT")) {
                    return -1;
                }
                if (mysqlType.equalsIgnoreCase("ENUM")) {
                    return mysqlToJavaType((int) FIELD_TYPE_ENUM);
                }
                if (mysqlType.equalsIgnoreCase("SET")) {
                    return mysqlToJavaType((int) FIELD_TYPE_SET);
                }
                if (mysqlType.equalsIgnoreCase("GEOMETRY")) {
                    return mysqlToJavaType(255);
                }
                if (mysqlType.equalsIgnoreCase("BINARY")) {
                    return -2;
                }
                if (mysqlType.equalsIgnoreCase("VARBINARY")) {
                    return -3;
                }
                if (mysqlType.equalsIgnoreCase("BIT")) {
                    return mysqlToJavaType(16);
                }
                if (mysqlType.equalsIgnoreCase("JSON")) {
                    return mysqlToJavaType((int) FIELD_TYPE_JSON);
                }
                return MysqlErrorNumbers.ER_INVALID_GROUP_FUNC_USE;
            }
        }
        return mysqlToJavaType(3);
    }

    public static String typeToName(int mysqlType) {
        if (mysqlType == FIELD_TYPE_JSON) {
            return "FIELD_TYPE_JSON";
        }
        switch (mysqlType) {
            case 0:
                return "FIELD_TYPE_DECIMAL";
            case 1:
                return "FIELD_TYPE_TINY";
            case 2:
                return "FIELD_TYPE_SHORT";
            case 3:
                return "FIELD_TYPE_LONG";
            case 4:
                return "FIELD_TYPE_FLOAT";
            case 5:
                return "FIELD_TYPE_DOUBLE";
            case 6:
                return "FIELD_TYPE_NULL";
            case 7:
                return "FIELD_TYPE_TIMESTAMP";
            case 8:
                return "FIELD_TYPE_LONGLONG";
            case 9:
                return "FIELD_TYPE_INT24";
            case 10:
                return "FIELD_TYPE_DATE";
            case 11:
                return "FIELD_TYPE_TIME";
            case 12:
                return "FIELD_TYPE_DATETIME";
            case 13:
                return "FIELD_TYPE_YEAR";
            case 14:
                return "FIELD_TYPE_NEWDATE";
            case 15:
                return "FIELD_TYPE_VARCHAR";
            case 16:
                return "FIELD_TYPE_BIT";
            default:
                switch (mysqlType) {
                    case FIELD_TYPE_ENUM /*247*/:
                        return "FIELD_TYPE_ENUM";
                    case FIELD_TYPE_SET /*248*/:
                        return "FIELD_TYPE_SET";
                    case FIELD_TYPE_TINY_BLOB /*249*/:
                        return "FIELD_TYPE_TINY_BLOB";
                    case 250:
                        return "FIELD_TYPE_MEDIUM_BLOB";
                    case FIELD_TYPE_LONG_BLOB /*251*/:
                        return "FIELD_TYPE_LONG_BLOB";
                    case FIELD_TYPE_BLOB /*252*/:
                        return "FIELD_TYPE_BLOB";
                    case FIELD_TYPE_VAR_STRING /*253*/:
                        return "FIELD_TYPE_VAR_STRING";
                    case FIELD_TYPE_STRING /*254*/:
                        return "FIELD_TYPE_STRING";
                    case 255:
                        return "FIELD_TYPE_GEOMETRY";
                    default:
                        StringBuilder stringBuilder = new StringBuilder();
                        stringBuilder.append(" Unknown MySQL Type # ");
                        stringBuilder.append(mysqlType);
                        return stringBuilder.toString();
                }
        }
    }

    static {
        mysqlToJdbcTypesMap.put("BIT", Integer.valueOf(mysqlToJavaType(16)));
        mysqlToJdbcTypesMap.put("TINYINT", Integer.valueOf(mysqlToJavaType(1)));
        mysqlToJdbcTypesMap.put("SMALLINT", Integer.valueOf(mysqlToJavaType(2)));
        mysqlToJdbcTypesMap.put("MEDIUMINT", Integer.valueOf(mysqlToJavaType(9)));
        mysqlToJdbcTypesMap.put("INT", Integer.valueOf(mysqlToJavaType(3)));
        mysqlToJdbcTypesMap.put("INTEGER", Integer.valueOf(mysqlToJavaType(3)));
        mysqlToJdbcTypesMap.put("BIGINT", Integer.valueOf(mysqlToJavaType(8)));
        mysqlToJdbcTypesMap.put("INT24", Integer.valueOf(mysqlToJavaType(9)));
        mysqlToJdbcTypesMap.put("REAL", Integer.valueOf(mysqlToJavaType(5)));
        mysqlToJdbcTypesMap.put("FLOAT", Integer.valueOf(mysqlToJavaType(4)));
        mysqlToJdbcTypesMap.put("DECIMAL", Integer.valueOf(mysqlToJavaType(0)));
        mysqlToJdbcTypesMap.put("NUMERIC", Integer.valueOf(mysqlToJavaType(0)));
        mysqlToJdbcTypesMap.put("DOUBLE", Integer.valueOf(mysqlToJavaType(5)));
        mysqlToJdbcTypesMap.put("CHAR", Integer.valueOf(mysqlToJavaType((int) FIELD_TYPE_STRING)));
        mysqlToJdbcTypesMap.put("VARCHAR", Integer.valueOf(mysqlToJavaType((int) FIELD_TYPE_VAR_STRING)));
        mysqlToJdbcTypesMap.put("DATE", Integer.valueOf(mysqlToJavaType(10)));
        mysqlToJdbcTypesMap.put("TIME", Integer.valueOf(mysqlToJavaType(11)));
        mysqlToJdbcTypesMap.put("YEAR", Integer.valueOf(mysqlToJavaType(13)));
        mysqlToJdbcTypesMap.put("TIMESTAMP", Integer.valueOf(mysqlToJavaType(7)));
        mysqlToJdbcTypesMap.put("DATETIME", Integer.valueOf(mysqlToJavaType(12)));
        mysqlToJdbcTypesMap.put("TINYBLOB", Integer.valueOf(-2));
        mysqlToJdbcTypesMap.put("BLOB", Integer.valueOf(-4));
        mysqlToJdbcTypesMap.put("MEDIUMBLOB", Integer.valueOf(-4));
        mysqlToJdbcTypesMap.put("LONGBLOB", Integer.valueOf(-4));
        mysqlToJdbcTypesMap.put("TINYTEXT", Integer.valueOf(12));
        mysqlToJdbcTypesMap.put("TEXT", Integer.valueOf(-1));
        mysqlToJdbcTypesMap.put("MEDIUMTEXT", Integer.valueOf(-1));
        mysqlToJdbcTypesMap.put("LONGTEXT", Integer.valueOf(-1));
        mysqlToJdbcTypesMap.put("ENUM", Integer.valueOf(mysqlToJavaType((int) FIELD_TYPE_ENUM)));
        mysqlToJdbcTypesMap.put("SET", Integer.valueOf(mysqlToJavaType((int) FIELD_TYPE_SET)));
        mysqlToJdbcTypesMap.put("GEOMETRY", Integer.valueOf(mysqlToJavaType(255)));
        mysqlToJdbcTypesMap.put("JSON", Integer.valueOf(mysqlToJavaType((int) FIELD_TYPE_JSON)));
    }

    static final void appendJdbcTypeMappingQuery(StringBuilder buf, String mysqlTypeColumnName) {
        buf.append("CASE ");
        Map<String, Integer> typesMap = new HashMap();
        typesMap.putAll(mysqlToJdbcTypesMap);
        typesMap.put("BINARY", Integer.valueOf(-2));
        typesMap.put("VARBINARY", Integer.valueOf(-3));
        for (String mysqlTypeName : typesMap.keySet()) {
            buf.append(" WHEN UPPER(");
            buf.append(mysqlTypeColumnName);
            buf.append(")='");
            buf.append(mysqlTypeName);
            buf.append("' THEN ");
            buf.append(typesMap.get(mysqlTypeName));
            if (mysqlTypeName.equalsIgnoreCase("DOUBLE") || mysqlTypeName.equalsIgnoreCase("FLOAT") || mysqlTypeName.equalsIgnoreCase("DECIMAL") || mysqlTypeName.equalsIgnoreCase("NUMERIC")) {
                buf.append(" WHEN ");
                buf.append(mysqlTypeColumnName);
                buf.append("='");
                buf.append(mysqlTypeName);
                buf.append(" UNSIGNED' THEN ");
                buf.append(typesMap.get(mysqlTypeName));
            }
        }
        buf.append(" ELSE ");
        buf.append(MysqlErrorNumbers.ER_INVALID_GROUP_FUNC_USE);
        buf.append(" END ");
    }
}
