package com.mysql.jdbc;

import android.support.v4.view.InputDeviceCompat;
import com.mysql.jdbc.log.Log;
import com.mysql.jdbc.log.NullLogger;
import java.io.EOFException;
import java.io.IOException;
import java.io.InputStream;
import java.sql.SQLException;
import java.util.zip.DataFormatException;
import java.util.zip.Inflater;

class CompressedInputStream extends InputStream {
    private byte[] buffer;
    private InputStream in;
    private Inflater inflater;
    private Log log;
    private byte[] packetHeaderBuffer = new byte[7];
    private int pos = 0;
    private BooleanConnectionProperty traceProtocol;

    public CompressedInputStream(Connection conn, InputStream streamFromServer) {
        this.traceProtocol = ((ConnectionPropertiesImpl) conn).traceProtocol;
        try {
            this.log = conn.getLog();
        } catch (SQLException e) {
            this.log = new NullLogger(null);
        }
        this.in = streamFromServer;
        this.inflater = new Inflater();
    }

    public int available() throws IOException {
        if (this.buffer == null) {
            return this.in.available();
        }
        return (this.buffer.length - this.pos) + this.in.available();
    }

    public void close() throws IOException {
        this.in.close();
        this.buffer = null;
        this.inflater.end();
        this.inflater = null;
        this.traceProtocol = null;
        this.log = null;
    }

    private void getNextPacketFromServer() throws IOException {
        if (readFully(this.packetHeaderBuffer, 0, 7) < 7) {
            throw new IOException("Unexpected end of input stream");
        }
        byte[] uncompressedData;
        int compressedPacketLength = ((this.packetHeaderBuffer[0] & 255) + ((this.packetHeaderBuffer[1] & 255) << 8)) + ((this.packetHeaderBuffer[2] & 255) << 16);
        int uncompressedLength = ((this.packetHeaderBuffer[4] & 255) + ((this.packetHeaderBuffer[5] & 255) << 8)) + ((this.packetHeaderBuffer[6] & 255) << 16);
        boolean doTrace = this.traceProtocol.getValueAsBoolean();
        if (doTrace) {
            Log log = this.log;
            StringBuilder stringBuilder = new StringBuilder();
            stringBuilder.append("Reading compressed packet of length ");
            stringBuilder.append(compressedPacketLength);
            stringBuilder.append(" uncompressed to ");
            stringBuilder.append(uncompressedLength);
            log.logTrace(stringBuilder.toString());
        }
        if (uncompressedLength > 0) {
            uncompressedData = new byte[uncompressedLength];
            byte[] compressedBuffer = new byte[compressedPacketLength];
            readFully(compressedBuffer, 0, compressedPacketLength);
            this.inflater.reset();
            this.inflater.setInput(compressedBuffer);
            try {
                this.inflater.inflate(uncompressedData);
            } catch (DataFormatException e) {
                throw new IOException("Error while uncompressing packet from server.");
            }
        }
        if (doTrace) {
            this.log.logTrace("Packet didn't meet compression threshold, not uncompressing...");
        }
        uncompressedLength = compressedPacketLength;
        uncompressedData = new byte[uncompressedLength];
        readFully(uncompressedData, 0, uncompressedLength);
        if (doTrace) {
            if (uncompressedLength > 1024) {
                log = this.log;
                stringBuilder = new StringBuilder();
                stringBuilder.append("Uncompressed packet: \n");
                stringBuilder.append(StringUtils.dumpAsHex(uncompressedData, 256));
                log.logTrace(stringBuilder.toString());
                compressedBuffer = new byte[256];
                System.arraycopy(uncompressedData, uncompressedLength + InputDeviceCompat.SOURCE_ANY, compressedBuffer, 0, 256);
                Log log2 = this.log;
                StringBuilder stringBuilder2 = new StringBuilder();
                stringBuilder2.append("Uncompressed packet: \n");
                stringBuilder2.append(StringUtils.dumpAsHex(compressedBuffer, 256));
                log2.logTrace(stringBuilder2.toString());
                this.log.logTrace("Large packet dump truncated. Showing first and last 256 bytes.");
            } else {
                log = this.log;
                stringBuilder = new StringBuilder();
                stringBuilder.append("Uncompressed packet: \n");
                stringBuilder.append(StringUtils.dumpAsHex(uncompressedData, uncompressedLength));
                log.logTrace(stringBuilder.toString());
            }
        }
        if (this.buffer != null && this.pos < this.buffer.length) {
            if (doTrace) {
                this.log.logTrace("Combining remaining packet with new: ");
            }
            int remaining = this.buffer.length - this.pos;
            byte[] newBuffer = new byte[(uncompressedData.length + remaining)];
            System.arraycopy(this.buffer, this.pos, newBuffer, 0, remaining);
            System.arraycopy(uncompressedData, 0, newBuffer, remaining, uncompressedData.length);
            uncompressedData = newBuffer;
        }
        this.pos = 0;
        this.buffer = uncompressedData;
    }

    private void getNextPacketIfRequired(int numBytes) throws IOException {
        if (this.buffer == null || this.pos + numBytes > this.buffer.length) {
            getNextPacketFromServer();
        }
    }

    public int read() throws IOException {
        try {
            getNextPacketIfRequired(1);
            byte[] bArr = this.buffer;
            int i = this.pos;
            this.pos = i + 1;
            return bArr[i] & 255;
        } catch (IOException e) {
            return -1;
        }
    }

    public int read(byte[] b) throws IOException {
        return read(b, 0, b.length);
    }

    public int read(byte[] b, int off, int len) throws IOException {
        if (b == null) {
            throw new NullPointerException();
        }
        if (off >= 0 && off <= b.length && len >= 0 && off + len <= b.length) {
            if (off + len >= 0) {
                if (len <= 0) {
                    return 0;
                }
                try {
                    getNextPacketIfRequired(len);
                    int consummedBytesLength = Math.min(this.buffer.length - this.pos, len);
                    System.arraycopy(this.buffer, this.pos, b, off, consummedBytesLength);
                    this.pos += consummedBytesLength;
                    return consummedBytesLength;
                } catch (IOException e) {
                    return -1;
                }
            }
        }
        throw new IndexOutOfBoundsException();
    }

    private final int readFully(byte[] b, int off, int len) throws IOException {
        if (len < 0) {
            throw new IndexOutOfBoundsException();
        }
        int n = 0;
        while (n < len) {
            int count = this.in.read(b, off + n, len - n);
            if (count < 0) {
                throw new EOFException();
            }
            n += count;
        }
        return n;
    }

    public long skip(long n) throws IOException {
        long count = 0;
        long i = 0;
        while (i < n) {
            if (read() == -1) {
                break;
            }
            i++;
            count++;
        }
        return count;
    }
}
