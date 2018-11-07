package com.mysql.jdbc;

import java.sql.SQLException;
import java.sql.Timestamp;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Collections;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.StringTokenizer;
import java.util.TimeZone;

class EscapeProcessor {
    private static Map<String, String> JDBC_CONVERT_TO_MYSQL_TYPE_MAP;
    private static Map<String, String> JDBC_NO_CONVERT_TO_MYSQL_EXPRESSION_MAP;

    EscapeProcessor() {
    }

    static {
        Map<String, String> tempMap = new HashMap();
        tempMap.put("BIGINT", "0 + ?");
        tempMap.put("BINARY", "BINARY");
        tempMap.put("BIT", "0 + ?");
        tempMap.put("CHAR", "CHAR");
        tempMap.put("DATE", "DATE");
        tempMap.put("DECIMAL", "0.0 + ?");
        tempMap.put("DOUBLE", "0.0 + ?");
        tempMap.put("FLOAT", "0.0 + ?");
        tempMap.put("INTEGER", "0 + ?");
        tempMap.put("LONGVARBINARY", "BINARY");
        tempMap.put("LONGVARCHAR", "CONCAT(?)");
        tempMap.put("REAL", "0.0 + ?");
        tempMap.put("SMALLINT", "CONCAT(?)");
        tempMap.put("TIME", "TIME");
        tempMap.put("TIMESTAMP", "DATETIME");
        tempMap.put("TINYINT", "CONCAT(?)");
        tempMap.put("VARBINARY", "BINARY");
        tempMap.put("VARCHAR", "CONCAT(?)");
        JDBC_CONVERT_TO_MYSQL_TYPE_MAP = Collections.unmodifiableMap(tempMap);
        HashMap tempMap2 = new HashMap(JDBC_CONVERT_TO_MYSQL_TYPE_MAP);
        tempMap2.put("BINARY", "CONCAT(?)");
        tempMap2.put("CHAR", "CONCAT(?)");
        tempMap2.remove("DATE");
        tempMap2.put("LONGVARBINARY", "CONCAT(?)");
        tempMap2.remove("TIME");
        tempMap2.remove("TIMESTAMP");
        tempMap2.put("VARBINARY", "CONCAT(?)");
        JDBC_NO_CONVERT_TO_MYSQL_EXPRESSION_MAP = Collections.unmodifiableMap(tempMap2);
    }

