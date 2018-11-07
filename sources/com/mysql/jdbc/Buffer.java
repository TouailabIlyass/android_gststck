package com.mysql.jdbc;

import android.support.v4.media.session.PlaybackStateCompat;
import android.support.v4.view.ViewCompat;
import java.io.PrintStream;
import java.io.UnsupportedEncodingException;
import java.nio.ByteBuffer;
import java.sql.SQLException;

public class Buffer {
    static final int MAX_BYTES_TO_DUMP = 512;
    static final int NO_LENGTH_LIMIT = -1;
    static final long NULL_LENGTH = -1;
    public static final short TYPE_ID_AUTH_SWITCH = (short) 254;
    public static final short TYPE_ID_EOF = (short) 254;
    public static final short TYPE_ID_ERROR = (short) 255;
    public static final short TYPE_ID_LOCAL_INFILE = (short) 251;
    public static final short TYPE_ID_OK = (short) 0;
    private int bufLength = 0;
    private byte[] byteBuffer;
    private int position = 0;
    protected boolean wasMultiPacket = false;

    public Buffer(byte[] buf) {
        this.byteBuffer = buf;
        setBufLength(buf.length);
    }

    Buffer(int size) {
        this.byteBuffer = new byte[size];
        setBufLength(this.byteBuffer.length);
        this.position = 4;
    }

    final void clear() {
        this.position = 4;
    }

    final void dump() {
        dump(getBufLength());
    }

    final String dump(int numBytes) {
        return StringUtils.dumpAsHex(getBytes(0, numBytes > getBufLength() ? getBufLength() : numBytes), numBytes > getBufLength() ? getBufLength() : numBytes);
    }

    final String dumpClampedBytes(int numBytes) {
        int numBytesToDump = 512;
        if (numBytes < 512) {
            numBytesToDump = numBytes;
        }
        String dumped = StringUtils.dumpAsHex(getBytes(0, numBytesToDump > getBufLength() ? getBufLength() : numBytesToDump), numBytesToDump > getBufLength() ? getBufLength() : numBytesToDump);
        if (numBytesToDump >= numBytes) {
            return dumped;
        }
        StringBuilder stringBuilder = new StringBuilder();
        stringBuilder.append(dumped);
        stringBuilder.append(" ....(packet exceeds max. dump length)");
        return stringBuilder.toString();
    }

    final void dumpHeader() {
        for (int i = 0; i < 4; i++) {
            String hexVal = Integer.toHexString(readByte(i) & 255);
            if (hexVal.length() == 1) {
                StringBuilder stringBuilder = new StringBuilder();
                stringBuilder.append("0");
                stringBuilder.append(hexVal);
                hexVal = stringBuilder.toString();
            }
            PrintStream printStream = System.out;
            StringBuilder stringBuilder2 = new StringBuilder();
            stringBuilder2.append(hexVal);
            stringBuilder2.append(" ");
            printStream.print(stringBuilder2.toString());
        }
    }

    final void dumpNBytes(int start, int nBytes) {
        StringBuilder asciiBuf = new StringBuilder();
        int i = start;
        while (i < start + nBytes && i < getBufLength()) {
            String hexVal = Integer.toHexString(readByte(i) & 255);
            if (hexVal.length() == 1) {
                StringBuilder stringBuilder = new StringBuilder();
                stringBuilder.append("0");
                stringBuilder.append(hexVal);
                hexVal = stringBuilder.toString();
            }
            PrintStream printStream = System.out;
            StringBuilder stringBuilder2 = new StringBuilder();
            stringBuilder2.append(hexVal);
            stringBuilder2.append(" ");
            printStream.print(stringBuilder2.toString());
            if (readByte(i) <= (byte) 32 || readByte(i) >= Byte.MAX_VALUE) {
                asciiBuf.append(".");
            } else {
                asciiBuf.append((char) readByte(i));
            }
            asciiBuf.append(" ");
            i++;
        }
        PrintStream printStream2 = System.out;
        StringBuilder stringBuilder3 = new StringBuilder();
        stringBuilder3.append("    ");
        stringBuilder3.append(asciiBuf.toString());
        printStream2.println(stringBuilder3.toString());
    }

