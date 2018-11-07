package com.mysql.jdbc;

import java.io.ByteArrayOutputStream;
import java.io.UnsupportedEncodingException;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.math.BigDecimal;
import java.nio.ByteBuffer;
import java.nio.CharBuffer;
import java.nio.charset.Charset;
import java.nio.charset.IllegalCharsetNameException;
import java.nio.charset.UnsupportedCharsetException;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.EnumSet;
import java.util.List;
import java.util.Set;
import java.util.StringTokenizer;
import java.util.concurrent.ConcurrentHashMap;

public class StringUtils {
    private static final int BYTE_RANGE = 256;
    private static final char[] HEX_DIGITS = new char[]{'0', '1', '2', '3', '4', '5', '6', '7', '8', '9', 'a', 'b', 'c', 'd', 'e', 'f'};
    private static final int NON_COMMENTS_MYSQL_VERSION_REF_LENGTH = 5;
    public static final Set<SearchMode> SEARCH_MODE__ALL = Collections.unmodifiableSet(EnumSet.allOf(SearchMode.class));
    public static final Set<SearchMode> SEARCH_MODE__BSESC_COM_WS = Collections.unmodifiableSet(EnumSet.of(SearchMode.ALLOW_BACKSLASH_ESCAPE, SearchMode.SKIP_BLOCK_COMMENTS, SearchMode.SKIP_LINE_COMMENTS, SearchMode.SKIP_WHITE_SPACE));
    public static final Set<SearchMode> SEARCH_MODE__BSESC_MRK_WS = Collections.unmodifiableSet(EnumSet.of(SearchMode.ALLOW_BACKSLASH_ESCAPE, SearchMode.SKIP_BETWEEN_MARKERS, SearchMode.SKIP_WHITE_SPACE));
    public static final Set<SearchMode> SEARCH_MODE__COM_WS = Collections.unmodifiableSet(EnumSet.of(SearchMode.SKIP_BLOCK_COMMENTS, SearchMode.SKIP_LINE_COMMENTS, SearchMode.SKIP_WHITE_SPACE));
    public static final Set<SearchMode> SEARCH_MODE__MRK_COM_WS = Collections.unmodifiableSet(EnumSet.of(SearchMode.SKIP_BETWEEN_MARKERS, SearchMode.SKIP_BLOCK_COMMENTS, SearchMode.SKIP_LINE_COMMENTS, SearchMode.SKIP_WHITE_SPACE));
    public static final Set<SearchMode> SEARCH_MODE__MRK_WS = Collections.unmodifiableSet(EnumSet.of(SearchMode.SKIP_BETWEEN_MARKERS, SearchMode.SKIP_WHITE_SPACE));
    public static final Set<SearchMode> SEARCH_MODE__NONE = Collections.unmodifiableSet(EnumSet.noneOf(SearchMode.class));
    private static final String VALID_ID_CHARS = "abcdefghijklmnopqrstuvwxyzABCDEFGHIGKLMNOPQRSTUVWXYZ0123456789$_#@";
    static final char WILDCARD_ESCAPE = '\\';
    static final char WILDCARD_MANY = '%';
    static final char WILDCARD_ONE = '_';
    private static final int WILD_COMPARE_CONTINUE_WITH_WILD = 1;
    private static final int WILD_COMPARE_MATCH = 0;
    private static final int WILD_COMPARE_NO_MATCH = -1;
    private static byte[] allBytes = new byte[256];
    private static char[] byteToChars = new char[256];
    private static final ConcurrentHashMap<String, Charset> charsetsByAlias = new ConcurrentHashMap();
    private static final String platformEncoding = System.getProperty("file.encoding");
    private static Method toPlainStringMethod;

    public enum SearchMode {
        ALLOW_BACKSLASH_ESCAPE,
        SKIP_BETWEEN_MARKERS,
        SKIP_BLOCK_COMMENTS,
        SKIP_LINE_COMMENTS,
        SKIP_WHITE_SPACE
    }

    static {
        for (int i = -128; i <= 127; i++) {
            allBytes[i + 128] = (byte) i;
        }
        String allBytesString = new String(allBytes, 0, 255);
        int allBytesStringLen = allBytesString.length();
        int i2 = 0;
        while (i2 < 255 && i2 < allBytesStringLen) {
            byteToChars[i2] = allBytesString.charAt(i2);
            i2++;
        }
        try {
            toPlainStringMethod = BigDecimal.class.getMethod("toPlainString", new Class[0]);
        } catch (NoSuchMethodException e) {
        }
    }

    static Charset findCharset(String alias) throws UnsupportedEncodingException {
        try {
            Charset cs = (Charset) charsetsByAlias.get(alias);
            if (cs != null) {
                return cs;
            }
            cs = Charset.forName(alias);
            Charset oldCs = (Charset) charsetsByAlias.putIfAbsent(alias, cs);
            if (oldCs != null) {
                return oldCs;
            }
            return cs;
        } catch (UnsupportedCharsetException e) {
            throw new UnsupportedEncodingException(alias);
        } catch (IllegalCharsetNameException e2) {
            throw new UnsupportedEncodingException(alias);
        } catch (IllegalArgumentException e3) {
            throw new UnsupportedEncodingException(alias);
        }
    }

    public static String consistentToString(BigDecimal decimal) {
        if (decimal == null) {
            return null;
        }
        if (toPlainStringMethod != null) {
            try {
                return (String) toPlainStringMethod.invoke(decimal, (Object[]) null);
            } catch (InvocationTargetException e) {
            } catch (IllegalAccessException e2) {
            }
        }
        return decimal.toString();
    }

    public static String dumpAsHex(byte[] byteBuffer, int length) {
        int i;
        int j;
        int i2 = length;
        StringBuilder outputBuilder = new StringBuilder(i2 * 4);
        int rows = i2 / 8;
        int p = 0;
        for (i = 0; i < rows && p < i2; i++) {
            int ptemp = p;
            for (int j2 = 0; j2 < 8; j2++) {
                String hexVal = Integer.toHexString(byteBuffer[ptemp] & 255);
                if (hexVal.length() == 1) {
                    StringBuilder stringBuilder = new StringBuilder();
                    stringBuilder.append("0");
                    stringBuilder.append(hexVal);
                    hexVal = stringBuilder.toString();
                }
                StringBuilder stringBuilder2 = new StringBuilder();
                stringBuilder2.append(hexVal);
                stringBuilder2.append(" ");
                outputBuilder.append(stringBuilder2.toString());
                ptemp++;
            }
            outputBuilder.append("    ");
            for (j = 0; j < 8; j++) {
                int b = byteBuffer[p] & 255;
                if (b <= 32 || b >= 127) {
                    outputBuilder.append(". ");
                } else {
                    StringBuilder stringBuilder3 = new StringBuilder();
                    stringBuilder3.append((char) b);
                    stringBuilder3.append(" ");
                    outputBuilder.append(stringBuilder3.toString());
                }
                p++;
            }
            outputBuilder.append("\n");
        }
        j = 0;
        for (i = p; i < i2; i++) {
            StringBuilder stringBuilder4;
            String hexVal2 = Integer.toHexString(byteBuffer[i] & 255);
            if (hexVal2.length() == 1) {
                stringBuilder4 = new StringBuilder();
                stringBuilder4.append("0");
                stringBuilder4.append(hexVal2);
                hexVal2 = stringBuilder4.toString();
            }
            stringBuilder4 = new StringBuilder();
            stringBuilder4.append(hexVal2);
            stringBuilder4.append(" ");
            outputBuilder.append(stringBuilder4.toString());
            j++;
        }
        for (i = j; i < 8; i++) {
            outputBuilder.append("   ");
        }
        outputBuilder.append("    ");
        for (i = p; i < i2; i++) {
            int b2 = byteBuffer[i] & 255;
            if (b2 <= 32 || b2 >= 127) {
                outputBuilder.append(". ");
            } else {
                StringBuilder stringBuilder5 = new StringBuilder();
                stringBuilder5.append((char) b2);
                stringBuilder5.append(" ");
                outputBuilder.append(stringBuilder5.toString());
            }
        }
        outputBuilder.append("\n");
        return outputBuilder.toString();
    }

    private static boolean endsWith(byte[] dataFrom, String suffix) {
        for (int i = 1; i <= suffix.length(); i++) {
            if (dataFrom[dataFrom.length - i] != suffix.charAt(suffix.length() - i)) {
                return false;
            }
        }
        return true;
    }

    public static byte[] escapeEasternUnicodeByteStream(byte[] origBytes, String origString) {
        if (origBytes == null) {
            return null;
        }
        if (origBytes.length == 0) {
            return new byte[0];
        }
        int bytesLen = origBytes.length;
        int bufIndex = 0;
        int strIndex = 0;
        ByteArrayOutputStream bytesOut = new ByteArrayOutputStream(bytesLen);
        while (true) {
            int bufIndex2;
            if (origString.charAt(strIndex) == WILDCARD_ESCAPE) {
                bufIndex2 = bufIndex + 1;
                bytesOut.write(origBytes[bufIndex]);
                bufIndex = bufIndex2;
            } else {
                bufIndex2 = origBytes[bufIndex];
                if (bufIndex2 < 0) {
                    bufIndex2 += 256;
                }
                bytesOut.write(bufIndex2);
                int hiByte;
                if (bufIndex2 >= 128) {
                    if (bufIndex < bytesLen - 1) {
                        hiByte = origBytes[bufIndex + 1];
                        if (hiByte < 0) {
                            hiByte += 256;
                        }
                        bytesOut.write(hiByte);
                        bufIndex++;
                        if (hiByte == 92) {
                            bytesOut.write(hiByte);
                        }
                    }
                } else if (bufIndex2 == 92 && bufIndex < bytesLen - 1) {
                    hiByte = origBytes[bufIndex + 1];
                    if (hiByte < 0) {
                        hiByte += 256;
                    }
                    if (hiByte == 98) {
                        bytesOut.write(92);
                        bytesOut.write(98);
                        bufIndex++;
                    }
                }
                bufIndex++;
            }
            if (bufIndex >= bytesLen) {
                return bytesOut.toByteArray();
            }
            strIndex++;
        }
    }