    public static final Object escapeSQL(String sql, boolean serverSupportsConvertFn, MySQLConnection conn) throws SQLException {
        String fnToken;
        NoSuchElementException e;
        String str = sql;
        boolean z = serverSupportsConvertFn;
        MySQLConnection mySQLConnection = conn;
        if (str == null) {
            return null;
        }
        char c = '{';
        int beginBrace = str.indexOf(123);
        if ((beginBrace == -1 ? -1 : str.indexOf(125, beginBrace)) == -1) {
            return str;
        }
        String dateString;
        StringBuilder newSql = new StringBuilder();
        EscapeTokenizer escapeTokenizer = new EscapeTokenizer(str);
        int i = 0;
        byte usesVariables = (byte) 0;
        String escapeSequence = null;
        boolean replaceEscapeSequence = false;
        boolean callingStoredFunction = false;
        while (escapeTokenizer.hasMoreTokens()) {
            int beginBrace2;
            int endPos;
            String token = escapeTokenizer.nextToken();
            if (token.length() == 0) {
                beginBrace2 = beginBrace;
            } else if (token.charAt(i) != c) {
                beginBrace2 = beginBrace;
                newSql.append(token);
            } else if (token.endsWith("}")) {
                if (token.length() <= 2 || token.indexOf(c, 2) == -1) {
                    beginBrace2 = beginBrace;
                } else {
                    beginBrace2 = beginBrace;
                    StringBuilder buf = new StringBuilder(token.substring(0, 1));
                    Object remainingResults = escapeSQL(token.substring(1, token.length() - 1), z, mySQLConnection);
                    if (remainingResults instanceof String) {
                        beginBrace = (String) remainingResults;
                    } else {
                        beginBrace = ((EscapeProcessorResult) remainingResults).escapedSql;
                        if (usesVariables != (byte) 1) {
                            usesVariables = ((EscapeProcessorResult) remainingResults).usesVariables;
                        }
                    }
                    buf.append(beginBrace);
                    buf.append('}');
                    token = buf.toString();
                }
                str = removeWhitespace(token);
                if (StringUtils.startsWithIgnoreCase(str, "{escape")) {
                    try {
                        StringTokenizer st = new StringTokenizer(token, " '");
                        st.nextToken();
                        escapeSequence = st.nextToken();
                        if (escapeSequence.length() < 3) {
                            newSql.append(token);
                        } else {
                            replaceEscapeSequence = true;
                            escapeSequence = escapeSequence.substring(1, escapeSequence.length() - 1);
                        }
                    } catch (NoSuchElementException e2) {
                        newSql.append(token);
                    }
                } else if (StringUtils.startsWithIgnoreCase(str, "{fn")) {
                    fnToken = token.substring(token.toLowerCase().indexOf("fn ") + 3, token.length() - 1);
                    if (StringUtils.startsWithIgnoreCaseAndWs(fnToken, "convert")) {
                        newSql.append(processConvertToken(fnToken, z, mySQLConnection));
                    } else {
                        newSql.append(fnToken);
                    }
                } else if (StringUtils.startsWithIgnoreCase(str, "{d")) {
                    beginBrace = token.indexOf(39) + 1;
                    endPos = token.lastIndexOf(39);
                    if (beginBrace == -1) {
                    } else if (endPos == -1) {
                        r21 = endPos;
                    } else {
                        fnToken = token.substring(beginBrace, endPos);
                        try {
                            StringTokenizer st2 = new StringTokenizer(fnToken, " -");
                            String year4 = st2.nextToken();
                            String month2 = st2.nextToken();
                            String day2 = st2.nextToken();
                            dateString = new StringBuilder();
                            try {
                                dateString.append("'");
                                dateString.append(year4);
                                dateString.append("-");
                                String month22 = month2;
                                dateString.append(month22);
                                dateString.append("-");
                                month22 = day2;
                                dateString.append(month22);
                                dateString.append("'");
                                newSql.append(dateString.toString());
                            } catch (NoSuchElementException e3) {
                                e = e3;
                            }
                        } catch (NoSuchElementException e32) {
                            r21 = endPos;
                            e = e32;
                        }
                    }
                    newSql.append(token);
                } else if (StringUtils.startsWithIgnoreCase(str, "{ts")) {
                    processTimestampToken(mySQLConnection, newSql, token);
                } else if (StringUtils.startsWithIgnoreCase(str, "{t")) {
                    processTimeToken(mySQLConnection, newSql, token);
                } else {
                    if (!StringUtils.startsWithIgnoreCase(str, "{call")) {
                        if (!StringUtils.startsWithIgnoreCase(str, "{?=call")) {
                            if (StringUtils.startsWithIgnoreCase(str, "{oj")) {
                                newSql.append(token);
                            } else {
                                newSql.append(token);
                            }
                        }
                    }
                    int startPos = StringUtils.indexOfIgnoreCase(token, "CALL") + 5;
                    endPos = token.length() - 1;
                    if (StringUtils.startsWithIgnoreCase(str, "{?=call") != 0) {
                        callingStoredFunction = true;
                        newSql.append("SELECT ");
                        newSql.append(token.substring(startPos, endPos));
                    } else {
                        callingStoredFunction = false;
                        newSql.append("CALL ");
                        newSql.append(token.substring(startPos, endPos));
                    }
                    beginBrace = endPos - 1;
                    while (beginBrace >= startPos) {
                        char c2 = token.charAt(beginBrace);
                        if (Character.isWhitespace(c2)) {
                            beginBrace--;
                        } else if (c2 != ')') {
                            newSql.append("()");
                        }
                    }
                }
            } else {
                StringBuilder stringBuilder = new StringBuilder();
                stringBuilder.append("Not a valid escape sequence: ");
                stringBuilder.append(token);
                throw SQLError.createSQLException(stringBuilder.toString(), conn.getExceptionInterceptor());
            }
            beginBrace = beginBrace2;
            str = sql;
            z = serverSupportsConvertFn;
            c = '{';
            i = 0;
        }
        str = newSql.toString();
        if (replaceEscapeSequence) {
            dateString = str;
            while (dateString.indexOf(escapeSequence) != -1) {
                endPos = dateString.indexOf(escapeSequence);
                fnToken = dateString.substring(0, endPos);
                token = dateString.substring(endPos + 1, dateString.length());
                StringBuilder stringBuilder2 = new StringBuilder();
                stringBuilder2.append(fnToken);
                stringBuilder2.append("\\");
                stringBuilder2.append(token);
                dateString = stringBuilder2.toString();
            }
            str = dateString;
        }
        EscapeProcessorResult epr = new EscapeProcessorResult();
        epr.escapedSql = str;
        epr.callingStoredFunction = callingStoredFunction;
        if (usesVariables != (byte) 1) {
            if (escapeTokenizer.sawVariableUse()) {
                epr.usesVariables = (byte) 1;
            } else {
                epr.usesVariables = (byte) 0;
            }
        }
        return epr;
        stringBuilder = new StringBuilder();
        stringBuilder.append("Syntax error for DATE escape sequence '");
        stringBuilder.append(fnToken);
        stringBuilder.append("'");
        throw SQLError.createSQLException(stringBuilder.toString(), SQLError.SQL_STATE_SYNTAX_ERROR, conn.getExceptionInterceptor());
    }