    final void ensureCapacity(int additionalData) throws SQLException {
        if (this.position + additionalData <= getBufLength()) {
            return;
        }
        if (this.position + additionalData < this.byteBuffer.length) {
            setBufLength(this.byteBuffer.length);
            return;
        }
        int newLength = (int) (((double) this.byteBuffer.length) * 1.25d);
        if (newLength < this.byteBuffer.length + additionalData) {
            newLength = this.byteBuffer.length + ((int) (((double) additionalData) * 1.25d));
        }
        if (newLength < this.byteBuffer.length) {
            newLength = this.byteBuffer.length + additionalData;
        }
        byte[] newBytes = new byte[newLength];
        System.arraycopy(this.byteBuffer, 0, newBytes, 0, this.byteBuffer.length);
        this.byteBuffer = newBytes;
        setBufLength(this.byteBuffer.length);
    }

    public int fastSkipLenString() {
        long len = readFieldLength();
        this.position = (int) (((long) this.position) + len);
        return (int) len;
    }

    public void fastSkipLenByteArray() {
        long len = readFieldLength();
        if (len != -1) {
            if (len != 0) {
                this.position = (int) (((long) this.position) + len);
            }
        }
    }

    protected final byte[] getBufferSource() {
        return this.byteBuffer;
    }

    public int getBufLength() {
        return this.bufLength;
    }

    public byte[] getByteBuffer() {
        return this.byteBuffer;
    }

    final byte[] getBytes(int len) {
        byte[] b = new byte[len];
        System.arraycopy(this.byteBuffer, this.position, b, 0, len);
        this.position += len;
        return b;
    }

    byte[] getBytes(int offset, int len) {
        byte[] dest = new byte[len];
        System.arraycopy(this.byteBuffer, offset, dest, 0, len);
        return dest;
    }

    int getCapacity() {
        return this.byteBuffer.length;
    }

    public ByteBuffer getNioBuffer() {
        throw new IllegalArgumentException(Messages.getString("ByteArrayBuffer.0"));
    }

    public int getPosition() {
        return this.position;
    }

    final boolean isEOFPacket() {
        return (this.byteBuffer[0] & 255) == 254 && getBufLength() <= 5;
    }

    final boolean isAuthMethodSwitchRequestPacket() {
        return (this.byteBuffer[0] & 255) == 254;
    }

    final boolean isOKPacket() {
        return (this.byteBuffer[0] & 255) == 0;
    }

    final boolean isResultSetOKPacket() {
        return (this.byteBuffer[0] & 255) == 254 && getBufLength() < ViewCompat.MEASURED_SIZE_MASK;
    }

    final boolean isRawPacket() {
        return (this.byteBuffer[0] & 255) == 1;
    }

    final long newReadLength() {
        byte[] bArr = this.byteBuffer;
        int i = this.position;
        this.position = i + 1;
        int sw = bArr[i] & 255;
        switch (sw) {
            case 251:
                return 0;
            case MysqlDefs.FIELD_TYPE_BLOB /*252*/:
                return (long) readInt();
            case 253:
                return (long) readLongInt();
            case 254:
                return readLongLong();
            default:
                return (long) sw;
        }
    }

    final byte readByte() {
        byte[] bArr = this.byteBuffer;
        int i = this.position;
        this.position = i + 1;
        return bArr[i];
    }

    final byte readByte(int readAt) {
        return this.byteBuffer[readAt];
    }

    final long readFieldLength() {
        byte[] bArr = this.byteBuffer;
        int i = this.position;
        this.position = i + 1;
        int sw = bArr[i] & 255;
        switch (sw) {
            case 251:
                return -1;
            case MysqlDefs.FIELD_TYPE_BLOB /*252*/:
                return (long) readInt();
            case 253:
                return (long) readLongInt();
            case 254:
                return readLongLong();
            default:
                return (long) sw;
        }
    }

    final int readInt() {
        byte[] b = this.byteBuffer;
        int i = this.position;
        this.position = i + 1;
        i = b[i] & 255;
        int i2 = this.position;
        this.position = i2 + 1;
        return i | ((b[i2] & 255) << 8);
    }

    final int readIntAsLong() {
        byte[] b = this.byteBuffer;
        int i = this.position;
        this.position = i + 1;
        i = b[i] & 255;
        int i2 = this.position;
        this.position = i2 + 1;
        i |= (b[i2] & 255) << 8;
        i2 = this.position;
        this.position = i2 + 1;
        i |= (b[i2] & 255) << 16;
        i2 = this.position;
        this.position = i2 + 1;
        return i | ((b[i2] & 255) << 24);
    }