    public static char firstNonWsCharUc(String searchIn) {
        return firstNonWsCharUc(searchIn, 0);
    }

    public static char firstNonWsCharUc(String searchIn, int startAt) {
        if (searchIn == null) {
            return '\u0000';
        }
        int length = searchIn.length();
        for (int i = startAt; i < length; i++) {
            char c = searchIn.charAt(i);
            if (!Character.isWhitespace(c)) {
                return Character.toUpperCase(c);
            }
        }
        return '\u0000';
    }

    public static char firstAlphaCharUc(String searchIn, int startAt) {
        if (searchIn == null) {
            return '\u0000';
        }
        int length = searchIn.length();
        for (int i = startAt; i < length; i++) {
            char c = searchIn.charAt(i);
            if (Character.isLetter(c)) {
                return Character.toUpperCase(c);
            }
        }
        return '\u0000';
    }

    public static String fixDecimalExponent(String dString) {
        int ePos = dString.indexOf(69);
        if (ePos == -1) {
            ePos = dString.indexOf(101);
        }
        if (ePos == -1 || dString.length() <= ePos + 1) {
            return dString;
        }
        char maybeMinusChar = dString.charAt(ePos + 1);
        if (maybeMinusChar == '-' || maybeMinusChar == '+') {
            return dString;
        }
        StringBuilder strBuilder = new StringBuilder(dString.length() + 1);
        strBuilder.append(dString.substring(0, ePos + 1));
        strBuilder.append('+');
        strBuilder.append(dString.substring(ePos + 1, dString.length()));
        return strBuilder.toString();
    }

    public static byte[] getBytes(char[] c, SingleByteCharsetConverter converter, String encoding, String serverEncoding, boolean parserKnowsUnicode, ExceptionInterceptor exceptionInterceptor) throws SQLException {
        UnsupportedEncodingException uee;
        if (converter != null) {
            try {
                uee = converter.toBytes(c);
            } catch (UnsupportedEncodingException e) {
                StringBuilder stringBuilder = new StringBuilder();
                stringBuilder.append(Messages.getString("StringUtils.0"));
                stringBuilder.append(encoding);
                stringBuilder.append(Messages.getString("StringUtils.1"));
                throw SQLError.createSQLException(stringBuilder.toString(), SQLError.SQL_STATE_ILLEGAL_ARGUMENT, exceptionInterceptor);
            }
        } else if (encoding == null) {
            uee = getBytes(c);
        } else {
            uee = getBytes(c, encoding);
            if (parserKnowsUnicode || !CharsetMapping.requiresEscapeEasternUnicode(encoding) || encoding.equalsIgnoreCase(serverEncoding)) {
                return uee;
            }
            return escapeEasternUnicodeByteStream(uee, new String(c));
        }
        return uee;
    }

    public static byte[] getBytes(char[] c, SingleByteCharsetConverter converter, String encoding, String serverEncoding, int offset, int length, boolean parserKnowsUnicode, ExceptionInterceptor exceptionInterceptor) throws SQLException {
        UnsupportedEncodingException uee;
        if (converter != null) {
            try {
                uee = converter.toBytes(c, offset, length);
            } catch (UnsupportedEncodingException e) {
                StringBuilder stringBuilder = new StringBuilder();
                stringBuilder.append(Messages.getString("StringUtils.0"));
                stringBuilder.append(encoding);
                stringBuilder.append(Messages.getString("StringUtils.1"));
                throw SQLError.createSQLException(stringBuilder.toString(), SQLError.SQL_STATE_ILLEGAL_ARGUMENT, exceptionInterceptor);
            }
        } else if (encoding == null) {
            uee = getBytes(c, offset, length);
        } else {
            uee = getBytes(c, offset, length, encoding);
            if (parserKnowsUnicode || !CharsetMapping.requiresEscapeEasternUnicode(encoding) || encoding.equalsIgnoreCase(serverEncoding)) {
                return uee;
            }
            return escapeEasternUnicodeByteStream(uee, new String(c, offset, length));
        }
        return uee;
    }

    public static byte[] getBytes(char[] c, String encoding, String serverEncoding, boolean parserKnowsUnicode, MySQLConnection conn, ExceptionInterceptor exceptionInterceptor) throws SQLException {
        SingleByteCharsetConverter charsetConverter;
        if (conn != null) {
            try {
                charsetConverter = conn.getCharsetConverter(encoding);
            } catch (UnsupportedEncodingException e) {
                StringBuilder stringBuilder = new StringBuilder();
                stringBuilder.append(Messages.getString("StringUtils.0"));
                stringBuilder.append(encoding);
                stringBuilder.append(Messages.getString("StringUtils.1"));
                throw SQLError.createSQLException(stringBuilder.toString(), SQLError.SQL_STATE_ILLEGAL_ARGUMENT, exceptionInterceptor);
            }
        }
        charsetConverter = SingleByteCharsetConverter.getInstance(encoding, null);
        return getBytes(c, charsetConverter, encoding, serverEncoding, parserKnowsUnicode, exceptionInterceptor);
    }

    public static byte[] getBytes(String s, SingleByteCharsetConverter converter, String encoding, String serverEncoding, boolean parserKnowsUnicode, ExceptionInterceptor exceptionInterceptor) throws SQLException {
        UnsupportedEncodingException uee;
        if (converter != null) {
            try {
                uee = converter.toBytes(s);
            } catch (UnsupportedEncodingException e) {
                StringBuilder stringBuilder = new StringBuilder();
                stringBuilder.append(Messages.getString("StringUtils.5"));
                stringBuilder.append(encoding);
                stringBuilder.append(Messages.getString("StringUtils.6"));
                throw SQLError.createSQLException(stringBuilder.toString(), SQLError.SQL_STATE_ILLEGAL_ARGUMENT, exceptionInterceptor);
            }
        } else if (encoding == null) {
            uee = getBytes(s);
        } else {
            uee = getBytes(s, encoding);
            if (parserKnowsUnicode || !CharsetMapping.requiresEscapeEasternUnicode(encoding) || encoding.equalsIgnoreCase(serverEncoding)) {
                return uee;
            }
            return escapeEasternUnicodeByteStream(uee, s);
        }
        return uee;
    }

    public static byte[] getBytes(String s, SingleByteCharsetConverter converter, String encoding, String serverEncoding, int offset, int length, boolean parserKnowsUnicode, ExceptionInterceptor exceptionInterceptor) throws SQLException {
        UnsupportedEncodingException uee;
        if (converter != null) {
            try {
                uee = converter.toBytes(s, offset, length);
            } catch (UnsupportedEncodingException e) {
                StringBuilder stringBuilder = new StringBuilder();
                stringBuilder.append(Messages.getString("StringUtils.5"));
                stringBuilder.append(encoding);
                stringBuilder.append(Messages.getString("StringUtils.6"));
                throw SQLError.createSQLException(stringBuilder.toString(), SQLError.SQL_STATE_ILLEGAL_ARGUMENT, exceptionInterceptor);
            }
        } else if (encoding == null) {
            uee = getBytes(s, offset, length);
        } else {
            s = s.substring(offset, offset + length);
            uee = getBytes(s, encoding);
            if (parserKnowsUnicode || !CharsetMapping.requiresEscapeEasternUnicode(encoding) || encoding.equalsIgnoreCase(serverEncoding)) {
                return uee;
            }
            return escapeEasternUnicodeByteStream(uee, s);
        }
        return uee;
    }

    public static byte[] getBytes(String s, String encoding, String serverEncoding, boolean parserKnowsUnicode, MySQLConnection conn, ExceptionInterceptor exceptionInterceptor) throws SQLException {
        SingleByteCharsetConverter charsetConverter;
        if (conn != null) {
            try {
                charsetConverter = conn.getCharsetConverter(encoding);
            } catch (UnsupportedEncodingException e) {
                StringBuilder stringBuilder = new StringBuilder();
                stringBuilder.append(Messages.getString("StringUtils.5"));
                stringBuilder.append(encoding);
                stringBuilder.append(Messages.getString("StringUtils.6"));
                throw SQLError.createSQLException(stringBuilder.toString(), SQLError.SQL_STATE_ILLEGAL_ARGUMENT, exceptionInterceptor);
            }
        }
        charsetConverter = SingleByteCharsetConverter.getInstance(encoding, null);
        return getBytes(s, charsetConverter, encoding, serverEncoding, parserKnowsUnicode, exceptionInterceptor);
    }

    public static final byte[] getBytes(String s, String encoding, String serverEncoding, int offset, int length, boolean parserKnowsUnicode, MySQLConnection conn, ExceptionInterceptor exceptionInterceptor) throws SQLException {
        SingleByteCharsetConverter charsetConverter;
        String str = encoding;
        MySQLConnection mySQLConnection = conn;
        if (mySQLConnection != null) {
            try {
                charsetConverter = mySQLConnection.getCharsetConverter(str);
            } catch (UnsupportedEncodingException e) {
                UnsupportedEncodingException unsupportedEncodingException = e;
                StringBuilder stringBuilder = new StringBuilder();
                stringBuilder.append(Messages.getString("StringUtils.5"));
                stringBuilder.append(str);
                stringBuilder.append(Messages.getString("StringUtils.6"));
                throw SQLError.createSQLException(stringBuilder.toString(), SQLError.SQL_STATE_ILLEGAL_ARGUMENT, exceptionInterceptor);
            }
        }
        charsetConverter = SingleByteCharsetConverter.getInstance(str, null);
        return getBytes(s, charsetConverter, str, serverEncoding, offset, length, parserKnowsUnicode, exceptionInterceptor);
    }

