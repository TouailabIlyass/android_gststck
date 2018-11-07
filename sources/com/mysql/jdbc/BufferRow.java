package com.mysql.jdbc;

import android.support.v7.widget.helper.ItemTouchHelper.Callback;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.io.UnsupportedEncodingException;
import java.sql.Date;
import java.sql.SQLException;
import java.sql.Time;
import java.sql.Timestamp;
import java.util.Calendar;
import java.util.List;
import java.util.TimeZone;

public class BufferRow extends ResultSetRow {
    private int homePosition = 0;
    private boolean isBinaryEncoded;
    private boolean[] isNull;
    private int lastRequestedIndex = -1;
    private int lastRequestedPos;
    private Field[] metadata;
    private List<InputStream> openStreams;
    private int preNullBitmaskHomePosition = 0;
    private Buffer rowFromServer;

    public BufferRow(Buffer buf, Field[] fields, boolean isBinaryEncoded, ExceptionInterceptor exceptionInterceptor) throws SQLException {
        super(exceptionInterceptor);
        this.rowFromServer = buf;
        this.metadata = fields;
        this.isBinaryEncoded = isBinaryEncoded;
        this.homePosition = this.rowFromServer.getPosition();
        this.preNullBitmaskHomePosition = this.homePosition;
        if (fields != null) {
            setMetadata(fields);
        }
    }

    public synchronized void closeOpenStreams() {
        if (this.openStreams != null) {
            for (InputStream close : this.openStreams) {
                try {
                    close.close();
                } catch (IOException e) {
                }
            }
            this.openStreams.clear();
        }
    }

    private int findAndSeekToOffset(int index) throws SQLException {
        if (this.isBinaryEncoded) {
            return findAndSeekToOffsetForBinaryEncoding(index);
        }
        if (index == 0) {
            this.lastRequestedIndex = 0;
            this.lastRequestedPos = this.homePosition;
            this.rowFromServer.setPosition(this.homePosition);
            return 0;
        } else if (index == this.lastRequestedIndex) {
            this.rowFromServer.setPosition(this.lastRequestedPos);
            return this.lastRequestedPos;
        } else {
            int startingIndex = 0;
            if (index > this.lastRequestedIndex) {
                if (this.lastRequestedIndex >= 0) {
                    startingIndex = this.lastRequestedIndex;
                } else {
                    startingIndex = 0;
                }
                this.rowFromServer.setPosition(this.lastRequestedPos);
            } else {
                this.rowFromServer.setPosition(this.homePosition);
            }
            for (int i = startingIndex; i < index; i++) {
                this.rowFromServer.fastSkipLenByteArray();
            }
            this.lastRequestedIndex = index;
            this.lastRequestedPos = this.rowFromServer.getPosition();
            return this.lastRequestedPos;
        }
    }