    final byte[] readLenByteArray(int offset) {
        long len = readFieldLength();
        if (len == -1) {
            return null;
        }
        if (len == 0) {
            return Constants.EMPTY_BYTE_ARRAY;
        }
        this.position += offset;
        return getBytes((int) len);
    }

    final long readLength() {
        byte[] bArr = this.byteBuffer;
        int i = this.position;
        this.position = i + 1;
        int sw = bArr[i] & 255;
        switch (sw) {
            case 251:
                return 0;
            case MysqlDefs.FIELD_TYPE_BLOB /*252*/:
                return (long) readInt();
            case 253:
                return (long) readLongInt();
            case 254:
                return readLong();
            default:
                return (long) sw;
        }
    }

    final long readLong() {
        byte[] b = this.byteBuffer;
        int i = this.position;
        this.position = i + 1;
        long j = ((long) b[i]) & 255;
        i = this.position;
        this.position = i + 1;
        long j2 = j | ((((long) b[i]) & 255) << 8);
        i = this.position;
        this.position = i + 1;
        j = j2 | (((long) (b[i] & 255)) << 16);
        i = this.position;
        this.position = i + 1;
        return j | (((long) (b[i] & 255)) << 24);
    }

    final int readLongInt() {
        byte[] b = this.byteBuffer;
        int i = this.position;
        this.position = i + 1;
        i = b[i] & 255;
        int i2 = this.position;
        this.position = i2 + 1;
        i |= (b[i2] & 255) << 8;
        i2 = this.position;
        this.position = i2 + 1;
        return i | ((b[i2] & 255) << 16);
    }

    final long readLongLong() {
        byte[] b = this.byteBuffer;
        int i = this.position;
        this.position = i + 1;
        long j = (long) (b[i] & 255);
        int i2 = this.position;
        this.position = i2 + 1;
        long j2 = j | (((long) (b[i2] & 255)) << 8);
        i = this.position;
        this.position = i + 1;
        long j3 = j2 | (((long) (b[i] & 255)) << 16);
        i = this.position;
        this.position = i + 1;
        j2 = j3 | (((long) (b[i] & 255)) << 24);
        i = this.position;
        this.position = i + 1;
        j3 = j2 | (((long) (b[i] & 255)) << 32);
        i = this.position;
        this.position = i + 1;
        j2 = j3 | (((long) (b[i] & 255)) << 40);
        i = this.position;
        this.position = i + 1;
        j3 = j2 | (((long) (b[i] & 255)) << 48);
        i = this.position;
        this.position = i + 1;
        return j3 | (((long) (b[i] & 255)) << 56);
    }

    final int readnBytes() {
        byte[] bArr = this.byteBuffer;
        int i = this.position;
        this.position = i + 1;
        switch (bArr[i] & 255) {
            case 1:
                byte[] bArr2 = this.byteBuffer;
                int i2 = this.position;
                this.position = i2 + 1;
                return 255 & bArr2[i2];
            case 2:
                return readInt();
            case 3:
                return readLongInt();
            case 4:
                return (int) readLong();
            default:
                return 255;
        }
    }

    public final String readString() {
        int i = this.position;
        int len = 0;
        int maxLen = getBufLength();
        while (i < maxLen && this.byteBuffer[i] != (byte) 0) {
            len++;
            i++;
        }
        String s = StringUtils.toString(this.byteBuffer, this.position, len);
        this.position += len + 1;
        return s;
    }

    final String readString(String encoding, ExceptionInterceptor exceptionInterceptor) throws SQLException {
        int i = this.position;
        int len = 0;
        int maxLen = getBufLength();
        while (i < maxLen && this.byteBuffer[i] != (byte) 0) {
            len++;
            i++;
        }
        try {
            String stringUtils = StringUtils.toString(this.byteBuffer, this.position, len, encoding);
            this.position += len + 1;
            return stringUtils;
        } catch (UnsupportedEncodingException e) {
            StringBuilder stringBuilder = new StringBuilder();
            stringBuilder.append(Messages.getString("ByteArrayBuffer.1"));
            stringBuilder.append(encoding);
            stringBuilder.append("'");
            throw SQLError.createSQLException(stringBuilder.toString(), SQLError.SQL_STATE_ILLEGAL_ARGUMENT, exceptionInterceptor);
        } catch (Throwable th) {
            this.position += len + 1;
        }
    }