    public static byte[] getBytesWrapped(String s, char beginWrap, char endWrap, SingleByteCharsetConverter converter, String encoding, String serverEncoding, boolean parserKnowsUnicode, ExceptionInterceptor exceptionInterceptor) throws SQLException {
        if (converter != null) {
            try {
                return converter.toBytesWrapped(s, beginWrap, endWrap);
            } catch (UnsupportedEncodingException e) {
                StringBuilder stringBuilder = new StringBuilder();
                stringBuilder.append(Messages.getString("StringUtils.10"));
                stringBuilder.append(encoding);
                stringBuilder.append(Messages.getString("StringUtils.11"));
                throw SQLError.createSQLException(stringBuilder.toString(), SQLError.SQL_STATE_ILLEGAL_ARGUMENT, exceptionInterceptor);
            }
        } else if (encoding == null) {
            strBuilder = new StringBuilder(s.length() + 2);
            strBuilder.append(beginWrap);
            strBuilder.append(s);
            strBuilder.append(endWrap);
            return getBytes(strBuilder.toString());
        } else {
            strBuilder = new StringBuilder(s.length() + 2);
            strBuilder.append(beginWrap);
            strBuilder.append(s);
            strBuilder.append(endWrap);
            s = strBuilder.toString();
            byte[] b = getBytes(s, encoding);
            if (parserKnowsUnicode || !CharsetMapping.requiresEscapeEasternUnicode(encoding) || encoding.equalsIgnoreCase(serverEncoding)) {
                return b;
            }
            return escapeEasternUnicodeByteStream(b, s);
        }
    }

    public static int getInt(byte[] buf) throws NumberFormatException {
        return getInt(buf, 0, buf.length);
    }

    public static int getInt(byte[] buf, int offset, int endPos) throws NumberFormatException {
        int s = offset;
        while (s < endPos && Character.isWhitespace((char) buf[s])) {
            s++;
        }
        if (s == endPos) {
            throw new NumberFormatException(toString(buf));
        }
        boolean negative = false;
        if (((char) buf[s]) == '-') {
            negative = true;
            s++;
        } else if (((char) buf[s]) == '+') {
            s++;
        }
        int save = s;
        int cutoff = ActivityChooserViewAdapter.MAX_ACTIVITY_COUNT_UNLIMITED / 10;
        char cutlim = ActivityChooserViewAdapter.MAX_ACTIVITY_COUNT_UNLIMITED % 10;
        if (negative) {
            cutlim++;
        }
        boolean overflow = false;
        int i = 0;
        while (s < endPos) {
            char c = (char) buf[s];
            if (!Character.isDigit(c)) {
                if (!Character.isLetter(c)) {
                    break;
                }
                c = (char) ((Character.toUpperCase(c) - 65) + 10);
            } else {
                c = (char) (c - 48);
            }
            if (c >= '\n') {
                break;
            }
            if (i <= cutoff) {
                if (i != cutoff || c <= cutlim) {
                    i = (i * 10) + c;
                    s++;
                }
            }
            overflow = true;
            s++;
        }
        if (s == save) {
            throw new NumberFormatException(toString(buf));
        } else if (!overflow) {
            return negative ? -i : i;
        } else {
            throw new NumberFormatException(toString(buf));
        }
    }

    public static long getLong(byte[] buf) throws NumberFormatException {
        return getLong(buf, 0, buf.length);
    }

    public static long getLong(byte[] buf, int offset, int endpos) throws NumberFormatException {
        int i = endpos;
        int s = offset;
        while (s < i && Character.isWhitespace((char) buf[s])) {
            s++;
        }
        if (s == i) {
            throw new NumberFormatException(toString(buf));
        }
        boolean negative = false;
        if (((char) buf[s]) == '-') {
            negative = true;
            s++;
        } else if (((char) buf[s]) == '+') {
            s++;
        }
        int save = s;
        long cutoff = Long.MAX_VALUE / ((long) 10);
        long cutlim = (long) ((int) (Long.MAX_VALUE % ((long) 10)));
        long cutlim2;
        if (negative) {
            cutlim2 = cutlim + 1;
        } else {
            cutlim2 = cutlim;
        }
        boolean overflow = false;
        long i2 = 0;
        while (s < i) {
            char c = (char) buf[s];
            if (!Character.isDigit(c)) {
                if (!Character.isLetter(c)) {
                    break;
                }
                c = (char) ((Character.toUpperCase(c) - 65) + 10);
            } else {
                c = (char) (c - 48);
            }
            if (c >= '\n') {
                break;
            }
            if (i2 <= cutoff) {
                if (i2 != cutoff || ((long) c) <= cutlim) {
                    i2 = (i2 * ((long) 10)) + ((long) c);
                    s++;
                }
            }
            overflow = true;
            s++;
        }
        if (s == save) {
            throw new NumberFormatException(toString(buf));
        } else if (!overflow) {
            return negative ? -i2 : i2;
        } else {
            throw new NumberFormatException(toString(buf));
        }
    }

    public static short getShort(byte[] buf) throws NumberFormatException {
        return getShort(buf, 0, buf.length);
    }

    public static short getShort(byte[] buf, int offset, int endpos) throws NumberFormatException {
        int s = offset;
        while (s < endpos && Character.isWhitespace((char) buf[s])) {
            s++;
        }
        if (s == endpos) {
            throw new NumberFormatException(toString(buf));
        }
        boolean negative = false;
        if (((char) buf[s]) == '-') {
            negative = true;
            s++;
        } else if (((char) buf[s]) == '+') {
            s++;
        }
        int save = s;
        short cutoff = (short) (Short.MAX_VALUE / (short) 10);
        char cutlim = (short) (Short.MAX_VALUE % (short) 10);
        if (negative) {
            cutlim = (short) (cutlim + 1);
        }
        boolean overflow = false;
        short i = (short) 0;
        while (s < endpos) {
            char c = (char) buf[s];
            if (!Character.isDigit(c)) {
                if (!Character.isLetter(c)) {
                    break;
                }
                c = (char) ((Character.toUpperCase(c) - 65) + 10);
            } else {
                c = (char) (c - 48);
            }
            if (c >= '\n') {
                break;
            }
            if (i <= cutoff) {
                if (i != cutoff || c <= cutlim) {
                    i = (short) (((short) (i * 10)) + c);
                    s++;
                }
            }
            overflow = true;
            s++;
        }
        if (s == save) {
            throw new NumberFormatException(toString(buf));
        } else if (!overflow) {
            return negative ? (short) (-i) : i;
        } else {
            throw new NumberFormatException(toString(buf));
        }
    }

    public static int indexOfIgnoreCase(String searchIn, String searchFor) {
        return indexOfIgnoreCase(0, searchIn, searchFor);
    }

    public static int indexOfIgnoreCase(int startingPosition, String searchIn, String searchFor) {
        if (searchIn != null) {
            if (searchFor != null) {
                int searchInLength = searchIn.length();
                int searchForLength = searchFor.length();
                int stopSearchingAt = searchInLength - searchForLength;
                if (startingPosition <= stopSearchingAt) {
                    if (searchForLength != 0) {
                        char firstCharOfSearchForUc = Character.toUpperCase(searchFor.charAt(0));
                        char firstCharOfSearchForLc = Character.toLowerCase(searchFor.charAt(0));
                        int i = startingPosition;
                        while (i <= stopSearchingAt) {
                            if (isCharAtPosNotEqualIgnoreCase(searchIn, i, firstCharOfSearchForUc, firstCharOfSearchForLc)) {
                                while (true) {
                                    i++;
                                    if (i > stopSearchingAt || !isCharAtPosNotEqualIgnoreCase(searchIn, i, firstCharOfSearchForUc, firstCharOfSearchForLc)) {
                                        break;
                                    }
                                }
                            }
                            if (i <= stopSearchingAt && startsWithIgnoreCase(searchIn, i, searchFor)) {
                                return i;
                            }
                            i++;
                        }
                        return -1;
                    }
                }
                return -1;
            }
        }
        return -1;
    }