    private int findAndSeekToOffsetForBinaryEncoding(int index) throws SQLException {
        if (index == 0) {
            this.lastRequestedIndex = 0;
            this.lastRequestedPos = this.homePosition;
            this.rowFromServer.setPosition(this.homePosition);
            return 0;
        } else if (index == this.lastRequestedIndex) {
            this.rowFromServer.setPosition(this.lastRequestedPos);
            return this.lastRequestedPos;
        } else {
            int startingIndex = 0;
            if (index > this.lastRequestedIndex) {
                if (this.lastRequestedIndex >= 0) {
                    startingIndex = this.lastRequestedIndex;
                } else {
                    startingIndex = 0;
                    this.lastRequestedPos = this.homePosition;
                }
                this.rowFromServer.setPosition(this.lastRequestedPos);
            } else {
                this.rowFromServer.setPosition(this.homePosition);
            }
            for (int i = startingIndex; i < index; i++) {
                if (!this.isNull[i]) {
                    int curPosition = this.rowFromServer.getPosition();
                    int mysqlType = this.metadata[i].getMysqlType();
                    switch (mysqlType) {
                        case 0:
                            break;
                        case 1:
                            this.rowFromServer.setPosition(curPosition + 1);
                            continue;
                        case 2:
                        case 13:
                            this.rowFromServer.setPosition(curPosition + 2);
                            continue;
                        case 3:
                        case 9:
                            this.rowFromServer.setPosition(curPosition + 4);
                            continue;
                        case 4:
                            this.rowFromServer.setPosition(curPosition + 4);
                            continue;
                        case 5:
                            this.rowFromServer.setPosition(curPosition + 8);
                            continue;
                        case 6:
                            continue;
                        case 7:
                        case 12:
                            this.rowFromServer.fastSkipLenByteArray();
                            continue;
                        case 8:
                            this.rowFromServer.setPosition(curPosition + 8);
                            continue;
                        case 10:
                            this.rowFromServer.fastSkipLenByteArray();
                            continue;
                        case 11:
                            this.rowFromServer.fastSkipLenByteArray();
                            continue;
                        default:
                            switch (mysqlType) {
                                case 15:
                                case 16:
                                    break;
                                default:
                                    switch (mysqlType) {
                                        case 245:
                                        case 246:
                                            break;
                                        default:
                                            switch (mysqlType) {
                                                case 249:
                                                case Callback.DEFAULT_SWIPE_ANIMATION_DURATION /*250*/:
                                                case 251:
                                                case MysqlDefs.FIELD_TYPE_BLOB /*252*/:
                                                case 253:
                                                case 254:
                                                case 255:
                                                    break;
                                                default:
                                                    StringBuilder stringBuilder = new StringBuilder();
                                                    stringBuilder.append(Messages.getString("MysqlIO.97"));
                                                    stringBuilder.append(this.metadata[i].getMysqlType());
                                                    stringBuilder.append(Messages.getString("MysqlIO.98"));
                                                    stringBuilder.append(i + 1);
                                                    stringBuilder.append(Messages.getString("MysqlIO.99"));
                                                    stringBuilder.append(this.metadata.length);
                                                    stringBuilder.append(Messages.getString("MysqlIO.100"));
                                                    throw SQLError.createSQLException(stringBuilder.toString(), SQLError.SQL_STATE_GENERAL_ERROR, this.exceptionInterceptor);
                                            }
                                    }
                            }
                    }
                    this.rowFromServer.fastSkipLenByteArray();
                }
            }
            this.lastRequestedIndex = index;
            this.lastRequestedPos = this.rowFromServer.getPosition();
            return this.lastRequestedPos;
        }
    }

    /* JADX WARNING: inconsistent code. */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    public synchronized java.io.InputStream getBinaryInputStream(int r8) throws java.sql.SQLException {
        /*
        r7 = this;
        monitor-enter(r7);
        r0 = r7.isBinaryEncoded;	 Catch:{ all -> 0x003e }
        r1 = 0;
        if (r0 == 0) goto L_0x000e;
    L_0x0006:
        r0 = r7.isNull(r8);	 Catch:{ all -> 0x003e }
        if (r0 == 0) goto L_0x000e;
    L_0x000c:
        monitor-exit(r7);
        return r1;
    L_0x000e:
        r7.findAndSeekToOffset(r8);	 Catch:{ all -> 0x003e }
        r0 = r7.rowFromServer;	 Catch:{ all -> 0x003e }
        r2 = r0.readFieldLength();	 Catch:{ all -> 0x003e }
        r0 = r7.rowFromServer;	 Catch:{ all -> 0x003e }
        r0 = r0.getPosition();	 Catch:{ all -> 0x003e }
        r4 = -1;
        r6 = (r2 > r4 ? 1 : (r2 == r4 ? 0 : -1));
        if (r6 != 0) goto L_0x0025;
    L_0x0023:
        monitor-exit(r7);
        return r1;
    L_0x0025:
        r1 = new java.io.ByteArrayInputStream;	 Catch:{ all -> 0x003e }
        r4 = r7.rowFromServer;	 Catch:{ all -> 0x003e }
        r4 = r4.getByteBuffer();	 Catch:{ all -> 0x003e }
        r5 = (int) r2;	 Catch:{ all -> 0x003e }
        r1.<init>(r4, r0, r5);	 Catch:{ all -> 0x003e }
        r4 = r7.openStreams;	 Catch:{ all -> 0x003e }
        if (r4 != 0) goto L_0x003c;
    L_0x0035:
        r4 = new java.util.LinkedList;	 Catch:{ all -> 0x003e }
        r4.<init>();	 Catch:{ all -> 0x003e }
        r7.openStreams = r4;	 Catch:{ all -> 0x003e }
    L_0x003c:
        monitor-exit(r7);
        return r1;
    L_0x003e:
        r8 = move-exception;
        monitor-exit(r7);
        throw r8;
        */
        throw new UnsupportedOperationException("Method not decompiled: com.mysql.jdbc.BufferRow.getBinaryInputStream(int):java.io.InputStream");
    }