    final String readString(String encoding, ExceptionInterceptor exceptionInterceptor, int expectedLength) throws SQLException {
        if (this.position + expectedLength > getBufLength()) {
            throw SQLError.createSQLException(Messages.getString("ByteArrayBuffer.2"), SQLError.SQL_STATE_ILLEGAL_ARGUMENT, exceptionInterceptor);
        }
        try {
            String stringUtils = StringUtils.toString(this.byteBuffer, this.position, expectedLength, encoding);
            this.position += expectedLength;
            return stringUtils;
        } catch (UnsupportedEncodingException e) {
            StringBuilder stringBuilder = new StringBuilder();
            stringBuilder.append(Messages.getString("ByteArrayBuffer.1"));
            stringBuilder.append(encoding);
            stringBuilder.append("'");
            throw SQLError.createSQLException(stringBuilder.toString(), SQLError.SQL_STATE_ILLEGAL_ARGUMENT, exceptionInterceptor);
        } catch (Throwable th) {
            this.position += expectedLength;
        }
    }

    public void setBufLength(int bufLengthToSet) {
        this.bufLength = bufLengthToSet;
    }

    public void setByteBuffer(byte[] byteBufferToSet) {
        this.byteBuffer = byteBufferToSet;
    }

    public void setPosition(int positionToSet) {
        this.position = positionToSet;
    }

    public void setWasMultiPacket(boolean flag) {
        this.wasMultiPacket = flag;
    }

    public String toString() {
        return dumpClampedBytes(getPosition());
    }

    public String toSuperString() {
        return super.toString();
    }

    public boolean wasMultiPacket() {
        return this.wasMultiPacket;
    }

    public final void writeByte(byte b) throws SQLException {
        ensureCapacity(1);
        byte[] bArr = this.byteBuffer;
        int i = this.position;
        this.position = i + 1;
        bArr[i] = b;
    }

    public final void writeBytesNoNull(byte[] bytes) throws SQLException {
        int len = bytes.length;
        ensureCapacity(len);
        System.arraycopy(bytes, 0, this.byteBuffer, this.position, len);
        this.position += len;
    }

    final void writeBytesNoNull(byte[] bytes, int offset, int length) throws SQLException {
        ensureCapacity(length);
        System.arraycopy(bytes, offset, this.byteBuffer, this.position, length);
        this.position += length;
    }

    final void writeDouble(double d) throws SQLException {
        writeLongLong(Double.doubleToLongBits(d));
    }

    final void writeFieldLength(long length) throws SQLException {
        if (length < 251) {
            writeByte((byte) ((int) length));
        } else if (length < PlaybackStateCompat.ACTION_PREPARE_FROM_SEARCH) {
            ensureCapacity(3);
            writeByte((byte) -4);
            writeInt((int) length);
        } else if (length < 16777216) {
            ensureCapacity(4);
            writeByte((byte) -3);
            writeLongInt((int) length);
        } else {
            ensureCapacity(9);
            writeByte((byte) -2);
            writeLongLong(length);
        }
    }

    final void writeFloat(float f) throws SQLException {
        ensureCapacity(4);
        int i = Float.floatToIntBits(f);
        byte[] b = this.byteBuffer;
        int i2 = this.position;
        this.position = i2 + 1;
        b[i2] = (byte) (i & 255);
        i2 = this.position;
        this.position = i2 + 1;
        b[i2] = (byte) (i >>> 8);
        i2 = this.position;
        this.position = i2 + 1;
        b[i2] = (byte) (i >>> 16);
        i2 = this.position;
        this.position = i2 + 1;
        b[i2] = (byte) (i >>> 24);
    }

    final void writeInt(int i) throws SQLException {
        ensureCapacity(2);
        byte[] b = this.byteBuffer;
        int i2 = this.position;
        this.position = i2 + 1;
        b[i2] = (byte) (i & 255);
        i2 = this.position;
        this.position = i2 + 1;
        b[i2] = (byte) (i >>> 8);
    }

    final void writeLenBytes(byte[] b) throws SQLException {
        int len = b.length;
        ensureCapacity(len + 9);
        writeFieldLength((long) len);
        System.arraycopy(b, 0, this.byteBuffer, this.position, len);
        this.position += len;
    }