    private static void processTimeToken(MySQLConnection conn, StringBuilder newSql, String token) throws SQLException {
        int i;
        NoSuchElementException e;
        StringBuilder stringBuilder;
        StringBuilder stringBuilder2 = newSql;
        String str = token;
        int startPos = str.indexOf(39) + 1;
        int endPos = str.lastIndexOf(39);
        if (startPos == -1) {
            i = startPos;
        } else if (endPos == -1) {
            r23 = endPos;
            i = startPos;
        } else {
            String argument = str.substring(startPos, endPos);
            try {
                StringTokenizer st = new StringTokenizer(argument, " :.");
                String hour = st.nextToken();
                String minute = st.nextToken();
                String second = st.nextToken();
                boolean serverSupportsFractionalSecond = false;
                String fractionalSecond = "";
                if (st.hasMoreTokens()) {
                    try {
                        if (conn.versionMeetsMinimum(5, 6, 4)) {
                            serverSupportsFractionalSecond = true;
                            StringBuilder stringBuilder3 = new StringBuilder();
                            stringBuilder3.append(".");
                            stringBuilder3.append(st.nextToken());
                            fractionalSecond = stringBuilder3.toString();
                        }
                    } catch (NoSuchElementException e2) {
                        e = e2;
                        endPos = e;
                        stringBuilder = new StringBuilder();
                        stringBuilder.append("Syntax error for escape sequence '");
                        stringBuilder.append(argument);
                        stringBuilder.append("'");
                        throw SQLError.createSQLException(stringBuilder.toString(), SQLError.SQL_STATE_SYNTAX_ERROR, conn.getExceptionInterceptor());
                    }
                }
                MySQLConnection mySQLConnection = conn;
                if (!conn.getUseTimezone()) {
                    i = startPos;
                } else if (conn.getUseLegacyDatetimeCode()) {
                    Calendar sessionCalendar = conn.getCalendarInstanceForSessionOrNew();
                    try {
                        r23 = endPos;
                        try {
                            i = startPos;
                        } catch (NumberFormatException e3) {
                            i = startPos;
                            endPos = e3;
                            try {
                                stringBuilder = new StringBuilder();
                                stringBuilder.append("Syntax error in TIMESTAMP escape sequence '");
                                stringBuilder.append(str);
                                stringBuilder.append("'.");
                                throw SQLError.createSQLException(stringBuilder.toString(), SQLError.SQL_STATE_ILLEGAL_ARGUMENT, conn.getExceptionInterceptor());
                            } catch (NoSuchElementException e4) {
                                e = e4;
                                endPos = e;
                                stringBuilder = new StringBuilder();
                                stringBuilder.append("Syntax error for escape sequence '");
                                stringBuilder.append(argument);
                                stringBuilder.append("'");
                                throw SQLError.createSQLException(stringBuilder.toString(), SQLError.SQL_STATE_SYNTAX_ERROR, conn.getExceptionInterceptor());
                            }
                        } catch (NoSuchElementException e5) {
                            i = startPos;
                            endPos = e5;
                            stringBuilder = new StringBuilder();
                            stringBuilder.append("Syntax error for escape sequence '");
                            stringBuilder.append(argument);
                            stringBuilder.append("'");
                            throw SQLError.createSQLException(stringBuilder.toString(), SQLError.SQL_STATE_SYNTAX_ERROR, conn.getExceptionInterceptor());
                        }
                        try {
                            endPos = TimeUtil.changeTimezone(conn, sessionCalendar, null, TimeUtil.fastTimeCreate(sessionCalendar, Integer.parseInt(hour), Integer.parseInt(minute), Integer.parseInt(second), conn.getExceptionInterceptor()), sessionCalendar.getTimeZone(), conn.getServerTimezoneTZ(), false);
                            stringBuilder2.append("'");
                            stringBuilder2.append(endPos.toString());
                            if (serverSupportsFractionalSecond) {
                                stringBuilder2.append(fractionalSecond);
                            }
                            stringBuilder2.append("'");
                            return;
                        } catch (NumberFormatException e32) {
                            endPos = e32;
                            stringBuilder = new StringBuilder();
                            stringBuilder.append("Syntax error in TIMESTAMP escape sequence '");
                            stringBuilder.append(str);
                            stringBuilder.append("'.");
                            throw SQLError.createSQLException(stringBuilder.toString(), SQLError.SQL_STATE_ILLEGAL_ARGUMENT, conn.getExceptionInterceptor());
                        }
                    } catch (NumberFormatException e322) {
                        r23 = endPos;
                        i = startPos;
                        endPos = e322;
                        stringBuilder = new StringBuilder();
                        stringBuilder.append("Syntax error in TIMESTAMP escape sequence '");
                        stringBuilder.append(str);
                        stringBuilder.append("'.");
                        throw SQLError.createSQLException(stringBuilder.toString(), SQLError.SQL_STATE_ILLEGAL_ARGUMENT, conn.getExceptionInterceptor());
                    }
                } else {
                    r23 = endPos;
                    i = startPos;
                }
                stringBuilder2.append("'");
                stringBuilder2.append(hour);
                stringBuilder2.append(":");
                stringBuilder2.append(minute);
                stringBuilder2.append(":");
                stringBuilder2.append(second);
                stringBuilder2.append(fractionalSecond);
                stringBuilder2.append("'");
                return;
            } catch (NoSuchElementException e52) {
                r23 = endPos;
                i = startPos;
                endPos = e52;
                stringBuilder = new StringBuilder();
                stringBuilder.append("Syntax error for escape sequence '");
                stringBuilder.append(argument);
                stringBuilder.append("'");
                throw SQLError.createSQLException(stringBuilder.toString(), SQLError.SQL_STATE_SYNTAX_ERROR, conn.getExceptionInterceptor());
            }
        }
        newSql.append(token);
    }