    /* JADX WARNING: inconsistent code. */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    public static int indexOfIgnoreCase(int r23, java.lang.String r24, java.lang.String[] r25, java.lang.String r26, java.lang.String r27, java.util.Set<com.mysql.jdbc.StringUtils.SearchMode> r28) {
        /*
        r7 = r24;
        r8 = r25;
        r0 = r28;
        r9 = -1;
        if (r7 == 0) goto L_0x011e;
    L_0x0009:
        if (r8 != 0) goto L_0x000d;
    L_0x000b:
        goto L_0x011e;
    L_0x000d:
        r10 = r24.length();
        r1 = 0;
        r2 = r8;
        r11 = 0;
        r3 = r2.length;
        r4 = r1;
        r1 = r11;
    L_0x0017:
        if (r1 >= r3) goto L_0x0023;
    L_0x0019:
        r5 = r2[r1];
        r6 = r5.length();
        r4 = r4 + r6;
        r1 = r1 + 1;
        goto L_0x0017;
    L_0x0023:
        if (r4 != 0) goto L_0x0026;
    L_0x0025:
        return r9;
    L_0x0026:
        r12 = r8.length;
        if (r12 <= 0) goto L_0x002c;
    L_0x0029:
        r1 = r12 + -1;
        goto L_0x002d;
    L_0x002c:
        r1 = r11;
    L_0x002d:
        r13 = r4 + r1;
        r6 = r10 - r13;
        r5 = r23;
        if (r5 <= r6) goto L_0x0036;
    L_0x0035:
        return r9;
    L_0x0036:
        r1 = com.mysql.jdbc.StringUtils.SearchMode.SKIP_BETWEEN_MARKERS;
        r1 = r0.contains(r1);
        r14 = 1;
        if (r1 == 0) goto L_0x0060;
    L_0x003f:
        if (r26 == 0) goto L_0x004d;
    L_0x0041:
        if (r27 == 0) goto L_0x004d;
    L_0x0043:
        r1 = r26.length();
        r2 = r27.length();
        if (r1 == r2) goto L_0x0060;
    L_0x004d:
        r1 = new java.lang.IllegalArgumentException;
        r2 = "StringUtils.15";
        r3 = 2;
        r3 = new java.lang.String[r3];
        r3[r11] = r26;
        r3[r14] = r27;
        r2 = com.mysql.jdbc.Messages.getString(r2, r3);
        r1.<init>(r2);
        throw r1;
    L_0x0060:
        r1 = r8[r11];
        r1 = r1.charAt(r11);
        r1 = java.lang.Character.isWhitespace(r1);
        if (r1 == 0) goto L_0x007d;
    L_0x006c:
        r1 = com.mysql.jdbc.StringUtils.SearchMode.SKIP_WHITE_SPACE;
        r1 = r0.contains(r1);
        if (r1 == 0) goto L_0x007d;
    L_0x0074:
        r0 = java.util.EnumSet.copyOf(r28);
        r1 = com.mysql.jdbc.StringUtils.SearchMode.SKIP_WHITE_SPACE;
        r0.remove(r1);
    L_0x007d:
        r4 = r0;
        r0 = com.mysql.jdbc.StringUtils.SearchMode.SKIP_WHITE_SPACE;
        r3 = java.util.EnumSet.of(r0);
        r3.addAll(r4);
        r0 = com.mysql.jdbc.StringUtils.SearchMode.SKIP_BETWEEN_MARKERS;
        r3.remove(r0);
        r0 = r5;
    L_0x008d:
        r2 = r0;
        if (r2 > r6) goto L_0x0118;
    L_0x0090:
        r15 = r8[r11];
        r0 = r2;
        r1 = r7;
        r16 = r2;
        r2 = r15;
        r15 = r3;
        r3 = r26;
        r17 = r4;
        r4 = r27;
        r5 = r17;
        r5 = indexOfIgnoreCase(r0, r1, r2, r3, r4, r5);
        if (r5 == r9) goto L_0x0113;
    L_0x00a6:
        if (r5 <= r6) goto L_0x00ae;
    L_0x00a8:
        r18 = r5;
        r19 = r6;
        goto L_0x0117;
    L_0x00ae:
        r0 = r8[r11];
        r0 = r0.length();
        r0 = r0 + r5;
        r1 = 0;
        r4 = r0;
        r0 = r14;
    L_0x00b8:
        r16 = r0;
        r3 = r1 + 1;
        if (r3 >= r12) goto L_0x00fd;
    L_0x00be:
        if (r16 == 0) goto L_0x00fd;
    L_0x00c0:
        r1 = r10 + -1;
        r18 = 0;
        r19 = 0;
        r20 = 0;
        r0 = r4;
        r2 = r7;
        r21 = r3;
        r3 = r18;
        r11 = r4;
        r4 = r19;
        r18 = r5;
        r5 = r20;
        r19 = r6;
        r6 = r15;
        r0 = indexOfNextChar(r0, r1, r2, r3, r4, r5, r6);
        if (r11 == r0) goto L_0x00f2;
    L_0x00de:
        r1 = r8[r21];
        r1 = startsWithIgnoreCase(r7, r0, r1);
        if (r1 != 0) goto L_0x00e7;
    L_0x00e6:
        goto L_0x00f2;
    L_0x00e7:
        r1 = r8[r21];
        r1 = r1.length();
        r4 = r0 + r1;
        r0 = r16;
        goto L_0x00f5;
    L_0x00f2:
        r1 = 0;
        r0 = r1;
        r4 = r11;
    L_0x00f5:
        r5 = r18;
        r6 = r19;
        r1 = r21;
        r11 = 0;
        goto L_0x00b8;
    L_0x00fd:
        r21 = r3;
        r11 = r4;
        r18 = r5;
        r19 = r6;
        if (r16 == 0) goto L_0x0107;
    L_0x0106:
        return r18;
    L_0x0107:
        r0 = r18 + 1;
        r5 = r23;
        r3 = r15;
        r4 = r17;
        r6 = r19;
        r11 = 0;
        goto L_0x008d;
    L_0x0113:
        r18 = r5;
        r19 = r6;
    L_0x0117:
        return r9;
    L_0x0118:
        r15 = r3;
        r17 = r4;
        r19 = r6;
        return r9;
    L_0x011e:
        return r9;
        */
        throw new UnsupportedOperationException("Method not decompiled: com.mysql.jdbc.StringUtils.indexOfIgnoreCase(int, java.lang.String, java.lang.String[], java.lang.String, java.lang.String, java.util.Set):int");
    }

    public static int indexOfIgnoreCase(int startingPosition, String searchIn, String searchFor, String openingMarkers, String closingMarkers, Set<SearchMode> searchMode) {
        return indexOfIgnoreCase(startingPosition, searchIn, searchFor, openingMarkers, closingMarkers, "", searchMode);
    }

    public static int indexOfIgnoreCase(int startingPosition, String searchIn, String searchFor, String openingMarkers, String closingMarkers, String overridingMarkers, Set<SearchMode> searchMode) {
        String str = searchIn;
        String str2 = searchFor;
        String str3 = openingMarkers;
        Set<SearchMode> searchMode2 = searchMode;
        if (str != null) {
            if (str2 != null) {
                int searchInLength = searchIn.length();
                int searchForLength = searchFor.length();
                int stopSearchingAt = searchInLength - searchForLength;
                int i = startingPosition;
                if (i <= stopSearchingAt) {
                    if (searchForLength != 0) {
                        int i$;
                        char c;
                        int i2 = 0;
                        if (searchMode2.contains(SearchMode.SKIP_BETWEEN_MARKERS)) {
                            if (!(str3 == null || closingMarkers == null)) {
                                if (openingMarkers.length() == closingMarkers.length()) {
                                    if (overridingMarkers != null) {
                                        char[] arr$ = overridingMarkers.toCharArray();
                                        int len$ = arr$.length;
                                        int i$2 = 0;
                                        while (true) {
                                            i$ = i$2;
                                            if (i$ >= len$) {
                                                break;
                                            }
                                            c = arr$[i$];
                                            if (str3.indexOf(c) == -1) {
                                                throw new IllegalArgumentException(Messages.getString("StringUtils.16", new String[]{overridingMarkers, str3}));
                                            }
                                            i$2 = i$ + 1;
                                            i2 = 0;
                                        }
                                    } else {
                                        throw new IllegalArgumentException(Messages.getString("StringUtils.16", new String[]{overridingMarkers, str3}));
                                    }
                                }
                            }
                            throw new IllegalArgumentException(Messages.getString("StringUtils.15", new String[]{str3, closingMarkers}));
                        }
                        int i3 = i2;
                        char firstCharOfSearchForUc = Character.toUpperCase(str2.charAt(i3));
                        char firstCharOfSearchForLc = Character.toLowerCase(str2.charAt(i3));
                        if (Character.isWhitespace(firstCharOfSearchForLc) && searchMode2.contains(SearchMode.SKIP_WHITE_SPACE)) {
                            searchMode2 = EnumSet.copyOf(searchMode);
                            searchMode2.remove(SearchMode.SKIP_WHITE_SPACE);
                        }
                        Set<SearchMode> searchMode3 = searchMode2;
                        int i4 = i;
                        while (true) {
                            i$ = i4;
                            if (i$ <= stopSearchingAt) {
                                char firstCharOfSearchForLc2 = firstCharOfSearchForLc;
                                char firstCharOfSearchForUc2 = firstCharOfSearchForUc;
                                i4 = indexOfNextChar(i$, stopSearchingAt, str, str3, closingMarkers, overridingMarkers, searchMode3);
                                if (i4 == -1) {
                                    return -1;
                                }
                                char firstCharOfSearchForLc3 = firstCharOfSearchForLc2;
                                c = firstCharOfSearchForUc2;
                                if (isCharEqualIgnoreCase(str.charAt(i4), c, firstCharOfSearchForLc3) && startsWithIgnoreCase(str, i4, str2)) {
                                    return i4;
                                }
                                i4++;
                                i = startingPosition;
                                firstCharOfSearchForUc = c;
                                firstCharOfSearchForLc = firstCharOfSearchForLc3;
                            } else {
                                c = firstCharOfSearchForUc;
                                return -1;
                            }
                        }
                    }
                }
                return -1;
            }
        }
        return -1;
    }

