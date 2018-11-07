package com.mysql.jdbc;

import java.io.UnsupportedEncodingException;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.Map;

public class SingleByteCharsetConverter {
    private static final int BYTE_RANGE = 256;
    private static final Map<String, SingleByteCharsetConverter> CONVERTER_MAP = new HashMap();
    private static final byte[] EMPTY_BYTE_ARRAY = new byte[0];
    private static byte[] allBytes = new byte[256];
    private static byte[] unknownCharsMap = new byte[65536];
    private char[] byteToChars = new char[256];
    private byte[] charToByteMap = new byte[65536];

    static {
        int i = 0;
        for (int i2 = -128; i2 <= 127; i2++) {
            allBytes[i2 + 128] = (byte) i2;
        }
        while (i < unknownCharsMap.length) {
            unknownCharsMap[i] = (byte) 63;
            i++;
        }
    }

    public static synchronized SingleByteCharsetConverter getInstance(String encodingName, Connection conn) throws UnsupportedEncodingException, SQLException {
        SingleByteCharsetConverter instance;
        synchronized (SingleByteCharsetConverter.class) {
            instance = (SingleByteCharsetConverter) CONVERTER_MAP.get(encodingName);
            if (instance == null) {
                instance = initCharset(encodingName);
            }
        }
        return instance;
    }

    public static SingleByteCharsetConverter initCharset(String javaEncodingName) throws UnsupportedEncodingException, SQLException {
        try {
            if (CharsetMapping.isMultibyteCharset(javaEncodingName)) {
                return null;
            }
            SingleByteCharsetConverter converter = new SingleByteCharsetConverter(javaEncodingName);
            CONVERTER_MAP.put(javaEncodingName, converter);
            return converter;
        } catch (RuntimeException ex) {
            SQLException sqlEx = SQLError.createSQLException(ex.toString(), SQLError.SQL_STATE_ILLEGAL_ARGUMENT, null);
            sqlEx.initCause(ex);
            throw sqlEx;
        }
    }

    public static String toStringDefaultEncoding(byte[] buffer, int startPos, int length) {
        return new String(buffer, startPos, length);
    }

    private SingleByteCharsetConverter(String encodingName) throws UnsupportedEncodingException {
        int i = 0;
        String allBytesString = new String(allBytes, 0, 256, encodingName);
        int allBytesLen = allBytesString.length();
        System.arraycopy(unknownCharsMap, 0, this.charToByteMap, 0, this.charToByteMap.length);
        while (i < 256 && i < allBytesLen) {
            char c = allBytesString.charAt(i);
            this.byteToChars[i] = c;
            this.charToByteMap[c] = allBytes[i];
            i++;
        }
    }

    public final byte[] toBytes(char[] c) {
        if (c == null) {
            return null;
        }
        int length = c.length;
        byte[] bytes = new byte[length];
        for (int i = 0; i < length; i++) {
            bytes[i] = this.charToByteMap[c[i]];
        }
        return bytes;
    }

    public final byte[] toBytesWrapped(char[] c, char beginWrap, char endWrap) {
        if (c == null) {
            return null;
        }
        int i = 0;
        int length = c.length + 2;
        int charLength = c.length;
        byte[] bytes = new byte[length];
        bytes[0] = this.charToByteMap[beginWrap];
        while (i < charLength) {
            bytes[i + 1] = this.charToByteMap[c[i]];
            i++;
        }
        bytes[length - 1] = this.charToByteMap[endWrap];
        return bytes;
    }

    public final byte[] toBytes(char[] chars, int offset, int length) {
        if (chars == null) {
            return null;
        }
        if (length == 0) {
            return EMPTY_BYTE_ARRAY;
        }
        byte[] bytes = new byte[length];
        for (int i = 0; i < length; i++) {
            bytes[i] = this.charToByteMap[chars[i + offset]];
        }
        return bytes;
    }

    public final byte[] toBytes(String s) {
        if (s == null) {
            return null;
        }
        int length = s.length();
        byte[] bytes = new byte[length];
        for (int i = 0; i < length; i++) {
            bytes[i] = this.charToByteMap[s.charAt(i)];
        }
        return bytes;
    }

    public final byte[] toBytesWrapped(String s, char beginWrap, char endWrap) {
        if (s == null) {
            return null;
        }
        int stringLength = s.length();
        int length = stringLength + 2;
        byte[] bytes = new byte[length];
        int i = 0;
        bytes[0] = this.charToByteMap[beginWrap];
        while (true) {
            int i2 = i;
            if (i2 < stringLength) {
                bytes[i2 + 1] = this.charToByteMap[s.charAt(i2)];
                i = i2 + 1;
            } else {
                bytes[length - 1] = this.charToByteMap[endWrap];
                return bytes;
            }
        }
    }

    public final byte[] toBytes(String s, int offset, int length) {
        if (s == null) {
            return null;
        }
        if (length == 0) {
            return EMPTY_BYTE_ARRAY;
        }
        byte[] bytes = new byte[length];
        for (int i = 0; i < length; i++) {
            bytes[i] = this.charToByteMap[s.charAt(i + offset)];
        }
        return bytes;
    }

    public final String toString(byte[] buffer) {
        return toString(buffer, 0, buffer.length);
    }

    public final String toString(byte[] buffer, int startPos, int length) {
        char[] charArray = new char[length];
        int readpoint = startPos;
        for (int i = 0; i < length; i++) {
            charArray[i] = this.byteToChars[buffer[readpoint] + 128];
            readpoint++;
        }
        return new String(charArray);
    }
}