    final void writeLenString(String s, String encoding, String serverEncoding, SingleByteCharsetConverter converter, boolean parserKnowsUnicode, MySQLConnection conn) throws UnsupportedEncodingException, SQLException {
        byte[] b;
        if (converter != null) {
            b = converter.toBytes(s);
        } else {
            b = StringUtils.getBytes(s, encoding, serverEncoding, parserKnowsUnicode, conn, conn.getExceptionInterceptor());
        }
        int len = b.length;
        ensureCapacity(len + 9);
        writeFieldLength((long) len);
        System.arraycopy(b, 0, this.byteBuffer, this.position, len);
        this.position += len;
    }

    final void writeLong(long i) throws SQLException {
        ensureCapacity(4);
        byte[] b = this.byteBuffer;
        int i2 = this.position;
        this.position = i2 + 1;
        b[i2] = (byte) ((int) (i & 255));
        i2 = this.position;
        this.position = i2 + 1;
        b[i2] = (byte) ((int) (i >>> 8));
        i2 = this.position;
        this.position = i2 + 1;
        b[i2] = (byte) ((int) (i >>> 16));
        i2 = this.position;
        this.position = i2 + 1;
        b[i2] = (byte) ((int) (i >>> 24));
    }

    final void writeLongInt(int i) throws SQLException {
        ensureCapacity(3);
        byte[] b = this.byteBuffer;
        int i2 = this.position;
        this.position = i2 + 1;
        b[i2] = (byte) (i & 255);
        i2 = this.position;
        this.position = i2 + 1;
        b[i2] = (byte) (i >>> 8);
        i2 = this.position;
        this.position = i2 + 1;
        b[i2] = (byte) (i >>> 16);
    }

    final void writeLongLong(long i) throws SQLException {
        ensureCapacity(8);
        byte[] b = this.byteBuffer;
        int i2 = this.position;
        this.position = i2 + 1;
        b[i2] = (byte) ((int) (i & 255));
        i2 = this.position;
        this.position = i2 + 1;
        b[i2] = (byte) ((int) (i >>> 8));
        int i3 = this.position;
        this.position = i3 + 1;
        b[i3] = (byte) ((int) (i >>> 16));
        i3 = this.position;
        this.position = i3 + 1;
        b[i3] = (byte) ((int) (i >>> 24));
        i3 = this.position;
        this.position = i3 + 1;
        b[i3] = (byte) ((int) (i >>> 32));
        i3 = this.position;
        this.position = i3 + 1;
        b[i3] = (byte) ((int) (i >>> 40));
        i3 = this.position;
        this.position = i3 + 1;
        b[i3] = (byte) ((int) (i >>> 48));
        i3 = this.position;
        this.position = i3 + 1;
        b[i3] = (byte) ((int) (i >>> 56));
    }

    final void writeString(String s) throws SQLException {
        ensureCapacity((s.length() * 3) + 1);
        writeStringNoNull(s);
        byte[] bArr = this.byteBuffer;
        int i = this.position;
        this.position = i + 1;
        bArr[i] = (byte) 0;
    }

    final void writeString(String s, String encoding, MySQLConnection conn) throws SQLException {
        ensureCapacity((s.length() * 3) + 1);
        try {
            writeStringNoNull(s, encoding, encoding, false, conn);
            byte[] bArr = this.byteBuffer;
            int i = this.position;
            this.position = i + 1;
            bArr[i] = (byte) 0;
        } catch (UnsupportedEncodingException ue) {
            throw new SQLException(ue.toString(), SQLError.SQL_STATE_GENERAL_ERROR);
        }
    }

    final void writeStringNoNull(String s) throws SQLException {
        int len = s.length();
        ensureCapacity(len * 3);
        System.arraycopy(StringUtils.getBytes(s), 0, this.byteBuffer, this.position, len);
        this.position += len;
    }

    final void writeStringNoNull(String s, String encoding, String serverEncoding, boolean parserKnowsUnicode, MySQLConnection conn) throws UnsupportedEncodingException, SQLException {
        byte[] b = StringUtils.getBytes(s, encoding, serverEncoding, parserKnowsUnicode, conn, conn.getExceptionInterceptor());
        int len = b.length;
        ensureCapacity(len);
        System.arraycopy(b, 0, this.byteBuffer, this.position, len);
        this.position += len;
    }
}
