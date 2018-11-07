package com.mysql.jdbc.profiler;

import com.mysql.jdbc.StringUtils;
import java.util.Date;

public class ProfilerEvent {
    public static final byte TYPE_EXECUTE = (byte) 4;
    public static final byte TYPE_FETCH = (byte) 5;
    public static final byte TYPE_OBJECT_CREATION = (byte) 1;
    public static final byte TYPE_PREPARE = (byte) 2;
    public static final byte TYPE_QUERY = (byte) 3;
    public static final byte TYPE_SLOW_QUERY = (byte) 6;
    public static final byte TYPE_WARN = (byte) 0;
    protected String catalog;
    protected int catalogIndex;
    protected long connectionId;
    protected String durationUnits;
    protected String eventCreationPointDesc;
    protected int eventCreationPointIndex;
    protected long eventCreationTime;
    protected long eventDuration;
    protected byte eventType;
    protected String hostName;
    protected int hostNameIndex;
    protected String message;
    protected int resultSetId;
    protected int statementId;

    public ProfilerEvent(byte eventType, String hostName, String catalog, long connectionId, int statementId, int resultSetId, long eventCreationTime, long eventDuration, String durationUnits, String eventCreationPointDesc, String eventCreationPoint, String message) {
        this.eventType = eventType;
        this.connectionId = connectionId;
        this.statementId = statementId;
        this.resultSetId = resultSetId;
        this.eventCreationTime = eventCreationTime;
        this.eventDuration = eventDuration;
        this.durationUnits = durationUnits;
        this.eventCreationPointDesc = eventCreationPointDesc;
        this.message = message;
    }

    public String getEventCreationPointAsString() {
        return this.eventCreationPointDesc;
    }

    public String toString() {
        StringBuilder buf = new StringBuilder(32);
        switch (this.eventType) {
            case (byte) 0:
                buf.append("WARN");
                break;
            case (byte) 1:
                buf.append("CONSTRUCT");
                break;
            case (byte) 2:
                buf.append("PREPARE");
                break;
            case (byte) 3:
                buf.append("QUERY");
                break;
            case (byte) 4:
                buf.append("EXECUTE");
                break;
            case (byte) 5:
                buf.append("FETCH");
                break;
            case (byte) 6:
                buf.append("SLOW QUERY");
                break;
            default:
                buf.append("UNKNOWN");
                break;
        }
        buf.append(" created: ");
        buf.append(new Date(this.eventCreationTime));
        buf.append(" duration: ");
        buf.append(this.eventDuration);
        buf.append(" connection: ");
        buf.append(this.connectionId);
        buf.append(" statement: ");
        buf.append(this.statementId);
        buf.append(" resultset: ");
        buf.append(this.resultSetId);
        if (this.message != null) {
            buf.append(" message: ");
            buf.append(this.message);
        }
        if (this.eventCreationPointDesc != null) {
            buf.append("\n\nEvent Created at:\n");
            buf.append(this.eventCreationPointDesc);
        }
        return buf.toString();
    }

    public static ProfilerEvent unpack(byte[] buf) throws Exception {
        byte[] bArr = buf;
        int pos = 0 + 1;
        byte eventType = bArr[0];
        long connectionId = (long) readInt(bArr, pos);
        pos += 8;
        int statementId = readInt(bArr, pos);
        pos += 4;
        int resultSetId = readInt(bArr, pos);
        pos += 4;
        long eventCreationTime = readLong(bArr, pos);
        pos += 8;
        long eventDuration = readLong(bArr, pos);
        pos += 4;
        byte[] eventDurationUnits = readBytes(bArr, pos);
        pos += 4;
        if (eventDurationUnits != null) {
            pos += eventDurationUnits.length;
        }
        readInt(bArr, pos);
        pos += 4;
        byte[] eventCreationAsBytes = readBytes(bArr, pos);
        pos += 4;
        if (eventCreationAsBytes != null) {
            pos += eventCreationAsBytes.length;
        }
        byte[] message = readBytes(bArr, pos);
        pos += 4;
        if (message != null) {
            pos += message.length;
        }
        return new ProfilerEvent(eventType, "", "", connectionId, statementId, resultSetId, eventCreationTime, eventDuration, StringUtils.toString(eventDurationUnits, "ISO8859_1"), StringUtils.toString(eventCreationAsBytes, "ISO8859_1"), null, StringUtils.toString(message, "ISO8859_1"));
    }