    private static int indexOfNextChar(int startingPosition, int stopPosition, String searchIn, String openingMarkers, String closingMarkers, String overridingMarkers, Set<SearchMode> searchMode) {
        int i = startingPosition;
        int i2 = stopPosition;
        String str = searchIn;
        String str2 = openingMarkers;
        String str3 = closingMarkers;
        String str4 = overridingMarkers;
        Set<SearchMode> set = searchMode;
        if (str == null) {
            return -1;
        }
        int searchInLength = searchIn.length();
        if (i >= searchInLength) {
            return -1;
        }
        char c1 = str.charAt(i);
        char c2 = i + 1 < searchInLength ? str.charAt(i + 1) : '\u0000';
        char c0 = '\u0000';
        int i3 = i;
        while (i3 <= i2) {
            c0 = c1;
            c1 = c2;
            c2 = i3 + 2 < searchInLength ? str.charAt(i3 + 2) : '\u0000';
            if (set.contains(SearchMode.ALLOW_BACKSLASH_ESCAPE) && c0 == WILDCARD_ESCAPE) {
                i3++;
                char c12 = c2;
                c2 = i3 + 2 < searchInLength ? str.charAt(i3 + 2) : '\u0000';
                c1 = c12;
            } else {
                boolean openingMarker;
                boolean dashDashCommentImmediateEnd;
                boolean dashDashCommentImmediateEnd2;
                char charAt;
                char overridingClosingMarker;
                char c13;
                if (set.contains(SearchMode.SKIP_BETWEEN_MARKERS)) {
                    int indexOf = str2.indexOf(c0);
                    int markerIndex = indexOf;
                    if (indexOf != -1) {
                        boolean openingMarker2 = c0;
                        boolean closingMarker = str3.charAt(markerIndex);
                        int nestedMarkersCount = 0;
                        openingMarker = openingMarker2;
                        dashDashCommentImmediateEnd = false;
                        boolean outerIsAnOverridingMarker = !str4.indexOf(openingMarker);
                        while (true) {
                            i3++;
                            if (i3 > i2) {
                                break;
                            }
                            dashDashCommentImmediateEnd2 = str.charAt(i3);
                            boolean c02 = dashDashCommentImmediateEnd2;
                            if (dashDashCommentImmediateEnd2 == closingMarker && nestedMarkersCount == 0) {
                                break;
                            }
                            if (!outerIsAnOverridingMarker && !r5.indexOf(c02)) {
                                dashDashCommentImmediateEnd2 = false;
                                char overridingOpeningMarker = c02;
                                char overridingClosingMarker2 = str3.charAt(str2.indexOf(c02));
                                while (true) {
                                    char overridingClosingMarker3 = overridingClosingMarker2;
                                    i3++;
                                    if (i3 > i2) {
                                        break;
                                    }
                                    charAt = str.charAt(i3);
                                    c0 = charAt;
                                    overridingClosingMarker = overridingClosingMarker3;
                                    if (charAt == overridingClosingMarker && !dashDashCommentImmediateEnd2) {
                                        break;
                                    }
                                    charAt = overridingOpeningMarker;
                                    if (c0 == charAt) {
                                        dashDashCommentImmediateEnd2++;
                                    } else if (c0 == overridingClosingMarker) {
                                        dashDashCommentImmediateEnd2--;
                                    } else {
                                        char c = charAt;
                                        if (set.contains(SearchMode.ALLOW_BACKSLASH_ESCAPE) && c0 == WILDCARD_ESCAPE) {
                                            i3++;
                                        }
                                        overridingClosingMarker2 = overridingClosingMarker;
                                        overridingOpeningMarker = c;
                                        str2 = openingMarkers;
                                        str3 = closingMarkers;
                                    }
                                    overridingOpeningMarker = charAt;
                                    overridingClosingMarker2 = overridingClosingMarker;
                                    str2 = openingMarkers;
                                    str3 = closingMarkers;
                                }
                            } else if (c02 == openingMarker) {
                                nestedMarkersCount++;
                            } else if (c02 == closingMarker) {
                                nestedMarkersCount--;
                            } else if (set.contains(SearchMode.ALLOW_BACKSLASH_ESCAPE)) {
                                if (c02 == WILDCARD_ESCAPE) {
                                    i3++;
                                }
                            }
                            str2 = openingMarkers;
                            str3 = closingMarkers;
                            str4 = overridingMarkers;
                        }
                        c2 = i3 + 2 < searchInLength ? str.charAt(i3 + 2) : '\u0000';
                        c1 = i3 + 1 < searchInLength ? str.charAt(i3 + 1) : '\u0000';
                    }
                }
                dashDashCommentImmediateEnd = false;
                if (set.contains(SearchMode.SKIP_BLOCK_COMMENTS) && c0 == true && c1 == '*') {
                    if (c2 != '!') {
                        i3++;
                        while (true) {
                            i3++;
                            if (i3 <= i2) {
                                if (str.charAt(i3) == '*') {
                                    if ((i3 + 1 < searchInLength ? str.charAt(i3 + 1) : '\u0000') == '/') {
                                        break;
                                    }
                                }
                            } else {
                                break;
                            }
                        }
                        i3++;
                    } else {
                        i3 = (i3 + 1) + 1;
                        i = 1;
                        while (i <= 5 && i3 + i < searchInLength) {
                            if (!Character.isDigit(str.charAt(i3 + i))) {
                                break;
                            }
                            i++;
                        }
                        if (i == 5) {
                            i3 += 5;
                        }
                    }
                    c13 = i3 + 1 < searchInLength ? str.charAt(i3 + 1) : '\u0000';
                    charAt = i3 + 2 < searchInLength ? str.charAt(i3 + 2) : '\u0000';
                } else if (set.contains(SearchMode.SKIP_BLOCK_COMMENTS) && c0 == true && c1 == '/') {
                    i3++;
                    c13 = c2;
                    charAt = i3 + 2 < searchInLength ? str.charAt(i3 + 2) : '\u0000';
                } else {
                    if (set.contains(SearchMode.SKIP_LINE_COMMENTS)) {
                        if (c0 == true && c1 == '-') {
                            if (Character.isWhitespace(c2)) {
                                dashDashCommentImmediateEnd2 = dashDashCommentImmediateEnd;
                            } else {
                                openingMarker = c2 == ';';
                                dashDashCommentImmediateEnd2 = openingMarker;
                                if (!(openingMarker || c2 == '\u0000')) {
                                }
                            }
                            if (dashDashCommentImmediateEnd2) {
                                while (true) {
                                    i3++;
                                    if (i3 <= i2) {
                                        break;
                                    }
                                    overridingClosingMarker = str.charAt(i3);
                                    c0 = overridingClosingMarker;
                                    if (overridingClosingMarker == '\n' || c0 == '\r') {
                                        break;
                                    }
                                }
                                overridingClosingMarker = i3 + 1 >= searchInLength ? str.charAt(i3 + 1) : '\u0000';
                                if (c0 == '\r' && overridingClosingMarker == '\n') {
                                    i3++;
                                    overridingClosingMarker = i3 + 1 >= searchInLength ? str.charAt(i3 + 1) : '\u0000';
                                }
                                c2 = i3 + 2 >= searchInLength ? str.charAt(i3 + 2) : '\u0000';
                                c1 = overridingClosingMarker;
                            } else {
                                i3 = (i3 + 1) + 1;
                                c13 = i3 + 1 >= searchInLength ? str.charAt(i3 + 1) : '\u0000';
                                charAt = i3 + 2 >= searchInLength ? str.charAt(i3 + 2) : '\u0000';
                            }
                        } else {
                            dashDashCommentImmediateEnd2 = dashDashCommentImmediateEnd;
                        }
                        if (c0 != true) {
                            dashDashCommentImmediateEnd = dashDashCommentImmediateEnd2;
                        }
                        if (dashDashCommentImmediateEnd2) {
                            while (true) {
                                i3++;
                                if (i3 <= i2) {
                                    break;
                                }
                                overridingClosingMarker = str.charAt(i3);
                                c0 = overridingClosingMarker;
                            }
                            if (i3 + 1 >= searchInLength) {
                            }
                            overridingClosingMarker = i3 + 1 >= searchInLength ? str.charAt(i3 + 1) : '\u0000';
                            i3++;
                            if (i3 + 1 >= searchInLength) {
                            }
                            overridingClosingMarker = i3 + 1 >= searchInLength ? str.charAt(i3 + 1) : '\u0000';
                            if (i3 + 2 >= searchInLength) {
                            }
                            c2 = i3 + 2 >= searchInLength ? str.charAt(i3 + 2) : '\u0000';
                            c1 = overridingClosingMarker;
                        } else {
                            i3 = (i3 + 1) + 1;
                            if (i3 + 1 >= searchInLength) {
                            }
                            c13 = i3 + 1 >= searchInLength ? str.charAt(i3 + 1) : '\u0000';
                            if (i3 + 2 >= searchInLength) {
                            }
                            charAt = i3 + 2 >= searchInLength ? str.charAt(i3 + 2) : '\u0000';
                        }
                    }
                    if (set.contains(SearchMode.SKIP_WHITE_SPACE)) {
                        if (Character.isWhitespace(c0)) {
                        }
                    }
                    return i3;
                }
                c1 = c13;
                c2 = charAt;
            }
            i3++;
            i = startingPosition;
            str2 = openingMarkers;
            str3 = closingMarkers;
            str4 = overridingMarkers;
        }
        return -1;
    }

    private static boolean isCharAtPosNotEqualIgnoreCase(String searchIn, int pos, char firstCharOfSearchForUc, char firstCharOfSearchForLc) {
        return (Character.toLowerCase(searchIn.charAt(pos)) == firstCharOfSearchForLc || Character.toUpperCase(searchIn.charAt(pos)) == firstCharOfSearchForUc) ? false : true;
    }

    private static boolean isCharEqualIgnoreCase(char charToCompare, char compareToCharUC, char compareToCharLC) {
        if (Character.toLowerCase(charToCompare) != compareToCharLC) {
            if (Character.toUpperCase(charToCompare) != compareToCharUC) {
                return false;
            }
        }
        return true;
    }

