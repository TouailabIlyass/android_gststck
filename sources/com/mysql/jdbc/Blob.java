package com.mysql.jdbc;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.sql.SQLException;

public class Blob implements java.sql.Blob, OutputStreamWatcher {
    private byte[] binaryData = null;
    private ExceptionInterceptor exceptionInterceptor;
    private boolean isClosed = false;

    Blob(ExceptionInterceptor exceptionInterceptor) {
        setBinaryData(Constants.EMPTY_BYTE_ARRAY);
        this.exceptionInterceptor = exceptionInterceptor;
    }

    Blob(byte[] data, ExceptionInterceptor exceptionInterceptor) {
        setBinaryData(data);
        this.exceptionInterceptor = exceptionInterceptor;
    }

    Blob(byte[] data, ResultSetInternalMethods creatorResultSetToSet, int columnIndexToSet) {
        setBinaryData(data);
    }

    private synchronized byte[] getBinaryData() {
        return this.binaryData;
    }

    public synchronized InputStream getBinaryStream() throws SQLException {
        checkClosed();
        return new ByteArrayInputStream(getBinaryData());
    }

    public synchronized byte[] getBytes(long pos, int length) throws SQLException {
        byte[] newData;
        checkClosed();
        if (pos < 1) {
            throw SQLError.createSQLException(Messages.getString("Blob.2"), SQLError.SQL_STATE_ILLEGAL_ARGUMENT, this.exceptionInterceptor);
        }
        long pos2 = pos - 1;
        if (pos2 > ((long) this.binaryData.length)) {
            throw SQLError.createSQLException("\"pos\" argument can not be larger than the BLOB's length.", SQLError.SQL_STATE_ILLEGAL_ARGUMENT, this.exceptionInterceptor);
        } else if (pos2 + ((long) length) > ((long) this.binaryData.length)) {
            throw SQLError.createSQLException("\"pos\" + \"length\" arguments can not be larger than the BLOB's length.", SQLError.SQL_STATE_ILLEGAL_ARGUMENT, this.exceptionInterceptor);
        } else {
            newData = new byte[length];
            System.arraycopy(getBinaryData(), (int) pos2, newData, 0, length);
        }
        return newData;
    }

    public synchronized long length() throws SQLException {
        checkClosed();
        return (long) getBinaryData().length;
    }

    public synchronized long position(byte[] pattern, long start) throws SQLException {
        throw SQLError.createSQLException("Not implemented", this.exceptionInterceptor);
    }

    public synchronized long position(java.sql.Blob pattern, long start) throws SQLException {
        checkClosed();
        return position(pattern.getBytes(0, (int) pattern.length()), start);
    }

    private synchronized void setBinaryData(byte[] newBinaryData) {
        this.binaryData = newBinaryData;
    }

    public synchronized OutputStream setBinaryStream(long indexToWriteAt) throws SQLException {
        WatchableOutputStream bytesOut;
        checkClosed();
        if (indexToWriteAt < 1) {
            throw SQLError.createSQLException(Messages.getString("Blob.0"), SQLError.SQL_STATE_ILLEGAL_ARGUMENT, this.exceptionInterceptor);
        }
        bytesOut = new WatchableOutputStream();
        bytesOut.setWatcher(this);
        if (indexToWriteAt > 0) {
            bytesOut.write(this.binaryData, 0, (int) (indexToWriteAt - 1));
        }
        return bytesOut;
    }

    public synchronized int setBytes(long writeAt, byte[] bytes) throws SQLException {
        checkClosed();
        return setBytes(writeAt, bytes, 0, bytes.length);
    }

    public synchronized int setBytes(long writeAt, byte[] bytes, int offset, int length) throws SQLException {
        checkClosed();
        OutputStream bytesOut = setBinaryStream(writeAt);
        try {
            bytesOut.write(bytes, offset, length);
            try {
                bytesOut.close();
            } catch (IOException e) {
            }
        } catch (IOException ioEx) {
            SQLException sqlEx = SQLError.createSQLException(Messages.getString("Blob.1"), SQLError.SQL_STATE_GENERAL_ERROR, this.exceptionInterceptor);
            sqlEx.initCause(ioEx);
            throw sqlEx;
        } catch (Throwable th) {
            try {
                bytesOut.close();
            } catch (IOException e2) {
            }
        }
        return length;
    }

    public synchronized void streamClosed(byte[] byteData) {
        this.binaryData = byteData;
    }

    public synchronized void streamClosed(WatchableOutputStream out) {
        int streamSize = out.size();
        if (streamSize < this.binaryData.length) {
            out.write(this.binaryData, streamSize, this.binaryData.length - streamSize);
        }
        this.binaryData = out.toByteArray();
    }

    public synchronized void truncate(long len) throws SQLException {
        checkClosed();
        if (len < 0) {
            throw SQLError.createSQLException("\"len\" argument can not be < 1.", SQLError.SQL_STATE_ILLEGAL_ARGUMENT, this.exceptionInterceptor);
        } else if (len > ((long) this.binaryData.length)) {
            throw SQLError.createSQLException("\"len\" argument can not be larger than the BLOB's length.", SQLError.SQL_STATE_ILLEGAL_ARGUMENT, this.exceptionInterceptor);
        } else {
            byte[] newData = new byte[((int) len)];
            System.arraycopy(getBinaryData(), 0, newData, 0, (int) len);
            this.binaryData = newData;
        }
    }

    public synchronized void free() throws SQLException {
        this.binaryData = null;
        this.isClosed = true;
    }

    public synchronized InputStream getBinaryStream(long pos, long length) throws SQLException {
        long pos2;
        checkClosed();
        if (pos < 1) {
            throw SQLError.createSQLException("\"pos\" argument can not be < 1.", SQLError.SQL_STATE_ILLEGAL_ARGUMENT, this.exceptionInterceptor);
        }
        pos2 = pos - 1;
        if (pos2 > ((long) this.binaryData.length)) {
            throw SQLError.createSQLException("\"pos\" argument can not be larger than the BLOB's length.", SQLError.SQL_STATE_ILLEGAL_ARGUMENT, this.exceptionInterceptor);
        } else if (pos2 + length > ((long) this.binaryData.length)) {
            throw SQLError.createSQLException("\"pos\" + \"length\" arguments can not be larger than the BLOB's length.", SQLError.SQL_STATE_ILLEGAL_ARGUMENT, this.exceptionInterceptor);
        }
        return new ByteArrayInputStream(getBinaryData(), (int) pos2, (int) length);
    }

    private synchronized void checkClosed() throws SQLException {
        if (this.isClosed) {
            throw SQLError.createSQLException("Invalid operation on closed BLOB", SQLError.SQL_STATE_ILLEGAL_ARGUMENT, this.exceptionInterceptor);
        }
    }
}