    private static void processTimestampToken(MySQLConnection conn, StringBuilder newSql, String token) throws SQLException {
        NumberFormatException e;
        String sessionCalendar;
        NumberFormatException hour;
        StringBuilder stringBuilder;
        MySQLConnection mySQLConnection = conn;
        StringBuilder stringBuilder2 = newSql;
        String str = token;
        int startPos = str.indexOf(39) + 1;
        int endPos = str.lastIndexOf(39);
        if (startPos != -1) {
            if (endPos != -1) {
                String argument = str.substring(startPos, endPos);
                try {
                    if (conn.getUseLegacyDatetimeCode()) {
                        StringTokenizer st = new StringTokenizer(argument, " .-:");
                        String year4 = st.nextToken();
                        String month2 = st.nextToken();
                        String day2 = st.nextToken();
                        String hour2 = st.nextToken();
                        String minute = st.nextToken();
                        String second = st.nextToken();
                        boolean serverSupportsFractionalSecond = false;
                        String fractionalSecond = "";
                        if (st.hasMoreTokens() && mySQLConnection.versionMeetsMinimum(5, 6, 4)) {
                            serverSupportsFractionalSecond = true;
                            StringBuilder stringBuilder3 = new StringBuilder();
                            stringBuilder3.append(".");
                            stringBuilder3.append(st.nextToken());
                            fractionalSecond = stringBuilder3.toString();
                        }
                        String fractionalSecond2 = fractionalSecond;
                        if (conn.getUseTimezone() || conn.getUseJDBCCompliantTimezoneShift()) {
                            String minute2 = minute;
                            String second2 = second;
                            Calendar sessionCalendar2 = conn.getCalendarInstanceForSessionOrNew();
                            String str2;
                            String str3;
                            String str4;
                            Calendar calendar;
                            try {
                                Calendar instance;
                                int year4Int = Integer.parseInt(year4);
                                int month2Int = Integer.parseInt(month2);
                                int day2Int = Integer.parseInt(day2);
                                int hourInt = Integer.parseInt(hour2);
                                int minuteInt = Integer.parseInt(minute2);
                                int secondInt = Integer.parseInt(second2);
                                boolean useGmtMillis = conn.getUseGmtMillisForDatetimes();
                                if (useGmtMillis) {
                                    try {
                                        instance = Calendar.getInstance(TimeZone.getTimeZone("GMT"));
                                    } catch (NumberFormatException e2) {
                                        e = e2;
                                        second = second2;
                                        minute2 = fractionalSecond2;
                                        sessionCalendar = hour2;
                                        hour = e;
                                        stringBuilder = new StringBuilder();
                                        stringBuilder.append("Syntax error in TIMESTAMP escape sequence '");
                                        stringBuilder.append(str);
                                        stringBuilder.append("'.");
                                        throw SQLError.createSQLException(stringBuilder.toString(), SQLError.SQL_STATE_ILLEGAL_ARGUMENT, conn.getExceptionInterceptor());
                                    }
                                }
                                instance = null;
                                second = second2;
                                Timestamp second3 = TimeUtil.fastTimestampCreate(useGmtMillis, instance, sessionCalendar2, year4Int, month2Int, day2Int, hourInt, minuteInt, secondInt, 0);
                                String year42 = year4;
                                Calendar year43 = sessionCalendar2;
                                try {
                                    str2 = day2;
                                    str3 = month2;
                                    str4 = minute2;
                                    String fractionalSecond3 = fractionalSecond2;
                                    calendar = year43;
                                    fractionalSecond = year42;
                                    try {
                                        Timestamp inServerTimezone = TimeUtil.changeTimezone(mySQLConnection, year43, null, second3, year43.getTimeZone(), conn.getServerTimezoneTZ(), (boolean) null);
                                        stringBuilder2.append("'");
                                        day2 = inServerTimezone.toString();
                                        month2 = day2.indexOf(".");
                                        if (month2 != -1) {
                                            try {
                                                day2 = day2.substring(0, month2);
                                            } catch (NumberFormatException e3) {
                                                hour = e3;
                                                minute2 = fractionalSecond3;
                                                stringBuilder = new StringBuilder();
                                                stringBuilder.append("Syntax error in TIMESTAMP escape sequence '");
                                                stringBuilder.append(str);
                                                stringBuilder.append("'.");
                                                throw SQLError.createSQLException(stringBuilder.toString(), SQLError.SQL_STATE_ILLEGAL_ARGUMENT, conn.getExceptionInterceptor());
                                            }
                                        }
                                        stringBuilder2.append(day2);
                                        if (serverSupportsFractionalSecond) {
                                            try {
                                                stringBuilder2.append(fractionalSecond3);
                                            } catch (NumberFormatException e4) {
                                                e3 = e4;
                                                hour = e3;
                                                stringBuilder = new StringBuilder();
                                                stringBuilder.append("Syntax error in TIMESTAMP escape sequence '");
                                                stringBuilder.append(str);
                                                stringBuilder.append("'.");
                                                throw SQLError.createSQLException(stringBuilder.toString(), SQLError.SQL_STATE_ILLEGAL_ARGUMENT, conn.getExceptionInterceptor());
                                            }
                                        }
                                        stringBuilder2.append("'");
                                    } catch (NumberFormatException e32) {
                                        minute2 = fractionalSecond3;
                                        hour = e32;
                                        stringBuilder = new StringBuilder();
                                        stringBuilder.append("Syntax error in TIMESTAMP escape sequence '");
                                        stringBuilder.append(str);
                                        stringBuilder.append("'.");
                                        throw SQLError.createSQLException(stringBuilder.toString(), SQLError.SQL_STATE_ILLEGAL_ARGUMENT, conn.getExceptionInterceptor());
                                    }
                                } catch (NumberFormatException e322) {
                                    sessionCalendar = hour2;
                                    str2 = day2;
                                    str3 = month2;
                                    str4 = minute2;
                                    minute2 = fractionalSecond2;
                                    calendar = year43;
                                    fractionalSecond = year42;
                                    hour = e322;
                                    stringBuilder = new StringBuilder();
                                    stringBuilder.append("Syntax error in TIMESTAMP escape sequence '");
                                    stringBuilder.append(str);
                                    stringBuilder.append("'.");
                                    throw SQLError.createSQLException(stringBuilder.toString(), SQLError.SQL_STATE_ILLEGAL_ARGUMENT, conn.getExceptionInterceptor());
                                }
                            } catch (NumberFormatException e3222) {
                                str2 = day2;
                                str3 = month2;
                                second = second2;
                                str4 = minute2;
                                fractionalSecond = year4;
                                calendar = sessionCalendar2;
                                hour = e3222;
                                stringBuilder = new StringBuilder();
                                stringBuilder.append("Syntax error in TIMESTAMP escape sequence '");
                                stringBuilder.append(str);
                                stringBuilder.append("'.");
                                throw SQLError.createSQLException(stringBuilder.toString(), SQLError.SQL_STATE_ILLEGAL_ARGUMENT, conn.getExceptionInterceptor());
                            }
                        }
                        stringBuilder2.append("'");
                        stringBuilder2.append(year4);
                        stringBuilder2.append("-");
                        stringBuilder2.append(month2);
                        stringBuilder2.append("-");
                        stringBuilder2.append(day2);
                        stringBuilder2.append(" ");
                        stringBuilder2.append(hour2);
                        stringBuilder2.append(":");
                        stringBuilder2.append(minute);
                        stringBuilder2.append(":");
                        stringBuilder2.append(second);
                        stringBuilder2.append(fractionalSecond2);
                        stringBuilder2.append("'");
                    } else {
                        Timestamp ts = Timestamp.valueOf(argument);
                        SimpleDateFormat tsdf = new SimpleDateFormat("''yyyy-MM-dd HH:mm:ss", Locale.US);
                        tsdf.setTimeZone(conn.getServerTimezoneTZ());
                        stringBuilder2.append(tsdf.format(ts));
                        if (ts.getNanos() > 0 && mySQLConnection.versionMeetsMinimum(5, 6, 4)) {
                            stringBuilder2.append('.');
                            stringBuilder2.append(TimeUtil.formatNanos(ts.getNanos(), true, true));
                        }
                        stringBuilder2.append('\'');
                    }
                    return;
                } catch (NoSuchElementException e5) {
                    stringBuilder = new StringBuilder();
                    stringBuilder.append("Syntax error for TIMESTAMP escape sequence '");
                    stringBuilder.append(argument);
                    stringBuilder.append("'");
                    throw SQLError.createSQLException(stringBuilder.toString(), SQLError.SQL_STATE_SYNTAX_ERROR, conn.getExceptionInterceptor());
                } catch (IllegalArgumentException e6) {
                    IllegalArgumentException illegalArgumentException = e6;
                    stringBuilder = new StringBuilder();
                    stringBuilder.append("Syntax error for TIMESTAMP escape sequence '");
                    stringBuilder.append(argument);
                    stringBuilder.append("'");
                    SQLException sqlEx = SQLError.createSQLException(stringBuilder.toString(), SQLError.SQL_STATE_SYNTAX_ERROR, conn.getExceptionInterceptor());
                    sqlEx.initCause(illegalArgumentException);
                    throw sqlEx;
                }
            }
        }
        newSql.append(token);
    }