    public static List<String> split(String stringToSplit, String delimiter, boolean trim) {
        if (stringToSplit == null) {
            return new ArrayList();
        }
        if (delimiter == null) {
            throw new IllegalArgumentException();
        }
        StringTokenizer tokenizer = new StringTokenizer(stringToSplit, delimiter, false);
        List<String> splitTokens = new ArrayList(tokenizer.countTokens());
        while (tokenizer.hasMoreTokens()) {
            String token = tokenizer.nextToken();
            if (trim) {
                token = token.trim();
            }
            splitTokens.add(token);
        }
        return splitTokens;
    }

    public static List<String> split(String stringToSplit, String delimiter, String openingMarkers, String closingMarkers, boolean trim) {
        return split(stringToSplit, delimiter, openingMarkers, closingMarkers, "", trim);
    }

    public static List<String> split(String stringToSplit, String delimiter, String openingMarkers, String closingMarkers, String overridingMarkers, boolean trim) {
        if (stringToSplit == null) {
            return new ArrayList();
        }
        if (delimiter == null) {
            throw new IllegalArgumentException();
        }
        int currentPos = 0;
        List<String> splitTokens = new ArrayList();
        while (true) {
            int indexOfIgnoreCase = indexOfIgnoreCase(currentPos, stringToSplit, delimiter, openingMarkers, closingMarkers, overridingMarkers, SEARCH_MODE__MRK_COM_WS);
            int delimPos = indexOfIgnoreCase;
            if (indexOfIgnoreCase == -1) {
                break;
            }
            String token = stringToSplit.substring(currentPos, delimPos);
            if (trim) {
                token = token.trim();
            }
            splitTokens.add(token);
            currentPos = delimPos + 1;
        }
        if (currentPos < stringToSplit.length()) {
            token = stringToSplit.substring(currentPos);
            if (trim) {
                token = token.trim();
            }
            splitTokens.add(token);
        }
        return splitTokens;
    }

    private static boolean startsWith(byte[] dataFrom, String chars) {
        int charsLength = chars.length();
        if (dataFrom.length < charsLength) {
            return false;
        }
        for (int i = 0; i < charsLength; i++) {
            if (dataFrom[i] != chars.charAt(i)) {
                return false;
            }
        }
        return true;
    }

    public static boolean startsWithIgnoreCase(String searchIn, int startAt, String searchFor) {
        return searchIn.regionMatches(true, startAt, searchFor, 0, searchFor.length());
    }

    public static boolean startsWithIgnoreCase(String searchIn, String searchFor) {
        return startsWithIgnoreCase(searchIn, 0, searchFor);
    }

    public static boolean startsWithIgnoreCaseAndNonAlphaNumeric(String searchIn, String searchFor) {
        if (searchIn == null) {
            return searchFor == null;
        }
        int beginPos = 0;
        int inLength = searchIn.length();
        while (beginPos < inLength) {
            if (Character.isLetterOrDigit(searchIn.charAt(beginPos))) {
                break;
            }
            beginPos++;
        }
        return startsWithIgnoreCase(searchIn, beginPos, searchFor);
    }

    public static boolean startsWithIgnoreCaseAndWs(String searchIn, String searchFor) {
        return startsWithIgnoreCaseAndWs(searchIn, searchFor, 0);
    }

    public static boolean startsWithIgnoreCaseAndWs(String searchIn, String searchFor, int beginPos) {
        if (searchIn == null) {
            return searchFor == null;
        }
        int inLength = searchIn.length();
        while (beginPos < inLength) {
            if (!Character.isWhitespace(searchIn.charAt(beginPos))) {
                break;
            }
            beginPos++;
        }
        return startsWithIgnoreCase(searchIn, beginPos, searchFor);
    }

    public static int startsWithIgnoreCaseAndWs(String searchIn, String[] searchFor) {
        for (int i = 0; i < searchFor.length; i++) {
            if (startsWithIgnoreCaseAndWs(searchIn, searchFor[i], 0)) {
                return i;
            }
        }
        return -1;
    }

    public static byte[] stripEnclosure(byte[] source, String prefix, String suffix) {
        if (source.length < prefix.length() + suffix.length() || !startsWith(source, prefix) || !endsWith(source, suffix)) {
            return source;
        }
        byte[] enclosed = new byte[(source.length - (prefix.length() + suffix.length()))];
        System.arraycopy(source, prefix.length(), enclosed, 0, enclosed.length);
        return enclosed;
    }

    public static String toAsciiString(byte[] buffer) {
        return toAsciiString(buffer, 0, buffer.length);
    }

    public static String toAsciiString(byte[] buffer, int startPos, int length) {
        char[] charArray = new char[length];
        int readpoint = startPos;
        for (int i = 0; i < length; i++) {
            charArray[i] = (char) buffer[readpoint];
            readpoint++;
        }
        return new String(charArray);
    }

    public static boolean wildCompareIgnoreCase(String searchIn, String searchFor) {
        return wildCompareInternal(searchIn, searchFor) == 0;
    }

    private static int wildCompareInternal(String searchIn, String searchFor) {
        if (searchIn != null) {
            if (searchFor != null) {
                int i = 0;
                if (searchFor.equals("%")) {
                    return 0;
                }
                int searchForEnd = searchFor.length();
                int searchInEnd = searchIn.length();
                int searchInPos = 0;
                int searchForPos = 0;
                int result = -1;
                while (searchForPos != searchForEnd) {
                    while (searchFor.charAt(searchForPos) != WILDCARD_MANY && searchFor.charAt(searchForPos) != WILDCARD_ONE) {
                        if (searchFor.charAt(searchForPos) == WILDCARD_ESCAPE && searchForPos + 1 != searchForEnd) {
                            searchForPos++;
                        }
                        if (searchInPos != searchInEnd) {
                            int searchForPos2 = searchForPos + 1;
                            int searchInPos2 = searchInPos + 1;
                            if (Character.toUpperCase(searchFor.charAt(searchForPos)) != Character.toUpperCase(searchIn.charAt(searchInPos))) {
                                searchForPos = searchForPos2;
                                searchInPos = searchInPos2;
                            } else if (searchForPos2 == searchForEnd) {
                                if (searchInPos2 != searchInEnd) {
                                    i = 1;
                                }
                                return i;
                            } else {
                                result = 1;
                                searchForPos = searchForPos2;
                                searchInPos = searchInPos2;
                            }
                        }
                        return 1;
                    }
                    if (searchFor.charAt(searchForPos) == WILDCARD_ONE) {
                        while (searchInPos != searchInEnd) {
                            searchInPos++;
                            searchForPos++;
                            if (searchForPos < searchForEnd) {
                                if (searchFor.charAt(searchForPos) != WILDCARD_ONE) {
                                }
                            }
                            if (searchForPos == searchForEnd) {
                                break;
                            }
                        }
                        return result;
                    }
                    if (searchFor.charAt(searchForPos) == WILDCARD_MANY) {
                        searchForPos++;
                        while (searchForPos != searchForEnd) {
                            if (searchFor.charAt(searchForPos) != WILDCARD_MANY) {
                                if (searchFor.charAt(searchForPos) != WILDCARD_ONE) {
                                    break;
                                } else if (searchInPos == searchInEnd) {
                                    return -1;
                                } else {
                                    searchInPos++;
                                }
                            }
                            searchForPos++;
                        }
                        if (searchForPos == searchForEnd) {
                            return 0;
                        }
                        if (searchInPos == searchInEnd) {
                            return -1;
                        }
                        char charAt = searchFor.charAt(searchForPos);
                        char cmp = charAt;
                        if (charAt == WILDCARD_ESCAPE && searchForPos + 1 != searchForEnd) {
                            searchForPos++;
                            cmp = searchFor.charAt(searchForPos);
                        }
                        int searchForPos3 = 1 + searchForPos;
                        while (true) {
                            if (searchInPos == searchInEnd || Character.toUpperCase(searchIn.charAt(searchInPos)) == Character.toUpperCase(cmp)) {
                                i = searchInPos + 1;
                                if (searchInPos == searchInEnd) {
                                    return -1;
                                }
                                searchForPos = wildCompareInternal(searchIn.substring(i), searchFor.substring(searchForPos3));
                                if (searchForPos <= 0) {
                                    return searchForPos;
                                }
                                if (i == searchInEnd) {
                                    return -1;
                                }
                                searchInPos = i;
                            } else {
                                searchInPos++;
                            }
                        }
                    }
                }
                if (searchInPos != searchInEnd) {
                    i = 1;
                }
                return i;
            }
        }
        return -1;
    }

    static byte[] s2b(String s, MySQLConnection conn) throws SQLException {
        if (s == null) {
            return null;
        }
        if (conn == null || !conn.getUseUnicode()) {
            return s.getBytes();
        }
        try {
            String encoding = conn.getEncoding();
            if (encoding == null) {
                return s.getBytes();
            }
            SingleByteCharsetConverter converter = conn.getCharsetConverter(encoding);
            if (converter != null) {
                return converter.toBytes(s);
            }
            return s.getBytes(encoding);
        } catch (UnsupportedEncodingException e) {
            return s.getBytes();
        }
    }

    public static int lastIndexOf(byte[] s, char c) {
        if (s == null) {
            return -1;
        }
        for (int i = s.length - 1; i >= 0; i--) {
            if (s[i] == c) {
                return i;
            }
        }
        return -1;
    }

    public static int indexOf(byte[] s, char c) {
        if (s == null) {
            return -1;
        }
        int length = s.length;
        for (int i = 0; i < length; i++) {
            if (s[i] == c) {
                return i;
            }
        }
        return -1;
    }

    public static boolean isNullOrEmpty(String toTest) {
        if (toTest != null) {
            if (toTest.length() != 0) {
                return false;
            }
        }
        return true;
    }