    public byte[] pack() throws Exception {
        int len;
        byte[] durationUnitsAsBytes;
        byte[] eventCreationAsBytes = null;
        getEventCreationPointAsString();
        if (this.eventCreationPointDesc != null) {
            eventCreationAsBytes = StringUtils.getBytes(this.eventCreationPointDesc, "ISO8859_1");
            len = 29 + (eventCreationAsBytes.length + 4);
        } else {
            len = 29 + 4;
        }
        byte[] messageAsBytes = null;
        if (this.message != null) {
            messageAsBytes = StringUtils.getBytes(this.message, "ISO8859_1");
            len += messageAsBytes.length + 4;
        } else {
            len += 4;
        }
        if (this.durationUnits != null) {
            durationUnitsAsBytes = StringUtils.getBytes(this.durationUnits, "ISO8859_1");
            len += 4 + durationUnitsAsBytes.length;
        } else {
            len += 4;
            durationUnitsAsBytes = StringUtils.getBytes("", "ISO8859_1");
        }
        byte[] buf = new byte[len];
        int pos = 0 + 1;
        buf[0] = this.eventType;
        int pos2 = writeInt(this.eventCreationPointIndex, buf, writeBytes(durationUnitsAsBytes, buf, writeLong(this.eventDuration, buf, writeLong(this.eventCreationTime, buf, writeInt(this.resultSetId, buf, writeInt(this.statementId, buf, writeLong(this.connectionId, buf, pos)))))));
        if (eventCreationAsBytes != null) {
            pos2 = writeBytes(eventCreationAsBytes, buf, pos2);
        } else {
            pos2 = writeInt(0, buf, pos2);
        }
        if (messageAsBytes != null) {
            pos2 = writeBytes(messageAsBytes, buf, pos2);
        } else {
            pos2 = writeInt(0, buf, pos2);
        }
        return buf;
    }

    private static int writeInt(int i, byte[] buf, int pos) {
        int pos2 = pos + 1;
        buf[pos] = (byte) (i & 255);
        pos = pos2 + 1;
        buf[pos2] = (byte) (i >>> 8);
        pos2 = pos + 1;
        buf[pos] = (byte) (i >>> 16);
        pos = pos2 + 1;
        buf[pos2] = (byte) (i >>> 24);
        return pos;
    }

    private static int writeLong(long l, byte[] buf, int pos) {
        int pos2 = pos + 1;
        buf[pos] = (byte) ((int) (l & 255));
        pos = pos2 + 1;
        buf[pos2] = (byte) ((int) (l >>> 8));
        pos2 = pos + 1;
        buf[pos] = (byte) ((int) (l >>> 16));
        pos = pos2 + 1;
        buf[pos2] = (byte) ((int) (l >>> 24));
        pos2 = pos + 1;
        buf[pos] = (byte) ((int) (l >>> 32));
        pos = pos2 + 1;
        buf[pos2] = (byte) ((int) (l >>> 40));
        pos2 = pos + 1;
        buf[pos] = (byte) ((int) (l >>> 48));
        pos = pos2 + 1;
        buf[pos2] = (byte) ((int) (l >>> 56));
        return pos;
    }

    private static int writeBytes(byte[] msg, byte[] buf, int pos) {
        pos = writeInt(msg.length, buf, pos);
        System.arraycopy(msg, 0, buf, pos, msg.length);
        return msg.length + pos;
    }

    private static int readInt(byte[] buf, int pos) {
        int pos2 = pos + 1;
        int pos3 = pos2 + 1;
        pos = (buf[pos] & 255) | ((buf[pos2] & 255) << 8);
        pos2 = pos3 + 1;
        pos |= (buf[pos3] & 255) << 16;
        pos3 = pos2 + 1;
        return pos | ((buf[pos2] & 255) << 24);
    }

    private static long readLong(byte[] buf, int pos) {
        int pos2 = pos + 1;
        long j = (long) (buf[pos] & 255);
        pos = pos2 + 1;
        pos2 = pos + 1;
        pos = pos2 + 1;
        long j2 = ((j | (((long) (buf[pos2] & 255)) << 8)) | (((long) (buf[pos] & 255)) << 16)) | (((long) (buf[pos2] & 255)) << 24);
        pos2 = pos + 1;
        pos = pos2 + 1;
        j2 = (j2 | (((long) (buf[pos] & 255)) << 32)) | (((long) (buf[pos2] & 255)) << 40);
        pos2 = pos + 1;
        pos = pos2 + 1;
        return (j2 | (((long) (buf[pos] & 255)) << 48)) | (((long) (buf[pos2] & 255)) << 56);
    }

    private static byte[] readBytes(byte[] buf, int pos) {
        int length = readInt(buf, pos);
        byte[] msg = new byte[length];
        System.arraycopy(buf, pos + 4, msg, 0, length);
        return msg;
    }

    public String getCatalog() {
        return this.catalog;
    }

    public long getConnectionId() {
        return this.connectionId;
    }

    public long getEventCreationTime() {
        return this.eventCreationTime;
    }

    public long getEventDuration() {
        return this.eventDuration;
    }

    public String getDurationUnits() {
        return this.durationUnits;
    }

    public byte getEventType() {
        return this.eventType;
    }

    public int getResultSetId() {
        return this.resultSetId;
    }

    public int getStatementId() {
        return this.statementId;
    }

    public String getMessage() {
        return this.message;
    }
}