    public byte[] getColumnValue(int index) throws SQLException {
        findAndSeekToOffset(index);
        if (!this.isBinaryEncoded) {
            return this.rowFromServer.readLenByteArray(0);
        }
        if (this.isNull[index]) {
            return null;
        }
        int mysqlType = this.metadata[index].getMysqlType();
        switch (mysqlType) {
            case 0:
            case 7:
            case 10:
            case 11:
            case 12:
                break;
            case 1:
                return new byte[]{this.rowFromServer.readByte()};
            case 2:
            case 13:
                return this.rowFromServer.getBytes(2);
            case 3:
            case 9:
                return this.rowFromServer.getBytes(4);
            case 4:
                return this.rowFromServer.getBytes(4);
            case 5:
                return this.rowFromServer.getBytes(8);
            case 6:
                return null;
            case 8:
                return this.rowFromServer.getBytes(8);
            default:
                switch (mysqlType) {
                    case 15:
                    case 16:
                        break;
                    default:
                        switch (mysqlType) {
                            case 245:
                            case 246:
                                break;
                            default:
                                switch (mysqlType) {
                                    case 249:
                                    case Callback.DEFAULT_SWIPE_ANIMATION_DURATION /*250*/:
                                    case 251:
                                    case MysqlDefs.FIELD_TYPE_BLOB /*252*/:
                                    case 253:
                                    case 254:
                                    case 255:
                                        break;
                                    default:
                                        StringBuilder stringBuilder = new StringBuilder();
                                        stringBuilder.append(Messages.getString("MysqlIO.97"));
                                        stringBuilder.append(this.metadata[index].getMysqlType());
                                        stringBuilder.append(Messages.getString("MysqlIO.98"));
                                        stringBuilder.append(index + 1);
                                        stringBuilder.append(Messages.getString("MysqlIO.99"));
                                        stringBuilder.append(this.metadata.length);
                                        stringBuilder.append(Messages.getString("MysqlIO.100"));
                                        throw SQLError.createSQLException(stringBuilder.toString(), SQLError.SQL_STATE_GENERAL_ERROR, this.exceptionInterceptor);
                                }
                        }
                }
        }
        return this.rowFromServer.readLenByteArray(0);
    }

    public int getInt(int columnIndex) throws SQLException {
        findAndSeekToOffset(columnIndex);
        long length = this.rowFromServer.readFieldLength();
        int offset = this.rowFromServer.getPosition();
        if (length == -1) {
            return 0;
        }
        return StringUtils.getInt(this.rowFromServer.getByteBuffer(), offset, ((int) length) + offset);
    }

    public long getLong(int columnIndex) throws SQLException {
        findAndSeekToOffset(columnIndex);
        long length = this.rowFromServer.readFieldLength();
        int offset = this.rowFromServer.getPosition();
        if (length == -1) {
            return 0;
        }
        return StringUtils.getLong(this.rowFromServer.getByteBuffer(), offset, ((int) length) + offset);
    }

    public double getNativeDouble(int columnIndex) throws SQLException {
        if (isNull(columnIndex)) {
            return 0.0d;
        }
        findAndSeekToOffset(columnIndex);
        return getNativeDouble(this.rowFromServer.getByteBuffer(), this.rowFromServer.getPosition());
    }

    public float getNativeFloat(int columnIndex) throws SQLException {
        if (isNull(columnIndex)) {
            return 0.0f;
        }
        findAndSeekToOffset(columnIndex);
        return getNativeFloat(this.rowFromServer.getByteBuffer(), this.rowFromServer.getPosition());
    }