    /* JADX WARNING: inconsistent code. */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    public static java.lang.String stripComments(java.lang.String r18, java.lang.String r19, java.lang.String r20, boolean r21, boolean r22, boolean r23, boolean r24) {
        /*
        r1 = r18;
        if (r1 != 0) goto L_0x0006;
    L_0x0004:
        r2 = 0;
        return r2;
    L_0x0006:
        r2 = new java.lang.StringBuilder;
        r3 = r18.length();
        r2.<init>(r3);
        r3 = new java.io.StringReader;
        r3.<init>(r1);
        r4 = 0;
        r5 = 0;
        r6 = -1;
        r7 = 0;
        r9 = r7;
        r7 = r4;
        r4 = 0;
    L_0x001b:
        r10 = r3.read();	 Catch:{ IOException -> 0x0117 }
        r4 = r10;
        r11 = -1;
        if (r10 == r11) goto L_0x0112;
    L_0x0023:
        if (r6 == r11) goto L_0x0039;
    L_0x0025:
        r10 = r20;
        r12 = r10.charAt(r6);	 Catch:{ IOException -> 0x0034 }
        if (r4 != r12) goto L_0x003b;
    L_0x002d:
        if (r5 != 0) goto L_0x003b;
    L_0x002f:
        r7 = 0;
        r6 = -1;
        r12 = r19;
        goto L_0x004a;
    L_0x0034:
        r0 = move-exception;
        r12 = r19;
        goto L_0x011c;
    L_0x0039:
        r10 = r20;
    L_0x003b:
        r12 = r19;
        r13 = r12.indexOf(r4);	 Catch:{ IOException -> 0x0110 }
        r9 = r13;
        if (r13 == r11) goto L_0x004a;
    L_0x0044:
        if (r5 != 0) goto L_0x004a;
    L_0x0046:
        if (r7 != 0) goto L_0x004a;
    L_0x0048:
        r6 = r9;
        r7 = r4;
    L_0x004a:
        if (r7 != 0) goto L_0x00b9;
    L_0x004c:
        r11 = 47;
        if (r4 != r11) goto L_0x00b9;
    L_0x0050:
        if (r22 != 0) goto L_0x0054;
    L_0x0052:
        if (r21 == 0) goto L_0x00b9;
    L_0x0054:
        r15 = r3.read();	 Catch:{ IOException -> 0x0110 }
        r4 = r15;
        r8 = 42;
        if (r4 != r8) goto L_0x00a5;
    L_0x005d:
        if (r21 == 0) goto L_0x00a5;
    L_0x005f:
        r14 = r4;
        r4 = 0;
    L_0x0061:
        r13 = r3.read();	 Catch:{ IOException -> 0x00a1 }
        r16 = r13;
        if (r13 != r11) goto L_0x006f;
    L_0x0069:
        if (r4 == r8) goto L_0x006c;
    L_0x006b:
        goto L_0x006f;
    L_0x006c:
        r4 = r16;
        goto L_0x001b;
    L_0x006f:
        r13 = r16;
        r8 = 13;
        if (r13 != r8) goto L_0x008d;
    L_0x0075:
        r8 = r3.read();	 Catch:{ IOException -> 0x0089 }
        r13 = 10;
        if (r8 != r13) goto L_0x0087;
    L_0x007d:
        r13 = r3.read();	 Catch:{ IOException -> 0x0083 }
        r8 = r13;
        goto L_0x0087;
    L_0x0083:
        r0 = move-exception;
        r4 = r8;
        goto L_0x011c;
    L_0x0087:
        r14 = r8;
        goto L_0x0097;
    L_0x0089:
        r0 = move-exception;
        r4 = r13;
        goto L_0x011c;
    L_0x008d:
        r8 = 10;
        if (r13 != r8) goto L_0x0096;
    L_0x0091:
        r8 = r3.read();	 Catch:{ IOException -> 0x0089 }
        goto L_0x0087;
    L_0x0096:
        r14 = r13;
    L_0x0097:
        if (r14 >= 0) goto L_0x009d;
        r4 = r14;
        goto L_0x001b;
    L_0x009d:
        r4 = r14;
        r8 = 42;
        goto L_0x0061;
    L_0x00a1:
        r0 = move-exception;
        r4 = r14;
        goto L_0x011c;
    L_0x00a5:
        if (r4 != r11) goto L_0x0105;
    L_0x00a7:
        if (r22 == 0) goto L_0x0105;
    L_0x00a9:
        r8 = r3.read();	 Catch:{ IOException -> 0x0110 }
        r4 = r8;
        r11 = 10;
        if (r8 == r11) goto L_0x0105;
    L_0x00b2:
        r8 = 13;
        if (r4 == r8) goto L_0x0105;
    L_0x00b6:
        if (r4 < 0) goto L_0x0105;
    L_0x00b8:
        goto L_0x00a9;
    L_0x00b9:
        if (r7 != 0) goto L_0x00d1;
    L_0x00bb:
        r8 = 35;
        if (r4 != r8) goto L_0x00d1;
    L_0x00bf:
        if (r23 == 0) goto L_0x00d1;
    L_0x00c1:
        r11 = r3.read();	 Catch:{ IOException -> 0x0110 }
        r4 = r11;
        r13 = 10;
        if (r11 == r13) goto L_0x0105;
    L_0x00ca:
        r11 = 13;
        if (r4 == r11) goto L_0x0105;
    L_0x00ce:
        if (r4 < 0) goto L_0x0105;
    L_0x00d0:
        goto L_0x00c1;
    L_0x00d1:
        if (r7 != 0) goto L_0x0105;
    L_0x00d3:
        r11 = 45;
        if (r4 != r11) goto L_0x0105;
    L_0x00d7:
        if (r24 == 0) goto L_0x0105;
    L_0x00d9:
        r15 = r3.read();	 Catch:{ IOException -> 0x0110 }
        r4 = r15;
        r11 = -1;
        if (r4 == r11) goto L_0x00f8;
    L_0x00e1:
        r11 = 45;
        if (r4 == r11) goto L_0x00e6;
    L_0x00e5:
        goto L_0x00f8;
    L_0x00e6:
        r11 = r3.read();	 Catch:{ IOException -> 0x0110 }
        r4 = r11;
        r1 = 10;
        if (r11 == r1) goto L_0x0105;
    L_0x00ef:
        r11 = 13;
        if (r4 == r11) goto L_0x0105;
    L_0x00f3:
        if (r4 < 0) goto L_0x0105;
    L_0x00f5:
        r1 = r18;
        goto L_0x00e6;
    L_0x00f8:
        r1 = 45;
        r2.append(r1);	 Catch:{ IOException -> 0x0110 }
        r1 = -1;
        if (r4 == r1) goto L_0x010c;
    L_0x0100:
        r1 = (char) r4;	 Catch:{ IOException -> 0x0110 }
        r2.append(r1);	 Catch:{ IOException -> 0x0110 }
        goto L_0x010c;
    L_0x0105:
        r1 = -1;
        if (r4 == r1) goto L_0x010c;
    L_0x0108:
        r1 = (char) r4;	 Catch:{ IOException -> 0x0110 }
        r2.append(r1);	 Catch:{ IOException -> 0x0110 }
    L_0x010c:
        r1 = r18;
        goto L_0x001b;
    L_0x0110:
        r0 = move-exception;
        goto L_0x011c;
    L_0x0112:
        r12 = r19;
        r10 = r20;
        goto L_0x011c;
    L_0x0117:
        r0 = move-exception;
        r12 = r19;
        r10 = r20;
    L_0x011c:
        r1 = r2.toString();
        return r1;
        */
        throw new UnsupportedOperationException("Method not decompiled: com.mysql.jdbc.StringUtils.stripComments(java.lang.String, java.lang.String, java.lang.String, boolean, boolean, boolean, boolean):java.lang.String");
    }

    public static String sanitizeProcOrFuncName(String src) {
        if (src != null) {
            if (!src.equals("%")) {
                return src;
            }
        }
        return null;
    }

    public static List<String> splitDBdotName(String source, String catalog, String quoteId, boolean isNoBslashEscSet) {
        if (source != null) {
            if (!source.equals("%")) {
                int dotIndex;
                String entityName;
                if (" ".equals(quoteId)) {
                    dotIndex = source.indexOf(".");
                } else {
                    dotIndex = indexOfIgnoreCase(0, source, ".", quoteId, quoteId, isNoBslashEscSet ? SEARCH_MODE__MRK_WS : SEARCH_MODE__BSESC_MRK_WS);
                }
                String database = catalog;
                if (dotIndex != -1) {
                    database = unQuoteIdentifier(source.substring(0, dotIndex), quoteId);
                    entityName = unQuoteIdentifier(source.substring(dotIndex + 1), quoteId);
                } else {
                    entityName = unQuoteIdentifier(source, quoteId);
                }
                return Arrays.asList(new String[]{database, entityName});
            }
        }
        return Collections.emptyList();
    }

    public static boolean isEmptyOrWhitespaceOnly(String str) {
        if (str != null) {
            if (str.length() != 0) {
                int length = str.length();
                for (int i = 0; i < length; i++) {
                    if (!Character.isWhitespace(str.charAt(i))) {
                        return false;
                    }
                }
                return true;
            }
        }
        return true;
    }