    private static String processConvertToken(String functionToken, boolean serverSupportsConvertFn, MySQLConnection conn) throws SQLException {
        int firstIndexOfParen = functionToken.indexOf("(");
        if (firstIndexOfParen == -1) {
            StringBuilder stringBuilder = new StringBuilder();
            stringBuilder.append("Syntax error while processing {fn convert (... , ...)} token, missing opening parenthesis in token '");
            stringBuilder.append(functionToken);
            stringBuilder.append("'.");
            throw SQLError.createSQLException(stringBuilder.toString(), SQLError.SQL_STATE_SYNTAX_ERROR, conn.getExceptionInterceptor());
        }
        int indexOfComma = functionToken.lastIndexOf(",");
        if (indexOfComma == -1) {
            stringBuilder = new StringBuilder();
            stringBuilder.append("Syntax error while processing {fn convert (... , ...)} token, missing comma in token '");
            stringBuilder.append(functionToken);
            stringBuilder.append("'.");
            throw SQLError.createSQLException(stringBuilder.toString(), SQLError.SQL_STATE_SYNTAX_ERROR, conn.getExceptionInterceptor());
        }
        int indexOfCloseParen = functionToken.indexOf(41, indexOfComma);
        if (indexOfCloseParen == -1) {
            stringBuilder = new StringBuilder();
            stringBuilder.append("Syntax error while processing {fn convert (... , ...)} token, missing closing parenthesis in token '");
            stringBuilder.append(functionToken);
            stringBuilder.append("'.");
            throw SQLError.createSQLException(stringBuilder.toString(), SQLError.SQL_STATE_SYNTAX_ERROR, conn.getExceptionInterceptor());
        }
        String newType;
        String expression = functionToken.substring(firstIndexOfParen + 1, indexOfComma);
        String type = functionToken.substring(indexOfComma + 1, indexOfCloseParen);
        String trimmedType = type.trim();
        if (StringUtils.startsWithIgnoreCase(trimmedType, "SQL_")) {
            trimmedType = trimmedType.substring(4, trimmedType.length());
        }
        if (serverSupportsConvertFn) {
            newType = (String) JDBC_CONVERT_TO_MYSQL_TYPE_MAP.get(trimmedType.toUpperCase(Locale.ENGLISH));
        } else {
            newType = (String) JDBC_NO_CONVERT_TO_MYSQL_EXPRESSION_MAP.get(trimmedType.toUpperCase(Locale.ENGLISH));
            if (newType == null) {
                stringBuilder = new StringBuilder();
                stringBuilder.append("Can't find conversion re-write for type '");
                stringBuilder.append(type);
                stringBuilder.append("' that is applicable for this server version while processing escape tokens.");
                throw SQLError.createSQLException(stringBuilder.toString(), SQLError.SQL_STATE_GENERAL_ERROR, conn.getExceptionInterceptor());
            }
        }
        if (newType == null) {
            stringBuilder = new StringBuilder();
            stringBuilder.append("Unsupported conversion type '");
            stringBuilder.append(type.trim());
            stringBuilder.append("' found while processing escape token.");
            throw SQLError.createSQLException(stringBuilder.toString(), SQLError.SQL_STATE_GENERAL_ERROR, conn.getExceptionInterceptor());
        }
        int replaceIndex = newType.indexOf("?");
        if (replaceIndex != -1) {
            stringBuilder = new StringBuilder(newType.substring(0, replaceIndex));
            stringBuilder.append(expression);
            stringBuilder.append(newType.substring(replaceIndex + 1, newType.length()));
            return stringBuilder.toString();
        }
        stringBuilder = new StringBuilder("CAST(");
        stringBuilder.append(expression);
        stringBuilder.append(" AS ");
        stringBuilder.append(newType);
        stringBuilder.append(")");
        return stringBuilder.toString();
    }

    private static String removeWhitespace(String toCollapse) {
        if (toCollapse == null) {
            return null;
        }
        int length = toCollapse.length();
        StringBuilder collapsed = new StringBuilder(length);
        for (int i = 0; i < length; i++) {
            char c = toCollapse.charAt(i);
            if (!Character.isWhitespace(c)) {
                collapsed.append(c);
            }
        }
        return collapsed.toString();
    }
}