    public int getNativeInt(int columnIndex) throws SQLException {
        if (isNull(columnIndex)) {
            return 0;
        }
        findAndSeekToOffset(columnIndex);
        return getNativeInt(this.rowFromServer.getByteBuffer(), this.rowFromServer.getPosition());
    }

    public long getNativeLong(int columnIndex) throws SQLException {
        if (isNull(columnIndex)) {
            return 0;
        }
        findAndSeekToOffset(columnIndex);
        return getNativeLong(this.rowFromServer.getByteBuffer(), this.rowFromServer.getPosition());
    }

    public short getNativeShort(int columnIndex) throws SQLException {
        if (isNull(columnIndex)) {
            return (short) 0;
        }
        findAndSeekToOffset(columnIndex);
        return getNativeShort(this.rowFromServer.getByteBuffer(), this.rowFromServer.getPosition());
    }

    public Timestamp getNativeTimestamp(int columnIndex, Calendar targetCalendar, TimeZone tz, boolean rollForward, MySQLConnection conn, ResultSetImpl rs) throws SQLException {
        if (isNull(columnIndex)) {
            return null;
        }
        findAndSeekToOffset(columnIndex);
        long length = r9.rowFromServer.readFieldLength();
        return getNativeTimestamp(r9.rowFromServer.getByteBuffer(), r9.rowFromServer.getPosition(), (int) length, targetCalendar, tz, rollForward, conn, rs);
    }

    public Reader getReader(int columnIndex) throws SQLException {
        InputStream stream = getBinaryInputStream(columnIndex);
        if (stream == null) {
            return null;
        }
        try {
            return new InputStreamReader(stream, this.metadata[columnIndex].getEncoding());
        } catch (UnsupportedEncodingException e) {
            SQLException sqlEx = SQLError.createSQLException("", this.exceptionInterceptor);
            sqlEx.initCause(e);
            throw sqlEx;
        }
    }

    public String getString(int columnIndex, String encoding, MySQLConnection conn) throws SQLException {
        if (this.isBinaryEncoded && isNull(columnIndex)) {
            return null;
        }
        findAndSeekToOffset(columnIndex);
        long length = this.rowFromServer.readFieldLength();
        if (length == -1) {
            return null;
        }
        if (length == 0) {
            return "";
        }
        return getString(encoding, conn, this.rowFromServer.getByteBuffer(), this.rowFromServer.getPosition(), (int) length);
    }

    public Time getTimeFast(int columnIndex, Calendar targetCalendar, TimeZone tz, boolean rollForward, MySQLConnection conn, ResultSetImpl rs) throws SQLException {
        if (isNull(columnIndex)) {
            return null;
        }
        findAndSeekToOffset(columnIndex);
        long length = r10.rowFromServer.readFieldLength();
        return getTimeFast(columnIndex, r10.rowFromServer.getByteBuffer(), r10.rowFromServer.getPosition(), (int) length, targetCalendar, tz, rollForward, conn, rs);
    }

    public Timestamp getTimestampFast(int columnIndex, Calendar targetCalendar, TimeZone tz, boolean rollForward, MySQLConnection conn, ResultSetImpl rs) throws SQLException {
        if (isNull(columnIndex)) {
            return null;
        }
        findAndSeekToOffset(columnIndex);
        long length = r10.rowFromServer.readFieldLength();
        return getTimestampFast(columnIndex, r10.rowFromServer.getByteBuffer(), r10.rowFromServer.getPosition(), (int) length, targetCalendar, tz, rollForward, conn, rs);
    }

    public boolean isFloatingPointNumber(int index) throws SQLException {
        int sQLType;
        if (this.isBinaryEncoded) {
            sQLType = this.metadata[index].getSQLType();
            if (!(sQLType == 6 || sQLType == 8)) {
                switch (sQLType) {
                    case 2:
                    case 3:
                        break;
                    default:
                        return false;
                }
            }
            return true;
        }
        findAndSeekToOffset(index);
        long length = this.rowFromServer.readFieldLength();
        if (length == -1 || length == 0) {
            return false;
        }
        sQLType = this.rowFromServer.getPosition();
        byte[] buffer = this.rowFromServer.getByteBuffer();
        int i = 0;
        while (i < ((int) length)) {
            char c = (char) buffer[sQLType + i];
            if (c != 'e') {
                if (c != 'E') {
                    i++;
                }
            }
            return true;
        }
        return false;
    }