    public static String escapeQuote(String src, String quotChar) {
        if (src == null) {
            return null;
        }
        src = toString(stripEnclosure(src.getBytes(), quotChar, quotChar));
        int lastNdx = src.indexOf(quotChar);
        String tmpSrc = src.substring(0, lastNdx);
        StringBuilder stringBuilder = new StringBuilder();
        stringBuilder.append(tmpSrc);
        stringBuilder.append(quotChar);
        stringBuilder.append(quotChar);
        tmpSrc = stringBuilder.toString();
        String tmpRest = src.substring(lastNdx + 1, src.length());
        lastNdx = tmpRest.indexOf(quotChar);
        while (lastNdx > -1) {
            StringBuilder stringBuilder2 = new StringBuilder();
            stringBuilder2.append(tmpSrc);
            stringBuilder2.append(tmpRest.substring(0, lastNdx));
            tmpSrc = stringBuilder2.toString();
            stringBuilder2 = new StringBuilder();
            stringBuilder2.append(tmpSrc);
            stringBuilder2.append(quotChar);
            stringBuilder2.append(quotChar);
            tmpSrc = stringBuilder2.toString();
            tmpRest = tmpRest.substring(lastNdx + 1, tmpRest.length());
            lastNdx = tmpRest.indexOf(quotChar);
        }
        String tmpSrc2 = new StringBuilder();
        tmpSrc2.append(tmpSrc);
        tmpSrc2.append(tmpRest);
        return tmpSrc2.toString();
    }

    public static String quoteIdentifier(String identifier, String quoteChar, boolean isPedantic) {
        if (identifier == null) {
            return null;
        }
        identifier = identifier.trim();
        int quoteCharLength = quoteChar.length();
        if (quoteCharLength != 0) {
            if (!" ".equals(quoteChar)) {
                if (!isPedantic && identifier.startsWith(quoteChar) && identifier.endsWith(quoteChar)) {
                    String identifierQuoteTrimmed = identifier.substring(quoteCharLength, identifier.length() - quoteCharLength);
                    int quoteCharPos = identifierQuoteTrimmed.indexOf(quoteChar);
                    while (quoteCharPos >= 0) {
                        int quoteCharNextExpectedPos = quoteCharPos + quoteCharLength;
                        int quoteCharNextPosition = identifierQuoteTrimmed.indexOf(quoteChar, quoteCharNextExpectedPos);
                        if (quoteCharNextPosition != quoteCharNextExpectedPos) {
                            break;
                        }
                        quoteCharPos = identifierQuoteTrimmed.indexOf(quoteChar, quoteCharNextPosition + quoteCharLength);
                    }
                    if (quoteCharPos < 0) {
                        return identifier;
                    }
                }
                StringBuilder stringBuilder = new StringBuilder();
                stringBuilder.append(quoteChar);
                StringBuilder stringBuilder2 = new StringBuilder();
                stringBuilder2.append(quoteChar);
                stringBuilder2.append(quoteChar);
                stringBuilder.append(identifier.replaceAll(quoteChar, stringBuilder2.toString()));
                stringBuilder.append(quoteChar);
                return stringBuilder.toString();
            }
        }
        return identifier;
    }

    public static String quoteIdentifier(String identifier, boolean isPedantic) {
        return quoteIdentifier(identifier, "`", isPedantic);
    }

    public static String unQuoteIdentifier(String identifier, String quoteChar) {
        if (identifier == null) {
            return null;
        }
        identifier = identifier.trim();
        int quoteCharLength = quoteChar.length();
        if (quoteCharLength != 0) {
            if (!" ".equals(quoteChar)) {
                if (!identifier.startsWith(quoteChar) || !identifier.endsWith(quoteChar)) {
                    return identifier;
                }
                String identifierQuoteTrimmed = identifier.substring(quoteCharLength, identifier.length() - quoteCharLength);
                int quoteCharPos = identifierQuoteTrimmed.indexOf(quoteChar);
                while (quoteCharPos >= 0) {
                    int quoteCharNextExpectedPos = quoteCharPos + quoteCharLength;
                    int quoteCharNextPosition = identifierQuoteTrimmed.indexOf(quoteChar, quoteCharNextExpectedPos);
                    if (quoteCharNextPosition != quoteCharNextExpectedPos) {
                        return identifier;
                    }
                    quoteCharPos = identifierQuoteTrimmed.indexOf(quoteChar, quoteCharNextPosition + quoteCharLength);
                }
                String substring = identifier.substring(quoteCharLength, identifier.length() - quoteCharLength);
                StringBuilder stringBuilder = new StringBuilder();
                stringBuilder.append(quoteChar);
                stringBuilder.append(quoteChar);
                return substring.replaceAll(stringBuilder.toString(), quoteChar);
            }
        }
        return identifier;
    }

    public static int indexOfQuoteDoubleAware(String searchIn, String quoteChar, int startFrom) {
        if (!(searchIn == null || quoteChar == null || quoteChar.length() == 0)) {
            if (startFrom <= searchIn.length()) {
                boolean next = true;
                int lastIndex = searchIn.length() - 1;
                int beginPos = startFrom;
                int pos = -1;
                while (next) {
                    pos = searchIn.indexOf(quoteChar, beginPos);
                    if (!(pos == -1 || pos == lastIndex)) {
                        if (searchIn.startsWith(quoteChar, pos + 1)) {
                            beginPos = pos + 2;
                        }
                    }
                    next = false;
                }
                return pos;
            }
        }
        return -1;
    }

    public static String toString(byte[] value, int offset, int length, String encoding) throws UnsupportedEncodingException {
        return findCharset(encoding).decode(ByteBuffer.wrap(value, offset, length)).toString();
    }

    public static String toString(byte[] value, String encoding) throws UnsupportedEncodingException {
        return findCharset(encoding).decode(ByteBuffer.wrap(value)).toString();
    }

    public static String toString(byte[] value, int offset, int length) {
        try {
            return findCharset(platformEncoding).decode(ByteBuffer.wrap(value, offset, length)).toString();
        } catch (UnsupportedEncodingException e) {
            return null;
        }
    }

    public static String toString(byte[] value) {
        try {
            return findCharset(platformEncoding).decode(ByteBuffer.wrap(value)).toString();
        } catch (UnsupportedEncodingException e) {
            return null;
        }
    }

    public static byte[] getBytes(char[] value) {
        try {
            return getBytes(value, 0, value.length, platformEncoding);
        } catch (UnsupportedEncodingException e) {
            return null;
        }
    }

    public static byte[] getBytes(char[] value, int offset, int length) {
        try {
            return getBytes(value, offset, length, platformEncoding);
        } catch (UnsupportedEncodingException e) {
            return null;
        }
    }

    public static byte[] getBytes(char[] value, String encoding) throws UnsupportedEncodingException {
        return getBytes(value, 0, value.length, encoding);
    }

    public static byte[] getBytes(char[] value, int offset, int length, String encoding) throws UnsupportedEncodingException {
        ByteBuffer buf = findCharset(encoding).encode(CharBuffer.wrap(value, offset, length));
        int encodedLen = buf.limit();
        byte[] asBytes = new byte[encodedLen];
        buf.get(asBytes, 0, encodedLen);
        return asBytes;
    }

    public static byte[] getBytes(String value) {
        try {
            return getBytes(value, 0, value.length(), platformEncoding);
        } catch (UnsupportedEncodingException e) {
            return null;
        }
    }

    public static byte[] getBytes(String value, int offset, int length) {
        try {
            return getBytes(value, offset, length, platformEncoding);
        } catch (UnsupportedEncodingException e) {
            return null;
        }
    }

    public static byte[] getBytes(String value, String encoding) throws UnsupportedEncodingException {
        return getBytes(value, 0, value.length(), encoding);
    }

    public static byte[] getBytes(String value, int offset, int length, String encoding) throws UnsupportedEncodingException {
        if (Util.isJdbc4()) {
            ByteBuffer buf = findCharset(encoding).encode(CharBuffer.wrap(value.toCharArray(), offset, length));
            int encodedLen = buf.limit();
            byte[] asBytes = new byte[encodedLen];
            buf.get(asBytes, 0, encodedLen);
            return asBytes;
        }
        if (offset == 0) {
            if (length == value.length()) {
                return value.getBytes(encoding);
            }
        }
        return value.substring(offset, offset + length).getBytes(encoding);
    }

    public static final boolean isValidIdChar(char c) {
        return VALID_ID_CHARS.indexOf(c) != -1;
    }

    public static void appendAsHex(StringBuilder builder, byte[] bytes) {
        builder.append("0x");
        for (byte b : bytes) {
            builder.append(HEX_DIGITS[(b >>> 4) & 15]);
            builder.append(HEX_DIGITS[b & 15]);
        }
    }

    public static void appendAsHex(StringBuilder builder, int value) {
        if (value == 0) {
            builder.append("0x0");
            return;
        }
        int shift = 32;
        boolean nonZeroFound = false;
        builder.append("0x");
        do {
            shift -= 4;
            byte nibble = (byte) ((value >>> shift) & 15);
            if (nonZeroFound) {
                builder.append(HEX_DIGITS[nibble]);
                continue;
            } else if (nibble != (byte) 0) {
                builder.append(HEX_DIGITS[nibble]);
                nonZeroFound = true;
                continue;
            } else {
                continue;
            }
        } while (shift != 0);
    }

    public static byte[] getBytesNullTerminated(String value, String encoding) throws UnsupportedEncodingException {
        ByteBuffer buf = findCharset(encoding).encode(value);
        int encodedLen = buf.limit();
        byte[] asBytes = new byte[(encodedLen + 1)];
        buf.get(asBytes, 0, encodedLen);
        asBytes[encodedLen] = (byte) 0;
        return asBytes;
    }

    public static boolean isStrictlyNumeric(CharSequence cs) {
        if (cs != null) {
            if (cs.length() != 0) {
                for (int i = 0; i < cs.length(); i++) {
                    if (!Character.isDigit(cs.charAt(i))) {
                        return false;
                    }
                }
                return true;
            }
        }
        return false;
    }
}