    public boolean isNull(int index) throws SQLException {
        if (this.isBinaryEncoded) {
            return this.isNull[index];
        }
        findAndSeekToOffset(index);
        return this.rowFromServer.readFieldLength() == -1;
    }

    public long length(int index) throws SQLException {
        findAndSeekToOffset(index);
        long length = this.rowFromServer.readFieldLength();
        if (length == -1) {
            return 0;
        }
        return length;
    }

    public void setColumnValue(int index, byte[] value) throws SQLException {
        throw new OperationNotSupportedException();
    }

    public ResultSetRow setMetadata(Field[] f) throws SQLException {
        super.setMetadata(f);
        if (this.isBinaryEncoded) {
            setupIsNullBitmask();
        }
        return this;
    }

    private void setupIsNullBitmask() throws SQLException {
        if (this.isNull == null) {
            int i;
            this.rowFromServer.setPosition(this.preNullBitmaskHomePosition);
            int nullCount = (this.metadata.length + 9) / 8;
            byte[] nullBitMask = new byte[nullCount];
            for (i = 0; i < nullCount; i++) {
                nullBitMask[i] = this.rowFromServer.readByte();
            }
            this.homePosition = this.rowFromServer.getPosition();
            this.isNull = new boolean[this.metadata.length];
            int bit = 4;
            int nullMaskPos = 0;
            for (i = 0; i < this.metadata.length; i++) {
                this.isNull[i] = (nullBitMask[nullMaskPos] & bit) != 0;
                int i2 = bit << 1;
                bit = i2;
                if ((i2 & 255) == 0) {
                    bit = 1;
                    nullMaskPos++;
                }
            }
        }
    }

    public Date getDateFast(int columnIndex, MySQLConnection conn, ResultSetImpl rs, Calendar targetCalendar) throws SQLException {
        if (isNull(columnIndex)) {
            return null;
        }
        findAndSeekToOffset(columnIndex);
        long length = this.rowFromServer.readFieldLength();
        return getDateFast(columnIndex, this.rowFromServer.getByteBuffer(), this.rowFromServer.getPosition(), (int) length, conn, rs, targetCalendar);
    }

    public Date getNativeDate(int columnIndex, MySQLConnection conn, ResultSetImpl rs, Calendar cal) throws SQLException {
        if (isNull(columnIndex)) {
            return null;
        }
        findAndSeekToOffset(columnIndex);
        long length = this.rowFromServer.readFieldLength();
        return getNativeDate(columnIndex, this.rowFromServer.getByteBuffer(), this.rowFromServer.getPosition(), (int) length, conn, rs, cal);
    }

    public Object getNativeDateTimeValue(int columnIndex, Calendar targetCalendar, int jdbcType, int mysqlType, TimeZone tz, boolean rollForward, MySQLConnection conn, ResultSetImpl rs) throws SQLException {
        BufferRow bufferRow = this;
        if (isNull(columnIndex)) {
            return null;
        }
        findAndSeekToOffset(columnIndex);
        long length = bufferRow.rowFromServer.readFieldLength();
        return getNativeDateTimeValue(columnIndex, bufferRow.rowFromServer.getByteBuffer(), bufferRow.rowFromServer.getPosition(), (int) length, targetCalendar, jdbcType, mysqlType, tz, rollForward, conn, rs);
    }

    public Time getNativeTime(int columnIndex, Calendar targetCalendar, TimeZone tz, boolean rollForward, MySQLConnection conn, ResultSetImpl rs) throws SQLException {
        if (isNull(columnIndex)) {
            return null;
        }
        findAndSeekToOffset(columnIndex);
        long length = r10.rowFromServer.readFieldLength();
        return getNativeTime(columnIndex, r10.rowFromServer.getByteBuffer(), r10.rowFromServer.getPosition(), (int) length, targetCalendar, tz, rollForward, conn, rs);
    }

    public int getBytesSize() {
        return this.rowFromServer.getBufLength();
    }
}
